package com.jj.dev.myapplication.Model

import com.google.firebase.database.IgnoreExtraProperties

/**
 * Represents one kind of service a customer can book.
 *
 * Stored under “serviceTypes/{serviceId}” in Firebase.
 */
@IgnoreExtraProperties
data class ServiceType(
    var serviceId: String = "",              // unique key in database
    var serviceName: String = "",            // human-readable name (e.g. “Oil Change”)
    var defaultDurationMinutes: Int = 0      // default length in minutes
)