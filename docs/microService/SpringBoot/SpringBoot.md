


Spring Boot、Spring MVC 和 Spring 有什么区别？  
1、Spring  
Spring最重要的特征是依赖注入。所有 SpringModules 不是依赖注入就是 IOC 控制反转。当我们恰当的使用 DI 或者是 IOC 的时候，我们可以开发松耦合应用。松耦合应用的单元测试可以很容易的进行。  
2、Spring MVC  
Spring MVC 提供了一种分离式的方法来开发 Web 应用。通过运用像 DispatcherServelet，MoudlAndView 和 ViewResolver 等一些简单的概念，开发 Web 应用将会变的非常简单。  
3、SpringBoot  
Spring 和 SpringMVC 的问题在于需要配置大量的参数。Spring Boot 通过一个自动配置和启动的项来目解决这个问题。为了更快的构建产品就绪应用程序，Spring Boot 提供了一些非功能性特征。  




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


&emsp; SpringBoot中的一些特征：  
1. 创建独立的 Spring应用。  
4. 尽可能自动配置 spring应用。 
6. 可以完全没有代码生成和 XML配置要求。 
3. 提供的 starters 简化构建配置。  
2. 嵌入式 Tomcat、 Jetty、 Undertow容器（无需部署war文件）。  
5. 提供生产指标，例如指标、健壮检查和外部化配置。  


spring boot来简化spring应用开发，约定大于配置，去繁从简，just run就能创建一个独立的，产品级别的应用  
Spring Boot 优点非常多，如：  
一、独立运行  
Spring Boot而且内嵌了各种servlet容器，Tomcat、Jetty等，现在不再需要打成war包部署到容器中，Spring Boot只要打成一个可执行的jar包就能独立运行，所有的依赖包都在一个jar包内。  
二、简化配置  
spring-boot-starter-web启动器自动依赖其他组件，简少了maven的配置。  
三、自动配置  
Spring Boot能根据当前类路径下的类、jar包来自动配置bean，如添加一个spring-boot-starter-web启动器就能拥有web的功能，无需其他配置。  
四、无代码生成和XML配置  
Spring Boot配置过程中无代码生成，也无需XML配置文件就能完成所有配置工作，这一切都是借助于条件注解完成的，这也是Spring4.x的核心功能之一。  
五、应用监控  
Spring Boot提供一系列端点可以监控服务及应用，做健康检测。  

