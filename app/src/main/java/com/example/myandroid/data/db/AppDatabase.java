package com.example.myandroid.data.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.myandroid.data.db.dao.CategoryDao;
import com.example.myandroid.data.db.dao.PasswordDao;
import com.example.myandroid.data.db.entity.Category;
import com.example.myandroid.data.db.entity.PasswordEntry;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {PasswordEntry.class, Category.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract PasswordDao passwordDao();
    public abstract CategoryDao categoryDao();

    private static volatile AppDatabase INSTANCE;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "password_manager.db"
                    )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(roomCallback)
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE password_entries ADD COLUMN entry_type INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE password_entries ADD COLUMN is_favorite INTEGER NOT NULL DEFAULT 0");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_password_entries_is_favorite ON password_entries (is_favorite)");
        }
    };

    private static final RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                CategoryDao categoryDao = INSTANCE.categoryDao();
                
                Category social = new Category();
                social.setName("社交");
                social.setIcon("ic_social");
                social.setSortOrder(0);
                categoryDao.insert(social);

                Category bank = new Category();
                bank.setName("银行");
                bank.setIcon("ic_bank");
                bank.setSortOrder(1);
                categoryDao.insert(bank);

                Category email = new Category();
                email.setName("邮箱");
                email.setIcon("ic_email");
                email.setSortOrder(2);
                categoryDao.insert(email);

                Category work = new Category();
                work.setName("工作");
                work.setIcon("ic_work");
                work.setSortOrder(3);
                categoryDao.insert(work);

                Category api = new Category();
                api.setName("API Key");
                api.setIcon("ic_api");
                api.setSortOrder(4);
                categoryDao.insert(api);

                Category other = new Category();
                other.setName("其他");
                other.setIcon("ic_other");
                other.setSortOrder(5);
                categoryDao.insert(other);
            });
        }
    };
}
