package com.test.lifecycle_api.service;

import com.blankj.utilcode.util.GsonUtils;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class JsonServiceImpl implements JsonService {

    @Override
    public <T> T parseObject(String text, Class<T> clazz) {
        return GsonUtils.fromJson(text, clazz);
    }

    @Override
    public <T> List<T> parseArray(String text, Class<T> clazz) {
        return GsonUtils.getGson().fromJson(text, new TypeToken<List<T>>() {
        }.getType());
    }

    @Override
    public String toJsonString(Object instance) {
        return GsonUtils.toJson(instance);
    }
}
