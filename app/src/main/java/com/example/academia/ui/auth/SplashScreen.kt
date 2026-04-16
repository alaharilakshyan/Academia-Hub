package com.example.academia.ui.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.academia.ui.theme.VividViolet
import com.example.academia.ui.theme.BrightCyan
import com.example.academia.ui.theme.VividCoral
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val alphaAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.8f) }

    // Infinite gradient animation
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "gradient_offset"
    )

    // Pulsating animation for logo
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse_scale"
    )

    LaunchedEffect(key1 = true) {
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
        delay(2500) // Slightly longer to show off the animation
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface,
                        BrightCyan.copy(alpha = 0.4f),
                        BrightCyan.copy(alpha = 0.2f)
                    ),
                    start = androidx.compose.ui.geometry.Offset(gradientOffset, 0f),
                    end = androidx.compose.ui.geometry.Offset(0f, gradientOffset)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(alphaAnim.value)
                .scale(scaleAnim.value)
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = "App Logo",
                tint = VividViolet,
                modifier = Modifier
                    .size(100.dp)
                    .scale(pulseScale)
                    .padding(bottom = 16.dp)
            )
            Text(
                text = "Academia",
                color = VividViolet, // Unified primary
                fontSize = 56.sp,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Secure Digital Verification",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(48.dp))
            CircularProgressIndicator(
                color = VividCoral,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}