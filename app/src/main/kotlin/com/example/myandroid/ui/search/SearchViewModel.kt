package com.example.myandroid.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.example.myandroid.data.db.entity.PasswordEntry
import com.example.myandroid.data.repository.PasswordRepository

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val passwordRepository = PasswordRepository(application)
    private val searchQuery = MutableLiveData("")
    private var searchSource: LiveData<List<PasswordEntry>>? = null

    val searchResults: LiveData<List<PasswordEntry>?> = MediatorLiveData<List<PasswordEntry>?>().also { result ->
        result.addSource(searchQuery) { query ->
            if (query.isNullOrEmpty()) {
                searchSource?.let { result.removeSource(it) }
                searchSource = null
                result.value = null
            } else {
                searchSource?.let { result.removeSource(it) }
                val src = passwordRepository.searchPasswords(query)
                result.addSource(src) { result.value = it }
                searchSource = src
            }
        }
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }
}
