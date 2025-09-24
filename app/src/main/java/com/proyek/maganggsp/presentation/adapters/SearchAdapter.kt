// File: app/src/main/java/com/proyek/maganggsp/presentation/adapters/SearchAdapter.kt
package com.proyek.maganggsp.presentation.adapters

import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.proyek.maganggsp.R
import com.proyek.maganggsp.databinding.ItemLoketHistoryBinding
import com.proyek.maganggsp.domain.model.Receipt

/**
 * STREAMLINED: Search adapter for PPID-based search results
 * Same as ReceiptAdapter but with search-specific optimizations
 * Usage: HomeFragment (search results display)
 */
class SearchAdapter(
    private val onResultClick: (Receipt) -> Unit
) : ListAdapter<Receipt, SearchAdapter.SearchResultViewHolder>(ReceiptDiffCallback()) {

    private var searchQuery: String = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val binding = ItemLoketHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SearchResultViewHolder(binding, onResultClick)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bind(getItem(position), searchQuery)
    }

    class SearchResultViewHolder(
        private val binding: ItemLoketHistoryBinding,
        private val onResultClick: (Receipt) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(receipt: Receipt, query: String) {
            with(binding) {
                // Highlight search matches
                tvLoketName.text = highlightSearchMatch(receipt.getDisplayTitle(), query)
                tvLoketPhone.text = highlightSearchMatch(receipt.getDisplayPhone(), query)
                tvNomorLoket.text = highlightSearchMatch("#${receipt.ppid}", query)

                // Show search context
                tvLastAccessed.text = "PPID: ${receipt.ppid}"
                tvLastAccessed.visibility = android.view.View.VISIBLE

                root.setOnClickListener {
                    AppUtils.logDebug("SearchAdapter", "Search result clicked: ${receipt.ppid}")
                    onResultClick(receipt)
                }
            }
        }

        private fun highlightSearchMatch(text: String, query: String): CharSequence {
            if (query.isBlank() || !text.contains(query, ignoreCase = true)) {
                return text
            }

            val spannableString = SpannableString(text)
            val startIndex = text.indexOf(query, ignoreCase = true)
            if (startIndex >= 0) {
                val highlightColor = ContextCompat.getColor(
                    binding.root.context,
                    R.color.yellow_secondary_accent
                )
                spannableString.setSpan(
                    BackgroundColorSpan(highlightColor),
                    startIndex,
                    startIndex + query.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            return spannableString
        }
    }

    // Update search results with query context
    fun updateSearchResults(results: List<Receipt>, query: String) {
        searchQuery = query
        submitList(results)
        AppUtils.logDebug("SearchAdapter", "Search results updated: ${results.size} results for '$query'")
    }

    fun clearSearchResults() {
        searchQuery = ""
        submitList(emptyList())
    }

    class ReceiptDiffCallback : DiffUtil.ItemCallback<Receipt>() {
        override fun areItemsTheSame(oldItem: Receipt, newItem: Receipt): Boolean {
            return oldItem.refNumber == newItem.refNumber && oldItem.ppid == newItem.ppid
        }

        override fun areContentsTheSame(oldItem: Receipt, newItem: Receipt): Boolean {
            return oldItem == newItem
        }
    }
}