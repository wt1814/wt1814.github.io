package com.wt.monitor.service;

import com.alibaba.fastjson.JSON;
import com.wt.monitor.check.vo.DubboStatusVO;
import com.wt.monitor.config.AppConfig;
import com.wt.monitor.constant.CommonConstant;
import com.wt.monitor.utils.CalculateUtils;
import com.wt.monitor.utils.SystemUtil;
import com.wt.monitor.utils.dingding.SendDingTalkUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: wt1814
 * @Date: 2021/6/2 3:15 下午
 * @Description:
 */
@Slf4j
public class DubboService {
    public static String ipAddress = SystemUtil.getIpAddress();
    private static Integer OVER_THRESHOLD_TIMES = 0;

    private static Integer BELOW_THRESHOLD_TIMES = 0;

    public static void checkDubbo() {
        //dubbo线程池数量监控
        Class<?> clazz = null;
        try {
            clazz = Class.forName("com.alibaba.dubbo.rpc.protocol.dubbo.status.ThreadPoolStatusChecker");
        } catch (ClassNotFoundException e) {
            log.error("类不存在:", e);
        }
        Method check = null;
        try {
            check = clazz.getMethod("check");
        } catch (NoSuchMethodException e) {
            log.error("方法不存在:", e);
        }
        Object result = null;
        try {
            result = check.invoke(clazz.newInstance());
        } catch (IllegalAccessException e) {
            log.error("error:", e);
        } catch (InvocationTargetException e) {
            log.error("error:", e);
        } catch (InstantiationException e) {
            log.error("error:", e);
        }
        Map<String, String> info = getInfo(JSON.toJSONString(result));
        int active = Integer.valueOf(info.get("active"));
        int max = Integer.valueOf(info.get("max"));

        if (active < max * AppConfig.dubboThreadLimitRate) {
            addBelowTimes();
            return;
        }

        addOverTimes();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("**Dubbo线程池资源预警** :" + getDubboOverTimes() + CommonConstant.LINE_FEED)
                .append("**服务名称** :" + AppConfig.appName + CommonConstant.LINE_FEED)
                .append("**服务器** :" + ipAddress + CommonConstant.LINE_FEED)
                .append("**当前配置阈值** :" + CalculateUtils.getRate(AppConfig.dubboThreadLimitRate) + CommonConstant.LINE_FEED)
                .append("线程池状态 :" + info.get("Pool status") + CommonConstant.LINE_FEED)
                .append("最大数量 :" + info.get("max") + CommonConstant.LINE_FEED)
                .append("活跃数量 :" + info.get("active") + CommonConstant.LINE_FEED)
                .append("核心数量 :" + info.get("core") + CommonConstant.LINE_FEED)
                .append("峰值 :" + info.get("largest") + CommonConstant.LINE_FEED)
                .append("队列 :" + info.get("queues"));
        SendDingTalkUtil.sendDingDing(stringBuilder.toString());

    }

    private static void addOverTimes() {
        BELOW_THRESHOLD_TIMES = 0;
        OVER_THRESHOLD_TIMES++;
    }

    private static void addBelowTimes() {
        BELOW_THRESHOLD_TIMES++;

        if (BELOW_THRESHOLD_TIMES >= AppConfig.dubboBelowThresholdLimit) OVER_THRESHOLD_TIMES = 0;
    }

    public static String getDubboOverTimes() {
        return " 已经连续" + OVER_THRESHOLD_TIMES + "次超过阈值";
    }

    /**
     * 构建dubbo线程信息
     *
     * @param result
     * @return
     */
    public static Map<String, String> getInfo(String result) {
        DubboStatusVO dubboStatusVO = JSON.parseObject(result, DubboStatusVO.class);
        String[] messages = dubboStatusVO.getMessage().split(",");
        String[] values = null;
        Map<String, String> map = new HashMap<>();
        for (String s : messages) {
            values = s.split(":");
            map.put(values[0].trim(), values[1].trim());
        }
        return map;
    }
}
