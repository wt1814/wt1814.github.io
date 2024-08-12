package com.wuw.common.server.model.VO;

import lombok.Data;

@Data
public class SMSValidateVo {


    private Long templateId;

    /**
     * 发送人
     */
    private String sendCode;

    /**
     * 验证码必填
     */
    private String validateCode;



}
