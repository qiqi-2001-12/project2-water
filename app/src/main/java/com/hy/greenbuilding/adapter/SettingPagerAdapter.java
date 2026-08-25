package com.hy.greenbuilding.adapter;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;
public class SettingPagerAdapter extends FragmentStateAdapter {

    private final List<Fragment> mFragments;
    private final List<String> mTitles;

    public SettingPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, List<Fragment> fragments, List<String> titles) {
        super(fragmentManager,lifecycle);
        this.mFragments = fragments;
        this.mTitles = titles;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getItemCount() {
        return mFragments.size();
    }

    public String getPageTitle(int position) {
        return mTitles.get(position);
    }
}
