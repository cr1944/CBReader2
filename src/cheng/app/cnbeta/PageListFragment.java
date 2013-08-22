package cheng.app.cnbeta;

import java.lang.ref.WeakReference;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher.OnRefreshListener;
import cheng.app.cnbeta.util.Configs;
import cheng.app.cnbeta.util.HttpUtil;
import cheng.app.cnbeta.util.JSONUtil;
import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
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
    LoaderManager.LoaderCallbacks<Cursor>, OnClickListener, OnRefreshListener {
    private static final String TAG = "PageListFragment";

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

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
    private int mActivatedPosition = ListView.INVALID_POSITION;
    private boolean mTwoPane;
    private boolean mIsReCreated = false;
    private boolean mIsLoading = false;
    public static final String ARG_IS_TWO_PANE = "is_two_pane";
    public static final String ARG_PAGE = "page";
    PageListAdapter mAdapter;
    private PullToRefreshAttacher mPullToRefreshAttacher;
    private Menu mOptionsMenu;
    private int mPageId;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(String id);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id) {
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
        if (getArguments().containsKey(ARG_IS_TWO_PANE)) {
            mTwoPane = getArguments().getBoolean(ARG_IS_TWO_PANE);
        }
        if (getArguments().containsKey(ARG_PAGE)) {
            mPageId = getArguments().getInt(ARG_PAGE, PAGE_NEWS);
        }
        // In two-pane mode, list items should be given the
        // 'activated' state when touched.
        if (mTwoPane) {
            setActivateOnItemClick(true);
        }
        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
        mAdapter = new PageListAdapter(getActivity(), mPageId);
        setListAdapter(mAdapter);

        // Start out with a progress indicator.
        setListShown(false);

        mIsLoading = true;
        if (mIsReCreated)
            getLoaderManager().initLoader(0, null, this);
        else
            refresh(-1L);
    }

    void refresh(long lastId) {
        triggerRefresh(true);
        new PageListAsyncTask(this).execute(lastId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v =  super.onCreateView(inflater, container, savedInstanceState);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.e(TAG, "onCreateOptionsMenu");
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

    void triggerRefresh(boolean refreshing) {
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
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onItemSelected("");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    @Override
    public void onClick(View arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        PageListLoader cl = new PageListLoader(getActivity(), mPageId);
        cl.setUpdateThrottle(2000); // update at most every 2 seconds.
        return cl;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
        mAdapter.swapCursor(arg1);

        // The list should now be shown.
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
        mIsLoading = false;
        triggerRefresh(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        mAdapter.swapCursor(null);
    }

    private class PageListAsyncTask extends AsyncTask<Long, Void, Boolean> {
        private WeakReference<PageListFragment> mFragment;
        private ContentResolver mCr;

        public PageListAsyncTask(PageListFragment fragment) {
            mFragment = new WeakReference<PageListFragment>(fragment);
            mCr = fragment.getActivity().getContentResolver();
        }

        @Override
        protected Boolean doInBackground(Long... params) {
            final PageListFragment fragment = mFragment.get();
            boolean result = false;
            long lastId = params[0];
            if (fragment != null) {
                int pageId = fragment.getPageId();
                if (pageId == PAGE_HM) {
                    result = loadHM(lastId);
                } else {
                    result = loadNewsList(lastId);
                }
            }
            return result;
        }

        private boolean loadNewsList(long lastId) {
            String url = Configs.NEWSLIST_URL + Configs.LIMIT;
            if (lastId > 0) {
                url += (Configs.NEWSLIST_PAGE + lastId);
            }
            String html = HttpUtil.getInstance().httpGet(url);
            if (!TextUtils.isEmpty(html)) {
                return JSONUtil.parseAndSaveNewsList(html, mCr);
            }
            return false;
        }

        private boolean loadHM(long lastId) {
            String url = Configs.HMCOMMENT_URL + Configs.LIMIT;
            if (lastId > 0) {
                url += (Configs.HMCOMMENT_PAGE + lastId);
            }
            String html = HttpUtil.getInstance().httpGet(url);
            if (!TextUtils.isEmpty(html)) {
                return JSONUtil.parseAndSaveHotComments(html, mCr);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            final PageListFragment fragment = mFragment.get();
            if (fragment != null) {
                Activity activity = fragment.getActivity();
                if (activity != null && !activity.isFinishing()) {
                    // Prepare the loader.  Either re-connect with an existing one,
                    // or start a new one.
                    fragment.getLoaderManager().initLoader(0, null, fragment);
                    mPullToRefreshAttacher.setRefreshComplete();
                }
            }
        }
    }
}
