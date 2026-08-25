package com.hy.greenbuilding.ui.activity;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout; // <-- 导入 RelativeLayout
import android.graphics.Color;
import android.provider.Settings; // 导入 Settings
import android.content.Intent; // 导入 Intent
import android.net.Uri; // 导入 Uri
import android.widget.Toast; // 导入 Toast

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hy.greenbuilding.ui.widget.TouchablePopUpWindow;
import com.hy.greenbuilding.utils.MySpUtil;
import com.hy.greenbuilding.R;

import org.greenrobot.eventbus.EventBus;

// 1. 声明实现接口
public abstract class BaseActivity extends AppCompatActivity implements TouchablePopUpWindow.BrightnessPermissionListener {
    private RelativeLayout parentLinearLayout;
    private TouchablePopUpWindow mPopupWindow; // 声明成员变量来持有 PopupWindow 实例
    private static final int REQUEST_WRITE_SETTINGS = 101; // 用于 onActivityResult 的请求码
    private View baseLayoutRootView;
    /**
     * 注意：这里假设您的全局布局文件 ID 为 R.layout.activity_base
     */
    private static final int BASE_LAYOUT_ID = R.layout.activity_base;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        NavigationBarStatusBar(this,true);
        setupKeyboardListener();
        boolean isCareMode = (boolean) MySpUtil.getParam(this, MySpUtil.CARE_MODE, false);
        if (isCareMode) initFontScale((float) 1);
        else initFontScale((float) 1);

        initContentView(BASE_LAYOUT_ID);
    }

    private void initContentView(int layoutResID) {
        ViewGroup group = (ViewGroup) findViewById(android.R.id.content);
        group.removeAllViews();

        parentLinearLayout = new RelativeLayout(this);
        parentLinearLayout.setBackgroundColor(Color.parseColor("#F4F0EA"));

        group.addView(parentLinearLayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));


    }

    @Override
    public boolean isCaringMode(boolean CaringMode) {
        MySpUtil.setParam(this, MySpUtil.CARE_MODE, CaringMode);

        if (CaringMode) {
            initFontScale((float) 1);
        } else {
            initFontScale((float) 1);
        }
        EventBus.getDefault().post("changeMode");
        return CaringMode;
    }

    @Override
    public void setContentView(int layoutResID) {
        View childView = LayoutInflater.from(this).inflate(layoutResID, null);
        RelativeLayout.LayoutParams childParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        parentLinearLayout.addView(childView, childParams);
        View inflate = LayoutInflater.from(this).inflate(BASE_LAYOUT_ID, parentLinearLayout, true);
        View viewById = inflate.findViewById(R.id.ll_pop); // 这是触摸窗口的目标视图
        this.baseLayoutRootView = viewById;
        // 实例化 PopupWindow 并设置监听器
        mPopupWindow = new TouchablePopUpWindow(this,viewById);
        mPopupWindow.setBrightnessPermissionListener(this);
    }

    /**
     * 【新增方法】控制 BASE_LAYOUT_ID 布局中需要控制的视图的显示/隐藏。
     * * @param visible true 为显示 (View.VISIBLE)，false 为隐藏 (View.GONE)
     */
    public void controlBaseLayoutVisibility(boolean visible) {
        if (baseLayoutRootView != null) {
            baseLayoutRootView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void setContentView(View view) {
        RelativeLayout.LayoutParams childParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        parentLinearLayout.addView(view, childParams);

        LayoutInflater.from(this).inflate(BASE_LAYOUT_ID, parentLinearLayout, true);
    }

    @Override
    public void setContentView(View view, LayoutParams params) {
        parentLinearLayout.addView(view, params);

        LayoutInflater.from(this).inflate(BASE_LAYOUT_ID, parentLinearLayout, true);
    }


    @Override
    public void onPermissionNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_WRITE_SETTINGS);
            }
        }
    }

    @Override
    public boolean canWriteSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.System.canWrite(this);
        }
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_WRITE_SETTINGS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(this)) {
                    Toast.makeText(this, "权限已授予，现在可以修改系统亮度。", Toast.LENGTH_LONG).show();
                    // 权限授予后，可以考虑自动更新 SeekBar 的值或刷新 UI
                } else {
                    Toast.makeText(this, "权限未授予，无法修改系统亮度。", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            NavigationBarStatusBar(this,true);
        }
    }

    public static void NavigationBarStatusBar(Activity activity, boolean hasFocus){
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = activity.getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    protected void hideBottomUIMenu() {
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;
        getWindow().setAttributes(params);
    }
    private void hideNavigationBar() {
        // 检查SDK版本是否支持全屏模式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 设置全屏模式
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            // 在较低版本的Android上，隐藏底部导航栏可能无法实现
            // 可以考虑其他的解决方案或给出适当的提示
        }
    }

    /**
     * 设置字体大小
     */
    protected void initFontScale(float size) {
        Configuration configuration = getResources().getConfiguration();
        configuration.fontScale = size;
        //0.85 小, 1 标准大小, 1.15 大，1.3 超大 ，1.45 特大
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        metrics.scaledDensity = configuration.fontScale * metrics.density;
        getBaseContext().getResources().updateConfiguration(configuration, metrics);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // ---------------------- 接口定义 ----------------------
    public interface KeyboardVisibilityListener {
        void onKeyboardVisibilityChanged(boolean isVisible);
    }

    // ---------------------- Activity 内部实现 ----------------------
    private KeyboardVisibilityListener keyboardListener;
    private View activityRootView;

    public void setKeyboardVisibilityListener(KeyboardVisibilityListener listener) {
        this.keyboardListener = listener;
    }

    // 在 onCreate 或 onContentChanged() 之后调用此方法
    private void setupKeyboardListener() {
        activityRootView = getWindow().getDecorView().getRootView();
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private boolean isKeyboardVisible = false;
            private final int KEYBOARD_THRESHOLD = 100; // 阈值（例如 100 像素）

            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                // 获取可见窗口区域，包括状态栏但不包括导航栏
                activityRootView.getWindowVisibleDisplayFrame(r);

                // 根视图的总高度
                int screenHeight = activityRootView.getRootView().getHeight();
                // 根视图可见区域的高度
                int visibleHeight = r.height();
                // 高度差（通常是键盘的高度）
                int heightDiff = screenHeight - visibleHeight;

                boolean newKeyboardVisible = heightDiff > KEYBOARD_THRESHOLD;

                if (newKeyboardVisible != isKeyboardVisible) {
                    isKeyboardVisible = newKeyboardVisible;
                    if (keyboardListener != null) {
                        keyboardListener.onKeyboardVisibilityChanged(isKeyboardVisible);
                    }
                }
            }
        });
    }

}
