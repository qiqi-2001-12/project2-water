package com.hy.greenbuilding.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.hy.greenbuilding.R;
import com.hy.greenbuilding.model.RoomInfo;

import java.util.ArrayList;
import java.util.List;

public class AddRoomListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    // 定义两种视图类型
    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_ADD = 1;

    private LayoutInflater layoutInflater;
    private List<RoomInfo> mList = new ArrayList<>();
    private Context mContext;

    public AddRoomListAdapter(Context context, List<RoomInfo> list) {
        this.mContext = context;
        layoutInflater = LayoutInflater.from(context);
        setList(list);
    }

    public void setList(List<RoomInfo> mDataList) {
        this.mList.clear();
        this.mList.addAll(mDataList);
        notifyDataSetChanged();
    }

    // --- 核心修改 1: 获取视图类型 ---
    @Override
    public int getItemViewType(int position) {
        // 如果是最后一个位置，则显示添加按钮
        if (position == mList.size()) {
            return VIEW_TYPE_ADD;
        }
        return VIEW_TYPE_ITEM;
    }

    // --- 核心修改 2: 创建 ViewHolder ---
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ADD) {
            View addView = layoutInflater.inflate(R.layout.add_room_button, parent, false);
            return new AddButtonHolder(addView);
        } else {
            View itemView = layoutInflater.inflate(R.layout.add_room_item, parent, false);
            return new AddRoomListHolder(itemView);
        }
    }

    // --- 核心修改 3: 绑定 ViewHolder ---
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_ADD) {
            // 绑定添加按钮的点击事件
            AddButtonHolder addHolder = (AddButtonHolder) holder;
            addHolder.llAddContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemEditListener != null) {
                        // 触发新增房间的接口
                        mOnItemEditListener.onItemAddClick(view);
                    }
                }
            });
        } else {
            // 绑定房间列表项的数据
            AddRoomListHolder itemHolder = (AddRoomListHolder) holder;
            itemHolder.initViews(mList, position);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size() + 1;
    }

    class AddRoomListHolder extends RecyclerView.ViewHolder {
        TextView roomName;
        TextView roomTemp;
        TextView tv_addRoomTitle;
        ImageView iv_Edit;
        ImageView iv_delete;
        private int position;

        public AddRoomListHolder(View itemView) {
            super(itemView);
            roomName = itemView.findViewById(R.id.tv_roomItem_name);
            roomTemp = itemView.findViewById(R.id.tv_roomItem_temp);
            iv_Edit = itemView.findViewById(R.id.iv_roomItem_edit);
            iv_delete = itemView.findViewById(R.id.iv_roomItem_delete);
            tv_addRoomTitle = itemView.findViewById(R.id.tv_addRoomTitle);
        }
        void initViews(List<RoomInfo> itemList, int pos) {
            position = pos;
            RoomInfo roomInfo = itemList.get(position);
            roomName.setText(roomInfo.getRoomName());
            tv_addRoomTitle.setText(roomInfo.getRoomName()+"温度");
            roomTemp.setText(roomInfo.getTemp() + "");
            if(roomInfo.getRoomId()< 9){
                iv_delete.setVisibility(View.INVISIBLE);
            }else{
                iv_delete.setVisibility(View.VISIBLE);
            }
            iv_Edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemEditListener != null) {
                        mOnItemEditListener.onItemEditClick(view, position);
                    }
                }
            });
            iv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemEditListener != null) {
                        mOnItemEditListener.onItemDeleteClick(view, position);
                    }
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemEditListener !=null){
                        mOnItemEditListener.onItemClick(view, position);
                    }
                }
            });
        }
    }

    /**
     * 添加按钮项的 ViewHolder
     */
    class AddButtonHolder extends RecyclerView.ViewHolder {
        // 假设 add_room_button.xml 中用于点击的根布局 ID 为 ll_add_container
        LinearLayout llAddContainer;

        public AddButtonHolder(View itemView) {
            super(itemView);
            llAddContainer = itemView.findViewById(R.id.ll_add_container);
        }
    }

    public interface OnItemEditListener {
        void onItemEditClick(View view, int position);
        void onItemDeleteClick(View view, int position);
        void onItemAddClick(View view);
        void onItemClick(View view, int position);

    }
    private OnItemEditListener mOnItemEditListener;

    public void setOnItemEditListener(OnItemEditListener onItemEditListener) {
        mOnItemEditListener = onItemEditListener;

    }

}
