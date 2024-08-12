package com.wt.monitor.scheduled;


import com.wt.monitor.service.ResourceCheckService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author: wt1814
 * @Date: 2021/5/31 5:56 下午
 * @Description:
 */
@Slf4j
@Component
@EnableScheduling
public class CheckTask {

    @Resource
    ResourceCheckService resourceCheckService;

    /**
     * 默认1min 执行一次
     */
    @Scheduled(cron = "${cronValue:0 */1 * * * ?}")
    public void check() {
        log.info("==开始进行资源校验==");
        resourceCheckService.check();
        log.info("==结束校验==");
    }
}
