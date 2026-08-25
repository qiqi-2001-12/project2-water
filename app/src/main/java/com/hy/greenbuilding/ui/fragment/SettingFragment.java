package com.hy.greenbuilding.ui.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.os.Handler;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import com.hy.greenbuilding.HyApplication;
import com.hy.greenbuilding.R;
import com.hy.greenbuilding.config.SaveControlInfo;
import com.hy.greenbuilding.event.ResetSystemEvent;
import com.hy.greenbuilding.event.RoomChangeEvent;
import com.hy.greenbuilding.event.SetStatusEvent;
import com.hy.greenbuilding.event.SettingUpdateEvent;
import com.hy.greenbuilding.event.VersionUpdateEvent;
import com.hy.greenbuilding.event.WeatherDataEvent;
import com.hy.greenbuilding.model.RoomInfo;
import com.hy.greenbuilding.protocol.FunctionObject;
import com.hy.greenbuilding.protocol.ResPonseInfo.CustomDataInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.ElectricityMeterInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.EnvironmentDataInfo;
import com.hy.greenbuilding.protocol.SpDataProcessor;
import com.hy.greenbuilding.protocol.command.ControlCommand;
import com.hy.greenbuilding.protocol.command.CustomCommand;
import com.hy.greenbuilding.protocol.command.MeterCommand;
import com.hy.greenbuilding.protocol.command.OTARequestCommand;
import com.hy.greenbuilding.ui.activity.ManagerActivity;
import com.hy.greenbuilding.ui.activity.SettingActivity;
import com.hy.greenbuilding.ui.widget.NoScrollViewPager;
import com.hy.greenbuilding.ui.widget.verticaltablayout.util.DisplayUtil;
import com.hy.greenbuilding.utils.AppManagerUtil;
import com.hy.greenbuilding.utils.ByteUtils;
import com.hy.greenbuilding.utils.MySpUtil;
import com.hy.greenbuilding.utils.StringUtils;
import com.hy.greenbuilding.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import butterknife.OnClick;

public class SettingFragment extends Fragment {
    @BindView(R.id.tab_layout)
    TabLayout mTabLayout;
    private List<RoomInfo> roomList = new ArrayList<>();

    private int currentTabIndex = 0;

    @BindView(R.id.tv_inRoom_temp)
    TextView mRoomTemp;
    @BindView(R.id.tv_inRoom_humidity)
    TextView mRoomHumidity;
    @BindView(R.id.tv_inRoom_co2)
    TextView mRoomCo2;
    @BindView(R.id.tv_inRoom_pm)
    TextView mRoomPm;

    @BindView(R.id.viewpager)
    NoScrollViewPager viewpager;
    private List<Fragment> list = new ArrayList<Fragment>();
    private SimpleFragmentAdapter simpleFragmentAdapter;

    public SettingFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        View inflate = inflater.inflate(R.layout.fragment_setting, container, false);
        ButterKnife.bind(this, inflate);

        simpleFragmentAdapter = new SimpleFragmentAdapter(getChildFragmentManager());
        list.add(SettingHumidityTempFragment.newInstance("dehumidify"));
        list.add(SettingHumidityTempFragment.newInstance("temperature"));
        viewpager.setScrollEnabled(false);
        viewpager.setAdapter(simpleFragmentAdapter);
        mTabLayout.setTabIndicatorFullWidth(false);
        mTabLayout.setupWithViewPager(viewpager);
        setupTabMargin();
        return inflate;
    }

    /**
     * 为 TabLayout 中的每个 Tab 设置间距
     */
    private void setupTabMargin() {
        // 获取 TabLayout 的布局参数
        ViewGroup slidingTabIndicator = (ViewGroup) mTabLayout.getChildAt(0);

        for (int i = 0; i < slidingTabIndicator.getChildCount(); i++) {
            View tab = slidingTabIndicator.getChildAt(i);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) tab.getLayoutParams();

            // 设置左右外边距
            // 第一个 Tab 只设置右边距
            if (i == 0) {
                params.setMargins(0, 0, dpToPx(10), 0);
            }
            // 最后一个 Tab 只设置左边距
            else if (i == slidingTabIndicator.getChildCount() - 1) {
                params.setMargins(dpToPx(10), 0, 0, 0);
            }
            // 中间的 Tab 设置左右边距
            else {
                params.setMargins(dpToPx(10), 0, dpToPx(10), 0);
            }

            tab.setLayoutParams(params);
        }
    }

    /**
     * 辅助方法：将 dp 单位转换为像素
     * @param dp 需要转换的 dp 值
     * @return 转换后的像素值
     */
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    public class SimpleFragmentAdapter extends FragmentPagerAdapter {
        private String[] tabTitles = new String[]{"控温", "控湿"};
        public SimpleFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return list.get(position);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void resetToDefaultView() {
        if (mTabLayout != null && mTabLayout.getTabCount() > 0) {
            mTabLayout.selectTab(mTabLayout.getTabAt(0));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWeatherDataUpdate(WeatherDataEvent event) {
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRoomEvent(EnvironmentDataInfo info) {
        if (info != null) {
            byte[] errorBytes = info.getRoomError();
            HyApplication.setRoomError(errorBytes);
            if (roomList != null) {
                roomList.clear();
                List<RoomInfo> mList = info.getRoomData(getActivity());
                RoomChangeEvent roomChangeEvent = new RoomChangeEvent("");
                EventBus.getDefault().post(roomChangeEvent);
                if (mList.size() > 0) {
                    roomList.addAll(mList);
                    SaveControlInfo saveControlInfo = MySpUtil.getControlData(getActivity());
                    if (!StringUtils.isNullOrEmpty(saveControlInfo.getTempMin()) && !StringUtils.isNullOrEmpty(saveControlInfo.getTempMax())) {
                        if (HyApplication.getOutTemp().intValue() < Integer.parseInt(saveControlInfo.getTempMin()) * 10) {
                            Collections.sort(roomList, new Comparator<RoomInfo>() {
                                public int compare(RoomInfo arg0, RoomInfo arg1) {
                                    return arg0.getTemp() - arg1.getTemp();
                                }
                            });
                        } else if (HyApplication.getOutTemp().intValue() > Integer.parseInt(saveControlInfo.getTempMax()) * 10) {
                            Collections.sort(roomList, new Comparator<RoomInfo>() {
                                public int compare(RoomInfo arg0, RoomInfo arg1) {
                                    return arg1.getTemp() - arg0.getTemp();
                                }
                            });
                        } else {
                            BigDecimal temp = new BigDecimal(saveControlInfo.getTempMin()).add(new BigDecimal(saveControlInfo.getTempMax()));
                            BigDecimal temp1 = temp.divide(new BigDecimal(2)).setScale(1, BigDecimal.ROUND_DOWN);
                            BigDecimal temp2 = temp1.multiply(new BigDecimal(10)).setScale(0, BigDecimal.ROUND_DOWN);
                            if (HyApplication.getOutTemp().intValue() < temp2.intValue()) {
                                Collections.sort(roomList, new Comparator<RoomInfo>() {
                                    public int compare(RoomInfo arg0, RoomInfo arg1) {
                                        return arg0.getTemp() - arg1.getTemp();
                                    }
                                });
                            } else {
                                Collections.sort(roomList, new Comparator<RoomInfo>() {
                                    public int compare(RoomInfo arg0, RoomInfo arg1) {
                                        return arg1.getTemp() - arg0.getTemp();
                                    }
                                });
                            }
                        }
                    }

                    if (roomList.get(0).getTemp() == 0) {
                        mRoomTemp.setText(roomList.get(0).getTemp() + "");
                        for (int i = 0; i < roomList.size(); i++) {
                            if (roomList.get(i).getTemp() != 0) {
                                mRoomTemp.setText(roomList.get(i).getTemp() + "");
                                break;
                            }
                        }
                    } else {
                        mRoomTemp.setText(roomList.get(0).getTemp() + "");
                    }

                    Collections.sort(roomList, new Comparator<RoomInfo>() {
                        public int compare(RoomInfo arg0, RoomInfo arg1) {
                            return arg1.getHumidity() - arg0.getHumidity();
                        }
                    });
                    mRoomHumidity.setText(roomList.get(0).getHumidity() + "");

                    Collections.sort(roomList, new Comparator<RoomInfo>() {
                        public int compare(RoomInfo arg0, RoomInfo arg1) {
                            return arg1.getCo2() - arg0.getCo2();
                        }
                    });
                    mRoomCo2.setText(roomList.get(0).getCo2() + "");

                    Collections.sort(roomList, new Comparator<RoomInfo>() {
                        public int compare(RoomInfo arg0, RoomInfo arg1) {
                            return arg1.getPm() - arg0.getPm();
                        }
                    });
                    mRoomPm.setText(roomList.get(0).getPm() + "");
                }
            }
        }
    }

}