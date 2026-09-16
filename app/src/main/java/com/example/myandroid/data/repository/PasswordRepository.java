package com.example.myandroid.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.myandroid.data.db.AppDatabase;
import com.example.myandroid.data.db.dao.PasswordDao;
import com.example.myandroid.data.db.entity.PasswordEntry;
import com.example.myandroid.util.StorageCrypto;
import android.util.Log;

import java.util.List;

public class PasswordRepository {

    private static final String TAG = "PasswordRepository";

    private final PasswordDao passwordDao;
    private final LiveData<List<PasswordEntry>> allPasswords;

    public static final class DecryptedEntriesResult {
        private final List<PasswordEntry> entries;
        private final String error;

        private DecryptedEntriesResult(List<PasswordEntry> entries, String error) {
            this.entries = entries;
            this.error = error;
        }

        public static DecryptedEntriesResult success(List<PasswordEntry> entries) {
            return new DecryptedEntriesResult(entries, null);
        }

        public static DecryptedEntriesResult failure(String error) {
            return new DecryptedEntriesResult(null, error);
        }

        public List<PasswordEntry> getEntries() {
            return entries;
        }

        public String getError() {
            return error;
        }
    }

    public PasswordRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        passwordDao = db.passwordDao();
        allPasswords = Transformations.map(passwordDao.getAllPasswords(), entries -> {
            if (entries != null) {
                for (PasswordEntry e : entries) {
                    e.setPassword(StorageCrypto.decrypt(e.getPassword()));
                }
            }
            return entries;
        });
    }

    public LiveData<List<PasswordEntry>> getAllPasswords() {
        return allPasswords;
    }

    /** Failure-aware read used by the active Compose navigation path. */
    public LiveData<DecryptedEntriesResult> getAllPasswordsRequired() {
        return Transformations.map(passwordDao.getAllPasswords(), this::decryptResult);
    }

    public LiveData<List<PasswordEntry>> getPasswordsByCategory(long categoryId) {
        return Transformations.map(passwordDao.getPasswordsByCategory(categoryId), entries -> {
            if (entries != null) {
                for (PasswordEntry e : entries) e.setPassword(StorageCrypto.decrypt(e.getPassword()));
            }
            return entries;
        });
    }

    public LiveData<PasswordEntry> getPasswordById(long id) {
        return Transformations.map(passwordDao.getPasswordById(id), entry -> {
            if (entry != null) entry.setPassword(StorageCrypto.decrypt(entry.getPassword()));
            return entry;
        });
    }

    public PasswordEntry getPasswordByIdSync(long id) {
        return passwordDao.getPasswordByIdSync(id);
    }

    /** Returns a detached entry with its secret decrypted for editor/backup use. */
    public PasswordEntry getDecryptedPasswordByIdSync(long id) throws Exception {
        PasswordEntry stored = passwordDao.getPasswordByIdSync(id);
        return stored == null ? null : decryptCopy(stored);
    }

    public LiveData<List<PasswordEntry>> searchPasswords(String query) {
        return Transformations.map(passwordDao.searchPasswords(query), entries -> {
            if (entries != null) {
                for (PasswordEntry e : entries) e.setPassword(StorageCrypto.decrypt(e.getPassword()));
            }
            return entries;
        });
    }

    /** Failure-aware search used by the active Compose navigation path. */
    public LiveData<DecryptedEntriesResult> searchPasswordsRequired(String query) {
        return Transformations.map(passwordDao.searchPasswords(query), this::decryptResult);
    }

    public LiveData<List<PasswordEntry>> getFavoritePasswords() {
        return Transformations.map(passwordDao.getFavoritePasswords(), entries -> {
            if (entries != null) {
                for (PasswordEntry e : entries) e.setPassword(StorageCrypto.decrypt(e.getPassword()));
            }
            return entries;
        });
    }

    public LiveData<Integer> getPasswordCount() {
        return passwordDao.getPasswordCount();
    }

    public void insert(PasswordEntry entry) {
        entry.setPassword(StorageCrypto.encrypt(entry.getPassword()));
        AppDatabase.databaseWriteExecutor.execute(() -> passwordDao.insert(entry));
    }

    /** Synchronous, failure-aware write used by the new UI and backup flow. */
    public long insertSync(PasswordEntry entry) throws Exception {
        PasswordEntry stored = encryptCopy(entry);
        return passwordDao.insert(stored);
    }

    public void update(PasswordEntry entry) {
        entry.setPassword(StorageCrypto.encrypt(entry.getPassword()));
        AppDatabase.databaseWriteExecutor.execute(() -> {
            entry.setUpdatedAt(System.currentTimeMillis());
            passwordDao.update(entry);
        });
    }

    /** Synchronous, failure-aware write used by the new UI and backup flow. */
    public void updateSync(PasswordEntry entry) throws Exception {
        PasswordEntry stored = encryptCopy(entry);
        stored.setUpdatedAt(System.currentTimeMillis());
        passwordDao.update(stored);
    }

    public void delete(PasswordEntry entry) {
        AppDatabase.databaseWriteExecutor.execute(() -> passwordDao.delete(entry));
    }

    public void deleteSync(PasswordEntry entry) {
        passwordDao.delete(entry);
    }

    public void deleteByCategoryId(long categoryId) {
        AppDatabase.databaseWriteExecutor.execute(() -> passwordDao.deleteByCategoryId(categoryId));
    }

    public List<PasswordEntry> getAllPasswordsSync() {
        List<PasswordEntry> entries = passwordDao.getAllPasswordsSync();
        if (entries != null) {
            for (PasswordEntry e : entries) e.setPassword(StorageCrypto.decrypt(e.getPassword()));
        }
        return entries;
    }

    /** Returns detached entries with required decryption for backup export. */
    public List<PasswordEntry> getAllPasswordsDecryptedSync() throws Exception {
        List<PasswordEntry> entries = passwordDao.getAllPasswordsSync();
        java.util.ArrayList<PasswordEntry> result = new java.util.ArrayList<>();
        if (entries != null) {
            for (PasswordEntry entry : entries) result.add(decryptCopy(entry));
        }
        return result;
    }

    public void insertAll(List<PasswordEntry> entries) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            for (PasswordEntry e : entries) {
                e.setPassword(StorageCrypto.encrypt(e.getPassword()));
            }
            passwordDao.insertAll(entries);
        });
    }

    public void deleteAll() {
        AppDatabase.databaseWriteExecutor.execute(() -> passwordDao.deleteAll());
    }

    public void deleteAllSync() {
        passwordDao.deleteAll();
    }

    public int getPasswordCountByCategorySync(long categoryId) {
        return passwordDao.getPasswordCountByCategorySync(categoryId);
    }

    public int getUncategorizedPasswordCountSync() {
        return passwordDao.getUncategorizedPasswordCountSync();
    }
    
    public List<PasswordDao.CategoryCount> getCategoryCountsSync() {
        return passwordDao.getCategoryCountsSync();
    }

    /**
     * Moves API keys saved by the original UI in the username column into the
     * encrypted password column. The operation is idempotent.
     */
    public void migrateLegacyApiKeys() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                List<PasswordEntry> entries = passwordDao.getAllPasswordsSync();
                if (entries == null) return;
                for (PasswordEntry entry : entries) {
                    if (!entry.isApiKey()) continue;
                    if (entry.getUsername() == null || entry.getUsername().isEmpty()) continue;

                    String currentSecret = entry.getPassword() == null || entry.getPassword().isEmpty()
                            ? ""
                            : StorageCrypto.decryptRequired(entry.getPassword());
                    if (!currentSecret.isEmpty()) continue;

                    String legacySecret = entry.getUsername();
                    entry.setUsername("");
                    entry.setPassword(StorageCrypto.encryptRequired(legacySecret));
                    entry.setUpdatedAt(System.currentTimeMillis());
                    passwordDao.update(entry);
                }
            } catch (Exception e) {
                Log.e(TAG, "Legacy API key migration failed", e);
            }
        });
    }

    private PasswordEntry decryptCopy(PasswordEntry stored) throws Exception {
        PasswordEntry copy = copyEntry(stored);
        copy.setPassword(StorageCrypto.decryptRequired(stored.getPassword()));
        return copy;
    }

    private DecryptedEntriesResult decryptResult(List<PasswordEntry> entries) {
        try {
            java.util.ArrayList<PasswordEntry> result = new java.util.ArrayList<>();
            if (entries != null) {
                for (PasswordEntry entry : entries) result.add(decryptCopy(entry));
            }
            return DecryptedEntriesResult.success(result);
        } catch (Exception error) {
            Log.e(TAG, "Required decryption failed", error);
            return DecryptedEntriesResult.failure("无法解密本地记录，请检查设备安全存储");
        }
    }

    private PasswordEntry encryptCopy(PasswordEntry source) throws Exception {
        PasswordEntry copy = copyEntry(source);
        copy.setPassword(StorageCrypto.encryptRequired(source.getPassword()));
        return copy;
    }

    private PasswordEntry copyEntry(PasswordEntry source) {
        PasswordEntry copy = new PasswordEntry();
        copy.setId(source.getId());
        copy.setCategoryId(source.getCategoryId());
        copy.setTitle(source.getTitle());
        copy.setUsername(source.getUsername());
        copy.setPassword(source.getPassword());
        copy.setUrl(source.getUrl());
        copy.setNotes(source.getNotes());
        copy.setEntryType(source.getEntryType());
        copy.setFavorite(source.isFavorite());
        copy.setCreatedAt(source.getCreatedAt());
        copy.setUpdatedAt(source.getUpdatedAt());
        return copy;
    }
}
