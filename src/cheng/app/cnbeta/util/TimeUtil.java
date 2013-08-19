package cheng.app.cnbeta.util;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {
    static final String TAG = "TimeUtil";
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static final long WEEK = 24 * 60 * 60 * 7;
    static final long DAY = 24 * 60 * 60;
    static final long HOUR = 60 * 60;
    static final long MINUTE = 60;

    public static CharSequence formatTime(Context context, String time) {
        long timestamp = 0;
        try {
            Date date = dateFormat.parse(time);
            timestamp = date.getTime();
        } catch (ParseException e) {
            Log.w(TAG, "can't parse time!");
            return time;
        }
        final long now = System.currentTimeMillis();
        final long timeGap = (now - timestamp) / 1000;
        if (timeGap < 0) {
            return time;
        } else if (timeGap < MINUTE) {
            return DateUtils.getRelativeTimeSpanString(timestamp, now, DateUtils.SECOND_IN_MILLIS);
        } else if (timeGap < HOUR) {
            return DateUtils.getRelativeTimeSpanString(timestamp, now, DateUtils.MINUTE_IN_MILLIS);
        } else if (timeGap < DAY) {
            return DateUtils.getRelativeTimeSpanString(timestamp, now, DateUtils.HOUR_IN_MILLIS);
        } else if (timeGap < WEEK) {
            return DateUtils.getRelativeTimeSpanString(timestamp, now, DateUtils.DAY_IN_MILLIS);
        } else if (timeGap < WEEK * 4) {
            return DateUtils.getRelativeTimeSpanString(timestamp, now, DateUtils.WEEK_IN_MILLIS);
        } else {
            return time;
        }
    }

}
