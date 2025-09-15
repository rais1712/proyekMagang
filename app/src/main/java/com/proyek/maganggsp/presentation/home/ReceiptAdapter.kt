// File: app/src/main/java/com/proyek/maganggsp/presentation/home/ReceiptAdapter.kt
package com.proyek.maganggsp.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.proyek.maganggsp.databinding.ItemLoketHistoryBinding
import com.proyek.maganggsp.util.AppUtils

/**
 * NEW ADAPTER: ReceiptAdapter replaces HistoryAdapter for Receipt data
 * Reuses existing item_loket_history.xml layout but maps Receipt fields
 */
class ReceiptAdapter : RecyclerView.Adapter<ReceiptAdapter.ReceiptViewHolder>() {

    private var onItemClickListener: ((Receipt) -> Unit)? = null

    fun setOnItemClickListener(listener: (Receipt) -> Unit) {
        onItemClickListener = listener
    }

    private val differCallback = object : DiffUtil.ItemCallback<Receipt>() {
        override fun areItemsTheSame(oldItem: Receipt, newItem: Receipt): Boolean {
            return oldItem.refNumber == newItem.refNumber
        }

        override fun areContentsTheSame(oldItem: Receipt, newItem: Receipt): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    inner class ReceiptViewHolder(private val binding: ItemLoketHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val currentList = differ.currentList
                    if (position < currentList.size) {
                        onItemClickListener?.invoke(currentList[position])
                    }
                }
            }
        }

        fun bind(receipt: Receipt) {
            binding.apply {
                // Map Receipt fields to existing layout
                tvLoketName.text = "Receipt #${receipt.refNumber}"
                tvLoketPhone.text = "ID: ${receipt.idPelanggan}"
                tvNomorLoket.text = AppUtils.formatCurrency(receipt.amount)

                // Show logged timestamp if available
                if (receipt.logged.isNotBlank() && receipt.logged != "-") {
                    tvLastAccessed.visibility = android.view.View.VISIBLE
                    tvLastAccessed.text = "Logged: ${AppUtils.formatDate(receipt.logged)}"
                } else {
                    tvLastAccessed.visibility = android.view.View.GONE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptViewHolder {
        val binding = ItemLoketHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReceiptViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReceiptViewHolder, position: Int) {
        val receipt = differ.currentList[position]
        holder.bind(receipt)
    }

    override fun getItemCount(): Int = differ.currentList.size

    // Helper methods
    fun updateData(newReceipts: List<Receipt>) {
        differ.submitList(newReceipts.toList())
    }

    fun clearData() {
        differ.submitList(emptyList())
    }
}