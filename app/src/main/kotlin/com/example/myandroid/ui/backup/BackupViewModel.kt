package com.example.myandroid.ui.backup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myandroid.data.repository.BackupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class BackupUiState(
    val isBusy: Boolean = false,
    val exportContent: String? = null,
    val message: String? = null,
    val success: Boolean = false
)

class BackupViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BackupRepository(application)
    private val _state = MutableStateFlow(BackupUiState())
    val state: StateFlow<BackupUiState> = _state

    fun export(password: String) {
        _state.value = BackupUiState(isBusy = true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val content = repository.exportEncryptedSync(password)
                _state.value = BackupUiState(
                    exportContent = content,
                    message = "备份已准备好，请选择保存位置",
                    success = true
                )
            } catch (error: Exception) {
                _state.value = BackupUiState(message = error.message ?: "导出失败")
            }
        }
    }

    fun restore(content: String, password: String) {
        _state.value = BackupUiState(isBusy = true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val count = repository.restoreEncryptedSync(content, password)
                _state.value = BackupUiState(message = "恢复完成（${count} 条记录）", success = true)
            } catch (error: Exception) {
                _state.value = BackupUiState(message = error.message ?: "恢复失败")
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null, exportContent = null)
    }
}
