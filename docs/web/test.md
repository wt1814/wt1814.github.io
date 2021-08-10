
<!-- TOC -->

- [1. SpringTest](#1-springtest)
    - [1.1. Test框架介绍](#11-test框架介绍)
        - [1.1.1. Junit4与Spring Test](#111-junit4与spring-test)
        - [1.1.2. 常用注解](#112-常用注解)
            - [1.1.2.1. 类注解](#1121-类注解)
            - [1.1.2.2. 方法注解](#1122-方法注解)
    - [1.2. Spring项目](#12-spring项目)
    - [1.3. SpringBoot项目](#13-springboot项目)

<!-- /TOC -->

# 1. SpringTest
<!-- 
spring framework test3种方式
https://blog.csdn.net/yejingtao703/article/details/77545300

spring classPath加载配置多个
https://blog.csdn.net/ojackhao/article/details/52624143

单元测试框架怎么搭？快来看看新版Junit5的这些神奇之处吧！ 
https://mp.weixin.qq.com/s/2gseNJ00Yh66Uxso8pYJXQ

https://blog.csdn.net/yejingtao703/article/details/77545300

@ContextConfiguration此注解使用在Class上，声明测试使用的配置文件，此外，也可以指定加载上下文的类。
此注解一般需要搭配SpringJUnit4ClassRunner使用。
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringCoreConfig.class)
public class UserServiceTest {

}


@RunWith 运行器，Spring中通常用于对JUnit的支持

@RunWith(SpringJUnit4ClassRunner.class)

@ContextConfiguration 用来加载配置ApplicationContext，其中classes属性用来加载配置类

@ContextConfiguration(classes={TestConfig.class})
-->

## 1.1. Test框架介绍
### 1.1.1. Junit4与Spring Test
&emsp; JUnit是一个Java语言的单元测试框架。SpringTest集成了Junit。  

### 1.1.2. 常用注解
#### 1.1.2.1. 类注解
&emsp; @RunWith(SpringJUnit4ClassRunner.class)：表示使用自己定制的Junit4.5+运行器来运行测试，即完成Spring TestContext框架与Junit集成；  
&emsp; @RunWith(SpringRunner.class)：表示要进行spring的全部流程启动单元测试，即类似于spring的正常启动启动服务。  
&emsp; @SpringBootTest(classes=com.mydemo.Application.class)  
&emsp; @ContextConfiguration：指定要加载的Spring配置文件，Spring资源配置文件为“applicationContext-resources-test.xml”；  
&emsp; @ContextConfiguration(classes=com.mydemo.Application.class)//此注解只适用不依赖于环境配置的单元测试。  
&emsp; @ContextConfiguration是纯基于代码的测试环境，不会加载application.properties等环境信息，适用与配置无关的算法测试。  
&emsp; @TransactionConfiguration：开启测试类的事务管理支持配置，并指定事务管理器和默认回滚行为；  
&emsp; @DirtiesContext：@DirtiesContext 表示测试上下文会影响到应用中的上下文，当测试执行后重新加载应用的上下文。  

#### 1.1.2.2. 方法注解
&emsp; @Before注解：与junit3.x中的setUp()方法功能一样，在每个测试方法之前执行；  
&emsp; @After注解：与junit3.x中的tearDown()方法功能一样，在每个测试方法之后执行；  
&emsp; @BeforeClass 注解：在所有方法执行之前执行；  
&emsp; @AfterClass 注解：在所有方法执行之后执行；  
&emsp; @Test(timeout = xxx) 注解：设置当前测试方法在一定时间内运行完，否则返回错误；  
&emsp; @Test(expected = Exception.class) 注解：设置被测试的方法是否有异常抛出。抛出异常类型为：Exception.class；  
&emsp; @Ignore 注解：注释掉一个测试方法或一个类，被注释的方法或类，不会被执行。  

## 1.2. Spring项目
&emsp; JUnit依赖  

```xml
<!-- ============== test begin ============== -->
<dependency><!-- JUnit单元测试框架 -->
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>4.12</version>
    <scope>test</scope>
</dependency>
<dependency><!-- spring对测试框架的简单封装功能 -->
    <groupId>org.springframework</groupId>
    <artifactId>spring-test</artifactId>
    <version>${spring.version}</version>
    <scope>test</scope>
</dependency>
<!-- ============== test end ============== -->
```
```java
import javax.annotation.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import test.demo.dao.UserDAO;
import test.demo.data.User;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath*:applicationContext.xml"})
@DirtiesContext
public class SpringTestUser {
    private static final Logger logger = LoggerFactory.getLogger(SpringTestUser.class);

    @Resource
    private UserDAO userDao;

    @Test
    public void testGetUserById() {
        Integer userId = 1;
        User user = userDao.getUserById(userId);
        logger.info("用户的名字是 : " + user.getName());
    }
}
```
&emsp; 注解ContextConfiguration({"classpath*:applicationContext.xml"})、@ContextConfiguration(classes = MyLoggerServiceApplication.class)
以标签的形式完成对配置文件的加载。  

## 1.3. SpringBoot项目
&emsp; spring-boot-starter-test  

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
</dependency>
```

```java
@RunWith(SpringRunner.class)
@SpringBootTest(classes = StartUpApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HelloControllerTest {

/**
 * @LocalServerPort 提供了 @Value("${local.server.port}") 的代替
 */
@LocalServerPort
private int port;
private URL base;

@Autowired
private TestRestTemplate restTemplate;

@Before
public void setUp() throws Exception {
    String url = String.format("http://localhost:%d/", port);
    System.out.println(String.format("port is : [%d]", port));
    this.base = new URL(url);
}

/**
 * 向"/test"地址发送请求，并打印返回结果
 * @throws Exception
 */
@Test
public void test1() throws Exception {

    ResponseEntity<String> response = this.restTemplate.getForEntity(
            this.base.toString() + "/test", String.class, "");
    System.out.println(String.format("测试结果为：%s", response.getBody()));
}
```
&emsp; 注解@SpringBootTest，classes属性指定启动类，SpringBootTest.WebEnvironment.RANDOM_PORT经常和测试类中@LocalServerPort一起在注入属性时使用，会随机生成一个端口号。两个属性值可选，非必须。  
