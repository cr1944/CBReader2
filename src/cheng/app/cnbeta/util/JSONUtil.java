package cheng.app.cnbeta.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.text.TextUtils;
import cheng.app.cnbeta.data.CBCommentEntry;
import cheng.app.cnbeta.data.CBContract;
import cheng.app.cnbeta.data.CBContract.HmColumns;
import cheng.app.cnbeta.data.CBContract.NewsColumns;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.LinkedList;

public class JSONUtil {
    public static final String TAG = "JSONUtil";

    public static long parseAndSaveNewsList(String text, ContentResolver cr) {
        long articleId = -1;
        if (!TextUtils.isEmpty(text)) {
            return articleId;
        }
        try {
            JSONArray array = new JSONArray(new JSONTokener(text));
            int length = array.length();
            if (length == 0) return articleId;
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
                int row = cr.update(Uri.withAppendedPath(CBContract.NEWS_CONTENT_URI, String.valueOf(articleId)), values, null, null);
                if (row < 1)
                    cr.insert(CBContract.NEWS_CONTENT_URI, values);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return articleId;
    }

    public static long parseAndSaveHotComments(String text, ContentResolver cr) {
        long hmid = -1;
        if (!TextUtils.isEmpty(text)) {
            return hmid;
        }
        try {
            JSONArray array = new JSONArray(new JSONTokener(text));
            int length = array.length();
            if (length == 0) return hmid;
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
                int row = cr.update(Uri.withAppendedPath(CBContract.HM_CONTENT_URI, String.valueOf(hmid)), values, null, null);
                if (row < 1)
                    cr.insert(CBContract.HM_CONTENT_URI, values);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return hmid;
    }

    public static LinkedList<CBCommentEntry> parseComments(long newsId, String title, String text) {
        try {
            JSONArray array = new JSONArray(new JSONTokener(text));
            int length = array.length();
            LinkedList<CBCommentEntry> result = new LinkedList<CBCommentEntry>();
            for (int i = 0; i < length; i++) {
                JSONObject item = array.getJSONObject(i);
                CBCommentEntry cmt = new CBCommentEntry();
                cmt.newsId = newsId;
                cmt.title = title;
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
            return null;
        }
    }

    public static boolean parserTencentResponse(String text) {
        if (TextUtils.isEmpty(text)) {
            return false;
        }
        try {
            JSONObject jo = new JSONObject(text);
            int errCode = jo.optInt("errcode");
            int ret = jo.optInt("ret");
            if (errCode == 0 && ret == 0) {
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
}
