package com.wuw.gateway.feign;

import com.wuw.common.api.apiResult.ApiResult;
import com.wuw.ucenter.api.model.VO.UserAccountResponseVO;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value="ucenter")
public interface UserAccountFeign {

    @GetMapping("/userAccount/getUserAccountByCode")
    ApiResult<UserAccountResponseVO> getUserAccountByCode(@RequestParam("userCode") String userCode);

}
