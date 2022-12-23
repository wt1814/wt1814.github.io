
<!-- TOC -->

- [1. XXL-JOB](#1-xxl-job)
    - [1.1. XXL-JOB文档](#11-xxl-job文档)
    - [1.2. 源码结构](#12-源码结构)
    - [1.3. 初始化数据库](#13-初始化数据库)
    - [1.4. 配置调度中心](#14-配置调度中心)
        - [1.4.1. 单机部署](#141-单机部署)
        - [1.4.2. 高可用集群搭建](#142-高可用集群搭建)
    - [1.5. 创建执行器项目](#15-创建执行器项目)
    - [1.6. 部署执行器](#16-部署执行器)
        - [1.6.1. 快速启动一个服务](#161-快速启动一个服务)
        - [1.6.2. 集群部署](#162-集群部署)
    - [1.7. 在调度中心中配置执行器](#17-在调度中心中配置执行器)
    - [1.8. 问题](#18-问题)

<!-- /TOC -->

<!-- 
https://www.cnblogs.com/ysocean/p/10541151.html#_label4_0
http://www.heartthinkdo.com/?p=2899#12
分布式任务调度平台XXL-JOB搭建教程
https://www.cnblogs.com/ysocean/p/10541151.html
-->

# 1. XXL-JOB  
&emsp; ***中文教程：https://www.xuxueli.com/xxl-job/#/  


## 1.1. XXL-JOB文档
1. 源码下载地址
    1. GitHub：https://github.com/xuxueli/xxl-job
    2. 码云：https://gitee.com/xuxueli0323/xxl-job

2. 文档地址
    1. 中文文档：http://www.xuxueli.com/xxl-job/#/
    2. 英文文档：http://www.xuxueli.com/xxl-job/en/#/ 

## 1.2. 源码结构  
&emsp; 通过上面给出的源码下载地址，将源码clone到IDEA中，如下：  
![image](http://182.92.69.8:8081/img/frame/xxl-2.png)  

## 1.3. 初始化数据库  
&emsp; 初始化脚本在上面源码目录的/doc/db/tables_xxl_job.sql，将此脚本在MySQL数据库中执行一遍。  
&emsp; 执行完毕，会在MySQL数据库中生成如下16张表：  
![image](http://182.92.69.8:8081/img/frame/xxl-3.png)  


## 1.4. 配置调度中心  
&emsp; 调度中心就是源码中的 xxl-job-admin 工程，需要将其配置成自己需要的调度中心，通过该工程能够以图形化的方式统一管理任务调度平台上调度任务，负责触发调度执行。  
&emsp; 修改调度中心配置文件  

```
### 1、调度中心项目的端口号以及访问路径
server.port=8080
server.context-path=/xxl-job-admin

### 2、配置静态文件的前缀
spring.mvc.static-path-pattern=/static/**
spring.resources.static-locations=classpath:/static/

### 3、配置模板文件
spring.freemarker.templateLoaderPath=classpath:/templates/
spring.freemarker.suffix=.ftl
spring.freemarker.charset=UTF-8
spring.freemarker.request-context-attribute=request
spring.freemarker.settings.number_format=0.##########

### 4、配置mybatis的mapper文件地址
mybatis.mapper-locations=classpath:/mybatis-mapper/*Mapper.xml

### 5、配置数据库的地址
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/xxl-job?Unicode=true&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=root_pwd
spring.datasource.driver-class-name=com.mysql.jdbc.Driver

spring.datasource.type=org.apache.tomcat.jdbc.pool.DataSource
spring.datasource.tomcat.max-wait=10000
spring.datasource.tomcat.max-active=30
spring.datasource.tomcat.test-on-borrow=true
spring.datasource.tomcat.validation-query=SELECT 1
spring.datasource.tomcat.validation-interval=30000

### 6、配置报警邮箱
spring.mail.host=smtp.qq.com
spring.mail.port=25
spring.mail.username=xxx@qq.com
spring.mail.password=xxx
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true


### 7、管理界面的登录用户名密码
xxl.job.login.username=admin
xxl.job.login.password=123456

### 8、调度中心通讯TOKEN，非空时启用
xxl.job.accessToken=

### 9、调度中心国际化设置，默认为中文版本，值设置为“en”时切换为英文版本
xxl.job.i18n=
```
&emsp; 注意：基本上上面的配置文件需要修改的只有第 5 点，修改数据库的地址，这要与前面初始化的数据库名称径，用户名密码保持一致；  
&emsp; 第二个就是修改第 6 点，报警邮箱，因为该工程任务失败后有失败告警功能，可以通过邮件来提醒，如果需要此功能，可以配置一下。  

### 1.4.1. 单机部署
该工程是一个springboot项目，只需要在IDEA中执行 XxlJobAdminApplication 类即可运行该工程：  
![image](http://182.92.69.8:8081/img/frame/xxl-4.png)  


### 1.4.2. 高可用集群搭建  
&emsp; 建议：推荐通过nginx为调度中心集群做负载均衡，分配域名。 <font color = "red">调度中心访问、执行器回调配置、调用API服务等操作均通过该域名进行。</font>  

&emsp; 注意：  

* DB配置保持一致；  
* 登陆账号配置保持一致；  
* 集群机器时钟保持一致（单机集群忽视）；  

![image](http://182.92.69.8:8081/img/frame/xxl-5.png)  
&emsp; 操作的数据库都是相同的，集群是tomcat服务器集群，但是连接的都是相同的数据库同表，不会产生Job的重复执行问题。  

&emsp; 最终执行的时候是只有一个admin去执行的  


## 1.5. 创建执行器项目  
&emsp; 其实在源码中，作者提供了各个版本的 执行器项目，下面以创建一个 springboot 版本的执行器为例来介绍。  
①、添加maven依赖
&emsp; 在创建好的springboot 项目的pom.xml 文件中添加如下依赖：  

```xml
<!-- xxl-rpc-core -->
<dependency>
      <groupId>com.xuxueli</groupId>
      <artifactId>xxl-job-core</artifactId>
      <version>2.0.1</version>
</dependency>
```

②、配置执行器  
&emsp; 在创建好的springboot 项目的配置文件 application.yml 添加如下配置：  

```yml
server:
  #项目端口号
  port: 8081
logging:
  #日志文件
  config: classpath:logback.xml

xxl:
  job:
    admin:
      #调度中心部署跟地址：如调度中心集群部署存在多个地址则用逗号分隔。
      #执行器将会使用该地址进行"执行器心跳注册"和"任务结果回调"。
      addresses: http://127.0.0.1:8080/xxl-job-admin

    #分别配置执行器的名称、ip地址、端口号
    #注意：如果配置多个执行器时，防止端口冲突
    executor:
      appname: executorDemo
      ip: 127.0.0.1
      port: 9999

      #执行器运行日志文件存储的磁盘位置，需要对该路径拥有读写权限
      logpath: /data/applogs/xxl-job/jobhandler
      #执行器Log文件定期清理功能，指定日志保存天数，日志文件过期自动删除。限制至少保持3天，否则功能不生效；
      #-1表示永不删除
      logretentiondays: -1
```
&emsp; 执行器服务属性介绍  

```text
### 为了实现自动组成到调度中心，所以这里必填
### 调度中心部署跟地址 [选填]：如调度中心集群部署存在多个地址则用逗号分隔。执行器将会使用该地址进行"执行器心跳注册"和"任务结果回调"；为空则关闭自动注册；
xxl.job.admin.addresses=http://127.0.0.1:8080/xxl-job-admin
 
### 执行器AppName [选填]：执行器心跳注册分组依据；为空则关闭自动注册
xxl.job.executor.appname=xxl-job-executor-sample
 
### 执行器IP [选填]：默认为空表示自动获取IP，多网卡时可手动设置指定IP，该IP不会绑定Host仅作为通讯实用；地址信息用于 "执行器注册" 和 "调度中心请求并触发任务"；
xxl.job.executor.ip=
 
### 执行器端口号 [选填]：小于等于0则自动获取；默认端口为9999，单机部署多个执行器时，注意要配置不同执行器端口；
xxl.job.executor.port=9999
 
 
### 执行器通讯TOKEN [选填]：非空时启用；
xxl.job.accessToken=
 
### 日志管理
### 执行器运行日志文件存储磁盘路径 [选填] ：需要对该路径拥有读写权限；为空则使用默认路径；
xxl.job.executor.logpath=/data/applogs/xxl-job/jobhandler
### 执行器日志保存天数 [选填] ：值大于3时生效，启用执行器Log文件定期清理功能，否则不生效；
xxl.job.executor.logretentiondays=-1
```

③、载入配置文件  
&emsp; 在项目中创建 XxlJobConfig.class 文件：  

```java
package com.ys.xxljobexecutordemo.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Create by YSOcean
 */
@Configuration
public class XxlJobConfig {
    private Logger logger = LoggerFactory.getLogger(XxlJobConfig.class);

    @Value("${xxl.job.admin.addresses}")
    private String adminAddresses;

    @Value("${xxl.job.executor.appname}")
    private String appName;

    @Value("${xxl.job.executor.ip}")
    private String ip;

    @Value("${xxl.job.executor.port}")
    private int port;

    @Value("${xxl.job.accessToken}")
    private String accessToken;

    @Value("${xxl.job.executor.logpath}")
    private String logPath;

    @Value("${xxl.job.executor.logretentiondays}")
    private int logRetentionDays;


    @Bean(initMethod = "start", destroyMethod = "destroy")
    public XxlJobSpringExecutor xxlJobExecutor() {
        logger.info(">>>>>>>>>>> xxl-job config init.");
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(adminAddresses);
        xxlJobSpringExecutor.setAppName(appName);
        xxlJobSpringExecutor.setIp(ip);
        xxlJobSpringExecutor.setPort(port);
        xxlJobSpringExecutor.setAccessToken(accessToken);
        xxlJobSpringExecutor.setLogPath(logPath);
        xxlJobSpringExecutor.setLogRetentionDays(logRetentionDays);

        return xxlJobSpringExecutor;
    }

}
```

④、创建任务JobHandler
&emsp; 在项目中创建一个Handler，用于执行我们想要执行的东西，这里我只是简单的打印一行日志：  

```java
XXL-JOB, Hello World!!!  
```

```java
package com.ys.xxljobexecutordemo.jobhandler;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.stereotype.Component;

/**
 * 任务Handler示例（Bean模式）
 *
 * 开发步骤：
 * 1、继承"IJobHandler"：“com.xxl.job.core.handler.IJobHandler”；
 * 2、注册到Spring容器：添加“@Component”注解，被Spring容器扫描为Bean实例；
 * 3、注册到执行器工厂：添加“@JobHandler(value="自定义jobhandler名称")”注解，注解value值对应的是调度中心新建任务的JobHandler属性的值。
 * 4、执行日志：需要通过 "XxlJobLogger.log" 打印执行日志；
 */
@JobHandler(value="demoJobHandler")
@Component
public class JobHandlerDemo extends IJobHandler{
    @Override
    public ReturnT<String> execute(String s) throws Exception {
        XxlJobLogger.log("XXL-JOB, Hello World.");
        return SUCCESS;
    }
}
```

1、日志管理  
&emsp; 日志路径是用来给调度平台展示日志的，这些日志内容，通过封装的XlJobLogger来打印日志  


## 1.6. 部署执行器  
### 1.6.1. 快速启动一个服务  
......

### 1.6.2. 集群部署  
&emsp; 执行器支持集群部署，执行器集群部署时，几点要求和建议：  
&emsp; 执行器回调地址（xxl.job.admin.addresses）需要保持一致；执行器根据该配置进行执行器自动注册等操作。  
&emsp; 同一个执行器集群内AppName（xxl.job.executor.appname）需要保持一致；调度中心根据该配置动态发现不同集群的在线执行器列表。  

## 1.7. 在调度中心中配置执行器  
&emsp; 新增一个任务时属性如下：  
![image](http://182.92.69.8:8081/img/frame/xxl-1.png)  
&emsp; 配置属性如下：  

```text
- 执行器：任务的绑定的执行器，任务触发调度时将会自动发现注册成功的执行器, 实现任务自动发现功能; 另一方面也可以方便的进行任务分组。每个任务必须绑定一个执行器, 可在 "执行器管理" 进行设置;
- 任务描述：任务的描述信息，便于任务管理；
- 路由策略：当执行器集群部署时，提供丰富的路由策略，包括；
    FIRST（第一个）：固定选择第一个机器；
    LAST（最后一个）：固定选择最后一个机器；
    ROUND（轮询）：；
    RANDOM（随机）：随机选择在线的机器；
    CONSISTENT_HASH（一致性HASH）：每个任务按照Hash算法固定选择某一台机器，且所有任务均匀散列在不同机器上。
    LEAST_FREQUENTLY_USED（最不经常使用）：使用频率最低的机器优先被选举；
    LEAST_RECENTLY_USED（最近最久未使用）：最久为使用的机器优先被选举；
    FAILOVER（故障转移）：按照顺序依次进行心跳检测，第一个心跳检测成功的机器选定为目标执行器并发起调度；
    BUSYOVER（忙碌转移）：按照顺序依次进行空闲检测，第一个空闲检测成功的机器选定为目标执行器并发起调度；
    SHARDING_BROADCAST(分片广播)：广播触发对应集群中所有机器执行一次任务，同时系统自动传递分片参数；可根据分片参数开发分片任务；
 
- Cron：触发任务执行的Cron表达式；
- 运行模式：
    BEAN模式：任务以JobHandler方式维护在执行器端；需要结合 "JobHandler" 属性匹配执行器中任务；
    GLUE模式(Java)：任务以源码方式维护在调度中心；该模式的任务实际上是一段继承自IJobHandler的Java类代码并 "groovy" 源码方式维护，它在执行器项目中运行，可使用@Resource/@Autowire注入执行器里中的其他服务；
    GLUE模式(Shell)：任务以源码方式维护在调度中心；该模式的任务实际上是一段 "shell" 脚本；
    GLUE模式(Python)：任务以源码方式维护在调度中心；该模式的任务实际上是一段 "python" 脚本；
    GLUE模式(PHP)：任务以源码方式维护在调度中心；该模式的任务实际上是一段 "php" 脚本；
    GLUE模式(NodeJS)：任务以源码方式维护在调度中心；该模式的任务实际上是一段 "nodejs" 脚本；
    GLUE模式(PowerShell)：任务以源码方式维护在调度中心；该模式的任务实际上是一段 "PowerShell" 脚本；
- JobHandler：运行模式为 "BEAN模式" 时生效，对应执行器中新开发的JobHandler类“@JobHandler”注解自定义的value值；
- 阻塞处理策略：调度过于密集执行器来不及处理时的处理策略；
    单机串行（默认）：调度请求进入单机执行器后，调度请求进入FIFO队列并以串行方式运行；
    丢弃后续调度：调度请求进入单机执行器后，发现执行器存在运行的调度任务，本次请求将会被丢弃并标记为失败；
    覆盖之前调度：调度请求进入单机执行器后，发现执行器存在运行的调度任务，将会终止运行中的调度任务并清空队列，然后运行本地调度任务；
- 子任务：每个任务都拥有一个唯一的任务ID(任务ID可以从任务列表获取)，当本任务执行结束并且执行成功时，将会触发子任务ID所对应的任务的一次主动调度。
- 任务超时时间：支持自定义任务超时时间，任务运行超时将会主动中断任务；
- 失败重试次数；支持自定义任务失败重试次数，当任务失败时将会按照预设的失败重试次数主动进行重试；
- 报警邮件：任务调度失败时邮件通知的邮箱地址，支持配置多邮箱地址，配置多个邮箱地址时用逗号分隔；
- 负责人：任务的负责人；
- 执行参数：任务执行所需的参数；
```
&emsp; 注意：  
* 执行器。表示一个执行器服务的名字。需要在“用户管理的”tab页添加这个服务信息。  

## 1.8. 问题  
<!-- 
https://blog.csdn.net/SweetyoYY/article/details/81843601
-->

1. java.lang.NoSuchMethodError: io.netty.util.internal.PlatformDependent.allocateUninitializedArray(I)  
&emsp; 参考：https://blog.csdn.net/u014209205/article/details/93970438  

```xml
<dependency>
     <groupId>com.corundumstudio.socketio</groupId>
     <artifactId>netty-socketio</artifactId>
     <version>1.7.17</version>
</dependency>
```




