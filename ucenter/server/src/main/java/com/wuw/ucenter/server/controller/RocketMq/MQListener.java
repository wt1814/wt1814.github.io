package com.wuw.ucenter.server.controller.RocketMq;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(consumerGroup = "springBootGroup", topic = "TestTopic")
public class MQListener implements RocketMQListener {

    @Override
    public void onMessage(Object message) {
        System.out.println("Received message : "+ message);
    }

}
