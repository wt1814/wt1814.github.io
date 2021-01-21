

# SpringBoot内置Tomcat
<!--
SpringBoot内嵌Tomcat的实现原理解析
https://blog.csdn.net/lveex/article/details/108942707?utm_medium=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-1.control&depth_1-utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-1.control 
SpringBoot内置tomcat启动原理
https://www.cnblogs.com/sword-successful/p/11383723.html
-->

<!-- 
~~
Spring Boot 内嵌Tomcat启动原理
https://blog.csdn.net/weixin_42440154/article/details/104943010
-->

## 内置Tomcat应用  
<!-- 
https://www.cnblogs.com/sword-successful/p/11383723.html
-->

## 嵌入式Tomcat  
<!-- 
https://blog.csdn.net/the_one_and_only/article/details/105177506
-->

## 内置Tomcat原理  
### Tomcat的自动装配  
&emsp; 自动装配过程中，查找classpath上所有jar包中的META-INF/spring.factories，找出其中的自动配置类并导入到容器中，其中Web容器所对应的自动配置类为ServletWebServerFactoryAutoConfiguration。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/boot/boot-1.png)  
&emsp; ServletWebServerFactoryAutoConfiguration中支持好几种web容器，比如Tomcat、Jetty和Undertow。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/boot/boot-2.png)  
&emsp; 而EmbeddedTomcat则是Tomcat组件相关的类，本身是一个FactoryBean，用来实例化TomcatServletWebServerFactory。此时TomcatServletWebServerFactory中就包含了创建和启动Tomcat的方法getWebServer()。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/boot/boot-3.png)  

### Tomcat的


