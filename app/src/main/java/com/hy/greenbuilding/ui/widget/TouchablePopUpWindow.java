package com.hy.greenbuilding.ui.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupWindow;
import android.view.LayoutInflater;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.hy.greenbuilding.HyApplication;
import com.hy.greenbuilding.R;
import com.hy.greenbuilding.config.SaveControlInfo;
import com.hy.greenbuilding.event.VersionUpdateEvent;
import com.hy.greenbuilding.ui.activity.HomeActivity;
import com.hy.greenbuilding.ui.activity.ManagerActivity;
import com.hy.greenbuilding.ui.activity.SettingActivity;
import com.hy.greenbuilding.ui.fragment.SettingCodeShowFragment;
import com.hy.greenbuilding.utils.MySpUtil;
import com.hy.greenbuilding.utils.PackageUtil;
import com.hy.greenbuilding.utils.StringUtils;
import com.hy.greenbuilding.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Objects;

import butterknife.BindView;

public class TouchablePopUpWindow extends PopupWindow {
    private Context mContext;
    private int mStartY;
    private int initialPopupHeight;
    private boolean isPopShowing = false;
    private final int targetHeight;


    @BindView(R.id.tv_control_version)
    TextView mControlVersion;
    @BindView(R.id.tv_app_version)
    TextView mAppVersion;
    private final TextView mVersionUpdate;

    // --- 权限委托接口 ---
    public interface BrightnessPermissionListener {
        // 当需要WRITE_SETTINGS权限时，Activity应该实现此方法来启动权限请求流程
        void onPermissionNeeded();

        // Activity应该实现此方法来检查当前是否拥有WRITE_SETTINGS权限
        boolean canWriteSettings();

        boolean isCaringMode(boolean CaringMode);
    }

    private BrightnessPermissionListener mListener;
    private SeekBar mSeekBar;
    // --- 权限委托接口 ---

    public void setBrightnessPermissionListener(BrightnessPermissionListener listener) {
        this.mListener = listener;
    }

    public TouchablePopUpWindow(Activity context, View viewById) {
        super(context);
        mContext = context;
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        // 获取屏幕总高度
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        // 计算 90% 的高度: (总高度 * 0.9)
        targetHeight = (int) (screenHeight * 0.95);
        setHeight(targetHeight);
        setBackgroundDrawable(new ColorDrawable(0));
        View mContentView = LayoutInflater.from(context).inflate(R.layout.touchable_pop, null);
        mSeekBar = mContentView.findViewById(R.id.seekBar); // 使用成员变量
        mControlVersion = mContentView.findViewById(R.id.tv_control_version);
        mAppVersion = mContentView.findViewById(R.id.tv_app_version);
        mVersionUpdate = mContentView.findViewById(R.id.tv_version_update);

        SaveControlInfo controlInfo = MySpUtil.getControlData(context);
        if (controlInfo != null) {
            if (controlInfo.getControl_version() == null) {
                mControlVersion.setText("");
            } else {
                mControlVersion.setText("v" + controlInfo.getControl_version());
            }

        }
        mAppVersion.setText("v" + PackageUtil.getVersion(context));

        View viewPop = mContentView.findViewById(R.id.ll_pop);
        setAnimationStyle(R.style.PopupWindowAnimation);
        setContentView(mContentView);
        setClippingEnabled(false);

        LinearLayout tvWifi = mContentView.findViewById(R.id.tv_wifi);
        LinearLayout tvCaringMode = mContentView.findViewById(R.id.tv_caring_mode);
        LinearLayout tvDarkMode = mContentView.findViewById(R.id.tv_dark_mode);
        LinearLayout tvClassicMode = mContentView.findViewById(R.id.tv_classic_mode);
        LinearLayout tvAdmin = mContentView.findViewById(R.id.tv_admin);
        boolean isCareMode = (boolean) MySpUtil.getParam(context, MySpUtil.CARE_MODE, false);

        if (isCareMode) {
            tvClassicMode.setVisibility(View.VISIBLE);
            tvCaringMode.setVisibility(View.GONE);
        } else {
            tvClassicMode.setVisibility(View.GONE);
            tvCaringMode.setVisibility(View.VISIBLE);
        }

        tvAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPasswordDialog(context);
            }
        });
        tvWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mContext instanceof androidx.appcompat.app.AppCompatActivity) {
                    androidx.appcompat.app.AppCompatActivity activity = (androidx.appcompat.app.AppCompatActivity) mContext;
                    SettingCodeShowFragment fragment = new SettingCodeShowFragment();
                    // 使用AndroidX的SupportFragmentManager
                    fragment.show(activity.getSupportFragmentManager(), "sodeshow");
                    dismiss();
                    isPopShowing = false;
                }
            }
        });

        tvCaringMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                tvCaringMode.setVisibility(View.GONE);
//                tvDarkMode.setVisibility(View.GONE);
//                tvClassicMode.setVisibility(View.GONE);
//                tvWifi.setVisibility(View.VISIBLE);
//                tvAdmin.setVisibility(View.VISIBLE);
                if (mListener != null) {
                    dismiss();
                    mListener.isCaringMode(true);
                }
            }
        });

        tvClassicMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                tvCaringMode.setVisibility(View.VISIBLE);
//                tvDarkMode.setVisibility(View.VISIBLE);
//                tvClassicMode.setVisibility(View.VISIBLE);
//                tvWifi.setVisibility(View.GONE);
//                tvAdmin.setVisibility(View.GONE);
                mListener.isCaringMode(false);

            }
        });

        // 尝试获取当前系统亮度，并设置给SeekBar
        try {
            int currentBrightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            mSeekBar.setProgress(currentBrightness);
        } catch (Settings.SettingNotFoundException e) {
            Log.e("PopupWindow", "Current brightness setting not found.", e);
        }

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private boolean permissionRequested = false;

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                permissionRequested = false;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                permissionRequested = false;
                // 拖动开始时，检查权限并仅请求一次
                if (mListener != null && !mListener.canWriteSettings() && !permissionRequested) {
                    mListener.onPermissionNeeded();
                    permissionRequested = true;
                    Toast.makeText(mContext, "请授予修改系统设置权限。", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;

                int brightnessValue = progress;

                if (mListener != null && mListener.canWriteSettings()) {
                    try {
                        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightnessValue);
                    } catch (Exception e) {
                        setWindowBrightness(context, brightnessValue);
                    }
                } else {
                    setWindowBrightness(context, brightnessValue);
                }
            }
        });


        viewById.setOnTouchListener((v, event) -> {


            boolean isOtaOpen = (boolean) MySpUtil.getParam(mContext, MySpUtil.OTA_STATUS, false);
            if (isOtaOpen) {
                ToastUtil.showToast(mContext, mContext.getString(R.string.server_not_permission));
                return true;
            }

            if (HyApplication.isLocking) {
                ToastUtil.showToast(mContext, mContext.getString(R.string.device_locked));
                return true;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mStartY = (int) event.getRawY();
                    initialPopupHeight = getHeight();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!isPopShowing) {
                        showAtLocation(context.getWindow().getDecorView(), Gravity.TOP, 0, 0);
                        isPopShowing = true;
                    } else {
                        update(0, 0, -1, (int) event.getRawY(), true);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (isPopShowing && getHeight() < targetHeight / 2) {
                        dismiss();
                        isPopShowing = false;
                    } else {
                        update(0, 0, -1, targetHeight, true);
                    }
                    break;
            }
            return true;
        });

        viewPop.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if ((boolean) MySpUtil.getParam(mContext, MySpUtil.OTA_STATUS, false)) {
                    ToastUtil.showToast(mContext, mContext.getResources().getString(R.string.server_not_permission));
                    return true;
                }

                if (HyApplication.isLocking) {
                    ToastUtil.showToast(mContext, mContext.getString(R.string.device_locked));
                    return true;
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // 只有在显示时才更新
                        if (isShowing()) {
                            update(0, 0, -1, (int) event.getRawY(), true);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (getHeight() < targetHeight / 2) {
                            dismiss();
                            isPopShowing = false;
                        } else {
                            update(0, 0, -1, targetHeight, true);
                        }
                        break;
                }
                // 确保事件被消耗
                return true;
            }
        });
    }

    /**
     * 设置当前 Activity 窗口的亮度 (无需权限, 回退方案)
     *
     * @param brightnessValue 亮度值，范围 0-255
     */
    private void setWindowBrightness(Activity activity, int brightnessValue) {
        // 规范化亮度值到 0.0f 到 1.0f 之间
        float normalizedBrightness = (float) brightnessValue / 255f;

        Window window = activity.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.screenBrightness = normalizedBrightness;
            window.setAttributes(params);
        }
    }


    public void update(int left, int top, int width, int height, boolean isAnimated) {
        super.update(left, top, width, height, isAnimated);
    }

    //管理员密码框
    private void showPasswordDialog(Activity activity) {
        View view = LayoutInflater.from(activity).inflate(R.layout.manager_pwd_dialog, null, false);
        AlertDialog mPwdDialog = new AlertDialog.Builder(activity).setView(view).create();
        Button sure = view.findViewById(R.id.bt_pwd_sure);
        EditText mEtManagerPwd = view.findViewById(R.id.et_manager_pwd);
        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String password = mEtManagerPwd.getText().toString().trim();
                if (!StringUtils.isNullOrEmpty(password) && password.equals(StringUtils.INIT_PASSWORD)) {
                    Intent intent = new Intent(activity, ManagerActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity.startActivity(intent);
                    mPwdDialog.dismiss();
                    dismiss();
                } else {
                    ToastUtil.showToast(activity, "请输入正确的管理员密码！");
                }

            }
        });
        mPwdDialog.show();
        mPwdDialog.getWindow().setLayout(550, 460);
        mPwdDialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        mPwdDialog.getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                uiOptions |= 0x00001000;
                mPwdDialog.getWindow().getDecorView().setSystemUiVisibility(uiOptions);
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateEvent(VersionUpdateEvent event) {
        if (event != null) {
            if (event.getType() == 1) {
                mVersionUpdate.setVisibility(View.VISIBLE);
                mVersionUpdate.setText(event.getMessage());
            } else if (event.getType() == 2) {
                mControlVersion.setText(event.getMessage());
            } else if (event.getType() == 3) {
                mVersionUpdate.setVisibility(View.INVISIBLE);
            }
        }
    }
}
