package com.wt.monitor.check.vo;

import lombok.Data;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Swap;

/**
 * @Author: wt1814
 * @Date: 2021/6/3 5:59 下午
 * @Description:
 */
@Data
public class MemoryVO {
    /**
     * 内存
     */
    private Mem mem;

    /**
     * 交换区
     */
    private Swap swap;
}
