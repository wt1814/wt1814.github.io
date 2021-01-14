

# 学习SpringBoot的感悟  

<!-- 
 非常有必要了解的Springboot启动扩展点 
 https://mp.weixin.qq.com/s/H9hcQHZUNhuRodEPiVOHfQ
 https://mp.weixin.qq.com/s/Z5meCbbfgUmnLnnWjeEeVw
-->

## 前言  
&emsp; SpringBoot基本上是Spring框架的扩展，它消除了设置Spring应用程序所需的 XML配置，为更快，更高效的开发生态系统铺平了道路。  

* SpringBoot简化了Spring的配置；
* SpringBoot提供了起步依赖、自动配置；
* SpringBoot内嵌了Tomcat、 Jetty、 Undertow容器（无需部署war文件）；
* 提供生产指标，例如指标、健壮检查和外部化配置。  

## Starts  
&emsp; 学习SpringBoot时，官方网址提供的资料很重要。SpringBoot提供的Starters：https://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#using-boot-starter

## 自定义starter  
&emsp; SpringBoot提供的Starters有限。但项目中可能会用到各种各样的组件，这时可以自定义Starter使项目组件化、模块化。  

## 事件回调  
&emsp; SpringBoot启动过程中，提供事件回调机制。巧妙运用事件回调机制，可能会使需求完成的更好。  
&emsp; SpringBoot提供的回调事件有：ApplicationContextInitializer、SpringApplicationRunListener、ApplicationRunner、CommandLineRunner。  

|类型|描述|获取方式|
|---|---|---|
|ApplicationContextInitializer|IOC容器初始化时被回调|需要配置在META-INF/spring.factories，因为SpringBoot启动流程中是从spring.factories中获取的|
|SpringApplicationRunListener|SpringBoot启动过程中多次被回调|需要配置在META-INF/spring.factories，因为SpringBoot启动流程中是从spring.factories中获取的|
|ApplicationRunner|容器启动完成后被回调|需要放在IOC容器中，因为SpringBoot启动流程中是从IOC容器中取出的|
|CommandLineRunner|ApplicationRunner之后被回调|需要放在IOC容器中，因为SpringBoot启动流程中是从IOC容器中取出的|

&emsp; 其中SpringApplicationRunListener是SpringBoot的事件监听机制。其最终会依赖到JDK的事件基类EventObject。SpringBoot内置了7种生命周期事件。  


