package com.example.myandroid.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myandroid.data.db.entity.Category
import com.example.myandroid.data.db.entity.PasswordEntry
import com.example.myandroid.data.repository.CategoryRepository
import com.example.myandroid.data.repository.PasswordRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val CATEGORY_ALL = -1L
        const val CATEGORY_FAVORITES = -2L
    }

    private val passwordRepository = PasswordRepository(application)
    private val categoryRepository = CategoryRepository(application)

    val allCategories: LiveData<List<Category>> = categoryRepository.allCategories

    private val allPasswords: LiveData<List<PasswordEntry>> = passwordRepository.allPasswords
    private val favoritePasswords: LiveData<List<PasswordEntry>> = passwordRepository.favoritePasswords

    private val selectedCategoryId = MutableLiveData(CATEGORY_ALL)
    private val searchQuery = MutableLiveData("")

    // Track sources to prevent leaks
    private var currentCategorySource: LiveData<List<PasswordEntry>>? = null
    private var currentSearchSource: LiveData<List<PasswordEntry>>? = null

    val displayedPasswords: LiveData<List<PasswordEntry>> = MediatorLiveData<List<PasswordEntry>>().also { result ->
        fun update() {
            val catId = selectedCategoryId.value ?: CATEGORY_ALL
            val query = searchQuery.value

            if (!query.isNullOrEmpty()) {
                currentSearchSource?.let { result.removeSource(it) }
                currentCategorySource?.let { result.removeSource(it) }
                val src = passwordRepository.searchPasswords(query)
                result.addSource(src) { result.value = it }
                currentSearchSource = src
            } else {
                currentSearchSource?.let { result.removeSource(it) }
                currentSearchSource = null
                currentCategorySource?.let { result.removeSource(it) }
                val src = when (catId) {
                    CATEGORY_ALL -> allPasswords
                    CATEGORY_FAVORITES -> favoritePasswords
                    else -> passwordRepository.getPasswordsByCategory(catId)
                }
                result.addSource(src) { result.value = it }
                currentCategorySource = src
            }
        }
        result.addSource(selectedCategoryId) { update() }
        result.addSource(searchQuery) { update() }
    }

    fun selectCategory(categoryId: Long) {
        selectedCategoryId.value = categoryId
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun deletePassword(entry: PasswordEntry) {
        viewModelScope.launch {
            passwordRepository.delete(entry)
        }
    }

    fun toggleFavorite(entry: PasswordEntry) {
        viewModelScope.launch {
            entry.isFavorite = !entry.isFavorite
            passwordRepository.update(entry)
        }
    }
}
