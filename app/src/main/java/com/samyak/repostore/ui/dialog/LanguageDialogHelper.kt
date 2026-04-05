package com.samyak.repostore.ui.dialog

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.samyak.repostore.R

/**
 * Helper extension to show the searchable language selection dialog.
 */
fun Fragment.showLanguageDialog() {
    val context = requireContext()
    val rawLocales = context.assets.locales ?: arrayOf()

    // Filter empty and distinct by full tag to build a list
    val availableLocales = rawLocales
        .filter { it.isNotEmpty() }
        .map { java.util.Locale.forLanguageTag(it) }
        .distinctBy { it.toLanguageTag() }
        .sortedBy { it.getDisplayName(it) }

    // Build display names: "System" + all available languages
    // Each entry stores display name + English name for searching
    data class LangItem(val displayName: String, val englishName: String, val locale: java.util.Locale?)
    
    val allItems = mutableListOf<LangItem>()
    allItems.add(LangItem(getString(R.string.theme_system), "System", null))
    allItems.addAll(availableLocales.map { locale ->
        var nativeName = locale.getDisplayName(locale)
        var englishName = locale.getDisplayName(java.util.Locale.ENGLISH)
        
        // Special case for Hinglish (Hindi-India in Latin script)
        if (locale.language == "hi" && locale.country == "IN") {
            nativeName = getString(R.string.hinglish)
            englishName = getString(R.string.hinglish)
        }
        
        LangItem(nativeName, englishName, locale)
    })

    val currentLocales = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales()
    var selectedTag: String? = null
    if (!currentLocales.isEmpty) {
        selectedTag = currentLocales.get(0)?.toLanguageTag()
    }

    // Build custom layout with search EditText + ListView
    val container = android.widget.LinearLayout(context).apply {
        orientation = android.widget.LinearLayout.VERTICAL
        setPadding(0, 0, 0, 0)
    }

    // Search EditText
    val searchBox = android.widget.EditText(context).apply {
        hint = getString(R.string.search_language)
        inputType = android.text.InputType.TYPE_CLASS_TEXT
        setSingleLine(true)
        val dp16 = (16 * resources.displayMetrics.density).toInt()
        setPadding(dp16, dp16, dp16, dp16)
        val searchIcon = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.ic_search)?.mutate()
        if (searchIcon != null) {
            val primaryColor = androidx.core.content.ContextCompat.getColor(context, R.color.primary)
            androidx.core.graphics.drawable.DrawableCompat.setTint(searchIcon, primaryColor)
            setCompoundDrawablesWithIntrinsicBounds(searchIcon, null, null, null)
        }
        compoundDrawablePadding = (8 * resources.displayMetrics.density).toInt()
    }
    container.addView(searchBox)

    // Divider
    val divider = android.view.View(context).apply {
        layoutParams = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            (1 * resources.displayMetrics.density).toInt()
        )
        setBackgroundColor(android.graphics.Color.parseColor("#30808080"))
    }
    container.addView(divider)

    // ListView for language items
    val listView = android.widget.ListView(context).apply {
        layoutParams = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            (350 * resources.displayMetrics.density).toInt()
        )
        dividerHeight = 0
    }
    container.addView(listView)

    // Filtered items list
    var filteredItems = allItems.toMutableList()

    // Custom adapter
    val adapter = object : android.widget.BaseAdapter() {
        override fun getCount() = filteredItems.size
        override fun getItem(position: Int) = filteredItems[position]
        override fun getItemId(position: Int) = position.toLong()
        override fun getView(position: Int, convertView: android.view.View?, parent: ViewGroup): android.view.View {
            val item = filteredItems[position]
            val isSelected = if (item.locale == null) {
                selectedTag == null
            } else {
                item.locale.toLanguageTag() == selectedTag
            }

            // Define colors directly from colors.xml
            val primaryColor = androidx.core.content.ContextCompat.getColor(context, R.color.primary)
            val containerColor = androidx.core.content.ContextCompat.getColor(context, R.color.transparent)
            val onSurfaceColor = androidx.core.content.ContextCompat.getColor(context, R.color.white)

            val dp8 = (8 * resources.displayMetrics.density).toInt()
            val dp12 = (12 * resources.displayMetrics.density).toInt()
            val dp16 = (16 * resources.displayMetrics.density).toInt()

            val row = android.widget.LinearLayout(context).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                
                val params = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(dp12, dp8, dp12, dp8)
                layoutParams = params
                
                setPadding(dp16, dp12, dp16, dp12)

                if (isSelected) {
                    val bg = android.graphics.drawable.GradientDrawable().apply {
                        cornerRadius = (12 * resources.displayMetrics.density)
                        setColor(containerColor)
                        setStroke(
                            (2 * resources.displayMetrics.density).toInt(),
                            primaryColor
                        )
                    }
                    background = bg
                } else {
                    val bg = android.graphics.drawable.GradientDrawable().apply {
                        cornerRadius = (12 * resources.displayMetrics.density)
                        setColor(android.graphics.Color.TRANSPARENT)
                    }
                    background = bg
                }
            }

            val radio = android.widget.RadioButton(context).apply {
                isChecked = isSelected
                isClickable = false
                isFocusable = false
                buttonTintList = android.content.res.ColorStateList.valueOf(primaryColor)
            }
            row.addView(radio)

            // Text container
            val textContainer = android.widget.LinearLayout(context).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                setPadding(dp12, 0, 0, 0)
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            // Native name (primary)
            val nameView = android.widget.TextView(context).apply {
                text = item.displayName
                textSize = 16f
                setTextColor(if (isSelected) primaryColor else onSurfaceColor)
            }
            textContainer.addView(nameView)

            // English name (secondary, only if different from native)
            if (item.englishName != item.displayName && item.locale != null) {
                val subView = android.widget.TextView(context).apply {
                    text = item.englishName
                    textSize = 13f
                    setTextColor(android.graphics.Color.GRAY)
                }
                textContainer.addView(subView)
            }

            row.addView(textContainer)
            return row
        }
    }
    listView.adapter = adapter

    val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
        .setTitle(R.string.language)
        .setView(container)
        .setNegativeButton(R.string.cancel, null)
        .create()

    // Item click -> select language & dismiss
    listView.setOnItemClickListener { _, _, position, _ ->
        val item = filteredItems[position]
        val newLocaleList = if (item.locale == null) {
            androidx.core.os.LocaleListCompat.getEmptyLocaleList()
        } else {
            androidx.core.os.LocaleListCompat.create(item.locale)
        }
        androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(newLocaleList)
        dialog.dismiss()
    }

    // Real-time search/filter
    searchBox.addTextChangedListener(object : android.text.TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val query = s?.toString()?.lowercase() ?: ""
            filteredItems = if (query.isEmpty()) {
                allItems.toMutableList()
            } else {
                allItems.filter { 
                    it.displayName.lowercase().contains(query) || 
                    it.englishName.lowercase().contains(query) 
                }.toMutableList()
            }
            adapter.notifyDataSetChanged()
        }
        override fun afterTextChanged(s: android.text.Editable?) {}
    })

    dialog.show()
}
