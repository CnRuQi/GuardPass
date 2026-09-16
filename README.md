# GuardPass

GuardPass 是一个本地优先的 Android 密码管理器，用于保存账号、密码、API Key、网址和备注。当前版本已从旧的多 Activity 页面重构为单 Activity + Navigation Compose，并使用 Material 3 提供适配手机和大屏的界面。

## 功能

- 密码库：按最近修改时间浏览记录，支持收藏和分类筛选。
- 收藏：独立查看常用记录。
- 搜索：实时搜索标题、账号和网址。
- 记录编辑：普通密码与 API Key 使用统一 secret 字段，敏感内容默认遮罩。
- 分类：创建、编辑、删除分类，并显示分类数量；删除分类不会删除密码记录。
- 应用锁：支持生物识别或设备凭据。
- 备份：导出和恢复本地加密备份文件。
- WebDAV：上传和恢复与本地文件相同格式的加密备份。
- 响应式 UI：紧凑手机使用底部导航，宽屏使用导航栏；页面适配系统安全区和输入法。
- 主题：支持浅色、深色和跟随系统，使用统一的 Material 3 色彩、排版和动效。

`LINEAR_OAUTH_PLAN.md` 中的 Linear OAuth 暂不实现。该文件只作为未来设计资料，不属于当前运行路径。

## 技术栈

| 领域 | 当前实现 |
| --- | --- |
| UI | Kotlin、Jetpack Compose、Material 3 |
| 导航 | 单 `MainActivity`、Navigation Compose |
| 状态 | ViewModel、StateFlow、LiveData |
| 数据 | Room 2.6.1 |
| 加密 | Android Keystore AES-GCM；备份使用 AES-GCM + PBKDF2 |
| 网络 | OkHttp 4.12.0，WebDAV |
| 序列化 | Gson 2.10.1 |
| 生物识别 | AndroidX Biometric |
| Android | `minSdk 24`，`targetSdk 36`，`compileSdk 36` |
| 构建 | AGP 8.7.3、Gradle 9.7.1、Kotlin 2.1.0 |
| Java | Gradle 使用 JDK 21；Android 源码和 Kotlin JVM 目标为 Java 11 |

## 代码结构

```text
app/src/main/java/com/example/myandroid/
├── App.java
├── data/
│   ├── db/             # Room 数据库、DAO、实体
│   ├── model/          # 导入导出模型
│   └── repository/     # 数据访问、加密和备份边界
└── util/               # 加密、生物识别、WebDAV 工具

app/src/main/kotlin/com/example/myandroid/ui/
├── AppNavHost.kt       # 当前路由图
├── AppRoute.kt
├── component/          # 共享 UI 组件
├── vault/              # 密码库和收藏
├── search/             # 搜索
├── entry/              # 记录编辑
├── category/           # 分类管理
├── settings/           # 安全、WebDAV 和数据设置
└── theme/              # ColorScheme、Typography、Shape
```

旧的 Activity 和 Compose 页面源码暂时保留用于迁移参考，但 Manifest 只注册新的 `MainActivity`，当前导航不会再启动旧页面。

## 数据兼容与安全

- 数据库文件名保持为 `password_manager.db`，应用 ID 保持为 `com.example.myandroid`。
- 密码、API Key 和 WebDAV 凭据使用 Android Keystore 保护的 AES-GCM 加密后保存。
- 导出文件使用用户输入的密码通过 PBKDF2 派生密钥，再使用 AES-GCM 加密。
- 新备份格式为 `ExportData.version = 2`，仍可导入没有分类数据的 version 1 备份。
- 恢复前会校验密码、JSON、版本、分类引用和重复 ID；清理与插入在同一个 Room 事务中执行，失败会回滚。
- 旧 API Key 数据会从历史的 username 存储形式迁移到加密 secret。
- 复制敏感内容后，应用会在剪贴板仍由自身持有时自动清理。

## 腾讯构建源

项目已将腾讯镜像放在优先位置，官方源作为回退：

- Google Maven：`https://mirrors.cloud.tencent.com/repository/maven-google/`
- Gradle Plugin：`https://mirrors.cloud.tencent.com/repository/gradle-plugin/`
- Maven 公共仓库：`https://mirrors.cloud.tencent.com/repository/maven-public/`
- Gradle Wrapper：`https://mirrors.cloud.tencent.com/gradle/gradle-9.7.1-bin.zip`

配置位于 `settings.gradle` 和 `gradle/wrapper/gradle-wrapper.properties`。项目不会自动通过 Foojay 下载 JDK，Gradle 应使用 Android Studio 配置的 JDK 或 `JAVA_HOME`。

## Windows 环境配置

建议使用 JDK 21 运行 Gradle。以本机目录为例：

1. 安装 Android SDK Platform 36 和 Build Tools 36.0.0。
2. 在 Android Studio 的 Gradle JDK 设置中选择 `D:\DevTools\Java` 下的 JDK 21，或选择 Android Studio 自带的 JDK。
3. 在项目根目录的 `local.properties` 中配置 SDK。该文件不会提交到 Git：

```properties
sdk.dir=D:/DevTools/Android/Sdk
```

也可以在 PowerShell 中指定环境变量：

```powershell
$env:JAVA_HOME = 'D:\DevTools\Java'
$env:GRADLE_USER_HOME = Join-Path (Get-Location) '.gradle'
$env:ANDROID_USER_HOME = Join-Path (Get-Location) '.android'
```

如果 `D:\DevTools\Java` 是 JDK 集合目录，请将 `JAVA_HOME` 和 Android Studio Gradle JDK 改为其中实际的 JDK 21 根目录。

## 构建、测试和安装

在项目根目录执行：

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:compileDebugAndroidTestKotlin
```

Debug APK 输出在 `app/build/outputs/apk/debug/app-debug.apk`。连接 Android 设备并启用 USB 调试后，可以使用：

```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

需要真实设备或可用模拟器时，再执行仪器测试：

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

## 开发说明

- `app/src/test` 保存 JVM 单元测试，`app/src/androidTest` 保存 Room 和 Compose 仪器测试。
- 构建缓存、SDK 配置、`local.properties`、`.android` 和 `.kotlin` 不属于源码提交内容。
- 当前不包含 Linear OAuth；如未来实现，应先更新设计文档、权限边界和数据安全方案。
