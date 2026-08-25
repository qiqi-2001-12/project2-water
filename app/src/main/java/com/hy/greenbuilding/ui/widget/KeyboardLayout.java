package com.hy.greenbuilding.ui.widget;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

public class KeyboardLayout extends LinearLayout {

    private OnKeyboardVisibilityListener keyboardVisibilityListener;
    // 存储全屏状态下 DecorView 可见区域的底部 Y 坐标（最可靠的屏幕高度基准）
    private int fullScreenVisibleBottom = 0;
    // 状态锁，防止 onGlobalLayout 频繁回调导致重复触发事件
    private boolean isKeyboardShowing = false;

    public KeyboardLayout(Context context) {
        super(context);
        init();
    }

    public KeyboardLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        ViewTreeObserver vto = getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final View decorView = getRootView();
                Rect visibleRect = new Rect();
                decorView.getWindowVisibleDisplayFrame(visibleRect);

                // 1. 初始化：存储最大的可见底部坐标作为全屏高度基准
                // 只有当 fullScreenVisibleBottom 为 0 或当前可见底部更大时才更新（防止状态栏变化影响）
                if (fullScreenVisibleBottom == 0 || visibleRect.bottom > fullScreenVisibleBottom) {
                    fullScreenVisibleBottom = visibleRect.bottom;
                    if (fullScreenVisibleBottom > 0) return; // 初始化完成，跳过首次布局
                }

                // 2. 计算键盘高度：基准底部 - 当前可见底部
                int keyboardHeight = fullScreenVisibleBottom - visibleRect.bottom;

                // 3. 设置阈值 (50dp 转换为像素)
                final int KEYBOARD_THRESHOLD_PX = dpToPx(getContext(), 50);

                // Log 语句，方便未来的调试
                Log.e("TAG", "onGlobalLayout: KeyboardHeight=" + keyboardHeight +
                        " | ThresholdPx=" + KEYBOARD_THRESHOLD_PX +
                        " | VisibleBottom=" + visibleRect.bottom);

                // 4. 核心判断逻辑
                if (keyboardHeight > KEYBOARD_THRESHOLD_PX) { // 键盘弹出
                    if (!isKeyboardShowing) {
                        isKeyboardShowing = true;
                        if (keyboardVisibilityListener != null) {
                            keyboardVisibilityListener.onKeyboardShow();
                        }
                    }
                } else if (keyboardHeight < dpToPx(getContext(), 10)) { // 键盘高度非常小时判断为隐藏（10dp作为安全余量）
                    if (isKeyboardShowing) {
                        isKeyboardShowing = false;
                        if (keyboardVisibilityListener != null) {
                            keyboardVisibilityListener.onKeyboardHide();
                        }
                    }
                }
            }
        });
    }

    public void setOnKeyboardVisibilityListener(OnKeyboardVisibilityListener listener) {
        this.keyboardVisibilityListener = listener;
    }

    public interface OnKeyboardVisibilityListener {
        void onKeyboardShow();
        void onKeyboardHide();
    }

    private int dpToPx(Context context, int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }
}