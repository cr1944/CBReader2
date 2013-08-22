package cheng.app.cnbeta;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

public class PageFragmentAdapter extends FragmentPagerAdapter {
    static final int PAGE_NUMBER = 2;
    private boolean mShortPageWidth;

    public PageFragmentAdapter(FragmentManager fm, boolean shortPageWidth) {
        super(fm);
        mShortPageWidth = shortPageWidth;
    }

    @Override
    public Fragment getItem(int arg0) {
        PageListFragment f = new PageListFragment();
        Bundle arguments = new Bundle();
        arguments.putBoolean(PageListFragment.ARG_IS_TWO_PANE, false);
        arguments.putInt(PageListFragment.ARG_PAGE, arg0);
        f.setArguments(arguments);
        return f;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }

    @Override
    public int getCount() {
        return PAGE_NUMBER;
    }

    @Override
    public float getPageWidth(int position) {
        return mShortPageWidth ? 0.75f : 1.f;
    }
}
