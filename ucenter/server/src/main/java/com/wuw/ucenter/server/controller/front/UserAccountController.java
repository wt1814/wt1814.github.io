package com.wuw.ucenter.server.controller.front;

import com.wuw.common.api.apiResult.ApiResult;
import com.wuw.ucenter.api.model.VO.UserAccountResponseVO;
import com.wuw.ucenter.server.service.UserAccountService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/ucenter/userAccount")
@Slf4j
public class UserAccountController {

    @Resource
    private UserAccountService userAccountService;


    @GetMapping("/getUserAccountByCode")
    public ApiResult<UserAccountResponseVO> getUserAccountByCode(@RequestParam("userCode") String userCode){

        UserAccountResponseVO userAccountByCode = userAccountService.getUserAccountByCode(userCode);
        return ApiResult.success(userAccountByCode);
    }




}
