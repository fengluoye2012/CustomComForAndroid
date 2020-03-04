package com.test.gradle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.test.lifecycle_annotation.RouteNode;

@RouteNode(path = "/main/DetailActivity", desc = "详情页")
public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
    }
}
