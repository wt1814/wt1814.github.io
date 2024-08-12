package com.wuw.ucenter.server.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.wuw.ucenter.api.model.DO.UserAccountDO;
import com.wuw.ucenter.server.service.WXService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class WXServiceImpl implements WXService{

    @Value("${wx.open.app_id}")
    private String openAppID;
    @Value("${wx.open.app_secret}")
    private String openAppSecret;
    @Value("${wx.open.redirect_url}")
    private String openRedirectUrl;
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public Map<String, Object> wxOfScanning() {
        try {
            Map<String,Object> map = new HashMap<>();
            String wxRedirectUri = URLEncoder.encode(openRedirectUrl, "utf-8");
            map.put("appid", openAppID);
            map.put("scope","snsapi_login");
            map.put("redirect_uri",wxRedirectUri); // todo 第二步的回调地址
            map.put("state",System.currentTimeMillis());
            return map;
        } catch (UnsupportedEncodingException e) {
            log.error("微信扫码错误：{}",e);
            return null;
        }
    }

    /**
     *
     * @param code
     * @param state
     * @return 返回前端跳转地址
     */
    @Override
    public String wxOfScanningCallback(String code, String state) {

        // 1. 根据临时票据code，获取openId
        StringBuffer baseAccessTokenUrl = new StringBuffer().append("https://api.weixin.qq.com/sns/oauth2/access_token").append("?appid=%s").append("&secret=%s").append("&code=%s").append("&grant_type=authorization_code");
        String accessTokenUrl = String.format(baseAccessTokenUrl.toString(), openAppID, openAppSecret, code);
        ResponseEntity<String> forEntity = restTemplate.getForEntity(accessTokenUrl, String.class);
        String accesstokenInfo = forEntity.getBody();
        log.info("微信扫码登录accesstokenInfo is {}",accesstokenInfo);
        JSONObject jsonObject = JSONObject.parseObject(accesstokenInfo);
        String access_token = jsonObject.getString("access_token");
        String openid = jsonObject.getString("openid");

        //2. 根据openid获取用户信息
        UserAccountDO userAccountDO = null; // todo

        // 3. token处理
        String token = "";
        if (null != userAccountDO){

        }

        //注意我这里跳转的是一个前端的页面带着用户的信息
        return "redirect:"+"" + "/weixin/callback?token="+token+ "&openid="+openid;  // todo

    }



}
