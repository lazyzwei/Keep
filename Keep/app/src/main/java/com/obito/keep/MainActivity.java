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

    private String test = "https://m2-3rd-miner.baijincdn.com/file/117f2ccc3ba7b245480b950c0ba1e5dc?sd" +
            "k_id=258&task_id=3667922824336900193&business_id=4097&bkt=p3-0000c9cdbcaac0cf67372f13dff718b0d" +
            "c7e&xcode=11fd2f00a2d126c9daa216761700f42acefa483ffe93e1b89717ec4418c70769&fid=1110174057-250528-618968" +
            "21366581&time=1491733702&sign=FDTAXGERLBHS-DCb740ccc5511e5e8fedcff06b081203-%2BRic%2FK8LXVLLHSamznbpE692vkQ" +
            "%3D&to=z1&size=196202083&sta_dx=196202083&sta_cs=11605&sta_ft=mp4&sta_ct=6&sta_mt=5&fm2=MH,Nanjing02," +
            "Netizen-anywhere,,beijing,cnc&newver=1&newfm=1&secfm=1&flow_ver=3&pkey=0000c9cdbcaac0cf67372f13dff718b0dc7e&" +
            "sl=73007182&expires=8h&rt=pr&r=643020141&mlogid=2290748241750994017&vuk=1110174057&vbdid=3217641226&fin=Jack.mp4&" +
            "fn=Jack.mp4&rtype=1&iv=0&dp-logid=2290748241750994017&dp-callid=0.1.1&hps=1&csl=133&csign=PoGY8rXjo6qizHmRkkfI%2FV" +
            "YpaZM%3D&by=themis";

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
                fileName = "jack.mp4";
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

//        List<String> datas = new ArrayList<>();
//        datas.add("item1");
//        datas.add("item2");
//        datas.add("item3");
//        adapter.setTask(datas);
        adapter.setTask(keep.getAllTaskInDb());
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
    public void onDownloadPaused(KeepTask task) {

    }

    @Override
    public void onDownloadResumed(KeepTask task) {

    }
}
