package com.wuw.common.server.model.VO;

import lombok.Data;

import java.util.Map;

@Data
public class SMSRequestVo {


    /**
     * 哪个系统使用
     */
    private String system;

    /**
     * 业务模块
     */
    private String moudle;

    ///////////////////////////////////////////////////////////
    /**
     * 被发送人
     */
    private String sendCode;

    ///////////////////////////////////////////////////////////

    private String thirdType;


    /////////////////////////////////////发送内容

    ///////////////使用模板发送

    /**
     * 是否使用模板
     */
    private boolean isUseTemplate;
    /**
     * 模板id
     */
    private Integer templateId;

    /**
     * 发送是否验证码
     */
    private boolean isValidateCode;

    /**
     * 验证码长度
     */
    private int validateCodeLength;

    /**
     * 使用模板发非验证码内容
     */
    private Map<String,String> templateContent;

    ///////////////不使用模板发送

    /**
     * 不使用模板，内容自定义
     */
    private String content;

}
