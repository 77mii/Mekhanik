package com.jj.dev.myapplication.repository



import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.jj.dev.myapplication.Model.Appointment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppointmentRepository @Inject constructor(
    private val database: FirebaseDatabase
) {
    companion object {
        private const val TAG = "AppointmentRepository"
    }

    fun saveAppointment(appointment: Appointment): Task<Void> {
        if (appointment.userId.isBlank()) {
            Log.e(TAG, "Cannot save appointment without a userId.")
            return Tasks.forException(IllegalArgumentException("Appointment must have a userId."))
        }
        val appointmentRef = database.getReference("appointments").push()
        appointment.appointmentId = appointmentRef.key ?: return Tasks.forException(Exception("Could not generate appointment ID."))

        Log.d(TAG, "Saving appointment ${appointment.appointmentId} for user ${appointment.userId}")
        return appointmentRef.setValue(appointment)
    }

    fun fetchAppointmentsByVehicleId(vehicleId: String): Task<DataSnapshot> {
        Log.d(TAG, "Fetching appointments for vehicleId: $vehicleId")
        return database.getReference("appointments")
            .orderByChild("vehicleId")
            .equalTo(vehicleId)
            .get()
    }

    fun fetchAppointmentsByUserId(userId: String): Task<DataSnapshot> {
        Log.d(TAG, "Fetching appointments for userId: $userId")
        return database.getReference("appointments")
            .orderByChild("userId")
            .equalTo(userId)
            .get()
    }

    fun fetchSingleAppointmentById(appointmentId: String): Task<DataSnapshot> {
        Log.d(TAG, "Fetching single appointment by ID: $appointmentId")
        return database.getReference("appointments").child(appointmentId).get()
    }

    fun fetchAllAppointments(): Task<DataSnapshot> {
        Log.d(TAG, "Fetching all appointments for Admin view.")
        return database.getReference("appointments").get()
    }
}