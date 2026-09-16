package com.example.myandroid;

import android.app.Application;

import com.example.myandroid.data.db.AppDatabase;
import com.example.myandroid.data.repository.PasswordRepository;

public class App extends Application {

    private AppDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        database = AppDatabase.getInstance(this);
        new PasswordRepository(this).migrateLegacyApiKeys();
    }

    public AppDatabase getDatabase() {
        return database;
    }

    private static App instance;

    public static App getInstance() {
        return instance;
    }
}
