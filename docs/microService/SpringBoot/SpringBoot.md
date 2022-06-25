

<!-- TOC -->

- [1. ~~SpringBoot~~](#1-springboot)
    - [1.1. SpringBoot简介](#11-springboot简介)
    - [1.2. 配置文件](#12-配置文件)
        - [1.2.1. bootstrap和application两种配置文件](#121-bootstrap和application两种配置文件)
        - [1.2.2. application.yaml自定义属性](#122-applicationyaml自定义属性)
        - [1.2.3. 自定义配置文件](#123-自定义配置文件)
            - [1.2.3.1. 加载自定义yml文件](#1231-加载自定义yml文件)
            - [1.2.3.2. 让yml,properties配置文件有提示](#1232-让ymlproperties配置文件有提示)
        - [1.2.4. 多环境的属性配置](#124-多环境的属性配置)
        - [1.2.5. 配置信息加密](#125-配置信息加密)

<!-- /TOC -->

# 1. ~~SpringBoot~~  
<!-- 

SpringBoot四大核心组件
https://mp.weixin.qq.com/s/1BZBka_mYzQ6oFHx15K-Lg
-->

&emsp; SpringBoot基本上是Spring框架的扩展，它消除了设置Spring应用程序所需的XML配置，为更快，更高效的开发生态系统铺平了道路。  

* SpringBoot简化了Spring的配置；
* SpringBoot提供了起步依赖、自动配置；
* SpringBoot内嵌了Tomcat、 Jetty、 Undertow容器（无需部署war文件）；
* 提供生产指标，例如指标、健壮检查和外部化配置。  


## 1.1. SpringBoot简介  
&emsp; Spring Boot、Spring MVC 和 Spring 有什么区别？  
1. Spring  
&emsp; Spring最重要的特征是依赖注入。所有SpringModules不是依赖注入就是IOC控制反转。恰当的使用DI或者IOC，可以开发松耦合应用。松耦合应用的单元测试可以很容易的进行。  
2. Spring MVC    
&emsp; Spring MVC提供了一种分离式的方法来开发Web应用。通过运用像DispatcherServelet，MoudlAndView和ViewResolver等一些简单的概念，开发Web应用将会变的非常简单。  


3. SpringBoot   
&emsp; Spring 和 SpringMVC 的问题在于需要配置大量的参数。Spring Boot 通过一个自动配置和启动的项来目解决这个问题。为了更快的构建产品就绪应用程序，Spring Boot 提供了一些非功能性特征。  


&emsp; SpringBoot基本上是 Spring框架的扩展，它消除了设置 Spring应用程序所需的 XML配置，为更快，更高效的开发生态系统铺平了道路。  
&emsp; spring 3.x版本  
&emsp; Spring Framework3.0是一个里程碑式的时代，它的功能特性开始出现了非常大的扩展，比如全面拥抱Java5、以及Spring Annotation。更重要的是，它提供了配置类注解@Configuration，它出现的首要任务就是取代XML配置方式  

* @Configuration 去xml化  
&emsp; 核心目的是：把bean对象如何更加便捷的方式去加载到Spring IOC容器中  
* Component-Scan - @Service @Repository @Controller
* Import  

&emsp; 为什么Spring Cloud会采用Spring Boot来作为基础框架呢？    
1. Spring Cloud是关注服务治理领域的解决方案，而服务治理是依托于服务架构之上，所以它仍然需要一个承载框架。  
2. Spring Boot可以简单认为它是一套快速配置Spring应用的脚手架，它可以快速开发单个微服务。  

&emsp; 所以spring cloud的版本和spring boot版本的兼容性有很大关联  

&emsp; SpringBoot中的一些特征：  
1. 创建独立的 Spring应用。  
4. 尽可能自动配置 spring应用。 
6. 可以完全没有代码生成和 XML配置要求。 
3. 提供的 starters 简化构建配置。  
2. 嵌入式 Tomcat、 Jetty、 Undertow容器（无需部署war文件）。  
5. 提供生产指标，例如指标、健壮检查和外部化配置。  


&emsp; spring boot来简化spring应用开发，约定大于配置，去繁从简，just run就能创建一个独立的，产品级别的应用  
Spring Boot 优点非常多，如：  
1. 独立运行  
&emsp; Spring Boot而且内嵌了各种servlet容器，Tomcat、Jetty等，现在不再需要打成war包部署到容器中，Spring Boot只要打成一个可执行的jar包就能独立运行，所有的依赖包都在一个jar包内。  
2. 简化配置  
&emsp; Spring-boot-starter-web启动器自动依赖其他组件，简少了maven的配置。  
3. 自动配置  
&emsp; Spring Boot能根据当前类路径下的类、jar包来自动配置bean，如添加一个spring-boot-starter-web启动器就能拥有web的功能，无需其他配置。  
4. 无代码生成和XML配置  
&emsp; Spring Boot配置过程中无代码生成，也无需XML配置文件就能完成所有配置工作，这一切都是借助于条件注解完成的，这也是Spring4.x的核心功能之一。  
5. 应用监控  
&emsp; Spring Boot提供一系列端点可以监控服务及应用，做健康检测。  



-----

## 1.2. 配置文件  
### 1.2.1. bootstrap和application两种配置文件
&emsp; SpringBoot的核心配置文件是bootstrap和application配置文件。  

* bootstrap配置文件是系统级别的，用来加载外部配置，如配置中心的配置信息，也可以用来定义系统不会变化的属性。bootstatp文件的加载先于application文件。  
* application配置文件是应用级别的，是当前应用的配置文件。  

&emsp; 在Spring Boot中有两种上下文，一种是 bootstrap，另外一种是application。bootstrap是应用程序的父上下文，boostrap由父ApplicationContext加载。bootstrap加载优先于applicaton。boostrap里面的属性不能被覆盖。  

&emsp; bootstrap/ application的应用场景：  
1. application主要用于SpringBoot项目的自动化配置。  
2. bootstrap配置文件有以下几个应用场景：  
1).使用Spring Cloud Config配置中心时，这时需要在bootstrap配置文件中添加连接到配置中心的配置属性来加载外部配置中心的配置信息；  
2).一些固定的不能被覆盖的属性；  
3).一些加密/解密的场景；  

&emsp; Application.properties中key在SpringBoot官网文档给出了详尽的配置以及说明。http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#common-application-properties 。

### 1.2.2. application.yaml自定义属性  
&emsp; Application自定义属性，获取配置文件属性值方式：@Value注解，注入Environment属性。  

### 1.2.3. 自定义配置文件  
#### 1.2.3.1. 加载自定义yml文件  
......
<!-- 

https://blog.csdn.net/Zack_tzh/article/details/103728869?utm_term=yml%E5%8C%85%E5%90%AB%E5%8F%A6%E4%B8%80%E4%B8%AAyml&utm_medium=distribute.pc_aggpage_search_result.none-task-blog-2~all~sobaiduweb~default-0-103728869-null-null&spm=3001.4430

https://blog.csdn.net/wangzhihao1994/article/details/96721708
https://www.cnblogs.com/huahua035/p/11272464.html

-->

#### 1.2.3.2. 让yml,properties配置文件有提示
<!--
超实用，Spring Boot 让yml,properties配置文件有提示 
https://mp.weixin.qq.com/s?__biz=MzA4NjgxMjQ5Mg==&mid=2665762955&idx=1&sn=95c84bb2bd98b2a7ad1ddf674bd51959&chksm=84d202a8b3a58bbed74fd75ebcacd0306dc903a7737833f022fa69ce5b7c381753a7ebd7f6f0&mpshare=1&scene=1&srcid=&sharer_sharetime=1573691098077&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=2459be73db906624e0949067f9260b7dff7fc16439cfbba9bed0142b226a920e566c156662dadf2b9695e0da6ac283e4bf4df8bdb6c156c8f21b2df31ecf98b28126c08f0a024633087f0a70cc074936&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070152&lang=zh_CN&pass_ticket=9PZBgG0W8u5aIQH8JwuoebfJbcWXVv%2F8Jwpab0URWoWCafXeDrv6e7zaSa2n%2B7Oa
-->

......

### 1.2.4. 多环境的属性配置  
......

### 1.2.5. 配置信息加密  
<!-- 
https://mp.weixin.qq.com/s/mfGtMEu1TUa6UTGPgSKy0A
-->
......


