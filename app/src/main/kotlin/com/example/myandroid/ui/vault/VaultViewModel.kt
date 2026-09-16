package com.example.myandroid.ui.vault

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.example.myandroid.data.db.entity.Category
import com.example.myandroid.data.db.entity.PasswordEntry
import com.example.myandroid.data.repository.CategoryRepository
import com.example.myandroid.data.repository.PasswordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class VaultUiState(
    val categories: List<Category> = emptyList(),
    val entries: List<PasswordEntry> = emptyList(),
    val totalCount: Int = 0,
    val selectedCategoryId: Long? = null,
    val query: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

class VaultViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val FAVORITES_FILTER = -2L
    }

    private val passwordRepository = PasswordRepository(application)
    private val categoryRepository = CategoryRepository(application)
    private val selectedCategoryId = MutableStateFlow<Long?>(null)
    private val query = MutableStateFlow("")
    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError

    private val categories = categoryRepository.allCategories.asFlow().map { it ?: emptyList() }
    private val entries = passwordRepository.getAllPasswordsRequired().asFlow()

    val uiState: StateFlow<VaultUiState> = combine(
        categories,
        entries,
        selectedCategoryId,
        query
    ) { categoryList, result, selected, search ->
        val entryList = result?.entries.orEmpty()
        val normalizedQuery = search.trim()
        val filtered = entryList.filter { entry ->
            val categoryMatches = when (selected) {
                FAVORITES_FILTER -> entry.isFavorite
                null -> true
                else -> entry.categoryId == selected
            }
            val queryMatches = normalizedQuery.isBlank() || listOf(
                entry.title.orEmpty(),
                entry.username.orEmpty(),
                entry.url.orEmpty()
            ).any { it.contains(normalizedQuery, ignoreCase = true) }
            categoryMatches && queryMatches
        }
        VaultUiState(
            categories = categoryList,
            entries = filtered,
            totalCount = filtered.size,
            selectedCategoryId = selected,
            query = search,
            isLoading = false,
            error = result?.error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), VaultUiState())

    fun selectCategory(categoryId: Long?) {
        selectedCategoryId.value = categoryId
    }

    fun setQuery(value: String) {
        query.value = value
    }

    fun toggleFavorite(entry: PasswordEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val updated = copyEntry(entry).apply { isFavorite = !isFavorite }
                passwordRepository.updateSync(updated)
                _actionError.value = null
            } catch (error: Exception) {
                _actionError.value = error.message ?: "更新收藏失败"
            }
        }
    }

    fun clearActionError() {
        _actionError.value = null
    }

    private fun copyEntry(source: PasswordEntry): PasswordEntry = PasswordEntry().apply {
        id = source.id
        categoryId = source.categoryId
        title = source.title
        username = source.username
        password = source.password
        url = source.url
        notes = source.notes
        entryType = source.entryType
        isFavorite = source.isFavorite
        createdAt = source.createdAt
        updatedAt = source.updatedAt
    }
}
