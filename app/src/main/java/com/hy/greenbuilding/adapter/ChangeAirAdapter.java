package com.hy.greenbuilding.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.hy.greenbuilding.R;
import com.hy.greenbuilding.model.Detail;

import java.util.ArrayList;
import java.util.List;

public class ChangeAirAdapter extends RecyclerView.Adapter<ChangeAirAdapter.CallViewHolder>{
    private List<Detail> mList = new ArrayList<>();
    public ChangeAirAdapter(Context context,List<Detail> list) {
        mList.clear();
        if(list != null){
            mList.addAll(list);
        }
    }

    @Override
    public CallViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CallViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.change_air_dialog_item, parent, false));
    }

    @Override
    public void onBindViewHolder(CallViewHolder holder, final int position) {
        holder.deviceTv.setText(mList.get(position).getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(v,position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class CallViewHolder extends RecyclerView.ViewHolder{
        TextView deviceTv;
        public CallViewHolder(View itemView) {
            super(itemView);
            deviceTv = itemView.findViewById(R.id.tv_device_item);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

    }
    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;

    }
}
