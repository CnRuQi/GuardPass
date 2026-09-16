package com.example.myandroid.data;

import static org.junit.Assert.assertNull;

import android.content.Context;

import androidx.room.Room;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.myandroid.data.db.AppDatabase;
import com.example.myandroid.data.db.entity.Category;
import com.example.myandroid.data.db.entity.PasswordEntry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AppDatabaseInstrumentedTest {

    private AppDatabase database;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
    }

    @After
    public void tearDown() {
        database.close();
    }

    @Test
    public void deletingCategoryLeavesPasswordsUncategorized() {
        Category category = new Category();
        category.setName("测试分类");
        category.setId(database.categoryDao().insert(category));

        PasswordEntry entry = new PasswordEntry();
        entry.setTitle("测试记录");
        entry.setUsername("user");
        entry.setPassword("");
        entry.setCategoryId(category.getId());
        long entryId = database.passwordDao().insert(entry);

        database.categoryDao().delete(category);

        assertNull(database.passwordDao().getPasswordByIdSync(entryId).getCategoryId());
    }
}
