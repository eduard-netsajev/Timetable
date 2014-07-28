package org.edunet.timetable;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


class TTableFragmentAdapter extends FragmentPagerAdapter {
    protected static final String[] CONTENT = new String[] {
            "E1", "T1", "K1", "N1", "R1", "L1",
            "E2", "T2", "K2", "N2", "R2", "L2"
    };

    private int mCount = CONTENT.length*10001;

    public TTableFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return TTableFragment.newInstance(CONTENT[position % CONTENT.length]);
    }

    @Override
    public int getCount() {
        return mCount;
    }


    @Override
    public CharSequence getPageTitle(int position) {
      return TTableFragmentAdapter.CONTENT[position % CONTENT.length];
    }

    /*
    public void setCount(int count) {
        if (count > 0 && count <= 10) {
            mCount = 12;
            notifyDataSetChanged();
        }
    }
    */
}