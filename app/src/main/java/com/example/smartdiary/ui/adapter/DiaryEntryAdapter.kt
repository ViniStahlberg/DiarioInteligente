package com.smartdiary.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smartdiary.databinding.ItemDiaryEntryBinding
import com.smartdiary.model.DiaryEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DiaryEntryAdapter(
    private val onItemClick: (DiaryEntry) -> Unit,
    private val onItemLongClick: (DiaryEntry) -> Unit
) : ListAdapter<DiaryEntry, DiaryEntryAdapter.EntryViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val binding = ItemDiaryEntryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return EntryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) =
        holder.bind(getItem(position))

    inner class EntryViewHolder(
        private val binding: ItemDiaryEntryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: DiaryEntry) {
            binding.tvItemMood.text = if (entry.mood.isNotEmpty()) entry.mood else "📝"
            binding.tvItemTitle.text = entry.title
            binding.tvItemDescription.text = entry.description
            binding.tvItemDate.text = formatDate(entry.createdAt)

            // Exibe a luminosidade e uma tag curta baseada nos passos
            val motionTag = if (entry.stepsAtTime > 0) "Ativo 🚶" else "Relaxado 🧘"
            binding.tvItemLight.text = "☀ %.1f lx • %s".format(entry.lightLevel, motionTag)

            binding.tvItemHasPhoto.visibility =
                if (entry.imageUrl.isNotEmpty()) View.VISIBLE
                else View.GONE

            // Tratamento seguro para o novo layout de movimentos/passos do Pitch
            if (binding.root.findViewById<View>(com.smartdiary.R.id.tvItemHasMotion) != null) {
                binding.tvItemHasMotion.visibility =
                    if (entry.stepsAtTime > 0) View.VISIBLE
                    else View.GONE
            }

            binding.root.setOnClickListener { onItemClick(entry) }
            binding.root.setOnLongClickListener { onItemLongClick(entry); true }
        }

        private fun formatDate(ts: Long): String =
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(ts))
    }

    class DiffCallback : DiffUtil.ItemCallback<DiaryEntry>() {
        override fun areItemsTheSame(old: DiaryEntry, new: DiaryEntry) = old.id == new.id
        override fun areContentsTheSame(old: DiaryEntry, new: DiaryEntry) = old == new
    }
}