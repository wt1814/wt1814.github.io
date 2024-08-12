package com.wt.monitor.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.wt.monitor.check.vo.DataSourceVO;
import com.wt.monitor.check.vo.DruidDataSourceStatValueVO;
import com.wt.monitor.config.AppConfig;
import com.wt.monitor.constant.CommonConstant;
import com.wt.monitor.utils.CalculateUtils;
import com.wt.monitor.utils.HttpClientUtil;
import com.wt.monitor.utils.SystemUtil;
import com.wt.monitor.utils.dingding.SendDingTalkUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: wt1814
 * @Date: 2021/6/30 11:03 上午
 * @Description: 数据库监控
 */
@Slf4j
public class DatabaseService {
    public static String ipAddress = SystemUtil.getIpAddress();
    private static Integer OVER_THRESHOLD_TIMES = 0;

    private static Integer BELOW_THRESHOLD_TIMES = 0;
    public static String COOKIE;

    public static void checkDatabase() {
        if (StringUtils.isEmpty(COOKIE) &&
                !StringUtils.isEmpty(AppConfig.druidUserName) &&
                !StringUtils.isEmpty(AppConfig.druidPassword)) {
            //调用登录接口 获取cookie
            COOKIE = initCookie();
            log.info("COOKIE:{}", COOKIE);
            if (StringUtils.isEmpty(COOKIE)) return;
        }
        String result = HttpClientUtil.sendGet(getDataUrl(), COOKIE);
        if (StringUtils.isEmpty(result) || CommonConstant.NO_PERMISSION.equals(result)) return;

        DataSourceVO dataSourceVO = null;
        try {
            dataSourceVO = JSON.parseObject(result, DataSourceVO.class);
        } catch (JSONException e) {
            log.error("json转换错误：", e);
        }

        if (ObjectUtils.isEmpty(dataSourceVO)) return;

        Map<String, String> databaseMap = null;

        if (CollectionUtils.isEmpty(dataSourceVO.getContent())) return;

        for (DruidDataSourceStatValueVO valueVO : dataSourceVO.getContent()) {
            //连接数超过阈值
            if (valueVO.getPoolingCount() >= valueVO.getMaxActive() * AppConfig.dataBaseLimitRate) {
                addOverTimes();

                if(StringUtils.isEmpty(valueVO.getUrl())) return;

                databaseMap = getDatabaseMap(valueVO.getUrl());

                if(CollectionUtils.isEmpty(databaseMap)) return;
                //活动连接数超过阈值 预警
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("**数据库连接池资源预警** :" + getDatabaseOverTimes() + CommonConstant.LINE_FEED)
                        .append("**服务名称** :" + AppConfig.appName + CommonConstant.LINE_FEED)
                        .append("**服务器** :" + ipAddress + CommonConstant.LINE_FEED)
                        .append("**当前配置阈值** :" + CalculateUtils.getRate(AppConfig.dataBaseLimitRate) + CommonConstant.LINE_FEED)
                        .append("数据库名称 :" + databaseMap.get("databaseName") + CommonConstant.LINE_FEED)
                        .append("数据库地址 :" + databaseMap.get("databaseUrl") + CommonConstant.LINE_FEED)
                        .append("活跃连接数 :" + valueVO.getActiveCount() + CommonConstant.LINE_FEED)
                        .append("池中现有连接数 :" + valueVO.getPoolingCount() + CommonConstant.LINE_FEED)
                        .append("最大连接数 :" + valueVO.getMaxActive() + CommonConstant.LINE_FEED);
                SendDingTalkUtil.sendDingDing(stringBuilder.toString());
                return;
            }
        }
        addBelowTimes();
    }

    /**
     * 获取数据库名称和地址
     *
     * @param url
     * @return
     */
    public static Map<String, String> getDatabaseMap(String url) {
        Map<String, String> map = new HashMap<>(2);
        try {
            String urlStr = subString(url, "jdbc:mysql://", "?");
            if(StringUtils.isEmpty(urlStr)) return null;

            map.put("databaseUrl", urlStr.substring(0, urlStr.indexOf(":")));
            String temp = urlStr.substring(0, urlStr.indexOf("/"));
            map.put("databaseName", urlStr.substring(temp.length() + 1));
        }catch (StringIndexOutOfBoundsException e) {
            log.error("error:", e);
        }
        return map;
    }

    public static String getDatabaseOverTimes() {

        return " 已经连续" + OVER_THRESHOLD_TIMES + "次超过阈值";
    }

    private static void addBelowTimes() {
        BELOW_THRESHOLD_TIMES++;

        if (BELOW_THRESHOLD_TIMES >= AppConfig.dataBaseBelowThresholdLimit) OVER_THRESHOLD_TIMES = 0;
    }

    private static void addOverTimes() {
        BELOW_THRESHOLD_TIMES = 0;
        OVER_THRESHOLD_TIMES++;
    }

    /**
     * 初始化cookie
     *
     * @return
     */
    private static String initCookie() {
        try {
            CloseableHttpResponse response = HttpClientUtil.doJsonPost(getLoginUrl(), null);
            if (!ObjectUtils.isEmpty(response) && null != response.getAllHeaders() && response.getAllHeaders().length > 0) {
                for (Header header : response.getAllHeaders()) {
                    for (HeaderElement element : header.getElements()) {
                        if (CommonConstant.JSESSIONID.equals(element.getName())) {
                            return CommonConstant.JSESSIONID + "=" + element.getValue();
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("error:", e);
        }
        return null;
    }

    private static String getLoginUrl() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(CommonConstant.PROTOCOL).
                append(ipAddress).append(":").append(AppConfig.port).
                append(CommonConstant.DRUID_LOGIN_URI).append("loginUsername=").append(AppConfig.druidUserName)
                .append("&loginPassword=").append(AppConfig.druidPassword);
        return stringBuilder.toString();
    }

    /**
     * 拼接数据获取api
     *
     * @return
     */
    private static String getDataUrl() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(CommonConstant.PROTOCOL).
                append(ipAddress).append(":").append(AppConfig.port).
                append(CommonConstant.DRUID_DATASOURCE_URI);
        return stringBuilder.toString();
    }

    public static String subString(String str, String strStart, String strEnd) {

        //找出指定的2个字符在 该字符串里面的 位置
        int strStartIndex = str.indexOf(strStart);
        int strEndIndex = str.indexOf(strEnd);

        // index 为负数 即表示该字符串中 没有该字符
        if (strStartIndex < 0) {
            log.error("字符串 :---->" + str + "<---- 中不存在 " + strStart + ", 无法截取目标字符串");
            return null;
        }
        if (strEndIndex < 0) {
            log.error("字符串 :---->" + str + "<---- 中不存在 " + strEnd + ", 无法截取目标字符串");
            return null;
        }

        //开始截取
        return str.substring(strStartIndex, strEndIndex).substring(strStart.length());
    }

}
