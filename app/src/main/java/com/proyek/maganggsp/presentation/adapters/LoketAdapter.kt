

// File: app/src/main/java/com/proyek/maganggsp/presentation/adapters/LoketAdapter.kt - NEW
package com.proyek.maganggsp.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.proyek.maganggsp.R
import com.proyek.maganggsp.databinding.ItemLoketHistoryBinding
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.model.LoketStatus
import com.proyek.maganggsp.util.AppUtils
import android.util.Log

/**
 * NEW ADAPTER: LoketAdapter for full Loket objects
 * Uses existing item_loket_history.xml layout but maps Loket fields with status indicators
 */
class LoketAdapter : RecyclerView.Adapter<LoketAdapter.LoketViewHolder>() {

    companion object {
        private const val TAG = "LoketAdapter"
    }

    private var onItemClickListener: ((Loket) -> Unit)? = null

    fun setOnItemClickListener(listener: (Loket) -> Unit) {
        onItemClickListener = listener
    }

    private val differCallback = object : DiffUtil.ItemCallback<Loket>() {
        override fun areItemsTheSame(oldItem: Loket, newItem: Loket): Boolean {
            return oldItem.ppid == newItem.ppid
        }

        override fun areContentsTheSame(oldItem: Loket, newItem: Loket): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    inner class LoketViewHolder(
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

        fun bind(loket: Loket) {
            val context = binding.root.context
            binding.apply {
                // Map Loket fields to existing layout
                tvLoketName.text = loket.namaLoket
                tvLoketPhone.text = loket.nomorHP
                tvNomorLoket.text = loket.ppid

                // Show status with color coding
                when (loket.status) {
                    LoketStatus.NORMAL -> {
                        layoutLoketItem.background = ContextCompat.getDrawable(context, R.color.white)
                        tvLastAccessed.visibility = android.view.View.VISIBLE
                        tvLastAccessed.text = "Status: ${loket.getStatusDisplayText()}"
                        tvLastAccessed.setTextColor(ContextCompat.getColor(context, R.color.text_secondary_gray))
                    }
                    LoketStatus.BLOCKED -> {
                        layoutLoketItem.background = ContextCompat.getDrawable(context, R.color.status_danger_background)
                        tvLastAccessed.visibility = android.view.View.VISIBLE
                        tvLastAccessed.text = "Status: ${loket.getStatusDisplayText()}"
                        tvLastAccessed.setTextColor(ContextCompat.getColor(context, R.color.red_danger))
                    }
                    LoketStatus.FLAGGED -> {
                        layoutLoketItem.background = ContextCompat.getDrawable(context, R.color.status_warning_background)
                        tvLastAccessed.visibility = android.view.View.VISIBLE
                        tvLastAccessed.text = "Status: ${loket.getStatusDisplayText()}"
                        tvLastAccessed.setTextColor(ContextCompat.getColor(context, R.color.yellow_secondary_accent))
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoketViewHolder {
        val binding = ItemLoketHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LoketViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LoketViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    // Helper methods
    fun updateData(newLokets: List<Loket>) {
        differ.submitList(newLokets.toList())
        Log.d(TAG, "Updated with ${newLokets.size} lokets")
    }

    fun clearData() {
        differ.submitList(emptyList())
        Log.d(TAG, "Cleared loket data")
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        onItemClickListener = null
    }
}