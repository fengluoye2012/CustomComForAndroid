package com.test.lifecycle_api.router.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.test.lifecycle_api.utils.UriUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通过路由跳转打开Activity 的基类
 */
public abstract class BaseComRouter implements IComponentRouter {

    /**
     * 路径->Class文件一一对应
     */
    protected Map<String, Class> routerMapper = new HashMap<>();

    /**
     * Class->参数一一对应
     */
    protected Map<Class, Map<String, Integer>> paramsMapper = new HashMap<>();

    protected boolean hasInitMap = false;

    protected abstract String getHost();


    protected void initMap() {
        hasInitMap = true;
    }

    @Override
    public boolean openUri(Context context, String url, Bundle bundle) {
        if (TextUtils.isEmpty(url) || context == null) {
            return false;
        }
        return openUri(context, Uri.parse(url), bundle, 0);
    }

    @Override
    public boolean openUri(Context context, Uri uri, Bundle bundle) {
        return openUri(context, uri, bundle, 0);
    }

    @Override
    public boolean openUri(Context context, String url, Bundle bundle, Integer requestCode) {
        if (TextUtils.isEmpty(url) || context == null) {
            return false;
        }
        return openUri(context, Uri.parse(url), bundle, requestCode);
    }

    @Override
    public boolean openUri(Context context, Uri uri, Bundle bundle, Integer requestCode) {
        if (!hasInitMap) {
            initMap();
        }

        if (uri == null || context == null) {
            return false;
        }

        String scheme = uri.getScheme();
        String host = uri.getHost();
        //判断在build.gradle中配置的host 和 UI 跳转的host 是否相等。
        if (!getHost().equals(host)) {
            return false;
        }

        List<String> pathSegments = uri.getPathSegments();
        //跳转到指定Activity的对应path
        String path = "/" + TextUtils.join("/", pathSegments);

        //判断是否存在对应的path，不存在表示无法找到对应的Activity;
        if (routerMapper.containsKey(path)) {
            Class target = routerMapper.get(path);
            if (bundle == null) {
                bundle = new Bundle();
            }

            //uri 后的参数
            HashMap<String, String> params = UriUtils.parseParams(uri);

            Map<String, Integer> paramType = paramsMapper.get(target);
            UriUtils.setBundleValue(bundle, params, paramType);//todo

            //开始正常跳转
            Intent intent = new Intent(context, target);
            intent.putExtras(bundle);
            if (requestCode > 0 && context instanceof Activity) {
                ((Activity) context).startActivityForResult(intent, requestCode);
                return true;
            }
            context.startActivity(intent);
            return true;
        }
        return false;
    }

    @Override
    public boolean verifyUri(Uri uri) {
        String host = uri.getHost();
        if (!getHost().equals(host)) {
            return false;
        }
        if (!hasInitMap) {
            initMap();
        }

        List<String> pathSegments = uri.getPathSegments();
        String path = "/" + TextUtils.join("/", pathSegments);
        return routerMapper.containsKey(path);
    }
}
