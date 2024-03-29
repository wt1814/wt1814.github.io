

<!-- TOC -->

- [1. WEB组件](#1-web组件)
    - [1.1. WEB三大组件](#11-web三大组件)
        - [1.1.1. Servlet](#111-servlet)
        - [1.1.2. 过滤器](#112-过滤器)
        - [1.1.3. 监听器](#113-监听器)
            - [1.1.3.1. 监听器简介](#1131-监听器简介)
            - [1.1.3.2. Servlet监听器](#1132-servlet监听器)
    - [1.2. SpringMVC拦截器](#12-springmvc拦截器)
        - [1.2.1. ★★★过滤器和拦截器的区别](#121-★★★过滤器和拦截器的区别)
        - [1.2.2. 组件执行顺序](#122-组件执行顺序)

<!-- /TOC -->

# 1. WEB组件
<!-- 
【原创】Spring Boot 过滤器、监听器、拦截器的使用 
https://mp.weixin.qq.com/s/zMnQL2xXzQ8R4Zw4lGfYUg

 拦截器（Interceptor）与过滤器（Filter） 
 https://mp.weixin.qq.com/s/fu3FMWqoYnz77okOGd6GZw

SpringBoot实现过滤器、拦截器与切片
https://mp.weixin.qq.com/s/ziaC5PYs0mS9FrxLsyGYug

监听器入门看这篇就够了(修订版) 
https://mp.weixin.qq.com/s?__biz=MzI4Njg5MDA5NA==&mid=2247484862&idx=4&sn=14cbb3bc2d91696adc5d5ba7318c19c2&chksm=ebd744bfdca0cda90cf1753c2aca06fd78012698e016fd6fb5ea26d24f2faac6772e223f05e3&scene=21#wechat_redirect

https://www.cnblogs.com/dudududu/p/8507735.html
-->

## 1.1. WEB三大组件  
&emsp; web三大组件：servlet、filter、listener。  
&emsp; 加载顺序: listener -> filter -> servlet。  

### 1.1.1. Servlet  
&emsp; ......

### 1.1.2. 过滤器  
&emsp; 过滤器Filter，是 JavaEE 的标准，依赖于Servlet容器，使用的时候是配置在 web.xml 文件中的。  

&emsp; filter过滤器的作用：请求和回应的过滤。对request，response提前过滤掉一些信息，或者提前设置一些参数，然后再传入servlet进行业务逻辑。  

&emsp; 使用场景：~~过滤掉非法url,或者在传入servlet前统一设置字符集，或者去除掉一些非法字符。~~

&emsp; filter链的形成：在 web.xml 文件中，可以配置多个，执行的顺序是根据配置顺序从上到下。当请求一个资源时，服务器会查询web.xml中所有对此资源路径进行过滤的filter，并根据在web.xml中的先后顺序形成一个filter链(filterchain)。  

### 1.1.3. 监听器  
#### 1.1.3.1. 监听器简介  
&emsp; 监听器，字面上的理解就是监听观察某个事件(程序)的发生情况，当被监听的事件真的发生了的时候，事件发生者(事件源) 就会给注册该事件的监听者(监听器)发送消息，告诉监听者某些信息，同时监听者也可以获得一份事件对象，根据这个对象可以获得相关属性和执行相关操作。  
&emsp; 监听器模型涉及以下三个对象，模型图如下：  
![image](http://182.92.69.8:8081/img/web/web-1.png)  

* 事件：用户对组件的一个操作，或者说程序执行某个方法，称之为一个事件，如机器人程序执行工作。  
* 事件源：发生事件的组件就是事件源，也就是被监听的对象，如机器人可以工作，可以跳舞，那么就可以把机器人看做是一个事件源。  
* 事件监听器(处理器)：监听并负责处理事件的方法，如监听机器人工作情况，在机器人工作前后做出相应的动作，或者获取机器人的状态信息。  

&emsp; 执行顺序如下：  
1. 给事件源注册监听器。  
2. 组件接受外部作用，也就是事件被触发。  
3. 组件产生一个相应的事件对象，并把此对象传递给与之关联的事件处理器。  
4. 事件处理器启动，并执行相关的代码来处理该事件。  

&emsp; 监听器模式：事件源注册监听器之后，当事件源触发事件，监听器就可以回调事件对象的方法；更形象地说，监听者模式是基于：注册-回调的事件/消息通知处理模式，就是被监控者将消息通知给所有监控者。   
1. 注册监听器：事件源.setListener。  
2. 回调：事件源实现onListener。  

#### 1.1.3.2. Servlet监听器  
&emsp; 常用使用场景：常用于统计网站在线人数、系统加载时进行信息初始化、统计网站的访问量等等。  
&emsp; servlet监听器要用到javax.servlet.jar中的一组监听接口和事件类，根据监听对象的不同，监听器可被划分为3种：  
1. <font color = "red">ServletContext事件监听器：用于监听应用程序环境对象。</font>  
2. <font color = "red">HttpSession事件监听器：用于监听用户会话对象。</font>   
3. <font color = "red">ServletRequest事件监听器：用于监听请求消息对象。</font>   

&emsp; 这3种监听器共包含了8个监听接口、6个监听事件类。  
![image](http://182.92.69.8:8081/img/web/web-2.png)  

-----
## 1.2. SpringMVC拦截器  
&emsp; 拦截器 Interceptor 不依赖Servlet容器，依赖 Spring 等 Web 框架，在 SpringMVC 框架中是配置在SpringMVC 的配置文件中，在 SpringBoot 项目中也可以采用注解的形式实现。  
&emsp; 拦截器是AOP的一种应用，底层采用 Java 的反射机制来实现的。在AOP(Aspect-Oriented Programming)中用于在某个方法或字段被访问之前，进行拦截然后在之前或之后加入某些操作。比如日志，安全等。  
&emsp; 拦截器作用：权限验证，或者判断用户是否登陆，或者是像12306判断当前时间是否是购票时间。  
&emsp; 拦截器链：将拦截器按一定的顺序联结成一条链。在访问被拦截的方法或字段时，拦截器链中的拦截器就会按其之前定义的顺序被调用。一般拦截器方法都是通过动态代理的方式实现。  

### 1.2.1. ★★★过滤器和拦截器的区别  
&emsp; Spring的拦截器与Servlet的Filter有相似之处，比如二者都是AOP编程思想的体现，都能实现权限检查、日志记录等。不同的是：
1. 原理不同：拦截器是基于java的反射机制的，而过滤器是基于函数回调。  
2. 使用范围不同：Filter是Servlet规范规定的，只能用于Web程序中。而拦截器既可以用于Web程序，也可以用于Application、Swing程序中。  
3. 规范不同：Filter是在Servlet规范中定义的，是Servlet容器支持的。而拦截器是在Spring容器内的，是Spring框架支持的。  
4. 使用的资源不同：同其他的代码块一样，<font color = "red">拦截器也是一个Spring的组件，归Spring管理，配置在Spring文件中，因此能使用Spring里的任何资源、对象</font>，例如Service对象、数据源、事物管理等，通过IOC注入到拦截器即可；而Filter则不能。  
5. 深度不同：<font color = "clime">拦截器可以在方法前后，异常前后等调用，而过滤器只能在请求前和请求后各调用一次。</font>拦截器的使用有更大的弹性。所以在Spring架构的程序中，要优先使用拦截器。  
6. 生命周期不同：<font color = "clime">在action的生命周期中，拦截器可以多次被调用，而过滤器只能在容器初始化时被调用一次。</font>  

### 1.2.2. 组件执行顺序  
&emsp; 过滤前-拦截前-action执行-拦截后-过滤后  