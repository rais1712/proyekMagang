// File: app/src/main/java/com/proyek/maganggsp/presentation/adapters/TransactionAdapter.kt - STREAMLINED
package com.proyek.maganggsp.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.proyek.maganggsp.R
import com.proyek.maganggsp.databinding.ItemMutasiBinding
import com.proyek.maganggsp.util.AppUtils

/**
 * STREAMLINED: Transaction adapter for detail screen
 * Reuses existing item_mutasi.xml layout, focused on transaction display
 */
class TransactionLogAdapter : ListAdapter<TransactionLog, TransactionLogAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemMutasiBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TransactionViewHolder(
        private val binding: ItemMutasiBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: TransactionLog) {
            with(binding) {
                val context = root.context
                val isIncoming = transaction.isIncomingTransaction()

                // Transaction type icon and amount styling
                if (isIncoming) {
                    ivTransactionType.setImageResource(R.drawable.arrow_circle_down_24)
                    ivTransactionType.setColorFilter(
                        ContextCompat.getColor(context, R.color.green_success)
                    )
                    tvAmount.setTextColor(
                        ContextCompat.getColor(context, R.color.green_success)
                    )
                } else {
                    ivTransactionType.setImageResource(R.drawable.arrow_circle_up_24)
                    ivTransactionType.setColorFilter(
                        ContextCompat.getColor(context, R.color.red_danger)
                    )
                    tvAmount.setTextColor(
                        ContextCompat.getColor(context, R.color.red_danger)
                    )
                }

                // Transaction details
                tvTimestamp.text = transaction.getFormattedDate()
                tvDescription.text = transaction.getDisplayDescription()
                tvSaldoInfo.text = transaction.getBalanceDisplayText()
                tvAmount.text = transaction.getFormattedAmount()
            }
        }
    }

    class TransactionDiffCallback : DiffUtil.ItemCallback<TransactionLog>() {
        override fun areItemsTheSame(oldItem: TransactionLog, newItem: TransactionLog): Boolean {
            return oldItem.tldRefnum == newItem.tldRefnum
        }

        override fun areContentsTheSame(oldItem: TransactionLog, newItem: TransactionLog): Boolean {
            return oldItem == newItem
        }
    }

    /**
     * STREAMLINED: Helper methods
     */
    fun updateTransactions(transactions: List<TransactionLog>) {
        submitList(transactions)
        AppUtils.logDebug("TransactionAdapter", "Updated with ${transactions.size} transactions")
    }

    fun clearTransactions() {
        submitList(emptyList())
    }

    fun getTransactionAtPosition(position: Int): TransactionLog? {
        return if (position in 0 until itemCount) getItem(position) else null
    }

    /**
     * Get transaction summary for display
     */
    fun getTransactionSummary(): TransactionSummary {
        val transactions = currentList
        val incoming = transactions.filter { it.isIncomingTransaction() }
        val outgoing = transactions.filter { it.isOutgoingTransaction() }

        return TransactionSummary(
            totalCount = transactions.size,
            incomingCount = incoming.size,
            outgoingCount = outgoing.size,
            totalIncoming = incoming.sumOf { it.tldAmount },
            totalOutgoing = outgoing.sumOf { kotlin.math.abs(it.tldAmount) },
            netAmount = transactions.sumOf { it.tldAmount },
            latestBalance = transactions.firstOrNull()?.tldBalance ?: 0L
        )
    }

    data class TransactionSummary(
        val totalCount: Int,
        val incomingCount: Int,
        val outgoingCount: Int,
        val totalIncoming: Long,
        val totalOutgoing: Long,
        val netAmount: Long,
        val latestBalance: Long
    ) {
        fun getFormattedSummary(): String {
            return """
            📊 Ringkasan Transaksi:
            • Total: $totalCount transaksi
            • Masuk: $incomingCount (${AppUtils.formatCurrency(totalIncoming)})
            • Keluar: $outgoingCount (${AppUtils.formatCurrency(totalOutgoing)})
            • Saldo Terakhir: ${AppUtils.formatCurrency(latestBalance)}
            """.trimIndent()
        }
    }
}