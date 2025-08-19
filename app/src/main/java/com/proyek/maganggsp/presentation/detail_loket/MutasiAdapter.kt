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
 * FIXED: MutasiAdapter yang menggunakan fields yang ada di Mutasi model
 */
class MutasiAdapter : RecyclerView.Adapter<MutasiAdapter.MutasiViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<Mutasi>() {
        override fun areItemsTheSame(oldItem: Mutasi, newItem: Mutasi): Boolean {
            // FIXED: Menggunakan field yang benar-benar ada
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Mutasi, newItem: Mutasi): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    inner class MutasiViewHolder(private val binding: ItemMutasiBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(mutasi: Mutasi) {
            val context = binding.root.context
            binding.apply {
                // FIXED: Menggunakan fields yang benar dari Mutasi model
                tvTransactionDate.text = Formatters.toReadableDateTime(mutasi.timestamp)
                tvReference.text = context.getString(R.string.label_ref, mutasi.reference)
                tvRemainingBalance.text = context.getString(
                    R.string.label_sisa_saldo,
                    Formatters.toRupiah(mutasi.balanceAfter)
                )

                // Membedakan tampilan berdasarkan tipe transaksi
                if (mutasi.type.equals("IN", ignoreCase = true)) {
                    // Transaksi masuk (DEBIT/IN)
                    tvTransactionAmount.text = "+ ${Formatters.toRupiah(mutasi.amount)}"
                    tvTransactionAmount.setTextColor(
                        ContextCompat.getColor(context, R.color.green_success)
                    )
                } else {
                    // Transaksi keluar (KREDIT/OUT)
                    tvTransactionAmount.text = "- ${Formatters.toRupiah(kotlin.math.abs(mutasi.amount))}"
                    tvTransactionAmount.setTextColor(
                        ContextCompat.getColor(context, R.color.red_danger)
                    )
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MutasiViewHolder {
        val binding = ItemMutasiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MutasiViewHolder(binding)
    }

    override fun getItemCount(): Int = differ.currentList.size

    override fun onBindViewHolder(holder: MutasiViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }
}