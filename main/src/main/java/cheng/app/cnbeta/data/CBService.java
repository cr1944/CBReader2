package cheng.app.cnbeta.data;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import cheng.app.cnbeta.util.Configs;

public class CBService extends Service {
    private static final String TAG = "CnbetaService";
    private static final boolean DEBUG = true;
    LocalBroadcastManager mLocalBroadcastManager;
    LoadThread mLoadThread;
    Context mContext;
    SQLiteDatabase mDb;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        CBSQLiteHelper dbHelper = new CBSQLiteHelper(this);
        mDb = dbHelper.getWritableDatabase();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            if (DEBUG) Log.d(TAG, "START_NOT_STICKY - intent is null.");
            return START_NOT_STICKY;
        }
        String action = intent.getAction();
        if (action == null) {
            if (DEBUG) Log.d(TAG, "START_NOT_STICKY - action is null.");
            return START_NOT_STICKY;
        } else if (action.equals(Configs.ACTION_NEWS_LOAD)) {
            long id = intent.getLongExtra(Configs.EXTRA_ID, 0);
            new LoadThread(id).start();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDb != null) {
            mDb.close();
            mDb = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class LoadThread extends Thread {
        long mId;

        public LoadThread(long id) {
            super("LoadThread");
            mId = id;
        }

        @Override
        public void run() {
            boolean success = false;
            Intent intent = new Intent(Configs.ACTION_NEWS_LOAD_DONE);
            intent.putExtra(Configs.EXTRA_SUCCESS, success);
            mLocalBroadcastManager.sendBroadcast(intent);
        }
    }

}
