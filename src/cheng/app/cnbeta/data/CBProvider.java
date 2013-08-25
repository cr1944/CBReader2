package cheng.app.cnbeta.data;

import java.util.HashMap;

import cheng.app.cnbeta.data.CBContract.HmColumns;
import cheng.app.cnbeta.data.CBContract.NewsColumns;
import cheng.app.cnbeta.data.CBSQLiteHelper.TABLES;
import cheng.app.cnbeta.util.Configs;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class CBProvider extends ContentProvider {
    private static final String TAG = "CBProvider";
    private static final boolean DEBUG = true;

    private static final int NEWS = 1;
    private static final int NEWS_ID = 2;
    private static final int HM =3;
    private static final int HM_ID = 4;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final HashMap<String, String> sNewsProjectionMap;
    private static final HashMap<String, String> sHMsProjectionMap;
    static {
        sUriMatcher.addURI(CBContract.AUTHORITY, "news", NEWS);
        sUriMatcher.addURI(CBContract.AUTHORITY, "news/#", NEWS_ID);
        sUriMatcher.addURI(CBContract.AUTHORITY, "hm", HM);
        sUriMatcher.addURI(CBContract.AUTHORITY, "hm/#", HM_ID);
        sNewsProjectionMap = new HashMap<String, String>();
        sNewsProjectionMap.put(NewsColumns._ID, NewsColumns._ID);
        sNewsProjectionMap.put(NewsColumns.TITLE, NewsColumns.TITLE);
        sNewsProjectionMap.put(NewsColumns.PUBTIME, NewsColumns.PUBTIME);
        sNewsProjectionMap.put(NewsColumns.ARTICLE_ID, NewsColumns.ARTICLE_ID);
        sNewsProjectionMap.put(NewsColumns.CMT_CLOSED, NewsColumns.CMT_CLOSED);
        sNewsProjectionMap.put(NewsColumns.CMT_NUMBER, NewsColumns.CMT_NUMBER);
        sNewsProjectionMap.put(NewsColumns.SUMMARY, NewsColumns.SUMMARY);
        sNewsProjectionMap.put(NewsColumns.TOPIC_LOGO, NewsColumns.TOPIC_LOGO);
        sNewsProjectionMap.put(NewsColumns.CACHED, NewsColumns.CACHED);
        sNewsProjectionMap.put(NewsColumns.THEME, NewsColumns.THEME);
        sHMsProjectionMap = new HashMap<String, String>();
        sHMsProjectionMap.put(HmColumns._ID, HmColumns._ID);
        sHMsProjectionMap.put(HmColumns.TITLE, HmColumns.TITLE);
        sHMsProjectionMap.put(HmColumns.COMMENT, HmColumns.COMMENT);
        sHMsProjectionMap.put(HmColumns.ARTICLE_ID, HmColumns.ARTICLE_ID);
        sHMsProjectionMap.put(HmColumns.NAME, HmColumns.NAME);
        sHMsProjectionMap.put(HmColumns.HMID, HmColumns.HMID);
        sHMsProjectionMap.put(HmColumns.CMT_CLOSED, HmColumns.CMT_CLOSED);
        sHMsProjectionMap.put(HmColumns.CMT_NUMBER, HmColumns.CMT_NUMBER);
    }
    private static final String SQL_WHERE_NEWS_ID = TABLES.NEWS_LIST + "." + NewsColumns.ARTICLE_ID + "=?";
    private static final String SQL_WHERE_HM_ID = TABLES.HM + "." + HmColumns.ARTICLE_ID + "=?";
    private CBSQLiteHelper mDbHelper;

    @Override
    public int delete(Uri arg0, String arg1, String[] arg2) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count;
        String finalWhere;
        switch (sUriMatcher.match(arg0)) {
            case NEWS:
                count = db.delete(TABLES.NEWS_LIST, arg1, arg2);
                break;
            case NEWS_ID:
                finalWhere = DatabaseUtils.concatenateWhere(
                        NewsColumns.ARTICLE_ID + " = " + ContentUris.parseId(arg0), arg1);
                count = db.delete(TABLES.NEWS_LIST, finalWhere, arg2);
                break;
            case HM:
                count = db.delete(TABLES.HM, arg1, arg2);
                break;
            case HM_ID:
                finalWhere = DatabaseUtils.concatenateWhere(
                        HmColumns.HMID + " = " + ContentUris.parseId(arg0), arg1);
                count = db.delete(TABLES.HM, finalWhere, arg2);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + arg0);
        }
        getContext().getContentResolver().notifyChange(arg0, null);
        return count;
    }

    @Override
    public String getType(Uri arg0) {
        int match = sUriMatcher.match(arg0);
        switch (match) {
            case NEWS:
                return CBContract.NEWS_CONTENT_TYPE;
            case NEWS_ID:
                return CBContract.NEWS_CONTENT_ITEM_TYPE;
            case HM:
                return CBContract.HM_CONTENT_TYPE;
            case HM_ID:
                return CBContract.HM_CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URL " + arg0);
        }
    }

    @Override
    public Uri insert(Uri arg0, ContentValues arg1) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long rowId;
        switch (sUriMatcher.match(arg0)) {
            case NEWS:
                rowId = db.insert(TABLES.NEWS_LIST, null, arg1);
                if (rowId > 0) {
                    Uri uri = ContentUris.withAppendedId(CBContract.NEWS_CONTENT_URI, rowId);
                    getContext().getContentResolver().notifyChange(uri, null);
                    return uri;
                }
                break;
            case HM:
                rowId = db.insert(TABLES.HM, null, arg1);
                if (rowId > 0) {
                    Uri uri = ContentUris.withAppendedId(CBContract.HM_CONTENT_URI, rowId);
                    getContext().getContentResolver().notifyChange(uri, null);
                    return uri;
                }
                break;
        }
        throw new SQLException("Failed to insert row into " + arg0);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new CBSQLiteHelper(getContext());
        return true;
    }

    @Override
    public void shutdown() {
        if (mDbHelper != null) {
            mDbHelper.close();
            mDbHelper = null;
        }
    }

    @Override
    public Cursor query(Uri arg0, String[] arg1, String arg2, String[] arg3, String arg4) {
        if (DEBUG) Log.v(TAG, "query uri - " + arg0);
        final SQLiteDatabase db = mDbHelper.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String limit = null;
        switch (sUriMatcher.match(arg0)) {
            case NEWS:
                qb.setTables(TABLES.NEWS_LIST);
                qb.setProjectionMap(sNewsProjectionMap);
                if (TextUtils.isEmpty(arg4)) {
                    arg4 = NewsColumns.DEFAULT_SORT_ORDER;
                }
                limit = "" + Configs.LIMIT;
                break;
            case NEWS_ID:
                qb.setTables(TABLES.NEWS_LIST);
                arg3 = insertSelectionArg(arg3, arg0.getLastPathSegment());
                qb.appendWhere(SQL_WHERE_NEWS_ID);
                qb.setProjectionMap(sNewsProjectionMap);
                if (TextUtils.isEmpty(arg4)) {
                    arg4 = NewsColumns.DEFAULT_SORT_ORDER;
                }
                break;
            case HM:
                qb.setTables(TABLES.HM);
                qb.setProjectionMap(sHMsProjectionMap);
                if (TextUtils.isEmpty(arg4)) {
                    arg4 = HmColumns.DEFAULT_SORT_ORDER;
                }
                limit = "" + Configs.LIMIT;
                break;
            case HM_ID:
                qb.setTables(TABLES.HM);
                arg3 = insertSelectionArg(arg3, arg0.getLastPathSegment());
                qb.appendWhere(SQL_WHERE_HM_ID);
                qb.setProjectionMap(sHMsProjectionMap);
                if (TextUtils.isEmpty(arg4)) {
                    arg4 = HmColumns.DEFAULT_SORT_ORDER;
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URL " + arg0);
        }
        Cursor c = qb.query(db, arg1, arg2, arg3, null, null, arg4, limit);
        c.setNotificationUri(getContext().getContentResolver(), arg0);
        return c;
    }

    @Override
    public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(arg0);
        String finalWhere;
        int count;
        switch (match) {
            case NEWS:
                count = db.update(TABLES.NEWS_LIST, arg1, arg2, arg3);
                break;
            case NEWS_ID:
                finalWhere = DatabaseUtils.concatenateWhere(
                        NewsColumns.ARTICLE_ID + " = " + ContentUris.parseId(arg0), arg2);
                count = db.update(TABLES.NEWS_LIST, arg1, finalWhere, arg3);
                break;
            case HM:
                count = db.update(TABLES.HM, arg1, arg2, arg3);
                break;
            case HM_ID:
                finalWhere = DatabaseUtils.concatenateWhere(
                        HmColumns.HMID + " = " + ContentUris.parseId(arg0), arg2);
                count = db.update(TABLES.HM, arg1, finalWhere, arg3);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + arg0);
        }
        getContext().getContentResolver().notifyChange(arg0, null);

        return count;
    }

    private String[] insertSelectionArg(String[] selectionArgs, String arg) {
        if (selectionArgs == null) {
            return new String[] {arg};
        } else {
            int newLength = selectionArgs.length + 1;
            String[] newSelectionArgs = new String[newLength];
            newSelectionArgs[0] = arg;
            System.arraycopy(selectionArgs, 0, newSelectionArgs, 1, selectionArgs.length);
            return newSelectionArgs;
        }
    }
}
