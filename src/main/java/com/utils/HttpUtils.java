package com.utils;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class HttpUtils {
    private static final Logger logger = Logger.getLogger(HttpUtils.class);

    public static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:32.0) Gecko/20100101 Firefox/33.0";
    public static final String MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; Android 4.4.4; HTC One_M8 Build/KTU84P) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.93 Mobile Safari/537.36";
    public static final String UTF_8 = "UTF-8";
    public static final String GBK = "GBK";
    public static final String IEPROXY_IP = "test";
    public static final int IEPROXY_PORT = 8888;
    public static final String FIDDLER_IP = "127.0.0.1";
    public static final int FIDDLER_PORT = 9900;
    public static final HttpHost PROXY_FIDDLER = new HttpHost(FIDDLER_IP, FIDDLER_PORT, "http");
    public static boolean userFiddler = false;
    private static RequestConfig DEFAULT_REQUEST_CONFIG = null;
    // 超时时间=60S
    private static int timeout = NumberUtils.toInt("60") * 1000;
    // 默认路由最大连接数=20
    private static int defaultMaxPerRoute = NumberUtils.toInt("20");

    // 执行post请求
    public static void executePost(CloseableHttpClient client, String url) throws ClientProtocolException, IOException {
        executePost(client, url, null);
    }

    public static String executePostWithResult(CloseableHttpClient client, HttpPost post)
            throws ClientProtocolException, IOException {
        CloseableHttpResponse resp = client.execute(post);
        String result = EntityUtils.toString(resp.getEntity());
        resp.close();
        return result;
    }

    public static Map<String, Object> executePostWithHead(CloseableHttpClient client, HttpPost post)
            throws ClientProtocolException, IOException {
        Map<String, Object> map = new HashMap<String, Object>();
        CloseableHttpResponse resp = client.execute(post);
        String result = EntityUtils.toString(resp.getEntity());
        map.put("result", result);
        map.put("url", getLocationFromHeader(resp));
        resp.close();
        return map;
    }

    public static String executePostWithResult(CloseableHttpClient client, HttpPost post, String encoding)
            throws ClientProtocolException, IOException {
        CloseableHttpResponse resp = client.execute(post);
        String result = EntityUtils.toString(resp.getEntity(), encoding);
        resp.close();
        return result;
    }

    public static String executePostWithResult(CloseableHttpClient client, String url, Map<String, Object> params)
            throws ClientProtocolException, IOException {
        return executePostWithResult(client, url, params, HttpUtils.UTF_8);
    }

    public static String executePostHeaderWithResult(CloseableHttpClient client, String url, Map<String, Object> params,
            Map<String, String> headersMap) throws ClientProtocolException, IOException {
        HttpPost post = postHeader(url, params, headersMap);
        CloseableHttpResponse resp = client.execute(post);
        String result = EntityUtils.toString(resp.getEntity(), HttpUtils.UTF_8);
        resp.close();
        return result;
    }

    public static String executePostHeaderObjWithResult(CloseableHttpClient client, String url, String params,
            Map<String, String> headersMap) throws ClientProtocolException, IOException {
        HttpPost post = postHeaderObj(url, params, headersMap);
        CloseableHttpResponse resp = client.execute(post);
        String result = EntityUtils.toString(resp.getEntity(), HttpUtils.UTF_8);
        resp.close();
        return result;
    }

    public static String executePostWithResult(CloseableHttpClient client, String url, Map<String, Object> params,
            String charset) throws ClientProtocolException, IOException {
        HttpPost post = params == null ? post(url) : post(url, params);
        CloseableHttpResponse resp = client.execute(post);
        String result = EntityUtils.toString(resp.getEntity(), charset);
        resp.close();
        return result;
    }

    public static String excuteResultPost(CloseableHttpClient client, HttpPost post, Object json)
            throws ClientProtocolException, IOException {
        StringEntity entity = new StringEntity(json.toString(), "utf-8");
        post.setEntity(entity);
        CloseableHttpResponse resp = client.execute(post);
        String result = EntityUtils.toString(resp.getEntity(), "UTF-8");
        resp.close();
        return result;
    }

    public static void executePost(CloseableHttpClient client, String url, Map<String, Object> params)
            throws ClientProtocolException, IOException {
        HttpPost post = params == null ? post(url) : post(url, params);
        client.execute(post).close();
    }

    // 执行get请求
    public static void executeGet(CloseableHttpClient client, String url) throws ClientProtocolException, IOException {
        HttpGet get = HttpUtils.get(url);
        client.execute(get).close();
    }

    public static String executeGetWithResult(CloseableHttpClient client, String url)
            throws ClientProtocolException, IOException {
        HttpGet httpGet = get(url);
        CloseableHttpResponse resp = client.execute(httpGet);
        String result = EntityUtils.toString(resp.getEntity());
        resp.close();
        return result;
    }

    public static String executeGetWithResult(CloseableHttpClient client, String url, Map<String, Object> params)
            throws ClientProtocolException, IOException {
        HttpGet httpGet = get(url, params);
        CloseableHttpResponse resp = client.execute(httpGet);
        String result = EntityUtils.toString(resp.getEntity());
        resp.close();
        return result;
    }

    public static String executeGetWithResult(CloseableHttpClient client, HttpGet get, String encoding)
            throws ClientProtocolException, IOException {
        CloseableHttpResponse resp = client.execute(get);
        String result = EntityUtils.toString(resp.getEntity(), encoding);
        resp.close();
        return result;
    }

    // 获取post
    public static HttpPost post(String url) {
        return post(url, null);
    }

    public static HttpPost post(String url, Map<String, Object> params) {
        return post(url, params, null);
    }

    public static HttpPost post(String url, Map<String, Object> params, HttpHost proxy) {
        return post(url, params, proxy, DEFAULT_USER_AGENT);// DEFAULT_USER_AGENT默认的用户代理

    }

    public static HttpPost post(String url, Map<String, Object> params, HttpHost proxy, String userAgent) {
        HttpPost result = new HttpPost(url);
        // result.addHeader("Connection", "close");
        // result.addHeader("User-Agent", userAgent == null ? DEFAULT_USER_AGENT
        // : userAgent);
        if (params != null && !params.isEmpty()) {
            result.setEntity(buildParams(params));
        }
        result.setConfig(copyDefaultConfig().build());// 设置超时时间为60S
        return result;
    }

    public static HttpPost postHeader(String url, Map<String, Object> params, Map<String, String> headersMap) {
        HttpPost post = new HttpPost(url);

        Iterator<String> iterator = headersMap.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            post.addHeader(key, headersMap.get(key));
        }

        // post.addHeader("User-Agent", userAgent == null ? DEFAULT_USER_AGENT :
        // userAgent);
        if (params != null && !params.isEmpty()) {
            post.setEntity(buildParams(params));
        }
        post.setConfig(copyDefaultConfig().build());// 设置超时时间为60S
        return post;
    }

    public static HttpPost postHeaderObj(String url, String params, Map<String, String> headersMap) {
        HttpPost post = new HttpPost(url);

        Iterator<String> iterator = headersMap.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            post.addHeader(key, headersMap.get(key));
        }

        // post.addHeader("User-Agent", userAgent == null ? DEFAULT_USER_AGENT :
        // userAgent);
        if (params != null && !params.isEmpty()) {
            post.setEntity(new StringEntity(params, HttpUtils.UTF_8));
        }
        post.setConfig(copyDefaultConfig().build());// 设置超时时间为60S
        return post;
    }

    // 获取get
    public static HttpGet get(String url) {
        return get(url, null);
    }

    public static HttpGet get(String url, Map<String, Object> params, String userAgent) {
        url += buildParamString(params);
        HttpGet result = new HttpGet(url);
        result.setConfig(copyDefaultConfig().build());
        return result;
    }

    public static HttpGet get(String url, Map<String, Object> params) {
        url += buildParamString(params);
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(copyDefaultConfig().build());
        return httpGet;
    }

    // 请求的配置
    public static RequestConfig.Builder copyDefaultConfig() {
        RequestConfig.Builder builder = RequestConfig.copy(getDefaultRequestConfig());
        // if (userFiddler || Boolean.valueOf(System.getProperty("use.fiddler",
        // "false"))) {
        // builder.setProxy(PROXY_FIDDLER);
        // }
        return builder;
    }

    public static RequestConfig getDefaultRequestConfig() {
        if (DEFAULT_REQUEST_CONFIG == null) {
            synchronized (HttpUtils.class) {
                if (DEFAULT_REQUEST_CONFIG == null) {
                    RequestConfig.Builder builder = RequestConfig.custom();
                    // builder.setRedirectsEnabled(false).setRelativeRedirectsAllowed(false);
                    // builder.setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY);
                    // connect to a url 1min
                    builder.setConnectTimeout(timeout);
                    // socket inputstream.read() 2min
                    builder.setSocketTimeout(timeout * 2);
                    DEFAULT_REQUEST_CONFIG = builder.build();
                }
            }
        }
        return DEFAULT_REQUEST_CONFIG;
    }

    // post方法，转化参数
    public static UrlEncodedFormEntity buildParams(Map<String, ? extends Object> params) {
        return buildParams(params, UTF_8);
    }

    @SuppressWarnings("rawtypes")
    public static UrlEncodedFormEntity buildParams(Map<String, ? extends Object> params, String encoding) {
        if (params == null || params.isEmpty()) {
            return null;
        }
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        for (Entry<String, ? extends Object> entry : params.entrySet()) {
            Object value = entry.getValue();
            if (value != null) {
                if (value instanceof List) {
                    for (Object o : (List) value) {
                        if (o != null) {
                            parameters.add(new BasicNameValuePair(entry.getKey(), o.toString()));
                        }
                    }
                } else {
                    parameters.add(new BasicNameValuePair(entry.getKey(), value.toString()));
                }
            } else {
                parameters.add(new BasicNameValuePair(entry.getKey(), null));
            }
        }
        return new UrlEncodedFormEntity(parameters, Charset.forName(encoding));
    }

    // get方法，转化参数
    public static String buildParamString(Map<String, ? extends Object> params) {
        return buildParamString(params, UTF_8);
    }

    public static String buildParamString(Map<String, ? extends Object> params, String encoding) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try {
            for (Entry<String, ? extends Object> entry : params.entrySet()) {
                Object value = entry.getValue();
                value = value == null ? "" : value.toString();
                sb.append("&").append(URLEncoder.encode(entry.getKey(), encoding)).append("=")
                        .append(URLEncoder.encode((String) value, encoding));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        sb.deleteCharAt(0).insert(0, "?");
        return sb.toString();
    }

    // 获取httpclient
    public static CloseableHttpClient getHttpClient() {
        HttpClientBuilder builder = HttpClientBuilder.create();
        // builder.setProxy(PROXY_FIDDLER);
        CloseableHttpClient httpclient = builder.build();
        return httpclient;
    }

    public static CloseableHttpClient getHttpClient_1() {
        return getHttpClient(false, null);
    }

    public static CloseableHttpClient getHttpClient(boolean trustAllSSL) {
        return getHttpClient(trustAllSSL, null);
    }

    public static CloseableHttpClient getHttpClient(SSLConnectionSocketFactory sslcsf, CookieStore cookieStore) {
        HttpClientBuilder builder = getBuilder();
        if (cookieStore != null) {
            builder.setDefaultCookieStore(cookieStore);
        }
        builder.setSSLSocketFactory(sslcsf);
        return builder.build();
    }

    public static CloseableHttpClient getHttpClient(boolean trustAllSSL, CookieStore cookieStore) {
        HttpClientBuilder builder = getBuilder();
        if (cookieStore != null) {
            builder.setDefaultCookieStore(cookieStore);
        }
        if (trustAllSSL) {
            builder.setSSLSocketFactory(SSLUtils.TRUAT_ALL_SSLSF);
        }
        return builder.build();
    }

    /**
     * 使用代理
     * 
     * @param trustAllSSL
     * @param cookieStore
     * @param isProxy
     * @return
     */
    public static CloseableHttpClient getHttpClient_Proxy(boolean trustAllSSL, CookieStore cookieStore,
            boolean isProxy) {

        HttpClientBuilder builder = null;
        if (isProxy) {
            builder = getProxyBuilder();
        } else {
            builder = getBuilder();
        }
        if (cookieStore != null) {
            builder.setDefaultCookieStore(cookieStore);
        }
        if (trustAllSSL) {
            builder.setSSLSocketFactory(SSLUtils.TRUAT_ALL_SSLSF);
        }
        return builder.build();
    }

    public static CloseableHttpClient getHttpClient(CookieStore cookieStore) {
        HttpClientBuilder builder = getProxyBuilder();
        if (cookieStore != null) {
            builder.setDefaultCookieStore(cookieStore);
        }
        builder.setSSLSocketFactory(SSLUtils.TRUAT_ALL_SSLSF);
        return builder.build();
    }

    public static CloseableHttpClient getHttpClient(boolean trustAllSSL, CookieStore cookieStore, boolean isFiddler) {
        if (isFiddler) {
            userFiddler = true;
        }
        HttpClientBuilder builder = getBuilder();
        if (cookieStore != null) {
            builder.setDefaultCookieStore(cookieStore);
        }
        if (trustAllSSL) {
            builder.setSSLSocketFactory(SSLUtils.TRUAT_ALL_SSLSF);
        }
        return builder.build();
    }

    private static HttpClientBuilder getBuilder() {
        HttpClientBuilder builder = HttpClients.custom();
        if (userFiddler || Boolean.valueOf(System.getProperty("use.fiddler", "false"))) {
            builder.setProxy(PROXY_FIDDLER);
        }
        // builder.setMaxConnPerRoute(defaultMaxPerRoute);
        // builder.setMaxConnTotal(defaultMaxPerRoute * 2);
        // builder.disableAutomaticRetries();// 禁用retry
        builder.setRetryHandler(new DefaultHttpRequestRetryHandler(1, false));// 重试一次

        // X509HostnameVerifier hostnameVerifier =
        // SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER;
        LayeredConnectionSocketFactory sslSocketFactory = SSLUtils.TRUAT_ALL_SSLSF;
        // LayeredConnectionSocketFactory sslSocketFactory = new
        // SSLConnectionSocketFactory(SSLContexts.createDefault(),hostnameVerifier);
        final PoolingHttpClientConnectionManager poolingmgr = new PoolingHttpClientConnectionManager(RegistryBuilder
                .<ConnectionSocketFactory> create().register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslSocketFactory).build());
        poolingmgr.closeIdleConnections(0, TimeUnit.SECONDS);
        poolingmgr.setDefaultMaxPerRoute(defaultMaxPerRoute); // 将每个路由基础的连接增加
        poolingmgr.setMaxTotal(defaultMaxPerRoute * 4); // 将目标主机的最大连接数增加
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        // 2min socket read
        requestConfigBuilder.setSocketTimeout(timeout * 2);
        // 1min connect to a url
        requestConfigBuilder.setConnectTimeout(timeout);
        // 30s get a connection from pool
        requestConfigBuilder.setConnectionRequestTimeout(timeout / 2);

        builder.setDefaultRequestConfig(requestConfigBuilder.build());
        builder.setConnectionManager(poolingmgr);
        // 链接保持策略
        ConnectionKeepAliveStrategy myStrategy = new ConnectionKeepAliveStrategy() {
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                // Honor 'keep-alive' header
                HeaderElementIterator it = new BasicHeaderElementIterator(
                        response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                while (it.hasNext()) {
                    HeaderElement he = it.nextElement();
                    String param = he.getName();
                    String value = he.getValue();
                    if (value != null && param.equalsIgnoreCase("timeout")) {
                        try {
                            return Long.parseLong(value) * 1000;
                        } catch (NumberFormatException ignore) {
                        }
                    }
                }
                // keep alive for 120 seconds
                return 120 * 1000;
            }
        };
        builder.setKeepAliveStrategy(myStrategy);
        return builder;
    }

    private static HttpClientBuilder getProxyBuilder() {
        HttpClientBuilder builder = HttpClients.custom();
        // HttpHost proxy = new HttpHost("127.0.0.1", 8888);
        // builder.setProxy(ProxyUtils.getProxyHost());
        return builder;
    }

    /*
     * @author 
     * 
     * @param response
     * 
     * @param name
     * 
     * @param encode
     * 
     * @return
     */
    public static String getHeader(CloseableHttpResponse response, String name) {
        Header[] headers = response.getHeaders(name);
        if (headers.length > 0) {
            return headers[0].getValue();
        }
        return null;
    }

    /**
     * 从header里获取Location
     * 
     * @author 
     * @param response
     * @return
     */
    public static String getLocationFromHeader(CloseableHttpResponse response) {
        return getLocationFromHeader(response, false);
    }

    public static String getLocationFromHeader(CloseableHttpResponse response, boolean closeResponse) {
        String result = getHeader(response, "Location");
        if (closeResponse) {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static String getLocationFromHeader(CloseableHttpClient client, String url) {
        return getLocationFromHeader(client, url, null, false);
    }

    public static String getLocationFromHeader(CloseableHttpClient client, String url, Map<String, Object> params) {
        return getLocationFromHeader(client, url, params, false);
    }

    public static String getLocationFromHeader(CloseableHttpClient client, String url, Map<String, Object> params,
            boolean isPost) {
        CloseableHttpResponse response;
        try {
            HttpPost request = null;
            if (isPost) {
                request = post(url, params);
                response = client.execute(request);

            } else {
                HttpGet get = get(url);
                response = client.execute(get);
            }
            return getLocationFromHeader(response, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @author 
     * @param name
     * @param value
     * @param path
     * @param domain
     * @return
     */
    public static BasicClientCookie getCookie(String name, String value, String domain, String path) {
        BasicClientCookie clientCookie = new BasicClientCookie(name, value);
        clientCookie.setDomain(domain);
        clientCookie.setPath(path);
        return clientCookie;
    }

    public static String getFirstCookie(CookieStore cookieStore, String name) {
        List<String> values = getCookie(cookieStore, name);
        return values.isEmpty() ? null : values.get(0);
    }

    public static List<String> getCookie(CookieStore cookieStore, String name) {
        List<String> result = new ArrayList<>();
        if (cookieStore == null) {
            return result;
        }
        for (Cookie cookie : cookieStore.getCookies()) {
            if (name.equals(cookie.getName())) {
                result.add(cookie.getValue());
            }
        }
        return result;
    }

    public static void printCookies(CookieStore cookieStore) {
        for (Cookie cookie : cookieStore.getCookies()) {
            System.out.println(cookie.toString());
        }
    }

    /**
     * @author 
     * @param cookieStore
     * @param driver
     */
    public static void copyCookies(CookieStore cookieStore, WebDriver driver) {
        copyCookies(cookieStore, driver, false);
    }

    /**
     * from cookieStore to driver
     * 
     * @author 
     * @param cookieStore
     * @param driver
     * @param clear
     *            true driver.manage().deleteAllCookies();
     */
    public static void copyCookies(CookieStore cookieStore, WebDriver driver, boolean clear) {
        if (clear) {
            driver.manage().deleteAllCookies();
        }
        for (org.apache.http.cookie.Cookie cookie : cookieStore.getCookies()) {
            try {
                driver.manage().addCookie(new org.openqa.selenium.Cookie(cookie.getName(), cookie.getValue(),
                        cookie.getDomain(), cookie.getPath(), cookie.getExpiryDate()));
            } catch (Exception e) {
                System.out.println(cookie.toString());
                e.printStackTrace();
            }
        }
    }

    /**
     * from cookies to cookieStore
     * 
     * @author 
     * @param cookies
     * @param cookieStore
     */
    public static void copyCookies(Set<org.openqa.selenium.Cookie> cookies, CookieStore cookieStore) {
        copyCookies(cookies, cookieStore, false);
    }

    /**
     * from cookies to cookieStore
     * 
     * @author 
     * @param cookies
     * @param cookieStore
     * @param clear
     *            true cookieStore.clear();
     */
    public static void copyCookies(Set<org.openqa.selenium.Cookie> cookies, CookieStore cookieStore, boolean clear) {
        if (clear) {
            cookieStore.clear();
        }
        for (org.openqa.selenium.Cookie cookie : cookies) {
            BasicClientCookie basicClientCookie = new BasicClientCookie(cookie.getName(), cookie.getValue());
            basicClientCookie.setDomain(cookie.getDomain());
            basicClientCookie.setPath(cookie.getPath());
            // basicClientCookie.setExpiryDate(cookie.getExpiry());
            cookieStore.addCookie(basicClientCookie);
        }
    }

    public static WebDriver getWebDriver() {
        return getWebDriver(null);
    }

    public static WebDriver getWebDriver(String useragent) {
        WebDriver result = null;
        if (PropertiesUtil.getProp("webdriver").equals("firefox")) {
            if (useragent != null) {
                FirefoxProfile firefoxProfile = new FirefoxProfile();// 火狐的配置文件
                firefoxProfile.setPreference("general.useragent.override", useragent);
                result = new FirefoxDriver(firefoxProfile);
            } else {
                result = new FirefoxDriver();
            }

        } else {
            // ChromeDriver 使用
            System.setProperty(PropertiesUtil.getProp("webdriver_lib"), PropertiesUtil.getProp("webdriver_path"));
            result = new ChromeDriver();
        }
        result.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        return result;

    }

    private static final FirefoxProfile DEFAULT_FIREFOX_PROFILE = new FirefoxProfile();

    static {
        DEFAULT_FIREFOX_PROFILE.setPreference("permissions.default.image", 2);
    }

    public static FirefoxDriver getFirefoxDriver() {
        return getFirefoxDriver(DEFAULT_FIREFOX_PROFILE);
    }

    public static FirefoxDriver getFoxDriverForImage(boolean isLoadImage) {
        if (isLoadImage) {
            FirefoxProfile firefoxProfile = new FirefoxProfile();
            firefoxProfile.setPreference("permissions.default.image", 1);
            return new FirefoxDriver(firefoxProfile);
        }
        return getFirefoxDriver(DEFAULT_FIREFOX_PROFILE);
    }

    public static FirefoxDriver getFirefoxDriver(FirefoxProfile firefoxProfile) {
        if (firefoxProfile != null) {
            return new FirefoxDriver(firefoxProfile);
        }
        return new FirefoxDriver();
    }

    /**
     * 更换URL中的特殊符号
     * 
     * @author liuheli
     * @param url
     * @return
     */
    public static String replaceUrlChara(String url) {
        url = url.replace("|", "%124");
        return url;
    }

    public static String resolvePath(String src, String url) {
        try {
            return new URL(src).toURI().resolve(url).toString();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return url;
    }

    public static void addAjaxRequest(HttpPost post) {
        post.addHeader("X-Requested-With", "XMLHttpRequest");
        post.addHeader("Accept", "application/xml, text/xml, */*");
    }

    public static void addAjaxRequest(HttpGet get) {
        get.addHeader("X-Requested-With", "XMLHttpRequest");
        get.addHeader("Accept", "application/xml, text/xml, */*");
    }

    /**
     * from cookies to cookieStore
     * 
     * @param cookies
     * @param cookieStore
     */
    public static void copyUnitCookies(Set<com.gargoylesoftware.htmlunit.util.Cookie> cookies,
            CookieStore cookieStore) {
        copyUnitCookies(cookies, cookieStore, false);
    }

    /**
     * from cookies to cookieStore
     * 
     * @param cookies
     * @param cookieStore
     * @param clear
     *            true cookieStore.clear();
     */
    public static void copyUnitCookies(Set<com.gargoylesoftware.htmlunit.util.Cookie> cookies, CookieStore cookieStore,
            boolean clear) {
        if (clear) {
            cookieStore.clear();
        }
        for (com.gargoylesoftware.htmlunit.util.Cookie cookie : cookies) {
            BasicClientCookie basicClientCookie = new BasicClientCookie(cookie.getName(), cookie.getValue());
            basicClientCookie.setDomain(cookie.getDomain());
            basicClientCookie.setPath(cookie.getPath());
            // basicClientCookie.setExpiryDate(cookie.getExpiry());
            cookieStore.addCookie(basicClientCookie);
        }
    }

    /**
     * 转换CookieStore
     * 
     * @author wangfukun
     * @param cookie
     * @return
     */
    public static CookieStore processCookies(String cookie) {
        CookieStore cookieStore = new BasicCookieStore();
        try {
            @SuppressWarnings("unchecked")
            List<Object> object = (List<Object>) JSONArray.parse(cookie);
            for (int i = 0; i < object.size(); i++) {
                JSONObject s = (JSONObject) JSONObject.parse(object.get(i).toString());
                BasicClientCookie basicCookie = new BasicClientCookie(s.get("name").toString(),
                        s.get("value").toString());
                basicCookie.setDomain(s.get("domain").toString());
                basicCookie.setPath(s.get("path").toString());
                cookieStore.addCookie(basicCookie);
            }
        } catch (Exception e1) {
            logger.info(e1.getMessage());
        }
        HttpUtils.printCookies(cookieStore);
        return cookieStore;
    }

    /**
     * 转换cookie
     * 
     * @author wangfukun
     * @param cookie
     *            ，按"\t"分割
     * @return
     * @throws Exception
     */
    public static List<com.gargoylesoftware.htmlunit.util.Cookie> initCookies(String cookie) {
        logger.info("cookie------>" + cookie);
        List<com.gargoylesoftware.htmlunit.util.Cookie> list = new ArrayList<com.gargoylesoftware.htmlunit.util.Cookie>();
        try {
            String[] arrays = cookie.split("\t");
            String[] arrs = null;
            for (int j = 0; j < arrays.length; j++) {
                String domain = "";
                String cookiesKey = "";
                arrs = arrays[j].split(";");
                if (arrs.length <= 1) {
                    continue;
                }
                for (int i = 0; i < arrs.length; i++) {
                    if (arrs[i].contains("domain")) {
                        domain = arrs[i].replace("domain=", "").trim();
                    } else if (arrs[i].contains("expires")) {
                        continue;
                    } else if (arrs[i].contains("path")) {
                        continue;
                    } else {
                        cookiesKey = arrs[i];
                    }
                }
                String name = cookiesKey.substring(0, cookiesKey.indexOf("="));
                String value = cookiesKey.substring(cookiesKey.indexOf("=") + 1, cookiesKey.length());
                com.gargoylesoftware.htmlunit.util.Cookie c = new com.gargoylesoftware.htmlunit.util.Cookie(domain,
                        name, value);
                list.add(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static DesiredCapabilities getWebDriverByProxy() {
        String PROXY = IEPROXY_IP + ":" + IEPROXY_PORT;
        Proxy proxy = new Proxy();
        proxy.setHttpProxy(PROXY);
        proxy.setFtpProxy(PROXY);
        proxy.setSslProxy(PROXY);
        DesiredCapabilities cap = new DesiredCapabilities();
        cap.setCapability(CapabilityType.PROXY, proxy);
        return cap;
    }

    public static HttpPost buildPostFromHtml(String html) {
        return buildPostFromHtml(html, "form");
    }

    public static HttpPost buildPostFromHtml(String html, String selector) {
        return buildPostFromHtml(html, selector, HttpUtils.GBK);
    }

    public static HttpPost buildPostFromHtml(String html, String selector, String charSet) {
        Document document = Jsoup.parse(html, charSet == null ? HttpUtils.GBK : charSet);
        Elements elements = document.select(selector);
        if (elements.size() > 0) {
            Element form = elements.get(0);
            String url = form.attr("action");
            Elements inputs = form.select("input[type=hidden]");
            Map<String, Object> params = new HashMap<>();
            for (int i = 0; i < inputs.size(); i++) {
                params.put(inputs.get(i).attr("name"), inputs.get(i).attr("value"));
            }
            return HttpUtils.post(url, params);
        }
        return null;
    }

    public static Map<String, Object> getFormUrlAndParamsFromHtml(String html, String selector) {
        return getFormUrlAndParamsFromHtml(html, selector, HttpUtils.GBK);
    }

    public static Map<String, Object> getFormUrlAndParamsFromHtml(String html, String selector, String charSet) {
        Document document = Jsoup.parse(html, charSet == null ? HttpUtils.GBK : charSet);
        Elements elements = document.select(selector);
        if (elements.size() > 0) {
            Element form = elements.get(0);
            String url = form.attr("action");
            Elements inputs = form.select("input[type=hidden]");
            Map<String, Object> params = new HashMap<>();
            for (int i = 0; i < inputs.size(); i++) {
                params.put(inputs.get(i).attr("name"), inputs.get(i).attr("value"));
            }
            Map<String, Object> result = new HashMap<>();
            result.put("url", url);
            result.put("params", params);
            return result;
        }
        return null;
    }

    /**
     * 获取input[type=hidden]
     * 
     * @author 
     * @param html
     * @return
     */
    public static Map<String, Object> buildHiddenInputParamsFromHtml(String html) {
        return buildHiddenInputParamsFromHtml(html, HttpUtils.GBK);
    }

    /**
     * 获取input[type=hidden]
     * 
     * @author 
     * @param html
     * @param charSet
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> buildHiddenInputParamsFromHtml(String html, String charSet) {
        Document document = Jsoup.parse(html, charSet == null ? HttpUtils.GBK : charSet);
        Elements inputs = document.select("input[type=hidden]");
        Map<String, Object> params = new HashMap<>();
        for (int i = 0; i < inputs.size(); i++) {
            String name = inputs.get(i).attr("name");
            String value = inputs.get(i).attr("value");
            if (params.get(name) != null) {
                Object v = params.get(name);
                if (v instanceof List) {
                    ((List<Object>) v).add(value);
                } else {
                    List<Object> l = new ArrayList<>();
                    l.add(v);
                    l.add(value);
                    params.put(name, l);
                }
            } else {
                params.put(name, value);
            }
        }
        return params;
    }

    public static Map<String, Object> buildParamsFromHtml(String html, String selector) {
        return buildParamsFromHtml(html, selector, HttpUtils.GBK);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> buildParamsFromHtml(String html, String selector, String charSet) {
        Document document = Jsoup.parse(html, charSet == null ? HttpUtils.GBK : charSet);
        Elements elements = document.select(selector);
        if (elements.size() > 0) {
            Element form = elements.get(0);
            Elements inputs = form.select("input[type=hidden]");
            Map<String, Object> params = new HashMap<>();
            for (int i = 0; i < inputs.size(); i++) {
                String name = inputs.get(i).attr("name");
                String value = inputs.get(i).attr("value");
                if (params.get(name) != null) {
                    Object v = params.get(name);
                    if (v instanceof List) {
                        ((List<Object>) v).add(value);
                    } else {
                        List<Object> l = new ArrayList<>();
                        l.add(v);
                        l.add(value);
                        params.put(name, l);
                    }
                } else {
                    params.put(name, value);
                }
            }
            return params;
        }
        return new HashMap<>();
    }

    public static String getCharsetFromContentType(String contentType) {
        if (StringUtils.isBlank(contentType)) {
            return null;
        }
        String[] cts = contentType.toLowerCase().split(";");
        for (String s : cts) {
            if (StringUtils.isNotBlank(s) && s.contains("charset")) {
                return s.split("=")[1];
            }
        }
        return null;
    }

    /**
     * 去掉url中的路径，留下请求参数部分
     * 
     * @param strURL
     *            url地址
     * @return url请求参数部分
     */
    private static String TruncateUrlPage(String strURL) {
        String strAllParam = null;
        String[] arrSplit = null;

        strURL = strURL.trim().toLowerCase();

        arrSplit = strURL.split("[?]");
        if (strURL.length() > 1) {
            if (arrSplit.length > 1) {
                if (arrSplit[1] != null) {
                    strAllParam = arrSplit[1];
                }
            }
        }

        return strAllParam;
    }

    /**
     * 解析出url参数中的键值对 如 "index.jsp?Action=del&id=123"，解析出Action:del,id:123存入map中
     * 
     * @param URL
     *            url地址
     * @return url请求参数部分
     */
    public static Map<String, String> URLRequest(String URL) {
        Map<String, String> mapRequest = new HashMap<String, String>();

        String[] arrSplit = null;

        String strUrlParam = TruncateUrlPage(URL);
        if (strUrlParam == null) {
            return mapRequest;
        }
        // 每个键值为一组
        arrSplit = strUrlParam.split("[&]");
        for (String strSplit : arrSplit) {
            String[] arrSplitEqual = null;
            arrSplitEqual = strSplit.split("[=]");

            // 解析出键值
            if (arrSplitEqual.length > 1) {
                // 正确解析
                mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);

            } else {
                if (arrSplitEqual[0] != "") {
                    // 只有参数没有值，不加入
                    mapRequest.put(arrSplitEqual[0], "");
                }
            }
        }
        return mapRequest;
    }

}
