package cheng.app.cnbeta.data;

import cheng.app.cnbeta.data.CBContract.CacheColumns;
import cheng.app.cnbeta.data.CBContract.HmColumns;
import cheng.app.cnbeta.data.CBContract.NewsColumns;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CBSQLiteHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "userdata.db";
    private static final int DB_VERSION = 20;

    public interface TABLES {
        public static final String NEWS_LIST = "news_list";
        public static final String TOP = "top";
        public static final String HM = "hm";
        public static final String DATA = "data";
        public static final String COMMENT = "comment";
        public static final String IMAGE = "image";
        public static final String ACCOUNT = "account";
        public static final String CACHE = "cache";
    }

    public CBSQLiteHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLES.NEWS_LIST + " ("
                + NewsColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + NewsColumns.TITLE + " varchar,"
                + NewsColumns.PUBTIME + " varchar,"
                + NewsColumns.ARTICLE_ID + " INTEGER NOT NULL DEFAULT 0,"
                + NewsColumns.CMT_CLOSED + " INTEGER NOT NULL DEFAULT 0,"
                + NewsColumns.CMT_NUMBER + " INTEGER NOT NULL DEFAULT 0,"
                + NewsColumns.SUMMARY + " varchar,"
                + NewsColumns.TOPIC_LOGO + " varchar,"
                + NewsColumns.THEME + " varchar,"
                + "UNIQUE(" + NewsColumns.ARTICLE_ID + "))");
//        db.execSQL("CREATE TABLE " + TABLES.TOP + " ("
//                + TopColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
//                + TopColumns.COUNTER + " INTEGER NOT NULL DEFAULT 0,"
//                + TopColumns.ARTICLE_ID + " INTEGER NOT NULL DEFAULT 0,"
//                + TopColumns.CMT_CLOSED + " INTEGER NOT NULL DEFAULT 0,"
//                + TopColumns.CMT_NUMBER + " INTEGER NOT NULL DEFAULT 0,"
//                + TopColumns.TITLE + " varchar,"
//                + "UNIQUE(" + TopColumns.ARTICLE_ID + "))");
        db.execSQL("CREATE TABLE " + TABLES.HM + " ("
                + HmColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + HmColumns.ARTICLE_ID + " INTEGER NOT NULL DEFAULT 0,"
                + HmColumns.CMT_CLOSED + " INTEGER NOT NULL DEFAULT 0,"
                + HmColumns.CMT_NUMBER + " INTEGER NOT NULL DEFAULT 0,"
                + HmColumns.HMID + " INTEGER NOT NULL DEFAULT 0,"
                + HmColumns.NAME + " varchar,"
                + HmColumns.TITLE + " varchar,"
                + HmColumns.COMMENT + " varchar,"
                + "UNIQUE(" + HmColumns.HMID + "))");
        db.execSQL("CREATE TABLE " + TABLES.CACHE + " ("
                + CacheColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + CacheColumns.ARTICLE_ID + " INTEGER NOT NULL DEFAULT 0,"
                + CacheColumns.CMT_NUMBER + " INTEGER NOT NULL DEFAULT 0,"
                + CacheColumns.TITLE + " varchar,"
                + "UNIQUE(" + CacheColumns.ARTICLE_ID + "))");
//        db.execSQL("CREATE TABLE " + TABLES.DATA + " ("
//                + Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
//                + Columns.DATA_ID + " INTEGER" + ")");
//        db.execSQL("CREATE TABLE " + TABLES.COMMENT + " ("
//                + Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
//                + Columns.DATA_ID + " INTEGER" + ")");
//        db.execSQL("CREATE TABLE " + TABLES.IMAGE + " ("
//                + Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
//                + Columns.DATA_URL + " varchar,"
//                + Columns.PATH + " varchar" + ")");
//        db.execSQL("CREATE TABLE " + TABLES.ACCOUNT + " ("
//                + AccountColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
//                + AccountColumns.ACCOUNTTYPE + " INTEGER,"
//                + AccountColumns.USERNAME + " varchar,"
//                + AccountColumns.USERNICK + " varchar,"
//                + AccountColumns.OPENID + " varchar,"
//                + AccountColumns.OPENKEY + " varchar,"
//                + AccountColumns.EXPIRESIN + " INTEGER,"
//                 + AccountColumns.REFRESHTOKEN + " varchar,"
//                + AccountColumns.ACCESSTOKEN + " varchar" + ")");
//        db.execSQL("CREATE UNIQUE INDEX news_list_ids ON " +
//                TABLES.NEWS_LIST + " (" +
//                NewsColumns.ARTICLE_ID +
//        ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < DB_VERSION) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLES.NEWS_LIST + ";");
            db.execSQL("DROP TABLE IF EXISTS " + TABLES.TOP + ";");
            db.execSQL("DROP TABLE IF EXISTS " + TABLES.HM + ";");
            db.execSQL("DROP TABLE IF EXISTS " + TABLES.DATA + ";");
            db.execSQL("DROP TABLE IF EXISTS " + TABLES.COMMENT + ";");
            db.execSQL("DROP TABLE IF EXISTS " + TABLES.IMAGE + ";");
            db.execSQL("DROP TABLE IF EXISTS " + TABLES.ACCOUNT + ";");
            db.execSQL("DROP TABLE IF EXISTS " + TABLES.CACHE + ";");
            onCreate(db);
            return;
        }
    }

}
