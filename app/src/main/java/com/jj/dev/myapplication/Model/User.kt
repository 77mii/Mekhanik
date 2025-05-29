package com.jj.dev.myapplication.Model

/**
 * A generic user of the system.
 * Subclasses (Customer, Mechanic, Admin) inherit from this.
 */
open class User(
    open val userId: String = "",
    open val email: String = "",
    open val name: String = "",
    open val role: String = "" // Added role for explicit storage
)
