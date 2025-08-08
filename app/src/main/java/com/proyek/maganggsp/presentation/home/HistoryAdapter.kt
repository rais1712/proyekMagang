package com.proyek.maganggsp.presentation.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.proyek.maganggsp.databinding.ItemLoketHistoryBinding
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.util.Formatters

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(val binding: ItemLoketHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    private val diffCallback = object : DiffUtil.ItemCallback<Loket>() {
        override fun areItemsTheSame(oldItem: Loket, newItem: Loket): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Loket, newItem: Loket): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    private var onItemClickListener: ((Loket) -> Unit)? = null

    fun setOnItemClickListener(listener: (Loket) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemLoketHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val loket = differ.currentList[position]
        holder.binding.apply {
            // Mengisi data dasar
            tvLoketName.text = loket.name
            tvLoketAddress.text = loket.address

            // Logika untuk menampilkan tanggal akses terakhir
            // Ini akan dieksekusi jika field 'lastAccessed' ada isinya
            loket.lastAccessed?.let { timestamp ->
                tvLastAccessed.text = "Diakses pada: ${Formatters.toReadableDateTime(timestamp)}"
                tvLastAccessed.visibility = View.VISIBLE
            } ?: run {
                // Jika tidak ada data tanggal, sembunyikan TextView-nya
                tvLastAccessed.visibility = View.GONE
            }

            // Memasang click listener pada seluruh item view
            holder.itemView.setOnClickListener {
                onItemClickListener?.let { listener ->
                    listener(loket)
                }
            }
        }
    }

    override fun getItemCount(): Int = differ.currentList.size
}