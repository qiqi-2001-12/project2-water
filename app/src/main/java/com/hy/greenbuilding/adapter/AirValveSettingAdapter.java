package com.hy.greenbuilding.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.hy.greenbuilding.R;
import com.hy.greenbuilding.model.AirValveItemInfo;
import com.hy.greenbuilding.ui.widget.KeyboardEditText;

import java.util.ArrayList;
import java.util.List;

public class AirValveSettingAdapter extends RecyclerView.Adapter<AirValveSettingAdapter.ItemHolder> {
    private LayoutInflater layoutInflater;
    private List<AirValveItemInfo> mList = new ArrayList<>();
    private Context mContext;

    public AirValveSettingAdapter(Context context, List<AirValveItemInfo> list) {
        layoutInflater = LayoutInflater.from(context);
        mList.clear();
        mList.addAll(list);
        mContext = context;
    }


    private OnItemMaxClickListener mOnItemMaxClickListener;
    private OnItemClickListener mOnItemClickListener;

    public void setOnItemMaxClickListener(OnItemMaxClickListener onItemMaxClickListener) {
        mOnItemMaxClickListener = onItemMaxClickListener;

    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;

    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemHolder(layoutInflater.inflate(R.layout.air_valve_setting_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        holder.initViews(mList, position, true);
    }

    public void setList(List<AirValveItemInfo> list) {
        mList.clear();
        mList.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class ItemHolder extends RecyclerView.ViewHolder {
        TextView title;
        KeyboardEditText maxNumEt;
        Button maxNumBt;
        KeyboardEditText openValueEt;
        Button openValueBt;
        TextView openValueTv;

        public ItemHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.name);
            maxNumEt = itemView.findViewById(R.id.max_num_et);
            maxNumBt = itemView.findViewById(R.id.max_num_bt);
            openValueEt = itemView.findViewById(R.id.open_value_et);
            openValueBt = itemView.findViewById(R.id.open_value_bt);
            openValueTv = itemView.findViewById(R.id.real_value_tv);

        }

        void initViews(List<AirValveItemInfo> itemList, int pos, boolean isRecycleView) {
            AirValveItemInfo airValveItemInfo = itemList.get(pos);
            title.setText("风阀" + airValveItemInfo.getValveId());
            maxNumEt.setText(airValveItemInfo.getMaxNumber() + "");
            openValueTv.setText(airValveItemInfo.getRealOpenValue() + "");
            maxNumBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemMaxClickListener != null) {
                        String maxValue = maxNumEt.getText().toString().trim();
                        mOnItemMaxClickListener.onItemMaxClick(v, pos, maxValue);
                    }
                }
            });

            openValueBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        String openValue = openValueEt.getText().toString();
                        String maxValue = maxNumEt.getText().toString().trim();
                        mOnItemClickListener.onItemClick(v, pos, openValue, maxValue);
                    }
                }
            });
        }
    }

    public interface OnItemMaxClickListener {
        void onItemMaxClick(View view, int position, String maxValue);

    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position, String openValue, String maxValue);
    }
}

