# GuardPass 全量重构 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (\`- [ ]\`) syntax for tracking.

**Goal:** 将 GuardPass 重构为使用腾讯依赖镜像、单 Activity 导航、Material 3 设计系统和事务性备份恢复的现代密码管理器，同时兼容已有本地数据。

**Architecture:** 保留现有 Room 数据库文件、表名和核心列，通过 Repository/BackupRepository 统一数据边界；使用单 Activity + Navigation Compose 承载所有新页面；页面 ViewModel 输出 StateFlow<UiState>，事件通过明确的 UiEvent 进入用例。旧页面文件暂时保留，但 Manifest 和新导航不再使用它们。

**Tech Stack:** Android Gradle Plugin 8.7.3、Kotlin 2.1.0、Jetpack Compose Material 3、Navigation Compose、Room 2.6.1、Coroutines、OkHttp 4.12.0、Gson 2.10.1、Biometric 1.1.0、JUnit、Compose UI Test。

**Spec:** docs/superpowers/specs/2026-09-15-password-manager-redesign-design.md

## Global Constraints

- 保持 applicationId = "com.example.myandroid"、数据库文件名 password_manager.db、Room 表名和现有字段名。
- 不实现 LINEAR_OAUTH_PLAN.md 中的 Linear OAuth。
- 腾讯镜像优先，官方仓库仅作为回退；删除所有 Aliyun 仓库地址。
- 不在 UI 中写死颜色；颜色统一来自 Color.kt 和 Material 3 ColorScheme。
- 普通密码和 API Key 都必须写入加密的 PasswordEntry.password 字段。
- 恢复必须在一个 Room transaction 中完成，失败时不报告成功。
- 不删除现有生产文件；旧 Activity 仅在新导航稳定后再单独评估清理。

---

### Task 1: 切换腾讯镜像并建立可重复构建基线

**Files:**
- Modify: settings.gradle
- Modify: gradle/wrapper/gradle-wrapper.properties only after Tencent distribution URL is verified
- Modify: gradle/libs.versions.toml
- Modify: app/build.gradle
- Test: Gradle dependency resolution and :app:assembleDebug

**Interfaces:**
- Produces: a build that resolves Android, Google, Gradle Plugin, Kotlin, Compose, Room, OkHttp, Gson and Biometric dependencies with Tencent repositories first.

- [ ] **Step 1: Check current repository and wrapper state**

Run:

~~~powershell
rg -n "aliyun|mirrors\.cloud\.tencent|distributionUrl|repositories" settings.gradle gradle/wrapper/gradle-wrapper.properties gradle/libs.versions.toml app/build.gradle
~~~

Expected: Aliyun URLs appear only in settings.gradle; the wrapper points to gradle-9.4.1-bin.zip.

- [ ] **Step 2: Verify Tencent endpoints before editing**

Probe these URLs with Invoke-WebRequest -Method Head and record which return a successful response:

~~~text
https://mirrors.cloud.tencent.com/repository/maven-google/
https://mirrors.cloud.tencent.com/repository/gradle-plugin/
https://mirrors.cloud.tencent.com/repository/maven-public/
https://mirrors.cloud.tencent.com/gradle/gradle-9.4.1-bin.zip
~~~

Use the first three in Gradle only when they are reachable. Change the wrapper distribution URL only when the fourth URL returns the matching distribution; otherwise keep services.gradle.org so bootstrap remains valid.

- [ ] **Step 3: Replace Aliyun repository definitions**

In settings.gradle, use Tencent mirrors first and retain official repositories as fallback:

~~~groovy
pluginManagement {
    repositories {
        maven { url 'https://mirrors.cloud.tencent.com/repository/maven-google/' }
        maven { url 'https://mirrors.cloud.tencent.com/repository/gradle-plugin/' }
        maven { url 'https://mirrors.cloud.tencent.com/repository/maven-public/' }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url 'https://mirrors.cloud.tencent.com/repository/maven-google/' }
        maven { url 'https://mirrors.cloud.tencent.com/repository/maven-public/' }
        google()
        mavenCentral()
    }
}
~~~

Do not leave an Aliyun repository in either block.

- [ ] **Step 4: Add explicit libraries needed by the new architecture**

Add versions and aliases for Fragment KTX and Compose UI tests in gradle/libs.versions.toml, then reference them from app/build.gradle. Keep existing versions unless dependency resolution requires a compatible patch version.

~~~toml
fragment = "1.8.5"

fragment-ktx = { group = "androidx.fragment", name = "fragment-ktx", version.ref = "fragment" }
compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
~~~

Add implementation libs.fragment.ktx, androidTestImplementation libs.compose.ui.test.junit4, and debugImplementation libs.compose.ui.test.manifest.

- [ ] **Step 5: Resolve dependencies and build the baseline**

Run with a workspace-local Gradle cache:

~~~powershell
$env:GRADLE_USER_HOME = (Join-Path (Get-Location) '.gradle')
./gradlew.bat :app:dependencies
./gradlew.bat :app:assembleDebug
~~~

Expected: no Aliyun URL is contacted in the Gradle output, dependency resolution completes, and app/build/outputs/apk/debug/app-debug.apk is produced.

- [ ] **Step 6: Commit the build-source change**

~~~powershell
git add settings.gradle gradle/wrapper/gradle-wrapper.properties gradle/libs.versions.toml app/build.gradle
git commit -m "build: use Tencent Maven mirrors"
~~~

If repository permissions still prevent commits, keep the working-tree changes and record the exact permission error in the handoff.

### Task 2: Harden the data and backup boundary

**Files:**
- Modify: app/src/main/java/com/example/myandroid/data/db/dao/PasswordDao.java
- Modify: app/src/main/java/com/example/myandroid/data/db/dao/CategoryDao.java
- Modify: app/src/main/java/com/example/myandroid/data/db/AppDatabase.java
- Modify: app/src/main/java/com/example/myandroid/data/repository/PasswordRepository.java
- Modify: app/src/main/java/com/example/myandroid/data/repository/CategoryRepository.java
- Create: app/src/main/java/com/example/myandroid/data/repository/BackupRepository.java
- Modify: app/src/main/java/com/example/myandroid/data/model/ExportData.java
- Modify: app/src/main/java/com/example/myandroid/util/StorageCrypto.java
- Modify: app/src/main/java/com/example/myandroid/util/WebDavUtils.java only for explicit response/error handling
- Test: app/src/test/java/com/example/myandroid/data/BackupRepositoryTest.java
- Test: app/src/androidTest/java/com/example/myandroid/data/AppDatabaseInstrumentedTest.java

**Interfaces:**
- Produces: BackupRepository.exportSync(), BackupRepository.restoreSync(ExportData), BackupRepository.parseAndRestore(String, String), and PasswordRepository.migrateLegacyApiKeys().
- Consumes: existing Room entities and CryptoUtils.

- [ ] **Step 1: Add DAO operations needed by a transaction**

Add deleteAll(), insertAll(List<Category>), and getAllCategoriesSync() to CategoryDao as needed. Add a reactive category-count query to PasswordDao and extend the search query to include url:

~~~java
@Query("SELECT * FROM password_entries WHERE title LIKE '%' || :query || '%' OR username LIKE '%' || :query || '%' OR url LIKE '%' || :query || '%' ORDER BY updated_at DESC")
LiveData<List<PasswordEntry>> searchPasswords(String query);
~~~

Keep the existing sync queries for backup work.

- [ ] **Step 2: Make StorageCrypto distinguish failure from an empty secret**

Add throwing methods with the same AES-GCM format:

~~~java
public static String encryptRequired(String plaintext) throws GeneralSecurityException;
public static String decryptRequired(String encrypted) throws GeneralSecurityException;
~~~

Keep the old methods only as compatibility wrappers for existing call sites, but make Repository and preference code use the throwing methods so a Keystore failure cannot silently save an empty value.

- [ ] **Step 3: Add one-time legacy API Key migration**

Implement PasswordRepository.migrateLegacyApiKeys() on the database executor. For each TYPE_API_KEY record whose decrypted password is empty and username is non-empty, copy username into the decrypted secret, clear username, encrypt the secret with StorageCrypto.encryptRequired, and update the entity. Make the operation idempotent and invoke it from App.onCreate() after database initialization.

- [ ] **Step 4: Implement transactional backup replacement**

Create BackupRepository with this behavior:

~~~java
public ExportData exportSync() throws Exception;
public void restoreSync(ExportData data) throws Exception;
public void restoreEncryptedSync(String encrypted, String password) throws Exception;
~~~

exportSync() must decrypt database secrets into the in-memory export object, set version = 2, and include categories. restoreSync() must validate the object, encrypt secrets before insert, and perform category deletion, password deletion, category insertion, and password insertion inside AppDatabase.runInTransaction(...). The operation must preserve imported IDs so category references remain valid.

- [ ] **Step 5: Encrypt WebDAV credentials at rest**

Update SettingsViewModel preference serialization to store username and password using StorageCrypto.encryptRequired. Keep the server URL as plain text. When reading an old unencrypted value, support it once and rewrite it encrypted after a successful load.

- [ ] **Step 6: Add tests for data safety**

Cover these concrete cases:

~~~java
@Test public void restoreV1WithoutCategoriesIsAccepted()
@Test public void restoreV2RestoresCategoriesAndPasswordsTogether()
@Test public void restoreFailureDoesNotDeleteExistingData()
@Test public void legacyApiKeyMovesFromUsernameToEncryptedSecret()
~~~

The instrumented Room test must assert deleting a category leaves its password row with category_id IS NULL.

### Task 3: Introduce the new app shell and application lock

**Files:**
- Modify: app/src/main/AndroidManifest.xml
- Modify: app/src/main/kotlin/com/example/myandroid/ui/main/MainActivity.kt
- Create: app/src/main/kotlin/com/example/myandroid/ui/AppNavHost.kt
- Create: app/src/main/kotlin/com/example/myandroid/ui/AppRoute.kt
- Modify: app/src/main/java/com/example/myandroid/util/BiometricHelper.java
- Modify: app/src/main/java/com/example/myandroid/App.java
- Test: app/src/androidTest/java/com/example/myandroid/ui/AppNavigationTest.kt

**Interfaces:**
- Produces: AppRoute, AppNavHost, and a lock gate that exposes locked, authenticating, and unlocked states.
- Consumes: the existing screen ViewModels and new screen Composables from Tasks 4-7.

- [ ] **Step 1: Change the main host to FragmentActivity**

Change MainActivity to extend FragmentActivity, call enableEdgeToEdge(), and render AppNavHost inside VisionOSPasswordManagerTheme. Do not start the old page Activities from the new host.

- [ ] **Step 2: Define typed routes**

Use a sealed route definition with stable route strings:

~~~kotlin
sealed interface AppRoute {
    data object Vault : AppRoute
    data object Search : AppRoute
    data class Editor(val id: Long?) : AppRoute
    data object Categories : AppRoute
    data object Settings : AppRoute
}
~~~

Encode the editor ID in the navigation route and restore it from SavedStateHandle.

- [ ] **Step 3: Implement app lock lifecycle**

Update BiometricHelper to allow BIOMETRIC_STRONG or DEVICE_CREDENTIAL. In MainActivity, when the setting is enabled, lock on cold start and after returning from background; call BiometricHelper.authenticate once per lock cycle. On error, keep the lock screen visible with retry and a clear failure message. When disabled or unavailable, render the app directly.

- [ ] **Step 4: Register only the new launcher entry**

Keep old Activity source files for compatibility, but remove their activity registrations from the manifest. Leave only the new MainActivity launcher declaration and the required permissions.

- [ ] **Step 5: Test navigation and lock states**

Add a Compose/instrumented test that launches the main route, verifies the vault content is reachable, and verifies the lock gate renders its retry action when authentication is required. Use a fake authentication callback for the test path rather than invoking device biometrics.

### Task 4: Replace the visual foundation with a Material 3 design system

**Files:**
- Modify: app/src/main/kotlin/com/example/myandroid/ui/theme/Color.kt
- Modify: app/src/main/kotlin/com/example/myandroid/ui/theme/Theme.kt
- Modify: app/src/main/kotlin/com/example/myandroid/ui/theme/Shape.kt
- Modify: app/src/main/kotlin/com/example/myandroid/ui/theme/Type.kt
- Modify: app/src/main/kotlin/com/example/myandroid/ui/theme/Animation.kt
- Create: app/src/main/kotlin/com/example/myandroid/ui/component/AppScaffold.kt
- Create: app/src/main/kotlin/com/example/myandroid/ui/component/SectionHeader.kt
- Create: app/src/main/kotlin/com/example/myandroid/ui/component/PasswordListItem.kt
- Create: app/src/main/kotlin/com/example/myandroid/ui/component/InlineStatus.kt
- Modify: app/src/main/res/values/strings.xml

**Interfaces:**
- Produces: shared AppScaffold, AppTopBar, AppSectionHeader, PasswordListItem, and status components with Material 3 tokens.

- [ ] **Step 1: Define the new palette and typography**

Replace the current purple/glass palette with semantic light and dark colors: neutral background and surface, ink text, low-saturation teal primary, amber warning, and red destructive color. Use lightColorScheme and darkColorScheme; every screen consumes MaterialTheme.colorScheme.

- [ ] **Step 2: Reduce shape and motion tokens**

Set cards and fields to restrained small/medium shapes, keep a 4dp spacing convention, and replace bouncy spring defaults with short tween/non-bouncy transitions. Add a reduced-motion branch using state-driven animation choices.

- [ ] **Step 3: Build shared app chrome**

AppScaffold must switch between bottom navigation on compact windows and a navigation rail on expanded windows. Its content API must accept selectedRoute, onRouteSelected, and a slot for page content. PasswordListItem must support title, username/secret masking, favorite toggle, copy actions, and loading/disabled states without nested cards.

- [ ] **Step 4: Add accessible strings and semantics**

Move new visible text and content descriptions into strings.xml. Add Role.Button, state descriptions, and a visible focus treatment to icon-only actions. Do not use the old literal chevron/back text in new screens.

### Task 5: Rebuild the vault and search flows

**Files:**
- Create: app/src/main/kotlin/com/example/myandroid/ui/vault/VaultScreen.kt
- Create: app/src/main/kotlin/com/example/myandroid/ui/vault/VaultViewModel.kt
- Create: app/src/main/kotlin/com/example/myandroid/ui/search/SearchScreenV2.kt
- Modify: app/src/main/kotlin/com/example/myandroid/ui/main/MainViewModel.kt or replace its exposed state with VaultUiState
- Modify: app/src/main/java/com/example/myandroid/data/db/dao/PasswordDao.java
- Test: app/src/androidTest/java/com/example/myandroid/ui/vault/VaultScreenTest.kt
- Test: app/src/test/java/com/example/myandroid/ui/vault/VaultViewModelTest.kt

**Interfaces:**
- Produces: VaultUiState, VaultEvent, and SearchUiState consumed by AppNavHost.

- [ ] **Step 1: Define vault state and events**

Use explicit state types:

~~~kotlin
data class VaultUiState(
    val categories: List<Category> = emptyList(),
    val entries: List<PasswordEntry> = emptyList(),
    val selectedCategoryId: Long? = null,
    val query: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

sealed interface VaultEvent {
    data object AddEntry : VaultEvent
    data class EditEntry(val id: Long) : VaultEvent
    data class SelectCategory(val id: Long?) : VaultEvent
    data class ToggleFavorite(val entry: PasswordEntry) : VaultEvent
    data object OpenSearch : VaultEvent
}
~~~

- [ ] **Step 2: Build the vault layout**

Render a top bar with the vault title and search action, a compact count summary, filter chips for all/favorites/categories, and a lazy list of PasswordListItem. Use a clear empty state with one add action. Use the existing repositories only through the ViewModel.

- [ ] **Step 3: Add safe copy and secret visibility behavior**

Keep secrets masked by default, show a per-row visibility state, expose separate copy actions, and clear the clipboard after a short delay when the app still owns the copied clip. API Keys use the same secret display path as passwords.

- [ ] **Step 4: Implement search over title, username, and URL**

Use a 300ms debounce in SearchViewModel, query the repository, and render loading, no-query, no-result, error, and result states. Tapping a result navigates to the editor route.

- [ ] **Step 5: Add flow tests**

Test category selection, favorite updates, empty state, URL search, and navigation events. Compose tests must assert labels and click targets rather than implementation details such as exact padding.

### Task 6: Rebuild entry editing and category management

**Files:**
- Create: app/src/main/kotlin/com/example/myandroid/ui/entry/EntryEditorScreen.kt
- Modify: app/src/main/kotlin/com/example/myandroid/ui/add/AddEditViewModel.kt
- Create: app/src/main/kotlin/com/example/myandroid/ui/category/CategoryScreenV2.kt
- Modify: app/src/main/kotlin/com/example/myandroid/ui/category/CategoryViewModel.kt
- Modify: app/src/main/java/com/example/myandroid/data/repository/CategoryRepository.java
- Test: app/src/test/java/com/example/myandroid/ui/entry/AddEditViewModelTest.kt
- Test: app/src/androidTest/java/com/example/myandroid/ui/category/CategoryScreenTest.kt

**Interfaces:**
- Produces: a form state with type, title, username, secret, URL, notes, category, validation errors, saving state, and delete state.

- [ ] **Step 1: Normalize the editor model**

Expose a single secret field in the editor. For TYPE_PASSWORD, label it “密码”; for TYPE_API_KEY, label it “API Key”. Save both values into PasswordEntry.password; leave username empty for API Key. Preserve the entry favorite flag and timestamps when editing.

- [ ] **Step 2: Build the editor screen**

Use grouped sections for type, basic information, secret, category, URL, and notes. Put save in a stable top/bottom action area, show validation beside the field, show password strength only for normal passwords, and provide a reveal/copy affordance for the secret.

- [ ] **Step 3: Make save/delete operations stateful**

Replace Boolean-only result events with loading/success/error states. Keep the screen open on failure, display the actionable error, and navigate back only after a confirmed successful write.

- [ ] **Step 4: Rebuild category management**

Render category rows with icon, name, count, edit, and delete actions. Add duplicate-name validation. Delete only the category entity and rely on ON DELETE SET NULL; refresh counts from a reactive DAO source so password changes are reflected.

- [ ] **Step 5: Test editor and category edge cases**

Cover blank title, blank username for passwords, API Key secret validation, edit preservation, duplicate category names, deleting a category with entries, and failed writes.

### Task 7: Rebuild settings, backup, WebDAV, and security UX

**Files:**
- Create: app/src/main/kotlin/com/example/myandroid/ui/settings/SettingsScreenV2.kt
- Modify: app/src/main/kotlin/com/example/myandroid/ui/settings/SettingsViewModel.kt
- Create: app/src/main/kotlin/com/example/myandroid/ui/backup/BackupViewModel.kt
- Modify: app/src/main/kotlin/com/example/myandroid/ui/main/MainScreen.kt only to remove new-flow ownership if needed
- Modify: app/src/main/res/values/strings.xml
- Test: app/src/test/java/com/example/myandroid/ui/settings/SettingsViewModelTest.kt
- Test: app/src/androidTest/java/com/example/myandroid/ui/settings/SettingsScreenTest.kt

**Interfaces:**
- Produces: SettingsUiState, BackupUiState, and explicit actions for test connection, upload, download, export, and import.
- Consumes: BackupRepository, WebDavUtils, and BiometricHelper.

- [ ] **Step 1: Split settings into security, sync, and data sections**

Render security/app-lock settings first, WebDAV connection settings second, and local export/import controls third. Disable actions while work is running and show progress in the action itself.

- [ ] **Step 2: Use the shared backup repository**

Remove duplicated export/import logic from MainScreen.kt. Route local file operations and WebDAV operations through BackupRepository so both paths use ExportData.version = 2, restore categories, and report errors consistently.

- [ ] **Step 3: Add recovery confirmation and progress**

Before restore, show the remote/local record count and state that existing records will be replaced. On failure, keep the current database and show a retry action. On success, show the restored count and invalidate vault/category state.

- [ ] **Step 4: Test settings states**

Test encrypted preference round-trip, blank server validation, connection failure, upload failure, restore confirmation, and successful restore invalidation.

### Task 8: Wire, verify, document, and finish the refactor

**Files:**
- Modify: app/src/main/kotlin/com/example/myandroid/ui/AppNavHost.kt
- Modify: app/src/main/AndroidManifest.xml
- Modify: README.md
- Modify: DEV_PLAN.md
- Create or modify: app/src/androidTest/java/com/example/myandroid/ui/EndToEndSmokeTest.kt
- Test: all existing JVM and Android tests

- [ ] **Step 1: Connect every new screen to the route graph**

Map each route to its new screen and pass only route arguments and navigation callbacks. Confirm back navigation, editor deep links, state restoration, and bottom navigation selection.

- [ ] **Step 2: Remove duplicated behavior from active paths**

Ensure active navigation no longer calls startActivity for add, search, category, or settings. Keep legacy source files untouched unless compilation requires a compatibility adjustment.

- [ ] **Step 3: Update project documentation**

Rewrite README and DEV_PLAN technology and structure sections to describe Kotlin/Compose, the single Activity shell, Room compatibility, encryption boundaries, Tencent mirrors, and the exclusion of Linear OAuth. Do not copy the old XML architecture as current behavior.

- [ ] **Step 4: Run the complete verification matrix**

Run:

~~~powershell
$env:GRADLE_USER_HOME = (Join-Path (Get-Location) '.gradle')
./gradlew.bat test
./gradlew.bat :app:testDebugUnitTest
./gradlew.bat :app:assembleDebug
./gradlew.bat :app:connectedDebugAndroidTest
~~~

When no device/emulator is available, record the instrumented-test limitation and still run unit tests and assemble. Inspect the generated APK and verify the launcher activity, app label, and no Linear OAuth activity are present.

- [ ] **Step 5: Review the final diff and commit each coherent batch**

Use:

~~~powershell
git status --short
git diff --check
git diff --stat
git log --oneline -5
~~~

Create separate commits for build sources, data/security, app shell, UI flows, and tests/docs when repository write permissions are available.
