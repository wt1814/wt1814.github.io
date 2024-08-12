package com.wuw.common.server.util.basic;


import java.util.HashMap;
import java.util.Map;

/**
 * 声明静态Map常量
 */
public class StaticMap {

    // https://blog.csdn.net/testcs_dn/article/details/90516890

    public final static Map map = new HashMap();
    static {
        map.put("key1", "value1");
        map.put("key2", "value2");
    }


}
