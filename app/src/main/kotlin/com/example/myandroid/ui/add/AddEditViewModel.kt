package com.example.myandroid.ui.add

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.myandroid.data.db.AppDatabase
import com.example.myandroid.data.db.entity.Category
import com.example.myandroid.data.db.entity.PasswordEntry
import com.example.myandroid.data.repository.CategoryRepository
import com.example.myandroid.data.repository.PasswordRepository
import com.example.myandroid.util.SingleLiveEvent
import com.example.myandroid.util.StorageCrypto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddEditViewModel(application: Application) : AndroidViewModel(application) {

    private val passwordRepository = PasswordRepository(application)
    private val categoryRepository = CategoryRepository(application)

    val allCategories: LiveData<List<Category>> = categoryRepository.allCategories

    private var currentEntry: PasswordEntry? = null
    private var isEditMode = false

    val saveResult = SingleLiveEvent<Boolean>()
    val deleteResult = SingleLiveEvent<Boolean>()
    val loadedEntry = SingleLiveEvent<PasswordEntry>()

    fun isEditMode() = isEditMode
    fun getCurrentEntry() = currentEntry

    fun loadPassword(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val entry = passwordRepository.getPasswordByIdSync(id)
            if (entry != null) {
                isEditMode = true
                entry.password = StorageCrypto.decrypt(entry.password)
            }
            currentEntry = entry
            loadedEntry.postValue(entry)
        }
    }

    fun savePassword(
        title: String, username: String, password: String,
        url: String, notes: String, categoryId: Long?, entryType: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (isEditMode && currentEntry != null) {
                    currentEntry!!.apply {
                        this.title = title
                        this.username = username
                        this.password = password
                        this.url = url
                        this.notes = notes
                        this.categoryId = categoryId
                        this.entryType = entryType
                        this.updatedAt = System.currentTimeMillis()
                    }
                    passwordRepository.update(currentEntry!!)
                } else {
                    val newEntry = PasswordEntry().apply {
                        this.title = title
                        this.username = username
                        this.password = password
                        this.url = url
                        this.notes = notes
                        this.categoryId = categoryId
                        this.entryType = entryType
                    }
                    passwordRepository.insert(newEntry)
                }
                saveResult.postValue(true)
            } catch (e: Exception) {
                saveResult.postValue(false)
            }
        }
    }

    fun deletePassword() {
        currentEntry?.let { entry ->
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    passwordRepository.delete(entry)
                    deleteResult.postValue(true)
                } catch (e: Exception) {
                    deleteResult.postValue(false)
                }
            }
        }
    }

    fun validate(title: String, username: String): Boolean {
        return title.isNotBlank() && username.isNotBlank()
    }
}
