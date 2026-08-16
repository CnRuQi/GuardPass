package com.example.myandroid.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.myandroid.data.db.AppDatabase;
import com.example.myandroid.data.db.dao.CategoryDao;
import com.example.myandroid.data.db.entity.Category;

import java.util.List;

public class CategoryRepository {

    private final CategoryDao categoryDao;
    private final LiveData<List<Category>> allCategories;

    public CategoryRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        categoryDao = db.categoryDao();
        allCategories = categoryDao.getAllCategories();
    }

    public LiveData<List<Category>> getAllCategories() {
        return allCategories;
    }

    public List<Category> getAllCategoriesSync() {
        return categoryDao.getAllCategoriesSync();
    }

    public LiveData<Category> getCategoryById(long id) {
        return categoryDao.getCategoryById(id);
    }

    public Category getCategoryByIdSync(long id) {
        return categoryDao.getCategoryByIdSync(id);
    }

    public LiveData<Integer> getCategoryCount() {
        return categoryDao.getCategoryCount();
    }

    public void insert(Category category) {
        AppDatabase.databaseWriteExecutor.execute(() -> categoryDao.insert(category));
    }

    public void update(Category category) {
        AppDatabase.databaseWriteExecutor.execute(() -> categoryDao.update(category));
    }

    public void delete(Category category) {
        AppDatabase.databaseWriteExecutor.execute(() -> categoryDao.delete(category));
    }
}
