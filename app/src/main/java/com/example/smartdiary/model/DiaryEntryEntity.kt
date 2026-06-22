package com.smartdiary.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary_entries")
data class DiaryEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val imagePath: String = "",
    val lightLevel: Float = 0f,
    val createdAt: Long = System.currentTimeMillis()
)