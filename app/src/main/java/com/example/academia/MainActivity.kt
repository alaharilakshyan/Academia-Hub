package com.example.academia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.example.academia.navigation.NavGraph
import com.example.academia.ui.theme.AcademiaTheme
import com.example.academia.ui.theme.ThemeManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeManager = ThemeManager.getInstance(applicationContext)
            val appTheme by themeManager.themeFlow.collectAsState()
            
            val isDarkTheme = when(appTheme) {
                "Dark" -> true
                "Light" -> false
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            AcademiaTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavGraph(navController = navController, themeManager = themeManager)
                    }
                }
            }
        }
    }
}