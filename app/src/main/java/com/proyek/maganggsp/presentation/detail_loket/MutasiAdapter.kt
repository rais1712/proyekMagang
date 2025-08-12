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

class MutasiAdapter : RecyclerView.Adapter<MutasiAdapter.MutasiViewHolder>() {

    inner class MutasiViewHolder(val binding: ItemMutasiBinding) : RecyclerView.ViewHolder(binding.root)

    private val diffCallback = object : DiffUtil.ItemCallback<Mutasi>() {
        override fun areItemsTheSame(oldItem: Mutasi, newItem: Mutasi): Boolean {
            // Menggunakan nomorReferensi sebagai unique identifier
            return oldItem.nomorReferensi == newItem.nomorReferensi
        }
        override fun areContentsTheSame(oldItem: Mutasi, newItem: Mutasi): Boolean = oldItem == newItem
    }

    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MutasiViewHolder {
        val binding = ItemMutasiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MutasiViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MutasiViewHolder, position: Int) {
        val mutasi = differ.currentList[position]
        val context = holder.itemView.context

        holder.binding.apply {
            // Format dan tampilkan tanggal menggunakan Formatters
            tvTimestamp.text = Formatters.toReadableDateTime(mutasi.tanggal)

            // Tampilkan nomor referensi
            tvDescription.text = "No. Ref: ${mutasi.nomorReferensi}"

            // Tampilkan sisa saldo
            tvSaldoInfo.text = "Sisa Saldo: ${Formatters.toRupiah(mutasi.sisaSaldo)}"

            // Tentukan tipe transaksi berdasarkan nilai nominalTransaksi (Opsi B)
            if (mutasi.nominalTransaksi > 0) {
                // Transaksi masuk (DEBIT/IN)
                tvAmount.text = "+${Formatters.toRupiah(mutasi.nominalTransaksi)}"
                tvAmount.setTextColor(ContextCompat.getColor(context, R.color.green_success))
                ivTransactionType.setImageResource(R.drawable.arrow_circle_up_24)
                ivTransactionType.setColorFilter(ContextCompat.getColor(context, R.color.green_success))
            } else {
                // Transaksi keluar (KREDIT/OUT)
                val absoluteAmount = kotlin.math.abs(mutasi.nominalTransaksi)
                tvAmount.text = "-${Formatters.toRupiah(absoluteAmount)}"
                tvAmount.setTextColor(ContextCompat.getColor(context, R.color.red_danger))
                ivTransactionType.setImageResource(R.drawable.arrow_circle_down_24)
                ivTransactionType.setColorFilter(ContextCompat.getColor(context, R.color.red_danger))
            }
        }
    }

    override fun getItemCount(): Int = differ.currentList.size
}