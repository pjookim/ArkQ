package me.pjookim.arkq;

import android.content.Intent;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    //데이터 배열 선언
    private ArrayList<ItemObject> mList;

    public  class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView_img;
        private TextView serverTitle, queue, status;
        private ConstraintLayout listItem;

        public ViewHolder(View itemView) {
            super(itemView);

            serverTitle = (TextView) itemView.findViewById(R.id.server_title);
            queue = (TextView) itemView.findViewById(R.id.queue);
            status = (TextView) itemView.findViewById(R.id.status);
            listItem = (ConstraintLayout) itemView.findViewById(R.id.list_item);
        }
    }

    //생성자
    public MyAdapter(ArrayList<ItemObject> list) {
        this.mList = list;
    }

    @NonNull
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_data, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyAdapter.ViewHolder holder, int position) {
        holder.serverTitle.setText(String.valueOf(mList.get(position).getServer()));
        DecimalFormat formatter = new DecimalFormat("#,###,###");
        String formattedQueue = formatter.format(mList.get(position).getQueue());
        holder.queue.setText(formattedQueue);
        int nQueue = mList.get(position).getQueue();
        if(nQueue>3000) {
            holder.status.setText("혼잡");
            holder.status.setTextColor(Color.parseColor("#e74c3c"));
        } else if(nQueue>1000) {
            holder.status.setText("앵간");
            holder.status.setTextColor(Color.parseColor("#f5c815"));
        } else {
            holder.status.setText("원활");
            holder.status.setTextColor(Color.parseColor("#27ae60"));
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}