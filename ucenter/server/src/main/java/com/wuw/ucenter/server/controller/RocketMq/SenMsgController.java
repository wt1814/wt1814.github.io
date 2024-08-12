package com.wuw.ucenter.server.controller.RocketMq;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class SenMsgController {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;





}
