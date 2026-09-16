package com.example.myandroid.data.repository;

import android.app.Application;

import com.example.myandroid.data.db.AppDatabase;
import com.example.myandroid.data.db.entity.Category;
import com.example.myandroid.data.db.entity.PasswordEntry;
import com.example.myandroid.data.model.ExportData;
import com.example.myandroid.util.CryptoUtils;
import com.example.myandroid.util.StorageCrypto;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Single boundary for local and WebDAV backups. Secrets are plaintext only in
 * the short-lived in-memory export object and are encrypted before persistence.
 */
public class BackupRepository {

    private static final int CURRENT_VERSION = 2;

    private final AppDatabase database;
    private final PasswordRepository passwordRepository;
    private final CategoryRepository categoryRepository;
    private final Gson gson = new Gson();

    public BackupRepository(Application application) {
        database = AppDatabase.getInstance(application);
        passwordRepository = new PasswordRepository(application);
        categoryRepository = new CategoryRepository(application);
    }

    public ExportData exportSync() throws Exception {
        ExportData data = new ExportData();
        data.version = CURRENT_VERSION;
        data.timestamp = System.currentTimeMillis();
        data.categories = copyCategories(categoryRepository.getAllCategoriesSync());
        data.passwords = passwordRepository.getAllPasswordsDecryptedSync();
        return data;
    }

    public String exportEncryptedSync(String password) throws Exception {
        requireBackupPassword(password);
        return CryptoUtils.encrypt(gson.toJson(exportSync()), password);
    }

    /** Replaces all local records atomically after validating the import. */
    public void restoreSync(ExportData data) throws Exception {
        PreparedRestore prepared = prepareRestore(data);

        database.runInTransaction(() -> {
            // Password rows must be removed before their parent categories.
            database.passwordDao().deleteAll();
            database.categoryDao().deleteAll();
            if (!prepared.categories.isEmpty()) {
                database.categoryDao().insertAll(prepared.categories);
            }
            if (!prepared.passwords.isEmpty()) {
                database.passwordDao().insertAll(prepared.passwords);
            }
        });
    }

    public int restoreEncryptedSync(String encrypted, String password) throws Exception {
        requireBackupPassword(password);
        if (encrypted == null || encrypted.trim().isEmpty()) {
            throw new IllegalArgumentException("备份内容为空");
        }
        String json = CryptoUtils.decrypt(encrypted, password);
        ExportData data = gson.fromJson(json, ExportData.class);
        if (data == null) {
            throw new IllegalArgumentException("备份格式无效");
        }
        restoreSync(data);
        return data.passwords == null ? 0 : data.passwords.size();
    }

    private PreparedRestore prepareRestore(ExportData data) throws Exception {
        if (data == null) throw new IllegalArgumentException("备份格式无效");
        if (data.version != 1 && data.version != CURRENT_VERSION) {
            throw new IllegalArgumentException("不支持的备份版本: " + data.version);
        }
        if (data.passwords == null) {
            throw new IllegalArgumentException("备份缺少密码记录");
        }

        if (data.version == CURRENT_VERSION && data.categories == null) {
            throw new IllegalArgumentException("备份缺少分类列表");
        }
        List<Category> categories = data.categories == null
                ? new ArrayList<>()
                : copyCategories(data.categories);
        Set<Long> categoryIds = new HashSet<>();
        for (Category category : categories) {
            if (category == null || category.getId() <= 0 || isBlank(category.getName())) {
                throw new IllegalArgumentException("备份包含无效分类");
            }
            if (!categoryIds.add(category.getId())) {
                throw new IllegalArgumentException("备份包含重复分类");
            }
        }

        List<PasswordEntry> passwords = new ArrayList<>();
        Set<Long> passwordIds = new HashSet<>();
        for (PasswordEntry imported : data.passwords) {
            if (imported == null || isBlank(imported.getTitle())) {
                throw new IllegalArgumentException("备份包含无效记录");
            }
            if (imported.getId() > 0 && !passwordIds.add(imported.getId())) {
                throw new IllegalArgumentException("备份包含重复记录");
            }
            if (imported.getCategoryId() != null && !categoryIds.contains(imported.getCategoryId())) {
                if (data.version == 1 && data.categories == null) {
                    imported.setCategoryId(null);
                } else {
                    throw new IllegalArgumentException("记录引用了不存在的分类");
                }
            }

            PasswordEntry copy = copyEntry(imported);
            String secret = copy.getPassword() == null ? "" : copy.getPassword();
            // Version 1 stored API keys in username. Accept that legacy shape.
            if (copy.isApiKey() && isBlank(secret) && !isBlank(copy.getUsername())) {
                secret = copy.getUsername();
                copy.setUsername("");
            }
            copy.setPassword(StorageCrypto.encryptRequired(secret));
            passwords.add(copy);
        }
        return new PreparedRestore(categories, passwords);
    }

    private List<Category> copyCategories(List<Category> source) {
        List<Category> result = new ArrayList<>();
        if (source == null) return result;
        for (Category category : source) {
            if (category == null) {
                result.add(null);
                continue;
            }
            Category copy = new Category();
            copy.setId(category.getId());
            copy.setName(category.getName());
            copy.setIcon(category.getIcon());
            copy.setSortOrder(category.getSortOrder());
            copy.setCreatedAt(category.getCreatedAt());
            result.add(copy);
        }
        return result;
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

    private void requireBackupPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("请输入备份密码");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static final class PreparedRestore {
        private final List<Category> categories;
        private final List<PasswordEntry> passwords;

        private PreparedRestore(List<Category> categories, List<PasswordEntry> passwords) {
            this.categories = categories;
            this.passwords = passwords;
        }
    }
}
