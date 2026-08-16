package com.example.myandroid;

import android.app.Application;

import com.example.myandroid.data.db.AppDatabase;

public class App extends Application {

    private AppDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        database = AppDatabase.getInstance(this);
    }

    public AppDatabase getDatabase() {
        return database;
    }

    private static App instance;

    public static App getInstance() {
        return instance;
    }
}
