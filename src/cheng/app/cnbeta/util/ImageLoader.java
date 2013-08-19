package cheng.app.cnbeta.util;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Handler.Callback;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import cheng.app.cnbeta.data.CBSQLiteHelper;

public class ImageLoader implements Callback {
    private static final String TAG = "ImageLoader";
    private static ImageLoader sInstance;

    public static ImageLoader getInstance(Context context) {
        if (sInstance == null)
            sInstance = new ImageLoader(context);
        return sInstance;
    }

    private static final int MESSAGE_REQUEST_LOADING = 1;
    private static final int MESSAGE_PHOTOS_LOADED = 3;

    private static class BitmapHolder {
        private static final int NEEDED = 0;
        private static final int LOADING = 1;
        private static final int LOADED = 2;

        int state = NEEDED;
        SoftReference<Bitmap> bitmapRef;
    }
    private final ConcurrentHashMap<ImageView, String> mPendingRequests =
        new ConcurrentHashMap<ImageView, String>();
    private final ConcurrentHashMap<String, BitmapHolder> mBitmapCache =
        new ConcurrentHashMap<String, BitmapHolder>();
    private final Handler mMainThreadHandler = new Handler(this);
    private LoaderThread mLoaderThread;
    private boolean mLoadingRequested;
    private boolean mPaused = false;
    private SQLiteDatabase mDb;
    public interface OnLoadedListener {
        void onLoaded();
    }

    private ImageLoader(Context context) {
        CBSQLiteHelper dbHelper = new CBSQLiteHelper(context);
        mDb = dbHelper.getWritableDatabase();
    }

    private boolean loadCachedImage(String imageUrl, ImageView view) {
        BitmapHolder holder = mBitmapCache.get(imageUrl);
        if (holder == null) {
            holder = new BitmapHolder();
            mBitmapCache.put(imageUrl, holder);
        } else if (holder.state == BitmapHolder.LOADED) {
            if (holder.bitmapRef == null) {
                Log.e(TAG, "loadCachedImage: holder.bitmapRef == null");
                holder.state = BitmapHolder.NEEDED;
                return true;
            }

            Bitmap bitmap = holder.bitmapRef.get();
            if (bitmap != null) {
                view.setImageBitmap(bitmap);
                return true;
            }

            // Null bitmap means that the soft reference was released by the GC
            // and we need to reload the photo.
            holder.bitmapRef = null;
        }

        // The bitmap has not been loaded - should display the placeholder image.
        //view.setImageResource(mDefaultResourceId);
        holder.state = BitmapHolder.NEEDED;
        return false;
    }

    public void loadPhoto(String imageUrl, ImageView view, int defalutRes) {
        view.setImageResource(defalutRes);
        mPendingRequests.remove(view);
        if (TextUtils.isEmpty(imageUrl)) {
            Log.e(TAG, "loadPhoto: imageUrl is empty!");
        } else {
            if (!mPaused) {
                mPendingRequests.put(view, imageUrl);
                boolean loaded = loadCachedImage(imageUrl, view);
                if (loaded) {
                    mPendingRequests.remove(view);
                } else {
                    requestLoading();
                }
            }
        }
   }

    private void requestLoading() {
        if (!mLoadingRequested) {
            mLoadingRequested = true;
            mMainThreadHandler.sendEmptyMessage(MESSAGE_REQUEST_LOADING);
        }
    }

    public void stop() {
        pause();

        if (mLoaderThread != null) {
            mLoaderThread.quit();
            mLoaderThread = null;
        }

        clear();
        mDb.close();
    }

    public void clear() {
        mPendingRequests.clear();
        mBitmapCache.clear();
    }

    public void pause() {
        mPaused = true;
    }

    public void resume() {
        mPaused = false;
        if (!mPendingRequests.isEmpty()) {
            requestLoading();
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_REQUEST_LOADING: {
                mLoadingRequested = false;
                if (!mPaused) {
                    if (mLoaderThread == null) {
                        mLoaderThread = new LoaderThread();
                        mLoaderThread.start();
                    }

                    mLoaderThread.requestLoading();
                }
                return true;
            }

            case MESSAGE_PHOTOS_LOADED: {
                if (!mPaused) {
                    processLoadedImages();
                }
                return true;
            }
        }
        return false;
    }

    private void processLoadedImages() {
        Iterator<ImageView> iterator = mPendingRequests.keySet().iterator();
        while (iterator.hasNext()) {
            ImageView view = iterator.next();
            String imageUrl = mPendingRequests.get(view);
            boolean loaded = loadCachedImage(imageUrl, view);
            if (loaded) {
                iterator.remove();
            }
        }
        if (!mPendingRequests.isEmpty()) {
            requestLoading();
        }
    }

    private void cacheBitmap(String url, byte[] bytes) {
        if (mPaused) {
            return;
        }

        BitmapHolder holder = new BitmapHolder();
        holder.state = BitmapHolder.LOADED;
        if (bytes != null) {
            try {
                Log.d(TAG, "cacheBitmap, bytes.length = "+bytes.length);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
                if (bitmap == null) {
                    Log.e(TAG,"cacheBitmap: bitmap decode is null");
                    ImageUtil.removeImage(url, mDb);
                    mBitmapCache.remove(url);
                    return;
                } else {
                    holder.bitmapRef = new SoftReference<Bitmap>(bitmap);
                }
            } catch (OutOfMemoryError e) {
                // Do nothing - the photo will appear to be missing
                Log.w(TAG, e);
            }
        }
        mBitmapCache.put(url, holder);
    }

    private void obtainPhotoIdsToLoad(ArrayList<String> urls) {
        urls.clear();

        Iterator<String> iterator = mPendingRequests.values().iterator();
        while (iterator.hasNext()) {
            String url = iterator.next();
            BitmapHolder holder = mBitmapCache.get(url);
            if (holder != null && holder.state == BitmapHolder.NEEDED) {
                // Assuming atomic behavior
                holder.state = BitmapHolder.LOADING;
                urls.add(url);
            }
        }
    }

    private class LoaderThread extends HandlerThread implements Callback {
        private Handler mLoaderThreadHandler;
        private final ArrayList<String> mImageUrls = new ArrayList<String>();

        public LoaderThread() {
            super(TAG);
        }

        public void requestLoading() {
            if (mLoaderThreadHandler == null) {
                mLoaderThreadHandler = new Handler(getLooper(), this);
            }
            mLoaderThreadHandler.sendEmptyMessage(0);
        }

        @Override
        public boolean handleMessage(Message msg) {
            loadImage();
            Message m = mMainThreadHandler.obtainMessage(MESSAGE_PHOTOS_LOADED);
            mMainThreadHandler.sendMessage(m);
            return true;
        }

        private void loadImage() {
            obtainPhotoIdsToLoad(mImageUrls);
            int count = mImageUrls.size();
            if (count == 0) {
                return;
            }
            try {
                ArrayList<String> temp = new ArrayList<String>();
                temp.addAll(mImageUrls);
                for (int i = 0; i < count; i++) {
                    String url = temp.get(i);
                    byte[] bytes = null;
                    if (ImageUtil.hasSdcard())
                        bytes = ImageUtil.readFromStore(url, mDb);
                    if (bytes == null) {
                        Log.d(TAG, "store is empty, getBytesFromUrl");
                        bytes = ImageUtil.getBytesFromUrl(url);
                        if (bytes != null && ImageUtil.hasSdcard())
                            ImageUtil.storeImage(bytes, url, mDb);
                    }
                    cacheBitmap(url, bytes);
                    mImageUrls.remove(url);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            count = mImageUrls.size();
            for (int i = 0; i < count; i++) {
                cacheBitmap(mImageUrls.get(i), null);
            }
        }

    }
}
