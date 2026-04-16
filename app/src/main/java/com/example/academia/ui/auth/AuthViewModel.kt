package com.example.academia.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.academia.data.models.ScanHistory
import com.example.academia.data.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.delay

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _userState = MutableStateFlow<User?>(null)
    val userState: StateFlow<User?> = _userState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _scanHistory = MutableStateFlow<List<ScanHistory>>(emptyList())
    val scanHistory: StateFlow<List<ScanHistory>> = _scanHistory

    init {
        checkAutoLogin()
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
                fetchUserFromFirestore(auth.currentUser?.uid)
            } catch (e: Exception) {
                _errorMessage.value = "Login failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signup(name: String, email: String, pass: String, role: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = auth.createUserWithEmailAndPassword(email, pass).await()
                val uid = result.user?.uid
                if (uid != null) {
                    val newUser = User(uid = uid, name = name, email = email, role = role)
                    db.collection("Users").document(uid).set(newUser).await()
                    _userState.value = newUser
                }
            } catch (e: Exception) {
                _errorMessage.value = "Signup failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun fetchUserFromFirestore(uid: String?) {
        if (uid == null) return
        var retries = 3
        while (retries > 0) {
            try {
                // Periodically try to force network enablement in case the emulator dropped it
                db.enableNetwork().await()
                
                val doc = db.collection("Users").document(uid).get().await()
                if (doc.exists()) {
                    val user = doc.toObject(User::class.java)
                    _userState.value = user
                } else {
                    _errorMessage.value = "User not found in database. Please register first."
                    auth.signOut()
                }
                return // Success, break out of loop
            } catch (e: Exception) {
                retries--
                if (retries == 0) {
                    val errorDetail = e.message ?: "Unknown error"
                    if (errorDetail.contains("client is offline")) {
                         _errorMessage.value = "Failed to reach database. Check your internet or ensure the Firestore Database is created in the Firebase Console."
                    } else {
                         _errorMessage.value = "Failed to fetch user data: $errorDetail"
                    }
                } else {
                    delay(1500) // wait before retrying
                }
            }
        }
    }

    fun updatePassword(oldPass: String, newPass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // In Firebase, oldPass usually requires re-authentication, but we'll attempt a direct update
                // For a robust app, re-authentication of the credentials should be done first.
                auth.currentUser?.updatePassword(newPass)?.await()
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update password: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchScanHistory() {
        viewModelScope.launch {
            try {
                val user = auth.currentUser
                if (user != null) {
                    val snapshot = db.collection("ScanHistories")
                        .whereEqualTo("uid", user.uid)
                        .orderBy("scannedAt", Query.Direction.DESCENDING)
                        .get()
                        .await()
                    
                    val historyList = snapshot.documents.mapNotNull { it.toObject(ScanHistory::class.java) }
                    _scanHistory.value = historyList
                }
            } catch (e: Exception) {
                // Ignore or log error
            }
        }
    }

    fun logScanHistory(certId: String, studentName: String? = null) {
        viewModelScope.launch {
            try {
                val user = auth.currentUser
                if (user != null) {
                    val scanLog = ScanHistory(
                        uid = user.uid,
                        certificateId = certId,
                        studentName = studentName,
                        status = "Verified"
                    )
                    db.collection("ScanHistories").add(scanLog).await()
                    fetchScanHistory() // Refresh history
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun updateProfile(uid: String, name: String, email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Update specific fields in Firestore
                val updates = mapOf(
                    "name" to name,
                    "email" to email
                )
                db.collection("Users").document(uid).update(updates).await()
                // Update email in Auth
                auth.currentUser?.updateEmail(email)?.await()
                
                fetchUserFromFirestore(uid)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update profile: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        auth.signOut()
        _userState.value = null
        _scanHistory.value = emptyList()
    }

    fun checkAutoLogin() {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user != null) {
                _isLoading.value = true
                fetchUserFromFirestore(user.uid)
                _isLoading.value = false
            } else {
                _userState.value = null
            }
        }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val user = auth.currentUser
                if (user != null) {
                    val uid = user.uid
                    // 1. Delete all scan history associated with the user
                    val scanDocs = db.collection("ScanHistories").whereEqualTo("uid", uid).get().await()
                    db.runBatch { batch ->
                        for (doc in scanDocs.documents) {
                            batch.delete(doc.reference)
                        }
                    }.await()
                    
                    // 2. Delete user from Firestore
                    db.collection("Users").document(uid).delete().await()
                    
                    // 3. Delete from Firebase Auth
                    user.delete().await()
                    
                    logout()
                    onSuccess()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete account: ${e.message}"
            }
        }
    }
}