package com.wt.monitor.service;

import com.wt.monitor.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Author: wt1814
 * @Date: 2021/6/2 4:01 下午
 * @Description:
 */
@Slf4j
@Service
public class ResourceCheckService {
    public void check() {
        if (AppConfig.cpuEnable) {
            CPUService.checkCPU();
        }
        if (AppConfig.memEnable) {
            MemoryService.checkUsageRate();
        }
        if (AppConfig.dubboEnable) {
            DubboService.checkDubbo();
        }
        if (AppConfig.dataBaseEnable) {
            DatabaseService.checkDatabase();
        }
    }
}
