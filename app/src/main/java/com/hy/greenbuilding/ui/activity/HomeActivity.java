package com.hy.greenbuilding.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.hy.greenbuilding.HyApplication;
import com.hy.greenbuilding.R;
import com.hy.greenbuilding.event.TempSwitchUpdateEvent;
import com.hy.greenbuilding.ui.fragment.AddRoomFragment;
import com.hy.greenbuilding.ui.fragment.AntiFreezingFragment;
import com.hy.greenbuilding.ui.fragment.HomeFragment;
import com.hy.greenbuilding.ui.fragment.ManagerFragment;
import com.hy.greenbuilding.ui.fragment.SettingFragment;
import com.hy.greenbuilding.ui.fragment.UpTempErrorFragment;
import com.hy.greenbuilding.ui.widget.verticaltablayout.VerticalTabLayout;
import com.hy.greenbuilding.ui.widget.verticaltablayout.adapter.MyTabAdapter;
import com.hy.greenbuilding.ui.widget.verticaltablayout.widget.TabView;
import com.hy.greenbuilding.utils.MySpUtil;
import com.hy.greenbuilding.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;

public class HomeActivity extends BaseActivity {
    private VerticalTabLayout mTabLayout;
    private final String[] TAB_TITLES = {"首页", "设置", "房间环境", "设备管理"};
    private Fragment[] mFragments;
    private FragmentManager mFragmentManager;
    private static final String CURRENT_TAB_KEY = "current_tab";
    private int currentTabIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mTabLayout = findViewById(R.id.vertical_tab_layout);
        mFragmentManager = getSupportFragmentManager();
        if (savedInstanceState != null) {
            currentTabIndex = savedInstanceState.getInt(CURRENT_TAB_KEY, 0);
        }

        initFragments(savedInstanceState);
        setupTabLayout();

        if (savedInstanceState != null) {
            mTabLayout.post(new Runnable() {
                @Override
                public void run() {
                    mTabLayout.setTabSelected(currentTabIndex);
                }
            });
        }
    }

    /**
     * 在 Activity 被销毁前保存当前状态
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // 保存当前选中的 Tab 索引
        outState.putInt(CURRENT_TAB_KEY, currentTabIndex);
    }



    private final int[] NORMAL_ICONS = {
            R.drawable.icon_home_white,
            R.drawable.icon_settings_white,
            R.drawable.icon_room_management_white,
            R.drawable.icon_equipment_management_white
    };

    private final int[] SELECTED_ICONS = {
            R.drawable.icon_home,

            R.drawable.icon_settings,
            R.drawable.icon_room_management,
            R.drawable.icon_equipment_management
    };
    private void setupTabLayout() {
        mTabLayout.setTabAdapter(new MyTabAdapter(TAB_TITLES,NORMAL_ICONS,SELECTED_ICONS),(boolean) MySpUtil.getParam(HomeActivity.this, MySpUtil.OTA_STATUS, false));
        mTabLayout.addOnTabSelectedListener(new VerticalTabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabView tab, int position) {
                boolean isOtaOpen = (boolean) MySpUtil.getParam(HomeActivity.this, MySpUtil.OTA_STATUS, false);
                if (isOtaOpen) {
                    ToastUtil.showToast(HomeActivity.this, getString(R.string.server_not_permission));
                    return;
                }

                if (HyApplication.isLocking) {
                    ToastUtil.showToast(HomeActivity.this, getString(R.string.device_locked));
                    return;
                }

                // 更新当前索引
                currentTabIndex = position;
                showFragment(position);
            }

            @Override
            public void onTabReselected(TabView tab, int position) {
                boolean isOtaOpen = (boolean) MySpUtil.getParam(HomeActivity.this, MySpUtil.OTA_STATUS, false);
                if (isOtaOpen) {
                    ToastUtil.showToast(HomeActivity.this, getString(R.string.server_not_permission));
                    return;
                }

                if (HyApplication.isLocking) {
                    ToastUtil.showToast(HomeActivity.this, getString(R.string.device_locked));
                    return;
                }
                //处理 Tab 再次选中事件
                Fragment reselectedFragment = mFragments[position];
                if (reselectedFragment instanceof SettingFragment) {
                    ((SettingFragment) reselectedFragment).resetToDefaultView();
                }else if (reselectedFragment instanceof HomeFragment) {
                    ((HomeFragment) reselectedFragment).resetToDefaultView();
                }else if (reselectedFragment instanceof AddRoomFragment) {
                    ((AddRoomFragment) reselectedFragment).resetToDefaultView();
                }
            }
        });
    }

    private Fragment createNewFragment(int position) {
        switch (position) {
            case 0:
                return new HomeFragment();
            case 3:
                return new ManagerFragment();
            case 1:
                return new SettingFragment();
            case 2:
                return new AddRoomFragment();
            default:
                return new Fragment();
        }
    }

    private void initFragments(Bundle savedInstanceState) {
        mFragments = new Fragment[TAB_TITLES.length];
        FragmentTransaction transaction = mFragmentManager.beginTransaction();

        if (savedInstanceState == null) {
            // 仅初始化默认显示的Fragment（首页）
            mFragments[0] = createNewFragment(0);
            transaction.add(R.id.content_frame, mFragments[0], TAB_TITLES[0]);
            // 其余Fragment先置为null，首次切换时创建
            for (int i = 1; i < TAB_TITLES.length; i++) {
                mFragments[i] = null;
            }
            transaction.commitAllowingStateLoss();
        } else {
            // 恢复已创建的Fragment
            for (int i = 0; i < TAB_TITLES.length; i++) {
                mFragments[i] = mFragmentManager.findFragmentByTag(TAB_TITLES[i]);
            }
        }
    }

    // 改造showFragment方法，懒加载Fragment
    private void showFragment(int position) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        // 隐藏所有已显示的Fragment
        for (Fragment fragment : mFragments) {
            if (fragment != null && !fragment.isHidden()) {
                transaction.hide(fragment);
            }
        }
        // 若目标Fragment未创建，先创建并add
        if (mFragments[position] == null) {
            mFragments[position] = createNewFragment(position);
            transaction.add(R.id.content_frame, mFragments[position], TAB_TITLES[position]);
        }
        // 显示目标Fragment
        transaction.show(mFragments[position]);
        transaction.commitAllowingStateLoss();
        controlBaseLayoutVisibility(position == 0);
    }
}
