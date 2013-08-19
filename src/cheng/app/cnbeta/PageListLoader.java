package cheng.app.cnbeta;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;

import cheng.app.cnbeta.data.CBContract;
import cheng.app.cnbeta.data.CBContract.HmColumns;
import cheng.app.cnbeta.data.CBContract.NewsColumns;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

public class PageListLoader extends AsyncTaskLoader<Cursor> {
    private long mLastId = -1;
    private int mPageId;
    final ForceLoadContentObserver mObserver;

    Uri mUri = CBContract.NEWS_CONTENT_URI;
    String[] mProjection;
    String mSelection;
    String[] mSelectionArgs;
    String mSortOrder;

    Cursor mCursor;

    /* Runs on a worker thread */
    @Override
    public Cursor loadInBackground() {
        if (mPageId == PageListFragment.PAGE_HM) {
            mUri = CBContract.HM_CONTENT_URI;
            if (mLastId > 0) {
                mSelection = HmColumns.HMID + " < ?" + mLastId;
                mSelectionArgs = new String[] {String.valueOf(mLastId)};
            } else {
                mSelection = null;
                mSelectionArgs = null;
            }
        } else {
            mUri = CBContract.NEWS_CONTENT_URI;
            if (mLastId > 0) {
                mSelection = NewsColumns.ARTICLE_ID + " < ?" + mLastId;
                mSelectionArgs = new String[] {String.valueOf(mLastId)};
            } else {
                mSelection = null;
                mSelectionArgs = null;
            }
        }
        Cursor cursor = getContext().getContentResolver().query(mUri, mProjection, mSelection,
                mSelectionArgs, mSortOrder);
        if (cursor != null) {
            // Ensure the cursor window is filled
            cursor.getCount();
            registerContentObserver(cursor, mObserver);
        }
        return cursor;
    }

    /**
     * Registers an observer to get notifications from the content provider
     * when the cursor needs to be refreshed.
     */
    void registerContentObserver(Cursor cursor, ContentObserver observer) {
        cursor.registerContentObserver(observer);
    }

    /* Runs on the UI thread */
    @Override
    public void deliverResult(Cursor cursor) {
        if (isReset()) {
            // An async query came in while the loader is stopped
            if (cursor != null) {
                cursor.close();
            }
            return;
        }
        Cursor oldCursor = mCursor;
        mCursor = cursor;

        if (isStarted()) {
            super.deliverResult(cursor);
        }

        if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
            oldCursor.close();
        }
    }

    public PageListLoader(Context context, int pageId) {
        super(context);
        mObserver = new ForceLoadContentObserver();
        mProjection = null;
        mSelection = null;
        mSelectionArgs = null;
        mSortOrder = NewsColumns.DEFAULT_SORT_ORDER;
        mLastId = -1;
        mPageId = pageId;
    }

    /**
     * Starts an asynchronous load of the contacts list data. When the result is ready the callbacks
     * will be called on the UI thread. If a previous load has been completed and is still valid
     * the result may be passed to the callbacks immediately.
     *
     * Must be called from the UI thread
     */
    @Override
    protected void onStartLoading() {
        if (mCursor != null) {
            deliverResult(mCursor);
        }
        if (takeContentChanged() || mCursor == null) {
            forceLoad();
        }
    }

    /**
     * Must be called from the UI thread
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    public void onCanceled(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    @Override
    protected void onReset() {
        super.onReset();
        
        // Ensure the loader is stopped
        onStopLoading();

        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
        mCursor = null;
    }

    public void setLastId(long lastId) {
        mLastId = lastId;
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(prefix, fd, writer, args);
        writer.print(prefix); writer.print("mUri="); writer.println(mUri);
        writer.print(prefix); writer.print("mProjection=");
                writer.println(Arrays.toString(mProjection));
        writer.print(prefix); writer.print("mSelection="); writer.println(mSelection);
        writer.print(prefix); writer.print("mSelectionArgs=");
                writer.println(Arrays.toString(mSelectionArgs));
        writer.print(prefix); writer.print("mSortOrder="); writer.println(mSortOrder);
        writer.print(prefix); writer.print("mCursor="); writer.println(mCursor);
    }
}
