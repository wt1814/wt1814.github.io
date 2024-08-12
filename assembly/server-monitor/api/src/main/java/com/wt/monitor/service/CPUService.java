package com.wt.monitor.service;

import com.wt.monitor.check.CPUInfo;
import com.wt.monitor.config.AppConfig;
import com.wt.monitor.constant.CommonConstant;
import com.wt.monitor.utils.CalculateUtils;
import com.wt.monitor.utils.SystemUtil;
import com.wt.monitor.utils.dingding.SendDingTalkUtil;
import lombok.extern.slf4j.Slf4j;
import org.hyperic.sigar.CpuPerc;

/**
 * @Author: wt1814
 * @Date: 2021/5/31 5:56 下午
 * @Description:
 */
@Slf4j
public class CPUService {
    public static String ipAddress = SystemUtil.getIpAddress();

    /**
     * 连续超过阈值次数
     */
    private static Integer OVER_THRESHOLD_TIMES = 0;

    /**
     * 连续低于阈值次数
     */
    private static Integer BELOW_THRESHOLD_TIMES = 0;

    public static void checkCPU() {
        CpuPerc[] cpus = null;
        cpus = CPUInfo.cpu();
        StringBuilder stringBuilder = new StringBuilder();
        for (CpuPerc cpuPerc : cpus) {
            if (cpuPerc.getCombined() < AppConfig.cpuLimitRate) continue;

            addOverTimes();
            stringBuilder.append("**CPU资源预警** :" + getCPUOverTimes() + CommonConstant.LINE_FEED)
                    .append("**服务名称** :" + AppConfig.appName + CommonConstant.LINE_FEED)
                    .append("**服务器** :" + ipAddress + CommonConstant.LINE_FEED)
                    .append("**当前配置阈值** :" + CalculateUtils.getRate(AppConfig.cpuLimitRate) + CommonConstant.LINE_FEED)
                    .append("CPU总的使用率:" + CpuPerc.format(cpuPerc.getCombined()) + CommonConstant.LINE_FEED)
                    .append("CPU当前空闲率: " + CpuPerc.format(cpuPerc.getIdle()) + CommonConstant.LINE_FEED)
                    .append("CPU用户使用率: " + CpuPerc.format(cpuPerc.getUser()) + CommonConstant.LINE_FEED)
                    .append("CPU系统使用率: " + CpuPerc.format(cpuPerc.getSys()));

            String content = stringBuilder.toString();
            SendDingTalkUtil.sendDingDing(content);

            return;
        }
        addBelowTimes();
    }

    private static void addBelowTimes() {
        //如果cpu资源没有预警 连续低于阈值次数累加
        BELOW_THRESHOLD_TIMES++;

        //连续低于阈值次数超过设定值 则把 连续超过阈值次数清零
        if (BELOW_THRESHOLD_TIMES >= AppConfig.cpuBelowThresholdLimit) OVER_THRESHOLD_TIMES = 0;
    }

    private static void addOverTimes() {
        BELOW_THRESHOLD_TIMES = 0;
        OVER_THRESHOLD_TIMES++;
    }

    public static String getCPUOverTimes() {
        return " 已经连续" + OVER_THRESHOLD_TIMES + "次超过阈值";
    }
}
