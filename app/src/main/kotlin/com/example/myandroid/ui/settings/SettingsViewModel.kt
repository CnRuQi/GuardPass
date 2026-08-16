package com.example.myandroid.ui.settings

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myandroid.data.model.ExportData
import com.example.myandroid.data.repository.CategoryRepository
import com.example.myandroid.data.repository.PasswordRepository
import com.example.myandroid.util.CryptoUtils
import com.example.myandroid.util.WebDavUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    data class WebDavConfig(
        val serverUrl: String = "",
        val username: String = "",
        val password: String = ""
    )

    data class StatusMessage(
        val key: String,
        val success: Boolean,
        val param: String? = null
    )

    private val prefs: SharedPreferences =
        application.getSharedPreferences(PREFS_NAME, Application.MODE_PRIVATE)
    private val gson = Gson()
    private val webDav = WebDavUtils.getInstance()
    private val passwordRepo = PasswordRepository(application)
    private val categoryRepo = CategoryRepository(application)

    private val _config = MutableLiveData(WebDavConfig())
    val config: LiveData<WebDavConfig> = _config

    private val _connectionStatus = MutableLiveData<StatusMessage>()
    val connectionStatus: LiveData<StatusMessage> = _connectionStatus

    private val _syncStatus = MutableLiveData<StatusMessage>()
    val syncStatus: LiveData<StatusMessage> = _syncStatus

    init {
        loadSavedConfig()
    }

    private fun loadSavedConfig() {
        _config.value = WebDavConfig(
            serverUrl = prefs.getString(KEY_SERVER_URL, "") ?: "",
            username = prefs.getString(KEY_USERNAME, "") ?: "",
            password = prefs.getString(KEY_PASSWORD, "") ?: ""
        )
    }

    fun updateConfig(newConfig: WebDavConfig) {
        val url = normalizeUrl(newConfig.serverUrl)
        val normalized = newConfig.copy(serverUrl = url)
        prefs.edit()
            .putString(KEY_SERVER_URL, normalized.serverUrl)
            .putString(KEY_USERNAME, normalized.username)
            .putString(KEY_PASSWORD, normalized.password)
            .apply()
        _config.value = normalized
    }

    private fun normalizeUrl(url: String): String {
        if (url.isBlank()) return url
        if (url.startsWith("http://") || url.startsWith("https://")) return url
        return "https://$url"
    }

    fun testConnection() {
        val c = _config.value ?: return
        _connectionStatus.value = StatusMessage("connecting", false)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val ok = webDav.testConnection(c.serverUrl, c.username, c.password)
                _connectionStatus.postValue(
                    StatusMessage(if (ok) "connection_success" else "connection_failed_server", ok)
                )
            } catch (e: Exception) {
                _connectionStatus.postValue(
                    StatusMessage("connection_failed", false, e.message)
                )
            }
        }
    }

    fun uploadBackup(encryptionKey: String) {
        _syncStatus.value = StatusMessage("uploading", false)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val passwords = passwordRepo.allPasswordsSync
                val categories = categoryRepo.allCategoriesSync
                val data = ExportData()
                data.version = 1
                data.timestamp = System.currentTimeMillis()
                data.categories = categories
                data.passwords = passwords
                val json = gson.toJson(data)
                val encrypted = CryptoUtils.encrypt(json, encryptionKey)
                val c = _config.value
                if (c != null) {
                    webDav.uploadFile(c.serverUrl, c.username, c.password, REMOTE_FILE, encrypted)
                }
                _syncStatus.postValue(
                    StatusMessage("upload_success", true, passwords.size.toString())
                )
            } catch (e: Exception) {
                _syncStatus.postValue(
                    StatusMessage("upload_failed", false, e.message)
                )
            }
        }
    }

    fun downloadAndRestore(encryptionKey: String) {
        _syncStatus.value = StatusMessage("downloading", false)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val c = _config.value ?: return@launch
                val encrypted = webDav.downloadFile(c.serverUrl, c.username, c.password, REMOTE_FILE)
                val decrypted = CryptoUtils.decrypt(encrypted, encryptionKey)
                val type = object : TypeToken<ExportData>() {}.type
                val data: ExportData = gson.fromJson(decrypted, type)
                if (data.passwords == null) {
                    _syncStatus.postValue(StatusMessage("download_failed", false, "数据格式错误"))
                    return@launch
                }
                passwordRepo.deleteAll()
                if (data.passwords.isNotEmpty()) {
                    passwordRepo.insertAll(data.passwords)
                }
                _syncStatus.postValue(
                    StatusMessage("download_success", true, data.passwords.size.toString())
                )
            } catch (e: Exception) {
                _syncStatus.postValue(
                    StatusMessage("download_failed", false, e.message)
                )
            }
        }
    }

    companion object {
        private const val PREFS_NAME = "webdav_prefs"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val REMOTE_FILE = "password_backup.enc"
    }
}
