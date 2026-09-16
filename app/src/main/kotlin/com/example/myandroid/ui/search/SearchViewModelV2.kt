package com.example.myandroid.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.example.myandroid.data.db.entity.PasswordEntry
import com.example.myandroid.data.repository.PasswordRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val results: List<PasswordEntry> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModelV2(application: Application) : AndroidViewModel(application) {
    private val repository = PasswordRepository(application)
    private val query = MutableStateFlow("")
    private val debouncedQuery = query
        .debounce(300)
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    private val resultFlow = debouncedQuery
        .flatMapLatest { value ->
            if (value.trim().isBlank()) {
                flowOf(PasswordRepository.DecryptedEntriesResult.success(emptyList()))
            } else {
                repository.searchPasswordsRequired(value.trim()).asFlow()
            }
        }

    val uiState: StateFlow<SearchUiState> = kotlinx.coroutines.flow.combine(query, debouncedQuery, resultFlow) { value, settledQuery, result ->
        SearchUiState(
            query = value,
            results = result?.entries.orEmpty(),
            isLoading = value.isNotBlank() && value.trim() != settledQuery.trim(),
            error = result?.error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState())

    fun setQuery(value: String) {
        query.value = value
    }

    fun toggleFavorite(entry: PasswordEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSync(PasswordEntry().apply {
                id = entry.id
                categoryId = entry.categoryId
                title = entry.title
                username = entry.username
                password = entry.password
                url = entry.url
                notes = entry.notes
                entryType = entry.entryType
                isFavorite = !entry.isFavorite
                createdAt = entry.createdAt
                updatedAt = entry.updatedAt
            })
        }
    }
}
