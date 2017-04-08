package com.obito.keep;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.obito.keep.adapter.DownLoadListAdapter;
import com.obito.keeplib.Keep;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditText editText;
    Button btnDownload;
    RecyclerView recyclerView;

    DownLoadListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Keep keep = new Keep.Builder(this).setThreads(2).build();
        Keep.setInstance(keep);
        Keep.getInstance().start();


        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("mydebug MainActivity onClick ");
            }
        });
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);

        adapter = new DownLoadListAdapter();
        adapter.init(this);
        recyclerView.setAdapter(adapter);

        List<String> datas = new ArrayList<>();
        datas.add("item1");
        datas.add("item2");
        datas.add("item3");
        adapter.setTask(datas);
//        adapter.setTask(keep.getAllTaskInDb());
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        editText = (EditText) findViewById(R.id.edit_url);
        btnDownload = (Button) findViewById(R.id.btn_download);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);

    }

}
