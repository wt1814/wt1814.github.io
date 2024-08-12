package com.wuw.pay.server.service.impl;

import com.wuw.common.api.uid.IdGenerator;
import com.wuw.pay.api.BO.PayInBO;
import com.wuw.pay.api.BO.PayInNotifyBO;
import com.wuw.pay.api.DO.PayIn;
import com.wuw.pay.api.DO.PayInOperation;
import com.wuw.pay.api.VO.*;
import com.wuw.pay.server.dao.PayInMapper;
import com.wuw.pay.server.dao.PayInOperationMapper;
import com.wuw.pay.server.service.AliPayService;
import com.wuw.pay.server.service.PayInService;
import com.wuw.pay.server.service.WXPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
@Slf4j
public class PayInServiceImpl implements PayInService {

    @Resource
    private AliPayService aliPayService;
    @Resource
    private WXPayService wxPayService;
    @Resource
    private PayInMapper payInMapper;
    @Resource
    private PayInOperationMapper payInOperationMapper;


    @Resource
    private IdGenerator idGenerator;


    @Transactional(rollbackForClassName={"RuntimeException"})
    @Override
    public Object pay(String token, PayInRequestVO payInRequestVO) throws Exception {

        Object response = new Object();
        PayInBO payInBO = new PayInBO(); // 调用第三方结果

        // 1. 校验
        // 1.1. 参数校验

        // 1.2. 幂等校验

        // 2. token校验，获取用户信息

        // 3. 获取订单信息（支付总额、实际支付金额、商品信息），

        // 4. 生成初始状态的支付单，并入库
        // todo 注意支付金额是0的时候，支付状态的特殊处理
        String payInId = String.valueOf(idGenerator.nextId()); // 支付单
        String payInThirdId = String.valueOf(idGenerator.nextId()); // 支付流水号
        PayIn payIn = PayIn.builder().payInId(payInId).orderFrom(payInRequestVO.getProductCode()).orderNo(payInRequestVO.getOrderNo()).payInStatus(payInRequestVO.getTotalAmount() != 0?0:2).payInTotalAmount(payInRequestVO.getTotalAmount()).payInCallback(false).createBy("用户ID").createTime(new Date()).updateBy("用户ID").updateTime(new Date()).build();
        PayInOperation payInOperation = PayInOperation.builder().payInThirdId(payInThirdId).orderNo(payInRequestVO.getOrderNo()).payInNo(payInId).payInStatus(payInRequestVO.getTotalAmount() != 0?0:2).payInTotalAmount(payInRequestVO.getTotalAmount()).payInAmount(payInRequestVO.getAmount()).userCode("用户ID").payTime(new Date()).deviceId(payInRequestVO.getDeviceId())
                .deviceType(payInRequestVO.getDeviceType()).clientCode(payInRequestVO.getClientCode()).payInType(payInRequestVO.getPayInType()).payInTypeDetail(payInRequestVO.getPayInTypeDetail()).thirdTradeNo(null).createBy("用户ID").createTime(new Date()).updateBy("用户ID").updateTime(new Date()).build();
        // 事务处理
        PayIn payIn1 = payInMapper.selectByOrderNo(payInRequestVO.getOrderNo());
        if (null == payIn1){
            payInMapper.insert(payIn);
        }
        payInOperationMapper.insert(payInOperation);
        // 如果支付金额是0，直接通知业务系统：支付成功
        if (payInRequestVO.getTotalAmount() == 0){
            payInOperation.setPayInStatus(2);
            payInOperationMapper.updateByPrimaryKeySelective(payInOperation);
            return response;
        }

        // 5. 调用第三方支付
        // todo 注意两种异常（网络异常和业务异常）的处理
        if (payInRequestVO.getPayInType().equals("ali") && payInRequestVO.getPayInTypeDetail().equals("app")){

        }else if(payInRequestVO.getPayInType().equals("wx") && payInRequestVO.getPayInTypeDetail().equals("jsapi")){
            WXAmount wxAmount = WXAmount.builder().currency("CNY").total(payInRequestVO.getTotalAmount()).build();
            WXJSAPIPayer wxjsapiPayer = new WXJSAPIPayer();
            wxjsapiPayer.setOpenid(payInRequestVO.getOpenid());
            // todo
            WXJSAPIRequestVo wxjsapiRequestVo = WXJSAPIRequestVo.builder().out_trade_no("payInId").appid("appId").description(payInRequestVO.getBody()).amount(wxAmount).payer(wxjsapiPayer).build();
            payInBO = wxPayService.payOfJSAPI(wxjsapiRequestVo);
        }

        // 6. 根据调用第三方支付结果，修改支付单状态：作废或待支付
        payInOperation.setThirdTradeNo(payInBO.getOutTradeNo());
        payInOperation.setPayInStatus(payInBO.getStatus());
        // 成功
        if (payInBO.isResult()){
            payInOperation.setQuerySize(0);
            payInOperation.setQueryTime(new Date());
        }
        payInOperationMapper.updateByPrimaryKeySelective(payInOperation);

        return payInBO.getPayMap();
    }


    /**
     *
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, String> payInNotify(HttpServletRequest request) throws Exception{
        Map<String, String> result = new HashMap<>();

        String payInType = (String) request.getAttribute("payInType");
        String payInTypeDetail = (String)request.getAttribute("payInTypeDetail");

        // 1. 第三方回调，获取信息
        PayInNotifyBO payInNotifyBO = null;
        if (payInType.equals("ali") && payInTypeDetail.equals("app")){

        }else if (payInType.equals("wx") && payInTypeDetail.equals("jsapi")){
            payInNotifyBO = wxPayService.payNotify(request);
        }
        log.info("payInNotifyBOApiResult is{}",payInNotifyBO);

        // 2. 校验：支付单是否存在，金额是否正确，状态是否正确
        PayInOperation payInOperation = null;
        // 对数据单子校验，防止重复通知
        if (null == payInOperation || payInOperation.getPayInStatus() != 1){
            result.put("code", "FAIL");
            result.put("message", "失败");
            return result;
        }
        // 钱
        if (payInNotifyBO.getPayInTotalAmount() != payInOperation.getPayInAmount()){
            result.put("code", "FAIL");
            result.put("message", "失败");
            return result;
        }

        // 3. 入库
        payInOperation.setPayInStatus(payInNotifyBO.getPayInStatus());
        payInOperation.setUpdateTime(new Date());
        payInOperationMapper.updateByPrimaryKeySelective(payInOperation);
        //
        PayIn payIn = new PayIn();
        payIn.setPayInId(payInOperation.getPayInNo());
        payIn.setPayInStatus(payInNotifyBO.getPayInStatus());
        payIn.setUpdateTime(new Date());
        payInMapper.updateByPrimaryKeySelective(payIn);

        // 4. 通知业务系统

        result.put("code", "SUCCESS");
        result.put("message", "成功");
        return result;
    }


    // 间隔时间，以秒为单位
    public final static Map<Integer,Integer> queryTimeInterval = new HashMap();
    static {
        queryTimeInterval.put(0,5);
        queryTimeInterval.put(1, 30);
        queryTimeInterval.put(2, 60);
        queryTimeInterval.put(3, 180);
        queryTimeInterval.put(4, 300);
        queryTimeInterval.put(5, 600);
        queryTimeInterval.put(6, 1800);
    }

    /**
     * 自动查询
     */
    @Override
    public void payInQuery() {

        // 1. 支付单的状态、查询次数、查询时间，查询待支付的支付单
        // todo 最好当前时间和创建时间比较
        List<PayInOperation> selectUnpaid = null;
        if (null == selectUnpaid || selectUnpaid.size() == 0){
            return;
        }
        // 2. 调用第三方支付系统查询
        for (PayInOperation payInOperation:selectUnpaid){
            PayInQueryResponse query = null;
            if (payInOperation.getDeviceType().equals("wx") && payInOperation.getPayInTypeDetail().equals("jsapi")){
                try {
                    query = wxPayService.query(payInOperation.getPayInNo());
                }catch (Exception e){
                    continue;
                }
            }

            // 3. 根据支付结果处理
            // 3.1. 未支付，更新支付单流水表
            if (!query.getResult()){
                Integer querySize = payInOperation.getQuerySize();
                payInOperation.setQuerySize(querySize+1);

                Integer integer = queryTimeInterval.get(querySize);
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.SECOND, integer);
                payInOperation.setQueryTime(calendar.getTime());
                payInOperationMapper.updateByPrimaryKeySelective(payInOperation);
            }
            // 3.2. 支付成功，调用业务系统
            else {


            }
        }
    }


    /**
     * 手动查询
     * @param payInQueryRequestVO
     * @return
     */
    @Override
    public PayInQueryResponse payOfQuery(PayInQueryRequestVO payInQueryRequestVO) {

        PayInQueryResponse query = null;
        // 1. 查询结果
        try {
            if (payInQueryRequestVO.getPayInType().equals("ali") && payInQueryRequestVO.getPayInTypeDetail().equals("app")){

            }else if(payInQueryRequestVO.getPayInType().equals("wx") && payInQueryRequestVO.getPayInTypeDetail().equals("jsapi")){
                query = wxPayService.query("");
            }

        }catch (Exception e){

        }


        // 2. 调用业务系统


        return query;

    }


}
