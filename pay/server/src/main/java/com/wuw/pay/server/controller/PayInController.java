package com.wuw.pay.server.controller;

import com.wuw.pay.api.VO.PayInQueryRequestVO;
import com.wuw.pay.api.VO.PayInQueryResponse;
import com.wuw.pay.api.VO.PayInRequestVO;
import com.wuw.pay.server.service.PayInService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 支付流程，跟三方支付平台交互
 */
@RestController
@RequestMapping(value = "/pay")
@Slf4j
public class PayInController {

    @Resource
    private PayInService payInService;

    /**
     *
     * @param token
     * @param payInRequestVO
     */
    public void payIn(@RequestHeader String token,@RequestBody PayInRequestVO payInRequestVO){

        try {
            payInService.pay(token,payInRequestVO);
        }catch (Exception e){

        }
    }

    /**
     *
     * @param request
     */
    public void  payInNotify(HttpServletRequest request) {

        Map<String, String> stringStringMap = null;
        try {
            stringStringMap = payInService.payInNotify(request);
        }catch (Exception e){

        }
    }

    /**
     * 手动查询
     * @param payInQueryRequestVO
     * @return
     */
    public PayInQueryResponse payOfQuery(PayInQueryRequestVO payInQueryRequestVO){
        PayInQueryResponse payInQueryResponse = payInService.payOfQuery(payInQueryRequestVO);
        return payInQueryResponse;
    }



}
