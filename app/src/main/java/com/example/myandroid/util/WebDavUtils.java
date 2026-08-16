package com.example.myandroid.util;

import java.util.concurrent.TimeUnit;

import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WebDavUtils {

    private static final MediaType OCTET_STREAM = MediaType.parse("application/octet-stream");
    private static final int CONNECT_TIMEOUT = 15;
    private static final int READ_TIMEOUT = 30;

    private static volatile WebDavUtils INSTANCE;

    private final OkHttpClient client;

    private WebDavUtils() {
        client = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .build();
    }

    public static WebDavUtils getInstance() {
        if (INSTANCE == null) {
            synchronized (WebDavUtils.class) {
                if (INSTANCE == null) {
                    INSTANCE = new WebDavUtils();
                }
            }
        }
        return INSTANCE;
    }

    private String getAuthHeader(String username, String password) {
        return Credentials.basic(username, password);
    }

    private String buildUrl(String serverUrl, String fileName) {
        String url = normalizeUrl(serverUrl);
        url = url.endsWith("/") ? url : url + "/";
        return url + fileName;
    }

    private String normalizeUrl(String serverUrl) {
        if (serverUrl == null || serverUrl.isEmpty()) return serverUrl;
        if (serverUrl.startsWith("http://") || serverUrl.startsWith("https://")) {
            return serverUrl;
        }
        return "https://" + serverUrl;
    }

    public boolean testConnection(String serverUrl, String username, String password) throws Exception {
        Request request = new Request.Builder()
                .url(buildUrl(serverUrl, ""))
                .method("PROPFIND", null)
                .header("Authorization", getAuthHeader(username, password))
                .header("Depth", "0")
                .build();

        try (Response response = client.newCall(request).execute()) {
            int code = response.code();
            return code >= 200 && code < 300;
        }
    }

    public void uploadFile(String serverUrl, String username, String password, String fileName, String content) throws Exception {
        RequestBody body = RequestBody.create(content, OCTET_STREAM);
        Request request = new Request.Builder()
                .url(buildUrl(serverUrl, fileName))
                .put(body)
                .header("Authorization", getAuthHeader(username, password))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new Exception("上传失败: HTTP " + response.code() + " " + response.message());
            }
        }
    }

    public String downloadFile(String serverUrl, String username, String password, String fileName) throws Exception {
        Request request = new Request.Builder()
                .url(buildUrl(serverUrl, fileName))
                .get()
                .header("Authorization", getAuthHeader(username, password))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new Exception("下载失败: HTTP " + response.code() + " " + response.message());
            }
            if (response.body() != null) {
                return response.body().string();
            }
            throw new Exception("下载失败: 响应为空");
        }
    }

    public boolean fileExists(String serverUrl, String username, String password, String fileName) throws Exception {
        Request request = new Request.Builder()
                .url(buildUrl(serverUrl, fileName))
                .head()
                .header("Authorization", getAuthHeader(username, password))
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        }
    }

    public void deleteFile(String serverUrl, String username, String password, String fileName) throws Exception {
        Request request = new Request.Builder()
                .url(buildUrl(serverUrl, fileName))
                .delete()
                .header("Authorization", getAuthHeader(username, password))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() && response.code() != 404) {
                throw new Exception("删除失败: HTTP " + response.code());
            }
        }
    }
}
