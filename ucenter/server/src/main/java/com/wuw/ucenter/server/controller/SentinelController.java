package com.wuw.ucenter.server.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/sentinel")
@Slf4j
public class SentinelController {

    @SentinelResource(value = "Sentinel_Cloud",blockHandler = "exceptionHandler")
    @GetMapping("/sentinelCloud")
    public String sentinelCloud(){
        //使用限流规则
        return "Sentinel_Cloud,成功调用";
    }


    /**
     * 定义降级 / 限流 的处理函数
     * @param exception
     * @return
     */
    public String exceptionHandler(BlockException exception) {
        exception.printStackTrace();
        return "Sentinel_Cloud,访问限流";
    }


}
