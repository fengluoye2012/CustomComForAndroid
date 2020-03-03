package com.test.lifecycle_annotation.utils;

public class RouterUtils {

    /**
     * 生成路由实现类的输出包名
     */
    private static final String ROUTER_IMPL_OUTPUT_PKG = "com.test.gen.router";

    public static final String DOT = ".";

    private static final String UI_ROUTER = "UiRouter";

    private static final String ROUTER_TABLE = "RouterTable";

    public static String firstCharUpperCase(String str) {
        char[] ch = str.toCharArray();
        if (ch[0] >= 'a' && ch[0] <= 'z') {
            ch[0] = (char) (ch[0] - 32);
        }
        return new String(ch);
    }

    /**
     * 生成全类名
     *
     * @param host
     * @return
     */
    public static String genHostUIRouterClass(String host) {
        return ROUTER_IMPL_OUTPUT_PKG + DOT + firstCharUpperCase(host) + UI_ROUTER;
    }


    /**
     * 在项目根目录下生成 UIRouterTable 文件，在文件夹中保存txt 文件
     *
     * @param host
     * @return
     */
    public static String genRouterTable(String host) {
        return "./UIRouterTable/" + firstCharUpperCase(host) + ROUTER_TABLE + ".txt";
    }
}
