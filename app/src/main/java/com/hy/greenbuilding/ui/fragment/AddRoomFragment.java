package com.hy.greenbuilding.ui.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hwellyi.smarthome.HYJniService;
import com.hwellyi.smarthome.PublicUse;
import com.hy.greenbuilding.HyApplication;
import com.hy.greenbuilding.R;
import com.hy.greenbuilding.adapter.AddRoomListAdapter;
import com.hy.greenbuilding.adapter.AddRoomTitleAdapter;
import com.hy.greenbuilding.adapter.ChangeAirAdapter;
import com.hy.greenbuilding.config.SaveControlInfo;
import com.hy.greenbuilding.event.RoomChangeEvent;
import com.hy.greenbuilding.event.WeatherDataEvent;
import com.hy.greenbuilding.model.AirQualityInfo;
import com.hy.greenbuilding.model.RoomInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.EnvironmentDataInfo;
import com.hy.greenbuilding.ui.widget.HorizontalItemDecoration;
import com.hy.greenbuilding.utils.MySpUtil;
import com.hy.greenbuilding.utils.StringUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 房间管理
 */
public class AddRoomFragment extends Fragment {

    @BindView(R.id.recycler_roomTitle)
    RecyclerView recyclerTitleView;
    @BindView(R.id.recycler_roomItem)
    RecyclerView recyclerItemView;
    @BindView(R.id.li_Room_item)
    LinearLayout mItemRoomView;
    @BindView(R.id.ll_allRoom)
    LinearLayout mBtAllRoom;
    @BindView(R.id.tv_item_roomName)
    TextView mItemNameView;
    @BindView(R.id.tv_roomNum)
    TextView mRoomNum;
    @BindView(R.id.item_temp)
    TextView itemTemp;
    @BindView(R.id.item_humidity)
    TextView itemHumidity;
    @BindView(R.id.item_pm2_5)
    TextView itemPm;
    @BindView(R.id.item_co2)
    TextView itemCo2;
    @BindView(R.id.item_fan_switch)
    TextView itemFanSwitch;

    @BindView(R.id.tv_air)
    TextView mAirText;
    @BindView(R.id.bt_change_air)
    Button mChangeAir;

    @BindView(R.id.bt_change_air_release)
    Button mReleaseAir;

    @BindView(R.id.tv_weather_temp)
    TextView tvWeatherTemp;

    @BindView(R.id.tv_weather_humidity)
    TextView tvWeatherHumidity;

    @BindView(R.id.tv_weather_pm2_5)
    TextView tvWeatherPm2_5;
    private List<RoomInfo> roomList = new ArrayList<>();
    @BindView(R.id.tv_inRoom_temp)
    TextView mRoomTemp;
    @BindView(R.id.tv_inRoom_humidity)
    TextView mRoomHumidity;
    @BindView(R.id.tv_inRoom_co2)
    TextView mRoomCo2;
    @BindView(R.id.tv_inRoom_pm)
    TextView mRoomPm;

    @BindView(R.id.tv_tvoc)
    TextView tvTvoc;

    @BindView(R.id.tv_formaldehyde)
    TextView tvFormaldehyde;

    @BindView(R.id.ll_bind)
    LinearLayout llBind;

    private View mView;
    private Context mContext;
    private List<RoomInfo> saveRoomData = new ArrayList<>();
    private AddRoomTitleAdapter titleAdapter;
    private AddRoomListAdapter listAdapter;
    private Unbinder unbinder;
    private AlertDialog mEditDialog;
    private AlertDialog mDeleteDialog;
    private AlertDialog mReleaseDialog;
    private int currentPosition = -1;
    private AirQualityInfo qualityInfo;
    private List<RoomInfo> roomData = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        mView = inflater.inflate(R.layout.add_room_main, null);
        mContext = this.getActivity();
        unbinder = ButterKnife.bind(this, mView);
        init(true);

        return mView;
    }

    private void init(boolean isInit) {
        String roomJson = MySpUtil.getParam(mContext, MySpUtil.ROOM_DATA, "").toString();
        if (!StringUtils.isNullOrEmpty(roomJson)) {
            saveRoomData = new Gson().fromJson(roomJson, new TypeToken<List<RoomInfo>>() {
            }.getType());
            Collections.sort(saveRoomData, new Comparator<RoomInfo>() {
                public int compare(RoomInfo arg0, RoomInfo arg1) {
                    return arg0.getRoomId() - arg1.getRoomId();
                }
            });
        }
        if (PublicUse.mJniFunCB != null) {
            String tempJsonString = PublicUse.mJniFunCB.onGetDeviceTypeInfo(0, (1 << HYJniService.SUB_DEVICE_TYPE_ENV_DETECTOR));
            qualityInfo = new Gson().fromJson(tempJsonString, AirQualityInfo.class);
        } else {
            qualityInfo = new AirQualityInfo();
        }
        if (saveRoomData.size() > 9) {
            for (int i = 9; i < saveRoomData.size(); i++) {
                if (qualityInfo.getDevlist() != null && qualityInfo.getDevlist().size() > 0) {
                    if (saveRoomData.get(i).getAirQualityId() == 0) {
                        saveRoomData.get(i).setTemp(0);
                        saveRoomData.get(i).setPm(0);
                        saveRoomData.get(i).setCo2(0);
                        saveRoomData.get(i).setHumidity(0);
                    } else {
                        for (int j = 0; j < qualityInfo.getDevlist().size(); j++) {
                            if (qualityInfo.getDevlist().get(j).getId() == saveRoomData.get(i).getAirQualityId()) {
                                saveRoomData.get(i).setTemp(qualityInfo.getDevlist().get(j).getTemp());
                                saveRoomData.get(i).setPm(qualityInfo.getDevlist().get(j).getPM25());
                                saveRoomData.get(i).setCo2(qualityInfo.getDevlist().get(j).getCO2());
                                saveRoomData.get(i).setHumidity(qualityInfo.getDevlist().get(j).getHumi());
                            }
                        }
                    }
                } else {
                    saveRoomData.get(i).setTemp(0);
                    saveRoomData.get(i).setPm(0);
                    saveRoomData.get(i).setCo2(0);
                    saveRoomData.get(i).setHumidity(0);
                }
            }
            MySpUtil.setParam(mContext, MySpUtil.ROOM_DATA, new Gson().toJson(saveRoomData));
        }

        roomData.clear();
        for (RoomInfo room : saveRoomData) {
            if (room.getTemp() != 0) { // 仅保留温度非0的房间
                roomData.add(room);
            }
        }
        mRoomNum.setText("（" + roomData.size() + "）");

        Collections.sort(roomData, new Comparator<RoomInfo>() {
            public int compare(RoomInfo arg0, RoomInfo arg1) {
                return arg0.getRoomId() - arg1.getRoomId();
            }
        });

        if (isInit) {
            initAdapter();
        } else {
            titleAdapter.setList(roomData);
            listAdapter.setList(roomData);
        }
    }

    public void resetToDefaultView() {
        recyclerItemView.setVisibility(View.VISIBLE);
        mItemRoomView.setVisibility(View.GONE);
        if (titleAdapter != null) {
            titleAdapter.setmPosition(-1);
            titleAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            onAllRoomClick(recyclerTitleView);
            resetToDefaultView();
        }
    }

    private void initAdapter() {
        titleAdapter = new AddRoomTitleAdapter(mContext, roomData);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerTitleView.setLayoutManager(layoutManager);
        recyclerTitleView.setAdapter(titleAdapter);
        recyclerTitleView.setItemAnimator(new DefaultItemAnimator());
//        recyclerTitleView.addItemDecoration(new DividerItemDecoration(mContext, 1));
        titleAdapter.setOnItemClickListener(new AddRoomTitleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                currentPosition = position;
                recyclerItemView.setVisibility(View.GONE);
                mItemRoomView.setVisibility(View.VISIBLE);
                llBind.setVisibility(View.GONE);
                titleAdapter.setmPosition(position);
                titleAdapter.notifyDataSetChanged();
                mItemNameView.setText(roomData.get(position).getRoomName() + "环境");
                itemTemp.setText(roomData.get(position).getTemp() + "");
                itemHumidity.setText(roomData.get(position).getHumidity() + "");
                itemPm.setText(roomData.get(position).getPm() + "");
                itemCo2.setText(roomData.get(position).getCo2() + "");
                itemFanSwitch.setText(roomData.get(position).getAirValve() + "");
                mAirText.setText(TextUtils.isEmpty(roomData.get(position).getAirQualityName()) ? "" : roomData.get(position).getAirQualityName() + "");
                tvTvoc.setText(roomData.get(position).getTvoc() + "");
                tvFormaldehyde.setText(roomData.get(position).getFormaldehyde() + "");
            }
        });
        listAdapter = new AddRoomListAdapter(mContext, roomData);
        recyclerItemView.setLayoutManager(new GridLayoutManager(mContext, 4, GridLayoutManager.VERTICAL, false));
        recyclerItemView.setAdapter(listAdapter);
        // recyclerItemView.setItemAnimator(new DefaultItemAnimator());
        recyclerItemView.addItemDecoration(new HorizontalItemDecoration(15, mContext));
        listAdapter.setOnItemEditListener(new AddRoomListAdapter.OnItemEditListener() {
            @Override
            public void onItemEditClick(View view, int position) {
                showEditDialog(2, position);
            }

            @Override
            public void onItemDeleteClick(View view, int position) {
                showDeleteDialog(position);
            }

            @Override
            public void onItemAddClick(View view) {
                RoomFragment fragment = new RoomFragment();
                fragment.show(getParentFragmentManager(), "roomshow");
            }

            @Override
            public void onItemClick(View view, int position) {
                currentPosition = position;
                recyclerItemView.setVisibility(View.GONE);
                mItemRoomView.setVisibility(View.VISIBLE);
                llBind.setVisibility(View.GONE);
                titleAdapter.setmPosition(position);
                titleAdapter.notifyDataSetChanged();
                mItemNameView.setText(roomData.get(position).getRoomName() + "环境");
                itemTemp.setText(roomData.get(position).getTemp() + "");
                itemHumidity.setText(roomData.get(position).getHumidity() + "");
                itemPm.setText(roomData.get(position).getPm() + "");
                itemCo2.setText(roomData.get(position).getCo2() + "");
                itemFanSwitch.setText(roomData.get(position).getAirValve() + "");
                mAirText.setText(TextUtils.isEmpty(roomData.get(position).getAirQualityName()) ? "" : roomData.get(position).getAirQualityName() + "");
                tvTvoc.setText(roomData.get(position).getTvoc() + "");
                tvFormaldehyde.setText(roomData.get(position).getFormaldehyde() + "");
            }
        });
        resetToDefaultView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void roomUpdateEvent(RoomChangeEvent roomChangeEvent) {
        if (roomChangeEvent != null) {
            init(false);
            if (currentPosition != -1 && roomData.size() > currentPosition) {
                itemTemp.setText(roomData.get(currentPosition).getTemp() + "");
                itemHumidity.setText(roomData.get(currentPosition).getHumidity() +"");
                itemPm.setText(roomData.get(currentPosition).getPm()+"");
                itemCo2.setText(roomData.get(currentPosition).getCo2()+"");
                itemFanSwitch.setText(roomData.get(currentPosition).getAirValve()+"");
                mAirText.setText(TextUtils.isEmpty(roomData.get(currentPosition).getAirQualityName()) ? "" : roomData.get(currentPosition).getAirQualityName() + "");
                tvTvoc.setText(roomData.get(currentPosition).getTvoc() + "");
                tvFormaldehyde.setText(roomData.get(currentPosition).getFormaldehyde() + "");
            }
        }
    }

    private Dialog dialog;

    @OnClick({R.id.bt_change_air})
    public void onChangeClick(View view) {
        showAirQualityDialog();
    }

    @OnClick({R.id.bt_change_air_release})
    public void onReleaseClick(View view) {
        showReleaseDialog();
    }

    @OnClick({R.id.ll_allRoom})
    public void onAllRoomClick(View view) {
        recyclerItemView.setVisibility(View.VISIBLE);
        mItemRoomView.setVisibility(View.GONE);
        if (titleAdapter != null) {
            titleAdapter.setmPosition(-1);
            titleAdapter.notifyDataSetChanged();
        }
    }

    private void showReleaseDialog() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.release_dialog, null, false);
        mReleaseDialog = new AlertDialog.Builder(getActivity()).setView(view).create();
        Button cancel = view.findViewById(R.id.bt_negative);
        Button sure = view.findViewById(R.id.bt_positive);
        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < saveRoomData.size(); i++) {
                    if (saveRoomData.get(i).getRoomId() == saveRoomData.get(currentPosition).getRoomId()) {
                        saveRoomData.get(i).setAirQualityId(0);
                        saveRoomData.get(i).setAirQualityName("");
                    }
                }
                mAirText.setText(saveRoomData.get(currentPosition).getAirQualityName());
                MySpUtil.setParam(mContext, MySpUtil.ROOM_DATA, new Gson().toJson(saveRoomData));
                mReleaseDialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mReleaseDialog.dismiss();
            }
        });
        mReleaseDialog.show();
        mReleaseDialog.getWindow().setLayout(520, 400);
    }

    /**
     * 空气质量对话框
     */
    private void showAirQualityDialog() {
        View bottomView = View.inflate(getActivity(), R.layout.change_air_dialog, null);
        RecyclerView lvCarIds = bottomView.findViewById(R.id.recycler_change);
        ChangeAirAdapter adapter = new ChangeAirAdapter(getActivity(), qualityInfo.getDevlist());
        lvCarIds.setLayoutManager(new GridLayoutManager(getActivity(), 1, GridLayoutManager.VERTICAL, false));
        lvCarIds.setItemAnimator(new DefaultItemAnimator());
        lvCarIds.addItemDecoration(new HorizontalItemDecoration(1, getActivity()));
        lvCarIds.setAdapter(adapter);
        adapter.setOnItemClickListener(new ChangeAirAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                for (int i = 0; i < saveRoomData.size(); i++) {
                    if (saveRoomData.get(i).getAirQualityId() == qualityInfo.getDevlist().get(position).getId()) {
                        saveRoomData.get(i).setAirQualityId(0);
                        saveRoomData.get(i).setAirQualityName("");
                    }
                }
                saveRoomData.get(currentPosition).setAirQualityId(qualityInfo.getDevlist().get(position).getId());
                saveRoomData.get(currentPosition).setAirQualityName(qualityInfo.getDevlist().get(position).getName());
                mAirText.setText(saveRoomData.get(currentPosition).getAirQualityName());
                MySpUtil.setParam(mContext, MySpUtil.ROOM_DATA, new Gson().toJson(saveRoomData));
                dialog.dismiss();
            }
        });
        dialog = new Dialog(getActivity());
        dialog.setContentView(bottomView);
        Display display = dialog.getWindow().getWindowManager().getDefaultDisplay();
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = (int) (600);
        params.height = (int) (500);
        dialog.getWindow().setAttributes(params);
        dialog.show();
    }

    /**
     * 编辑房间
     *
     * @param type     1添加房间  2.编辑房间
     * @param position
     */
    private void showEditDialog(int type, int position) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.edit_room_dialog, null, false);
        mEditDialog = new AlertDialog.Builder(mContext).setView(view).create();
        TextView tvTitle = view.findViewById(R.id.room_dialog_title);
        if (type == 1) {
            tvTitle.setText("添加房间");
        } else {
            tvTitle.setText("房间名称");
        }
        EditText editText = view.findViewById(R.id.et_room_name);
        Button negative = view.findViewById(R.id.bt_room_negative);
        negative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditDialog.dismiss();
            }
        });
        Button positive = view.findViewById(R.id.bt_room_positive);
        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (type == 2) {
                    RoomInfo roomInfo = roomData.get(position);
                    for (int i = 0; i < saveRoomData.size(); i++) {
                        if (saveRoomData.get(i).getRoomId() == roomInfo.getRoomId()){
                            saveRoomData.get(i).setRoomName(editText.getText().toString());

                        }
                    }
                    roomData.get(position).setRoomName(editText.getText().toString());
                    listAdapter.notifyDataSetChanged();
                    titleAdapter.notifyDataSetChanged();
                    MySpUtil.setParam(mContext, MySpUtil.ROOM_DATA, new Gson().toJson(saveRoomData));
                    mEditDialog.dismiss();
                } else {
                    int id = (int) MySpUtil.getParam(mContext, MySpUtil.SERIAI_ID, 10);
                    RoomInfo roomInfo = new RoomInfo();
                    roomInfo.setRoomId(id + 1);
                    roomInfo.setRoomName(editText.getText().toString());
                    saveRoomData.add(roomInfo);
                    mRoomNum.setText(saveRoomData.size() + "");
                    titleAdapter.setList(saveRoomData);
                    listAdapter.setList(saveRoomData);
                    MySpUtil.setParam(mContext, MySpUtil.ROOM_DATA, new Gson().toJson(saveRoomData));
                    MySpUtil.setParam(mContext, MySpUtil.SERIAI_ID, id + 1);
                    mEditDialog.dismiss();
                }
            }
        });
        mEditDialog.show();
        mEditDialog.getWindow().setLayout(520, 400);
    }

    /**
     * 删除房间
     *
     * @param position
     */
    private void showDeleteDialog(int position) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.delete_room_dialog, null, false);
        mDeleteDialog = new AlertDialog.Builder(mContext).setView(view).create();

        Button negative = view.findViewById(R.id.bt_room_negative);
        negative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDeleteDialog.dismiss();
            }
        });
        Button positive = view.findViewById(R.id.bt_room_positive);
        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RoomInfo roomInfo = roomData.get(position);
                for (int i = 0; i < saveRoomData.size(); i++) {
                    if (saveRoomData.get(i).getRoomId() == roomInfo.getRoomId()){
                        saveRoomData.remove(i);
                    }
                }

                roomData.remove(position);
                mRoomNum.setText(roomData.size() + "");
                titleAdapter.setList(roomData);
                listAdapter.setList(roomData);
                MySpUtil.setParam(mContext, MySpUtil.ROOM_DATA, new Gson().toJson(saveRoomData));
                mDeleteDialog.dismiss();

            }
        });
        mDeleteDialog.show();
        mDeleteDialog.getWindow().setLayout(520, 400);
    }

    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWeatherDataUpdate(WeatherDataEvent event) {
        if (event !=null){
            tvWeatherTemp.setText(event.getOutdoorTemp() +"");
            tvWeatherHumidity.setText(event.getOutdoorHumidity()+"");
            tvWeatherPm2_5.setText(event.getOutdoorPM25()+" 室外");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRoomEvent(EnvironmentDataInfo info) {
        if (info != null) {
            byte[] errorBytes = info.getRoomError();
            HyApplication.setRoomError(errorBytes);
            if (roomList != null) {
                roomList.clear();
                List<RoomInfo> mList = info.getRoomData(getActivity());

                List<RoomInfo> filteredList = new ArrayList<>();
                for (RoomInfo room : mList) {
                    if (room.getTemp() != 0) { // 温度值非0才添加
                        filteredList.add(room);
                    }
                }
                RoomChangeEvent roomChangeEvent = new RoomChangeEvent("");
                EventBus.getDefault().post(roomChangeEvent);
                if (filteredList.size() > 0) {
                    roomList.addAll(filteredList);
                    SaveControlInfo saveControlInfo = MySpUtil.getControlData(getActivity());
                    if (!StringUtils.isNullOrEmpty(saveControlInfo.getTempMin()) && !StringUtils.isNullOrEmpty(saveControlInfo.getTempMax())) {
                        if (HyApplication.getOutTemp().intValue() < Integer.parseInt(saveControlInfo.getTempMin()) * 10) {
                            Collections.sort(roomList, new Comparator<RoomInfo>() {//温度显示最小值
                                public int compare(RoomInfo arg0, RoomInfo arg1) {
                                    return arg0.getTemp() - arg1.getTemp();
                                }
                            });
                        } else if (HyApplication.getOutTemp().intValue() > Integer.parseInt(saveControlInfo.getTempMax()) * 10) {
                            Collections.sort(roomList, new Comparator<RoomInfo>() {//温度显示最大值
                                public int compare(RoomInfo arg0, RoomInfo arg1) {
                                    return arg1.getTemp() - arg0.getTemp();
                                }
                            });
                        } else {
                            BigDecimal temp = new BigDecimal(saveControlInfo.getTempMin()).add(new BigDecimal(saveControlInfo.getTempMax()));
                            BigDecimal temp1 = temp.divide(new BigDecimal(2)).setScale(1, BigDecimal.ROUND_DOWN);
                            BigDecimal temp2 = temp1.multiply(new BigDecimal(10)).setScale(0, BigDecimal.ROUND_DOWN);
                            if (HyApplication.getOutTemp().intValue() < temp2.intValue()) {
                                Collections.sort(roomList, new Comparator<RoomInfo>() {//温度显示最小值
                                    public int compare(RoomInfo arg0, RoomInfo arg1) {
                                        return arg0.getTemp() - arg1.getTemp();
                                    }
                                });
                            } else {
                                Collections.sort(roomList, new Comparator<RoomInfo>() {//温度显示最大值
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

                    Collections.sort(roomList, new Comparator<RoomInfo>() {//湿度显示最大值
                        public int compare(RoomInfo arg0, RoomInfo arg1) {
                            return arg1.getHumidity() - arg0.getHumidity();
                        }
                    });
                    mRoomHumidity.setText(roomList.get(0).getHumidity() + "");

                    Collections.sort(roomList, new Comparator<RoomInfo>() {//CO2显示最大值
                        public int compare(RoomInfo arg0, RoomInfo arg1) {
                            return arg1.getCo2() - arg0.getCo2();
                        }
                    });
                    mRoomCo2.setText(roomList.get(0).getCo2() + "");

                    Collections.sort(roomList, new Comparator<RoomInfo>() {//PM2.5显示最大值
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
