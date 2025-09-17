// File: app/src/main/java/com/proyek/maganggsp/presentation/adapters/ReceiptAdapter.kt - NEW
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
 * STREAMLINED: Receipt adapter for home screen
 * Reuses existing item_loket_history.xml layout, focused on receipt data
 */
class ReceiptAdapter(
    private val onReceiptClick: (Receipt) -> Unit
) : ListAdapter<Receipt, ReceiptAdapter.ReceiptViewHolder>(ReceiptDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptViewHolder {
        val binding = ItemLoketHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReceiptViewHolder(binding, onReceiptClick)
    }

    override fun onBindViewHolder(holder: ReceiptViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ReceiptViewHolder(
        private val binding: ItemLoketHistoryBinding,
        private val onReceiptClick: (Receipt) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(receipt: Receipt) {
            with(binding) {
                // Primary display: Receipt info
                tvLoketName.text = receipt.getDisplayTitle()
                tvLoketPhone.text = receipt.getDisplayPhone()
                tvNomorLoket.text = "#${receipt.refNumber}"

                // Show formatted amount if available
                if (receipt.amount != 0L) {
                    tvLastAccessed.text = "Nominal: ${receipt.getFormattedAmount()}"
                    tvLastAccessed.visibility = android.view.View.VISIBLE
                } else {
                    tvLastAccessed.visibility = android.view.View.GONE
                }

                // Click listener - navigate with PPID
                root.setOnClickListener {
                    onReceiptClick(receipt)
                }
            }
        }
    }

    class ReceiptDiffCallback : DiffUtil.ItemCallback<Receipt>() {
        override fun areItemsTheSame(oldItem: Receipt, newItem: Receipt): Boolean {
            return oldItem.refNumber == newItem.refNumber && oldItem.ppid == newItem.ppid
        }

        override fun areContentsTheSame(oldItem: Receipt, newItem: Receipt): Boolean {
            return oldItem == newItem
        }
    }

    /**
     * STREAMLINED: Helper methods
     */
    fun updateReceipts(receipts: List<Receipt>) {
        submitList(receipts)
        AppUtils.logDebug("ReceiptAdapter", "Updated with ${receipts.size} receipts")
    }

    fun clearReceipts() {
        submitList(emptyList())
    }

    fun getReceiptAtPosition(position: Int): Receipt? {
        return if (position in 0 until itemCount) getItem(position) else null
    }
}



