package com.test.gradle;

import android.os.Bundle;

import com.test.baselibrary.base.BaseActivity;
import com.test.lifecycle_annotation.RouteNode;

@RouteNode(path = "/main/DetailActivity", desc = "详情页")
public class DetailActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
    }
}
