package com.test.lifecycle_api.service;

public interface AutoWiredService {

    void autoWired(Object instance);

    class Factory {
        private static Factory instance;

        public static Factory getInstance() {
            if (instance == null) {
                synchronized (JsonService.Factory.class) {
                    if (instance == null) {
                        instance = new Factory();
                    }
                }
            }
            return instance;
        }

        public AutoWiredService create() {
            return new AutoWiredServiceImpl();
        }
    }
}
