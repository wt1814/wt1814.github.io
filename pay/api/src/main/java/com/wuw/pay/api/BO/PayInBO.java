package com.wuw.pay.api.BO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayInBO {

    /**
     * 结果
     */
    private boolean result;
    /**
     * 描述
     */
    private String desc;

    /**
     * 第三方单号
     */
    private String outTradeNo;


    /**
     * 支付状态，0，初始，1待支付，2已支付，3，取消支付，4，订单失效
     */
    private Integer status;


    /**
     * 返回前端参数
     */
    private HashMap<String, String> payMap;


}
