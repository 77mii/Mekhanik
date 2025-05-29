package com.jj.dev.myapplication.Model

data class Mechanic(
    override val userId: String = "",
    override val email: String = "",
    override val name: String = "",
    override val role: String = "Mechanic", // Explicitly set role
    val specialization: String = ""
) : User(userId, email, name, role)