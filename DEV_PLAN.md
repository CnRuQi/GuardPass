# GuardPass 开发计划

## 当前方向

GuardPass 是一个本地优先的 Android 密码管理器。当前重构目标是把旧的多 Activity、玻璃拟态界面收敛为单 Activity + Navigation Compose 的安全工作台，同时保持已有 Room 数据库兼容。

本次范围包含：

- 腾讯 Maven 镜像优先，Google/Maven Central/Gradle 官方仓库作为回退。
- Material 3、浅色/深色主题、手机底部导航和大屏导航栏。
- 密码库、收藏、搜索、记录编辑、分类、设置和应用锁。
- 普通密码与 API Key 都写入加密的 password 字段。
- ExportData.version = 2 加密备份，恢复分类与密码使用同一个 Room 事务。
- WebDAV 与本地文件共享备份格式。

明确不在本次范围内：

- LINEAR_OAUTH_PLAN.md 中的 Linear OAuth。文档保留作未来方案，不注册 OAuth Activity，也不添加 OAuth 网络流程。

## 当前架构

MainActivity (FragmentActivity)
└── AppNavHost
    ├── VaultScreen
    ├── Favorites -> VaultScreen(filter=favorites)
    ├── SearchScreenV2
    ├── EntryEditorScreen
    ├── CategoryScreenV2
    └── SettingsScreenV2

Compose UI -> ViewModel StateFlow/LiveData -> Repository -> Room DAO
                                           └── BackupRepository -> CryptoUtils/WebDAV

旧的页面源码暂时保留，便于迁移期间参考和兼容编译；它们已经从 AndroidManifest.xml 中移除，不属于当前启动和导航路径。

## 数据与安全里程碑

- [x] 保留 password_manager.db、Room 表名和核心字段。
- [x] 分类外键使用 ON DELETE SET NULL，删除分类不会删除密码。
- [x] API Key 旧数据从明文 username 一次性迁移到加密 secret。
- [x] Android Keystore AES-GCM 存储密码、API Key 和 WebDAV 凭据。
- [x] 备份恢复预检格式、版本、分类引用和重复 ID。
- [x] 清空和插入操作放在同一个事务，失败自动回滚。
- [x] 兼容 version = 1 无分类备份；新导出固定为 version = 2。
- [x] 剪贴板复制后在应用仍持有剪贴板时自动清理。

## UI 里程碑

- [x] 中性背景、墨色文字、低饱和青绿色主色、琥珀提示和红色危险色。
- [x] 小圆角、明确边界、克制的非弹性动效和系统字体。
- [x] 搜索覆盖标题、账号和网址。
- [x] 密钥默认遮罩，按条目独立显示/复制。
- [x] 编辑页统一 secret 字段，API Key 不再占用 username。
- [x] 分类数量响应式刷新、重复名称校验和删除确认。
- [x] 生物识别或设备凭据应用锁。

## 验证清单

PowerShell:

$env:GRADLE_USER_HOME = (Join-Path (Get-Location) '.gradle')
$env:ANDROID_USER_HOME = (Join-Path (Get-Location) '.android')
./gradlew.bat :app:dependencies --configuration debugCompileClasspath
./gradlew.bat test
./gradlew.bat :app:assembleDebug
./gradlew.bat :app:connectedDebugAndroidTest

如果没有 Android SDK 或设备，至少执行依赖解析、静态检查，并在交付记录中说明未执行的编译/仪器测试。
