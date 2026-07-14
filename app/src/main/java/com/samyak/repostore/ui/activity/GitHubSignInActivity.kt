package com.samyak.repostore.ui.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.samyak.repostore.R
import com.samyak.repostore.data.auth.GitHubAuth
import com.samyak.repostore.databinding.ActivityGithubSignInBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * GitHub Device Flow Sign In Activity
 * 
 * Flow:
 * 1. Request device code from GitHub
 * 2. Show user_code to user
 * 3. User opens GitHub URL and enters code
 * 4. App polls for access token
 * 5. Save token and finish
 */
class GitHubSignInActivity : BaseThemedActivity() {

    private lateinit var binding: ActivityGithubSignInBinding
    private var pollingJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityGithubSignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
        setupClickListeners()
        
        // Check if already signed in
        if (GitHubAuth.isSignedIn(this)) {
            showSignedInState()
        } else {
            startDeviceFlow()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        pollingJob?.cancel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        binding.btnCopyCode.setOnClickListener {
            val code = binding.tvUserCode.text.toString()
            if (code.isNotEmpty() && code != "--------") {
                copyToClipboard(code)
                Toast.makeText(this, R.string.code_copied, Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnOpenGithub.setOnClickListener {
            openGitHubVerification()
        }

        binding.btnSignOut.setOnClickListener {
            signOut()
        }

        binding.btnRetry.setOnClickListener {
            startDeviceFlow()
        }
    }

    private fun startDeviceFlow() {
        showLoadingState()

        lifecycleScope.launch {
            try {
                val deviceCode = GitHubAuth.requestDeviceCode()
                
                if (deviceCode != null) {
                    showCodeState(deviceCode.userCode, deviceCode.verificationUri)
                    startPolling(deviceCode.deviceCode, deviceCode.interval)
                } else {
                    showErrorState(getString(R.string.failed_to_get_code))
                }
            } catch (e: Exception) {
                showErrorState(e.message ?: getString(R.string.unknown_error))
            }
        }
    }

    private fun startPolling(deviceCode: String, interval: Int) {
        pollingJob?.cancel()
        
        pollingJob = lifecycleScope.launch {
            val result = GitHubAuth.pollForToken(deviceCode, interval)
            
            if (result != null && result.accessToken != null) {
                // Save token
                GitHubAuth.saveToken(this@GitHubSignInActivity, result.accessToken)
                
                // Fetch user info
                fetchUserInfo(result.accessToken)
            } else {
                showErrorState(getString(R.string.auth_expired))
            }
        }
    }

    private suspend fun fetchUserInfo(token: String) {
        try {
            val user = GitHubAuth.fetchUser(token)
            if (user != null) {
                GitHubAuth.saveUser(this, user)
                showSignedInState()
                Toast.makeText(this, getString(R.string.signed_in_as, user.login), Toast.LENGTH_SHORT).show()
            } else {
                showSignedInState()
            }
        } catch (e: Exception) {
            showSignedInState()
        }
    }

    private fun showLoadingState() {
        binding.layoutLoading.visibility = View.VISIBLE
        binding.layoutCode.visibility = View.GONE
        binding.layoutSignedIn.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
    }

    private fun showCodeState(userCode: String, verificationUri: String) {
        binding.layoutLoading.visibility = View.GONE
        binding.layoutCode.visibility = View.VISIBLE
        binding.layoutSignedIn.visibility = View.GONE
        binding.layoutError.visibility = View.GONE

        binding.tvUserCode.text = userCode
        binding.tvVerificationUrl.text = verificationUri
    }

    private fun showSignedInState() {
        binding.layoutLoading.visibility = View.GONE
        binding.layoutCode.visibility = View.GONE
        binding.layoutSignedIn.visibility = View.VISIBLE
        binding.layoutError.visibility = View.GONE

        val user = GitHubAuth.getUser(this)
        if (user != null) {
            binding.tvUsername.text = user.login
            binding.tvUserInfo.text = getString(R.string.rate_limit_increased)
        } else {
            binding.tvUsername.text = getString(R.string.signed_in)
            binding.tvUserInfo.text = getString(R.string.rate_limit_increased)
        }
    }

    private fun showErrorState(message: String) {
        binding.layoutLoading.visibility = View.GONE
        binding.layoutCode.visibility = View.GONE
        binding.layoutSignedIn.visibility = View.GONE
        binding.layoutError.visibility = View.VISIBLE

        binding.tvErrorMessage.text = message
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("GitHub Code", text)
        clipboard.setPrimaryClip(clip)
    }

    private fun openGitHubVerification() {
        val url = binding.tvVerificationUrl.text.toString()
        if (url.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
    }

    private fun signOut() {
        GitHubAuth.signOut(this)
        Toast.makeText(this, R.string.signed_out, Toast.LENGTH_SHORT).show()
        startDeviceFlow()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, GitHubSignInActivity::class.java)
        }
    }
}
