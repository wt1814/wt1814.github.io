package com.wuw.common.server.service.impl;

import com.wuw.common.server.dao.SmsRecordMapper;
import com.wuw.common.server.dao.SmsTemplateMapper;
import com.wuw.common.server.model.BO.SMSBO;
import com.wuw.common.server.model.DO.SmsRecord;
import com.wuw.common.server.model.DO.SmsTemplate;
import com.wuw.common.server.model.VO.SMSRequestVo;
import com.wuw.common.server.model.VO.SMSResponseVo;
import com.wuw.common.server.model.VO.SMSValidateVo;
import com.wuw.common.server.service.SMSContentService;
import com.wuw.common.server.service.SMSService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service(value = "smsService")
@Slf4j
public class SMSServiceImpl implements SMSService{

    public Map<String,SMSContentService> smsContentServiceMap;

    @Resource
    private SmsTemplateMapper smsTemplateMapper;
    @Resource
    private SmsRecordMapper smsRecordMapper;

    /**
     * 发送内容、验证码
     * @param smsRequestVo
     * @return
     */
    @Override
    public SMSResponseVo send(SMSRequestVo smsRequestVo) {

        SMSResponseVo smsResponseVo = new SMSResponseVo();

        // 1. 参数校验

        // 2. ~~幂等校验~~，前端做防重按钮

        // 3. 验证手机号或邮箱是否匹配。若无手机号或邮箱，联系管理员处理

        // 4. 发送内容
        SMSBO smsbo = new SMSBO();
        // 4.1. 是否需要模板，获取模板，缓存处理
        if (smsRequestVo.isUseTemplate()){
            // Commons StringSubstitutor对模板占位符进行赋值  https://juejin.cn/post/6893007620416897038
            SmsTemplate smsTemplate = smsTemplateMapper.selectByPrimaryKey(smsRequestVo.getTemplateId());
            // todo 邮箱模板：
            // todo 尊敬的用户，您好：<br/>&emsp;&emsp;您正在六合心理进行邮箱验证的操作，本次请求的邮件验证码是：<span style="color:red">${code}</span>。<br/>&emsp;&emsp;本验证码10分钟内有效，请及时输入。<br/><br/>&emsp;&emsp;此为系统邮件，请勿回复<br/>&emsp;&emsp;请保管好您的邮箱，避免账号被他人盗用<br/><br/>&emsp; &emsp;六合心理<br/>&emsp;&emsp;${date}
            String smsTemplateContent = smsTemplate.getSmsTemplateContent();
            if (smsRequestVo.isValidateCode()){
                Integer validateCode = generateValidateCode(smsRequestVo.getValidateCodeLength());
                // 占位符对应的值
                Map<String,String> valueMap = new HashMap<>();
                valueMap.put("code",String.valueOf(validateCode));
                StringSubstitutor sub = new StringSubstitutor(valueMap);
                String content = sub.replace(smsTemplateContent);
                smsRequestVo.setContent(content);
            }else {
                StringSubstitutor sub = new StringSubstitutor(smsRequestVo.getTemplateContent());
                String content = sub.replace(smsTemplateContent);
                smsRequestVo.setContent(content);
            }
        }

        // 4.2. 发送内容
        smsbo = smsContentServiceMap.get("").send(smsRequestVo, null,smsbo);

        // 5.
        // // 5.1 入库
        if (smsRequestVo.getThirdType().equals("sms")){
            smsRecordMapper.insert(new SmsRecord());
        }else if(smsRequestVo.getThirdType().equals("email")){

        }
        // 5.2 如果是验证码，验证码放缓存，缓存时间为过期时间
        if (smsRequestVo.isValidateCode()){

        }

        smsResponseVo.setRequestId(smsbo.getRequestId());
        return smsResponseVo;

    }

    /**
     * 校验验证码
     * @param smsValidateVo
     * @return
     */
    @Override
    public Boolean validate(SMSValidateVo smsValidateVo) {

        // 1. 配置后门使用

        // 2. 从redis获取

        // 3. 从数据库获取，获取最近一条

        // 4. 比较时间和验证码

        // 异步删除redis，更新数据库

        return null;
    }



    /**
     * 随机生成验证码
     * @param length 长度为4位或者6位
     * @return
     */
    public static Integer generateValidateCode(int length){
        Integer code =null;
        if(length == 4){
            code = new Random().nextInt(9999);//生成随机数，最大为9999
            if(code < 1000){
                code = code + 1000;//保证随机数为4位数字
            }
        }else if(length == 6){
            code = new Random().nextInt(999999);//生成随机数，最大为999999
            if(code < 100000){
                code = code + 100000;//保证随机数为6位数字
            }
        }else{
            throw new RuntimeException("只能生成4位或6位数字验证码");
        }
        return code;
    }

    /**
     * 随机生成指定长度字符串验证码
     * @param length 长度
     * @return
     */
    public static String generateValidateCode4String(int length){
        Random rdm = new Random();
        String hash1 = Integer.toHexString(rdm.nextInt());
        String capstr = hash1.substring(0, length);
        return capstr;
    }

}
