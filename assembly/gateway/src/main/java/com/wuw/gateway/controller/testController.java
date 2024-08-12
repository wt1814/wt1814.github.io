package com.wuw.gateway.controller;

import com.wuw.common.api.apiResult.ApiResult;
import com.wuw.gateway.feign.UserAccountFeign;
import com.wuw.ucenter.api.model.VO.UserAccountResponseVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping(value = "/user")
public class testController {

    @Resource
    private UserAccountFeign userAccountFeign;

    @GetMapping("/getUserAccountByCode")
    public ApiResult<UserAccountResponseVO> getUserAccountByCode(@RequestParam("userCode") String userCode) throws Exception{
        //return null;
        ThreadPoolExecutor exe = new ThreadPoolExecutor(
                5,
                8,
                1,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(200),
                new ThreadPoolExecutor.CallerRunsPolicy());

        Future<ApiResult<UserAccountResponseVO>> submit = exe.submit(() -> userAccountFeign.getUserAccountByCode(userCode));

        ApiResult<UserAccountResponseVO> userAccountVOApiResult = submit.get(8, TimeUnit.SECONDS);

        System.out.println("11");
        return userAccountVOApiResult;
    }

}
