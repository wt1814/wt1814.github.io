package com.wuw.pay.api.VO;

import lombok.Data;

@Data
public class PayInQueryRequestVO {

    private String payInType;
    private String payInTypeDetail;

    String transactionId;
    boolean isNotify;
    String orderId;

}