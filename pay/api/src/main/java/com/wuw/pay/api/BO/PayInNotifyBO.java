package com.wuw.pay.api.BO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayInNotifyBO {

    private String outTradeNo;

    private Integer payInStatus;

    private Integer payInTotalAmount;

}

