// ENHANCED: MutasiAdapter with memory leak prevention and better performance
// File: app/src/main/java/com/proyek/maganggsp/presentation/detailloket/MutasiAdapter.kt

package com.proyek.maganggsp.presentation.detailloket

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.proyek.maganggsp.R
import com.proyek.maganggsp.databinding.ItemMutasiBinding
import com.proyek.maganggsp.domain.model.Mutasi
import com.proyek.maganggsp.util.Formatters

/**
 * ENHANCED: MutasiAdapter dengan proper memory management dan click handling
 */
class MutasiAdapter : RecyclerView.Adapter<MutasiAdapter.MutasiViewHolder>() {

    // ENHANCED: Click listener with more specific callback
    private var onFlagClickListener: ((Mutasi) -> Unit)? = null

    fun setOnFlagClickListener(listener: (Mutasi) -> Unit) {
        onFlagClickListener = listener
    }

    private val differCallback = object : DiffUtil.ItemCallback<Mutasi>() {
        override fun areItemsTheSame(oldItem: Mutasi, newItem: Mutasi): Boolean {
            // FIXED: Use reliable ID comparison
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Mutasi, newItem: Mutasi): Boolean {
            // ENHANCED: More efficient comparison
            return oldItem.reference == newItem.reference &&
                    oldItem.amount == newItem.amount &&
                    oldItem.balanceAfter == newItem.balanceAfter &&
                    oldItem.timestamp == newItem.timestamp &&
                    oldItem.type == newItem.type
        }

        override fun getChangePayload(oldItem: Mutasi, newItem: Mutasi): Any? {
            // ENHANCED: Partial update support for better performance
            val changes = mutableListOf<String>()

            if (oldItem.amount != newItem.amount) changes.add("amount")
            if (oldItem.balanceAfter != newItem.balanceAfter) changes.add("balance")

            return if (changes.isNotEmpty()) changes else null
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    inner class MutasiViewHolder(
        private val binding: ItemMutasiBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            // ENHANCED: Click handling with proper bounds checking
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION && position < differ.currentList.size) {
                    onFlagClickListener?.invoke(differ.currentList[position])
                }
            }
        }

        fun bind(mutasi: Mutasi, payloads: List<Any>? = null) {
            // ENHANCED: Support partial updates for better performance
            if (!payloads.isNullOrEmpty()) {
                val changes = payloads.firstOrNull() as? List<*>
                changes?.forEach { change ->
                    when (change) {
                        "amount" -> updateAmount(mutasi)
                        "balance" -> updateBalance(mutasi)
                    }
                }
                return
            }

            // Full bind
            bindComplete(mutasi)
        }

        private fun bindComplete(mutasi: Mutasi) {
            val context = binding.root.context
            binding.apply {
                // ENHANCED: Better date formatting with fallback
                tvTransactionDate.text = try {
                    Formatters.toReadableDateTime(mutasi.timestamp)
                } catch (e: Exception) {
                    mutasi.timestamp // Fallback to original string
                }

                tvReference.text = context.getString(R.string.label_ref, mutasi.reference)

                updateAmount(mutasi)
                updateBalance(mutasi)
            }
        }

        private fun updateAmount(mutasi: Mutasi) {
            val context = binding.root.context
            binding.apply {
                // ENHANCED: Better amount formatting and coloring
                val isIncoming = mutasi.type.equals("IN", ignoreCase = true) || mutasi.amount >= 0

                if (isIncoming) {
                    tvTransactionAmount.text = "+ ${Formatters.toRupiah(kotlin.math.abs(mutasi.amount))}"
                    tvTransactionAmount.setTextColor(
                        ContextCompat.getColor(context, R.color.green_success)
                    )
                } else {
                    tvTransactionAmount.text = "- ${Formatters.toRupiah(kotlin.math.abs(mutasi.amount))}"
                    tvTransactionAmount.setTextColor(
                        ContextCompat.getColor(context, R.color.red_danger)
                    )
                }
            }
        }

        private fun updateBalance(mutasi: Mutasi) {
            val context = binding.root.context
            binding.tvRemainingBalance.text = context.getString(
                R.string.label_sisa_saldo,
                Formatters.toRupiah(mutasi.balanceAfter)
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MutasiViewHolder {
        val binding = ItemMutasiBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MutasiViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MutasiViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun onBindViewHolder(
        holder: MutasiViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            holder.bind(differ.currentList[position], payloads)
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    // ENHANCED: Memory leak prevention
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        onFlagClickListener = null
    }

    // ENHANCED: ViewHolder recycling optimization
    override fun onViewRecycled(holder: MutasiViewHolder) {
        super.onViewRecycled(holder)
        // Clear any ongoing operations or listeners if needed
    }

    // ENHANCED: Helper method to update data efficiently
    fun updateData(newMutations: List<Mutasi>) {
        differ.submitList(newMutations.toList()) // Create new list instance to trigger diff
    }

    // ENHANCED: Helper to clear data
    fun clearData() {
        differ.submitList(emptyList())
    }
}