// ENHANCED: MutasiAdapter with FIXED field mapping to match item_mutasi.xml
// File: app/src/main/java/com/proyek/maganggsp/presentation/detail_loket/MutasiAdapter.kt

package com.proyek.maganggsp.presentation.detail_loket

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
import com.proyek.maganggsp.util.FeatureFlags
import android.util.Log

/**
 * FIXED: MutasiAdapter dengan correct field mapping ke item_mutasi.xml
 * Mapping: tvTransactionDate -> tvTimestamp, tvReference -> tvDescription, etc.
 */
class MutasiAdapter : RecyclerView.Adapter<MutasiAdapter.MutasiViewHolder>() {

    companion object {
        private const val TAG = "MutasiAdapter"
    }

    // Click listener with more specific callback
    private var onFlagClickListener: ((Mutasi) -> Unit)? = null

    fun setOnFlagClickListener(listener: (Mutasi) -> Unit) {
        onFlagClickListener = listener
    }

    private val differCallback = object : DiffUtil.ItemCallback<Mutasi>() {
        override fun areItemsTheSame(oldItem: Mutasi, newItem: Mutasi): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Mutasi, newItem: Mutasi): Boolean {
            return oldItem.reference == newItem.reference &&
                    oldItem.amount == newItem.amount &&
                    oldItem.balanceAfter == newItem.balanceAfter &&
                    oldItem.timestamp == newItem.timestamp &&
                    oldItem.type == newItem.type
        }

        override fun getChangePayload(oldItem: Mutasi, newItem: Mutasi): Any? {
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
            // 🚩 FEATURE FLAGS: Only enable click if flag management enabled
            if (FeatureFlags.ENABLE_FLAG_MANAGEMENT) {
                binding.root.setOnClickListener {
                    val position = adapterPosition // Changed from bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION && position < differ.currentList.size) {
                        onFlagClickListener?.invoke(differ.currentList[position])
                    }
                }
            } else {
                // Disable click functionality
                binding.root.setOnClickListener {
                    // Show message that flag functionality is disabled
                    if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                        Log.w(TAG, "🚩 Mutation click disabled by feature flag")
                    }
                }
            }
        }
        fun bind(mutasi: Mutasi, payloads: List<Any>? = null) {
            // Support partial updates for better performance
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
                // FIXED: Correct field mapping to match item_mutasi.xml

                // tvTransactionDate -> tvTimestamp (XML field)
                tvTimestamp.text = try {
                    Formatters.toReadableDateTime(mutasi.timestamp)
                } catch (e: Exception) {
                    mutasi.timestamp // Fallback to original string
                }

                // tvReference -> tvDescription (XML field)
                tvDescription.text = context.getString(R.string.label_ref, mutasi.reference)

                // Update amount and balance
                updateAmount(mutasi)
                updateBalance(mutasi)

                // Update transaction type icon
                updateTransactionIcon(mutasi)
            }
        }

        private fun updateAmount(mutasi: Mutasi) {
            val context = binding.root.context
            binding.apply {
                // Better amount formatting and coloring
                val isIncoming = mutasi.type.equals("IN", ignoreCase = true) || mutasi.amount >= 0

                if (isIncoming) {
                    tvAmount.text = "+ ${Formatters.toRupiah(kotlin.math.abs(mutasi.amount))}"
                    tvAmount.setTextColor(
                        ContextCompat.getColor(context, R.color.green_success)
                    )
                } else {
                    tvAmount.text = "- ${Formatters.toRupiah(kotlin.math.abs(mutasi.amount))}"
                    tvAmount.setTextColor(
                        ContextCompat.getColor(context, R.color.red_danger)
                    )
                }
            }
        }

        private fun updateBalance(mutasi: Mutasi) {
            val context = binding.root.context
            // FIXED: tvRemainingBalance -> tvSaldoInfo (XML field)
            binding.tvSaldoInfo.text = context.getString(
                R.string.label_sisa_saldo,
                Formatters.toRupiah(mutasi.balanceAfter)
            )
        }

        /**
         * NEW: Update transaction type icon based on amount/type
         */
        private fun updateTransactionIcon(mutasi: Mutasi) {
            val context = binding.root.context
            binding.apply {
                val isIncoming = mutasi.type.equals("IN", ignoreCase = true) || mutasi.amount >= 0

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

    // Memory leak prevention
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        onFlagClickListener = null
    }

    // Helper methods for external usage
    fun updateData(newMutations: List<Mutasi>) {
        differ.submitList(newMutations.toList())
    }

    fun clearData() {
        differ.submitList(emptyList())
    }

    /**
     * 🚩 SURGICAL CUTTING: Get current feature status for UI
     */
    fun isInteractionEnabled(): Boolean {
        return FeatureFlags.ENABLE_FLAG_MANAGEMENT
    }
}