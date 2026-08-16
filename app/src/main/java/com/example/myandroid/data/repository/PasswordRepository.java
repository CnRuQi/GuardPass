package com.example.myandroid.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.myandroid.data.db.AppDatabase;
import com.example.myandroid.data.db.dao.PasswordDao;
import com.example.myandroid.data.db.entity.PasswordEntry;
import com.example.myandroid.util.StorageCrypto;

import java.util.List;

public class PasswordRepository {

    private final PasswordDao passwordDao;
    private final LiveData<List<PasswordEntry>> allPasswords;

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

    public LiveData<List<PasswordEntry>> searchPasswords(String query) {
        return Transformations.map(passwordDao.searchPasswords(query), entries -> {
            if (entries != null) {
                for (PasswordEntry e : entries) e.setPassword(StorageCrypto.decrypt(e.getPassword()));
            }
            return entries;
        });
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

    public void update(PasswordEntry entry) {
        entry.setPassword(StorageCrypto.encrypt(entry.getPassword()));
        AppDatabase.databaseWriteExecutor.execute(() -> {
            entry.setUpdatedAt(System.currentTimeMillis());
            passwordDao.update(entry);
        });
    }

    public void delete(PasswordEntry entry) {
        AppDatabase.databaseWriteExecutor.execute(() -> passwordDao.delete(entry));
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

    public int getPasswordCountByCategorySync(long categoryId) {
        return passwordDao.getPasswordCountByCategorySync(categoryId);
    }

    public int getUncategorizedPasswordCountSync() {
        return passwordDao.getUncategorizedPasswordCountSync();
    }
    
    public List<PasswordDao.CategoryCount> getCategoryCountsSync() {
        return passwordDao.getCategoryCountsSync();
    }
}
