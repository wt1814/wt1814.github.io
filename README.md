
<!-- TOC -->

- [1. 项目搭建](#1-项目搭建)
    - [1.1. 待解决](#11-待解决)
    - [1.2. 环境基础](#12-环境基础)
    - [1.3. Maven](#13-maven)
    - [1.4. 项目组织](#14-项目组织)
        - [1.4.1. 项目介绍](#141-项目介绍)
        - [1.4.2. MVC三层架构上再加一层Manager层](#142-mvc三层架构上再加一层manager层)
        - [1.4.3. ~~项目Demo脚手架制作~~](#143-项目demo脚手架制作)
    - [1.5. 项目基础](#15-项目基础)
        - [1.5.1. Java基础](#151-java基础)
        - [1.5.2. 格式化](#152-格式化)
        - [1.5.3. 工具](#153-工具)
        - [1.5.4. 异常](#154-异常)
        - [1.5.5. 日志](#155-日志)
        - [1.5.6. 接口](#156-接口)
        - [1.5.7. ⁕⁕⁕使用Gzip压缩请求响应数据](#157-⁕⁕⁕使用gzip压缩请求响应数据)
        - [1.5.8. 集成JSch，文件传输，操作Linux命令](#158-集成jsch文件传输操作linux命令)
    - [1.6. 集成Mybatis](#16-集成mybatis)
    - [1.7. 集成ShardingSphere](#17-集成shardingsphere)
    - [1.8. 集成Redis](#18-集成redis)
        - [1.8.1. Redis搭建](#181-redis搭建)
        - [1.8.2. 集成Redis](#182-集成redis)
        - [1.8.3. 集成redisson](#183-集成redisson)
        - [1.8.4. 集成Redis+Caffeine两级缓存](#184-集成rediscaffeine两级缓存)
        - [1.8.5. XXX使用两级缓存框架j2Cache](#185-xxx使用两级缓存框架j2cache)
    - [1.9. 集成RocketMq](#19-集成rocketmq)
    - [1.10. SpringBoot](#110-springboot)
    - [1.11. SpringCloud](#111-springcloud)
        - [1.11.1. 集成nacos](#1111-集成nacos)
        - [1.11.2. 集成网关gateWay、认证授权security](#1112-集成网关gateway认证授权security)
        - [1.11.3. 集成Sentinel](#1113-集成sentinel)
        - [1.11.4. 集成链路SkyWalking](#1114-集成链路skywalking)
    - [1.12. 分布式](#112-分布式)
        - [1.12.1. 分布式id](#1121-分布式id)
        - [1.12.2. 分布式事务](#1122-分布式事务)
        - [1.12.3. 分布式锁](#1123-分布式锁)
    - [1.13. 并发，大数据量](#113-并发大数据量)
    - [1.14. 工具](#114-工具)
        - [1.14.1. 接口管理](#1141-接口管理)
            - [1.14.1.1. 步骤一：集成Swagger3](#11411-步骤一集成swagger3)
            - [1.14.1.2. 步骤二：Mock平台](#11412-步骤二mock平台)
        - [1.14.2. 定时任务](#1142-定时任务)
        - [1.14.3. IO流](#1143-io流)
            - [1.14.3.1. 读取Resources目录下文件](#11431-读取resources目录下文件)
            - [1.14.3.2. 上传/下载](#11432-上传下载)
                - [1.14.3.2.1. 上传服务器](#114321-上传服务器)
                - [1.14.3.2.2. 阿里OSS](#114322-阿里oss)
                - [1.14.3.2.3. ~~分片上传和断点续传~~](#114323-分片上传和断点续传)
            - [1.14.3.3. 导入导出](#11433-导入导出)
                - [1.14.3.3.1. easyExcel使用](#114331-easyexcel使用)
        - [1.14.4. 字典表（树状图/级联关系）](#1144-字典表树状图级联关系)
        - [1.14.5. 发送短信邮件](#1145-发送短信邮件)
        - [1.14.6. 获取Ip](#1146-获取ip)
    - [1.15. Elasticsearch](#115-elasticsearch)
        - [1.15.1. EFK](#1151-efk)
        - [1.15.2. Elasticsearch使用](#1152-elasticsearch使用)
    - [1.16. 替换web服务器](#116-替换web服务器)
    - [1.17. XXXIM通信](#117-xxxim通信)
        - [1.17.1. 单方面消息推送](#1171-单方面消息推送)
        - [1.17.2. 聊天](#1172-聊天)
    - [1.18. XXX响应式编程](#118-xxx响应式编程)
    - [1.19. Nginx](#119-nginx)
    - [1.20. 监控](#120-监控)
    - [1.21. 服务重启脚本制作](#121-服务重启脚本制作)
    - [1.22. 接口对接](#122-接口对接)
        - [1.22.1. 从Json自动生成JavaBean](#1221-从json自动生成javabean)
    - [1.23. 用户系统](#123-用户系统)
        - [1.23.1. 注册登录](#1231-注册登录)
            - [1.23.1.1. 注册](#12311-注册)
            - [1.23.1.2. jwt生成token](#12312-jwt生成token)
            - [1.23.1.3. 微信授权登录](#12313-微信授权登录)
            - [1.23.1.4. 微信扫码登录](#12314-微信扫码登录)
        - [1.23.2. 权限](#1232-权限)
            - [1.23.2.1. 功能权限](#12321-功能权限)
            - [1.23.2.2. 面向B端的数据权限](#12322-面向b端的数据权限)
    - [1.24. 订单](#124-订单)
    - [1.25. 支付](#125-支付)
    - [1.26. 微信开发](#126-微信开发)
    - [1.27. 运维](#127-运维)
        - [1.27.1. Kubernetes](#1271-kubernetes)
        - [1.27.2. Jenkins](#1272-jenkins)

<!-- /TOC -->

# 1. 项目搭建  
&emsp; 记录日常所学，项目所接触到的。  

&emsp; 目前暴露的ip是真实ip，1核2G服务器，仅用来学习，误攻击。  
&emsp; 努力更新中，敬请期待...  

## 1.1. 待解决  
1. redis哨兵搭建：  
    1. 问题：出口ip问题  
    2. 代码解决：1).@SpringBootApplication干掉自动配置，2).RedisTemplate自动装配@Autowired(required = false)  
2. MHA搭建mysql一主一从切换。MyBatisGenerator的使用  
3. Gateway集成单点登录spring-security-cas。全网无案例，待研发。  
4. 待集成  
    1. 集成elk  
    3. 监控  

## 1.2. 环境基础  
1. JDK1.8  
2. ip地址一般在服务器hosts中配置。（本项目中暂无使用）  

    ```xml
    <consul.host>wuw</consul.host>
    ```

## 1.3. Maven  
1. maven repository地址：https://mvnrepository.com/  
2. mapper-generator小工具：https://zhuanlan.zhihu.com/p/636727703/ 、 https://github.com/alansun2/mapper-generator-javafx/releases  
1. maven项目循环依赖，采用api与服务server分离接口形式搭建微服务。  
2. maven多模块项目。依赖的版本号一般在父项目parent中统一管理。  

    ```xml
        <dependencyManagement>
            <dependencies>
                <dependency>
                    <groupId>org.springframework.cloud</groupId>
                    <artifactId>spring-cloud-dependencies</artifactId>
                    <version>${spring-cloud.version}</version>
                    <type>pom</type>
                    <scope>import</scope>
                </dependency>
            </dependencies>
        </dependencyManagement>
    ```
3. Maven设置多环境：https://www.likecs.com/show-204897168.html    
&emsp; 已经集成，如果environment下有同名文件，会覆盖。目前只用到nacos地址多环境。  

    ```xml
    <profile>
        <!-- 开发 -->
        <id>dev</id>
        <activation>
            <!--默认开发环境-->
            <activeByDefault>true</activeByDefault>
        </activation>
        <properties>
            <profile.active>dev</profile.active>
            <!--  本地hosts配置 -->
            <nacos.addr>182.92.69.8:8848</nacos.addr>
        </properties>
    </profile>
    ```
4. 暂无maven私服，个人维护...  
5. 在聚合项目中可以添加maven-compiler-plugin插件。即每个项目中都要定义java编译版本。    
6. 远程仓库和镜像  

## 1.4. 项目组织  
### 1.4.1. 项目介绍
1. 项目介绍  
    1. parent，父项目；  
    2. assembly，组件项目，包含网关、百度uid-generator、链路...；  
    &emsp; 脚手架工程：cloud-scaffolding-demo。~~待制作：maven archetype。~~   
    3. common，基础项目。  
        &emsp; common-api基础工具类，需要@ComponentScan("com.wuw")扫描，xxx-api依赖common-api。  
        &emsp; common-server，基础服务。  
    4. ucenter，用户系统  
    5. cms，内容管理
2. 项目配置介绍：  
    1. bootstrap.yml，项目启动配置  
    &emsp; application.yml配置文件引入其他的yml配置文件：https://blog.csdn.net/Zack_tzh/article/details/103728869?utm_term=yml%E5%8C%85%E5%90%AB%E5%8F%A6%E4%B8%80%E4%B8%AAyml&utm_medium=distribute.pc_aggpage_search_result.none-task-blog-2~all~sobaiduweb~default-0-103728869-null-null&spm=3001.4430  
    2. application.yml，系统级配置  
    3. application-dataSource，中间件配置  
    4. application-buiness.yml，业务配置  

### 1.4.2. MVC三层架构上再加一层Manager层
&emsp; http://www.cppcns.com/ruanjian/java/433037.html  


### 1.4.3. ~~项目Demo脚手架制作~~  
&emsp; 脚手架工程：cloud-scaffolding-demo。~~待制作：maven archetype。~~   

## 1.5. 项目基础  
### 1.5.1. Java基础
1. java声明静态Map常量：StaticMap.java  


### 1.5.2. 格式化
1. 统一格式返回ApiResult  
2. 统一响应处理：https://mp.weixin.qq.com/s/1ncfvS1XN_xLIM_8vHskBw  
2. JSON不返回Null，两种方案：  
    &emsp; 1).https://blog.csdn.net/javaee520/article/details/117900370  
    &emsp; 2).@JsonInclude(JsonInclude.Include.NON_NULL)  
2. 日期格式化：fastJson的@JSONField （~~非jackson的@JsonFormat~~） 和 Spring的@DateTimeFormat    
3. ~~数据相关~~  
    &emsp; 1).~~格式化~~  
    &emsp; 2).~~数据脱敏~~  
    &emsp; &emsp; 1. 数据库脱敏  
    &emsp; &emsp; &emsp; https://mp.weixin.qq.com/s/sBzWHygMffD2i8po4HQetA  
    &emsp; &emsp; &emsp; https://mp.weixin.qq.com/s/4xi0K9s_H4phMI-mAGurIw  
    &emsp; &emsp; 2. 日志脱敏  
    &emsp; &emsp; &emsp; https://mp.weixin.qq.com/s/cBiH6Jxf2N0eoTgLRe4p-g  
    &emsp; &emsp; 3. 接口返回数据脱敏  
    &emsp; &emsp; &emsp; https://mp.weixin.qq.com/s/gL3bKtZB-DNGWK8NgGEiVQ  
    &emsp; 3).~~加密算法~~  

### 1.5.3. 工具
1. JavaBean  
    &emsp; 1. BeanUtils：MapStuct：https://baijiahao.baidu.com/s?id=1710072420980854506&wfr=spider&for=pc    
    &emsp; &emsp; BeanUtils.copyProperties()复制时，去null值方法：https://blog.csdn.net/2301_76585121/article/details/133980244  
    &emsp; 2. 参数校验：  
    &emsp; &emsp; 1).https://blog.csdn.net/xnn_fjj/article/details/100603270  
    &emsp; &emsp; 2).结合统一异常处理  
    &emsp; &emsp; 3).~~普通Java项目使用Hibernate Validator手动校验Bean：https://www.icode9.com/content-1-1305959.html~~  
    &emsp; 3. lombok使用： 
    ```java
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    ```

2. http  
    &emsp; 1. restTemplate（个人喜好）  
    &emsp; &emsp; 1). 注意：@ComponentScan("com.wuw")扫描RestTemplateConfig类  
    &emsp; &emsp; 2). 使用：https://blog.csdn.net/jinjiniao1/article/details/100849237  
    &emsp; &emsp; *** https://blog.csdn.net/lllhhhv/article/details/123129228  
    &emsp; &emsp; 3). RestTemplate中文乱码问题：https://www.cnblogs.com/qianxiaoPro/p/15419528.html    
    &emsp; 2. http重试spring-retry：https://www.hangge.com/blog/com.wuw.doubleCache.cache/detail_2522.html  

### 1.5.4. 异常
1. 自定义业务异常：（两种方案，看个人喜好）    
    &emsp; 1).若service层以javaBean返回，则需在service逻辑代码里抛出业务异常BusinessException；  
    &emsp; 2).异常处理消耗性能，service可以直接以ApiResult返回。   
2. 统一异常处理：https://blog.csdn.net/qq_49281137/article/details/121101543  
3. 获取异常堆栈字符串：https://blog.csdn.net/wsdhla/article/details/130356769  

### 1.5.5. 日志
&emsp; 本项目整合了log4j2  
1. 日志整合log4j2  
    &emsp; 1.https://blog.csdn.net/qq_43842093/article/details/123027783  
    &emsp; 2.日志文件json输出  
                ```text
                <!--<PatternLayout pattern="${LOG_PATTERN}"/>-->
                <JsonLayout compact="true" eventEol="true" objectMessageAsJsonObject="true"/>
                ```
    &emsp; 3.依赖冲突  
    &emsp; 4.日志性能：异步输出，https://blog.csdn.net/qq_26323323/article/details/124741008    
    &emsp; 5.多环境设置：  
        &emsp; &emsp; 1).SpringBoot+log4j2.xml使用application.yml属性值 https://www.cnblogs.com/extjava/p/7553642.html  
        &emsp; &emsp; 2).~~maven-resources插件~~   
    &emsp; 6.main ERROR Unable to create file ${sys:log.path.prefix}/log.log java.io.IOException
        &emsp; &emsp; https://blog.csdn.net/linmengmeng_1314/article/details/100016303
2. ~~logbcak异步输出：https://blog.csdn.net/qq_38536878/article/details/123821072~~   
3. ~~日志切面记录请求~~   

### 1.5.6. 接口 
1. RESTful风格  
2. ~~接口幂等~~  
3. ~~接口防刷/反爬虫~~  
4. ~~接口安全~~
5. ~~日志预警：1).日志框架预警；2). Filebeat+Logstash发送Email告警日志~~  


### 1.5.7. ⁕⁕⁕使用Gzip压缩请求响应数据  


### 1.5.8. 集成JSch，文件传输，操作Linux命令  
JSch 是SSH2的一个纯Java实现。它允许你连接到一个sshd 服务器，使用端口转发，X11转发，文件传输等等。  

```java
// channel类型有ftp，exec，shell
channel = (ChannelExec) session.openChannel("exec"); 
```


## 1.6. 集成Mybatis  
1. ~~整合druid：（集成ShardingSphere时废弃了）~~ 
    * https://www.cnblogs.com/carry-huang/p/15260422.html  
    * https://blog.csdn.net/kobe_IT/article/details/123531088
    * logback配置Druid Filter：https://blog.csdn.net/qq_42145871/article/details/90704632
2. 集成mybatis：  
    * https://blog.csdn.net/jzman/article/details/111027453  
3. common-server集成mybatis-generator-core插件。（个人喜好。一直用的maven插件，不怎么喜欢idea或其他外置插件）  
    1. 
    2. 避免用blob生成模型类：https://www.5axxw.com/questions/content/dkqfvh 、 https://blog.csdn.net/lenny_wants/article/details/123649547    
4. mybatis-generator小工具：https://zhuanlan.zhihu.com/p/636727703/      
4. 增删改查  
    1. 批量插入或者更新(on duplicate key update)：https://blog.csdn.net/a1275302036/article/details/120923406  

## 1.7. 集成ShardingSphere
1. 集成ShardingSphere，实现读写分离    
    1. ShardingSphere-JDBC5.1.0读写分离配置示例：https://blog.csdn.net/qq_31226223/article/details/123815551    
    2. 强制走主库  
    &emsp; https://www.csdn.net/tags/MtTaEgzsNzExMDcyLWJsb2cO0O0O.html  
    &emsp; 强制走主库正确使用：https://blog.csdn.net/seanxwq/article/details/122810902  


## 1.8. 集成Redis  
1. 目前使用redis单机模式。redis哨兵模式，哨兵之间注册的是内网地址，需解决出口ip问题。  

### 1.8.1. Redis搭建  
1. linux 下安装redis并设置开机自启动  https://blog.csdn.net/linhui258/article/details/124524729  

    
### 1.8.2. 集成Redis  
&emsp; SpringBoot集成Redis：https://blog.csdn.net/wl_honest/article/details/124171062  

### 1.8.3. 集成redisson
1. 哨兵模式redisson：https://blog.csdn.net/weixin_45973130/article/details/122383689  
2. 单机模式：  
3. 报错：READONLY You can‘t write against a read only replica  
&emsp; https://blog.csdn.net/qq_42818496/article/details/107838154  
4. Redisson看门狗失效：https://blog.csdn.net/nlcexiyue/article/details/120783519  

### 1.8.4. 集成Redis+Caffeine两级缓存  
1. caffeine：咖啡因，本地缓存之王。  
1. 集成：  
    1. https://blog.csdn.net/Trunks2009/article/details/123982910  
    &emsp; 1).集成了V3版本，可以设置过期时间；2).集成了分布式环境改造，解决了缓存一致性问题。  
    2. 项目com.wuw.double-cache  
    3. 两个不同项目集成：报错NoClassDefFoundError，maven依赖冲突。  
 2. 使用  

### 1.8.5. XXX使用两级缓存框架j2Cache  


## 1.9. 集成RocketMq  
1. 集成RocketMq（~~两主两从~~）。  
    1. 先搭建成功，创建一个topic，最后再在程序中集成。  
    2. 搭建：  
        &emsp; https://blog.csdn.net/qq_39280536/article/details/105020434
        &emsp; https://blog.csdn.net/moyuanbomo/article/details/115375785  
        &emsp; RocketMQ Web控制台监控界面介绍+部署 https://blog.csdn.net/abu935009066/article/details/120828337
    3. 部分问题：  
        &emsp; 1). /bin/runserver.sh和runbroker.sh设置堆大小  
        &emsp; 2). RocketMQ集群启动报错：java.lang.RuntimeException: Lock failed,MQ already started
        &emsp; 3). https://blog.csdn.net/TaylorSwiftiiln/article/details/121077705
    4. SpringBoot整合RocketMq：  
        &emsp; https://blog.csdn.net/qq_26154077/article/details/111013842  
        &emsp; https://blog.csdn.net/qq_43631716/article/details/119902582  

## 1.10. SpringBoot  
1. 自动装配  
    1. 前期redis未搭建成功，去掉redis相关自动装配。  
    &emsp; 方式一：  
    
    ```java
    @SpringBootApplication(exclude= {DataSourceAutoConfiguration.class,
                    RedisAutoConfiguration.class, // todo redis
                    RedissonAutoConfiguration.class
               })
    ```
    &emsp; 方式二：  
    
    ```java
    @ConditionalOnProperty(name = "spring.redis.sentinel.enable", havingValue = "true")
    ```
        

## 1.11. SpringCloud
### 1.11.1. 集成nacos 
1. springCloud集成nacos：https://blog.csdn.net/footless_bird/article/details/125362050  
2. maven多环境参考https://blog.csdn.net/DU87680258/article/details/111879755  
3. JavaWeb获取系统环境变量[/doc/system/EnvironmentVariable.md]  

### 1.11.2. 集成网关gateWay、认证授权security  
1. 网关，spring cloud gateway集成spring cloud security统一认证、授权  
    * https://www.jianshu.com/p/fbabb8684dfd  
2. gateway集成feign   
    &emsp; openFeign 调用服务报错:No qualifying bean of type ‘org.springframework.boot.autoconfigure.http.HttpMessage   
    * https://blog.csdn.net/shehuinibingge/article/details/108470373   
    * https://www.pudn.com/news/628f8328bf399b7f351e7130.html  


### 1.11.3. 集成Sentinel
1. 搭建  
&emsp; https://sentinelguard.io/zh-cn/docs/dashboard.html    
2. 集成  
    1. 官方使用文档：https://sentinelguard.io/zh-cn/docs/basic-api-resource-rule.html  
    2. gateway集成Sentinel https://blog.51cto.com/u_15284359/4874743  
    3. 普通接口和feign集成Sentinel  
    &emsp; https://www.jb51.net/article/226839.htm  
    &emsp; https://www.jianshu.com/p/f5cabdef0de1  
    &emsp; ~~https://blog.csdn.net/MenBad/article/details/125118367~~  
    4. ~~dubbo之使用sentinel限流~~  
    &emsp; https://blog.csdn.net/wang0907/article/details/121356872  
    
### 1.11.4. 集成链路SkyWalking
1. 集成SkyWalking  
&emsp; https://blog.csdn.net/weixin_35574537/article/details/112952419   

&emsp; Skywalking8：https://blog.csdn.net/Cy_LightBule/article/details/123855647  
&emsp; https://zhuanlan.zhihu.com/p/268913908  
&emsp; http://t.zoukankan.com/duanxz-p-15602842.html  

&emsp; 注意：  
&emsp; &emsp; 1. 要注意版本。  
&emsp; &emsp; 2. 要启动两个服务：startup.*:组合脚本，同时启动oapService.*，webappService.*脚本  
&emsp; &emsp; 3. 启动服务时VM参数：
  
    -javaagent:G:\software\apache-skywalking-java-agent-8.9.0\skywalking-agent\skywalking-agent.jar -Dskywalking.agent.service_name=consumer -Dskywalking.collector.backend_service=ip:11800    

2. ~~如何使用 SkyWalking 给 Dubbo 服务做链路追踪？~~  
&emsp; https://blog.csdn.net/XiaoHanZuoFengZhou/article/details/103287858?utm_medium=distribute.pc_relevant.none-task-blog-2~default~baidujs_baidulandingword~default-0-103287858-blog-120191083.pc_relevant_multi_platform_whitelistv2_ad_hc&spm=1001.2101.3001.4242.1&utm_relevant_index=3
  

## 1.12. 分布式
### 1.12.1. 分布式id
&emsp; 集成百度uid-generator  
1. 百度uid-generator：https://blog.csdn.net/qq_43690938/article/details/116712041  
2. 步骤：1. 集成uid-generator原有项目， 2. 基础项目common-api配置， 3. 业务项目使用。  
3. 编码：  
    1. common-api中配置UidGeneratorConfiguration，使用@MapperScan(value = "com.baidu.fsg.uid.worker.dao")扫描。否则每个项目需要做一样的配置。    
    2. 业务项目的配置文件里mybatis.mapper-locations = classpath:/idmapper/*.xml,classpath:/mapper/*.xml扫描。  
    3. 业务代码里具体使用：    
        
        ````java
        @Resource
        private IdGenerator idGenerator;
        
        long l = idGenerator.nextId();
        ````

### 1.12.2. 分布式事务  
1. shardingsphere分布式事务：https://shardingsphere.apache.org/document/legacy/4.x/document/cn/manual/sharding-jdbc/usage/transaction/#%E9%85%8D%E7%BD%AEspring-boot%E7%9A%84%E4%BA%8B%E5%8A%A1%E7%AE%A1%E7%90%86%E5%99%A8   


### 1.12.3. 分布式锁  
&emsp; 参考Redission章节  


## 1.13. 并发，大数据量
&emsp; CollectionUtils，一组数据固定分组  


## 1.14. 工具  
### 1.14.1. 接口管理  
#### 1.14.1.1. 步骤一：集成Swagger3  
1. ~~springboot与swagger3的集成~~  
2. 网关聚合Swagger  
    1. gateway网关中聚合swagger： https://blog.csdn.net/w1014074794/article/details/109100433  
    2. Spring Security+Swagger3：https://blog.csdn.net/sunxiaoju/article/details/110751151  
3. swagger3访问： http://localhost:8081/swagger-ui/index.html#/  


#### 1.14.1.2. 步骤二：Mock平台  
&emsp; 使用接口管理平台Apifox、eolinker等导入swagger数据。    

### 1.14.2. 定时任务  
1. SpringMvc+quartz整合：  
&emsp; https://blog.csdn.net/inspurs06/article/details/100355629  
&emsp; https://blog.csdn.net/qq_24953751/article/details/134027973  
2. xxl-job
&emsp; https://blog.csdn.net/Fristm/article/details/125351356  
&emsp; https://blog.csdn.net/yunhaoyoung/article/details/120508147  
&emsp; docker安装  https://www.bbsmax.com/A/ZOJPNR6xdv/  
&emsp; https://www.cnblogs.com/ysocean/p/10541151.html#_label4_0  
&emsp; https://mp.weixin.qq.com/s/G6yGtDGyf3gASvUBTo6AEg  

### 1.14.3. IO流  
#### 1.14.3.1. 读取Resources目录下文件  
1. 报错：cannot be resolved to absolute file path because it does not reside in the file system  
&emsp; https://blog.csdn.net/m0_59092234/article/details/125402107  
2. 编码：FileOfReadJar.java  

#### 1.14.3.2. 上传/下载
##### 1.14.3.2.1. 上传服务器  
1. 代码：UploadFileController.java  
2. 上传：  
    1. 即上传文件又传输参数使用MultipartHttpServletRequest或者@RequestPart(https://blog.csdn.net/weixin_50158735/article/details/115768430)  
    2. 使用NIO-FileChannel优化  
3. 下载  
    1. 使用mount命令将多个应用服务器的资源挂载到一块共享磁盘上。    
    2. nginx配置图片服务器。  

##### 1.14.3.2.2. 阿里OSS  
1. 编码：参考ALiOSS.java  
2. 文档：  
    1. 公有桶   
    2. 私有桶   
        &emsp; 1. 上传  
            &emsp; &emsp; 使用STS临时访问凭证上传  
            &emsp; &emsp; https://help.aliyun.com/document_detail/100624.html?spm=5176.8466032.help.dexternal.739f1450FwuC0e  
            &emsp; &emsp; https://help.aliyun.com/document_detail/100624.html?spm=5176.8466032.policy.1.17ea1450MYk2w4  
        &emsp; 2. 下载  
            &emsp; &emsp; https://help.aliyun.com/document_detail/39607.htm?spm=a2c4g.11186623.0.0.72075d60lbnzq4#concept-39607-zh  
            &emsp; &emsp; 使用签名URL进行临时授权  
            &emsp; &emsp; https://help.aliyun.com/document_detail/32016.htm?spm=a2c4g.11186623.0.0.c8861c91aMITas#concept-32016-zh  
            &emsp; &emsp; STS临时授权OSS操作权限报“Access denied by authorizer's policy”错误  
            &emsp; &emsp; https://help.aliyun.com/document_detail/161911.html  
        &emsp; 3. ~~回源（CDN加速）：私有桶的cdn加速在cdn处做访问限制。~~    
        &emsp; 4. 跨域：  
            &emsp; &emsp; https://help.aliyun.com/document_detail/31870.html?spm=5176.8466032.cors.1.527a1450z1AZq0  
            &emsp; &emsp; https://help.aliyun.com/document_detail/40183.html?spm=a2c4g.11186623.0.0.52af68bath8RRd  
    3. JAVA实现服务端获取签名后前端直传文件到oss服务器：  
    &emsp; https://blog.csdn.net/qq_44682266/article/details/108258326  
    &emsp; 文件上传成功后则返回204状态码：https://help.aliyun.com/document_detail/31925.html?spm=a2c4g.11186623.6.1568.759b6e28YhoGnX  
    &emsp; 私有bucket：https://blog.csdn.net/weixin_44179010/article/details/121657880  
    &emsp; 注意：前端分片上传需要id和key。  

##### 1.14.3.2.3. ~~分片上传和断点续传~~ 


#### SFTP使用  
SFTPUtils.java


#### 1.14.3.3. 导入导出  
##### 1.14.3.3.1. easyExcel使用  
1. 官网：https://easyexcel.opensource.alibaba.com/   
2. 使用：  
    1. 读Excel、写Excel、填充Excel。  
    2. web上传、下载：https://www.yuque.com/easyexcel/doc/easyexcel#9cd151de  
        1. web下载前端直接http请求接口地址获取文件。  
2. 注意：  
    1. 时间、数字格式化(https://www.yuque.com/easyexcel/doc/write#c2bc0689)，引入的是excel包的com.alibaba.excel.annotation.format.DateTimeFormat。  
    &emsp; 也可以实现Converter<T>接口自定义类型转换，本人开发中未使用。  
    2. 项目组多人开发，留意poi的依赖    
    
### 1.14.4. 字典表（树状图/级联关系）  
1. 级联结构，即树状图展示。项目中采用mybatis树状图sql。
2. 树形结构【向下查询】（采用Mybatis构造树）： 
    1. 缓存中放入全量树。  
    2. 增删改，删除缓存中的全量树。  
    3. 查询：首先查询全量树，然后按照parentCode查询子树。  
    4. 编码：DictServiceImpl#findChildsByParentCode()    
3. 树形结构【向上查询】（采用Mybatis构造树）：  
    1. 后台筛选时使用  
    2. 编码：DictServiceImpl#conditionalFilter()  
4. 待优化：采用java构造树。   
5. 拥有数据权限的数据结构：参考面向B端的数据权限一节。  

### 1.14.5. 发送短信邮件  
1. 需求点：
    1. 内容为验证码：  
        1. 随机数不固定位  
        2. 过期时间    
    2. 内容非验证码  
2. 库表设计：发送模板sms_template、发送记录：sms_record（短信记录）、email_record（邮件记录）。     
3. 编码：SMSService.java，使用模板设计模式  
    1. 阿里云发送短信  
    2. 发送邮件：  
        1. 采用授权码方式。  
        &emsp; SpringBoot：https://blog.csdn.net/weixin_46822367/article/details/123893527  
        &emsp; Spring：https://blog.csdn.net/q_qwp_p/article/details/131621962  
        2. ~~在阿里云服务器上实现邮件发送功能，提示25端口无法使用~~ 
        &emsp; https://blog.csdn.net/muyingmiao/article/details/100027729?spm=1001.2101.3001.6650.11&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EOPENSEARCH%7ERate-11-100027729-blog-105774436.pc_relevant_multi_platform_featuressortv2dupreplace&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EOPENSEARCH%7ERate-11-100027729-blog-105774436.pc_relevant_multi_platform_featuressortv2dupreplace&utm_relevant_index=12  
        &emsp; https://blog.csdn.net/qq_36493719/article/details/105774436
        


### 1.14.6. 获取Ip
&emsp; https://mp.weixin.qq.com/s?__biz=MzA4NjgxMjQ5Mg==&mid=2665762376&idx=1&sn=967b663047006d9b994b9c6df10baab0&chksm=84d21c6bb3a5957de6ae2f50f265a8d51641e1771cab645e658b19abe2d5354644ba887fd583&mpshare=1&scene=1&srcid=&sharer_sharetime=1564100760999&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=36a99a852770fa03efd2a584477dd8f6ac3dbbaffc265a85be537eb1c84044e0d27a9829fcc9dc48fbdb5a26f8c1f0dfd9fcedfdfa556701a2f2046f2a586b0fd0b112d53ca231f501b813f6a00e6cf2&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060834&lang=zh_CN&pass_ticket=s18sClyfZbM76kH9b%2BDm2ZIiUkdunIxTxxrp3gvbhsi2bpqzDx36yk0a37PqzHNS  
1. 编码：com.wuw.common.server.util.network.IpUtils
2. nginx配置    


### 解析xml 
dom4jToXml.xml  

## 1.15. Elasticsearch
### 1.15.1. EFK  
1. 多种方案：  
    1. 方案一：FileBeat采集Spring日志  
    2. 方案二：Spring日志直接输出到logstash  
2. FileBeat采集Spring日志  
    1. 日志文件采用json格式。  
    2. FileBeat是直接发送到ELS还是通过LogStash? https://www.it1352.com/1529867.html    
    
### 1.15.2. Elasticsearch使用  
1. Elasticsearch使用场景：https://blog.csdn.net/laoyang360/article/details/52227541    
&emsp; 使用Elasticsearch做一些统计分析。将数据异步（定时job、logStash）同步到ES中。  
2. Elasticsearch建模  
&emsp; 与关系型数据库（如 MySQL）不同的是，ES中尽量考虑非范式化设计，即通过冗余字段，尽可能地消除 join 查询，以提高查询性能，本质上是一种以空间换时间的思想。  
3. SpringBoot集成Elasticsearch  


## 1.16. 替换web服务器  
&emsp; 用Undertow替换Tomcat：https://mp.weixin.qq.com/s/5I9zmCNJB4KXHhSyxOnFDg  
&emsp; spring-cloud-gateway集成了security。未能集成Undertow  


## 1.17. XXXIM通信  
### 1.17.1. 单方面消息推送  
1. https://mp.weixin.qq.com/s/_3uGLngOab7NDtUmNCVXgw  
2. SSE实现方式：SseEmitterUtils.java   


### 1.17.2. 聊天 
1. https://mp.weixin.qq.com/s/2z26QzBHhxBre8yeCPD0iQ  


## 1.18. XXX响应式编程

## 1.19. Nginx
1. Nginx搭建、启动、重启  
    
2. Nginx日志位置：.../nginx/logs    
3. Nginx五大应用场景：https://blog.csdn.net/vbirdbest/article/details/80913319    
    1. 建立conf.d文件夹：https://blog.csdn.net/weixin_43652507/article/details/124123540    
    2. 静态服务器，存储图片、视频、音频。    
    3. 负载  
    4. HTTP服务器  


## 1.20. 监控  
1. 一个监控项目（待调试）：项目，server-monitor；说明，README.md  


## 1.21. 服务重启脚本制作   
1. 使用system  
    1. docker    
2. rc.local中添加sh脚本：  
    1. 脚本文件位置：/usr/work/sh
    2. rc.local添加脚本文件绝对路径  
    3. IdeaServer  


## 1.22. 接口对接  
### 1.22.1. 从Json自动生成JavaBean  
&emsp; GsonFormat的使用：https://www.likecs.com/show-306937306.html  

## 1.23. 用户系统  

### 1.23.1. 注册登录  
#### 1.23.1.1. 注册
1. 注意：多个注册入口，防止一个人注册多次。  
2. 注意：CB端都能注册时，注意顺序影响。C端先注册，B端报错；B端先注册，C端能正常登录。  

#### 1.23.1.2. jwt生成token  
&emsp; 编码JWTUtil.java  

#### 1.23.1.3. 微信授权登录  
1. 微信公众平台的静默授权和网页授权区别详解：http://t.zoukankan.com/jiangzhaowei-p-9792648.html    


#### 1.23.1.4. 微信扫码登录  
1. https://developers.weixin.qq.com/doc/oplatform/Website_App/WeChat_Login/Wechat_Login.html  
2. 前期准备
    1. 登录微信开放平台：https://open.weixin.qq.com/    
    2. 账号中心 -> 开发者资质认证  
    3. 网站应用 -> 创建网站应用  
    4. 获取appId和appSecret
3. 编码：  
    1. 第一步：获取二维码参数  
    2. 第二步：微信回调，返回的是前端微信扫码后的跳转地址。首次登录不返回token，强制绑定手机号。  
    3. 代码：com.wuw.ucenter.server.service.WXService  


### 1.23.2. 权限  
#### 1.23.2.1. 功能权限  


#### 1.23.2.2. 面向B端的数据权限  
0. 数据权限：可以查看哪些机构的数据。  
1. 用户登录将当前用户信息放入请求头中：  
    1. 用户基本信息、功能权限、数据权限放置在http请求头中。  
    2. ServletRequestAttributes的使用（https://blog.csdn.net/m0_37635053/article/details/103969075）   
2. 数据权限两种方案：  
    1. 方案一：(细粒度)将数据权限绑定在用户上。  
    2. 方案二：(粗粒度)将数据权限绑定在角色上。  
3. 项目采用粗粒度，将数据权限绑定在角色上。【需要数据权限的列表页】获取【当前用户拥有权限的机构】：  
    
```java
public class RolePermiResponseVo {
   // 用户id
    private String userBaseId;       
    // 所属机构
    private List<UserManageOffice> userManageOffices;
    // 当前角色
    private Role role;
    // 拥有权限的机构
    private List<String> officeCodes;

}
```

&emsp; 拥有数据权限的查询操作步骤：  
&emsp; 1. 获取用户角色  
&emsp; 2. 获取角色的数据权限  
&emsp; 3. 获取拥有权限的机构编码  
&emsp; 4. 进入正常查询逻辑  
    
4. 机构树的展示：  
    1. 基础：字典表（树状图/级联关系）  
    2. 提供两个接口。接口一返回拥有权限的机构树，从下往上只返回用户拥有的机构树；接口二返回有权限的机构编码；最后由前端进行比对展示。  

## 1.24. 订单  
1. 商品，两级类目：SPU与SKU。    
   1. 概念：https://www.bainiu.com/show/655.html   https://www.xiaohongshu.com/discovery/item/62a555eb000000002103c1a2    
   2. 库表设计：https://q.cnblogs.com/q/137774/ https://blog.csdn.net/lianghecai52171314/article/details/121756100?utm_medium=distribute.pc_aggpage_search_result.none-task-blog-2~aggregatepage~first_rank_ecpm_v1~rank_v31_ecpm-2-121756100-null-null.pc_agg_new_rank&utm_term=%E7%94%B5%E5%95%86sku%E6%95%B0%E6%8D%AE%E5%BA%93&spm=1000.2123.3001.4430  


## 1.25. 支付  
1. 支付系统数据库设计思考：https://blog.csdn.net/luckyzhoustar/article/details/97389504    
2. 第三方支付文档：  
    1. 支付宝支付
        支付宝支付：https://open.alipay.com/  
    2. 微信支付v3  
        1. 目前使用v0.4.7版本，编码WXPayService.java  
        2. 接入前准备：https://pay.weixin.qq.com/wiki/doc/apiv3/open/pay/chapter2_8_1.shtml    
        &emsp; 公众号绑定微信支付简要攻略：https://developers.weixin.qq.com/community/develop/article/doc/0004449a560590f759ed0cbf75d013  
        &emsp; APPID授权管理功能介绍：https://pay.weixin.qq.com/static/pay_setting/appid_protocol.shtml  
        3. 微信开发指引：https://pay.weixin.qq.com/wiki/doc/apiv3/open/pay/chapter2_8_2.shtml 微信java给出的示例：https://github.com/wechatpay-apiv3/wechatpay-apache-httpclient   
        &emsp; 参考文档：https://zhuanlan.zhihu.com/p/464745934  
        4. 问题：  
            1. 解密出现Illegal key size错误：https://developers.weixin.qq.com/community/develop/article/doc/000eae9daa4a8057bd8c3a0a35d813  
            2. 使用官方客户端WechatPayHttpClientBuilder，内部已经封装好了验签信息，即请求头已经添加Authorization参数。  
            &emsp; 签名生成文档：https://pay.weixin.qq.com/wiki/doc/apiv3/wechatpay/wechatpay4_0.shtml  
            3. 定时轮询查单：https://pay.weixin.qq.com/wiki/doc/apiv3/Practices/chapter1_1_1.shtml  
            4. 回调通知注意事项：https://pay.weixin.qq.com/wiki/doc/apiv3/Practices/chapter1_1_5.shtml    
        5. 前端返回商家  
        ![image](/doc/pay/weiXinCall.png)  
3. 分笔支付：一个订单拆分成多个支付单。  


## 1.26. 微信开发  
&emsp; 3个平台：微信开放平台、微信公众平台、服务商平台。    
1. 微信官方文档：https://developers.weixin.qq.com/doc/offiaccount/Getting_Started/Overview.html左上脚下拉选公众号、小程序、开放平台(服务商)、微信支付...    
2. 微信公众号开发之微信服务器配置：http://t.zoukankan.com/shenhaha520-p-10132873.html    

## 1.27. 运维    

### 1.27.1. Kubernetes    
kubeadm 搭建 K8s  

### 1.27.2. Jenkins    
1. Docker安装jdk；查看安装目录：echo $JAVA_HOME  
2. jenkins访问：ip1:8888。账号密码admin。  

