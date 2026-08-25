package com.hy.greenbuilding.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.hy.greenbuilding.R;
import com.hy.greenbuilding.model.UpTempSystemStatusInfo;

import java.util.ArrayList;
import java.util.List;

public class SystemStatusAdapter extends RecyclerView.Adapter<SystemStatusAdapter.SystemStatusHolder> {
    private LayoutInflater layoutInflater;
    private List<UpTempSystemStatusInfo> mList = new ArrayList<>();
    private Context mContext;
    private int mType = 0;
    public SystemStatusAdapter(Context context, List<UpTempSystemStatusInfo> list,int type) {
        layoutInflater = LayoutInflater.from(context);
        mList.clear();
        mList.addAll(list);
        mContext = context;
        mType = type;
    }

    @Override
    public SystemStatusHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(mType == 0){
            return new SystemStatusHolder(layoutInflater.inflate(R.layout.up_temp_system_status_item, parent, false));
        }else{
            return new SystemStatusHolder(layoutInflater.inflate(R.layout.up_temp_error_item_item, parent, false));
        }

    }

    @Override
    public void onBindViewHolder(SystemStatusHolder holder, int position) {
        holder.initViews(mList, position, true);
    }

    public void setList(List<UpTempSystemStatusInfo> list){
        mList.clear();
        mList.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class SystemStatusHolder extends RecyclerView.ViewHolder {
        TextView title;

        public SystemStatusHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_listTitle);
        }

        void initViews(List<UpTempSystemStatusInfo> itemList, int pos, boolean isRecycleView) {
            UpTempSystemStatusInfo menuItem = itemList.get(pos);
            title.setText(menuItem.getName());

        }
    }
}
