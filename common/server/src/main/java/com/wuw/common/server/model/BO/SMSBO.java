package com.wuw.common.server.model.BO;

import lombok.Data;

@Data
public class SMSBO {

    private Long requestId;

    /**
     * 验证码
     */
    private Integer validateCode;

}
