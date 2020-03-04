package com.test.test;

import com.test.componentservice.module.test.TestService;

public class TestServiceImpl implements TestService {
    @Override
    public String getTitle() {
        return "test";
    }
}
