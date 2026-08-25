package com.hy.greenbuilding.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.hy.greenbuilding.R;
import com.hy.greenbuilding.model.RoomInfo;

import java.util.ArrayList;
import java.util.List;

public class AddRoomTitleAdapter extends RecyclerView.Adapter<AddRoomTitleAdapter.AddRoomTitleHolder>{
    private LayoutInflater layoutInflater;
    private List<RoomInfo> mList = new ArrayList<>();
    private int mPosition = -1;
    private Context mContext;
    public AddRoomTitleAdapter(Context context, List<RoomInfo> list) {
        mContext = context;
        mList.clear();
        layoutInflater = LayoutInflater.from(context);
        mList.addAll(list);

    }

    @Override
    public AddRoomTitleHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AddRoomTitleHolder(layoutInflater.inflate(R.layout.add_room_title, parent, false));
    }

    @Override
    public void onBindViewHolder(AddRoomTitleHolder holder, int position) {
        holder.initViews(mList, position, true);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class AddRoomTitleHolder extends RecyclerView.ViewHolder {
        TextView title;
        private int position;

        public AddRoomTitleHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_addRoomTitle);
        }

        void initViews(List<RoomInfo> itemList, int pos, boolean isRecycleView) {
            position = pos;
            RoomInfo roomInfo = itemList.get(position);
            title.setText((roomInfo.getRoomName()+"").trim());
            title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(view, position);
                    }
                }
            });
            title.setSelected(position == getmPosition());
        }
    }

    public int getmPosition() {
        return mPosition;
    }

    public void setmPosition(int mPosition) {
        this.mPosition = mPosition;
    }
    public void setList(List<RoomInfo> mDataList) {
        this.mList = mDataList;
        notifyDataSetChanged();
    }
    public interface OnItemClickListener {
        void onItemClick(View view, int position);

    }
    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;

    }

    public void clearSelection() {
        this.mPosition = -1;
        notifyDataSetChanged();
    }
}
