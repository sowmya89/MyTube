package com.example.lab2.myowntube;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

public class TabsPagerAdapter extends FragmentStatePagerAdapter {

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }
    private String[] tabs = {"Search", "Favorite"};

    @Override
    public Fragment getItem(int pos) {
        switch(pos) {

            case 0: return SearchFragment.newInstance();
            case 1: return FavoriteFragment.newInstance();
            default: return SearchFragment.newInstance();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabs[position];
    }
}
