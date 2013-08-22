
package cheng.app.cnbeta;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * An activity representing a list of Pages. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link PageDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link PageListFragment} and the item details (if present) is a
 * {@link PageDetailFragment}.
 * <p>
 * This activity also implements the required {@link PageListFragment.Callbacks}
 * interface to listen for item selections.
 */
public class PageListActivity extends FragmentActivity implements PageListFragment.Callbacks,
        TabListener, OnPageChangeListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private PageFragmentAdapter mPageFragmentAdapter;
    private ViewPager mViewPager;
    private PullToRefreshAttacher mPullToRefreshAttacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_list);
        mViewPager = (ViewPager) findViewById(R.id.list_pager);
        mPullToRefreshAttacher = PullToRefreshAttacher.get(this);

        if (mViewPager == null) {
            mTwoPane = true;
        }
        final ActionBar bar = getActionBar();
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME,
                ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
        final FragmentManager fm = getSupportFragmentManager();
        if (!mTwoPane) {
            mPageFragmentAdapter = new PageFragmentAdapter(fm);
            mViewPager.setAdapter(mPageFragmentAdapter);
            mViewPager.setOnPageChangeListener(this);
            bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            bar.addTab(bar.newTab()
                    .setText(R.string.page_title_news)
                    .setTabListener(this));
            bar.addTab(bar.newTab()
                    .setText(R.string.page_title_comments)
                    .setTabListener(this));
        } else {
            FragmentTransaction ft = fm.beginTransaction();
            Bundle arg1 = new Bundle();
            arg1.putBoolean(PageListFragment.ARG_IS_TWO_PANE, true);
            arg1.putInt(PageListFragment.ARG_PAGE, 0);
            PageListFragment f1 = new PageListFragment();
            f1.setArguments(arg1);
            ft.add(R.id.fragment_news_list, f1);
            Bundle arg2 = new Bundle();
            arg2.putBoolean(PageListFragment.ARG_IS_TWO_PANE, true);
            arg2.putInt(PageListFragment.ARG_PAGE, 1);
            PageListFragment f2 = new PageListFragment();
            f2.setArguments(arg2);
            ft.add(R.id.fragment_hot_comments, f2);
            ft.commit();
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    PullToRefreshAttacher getPullToRefreshAttacher() {
        return mPullToRefreshAttacher;
    }

    /**
     * Callback method from {@link PageListFragment.Callbacks} indicating that
     * the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        Intent detailIntent = new Intent(this, PageDetailActivity.class);
        detailIntent.putExtra(PageDetailFragment.ARG_ITEM_ID, id);
        startActivity(detailIntent);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return true;
    }

    @Override
    public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTabSelected(Tab tab, android.app.FragmentTransaction ft) {
        if (!mTwoPane)
            mViewPager.setCurrentItem(tab.getPosition());

    }

    @Override
    public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onPageSelected(int arg0) {
        if (!mTwoPane)
            getActionBar().setSelectedNavigationItem(arg0);
    }
}
