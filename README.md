# 密码管理器 - 项目说明文档

## 一、项目概述

**项目名称**：MyAndroid（密码管理器）

**项目简介**：
MyAndroid 是一款基于 Android 平台的本地密码管理应用，旨在帮助用户安全地存储和管理各类账号密码、API Key 等敏感信息。应用采用现代化的 Android 开发技术栈，遵循 MVVM 架构模式，实现了密码的加密存储、分类管理、搜索查找、数据导出导入以及 WebDAV 云端同步等功能。

**开发目标**：
- 提供安全可靠的密码存储方案
- 实现直观易用的用户界面
- 支持数据备份与恢复
- 支持多设备数据同步

---

## 二、技术架构

### 2.1 技术栈

| 技术领域 | 技术选型 | 说明 |
|---------|---------|------|
| 编程语言 | Java | 主要开发语言 |
| UI 框架 | XML + ViewBinding | 传统 Android UI 开发方式 |
| 架构模式 | MVVM | Model-View-ViewModel |
| 数据库 | Room | Android Jetpack 持久化库 |
| 异步处理 | LiveData + ExecutorService | 响应式数据 + 线程池 |
| 网络请求 | OkHttp | HTTP 客户端 |
| 数据序列化 | Gson | JSON 解析库 |
| 加密算法 | AES-256-GCM + PBKDF2 | 企业级加密标准 |
| 生物识别 | Biometric API | 指纹/面部识别 |

### 2.2 MVVM 架构说明

```
┌─────────────────────────────────────────────────────────────┐
│                        View 层                              │
│  (Activity / XML Layout)                                    │
│  - MainActivity      - 主页，显示密码列表                      │
│  - AddEditActivity   - 添加/编辑密码                          │
│  - CategoryActivity  - 分类管理                              │
│  - SearchActivity    - 搜索功能                              │
│  - SettingsActivity  - 设置页面                              │
└──────────────────────────┬──────────────────────────────────┘
                           │ 观察 LiveData
┌──────────────────────────▼──────────────────────────────────┐
│                      ViewModel 层                           │
│  - MainViewModel      - 主页业务逻辑                          │
│  - AddEditViewModel   - 添加编辑业务逻辑                       │
│  - CategoryViewModel  - 分类管理业务逻辑                       │
│  - SearchViewModel    - 搜索业务逻辑                          │
│  - SettingsViewModel  - 设置业务逻辑                          │
└──────────────────────────┬──────────────────────────────────┘
                           │ 调用 Repository
┌──────────────────────────▼──────────────────────────────────┐
│                       Model 层                              │
│  Repository:                                                │
│  - PasswordRepository  - 密码数据仓库                         │
│  - CategoryRepository  - 分类数据仓库                         │
│                                                              │
│  DAO:                                                       │
│  - PasswordDao         - 密码数据访问对象                      │
│  - CategoryDao         - 分类数据访问对象                      │
│                                                              │
│  Entity:                                                    │
│  - PasswordEntry       - 密码实体类                           │
│  - Category            - 分类实体类                           │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                    Room Database                            │
│                   password_manager.db                        │
└─────────────────────────────────────────────────────────────┘
```

---

## 三、功能模块详解

### 3.1 密码记录模块

**核心功能**：
- **增删改查**：完整管理密码条目的生命周期
- **密码显示/隐藏**：保护敏感信息不被旁人窥视
- **一键复制**：快速复制用户名、密码到剪贴板
- **收藏功能**：标记常用密码，快速访问
- **密码类型**：支持普通密码和 API Key 两种类型

**数据字段**：
```java
public class PasswordEntry {
    private long id;              // 主键，自增
    private Long categoryId;      // 外键，关联分类
    private String title;         // 标题（如：淘宝、GitHub）
    private String username;      // 用户名
    private String password;      // 密码（AES加密存储）
    private String url;           // 网址
    private String notes;         // 备注
    private int entryType;        // 类型：0=密码，1=API Key
    private boolean isFavorite;   // 是否收藏
    private long createdAt;       // 创建时间
    private long updatedAt;       // 更新时间
}
```

### 3.2 分类管理模块

**核心功能**：
- **自定义分类**：用户可创建、编辑、删除分类
- **分类图标**：每个分类可设置专属图标
- **排序功能**：支持自定义分类排序顺序
- **按分类筛选**：主页可按分类过滤密码列表

**预置分类**：
| 分类名称 | 图标标识 | 排序 |
|---------|---------|------|
| 社交 | ic_social | 0 |
| 银行 | ic_bank | 1 |
| 邮箱 | ic_email | 2 |
| 工作 | ic_work | 3 |
| API Key | ic_api | 4 |
| 其他 | ic_other | 5 |

### 3.3 搜索模块

**核心功能**：
- **模糊搜索**：按标题、用户名进行模糊匹配
- **实时搜索**：输入即时显示搜索结果
- **搜索结果高亮**：方便用户快速定位

**搜索 SQL**：
```sql
SELECT * FROM password_entries 
WHERE title LIKE '%' || :query || '%' 
   OR username LIKE '%' || :query || '%' 
ORDER BY updated_at DESC
```

### 3.4 数据同步模块

#### 3.4.1 导出功能
- 将所有密码数据导出为加密的 JSON 文件
- 使用用户设置的主密码进行 AES-256-GCM 加密
- 导出文件格式：`.json`（Base64 编码的加密数据）

#### 3.4.2 导入功能
- 从加密的 JSON 文件恢复密码数据
- 验证主密码后解密并导入
- 支持覆盖或合并导入策略

#### 3.4.3 WebDAV 同步
- 支持配置 WebDAV 服务器地址
- 支持上传本地数据到云端
- 支持从云端下载数据到本地
- 自动处理冲突合并

---

## 四、项目结构

```
com.example.myandroid/
├── App.java                          # Application 类，初始化数据库
│
├── data/                             # 数据层
│   ├── db/                           # 数据库相关
│   │   ├── AppDatabase.java          # Room 数据库定义
│   │   ├── dao/                      # 数据访问对象
│   │   │   ├── PasswordDao.java      # 密码 DAO
│   │   │   └── CategoryDao.java      # 分类 DAO
│   │   └── entity/                   # 实体类
│   │       ├── PasswordEntry.java    # 密码实体
│   │       └── Category.java         # 分类实体
│   ├── model/                        # 数据模型
│   │   └── ExportData.java           # 导出数据模型
│   └── repository/                   # 数据仓库
│       ├── PasswordRepository.java   # 密码仓库
│       └── CategoryRepository.java   # 分类仓库
│
├── ui/                               # 界面层
│   ├── main/                         # 主页模块
│   │   ├── MainActivity.java         # 主 Activity
│   │   ├── MainViewModel.java        # 主页 ViewModel
│   │   └── PasswordAdapter.java      # 密码列表适配器
│   ├── add/                          # 添加/编辑模块
│   │   ├── AddEditActivity.java      # 添加编辑 Activity
│   │   └── AddEditViewModel.java     # 添加编辑 ViewModel
│   ├── category/                     # 分类管理模块
│   │   ├── CategoryActivity.java     # 分类管理 Activity
│   │   ├── CategoryViewModel.java    # 分类 ViewModel
│   │   └── CategoryAdapter.java      # 分类列表适配器
│   ├── search/                       # 搜索模块
│   │   ├── SearchActivity.java       # 搜索 Activity
│   │   └── SearchViewModel.java      # 搜索 ViewModel
│   └── settings/                     # 设置模块
│       └── SettingsActivity.java     # 设置 Activity
│
└── util/                             # 工具类
    ├── CryptoUtils.java              # AES 加密工具
    ├── StorageCrypto.java            # 本地存储加密
    ├── WebDavUtils.java              # WebDAV 工具
    ├── BiometricHelper.java          # 生物识别工具
    ├── PasswordStrengthUtil.java     # 密码强度检测
    ├── CategoryIconHelper.java       # 分类图标工具
    └── SingleLiveEvent.java          # 单次事件 LiveData
```

---

## 五、核心类说明

### 5.1 App.java - Application 类

**职责**：
- 应用全局初始化
- 持有数据库单例实例
- 提供全局访问点

```java
public class App extends Application {
    private AppDatabase database;
    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        database = AppDatabase.getInstance(this);
    }

    public static App getInstance() { return instance; }
    public AppDatabase getDatabase() { return database; }
}
```

### 5.2 AppDatabase.java - Room 数据库

**职责**：
- 定义数据库配置
- 提供 DAO 访问方法
- 管理数据库迁移
- 初始化预置数据

**关键特性**：
- 单例模式确保唯一实例
- 使用线程池处理数据库操作
- 自动插入预置分类数据
- 支持数据库版本迁移

### 5.3 Repository 模式

**PasswordRepository 职责**：
- 封装数据访问逻辑
- 处理密码的加解密转换
- 提供 LiveData 供 ViewModel 观察
- 异步执行数据库操作

**核心代码示例**：
```java
public LiveData<List<PasswordEntry>> getAllPasswords() {
    return Transformations.map(passwordDao.getAllPasswords(), entries -> {
        if (entries != null) {
            for (PasswordEntry e : entries) {
                e.setPassword(StorageCrypto.decrypt(e.getPassword()));
            }
        }
        return entries;
    });
}

public void insert(PasswordEntry entry) {
    entry.setPassword(StorageCrypto.encrypt(entry.getPassword()));
    AppDatabase.databaseWriteExecutor.execute(() -> passwordDao.insert(entry));
}
```

### 5.4 ViewModel 模式

**职责**：
- 持有 UI 数据
- 处理业务逻辑
- 响应用户操作
- 管理数据生命周期

**示例 - MainViewModel**：
```java
public class MainViewModel extends AndroidViewModel {
    private final PasswordRepository passwordRepository;
    private final CategoryRepository categoryRepository;
    private final LiveData<List<PasswordEntry>> allPasswords;
    private final LiveData<List<Category>> allCategories;
    
    // 筛选状态
    private MutableLiveData<Long> selectedCategoryId = new MutableLiveData<>(null);
    private MutableLiveData<List<PasswordEntry>> filteredPasswords = new MutableLiveData<>();
    
    // 业务方法
    public void delete(PasswordEntry entry) { passwordRepository.delete(entry); }
    public void toggleFavorite(PasswordEntry entry) { ... }
    public void applyFilters() { ... }
}
```

---

## 六、数据库设计

### 6.1 数据库表结构

#### password_entries 表（密码表）

| 字段名 | 类型 | 约束 | 说明 |
|-------|------|------|------|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT | 主键 |
| category_id | INTEGER | FOREIGN KEY, INDEX | 分类外键 |
| title | TEXT | NOT NULL | 标题 |
| username | TEXT | | 用户名 |
| password | TEXT | NOT NULL | 密码（加密存储） |
| url | TEXT | | 网址 |
| notes | TEXT | | 备注 |
| entry_type | INTEGER | NOT NULL DEFAULT 0 | 类型：0=密码，1=API Key |
| is_favorite | INTEGER | NOT NULL DEFAULT 0, INDEX | 是否收藏 |
| created_at | INTEGER | NOT NULL | 创建时间戳 |
| updated_at | INTEGER | NOT NULL, INDEX | 更新时间戳 |

#### categories 表（分类表）

| 字段名 | 类型 | 约束 | 说明 |
|-------|------|------|------|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT | 主键 |
| name | TEXT | NOT NULL | 分类名称 |
| icon | TEXT | | 图标标识 |
| sort_order | INTEGER | NOT NULL DEFAULT 0 | 排序顺序 |
| created_at | INTEGER | NOT NULL | 创建时间戳 |

### 6.2 表关系

```
categories (1) ──────── (N) password_entries
    │                           │
    └── id (PK)                 ├── id (PK)
                                └── category_id (FK) ──→ categories.id
                                    ON DELETE SET NULL
```

### 6.3 数据库迁移

**版本 1 → 2 迁移**：
```java
// 新增字段
ALTER TABLE password_entries ADD COLUMN entry_type INTEGER NOT NULL DEFAULT 0;
ALTER TABLE password_entries ADD COLUMN is_favorite INTEGER NOT NULL DEFAULT 0;

// 新增索引
CREATE INDEX IF NOT EXISTS index_password_entries_is_favorite 
ON password_entries (is_favorite);
```

---

## 七、安全机制

### 7.1 密码加密存储

**加密算法**：AES-256-GCM

**加密流程**：
1. 生成随机 Salt（16 字节）
2. 生成随机 IV（12 字节）
3. 使用 PBKDF2WithHmacSHA256 派生密钥
4. 使用 AES-256-GCM 加密明文
5. 拼接：Salt + IV + 密文
6. Base64 编码存储

**加密参数**：
```java
private static final String ALGORITHM = "AES/GCM/NoPadding";
private static final int GCM_TAG_LENGTH = 128;      // 认证标签长度
private static final int GCM_IV_LENGTH = 12;         // IV 长度
private static final int SALT_LENGTH = 16;           // Salt 长度
private static final int PBKDF2_ITERATIONS = 100000; // 迭代次数
private static final int KEY_LENGTH = 256;           // 密钥长度
```

### 7.2 数据导出加密

**导出加密流程**：
1. 收集所有密码数据
2. 序列化为 JSON 字符串
3. 使用用户设置的主密码进行 AES-256-GCM 加密
4. 生成加密文件供用户保存

### 7.3 生物识别保护

**支持的生物识别方式**：
- 指纹识别
- 面部识别（设备支持时）

**实现方式**：
- 使用 AndroidX Biometric API
- 支持设备凭据回退（PIN/图案/密码）

---

## 八、UI 设计

### 8.1 主页面

**布局结构**：
- 顶部工具栏：应用标题、搜索按钮、设置按钮
- 分类筛选栏：横向滚动的分类标签
- 密码列表：RecyclerView 展示密码卡片
- 底部悬浮按钮：添加新密码

**密码卡片信息**：
- 分类图标
- 标题
- 用户名
- 密码（可切换显示/隐藏）
- 收藏状态
- 复制按钮

### 8.2 添加/编辑页面

**表单字段**：
- 标题（必填）
- 用户名
- 密码（必填，带强度指示器）
- 网址
- 分类选择（Spinner）
- 备注
- 密码类型切换

### 8.3 分类管理页面

**功能**：
- 分类列表展示
- 添加新分类（弹窗）
- 编辑分类（弹窗）
- 删除分类（确认对话框）
- 显示每个分类下的密码数量

### 8.4 搜索页面

**功能**：
- 搜索输入框（自动获取焦点）
- 实时搜索结果列表
- 点击结果跳转编辑

---

## 九、开发流程

### 9.1 开发阶段

| 阶段 | 内容 | 状态 |
|------|------|------|
| 阶段 1 | 基础架构搭建（依赖、Entity、DAO、Database、Repository） | ✅ 完成 |
| 阶段 2 | 主页 + 密码列表（布局、ViewModel、Adapter、筛选） | ✅ 完成 |
| 阶段 3 | 添加/编辑功能（表单、ViewModel、Activity） | ✅ 完成 |
| 阶段 4 | 分类管理（CRUD、弹窗、数量统计） | ✅ 完成 |
| 阶段 5 | 搜索功能（模糊搜索、实时结果） | ✅ 完成 |
| 阶段 6 | 数据加密 + 导出导入（CryptoUtils、文件操作） | ✅ 完成 |
| 阶段 7 | WebDAV 同步（配置、上传、下载） | ✅ 完成 |
| 阶段 8 | 优化完善（Bug修复、代码重构、主题统一） | ✅ 完成 |

### 9.2 依赖配置

```groovy
dependencies {
    // AndroidX Core
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    
    // Lifecycle (ViewModel + LiveData)
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0'
    implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.7.0'
    
    // Room Database
    implementation 'androidx.room:room-runtime:2.6.1'
    annotationProcessor 'androidx.room:room-compiler:2.6.1'
    
    // OkHttp (WebDAV)
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    
    // Gson (JSON)
    implementation 'com.google.code.gson:gson:2.10.1'
    
    // Biometric
    implementation 'androidx.biometric:biometric:1.1.0'
}
```

---

## 十、运行环境

### 10.1 系统要求

| 项目 | 要求 |
|------|------|
| 最低 Android 版本 | Android 7.0 (API 24) |
| 目标 Android 版本 | Android 14 (API 36) |
| 编译 SDK 版本 | API 35 |
| Java 版本 | Java 11 |

### 10.2 权限声明

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

- **INTERNET**：用于 WebDAV 同步功能
- **ACCESS_NETWORK_STATE**：检测网络连接状态

---

## 十一、项目亮点

1. **安全性高**：采用 AES-256-GCM 加密，PBKDF2 密钥派生，10万次迭代
2. **架构清晰**：严格遵循 MVVM 架构，代码职责分明
3. **数据响应式**：使用 LiveData 实现数据自动更新 UI
4. **异步处理**：使用线程池处理数据库操作，避免主线程阻塞
5. **用户体验**：支持生物识别、一键复制、密码强度检测
6. **数据可靠**：支持导出导入、WebDAV 多设备同步
7. **代码规范**：完整的注释、清晰的命名、合理的包结构

---

## 十二、总结

MyAndroid 密码管理器是一个功能完整、架构规范的 Android 应用项目。通过本项目的开发，全面实践了 Android 应用开发的核心技术，包括：

- **MVVM 架构**的设计与实现
- **Room 数据库**的使用与迁移
- **LiveData** 响应式编程
- **加密算法**的应用
- **网络请求**与数据同步
- **Material Design** UI 组件的使用

该项目可作为 Android 开发学习的综合实践案例，展示了从需求分析、架构设计到功能实现的完整开发流程。

---

*文档生成日期：2026年6月8日*
