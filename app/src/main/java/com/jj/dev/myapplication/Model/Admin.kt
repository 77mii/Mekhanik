package com.jj.dev.myapplication.Model

data class Admin(
    override val userId: String = "",
    override val email: String = "",
    override val name: String = "",
    override val role: String = "Admin" // Explicitly set role
    // You can add admin-specific fields here if needed in the future
) : User(userId, email, name, role)