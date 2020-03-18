package com.test.baselibrary.base;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.test.lifecycle_api.service.AutoWiredService;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AutoWiredService.Factory.getInstance().create().autoWired(this);
    }
}
