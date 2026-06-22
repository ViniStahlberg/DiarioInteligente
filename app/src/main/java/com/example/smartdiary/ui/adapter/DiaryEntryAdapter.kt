package com.smartdiary.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smartdiary.databinding.ItemDiaryEntryBinding
import com.smartdiary.model.DiaryEntryEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DiaryEntryAdapter(
    private val onItemClick: (DiaryEntryEntity) -> Unit,
    private val onItemLongClick: (DiaryEntryEntity) -> Unit
) : ListAdapter<DiaryEntryEntity, DiaryEntryAdapter.EntryViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val binding = ItemDiaryEntryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return EntryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EntryViewHolder(
        private val binding: ItemDiaryEntryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: DiaryEntryEntity) {
            binding.tvItemTitle.text = entry.title
            binding.tvItemDate.text = formatDate(entry.createdAt)
            binding.tvItemLight.text = "☀ %.1f lx".format(entry.lightLevel)

            binding.root.setOnClickListener { onItemClick(entry) }
            binding.root.setOnLongClickListener {
                onItemLongClick(entry)
                true
            }
        }

        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<DiaryEntryEntity>() {
        override fun areItemsTheSame(old: DiaryEntryEntity, new: DiaryEntryEntity) =
            old.id == new.id

        override fun areContentsTheSame(old: DiaryEntryEntity, new: DiaryEntryEntity) =
            old == new
    }
}