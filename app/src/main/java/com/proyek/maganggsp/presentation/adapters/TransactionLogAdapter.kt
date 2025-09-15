// File: app/src/main/java/com/proyek/maganggsp/presentation/adapters/TransactionLogAdapter.kt - MVP CORE
package com.proyek.maganggsp.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.proyek.maganggsp.R
import com.proyek.maganggsp.databinding.ItemMutasiBinding
import com.proyek.maganggsp.domain.model.TransactionLog

/**
 * MVP CORE: Adapter untuk transaction logs dalam format card
 * Reuses existing item_mutasi.xml layout
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
                // Transaction type icon and amount styling
                val isIncoming = transaction.isIncomingTransaction()

                if (isIncoming) {
                    ivTransactionType.setImageResource(R.drawable.arrow_circle_down_24)
                    ivTransactionType.setColorFilter(
                        ContextCompat.getColor(root.context, R.color.green_success)
                    )
                    tvAmount.setTextColor(
                        ContextCompat.getColor(root.context, R.color.green_success)
                    )
                } else {
                    ivTransactionType.setImageResource(R.drawable.arrow_circle_up_24)
                    ivTransactionType.setColorFilter(
                        ContextCompat.getColor(root.context, R.color.red_danger)
                    )
                    tvAmount.setTextColor(
                        ContextCompat.getColor(root.context, R.color.red_danger)
                    )
                }

                // Date and time
                tvTimestamp.text = transaction.getFormattedDate()

                // Transaction description
                tvDescription.text = transaction.getDisplayDescription()

                // Balance info
                tvSaldoInfo.text = transaction.getBalanceDisplayText()

                // Amount with proper formatting
                tvAmount.text = transaction.getFormattedAmount()
            }
        }
    }

    /**
     * DiffUtil for efficient list updates
     */
    class TransactionDiffCallback : DiffUtil.ItemCallback<TransactionLog>() {
        override fun areItemsTheSame(oldItem: TransactionLog, newItem: TransactionLog): Boolean {
            return oldItem.tldRefnum == newItem.tldRefnum
        }

        override fun areContentsTheSame(oldItem: TransactionLog, newItem: TransactionLog): Boolean {
            return oldItem == newItem
        }
    }

    /**
     * Helper methods for adapter functionality
     */
    fun updateTransactions(transactions: List<TransactionLog>) {
        submitList(transactions)
    }

    fun clearTransactions() {
        submitList(emptyList())
    }

    fun getTransactionAtPosition(position: Int): TransactionLog? {
        return if (position in 0 until itemCount) {
            getItem(position)
        } else null
    }

    /**
     * Get transaction statistics
     */
    fun getTransactionStats(): TransactionStats {
        val transactions = currentList
        val incoming = transactions.filter { it.isIncomingTransaction() }
        val outgoing = transactions.filter { it.isOutgoingTransaction() }

        return TransactionStats(
            totalTransactions = transactions.size,
            incomingCount = incoming.size,
            outgoingCount = outgoing.size,
            totalIncoming = incoming.sumOf { it.tldAmount },
            totalOutgoing = outgoing.sumOf { kotlin.math.abs(it.tldAmount) },
            netAmount = transactions.sumOf { it.tldAmount },
            latestBalance = transactions.firstOrNull()?.tldBalance ?: 0L
        )
    }

    data class TransactionStats(
        val totalTransactions: Int,
        val incomingCount: Int,
        val outgoingCount: Int,
        val totalIncoming: Long,
        val totalOutgoing: Long,
        val netAmount: Long,
        val latestBalance: Long
    )
}