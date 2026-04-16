package com.example.academia.ui.dashboard

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.academia.ui.certificates.CertificateViewModel
import com.example.academia.ui.theme.*
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationScreen(
    onBack: () -> Unit,
    onNavigateToScanner: () -> Unit,
    navController: NavController,
    authViewModel: com.example.academia.ui.auth.AuthViewModel,
    viewModel: CertificateViewModel = viewModel()
) {
    val context = LocalContext.current
    var certId by remember { mutableStateOf("") }
    val result by viewModel.verificationResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val scannedId = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<String>("scanned_id")
    
    LaunchedEffect(scannedId) {
        if (scannedId != null) {
            certId = scannedId
            viewModel.verifyCertificate(scannedId)
        }
    }

    // Log to backend history
    LaunchedEffect(result) {
        result?.let {
            if (it.status != "Fake") {
                authViewModel.logScanHistory(it.id ?: certId, it.studentName)
            }
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val image = InputImage.fromFilePath(context, uri)
                val scanner = BarcodeScanning.getClient()
                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        if (barcodes.isNotEmpty()) {
                            val scannedValue = barcodes[0].rawValue
                            if (scannedValue != null) {
                                certId = scannedValue
                                viewModel.verifyCertificate(scannedValue)
                            }
                        }
                    }
            } catch (e: Exception) {}
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = { Text("Verification", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Tune Action */ }) {
                        Icon(Icons.Default.Tune, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            // Hero Status Banner
            val bannerColor = when(result?.status) {
                "Accepted" -> StatusAccepted
                "Rejected", "Fake" -> StatusRejected
                "Pending" -> StatusPending
                else -> VividViolet
            }
            val titleText = when(result?.status) {
                "Accepted" -> "Certificate Authentic 😊"
                "Rejected", "Fake" -> "Invalid Credential 🚨"
                "Pending" -> "Verification Pending ⏳"
                else -> "Verify any Certificate 🕵️"
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(24.dp),
                color = bannerColor,
                shadowElevation = 8.dp
            ) {
                Box(modifier = Modifier.padding(24.dp)) {
                    Column {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Black.copy(alpha = 0.2f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White, modifier = Modifier.padding(12.dp))
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = titleText,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (result?.status == "Accepted") {
                            Text("Verified by Academia", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                "About this scan",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Instantly verify the authenticity of an academic credential using the unique ID, camera scanner, or gallery upload.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (result == null) {
                // Input controls explicitly following Glassmorphism clean layout
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = certId,
                            onValueChange = { certId = it },
                            label = { Text("Enter Certificate ID") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = onNavigateToScanner,
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VividCoral)
                            ) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Camera", fontWeight = FontWeight.SemiBold)
                            }
                            Button(
                                onClick = { imagePicker.launch("image/*") },
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrightCyan, contentColor = VividViolet)
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Gallery", fontWeight = FontWeight.SemiBold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = { viewModel.verifyCertificate(certId) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            enabled = !isLoading && certId.isNotBlank(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VividViolet)
                        ) {
                            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            else Text("Submit verification", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Detail List matching the "Intro to videography" white boxes
                val details = listOf(
                    "Student" to result!!.studentName,
                    "Course" to result!!.course,
                    "Institution" to result!!.institution,
                    "Date Issued" to result!!.issueDate
                )
                
                details.forEach { (label, value) ->
                    DetailRow(label = label, value = value)
                }

                if (result!!.status == "Accepted") {
                    Spacer(modifier = Modifier.height(40.dp))
                    Button(
                        onClick = {
                            val i = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_SUBJECT, "Verified Certificate")
                                putExtra(android.content.Intent.EXTRA_TEXT, "I just verified my certificate ID: ${result!!.id} using Academia!")
                            }
                            context.startActivity(android.content.Intent.createChooser(i, "Share via"))
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VividCoral) // Large Pill button from mockup bottom
                    ) {
                        Text("Share verification report", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(VividViolet),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.School, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = value,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}