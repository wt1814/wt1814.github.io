
<!-- TOC -->

- [1. 自定义springboot-starter](#1-自定义springboot-starter)
    - [1.1. 场景](#11-场景)
    - [1.2. 开发](#12-开发)
        - [1.2.1. 第一步，创建maven项目](#121-第一步创建maven项目)
        - [1.2.2. 第二步，写自动配置逻辑](#122-第二步写自动配置逻辑)
        - [1.2.3. 第三步，应用加载starter的配置](#123-第三步应用加载starter的配置)
        - [1.2.4. 第四步，打包测试](#124-第四步打包测试)

<!-- /TOC -->


# 1. 自定义springboot-starter
&emsp; 通过配置文件定义自动配置开关：  
1. 编写业务逻辑。  
2. 定义配置文件对应类。  
    * @ConfigurationProperties配置属性文件，需要指定前缀prefix。
    * @EnableConfigurationProperties启用配置，需要指定启用的配置类。
    * @NestedConfigurationProperty当一个类中引用了外部类，需要在该属性上加该注解。
3. 定义自动配置类，该类自动暴露功能接口。  
    * @Configuration注解。
    * @EnableConfigurationProperties注解。该注解是用来开启对配置文件对应类中@ConfigurationProperties注解配置Bean的支持。也就是@EnableConfigurationProperties注解告诉Spring Boot能支持@ConfigurationProperties。 当然了，也可以在 @ConfigurationProperties注解的类上添加@Configuration或者@Component注解。
    * @ConditionalOnProperty注解控制@Configuration是否生效。简单来说也就是可以通过在yml配置文件中控制@Configuration注解的配置类是否生效。

## 1.1. 场景  
<!-- 
https://www.jianshu.com/p/2fd2b5a51227
-->
&emsp; 自定义starter使项目组件化、模块化。  

## 1.2. 开发  
<!-- 
https://mp.weixin.qq.com/s/O4Gni_c725p-qh9Zs7JmfA
https://mp.weixin.qq.com/s/xG8WCmmm0aIqy2Kmrxn2sg
https://www.cnblogs.com/hello-shf/p/10864977.html
-->

### 1.2.1. 第一步，创建maven项目  

1. 命名潜规则  
&emsp; spring-boot-starter-XX是springboot官方的starter；XX-spring-boot-starter是第三方扩展的starter。

2. 项目pom文件  

    ```xml
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-autoconfigure</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>
    ```
    &emsp; 关于spring-boot-configuration-processor的说明：  
    &emsp; 写starter时，在pom中配置spring-boot-autoconfigure-processor，在编译时会自动收集配置类的条件，写到META-INF/spring-autoconfigure-metadata.properties中。

### 1.2.2. 第二步，写自动配置逻辑  
1. 编写业务逻辑  
2. 定义配置文件对应类  
    * @ConfigurationProperties 配置属性文件，需要指定前缀 prefix。
    * @EnableConfigurationProperties 启用配置，需要指定启用的配置类。
    * @NestedConfigurationProperty 当一个类中引用了外部类，需要在该属性上加该注解。
3. 定义自动配置类  
&emsp; 自动配置类自动暴露功能接口。  

    ```java
    @ConditionalOnProperty(prefix = "xxxxx", name = "enable",havingValue = "true", matchIfMissing = true)
    ```

    &emsp; matchIfMissing：在matchIfMissing为false时，如果name值为空，则返回false；如果name不为空，则将该值与havingValue指定的值进行比较，如果一样则返回true，否则返回false。返回false也就意味着自动配置不会生效。  

    &emsp; ~~各种condition注解：~~  

    |类型|注解|说明|
    |---|---|---|
    |Class| Conditions类条件注解|@ConditionalOnClass当前classpath下有指定类才加载|
    |@ConditionalOnMissingClass	|当前classpath下无指定类才加载|  |
    |Bean ConditionsBean条件注解|@ConditionalOnBean|当期容器内有指定bean才加载|
    |@ConditionalOnMissingBean|当期容器内无指定bean才加载|  |
    |Property Conditions环境变量条件注解（含配置文件）|	@ConditionalOnProperty|	prefix 前缀name 名称havingValue 用于匹配配置项值matchIfMissing 没找指定配置项时的默认值|
    |ResourceConditions 资源条件注解|@ConditionalOnResource	|有指定资源才加载|
    |Web Application Conditionsweb条件注解|	@ConditionalOnWebApplication|是web才加载|
    |@ConditionalOnNotWebApplication|不是web才加载	| |
    |SpEL Expression Conditions	|@ConditionalOnExpression|符合SpEL 表达式才加载|

### 1.2.3. 第三步，应用加载starter的配置  

&emsp; 外部应用加载自定义的starter有两种方式：主动加载、被动加载。  

1. 被动加载  
&emsp; 使用的SpringBoot的SPI机制。在resource资源包下新建META-INF/spring.factories写入自动配置类全限定名。  

    ```properties
    org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
    xxxx.xxxx.xxxx
    ```
2. 主动加载使用@Import注解。

### 1.2.4. 第四步，打包测试  
&emsp; mvn intall，在其他项目中的pom中引入进行测试。
