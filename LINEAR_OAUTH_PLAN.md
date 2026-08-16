# Linear OAuth 流程实现计划

> **For agentic workers:** 使用 writing-plans skill 创建的实现计划

**Goal:** 在 Android 密码管理器中实现 Linear OAuth 2.1 认证流程，支持动态客户端注册

**Architecture:** 使用 Chrome Custom Tabs 安全地打开 Linear 授权页面，通过 App Links 处理 OAuth 回调，使用 OkHttp 进行令牌交换请求

**Tech Stack:** Java, OkHttp, Chrome Custom Tabs, AndroidX Security

---

## 文件结构

| 文件路径 | 职责 |
|---------|------|
| `app/src/main/java/com/example/myandroid/util/LinearOAuthConfig.java` | OAuth 配置常量 |
| `app/src/main/java/com/example/myandroid/util/LinearOAuthManager.java` | OAuth 认证管理（单例） |
| `app/src/main/java/com/example/myandroid/ui/oauth/LinearOAuthActivity.java` | OAuth 认证 Activity |
| `app/src/main/res/layout/activity_linear_oauth.xml` | OAuth 页面布局 |
| `app/src/main/res/values/strings.xml` | 添加 OAuth 相关字符串 |
| `app/src/main/AndroidManifest.xml` | 注册 Activity 和 intent-filter |
| `gradle/libs.versions.toml` | 添加 Chrome Custom Tabs 依赖 |
| `app/build.gradle` | 添加依赖引用 |

---

## 任务分解

### Task 1: 添加 Chrome Custom Tabs 依赖

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle`

- [ ] **Step 1: 更新版本目录**

在 `gradle/libs.versions.toml` 的 `[versions]` 部分添加：
```toml
chrome-custom-tabs = "1.7.0"
```

在 `[libraries]` 部分添加：
```toml
# Chrome Custom Tabs
chrome-custom-tabs = { group = "androidx.browser", name = "browser", version.ref = "chrome-custom-tabs" }
```

- [ ] **Step 2: 更新 app/build.gradle**

在 `dependencies` 部分添加：
```groovy
implementation libs.chrome.custom.tabs
```

- [ ] **Step 3: 同步 Gradle**

运行: `./gradlew sync`

---

### Task 2: 创建 OAuth 配置类

**Files:**
- Create: `app/src/main/java/com/example/myandroid/util/LinearOAuthConfig.java`

- [ ] **Step 1: 创建配置类**

```java
package com.example.myandroid.util;

public class LinearOAuthConfig {
    // Linear MCP OAuth 端点
    public static final String MCP_ENDPOINT = "https://mcp.linear.app/mcp";
    
    // OAuth 2.1 授权端点（动态发现）
    public static final String AUTHORIZATION_ENDPOINT = "https://mcp.linear.app/oauth/authorize";
    
    // OAuth 2.1 令牌端点（动态发现）
    public static final String TOKEN_ENDPOINT = "https://mcp.linear.app/oauth/token";
    
    // 动态客户端注册端点
    public static final String REGISTRATION_ENDPOINT = "https://mcp.linear.app/oauth/register";
    
    // 回调 URI（使用 App Links）
    public static final String REDIRECT_URI = "com.example.myandroid://oauth/callback";
    
    // 请求的权限范围
    public static final String SCOPE = "read write";
    
    // PKCE 参数
    public static final String CODE_CHALLENGE_METHOD = "S256";
    
    // 令牌存储键名
    public static final String PREF_NAME = "linear_oauth";
    public static final String KEY_ACCESS_TOKEN = "access_token";
    public static final String KEY_REFRESH_TOKEN = "refresh_token";
    public static final String KEY_TOKEN_EXPIRES_AT = "token_expires_at";
    public static final String KEY_CLIENT_ID = "client_id";
    public static final String KEY_CLIENT_SECRET = "client_secret";
}
```

---

### Task 3: 创建 OAuth 管理类

**Files:**
- Create: `app/src/main/java/com/example/myandroid/util/LinearOAuthManager.java`

- [ ] **Step 1: 创建 OAuth 管理类骨架**

```java
package com.example.myandroid.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LinearOAuthManager {
    private static final String TAG = "LinearOAuthManager";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    
    private static volatile LinearOAuthManager INSTANCE;
    private final Context context;
    private final OkHttpClient client;
    private final SharedPreferences prefs;
    
    // PKCE 参数
    private String codeVerifier;
    private String codeChallenge;
    
    private LinearOAuthManager(Context context) {
        this.context = context.getApplicationContext();
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.prefs = this.context.getSharedPreferences(
                LinearOAuthConfig.PREF_NAME, Context.MODE_PRIVATE);
    }
    
    public static LinearOAuthManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (LinearOAuthManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new LinearOAuthManager(context);
                }
            }
        }
        return INSTANCE;
    }
}
```

- [ ] **Step 2: 添加 PKCE 方法**

在 `LinearOAuthManager` 类中添加：
```java
/**
 * 生成 PKCE code verifier
 */
public String generateCodeVerifier() {
    SecureRandom random = new SecureRandom();
    byte[] bytes = new byte[32];
    random.nextBytes(bytes);
    codeVerifier = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    return codeVerifier;
}

/**
 * 生成 PKCE code challenge
 */
public String generateCodeChallenge(String verifier) {
    try {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(verifier.getBytes());
        codeChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        return codeChallenge;
    } catch (NoSuchAlgorithmException e) {
        Log.e(TAG, "SHA-256 not available", e);
        return null;
    }
}

/**
 * 获取授权 URL
 */
public String getAuthorizationUrl() {
    String verifier = generateCodeVerifier();
    String challenge = generateCodeChallenge(verifier);
    
    return Uri.parse(LinearOAuthConfig.AUTHORIZATION_ENDPOINT)
            .buildUpon()
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("client_id", getClientId())
            .appendQueryParameter("redirect_uri", LinearOAuthConfig.REDIRECT_URI)
            .appendQueryParameter("scope", LinearOAuthConfig.SCOPE)
            .appendQueryParameter("code_challenge", challenge)
            .appendQueryParameter("code_challenge_method", LinearOAuthConfig.CODE_CHALLENGE_METHOD)
            .build()
            .toString();
}
```

- [ ] **Step 3: 添加令牌管理方法**

```java
/**
 * 保存令牌
 */
public void saveTokens(String accessToken, String refreshToken, long expiresAt) {
    prefs.edit()
            .putString(LinearOAuthConfig.KEY_ACCESS_TOKEN, accessToken)
            .putString(LinearOAuthConfig.KEY_REFRESH_TOKEN, refreshToken)
            .putLong(LinearOAuthConfig.KEY_TOKEN_EXPIRES_AT, expiresAt)
            .apply();
}

/**
 * 获取访问令牌
 */
public String getAccessToken() {
    return prefs.getString(LinearOAuthConfig.KEY_ACCESS_TOKEN, null);
}

/**
 * 获取刷新令牌
 */
public String getRefreshToken() {
    return prefs.getString(LinearOAuthConfig.KEY_REFRESH_TOKEN, null);
}

/**
 * 检查令牌是否过期
 */
public boolean isTokenExpired() {
    long expiresAt = prefs.getLong(LinearOAuthConfig.KEY_TOKEN_EXPIRES_AT, 0);
    return System.currentTimeMillis() >= expiresAt;
}

/**
 * 检查是否已认证
 */
public boolean isAuthenticated() {
    return getAccessToken() != null && !isTokenExpired();
}

/**
 * 清除令牌
 */
public void clearTokens() {
    prefs.edit()
            .remove(LinearOAuthConfig.KEY_ACCESS_TOKEN)
            .remove(LinearOAuthConfig.KEY_REFRESH_TOKEN)
            .remove(LinearOAuthConfig.KEY_TOKEN_EXPIRES_AT)
            .apply();
}

/**
 * 保存客户端信息
 */
public void saveClientInfo(String clientId, String clientSecret) {
    prefs.edit()
            .putString(LinearOAuthConfig.KEY_CLIENT_ID, clientId)
            .putString(LinearOAuthConfig.KEY_CLIENT_SECRET, clientSecret)
            .apply();
}

/**
 * 获取客户端 ID
 */
public String getClientId() {
    return prefs.getString(LinearOAuthConfig.KEY_CLIENT_ID, null);
}

/**
 * 获取客户端密钥
 */
public String getClientSecret() {
    return prefs.getString(LinearOAuthConfig.KEY_CLIENT_SECRET, null);
}
```

- [ ] **Step 4: 添加网络请求方法**

```java
/**
 * 动态注册客户端
 */
public void registerClient(Callback callback) {
    String json = "{" +
            "\"client_name\":\"MyAndroid Password Manager\"," +
            "\"redirect_uris\":[\"" + LinearOAuthConfig.REDIRECT_URI + "\"]," +
            "\"grant_types\":[\"authorization_code\",\"refresh_token\"]," +
            "\"response_types\":[\"code\"]," +
            "\"scope\":\"" + LinearOAuthConfig.SCOPE + "\"" +
            "}";
    
    RequestBody body = RequestBody.create(json, JSON);
    Request request = new Request.Builder()
            .url(LinearOAuthConfig.REGISTRATION_ENDPOINT)
            .post(body)
            .header("Content-Type", "application/json")
            .build();
    
    client.newCall(request).enqueue(new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.e(TAG, "Client registration failed", e);
            if (callback != null) callback.onFailure(call, e);
        }
        
        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                // 解析 JSON 获取 client_id 和 client_secret
                // 使用 Gson 解析
                saveClientInfo(/* clientId */, /* clientSecret */);
                if (callback != null) callback.onResponse(call, response);
            } else {
                if (callback != null) callback.onFailure(call, 
                    new IOException("Registration failed: " + response.code()));
            }
        }
    });
}

/**
 * 用授权码换取令牌
 */
public void exchangeCodeForToken(String code, Callback callback) {
    String json = "{" +
            "\"grant_type\":\"authorization_code\"," +
            "\"code\":\"" + code + "\"," +
            "\"redirect_uri\":\"" + LinearOAuthConfig.REDIRECT_URI + "\"," +
            "\"client_id\":\"" + getClientId() + "\"," +
            "\"client_secret\":\"" + getClientSecret() + "\"," +
            "\"code_verifier\":\"" + codeVerifier + "\"" +
            "}";
    
    RequestBody body = RequestBody.create(json, JSON);
    Request request = new Request.Builder()
            .url(LinearOAuthConfig.TOKEN_ENDPOINT)
            .post(body)
            .header("Content-Type", "application/json")
            .build();
    
    client.newCall(request).enqueue(callback);
}

/**
 * 刷新令牌
 */
public void refreshToken(Callback callback) {
    String refreshToken = getRefreshToken();
    if (refreshToken == null) {
        if (callback != null) {
            callback.onFailure(null, new IOException("No refresh token available"));
        }
        return;
    }
    
    String json = "{" +
            "\"grant_type\":\"refresh_token\"," +
            "\"refresh_token\":\"" + refreshToken + "\"," +
            "\"client_id\":\"" + getClientId() + "\"," +
            "\"client_secret\":\"" + getClientSecret() + "\"" +
            "}";
    
    RequestBody body = RequestBody.create(json, JSON);
    Request request = new Request.Builder()
            .url(LinearOAuthConfig.TOKEN_ENDPOINT)
            .post(body)
            .header("Content-Type", "application/json")
            .build();
    
    client.newCall(request).enqueue(callback);
}
```

---

### Task 4: 创建 OAuth Activity 布局

**Files:**
- Create: `app/src/main/res/layout/activity_linear_oauth.xml`

- [ ] **Step 1: 创建布局文件**

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center"
    android:padding="24dp">

    <ImageView
        android:layout_width="80dp"
        android:layout_height="80dp"
        android:src="@drawable/ic_launcher_foreground"
        android:contentDescription="@string/app_name"
        android:layout_marginBottom="24dp" />

    <TextView
        android:id="@+id/tv_status"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/oauth_connecting"
        android:textSize="18sp"
        android:textColor="?android:attr/textColorPrimary"
        android:layout_marginBottom="16dp" />

    <ProgressBar
        android:id="@+id/progress_bar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginBottom="24dp" />

    <Button
        android:id="@+id/btn_retry"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/oauth_retry"
        android:visibility="gone" />

    <Button
        android:id="@+id/btn_cancel"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/oauth_cancel"
        android:visibility="gone"
        style="?attr/materialButtonOutlinedStyle" />

</LinearLayout>
```

---

### Task 5: 创建 OAuth Activity

**Files:**
- Create: `app/src/main/java/com/example/myandroid/ui/oauth/LinearOAuthActivity.java`

- [ ] **Step 1: 创建 Activity 骨架**

```java
package com.example.myandroid.ui.oauth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import com.example.myandroid.R;
import com.example.myandroid.util.LinearOAuthManager;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LinearOAuthActivity extends AppCompatActivity {
    private static final String TAG = "LinearOAuthActivity";
    
    private LinearOAuthManager oAuthManager;
    private TextView tvStatus;
    private ProgressBar progressBar;
    private Button btnRetry;
    private Button btnCancel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linear_oauth);
        
        oAuthManager = LinearOAuthManager.getInstance(this);
        
        initViews();
        setupListeners();
        
        // 检查是否已有有效的令牌
        if (oAuthManager.isAuthenticated()) {
            setResult(RESULT_OK);
            finish();
            return;
        }
        
        // 检查是否是回调
        if (handleOAuthCallback()) {
            return;
        }
        
        // 开始 OAuth 流程
        startOAuthFlow();
    }
    
    private void initViews() {
        tvStatus = findViewById(R.id.tv_status);
        progressBar = findViewById(R.id.progress_bar);
        btnRetry = findViewById(R.id.btn_retry);
        btnCancel = findViewById(R.id.btn_cancel);
    }
    
    private void setupListeners() {
        btnRetry.setOnClickListener(v -> startOAuthFlow());
        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }
}
```

- [ ] **Step 2: 添加 OAuth 流程方法**

在 `LinearOAuthActivity` 类中添加：
```java
/**
 * 开始 OAuth 流程
 */
private void startOAuthFlow() {
    showLoading(getString(R.string.oauth_registering));
    
    // 先注册客户端
    oAuthManager.registerClient(new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            runOnUiThread(() -> {
                Log.e(TAG, "Client registration failed", e);
                showError(getString(R.string.oauth_registration_failed));
            });
        }
        
        @Override
        public void onResponse(Call call, Response response) throws IOException {
            runOnUiThread(() -> {
                if (response.isSuccessful()) {
                    // 打开授权页面
                    openAuthorizationPage();
                } else {
                    showError(getString(R.string.oauth_registration_failed));
                }
            });
        }
    });
}

/**
 * 打开授权页面
 */
private void openAuthorizationPage() {
    showLoading(getString(R.string.oauth_opening_auth));
    
    String authUrl = oAuthManager.getAuthorizationUrl();
    
    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
    CustomTabsIntent customTabsIntent = builder.build();
    
    try {
        customTabsIntent.launchUrl(this, Uri.parse(authUrl));
    } catch (Exception e) {
        Log.e(TAG, "Failed to open auth page", e);
        showError(getString(R.string.oauth_auth_page_failed));
    }
}

/**
 * 处理 OAuth 回调
 */
private boolean handleOAuthCallback() {
    Intent intent = getIntent();
    Uri data = intent.getData();
    
    if (data != null && data.toString().startsWith(LinearOAuthConfig.REDIRECT_URI)) {
        String code = data.getQueryParameter("code");
        String error = data.getQueryParameter("error");
        
        if (error != null) {
            showError(getString(R.string.oauth_auth_denied));
            return true;
        }
        
        if (code != null) {
            exchangeCodeForToken(code);
            return true;
        }
    }
    
    return false;
}

/**
 * 用授权码换取令牌
 */
private void exchangeCodeForToken(String code) {
    showLoading(getString(R.string.oauth_exchanging_code));
    
    oAuthManager.exchangeCodeForToken(code, new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            runOnUiThread(() -> {
                Log.e(TAG, "Token exchange failed", e);
                showError(getString(R.string.oauth_token_exchange_failed));
            });
        }
        
        @Override
        public void onResponse(Call call, Response response) throws IOException {
            runOnUiThread(() -> {
                if (response.isSuccessful()) {
                    // 解析响应并保存令牌
                    // 使用 Gson 解析
                    showSuccess();
                } else {
                    showError(getString(R.string.oauth_token_exchange_failed));
                }
            });
        }
    });
}
```

- [ ] **Step 3: 添加 UI 状态方法**

```java
/**
 * 显示加载状态
 */
private void showLoading(String message) {
    tvStatus.setText(message);
    progressBar.setVisibility(View.VISIBLE);
    btnRetry.setVisibility(View.GONE);
    btnCancel.setVisibility(View.GONE);
}

/**
 * 显示错误状态
 */
private void showError(String message) {
    tvStatus.setText(message);
    progressBar.setVisibility(View.GONE);
    btnRetry.setVisibility(View.VISIBLE);
    btnCancel.setVisibility(View.VISIBLE);
}

/**
 * 显示成功状态
 */
private void showSuccess() {
    tvStatus.setText(getString(R.string.oauth_success));
    progressBar.setVisibility(View.GONE);
    btnRetry.setVisibility(View.GONE);
    btnCancel.setVisibility(View.GONE);
    
    // 延迟关闭
    tvStatus.postDelayed(() -> {
        setResult(RESULT_OK);
        finish();
    }, 1500);
}
```

---

### Task 6: 添加字符串资源

**Files:**
- Modify: `app/src/main/res/values/strings.xml`

- [ ] **Step 1: 添加 OAuth 相关字符串**

在 `strings.xml` 中添加：
```xml
<!-- Linear OAuth -->
<string name="oauth_connecting">正在连接 Linear…</string>
<string name="oauth_registering">正在注册应用…</string>
<string name="oauth_opening_auth">正在打开授权页面…</string>
<string name="oauth_exchanging_code">正在获取访问令牌…</string>
<string name="oauth_auth_denied">授权被拒绝</string>
<string name="oauth_registration_failed">应用注册失败，请重试</string>
<string name="oauth_auth_page_failed">无法打开授权页面</string>
<string name="oauth_token_exchange_failed">令牌获取失败，请重试</string>
<string name="oauth_success">授权成功！</string>
<string name="oauth_retry">重试</string>
<string name="oauth_cancel">取消</string>
<string name="oauth_login">连接 Linear</string>
<string name="oauth_logout">断开 Linear</string>
```

---

### Task 7: 更新 AndroidManifest.xml

**Files:**
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: 注册 OAuth Activity**

在 `<application>` 标签内添加：
```xml
<activity
    android:name=".ui.oauth.LinearOAuthActivity"
    android:exported="true"
    android:parentActivityName=".ui.settings.SettingsActivity">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data
            android:scheme="com.example.myandroid"
            android:host="oauth"
            android:path="/callback" />
    </intent-filter>
</activity>
```

---

### Task 8: 在设置页面添加入口

**Files:**
- Modify: `app/src/main/java/com/example/myandroid/ui/settings/SettingsActivity.java`
- Modify: `app/src/main/res/layout/activity_settings.xml`

- [ ] **Step 1: 添加 Linear 连接按钮到布局**

在设置布局中添加：
```xml
<com.google.android.material.button.MaterialButton
    android:id="@+id/btn_linear_oauth"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="@string/oauth_login"
    style="?attr/materialButtonOutlinedStyle"
    app:icon="@drawable/ic_launcher_foreground"
    android:layout_marginTop="16dp" />
```

- [ ] **Step 2: 添加点击事件**

在 `SettingsActivity.java` 中添加：
```java
private void setupLinearOAuth() {
    LinearOAuthManager oAuthManager = LinearOAuthManager.getInstance(this);
    MaterialButton btnLinearOAuth = findViewById(R.id.btn_linear_oauth);
    
    // 更新按钮状态
    updateOAuthButton(btnLinearOAuth, oAuthManager);
    
    btnLinearOAuth.setOnClickListener(v -> {
        if (oAuthManager.isAuthenticated()) {
            // 断开连接
            oAuthManager.clearTokens();
            updateOAuthButton(btnLinearOAuth, oAuthManager);
        } else {
            // 启动 OAuth 流程
            Intent intent = new Intent(this, LinearOAuthActivity.class);
            startActivity(intent);
        }
    });
}

private void updateOAuthButton(MaterialButton button, LinearOAuthManager manager) {
    if (manager.isAuthenticated()) {
        button.setText(R.string.oauth_logout);
    } else {
        button.setText(R.string.oauth_login);
    }
}

@Override
protected void onResume() {
    super.onResume();
    // 刷新按钮状态
    MaterialButton btnLinearOAuth = findViewById(R.id.btn_linear_oauth);
    if (btnLinearOAuth != null) {
        LinearOAuthManager oAuthManager = LinearOAuthManager.getInstance(this);
        updateOAuthButton(btnLinearOAuth, oAuthManager);
    }
}
```

---

## 测试验证

### 手动测试步骤

1. **构建应用**
   ```bash
   ./gradlew assembleDebug
   ```

2. **安装并运行**
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

3. **测试 OAuth 流程**
   - 打开设置页面
   - 点击"连接 Linear"按钮
   - 验证 Chrome Custom Tabs 打开
   - 完成 Linear 授权
   - 验证回调处理
   - 验证令牌保存

4. **测试断开连接**
   - 点击"断开 Linear"按钮
   - 验证令牌清除

---

## 注意事项

1. **安全性**: 使用 PKCE 保护授权码流程
2. **用户体验**: Chrome Custom Tabs 提供安全的浏览器体验
3. **错误处理**: 完善的错误提示和重试机制
4. **令牌管理**: 自动检测令牌过期并支持刷新
