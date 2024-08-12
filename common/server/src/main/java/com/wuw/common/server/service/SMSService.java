package com.wuw.common.server.service;

import com.wuw.common.server.model.VO.SMSRequestVo;
import com.wuw.common.server.model.VO.SMSResponseVo;
import com.wuw.common.server.model.VO.SMSValidateVo;

public interface SMSService {


    // 发送
    SMSResponseVo send(SMSRequestVo smsRequestVo);

    // 验证
    Boolean validate(SMSValidateVo smsValidateVo);

}
