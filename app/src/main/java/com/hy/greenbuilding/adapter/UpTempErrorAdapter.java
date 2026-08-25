package com.hy.greenbuilding.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hy.greenbuilding.R;
import com.hy.greenbuilding.model.UpTempSystemStatusInfo;
import com.hy.greenbuilding.model.UptempErrorInfo;

import java.util.ArrayList;
import java.util.List;

public class UpTempErrorAdapter extends RecyclerView.Adapter<UpTempErrorAdapter.SystemStatusHolder> {
    private LayoutInflater layoutInflater;
    private List<UptempErrorInfo> mList = new ArrayList<>();
    private Context mContext;

    public UpTempErrorAdapter(Context context, List<UptempErrorInfo> list) {
        layoutInflater = LayoutInflater.from(context);
        mList.clear();
        mList.addAll(list);
        mContext = context;
    }

    @Override
    public SystemStatusHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SystemStatusHolder(layoutInflater.inflate(R.layout.up_temp_system_error_item, parent, false));
    }

    @Override
    public void onBindViewHolder(SystemStatusHolder holder, int position) {
        holder.initViews(mList, position, true);
    }

    public void setList(List<UptempErrorInfo> list){
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
        RecyclerView  mListView;
        public SystemStatusHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_name);
            mListView = itemView.findViewById(R.id.recycler_view);
        }

        void initViews(List<UptempErrorInfo> itemList, int pos, boolean isRecycleView) {
            UptempErrorInfo menuItem = itemList.get(pos);
            title.setText(menuItem.getName());
            List<UpTempSystemStatusInfo> list = menuItem.getValue();
            SystemStatusAdapter mAdapter = new SystemStatusAdapter(mContext, list,1);
            mListView.setLayoutManager(new LinearLayoutManager(mContext,RecyclerView.HORIZONTAL,false));
            mListView.setAdapter(mAdapter);
        }
    }
}
