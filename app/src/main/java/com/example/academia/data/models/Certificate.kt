package com.example.academia.data.models

data class Certificate(
    val id: String = "",
    val studentName: String = "",
    val course: String = "",
    val institution: String = "",
    val issueDate: String = "",
    val fileUrl: String = "",
    val status: String = "Pending" // Pending, Valid, Rejected
)