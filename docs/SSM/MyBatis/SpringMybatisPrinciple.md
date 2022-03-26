
<!-- TOC -->

- [1. Spring整合MyBatis原理](#1-spring整合mybatis原理)
    - [1.1. 创建SqlSessionFacory](#11-创建sqlsessionfacory)
    - [1.2. 创建SqlSession](#12-创建sqlsession)
    - [1.3. 接口的扫描注册](#13-接口的扫描注册)
    - [1.4. 接口注入使用](#14-接口注入使用)

<!-- /TOC -->

# 1. Spring整合MyBatis原理  
&emsp; Spring整合MyBatis并不会对MyBatis内部进行改造，只会进行集成，对其实现进行了包装。  
&emsp; **MyBatis运行原理：**  
1. 创建SqlSessionFacory；
2. 从SqlSessionFactory对象中获取SqlSession对象；
3. 获取Mapper；
4. 执行操作；

## 1.1. 创建SqlSessionFacory  
&emsp; <font color = "red">MyBatis-Spring中创建SqlSessionFacory是由SqlSessionFactoryBean完成的。</font>  
![image](http://www.wt1814.com/static/view/images/SSM/Mybatis/mybatis-24.png)  

* InitializingBean接口：实现了这个接口，那么当bean初始化的时候，spring就会调用该接口的实现类的afterPropertiesSet方法，去实现当spring初始化该Bean的时候所需要的逻辑。afterPropertiesSet()会在 bean 的属性值设置完的时候被调用。  
* FactoryBean接口：实现了该接口的类，在调用getBean进行实例化的时候会返回该工厂返回的实例对象。实际上调用的是getObject()方法，getObject() 方法里面调用的也是 afterPropertiesSet()方法。  
* ApplicationListener接口：实现了该接口，如果注册了该监听的话，那么就可以了监听到Spring的一些事件，然后做相应的处理。  

&emsp; SqlSessionFactoryBean#afterPropertiesSet()方法：  

```java
@Override
//在spring容器中创建全局唯一的sqlSessionFactory
public void afterPropertiesSet() throws Exception {
    notNull(dataSource, "Property 'dataSource' is required");
    notNull(sqlSessionFactoryBuilder, "Property 'sqlSessionFactoryBuilder' is required");
    state((configuration == null && configLocation == null) || !(configuration != null && configLocation != null),
            "Property 'configuration' and 'configLocation' can not specified with together");

    this.sqlSessionFactory = buildSqlSessionFactory();
}
```
&emsp; buildSqlSessionFactory()方法会对sqlSessionFactory做定制的初始化，初始化sqlSessionFactory有两种方式，一种是直接通过property直接注入到该实例中，另一种是通过解析xml的方式，就是在configuration.xml里面的配置，根据这些配置做了相应的初始化操作，里面也是一些标签的解析属性的获取，操作，和Spring的默认标签解析有点类似。  

## 1.2. 创建SqlSession  
&emsp; 在Spring中并没有直接使用DefaultSqlSession。DefaultSqlSession是线程不安全的，注意看类上的注解：  

    Note that this class is not Thread-Safe. 

&emsp; <font color = "red">Spring对SqlSession 进行了一个封装，这个SqlSession的实现类就是SqlSessionTemplate。</font>SqlSessionTemplate是线程安全的。SqlSessionTemplate的获取是在mapper接口注入流程中。 

&emsp; 在编程式的开发中，SqlSession会在每次请求的时候创建一个，但是 Spring 里面只有一个 SqlSessionTemplate（默认是单例的），多个线程同时调用的时候怎么保证线程安全？  
&emsp; <font color = "red">SqlSessionTemplate通过动态代理的方式来保证DefaultSqlSession操作的线程安全性。</font> SqlSessionTemplate 里面有 DefaultSqlSession 的所有的方法：selectOne()、 selectList()、insert()、update()、delete()，不过它都是通过一个代理对象实现的。这个代理对象在构造方法里面通过一个代理类创建：    

```java
this.sqlSessionProxy = (SqlSession) newProxyInstance( SqlSessionFactory.class.getClassLoader(), new Class[] { SqlSession.class }, new SqlSessionInterceptor());
```

## 1.3. 接口的扫描注册  
&emsp; 获取Mapper接口。在Service层可以使用@Autowired自动注入的Mapper接口，需要保存在 BeanFactory（比如 XmlWebApplicationContext）中。也就是说接口是在 Spring 启动的时候，会被扫描、注册。     
&emsp; 扫描注册Mapper接口是在 applicationContext.xml里面配置了MapperScannerConfigurer或者使用注解@MapperScan完成的。  
![image](http://www.wt1814.com/static/view/images/SSM/Mybatis/mybatis-25.png)  
&emsp; <font color = "red">MapperScannerConfigurer 实现了BeanDefinitionRegistryPostProcessor接口， BeanDefinitionRegistryPostProcessor 是 BeanFactoryPostProcessor的子类。</font>  

&emsp; MapperScannerConfigurer#postProcessBeanDefinitionRegistry()方法：  

```java
@Override
public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
    if (this.processPropertyPlaceHolders) {//占位符处理
        processPropertyPlaceHolders();
    }
    //实例化ClassPathMapperScanner，并对scanner相关属性进行配置
    ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);
    scanner.setAddToConfig(this.addToConfig);
    scanner.setAnnotationClass(this.annotationClass);
    scanner.setMarkerInterface(this.markerInterface);
    scanner.setSqlSessionFactory(this.sqlSessionFactory);
    scanner.setSqlSessionTemplate(this.sqlSessionTemplate);
    scanner.setSqlSessionFactoryBeanName(this.sqlSessionFactoryBeanName);
    scanner.setSqlSessionTemplateBeanName(this.sqlSessionTemplateBeanName);
    scanner.setResourceLoader(this.applicationContext);
    scanner.setBeanNameGenerator(this.nameGenerator);
    scanner.registerFilters();//根据上述配置，生成过滤器，只扫描合条件的class
    //扫描指定的包以及其子包
    scanner.scan(StringUtils.tokenizeToStringArray(this.basePackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
}
```
&emsp; 这个方法中创建了一个ClassPathMapperScanner扫描器，这个扫描器继承了Spring的ClassPathBeanDefinitionScanner。  
![image](http://www.wt1814.com/static/view/images/SSM/Mybatis/mybatis-27.png)  

&emsp; ClassPathMapperScanner这个扫描器的主要的作用有以下几个：  
&emsp; 第一扫描basePackage包下面所有的class类。  
&emsp; 第二将所有的class类封装成为spring的ScannedGenericBeanDefinition sbd对象。  
![image](http://www.wt1814.com/static/view/images/SSM/Mybatis/mybatis-28.png)  
&emsp; 第三过滤sbd对象，只接受接口类，从下面的代码中可以看出。  

```java
protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
    return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
}
```
&emsp; 第四完成sbd对象属性的设置，比如设置sqlSessionFactory、BeanClass等。  

```java
sbd.getPropertyValues().add("mapperInterface", definition.getBeanClassName());
sbd.setBeanClass(MapperFactoryBean.class);
sbd.getPropertyValues().add("sqlSessionFactory", this.sqlSessionFactory);
```
&emsp; 第五将过滤出来的sbd对象通过这个BeanDefinitionRegistry registry注册器注册到DefaultListableBeanFactory中，这个registry就是方法postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)中的参数。  

&emsp; 以上就是实例化MapperScannerConfigurer类的主要工作，总结起来就是扫描basePackage包下所有的mapper接口类，并将mapper接口类封装成为BeanDefinition对象，注册到spring的BeanFactory容器中。  

## 1.4. 接口注入使用  
&emsp; 使用 Mapper 的时候，只需要在加了 Service 注解的类里面使用@Autowired 注入 Mapper 接口就可以了。  

```java
@Service 
public class EmployeeService { 
    @Autowired EmployeeMapper employeeMapper; 
    
    public List<Employee> getAll() { 
        return employeeMapper.selectByMap(null); 
    } 
}
```
&emsp; Spring在启动的时候需要去实例化EmployeeService。EmployeeService依赖了EmployeeMapper接口（是EmployeeService的一个属性）。  
&emsp; Spring会根据Mapper的名字从BeanFactory中获取它的BeanDefination，再从BeanDefination中获取BeanClass， EmployeeMapper对应的BeanClass是MapperFactoryBean。   
&emsp; 接下来就是创建 MapperFactoryBean，因为实现了 FactoryBean 接口，同样是调用getObject()方法。  

```java
// MapperFactoryBean.java 
public T getObject() throws Exception { 
    return getSqlSession().getMapper(this.mapperInterface); 
}
```
&emsp; 因为MapperFactoryBean继承了SqlSessionDaoSupport ，所以这个getSqlSession()就是调用父类的方法，返回SqlSessionTemplate。  

```java
// SqlSessionDaoSupport.java 
public SqlSession getSqlSession() { 
    return this.sqlSessionTemplate; 
}
```

&emsp; SqlSessionTemplate 的 getMapper()方法，里面又有两个方法：  

```java
// SqlSessionTemplate.java 
public <T> T getMapper(Class<T> type) { 
    return getConfiguration().getMapper(type, this); 
}
```
&emsp; 第一步：SqlSessionTemplate 的 getConfiguration()方法：  

```java
// SqlSessionTemplate.java 
public Configuration getConfiguration() { 
    return this.sqlSessionFactory.getConfiguration(); 
}
```
&emsp; 进入方法，通过 DefaultSqlSessionFactory，返回全部配置 Configuration：  

```java
// DefaultSqlSessionFactory.java 
public Configuration getConfiguration() { 
    return configuration; 
}
```
&emsp; 第二步：Configuration 的 getMapper()方法：  

```java
// Configuration.java 
public <T> T getMapper(Class<T> type, SqlSession sqlSession) { 
    return mapperRegistry.getMapper(type, sqlSession); 
}
```
&emsp; 这 一 步 跟 编 程 式 使 用 里 面 的 getMapper 一 样 ， 通 过 工 厂 类 MapperProxyFactory 获得一个 MapperProxy 代理对象。  

&emsp; 也就是说，注入到 Service 层的接口，实际上还是一个 MapperProxy 代理对象。 所以最后调用Mapper接口的方法，也是执行 MapperProxy 的 invoke()方法，后面的 流程就跟编程式的工程里面一模一样了。  
