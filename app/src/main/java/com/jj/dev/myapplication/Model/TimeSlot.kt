package com.jj.dev.myapplication.Model

import java.time.Instant

/*
 * Represents a single “slot” that can be booked by a customer.
 * We store it in RTDB under “timeSlots/{date}/{slotId}”.
 */
data class TimeSlot(
    val slotId: String = "",
    val date: String,         // YYYY-MM-DD, e.g. “2025-06-01” derived from Appointment.date
    val startTime: String,    // ISO 8601, e.g. “2025-06-01T09:00:00Z” derived from ServiceType.defaultDurationMinutes
    val endTime: String,      // ISO 8601, e.g. “2025-06-01T10:00:00Z” derived from ServiceType.defaultDurationMinutes
    val isBooked: Boolean = false,
    val appointmentId: String? = null // once booked, points back at the Appointment
)
