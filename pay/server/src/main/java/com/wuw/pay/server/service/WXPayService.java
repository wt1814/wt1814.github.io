package com.wuw.pay.server.service;

import com.wuw.pay.api.BO.PayInBO;
import com.wuw.pay.api.BO.PayInNotifyBO;
import com.wuw.pay.api.VO.PayInQueryResponse;
import com.wuw.pay.api.VO.WXJSAPIRequestVo;

import javax.servlet.http.HttpServletRequest;

public interface WXPayService {

    PayInBO payOfJSAPI(WXJSAPIRequestVo wxjsapiRequestVo) throws Exception;

    PayInNotifyBO payNotify(HttpServletRequest httpServletRequest)throws Exception;

    PayInQueryResponse query(String outTradeNo) throws Exception;

}
