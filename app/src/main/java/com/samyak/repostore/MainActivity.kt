package com.samyak.repostore

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.samyak.repostore.data.prefs.ThemePreferences
import com.samyak.repostore.databinding.ActivityMainBinding
import com.samyak.repostore.ui.fragment.GameFragment
import com.samyak.repostore.ui.fragment.HomeFragment
import com.samyak.repostore.ui.fragment.SearchFragment
import com.samyak.repostore.ui.fragment.SettingsFragment
import com.samyak.repostore.ui.fragment.TrendingFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply Liquid Glass theme before super.onCreate
        if (ThemePreferences.isLiquidGlass(this)) {
            setTheme(R.style.Theme_RepoStore_LiquidGlass)
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        setupBottomNavigation()

        if (savedInstanceState == null) {
            loadFragment(GameFragment.newInstance())
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(GameFragment.newInstance())
                    true
                }
                R.id.nav_apps -> {
                    loadFragment(HomeFragment.newInstance())
                    true
                }
                R.id.nav_trending -> {
                    loadFragment(TrendingFragment.newInstance())
                    true
                }
                R.id.nav_search -> {
                    loadFragment(SearchFragment.newInstance())
                    true
                }
                R.id.nav_settings -> {
                    loadFragment(SettingsFragment.newInstance())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
