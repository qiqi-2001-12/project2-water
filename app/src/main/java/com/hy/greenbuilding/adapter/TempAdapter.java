package com.hy.greenbuilding.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.hy.greenbuilding.R;
import com.hy.greenbuilding.model.UpTempItem;
import com.hy.greenbuilding.utils.MySpUtil;

import java.util.ArrayList;
import java.util.List;

public class TempAdapter extends RecyclerView.Adapter<TempAdapter.UpTempHolder> {
    private LayoutInflater layoutInflater;
    private List<UpTempItem> mList;
    private Context mContext;

    public TempAdapter(Context context, List<UpTempItem> list) {
        layoutInflater = LayoutInflater.from(context);
        mList = new ArrayList<>();
        mList.addAll(list);
        mContext = context;
    }

    @Override
    public UpTempHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new UpTempHolder(layoutInflater.inflate(R.layout.up_temp_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(UpTempHolder holder, int position) {
        holder.initViews(mList, position, true);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class UpTempHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView value;
        RelativeLayout relativeLayout;
        private int position;

        public UpTempHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_listTitle);
            value = itemView.findViewById(R.id.tv_listValue);
            relativeLayout = itemView.findViewById(R.id.rl_item_height);
            boolean isCareMode = (boolean) MySpUtil.getParam(mContext, MySpUtil.CARE_MODE, false);
            if (isCareMode) {
                relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(260, 80));
            } else {
                relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(260, 45));
            }
        }

        void initViews(List<UpTempItem> itemList, int pos, boolean isRecycleView) {
            position = pos;
            UpTempItem menuItem = itemList.get(position);
            title.setText(menuItem.getTitle());
            value.setText(menuItem.getValue());
        }
    }
}
