package com.example.zane.bitmapdownload;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    private RecycleViewAdapter mAdapter;
    private RecyclerView mRecycleview;
    private LinearLayoutManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecycleview = (RecyclerView) findViewById(R.id.recycleview);
        mAdapter = new RecycleViewAdapter(this);
        manager = new LinearLayoutManager(this);
        mRecycleview.setLayoutManager(manager);
        mRecycleview.setAdapter(mAdapter);

    }
}
