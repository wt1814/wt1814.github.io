package com.wt.monitor.config;

import com.wt.monitor.constant.CommonConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

/**
 * @Author: wt1814
 * @Date: 2021/5/31 5:56 下午
 * @Description: 获取app名称
 */
@Slf4j
@Component
public class AppConfig {
    @Autowired
    Environment environment;

    /**
     * 系统应用名称
     */
    public static String appName;

    /**
     * 端口号
     */
    public static String port;

    /**
     * 钉钉通知地址
     */
    public static String dingdingUrl;

    /**
     * 资源阈值
     */
    public static double cpuLimitRate;
    public static double memLimitRate;
    public static double dubboThreadLimitRate;
    public static double dataBaseLimitRate;

    /**
     * 使用率连续低于阈值次数
     */
    public static Integer cpuBelowThresholdLimit;
    public static Integer dubboBelowThresholdLimit;
    public static Integer memBelowThresholdLimit;
    public static Integer dataBaseBelowThresholdLimit;

    /**
     * 监控开关
     */
    public static boolean cpuEnable;
    public static boolean dubboEnable;
    public static boolean memEnable;
    public static boolean dataBaseEnable;

    /**
     * druid用户名 密码
     */
    public static String druidUserName;
    public static String druidPassword;

    /**
     * http连接
     */
    public static Integer poolMaxTotal;
    public static Integer defaultMaxPerRoute;
    public static Integer connectTimeout;
    public static Integer connectionRequestTimeout;
    public static Integer socketTimeout;

    @PostConstruct
    public void initData() {
        appName = environment.getProperty("spring.application.name");

        String portValue = environment.getProperty("server.port");
        port = !StringUtils.isEmpty(portValue) ? portValue : CommonConstant.SERVER_PORT;

        String dingUrl = environment.getProperty("dingdingNoticeUrl");
        dingdingUrl = !StringUtils.isEmpty(dingUrl) ? dingUrl : CommonConstant.dingNoticeUrl;

        //资源阈值
        String cpuLimitRateStr = environment.getProperty("cpuLimitRate");
        cpuLimitRate = !StringUtils.isEmpty(cpuLimitRateStr) ? Double.valueOf(cpuLimitRateStr) : CommonConstant.RESOURCE_THRESHOLD;

        String memLimitRateStr = environment.getProperty("memLimitRate");
        memLimitRate = !StringUtils.isEmpty(memLimitRateStr) ? Double.valueOf(memLimitRateStr) : CommonConstant.RESOURCE_THRESHOLD;

        String dubboThreadLimitRateStr = environment.getProperty("dubboThreadLimitRate");
        dubboThreadLimitRate = !StringUtils.isEmpty(dubboThreadLimitRateStr)
                ? Double.valueOf(dubboThreadLimitRateStr) : CommonConstant.RESOURCE_THRESHOLD;

        String dataBaseLimitRateStr = environment.getProperty("dataBaseLimitRate");
        dataBaseLimitRate = !StringUtils.isEmpty(dataBaseLimitRateStr)
                ? Double.valueOf(dataBaseLimitRateStr) : CommonConstant.RESOURCE_THRESHOLD;

        String cpuBelowThresholdLimitStr = environment.getProperty("cpuBelowThresholdLimit");
        cpuBelowThresholdLimit = !StringUtils.isEmpty(cpuBelowThresholdLimitStr)
                ? Integer.valueOf(cpuBelowThresholdLimitStr) : CommonConstant.DEFAULT_BELOW_THRESHOLD_TIMES;

        String dubboBelowThresholdLimitStr = environment.getProperty("dubboBelowThresholdLimit");
        dubboBelowThresholdLimit = !StringUtils.isEmpty(dubboBelowThresholdLimitStr)
                ? Integer.valueOf(dubboBelowThresholdLimitStr) : CommonConstant.DEFAULT_BELOW_THRESHOLD_TIMES;

        String memBelowThresholdLimitStr = environment.getProperty("memBelowThresholdLimit");
        memBelowThresholdLimit = !StringUtils.isEmpty(memBelowThresholdLimitStr)
                ? Integer.valueOf(memBelowThresholdLimitStr) : CommonConstant.DEFAULT_BELOW_THRESHOLD_TIMES;

        String dataBaseBelowThresholdLimitStr = environment.getProperty("dataBaseBelowThresholdLimit");
        dataBaseBelowThresholdLimit = !StringUtils.isEmpty(dataBaseBelowThresholdLimitStr)
                ? Integer.valueOf(dataBaseBelowThresholdLimitStr) : CommonConstant.DEFAULT_BELOW_THRESHOLD_TIMES;

        //druid
        druidUserName = environment.getProperty("spring.datasource.druid.stat-view-servlet.login-username");
        druidPassword = environment.getProperty("spring.datasource.druid.stat-view-servlet.login-password");

        //开关
        String cpuEnableStr = environment.getProperty("cpuEnable");
        cpuEnable = !StringUtils.isEmpty(cpuEnableStr)
                ? Boolean.valueOf(cpuEnableStr) : CommonConstant.MONITOR_ENABLE;

        String memEnableStr = environment.getProperty("memEnable");
        memEnable = !StringUtils.isEmpty(memEnableStr)
                ? Boolean.valueOf(memEnableStr) : CommonConstant.MONITOR_ENABLE;

        String dubboEnableStr = environment.getProperty("dubboEnable");
        dubboEnable = !StringUtils.isEmpty(dubboEnableStr)
                ? Boolean.valueOf(dubboEnableStr) : CommonConstant.MONITOR_ENABLE;

        String databaseEnableStr = environment.getProperty("dataBaseEnable");
        dataBaseEnable = !StringUtils.isEmpty(databaseEnableStr)
                ? Boolean.valueOf(databaseEnableStr) : CommonConstant.MONITOR_ENABLE;

        //http
        String poolMaxTotalStr = environment.getProperty("poolMaxTotal");
        poolMaxTotal = !StringUtils.isEmpty(poolMaxTotalStr) ? Integer.valueOf(poolMaxTotalStr) : CommonConstant.POOL_MAX_NUM;

        String defaultMaxPerRouteStr = environment.getProperty("defaultMaxPerRoute");
        defaultMaxPerRoute = !StringUtils.isEmpty(defaultMaxPerRouteStr) ? Integer.valueOf(defaultMaxPerRouteStr) : CommonConstant.DEFAULT_MAX_PER_ROUTE;

        String connectTimeoutStr = environment.getProperty("connectTimeoutStr");
        connectTimeout = !StringUtils.isEmpty(connectTimeoutStr) ? Integer.valueOf(connectTimeoutStr) : CommonConstant.CONNECT_TIMEOUT;

        String connectionRequestTimeoutStr = environment.getProperty("connectionRequestTimeout");
        connectionRequestTimeout = !StringUtils.isEmpty(connectionRequestTimeoutStr) ? Integer.valueOf(connectionRequestTimeoutStr) : CommonConstant.CONNECTION_REQUEST_TIMEOUT;

        String socketTimeoutStr = environment.getProperty("socketTimeout");
        socketTimeout = !StringUtils.isEmpty(socketTimeoutStr) ? Integer.valueOf(socketTimeoutStr) : CommonConstant.SOCKET_TIMEOUT;

    }
}
