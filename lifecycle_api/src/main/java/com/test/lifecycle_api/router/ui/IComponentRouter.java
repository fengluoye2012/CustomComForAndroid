package com.test.lifecycle_api.router.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

public interface IComponentRouter {

    /**
     * 打开一个链接
     *
     * @param url    目标url可以是http 或者 自定义scheme
     * @param bundle 打开目录activity时要传如的参数。建议只传基本类型
     * @return 是否正常打开
     */
    boolean openUri(Context context, String url, Bundle bundle);

    boolean openUri(Context context, Uri uri, Bundle bundle);

    boolean openUri(Context context, String url, Bundle bundle, Integer requestCode);

    boolean openUri(Context context, Uri uri, Bundle bundle, Integer requestCode);

    boolean verifyUri(Uri uri);
}
