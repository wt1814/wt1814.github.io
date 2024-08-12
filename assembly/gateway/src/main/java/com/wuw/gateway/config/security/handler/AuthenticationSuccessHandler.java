package com.wuw.gateway.config.security.handler;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuw.common.api.apiResult.ApiResult;
import com.wuw.common.api.apiResult.ApiResultCode;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.WebFilterChainServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class AuthenticationSuccessHandler extends WebFilterChainServerAuthenticationSuccessHandler   {

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication){

        System.out.println("success");
        ServerWebExchange exchange = webFilterExchange.getExchange();
        ServerHttpResponse response = exchange.getResponse();
        //设置headers
        HttpHeaders httpHeaders = response.getHeaders();
        httpHeaders.add("Content-Type", "application/json; charset=UTF-8");
        httpHeaders.add("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        httpHeaders.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Authorization");
        //设置body
        // todo
        //WsResponse wsResponse = WsResponse.success();
        ApiResult apiResult = new ApiResult();
        byte[] dataBytes={};
        ObjectMapper mapper = new ObjectMapper();
        try {
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            httpHeaders.add(HttpHeaders.AUTHORIZATION, uuid);
            apiResult.setData(authentication.getName());
            // wsResponse.setResult(authentication.getName());
            //保存token
            redisTemplate.boundValueOps(uuid).set(authentication.getName(), 2*60*60, TimeUnit.SECONDS);
            dataBytes=mapper.writeValueAsBytes(apiResult);
        }
        catch (Exception ex){
            ex.printStackTrace();

            apiResult.setData(null);
            apiResult.setCode(String.valueOf(ApiResultCode.SYSTEM_EXCEPTION.getCode()));
            apiResult.setMsg("授权异常");
            dataBytes= JSON.toJSONString(apiResult).getBytes();
        }
        DataBuffer bodyDataBuffer = response.bufferFactory().wrap(dataBytes);
        return response.writeWith(Mono.just(bodyDataBuffer));

    }

}