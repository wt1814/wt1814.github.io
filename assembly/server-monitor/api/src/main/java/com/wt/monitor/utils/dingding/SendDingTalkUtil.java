package com.wt.monitor.utils.dingding;

import com.alibaba.fastjson.JSON;
import com.wt.monitor.config.AppConfig;
import com.wt.monitor.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.ArrayList;

/**
 * 钉钉发送工具类
 */
@Slf4j
public class SendDingTalkUtil {

    public static void sendDingDing(String content) {
        log.info("==开始推送预警消息到钉钉==");
        MarkDownDingTalkBO dingTalkBO = new MarkDownDingTalkBO();
        dingTalkBO.setMsgtype("markdown");
        MarkDownBO markDownBO = new MarkDownBO();
        markDownBO.setTitle("资源预警");
        markDownBO.setText(content);
        dingTalkBO.setMarkdown(markDownBO);
        DingtalkAtBO dingtalkAtBO = new DingtalkAtBO();
        dingtalkAtBO.setAtMobiles(new ArrayList<String>());
        dingtalkAtBO.setIsAtAll(false);
        dingTalkBO.setAt(dingtalkAtBO);
        try {
            //发送钉钉消息
            String url = AppConfig.dingdingUrl;
            SendDingTalkUtil.sendDingTalk(url, dingTalkBO);
        } catch (Exception e) {
            log.error("发送钉钉消息失败: ", e);
        }
    }

    /**
     * 发送钉钉消息s
     *
     * @param url
     * @param dingTalkBO
     */
    public static void sendDingTalk(String url, DingTalkBO dingTalkBO) {
        CloseableHttpResponse response = HttpClientUtil.doJsonPost(url, JSON.toJSONString(dingTalkBO));
        if (ObjectUtils.isEmpty(response)) {
            return;
        }
        HttpEntity entity = response.getEntity();
        String result = null;
        if (entity != null) {
            try {
                result = EntityUtils.toString(entity);
            } catch (IOException e) {
                log.error("error:", e);
            }
        }
        log.info("==预警消息发送，result：{}==", result);
    }
}
