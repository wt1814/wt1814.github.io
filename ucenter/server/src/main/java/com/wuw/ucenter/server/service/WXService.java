package com.wuw.ucenter.server.service;

import java.util.Map;

public interface WXService {

    /**
     * 微信扫码登录第一步：获取二维码参数
     * @return
     */
    Map<String,Object> wxOfScanning();


    /**
     * 微信扫码登录第二步：微信回调
     * @param code
     * @param state
     * @return
     */
    String wxOfScanningCallback(String code,String state);



}
