package com.wuw.ucenter.server.controller;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(value = "/redis")
@Slf4j
public class RedisController {

    @Autowired
    private RedissonClient redissonClient;

    @GetMapping(value = "/redisson")
    public String redissonTest(@RequestParam("key") String key) {
        RLock lock = redissonClient.getLock(key);
        try {
            while (lock.isLocked()) {
                Thread.sleep(10);
            }
            lock.tryLock(100, TimeUnit.SECONDS);
            System.out.println("111");
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return "已解锁";
    }

}
