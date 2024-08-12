package com.wuw.ucenter.server.controller;

import com.wuw.common.api.apiResult.ApiResult;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class xxlJob {

    @XxlJob("UpdateUserHandler")
    public ApiResult<String> test(String param) throws Exception {
        log.info("Hlelo,world");
        return ApiResult.success("SUCCESS");
    }

}
