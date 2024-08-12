package com.wuw.ucenter.server.controller;

import com.wuw.common.api.apiResult.ApiResult;
import com.wuw.ucenter.api.model.VO.UserAccountResponseVO;
import com.wuw.ucenter.server.service.UserAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/doubleCache")
@Slf4j
public class DoubleCacheController {

    @Resource
    private UserAccountService userAccountService;

    @GetMapping("/getUserAccountOfCache")
    public ApiResult<UserAccountResponseVO> getUserAccountOfCache(@RequestParam("userCode") String userCode){

        UserAccountResponseVO userAccountByCode = userAccountService.getUserAccountOfCache(userCode);
        return ApiResult.success(userAccountByCode);
    }

}
