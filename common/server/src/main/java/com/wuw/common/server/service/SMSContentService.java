package com.wuw.common.server.service;

import com.wuw.common.server.model.BO.SMSBO;
import com.wuw.common.server.model.DO.SmsTemplate;
import com.wuw.common.server.model.VO.SMSRequestVo;

public interface SMSContentService {

    SMSBO send(SMSRequestVo smsRequestVo, SmsTemplate smsTemplate,SMSBO smsbo);

}
