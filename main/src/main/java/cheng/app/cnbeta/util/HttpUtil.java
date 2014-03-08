
package cheng.app.cnbeta.util;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpUtil {
    private static final String TAG = "HttpUtil";
    private static final boolean DEBUG = true;
    private static final int REQUEST_TIMEOUT = 20 * 1000;
    private static final int SO_TIMEOUT = 20 * 1000;
    private static final int MAX_CONNECTION = 10;
    private DefaultHttpClient mClient;

    private static HttpUtil sInstance;

    public synchronized static HttpUtil getInstance() {
        if (sInstance == null) {
            sInstance = new HttpUtil();
        }
        return sInstance;
    }

    private HttpUtil() {
        mClient = init();
    }

    private DefaultHttpClient init() {
        BasicHttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, REQUEST_TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParams, SO_TIMEOUT);
        ConnManagerParams.setMaxConnectionsPerRoute(httpParams,
                new ConnPerRouteBean(MAX_CONNECTION));
        ConnManagerParams.setMaxTotalConnections(httpParams, MAX_CONNECTION);
        HttpConnectionParams.setTcpNoDelay(httpParams, true);
        HttpConnectionParams.setSocketBufferSize(httpParams, 8192);
        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setUserAgent(httpParams,
                String.format("Mozilla/5.0 (Android;async-http/%s)", "1.4.1"));
        SchemeRegistry sr = new SchemeRegistry();
        sr.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        sr.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        ClientConnectionManager c = new ThreadSafeClientConnManager(httpParams, sr);
        return new DefaultHttpClient(c, httpParams);
    }

    public void reset() {
        if (mClient != null) {
            mClient.getConnectionManager().shutdown();
        }
        mClient = init();
    }

    public byte[] httpGetByte(String url) {
        if (DEBUG)
            Log.d(TAG, "httpGet:" + url);
        HttpGet get = new HttpGet(url);
        // BasicHttpParams httpParams = new BasicHttpParams();
        // HttpConnectionParams.setConnectionTimeout(httpParams,
        // REQUEST_TIMEOUT);
        // HttpConnectionParams.setSoTimeout(httpParams, SO_TIMEOUT);
        // HttpClient client = new DefaultHttpClient(httpParams);
        byte[] result = null;
        try {
            HttpResponse response = mClient.execute(get);
            int status = response.getStatusLine().getStatusCode();
            if (DEBUG)
                Log.d(TAG, "StatusCode:" + status);
            if (status == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    // result = EntityUtils.toString(entity, "UTF-8");
                    result = EntityUtils.toByteArray(entity);
                    // result = new String(result.getBytes("ISO-8859-1"),
                    // "UTF-8");
                }
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // mClient.getConnectionManager().shutdown();
        }
        return result;
    }

    public String httpGet(String url) {
        byte[] result = httpGetByte(url);
        if (result != null) {
            try {
                return new String(result, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "UnsupportedEncodingException", e);
            }
        }
        return null;
    }

    public String httpPost(String url, HashMap<String, String> param, String encoding) {
        HttpPost httpRequest = new HttpPost(url);
        httpRequest.addHeader("Content-Type", "application/x-www-form-urlencoded");
        // httpRequest.addHeader("charset", "gb2312");
        // BasicHttpParams httpParams = new BasicHttpParams();
        // HttpConnectionParams.setConnectionTimeout(httpParams,
        // REQUEST_TIMEOUT);
        // HttpConnectionParams.setSoTimeout(httpParams, SO_TIMEOUT);
        // HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
        // HttpProtocolParams.setUserAgent(httpParams,
        // String.format("Mozilla/5.0 (Android;async-http/%s)", "1.4.1"));
        // HttpClient client = new DefaultHttpClient();
        if (param != null && param.size() > 0) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            Iterator<Entry<String, String>> iterator = param.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, String> entry = (Entry<String, String>) iterator.next();
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                if (DEBUG)
                    Log.d(TAG, "param[" + key + "," + value + "]");
                params.add(new BasicNameValuePair(key, value));
            }
            try {
                HttpEntity httpentity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
                httpRequest.setEntity(httpentity);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        String result = null;
        try {
            HttpResponse response = mClient.execute(httpRequest);
            int status = response.getStatusLine().getStatusCode();
            if (DEBUG)
                Log.d(TAG, "StatusCode:" + status);
            if (status == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    result = EntityUtils.toString(entity, encoding);
                }
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // mClient.getConnectionManager().shutdown();
        }
        return result;
    }

    public static String encode(String s) {
        if (s == null) {
            return "";
        }
        try {
            return URLEncoder.encode(s, "UTF-8").replace("+", "%20").replace("*", "%2A")
                    .replace("~", "%7E").replace("#", "%23");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static String escape(String src) {
        int i;
        char j;
        StringBuilder tmp = new StringBuilder(20);
        tmp.ensureCapacity(src.length() * 6);
        for (i = 0; i < src.length(); i++) {
            j = src.charAt(i);
            if (Character.isDigit(j) || Character.isLowerCase(j) || Character.isUpperCase(j))
                tmp.append(j);
            else if (j < 256) {
                tmp.append("%");
                if (j < 16)
                    tmp.append("0");
                tmp.append(Integer.toString(j, 16));
            } else {
                tmp.append("%u");
                tmp.append(Integer.toString(j, 16));
            }
        }
        return tmp.toString();
    }

    public static String unescape(String src) {
        StringBuffer tmp = new StringBuffer();
        tmp.ensureCapacity(src.length());
        int lastPos = 0, pos = 0;
        char ch;
        while (lastPos < src.length()) {
            pos = src.indexOf("%", lastPos);
            if (pos == lastPos) {
                if (src.charAt(pos + 1) == 'u') {
                    ch = (char) Integer.parseInt(src.substring(pos + 2, pos + 6), 16);
                    tmp.append(ch);
                    lastPos = pos + 6;
                } else {
                    ch = (char) Integer.parseInt(src.substring(pos + 1, pos + 3), 16);
                    tmp.append(ch);
                    lastPos = pos + 3;
                }
            } else {
                if (pos == -1) {
                    tmp.append(src.substring(lastPos));
                    lastPos = src.length();
                } else {
                    tmp.append(src.substring(lastPos, pos));
                    lastPos = pos;
                }
            }
        }
        return tmp.toString();
    }

    public static String filterEntities(String src) {
        StringBuffer sb = new StringBuffer();
        Pattern p = Pattern.compile("&[a-zA-Z]*;|&#[0-9]*;");
        Matcher m = p.matcher(src);
        int pos1 = 0;
        while (m.find(pos1)) {
            int pos2 = m.start();
            sb.append(src.substring(pos1, pos2));
            String entity = m.group().toLowerCase();
            if ("&nbsp;".equals(entity) || "&#160;".equals(entity)) {
                sb.append((char)160);
            } else if ("&lt;".equals(entity) || "&#60;".equals(entity)) {
                sb.append((char)60);
            } else if ("&gt;".equals(entity) || "&#62;".equals(entity)) {
                sb.append((char)62);
            } else if ("&amp;".equals(entity) || "&#38;".equals(entity)) {
                sb.append((char)38);
            } else if ("&quot;".equals(entity) || "&#34;".equals(entity)) {
                sb.append((char)34);
            } else if ("&apos;".equals(entity) || "&#39;".equals(entity)) {
                sb.append((char)39);
            } else if ("&cent;".equals(entity) || "&#162;".equals(entity)) {
                sb.append((char)0xa2);
            } else if ("&pound;".equals(entity) || "&#163;".equals(entity)) {
                sb.append((char)0xa3);
            } else if ("&yen;".equals(entity) || "&#165;".equals(entity)) {
                sb.append((char)0xa5);
            } else if ("&sect;".equals(entity) || "&#167;".equals(entity)) {
                sb.append((char)0xa7);
            } else if ("&copy;".equals(entity) || "&#169;".equals(entity)) {
                sb.append((char)0xa9);
            } else if ("&reg;".equals(entity) || "&#174;".equals(entity)) {
                sb.append((char)0xae);
            } else if ("&times;".equals(entity) || "&#215;".equals(entity)) {
                sb.append((char)215);
            } else if ("&divide;".equals(entity) || "&#247;".equals(entity)) {
                sb.append((char)247);
            }
            pos1 = m.end();
        }
        sb.append(src.substring(pos1));
        return sb.toString();
    }

    public String getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> mEnumeration = NetworkInterface
                    .getNetworkInterfaces(); mEnumeration.hasMoreElements();) {
                NetworkInterface intf = mEnumeration.nextElement();
                for (Enumeration<InetAddress> enumIPAddr = intf.getInetAddresses(); enumIPAddr
                        .hasMoreElements();) {
                    InetAddress inetAddress = enumIPAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("Error", ex.toString());
        }
        return null;
    }
}
