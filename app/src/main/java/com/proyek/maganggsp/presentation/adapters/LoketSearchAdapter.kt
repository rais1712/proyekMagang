// File: app/src/main/java/com/proyek/maganggsp/presentation/adapters/SearchAdapter.kt - UNIFIED SEARCH
package com.proyek.maganggsp.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.proyek.maganggsp.databinding.ItemLoketHistoryBinding
import com.proyek.maganggsp.domain.model.Receipt
import com.proyek.maganggsp.util.AppUtils

/**
 * UNIFIED: Search adapter for PPID-based search results
 * Same as ReceiptAdapter but with search-specific optimizations
 */
class LoketSearchAdapter(
    private val onResultClick: (Receipt) -> Unit
) : ListAdapter<Receipt, LoketSearchAdapter.SearchResultViewHolder>(ReceiptDiffCallback()) {

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
                    onResultClick(receipt)
                }
            }
        }

        private fun highlightSearchMatch(text: String, query: String): String {
            // Simple highlighting - could be enhanced with SpannableString
            return if (query.isNotBlank() && text.contains(query, ignoreCase = true)) {
                text.replace(query, "[$query]", ignoreCase = true)
            } else {
                text
            }
        }
    }

    /**
     * Update search results with query context
     */
    fun updateSearchResults(results: List<Receipt>, query: String) {
        searchQuery = query
        submitList(results)
        AppUtils.logDebug("SearchAdapter", "Search results updated: ${results.size} results for '$query'")
    }

    fun clearSearchResults() {
        searchQuery = ""
        submitList(emptyList())
    }
}