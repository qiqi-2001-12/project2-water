package com.hy.greenbuilding.ui.fragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hy.greenbuilding.HyApplication;
import com.hy.greenbuilding.R;
import com.hy.greenbuilding.adapter.SystemStatusAdapter;
import com.hy.greenbuilding.adapter.UpTempErrorAdapter;
import com.hy.greenbuilding.config.ErrorDefine;
import com.hy.greenbuilding.event.DefrostChangeEvent;
import com.hy.greenbuilding.model.UpTempSystemStatusInfo;
import com.hy.greenbuilding.model.UptempErrorInfo;
import com.hy.greenbuilding.protocol.FunctionObject;
import com.hy.greenbuilding.protocol.ResPonseInfo.UpTempStatusInfo;
import com.hy.greenbuilding.protocol.SpDataProcessor;
import com.hy.greenbuilding.protocol.command.UpTempCommand;
import com.hy.greenbuilding.utils.ByteUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class UpTempErrorFragment extends BaseDialogFragment {
    @BindView(R.id.li_back)
    LinearLayout mReturnView;
    @BindView(R.id.recycler_view)
    RecyclerView listView;

    @BindView(R.id.recycler_view_error)
    RecyclerView errorListView;

    private View mView;
    private Unbinder unbinder;
    private Context mContext;
    private List<UpTempSystemStatusInfo> systemStatusInfoList = new ArrayList<>();
    private List<UptempErrorInfo> upTempErrorInfoList = new ArrayList<>();
    private SystemStatusAdapter mAdapter;
    private UpTempErrorAdapter mErrorAdapter;
    private boolean isDefrost;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogFullScreen);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.up_temp_error, null);
        mContext = this.getActivity();
        unbinder = ButterKnife.bind(this, mView);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        init();
        mAdapter = new SystemStatusAdapter(getActivity(), systemStatusInfoList, 0);
        listView.setLayoutManager(new LinearLayoutManager(getActivity()));
        listView.setAdapter(mAdapter);

        sendStatusCommand();

        return mView;
    }

    private void init() {
        for (int i = 0; i < ErrorDefine.Up_temp_Error.length; i++) {
            UptempErrorInfo info = new UptempErrorInfo();
            info.setName(ErrorDefine.Up_temp_Error[i]);
            upTempErrorInfoList.add(info);
        }
        mErrorAdapter = new UpTempErrorAdapter(getActivity(), upTempErrorInfoList);
        errorListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        errorListView.setAdapter(mErrorAdapter);

    }

    private void sendStatusCommand() {
        UpTempCommand pvCommand = new UpTempCommand(FunctionObject.UP_GET_OUT_STATUS);
        SpDataProcessor.getInstance().send(pvCommand);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDefrostEvent(DefrostChangeEvent event) {
        if (event != null) {
            isDefrost = event.getDefrostStatus();
        }
    }

    @OnClick({R.id.li_back})
    public void onReturnClick(View view) {
        this.dismiss();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpTempStatusInfo info) {
        if (info != null) {
            HashMap<String, Object> upTempMap = info.getDataMap();
            //系统状态信息
            systemStatusInfoList.clear();
            short systemStatus = (short) upTempMap.get(ErrorDefine.SYSTEM_STATUS);
            byte[] systemStatusBytes = ByteUtils.shortToByteArray(systemStatus);
            if (systemStatusBytes != null) {
                byte[] bytes = ByteUtils.getBitArray(systemStatusBytes);
                for (int i = 0; i < bytes.length; i++) {
                    if (bytes[i] == 1) {
                        UpTempSystemStatusInfo upTempSystemStatusInfo = new UpTempSystemStatusInfo();
                        upTempSystemStatusInfo.setName(ErrorDefine.SystemStatus[bytes.length - 1 - i]);
                        systemStatusInfoList.add(upTempSystemStatusInfo);
                    }
                }
            }
            byte[] bytes = HyApplication.getNtcError();
            if (bytes != null) {
                if (!isDefrost) {
                    if (bytes[2] == 0) {
                        UpTempSystemStatusInfo upTempSystemStatusInfo = new UpTempSystemStatusInfo();
                        upTempSystemStatusInfo.setName(ErrorDefine.NTC_Error[2]);
                        systemStatusInfoList.add(upTempSystemStatusInfo);
                    }
                }
                if (bytes[5] == 0) {
                    UpTempSystemStatusInfo upTempSystemStatusInfo = new UpTempSystemStatusInfo();
                    upTempSystemStatusInfo.setName(ErrorDefine.NTC_Error[5]);
                    systemStatusInfoList.add(upTempSystemStatusInfo);

                }
                if (bytes[7] == 0) {
                    UpTempSystemStatusInfo upTempSystemStatusInfo = new UpTempSystemStatusInfo();
                    upTempSystemStatusInfo.setName(ErrorDefine.NTC_Error[7]);
                    systemStatusInfoList.add(upTempSystemStatusInfo);
                }
            }


            mAdapter.setList(systemStatusInfoList);

            for (int i = 0; i < upTempErrorInfoList.size(); i++) {
                String name = upTempErrorInfoList.get(i).getName();
                if (upTempMap.containsKey(name)) {
                    Object value = upTempMap.get(name);
                    List<UpTempSystemStatusInfo> list = new Gson().fromJson(value.toString(), new TypeToken<List<UpTempSystemStatusInfo>>() {
                    }.getType());
                    upTempErrorInfoList.get(i).setValue(list);
                }
            }

            mErrorAdapter.setList(upTempErrorInfoList);
        }
    }


    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }
}
