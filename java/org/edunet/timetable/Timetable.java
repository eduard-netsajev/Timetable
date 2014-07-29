package org.edunet.timetable;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Random;
import android.support.v4.app.FragmentActivity;

public class Timetable extends FragmentActivity {

    TTableFragmentAdapter mAdapter;
    ViewPager mPager;
    PageIndicator mIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable);

        mAdapter = new TTableFragmentAdapter(getSupportFragmentManager());

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);

        //Find is it even or odd week, and current day

        Calendar today = new GregorianCalendar(Locale.UK);
        int dayOfWeek = today.get(Calendar.DAY_OF_WEEK) - 1;//returns int Day of Week

        boolean evenWeek;
        //Doesn't mean truly even week of the year
        //Must be even/odd according to TTU schedule
        //In future, can be reworked to work so that
        //1st of September is first odd week and counting from there
        evenWeek = today.get(Calendar.WEEK_OF_YEAR) % 2 != 0;

        if (dayOfWeek == 0) {
        dayOfWeek++;
        evenWeek = !evenWeek;
        }


        int initialPage = 5003 + dayOfWeek;
        if(evenWeek){
            initialPage += 6;
        }
        mPager.setCurrentItem(initialPage);

        //We set this on the indicator, NOT the pager
        mIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                //Toast.makeText(Timetable.this, "Changed to page " + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.random:
                int page = 4;
                mPager.setCurrentItem(page);
                return true;
            /*
            case R.id.add_page:
                if (mAdapter.getCount() < 10) {
                    mAdapter.setCount(mAdapter.getCount() + 1);
                    mIndicator.notifyDataSetChanged();
                }
                return true;

            case R.id.remove_page:
                if (mAdapter.getCount() > 1) {
                    mAdapter.setCount(mAdapter.getCount() - 1);
                    mIndicator.notifyDataSetChanged();
                }
                return true;*/
        }
        return super.onOptionsItemSelected(item);
    }


}

