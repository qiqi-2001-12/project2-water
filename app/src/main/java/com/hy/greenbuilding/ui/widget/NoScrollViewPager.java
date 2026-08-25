package com.hy.greenbuilding.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

public class NoScrollViewPager extends ViewPager {
    private boolean isScrollEnabled = false; // 默认禁止滑动

    // 构造方法 1：在代码中动态创建 View 时调用
    public NoScrollViewPager(@NonNull Context context) {
        super(context);
    }

    // 构造方法 2：在 XML 布局文件中使用时调用 (这是你缺失的那个)
    public NoScrollViewPager(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 设置是否允许滑动
     * @param enabled true 允许滑动, false 禁止滑动
     */
    public void setScrollEnabled(boolean enabled) {
        isScrollEnabled = enabled;
    }

    /**
     * 重写触摸事件，控制是否允许滑动
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // 如果允许滑动，则调用父类方法；否则返回 false，不处理触摸事件
        return isScrollEnabled && super.onTouchEvent(ev);
    }

    /**
     * 重写事件拦截方法，控制是否拦截触摸事件
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // 如果允许滑动，则调用父类方法；否则返回 false，不拦截触摸事件
        return isScrollEnabled && super.onInterceptTouchEvent(ev);
    }
}