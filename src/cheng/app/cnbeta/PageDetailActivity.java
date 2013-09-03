
package cheng.app.cnbeta;

import cheng.app.cnbeta.PageDetailFragment.Callbacks;
import cheng.app.cnbeta.lib.SlidingUpPanelLayout;
import cheng.app.cnbeta.lib.SlidingUpPanelLayout.PanelSlideListener;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * An activity representing a single Page detail screen.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than
 * a {@link PageDetailFragment}.
 */
public class PageDetailActivity extends FragmentActivity
    implements PanelSlideListener, Callbacks, DrawerListener {
    static final String TAG = "PageDetailActivity";
    private static final String STATE_SLIDINGPANE_OPEN = "slidingpane_open";

    private static final int LAYOUT_SLIDINGUP = 1;
    private static final int LAYOUT_DRAWER = 2;
    private int mLayoutState;

    SlidingUpPanelLayout mSlidingUpPanelLayout;
    DrawerLayout mDrawerLayout;
    LinearLayout mCommentsLayout;
    TextView mCommentTitleView;
    private Menu mOptionsMenu;
    private int mCmtNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_detail);

        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mSlidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mCommentsLayout = (LinearLayout) findViewById(R.id.page_detail_comments);
        if (mSlidingUpPanelLayout != null) {
            mLayoutState = LAYOUT_SLIDINGUP;
            mSlidingUpPanelLayout.setPanelHeight(getResources().getDimensionPixelSize(
                    R.dimen.slidingup_panel_height));
            mCommentTitleView = (TextView) findViewById(R.id.page_detail_comments_title);
            mSlidingUpPanelLayout.setDragView(mCommentTitleView);
            mSlidingUpPanelLayout
                    .setShadowDrawable(getResources().getDrawable(R.drawable.above_shadow));
            mSlidingUpPanelLayout.setPanelSlideListener(this);
        } else if (mDrawerLayout != null) {
            mLayoutState = LAYOUT_DRAWER;
            mDrawerLayout.setDrawerListener(this);
        } else {
            throw new IllegalStateException("must setup a SlidingUpPanelLayout or DrawerLayout.");
        }

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
                if (mLayoutState == LAYOUT_SLIDINGUP && isExpanded) {
                    getActionBar().hide();
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mLayoutState == LAYOUT_SLIDINGUP)
            outState.putBoolean(STATE_SLIDINGPANE_OPEN, mSlidingUpPanelLayout.isExpanded());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail_activity_actions, menu);
        mOptionsMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.action_cmt);
        if (mCmtNumber < 0) {
            menuItem.setTitle(R.string.cmt_closed);
        } else {
            menuItem.setTitle(getString(R.string.display_cmt, mCmtNumber));
        }
        menuItem.setVisible(mLayoutState == LAYOUT_DRAWER);
        if (mLayoutState == LAYOUT_DRAWER) {
            boolean drawerOpen = mDrawerLayout.isDrawerOpen(mCommentsLayout);
            menu.findItem(R.id.action_refresh).setVisible(!drawerOpen);
            menu.findItem(R.id.action_share).setVisible(!drawerOpen);
        }
        return super.onPrepareOptionsMenu(menu);
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
            case R.id.action_cmt:
                if (mDrawerLayout.isDrawerVisible(GravityCompat.END)) {
                    mDrawerLayout.closeDrawer(GravityCompat.END);
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.END);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void triggerRefresh(boolean refreshing) {
        if (mOptionsMenu == null) {
            return;
        }

        final MenuItem menuItem = mOptionsMenu.findItem(R.id.action_refresh);
        if (menuItem != null) {
            if (refreshing) {
                menuItem.setActionView(R.layout.actionbar_indeterminate_progress);
            } else {
                menuItem.setActionView(null);
            }
        }
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
    public void onDrawerClosed(View arg0) {
        invalidateOptionsMenu();
    }

    @Override
    public void onDrawerOpened(View arg0) {
        invalidateOptionsMenu();
    }

    @Override
    public void onDrawerSlide(View arg0, float arg1) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onDrawerStateChanged(int arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onLoaded(int cmt) {
        if (mLayoutState == LAYOUT_SLIDINGUP) {
            if (mCommentTitleView != null) {
                if (cmt < 0) {
                    mCommentTitleView.setText(R.string.cmt_closed);
                } else {
                    mCommentTitleView.setText(getString(R.string.display_cmt, cmt));
                }
            }
        } else {
            mCmtNumber = cmt;
            invalidateOptionsMenu();
        }
    }

    @Override
    public void onUpdateLoading(boolean loading) {
        triggerRefresh(loading);
    }

    @Override
    public void onBackPressed() {
        if (mLayoutState == LAYOUT_SLIDINGUP && mSlidingUpPanelLayout.isExpanded()) {
            mSlidingUpPanelLayout.collapsePane();
            return;
        }
        if (mLayoutState == LAYOUT_DRAWER && mDrawerLayout.isDrawerOpen(mCommentsLayout)) {
            mDrawerLayout.closeDrawer(mCommentsLayout);
            return;
        }
        super.onBackPressed();
    }
}
