// File: app/src/main/java/com/proyek/maganggsp/presentation/adapters/LoketSearchAdapter.kt - NEW
package com.proyek.maganggsp.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.proyek.maganggsp.databinding.ItemLoketHistoryBinding
import com.proyek.maganggsp.domain.model.LoketSearchHistory
import com.proyek.maganggsp.util.AppUtils
import android.util.Log

/**
 * NEW ADAPTER: LoketSearchAdapter for search results and recent history
 * Uses existing item_loket_history.xml layout but maps LoketSearchHistory fields
 */
class LoketSearchAdapter : RecyclerView.Adapter<LoketSearchAdapter.LoketSearchViewHolder>() {

    companion object {
        private const val TAG = "LoketSearchAdapter"
    }

    private var onItemClickListener: ((LoketSearchHistory) -> Unit)? = null

    fun setOnItemClickListener(listener: (LoketSearchHistory) -> Unit) {
        onItemClickListener = listener
    }

    private val differCallback = object : DiffUtil.ItemCallback<LoketSearchHistory>() {
        override fun areItemsTheSame(oldItem: LoketSearchHistory, newItem: LoketSearchHistory): Boolean {
            return oldItem.ppid == newItem.ppid
        }

        override fun areContentsTheSame(oldItem: LoketSearchHistory, newItem: LoketSearchHistory): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    inner class LoketSearchViewHolder(
        private val binding: ItemLoketHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION && position < differ.currentList.size) {
                    onItemClickListener?.invoke(differ.currentList[position])
                }
            }
        }

        fun bind(loketHistory: LoketSearchHistory) {
            binding.apply {
                // Map LoketSearchHistory fields to existing layout
                tvLoketName.text = loketHistory.namaLoket
                tvLoketPhone.text = loketHistory.nomorHP
                tvNomorLoket.text = loketHistory.ppid

                // Show access frequency and time
                if (loketHistory.jumlahAkses > 1) {
                    tvLastAccessed.visibility = android.view.View.VISIBLE
                    tvLastAccessed.text = "Diakses ${loketHistory.jumlahAkses}x - ${loketHistory.getFormattedTanggalAkses()}"
                } else {
                    tvLastAccessed.visibility = android.view.View.VISIBLE
                    tvLastAccessed.text = "Terakhir: ${loketHistory.getFormattedTanggalAkses()}"
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoketSearchViewHolder {
        val binding = ItemLoketHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LoketSearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LoketSearchViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    // Helper methods
    fun updateData(newLoketHistory: List<LoketSearchHistory>) {
        differ.submitList(newLoketHistory.toList())
        Log.d(TAG, "Updated with ${newLoketHistory.size} loket history items")
    }

    fun clearData() {
        differ.submitList(emptyList())
        Log.d(TAG, "Cleared loket history data")
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        onItemClickListener = null
    }
}