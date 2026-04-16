package com.example.academia.ui.theme

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ThemeManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    private val _themeFlow = MutableStateFlow(prefs.getString("app_theme", "System") ?: "System")
    val themeFlow: StateFlow<String> = _themeFlow

    fun setTheme(theme: String) {
        prefs.edit().putString("app_theme", theme).apply()
        _themeFlow.value = theme
    }

    companion object {
        @Volatile
        private var instance: ThemeManager? = null

        fun getInstance(context: Context): ThemeManager {
            return instance ?: synchronized(this) {
                instance ?: ThemeManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
