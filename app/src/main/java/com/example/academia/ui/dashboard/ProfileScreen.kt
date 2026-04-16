package com.example.academia.ui.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.academia.data.models.User
import com.example.academia.ui.auth.AuthViewModel
import com.example.academia.ui.theme.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User?,
    viewModel: AuthViewModel,
    themeManager: ThemeManager? = null,
    onLogout: () -> Unit
) {
    var isEditingProfile by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(user?.name ?: "User") }
    var email by remember { mutableStateOf(user?.email ?: "user@example.com") }

    // Password fields
    var isEditingPassword by remember { mutableStateOf(false) }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    
    val storagePerm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    
    var hasStoragePermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, storagePerm) == PackageManager.PERMISSION_GRANTED)
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasCameraPermission = it }
    val storageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasStoragePermission = it }

    val currentTheme = themeManager?.themeFlow?.collectAsState()?.value ?: "System"
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Settings", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                actions = {
                    IconButton(onClick = {
                        val newTheme = if (currentTheme == "Dark") "Light" else "Dark"
                        themeManager?.setTheme(newTheme)
                    }) {
                        Icon(
                            if (currentTheme == "Dark") Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(32.dp))

            // -- PROFILE REGION --
            SectionHeader("Basic Information")
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("Full Name") }, enabled = isEditingProfile,
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = email, onValueChange = { email = it },
                        label = { Text("Email Address") }, enabled = isEditingProfile,
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (isEditingProfile) {
                                user?.uid?.let { viewModel.updateProfile(it, name, email) }
                            }
                            isEditingProfile = !isEditingProfile
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isEditingProfile) "Save Profile" else "Edit Profile")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // -- SECURITY REGION --
            SectionHeader("Security")
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (isEditingPassword) {
                        OutlinedTextField(
                            value = oldPassword, onValueChange = { oldPassword = it },
                            label = { Text("Current Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = newPassword, onValueChange = { newPassword = it },
                            label = { Text("New Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            TextButton(onClick = { isEditingPassword = false }) { Text("Cancel") }
                            Button(onClick = {
                                viewModel.updatePassword(oldPassword, newPassword) {
                                    isEditingPassword = false
                                    oldPassword = ""
                                    newPassword = ""
                                }
                            }) { Text("Update Password") }
                        }
                    } else {
                        Button(onClick = { isEditingPassword = true }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Lock, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Change Password")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))



            Spacer(modifier = Modifier.height(24.dp))

            // -- PERMISSIONS REGION --
            SectionHeader("Permissions")
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    PermissionRow("Camera", Icons.Default.CameraAlt, hasCameraPermission) {
                        cameraLauncher.launch(Manifest.permission.CAMERA)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    PermissionRow("Storage", Icons.Default.Folder, hasStoragePermission) {
                        storageLauncher.launch(storagePerm)
                    }
                }
            }

            
            Spacer(modifier = Modifier.height(32.dp))

            // -- DANGER ZONE --
            SectionHeader("Danger Zone")
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    var showDeleteDialog by remember { mutableStateOf(false) }
                    
                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Account permanently")
                    }

                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            title = { Text("Delete Account") },
                            text = { Text("Are you sure? This action cannot be undone and your certificates history will be wiped.") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showDeleteDialog = false
                                        viewModel.deleteAccount(onSuccess = onLogout)
                                    }
                                ) {
                                    Text("Yes, Delete", color = MaterialTheme.colorScheme.error)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp)) // Buffer for bottom nav
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    )
}

@Composable
fun PermissionRow(name: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isGranted: Boolean, onRequest: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(16.dp))
            Text(name, style = MaterialTheme.typography.bodyLarge)
        }
        if (isGranted) {
            Icon(Icons.Default.CheckCircle, contentDescription = "Granted", tint = MaterialTheme.colorScheme.primary)
        } else {
            Button(onClick = onRequest) { Text("Grant") }
        }
    }
}
