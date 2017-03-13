package com.obito.keep.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.obito.keep.R;
import com.obito.keep.adapter.viewholder.ItemViewHolder;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by zwfang on 2017/3/13.
 */

public class DownLoadListAdapter extends RecyclerView.Adapter {


    LayoutInflater inflater;
    List<String> tasks;

    public void init(Context context){
        inflater = LayoutInflater.from(context);
        tasks = new ArrayList<>();
    }

    public void setTask(List<String> tasks){
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.view_item_download,null);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ItemViewHolder viewHolder = (ItemViewHolder) holder;
        viewHolder.bind(position);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }


}
