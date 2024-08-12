package com.wuw.pay.api.VO;

import lombok.Data;

@Data
public class WXJSAPIAmount {

    private Integer total;
    private Integer payer_total;
    private String currency;
    private String payer_currency;

}
