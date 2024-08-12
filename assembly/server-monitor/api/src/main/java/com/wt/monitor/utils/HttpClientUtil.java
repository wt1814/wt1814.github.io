package com.wt.monitor.utils;

import com.wt.monitor.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

@SuppressWarnings("all")
@Slf4j
public class HttpClientUtil {
    private static PoolingHttpClientConnectionManager cm;
    private static String EMPTY_STR = "";
    private static String UTF_8 = "UTF-8";

    private static Boolean SetTimeOut = true;

    /**
     * 执行post json请求,200返回响应内容，其他状态码返回null
     *
     * @param url
     * @param json
     * @return
     */
    public static CloseableHttpResponse doJsonPost(String url, String json) {
        CloseableHttpResponse response = null;
        try {
            return jsonPost(url, json);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                log.error("http输出流关闭错误:", e.getMessage());
            }
        }
    }

    /**
     * @param url
     * @param json
     * @return
     */
    public static CloseableHttpResponse jsonPost(String url, String json) {
        HttpPost httpPost = new HttpPost(url);
        if (!StringUtils.isEmpty(json)) {
            StringEntity stringEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
            httpPost.setEntity(stringEntity);
        }
        CloseableHttpClient httpClient = getHttpClient();
        setRequestConfig(httpPost);
        try {
            return httpClient.execute(httpPost);
        } catch (IOException e) {
            log.error("error:", e);
        }
        return null;
    }

    public static String sendGet(String url, String cookie) {
        String result = "";
        BufferedReader in = null;
        try {
            String urlName = url;
            URL realUrl = new URL(urlName);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            if (!StringUtils.isEmpty(cookie)) {
                conn.setRequestProperty("cookie", cookie);
            }

            // 建立实际的连接
            conn.connect();

            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            log.error("发送GET请求出现异常：" + e);
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                log.error("http输出流关闭错误:", e.getMessage());
            }
        }
        return result;
    }

    /**
     * 通过连接池获取HttpClient
     *
     * @return
     */
    private static CloseableHttpClient getHttpClient() {
        init();
        return HttpClients.custom().setConnectionManager(cm).build();
    }

    private static void init() {
        if (cm == null) {
            cm = new PoolingHttpClientConnectionManager();
            cm.setMaxTotal(AppConfig.poolMaxTotal);// 整个连接池最大连接数
            cm.setDefaultMaxPerRoute(AppConfig.defaultMaxPerRoute);// 每路由最大连接数，默认值是2
        }
    }

    private static void setRequestConfig(HttpRequestBase request) {
        RequestConfig requestConfig = RequestConfig
                .custom()
                .setConnectTimeout(AppConfig.connectTimeout)// 设置连接超时时间，单位毫秒
                .setConnectionRequestTimeout(AppConfig.connectionRequestTimeout)// 设置从connectManager获取Connection超时时间，单位毫秒
                .setSocketTimeout(AppConfig.socketTimeout)
                .build();// 请求获取数据的超时时间，单位毫秒
        request.setConfig(requestConfig);
    }
}