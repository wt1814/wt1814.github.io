package com.wuw.common.server.model.DO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SmsRecord {
    private Integer id;

    private Integer templateId;

    private String thirdType;

    private String sendCode;

    private Integer validateCode;

    private Date sendDate;

    private Date validateExpireTime;

    private Date finishDate;

    private Integer status;

    private String thirdRequestId;

    private String thirdErrorMsg;

    private String thirdResultCode;

    private String content;

}