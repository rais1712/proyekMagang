// File: app/src/main/java/com/proyek/maganggsp/presentation/adapters/LoketSearchAdapter.kt - MVP CORE
package com.proyek.maganggsp.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.proyek.maganggsp.R
import com.proyek.maganggsp.databinding.ItemLoketHistoryBinding
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.model.LoketStatus
import java.text.SimpleDateFormat
import java.util.*

/**
 * MVP CORE: Adapter untuk search results dan recent lokets
 * Reuses existing item_loket_history.xml layout
 */
class LoketSearchAdapter(
    private val onItemClick: (Loket) -> Unit
) : ListAdapter<Loket, LoketSearchAdapter.LoketViewHolder>(LoketDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoketViewHolder {
        val binding = ItemLoketHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LoketViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: LoketViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class LoketViewHolder(
        private val binding: ItemLoketHistoryBinding,
        private val onItemClick: (Loket) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(loket: Loket) {
            with(binding) {
                // Basic loket info
                tvLoketName.text = loket.namaLoket
                tvLoketPhone.text = formatPhoneNumber(loket.nomorHP)
                tvNomorLoket.text = formatPpid(loket.ppid)

                // Show access info if available
                if (loket.tanggalAkses.isNotBlank()) {
                    tvLastAccessed.text = "Terakhir diakses: ${formatAccessDate(loket.tanggalAkses)}"
                    tvLastAccessed.visibility = android.view.View.VISIBLE
                } else {
                    tvLastAccessed.visibility = android.view.View.GONE
                }

                // Status-based styling
                applyStatusStyling(loket.status)

                // Click listener
                root.setOnClickListener {
                    onItemClick(loket)
                }
            }
        }

        private fun formatPhoneNumber(phone: String): String {
            return when {
                phone.startsWith("+62") -> phone
                phone.startsWith("08") -> "+62${phone.substring(1)}"
                phone.startsWith("62") -> "+$phone"
                else -> phone
            }
        }

        private fun formatPpid(ppid: String): String {
            return when {
                ppid.length > 15 -> "#${ppid.take(10)}...${ppid.takeLast(4)}"
                ppid.isBlank() -> "#UNKNOWN"
                else -> "#$ppid"
            }
        }

        private fun formatAccessDate(dateString: String): String {
            return try {
                val timestamp = dateString.toLongOrNull() ?: return dateString
                val date = java.util.Date(timestamp)
                java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale("in", "ID"))
                    .format(date)
            } catch (e: Exception) {
                dateString
            }
        }

        private fun applyStatusStyling(status: LoketStatus) {
            val context = binding.root.context

            when (status) {
                LoketStatus.BLOCKED -> {
                    // Red tint for blocked lokets
                    binding.layoutLoketItem.setBackgroundColor(
                        ContextCompat.getColor(context, R.color.status_danger_background)
                    )
                    binding.tvLoketName.setTextColor(
                        ContextCompat.getColor(context, R.color.red_danger)
                    )
                }
                LoketStatus.FLAGGED -> {
                    // Yellow tint for flagged lokets
                    binding.layoutLoketItem.setBackgroundColor(
                        ContextCompat.getColor(context, R.color.status_warning_background)
                    )
                    binding.tvLoketName.setTextColor(
                        ContextCompat.getColor(context, R.color.yellow_secondary_accent)
                    )
                }
                LoketStatus.NORMAL -> {
                    // Normal white background
                    binding.layoutLoketItem.setBackgroundColor(
                        ContextCompat.getColor(context, R.color.white)
                    )
                    binding.tvLoketName.setTextColor(
                        ContextCompat.getColor(context, R.color.text_primary_black)
                    )
                }
            }
        }
    }

    /**
     * DiffUtil for efficient list updates
     */
    class LoketDiffCallback : DiffUtil.ItemCallback<Loket>() {
        override fun areItemsTheSame(oldItem: Loket, newItem: Loket): Boolean {
            return oldItem.ppid == newItem.ppid
        }

        override fun areContentsTheSame(oldItem: Loket, newItem: Loket): Boolean {
            return oldItem == newItem
        }
    }

    /**
     * Helper methods for adapter functionality
     */
    fun updateSearchResults(results: List<Loket>) {
        submitList(results)
    }

    fun clearResults() {
        submitList(emptyList())
    }

    fun getItemAtPosition(position: Int): Loket? {
        return if (position in 0 until itemCount) {
            getItem(position)
        } else null
    }
}