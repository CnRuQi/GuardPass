package com.example.myandroid.ui.entry

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.myandroid.data.db.entity.Category
import com.example.myandroid.data.db.entity.PasswordEntry
import com.example.myandroid.data.repository.CategoryRepository
import com.example.myandroid.data.repository.PasswordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class EntryFormState(
    val entryType: Int = PasswordEntry.TYPE_PASSWORD,
    val title: String = "",
    val username: String = "",
    val secret: String = "",
    val url: String = "",
    val notes: String = "",
    val categoryId: Long? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val saved: Boolean = false,
    val deleted: Boolean = false,
    val error: String? = null,
    val fieldErrors: Map<String, String> = emptyMap()
)

class EntryEditorViewModel(application: Application) : AndroidViewModel(application) {
    private val passwordRepository = PasswordRepository(application)
    private val categoryRepository = CategoryRepository(application)
    private var currentEntry: PasswordEntry? = null
    private var loadedId: Long? = null

    val categories: LiveData<List<Category>> = categoryRepository.allCategories
    private val _state = MutableStateFlow(EntryFormState())
    val state: StateFlow<EntryFormState> = _state

    fun load(entryId: Long?) {
        if (loadedId == entryId && !_state.value.isLoading) return
        loadedId = entryId
        if (entryId == null) {
            currentEntry = null
            _state.value = EntryFormState(isLoading = false)
            return
        }
        currentEntry = null
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                currentEntry = passwordRepository.getDecryptedPasswordByIdSync(entryId)
                val entry = currentEntry
                if (entry == null) {
                    _state.value = EntryFormState(isLoading = false, error = "记录不存在")
                } else {
                    _state.value = EntryFormState(
                        entryType = entry.entryType,
                        title = entry.title.orEmpty(),
                        username = entry.username.orEmpty(),
                        secret = entry.password.orEmpty(),
                        url = entry.url.orEmpty(),
                        notes = entry.notes.orEmpty(),
                        categoryId = entry.categoryId,
                        isLoading = false
                    )
                }
            } catch (error: Exception) {
                _state.value = EntryFormState(isLoading = false, error = error.message ?: "读取记录失败")
            }
        }
    }

    fun setType(type: Int) {
        _state.value = _state.value.copy(
            entryType = type,
            username = if (type == PasswordEntry.TYPE_API_KEY) "" else _state.value.username,
            fieldErrors = emptyMap(),
            error = null
        )
    }

    fun setTitle(value: String) = update { copy(title = value) }
    fun setUsername(value: String) = update { copy(username = value) }
    fun setSecret(value: String) = update { copy(secret = value) }
    fun setUrl(value: String) = update { copy(url = value) }
    fun setNotes(value: String) = update { copy(notes = value) }
    fun setCategory(value: Long?) = update { copy(categoryId = value) }

    fun save() {
        val current = _state.value
        if (loadedId != null && currentEntry == null) {
            _state.value = current.copy(error = "记录不存在")
            return
        }
        val errors = buildMap {
            if (current.title.isBlank()) put("title", "请输入标题")
            if (current.entryType == PasswordEntry.TYPE_PASSWORD && current.username.isBlank()) {
                put("username", "请输入账号")
            }
            if (current.secret.isBlank()) put("secret", "请输入${if (current.entryType == PasswordEntry.TYPE_API_KEY) "API Key" else "密码"}")
        }
        if (errors.isNotEmpty()) {
            _state.value = current.copy(fieldErrors = errors, error = null)
            return
        }

        _state.value = current.copy(isSaving = true, error = null, fieldErrors = emptyMap())
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val entry = currentEntry ?: PasswordEntry()
                entry.entryType = current.entryType
                entry.title = current.title.trim()
                entry.username = if (current.entryType == PasswordEntry.TYPE_API_KEY) "" else current.username.trim()
                entry.password = current.secret
                entry.url = current.url.trim()
                entry.notes = current.notes.trim()
                entry.categoryId = current.categoryId
                if (currentEntry == null) {
                    passwordRepository.insertSync(entry)
                } else {
                    passwordRepository.updateSync(entry)
                }
                _state.value = _state.value.copy(isSaving = false, saved = true)
            } catch (error: Exception) {
                _state.value = _state.value.copy(
                    isSaving = false,
                    error = error.message ?: "保存失败"
                )
            }
        }
    }

    fun delete() {
        val entry = currentEntry ?: return
        _state.value = _state.value.copy(isDeleting = true, error = null)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                passwordRepository.deleteSync(entry)
                _state.value = _state.value.copy(isDeleting = false, deleted = true)
            } catch (error: Exception) {
                _state.value = _state.value.copy(isDeleting = false, error = error.message ?: "删除失败")
            }
        }
    }

    private fun update(block: EntryFormState.() -> EntryFormState) {
        _state.value = block(_state.value).copy(fieldErrors = emptyMap(), error = null)
    }
}
