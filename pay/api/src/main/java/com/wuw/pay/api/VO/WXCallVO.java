package com.wuw.pay.api.VO;

import lombok.Data;

@Data
public class WXCallVO {

    // https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_1_5.shtml
    private String transaction_id;
    private WXAmount amount;
    private String mchid;
    private String trade_state;
    private String bank_type;
    private String out_trade_no;

}

