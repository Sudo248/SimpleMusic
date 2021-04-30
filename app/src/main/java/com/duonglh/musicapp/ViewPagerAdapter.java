package com.duonglh.musicapp;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private final PlayListFragment playListFragment = new PlayListFragment();
    private final DownloadMusicFragment downloadMusicFragment = new DownloadMusicFragment();
    private final RankFragment rankFragment = new RankFragment();
    private int currentFragment = 0, newFragment = 0;


    public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return playListFragment;
            case 1:
                return downloadMusicFragment;
            case 2:
                return rankFragment;
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    public int getCurrentFragment(){
        if(newFragment > currentFragment) currentFragment = newFragment - 1;
        else if(currentFragment < newFragment) currentFragment = newFragment + 1;
        Log.e("Fragment","new Fragment: "+newFragment+" Current Fragment: "+currentFragment);
        return currentFragment;
    }
}