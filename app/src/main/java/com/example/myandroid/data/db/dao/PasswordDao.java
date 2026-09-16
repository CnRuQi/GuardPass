package com.example.myandroid.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myandroid.data.db.entity.PasswordEntry;

import java.util.List;

@Dao
public interface PasswordDao {

    @Insert
    long insert(PasswordEntry entry);

    @Update
    void update(PasswordEntry entry);

    @Delete
    void delete(PasswordEntry entry);

    @Query("SELECT * FROM password_entries ORDER BY updated_at DESC")
    LiveData<List<PasswordEntry>> getAllPasswords();

    @Query("SELECT * FROM password_entries WHERE category_id = :categoryId ORDER BY updated_at DESC")
    LiveData<List<PasswordEntry>> getPasswordsByCategory(long categoryId);

    @Query("SELECT * FROM password_entries WHERE id = :id")
    LiveData<PasswordEntry> getPasswordById(long id);

    @Query("SELECT * FROM password_entries WHERE id = :id")
    PasswordEntry getPasswordByIdSync(long id);

    @Query("SELECT * FROM password_entries WHERE title LIKE '%' || :query || '%' OR username LIKE '%' || :query || '%' OR url LIKE '%' || :query || '%' ORDER BY updated_at DESC")
    LiveData<List<PasswordEntry>> searchPasswords(String query);

    @Query("SELECT * FROM password_entries WHERE is_favorite = 1 ORDER BY updated_at DESC")
    LiveData<List<PasswordEntry>> getFavoritePasswords();

    @Query("SELECT COUNT(*) FROM password_entries")
    LiveData<Integer> getPasswordCount();

    /**
     * Kept for compatibility with the original repository API. Removing a
     * category must never remove the records assigned to it.
     */
    @Query("UPDATE password_entries SET category_id = NULL WHERE category_id = :categoryId")
    void deleteByCategoryId(long categoryId);

    @Query("SELECT * FROM password_entries ORDER BY updated_at DESC")
    List<PasswordEntry> getAllPasswordsSync();

    @Insert
    void insertAll(List<PasswordEntry> entries);

    @Query("DELETE FROM password_entries")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM password_entries WHERE category_id = :categoryId")
    int getPasswordCountByCategorySync(long categoryId);

    @Query("SELECT COUNT(*) FROM password_entries WHERE category_id IS NULL")
    int getUncategorizedPasswordCountSync();
    
    @Query("SELECT category_id, COUNT(*) as count FROM password_entries GROUP BY category_id")
    List<CategoryCount> getCategoryCountsSync();

    @Query("SELECT category_id, COUNT(*) as count FROM password_entries GROUP BY category_id")
    LiveData<List<CategoryCount>> getCategoryCounts();
    
    static class CategoryCount {
        public Long category_id;
        public int count;
    }
}
