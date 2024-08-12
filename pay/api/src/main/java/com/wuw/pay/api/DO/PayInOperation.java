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
public class PayInOperation {
    private String payInThirdId;

    private String orderNo;

    private String payInNo;

    private Integer payInStatus;

    private Integer payInTotalAmount;

    private Integer payInAmount;

    private String userCode;

    private Date payTime;

    private String deviceId;

    private String deviceType;

    private String clientCode;

    private String payInType;

    private String payInTypeDetail;

    private String thirdTradeNo;

    private Integer querySize;

    private Date queryTime;

    private String createBy;

    private Date createTime;

    private String updateBy;

    private Date updateTime;


}