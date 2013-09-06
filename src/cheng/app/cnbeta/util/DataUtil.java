
package cheng.app.cnbeta.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import cheng.app.cnbeta.CBComment;
import cheng.app.cnbeta.data.CBContract;
import cheng.app.cnbeta.data.CBContract.HmColumns;
import cheng.app.cnbeta.data.CBContract.NewsColumns;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

public class DataUtil {
    public static final String TAG = "JSONUtil";

    public static long parseAndSaveNewsList(String text, ContentResolver cr) {
        long articleId = -1;
        if (TextUtils.isEmpty(text)) {
            return articleId;
        }
        try {
            JSONArray array = new JSONArray(new JSONTokener(text));
            int length = array.length();
            if (length == 0)
                return articleId;
            for (int i = 0; i < length; i++) {
                JSONObject item = array.getJSONObject(i);
                ContentValues values = new ContentValues();
                articleId = item.optLong("ArticleID");
                values.put(NewsColumns.ARTICLE_ID, articleId);
                values.put(NewsColumns.TITLE, item.optString("title"));
                values.put(NewsColumns.PUBTIME, item.optString("pubtime"));
                values.put(NewsColumns.CMT_CLOSED, item.optInt("cmtClosed"));
                values.put(NewsColumns.CMT_NUMBER, item.optInt("cmtnum"));
                values.put(NewsColumns.SUMMARY, HttpUtil.escape(item.optString("summary")));
                values.put(NewsColumns.THEME, item.optString("theme").replace(" ", "%20"));
                values.put(NewsColumns.TOPIC_LOGO, item.optString("topicLogo").replace(" ", "%20"));
                int row = cr.update(Uri.withAppendedPath(CBContract.NEWS_CONTENT_URI,
                        String.valueOf(articleId)), values, null, null);
                if (row < 1)
                    cr.insert(CBContract.NEWS_CONTENT_URI, values);
            }
            Log.d(TAG, "parseAndSaveNewsList, length=" + length);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return articleId;
    }

    public static long parseAndSaveHotComments(String text, ContentResolver cr) {
        long hmid = -1;
        if (TextUtils.isEmpty(text)) {
            return hmid;
        }
        try {
            JSONArray array = new JSONArray(new JSONTokener(text));
            int length = array.length();
            if (length == 0)
                return hmid;
            for (int i = 0; i < length; i++) {
                JSONObject item = array.getJSONObject(i);
                ContentValues values = new ContentValues();
                values.put(HmColumns.ARTICLE_ID, item.optLong("ArticleID"));
                values.put(HmColumns.COMMENT, item.optString("comment"));
                values.put(HmColumns.TITLE, item.optString("title"));
                values.put(HmColumns.NAME, item.optString("name"));
                hmid = item.optLong("HMID");
                values.put(HmColumns.HMID, hmid);
                values.put(HmColumns.CMT_CLOSED, item.optInt("cmtClosed"));
                values.put(HmColumns.CMT_NUMBER, item.optInt("cmtnum"));
                int row = cr.update(
                        Uri.withAppendedPath(CBContract.HM_CONTENT_URI, String.valueOf(hmid)),
                        values, null, null);
                if (row < 1)
                    cr.insert(CBContract.HM_CONTENT_URI, values);
            }
            Log.d(TAG, "parseAndSaveHotComments, length=" + length);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return hmid;
    }

    public static List<CBComment> readComments(long articleId) {
        String url = Configs.COMMENT_URL + articleId;
        String html = HttpUtil.getInstance().httpGet(url);
        if (!TextUtils.isEmpty(html)) {
            try {
                JSONArray array = new JSONArray(new JSONTokener(html));
                int length = array.length();
                List<CBComment> result = new LinkedList<CBComment>();
                for (int i = 0; i < length; i++) {
                    JSONObject item = array.getJSONObject(i);
                    CBComment cmt = new CBComment();
                    cmt.newsId = articleId;
                    // cmt.title = title;
                    cmt.name = item.optString("name");
                    cmt.comment = item.optString("comment");
                    cmt.date = item.optString("date");
                    cmt.tid = item.optLong("tid");
                    cmt.support = item.optInt("support");
                    cmt.against = item.optInt("against");
                    result.add(cmt);
                }
                return result;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String readNews(long articleId, boolean hasSdCard) {
        String path = Configs.NEWS_PATH + "/" + articleId;
        if (hasSdCard && checkExist(path)) {
            Log.d(TAG, "readNews: [cached] " + path);
            return readFile(path);
        } else {
            Log.d(TAG, "readNews: [not cached] " + path);
            String url = Configs.NEWS_CONTENT_URL + articleId;
            String html = HttpUtil.getInstance().httpGet(url);
            if (!TextUtils.isEmpty(html)) {
                html = parserArticle(html);
            }
            if (hasSdCard)
                saveNews(articleId, html);
            else
                Log.w(TAG, "no sdcard, not cached");
            return html;
        }
    }

    private static String parserArticle(String html) {
        return html.replace("background:#FFF", "background:#00000000");
    }

    private static void saveNews(long articleId, String result) {
        if (TextUtils.isEmpty(result)) {
            Log.e(TAG, "saveNews: fail, news empty");
            return;
        }
        OutputStream outputStream = null;

        File dir = new File(Configs.NEWS_PATH);
        if (!dir.exists())
            dir.mkdirs();
        String path = Configs.NEWS_PATH + "/" + articleId;
        Log.d(TAG, "saveNews: " + path);
        File file = new File(path);
        try {
            outputStream = new FileOutputStream(file);
            outputStream.write(result.getBytes());
            outputStream.close();
        } catch (FileNotFoundException ex) {
            Log.w(TAG, ex);
        } catch (IOException ex) {
            Log.w(TAG, ex);
        }
    }

    private static String readFile(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                Log.e(TAG, "readFile: file not exists");
                return null;
            } else {
                FileInputStream f = new FileInputStream(path);
                int length = f.available();
                byte[] buffer = new byte[length];
                f.read(buffer);
                f.close();
                return new String(buffer);
            }
        } catch (FileNotFoundException ex) {
            Log.w(TAG, ex);
            return null;
        } catch (IOException ex) {
            Log.w(TAG, ex);
            return null;
        }
    }

    private static boolean checkExist(String path) {
        if (TextUtils.isEmpty(path)) {
            Log.d(TAG, "checkExist: path is empty");
            return false;
        }
        File file = new File(path);
        boolean exist = file.exists();
        return exist;
    }

    public static boolean hasSdcard() {
        try {
            return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
