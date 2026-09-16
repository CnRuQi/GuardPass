package com.example.myandroid.ui.category

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.example.myandroid.data.db.AppDatabase
import com.example.myandroid.data.db.dao.PasswordDao
import com.example.myandroid.data.db.entity.Category
import com.example.myandroid.data.repository.CategoryRepository

class CategoryViewModel(application: Application) : AndroidViewModel(application) {
    private val categoryRepository = CategoryRepository(application)

    val allCategories: LiveData<List<Category>> = categoryRepository.allCategories
    private val rawCounts: LiveData<List<PasswordDao.CategoryCount>> = categoryRepository.getCategoryCounts()
    private val _categoryCounts = MediatorLiveData<Map<Long, Int>>()
    val categoryCounts: LiveData<Map<Long, Int>> = _categoryCounts

    val addResult = MutableLiveData<Boolean>()
    val editResult = MutableLiveData<Boolean>()
    val deleteResult = MutableLiveData<Boolean>()

    init {
        _categoryCounts.addSource(allCategories) { categories -> rebuildCounts(categories, rawCounts.value) }
        _categoryCounts.addSource(rawCounts) { counts -> rebuildCounts(allCategories.value, counts) }
    }

    private fun rebuildCounts(categories: List<Category>?, counts: List<PasswordDao.CategoryCount>?) {
        val result = mutableMapOf<Long, Int>()
        categories.orEmpty().forEach { result[it.id] = 0 }
        counts.orEmpty().forEach { count ->
            count.category_id?.let { result[it] = count.count }
        }
        _categoryCounts.value = result
    }

    fun addCategory(name: String) {
        AppDatabase.databaseWriteExecutor.execute {
            try {
                val normalizedName = name.trim()
                if (normalizedName.isBlank()) {
                    addResult.postValue(false)
                    return@execute
                }
                val existing = categoryRepository.allCategoriesSync
                if (existing.any { it.name.orEmpty().equals(normalizedName, ignoreCase = true) }) {
                    addResult.postValue(false)
                    return@execute
                }
                categoryRepository.insertSync(Category().apply {
                    this.name = normalizedName
                    sortOrder = existing.size
                    icon = "ic_category"
                })
                addResult.postValue(true)
            } catch (_: Exception) {
                addResult.postValue(false)
            }
        }
    }

    fun updateCategory(category: Category, newName: String) {
        AppDatabase.databaseWriteExecutor.execute {
            try {
                val normalizedName = newName.trim()
                if (normalizedName.isBlank()) {
                    editResult.postValue(false)
                    return@execute
                }
                val existing = categoryRepository.allCategoriesSync
                if (existing.any { it.id != category.id && it.name.orEmpty().equals(normalizedName, ignoreCase = true) }) {
                    editResult.postValue(false)
                    return@execute
                }
                val updated = Category().apply {
                    id = category.id
                    name = normalizedName
                    icon = category.icon
                    sortOrder = category.sortOrder
                    createdAt = category.createdAt
                }
                categoryRepository.updateSync(updated)
                editResult.postValue(true)
            } catch (_: Exception) {
                editResult.postValue(false)
            }
        }
    }

    fun deleteCategory(category: Category) {
        AppDatabase.databaseWriteExecutor.execute {
            try {
                categoryRepository.deleteSync(category)
                deleteResult.postValue(true)
            } catch (_: Exception) {
                deleteResult.postValue(false)
            }
        }
    }
}
