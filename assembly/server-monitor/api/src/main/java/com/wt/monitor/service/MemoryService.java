package com.wt.monitor.service;

import com.wt.monitor.check.MemoryInfo;
import com.wt.monitor.check.vo.JvmMemoryVO;
import com.wt.monitor.config.AppConfig;
import com.wt.monitor.constant.CommonConstant;
import com.wt.monitor.utils.CalculateUtils;
import com.wt.monitor.utils.SystemUtil;
import com.wt.monitor.utils.dingding.SendDingTalkUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: wt1814
 * @Date: 2021/5/31 5:56 下午
 * @Description:
 */
@Slf4j
public class MemoryService {
    public static String ipAddress = SystemUtil.getIpAddress();

    private static Integer OVER_THRESHOLD_TIMES = 0;

    private static Integer BELOW_THRESHOLD_TIMES = 0;

    public static void checkUsageRate() {

        JvmMemoryVO jvmMemoryVO = MemoryInfo.jvmMemory();
        double usageRate = jvmMemoryVO.getMemoryUsageRate();
        /**
         * 未达到预警范围
         */
        if (usageRate < AppConfig.memLimitRate) {

            addBelowTimes();
            return;
        }

        addOverTimes();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("**JVM内存资源预警** :" + getMemOverTimes() + CommonConstant.LINE_FEED)
                .append("**服务名称** :" + AppConfig.appName + CommonConstant.LINE_FEED)
                .append("**服务器** :" + ipAddress + CommonConstant.LINE_FEED)
                .append("**当前配置阈值** :" + CalculateUtils.getRate(AppConfig.memLimitRate) + CommonConstant.LINE_FEED)
                .append("JVM内存使用率 :" + CalculateUtils.getRate(usageRate) + CommonConstant.LINE_FEED)
                .append("JVM内存总量 :" + jvmMemoryVO.getTotalMemory() + "M" + CommonConstant.LINE_FEED)
                .append("当前JVM内存使用量 :" + jvmMemoryVO.getUsedMemory() + "M used" + CommonConstant.LINE_FEED)
                .append("当前JVM内存剩余量 :" + jvmMemoryVO.getFreeMemory() + "M free");

        SendDingTalkUtil.sendDingDing(stringBuilder.toString());
    }

    private static void addBelowTimes() {
        BELOW_THRESHOLD_TIMES++;

        if (BELOW_THRESHOLD_TIMES >= AppConfig.memBelowThresholdLimit) OVER_THRESHOLD_TIMES = 0;
    }

    private static void addOverTimes() {
        BELOW_THRESHOLD_TIMES = 0;
        OVER_THRESHOLD_TIMES++;
    }

    public static String getMemOverTimes() {
        return " 已经连续" + OVER_THRESHOLD_TIMES + "次超过阈值";
    }
}
