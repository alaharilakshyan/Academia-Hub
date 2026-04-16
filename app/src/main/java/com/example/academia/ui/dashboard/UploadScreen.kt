package com.example.academia.ui.dashboard

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.academia.data.utils.FileUtils
import com.example.academia.ui.certificates.CertificateViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import androidx.compose.material.icons.filled.CameraAlt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    onBack: () -> Unit,
    viewModel: CertificateViewModel = viewModel()
) {
    val context = LocalContext.current
    var studentName by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var institution by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    val isLoading by viewModel.isLoading.collectAsState()

    var isPdf by remember { mutableStateOf(true) }

    var uploadMode by remember { mutableStateOf("CHOICE") } // CHOICE, CAMERA_CONFIRM, MANUAL

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedUri = uri
        isPdf = true
    }

    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempImageUri != null) {
            selectedUri = tempImageUri
            isPdf = false
            uploadMode = "CAMERA_CONFIRM"

            // Start MLKit OCR mapping
            try {
                val image = InputImage.fromFilePath(context, tempImageUri!!)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val text = visionText.text.lowercase()
                        if (studentName.isBlank() && visionText.textBlocks.isNotEmpty()) {
                            studentName = visionText.textBlocks.firstOrNull()?.text ?: ""
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload Certificate", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CloudUpload,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
            
            if (uploadMode == "CHOICE") {
                Spacer(modifier = Modifier.height(24.dp))
                ActionCard(
                    title = "Scan Certificate",
                    description = "Take a photo of the physical certificate. Details will be extracted automatically.",
                    icon = Icons.Default.CameraAlt,
                    color = MaterialTheme.colorScheme.primary,
                    onClick = {
                        val file = File(context.cacheDir, "camera_image_${System.currentTimeMillis()}.jpg")
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        tempImageUri = uri
                        cameraLauncher.launch(uri)
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                ActionCard(
                    title = "Upload Manually",
                    description = "Select a PDF and manually fill the credential details.",
                    icon = Icons.Default.Description,
                    color = MaterialTheme.colorScheme.secondary,
                    onClick = {
                        uploadMode = "MANUAL"
                    }
                )
            } else {
                Text(
                    text = if (uploadMode == "MANUAL") "Manual Upload" else "Review Scanned Details",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = studentName,
                            onValueChange = { studentName = it },
                            label = { Text("Student Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.School, contentDescription = null) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = course,
                            onValueChange = { course = it },
                            label = { Text("Course Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = institution,
                            onValueChange = { institution = it },
                            label = { Text("Institution") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.School, contentDescription = null) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (uploadMode == "MANUAL") {
                    Surface(
                        onClick = { filePicker.launch("application/pdf") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = if (selectedUri != null && isPdf) MaterialTheme.colorScheme.tertiaryContainer 
                                else MaterialTheme.colorScheme.secondaryContainer,
                        tonalElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (selectedUri != null && isPdf) Icons.Default.Description else Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = if (selectedUri != null && isPdf) MaterialTheme.colorScheme.onTertiaryContainer 
                                    else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (selectedUri != null && isPdf) "PDF Selected ✓" else "Select Certificate PDF",
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedUri != null && isPdf) MaterialTheme.colorScheme.onTertiaryContainer 
                                        else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                } else if (uploadMode == "CAMERA_CONFIRM" && selectedUri != null) {
                     Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                         Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                         Spacer(modifier = Modifier.width(8.dp))
                         Text("Image Captured ✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                     }
                }
                
                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = { 
                        selectedUri?.let { uri ->
                            val file = FileUtils.getFileFromUri(context, uri)
                            if (file != null) {
                                viewModel.uploadCertificate(studentName, course, institution, file)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading && selectedUri != null && studentName.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Text("Upload Now", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.15f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = color
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}