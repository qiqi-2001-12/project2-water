package com.hy.greenbuilding.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hy.greenbuilding.R;
import com.hy.greenbuilding.adapter.TempAdapter;
import com.hy.greenbuilding.config.ErrorDefine;
import com.hy.greenbuilding.event.DefrostChangeEvent;
import com.hy.greenbuilding.event.TempStatusUpdateEvent;
import com.hy.greenbuilding.model.UpTempItem;
import com.hy.greenbuilding.protocol.FunctionObject;
import com.hy.greenbuilding.protocol.ResPonseInfo.UpTempStatusInfo;
import com.hy.greenbuilding.protocol.SpDataProcessor;
import com.hy.greenbuilding.protocol.command.ControlCommand;
import com.hy.greenbuilding.protocol.command.UpTempCommand;
import com.hy.greenbuilding.ui.activity.UpTempTestActivity;
import com.hy.greenbuilding.utils.Hex;
import com.hy.greenbuilding.utils.InputLimitUtil;
import com.hy.greenbuilding.utils.MySpUtil;
import com.hy.greenbuilding.utils.StringUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 升温除湿页面
 */
public class UpTempFragment extends BaseDialogFragment {
    @BindView(R.id.li_back)
    ImageView mReturnView;
    @BindView(R.id.bt_pvTemp_test)
    Button mTestButton;
    @BindView(R.id.bt_error)
    Button mErrorButton;
    @BindView(R.id.rl_upTemp)
    RecyclerView mRecyclerView;

    @BindView(R.id.switch_status_tv)
    TextView switchStatus;
    @BindView(R.id.set_status_tv)
    TextView setStatus;
    @BindView(R.id.set_temp_tv)
    TextView setTemp;
    @BindView(R.id.wind_temp_tv)
    TextView windTemp;
    @BindView(R.id.indoor_temp_tv)
    TextView indoorTemp;
    @BindView(R.id.up_pipe_tv)
    TextView upPipe;
    @BindView(R.id.humidity_pipe_tv)
    TextView humidityPipe;

    @BindView(R.id.radio_group)
    RadioGroup mDefrostRg;
    @BindView(R.id.open_rb)
    RadioButton mOpenRb;
    @BindView(R.id.not_open_rb)
    RadioButton mNotOpenRb;

    private View mView;
    private Unbinder unbinder;
    private Context mContext;
    private List<UpTempItem> mList;
    private TempAdapter upTempAdapter;

    private int mode = 0;

    private boolean isDefrost;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogFullScreen);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        this.getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        mView = inflater.inflate(R.layout.up_temp_main, null);
        mContext = this.getActivity();
        unbinder = ButterKnife.bind(this, mView);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        init();
        mDefrostRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                UpTempCommand command = new UpTempCommand(FunctionObject.UP_DEFROST_STATUS);
                if (checkedId == R.id.open_rb) {
                    command.setData(new byte[]{(byte) 0x00, (byte) 0x01});
                } else if (checkedId == R.id.not_open_rb) {
                    command.setData(new byte[]{(byte) 0x00, (byte) 0x00});
                }
                SpDataProcessor.getInstance().send(command);
            }
        });

        return mView;
    }

    private void init() {
        mList = new ArrayList<>();
        for (int i = 0; i < ErrorDefine.Up_temp_data.length; i++) {
            UpTempItem info = new UpTempItem();
            info.setTitle(ErrorDefine.Up_temp_data[i]);
            mList.add(info);
        }
        upTempAdapter = new TempAdapter(mContext, mList);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 3, GridLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(upTempAdapter);
        sendStatusCommand();
        mHandler.removeCallbacks(mRunnable);
        mHandler.postDelayed(mRunnable, 5000);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ControlCommand controlCommand = new ControlCommand(FunctionObject.GET_CONTROL_STATUS);
                SpDataProcessor.getInstance().send(controlCommand);
            }
        }, 1000);

    }

    private void sendStatusCommand() {
        UpTempCommand pvCommand = new UpTempCommand(FunctionObject.UP_GET_OUT_STATUS);
        SpDataProcessor.getInstance().send(pvCommand);
    }

    @OnClick({R.id.li_back})
    public void onReturnClick(View view) {
        this.dismiss();
    }

    @OnClick({R.id.bt_error})
    public void onErrorClick(View view) {
        if (InputLimitUtil.isFastDoubleClick()) {
            return;
        }
        UpTempErrorFragment fragment = new UpTempErrorFragment();
        fragment.show(this.getActivity().getSupportFragmentManager(), "tempError");

    }

    @OnClick({R.id.bt_pvTemp_test})
    public void onPVTestClick(View view) {
        if (InputLimitUtil.isFastDoubleClick()) {
            return;
        }

        Intent intent = new Intent(getActivity(), UpTempTestActivity.class);
        startActivity(intent);
    }

    Handler mHandler = new Handler();
    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            sendStatusCommand();
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDefrostEvent(DefrostChangeEvent event) {
        if (event != null) {
            isDefrost = event.getDefrostStatus();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTempStatusEvent(TempStatusUpdateEvent event) {
        if (event != null) {
            switchStatus.setText(event.isOpen ? "开" : "关");
            if (event.getTempMode() == 1) {
                setStatus.setText("制冷");
            } else if (event.getTempMode() == 2) {
                setStatus.setText("制热");
            } else if (event.getTempMode() == 4) {
                setStatus.setText("除湿");
            } else {
                setStatus.setText("待机");
            }
            mode = event.getTempMode();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpTempStatusInfo info) {

        Log.e("TAG", "onEvent: "+new Gson().toJson(info));

        if (info != null) {
            HashMap<String, Object> upTempMap = info.getDataMap();
            if (mList != null) {
                for (int i = 0; i < mList.size(); i++) {
                    String name = mList.get(i).getTitle();
                    if (upTempMap.containsKey(name)) {
                        Object value = upTempMap.get(name);
                        if (value != null) {
                            mList.get(i).setValue(value.toString());
                        } else {
                            mList.get(i).setValue("");
                        }
                    }
                }
            }
            try {
//                switchStatus.setText(Objects.requireNonNull(upTempMap.get(ErrorDefine.OPEN_CLOSE)).toString());
//                setStatus.setText(Objects.requireNonNull(upTempMap.get(ErrorDefine.RUN_MODE)).toString());
                if(mode == 1 || mode == 4){
                    setTemp.setText(Objects.requireNonNull(upTempMap.get(ErrorDefine.COLD_SET_TEMP)).toString());
                }else if(mode == 2){
                    setTemp.setText(Objects.requireNonNull(upTempMap.get(ErrorDefine.HOT_SET_TEMP)).toString());
                }

                if (Objects.requireNonNull(upTempMap.get(ErrorDefine.DEFROST_SIGNAL)).toString().equals("1")) {
                    mOpenRb.setChecked(true);
                } else {
                    mNotOpenRb.setChecked(true);
                }

                String json = MySpUtil.getParam(mContext, MySpUtil.NTC_DATA, "").toString();
                Log.e("TAG", "onEvent: "+json);
                if (!StringUtils.isNullOrEmpty(json)) {
                    List<String> list = new Gson().fromJson(json, new TypeToken<List<String>>() {}.getType());
                    if (list != null && list.size() >= 6) {
                        windTemp.setText(list.get(3) + " \u2103");
                        indoorTemp.setText(list.get(2) + " \u2103");
                        upPipe.setText(list.get(5) + " \u2103");
                        humidityPipe.setText(list.get(4) + " \u2103");
                    }
                }

            } catch (Exception e) {
                Log.e("TAG", "onEvent: "+e.toString());
                e.printStackTrace();
            }
            upTempAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        EventBus.getDefault().unregister(this);
        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
            mHandler = null;
        }
        super.onDestroyView();
    }
}
