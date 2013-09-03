package cheng.app.cnbeta;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;
import cheng.app.cnbeta.data.CBContract;
import cheng.app.cnbeta.data.CBContract.CacheColumns;
import cheng.app.cnbeta.data.CBContract.HmColumns;
import cheng.app.cnbeta.data.CBContract.NewsColumns;
import cheng.app.cnbeta.util.DataUtil;

/**
 * A fragment representing a single Page detail screen.
 * This fragment is either contained in a {@link PageListActivity}
 * in two-pane mode (on tablets) or a {@link PageDetailActivity}
 * on handsets.
 */
public class PageDetailFragment extends Fragment implements LoaderCallbacks<Cursor> {
    private static final String TAG = "PageDetailFragment";
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_PAGE_ID = "page_id";
    public static final String ARG_ITEM_ID = "item_id";
    private long mItemId = -1;
    private int mPageId = 0;
    private long mNewsId = -1;
    private Callbacks mCallbacks = sDummyCallbacks;
    private WebView mWebView;
    private TextView mEmptyView;
    private ProgressBar mProgressBar;
    View mContentView;
    private boolean mLoading;

    public interface Callbacks {
        public void onLoaded(int cmt);
        public void onUpdateLoading(boolean loading);
    }
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onLoaded(int cmt) {
            Log.e(TAG, "onLoaded, no activity this fragment attached!");
        }
        @Override
        public void onUpdateLoading(boolean loading) {
            Log.e(TAG, "onUpdateLoading, no activity this fragment attached!");
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PageDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(ARG_ITEM_ID)) {
                mItemId = args.getLong(ARG_ITEM_ID);
            }
            if (args.containsKey(ARG_PAGE_ID)) {
                mPageId = args.getInt(ARG_PAGE_ID);
            }
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }
        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mCallbacks = sDummyCallbacks;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_page_detail, container, false);
        mWebView = (WebView) rootView.findViewById(R.id.page_detail);
        mEmptyView = (TextView) rootView.findViewById(android.R.id.empty);
        mProgressBar = (ProgressBar) rootView.findViewById(android.R.id.progress);
        mContentView = rootView.findViewById(R.id.listContainer);
        mWebView.setBackgroundColor(getResources().getColor(R.color.webview_bg));
        WebSettings s = mWebView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDefaultTextEncodingName("utf-8");
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setContentShown(false, true);
        refresh();
    }

    private void refresh() {
        mLoading = true;
        mCallbacks.onUpdateLoading(true);
        getLoaderManager().restartLoader(100, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mCallbacks.onUpdateLoading(mLoading);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refresh();
                return true;
        }
        return false;
    }

    private void setContentShown(boolean shown, boolean animate) {
        if ((mContentView.getVisibility() == View.VISIBLE) == shown) {
            return;
        }
        if (shown) {
            if (animate) {
                mProgressBar.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
                mContentView.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
            } else {
                mProgressBar.clearAnimation();
                mContentView.clearAnimation();
            }
            mProgressBar.setVisibility(View.GONE);
            mContentView.setVisibility(View.VISIBLE);
        } else {
            if (animate) {
                mProgressBar.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
                mContentView.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
            } else {
                mProgressBar.clearAnimation();
                mContentView.clearAnimation();
            }
            mProgressBar.setVisibility(View.VISIBLE);
            mContentView.setVisibility(View.GONE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        if (mPageId == PageListFragment.PAGE_HM) {
            Uri uri = Uri.withAppendedPath(CBContract.HM_CONTENT_URI, String.valueOf(mItemId));
            return new CursorLoader(getActivity(), uri, null, null, null, null);
        } else {
            Uri uri = Uri.withAppendedPath(CBContract.NEWS_CONTENT_URI, String.valueOf(mItemId));
            return new CursorLoader(getActivity(), uri, null, null, null, null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
        if (arg1 != null && arg1.moveToFirst()) {
            Log.d(TAG, "onLoadFinished");
            int cmtNumber;
            String title;
            if (mPageId == PageListFragment.PAGE_HM) {
                mNewsId = arg1.getLong(arg1.getColumnIndex(HmColumns.ARTICLE_ID));
                int cmtClosed = arg1.getInt(arg1.getColumnIndex(HmColumns.CMT_CLOSED));
                cmtNumber = arg1.getInt(arg1.getColumnIndex(HmColumns.CMT_NUMBER));
                cmtNumber = cmtClosed > 0 ? -1 : cmtNumber;
                title = arg1.getString(arg1.getColumnIndex(HmColumns.TITLE));
            } else {
                mNewsId = arg1.getLong(arg1.getColumnIndex(NewsColumns.ARTICLE_ID));
                int cmtClosed = arg1.getInt(arg1.getColumnIndex(NewsColumns.CMT_CLOSED));
                cmtNumber = arg1.getInt(arg1.getColumnIndex(NewsColumns.CMT_NUMBER));
                cmtNumber = cmtClosed > 0 ? -1 : cmtNumber;
                title = arg1.getString(arg1.getColumnIndex(NewsColumns.TITLE));
            }
            mCallbacks.onLoaded(cmtNumber);
            new LoadNewsTask(this, mNewsId, title, cmtNumber).execute();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        Log.d(TAG, "onLoaderReset, do nothing");
    }

    private static class LoadNewsTask extends AsyncTask<Void, Void, String> {
        private WeakReference<PageDetailFragment> mFragment;
        private ContentResolver mCr;
        private long mArticleId;
        private String mTitle;
        private int mCmtNumber;

        public LoadNewsTask(PageDetailFragment f, long articleId, String title, int cmtNumber) {
            mFragment = new WeakReference<PageDetailFragment>(f);
            mCr = f.getActivity().getContentResolver();
            mArticleId = articleId;
            mTitle = title;
            mCmtNumber = cmtNumber;
        }

        @Override
        protected String doInBackground(Void... arg0) {
            boolean hasSdCard = DataUtil.hasSdcard();
            String result = DataUtil.readNews(mArticleId, hasSdCard);
            if (hasSdCard) {
                mCr.delete(Uri.withAppendedPath(CBContract.CACHE_CONTENT_URI, String.valueOf(mArticleId)),
                        null, null);
                if (!TextUtils.isEmpty(result)) {
                    ContentValues values = new ContentValues();
                    values.put(CacheColumns.ARTICLE_ID, mArticleId);
                    values.put(CacheColumns.TITLE, mTitle);
                    values.put(CacheColumns.CMT_NUMBER, mCmtNumber);
                    mCr.insert(CBContract.CACHE_CONTENT_URI, values);
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            final PageDetailFragment f = mFragment.get();
            if (f != null && f.isResumed()) {
                f.setContentShown(true, true);
                f.mLoading = false;
                f.mCallbacks.onUpdateLoading(false);
                if (!TextUtils.isEmpty(result)) {
                    f.mWebView.loadDataWithBaseURL(null, result, "text/html", "utf-8", null);
                    f.mWebView.setVisibility(View.VISIBLE);
                    f.mEmptyView.setVisibility(View.GONE);
                } else {
                    f.mEmptyView.setVisibility(View.VISIBLE);
                    f.mWebView.setVisibility(View.GONE);
                }
            }
        }
    }
}
