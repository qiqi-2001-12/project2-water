package com.hy.greenbuilding.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.hy.greenbuilding.R;
import com.hy.greenbuilding.model.AirValveItemInfo;
import com.hy.greenbuilding.ui.widget.KeyboardEditText;

import java.util.ArrayList;
import java.util.List;

public class AirValveSettingAdapter1 extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<AirValveItemInfo> mList = new ArrayList<>();

    private Activity mActivity;

    private OnItemMaxClickListener mOnItemMaxClickListener;
    private OnItemClickListener mOnItemClickListener;

    public void setOnItemMaxClickListener(OnItemMaxClickListener onItemMaxClickListener) {
        mOnItemMaxClickListener = onItemMaxClickListener;

    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;

    }

    public AirValveSettingAdapter1(Activity activity, List<AirValveItemInfo> mList) {
        mActivity = activity;
        this.mList = mList;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemLayout(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.air_valve_setting_item,
                parent,
                false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        ItemLayout binding = (ItemLayout) holder;
        AirValveItemInfo airValveItemInfo = mList.get(position);
        binding.title.setText("风阀" + airValveItemInfo.getValveId());
        if(!binding.maxNumEt.isFocused())binding.maxNumEt.setText(airValveItemInfo.getMaxNumber() + "");
        binding.openValueTv.setText(airValveItemInfo.getRealOpenValue() + "");
        if(!binding.openValueEt.isFocused())binding.openValueEt.setText("0");
        binding.maxNumBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemMaxClickListener != null) {
                    String maxValue = binding.maxNumEt.getText().toString().trim();
                    mOnItemMaxClickListener.onItemMaxClick(v, position, maxValue);
                }
            }
        });

        binding.openValueBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    String openValue = binding.openValueEt.getText().toString();
                    String maxValue = binding.maxNumEt.getText().toString().trim();
                    mOnItemClickListener.onItemClick(v, position, openValue, maxValue);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public AirValveItemInfo getItem(int position) {
        return mList.get(position);
    }

    public static class ItemLayout extends RecyclerView.ViewHolder {

        TextView title;
        KeyboardEditText maxNumEt;
        Button maxNumBt;
        KeyboardEditText openValueEt;
        Button openValueBt;
        TextView openValueTv;

        public ItemLayout(View view) {
            super(view);
            title = view.findViewById(R.id.name);
            maxNumEt = view.findViewById(R.id.max_num_et);
            maxNumBt = view.findViewById(R.id.max_num_bt);
            openValueEt = view.findViewById(R.id.open_value_et);
            openValueBt = view.findViewById(R.id.open_value_bt);
            openValueTv = view.findViewById(R.id.real_value_tv);
        }
    }

    public interface OnItemMaxClickListener {
        void onItemMaxClick(View view, int position, String maxValue);

    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position, String openValue, String maxValue);
    }
}
