package com.smartdiary.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.smartdiary.database.DiaryDatabase
import com.smartdiary.model.DiaryEntryEntity
import com.smartdiary.repository.DiaryRepository
import kotlinx.coroutines.launch

class DiaryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DiaryRepository
    val allEntries: LiveData<List<DiaryEntryEntity>>
    val entryCount: LiveData<Int>

    init {
        val dao = DiaryDatabase.getInstance(application).diaryDao()
        repository = DiaryRepository(dao)
        allEntries = repository.allEntries
        entryCount = repository.entryCount
    }

    fun insert(entry: DiaryEntryEntity) {
        viewModelScope.launch {
            repository.insert(entry)
        }
    }

    fun insertAndGetId(
        entry: DiaryEntryEntity,
        onResult: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val id = repository.insert(entry)
            onResult(id)
        }
    }

    fun update(entry: DiaryEntryEntity) {
        viewModelScope.launch {
            repository.update(entry)
        }
    }

    fun delete(entry: DiaryEntryEntity) {
        viewModelScope.launch {
            repository.delete(entry)
        }
    }

    fun getEntryById(id: Long, onResult: (DiaryEntryEntity?) -> Unit) {
        viewModelScope.launch {
            val entry = repository.getEntryById(id)
            onResult(entry)
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }
}