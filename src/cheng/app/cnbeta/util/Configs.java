package cheng.app.cnbeta.util;

import android.os.Environment;

public class Configs {
    public static final String ACTION_NEWS_LOAD = "cheng.app.cnbeta.ACTION_NEWS_LOAD";
    public static final String ACTION_NEWS_LOAD_DONE = "cheng.app.cnbeta.ACTION_NEWS_LOAD_DONE";
    public static final String EXTRA_ID = "extra_id";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_TEXT = "extra_text";
    public static final String EXTRA_NUMBER = "extra_number";
    public static final String EXTRA_PAGE = "extra_page";
    public static final String EXTRA_SUCCESS = "extra_success";
    public static final String EXTRA_BITMAP = "extra_bitmap";

    public static final int CMT_PAGE_ALL = 0;
    public static final int CMT_PAGE_HOT = 1;
    public static final int CMT_PAGE_NUM = 2;

    public static final int HOME_PAGE_HOME = 0;
    public static final int HOME_PAGE_TOP = 1;
    public static final int HOME_PAGE_HM = 2;
    public static final int HOME_PAGE_NUM = 3;

    public static final int NETWORK_UNKNOWN = 0;
    public static final int NETWORK_NOT_ENABLED = 1;
    public static final int NETWORK_WIFI = 2;
    public static final int NETWORK_MOBILE = 3;

    public static final String KEY_DISPLAY_LOGO = "key_display_logo";
    //public static final String KEY_AUTO_CACHE = "key_auto_cache";
    public static final String KEY_AUTO_REFRESH = "key_auto_refresh";
    public static final String KEY_COMMENT_NAME = "key_comment_name";
    public static final String KEY_COMMENT_EMAIL = "key_comment_email";
    public static final String KEY_COMMENT_TAIL = "key_comment_tail";
    public static final String KEY_ACCOUNT_SINA = "key_account_sina";
    public static final String KEY_ACCOUNT_TENCENT = "key_account_tencent";
    public static final String KEY_SHARE_PIC = "key_share_pic";
    public static final String KEY_SYNC_SINA = "key_sync_sina";
    public static final String KEY_SYNC_TENCENT = "key_sync_tencent";
    public static final String KEY_CLEAN_CACHE = "key_clean_cache";
    public static final String KEY_SUGGESTION = "key_suggestion";
    public static final String KEY_FONT_SIZE = "key_font_size";
    public static final int ACCOUNT_TYPE_SINA = 1;
    public static final int ACCOUNT_TYPE_TENCENT = 2;
    public static final int MINIMUM_FONTSIZE = 12;
    public static final int MAXIMUM_FONTSIZE = 22;
    public static final int DEFAULT_FONTSIZE = 14;

    public static final String NEWS_PATH = Environment.getExternalStorageDirectory().toString()
            + "/.CBReader/news_cache";
    public static final String COMMENT_PATH = Environment.getExternalStorageDirectory().toString()
            + "/.CBReader/comments_cache";
    public static final String IMAGE_PATH = Environment.getExternalStorageDirectory().toString()
            + "/.CBReader/image_cache";
    public static final String SCREENSHOT_PATH = Environment.getExternalStorageDirectory().toString()
            + "/.CBReader/screenshot_cache";

    public static final String EMAIL_REX = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";
    public static final String ROOT_URL = "http://www.cnbeta.com";
    public static final String NEWSLIST_URL = "http://www.cnbeta.com/api/getNewsList.php?limit=";
    public static final int LIMIT = 20;
    public static final String NEWSLIST_PAGE = "&fromArticleId=";
    public static final String TOP_URL = "http://www.cnbeta.com/api/getTop10.php";
    public static final String HMCOMMENT_URL = "http://www.cnbeta.com/api/getHMComment.php?limit=";
    public static final String HMCOMMENT_PAGE = "&fromHMCommentId=";
    public static final String NEWS_CONTENT_URL = "http://www.cnbeta.com/api/getNewsContent2.php?articleId=";
    public static final String COMMENT_URL = "http://www.cnbeta.com/api/getComment.php?article=";
    public static final String VALIDATE_URL = "http://www.cnbeta.com/captcha.htm?refresh=1";
    public static final String POST_COMMENT_URL = "http://www.cnbeta.com/Ajax.comment.php?ver=new";
    public static final String REPORT_URL = "http://www.cnbeta.com/Ajax.report.php?tid=";
    public static final String SUPPORT_URL = "http://www.cnbeta.com/Ajax.vote.php?support=1&tid=";
    public static final String AGGAINST_URL = "http://www.cnbeta.com/Ajax.report.php?against=1&tid=";
    public static final String DIG_URL = "http://www.cnbeta.com/Ajax.dig.php?sid=";
    public static final String RATING_URL = "http://www.cnbeta.com/rpc.php?j=-5&q=217270&t=127.0.0.1&c=11&s=1";
    //public static final String HOME_URL = "http://m.cnbeta.com";
    //public static final String TOP_TJ = "http://m.cnbeta.com/top.php?type=tj";
    //public static final String TOP_HOT = "http://m.cnbeta.com/top.php?type=hot";
    //public static final String TOP_ARGUE = "http://m.cnbeta.com/top.php?type=argue";
    //public static final String PAGE_URL = "http://m.cnbeta.com/index.php?pageID=";
    //public static final String DETIAL_URL = "http://m.cnbeta.com/marticle.php?sid=";
    //public static final String HOT_COMMENT_URL = "http://m.cnbeta.com/hcomment.php?sid=";
    //public static final String COMMENT_URL = "http://m.cnbeta.com/mcomment.php?sid=";
    //public static final String HOT_COMMENT_URL = "http://www.cnbeta.com/comment/g_content/207959.html";
    //public static final String COMMENT_URL = "http://www.cnbeta.com/comment/normal/207959.html";
}
