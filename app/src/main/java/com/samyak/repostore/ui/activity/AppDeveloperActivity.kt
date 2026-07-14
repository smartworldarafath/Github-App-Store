package com.samyak.repostore.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.samyak.repostore.R
import com.samyak.repostore.data.api.RetrofitClient
import com.samyak.repostore.data.model.GitHubUser
import com.samyak.repostore.databinding.ActivityAppDeveloperBinding
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class AppDeveloperActivity : BaseThemedActivity() {

    private lateinit var binding: ActivityAppDeveloperBinding
    private var githubUser: GitHubUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAppDeveloperBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
        loadDeveloperInfo()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadDeveloperInfo() {
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val userDeferred = async { RetrofitClient.api.getUser(DEVELOPER_USERNAME) }
                val contributorsDeferred = async { 
                    try {
                        RetrofitClient.api.getContributors(DEVELOPER_USERNAME, "Github-App-Store")
                    } catch (e: Exception) {
                        emptyList()
                    }
                }
                
                val user = userDeferred.await()
                val contributors = contributorsDeferred.await()
                
                githubUser = user
                displayUserInfo(user)
                displayContributors(contributors)
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.tvError.visibility = View.VISIBLE
                binding.tvError.text = getString(R.string.network_error)
            }
        }
    }

    private fun displayUserInfo(user: GitHubUser) {
        binding.progressBar.visibility = View.GONE
        
        Glide.with(this)
            .load(user.avatarUrl)
            .placeholder(R.drawable.ic_app_placeholder)
            .into(binding.ivAvatar)

        binding.tvName.text = user.name ?: user.login
        binding.tvUsername.text = getString(R.string.username_format, user.login)
        
        if (!user.bio.isNullOrEmpty()) {
            binding.tvBio.text = user.bio
            binding.tvBio.visibility = View.VISIBLE
        }

        binding.tvReposCount.text = user.publicRepos.toString()
        binding.tvFollowersCount.text = user.followers.toString()
        binding.tvFollowingCount.text = user.following.toString()

        if (!user.location.isNullOrEmpty()) {
            binding.layoutLocation.visibility = View.VISIBLE
            binding.tvLocation.text = user.location
        }

        if (!user.company.isNullOrEmpty()) {
            binding.layoutCompany.visibility = View.VISIBLE
            binding.tvCompany.text = user.company
        }

        if (!user.blog.isNullOrEmpty()) {
            binding.layoutBlog.visibility = View.VISIBLE
            binding.tvBlog.text = user.blog
            binding.layoutBlog.setOnClickListener {
                val url = if (user.blog.startsWith("http")) user.blog else "https://${user.blog}"
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }

        binding.btnViewGithub.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(user.htmlUrl)))
        }
    }

    private fun displayContributors(contributors: List<GitHubUser>) {
        if (contributors.isEmpty()) {
            binding.layoutContributors.visibility = View.GONE
            return
        }
        
        binding.layoutContributors.visibility = View.VISIBLE
        binding.containerContributors.removeAllViews()
        
        val maxToShow = 6
        val toShow = contributors.take(maxToShow)
        
        for (contributor in toShow) {
            val avatarView = layoutInflater.inflate(R.layout.item_contributor_avatar, binding.containerContributors, false)
            val imageView = avatarView.findViewById<com.google.android.material.imageview.ShapeableImageView>(R.id.iv_contributor_avatar)
            
            Glide.with(this)
                .load(contributor.avatarUrl)
                .placeholder(R.drawable.ic_app_placeholder)
                .into(imageView)
                
            avatarView.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(contributor.htmlUrl)))
            }
            
            binding.containerContributors.addView(avatarView)
        }
        
        if (contributors.size > maxToShow) {
            val remaining = contributors.size - maxToShow
            val moreText = android.widget.TextView(this).apply {
                text = "+$remaining"
                textSize = 14f
                setPadding(24, 0, 24, 0)
                gravity = android.view.Gravity.CENTER
                
                // Fetch dynamic text and background color programmatically using Material variables
                val typedValue = android.util.TypedValue()
                context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurfaceVariant, typedValue, true)
                setTextColor(typedValue.data)
                
                // Add fixed height
                val heightPx = (40f * resources.displayMetrics.density).toInt()
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    heightPx
                )
            }
            binding.containerContributors.addView(moreText)
        }
    }

    companion object {
        private const val DEVELOPER_USERNAME = "smartworldarafath"

        fun newIntent(context: Context): Intent {
            return Intent(context, AppDeveloperActivity::class.java)
        }
    }
}
