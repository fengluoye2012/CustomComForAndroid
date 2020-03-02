package com.test.test;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.test.lifecycle_annotation.RouteNode;

@RouteNode(path = "/test/TestActivity", desc = "测试类")
public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
    }
}
