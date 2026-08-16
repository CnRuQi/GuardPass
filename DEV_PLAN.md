# 密码管理器开发计划

## 项目概述

**项目名称**：MyAndroid（密码管理器）  
**技术栈**：Java + XML + Room + MVVM  
**最低支持**：Android 7.0 (API 24)  
**目标版本**：Android 14 (API 36)

---

## 功能模块

| 模块 | 功能 |
|------|------|
| 密码记录 | 增删改查、密码显示/隐藏、一键复制 |
| 分类管理 | 自定义分类 CRUD、按分类筛选 |
| 搜索 | 按主题、账号模糊搜索 |
| 数据同步 | 导出加密文件、导入恢复、WebDAV 同步 |

---

## 项目结构

```
com.example.myandroid/
├── App.java
├── data/
│   ├── db/
│   │   ├── AppDatabase.java
│   │   ├── dao/ (PasswordDao, CategoryDao)
│   │   └── entity/ (PasswordEntry, Category)
│   ├── model/
│   │   └── ExportData.java
│   └── repository/ (PasswordRepository, CategoryRepository)
├── ui/
│   ├── main/ (MainActivity, MainViewModel, PasswordAdapter)
│   ├── add/ (AddEditActivity, AddEditViewModel)
│   ├── category/ (CategoryActivity, CategoryViewModel, CategoryAdapter)
│   ├── search/ (SearchActivity, SearchViewModel)
│   └── settings/ (SettingsActivity)
└── util/
    ├── CryptoUtils.java
    └── WebDavUtils.java
```

---

## 开发步骤

### 阶段 1：基础架构搭建 ✅

| 步骤 | 任务 | 状态 |
|------|------|------|
| 1.1 | 添加依赖 | ✅ |
| 1.2 | 创建 Application 类 | ✅ |
| 1.3 | 创建 Entity | ✅ |
| 1.4 | 创建 DAO | ✅ |
| 1.5 | 创建 Database | ✅ |
| 1.6 | 创建 Repository | ✅ |

### 阶段 2：主页 + 密码列表 ✅

| 步骤 | 任务 | 状态 |
|------|------|------|
| 2.1 | 设计主页布局 | ✅ |
| 2.2 | 创建 MainViewModel | ✅ |
| 2.3 | 创建 PasswordAdapter | ✅ |
| 2.4 | 实现密码列表 | ✅ |
| 2.5 | 实现分类筛选 | ✅ |
| 2.6 | 实现密码显示/隐藏 | ✅ |
| 2.7 | 实现一键复制 | ✅ |

### 阶段 3：添加/编辑功能 ✅

| 步骤 | 任务 | 状态 |
|------|------|------|
| 3.1 | 添加字符串资源 | ✅ |
| 3.2 | 创建表单布局 | ✅ |
| 3.3 | 创建 Spinner 项布局 | ✅ |
| 3.4 | 创建菜单资源 | ✅ |
| 3.5 | 创建 AddEditViewModel | ✅ |
| 3.6 | 实现 AddEditActivity | ✅ |

### 阶段 4：分类管理 ✅

| 步骤 | 任务 | 状态 |
|------|------|------|
| 4.1 | 添加字符串资源 | ✅ |
| 4.2 | 创建分类管理页布局 | ✅ |
| 4.3 | 创建分类列表项布局 | ✅ |
| 4.4 | 创建分类弹窗布局 | ✅ |
| 4.5 | 创建 CategoryViewModel | ✅ |
| 4.6 | 创建 CategoryAdapter | ✅ |
| 4.7 | 实现 CategoryActivity | ✅ |
| 4.8 | 在 MainActivity 添加入口 | ✅ |
| 4.9 | 注册 Activity | ✅ |

### 阶段 5：搜索功能 ✅

| 步骤 | 任务 | 状态 |
|------|------|------|
| 5.1 | 添加字符串资源 | ✅ |
| 5.2 | 创建搜索页布局 | ✅ |
| 5.3 | 创建 SearchViewModel | ✅ |
| 5.4 | 实现 SearchActivity | ✅ |
| 5.5 | 在 MainActivity 添加入口 | ✅ |
| 5.6 | 注册 Activity | ✅ |

### 阶段 6：数据加密 + 导出导入 ✅

| 步骤 | 任务 | 状态 |
|------|------|------|
| 6.1 | 实现 CryptoUtils | ✅ |
| 6.2 | 实现导出功能 | ✅ |
| 6.3 | 实现导入功能 | ✅ |

### 阶段 7：WebDAV 同步 ✅

| 步骤 | 任务 | 状态 |
|------|------|------|
| 7.1 | 实现 WebDavUtils | ✅ |
| 7.2 | 实现配置界面 | ✅ |
| 7.3 | 实现同步逻辑 | ✅ |

### 阶段 8：优化完善 ✅

| 步骤 | 任务 | 状态 |
|------|------|------|
| 8.1 | 修复夜间主题（统一 NoActionBar + 同色系） | ✅ |
| 8.2 | 修复 CategoryAdapter 分类数量显示 | ✅ |
| 8.3 | 修复 MainViewModel applyFilters getValue() 问题 | ✅ |
| 8.4 | 修复 AddEditActivity 加载密码的竞态条件 | ✅ |
| 8.5 | 修复 exportImportPassword 屏幕旋转丢失 | ✅ |
| 8.6 | 提取硬编码字符串到 strings.xml | ✅ |
| 8.7 | 移除 activity_main.xml 中的冗余 chip_all | ✅ |
| 8.8 | WebDavUtils 改为单例复用 OkHttpClient | ✅ |
| 8.9 | 为 SettingsActivity 创建 SettingsViewModel | ✅ |


---

## 预计开发时间

| 阶段 | 预计时间 | 状态 |
|------|----------|------|
| 阶段 1：基础架构 | 1-2 小时 | ✅ |
| 阶段 2：主页列表 | 2-3 小时 | ✅ |
| 阶段 3：添加编辑 | 2-3 小时 | ✅ |
| 阶段 4：分类管理 | 1-2 小时 | ✅ |
| 阶段 5：搜索功能 | 1 小时 | ✅ |
| 阶段 6：加密导出 | 2-3 小时 | ✅ |
| 阶段 7：WebDAV | 3-4 小时 | ✅ |
| 阶段 8：优化完善 | 2-3 小时 | ✅ |
| **总计** | **约 15-20 小时** | |
