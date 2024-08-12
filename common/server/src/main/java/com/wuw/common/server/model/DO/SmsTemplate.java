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
public class SmsTemplate {
    private Integer id;

    private String project;

    private String business;

    private String thirdType;

    private String smsTemplateCode;

    private String smsTemplateName;

    private Integer validateExpireInterval;

    private Date createTime;

    private String createBy;

    private Date updateTime;

    private String updateBy;

    private String smsTemplateContent;

}