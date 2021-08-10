package be.spacebel.metadataeditor.utils;

import be.spacebel.metadataeditor.business.AuthenticationException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

/**
 * HTTP client utilities
 *
 * @author mng
 */
public class HttpInvoker {

    private final static Logger log = Logger.getLogger(HttpInvoker.class);

    public static final String DETAILS_URL = "url";
    public static final String DETAILS_RESPONSE = "response";
    public static final String DETAILS_ERROR_CODE = "errorCode";
    public static final String DETAILS_ERROR_MSG = "errorMsg";
    public static final String HTTP_CODE = "httpCode";    

    public static String httpGET(String location, Map<String, String> details) throws IOException {
        return httpGET(location, null, null, details);
    }

    public static String httpGET(String location, String username, String password, Map<String, String> details) throws IOException {
        log.debug("Invoke HTTP GET of URL " + location + ", username = " + username + ")");
        /*
         * Encode the parameter values
         */
        if (StringUtils.isNotEmpty(location)) {
            String baseUrl = StringUtils.substringBefore(location, "?");
            String queryString = StringUtils.substringAfter(location, "?");
            if (StringUtils.isNotEmpty(queryString)) {
                String[] paramArr = StringUtils.split(queryString, "&");
                /*
                 * log.debug("QueryString before parameters values: " +
                 * queryString);
                 */
                queryString = "";

                for (String param : paramArr) {
                    String key = StringUtils.substringBefore(param, "=");
                    String value = StringUtils.substringAfter(param, "=");
                    if (StringUtils.isNotEmpty(value)) {
                        /*
                         * decode the value first
                         */
                        value = URLDecoder.decode(value, "UTF-8");
                        /*
                         * encode again the value
                         */
                        value = URLEncoder.encode(value, "UTF-8");
                    }
                    queryString += key + "=" + value + "&";
                }
                /*
                 * Remove character "&" at the end of the string
                 */
                queryString = queryString.substring(0, queryString.length() - 1);

                location = baseUrl + "?" + queryString;
            }
        }

        URL url = new URL(location);
        CloseableHttpClient httpClient = null;
        String result = null;
        try {
            // disable SNI of Java 7 on runtime to avoid exception
            // unrecognized_name
            //System.setProperty("jsse.enableSNIExtension", "false");

            HttpGet httpGet = new HttpGet(location);
            //httpGet.setHeader("Accept", "application/json");
            int timeout = 2 * 60 * 1000;
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout)
                    .setConnectTimeout(timeout).setConnectionRequestTimeout(timeout).build();
            httpGet.setConfig(requestConfig);

            if (details != null) {
                details.put(DETAILS_URL, location);
            }

            if ("https".equalsIgnoreCase(url.getProtocol())) {
                log.debug("Invoke HTTPS GET: " + location);
                TrustStrategy acceptingTrustStrategy = (X509Certificate[] certificate, String authType) -> true /*
                 * trust all certificates
                         */;
                try {
                    HttpClientBuilder httpBuilder = HttpClients.custom();
                    if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
                        CredentialsProvider credsProvider = new BasicCredentialsProvider();
                        credsProvider.setCredentials(
                                new AuthScope(url.getHost(), url.getPort()),
                                new UsernamePasswordCredentials(username, password));
                        httpBuilder.setDefaultCredentialsProvider(credsProvider);
                    }

                    httpClient = httpBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                            .setSSLContext(new SSLContextBuilder().loadTrustMaterial(acceptingTrustStrategy).build())
                            .build();
                } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
                    String errorMsg = CommonUtils.getErrorMessage(e);
                    log.debug(errorMsg);
                    throw new IOException(errorMsg);
                }
            } else {
                log.debug("Invoke HTTP GET: " + location);
                if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
                    CredentialsProvider credsProvider = new BasicCredentialsProvider();
                    credsProvider.setCredentials(
                            new AuthScope(url.getHost(), url.getPort()),
                            new UsernamePasswordCredentials(username, password));
                    httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
                } else {
                    httpClient = HttpClients.createDefault();
                }
            }

            CloseableHttpResponse response = httpClient.execute(httpGet);
            try {
                int status = response.getStatusLine().getStatusCode();
                HttpEntity entity = response.getEntity();
                String respStr = null;
                if (entity != null) {
                    respStr = EntityUtils.toString(entity);
                    EntityUtils.consume(entity);
                }

                if (status >= 200 && status < 300) {
                    result = respStr;
                } else {
                    if (details != null) {
                        details.put(DETAILS_ERROR_CODE, "" + status);
                        details.put(DETAILS_ERROR_MSG, respStr);
                    }
                }
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        } catch (ConnectTimeoutException e) {
            if (details != null) {
                details.put(DETAILS_ERROR_CODE, "408");
                details.put(DETAILS_ERROR_MSG, "Request Timeout");
            }
        } catch (UnknownHostException e) {
            details.put(DETAILS_ERROR_CODE, "404");
            details.put(DETAILS_ERROR_MSG, "Unknown host: " + e.getMessage());
        } finally {
            if (httpClient != null) {
                httpClient.close();
            }
        }
        //log.debug("result:" + result);
        return result;
    }

    /**
     * Read content of a remote file
     *
     * @param remoteUrl Remote location URL
     * @param mimeType The accept MIME type
     * @return the file content
     *
     * @throws IOException
     */
    public static String readFile(String remoteUrl, String mimeType) throws IOException {
        URL url = new URL(remoteUrl);
        CloseableHttpClient httpClient = null;

        try {
            // disable SNI of Java 7 on runtime to avoid exception
            // unrecognized_name
            //System.setProperty("jsse.enableSNIExtension", "false");

            HttpGet httpGet = new HttpGet(remoteUrl);
            if (StringUtils.isNotEmpty(mimeType)) {
                httpGet.setHeader("Accept", mimeType);
            }
            //
            int timeout = 2 * 60 * 1000;
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout)
                    .setConnectTimeout(timeout).setConnectionRequestTimeout(timeout).build();
            httpGet.setConfig(requestConfig);

            if ("https".equalsIgnoreCase(url.getProtocol())) {
                log.debug("Invoke HTTPS GET: " + remoteUrl);
                TrustStrategy acceptingTrustStrategy = (X509Certificate[] certificate, String authType) -> true /*
                 * trust all certificates
                         */;
                try {
                    HttpClientBuilder httpBuilder = HttpClients.custom();

                    httpClient = httpBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                            .setSSLContext(new SSLContextBuilder().loadTrustMaterial(acceptingTrustStrategy).build())
                            .build();
                } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
                    String errorMsg = CommonUtils.getErrorMessage(e);
                    log.debug(errorMsg);
                    throw new IOException(errorMsg);
                }
            } else {
                log.debug("Invoke HTTP GET: " + remoteUrl);
                httpClient = HttpClients.createDefault();
            }

            CloseableHttpResponse response = httpClient.execute(httpGet);
            try {
                int status = response.getStatusLine().getStatusCode();
                HttpEntity entity = response.getEntity();

                if (status >= 200 && status < 300) {
                    if (entity != null) {
                        String respStr = EntityUtils.toString(entity);
                        EntityUtils.consume(entity);
                        return respStr;
                    }
                }
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        } catch (ConnectTimeoutException e) {
            log.debug("Connection time out: " + e);
        } catch (UnknownHostException e) {
            log.debug("Unknown host: " + e);
        } finally {
            if (httpClient != null) {
                httpClient.close();
            }
        }
        return null;
    }

    public static String httpPostFile(String location, String filePath,
            String mimeType, String username, String password, Map<String, String> details, boolean reportCatalogMsg)
            throws IOException, AuthenticationException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        log.debug("Invoke HTTP POST: location=" + location + "; filePath = " + filePath + "; mimeType = " + mimeType);

        String result = null;
        URL url = new URL(location);
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(url.getHost(), url.getPort()),
                new UsernamePasswordCredentials(username, password));

        try (CloseableHttpClient httpclient = createHttpClientWithCredentialsProvider(url, credsProvider)) {
            HttpPost httpPost = new HttpPost(location);
            FileEntity fileEntity = new FileEntity(new File(filePath), ContentType.create(mimeType));
            httpPost.setEntity(fileEntity);

            try (CloseableHttpResponse response = httpclient.execute(httpPost)) {

                int status = response.getStatusLine().getStatusCode();
                HttpEntity entity = response.getEntity();
                String respStr = null;
                if (entity != null) {
                    respStr = EntityUtils.toString(entity);
                    EntityUtils.consume(entity);
                    log.debug("httpPostFile response: " + respStr);
                    if (details != null) {
                        details.put(DETAILS_RESPONSE, respStr);
                    }
                }

                if (status == 401 || status == 403) {
                    String errorMsg = "";
                    if (status == 401) {
                        errorMsg = "401 Unauthorized";
                    }
                    if (status == 403) {
                        errorMsg = "403 Forbidden";
                    }

                    throw new AuthenticationException(status, errorMsg);
                }

                if (status >= 200 && status < 300) {
                    if (reportCatalogMsg) {
                        if (details != null) {
                            details.put(HTTP_CODE, "" + status);                            
                        }
                    }

                    Header locationHeader = response.getFirstHeader("Location");
                    if (locationHeader != null) {
                        result = locationHeader.getValue();
                    }

                    //result = respStr;
                } else {
                    if (details != null) {
                        details.put(DETAILS_ERROR_CODE, "" + status);
                        details.put(DETAILS_ERROR_MSG, respStr);
                    }
                }
            }
        }
        log.debug("result = " + result);
        return result;
    }

    private static CloseableHttpClient createHttpClientWithCredentialsProvider(URL url, CredentialsProvider credsProvider)
            throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        if ("https".equalsIgnoreCase(url.getProtocol())) {
            TrustStrategy acceptingTrustStrategy = (certificate, authType) -> {
                /*
                 * trust all certificates
                 */
                return true;
            };

            return HttpClients.custom().setDefaultCredentialsProvider(credsProvider).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .setSSLContext(new SSLContextBuilder().loadTrustMaterial(acceptingTrustStrategy).build()).useSystemProperties()
                    .build();

        } else {
            return HttpClients.custom().setDefaultCredentialsProvider(credsProvider).useSystemProperties().build();
        }
    }

    public static boolean httpDelete(String location, String username, String password, Map<String, String> details)
            throws IOException, AuthenticationException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        log.debug("Invoke HTTP DELETE: location=" + location + "; username = " + username);

        boolean result = false;
        URL url = new URL(location);

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(url.getHost(), url.getPort()),
                new UsernamePasswordCredentials(username, password));

        try (CloseableHttpClient httpclient = createHttpClientWithCredentialsProvider(url, credsProvider)) {
            HttpDelete httpDelete = new HttpDelete(location);

            HttpResponse response = httpclient.execute(httpDelete);
            HttpEntity entity = response.getEntity();
            String respStr = null;
            if (entity != null) {
                respStr = EntityUtils.toString(entity);
                EntityUtils.consume(entity);
                log.debug("httpDelete response: " + respStr);
            }

            int status = response.getStatusLine().getStatusCode();

            if (status == 401 || status == 403) {
                String errorMsg = "";
                if (status == 401) {
                    errorMsg = "401 Unauthorized";
                }
                if (status == 403) {
                    errorMsg = "403 Forbidden";
                }

                throw new AuthenticationException(status, errorMsg);
            }

            if (status >= 200 && status < 300) {
                result = true;
            } else {
                if (details != null) {
                    details.put(DETAILS_ERROR_CODE, "" + status);
                    details.put(DETAILS_ERROR_MSG, respStr);
                }
            }
        }

        return result;
    }

    public static boolean httpDelete(String location, Map<String, String> details) throws IOException {
        log.debug("Invoke HTTP DELETE: location=" + location);

        boolean result = false;
        URL url = new URL(location);
        try (CloseableHttpClient httpclient = HttpClients.custom().build()) {
            HttpDelete httpDelete = new HttpDelete(location);

            HttpResponse response = httpclient.execute(httpDelete);
            HttpEntity entity = response.getEntity();
            String respStr = null;
            if (entity != null) {
                respStr = EntityUtils.toString(entity);
                EntityUtils.consume(entity);
                log.debug("httpDelete response: " + respStr);
            }

            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                result = true;
            } else {
                if (details != null) {
                    details.put(DETAILS_ERROR_CODE, "" + status);
                    details.put(DETAILS_ERROR_MSG, respStr);
                }
            }
        }

        return result;
    }

    public static String httpPostWithXMLRequest(String location, String inputXml, String mimeType, String username, String password, Map<String, String> details) throws IOException, AuthenticationException {
        log.debug("Invoke HTTP POST with an XML request: location=" + location + "; username = " + username);

        String result = null;
        URL url = new URL(location);
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(url.getHost(), url.getPort()),
                new UsernamePasswordCredentials(username, password));

        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build()) {
            StringEntity strEntity = new StringEntity(inputXml, ContentType.create(mimeType, Consts.UTF_8));
            strEntity.setChunked(true);
            HttpPost httpPost = new HttpPost(location);
            httpPost.setEntity(strEntity);

            try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
                int status = response.getStatusLine().getStatusCode();
                HttpEntity entity = response.getEntity();
                String respStr = null;
                if (entity != null) {
                    respStr = EntityUtils.toString(entity);
                    EntityUtils.consume(entity);
                    log.debug("httpPostFile response: " + respStr);
                }

                if (status == 401 || status == 403) {
                    String errorMsg = "";
                    if (status == 401) {
                        errorMsg = "401 Unauthorized";
                    }
                    if (status == 403) {
                        errorMsg = "403 Forbidden";
                    }

                    throw new AuthenticationException(status, errorMsg);
                }

                if (status >= 200 && status < 300) {
                    Header locationHeader = response.getFirstHeader("Location");
                    if (locationHeader != null) {
                        result = locationHeader.getValue();
                    }
                } else {
                    if (details != null) {
                        details.put(DETAILS_ERROR_CODE, "" + status);
                        details.put(DETAILS_ERROR_MSG, respStr);
                    }
                }
            }
        }
        //log.debug("result = " + result);
        return result;
    }

    public static String httpPostWithXMLRequest(String location, String inputXml, String mimeType,
            Map<String, String> headers, Map<String, String> details) throws IOException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        log.debug("Invoke HTTP POST with an XML request: location=" + location);

        String result = null;
        URL url = new URL(location);
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(url.getHost(), url.getPort()),
                new UsernamePasswordCredentials("", ""));

        //System.setProperty("jsse.enableSNIExtension", "false");
        try (CloseableHttpClient httpclient = createHttpClientWithCredentialsProvider(url, credsProvider)) {
            StringEntity strEntity = new StringEntity(inputXml, ContentType.create(mimeType, Consts.UTF_8));
            strEntity.setChunked(true);
            HttpPost httpPost = new HttpPost(location);
            httpPost.setEntity(strEntity);

            if (headers != null) {
                headers.keySet().forEach((key) -> {
                    httpPost.addHeader(key, headers.get(key));
                });
            }
            try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
                int status = response.getStatusLine().getStatusCode();
                HttpEntity entity = response.getEntity();
                String respStr = null;
                if (entity != null) {
                    result = EntityUtils.toString(entity);
                    EntityUtils.consume(entity);
                    log.debug("httpPostFile response: " + respStr);
                }
                if (details != null) {
                    details.put(DETAILS_ERROR_CODE, "" + status);
                    //details.put(DETAILS_ERROR_MSG, respStr);
                }
            }
        }
        //log.debug("result = " + result);
        return result;
    }

}
