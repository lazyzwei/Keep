package com.obito.keep;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.obito.keep.adapter.DownLoadListAdapter;
import com.obito.keeplib.DownloadListener;
import com.obito.keeplib.Keep;
import com.obito.keeplib.KeepTask;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DownloadListener {

    EditText editText;
    Button btnDownload;
    RecyclerView recyclerView;

    DownLoadListAdapter adapter;

    private String test = "http://downloads.rongcloud.cn/SealTalk_by_RongCloud_Android_v1_2_0.apk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Keep keep = new Keep.Builder(this).setThreads(2).build();
        Keep.setInstance(keep);
        Keep.getInstance().start();


        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = editText.getText().toString();
                url = test;
                if (TextUtils.isEmpty(url)) {
                    Toast.makeText(MainActivity.this, "url can not be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                Uri uri = Uri.parse(url);
                String fileName = uri.getLastPathSegment();
                KeepTask task = Keep.getInstance().addTask(url, KeepTask.FileType.FILE, null, fileName, MainActivity.this);
                if (task != null) {
                    adapter.addOneTask(task);
                }
            }
        });
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);

        adapter = new DownLoadListAdapter();
        adapter.init(this);
        recyclerView.setAdapter(adapter);
        adapter.setTask(keep.getAllTaskInDb());
        System.out.println("mydebug task size " + keep.getAllTaskInDb().size());
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        editText = (EditText) findViewById(R.id.edit_url);
        btnDownload = (Button) findViewById(R.id.btn_download);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);

    }

    @Override
    public void onDownloadStart(final KeepTask task) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.updateTask(task);
            }
        });
    }

    @Override
    public void onDownloadProgress(final KeepTask task) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.updateTask(task);
            }
        });
    }

    @Override
    public void onDownloadSuccess(final KeepTask task) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.updateTask(task);
            }
        });
    }

    @Override
    public void onDownloadFailed(final KeepTask task) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.updateTask(task);
            }
        });
    }

    @Override
    public void onDownloadDeleted(KeepTask task) {

    }

    @Override
    public void onDownloadPaused(final KeepTask task) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.updateTask(task);
            }
        });
    }

    @Override
    public void onDownloadResumed(KeepTask task) {

    }
}
