package com.example.academia.data.models

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val password: String? = null,
    val role: String = "Student" // Roles: Student, Employer, Admin
)