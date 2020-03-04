package com.test.componentservice.module.test;

import com.test.lifecycle_api.router.Router;

public class TestServiceProxy implements TestService {

    private TestService service;

    private static TestServiceProxy instance;

    private TestServiceProxy() {
    }
    
    public static TestServiceProxy getInstance() {
        if (instance == null) {
            synchronized (TestServiceProxy.class) {
                if (instance == null) {
                    instance = new TestServiceProxy();
                }
            }
        }
        return instance;
    }

    @Override
    public String getTitle() {
        if (!createService()) {
            return "";
        }
        return service.getTitle();
    }

    private boolean createService() {
        if (service == null) {
            service = (TestService) Router.getInstance().getService(TestService.class.getSimpleName());
        }
        return service != null;
    }
}
