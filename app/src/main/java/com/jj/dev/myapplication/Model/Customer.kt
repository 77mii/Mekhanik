package com.jj.dev.myapplication.Model

/**
 * A Customer, extends User.
 */
data class Customer(
    override val userId: String = "",
    override val email: String = "",
    override val name: String = "",
    override val role: String = "Customer", // Explicitly set role
    val contact: String = "",
    val vehicles: List<Vehicle> = emptyList() // Vehicles will be empty on registration by default
) : User(userId, email, name, role)