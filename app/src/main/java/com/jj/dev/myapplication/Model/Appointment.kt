package com.jj.dev.myapplication.Model

import com.google.firebase.database.IgnoreExtraProperties
import java.time.Instant

/**
 * Represents a service appointment booked by a customer.
 *
 * Fields match the Firebase structure under “appointments/{appointmentId}”.
 */
@IgnoreExtraProperties
data class Appointment(
    var appointmentId: String = "",          // unique key under /appointments
    var userId: String = "",                 // customer who booked
    var timeSlotId: String? = null,// staff assigned time-slot ID for this appointment
    var date: String = "",                   // date of appointment chosen by customer
    var serviceType: String? = "",            // id or name of the service chosen by the admin
    var description: String = "",            // customer notes/complaints
    var appointmentStatus: AppointmentStatusEnum = AppointmentStatusEnum.Booked,
    var mechanicId: String? = null,          //  staff assigned
    var vehicleId: String = "",               // which vehicle to service

)