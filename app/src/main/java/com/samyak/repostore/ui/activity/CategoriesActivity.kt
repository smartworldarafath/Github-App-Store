package com.samyak.repostore.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.samyak.repostore.data.model.AppCategory
import com.samyak.repostore.databinding.ActivityCategoriesBinding
import com.samyak.repostore.ui.adapter.CategoryGridAdapter
import com.samyak.repostore.ui.viewmodel.ListType

class CategoriesActivity : BaseThemedActivity() {

    private lateinit var binding: ActivityCategoriesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
        setupCategoryGrid()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupCategoryGrid() {
        val categories = AppCategory.entries.filter { it != AppCategory.ALL }

        val adapter = CategoryGridAdapter(categories) { category ->
            // Open AppListActivity for the selected category
            val intent = AppListActivity.newIntent(
                this,
                ListType.CATEGORY,
                getString(category.titleRes),
                category.name
            )
            startActivity(intent)
        }

        binding.rvCategories.apply {
            this.adapter = adapter
            layoutManager = GridLayoutManager(this@CategoriesActivity, 3)
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, CategoriesActivity::class.java)
        }
    }
}
