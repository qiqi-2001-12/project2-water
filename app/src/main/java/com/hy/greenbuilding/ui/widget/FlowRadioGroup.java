package com.hy.greenbuilding.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.RadioGroup;

public class FlowRadioGroup extends RadioGroup {
    private int horizontalSpacing = dp2px(16); // 按钮水平间距
    private int verticalSpacing = dp2px(8);    // 行垂直间距

    public FlowRadioGroup(Context context) {
        super(context);
        init();
    }

    public FlowRadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOrientation(VERTICAL); // 外层垂直布局
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int parentWidth = r - l;
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();

        int currentLeft = paddingLeft;
        int currentTop = paddingTop;
        int lineMaxHeight = 0;

        // 遍历所有直接子View（RadioButton）
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;

            // 测量子View尺寸
            child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            // 换行判断：当前行剩余宽度不足放下当前按钮
            if (currentLeft + childWidth > parentWidth - paddingRight) {
                currentTop += lineMaxHeight + verticalSpacing;
                currentLeft = paddingLeft;
                lineMaxHeight = 0;
            }

            // 布局当前按钮
            child.layout(currentLeft, currentTop, currentLeft + childWidth, currentTop + childHeight);

            // 更新行参数
            currentLeft += childWidth + horizontalSpacing;
            lineMaxHeight = Math.max(lineMaxHeight, childHeight);
        }
    }

    // dp转px工具方法
    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }
}
