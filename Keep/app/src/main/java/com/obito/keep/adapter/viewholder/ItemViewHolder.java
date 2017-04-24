package com.obito.keep.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.obito.keep.R;
import com.obito.keeplib.Keep;
import com.obito.keeplib.KeepTask;


public class ItemViewHolder extends RecyclerView.ViewHolder {

    ProgressBar progressBar;
    Button btnStart;

    KeepTask task;

    public ItemViewHolder(View itemView) {
        super(itemView);
        progressBar = (ProgressBar) itemView.findViewById(R.id.progress);
        btnStart = (Button) itemView.findViewById(R.id.btn_start);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (task.getStatus()){
                    case 2:
                    case 1:
                        //downloading,waiting
                        Keep.getInstance().pauseTask(task.getUrl());
                        break;
                    case 0:
                        //idle
                        Keep.getInstance().resume(task.getUrl());
                        break;

                }
            }
        });
    }

    public void bind(KeepTask task){
        this.task = task;
        progressBar.setProgress(task.getProgress());
        if (task.getStatus() == KeepTask.Status.DOWNLOADING.getValue()){
            btnStart.setText("stop");
        }else {
            btnStart.setText("start");
        }
    }
}
