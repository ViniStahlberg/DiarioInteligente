package com.smartdiary.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartdiary.model.DiaryEntry
import com.smartdiary.repository.DiaryRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DiaryViewModel : ViewModel() {

    private val repository = DiaryRepository()

    private val _entries = MutableLiveData<List<DiaryEntry>>()
    val entries: LiveData<List<DiaryEntry>> = _entries

    private val _entryCount = MutableLiveData<Int>()
    val entryCount: LiveData<Int> = _entryCount

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _saveResult = MutableLiveData<Boolean?>()
    val saveResult: LiveData<Boolean?> = _saveResult

    init {
        loadEntries()
        loadEntryCount()
    }

    private fun loadEntries() {
        viewModelScope.launch {
            repository.getEntriesFlow().collectLatest { list ->
                _entries.value = list
            }
        }
    }

    private fun loadEntryCount() {
        viewModelScope.launch {
            repository.getEntryCountFlow().collectLatest { count ->
                _entryCount.value = count
            }
        }
    }

    fun saveEntryWithPhoto(entry: DiaryEntry, photoUri: Uri?) {
        viewModelScope.launch {
            _isLoading.value = true
            var finalEntry = entry
            if (photoUri != null) {
                val uploadResult = repository.uploadPhoto(photoUri)
                if (uploadResult.isSuccess) {
                    finalEntry = entry.copy(imageUrl = uploadResult.getOrDefault(""))
                }
            }
            val result = repository.saveEntry(finalEntry)
            _isLoading.value = false
            _saveResult.value = result.isSuccess
            if (!result.isSuccess) {
                _error.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch {
            val result = repository.deleteEntry(id)
            if (!result.isSuccess) _error.value = result.exceptionOrNull()?.message
        }
    }

    fun deleteAllEntries() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.deleteAllEntries()
            _isLoading.value = false
        }
    }

    fun getEntryById(id: String, onResult: (DiaryEntry?) -> Unit) {
        viewModelScope.launch {
            onResult(repository.getEntryById(id))
        }
    }

    fun clearSaveResult() { _saveResult.value = null }
    fun clearError() { _error.value = null }

    // Humor da semana para o gráfico
    fun getMoodCountsForChart(): Map<String, Int> {
        val list = _entries.value ?: return emptyMap()
        val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        return list
            .filter { it.createdAt >= sevenDaysAgo }
            .groupBy { it.mood }
            .mapValues { it.value.size }
    }

    // ERRO CORRIGIDO: Agora a função filtra de forma inteligente as notas criadas
    // enquanto o usuário estava se movimentando (Passos > 0) para alimentar a nova interface!
    fun getActiveMovementEntries(): List<DiaryEntry> {
        return _entries.value?.filter { it.stepsAtTime > 0 } ?: emptyList()
    }
}