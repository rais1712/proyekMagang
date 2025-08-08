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
        override fun areItemsTheSame(oldItem: Mutasi, newItem: Mutasi): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Mutasi, newItem: Mutasi): Boolean = oldItem == newItem
    }

    val differ = AsyncListDiffer(this, diffCallback)

    private var onLongClickListener: ((Mutasi) -> Unit)? = null

    fun setOnLongClickListener(listener: (Mutasi) -> Unit) {
        onLongClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MutasiViewHolder {
        val binding = ItemMutasiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MutasiViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MutasiViewHolder, position: Int) {
        val mutasi = differ.currentList[position]
        val context = holder.itemView.context

        holder.binding.apply {
            tvDescription.text = mutasi.description
            tvTimestamp.text = Formatters.toReadableDateTime(mutasi.timestamp)

            // Atur warna dan format jumlah
            if (mutasi.type == "IN") {
                tvAmount.text = "+ ${Formatters.toRupiah(mutasi.amount)}"
                tvAmount.setTextColor(ContextCompat.getColor(context, R.color.green_success))
                ivTransactionType.setImageResource(R.drawable.arrow_circle_up_24) // Ganti dengan ikon Anda
            } else {
                tvAmount.text = "- ${Formatters.toRupiah(mutasi.amount)}"
                tvAmount.setTextColor(ContextCompat.getColor(context, R.color.red_danger))
                ivTransactionType.setImageResource(R.drawable.arrow_circle_down_24) // Ganti dengan ikon Anda
            }

            // Atur warna latar belakang jika ditandai
            if (mutasi.isFlagged) {
                cardMutasiRoot.setCardBackgroundColor(ContextCompat.getColor(context, R.color.status_warning_background))
            } else {
                cardMutasiRoot.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
            }

            holder.itemView.setOnLongClickListener {
                onLongClickListener?.let { it(mutasi) }
                true
            }
        }
    }

    override fun getItemCount(): Int = differ.currentList.size
}