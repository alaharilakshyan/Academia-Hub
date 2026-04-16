package com.example.academia.ui.dashboard

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.academia.data.models.User
import com.example.academia.ui.auth.AuthViewModel
import com.example.academia.ui.theme.ThemeManager

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "Home", Icons.Default.Home)
    object Scan : BottomNavItem("scan", "Scan QR", Icons.Default.QrCodeScanner)
    object History : BottomNavItem("history", "History", Icons.Default.History)
    object Profile : BottomNavItem("profile", "Profile", Icons.Default.Person)
}

@Composable
fun MainScreen(
    user: User?,
    rootNavController: NavController,
    authViewModel: AuthViewModel,
    themeManager: ThemeManager? = null
) {
    val bottomNavController = rememberNavController()
    
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Scan,
        BottomNavItem.History,
        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            
            androidx.compose.material3.Surface(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                color = androidx.compose.ui.graphics.Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    items.forEach { screen ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) com.example.academia.ui.theme.VividViolet else androidx.compose.ui.graphics.Color.Transparent)
                                .clickable {
                                    bottomNavController.navigate(screen.route) {
                                        popUpTo(bottomNavController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                                .padding(12.dp)
                        ) {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                tint = if (isSelected) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color.Gray
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            androidx.compose.material3.FloatingActionButton(
                onClick = { rootNavController.navigate(com.example.academia.navigation.Screen.ChatBot.route) },
                containerColor = com.example.academia.ui.theme.BrightCyan,
                contentColor = androidx.compose.ui.graphics.Color.White
            ) {
                Icon(Icons.Default.Chat, contentDescription = "AI Assistant")
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(400)
                ) + fadeIn(animationSpec = tween(400))
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(400)
                ) + fadeOut(animationSpec = tween(400))
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(400)
                ) + fadeIn(animationSpec = tween(400))
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(400)
                ) + fadeOut(animationSpec = tween(400))
            }
        ) {
            composable(BottomNavItem.Home.route) {
                DashboardScreen(
                    user = user,
                    onNavigateToUpload = { rootNavController.navigate("upload") },
                    onNavigateToVerify = { rootNavController.navigate("verify") },
                    onNavigateToAdmin = { rootNavController.navigate(com.example.academia.navigation.Screen.AdminDashboard.route) },
                    onLogout = {
                        authViewModel.logout()
                        rootNavController.navigate("login") {
                            popUpTo("dashboard_main") { inclusive = true }
                        }
                    }
                )
            }
            
            composable(BottomNavItem.Scan.route) {
                QRScannerScreen(onScanSuccess = { result ->
                    // Instead of going back, we can navigate to Verify details using rootNavController
                    bottomNavController.navigate(BottomNavItem.Home.route)
                    rootNavController.currentBackStackEntry?.savedStateHandle?.set("scanned_id", result)
                    rootNavController.navigate("verify_result_auto") // We will create this flow
                })
            }
            
            composable(BottomNavItem.History.route) {
                HistoryScreen(user = user, authViewModel = authViewModel)
            }
            
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    user = user,
                    viewModel = authViewModel,
                    themeManager = themeManager,
                    onLogout = {
                        authViewModel.logout()
                        rootNavController.navigate("login") {
                            popUpTo("dashboard_main") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
