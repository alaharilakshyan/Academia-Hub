package com.example.academia.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.academia.R
import com.example.academia.data.models.User
import com.example.academia.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    user: User?,
    onNavigateToUpload: () -> Unit,
    onNavigateToVerify: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onLogout: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(VividViolet),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user?.name?.take(1)?.uppercase() ?: "U",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Hello,",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${user?.name ?: "User"}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { /* Search Action */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.Tune, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurface)
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            DropdownMenuItem(
                                text = { Text("Logout", color = MaterialTheme.colorScheme.error) },
                                onClick = { 
                                    menuExpanded = false
                                    onLogout() 
                                },
                                leadingIcon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                            )
                        }
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
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            
            // Re-introduced original academia specific content
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(28.dp),
                color = VividViolet,
                shadowElevation = 12.dp
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "Academia Hub 🎉",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Verify secure records",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "Secure Ecosystem",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    // The 3D illustration
                    Image(
                        painter = painterResource(id = R.drawable.hero_3d),
                        contentDescription = "3D Hero",
                        modifier = Modifier
                            .size(160.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = 20.dp, y = 20.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Primary Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Verification Hub 🔍",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "See all",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Large wide Action Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToVerify() },
                shape = RoundedCornerShape(24.dp),
                color = SunburstYellow, // Bright yellow looks great with white text
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Black.copy(alpha = 0.2f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color.White, modifier = Modifier.padding(12.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Instant Verify", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Scan a QR code to verify", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        }
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White.copy(alpha = 0.6f))
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "All Tools ✨",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Popular",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (user?.role == "Admin") {
                // Admin gets a Grid of two actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ActionGridCard(
                        title = "Upload\nDocument",
                        iconId = R.drawable.abstract_yellow,
                        color = VividCoral,
                        onClick = onNavigateToUpload,
                        modifier = Modifier.weight(1f)
                    )
                    ActionGridCard(
                        title = "Pending\nApprovals",
                        iconId = R.drawable.abstract_pink,
                        color = BrightCyan,
                        onClick = onNavigateToAdmin,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                // Non-Admin gets a single, full-width action
                ActionGridCard(
                    title = "Upload Document",
                    iconId = R.drawable.abstract_yellow,
                    color = VividCoral,
                    onClick = onNavigateToUpload,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(100.dp)) // Padding for bottom nav
        }
    }
}

@Composable
fun ActionGridCard(
    title: String,
    iconId: Int,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = color,
        shadowElevation = 8.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomStart)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.2f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Widgets, contentDescription = null, tint = Color.White, modifier = Modifier.padding(6.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    lineHeight = 20.sp
                )
            }
            
            Image(
                painter = painterResource(id = iconId),
                contentDescription = null,
                modifier = Modifier
                    .size(90.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 10.dp, y = -10.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}