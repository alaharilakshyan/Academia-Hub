package com.example.academia.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.academia.ui.auth.AuthViewModel
import com.example.academia.ui.auth.LoginScreen
import com.example.academia.ui.auth.SignUpScreen
import com.example.academia.ui.auth.SplashScreen
import com.example.academia.ui.dashboard.DashboardScreen
import com.example.academia.ui.dashboard.ProfileScreen
import com.example.academia.ui.dashboard.QRScannerScreen
import com.example.academia.ui.dashboard.SettingsScreen
import com.example.academia.ui.dashboard.MainScreen
import com.example.academia.ui.dashboard.UploadScreen
import com.example.academia.ui.dashboard.VerificationScreen
import com.example.academia.ui.theme.ThemeManager

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Dashboard : Screen("dashboard")
    object Upload : Screen("upload")
    object Verify : Screen("verify")
    object QRScanner : Screen("qr_scanner")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object ChatBot : Screen("chatbot")
    object AdminDashboard : Screen("admin_dashboard")
}

@Composable
fun NavGraph(navController: NavHostController, themeManager: ThemeManager? = null) {
    val authViewModel: AuthViewModel = viewModel()
    val userState by authViewModel.userState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    val nextRoute = if (userState != null) "dashboard_main" else Screen.Login.route
                    navController.navigate(nextRoute) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("dashboard_main") {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate("dashboard_main") {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                viewModel = authViewModel
            )
        }

        composable("dashboard_main") {
            MainScreen(
                user = userState,
                rootNavController = navController,
                authViewModel = authViewModel,
                themeManager = themeManager
            )
        }

        composable(Screen.Upload.route) {
            UploadScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Verify.route) {
            VerificationScreen(
                onBack = { navController.popBackStack() },
                onNavigateToScanner = { navController.navigate(Screen.QRScanner.route) },
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Screen.QRScanner.route) {
            QRScannerScreen(onScanSuccess = { result ->
                navController.previousBackStackEntry?.savedStateHandle?.set("scanned_id", result)
                navController.popBackStack()
            })
        }

        composable(Screen.ChatBot.route) {
            com.example.academia.ui.dashboard.ChatBotScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminDashboard.route) {
            val certViewModel: com.example.academia.ui.certificates.CertificateViewModel = viewModel()
            com.example.academia.ui.dashboard.AdminDashboardScreen(
                onBack = { navController.popBackStack() },
                viewModel = certViewModel
            )
        }
    }
}