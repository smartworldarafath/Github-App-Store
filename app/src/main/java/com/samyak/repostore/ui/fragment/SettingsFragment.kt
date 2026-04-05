package com.samyak.repostore.ui.fragment

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.samyak.repostore.R
import com.samyak.repostore.data.api.RetrofitClient
import com.samyak.repostore.data.auth.GitHubAuth
import com.samyak.repostore.data.prefs.ThemePreferences
import com.samyak.repostore.databinding.FragmentSettingsBinding
import com.samyak.repostore.ui.activity.AboutActivity
import com.samyak.repostore.ui.activity.AppDeveloperActivity
import com.samyak.repostore.ui.activity.DonateActivity
import com.samyak.repostore.ui.activity.GitHubSignInActivity
import com.samyak.repostore.ui.activity.LicensesActivity
import com.samyak.repostore.ui.activity.FavoriteActivity
import com.samyak.repostore.ui.activity.DownloadSettingsActivity


import com.samyak.repostore.ui.dialog.showLanguageDialog

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAccountSection()
        setupLanguageSection()
        setupAppearanceSection()
        setupMyAppsSection()
        setupDownloadSettingsSection()
        setupAboutSection()
        setupDeveloperSection()
        setupDonateSection()
        setupLicensesSection()
        setupSourceCodeSection()
    }

    override fun onResume() {
        super.onResume()
        updateAccountStatus()
        // Refresh API client to pick up new auth
        RetrofitClient.refreshAuth()
    }

    private fun setupAccountSection() {
        updateAccountStatus()

        binding.accountCard.setOnClickListener {
            startActivity(Intent(requireContext(), GitHubSignInActivity::class.java))
        }
    }

    private fun updateAccountStatus() {
        val user = GitHubAuth.getUser(requireContext())
        val isSignedIn = GitHubAuth.isSignedIn(requireContext())

        if (isSignedIn && user != null) {
            // Show user info
            binding.tvAccountName.text = user.login
            binding.tvAccountStatus.text = getString(R.string.rate_limit_increased)
            
            // Load avatar
            if (!user.avatarUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(user.avatarUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_account)
                    .into(binding.ivAccountAvatar)
            }
        } else {
            // Show sign in prompt
            binding.tvAccountName.text = getString(R.string.github_sign_in)
            binding.tvAccountStatus.text = getString(R.string.sign_in_to_increase_limit)
            binding.ivAccountAvatar.setImageResource(R.drawable.ic_account)
        }
    }

    private fun setupLanguageSection() {
        updateCurrentLanguageText()

        binding.languageCard.setOnClickListener {
            showLanguageDialog()
        }
    }

    private fun updateCurrentLanguageText() {
        val currentLocales = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales()
        if (currentLocales.isEmpty) {
            binding.tvCurrentLanguage.text = getString(R.string.theme_system)
        } else {
            val currentLocale = currentLocales.get(0)
            if (currentLocale?.language == "hi" && currentLocale.country == "IN") {
                binding.tvCurrentLanguage.text = getString(R.string.hinglish)
            } else {
                binding.tvCurrentLanguage.text = currentLocale?.getDisplayName(currentLocale)
            }
        }
    }



    private fun setupAppearanceSection() {
        // Update selection state based on current theme
        updateThemeSelection()

        // Set click listeners for each theme option
        binding.themeSystemCard.setOnClickListener {
            selectTheme(ThemePreferences.THEME_SYSTEM)
        }

        binding.themeForYouCard.setOnClickListener {
            selectTheme(ThemePreferences.THEME_FOR_YOU)
        }

        binding.themeDarkCard.setOnClickListener {
            selectTheme(ThemePreferences.THEME_DARK)
        }

        binding.themeLightCard.setOnClickListener {
            selectTheme(ThemePreferences.THEME_LIGHT)
        }
    }

    private fun selectTheme(themeMode: Int) {
        ThemePreferences.setThemeMode(requireContext(), themeMode)
        updateThemeSelection()
    }

    private fun updateThemeSelection() {
        val currentTheme = ThemePreferences.getThemeMode(requireContext())
        
        // Helper to resolve theme attributes
        fun getThemeColor(attrId: Int): Int {
            val typedValue = android.util.TypedValue()
            requireContext().theme.resolveAttribute(attrId, typedValue, true)
            return typedValue.data
        }

        // Get colors for selected and unselected states
        // Use primary container for selected (or a distinct color)
        val selectedColor = try {
            getThemeColor(com.google.android.material.R.attr.colorPrimaryContainer)
        } catch (e: Exception) {
            ContextCompat.getColor(requireContext(), R.color.md_theme_primaryContainer)
        }
        
        // Use surface color for unselected (dynamic based on theme)
        val unselectedColor = try {
             getThemeColor(com.google.android.material.R.attr.colorSurfaceVariant)
        } catch (e: Exception) {
             ContextCompat.getColor(requireContext(), R.color.md_theme_surface)
        }
        
        val strokeColor = try {
            getThemeColor(com.google.android.material.R.attr.colorSurface)
        } catch (e: Exception) {
            ContextCompat.getColor(requireContext(), R.color.md_theme_primary)
        }
        
        // Helper function to update card visual state
        fun updateCard(card: MaterialCardView, isSelected: Boolean) {
            if (isSelected) {
                card.setCardBackgroundColor(selectedColor)
                card.strokeWidth = resources.getDimensionPixelSize(R.dimen.theme_card_stroke_width)
                card.strokeColor = strokeColor
            } else {
                card.setCardBackgroundColor(unselectedColor)
                card.strokeWidth = 0
            }
        }
        
        updateCard(binding.themeSystemCard, currentTheme == ThemePreferences.THEME_SYSTEM)
        updateCard(binding.themeForYouCard, currentTheme == ThemePreferences.THEME_FOR_YOU)
        updateCard(binding.themeDarkCard, currentTheme == ThemePreferences.THEME_DARK)
        updateCard(binding.themeLightCard, currentTheme == ThemePreferences.THEME_LIGHT)
    }

    private fun setupMyAppsSection() {
        binding.myAppsCard.setOnClickListener {
            startActivity(Intent(requireContext(), FavoriteActivity::class.java))
        }
    }


    private fun setupDownloadSettingsSection() {
        binding.downloadSettingsCard.setOnClickListener {
            startActivity(Intent(requireContext(), DownloadSettingsActivity::class.java))
        }
    }

    private fun setupAboutSection() {
        binding.aboutCard.setOnClickListener {
            startActivity(Intent(requireContext(), AboutActivity::class.java))
        }
    }

    private fun setupDeveloperSection() {
        binding.developerCard.setOnClickListener {
            startActivity(Intent(requireContext(), AppDeveloperActivity::class.java))
        }
    }

    private fun setupDonateSection() {
        binding.donateCard.setOnClickListener {
            startActivity(Intent(requireContext(), DonateActivity::class.java))
        }
    }

    private fun setupLicensesSection() {
        binding.licensesCard.setOnClickListener {
            startActivity(Intent(requireContext(), LicensesActivity::class.java))
        }
    }

    private fun setupSourceCodeSection() {
        binding.sourceCodeCard.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(SOURCE_CODE_URL))
                startActivity(intent)
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        // TODO: Replace with your actual GitHub repository URL
        private const val SOURCE_CODE_URL = "https://github.com/samyak2403/RepoStore"

        fun newInstance() = SettingsFragment()
    }
}
