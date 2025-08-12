package com.proyek.maganggsp.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.proyek.maganggsp.databinding.ItemLoketHistoryBinding
import com.proyek.maganggsp.domain.model.Loket

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    // On-item click listener
    private var onItemClickListener: ((Loket) -> Unit)? = null

    fun setOnItemClickListener(listener: (Loket) -> Unit) {
        onItemClickListener = listener
    }

    // DiffUtil untuk performa RecyclerView yang efisien
    private val differCallback = object : DiffUtil.ItemCallback<Loket>() {
        override fun areItemsTheSame(oldItem: Loket, newItem: Loket): Boolean {
            return oldItem.noLoket == newItem.noLoket // Menggunakan ID unik
        }

        override fun areContentsTheSame(oldItem: Loket, newItem: Loket): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    inner class HistoryViewHolder(private val binding: ItemLoketHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val currentList = differ.currentList
                    if (position < currentList.size) {
                        onItemClickListener?.invoke(currentList[position])
                    }
                }
            }
        }

        fun bind(loket: Loket) {
            binding.apply {
                tvLoketName.text = loket.namaLoket
                tvLoketPhone.text = loket.nomorTelepon
                // Menggunakan tvNomorLoket yang sudah diperbaiki di XML
                tvNomorLoket.text = loket.noLoket
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding =
            ItemLoketHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val loket = differ.currentList[position]
        holder.bind(loket)
    }

    override fun getItemCount(): Int = differ.currentList.size
}