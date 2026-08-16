package com.example.myandroid.data.model;

import com.example.myandroid.data.db.entity.Category;
import com.example.myandroid.data.db.entity.PasswordEntry;

import java.util.List;

public class ExportData {
    public int version;
    public long timestamp;
    public List<Category> categories;
    public List<PasswordEntry> passwords;
}
