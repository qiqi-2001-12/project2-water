package com.hy.greenbuilding.ui.fragment;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.hy.greenbuilding.R;
import com.hy.greenbuilding.event.FanErrorEvent;
import com.hy.greenbuilding.model.FanDataInfo;
import com.hy.greenbuilding.model.FanTypeCount;
import com.hy.greenbuilding.protocol.FunctionObject;
import com.hy.greenbuilding.protocol.ResPonseInfo.CO2StatusInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.FanStatusInfo;
import com.hy.greenbuilding.protocol.SpDataProcessor;
import com.hy.greenbuilding.protocol.command.EnvironmentCommand;
import com.hy.greenbuilding.protocol.command.FanCommand;
import com.hy.greenbuilding.ui.widget.NewNestedScrollView;
import com.hy.greenbuilding.utils.ByteUtils;
import com.hy.greenbuilding.utils.Hex;
import com.hy.greenbuilding.utils.InputLimitUtil;
import com.hy.greenbuilding.utils.MySpUtil;
import com.hy.greenbuilding.utils.StringUtils;
import com.hy.greenbuilding.utils.ToastUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class FanTestDataFragment extends Fragment implements RadioGroup.OnCheckedChangeListener, TextView.OnEditorActionListener {
    @BindView(R.id.radiogroup_wind1)
    RadioGroup mRg1;
    @BindView(R.id.radiogroup_wind2)
    RadioGroup mRg2;
    @BindView(R.id.radiogroup_wind3)
    RadioGroup mRg3;
    @BindView(R.id.radiogroup_wind4)
    RadioGroup mRg4;

    @BindView(R.id.rb_wind1_rs)
    RadioButton rb_wind1_rs;
    @BindView(R.id.rb_wind1_pwm)
    RadioButton rb_wind1_pwm;
    @BindView(R.id.rb_wind2_rs)
    RadioButton rb_wind2_rs;
    @BindView(R.id.rb_wind2_pwm)
    RadioButton rb_wind2_pwm;
    @BindView(R.id.rb_wind3_rs)
    RadioButton rb_wind3_rs;
    @BindView(R.id.rb_wind3_pwm)
    RadioButton rb_wind3_pwm;
    @BindView(R.id.rb_wind3_dc)
    RadioButton rb_wind3_dc;
    @BindView(R.id.rb_wind4_rs)
    RadioButton rb_wind4_rs;
    @BindView(R.id.rb_wind4_pwm)
    RadioButton rb_wind4_pwm;

    @BindView(R.id.tv_wind1)
    TextView mWindType1;
    @BindView(R.id.tv_wind2)
    TextView mWindType2;
    @BindView(R.id.tv_wind3)
    TextView mWindType3;
    @BindView(R.id.tv_wind4)
    TextView mWindType4;

    @BindView(R.id.tv_type_wind1)
    TextView mTypeWind1;
    @BindView(R.id.tv_type_wind2)
    TextView mTypeWind2;
    @BindView(R.id.tv_type_wind3)
    TextView mTypeWind3;
    @BindView(R.id.tv_type_wind4)
    TextView mTypeWind4;
    @BindView(R.id.tv_error_wind1)
    TextView mErrorWind1;
    @BindView(R.id.tv_error_wind2)
    TextView mErrorWind2;
    @BindView(R.id.tv_error_wind3)
    TextView mErrorWind3;
    @BindView(R.id.tv_error_wind4)
    TextView mErrorWind4;
    @BindView(R.id.tv_vref_wind1)
    TextView mvrefWind1;
    @BindView(R.id.tv_vref_wind2)
    TextView mvrefWind2;
    @BindView(R.id.tv_vref_wind3)
    TextView mvrefWind3;
    @BindView(R.id.tv_vref_wind4)
    TextView mvrefWind4;

    @BindView(R.id.et_small_wind1)
    EditText et_small_wind1;
    @BindView(R.id.et_small_wind2)
    EditText et_small_wind2;
    @BindView(R.id.et_small_wind3)
    EditText et_small_wind3;
    @BindView(R.id.et_small_wind4)
    EditText et_small_wind4;
    @BindView(R.id.et_middle_wind1)
    EditText et_middle_wind1;
    @BindView(R.id.et_middle_wind2)
    EditText et_middle_wind2;
    @BindView(R.id.et_middle_wind3)
    EditText et_middle_wind3;
    @BindView(R.id.et_middle_wind4)
    EditText et_middle_wind4;
    @BindView(R.id.et_high_wind1)
    EditText et_high_wind1;
    @BindView(R.id.et_high_wind2)
    EditText et_high_wind2;
    @BindView(R.id.et_high_wind3)
    EditText et_high_wind3;
    @BindView(R.id.et_high_wind4)
    EditText et_high_wind4;

    @BindView(R.id.bt_wind1)
    Button bt_wind1;
    @BindView(R.id.bt_wind2)
    Button bt_wind2;
    @BindView(R.id.bt_wind3)
    Button bt_wind3;
    @BindView(R.id.bt_wind4)
    Button bt_wind4;
    @BindView(R.id.et_small_co2)
    EditText etSmallCo2;
    @BindView(R.id.et_middle_co2)
    EditText etMiddleCo2;
    @BindView(R.id.et_high_co2)
    EditText etHighCo2;
    @BindView(R.id.et_small_pm)
    EditText etSmallPM;
    @BindView(R.id.et_middle_pm)
    EditText etMiddlePM;
    @BindView(R.id.et_high_pm)
    EditText etHighPM;
    @BindView(R.id.bt_co2_set)
    Button btCo2Set;
    @BindView(R.id.bt_pm_set)
    Button btPmSet;

    private int windType = 1;//当前设置的风机设备类型
    private List<FanDataInfo> fanList = new ArrayList<>();
    private List<FanTypeCount> fanTypeList = new ArrayList<>();
    private int countRS = 0;//rs485风机数量
    private int countPWM = 0;//pwm风机数量
    private int vref1 = 0;//新风最大风量
    private int vref2 = 0;//排风最大风量
    private int vref3 = 0;//循环风1最大风量
    private int vref4 = 0;//循环风2最大风量
    private View view;
    private Unbinder unbinder;
    private Context mContext;
    private boolean isInit;
    public static boolean isScroll;
    private boolean isRead;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fan_test_data_fragment, container, false);
        unbinder = ButterKnife.bind(this, view);
        mContext = getActivity();
        mRg1.setOnCheckedChangeListener(this);
        mRg2.setOnCheckedChangeListener(this);
        mRg3.setOnCheckedChangeListener(this);
        mRg4.setOnCheckedChangeListener(this);
        isInit = true;
        initData();
        NewNestedScrollView scrollView = view.findViewById(R.id.scrollView);
        scrollView.addScrollChangeListener(new NewNestedScrollView.AddScrollChangeListener() {
            @Override
            public void onScrollChange(int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

            }

            @Override
            public void onScrollState(NewNestedScrollView.ScrollState state) {
                switch (state) {
                    case DRAG:
                    case SCROLLING:
                        isScroll = true;
                        break;
                    case IDLE:
                        isScroll = false;
                        break;
                }
            }
        });
        et_small_wind1.setOnEditorActionListener(this);
        et_small_wind2.setOnEditorActionListener(this);
        et_small_wind3.setOnEditorActionListener(this);
        et_small_wind4.setOnEditorActionListener(this);

        et_middle_wind1.setOnEditorActionListener(this);
        et_middle_wind2.setOnEditorActionListener(this);
        et_middle_wind3.setOnEditorActionListener(this);
        et_middle_wind4.setOnEditorActionListener(this);

        et_high_wind1.setOnEditorActionListener(this);
        et_high_wind2.setOnEditorActionListener(this);
        et_high_wind3.setOnEditorActionListener(this);
        et_high_wind4.setOnEditorActionListener(this);

        etSmallCo2.setOnEditorActionListener(this);
        etMiddleCo2.setOnEditorActionListener(this);
        etHighCo2.setOnEditorActionListener(this);

        etSmallPM.setOnEditorActionListener(this);
        etMiddlePM.setOnEditorActionListener(this);
        etHighPM.setOnEditorActionListener(this);

        return view;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (unbinder != null) {
            unbinder.unbind();
        }

    }

    public void initData() {
        isRead = false;
    }

    /**
     * @param type        风机类型
     * @param smallValue  低速
     * @param middleValue 中速
     * @param highValue   高速
     */
    private void sendWindValue(int vref, boolean is485, byte type, String smallValue, String middleValue, String highValue) {
        byte[] smallData;
        // 400-0x0190   1000-0x0318
        byte fanType;

        if (type == 2) {
            if (is485) {
                fanType = (byte) 0x01;
            } else if (rb_wind3_dc.isChecked()) {
                fanType = (byte) 0x02;
            } else {
                fanType = (byte) 0x00;
            }
        } else {
            if (is485) {
                fanType = (byte) 0x01;
            } else {
                fanType = (byte) 0x00;
            }
        }
        if (StringUtils.isNullOrEmpty(smallValue)) {
            if (!StringUtils.isNullOrEmpty(middleValue)) {
                smallData = ByteUtils.int16ToByteArray(Integer.parseInt(middleValue));
            } else {
                smallData = new byte[]{(byte) 0x00, (byte) 0x01};
            }
        } else {
            smallData = ByteUtils.int16ToByteArray(Integer.parseInt(smallValue));
        }
        byte[] middleData;
        if (StringUtils.isNullOrEmpty(middleValue)) {
            if (!StringUtils.isNullOrEmpty(smallValue)) {
                middleData = ByteUtils.int16ToByteArray(Integer.parseInt(smallValue));
            } else {
                middleData = new byte[]{(byte) 0x00, (byte) 0x01};
            }
        } else {
            middleData = ByteUtils.int16ToByteArray(Integer.parseInt(middleValue));
        }
        byte[] highData;
        if (StringUtils.isNullOrEmpty(highValue)) {
            highData = ByteUtils.int16ToByteArray(vref);
        } else {
            highData = ByteUtils.int16ToByteArray(Integer.parseInt(highValue));
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        byteBuffer.put(fanType);
        byteBuffer.put(type);
        byteBuffer.put(smallData);
        byteBuffer.put(middleData);
        byteBuffer.put(highData);
        FanCommand fanCommand = new FanCommand(FunctionObject.SET_SPEED_VALUE);
        fanCommand.setData(byteBuffer.array());

        SpDataProcessor.getInstance().send(fanCommand);
    }

    @OnClick({R.id.bt_wind1})
    public void onWind1BtnClick(View view) {
        String smallText = et_small_wind1.getText().toString();
        String middleText = et_middle_wind1.getText().toString();
        String highText = et_high_wind1.getText().toString();
        boolean isInput = InputLimitUtil.inputLimit(rb_wind1_pwm, smallText, middleText, highText);
        if (isInput) {
            sendWindValue(vref1, rb_wind1_rs.isChecked(), (byte) 0x00, smallText, middleText, highText);
        } else {
            ToastUtil.showToast(mContext, getString(R.string.set_format_error));
        }
        windType = 1;
    }

    @OnClick({R.id.bt_wind2})
    public void onWind2BtnClick(View view) {
        String smallText = et_small_wind2.getText().toString();
        String middleText = et_middle_wind2.getText().toString();
        String highText = et_high_wind2.getText().toString();
        boolean isInput = InputLimitUtil.inputLimit(rb_wind2_pwm, smallText, middleText, highText);
        if (isInput) {
            sendWindValue(vref2, rb_wind2_rs.isChecked(), (byte) 0x01, smallText, middleText, highText);
        } else {
            ToastUtil.showToast(mContext, getString(R.string.set_format_error));
        }
        windType = 2;
    }

    @OnClick({R.id.bt_wind3})
    public void onWind3BtnClick(View view) {
        String smallText = et_small_wind3.getText().toString();
        String middleText = et_middle_wind3.getText().toString();
        String highText = et_high_wind3.getText().toString();
        boolean isInput = InputLimitUtil.inputLimit(rb_wind3_pwm, smallText, middleText, highText);
        if (isInput) {
            sendWindValue(vref3, rb_wind3_rs.isChecked(), (byte) 0x02, smallText, middleText, highText);
        } else {
            ToastUtil.showToast(mContext, getString(R.string.set_format_error));
        }
        windType = 3;
    }

    @OnClick({R.id.bt_wind4})
    public void onWind4BtnClick(View view) {
        String smallText = et_small_wind4.getText().toString();
        String middleText = et_middle_wind4.getText().toString();
        String highText = et_high_wind4.getText().toString();
        boolean isInput = InputLimitUtil.inputLimit(rb_wind4_pwm, smallText, middleText, highText);
        if (isInput) {
            sendWindValue(vref4, rb_wind4_rs.isChecked(), (byte) 0x03, smallText, middleText, highText);
        } else {
            ToastUtil.showToast(mContext, getString(R.string.set_format_error));
        }
        windType = 4;
    }

    @OnClick({R.id.bt_co2_set})
    public void onCo2SetClick(View view) {
        String smallText = etSmallCo2.getText().toString();
        String middleText = etMiddleCo2.getText().toString();
        String highText = etHighCo2.getText().toString();
        if (!StringUtils.isNullOrEmpty(smallText) && !StringUtils.isNullOrEmpty(middleText)
                && !StringUtils.isNullOrEmpty(highText)) {
            if (Integer.parseInt(smallText) >= Integer.parseInt(middleText) ||
                    Integer.parseInt(middleText) >= Integer.parseInt(highText)) {
                ToastUtil.showToast(mContext, getString(R.string.set_format_error));
                return;
            }
            EnvironmentCommand command = new EnvironmentCommand(FunctionObject.SET_CO2_VALUE);
            ByteBuffer byteBuffer = ByteBuffer.allocate(6);
            byteBuffer.put(ByteUtils.int16ToByteArray(Integer.parseInt(smallText)));
            byteBuffer.put(ByteUtils.int16ToByteArray(Integer.parseInt(middleText)));
            byteBuffer.put(ByteUtils.int16ToByteArray(Integer.parseInt(highText)));
            command.setData(byteBuffer.array());
            SpDataProcessor.getInstance().send(command);
        }
    }

    @OnClick({R.id.bt_pm_set})
    public void onPmSetClick(View view) {
        String smallText = etSmallPM.getText().toString();
        String middleText = etMiddlePM.getText().toString();
        String highText = etHighPM.getText().toString();
        if (!StringUtils.isNullOrEmpty(smallText) && !StringUtils.isNullOrEmpty(middleText)
                && !StringUtils.isNullOrEmpty(highText)) {
            if (Integer.parseInt(smallText) >= Integer.parseInt(middleText) ||
                    Integer.parseInt(middleText) >= Integer.parseInt(highText)) {
                ToastUtil.showToast(mContext, getString(R.string.set_format_error));
                return;
            }
            EnvironmentCommand command = new EnvironmentCommand(FunctionObject.SET_PM_VALUE);
            ByteBuffer byteBuffer = ByteBuffer.allocate(6);
            byteBuffer.put(ByteUtils.int16ToByteArray(Integer.parseInt(smallText)));
            byteBuffer.put(ByteUtils.int16ToByteArray(Integer.parseInt(middleText)));
            byteBuffer.put(ByteUtils.int16ToByteArray(Integer.parseInt(highText)));
            command.setData(byteBuffer.array());
            SpDataProcessor.getInstance().send(command);
        }
    }

    private FanStatusInfo fanStatusInfo;

    public void setFanData(FanStatusInfo info) {
        fanStatusInfo = info;
        fanList.clear();
        fanList.addAll(info.getFanData());
        if (fanList != null && fanList.size() == 4) {
            initWind1(fanList.get(0));
            initWind2(fanList.get(1));
            initWind3(fanList.get(2));
            initWind4(fanList.get(3));
            isRead = true;
            fanTypeList.clear();
            for (int i = 0; i < 2; i++) {
                FanTypeCount fanType = new FanTypeCount();
                fanType.setType(i + 1);
                if (i == 0) {
                    fanType.setNum(countRS);
                } else {
                    fanType.setNum(countPWM);
                }
                fanTypeList.add(fanType);
            }
            countRS = 0;
            countPWM = 0;
            MySpUtil.setParam(mContext, MySpUtil.FAN_COUNT, new Gson().toJson(fanTypeList));
        }
    }

    public void setCo2Data(CO2StatusInfo info) {
        if (info.getType() == 1) {
            if (info.getSuccess()) {
                ToastUtil.showToast(this.getActivity(), "CO2设置成功");
            } else {
                ToastUtil.showToast(this.getActivity(), "CO2设置失败");
            }
        } else if (info.getType() == 2) {
            if (info.getSuccess()) {
                ToastUtil.showToast(this.getActivity(), "PM2.5设置成功");
            } else {
                ToastUtil.showToast(this.getActivity(), "PM2.5设置失败");
            }
        } else if (info.getType() == 3) {
            if (!etSmallCo2.isFocused()) etSmallCo2.setText(info.getCO2Min() + "");
            if (!etMiddleCo2.isFocused()) etMiddleCo2.setText(info.getCO2Middle() + "");
            if (!etHighCo2.isFocused()) etHighCo2.setText(info.getCO2High() + "");
            if (!etSmallPM.isFocused()) etSmallPM.setText(info.getPmMin() + "");
            if (!etMiddlePM.isFocused()) etMiddlePM.setText(info.getPmMiddle() + "");
            if (!etHighPM.isFocused()) etHighPM.setText(info.getPmHigh() + "");
        }
    }

    public void setFanErrorData(FanErrorEvent info) {
        switch (info.getType()) {
            case 2:
                isRead = false;
                if (info.getStatus()) {
                    ToastUtil.showToast(this.getActivity(), "风机类型设置成功");
                } else {
                    ToastUtil.showToast(this.getActivity(), "风机类型设置失败");
                }
                break;
            case 3:
                if (info.getStatus()) {
                    ToastUtil.showToast(this.getActivity(), "风量设置成功");
                    if (windType == 1) {
                        MySpUtil.saveValueToLocal(mContext, 1, et_small_wind1.getText().toString(), et_middle_wind1.getText().toString(),
                                et_high_wind1.getText().toString());
                    } else if (windType == 2) {
                        MySpUtil.saveValueToLocal(mContext, 2, et_small_wind2.getText().toString(), et_middle_wind2.getText().toString(),
                                et_high_wind2.getText().toString());
                    } else if (windType == 3) {
                        MySpUtil.saveValueToLocal(mContext, 3, et_small_wind3.getText().toString(), et_middle_wind3.getText().toString(),
                                et_high_wind3.getText().toString());

                    } else if (windType == 4) {
                        MySpUtil.saveValueToLocal(mContext, 4, et_small_wind4.getText().toString(), et_middle_wind4.getText().toString(),
                                et_high_wind4.getText().toString());
                    }
                } else {
                    ToastUtil.showToast(this.getActivity(), "风量设置失败");
                }
                break;
        }
    }

    //新风
    private void initWind1(FanDataInfo info) {
        vref1 = info.getvrefModel();
        mvrefWind1.setText(info.getWindValue());
        mErrorWind1.setText(info.getFanError() + "");
        if (vref1 == 400) {
            mTypeWind1.setText("G133");
        } else if (vref1 == 1000) {
            mTypeWind1.setText("G190");
        }
        int fanType = info.getInterfaceType();
        if (fanType == 1) {
            countRS++;
            rb_wind1_rs.setChecked(true);
            if (!et_small_wind1.isFocused()) et_small_wind1.setText(info.getSmallGear() + "");
            if (!et_middle_wind1.isFocused()) et_middle_wind1.setText(info.getMiddleGear() + "");
            if (!et_high_wind1.isFocused()) et_high_wind1.setText(info.getBigGear() + "");

        } else if (fanType == 0) {
            countPWM++;
            rb_wind1_pwm.setChecked(true);
            if (!et_small_wind1.isFocused()) et_small_wind1.setText(info.getPwmSmallGear() + "");
            if (!et_middle_wind1.isFocused()) et_middle_wind1.setText(info.getPwmMiddleGear() + "");
            if (!et_high_wind1.isFocused()) et_high_wind1.setText(info.getPwmBigGear() + "");

        }
    }

    //排风
    private void initWind2(FanDataInfo info) {
        vref2 = info.getvrefModel();
        mvrefWind2.setText(info.getWindValue());
        mErrorWind2.setText(info.getFanError() + "");
        if (vref2 == 400) {
            mTypeWind2.setText("G133");
        } else if (vref2 == 1000) {
            mTypeWind2.setText("G190");
        }

        int fanType = info.getInterfaceType();
        if (fanType == 1) {
            countRS++;
            rb_wind2_rs.setChecked(true);
            if (!et_small_wind2.isFocused()) et_small_wind2.setText(info.getSmallGear() + "");
            if (!et_middle_wind2.isFocused()) et_middle_wind2.setText(info.getMiddleGear() + "");
            if (!et_high_wind2.isFocused()) et_high_wind2.setText(info.getBigGear() + "");
        } else if (fanType == 0) {
            countPWM++;
            rb_wind2_pwm.setChecked(true);
            if (!et_small_wind2.isFocused()) et_small_wind2.setText(info.getPwmSmallGear() + "");
            if (!et_middle_wind2.isFocused()) et_middle_wind2.setText(info.getPwmMiddleGear() + "");
            if (!et_high_wind2.isFocused()) et_high_wind2.setText(info.getPwmBigGear() + "");


        }
    }

    //循环风1
    private void initWind3(FanDataInfo info) {
        vref3 = info.getvrefModel();
        mvrefWind3.setText(info.getWindValue());
        mErrorWind3.setText(info.getFanError() + "");
        if (vref3 == 400) {
            mTypeWind3.setText("G133");
        } else if (vref3 == 1000) {
            mTypeWind3.setText("G190");
        }

        int fanType = info.getInterfaceType();
        if (fanType == 1) {
            countRS++;
            rb_wind3_rs.setChecked(true);

            if (!et_small_wind3.isFocused()) et_small_wind3.setText(info.getSmallGear() + "");
            if (!et_middle_wind3.isFocused()) et_middle_wind3.setText(info.getMiddleGear() + "");
            if (!et_high_wind3.isFocused()) et_high_wind3.setText(info.getBigGear() + "");

        } else if (fanType == 0) {
            countPWM++;
            rb_wind3_pwm.setChecked(true);

            if (!et_small_wind3.isFocused()) et_small_wind3.setText(info.getPwmSmallGear() + "");
            if (!et_middle_wind3.isFocused()) et_middle_wind3.setText(info.getPwmMiddleGear() + "");
            if (!et_high_wind3.isFocused()) et_high_wind3.setText(info.getPwmBigGear() + "");

        } else if (fanType == 2) {
            rb_wind3_dc.setChecked(true);
            if (fanStatusInfo != null) {
                if (!et_small_wind3.isFocused())
                    et_small_wind3.setText(fanStatusInfo.getDCFanSmall() + "");
                if (!et_middle_wind3.isFocused())
                    et_middle_wind3.setText(fanStatusInfo.getDCFanMiddle() + "");
                if (!et_high_wind3.isFocused())
                    et_high_wind3.setText(fanStatusInfo.getDCFanHigh() + "");
            }
        }
    }

    //循环风2
    private void initWind4(FanDataInfo info) {
        vref4 = info.getvrefModel();
        mvrefWind4.setText(info.getWindValue());
        mErrorWind4.setText(info.getFanError() + "");
        if (vref4 == 400) {
            mTypeWind4.setText("G133");
        } else if (vref4 == 1000) {
            mTypeWind4.setText("G190");
        }
        int fanType = info.getInterfaceType();
        if (fanType == 1) {
            countRS++;
            rb_wind4_rs.setChecked(true);
            if (!et_small_wind4.isFocused()) et_small_wind4.setText(info.getSmallGear() + "");
            if (!et_middle_wind4.isFocused()) et_middle_wind4.setText(info.getMiddleGear() + "");
            if (!et_high_wind4.isFocused()) et_high_wind4.setText(info.getBigGear() + "");

        } else if (fanType == 0) {
            countPWM++;
            rb_wind4_pwm.setChecked(true);
            if (!et_small_wind4.isFocused()) et_small_wind4.setText(info.getPwmSmallGear() + "");
            if (!et_middle_wind4.isFocused()) et_middle_wind4.setText(info.getPwmMiddleGear() + "");
            if (!et_high_wind4.isFocused()) et_high_wind4.setText(info.getPwmBigGear() + "");

        }

    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        switch (i) {
            case R.id.rb_wind1_pwm:
                mWindType1.setText("百分比");
                if (!rb_wind1_pwm.isPressed()) {
                    return;
                }
                setFanTypeCommand(new byte[]{(byte) 0x00, (byte) 0x00});
                break;
            case R.id.rb_wind1_rs:
                mWindType1.setText("风量");
                if (!rb_wind1_rs.isPressed()) {
                    return;
                }
                setFanTypeCommand(new byte[]{(byte) 0x00, (byte) 0x01});
                break;
            case R.id.rb_wind2_pwm:
                mWindType2.setText("百分比");
                if (!rb_wind2_pwm.isPressed()) {
                    return;
                }
                setFanTypeCommand(new byte[]{(byte) 0x01, (byte) 0x00});
                break;
            case R.id.rb_wind2_rs:
                mWindType2.setText("风量");
                if (!rb_wind2_rs.isPressed()) {
                    return;
                }
                setFanTypeCommand(new byte[]{(byte) 0x01, (byte) 0x01});
                break;
            case R.id.rb_wind3_pwm:
                mWindType3.setText("百分比");
                if (!rb_wind3_pwm.isPressed()) {
                    return;
                }
                setFanTypeCommand(new byte[]{(byte) 0x02, (byte) 0x00});
                break;
            case R.id.rb_wind3_rs:
                mWindType3.setText("风量");
                if (!rb_wind3_rs.isPressed()) {
                    return;
                }
                setFanTypeCommand(new byte[]{(byte) 0x02, (byte) 0x01});
                break;
            case R.id.rb_wind3_dc:
                mWindType3.setText("风量");
                if (!rb_wind3_dc.isPressed()) {
                    return;
                }
                setFanTypeCommand(new byte[]{(byte) 0x02, (byte) 0x02});
                break;
            case R.id.rb_wind4_pwm:
                mWindType4.setText("百分比");
                if (!rb_wind4_pwm.isPressed()) {
                    return;
                }
                setFanTypeCommand(new byte[]{(byte) 0x03, (byte) 0x00});
                break;
            case R.id.rb_wind4_rs:
                mWindType4.setText("风量");
                if (!rb_wind4_rs.isPressed()) {
                    return;
                }
                setFanTypeCommand(new byte[]{(byte) 0x03, (byte) 0x01});
                break;
        }
    }

    //设置风机类型
    public void setFanTypeCommand(byte[] bytes) {
        FanCommand command = new FanCommand(FunctionObject.SET_FAN_TYPE);
        command.setData(bytes);
        SpDataProcessor.getInstance().send(command);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

        if (!(actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) || v == null) {
            return false;
        }

        // 判断触发事件的控件ID
        if (v.getId() == R.id.et_small_wind1 || v.getId() == R.id.et_middle_wind1 || v.getId() == R.id.et_high_wind1) {
            onWind1BtnClick(v);
            hideKeyboardAndClearFocus(v); // 隐藏键盘+失去焦点
            return true;
        } else if (v.getId() == R.id.et_small_wind2 || v.getId() == R.id.et_middle_wind2 || v.getId() == R.id.et_high_wind2) {
            onWind2BtnClick(v);
            hideKeyboardAndClearFocus(v); // 隐藏键盘+失去焦点
            return true;
        }else if (v.getId() == R.id.et_small_wind3 || v.getId() == R.id.et_middle_wind3 || v.getId() == R.id.et_high_wind3) {
            onWind3BtnClick(v);
            hideKeyboardAndClearFocus(v); // 隐藏键盘+失去焦点
            return true;
        }else if (v.getId() == R.id.et_small_wind4 || v.getId() == R.id.et_middle_wind4 || v.getId() == R.id.et_high_wind4) {
            onWind4BtnClick(v);
            hideKeyboardAndClearFocus(v); // 隐藏键盘+失去焦点
            return true;
        }else if (v.getId() == R.id.et_small_co2 || v.getId() == R.id.et_middle_co2 || v.getId() == R.id.et_high_co2) {
            onCo2SetClick(v);
            hideKeyboardAndClearFocus(v); // 隐藏键盘+失去焦点
            return true;
        }else if (v.getId() == R.id.et_small_pm || v.getId() == R.id.et_middle_pm || v.getId() == R.id.et_high_pm) {
            onCo2SetClick(v);
            hideKeyboardAndClearFocus(v); // 隐藏键盘+失去焦点
            return true;
        }

        return false;
    }

    /**
     * 封装：隐藏软键盘 + 让输入框失去焦点
     * @param view 触发事件的输入框
     */
    private void hideKeyboardAndClearFocus(View view) {
        // 1. 隐藏软键盘
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        // 2. 让输入框失去焦点（需先设置view可取消焦点）
        view.clearFocus();
        // 可选：如果clearFocus无效，补充设置focusable为false再恢复（适配部分机型）
        view.setFocusable(false);
        view.setFocusableInTouchMode(true);
    }
}
