package com.jj.dev.myapplication.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.GenericTypeIndicator
import com.jj.dev.myapplication.Model.Vehicle
import com.jj.dev.myapplication.repository.AuthenticationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject







/*sealed class StartupState {
    object Loading : StartupState()
    object Unauthenticated : StartupState()
    data class Authenticated(val role: String) : StartupState()
    data class Error(val message: String) : StartupState()
}

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: FirebaseUser, val role: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

sealed class BookingStatus {
    object Idle : BookingStatus()
    object Loading : BookingStatus()
    object Success : BookingStatus()
    data class Error(val message: String) : BookingStatus()
}


@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val authRepository: AuthenticationRepository
) : ViewModel() {
    private val _startupState = MutableLiveData<StartupState>(StartupState.Loading)
    val startupState: LiveData<StartupState> = _startupState

    private val _loginState = MutableLiveData<LoginUiState>(LoginUiState.Idle)
    val loginState: LiveData<LoginUiState> = _loginState

    private val _registerResult = MutableLiveData<Boolean?>()
    val registerResult: LiveData<Boolean?> = _registerResult

    private val _logoutResult = MutableLiveData<Boolean>()
    val logoutResult: LiveData<Boolean> = _logoutResult

    private val _userRole = MutableLiveData<String?>()
    val userRole: LiveData<String?> = _userRole

    private val _customerVehicles = MutableLiveData<List<Vehicle>?>(null)
    val customerVehicles: LiveData<List<Vehicle>?> = _customerVehicles

    private val _bookingStatus = MutableLiveData<BookingStatus>(BookingStatus.Idle)
    val bookingStatus: LiveData<BookingStatus> = _bookingStatus

    companion object {
        private const val TAG = "AuthViewModel"
    }

    init {
        bootstrapUser()
    }

    private fun parseAndSetVehicles(dataSnapshot: DataSnapshot) {
        val vehiclesList = mutableListOf<Vehicle>()
        if (dataSnapshot.hasChild("vehicles")) {
            val vehiclesSnapshot = dataSnapshot.child("vehicles")
            try {
                val typeIndicator = object : GenericTypeIndicator<List<@JvmSuppressWildcards Vehicle>>() {}
                val parsedVehicles = vehiclesSnapshot.getValue(typeIndicator)
                if (parsedVehicles != null) {
                    vehiclesList.addAll(parsedVehicles.filterNotNull())
                } else {
                    for (vehicleSnap in vehiclesSnapshot.children) {
                        vehicleSnap.getValue(Vehicle::class.java)?.let { vehiclesList.add(it) }
                    }
                }
                _customerVehicles.value = vehiclesList
                Log.d(TAG, "Vehicles loaded: ${vehiclesList.size} vehicles.")
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing vehicles: ${e.localizedMessage}", e)
                _customerVehicles.value = emptyList()
            }
        } else {
            Log.d(TAG, "No 'vehicles' node found for customer.")
            _customerVehicles.value = emptyList()
        }
    }

    private fun bootstrapUser() {
        val user = authRepository.getCurrentUser()
        if (user == null) {
            _startupState.value = StartupState.Unauthenticated; _customerVehicles.value = null; return
        }
        _startupState.value = StartupState.Loading
        authRepository.fetchUserRole()
            .addOnSuccessListener { snap ->
                val role = snap.child("role").getValue(String::class.java) ?: "Customer"
                _userRole.value = role; _startupState.value = StartupState.Authenticated(role)
                if (role == "Customer") parseAndSetVehicles(snap) else _customerVehicles.value = null
            }
            .addOnFailureListener { ex ->
                _startupState.value = StartupState.Error(ex.localizedMessage ?: "Unknown error fetching role")
                _customerVehicles.value = null; Log.e(TAG, "Bootstrap failed: ${ex.message}", ex)
            }
    }

    fun login(email: String, password: String) {
        _loginState.value = LoginUiState.Loading
        authRepository.login(email, password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    _loginState.value = LoginUiState.Error(task.exception?.localizedMessage ?: "Unknown login error")
                    _customerVehicles.value = null; return@addOnCompleteListener
                }
                val user = authRepository.getCurrentUser()
                if (user == null) {
                    _loginState.value = LoginUiState.Error("Login successful but user data not found.")
                    _customerVehicles.value = null; return@addOnCompleteListener
                }
                authRepository.fetchUserRole()
                    .addOnSuccessListener { snap ->
                        val role = snap.child("role").getValue(String::class.java) ?: "Customer"
                        _userRole.value = role; _loginState.value = LoginUiState.Success(user, role)
                        if (role == "Customer") parseAndSetVehicles(snap) else _customerVehicles.value = null
                    }
                    .addOnFailureListener { ex ->
                        _userRole.value = null; _loginState.value = LoginUiState.Success(user, "Customer")
                        _customerVehicles.value = null; Log.e(TAG, "Failed to fetch user role after login: ${ex.message}", ex)
                    }
            }
    }

    fun register(email: String, password: String, name: String, role: String, contact: String?, specialization: String?) {
        _registerResult.value = null
        authRepository.register(email, password, name, role, contact, specialization)
            .addOnCompleteListener { task ->
                _registerResult.value = task.isSuccessful
                if (task.isSuccessful) {
                    _userRole.value = role
                    if (role == "Customer") {
                        authRepository.fetchUserRole().addOnSuccessListener { parseAndSetVehicles(it) }
                            .addOnFailureListener{ Log.e(TAG, "Failed to fetch vehicles post-reg: ${it.message}", it); _customerVehicles.value = emptyList()}
                    } else { _customerVehicles.value = null }
                } else { _customerVehicles.value = null }
            }
    }

    fun bookNewAppointment(
        date: String,
        description: String,
        selectedVehicle: Vehicle // We still need selectedVehicle to get its ID
    ) {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            _bookingStatus.value = BookingStatus.Error("User not logged in."); return
        }
        if (selectedVehicle.vehicleId.isBlank()) {
            _bookingStatus.value = BookingStatus.Error("Invalid vehicle selected."); return
        }

        _bookingStatus.value = BookingStatus.Loading

        val appointment = Appointment(
            userId = currentUser.uid,
            date = date,
            description = description,
            vehicleId = selectedVehicle.vehicleId, // Only vehicleId is needed now
            // vehicleModel and vehiclePlate are removed
            serviceType = null,
            appointmentStatus = AppointmentStatusEnum.Booked,
            timeSlotId = null,
            mechanicId = null
        )

        authRepository.saveAppointment(appointment, selectedVehicle) // Pass selectedVehicle for history update
            .addOnSuccessListener {
                _bookingStatus.value = BookingStatus.Success
                refreshCustomerData() // Refresh to get updated service history (if it changes locally)
                Log.d(TAG, "Appointment booked successfully (renormalized).")
            }
            .addOnFailureListener { exception ->
                _bookingStatus.value = BookingStatus.Error(exception.localizedMessage ?: "Failed to book appointment.")
                Log.e(TAG, "Appointment booking failed", exception)
            }
    }

    fun resetBookingStatus() {
        _bookingStatus.value = BookingStatus.Idle
    }

    fun logout() {
        viewModelScope.launch {
            val result = authRepository.logout()
            _logoutResult.value = result
            if (result) {
                _userRole.value = null; _loginState.value = LoginUiState.Idle
                _startupState.value = StartupState.Unauthenticated; _customerVehicles.value = null
            }
        }
    }

    fun refreshCustomerData() {
        val user = authRepository.getCurrentUser()
        if (user != null && _userRole.value == "Customer") {
            Log.d(TAG, "Refreshing customer data including vehicles...")
            authRepository.fetchUserRole().addOnSuccessListener { snap -> parseAndSetVehicles(snap) }
                .addOnFailureListener { ex ->
                    Log.e(TAG, "Error refreshing customer data: ${ex.localizedMessage}", ex)
                    _customerVehicles.value = emptyList()
                }
        }
    }
}*/





// StartupState and LoginUiState remain the same
sealed class StartupState {
    object Loading : StartupState(); object Unauthenticated : StartupState()
    data class Authenticated(val firebaseUser: FirebaseUser, val role: String) : StartupState() // Include FirebaseUser
    data class Error(val message: String) : StartupState()
}
sealed class LoginUiState {
    object Idle : LoginUiState(); object Loading : LoginUiState()
    data class Success(val firebaseUser: FirebaseUser, val role: String) : LoginUiState() // Include FirebaseUser
    data class Error(val message: String) : LoginUiState()
}

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val authRepository: AuthenticationRepository
    // UserViewModel will be injected where detailed profile setup is needed post-registration
) : ViewModel() {
    private val _startupState = MutableLiveData<StartupState>(StartupState.Loading)
    val startupState: LiveData<StartupState> = _startupState

    private val _loginState = MutableLiveData<LoginUiState>(LoginUiState.Idle)
    val loginState: LiveData<LoginUiState> = _loginState

    private val _registerResult = MutableLiveData<Pair<Boolean, FirebaseUser?>>() // Boolean for success, FirebaseUser for UID
    val registerResult: LiveData<Pair<Boolean, FirebaseUser?>> = _registerResult


    private val _logoutResult = MutableLiveData<Boolean>()
    val logoutResult: LiveData<Boolean> = _logoutResult

    // Exposes the current Firebase user if authenticated
    private val _currentFirebaseUser = MutableLiveData<FirebaseUser?>()
    val currentFirebaseUser: LiveData<FirebaseUser?> = _currentFirebaseUser


    companion object { private const val TAG = "AuthViewModel" }

    init {
        bootstrapUser()
    }

    private fun bootstrapUser() {
        val user = authRepository.getCurrentUser()
        if (user == null) {
            _startupState.value = StartupState.Unauthenticated
            _currentFirebaseUser.value = null
            return
        }
        _currentFirebaseUser.value = user
        _startupState.value = StartupState.Loading // Indicate loading for user data
        authRepository.fetchBasicUserDocument(user.uid)
            .addOnSuccessListener { snap ->
                val role = snap.child("role").getValue(String::class.java) ?: "Unknown"
                _startupState.value = StartupState.Authenticated(user, role)
                Log.d(TAG, "Bootstrap successful. User: ${user.uid}, Role: $role")
            }
            .addOnFailureListener { ex ->
                _startupState.value = StartupState.Error(ex.localizedMessage ?: "Error fetching basic user data.")
                Log.e(TAG, "Bootstrap failed: ${ex.message}", ex)
            }
    }

    fun login(email: String, password: String) {
        _loginState.value = LoginUiState.Loading
        authRepository.login(email, password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    _loginState.value = LoginUiState.Error(task.exception?.localizedMessage ?: "Unknown login error.")
                    return@addOnCompleteListener
                }
                val user = authRepository.getCurrentUser()
                if (user == null) {
                    _loginState.value = LoginUiState.Error("Login successful but user data not found.")
                    return@addOnCompleteListener
                }
                _currentFirebaseUser.value = user
                authRepository.fetchBasicUserDocument(user.uid)
                    .addOnSuccessListener { snap ->
                        val role = snap.child("role").getValue(String::class.java) ?: "Unknown"
                        _loginState.value = LoginUiState.Success(user, role)
                        Log.d(TAG, "Login successful. User: ${user.uid}, Role: $role")
                    }
                    .addOnFailureListener { ex ->
                        _loginState.value = LoginUiState.Error(ex.localizedMessage ?: "Failed to fetch role post-login.")
                        Log.e(TAG, "Failed to fetch user role after login: ${ex.message}", ex)
                    }
            }
    }

    // Handles only the auth part + basic user node.
    // Detailed profile creation (like adding dummy vehicle) will be triggered separately.
    fun register(email: String, password: String, name: String, role: String) {
        _registerResult.value = Pair(false, null) // Reset
        authRepository.register(email, password, name, role)
            .addOnCompleteListener { task ->
                _registerResult.value = Pair(task.isSuccessful, authRepository.getCurrentUser())
                if (task.isSuccessful) {
                    Log.d(TAG, "Auth registration successful for ${authRepository.getCurrentUser()?.uid}")
                    // The UI (e.g., RegisterView) will observe this and, if role is Customer,
                    // then call the UserViewModel to create the detailed customer profile.
                } else {
                    Log.e(TAG, "Auth registration failed.", task.exception)
                }
            }
    }

    fun logout() {
        viewModelScope.launch {
            val result = authRepository.logout()
            _logoutResult.value = result
            if (result) {
                _loginState.value = LoginUiState.Idle
                _startupState.value = StartupState.Unauthenticated
                _currentFirebaseUser.value = null
                Log.d(TAG, "Logout successful.")
            }
        }
    }
}
