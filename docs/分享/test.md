
<!-- TOC -->

- [1. 怎么写“好”一个接口？](#1-怎么写好一个接口)
    - [1.1. 项目构建基础](#11-项目构建基础)
        - [1.1.1. 统一结果返回](#111-统一结果返回)
        - [1.1.2. 统一异常](#112-统一异常)
        - [1.1.3. 统一日志](#113-统一日志)
        - [1.1.4. 编码处理](#114-编码处理)
        - [1.1.5. 工具类](#115-工具类)
    - [1.2. api接口设计](#12-api接口设计)
        - [1.2.1. Restful API设计](#121-restful-api设计)
        - [1.2.2. 接口幂等](#122-接口幂等)
        - [1.2.3. 接口安全](#123-接口安全)
        - [1.2.4. 接口防刷](#124-接口防刷)
        - [1.2.5. 接口响应时间](#125-接口响应时间)
        - [1.2.6. 接口预警](#126-接口预警)
    - [1.3. JavaBean](#13-javabean)
        - [1.3.1. POJO](#131-pojo)
        - [1.3.2. BeanUtils](#132-beanutils)
        - [1.3.3. 参数校验](#133-参数校验)
        - [1.3.4. Lombok](#134-lombok)
    - [1.4. 数据安全](#14-数据安全)
    - [1.5. 事务相关](#15-事务相关)
        - [1.5.1. Spring事务](#151-spring事务)
        - [1.5.2. 分布式事务](#152-分布式事务)
            - [1.5.2.1. 本地消息表](#1521-本地消息表)
            - [1.5.2.2. mq推送](#1522-mq推送)

<!-- /TOC -->


# 1. 怎么写“好”一个接口？  

## 1.1. 项目构建基础
### 1.1.1. 统一结果返回


### 1.1.2. 统一异常



### 1.1.3. 统一日志  

1. 日志的5种级别，从高到低是：`Error`、Warn、`Info`、`Debug`、Trace。  

2. 日志配置按照级别分类存储日志。  

3. 日志输出格式：log4j有占位符替换（美观）和字符串拼接（性能好）两种方式。  

   ```java
   logger.debug("i'm: [{}] ", name);
   ```



### 1.1.4. 编码处理  



### 1.1.5. 工具类  



## 1.2. api接口设计  

### 1.2.1. Restful API设计

&emsp; Restful API的设计通过HTTP的方法来表示CRUD相关的操作。因此，除了get和post方法外，还会用到其他的HTTP方法，如PUT、DELETE、HEAD等，通过不同的HTTP方法来表示不同含义的操作。例如下面这组对文章的增删改查的Restful API：   



### 1.2.2. 接口幂等  



### 1.2.3. 接口安全  

&emsp; 接口安全是个比较大的工程。包含SQL注入、JSON反序列化漏洞、XSS攻击、CSRF攻击、文件上传下载漏洞、`敏感数据泄露`、XXE漏洞、DDoS攻击、框架或应用漏洞、弱口令、证书有效性验证、内部接口在公网暴露、未鉴权等权限相关漏洞......   

&emsp; 一个对外的接口，可以简单使用sign签名。把部分或所有参数拼接一起，再加入系统秘钥，进行MD5计算生成一个sign签名，防止参数被人恶意篡改，后台按同样的方法生成秘钥，进行签名对比。  

### 1.2.4. 接口防刷

1. 为什么会有人要刷接口？   
1、牟利。黄牛在 12306 网上抢票再倒卖。  
2、恶意攻击竞争对手。如短信接口被请求一次，会触发几分钱的运营商费用，当量级大了也很可观。  
3、压测。  

2. 什么是刷接口的"刷"字？  
1、次数多。  
2、频率频繁，可能 1 秒上千次。  
3. 判断接口是否是恶意？  
根据用户粒度，如果该用户符合上面提到的“刷”的概念，就是恶意的。  
4. 当知道接口是恶意请求时，我们该怎么做？  
一、直接拒绝访问。优点：简单粗暴，缺点：简单粗暴。  
二、返回“操作频繁”的错误提示。优点：提示友好，缺点：会把确实是操作比较频繁的真实用户拦截。  
三、验证码。  

### 1.2.5. 接口响应时间

1. 接口耗时统计
四种统计代码耗时的方法：时间差统计、StopWatch、Function、AutoCloseable。  

2. 降低接口响应时间
异步、并行、缓存、消除重量级锁...  
小知识点：ExecutorService结合Future，Future会阻塞线程池。考虑使用CompletableFuture和CompletionService。  

3. 接口超时处理
增加超时时间、尝试多调用一次、使用待处理队列、回滚数据、使用异步机制。  

### 1.2.6. 接口预警
自定义机器人接入：https://developers.dingtalk.com/document/app/custom-robot-access?spm=ding_open_doc.document.0.0.6d9d28e1FpnG8g#topic-2026027

## 1.3. JavaBean

### 1.3.1. POJO


### 1.3.2. BeanUtils
&emsp; 项目中使用到了MapStruct。使用Spring BeanUtils，注意怎么拷贝一个集合对象。   


### 1.3.3. 参数校验



### 1.3.4. Lombok



## 1.4. 数据安全

&emsp; 敏感数据脱敏处理  


## 1.5. 事务相关  
### 1.5.1. Spring事务  

```java
@Transactional(value = "hikariCPTransactionManager"， propagation = Propagation.REQUIRED，rollbackFor = Exception.class，timeout = 1， isolation = Isolation.DEFAULT)
```

&emsp; 注解中属性详解： 

|参数名称|功能描述|
|---|---|
|value |可选的限定描述符，指定使用的事务管理器。|
|propagation|该属性用于设置事务的传播行为。例如：@Transactional(propagation=Propagation.NOT_SUPPORTED，readOnly=true)。|
|isolation|该属性用于设置底层数据库的事务隔离级别，事务隔离级别用于处理多事务并发的情况，通常使用数据库的默认隔离级别即可，基本不需要进行设置。|
|timeout|该属性用于设置事务的超时秒数，默认值为-1表示永不超时。|
|readOnly|该属性用于设置当前事务是否为只读事务，默认值为false(可读写)。|
|rollbackFor|该属性用于设置需要进行回滚的异常类数组，当方法中抛出指定异常数组中的异常时，则进行事务回滚。例如：指定单一异常类：@Transactional(rollbackFor=RuntimeException.class)。可以指定多个异常类：@Transactional(rollbackFor={RuntimeException.class， Exception.class})。|
|rollbackForClassName|该属性用于设置需要进行回滚的异常类名称数组，当方法中抛出指定异常名称数组中的异常时，则进行事务回滚。例如：指定单一异常类名称@Transactional(rollbackForClassName=”RuntimeException”)。可以指定多个异常类名称：@Transactional(rollbackForClassName={“RuntimeException”，”Exception”})。|
|noRollbackFor| 该属性用于设置不需要进行回滚的异常类数组，当方法中抛出指定异常数组中的异常时，不进行事务回滚。例如：指定单一异常类：@Transactional(noRollbackFor=RuntimeException.class)指定多个异常类：@Transactional(noRollbackFor={RuntimeException.class， Exception.class})。|
| noRollbackForClassName | 该属性用于设置不需要进行回滚的异常类名称数组，当方法中抛出指定异常名称数组中的异常时，不进行事务回滚。例如：指定单一异常类名称：@Transactional(noRollbackForClassName=”RuntimeException”)指定多个异常类名称：@Transactional(noRollbackForClassName={“RuntimeException”，”Exception”}) |

Spring事务属性通常由事务的传播行为、事务的隔离级别、事务的超时值、事务只读标志组成。在进行事务划分时，须要进行事务定义，也就是配置事务的属性。  

事务传播行为是多个事务方法相互调用时，事务如何在这些方法间传播。事务传播行为是针对于被调用方法来说的，理解起来要想象成两个service 方法的调用才可以。有支持当前事务、不支持当前事务的情况。  

Spring事务失效：1. Transactional默认只回滚RuntimeException，但是可以指定要回滚的异常类型。`@Transactional(rollbackFor = Exception.class) `；2. 捕获了异常，未再抛出；3. 同一个类中方法调用；......  



### 1.5.2. 分布式事务  

分布式事务解决方案有XA两阶段提交、XA三阶段提交、TCC模式、事件溯源模式即长事务Saga、消息驱动模式，又包含本地消息表、事务消息、最大努力通知。  

具体框架有阿里开源的分布式事务框架Seata...  

项目中使用到的分布式事务本地消息表、mq最大努力通知。  

#### 1.5.2.1. 本地消息表
流程：  



#### 1.5.2.2. mq推送

mq消费消息流程：

1. 生产者（客户端）生产消息，并推送到服务端。  
2. 服务端进行持久化。  
3. 消费者（客户端）消费消息，消费结果通知服务端。  

在这3步流程中，都可能丢失消息。丢失消息的处理，可以采用补偿方案，即生产提供查询接口。  
