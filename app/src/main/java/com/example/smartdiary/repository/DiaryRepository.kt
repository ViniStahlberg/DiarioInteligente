package com.smartdiary.repository

import androidx.lifecycle.LiveData
import com.smartdiary.dao.DiaryDao
import com.smartdiary.model.DiaryEntryEntity

class DiaryRepository(private val dao: DiaryDao) {

    val allEntries: LiveData<List<DiaryEntryEntity>> = dao.getAllEntries()
    val entryCount: LiveData<Int> = dao.getEntryCount()

    suspend fun insert(entry: DiaryEntryEntity): Long {
        return dao.insert(entry)
    }

    suspend fun update(entry: DiaryEntryEntity) {
        dao.update(entry)
    }

    suspend fun delete(entry: DiaryEntryEntity) {
        dao.delete(entry)
    }

    suspend fun getEntryById(id: Long): DiaryEntryEntity? {
        return dao.getEntryById(id)
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }
}