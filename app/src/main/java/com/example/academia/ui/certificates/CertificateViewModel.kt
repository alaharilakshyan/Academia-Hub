package com.example.academia.ui.certificates

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.academia.data.models.Certificate
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CertificateViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _certificates = MutableStateFlow<List<Certificate>>(emptyList())
    val certificates: StateFlow<List<Certificate>> = _certificates

    private val _verificationResult = MutableStateFlow<Certificate?>(null)
    val verificationResult: StateFlow<Certificate?> = _verificationResult

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun uploadCertificate(
        studentName: String,
        course: String,
        institution: String,
        file: File
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // We use the file name as a part of the unique cert ID for verification
                val uniqueId = "CERT-" + (10000 + (Math.random() * 90000).toInt())
                
                // Upload file to Firebase Storage
                val fileUri = Uri.fromFile(file)
                val storageRef = storage.reference.child("uploads/${file.name}")
                val uploadTask = storageRef.putFile(fileUri).await()
                
                // Get Download URL
                val downloadUrl = storageRef.downloadUrl.await().toString()

                // Save Metadata to Firestore
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val currentDate = dateFormat.format(Date())

                val newCert = Certificate(
                    id = uniqueId,
                    studentName = studentName,
                    course = course,
                    institution = institution,
                    issueDate = currentDate,
                    fileUrl = downloadUrl,
                    status = "Pending"
                )

                db.collection("Certificates").document(uniqueId).set(newCert).await()

                // Successfully uploaded
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun verifyCertificate(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val document = db.collection("Certificates").document(id).get().await()
                if (document.exists()) {
                    val cert = document.toObject(Certificate::class.java)
                    _verificationResult.value = cert
                } else {
                    _verificationResult.value = Certificate(
                        id = id,
                        studentName = "Unknown",
                        course = "Unknown",
                        institution = "Unknown",
                        issueDate = "Unknown",
                        status = "Fake"
                    )
                }
            } catch (e: Exception) {
                _verificationResult.value = Certificate(
                    id = id,
                    studentName = "Unknown",
                    course = "Unknown",
                    institution = "Unknown",
                    issueDate = "Unknown",
                    status = "Fake"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
    private val _pendingCertificates = MutableStateFlow<List<Certificate>>(emptyList())
    val pendingCertificates: StateFlow<List<Certificate>> = _pendingCertificates

    fun fetchPendingCertificates() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = db.collection("Certificates")
                    .whereEqualTo("status", "Pending")
                    .get()
                    .await()
                
                val list = snapshot.documents.mapNotNull { it.toObject(Certificate::class.java) }
                _pendingCertificates.value = list
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCertificateStatus(id: String, newStatus: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                db.collection("Certificates").document(id).update("status", newStatus).await()
                fetchPendingCertificates() // Refresh the list
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}