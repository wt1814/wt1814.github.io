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
public class PayIn {

    private String payInId;

    private String orderFrom;

    private String orderNo;

    private Integer payInStatus;

    private Integer payInTotalAmount;

    private Boolean payInCallback;

    private String createBy;

    private Date createTime;

    private String updateBy;

    private Date updateTime;

}
