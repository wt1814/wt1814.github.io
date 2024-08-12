package com.wuw.common.server.service.impl;

import com.wuw.common.server.model.BO.SMSBO;
import com.wuw.common.server.model.DO.SmsTemplate;
import com.wuw.common.server.model.VO.SMSRequestVo;
import com.wuw.common.server.service.SMSContentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Date;

/**
 * 发送邮件
 */
@Slf4j
@Service(value = "mailService")
public class MailServiceImpl implements SMSContentService{


    @Autowired
    private JavaMailSender javaMailSender;

    /**
     * https://blog.csdn.net/weixin_44764019/article/details/120967059
     *
     * @param smsRequestVo
     * @param smsTemplate
     * @return
     */
    @Override
    public SMSBO send(SMSRequestVo smsRequestVo, SmsTemplate smsTemplate,SMSBO smsbo) {



        return null;

    }

    /**
     * 发送简单邮件(不带附件，不带格式)
     * @throws Exception
     */
    public void contextLoadsOfContent() throws Exception {
        SimpleMailMessage message=new SimpleMailMessage();
        message.setText("内容");
        message.setSubject("主题");
        message.setTo("收件人");
        message.setCc("抄送人");
        message.setBcc("密送人");
        javaMailSender.send(message);
    }

    /**
     * 发送带Html格式的附件
     * @throws Exception
     */
    public void contextLoadsOfHtml() throws Exception {
        MimeMessage mailMessage=javaMailSender.createMimeMessage();
        //需要借助Helper类
        MimeMessageHelper helper=new MimeMessageHelper(mailMessage);
        String context="<b>尊敬的用户：</b><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;您好，管理员已为你申请了新的账号，"+
                "请您尽快通过<a href=\"http://www.liwz.top/\">链接</a>登录系统。"
                +"<br>修改密码并完善你的个人信息。<br><br><br><b>员工管理系统<br>Li，Wan Zhi</b>";
        try {
            helper.setFrom("发送人");
            helper.setTo("收件人");
            helper.setBcc("密送人");
            helper.setSubject("主题");
            helper.setSentDate(new Date());//发送时间
            helper.setText(context,true);
            //第一个参数要发送的内容，第二个参数是不是Html格式。

            javaMailSender.send(mailMessage);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }


    /**
     * 发送带附件的邮件
     * @throws Exception
     */
    public void contextLoadsOfEnclosure() throws Exception {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        // true表示构建一个可以带附件的邮件对象
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,true);

        helper.setSubject("这是一封测试邮件");
        helper.setFrom("97******9@qq.com");
        helper.setTo("10*****16@qq.com");
        //helper.setCc("37xxxxx37@qq.com");
        //helper.setBcc("14xxxxx098@qq.com");
        helper.setSentDate(new Date());
        helper.setText("这是测试邮件的正文");
        // 第一个参数是自定义的名称，后缀需要加上，第二个参数是文件的位置
        helper.addAttachment("资料.xlsx",new File("/Users/gamedev/Desktop/测试数据 2.xlsx"));
        javaMailSender.send(mimeMessage);
    }


}
