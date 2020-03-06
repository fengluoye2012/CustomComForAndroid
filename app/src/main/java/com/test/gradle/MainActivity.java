package com.test.gradle;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.test.baselibrary.base.BaseActivity;
import com.test.componentservice.module.test.TestUIRouterHelper;
import com.test.lifecycle_annotation.RouteNode;

@RouteNode(path = "/main/MainActivity", desc = "主界面")
public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = findViewById(R.id.textView);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TestUIRouterHelper.startTestActivity(getBaseContext(),"main");
            }
        });
    }
}
