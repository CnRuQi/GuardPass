package com.example.myandroid.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import android.app.Application;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.myandroid.data.db.entity.Category;
import com.example.myandroid.data.db.entity.PasswordEntry;
import com.example.myandroid.data.model.ExportData;
import com.example.myandroid.data.repository.BackupRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class BackupRepositoryInstrumentedTest {

    private BackupRepository repository;

    @Before
    public void setUp() {
        Application application = (Application) InstrumentationRegistry.getInstrumentation()
                .getTargetContext()
                .getApplicationContext();
        repository = new BackupRepository(application);
    }

    @Test
    public void restoreV1WithoutCategoriesIsAccepted() throws Exception {
        ExportData data = new ExportData();
        data.version = 1;
        data.timestamp = System.currentTimeMillis();
        data.categories = null;
        data.passwords = new java.util.ArrayList<>();

        repository.restoreSync(data);

        assertEquals(2, repository.exportSync().version);
    }

    @Test
    public void restoreV2RestoresCategoriesAndPasswordsTogether() throws Exception {
        ExportData data = validData();

        repository.restoreSync(data);

        ExportData restored = repository.exportSync();
        assertEquals(2, restored.version);
        assertEquals(1, restored.categories.size());
        assertEquals(1, restored.passwords.size());
        assertEquals("secret", restored.passwords.get(0).getPassword());
    }

    @Test
    public void restoreFailureDoesNotDeleteExistingData() throws Exception {
        repository.restoreSync(validData());

        ExportData invalid = validData();
        invalid.passwords.get(0).setCategoryId(9999L);
        assertThrows(IllegalArgumentException.class, () -> repository.restoreSync(invalid));

        ExportData restored = repository.exportSync();
        assertEquals("Existing", restored.passwords.get(0).getTitle());
    }

    private ExportData validData() {
        Category category = new Category();
        category.setId(1001L);
        category.setName("测试分类");
        category.setIcon("ic_category");

        PasswordEntry entry = new PasswordEntry();
        entry.setId(2001L);
        entry.setCategoryId(1001L);
        entry.setTitle("Existing");
        entry.setUsername("user");
        entry.setPassword("secret");

        ExportData data = new ExportData();
        data.version = 2;
        data.timestamp = System.currentTimeMillis();
        data.categories = java.util.Collections.singletonList(category);
        data.passwords = java.util.Collections.singletonList(entry);
        return data;
    }
}
