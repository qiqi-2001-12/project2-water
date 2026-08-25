package com.hy.greenbuilding.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.hy.greenbuilding.R;
import com.hy.greenbuilding.protocol.FunctionObject;
import com.hy.greenbuilding.protocol.ResPonseInfo.PIDStatusInfo;
import com.hy.greenbuilding.protocol.SpDataProcessor;
import com.hy.greenbuilding.protocol.command.PIDCommand;
import com.hy.greenbuilding.utils.ByteUtils;
import com.hy.greenbuilding.utils.InputLimitUtil;
import com.hy.greenbuilding.utils.StringUtils;
import com.hy.greenbuilding.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.math.BigDecimal;
import java.nio.ByteBuffer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 防冻保护页面
 */
public class AntiFreezingFragment extends BaseDialogFragment {
    @BindView(R.id.li_back)
    ImageView mReturnView;
    @BindView(R.id.et_freezing_temp1)
    EditText etFreezingTemp1;
    @BindView(R.id.bt_freezing_set1)
    Button btFreezingSet1;
    @BindView(R.id.et_freezing_temp2)
    EditText etFreezingTemp2;
    @BindView(R.id.bt_freezing_set2)
    Button btFreezingSet2;
    @BindView(R.id.et_outTerm_temp)
    EditText etOutTemp;
    @BindView(R.id.bt_outTerm_set)
    Button btOutTempSet;
    @BindView(R.id.tv_outTerm_temp)
    TextView tvOuTTemp;
    @BindView(R.id.tv_ntc_temp)
    TextView tvNTCTemp;
    @BindView(R.id.tv_freezing_error)
    TextView tvFreezingError;
    @BindView(R.id.tv_freezing_setTe)
    TextView tvFreezingTe;

    @BindView(R.id.et_p_value)
    EditText etPValue;
    @BindView(R.id.et_i_value)
    EditText etIValue;
    @BindView(R.id.et_d_value)
    EditText etDValue;
    @BindView(R.id.et_pid_time)
    EditText etPIDTime;
    @BindView(R.id.et_pid_min)
    EditText etPIDMin;
    @BindView(R.id.bt_pid_set)
    Button btPIDSet;
    @BindView(R.id.tv_pid_status)
    TextView tvPidStatus;

    private View mView;
    private Unbinder unbinder;
    private Context mContext;
    private boolean isInit = true;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogFullScreen);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        this.getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        mView = inflater.inflate(R.layout.anti_freezing_main, null);
        mContext = this.getActivity();
        unbinder = ButterKnife.bind(this, mView);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        init();

        return mView;
    }

    private void init(){
        sendStatusCommand();
    }
    //获取PID状态
    private void sendStatusCommand(){
        PIDCommand command = new PIDCommand(FunctionObject.GET_PID_STATUS);
        SpDataProcessor.getInstance().send(command);
        mHandler.removeCallbacks(mRunnable);
        mHandler.postDelayed(mRunnable,1000 * 5);
    }

    @OnClick({R.id.li_back})
    public void onReturnClick(View view) {
        this.dismiss();
    }
    @OnClick({R.id.bt_freezing_set1})
    public void onTemp1Click(View view) {
        String temp = etFreezingTemp1.getText().toString();
        if(!StringUtils.isNullOrEmpty(temp)){
            int tempValue =  new BigDecimal(temp).intValue();
            PIDCommand command = new PIDCommand(FunctionObject.SET_PID_TEMP1);
            command.setData(ByteUtils.int16ToByteArray(tempValue));
            SpDataProcessor.getInstance().send(command);
            etFreezingTemp1.setText((float)tempValue/10+"");
        }
    }
    @OnClick({R.id.bt_freezing_set2})
    public void onTemp2Click(View view) {
        String temp = etFreezingTemp2.getText().toString();
        if(!StringUtils.isNullOrEmpty(temp)){
            int tempValue =  new BigDecimal(temp).intValue();
            PIDCommand command = new PIDCommand(FunctionObject.SET_PID_TEMP2);
            command.setData(ByteUtils.int16ToByteArray(tempValue));
            SpDataProcessor.getInstance().send(command);
            etFreezingTemp2.setText((float)tempValue/10+"");
        }
    }
    @OnClick({R.id.bt_outTerm_set})
    public void onTemp3Click(View view) {
        String temp = etOutTemp.getText().toString();
        if(!StringUtils.isNullOrEmpty(temp) ){
            int tempValue =  new BigDecimal(temp).intValue();
            PIDCommand command = new PIDCommand(FunctionObject.SET_OUT_TEMP);
            command.setData(ByteUtils.int16ToByteArray(tempValue));
            SpDataProcessor.getInstance().send(command);
            etOutTemp.setText((float)tempValue/10+"");
        }
    }
    @OnClick({R.id.bt_pid_set})
    public void onPIDClick(View view) {
        String pValue = etPValue.getText().toString();
        String iValue = etIValue.getText().toString();
        String dValue = etDValue.getText().toString();
        String pidTime = etPIDTime.getText().toString();
        String pidMin = etPIDMin.getText().toString();
        boolean input = InputLimitUtil.pidInputLimit(pValue,iValue,dValue,pidTime,pidMin);
        if(input){
            ByteBuffer byteBuffer = ByteBuffer.allocate(10);
            byteBuffer.put(ByteUtils.int16ToByteArray(Integer.parseInt(pValue)));
            byteBuffer.put(ByteUtils.int16ToByteArray(Integer.parseInt(iValue)));
            byteBuffer.put(ByteUtils.int16ToByteArray(Integer.parseInt(dValue)));
            byteBuffer.put(ByteUtils.int16ToByteArray(Integer.parseInt(pidTime)));
            byteBuffer.put(ByteUtils.int16ToByteArray(Integer.parseInt(pidMin)));
            PIDCommand pidCommand = new PIDCommand(FunctionObject.SET_PID_VALUE);
            pidCommand.setData(byteBuffer.array());
            SpDataProcessor.getInstance().send(pidCommand);
        }else{
            ToastUtil.showToast(mContext,getString(R.string.set_format_error));
        }
    }

    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            sendStatusCommand();
        }
    };

    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        if(mHandler != null){
            mHandler.removeCallbacks(mRunnable);
            mHandler = null;
        }
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void update(String s) {
        if(s!= null && s.equals("pid_update")){
            isInit = true;
            PIDCommand command = new PIDCommand(FunctionObject.GET_PID_STATUS);
            SpDataProcessor.getInstance().send(command);
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPIDEvent(PIDStatusInfo info) {
        if (info != null) {
            switch (info.getType()){
                case 1:
                    if(isInit){
                        etPValue.setText(info.getPValue()+"");
                        etIValue.setText(info.getIValue()+"");
                        etDValue.setText(info.getDValue()+"");
                        etPIDTime.setText(info.getPIDTime()+"");
                        etPIDMin.setText(info.getPIDMin()+"");
                        etFreezingTemp1.setText(info.getTempSet1()+"");
                        etFreezingTemp2.setText(info.getTempSet2()+"");
                        etOutTemp.setText(info.getDeviceTempSet()+"");
                    }
                    isInit = false;
                    tvOuTTemp.setText(info.getOutDeviceTemp()+" \u2103");
                    tvFreezingError.setText(info.getPIDError());
                    tvNTCTemp.setText(info.getPidNTC()+" \u2103");
                    if(info.getPIDStatus() == 1){//PID开关
                        tvPidStatus.setText("PID已开启");
                        //选择执行的目标温度
                        if(info.getChoiceTemp() == 0)
                            tvFreezingTe.setText(info.getTempSet1()+"");
                        else
                            tvFreezingTe.setText(info.getTempSet2()+"");
                    }else{
                        tvPidStatus.setText("PID未开启");
                        tvFreezingTe.setText("");
                    }
                    break;
                case 2:
                    if(info.getSuccess()){
                        ToastUtil.showToast(mContext,"设置PID成功！");
                    }else{
                        ToastUtil.showToast(mContext,"设置PID失败！");
                    }
                    break;
                case 3:
                    if(info.getSuccess()){
                        ToastUtil.showToast(mContext,"设置温度成功！");
                    }else{
                        ToastUtil.showToast(mContext,"设置温度失败！");
                    }
                    break;
            }
        }
    }
}
