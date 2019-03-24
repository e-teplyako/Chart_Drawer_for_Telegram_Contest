package com.example.android.telegramcontest;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ChartFragmentPagerAdapter extends FragmentPagerAdapter {

    private Context mContext;
    public List<Fragment> mFragments = new ArrayList<>();
    private List<String> mTabTitles = new ArrayList<>();

    public ChartFragmentPagerAdapter (FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int i) {
        return mFragments.get(i);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mTabTitles.get(position);
    }

    public void addFragment (PageFragment fragment, String tabtitle){
        mFragments.add(fragment);
        mTabTitles.add(tabtitle);
    }
}
