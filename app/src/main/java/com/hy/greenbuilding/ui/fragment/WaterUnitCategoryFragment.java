package com.hy.greenbuilding.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.hy.greenbuilding.R;

/** 水机状态或设置下的分类分页。 */
public class WaterUnitCategoryFragment extends Fragment {
    private static final String ARG_STATUS_PAGE = "status_page";
    private boolean statusPage;

    static WaterUnitCategoryFragment newInstance(boolean statusPage) {
        WaterUnitCategoryFragment fragment = new WaterUnitCategoryFragment();
        Bundle arguments = new Bundle();
        arguments.putBoolean(ARG_STATUS_PAGE, statusPage);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.water_unit_category_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        statusPage = getArguments() == null || getArguments().getBoolean(ARG_STATUS_PAGE, true);
        ViewPager viewPager = view.findViewById(R.id.water_category_viewpager);
        TabLayout tabLayout = view.findViewById(R.id.water_category_tabs);
        viewPager.setAdapter(new CategoryPagerAdapter(getChildFragmentManager(), statusPage));
        tabLayout.setupWithViewPager(viewPager);
        if (statusPage) {
            preserveStatusTabTextSize(tabLayout);
        }
    }

    /**
     * 状态页只有三个分类，统一等宽显示即可；使用固定 22sp 文本，避免 TabLayout
     * 在重新计算等宽布局时为了适配标题而自行缩小字号。
     */
    private void preserveStatusTabTextSize(TabLayout tabLayout) {
        for (int index = 0; index < tabLayout.getTabCount(); index++) {
            TabLayout.Tab tab = tabLayout.getTabAt(index);
            if (tab == null) {
                continue;
            }
            TextView label = new TextView(requireContext());
            label.setText(tab.getText());
            label.setTextSize(tabLayout.getTabCount() > 3 ? 18 : 22);
            label.setTextColor(tabLayout.getTabTextColors());
            label.setGravity(android.view.Gravity.CENTER);
            label.setSingleLine(true);
            label.setDuplicateParentStateEnabled(true);
            tab.setCustomView(label);
        }
    }

    void filter(String query) {
        for (Fragment fragment : getChildFragmentManager().getFragments()) {
            if (fragment instanceof WaterUnitParameterListFragment) {
                ((WaterUnitParameterListFragment) fragment).filter(query);
            }
        }
    }

    private static class CategoryPagerAdapter extends FragmentPagerAdapter {
        private final boolean statusPage;

        CategoryPagerAdapter(FragmentManager fragmentManager, boolean statusPage) {
            super(fragmentManager);
            this.statusPage = statusPage;
        }

        @Override
        public Fragment getItem(int position) {
            return WaterUnitParameterListFragment.newInstance(statusPage, position);
        }

        @Override
        public int getCount() {
            return WaterUnitParameterCatalog.getPageSections(statusPage).size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return WaterUnitParameterCatalog.getPageSections(statusPage).get(position).title;
        }
    }
}
