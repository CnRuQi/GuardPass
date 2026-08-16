package com.example.myandroid.ui.category

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.example.myandroid.data.db.AppDatabase
import com.example.myandroid.data.db.entity.Category
import com.example.myandroid.data.repository.CategoryRepository
import com.example.myandroid.data.repository.PasswordRepository

class CategoryViewModel(application: Application) : AndroidViewModel(application) {

    private val categoryRepository = CategoryRepository(application)
    private val passwordRepository = PasswordRepository(application)

    val allCategories: LiveData<List<Category>> = categoryRepository.allCategories

    private val _categoryCounts = MediatorLiveData<Map<Long, Int>>()
    val categoryCounts: LiveData<Map<Long, Int>> = _categoryCounts

    val addResult = MutableLiveData<Boolean>()
    val editResult = MutableLiveData<Boolean>()
    val deleteResult = MutableLiveData<Boolean>()

    init {
        _categoryCounts.addSource(allCategories) { categories ->
            if (categories != null) {
                refreshCounts(categories)
            }
        }
    }

    private fun refreshCounts(categories: List<Category>) {
        AppDatabase.databaseWriteExecutor.execute {
            val counts = mutableMapOf<Long, Int>()
            for (cat in categories) {
                counts[cat.id] = 0
            }
            val dbCounts = passwordRepository.getCategoryCountsSync()
            for (cc in dbCounts) {
                cc.category_id?.let { id ->
                    counts[id] = cc.count
                }
            }
            _categoryCounts.postValue(counts)
        }
    }

    fun addCategory(name: String) {
        AppDatabase.databaseWriteExecutor.execute {
            try {
                val existing = categoryRepository.allCategoriesSync
                if (existing.any { it.name.equals(name, ignoreCase = true) }) {
                    addResult.postValue(false)
                    return@execute
                }
                val category = Category().apply {
                    this.name = name
                    this.sortOrder = existing.size
                }
                categoryRepository.insert(category)
                addResult.postValue(true)
            } catch (e: Exception) {
                addResult.postValue(false)
            }
        }
    }

    fun updateCategory(category: Category, newName: String) {
        AppDatabase.databaseWriteExecutor.execute {
            try {
                val existing = categoryRepository.allCategoriesSync
                if (existing.any { it.id != category.id && it.name.equals(newName, ignoreCase = true) }) {
                    editResult.postValue(false)
                    return@execute
                }
                category.name = newName
                categoryRepository.update(category)
                editResult.postValue(true)
            } catch (e: Exception) {
                editResult.postValue(false)
            }
        }
    }

    fun deleteCategory(category: Category) {
        AppDatabase.databaseWriteExecutor.execute {
            try {
                passwordRepository.deleteByCategoryId(category.id)
                categoryRepository.delete(category)
                deleteResult.postValue(true)
            } catch (e: Exception) {
                deleteResult.postValue(false)
            }
        }
    }
}
