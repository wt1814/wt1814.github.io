package com.wuw.pay.server.service;

import com.wuw.pay.api.VO.PayInQueryRequestVO;
import com.wuw.pay.api.VO.PayInQueryResponse;
import com.wuw.pay.api.VO.PayInRequestVO;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 支付流程，跟三方支付平台交互
 */
public interface PayInService {

    Object pay(String token, PayInRequestVO payInRequestVO) throws Exception;

    Map<String, String> payInNotify(HttpServletRequest request)throws Exception;

    /**
     * 定时自动查询
     */
    void payInQuery();

    /**
     * 手动查询
     * @param payInQueryRequestVO
     * @return
     */
    PayInQueryResponse payOfQuery(PayInQueryRequestVO payInQueryRequestVO);

}
