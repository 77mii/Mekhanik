package com.jj.dev.myapplication.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jj.dev.myapplication.Model.Customer
import com.jj.dev.myapplication.Model.Vehicle
import com.jj.dev.myapplication.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


sealed class CustomerProfileState { // Renamed from UserProfileState
    object Idle : CustomerProfileState()
    object Loading : CustomerProfileState()
    data class Loaded(val customer: Customer?) : CustomerProfileState()
    data class Error(val message: String) : CustomerProfileState()
}

@HiltViewModel
class CustomerViewModel @Inject constructor( // Renamed from UserViewModel
    private val customerRepository: CustomerRepository // Updated repository type
) : ViewModel() {

    private val _customerProfileState = MutableLiveData<CustomerProfileState>(CustomerProfileState.Idle) // Updated type
    val customerProfileState: LiveData<CustomerProfileState> = _customerProfileState // Updated type

    private val _customerVehicles = MutableLiveData<List<Vehicle>?>(null)
    val customerVehicles: LiveData<List<Vehicle>?> = _customerVehicles

    private val _customerProfile = MutableLiveData<Customer?>(null)
    val customerProfile: LiveData<Customer?> = _customerProfile


    companion object { private const val TAG = "CustomerViewModel" } // Updated TAG

    fun loadCustomerProfile(uid: String) {
        _customerProfileState.value = CustomerProfileState.Loading
        Log.d(TAG, "Attempting to load customer profile for UID: $uid")
        customerRepository.fetchCustomerProfile(uid)
            .addOnSuccessListener { dataSnapshot ->
                val customer = dataSnapshot.getValue(Customer::class.java)
                // Ensure the fetched user is actually a customer and not another role by mistake
                if (customer != null && customer.role == "Customer") {
                    _customerProfile.value = customer
                    _customerVehicles.value = customer.vehicles
                    _customerProfileState.value = CustomerProfileState.Loaded(customer)
                    Log.d(TAG, "Customer profile loaded for ${customer.name}. Vehicles: ${customer.vehicles.size}")
                } else {
                    _customerProfile.value = null
                    _customerVehicles.value = null
                    // If a user document exists but isn't a customer, or is malformed
                    _customerProfileState.value = CustomerProfileState.Error("User profile is not a customer or is invalid.")
                    Log.w(TAG, "Fetched user document for UID: $uid is not a valid customer. Role: ${customer?.role}")
                }
            }
            .addOnFailureListener { exception ->
                _customerProfileState.value = CustomerProfileState.Error(exception.localizedMessage ?: "Failed to load customer profile.")
                _customerVehicles.value = null
                _customerProfile.value = null
                Log.e(TAG, "Error loading customer profile for UID: $uid", exception)
            }
    }

    fun createInitialCustomerProfileWithDummyVehicle(uid: String, name: String, email: String) {
        _customerProfileState.value = CustomerProfileState.Loading
        Log.d(TAG, "Creating initial customer profile with dummy vehicle for UID: $uid")
        customerRepository.addDummyVehicleToCustomer(uid, name, email)
            .addOnSuccessListener {
                Log.d(TAG, "Initial customer profile with dummy vehicle created successfully for UID: $uid. Reloading profile.")
                loadCustomerProfile(uid)
            }
            .addOnFailureListener { exception ->
                _customerProfileState.value = CustomerProfileState.Error(exception.localizedMessage ?: "Failed to create initial customer profile.")
                Log.e(TAG, "Error creating initial customer profile for UID: $uid", exception)
            }
    }

    fun clearCustomerData() { // Renamed for clarity
        _customerProfile.value = null
        _customerVehicles.value = null
        _customerProfileState.value = CustomerProfileState.Idle
        Log.d(TAG, "Customer data cleared from CustomerViewModel.")
    }
}
