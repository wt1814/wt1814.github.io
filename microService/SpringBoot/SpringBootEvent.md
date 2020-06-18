---
title: Spring Boot事件监听机制
date: 2020-04-14 00:00:00
tags:
    - SpringBoot
---

&emsp; 在SpringBoot启动过程中，每个不同的启动阶段会分别广播不同的内置生命周期事件，然后相应的监听器会监听这些事件来执行一些初始化逻辑工作比如ConfigFileApplicationListener会监听onApplicationEnvironmentPreparedEvent事件来加载配置文件application.properties的环境变量等。  




