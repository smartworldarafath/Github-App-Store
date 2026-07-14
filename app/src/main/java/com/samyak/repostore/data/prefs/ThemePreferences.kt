package com.samyak.repostore.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

/**
 * Manages theme/appearance preferences.
 * Supports: System, For You (Dynamic), Dark, Light, Liquid Glass modes.
 */
object ThemePreferences {

    private const val PREFS_NAME = "theme_preferences"
    private const val KEY_THEME_MODE = "theme_mode"

    // Theme mode constants
    const val THEME_SYSTEM = 0
    const val THEME_FOR_YOU = 1  // Dynamic - follows system but can be personalized
    const val THEME_DARK = 2
    const val THEME_LIGHT = 3
    const val THEME_LIQUID_GLASS = 4  // iOS 26-style frosted glass look

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Get the current theme mode
     */
    fun getThemeMode(context: Context): Int {
        return getPrefs(context).getInt(KEY_THEME_MODE, THEME_SYSTEM)
    }

    /**
     * Set the theme mode and apply it
     */
    fun setThemeMode(context: Context, mode: Int) {
        getPrefs(context).edit().putInt(KEY_THEME_MODE, mode).apply()
        applyTheme(mode)
    }

    /**
     * Apply the theme based on mode
     */
    fun applyTheme(mode: Int) {
        val nightMode = when (mode) {
            THEME_SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            THEME_FOR_YOU -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM  // Similar to system, personalized
            THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            THEME_LIQUID_GLASS -> AppCompatDelegate.MODE_NIGHT_NO  // Light base with glass overlays
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    /**
     * Check if the current theme is Liquid Glass
     */
    fun isLiquidGlass(context: Context): Boolean {
        return getThemeMode(context) == THEME_LIQUID_GLASS
    }

    /**
     * Apply saved theme on app start
     */
    fun applySavedTheme(context: Context) {
        val mode = getThemeMode(context)
        applyTheme(mode)
    }
}