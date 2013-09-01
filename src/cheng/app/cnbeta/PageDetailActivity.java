
package cheng.app.cnbeta;

import cheng.app.cnbeta.PageDetailFragment.Callbacks;
import cheng.app.cnbeta.lib.SlidingUpPanelLayout;
import cheng.app.cnbeta.lib.SlidingUpPanelLayout.PanelSlideListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

/**
 * An activity representing a single Page detail screen.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than
 * a {@link PageDetailFragment}.
 */
public class PageDetailActivity extends FragmentActivity implements PanelSlideListener, Callbacks {
    static final String TAG = "PageDetailActivity";
    private static final String STATE_SLIDINGPANE_OPEN = "slidingpane_open";

    SlidingUpPanelLayout mSlidingUpPanelLayout;
    TextView mCommentTitleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_detail);

        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);
        mSlidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mSlidingUpPanelLayout.setPanelHeight(getResources().getDimensionPixelSize(
                R.dimen.slidingup_panel_height));
        mCommentTitleView = (TextView) findViewById(R.id.page_detail_comments_title);
        mSlidingUpPanelLayout.setDragView(mCommentTitleView);
        mSlidingUpPanelLayout
                .setShadowDrawable(getResources().getDrawable(R.drawable.above_shadow));
        mSlidingUpPanelLayout.setPanelSlideListener(this);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putLong(PageDetailFragment.ARG_ITEM_ID,
                    getIntent().getLongExtra(PageDetailFragment.ARG_ITEM_ID, -1));
            arguments.putInt(
                    PageDetailFragment.ARG_PAGE_ID,
                    getIntent().getIntExtra(PageDetailFragment.ARG_PAGE_ID,
                            PageListFragment.PAGE_NEWS));
            PageDetailFragment fragment = new PageDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.page_detail_container, fragment).commit();
        } else {
            if(savedInstanceState.containsKey(STATE_SLIDINGPANE_OPEN)) {
                boolean isExpanded = savedInstanceState.getBoolean(STATE_SLIDINGPANE_OPEN);
                if (isExpanded) {
                    getActionBar().hide();
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_SLIDINGPANE_OPEN, mSlidingUpPanelLayout.isExpanded());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                NavUtils.navigateUpTo(this, new Intent(this, PageListActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPanelCollapsed(View arg0) {
        // TODO Auto-generated method stub
        Log.d(TAG, "onPanelCollapsed");

    }

    @Override
    public void onPanelExpanded(View arg0) {
        // TODO Auto-generated method stub
        Log.d(TAG, "onPanelExpanded");

    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {
        if (slideOffset < 0.2) {
            if (getActionBar().isShowing()) {
                getActionBar().hide();
            }
        } else {
            if (!getActionBar().isShowing()) {
                getActionBar().show();
            }
        }
    }

    @Override
    public void onLoaded(int cmt) {
        if (cmt < 0) {
            if (mCommentTitleView != null)
                mCommentTitleView.setText(R.string.cmt_closed);
        } else {
            if (mCommentTitleView != null)
                mCommentTitleView.setText(getString(R.string.display_cmt, cmt));
        }
    }

    @Override
    public void onBackPressed() {
        if (mSlidingUpPanelLayout.isExpanded()) {
            mSlidingUpPanelLayout.collapsePane();
            return;
        }
        super.onBackPressed();
    }
}
