package com.wt.monitor.check;

import com.wt.monitor.check.vo.JvmMemoryVO;
import com.wt.monitor.check.vo.MemoryVO;
import lombok.extern.slf4j.Slf4j;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.Swap;

import java.math.BigDecimal;


/**
 * @Author: wt1814
 * @Date: 2021/5/31 5:56 下午
 * @Description:
 */
@Slf4j
public class MemoryInfo {

    /**
     * @return
     */
    public static double getUsageRate() {
        Sigar sigar = new Sigar();
        Mem mem = null;
        try {
            mem = sigar.getMem();
        } catch (SigarException e) {
            log.error("error:", e);
        }

        return new BigDecimal((float) mem.getUsed() / mem.getTotal()).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 内存详情
     */
    public static MemoryVO memory() {
        MemoryVO memoryVO = new MemoryVO();
        Sigar sigar = new Sigar();
        Mem mem = null;
        try {
            mem = sigar.getMem();
        } catch (SigarException e) {
            log.error("error:", e);
        }

        Swap swap = null;
        try {
            swap = sigar.getSwap();
        } catch (SigarException e) {
            log.error("error:", e);
        }

        memoryVO.setMem(mem);
        memoryVO.setSwap(swap);
        return memoryVO;
    }

    /**
     * jvm内存详情
     */
    public static JvmMemoryVO jvmMemory() {
        JvmMemoryVO jvmMemoryVO = new JvmMemoryVO();
        double totalMemory = Runtime.getRuntime().totalMemory() / 1024L / 1024L;
        double freeMemory = Runtime.getRuntime().freeMemory() / 1024L / 1024L;
        double maxMemory = Runtime.getRuntime().maxMemory() / 1024L / 1024L;
        jvmMemoryVO.setTotalMemory(totalMemory);
        jvmMemoryVO.setFreeMemory(freeMemory);
        jvmMemoryVO.setUsedMemory(totalMemory - freeMemory);
        jvmMemoryVO.setMaxMemory(maxMemory);
        jvmMemoryVO.setMemoryUsageRate(jvmMemoryVO.getUsedMemory()/ jvmMemoryVO.getTotalMemory());
        return jvmMemoryVO;
    }
}
