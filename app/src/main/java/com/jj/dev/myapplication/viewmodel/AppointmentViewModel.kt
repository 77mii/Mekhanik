package com.jj.dev.myapplication.viewmodel





/*
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jj.dev.myapplication.Model.Appointment
import com.jj.dev.myapplication.Model.Vehicle
import com.jj.dev.myapplication.Model.AppointmentStatusEnum
import com.jj.dev.myapplication.repository.AppointmentRepository
import com.jj.dev.myapplication.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject



sealed class BookingUiStatus {
    object Idle : BookingUiStatus()
    object Loading : BookingUiStatus()
    object Success : BookingUiStatus()
    // Modified Error to include an optional conflicting appointment
    data class Error(val message: String, val conflictingAppointment: Appointment? = null) : BookingUiStatus()
}

sealed class AppointmentsListState {
    object Idle : AppointmentsListState()
    object Loading : AppointmentsListState()
    data class Loaded(val appointments: List<Appointment>) : AppointmentsListState()
    data class Error(val message: String) : AppointmentsListState()
    object Empty : AppointmentsListState()
}

// New state for loading a single appointment detail
sealed class SingleAppointmentLoadState {
    object Idle : SingleAppointmentLoadState()
    object Loading : SingleAppointmentLoadState()
    data class Success(val appointment: Appointment) : SingleAppointmentLoadState() // Not strictly needed if _selectedAppointment is the source of truth
    data class Error(val message: String) : SingleAppointmentLoadState()
    object NotFound : SingleAppointmentLoadState()
}


@HiltViewModel
class AppointmentViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _bookingUiStatus = MutableLiveData<BookingUiStatus>(BookingUiStatus.Idle)
    val bookingUiStatus: LiveData<BookingUiStatus> = _bookingUiStatus

    private val _customerAppointmentsState = MutableLiveData<AppointmentsListState>(AppointmentsListState.Idle)
    val customerAppointmentsState: LiveData<AppointmentsListState> = _customerAppointmentsState

    private val _selectedAppointment = MutableLiveData<Appointment?>()
    val selectedAppointment: LiveData<Appointment?> = _selectedAppointment

    // Optional: State for the specific loading operation of a single appointment detail
    private val _singleAppointmentLoadState = MutableLiveData<SingleAppointmentLoadState>(SingleAppointmentLoadState.Idle)
    val singleAppointmentLoadState: LiveData<SingleAppointmentLoadState> = _singleAppointmentLoadState


    companion object { private const val TAG = "AppointmentViewModel" }

    fun loadCustomerAppointments(userId: String) {
        if (userId.isBlank()) {
            _customerAppointmentsState.value = AppointmentsListState.Error("User ID is missing to fetch appointments.")
            return
        }
        if (_customerAppointmentsState.value is AppointmentsListState.Loading) {
            Log.d(TAG, "Appointments already loading for user: $userId. Skipping redundant load.")
            return
        }
        _customerAppointmentsState.value = AppointmentsListState.Loading
        Log.d(TAG, "Loading appointments for user: $userId")
        appointmentRepository.fetchAppointmentsByUserId(userId)
            .addOnSuccessListener { dataSnapshot ->
                val appointmentsList = mutableListOf<Appointment>()
                for (snap in dataSnapshot.children) {
                    snap.getValue(Appointment::class.java)?.let { appointmentsList.add(it) }
                }
                if (appointmentsList.isEmpty()) {
                    _customerAppointmentsState.value = AppointmentsListState.Empty
                } else {
                    val sortedAppointments = appointmentsList.sortedByDescending { it.date }
                    _customerAppointmentsState.value = AppointmentsListState.Loaded(sortedAppointments)
                }
                Log.d(TAG, "Loaded ${appointmentsList.size} appointments for user: $userId")
            }
            .addOnFailureListener { exception ->
                _customerAppointmentsState.value = AppointmentsListState.Error(exception.localizedMessage ?: "Failed to load appointments.")
                Log.e(TAG, "Error loading appointments for user $userId", exception)
            }
    }

    fun loadSelectedAppointmentDetails(appointmentId: String) {
        if (appointmentId.isBlank()) {
            Log.w(TAG, "loadSelectedAppointmentDetails called with blank ID.")
            _singleAppointmentLoadState.value = SingleAppointmentLoadState.Error("Appointment ID is missing.")
            _selectedAppointment.value = null // Ensure it's cleared if ID is invalid
            return
        }

        // If the currently selected appointment in LiveData already matches, no need to fetch
        if (_selectedAppointment.value?.appointmentId == appointmentId) {
            Log.d(TAG, "Appointment $appointmentId already selected and loaded in ViewModel.")
            _singleAppointmentLoadState.value = SingleAppointmentLoadState.Success(_selectedAppointment.value!!) // It's already there
            return
        }

        Log.d(TAG, "Loading details for appointment ID: $appointmentId")
        _singleAppointmentLoadState.value = SingleAppointmentLoadState.Loading
        _selectedAppointment.value = null // Clear previous selection while loading new one

        appointmentRepository.fetchSingleAppointmentById(appointmentId)
            .addOnSuccessListener { snapshot ->
                val appointment = snapshot.getValue(Appointment::class.java)
                if (appointment != null) {
                    _selectedAppointment.value = appointment
                    _singleAppointmentLoadState.value = SingleAppointmentLoadState.Success(appointment)
                    Log.d(TAG, "Successfully loaded details for appointment: ${appointment.appointmentId}")
                } else {
                    _selectedAppointment.value = null
                    _singleAppointmentLoadState.value = SingleAppointmentLoadState.NotFound
                    Log.w(TAG, "Appointment with ID $appointmentId not found.")
                }
            }
            .addOnFailureListener { exception ->
                _selectedAppointment.value = null
                _singleAppointmentLoadState.value = SingleAppointmentLoadState.Error(exception.localizedMessage ?: "Failed to load appointment details.")
                Log.e(TAG, "Error loading appointment details for ID $appointmentId", exception)
            }
    }




    fun bookNewAppointment(
        userId: String,
        date: String,
        description: String,
        selectedVehicle: Vehicle
    ) {
        if (userId.isBlank()) {
            _bookingUiStatus.value = BookingUiStatus.Error("User ID is missing."); return
        }
        if (selectedVehicle.vehicleId.isBlank()) {
            _bookingUiStatus.value = BookingUiStatus.Error("Invalid vehicle selected."); return
        }
        _bookingUiStatus.value = BookingUiStatus.Loading
        appointmentRepository.fetchAppointmentsByVehicleId(selectedVehicle.vehicleId)
            .addOnSuccessListener { appointmentsSnapshot ->
                val existingAppointmentsForVehicle = mutableListOf<Appointment>()
                for (snap in appointmentsSnapshot.children) {
                    snap.getValue(Appointment::class.java)?.let { existingAppointmentsForVehicle.add(it) }
                }
                val conflictingAppointment = existingAppointmentsForVehicle.find { appt ->
                    (appt.appointmentStatus == AppointmentStatusEnum.Booked || appt.appointmentStatus == AppointmentStatusEnum.InProgress)
                }

                if (conflictingAppointment != null) {
                    Log.w(TAG, "Booking conflict: Vehicle ${selectedVehicle.vehicleId} already has an active appointment (ID: ${conflictingAppointment.appointmentId}, Status: ${conflictingAppointment.appointmentStatus}).")
                    // Set error state with the conflicting appointment
                    _bookingUiStatus.value = BookingUiStatus.Error(
                        message = "This vehicle already has a 'Booked' or 'In Progress' appointment.",
                        conflictingAppointment = conflictingAppointment
                    )
                    return@addOnSuccessListener
                }

                Log.d(TAG, "No booking conflict found for vehicle ${selectedVehicle.vehicleId}. Proceeding to save.")
                val newAppointment = Appointment(userId = userId, date = date, description = description, vehicleId = selectedVehicle.vehicleId, serviceType = null, appointmentStatus = AppointmentStatusEnum.Booked)
                appointmentRepository.saveAppointment(newAppointment)
                    .addOnSuccessListener {
                        customerRepository.addAppointmentToVehicleHistory(userId, selectedVehicle.vehicleId, newAppointment)
                            .addOnSuccessListener {
                                _bookingUiStatus.value = BookingUiStatus.Success
                                loadCustomerAppointments(userId) // Refresh the list
                            }.addOnFailureListener { e -> _bookingUiStatus.value = BookingUiStatus.Error("Appointment saved, but failed to update vehicle history: ${e.localizedMessage}") }
                    }.addOnFailureListener { exception -> _bookingUiStatus.value = BookingUiStatus.Error(exception.localizedMessage ?: "Failed to book new appointment.") }
            }.addOnFailureListener { exception -> _bookingUiStatus.value = BookingUiStatus.Error(exception.localizedMessage ?: "Failed to check for conflicts.") }
    }



    fun resetBookingStatus() { _bookingUiStatus.value = BookingUiStatus.Idle }
    fun clearAppointmentsListState() { _customerAppointmentsState.value = AppointmentsListState.Idle }
    fun selectAppointment(appointment: Appointment) {
        _selectedAppointment.value = appointment
        _singleAppointmentLoadState.value = SingleAppointmentLoadState.Success(appointment) // Reflect that it's now "loaded" into selection
        Log.d(TAG, "Selected appointment for detail view (via click): ${appointment.appointmentId}")
    }
    fun clearSelectedAppointment() {
        _selectedAppointment.value = null
        _singleAppointmentLoadState.value = SingleAppointmentLoadState.Idle // Reset load state too
        Log.d(TAG, "Cleared selected appointment.")
    }
}*/

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jj.dev.myapplication.Model.Appointment
import com.jj.dev.myapplication.Model.Vehicle
import com.jj.dev.myapplication.Model.AppointmentStatusEnum
import com.jj.dev.myapplication.repository.AppointmentRepository
import com.jj.dev.myapplication.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
// Removed Application import as it's no longer used here after notification rollback

sealed class BookingUiStatus {
    object Idle : BookingUiStatus()
    object Loading : BookingUiStatus()
    data class Success(val newAppointment: Appointment) : BookingUiStatus() // Now holds the appointment
    data class Error(val message: String, val conflictingAppointment: Appointment? = null) : BookingUiStatus()
}

sealed class AppointmentsListState {
    object Idle : AppointmentsListState(); object Loading : AppointmentsListState()
    data class Loaded(val appointments: List<Appointment>) : AppointmentsListState()
    data class Error(val message: String) : AppointmentsListState(); object Empty : AppointmentsListState()
}

sealed class SingleAppointmentLoadState {
    object Idle : SingleAppointmentLoadState(); object Loading : SingleAppointmentLoadState()
    data class Success(val appointment: Appointment) : SingleAppointmentLoadState()
    data class Error(val message: String) : SingleAppointmentLoadState(); object NotFound : SingleAppointmentLoadState()
}


@HiltViewModel
class AppointmentViewModel @Inject constructor(
    // Application context removed
    private val appointmentRepository: AppointmentRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _bookingUiStatus = MutableLiveData<BookingUiStatus>(BookingUiStatus.Idle)
    val bookingUiStatus: LiveData<BookingUiStatus> = _bookingUiStatus

    private val _customerAppointmentsState = MutableLiveData<AppointmentsListState>(AppointmentsListState.Idle)
    val customerAppointmentsState: LiveData<AppointmentsListState> = _customerAppointmentsState

    private val _selectedAppointment = MutableLiveData<Appointment?>()
    val selectedAppointment: LiveData<Appointment?> = _selectedAppointment

    private val _singleAppointmentLoadState = MutableLiveData<SingleAppointmentLoadState>(SingleAppointmentLoadState.Idle)
    val singleAppointmentLoadState: LiveData<SingleAppointmentLoadState> = _singleAppointmentLoadState

    private val _allAppointmentsState = MutableLiveData<AppointmentsListState>(AppointmentsListState.Idle)
    val allAppointmentsState: LiveData<AppointmentsListState> = _allAppointmentsState

    companion object { private const val TAG = "AppointmentViewModel" }

    fun loadCustomerAppointments(userId: String) {
        if (userId.isBlank()) {
            _customerAppointmentsState.value = AppointmentsListState.Error("User ID is missing to fetch appointments.")
            return
        }
        if (_customerAppointmentsState.value is AppointmentsListState.Loading) {
            return
        }
        _customerAppointmentsState.value = AppointmentsListState.Loading
        appointmentRepository.fetchAppointmentsByUserId(userId)
            .addOnSuccessListener { dataSnapshot ->
                val appointmentsList = mutableListOf<Appointment>()
                for (snap in dataSnapshot.children) {
                    snap.getValue(Appointment::class.java)?.let { appointmentsList.add(it) }
                }
                if (appointmentsList.isEmpty()) {
                    _customerAppointmentsState.value = AppointmentsListState.Empty
                } else {
                    _customerAppointmentsState.value = AppointmentsListState.Loaded(appointmentsList.sortedByDescending { it.date })
                }
            }
            .addOnFailureListener { exception ->
                _customerAppointmentsState.value = AppointmentsListState.Error(exception.localizedMessage ?: "Failed to load appointments.")
            }
    }

    fun loadAllAppointments() {
        if (_allAppointmentsState.value is AppointmentsListState.Loading) return
        _allAppointmentsState.value = AppointmentsListState.Loading
        Log.d(TAG, "Loading all appointments for Admin.")
        appointmentRepository.fetchAllAppointments()
            .addOnSuccessListener { dataSnapshot ->
                val list = mutableListOf<Appointment>()
                dataSnapshot.children.mapNotNullTo(list) { it.getValue(Appointment::class.java) }
                _allAppointmentsState.value = if (list.isEmpty()) AppointmentsListState.Empty else AppointmentsListState.Loaded(list.sortedByDescending { it.date })
                Log.d(TAG, "Loaded ${list.size} total appointments.")
            }
            .addOnFailureListener { e ->
                _allAppointmentsState.value = AppointmentsListState.Error(e.localizedMessage ?: "Failed to load all appointments.")
                Log.e(TAG, "Error loading all appointments", e)
            }
    }

    fun loadSelectedAppointmentDetails(appointmentId: String) {
        if (appointmentId.isBlank()) {
            _singleAppointmentLoadState.value = SingleAppointmentLoadState.Error("Appointment ID is missing.")
            _selectedAppointment.value = null; return
        }
        if (_selectedAppointment.value?.appointmentId == appointmentId) {
            _singleAppointmentLoadState.value = SingleAppointmentLoadState.Success(_selectedAppointment.value!!); return
        }
        _singleAppointmentLoadState.value = SingleAppointmentLoadState.Loading
        _selectedAppointment.value = null
        appointmentRepository.fetchSingleAppointmentById(appointmentId)
            .addOnSuccessListener { snapshot ->
                val appointment = snapshot.getValue(Appointment::class.java)
                if (appointment != null) {
                    _selectedAppointment.value = appointment
                    _singleAppointmentLoadState.value = SingleAppointmentLoadState.Success(appointment)
                } else {
                    _selectedAppointment.value = null
                    _singleAppointmentLoadState.value = SingleAppointmentLoadState.NotFound
                }
            }
            .addOnFailureListener { exception ->
                _selectedAppointment.value = null
                _singleAppointmentLoadState.value = SingleAppointmentLoadState.Error(exception.localizedMessage ?: "Failed to load appointment details.")
            }
    }

    fun bookNewAppointment(
        userId: String,
        date: String,
        description: String,
        selectedVehicle: Vehicle
    ) {
        if (userId.isBlank()) {
            _bookingUiStatus.value = BookingUiStatus.Error("User ID is missing."); return
        }
        if (selectedVehicle.vehicleId.isBlank()) {
            _bookingUiStatus.value = BookingUiStatus.Error("Invalid vehicle selected."); return
        }
        _bookingUiStatus.value = BookingUiStatus.Loading
        appointmentRepository.fetchAppointmentsByVehicleId(selectedVehicle.vehicleId)
            .addOnSuccessListener { appointmentsSnapshot ->
                val existingAppointmentsForVehicle = mutableListOf<Appointment>()
                for (snap in appointmentsSnapshot.children) {
                    snap.getValue(Appointment::class.java)?.let { existingAppointmentsForVehicle.add(it) }
                }
                val conflictingAppointment = existingAppointmentsForVehicle.find { appt ->
                    (appt.appointmentStatus == AppointmentStatusEnum.Booked || appt.appointmentStatus == AppointmentStatusEnum.InProgress)
                }

                if (conflictingAppointment != null) {
                    _bookingUiStatus.value = BookingUiStatus.Error(
                        message = "This vehicle already has a 'Booked' or 'In Progress' appointment.",
                        conflictingAppointment = conflictingAppointment
                    )
                    return@addOnSuccessListener
                }

                val newAppointmentToSave = Appointment(userId = userId, date = date, description = description, vehicleId = selectedVehicle.vehicleId, serviceType = null, appointmentStatus = AppointmentStatusEnum.Booked)

                appointmentRepository.saveAppointment(newAppointmentToSave) // newAppointmentToSave's ID is set inside saveAppointment by the repository
                    .addOnSuccessListener {
                        // newAppointmentToSave now has its ID populated by the repository
                        Log.d(TAG, "Appointment saved to /appointments. ID: ${newAppointmentToSave.appointmentId}. Now updating vehicle history.")
                        customerRepository.addAppointmentToVehicleHistory(userId, selectedVehicle.vehicleId, newAppointmentToSave)
                            .addOnSuccessListener {
                                _bookingUiStatus.value = BookingUiStatus.Success(newAppointmentToSave) // Pass the appointment
                                loadCustomerAppointments(userId) // Refresh the main list
                            }.addOnFailureListener { e ->
                                _bookingUiStatus.value = BookingUiStatus.Error(
                                    message = "Appointment saved, but failed to update vehicle history: ${e.localizedMessage}",
                                    conflictingAppointment = null
                                )
                            }
                    }.addOnFailureListener { exception ->
                        _bookingUiStatus.value = BookingUiStatus.Error(
                            message = exception.localizedMessage ?: "Failed to book new appointment.",
                            conflictingAppointment = null
                        )
                    }
            }.addOnFailureListener { exception ->
                _bookingUiStatus.value = BookingUiStatus.Error(
                    message = exception.localizedMessage ?: "Failed to check for conflicts.",
                    conflictingAppointment = null
                )
            }
    }

    fun resetBookingStatus() { _bookingUiStatus.value = BookingUiStatus.Idle }
    fun clearAllAppointmentsListState() { _allAppointmentsState.value = AppointmentsListState.Idle }
    fun selectAppointment(appointment: Appointment) {
        _selectedAppointment.value = appointment
        _singleAppointmentLoadState.value = SingleAppointmentLoadState.Success(appointment)
        Log.d(TAG, "Selected appointment for detail view (via click): ${appointment.appointmentId}")
    }
    fun clearSelectedAppointment() {
        _selectedAppointment.value = null
        _singleAppointmentLoadState.value = SingleAppointmentLoadState.Idle
        Log.d(TAG, "Cleared selected appointment.")
    }
}