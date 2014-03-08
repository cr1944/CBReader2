
package cheng.app.cnbeta;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import cheng.app.cnbeta.util.HelpUtils;

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
public class PageListActivity extends ThemedFragmentActivity implements PageListFragment.Callbacks,
        TabListener, OnPageChangeListener {
    private static final String TAG = "PageListActivity";
    private boolean mTwoPane;
    private PageFragmentAdapter mPageFragmentAdapter;
    private ViewPager mViewPager;

    public static class DepthPageTransformer implements ViewPager.PageTransformer {

        @Override
        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position <= 0) {
                view.setPivotX(pageWidth);
                view.setPivotY(pageHeight / 2);
                view.setAlpha(1 + position);
                view.setRotationY(22.5f * position);
            } else if (position >= 0.25) {
                view.setPivotX(0);
                view.setPivotY(pageHeight / 2);
                view.setAlpha(1.25f - position);
                view.setRotationY(22.5f * position - 22.5f / 4);
            }
        }
    }

    public static class DepthPageTransformer2 implements ViewPager.PageTransformer {

        @Override
        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position == 0) {
                view.setAlpha(1);
                view.setRotationY(0);
            } else if (position < 0) {
                view.setPivotX(pageWidth);
                view.setPivotY(pageHeight / 2);
                view.setAlpha(1 + position / 2);
                view.setRotationY(22.5f * position);
            } else {
                view.setPivotX(0);
                view.setPivotY(pageHeight / 2);
                view.setAlpha(1 - position / 2);
                view.setRotationY(22.5f * position);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_list);
        mViewPager = (ViewPager) findViewById(R.id.list_pager);

        mTwoPane = getResources().getBoolean(R.bool.two_pane);
        boolean shortPageWidth = getResources().getBoolean(R.bool.short_page_width);
        final ActionBar bar = getActionBar();
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME,
                ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
        final FragmentManager fm = getSupportFragmentManager();
        float pageWidth = 1.f;
        if (mTwoPane) {
            pageWidth = 0.5f;
        } else if (shortPageWidth) {
            pageWidth = 0.75f;
            mViewPager.setPageTransformer(true, new DepthPageTransformer());
        } else {
            mViewPager.setPageTransformer(true, new DepthPageTransformer2());
        }
        mPageFragmentAdapter = new PageFragmentAdapter(fm, pageWidth);
        mViewPager.setAdapter(mPageFragmentAdapter);
        mViewPager.setOnPageChangeListener(this);
        if (!mTwoPane) {
            //mViewPager.setPageMarginDrawable(R.drawable.grey_border_inset_lr);
            //mViewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.page_margin_width));
            bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            bar.addTab(bar.newTab()
                    .setText(R.string.page_title_news)
                    .setTabListener(this));
            bar.addTab(bar.newTab()
                    .setText(R.string.page_title_comments)
                    .setTabListener(this));
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    /**
     * Callback method from {@link PageListFragment.Callbacks} indicating that
     * the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(int pageId, long id) {
        Intent detailIntent = new Intent(this, PageDetailActivity.class);
        detailIntent.putExtra(PageDetailFragment.ARG_PAGE_ID, pageId);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_abouts:
                HelpUtils.showAbout(this);
                return true;

            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
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
