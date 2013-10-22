package cheng.app.cnbeta;

import java.lang.ref.WeakReference;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher.OnRefreshListener;
import cheng.app.cnbeta.data.CBContract.HmColumns;
import cheng.app.cnbeta.data.CBContract.NewsColumns;
import cheng.app.cnbeta.lib.EndlessAdapter;
import cheng.app.cnbeta.util.Configs;
import cheng.app.cnbeta.util.HttpUtil;
import cheng.app.cnbeta.util.DataUtil;
import cheng.app.cnbeta.util.Utils;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;

/**
 * A list fragment representing a list of Pages. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link PageDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class PageListFragment extends ListFragment implements
    LoaderManager.LoaderCallbacks<Cursor>, OnRefreshListener {
    private static final String TAG = "PageListFragment";

    private static final String STATE_LAST_ITEM_ID = "last_item_id";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    public static final int PAGE_NEWS = 0;
    public static final int PAGE_HM = 1;
    //private int mActivatedPosition = ListView.INVALID_POSITION;
    private boolean mIsReCreated = false;
    private boolean mIsLoading = false;
    public static final String ARG_PAGE = "page";
    public static final String ARG_LAST_ID = "last_id";
    AutoLoadAdapter mAdapter;
    private PullToRefreshAttacher mPullToRefreshAttacher;
    private Menu mOptionsMenu;
    private int mPageId;
    private long mLastItemId = -1;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(int pageId, long id);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(int pageId, long id) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PageListFragment() {
    }

    public int getPageId() {
        return mPageId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIsReCreated = savedInstanceState != null;
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPullToRefreshAttacher = ((PageListActivity) getActivity())
                .getPullToRefreshAttacher();
        mPullToRefreshAttacher.addRefreshableView(getListView(), this);
        if (getArguments().containsKey(ARG_PAGE)) {
            mPageId = getArguments().getInt(ARG_PAGE, PAGE_NEWS);
        }
        // Restore the previously serialized activated item position.
        if (savedInstanceState != null) {
            if(savedInstanceState.containsKey(STATE_LAST_ITEM_ID)) {
                mLastItemId = savedInstanceState.getLong(STATE_LAST_ITEM_ID);
            }
        }
        mAdapter = new AutoLoadAdapter(getActivity(), new PageListAdapter(getActivity(), mPageId));
        mAdapter.setRunInBackground(false);
        setListAdapter(mAdapter);

        // Start out with a progress indicator.
        setListShown(false);

        if (mIsReCreated) {
            mIsLoading = true;
            Bundle args = new Bundle();
            args.putLong(ARG_LAST_ID, mLastItemId);
            getLoaderManager().restartLoader(mPageId, args, this);
        } else {
            refresh(-1);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.setFontSize();

    }

    void refresh(long lastId) {
        if (!mIsLoading) {
            Log.d(TAG, "[" + mPageId + "]start refresh from net, lastId=" + lastId);
            new PageListAsyncTask(this).execute(lastId);
        } else {
            Log.e(TAG, "[" + mPageId + "]already in loading!");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v =  super.onCreateView(inflater, container, savedInstanceState);
        FrameLayout root = (FrameLayout) inflater.inflate(R.layout.list_container, null);
        root.addView(v);
        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ListView lv = getListView();
        lv.setDrawSelectorOnTop(true);
        lv.setDivider(view.getResources().getDrawable(android.R.color.transparent));
        lv.setDividerHeight(view.getResources().getDimensionPixelSize(R.dimen.multipane_padding));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflater.inflate(R.menu.main_refresh_action, menu);
        mOptionsMenu = menu;
        triggerRefresh(mIsLoading);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refresh(-1L);
                return true;
        }
        return false;
    }

    private void triggerRefresh(boolean refreshing) {
        if (mOptionsMenu == null) {
            return;
        }

        final MenuItem refreshItem = mOptionsMenu.findItem(R.id.action_refresh);
        if (refreshItem != null) {
            if (refreshing) {
                refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
            } else {
                refreshItem.setActionView(null);
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onRefreshStarted(View view) {
        refresh(-1L);
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        Cursor c = (Cursor) mAdapter.getItem(position);
        long itemId = -1;
        if (mPageId == PAGE_HM) {
            itemId = c.getLong(c.getColumnIndex(HmColumns.HMID));
        } else {
            itemId = c.getLong(c.getColumnIndex(NewsColumns.ARTICLE_ID));
        }
        mCallbacks.onItemSelected(mPageId, itemId);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(STATE_LAST_ITEM_ID, mLastItemId);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        long lastId = -1;
        if (arg1 != null) {
            if (arg1.containsKey(ARG_LAST_ID))
                lastId = arg1.getLong(ARG_LAST_ID);
        }
        Log.d(TAG, "[" + arg0 + "]onCreateLoader, lastId=" + lastId);
        PageListLoader cl = new PageListLoader(getActivity(), arg0, lastId);
        cl.setUpdateThrottle(2000); // update at most every 2 seconds.
        return cl;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
        mAdapter.changeData(arg1);
        if (arg1 != null && arg1.moveToLast()) {
            if (mPageId == PAGE_HM)
                mLastItemId = arg1.getLong(arg1.getColumnIndex(HmColumns.HMID));
            else
                mLastItemId = arg1.getLong(arg1.getColumnIndex(NewsColumns.ARTICLE_ID));
            Log.d(TAG, "[" + mPageId + "]onLoadFinished, mLastItemId=" + mLastItemId);
        }

        // The list should now be shown.
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
        mIsLoading = false;
        triggerRefresh(false);
        mPullToRefreshAttacher.setRefreshComplete();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        mAdapter.changeData(null);
    }

    private static class PageListAsyncTask extends AsyncTask<Long, Void, Long> {
        private WeakReference<PageListFragment> mFragment;
        private ContentResolver mCr;
        private long mLastId;//this is the last item of the list

        @Override
        protected void onPreExecute() {
            final PageListFragment fragment = mFragment.get();
            if (fragment != null) {
                fragment.mIsLoading = true;
                fragment.triggerRefresh(true);
            }
        }

        public PageListAsyncTask(PageListFragment fragment) {
            mFragment = new WeakReference<PageListFragment>(fragment);
            mCr = fragment.getActivity().getContentResolver();
        }

        @Override
        protected Long doInBackground(Long... params) {
            final PageListFragment fragment = mFragment.get();
            mLastId = params[0];
            if (fragment != null) {
                int pageId = fragment.getPageId();
                if (pageId == PAGE_HM) {
                    return loadHM(mLastId);
                } else {
                    return loadNewsList(mLastId);
                }
            } else {
                return -1L;
            }
        }

        private long loadNewsList(long lastId) {
            String url = Configs.NEWSLIST_URL + Configs.LIMIT;
            if (lastId > 0) {
                url += (Configs.NEWSLIST_PAGE + lastId);
            }
            String html = HttpUtil.getInstance().httpGet(url);
            return DataUtil.parseAndSaveNewsList(html, mCr);
        }

        private long loadHM(long lastId) {
            String url = Configs.HMCOMMENT_URL + Configs.LIMIT;
            if (lastId > 0) {
                url += (Configs.HMCOMMENT_PAGE + lastId);
            }
            String html = HttpUtil.getInstance().httpGet(url);
            return DataUtil.parseAndSaveHotComments(html, mCr);
        }

        @Override
        protected void onPostExecute(Long result) {
            final PageListFragment fragment = mFragment.get();
            if (fragment != null && fragment.isResumed()) {
                Log.d(TAG, "[" + fragment.getPageId() + "]onPostExecute, last saved id is " + result);
                // Prepare the loader.  Either re-connect with an existing one,
                // or start a new one.
                Bundle args = new Bundle();
                args.putLong(ARG_LAST_ID, result);
                fragment.getLoaderManager().restartLoader(fragment.mPageId, args, fragment);
            }
        }
    }

    class AutoLoadAdapter extends EndlessAdapter {
        Context mContext;
        PageListAdapter mAdapter;

        public AutoLoadAdapter(Context c, PageListAdapter wrapped) {
            super(wrapped);
            mContext = c;
            mAdapter = wrapped;
        }

        @Override
        protected View getPendingView(ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View v = inflater.inflate(R.layout.list_loading_layout, null);
            return v;
        }

        @Override
        protected boolean cacheInBackground() throws Exception {
            Log.i(TAG, "start cacheInBackground");
            refresh(mLastItemId);
            return true;
        }

        @Override
        protected void appendCachedData() {
            // TODO Auto-generated method stub
        }

        public void changeData(Cursor c) {
            mAdapter.swapCursor(c);
            onDataReady();
        }

        public void setFontSize() {
            String textSize = Utils.getSharedPreferences(getActivity(), Utils.PREFERENCE_FONT_SIZE,
                    Utils.PREFERENCE_FONT_SIZE_DEFAULT);
            mAdapter.setFontSize(Integer.parseInt(textSize));
        }

        @Override
        public Object getItem(int position) {
            return mAdapter.getItem(position);
        }
    }

}
