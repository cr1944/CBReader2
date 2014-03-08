package cheng.app.cnbeta.data;

import android.net.Uri;

public class CBContract {
    public static final String AUTHORITY = "cheng.app.cnbeta";
    public static final Uri NEWS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/news");
    public static final Uri HM_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/hm");
    public static final Uri CACHE_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/cache");

    public static final String NEWS_CONTENT_TYPE = "vnd.android.cursor.dir/cheng.app.cnbeta-news";
    public static final String NEWS_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/cheng.app.cnbeta-news-item";
    public static final String HM_CONTENT_TYPE = "vnd.android.cursor.dir/cheng.app.cnbeta-hm";
    public static final String HM_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/cheng.app.cnbeta-hm-item";
    public static final String CACHE_CONTENT_TYPE = "vnd.android.cursor.dir/cheng.app.cnbeta-cache";
    public static final String CACHE_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/cheng.app.cnbeta-cache-item";

    public interface NewsColumns {
        public static final String _ID = "_id";
        public static final String TITLE = "title";
        public static final String PUBTIME = "pubtime";
        public static final String ARTICLE_ID = "article_id";
        public static final String CMT_CLOSED = "cmt_closed";
        public static final String CMT_NUMBER = "cmt_num";
        public static final String SUMMARY = "summary";
        public static final String TOPIC_LOGO = "topic_logo";
        public static final String THEME = "theme";
        public static final String DEFAULT_SORT_ORDER = ARTICLE_ID + " COLLATE LOCALIZED DESC";
    }
    public interface HmColumns {
        public static final String _ID = "_id";
        public static final String TITLE = "title";
        public static final String COMMENT = "comment";
        public static final String ARTICLE_ID = "article_id";
        public static final String NAME = "name";
        public static final String HMID = "hmid";
        public static final String CMT_CLOSED = "cmt_closed";
        public static final String CMT_NUMBER = "cmt_num";
        public static final String DEFAULT_SORT_ORDER = HMID + " COLLATE LOCALIZED DESC";
    }
    public interface CacheColumns {
        public static final String _ID = "_id";
        public static final String ARTICLE_ID = "article_id";
        public static final String TITLE = "data_url";
        public static final String CMT_NUMBER = "cmt_num";
        public static final String DEFAULT_SORT_ORDER = ARTICLE_ID + " COLLATE LOCALIZED DESC";
    }
}
