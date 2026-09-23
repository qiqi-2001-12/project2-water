package com.hy.greenbuilding.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.hy.greenbuilding.R;
import com.hy.greenbuilding.modbus.WaterUnitPollingController;

/**
 * 水机管理界面。
 *
 * 当前仅提供参数展示 UI；不读取、不保存，也不下发 Modbus 数据。
 */
public class WaterUnitFragment extends BaseDialogFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogFullScreen);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.water_unit_main, container, false);
        ImageView backView = root.findViewById(R.id.water_unit_back);
        backView.setOnClickListener(v -> dismiss());

        ViewPager viewPager = root.findViewById(R.id.water_unit_viewpager);
        TabLayout tabLayout = root.findViewById(R.id.water_unit_tabs);
        viewPager.setAdapter(new WaterUnitPagerAdapter(getChildFragmentManager()));
        viewPager.setOffscreenPageLimit(2);
        tabLayout.setTabIndicatorFullWidth(false);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == 1) {
                    WaterUnitPollingController.getInstance().readSettings();
                }
            }
        });

        return root;
    }

    private static class WaterUnitPagerAdapter extends FragmentPagerAdapter {
        private static final String[] TITLES = {"状态参数", "设置信息"};

        WaterUnitPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            return WaterUnitCategoryFragment.newInstance(position == 0);
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }
    }
}
