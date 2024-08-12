package com.wuw.pay.api.DO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayInThirdNotify {
    private String inThirdNotiryId;

    private String payInNo;

    private String thirdTradeNo;

    private String payInType;

    private String payInTypeDetail;

    private Date payInTime;

    private Integer payInAmount;

    private String payInResult;

    private Date createTime;

    private String notifyContent;

    private String notifyContentOriginal;

}