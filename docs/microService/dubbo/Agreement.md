
<!-- TOC -->

- [1. Dubbo](#1-dubbo)
    - [1.2. 通信协议](#12-通信协议)
        - [dubbo协议](#dubbo协议)
        - [rmi协议](#rmi协议)
        - [hessian协议](#hessian协议)
        - [http协议](#http协议)
        - [webservice协议](#webservice协议)
    - [1.1. Dubbo支持哪些序列化方式？](#11-dubbo支持哪些序列化方式)

<!-- /TOC -->

# 1. Dubbo


## 1.2. 通信协议  
&emsp; **<font color = "blue">不同服务在性能上适用不同协议进行传输，比如大数据用短连接协议，小数据大并发用长连接协议。</font>**  s

|协议名称|实现描述|连接|适用范围|使用场景|
|---|---|---|---|---|
|dubbo	|传输：mina、netty、grizzy <br/>序列化：dubbo、hessian2、java、json|dubbo缺省，采用单一长连接和NIO异步通讯，传输协议TCP|1.传入传出参数数据包较小<br/>2.消费者比提供者多<br/>3.常规远程服务方法调用<br/>4.不适合传送大数据量的服务，比如文件、传视频|常规远程服务方法调用|
|rmi|传输：java  rmi<br/>序列化：java 标准序列化(实现ser接口)|1.连接个数：多连接<br/>2.连接方式：短连接<br/>3.传输协议：TCP/IP<br/>4.传输方式：BIO|1.常规RPC调用<br/>2.与原RMI客户端互操作<br/>3.可传文件<br/>4.不支持防火墙穿|常规远程服务方法调用，与原生RMI服务互操作|
|hessian|传输：Serverlet容器<br/>序列化：hessian二进制序列化|1.连接个数：多连接<br/>2.连接方式：短连接<br/>3.传输协议：HTTP<br/>4.传输方式：同步传输|1.提供者比消费者多<br/>2.可传文件<br/>3.跨语言传输| 需同时给应用程序和浏览器JS使用的服务。|
|http|传输：Servlet容器<br/>序列化：表单序列化|	1.连接个数：多连接<br/>2.连接方式：短连接<br/>3.传输协议：HTTP<br/>4.传输方式：同步传输|1.提供者多余消费者<br/>2.数据包混合	|需同时给应用程序和浏览器JS使用的服务。|
|webservice	|传输：HTTP<br/>序列化：SOAP文件序列化|1.连接个数：多连接<br/>2.连接方式：短连接<br/>3.传输协议：HTTP<br/>4.传输方式：同步传输|	1.系统集成<br/>2.跨语言调用|系统集成，跨语言调用|
|thrift	|与thrift RPC实现集成，并在基础上修改了报文头 |长连接、NIO异步传输 |||	
|Redis|||||  	


### dubbo协议
缺省协议，使用基于mina1.1.7+hessian3.2.1的tbremoting交互。  
连接个数：单连接  
连接方式：长连接  
传输协议：TCP  
传输方式：NIO异步传输  
序列化：Hessian二进制序列化  
适用范围：传入传出参数数据包较小（建议小于100K），消费者比提供者个数多，单一消费者无法压满提供者，尽量不要用dubbo协议传输大文件或超大字符串。  
适用场景：常规远程服务方法调用

1、dubbo默认采用dubbo协议，dubbo协议采用单一长连接和NIO异步通讯，适合于小数据量大并发的服务调用，以及服务消费者机器数远大于服务提供者机器数的情况  
2、他不适合传送大数据量的服务，比如传文件，传视频等，除非请求量很低。  
配置如下：  

```text
<dubbo:protocol name="dubbo" port="20880" />
<dubbo:protocol name=“dubbo” port=“9090” server=“netty” client=“netty” codec=“dubbo”
serialization=“hessian2” charset=“UTF-8” threadpool=“fixed” threads=“100” queues=“0” iothreads=“9”
buffer=“8192” accepts=“1000” payload=“8388608” />
```
3、Dubbo协议缺省每服务每提供者每消费者使用单一长连接，如果数据量较大，可以使用多个连接。  
```text
<dubbo:protocol name="dubbo" connections="2" />
```
4、为防止被大量连接撑挂，可在服务提供方限制大接收连接数，以实现服务提供方自我保护  
```text
<dubbo:protocol name="dubbo" accepts="1000" />
```

### rmi协议
Java标准的远程调用协议。  
连接个数：多连接  
连接方式：短连接  
传输协议：TCP  
传输方式：同步传输  
序列化：Java标准二进制序列化  
适用范围：传入传出参数数据包大小混合，消费者与提供者个数差不多，可传文件。  
适用场景：常规远程服务方法调用，与原生RMI服务互操作  

RMI协议采用JDK标准的java.rmi.*实现，采用阻塞式短连接和JDK标准序列化方式。  

### hessian协议
基于Hessian的远程调用协议。  
连接个数：多连接  
连接方式：短连接  
传输协议：HTTP  
传输方式：同步传输  
序列化：表单序列化  
适用范围：传入传出参数数据包大小混合，提供者比消费者个数多，可用浏览器查看，可用表单或URL传入参数，暂不支持传文件。  
适用场景：需同时给应用程序和浏览器JS使用的服务。  

1、Hessian协议用于集成Hessian的服务，Hessian底层采用Http通讯，采用Servlet暴露服务，Dubbo缺省内嵌Jetty作为服务器实现。  
2、Hessian是Caucho开源的一个RPC框架：http://hessian.caucho.com，其通讯效率高于WebService和Java自带的序列化。  

### http协议
基于http表单的远程调用协议。  
连接个数：多连接  
连接方式：短连接  
传输协议：HTTP  
传输方式：同步传输  
序列化：表单序列化  
适用范围：传入传出参数数据包大小混合，提供者比消费者个数多，可用浏览器查看，可用表单或URL传入参数，暂不支持传文件。  
适用场景：需同时给应用程序和浏览器JS使用的服务。  

### webservice协议

基于WebService的远程调用协议。 
连接个数：多连接 
连接方式：短连接 
传输协议：HTTP 
传输方式：同步传输 
序列化：SOAP文本序列化 
适用场景：系统集成，跨语言调用


## 1.1. Dubbo支持哪些序列化方式？  
&emsp; 默认使用Hessian序列化，还有Duddo、FastJson、Java自带序列化。   


 序列化是将一个对象变成一个二进制流就是序列化， 反序列化是将二进制流转换成对象。  
 
为什么要序列化？  

1. 减小内存空间和网络传输的带宽  

2. 分布式的可扩展性  

3. 通用性，接口可共用。  

Dubbo序列化支持java、compactedjava、nativejava、fastjson、dubbo、fst、hessian2、kryo，其中默认hessian2。其中java、compactedjava、nativejava属于原生java的序列化。  

dubbo序列化：阿里尚未开发成熟的高效java序列化实现，阿里不建议在生产环境使用它。  

hessian2序列化：hessian是一种跨语言的高效二进制序列化方式。但这里实际不是原生的hessian2序列化，而是阿里修改过的，它是dubbo RPC默认启用的序列化方式。
json序列化：目前有两种实现，一种是采用的阿里的fastjson库，另一种是采用dubbo中自己实现的简单json库，但其实现都不是特别成熟，而且json这种文本序列化性能一般不如上面两种二进制序列化。  
java序列化：主要是采用JDK自带的Java序列化实现，性能很不理想。  