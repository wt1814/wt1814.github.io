---
title: SpringBoot
date: 2020-04-13 00:00:00
tags:
    - SpringBoot
---

&emsp; SpringBoot中的一些特征：  
1. 创建独立的 Spring应用。  
4. 尽可能自动配置 spring应用。 
6. 可以完全没有代码生成和 XML配置要求。 
3. 提供的 starters 简化构建配置。  
2. 嵌入式 Tomcat、 Jetty、 Undertow容器（无需部署war文件）。  
5. 提供生产指标，例如指标、健壮检查和外部化配置。  



&emsp; SpringBoot基本上是 Spring框架的扩展，它消除了设置 Spring应用程序所需的 XML配置，为更快，更高效的开发生态系统铺平了道路。  
&emsp; spring 3.x版本  
&emsp; Spring Framework3.0是一个里程碑式的时代，他的功能特性开始出现了非常大的扩展，比如全面拥抱Java5、以及Spring Annotation。更重要的是，它提供了配置类注解@Configuration， 它出现的首要任务就是取代XML配置方式  

* @Configuration 去xml化  
&emsp; 核心目的是：把bean对象如何更加便捷的方式去加载到Spring IOC容器中  
* Component-Scan - @Service @Repository @Controller
* Import  


&emsp; 为什么Spring Cloud会采用Spring Boot来作为基础框架呢？    
1. Spring Cloud它是关注服务治理领域的解决方案，而服务治理是依托于服务架构之上，所以它仍然需要一个承载框架。  
2. Spring Boot 可以简单认为它是一套快速配置Spring应用的脚手架，它可以快速开发单个微服务。  

&emsp; 所以spring cloud的版本和spring boot版本的兼容性有很大关联  
