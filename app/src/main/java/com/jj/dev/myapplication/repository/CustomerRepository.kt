package com.jj.dev.myapplication.repository

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.jj.dev.myapplication.Model.Appointment
import com.jj.dev.myapplication.Model.Customer
import com.jj.dev.myapplication.Model.Vehicle
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class CustomerRepository @Inject constructor( // Renamed from UserRepository
    private val database: FirebaseDatabase
) {
    companion object {
        private const val TAG = "CustomerRepository" // Updated TAG
    }

    fun fetchCustomerProfile(uid: String): Task<DataSnapshot> {
        Log.d(TAG, "Fetching full customer profile for UID: $uid")
        return database.getReference("users").child(uid).get()
    }

    // This was createFullCustomerProfile, name maintained for now but it's specific to Customer
    fun saveCustomerProfile(customer: Customer): Task<Void> {
        Log.d(TAG, "Saving full customer profile for UID: ${customer.userId}")
        return database.getReference("users").child(customer.userId).setValue(customer)
    }

    fun addDummyVehicleToCustomer(uid: String, name: String, email: String): Task<Void> {
        val dummyVehicleId = database.getReference("vehicles_placeholder").push().key ?: "dummy_v_${System.currentTimeMillis()}"
        val dummyAppointmentId = database.getReference("appointments_placeholder").push().key ?: "dummy_a_${System.currentTimeMillis()}"

        val dummyVehicle = Vehicle(
            vehicleId = dummyVehicleId,
            userId = uid,
            vehicleModel = "Default Sedan",
            vehiclePlate = "ABC-123",
            serviceHistory = listOf(
                Appointment(
                    appointmentId = dummyAppointmentId,
                    userId = uid,
                    vehicleId = dummyVehicleId,
                    date = "2024-01-01",
                    description = "Initial Setup",
                    serviceType = "System Generated"
                )
            )
        )
        val initialCustomer = Customer(
            userId = uid,
            email = email,
            name = name,
            role = "Customer",
            contact = "",
            vehicles = listOf(dummyVehicle)
        )
        // Using saveCustomerProfile which essentially does the same as setValue here
        return saveCustomerProfile(initialCustomer)
            .addOnSuccessListener { Log.d(TAG, "Full customer profile with dummy vehicle created for $uid") }
            .addOnFailureListener { e -> Log.e(TAG, "Failed to create full customer profile for $uid", e) }
    }


    fun addAppointmentToVehicleHistory(userId: String, vehicleId: String, appointment: Appointment): Task<Void> {
        val customerRef = database.getReference("users").child(userId)
        return customerRef.get().continueWithTask { task ->
            if (!task.isSuccessful) {
                Log.e(TAG, "Failed to fetch customer to update vehicle history.", task.exception)
                throw task.exception ?: Exception("Failed to fetch customer data.")
            }
            val customer = task.result?.getValue(Customer::class.java)
            if (customer == null) {
                Log.e(TAG, "Customer data not found for UID: $userId.")
                throw Exception("Customer data not found.")
            }

            val updatedVehicles = customer.vehicles.map { vehicle ->
                if (vehicle.vehicleId == vehicleId) {
                    vehicle.copy(serviceHistory = vehicle.serviceHistory + appointment)
                } else {
                    vehicle
                }
            }
            Log.d(TAG, "Updating vehicle history for $vehicleId for user $userId")
            // Save the updated customer object which includes the updated vehicles list
            return@continueWithTask saveCustomerProfile(customer.copy(vehicles = updatedVehicles))
        }
    }
}