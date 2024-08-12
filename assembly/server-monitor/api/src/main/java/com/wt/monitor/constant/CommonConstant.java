package com.wt.monitor.constant;

/**
 * @Author: wt1814
 * @Date: 2021/5/31 5:56 下午
 * @Description:
 */
public class CommonConstant {

    public static final String LINE_FEED = "  \n  ";

    /**
     * 默认各项资源指标临界值为 70%
     */
    public static final double RESOURCE_THRESHOLD = 0.70;

    /**
     * 阈值低于该次数 重新计数
     */
    public static final Integer DEFAULT_BELOW_THRESHOLD_TIMES = 2;

    public static final String dingNoticeUrl = "XXXXXXXXXX";

    public static final String SERVER_PORT = "8080";
    public static final String DRUID_LOGIN_URI = "/druid/submitLogin?";
    public static final String DRUID_DATASOURCE_URI = "/druid/datasource.json";
    public static final String PROTOCOL = "http://";

    public static final String JSESSIONID = "JSESSIONID";

    public static final Boolean MONITOR_ENABLE = true;

    public static final String NO_PERMISSION = "no permission";

    public static final Integer CONNECT_TIMEOUT = 2000;
    public static final Integer CONNECTION_REQUEST_TIMEOUT = 3000;
    public static final Integer SOCKET_TIMEOUT = 5000;
    public static final Integer POOL_MAX_NUM = 5;
    public static final Integer DEFAULT_MAX_PER_ROUTE = 3;

}
