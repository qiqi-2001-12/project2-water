package com.hy.greenbuilding.ui.widget.verticaltablayout.adapter;


import android.view.Gravity;

import com.hy.greenbuilding.R;
import com.hy.greenbuilding.ui.widget.verticaltablayout.widget.TabView;

public class MyTabAdapter implements TabAdapter {
    private final String[] titles;
    // 存储未选中状态的图标资源ID
    private int[] normalIcons;
    // 存储选中状态的图标资源ID
    private int[] selectedIcons;


    public MyTabAdapter(String[] titles) {
        this.titles = titles;
    }

    /**
     * 构造函数：需要传入标题、未选中图标和选中图标的资源ID数组
     */
    public MyTabAdapter(String[] titles, int[] normalIcons, int[] selectedIcons) {
        this.titles = titles;
        this.normalIcons = normalIcons;
        this.selectedIcons = selectedIcons;
    }

    @Override
    public int getCount() {
        return titles.length;
    }

    // 只需要提供标题
    @Override
    public TabView.TabTitle getTitle(int position) {
        return new TabView.TabTitle.Builder()
                .setContent(titles[position])
                .setTextColor(0xFF333333, 0xFF999999) // 选中色, 未选中色
                .build();
    }

    @Override
    public int getBackground(int position) {
        return R.color.baseTextColor;
    }

    @Override
    public TabView.TabIcon getIcon(int position) {
        // 使用 TabIcon.Builder 创建 TabIcon 实例
        return new TabView.TabIcon.Builder()
                // ⭐ 使用 setIcon 方法同时设置选中和未选中的图标
                .setIcon(selectedIcons[position], normalIcons[position])

                // 设置图标和文字之间的间距（单位：dp）
                .setIconMargin(10)
                // 设置图标相对于文字的位置，例如：Gravity.TOP（图标在上）或 Gravity.START（图标在左）
                .setIconGravity(Gravity.TOP)

                // 如果需要固定图标大小，可以使用 setIconSize(width, height)
                 .setIconSize(48, 48)

                .build();
    }

    @Override
    public TabView.TabBadge getBadge(int position) { return null; }
}
