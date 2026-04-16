package com.example.academia.data.models

data class ScanHistory(
    val _id: String? = null,
    val uid: String = "",
    val certificateId: String = "",
    val studentName: String? = null,
    val course: String? = null,
    val institution: String? = null,
    val status: String = "Verified",
    val scannedAt: Long? = System.currentTimeMillis()
)
