package com.test.gradle;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.test.lifecycle_annotation.RouteNode;

@RouteNode(path = "/main/MainActivity", desc = "主界面")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
