package com.example.myandroid.ui.settings

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myandroid.data.repository.BackupRepository
import com.example.myandroid.util.StorageCrypto
import com.example.myandroid.util.WebDavUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val storedSecretPrefix = "guardpass:v1:"

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
    private val webDav = WebDavUtils.getInstance()
    private val backupRepository = BackupRepository(application)

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
        val rawUsername = prefs.getString(KEY_USERNAME, "") ?: ""
        val rawPassword = prefs.getString(KEY_PASSWORD, "") ?: ""
        val username = readStoredSecret(rawUsername)
        val password = readStoredSecret(rawPassword)

        _config.value = WebDavConfig(
            serverUrl = prefs.getString(KEY_SERVER_URL, "") ?: "",
            username = username.value,
            password = password.value
        )

        if (username.wasPlaintext || password.wasPlaintext) {
            rewriteStoredSecrets(username.value, password.value)
        }
    }

    fun updateConfig(newConfig: WebDavConfig) {
        val url = normalizeUrl(newConfig.serverUrl)
        val normalized = newConfig.copy(serverUrl = url)
        try {
            val encryptedUsername = encryptStoredSecret(normalized.username)
            val encryptedPassword = encryptStoredSecret(normalized.password)
            prefs.edit()
                .putString(KEY_SERVER_URL, normalized.serverUrl)
                .putString(KEY_USERNAME, encryptedUsername)
                .putString(KEY_PASSWORD, encryptedPassword)
                .apply()
            _config.value = normalized
        } catch (e: Exception) {
            _connectionStatus.value = StatusMessage("config_failed", false, e.message)
        }
    }

    private fun normalizeUrl(url: String): String {
        if (url.isBlank()) return url
        if (url.startsWith("http://") || url.startsWith("https://")) return url
        return "https://$url"
    }

    fun testConnection() {
        val c = _config.value ?: return
        if (c.serverUrl.isBlank()) {
            _connectionStatus.value = StatusMessage("connection_failed", false, "请输入服务器地址")
            return
        }
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
        val c = _config.value
        if (c == null || c.serverUrl.isBlank()) {
            _syncStatus.value = StatusMessage("upload_failed", false, "请先保存服务器地址")
            return
        }
        _syncStatus.value = StatusMessage("uploading", false)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val encrypted = backupRepository.exportEncryptedSync(encryptionKey)
                webDav.uploadFile(c.serverUrl, c.username, c.password, REMOTE_FILE, encrypted)
                val count = backupRepository.exportSync().passwords?.size ?: 0
                _syncStatus.postValue(
                    StatusMessage("upload_success", true, count.toString())
                )
            } catch (e: Exception) {
                _syncStatus.postValue(
                    StatusMessage("upload_failed", false, e.message)
                )
            }
        }
    }

    fun downloadAndRestore(encryptionKey: String) {
        val c = _config.value
        if (c == null || c.serverUrl.isBlank()) {
            _syncStatus.value = StatusMessage("download_failed", false, "请先保存服务器地址")
            return
        }
        _syncStatus.value = StatusMessage("downloading", false)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val encrypted = webDav.downloadFile(c.serverUrl, c.username, c.password, REMOTE_FILE)
                val count = backupRepository.restoreEncryptedSync(encrypted, encryptionKey)
                _syncStatus.postValue(
                    StatusMessage("download_success", true, count.toString())
                )
            } catch (e: Exception) {
                _syncStatus.postValue(
                    StatusMessage("download_failed", false, e.message)
                )
            }
        }
    }

    private data class StoredSecret(val value: String, val wasPlaintext: Boolean)

    private fun readStoredSecret(raw: String): StoredSecret {
        if (raw.isBlank()) return StoredSecret("", false)
        if (!raw.startsWith(storedSecretPrefix)) return StoredSecret(raw, true)
        return try {
            StoredSecret(StorageCrypto.decryptRequired(raw.removePrefix(storedSecretPrefix)), false)
        } catch (_: Exception) {
            StoredSecret("", false)
        }
    }

    private fun encryptStoredSecret(value: String): String =
        storedSecretPrefix + StorageCrypto.encryptRequired(value)

    private fun rewriteStoredSecrets(username: String, password: String) {
        try {
            prefs.edit()
                .putString(KEY_USERNAME, encryptStoredSecret(username))
                .putString(KEY_PASSWORD, encryptStoredSecret(password))
                .apply()
        } catch (_: Exception) {
            // Keep the loaded values available; the next successful save retries encryption.
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
