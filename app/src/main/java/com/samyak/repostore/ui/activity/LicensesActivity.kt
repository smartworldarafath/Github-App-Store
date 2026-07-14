package com.samyak.repostore.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.samyak.repostore.databinding.ActivityLicensesBinding
import com.samyak.repostore.ui.adapter.LicenseAdapter
import com.samyak.repostore.ui.adapter.LibraryInfo

class LicensesActivity : BaseThemedActivity() {

    private lateinit var binding: ActivityLicensesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLicensesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
        setupLicensesList()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupLicensesList() {
        val libraries = getLibraries()
        
        binding.rvLicenses.layoutManager = LinearLayoutManager(this)
        binding.rvLicenses.adapter = LicenseAdapter(libraries) { library ->
            openUrl(library.url)
        }
    }

    private fun getLibraries(): List<LibraryInfo> {
        return listOf(
            LibraryInfo(
                name = "AndroidX Core KTX",
                author = "Google",
                license = "Apache 2.0",
                url = "https://developer.android.com/jetpack/androidx"
            ),
            LibraryInfo(
                name = "AndroidX AppCompat",
                author = "Google",
                license = "Apache 2.0",
                url = "https://developer.android.com/jetpack/androidx"
            ),
            LibraryInfo(
                name = "Material Components",
                author = "Google",
                license = "Apache 2.0",
                url = "https://github.com/material-components/material-components-android"
            ),
            LibraryInfo(
                name = "AndroidX ConstraintLayout",
                author = "Google",
                license = "Apache 2.0",
                url = "https://developer.android.com/jetpack/androidx/releases/constraintlayout"
            ),
            LibraryInfo(
                name = "Retrofit",
                author = "Square",
                license = "Apache 2.0",
                url = "https://github.com/square/retrofit"
            ),
            LibraryInfo(
                name = "OkHttp",
                author = "Square",
                license = "Apache 2.0",
                url = "https://github.com/square/okhttp"
            ),
            LibraryInfo(
                name = "Gson",
                author = "Google",
                license = "Apache 2.0",
                url = "https://github.com/google/gson"
            ),
            LibraryInfo(
                name = "Kotlin Coroutines",
                author = "JetBrains",
                license = "Apache 2.0",
                url = "https://github.com/Kotlin/kotlinx.coroutines"
            ),
            LibraryInfo(
                name = "AndroidX Lifecycle",
                author = "Google",
                license = "Apache 2.0",
                url = "https://developer.android.com/jetpack/androidx/releases/lifecycle"
            ),
            LibraryInfo(
                name = "Glide",
                author = "Bump Technologies",
                license = "Apache 2.0",
                url = "https://github.com/bumptech/glide"
            ),
            LibraryInfo(
                name = "AndroidX Fragment KTX",
                author = "Google",
                license = "Apache 2.0",
                url = "https://developer.android.com/jetpack/androidx/releases/fragment"
            ),
            LibraryInfo(
                name = "SwipeRefreshLayout",
                author = "Google",
                license = "Apache 2.0",
                url = "https://developer.android.com/jetpack/androidx/releases/swiperefreshlayout"
            ),
            LibraryInfo(
                name = "Room Database",
                author = "Google",
                license = "Apache 2.0",
                url = "https://developer.android.com/jetpack/androidx/releases/room"
            ),
            LibraryInfo(
                name = "Markwon",
                author = "Dimitry Ivanov",
                license = "Apache 2.0",
                url = "https://github.com/noties/Markwon"
            ),
            LibraryInfo(
                name = "PhotoView",
                author = "GetStream",
                license = "Apache 2.0",
                url = "https://github.com/GetStream/photoview-android"
            )
        )
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            // Handle error silently
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, LicensesActivity::class.java)
        }
    }
}
