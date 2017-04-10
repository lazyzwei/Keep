package com.obito.keep.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.obito.keep.R;
import com.obito.keeplib.KeepTask;


public class ItemViewHolder extends RecyclerView.ViewHolder {

    ProgressBar progressBar;
    Button btnStart;

    public ItemViewHolder(View itemView) {
        super(itemView);
        progressBar = (ProgressBar) itemView.findViewById(R.id.progress);
        btnStart = (Button) itemView.findViewById(R.id.btn_start);
    }

    public void bind(KeepTask task){
        progressBar.setProgress(task.getProgress());
    }
}
