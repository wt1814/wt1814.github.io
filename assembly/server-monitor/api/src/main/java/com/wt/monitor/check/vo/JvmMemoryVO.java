package com.wt.monitor.check.vo;

import lombok.Data;

/**
 * @Author: wt1814
 * @Date: 2021/6/4 9:38 上午
 * @Description:
 */
@Data
public class JvmMemoryVO {
    /**
     * Java 虚拟机中的总内存量 单位MB
     */
    private Double totalMemory;

    /**
     * Java 虚拟机中的可用内存量 单位MB
     */
    private Double freeMemory;

    /**
     * Java虚拟机将尝试使用的最大内存量 单位MB
     */
    private Double maxMemory;

    /**
     * Java 虚拟机中的占用内存量 单位MB
     */
    private Double usedMemory;

    /**
     * 使用率
     */
    private Double memoryUsageRate;
}
