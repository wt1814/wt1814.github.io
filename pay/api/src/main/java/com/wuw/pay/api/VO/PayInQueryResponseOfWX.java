package com.wuw.pay.api.VO;

import com.wuw.pay.api.VO.WXAmount;
import lombok.Data;

@Data
public class PayInQueryResponseOfWX {

    private WXAmount amount;
    private String appid;
    private String mchid;
    private String out_trade_no;
    // "NOTPAY"
    private String trade_state;
    private String trade_state_desc;

}