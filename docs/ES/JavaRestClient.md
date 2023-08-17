
<!-- TOC -->

- [1. JavaRestClient](#1-javarestclient)
    - [1.1. Java API介绍](#11-java-api介绍)
    - [1.2. SpringBoot集成ElasticSearch](#12-springboot集成elasticsearch)
        - [1.2.1. spring-boot-starter-data-elasticsearch](#121-spring-boot-starter-data-elasticsearch)
        - [1.2.2. elasticsearch-rest-high-level-client](#122-elasticsearch-rest-high-level-client)

<!-- /TOC -->

# 1. JavaRestClient  
<!-- 


****  springboot集成elasticsearch
*** https://www.cnblogs.com/tanghaorong/p/16344391.html
https://blog.csdn.net/u010833154/article/details/123919226

-->


## 1.1. Java API介绍
&emsp; ElasticSearch提供了官方的Java Api。这里包括两类：Low Level Rest Api(低级Rest Api)和 High Leve Rest Api(高级Rest Api)。  

* 低级 Api 并不是功能比较弱，而是指 Api 离底层实现比较近。官方提供的低级Api是对原始的 Rest Api 的第一层封装。只是把 Http 调用的细节封装起来。程序还是要自己组装查询的条件字符串、解析返回的结果 json 字符串等。同时也要处理http协议的各种方法、协议头等内容。  
* 高级 Api 是在低级 Api 上的进一步封装，不用在意接口的方法，协议头，也不用人工组合调用的参数字符串，同时对返回的 json 字符串有一定的解析。使用上更方便一些。但是高级 api 并没有实现所有低级 api 实现的功能。所以如果遇到这种情况，还需要利用低级 api 来实现自己功能。  

&emsp; 在项目中推荐使用elasticsearch-rest-high-level-client。  

```xml
<dependency>
    <groupId>org.elasticsearch.client</groupId>
    <artifactId>elasticsearch-rest-high-level-client</artifactId>
    <version>6.5.4</version>
</dependency>
```

## 1.2. SpringBoot集成ElasticSearch
<!-- 
SpringBoot整合ES
https://mp.weixin.qq.com/s/R0PNWF7NmFfjnFljIKG6Vg
-->

### 1.2.1. spring-boot-starter-data-elasticsearch  
......

### 1.2.2. elasticsearch-rest-high-level-client

<!-- 

https://blog.csdn.net/qq_27088383/article/details/107131901

https://blog.csdn.net/jacksonary/article/details/82729556?utm_medium=distribute.pc_relevant.none-task-blog-BlogCommendFromBaidu-3.not_use_machine_learn_pai&depth_1-utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromBaidu-3.not_use_machine_learn_pai

https://www.cnblogs.com/suruozhong/p/12190898.html

-->
