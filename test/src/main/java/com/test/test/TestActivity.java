package com.test.test;

import android.os.Bundle;
import android.widget.TextView;

import com.test.baselibrary.base.BaseActivity;
import com.test.lifecycle_annotation.AutoWired;
import com.test.lifecycle_annotation.RouteNode;

@RouteNode(path = "/test/TestActivity", desc = "测试类")
public class TestActivity extends BaseActivity {

    @AutoWired()
    String from;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        TextView textView = findViewById(R.id.textView);
        textView.setText("从 " + from + "打开当前页面");
    }
}
