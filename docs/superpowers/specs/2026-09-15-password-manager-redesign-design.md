# GuardPass 全量重构设计

**日期：** 2026-09-15

**范围：** 构建源配置、应用架构、数据安全、同步恢复和全部用户界面。Linear OAuth 计划不属于本次范围。

## 目标

将当前以多 Activity、LiveData 和玻璃拟态界面为主的密码管理器，重构为一个稳定、清晰、可维护的现代 Android 应用，同时保留现有用户数据和核心能力。

本次交付必须包含：

- 腾讯 Maven 镜像优先配置，并验证插件和依赖可解析。
- 单 Activity + Navigation Compose 的统一导航结构。
- Material 3 设计系统，支持浅色和深色模式。
- 密码、API Key、分类、搜索、收藏、导入导出和 WebDAV 的完整可用流程。
- 生物识别应用锁真正参与访问控制。
- 导入和 WebDAV 恢复的事务性数据替换，并同时恢复分类。
- 分类删除只解除关联，不删除密码记录。
- 针对加密、数据替换和主要页面交互的自动化测试。

## 非目标

- 不实现 `LINEAR_OAUTH_PLAN.md` 中的 Linear OAuth。
- 不引入账号系统、远程数据库或云端明文数据。
- 不改变应用的包名、数据库文件名和已有 Room 表名。
- 不为视觉效果引入大体积图片、网络字体或新的 UI 框架。

## 架构

### 应用入口与导航

`MainActivity` 改为唯一的应用入口，使用 `FragmentActivity` 以支持 `BiometricPrompt`，在 Compose 中承载 `NavHost`。页面路由包括：

- `vault`：密码库主页
- `search`：全局搜索
- `entry/new`：新建记录
- `entry/{id}`：编辑记录
- `categories`：分类管理
- `settings`：设置、安全和同步

旧的 Activity 文件在第一阶段保留以避免无关删除；新 Manifest 不再把它们作为业务入口。确认新导航和构建稳定后，再单独评估清理死代码。

### 状态管理

页面采用单向数据流：

```text
用户操作 -> UiEvent -> ViewModel -> Repository -> Room/WebDAV
                                      |
                                      v
                                  StateFlow<UiState>
```

每个页面的 ViewModel 只负责页面状态和用例编排，数据库、加密和网络细节不进入 Composable。一次性提示使用显式的 UI effect 流，避免重复消费和旋转屏幕重放。

现有 Repository 可在迁移期间继续作为兼容边界，但新的页面不直接访问 DAO 或 `AppDatabase`。

## 数据与安全

### 数据库兼容

保留数据库文件 `password_manager.db`、表名和现有列名，以便已有安装直接升级。Room 版本不做无必要的破坏性变更。

`PasswordEntry.password` 作为统一的加密 secret 字段：普通密码和 API Key 都写入该字段；API Key 不再放入明文 `username`。旧数据启动时执行一次性语义迁移：对于 `entry_type = API_KEY`、密码为空且用户名非空的旧记录，将旧用户名移入 secret 并清空用户名，随后通过 Android Keystore 加密保存。

分类删除依赖外键 `ON DELETE SET NULL`，不再主动删除分类下的密码记录。分类计数由密码数据变化驱动刷新。

### 加密边界

- 本地 secret：Android Keystore 中的 AES-GCM 密钥。
- 导出文件：随机 salt + IV、PBKDF2-HMAC-SHA256、AES-256-GCM。
- WebDAV 配置：服务器地址可明文保存，用户名和密码使用本地 Keystore 加密后保存。
- 剪贴板复制：保留系统复制能力，并提供短时自动清除策略；复制成功采用轻量状态提示。

任何解密失败都必须返回明确的错误状态，不得静默把错误当成空密码继续保存。

### 备份和恢复

新增独立的备份用例/Repository，负责序列化、加密、解密和数据库事务。导出格式升级为 `version = 2`，包含分类和密码记录；导入时兼容现有 `version = 1`。

恢复流程必须在一个 Room transaction 中完成：校验格式和密码 -> 清空旧密码与分类 -> 插入导入分类 -> 插入导入密码 -> 提交。任何一步失败都回滚，不向 UI 报告成功。

WebDAV 上传和本地文件导出共享同一备份格式，避免出现两套同步协议。

## 视觉和交互系统

### 设计方向

产品定位为“安静的安全工作台”：信息密度适中、对比度明确、操作反馈克制。移除当前大面积紫色渐变、毛玻璃卡片和无意义的弹性动画。

颜色采用中性背景、墨色文字、青绿色主色，危险操作使用红色，提示使用琥珀色。颜色统一收敛到 `Color.kt` 和 Material 3 `ColorScheme`，组件不得自行写硬编码颜色。

使用 Android 系统字体和 Material 3 Typography，沿用 4dp 间距基准。卡片使用小圆角和清晰边界，避免卡片嵌套卡片。

### 导航和页面

- 手机：底部导航显示密码库、收藏、分类、设置；搜索作为主页顶部的主入口。
- 大屏：自适应为导航栏/导航轨，内容区保持有限宽度并适合扫描。
- 密码库：搜索入口、总记录数、筛选 chips、最近修改列表、明确的新增操作。
- 记录编辑：按“类型、基本信息、secret、附加信息”分组；保存按钮固定在可触达区域；API Key 显示统一的 secret 字段。
- 搜索：输入即搜，覆盖标题、用户名和网址；结果可直接编辑或复制。
- 分类：显示数量、添加/编辑、删除后明确提示“记录将变为未分类”。
- 设置：按“安全、同步、数据”分组；连接、上传、恢复都有加载和失败状态。

### 交互规则

- 保存、收藏、分类变更使用乐观 UI 更新，失败时恢复状态并显示可操作错误。
- 破坏性操作保留确认，但恢复数据前展示覆盖范围和记录数量。
- 所有按钮、输入框、菜单、对话框提供默认、按下、焦点、禁用、加载、错误和成功状态。
- 动画只用于导航、按压和状态变化；支持 `prefers-reduced-motion` 对应的 Compose 设置。
- 文案使用用户能理解的对象名称，例如“恢复备份”“未分类”，不暴露 Repository、同步任务等实现术语。

## 构建源

`settings.gradle` 中移除 Aliyun 仓库，将腾讯镜像放在仓库列表首位，分别覆盖 Google、Gradle Plugin 和 Maven Public 仓库；官方仓库作为回退，避免腾讯镜像缺少某个 Android 工件时无法构建。

Gradle Wrapper 的发行版地址只有在确认腾讯镜像存在完全匹配的 `gradle-9.4.1-bin.zip` 后才切换；不使用未经验证的 URL 破坏首次构建。所有镜像切换必须通过 `dependencies` 或 `assembleDebug` 实际验证。

## 测试与验收

- JVM 单元测试：CryptoUtils、StorageCrypto 的成功/失败路径，密码强度，备份格式兼容。
- Repository/Room 测试：分类删除置空、事务恢复、API Key 旧数据迁移、恢复失败回滚。
- Compose 测试：主页导航、搜索输入、添加编辑校验、分类删除提示、设置同步状态。
- 构建验收：`test`、`connectedDebugAndroidTest`（存在设备时）和 `assembleDebug`。
- 手动验收：浅色/深色、小屏/大屏、旋转屏幕、无生物识别设备、WebDAV 失败和错误密码恢复。

## 预期文件边界

主要修改范围：

- `settings.gradle`
- `gradle/wrapper/gradle-wrapper.properties`（仅在镜像验证通过时）
- `app/build.gradle` 与 `gradle/libs.versions.toml`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/kotlin/com/example/myandroid/ui/**`
- `app/src/main/java/com/example/myandroid/data/**`
- `app/src/main/java/com/example/myandroid/util/**`
- `app/src/main/res/values/**`
- `app/src/test/**` 与 `app/src/androidTest/**`

不修改或实现：`LINEAR_OAUTH_PLAN.md`。
