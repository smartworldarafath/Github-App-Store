package com.samyak.repostore.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.samyak.repostore.ui.widget.ShimmerFrameLayout
import com.samyak.repostore.R
import com.samyak.repostore.RepoStoreApp
import com.samyak.repostore.databinding.ActivityAppListBinding
import com.samyak.repostore.ui.adapter.RankedAppAdapter
import com.samyak.repostore.ui.viewmodel.AppListViewModel
import com.samyak.repostore.ui.viewmodel.AppListViewModelFactory
import com.samyak.repostore.ui.viewmodel.ListType
import com.samyak.repostore.ui.viewmodel.AppListUiState
import com.samyak.repostore.util.RateLimitDialog
import kotlinx.coroutines.launch

class AppListActivity : BaseThemedActivity() {

    private lateinit var binding: ActivityAppListBinding
    
    private val viewModel: AppListViewModel by viewModels {
        AppListViewModelFactory(
            (application as RepoStoreApp).repository,
            intent.getStringExtra(EXTRA_LIST_TYPE) ?: ListType.TRENDING.name,
            intent.getStringExtra(EXTRA_CATEGORY)
        )
    }

    private lateinit var appAdapter: RankedAppAdapter
    
    // Shimmer layout for skeleton loading
    private var shimmerLayout: ShimmerFrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAppListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize shimmer layout
        shimmerLayout = findViewById(R.id.skeleton_layout)

        setupToolbar()
        setupRecyclerView()
        setupSwipeRefresh()
        observeViewModel()
    }

    private fun setupToolbar() {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: getString(R.string.apps)
        binding.toolbar.title = title
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        appAdapter = RankedAppAdapter(
            onItemClick = { appItem ->
                val intent = DetailActivity.newIntent(
                    this,
                    appItem.repo.owner.login,
                    appItem.repo.name
                )
                startActivity(intent)
            }
        )

        binding.rvApps.apply {
            adapter = appAdapter
            layoutManager = LinearLayoutManager(this@AppListActivity)

            // Pagination
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                    if (lastVisibleItem >= totalItemCount - 5 && dy > 0) {
                        viewModel.loadMore()
                    }
                }
            })
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    handleUiState(state)
                }
            }
        }
    }

    private fun handleUiState(state: AppListUiState) {
        binding.swipeRefresh.isRefreshing = false

        when (state) {
            is AppListUiState.Loading -> {
                showSkeleton()
                binding.rvApps.visibility = View.GONE
                binding.tvError.visibility = View.GONE
            }

            is AppListUiState.LoadingMore -> {
                hideSkeleton()
                binding.rvApps.visibility = View.VISIBLE
                binding.tvError.visibility = View.GONE
                appAdapter.submitList(state.currentApps)
            }

            is AppListUiState.Success -> {
                hideSkeleton()
                binding.rvApps.visibility = View.VISIBLE
                binding.tvError.visibility = View.GONE
                appAdapter.submitList(state.apps)
            }

            is AppListUiState.Empty -> {
                hideSkeleton()
                binding.rvApps.visibility = View.GONE
                binding.tvError.visibility = View.VISIBLE
                binding.tvError.text = getString(R.string.no_apps_found)
            }

            is AppListUiState.Error -> {
                hideSkeleton()
                binding.rvApps.visibility = View.GONE
                binding.tvError.visibility = View.VISIBLE
                binding.tvError.text = "${state.message}\n\n${getString(R.string.tap_to_retry)}"
                binding.tvError.setOnClickListener {
                    viewModel.retry()
                }
                
                // Show rate limit dialog if applicable
                RateLimitDialog.showIfNeeded(this, state.message)
            }
        }
    }
    
    private fun showSkeleton() {
        shimmerLayout?.apply {
            visibility = View.VISIBLE
            startShimmer()
        }
    }
    
    private fun hideSkeleton() {
        shimmerLayout?.apply {
            stopShimmer()
            visibility = View.GONE
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        shimmerLayout = null
    }

    companion object {
        private const val EXTRA_LIST_TYPE = "list_type"
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_CATEGORY = "category"

        fun newIntent(context: Context, listType: ListType, title: String, category: String? = null): Intent {
            return Intent(context, AppListActivity::class.java).apply {
                putExtra(EXTRA_LIST_TYPE, listType.name)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_CATEGORY, category)
            }
        }
    }
}
