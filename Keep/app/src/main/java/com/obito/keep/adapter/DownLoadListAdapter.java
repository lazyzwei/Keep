package com.obito.keep.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.obito.keep.R;
import com.obito.keep.adapter.viewholder.ItemViewHolder;
import com.obito.keeplib.KeepTask;

import java.util.ArrayList;
import java.util.List;


public class DownLoadListAdapter extends RecyclerView.Adapter {
    private static String TAG = "DownLoadListAdapter";

    LayoutInflater inflater;
    List<KeepTask> tasks;

    public void init(Context context) {
        inflater = LayoutInflater.from(context);
        tasks = new ArrayList<>();
    }

    public void setTask(List<KeepTask> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    public void addOneTask(KeepTask task) {
        if (tasks.contains(task)) {
            return;
        }
        tasks.add(task);
        notifyItemInserted(tasks.size() - 1);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.view_item_download, null);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ItemViewHolder viewHolder = (ItemViewHolder) holder;
        viewHolder.bind(tasks.get(position), position);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void updateTask(KeepTask task) {
        int pos = tasks.indexOf(task);
        System.out.println();
        Log.d(TAG, "DownLoadListAdapter updateTask pos : " + pos);
        if (pos >= 0) {
            tasks.set(pos, task);
            notifyItemChanged(pos);
        }
    }
}
