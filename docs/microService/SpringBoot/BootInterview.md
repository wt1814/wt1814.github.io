

# SpringBoot常见面试题 

## SpringBoot有哪些优点?  
&emsp; 减少开发，减少测试时间。  
&emsp; 使用JavaConfig有助于避免使用XML。  
&emsp; 避免大量的Maven导入和各种版本冲突。  
&emsp; 提供意见发展方法。   
&emsp; 通过提供默认值快速开始开发。    
&emsp; 没有单独的Web服务器需要。这意味着你不再需要启动Tomcat，Glassfish或其他任何东西。  
&emsp; 需要更少的配置 因为没有web.xml文件。只需添加用@ Configuration注释的类，然后添加用@Bean注释的方法，Spring将自动加载对象并像以前一样对其进行管理。您甚至可以将@Autowired添加到bean方法中，以使Spring自动装入需要的依赖关系中。   


## SpringBoot常用注解  
<!-- 

https://baijiahao.baidu.com/s?id=1713045744318175557&wfr=spider&for=pc
-->

1. Spring Boot注解
    1. @SpringBootApplication 是 @Configuration、@EnableAutoConfiguration、@ComponentScan 注解的集合。  
    &emsp; 根据 SpringBoot 官网，这三个注解的作用分别是：    
    &emsp; @EnableAutoConfiguration：启用 SpringBoot 的自动配置机制    
    &emsp; @ComponentScan： 扫描被@Component (@Service,@Controller)注解的 bean，注解默认会扫描该类所在的包下所有的类。  
    &emsp; @Configuration：允许在 Spring 上下文中注册额外的 bean 或导入其他配置类   

    2. @EnableAutoConfiguration  
    &emsp; @EnableAutoConfiguration注解用于通知Spring，根据当前类路径下引入的依赖包，自动配置与这些依赖包相关的配置项。   

    3. @ConditionalOnClass与@ConditionalOnMissingClass

2. Spring Web MVC注解
    2. @CrossOrigin  

3. Spring Bean注解  
