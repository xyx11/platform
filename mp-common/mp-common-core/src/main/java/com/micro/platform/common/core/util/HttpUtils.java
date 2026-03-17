package com.micro.platform.common.core.util;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * HTTP 工具类
 */
public class HttpUtils {

    private static final int DEFAULT_TIMEOUT = 30000;
    private static final String DEFAULT_CHARSET = "UTF-8";

    /**
     * 发送 GET 请求
     */
    public static String get(String url) {
        return get(url, null, null, DEFAULT_TIMEOUT);
    }

    /**
     * 发送 GET 请求（带参数）
     */
    public static String get(String url, Map<String, String> params) {
        return get(url, params, null, DEFAULT_TIMEOUT);
    }

    /**
     * 发送 GET 请求（带参数和请求头）
     */
    public static String get(String url, Map<String, String> params, Map<String, String> headers) {
        return get(url, params, headers, DEFAULT_TIMEOUT);
    }

    /**
     * 发送 GET 请求（完整参数）
     */
    public static String get(String url, Map<String, String> params, Map<String, String> headers, int timeout) {
        try {
            if (params != null && !params.isEmpty()) {
                String queryString = buildQueryString(params);
                url = url + (url.contains("?") ? "&" : "?") + queryString;
            }

            HttpURLConnection connection = createConnection(url, "GET", headers, timeout);
            return readResponse(connection);
        } catch (Exception e) {
            throw new RuntimeException("HTTP GET request failed: " + e.getMessage(), e);
        }
    }

    /**
     * 发送 POST 请求
     */
    public static String post(String url, String body) {
        return post(url, body, null, DEFAULT_TIMEOUT);
    }

    /**
     * 发送 POST 请求（带请求头）
     */
    public static String post(String url, String body, Map<String, String> headers) {
        return post(url, body, headers, DEFAULT_TIMEOUT);
    }

    /**
     * 发送 POST 请求（完整参数）
     */
    public static String post(String url, String body, Map<String, String> headers, int timeout) {
        try {
            HttpURLConnection connection = createConnection(url, "POST", headers, timeout);
            connection.setDoOutput(true);

            if (body != null && !body.isEmpty()) {
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = body.getBytes(DEFAULT_CHARSET);
                    os.write(input, 0, input.length);
                }
            }

            return readResponse(connection);
        } catch (Exception e) {
            throw new RuntimeException("HTTP POST request failed: " + e.getMessage(), e);
        }
    }

    /**
     * 发送 POST 请求（表单数据）
     */
    public static String postForm(String url, Map<String, String> params) {
        return postForm(url, params, null, DEFAULT_TIMEOUT);
    }

    /**
     * 发送 POST 请求（表单数据）
     */
    public static String postForm(String url, Map<String, String> params, Map<String, String> headers, int timeout) {
        try {
            HttpURLConnection connection = createConnection(url, "POST", headers, timeout);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            if (params != null && !params.isEmpty()) {
                String formData = buildQueryString(params);
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = formData.getBytes(DEFAULT_CHARSET);
                    os.write(input, 0, input.length);
                }
            }

            return readResponse(connection);
        } catch (Exception e) {
            throw new RuntimeException("HTTP POST form request failed: " + e.getMessage(), e);
        }
    }

    /**
     * 发送 PUT 请求
     */
    public static String put(String url, String body) {
        return put(url, body, null, DEFAULT_TIMEOUT);
    }

    /**
     * 发送 PUT 请求（带请求头）
     */
    public static String put(String url, String body, Map<String, String> headers) {
        return put(url, body, headers, DEFAULT_TIMEOUT);
    }

    /**
     * 发送 PUT 请求（完整参数）
     */
    public static String put(String url, String body, Map<String, String> headers, int timeout) {
        try {
            HttpURLConnection connection = createConnection(url, "PUT", headers, timeout);
            connection.setDoOutput(true);

            if (body != null && !body.isEmpty()) {
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = body.getBytes(DEFAULT_CHARSET);
                    os.write(input, 0, input.length);
                }
            }

            return readResponse(connection);
        } catch (Exception e) {
            throw new RuntimeException("HTTP PUT request failed: " + e.getMessage(), e);
        }
    }

    /**
     * 发送 DELETE 请求
     */
    public static String delete(String url) {
        return delete(url, null, DEFAULT_TIMEOUT);
    }

    /**
     * 发送 DELETE 请求（带请求头）
     */
    public static String delete(String url, Map<String, String> headers) {
        return delete(url, headers, DEFAULT_TIMEOUT);
    }

    /**
     * 发送 DELETE 请求（完整参数）
     */
    public static String delete(String url, Map<String, String> headers, int timeout) {
        try {
            HttpURLConnection connection = createConnection(url, "DELETE", headers, timeout);
            return readResponse(connection);
        } catch (Exception e) {
            throw new RuntimeException("HTTP DELETE request failed: " + e.getMessage(), e);
        }
    }

    /**
     * 创建 HTTP 连接
     */
    private static HttpURLConnection createConnection(String url, String method, Map<String, String> headers, int timeout) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);
        connection.setUseCaches(false);
        connection.setRequestProperty("Accept-Charset", DEFAULT_CHARSET);

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }

        return connection;
    }

    /**
     * 读取响应
     */
    private static String readResponse(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        InputStream inputStream = (responseCode >= 200 && responseCode < 300)
                ? connection.getInputStream()
                : connection.getErrorStream();

        if (inputStream == null) {
            return "";
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, DEFAULT_CHARSET))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    /**
     * 构建查询字符串
     */
    private static String buildQueryString(Map<String, String> params) {
        return params.entrySet().stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));
    }

    /**
     * URL 编码
     */
    public static String encode(String value) {
        try {
            return URLEncoder.encode(value, DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    /**
     * URL 解码
     */
    public static String decode(String value) {
        try {
            return URLDecoder.decode(value, DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    /**
     * 检查 URL 是否可访问
     */
    public static boolean isReachable(String url, int timeout) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(timeout);
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            return (responseCode >= 200 && responseCode < 400);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查 URL 是否可访问
     */
    public static boolean isReachable(String url) {
        return isReachable(url, 5000);
    }

    /**
     * 获取响应状态码
     */
    public static int getStatusCode(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            return connection.getResponseCode();
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * 下载文件
     */
    public static void downloadFile(String fileUrl, String savePath) throws IOException {
        URL url = new URL(fileUrl);
        try (InputStream in = url.openStream();
             FileOutputStream out = new FileOutputStream(savePath)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    /**
     * 上传文件（multipart/form-data）
     */
    public static String uploadFile(String url, String paramName, File file, Map<String, String> params) throws IOException {
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream os = connection.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8), true)) {

            // 添加其他表单字段
            if (params != null) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    writer.append("--").append(boundary).append("\r\n");
                    writer.append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"\r\n\r\n");
                    writer.append(entry.getValue()).append("\r\n");
                    writer.flush();
                }
            }

            // 添加文件
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"").append(paramName).append("\"; filename=\"").append(file.getName()).append("\"\r\n");
            writer.append("Content-Type: application/octet-stream\r\n\r\n");
            writer.flush();

            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }

            writer.append("\r\n").flush();
            writer.append("--").append(boundary).append("--\r\n");
            writer.flush();
        }

        return readResponse(connection);
    }
}