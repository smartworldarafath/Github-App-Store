package com.samyak.repostore.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.samyak.repostore.R
import com.samyak.repostore.data.prefs.ThemePreferences

/**
 * Base activity that applies the Liquid Glass theme when active.
 * All activities should extend this to get consistent theming.
 */
open class BaseThemedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply Liquid Glass theme before super.onCreate
        if (ThemePreferences.isLiquidGlass(this)) {
            setTheme(R.style.Theme_RepoStore_LiquidGlass)
        }
        super.onCreate(savedInstanceState)
    }
}
