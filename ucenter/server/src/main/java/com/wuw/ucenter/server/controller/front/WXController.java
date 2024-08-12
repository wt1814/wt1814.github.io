package com.wuw.ucenter.server.controller.front;

import com.wuw.common.api.apiResult.ApiResult;
import com.wuw.ucenter.server.service.WXService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
public class WXController {


    @Resource
    private WXService wxService;


    /**
     * 微信扫码登录第一步：获取二维码参数
     * @return
     */
    @RequestMapping
    public ApiResult<Map<String,Object>> wxOfScanning(){

        Map<String, Object> stringObjectMap = wxService.wxOfScanning();
        return ApiResult.success(stringObjectMap);

    }

    /**
     * 微信扫码登录第二步：微信回调
     * @param code
     * @param state
     * @return
     */
    @RequestMapping
    public String wxOfScanningCallback(@RequestParam String code, @RequestParam String state){
        String s = wxService.wxOfScanningCallback(code, state);
        return s;
    }


}
