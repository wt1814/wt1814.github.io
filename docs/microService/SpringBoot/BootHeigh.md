
<!-- TOC -->

- [1. SpringBoot高级](#1-springboot高级)
    - [1.1. 自动部署](#11-自动部署)
    - [1.2. 优雅停机](#12-优雅停机)
    - [1.3. 如何关闭自动配置](#13-如何关闭自动配置)
    - [1.4. 慎用devtools](#14-慎用devtools)
    - [1.5. Gzip压缩超大对象](#15-gzip压缩超大对象)

<!-- /TOC -->


# 1. SpringBoot高级  
<!-- 
SpringBoot项目打包部署
打War包：
https://blog.csdn.net/yalishadaa/article/details/70037846
springboot部署web容器SpringBootServletInitializer用途
https://blog.csdn.net/luckyzsion/article/details/81135438

SpringBoot打包部署最佳实践 
https://mp.weixin.qq.com/s?__biz=MzAxNjM2MTk0Ng==&mid=2247490663&idx=2&sn=5df1a2955b35f274ed484503ca7dee6e&chksm=9bf4acd2ac8325c452def387b2879f752deb66a4f3b1ac7fd24b177469e6f1b8cdd39b61d33f&mpshare=1&scene=1&srcid=&sharer_sharetime=1587487683720&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=21572a204414d60cf70767a38b2b2d305ecb90d3ae9b60881f4075fa24024cc78769cee1efabd676bde16534c1e3e124cfe71e26b1142d3379f9a4174b9d0fd6c09e36f20c6a0567e5be3d827e99bfd6&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62080079&lang=zh_CN&exportkey=AQ%2FeoYkinGupA195vydNrK8%3D&pass_ticket=t7WrYQgRWkv7fomJ9tKSvYV9vbBBrtBhylesb1eYH1AGZ3bs%2FIfhN20euL1DBMbi
***我把SpringBoot项目从18.18M瘦身到0.18M，部署起来真省事！ 
https://mp.weixin.qq.com/s/Wu_Yy54GCD2nP_dq9glxug

-->


## 1.1. 自动部署
<!-- 
Spring Boot 五种热部署方式，再也不用老重启了！
https://mp.weixin.qq.com/s/QjQ3OQRaZKpywwN-rOJIjA
Spring Boot Devtools热部署
https://mp.weixin.qq.com/s/WAIUpOJKJPfRRPrrwprnqg
springboot热部署与发布 
https://mp.weixin.qq.com/s?__biz=MzAxNDMwMTMwMw==&mid=2247490374&idx=1&sn=e2dced600ddbc54a075513cdb36c6e94&chksm=9b943a5eace3b348070c5b0c1e5f839bb5905e4cbd7f18bec598c87d09c23fc20eafe064c52a&mpshare=1&scene=1&srcid=#rd
spring-boot-devtools
https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247486119&idx=1&sn=c2cbf2e7f737fba902f6ebb25dabaefe&chksm=e9c358c7deb4d1d1305abd486554f77af7b35dae40b4f7fb93c109a373f250c57183e5899529&mpshare=1&scene=1&srcid=&sharer_sharetime=1565223561476&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=a98b434d6faae616630cab2be35f66a1cd08b3d2cdf397199616001aa19206f932e291abae2a2db270188835ea14446ce811f500d69d4d4b7e33714ff6b34a88306091699e7b4bb65f0dc1bb1cb83765&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=DfHn1sowEuE1UliJrfbichZ%2FKeZSJTa%2BYpgPG1cg7FwuiEd4YkpK9igSvNhEJrj3




-->

其他项目：  

```xml
<profile>
    <id>dev</id>
    <properties>
        <env>dev</env>
    </properties>
    <activation>
        <activeByDefault>true</activeByDefault>
    </activation>
    <dependencies>
        <!-- 热启动 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
        </dependency>
    </dependencies>
</profile>
```


## 1.2. 优雅停机  
<!-- 

boot优雅停机
https://mp.weixin.qq.com/s/SM2m8yfhMRGbXCB5eSoD9A
https://blog.csdn.net/qq276726581/article/details/55520762
https://blog.csdn.net/XlxfyzsFdblj/article/details/82054744
https://blog.csdn.net/nihao12323432/article/details/81205288
https://mp.weixin.qq.com/s?__biz=MzAxODcyNjEzNQ==&mid=2247487908&idx=2&sn=4773745275bd508103bc491480f0578d&chksm=9bd0bc3caca7352a978efa37b507603fc9ff67b585a2d8f37032b1335b3a877b00a17c8e1441&mpshare=1&scene=1&srcid=&sharer_sharetime=1564291575682&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=dd204f3b2a2710eddd25f8bb7f2d53dc35cb3419d6c5c6baa7e1961bef9a95b41a48320fd1a20d651c09224f5987b107462a282962034c529a1a50376a97f15aade63319b9fad090269345a341c9839f&ascene=1&uin=MTE1MTYxNzY2MQ==&devicetype=Windows+10&version=62060834&lang=zh_CN&pass_ticket=sz+/OSGoJ6z2kumEYXYxJXv9g+Xf3u0myHeKIrKL+1FA1hYcn4C7JGEcNUehxMVC
-->


## 1.3. 如何关闭自动配置  
<!-- 

https://www.jb51.net/article/222489.htm
http://www.zzvips.com/article/215505.html
-->

## 1.4. 慎用devtools
SpringBoot使用devtools导致的类型转换异常  
<!-- 

https://blog.csdn.net/m0_38043362/article/details/78064539
-->


## 1.5. Gzip压缩超大对象   
<!-- 

Springboot 之 Filter 实现 Gzip 压缩超大 json 对象
https://mp.weixin.qq.com/s/CrlS0thKzjPbclYL50Mj0A

Springboot 之 Filter 实现超大响应 JSON 数据压缩
https://mp.weixin.qq.com/s/oyUDJMDajROihpBJSHLK9Q

-->  
