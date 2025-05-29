package com.jj.dev.myapplication.Model

import com.google.firebase.database.IgnoreExtraProperties

/**
 * A vehicle owned by a customer.
 *
 * @property vehicleId      Unique identifier for this vehicle.
 * @property userId         The customer (User) who owns this vehicle.
 * @property vehicleModel   The make/model of the vehicle (e.g., “Toyota Camry”).
 * @property vehiclePlate   License plate or registration number.
 * @property serviceHistory The history of appointments (services) performed.
 */
@IgnoreExtraProperties
data class Vehicle(
    var vehicleId: String = "",
    var userId: String = "",
    var vehicleModel: String = "",
    var vehiclePlate: String = "",
    var serviceHistory: List<Appointment> = emptyList()
)