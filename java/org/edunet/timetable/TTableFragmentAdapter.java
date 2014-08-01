package org.edunet.timetable;


import android.app.ProgressDialog;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.widget.ListView;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;


class TTableFragmentAdapter extends FragmentPagerAdapter {
    protected static final int[] CONTENT = new int[] {
            10, 11, 12, 13, 14, 15,
            20, 21, 22, 23, 24, 25
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
    public int getItemPosition(Object item) {
        //this shit makes notifyDataSetChanged work
        /*
        By default, getItemPosition() returns POSITION_UNCHANGED, which means,
        "This object is fine where it is, don't destroy or remove it."
        Returning POSITION_NONE fixes the problem by instead saying,
        "This object is no longer an item I'm displaying, remove it."
        So it has the effect of removing and recreating every single item in your adapter.
         */
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return mCount;
    }


    @Override
    public CharSequence getPageTitle(int position) {
      return ""+TTableFragmentAdapter.CONTENT[position % CONTENT.length];
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