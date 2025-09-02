// File: app/src/main/java/com/proyek/maganggsp/presentation/detail_loket/TransactionLogAdapter.kt
package com.proyek.maganggsp.presentation.detail_loket

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.proyek.maganggsp.R
import com.proyek.maganggsp.databinding.ItemMutasiBinding
import com.proyek.maganggsp.domain.model.TransactionLog
import com.proyek.maganggsp.util.AppUtils
import android.util.Log

/**
 * NEW ADAPTER: TransactionLogAdapter replaces MutasiAdapter for TransactionLog data
 * Reuses existing item_mutasi.xml layout but maps TransactionLog fields
 */
class TransactionLogAdapter : RecyclerView.Adapter<TransactionLogAdapter.TransactionLogViewHolder>() {

    companion object {
        private const val TAG = "TransactionLogAdapter"
    }

    private var onItemClickListener: ((TransactionLog) -> Unit)? = null

    fun setOnItemClickListener(listener: (TransactionLog) -> Unit) {
        onItemClickListener = listener
    }

    private val differCallback = object : DiffUtil.ItemCallback<TransactionLog>() {
        override fun areItemsTheSame(oldItem: TransactionLog, newItem: TransactionLog): Boolean {
            return oldItem.tldRefnum == newItem.tldRefnum
        }

        override fun areContentsTheSame(oldItem: TransactionLog, newItem: TransactionLog): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    inner class TransactionLogViewHolder(
        private val binding: ItemMutasiBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION && position < differ.currentList.size) {
                    onItemClickListener?.invoke(differ.currentList[position])
                }
            }
        }

        fun bind(transactionLog: TransactionLog) {
            val context = binding.root.context
            binding.apply {
                // Map TransactionLog fields to existing item_mutasi.xml layout

                // tvTimestamp <- tldDate
                tvTimestamp.text = AppUtils.formatDate(transactionLog.tldDate)

                // tvDescription <- Reference number and ID pelanggan
                tvDescription.text = "Ref: ${transactionLog.tldRefnum} | ID: ${transactionLog.tldIdpel}"

                // tvSaldoInfo <- Balance info
                tvSaldoInfo.text = "Saldo: ${AppUtils.formatCurrency(transactionLog.tldBalance)}"

                // tvAmount <- Amount with proper formatting
                updateAmount(transactionLog)

                // ivTransactionType <- Transaction type icon
                updateTransactionIcon(transactionLog)
            }
        }

        private fun updateAmount(transactionLog: TransactionLog) {
            val context = binding.root.context
            binding.apply {
                val isIncoming = transactionLog.tldAmount >= 0

                if (isIncoming) {
                    tvAmount.text = "+ ${AppUtils.formatCurrency(kotlin.math.abs(transactionLog.tldAmount))}"
                    tvAmount.setTextColor(
                        ContextCompat.getColor(context, R.color.green_success)
                    )
                } else {
                    tvAmount.text = "- ${AppUtils.formatCurrency(kotlin.math.abs(transactionLog.tldAmount))}"
                    tvAmount.setTextColor(
                        ContextCompat.getColor(context, R.color.red_danger)
                    )
                }
            }
        }

        private fun updateTransactionIcon(transactionLog: TransactionLog) {
            val context = binding.root.context
            binding.apply {
                val isIncoming = transactionLog.tldAmount >= 0

                if (isIncoming) {
                    ivTransactionType.setImageResource(R.drawable.arrow_circle_up_24)
                    ivTransactionType.setColorFilter(
                        ContextCompat.getColor(context, R.color.green_success)
                    )
                } else {
                    ivTransactionType.setImageResource(R.drawable.arrow_circle_down_24)
                    ivTransactionType.setColorFilter(
                        ContextCompat.getColor(context, R.color.red_danger)
                    )
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionLogViewHolder {
        val binding = ItemMutasiBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionLogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionLogViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    // Helper methods
    fun updateData(newTransactionLogs: List<TransactionLog>) {
        differ.submitList(newTransactionLogs.toList())
        Log.d(TAG, "📊 Updated with ${newTransactionLogs.size} transaction logs")
    }

    fun clearData() {
        differ.submitList(emptyList())
        Log.d(TAG, "🧹 Cleared transaction log data")
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        onItemClickListener = null
    }
}