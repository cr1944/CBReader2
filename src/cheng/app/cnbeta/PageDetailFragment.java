package cheng.app.cnbeta;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cheng.app.cnbeta.data.CBContract;
import cheng.app.cnbeta.data.CBContract.HmColumns;
import cheng.app.cnbeta.data.CBContract.NewsColumns;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_page_detail, container, false);

        return rootView;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getLoaderManager().restartLoader(100, null, this);
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
            if (mPageId == PageListFragment.PAGE_HM) {
                mNewsId = arg1.getLong(arg1.getColumnIndex(HmColumns.ARTICLE_ID));
            } else {
                mNewsId = arg1.getLong(arg1.getColumnIndex(NewsColumns.ARTICLE_ID));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        Log.d(TAG, "onLoaderReset, do nothing");
    }
}
