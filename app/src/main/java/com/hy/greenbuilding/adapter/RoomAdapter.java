package com.hy.greenbuilding.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.hy.greenbuilding.R;
import com.hy.greenbuilding.model.RoomInfo;

import java.util.ArrayList;
import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomHolder>{
    private LayoutInflater layoutInflater;
    private List<RoomInfo> roomList;

    public RoomAdapter(Context context, List<RoomInfo> foodItems) {
        layoutInflater = LayoutInflater.from(context);
        roomList = new ArrayList<>();
        roomList.addAll(foodItems);

    }

    @Override
    public RoomHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RoomHolder(layoutInflater.inflate(R.layout.main_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RoomHolder holder, int position) {
        holder.initViews(roomList, position, true);
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    class RoomHolder extends RecyclerView.ViewHolder {
        TextView roomName;
        TextView roomTemp;
        TextView roomHumidity;
        TextView roomPm;
        TextView roomCo2;
        TextView roomAirValue;
        private int position;

        public RoomHolder(View itemView) {
            super(itemView);
            roomName = itemView.findViewById(R.id.item_roomName);
            roomTemp = itemView.findViewById(R.id.item_temp);
            roomHumidity = itemView.findViewById(R.id.item_humidity);
            roomPm = itemView.findViewById(R.id.item_pm);
            roomCo2 = itemView.findViewById(R.id.item_co2);
            roomAirValue = itemView.findViewById(R.id.item_airValve);
        }

        void initViews(List<RoomInfo> itemList, int pos, boolean isRecycleView) {
            position = pos;
            RoomInfo menuItem = itemList.get(position);
            roomName.setText(menuItem.getRoomName());
            roomTemp.setText(menuItem.getTemp()+" \u2103");
            roomHumidity.setText(menuItem.getHumidity()+"rh");
            roomPm.setText(menuItem.getPm()+"μg/m³");
            roomCo2.setText(menuItem.getCo2()+"ppm");
            roomAirValue.setText(menuItem.getAirValve());

        }
    }
    public void setRoomList(List<RoomInfo> mDataList) {
        this.roomList = mDataList;
        notifyDataSetChanged();
    }
}
