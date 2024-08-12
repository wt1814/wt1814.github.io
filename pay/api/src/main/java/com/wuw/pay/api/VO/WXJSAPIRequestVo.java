package com.wuw.pay.api.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WXJSAPIRequestVo {

    private String out_trade_no;
    private String mchid;
    private String appid;
    private String description;
    private String notify_url;
    private WXAmount amount;
    private WXJSAPIPayer payer;

}
