<!-- TOC -->

- [1. Mybatis架构](#1-mybatis架构)
    - [1.1. Mybatis工作流程概述](#11-mybatis工作流程概述)
    - [1.2. MyBatis架构分层与模块划分](#12-mybatis架构分层与模块划分)
        - [1.2.1. API接口层](#121-api接口层)
        - [1.2.2. 核心处理层](#122-核心处理层)
        - [1.2.3. 基础支持层](#123-基础支持层)

<!-- /TOC -->

# 1. Mybatis架构
## 1.1. Mybatis工作流程概述  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Mybatis/mybatis-13.png)  
&emsp; Mybatis工作流程概述：  
1. 读取核心配置文件并返回InputStream流对象。
2. 根据InputStream流对象解析出Configuration对象，然后创建SqlSessionFactory工厂对象。
3. 根据一系列属性从SqlSessionFactory工厂中创建SqlSession。
4. 从SqlSession中调用Executor执行数据库操作和生成具体SQL指令。
5. 对执行结果进行二次封装。
6. 提交与事务。

## 1.2. MyBatis架构分层与模块划分  
&emsp; 在 MyBatis 的主要工作流程里，不同的功能是由很多不同的类协作完成的，它们分布在MyBatis jar包的不同的 package里面。  
&emsp; MyBatis(基于3.5.1)jar 包结构（21 个包）：  

    └── org  
        └── apache   
            └── ibatis 
                ├── annotations 
                ├── binding 
                ├── builder 
                ├── cache 
                ├── cursor 
                ├── datasource 
                ├── exceptions 
                ├── executor 
                ├── io 
                ├── javassist 
                ├── jdbc 
                ├── lang 
                ├── logging 
                ├── mapping 
                ├── ognl 
                ├── parsing 
                ├── plugin 
                ├── reflection 
                ├── scripting 
                ├── session 
                ├── transaction 
                └── type   

&emsp; MyBatis按照功能职责的不同，所有的package可以分成不同的工作层次。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Mybatis/mybatis-14.png)  
&emsp; Mybatis的功能架构分为三层：  

* API接口层：提供给外部使用的接口API，开发人员通过这些本地API来操纵数据库。接口层一接收到调用请求就会调用核心处理层来完成具体的数据处理。  
* 核心处理层：负责具体的SQL查找、SQL解析、SQL执行和执行结果映射处理等。它主要的目的是根据调用的请求完成一次数据库操作。  
* 基础支持层：负责最基础的功能支撑，包括连接管理、事务管理、配置加载和缓存处理，这些都是共用的东西，将它们抽取出来作为最基础的组件。为上层的数据处理层提供最基础的支撑。  

### 1.2.1. API接口层  
&emsp; 在不与Spring 集成的情况下，使用 MyBatis 执行数据库的操作主要如下：  

```java
InputStream is = Resources.getResourceAsStream("myBatis-config.xml");
SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
SqlSessionFactory factory = builder.build(is);
sqlSession = factory.openSession();
```
&emsp; 其中的SqlSessionFactory，SqlSession是 MyBatis 接口的核心类。SqlSession是上层应用和 MyBatis 打交道的桥梁，SqlSession 上定义了非常多的对数据库的操作方法。  
&emsp; 接口层在接收到调用请求的时候，会调用核心处理层的相应模块来完成具体的数据库操作。  

### 1.2.2. 核心处理层  
&emsp; 核心处理层功能如下：  

* 配置解析  
&emsp; 在 Mybatis 初始化过程中，会加载 mybatis-config.xml 配置文件、映射配置文件以及 Mapper 接口中的注解信息，解析后的配置信息会形成相应的对象并保存到 Configration 对象中。之后，根据该对象创建SqlSessionFactory 对象。待 Mybatis 初始化完成后，可以通过 SqlSessionFactory 创建 SqlSession 对象并开始数据库操作。  
* SQL 解析与 scripting 模块  
&emsp; Mybatis 实现的动态 SQL 语句，几乎可以编写出所有满足需要的 SQL。  
&emsp; Mybatis 中 scripting 模块会根据用户传入的参数，解析映射文件中定义的动态 SQL 节点，形成数据库能执行的SQL 语句。  
* SQL 执行  
&emsp; 执行 SQL 语句；处理结果集，并映射成 Java 对象。  
&emsp; <font color = "red">SQL语句的执行涉及多个组件Configuration 、 SqlSessionFactory 、 Session 、 Executor 、 MappedStatement 、StatementHandler、ResultSetHandler。</font> **<font color = "lime">包括 MyBatis 的四大核心，它们是: Executor、StatementHandler、ParameterHandler、ResultSetHandler。</font>**  

    |名称 |意义 |
    |---|---|
    |Configuration |管理 mybatis-config.xml 全局配置关系类 |
    |SqlSessionFactory |Session 管理工厂接口 |
    |Session |SqlSession 是一个面向用户（程序员）的接口。SqlSession 中提供了很多操作数据库的方法 |
    |Executor |执行器是一个接口（基本执行器、缓存执行器）。作用：SqlSession 内部通过执行器操作数据库 |
    |MappedStatement |底层封装对象。作用：对操作数据库存储封装，包括 sql 语句、输入输出参数 
    |StatementHandler |具体操作数据库相关的 handler 接口| 
    |ResultSetHandler |具体操作数据库返回结果的 handler 接口|

<!-- 
MyBatis 层级结构各个组件的介绍(这里只是简单介绍，具体介绍在后面)：

    SqlSession：，它是 MyBatis 核心 API，主要用来执行命令，获取映射，管理事务。接收开发人员提供 Statement Id 和参数。并返回操作结果。
    Executor ：执行器，是 MyBatis 调度的核心，负责 SQL 语句的生成以及查询缓存的维护。
    StatementHandler :  封装了JDBC Statement 操作，负责对 JDBC Statement 的操作，如设置参数、将Statement 结果集转换成 List 集合。
    ParameterHandler :  负责对用户传递的参数转换成 JDBC Statement 所需要的参数。
    ResultSetHandler : 负责将 JDBC 返回的 ResultSet 结果集对象转换成 List 类型的集合。
    TypeHandler :  用于 Java 类型和 JDBC 类型之间的转换。
    MappedStatement : 动态 SQL 的封装
    SqlSource :  表示从 XML 文件或注释读取的映射语句的内容，它创建将从用户接收的输入参数传递给数据库的 SQL。
Configuration:  MyBatis 所有的配置信息都维持在 Configuration 对象之中。
-->


&emsp; <font color= "red">插件也属于核心层，这是由它的工作方式和拦截的对象决定的。</font>   

### 1.2.3. 基础支持层  
&emsp; 最后一个就是基础支持层。基础支持层主要是一些抽取出来的通用的功能（实现复用），用来支持核心处理层的功能。比如数据源、缓存、日志、xml 解析、反射、IO、事务等等这些功能。  

* 反射模块  
&emsp; Mybatis 中的反射模块，对 Java 反射进行了很好的封装，提供了简易的 API，方便上层调用，并且对反射操作进行了一系列的优化，比如，缓存了类的元数据（MetaClass）和对象的元数据（MetaObject），提高了反射操作的性能。  
* 类型转换模块  
&emsp; Mybatis 的别名机制，能够简化配置文件，该机制是类型转换模块的主要功能之一。类型转换模块的另一个功能是实现 JDBC 类型与 Java 类型的转换。在 SQL 语句绑定参数时，会将数据由 Java 类型转换成 JDBC 类型；在映射结果集时，会将数据由 JDBC 类型转换成 Java 类型。  
* 日志模块  
&emsp; 在 Java 中，有很多优秀的日志框架，如 Log4j、Log4j2、slf4j 等。Mybatis 除了提供了详细的日志输出信息，还能够集成多种日志框架，其日志模块的主要功能就是集成第三方日志框架。  
* 资源加载模块  
&emsp; 该模块主要封装了类加载器，确定了类加载器的使用顺序，并提供了加载类文件和其它资源文件的功能。  
* 解析器模块  
&emsp; 该模块有两个主要功能：一个是封装了 XPath，为 Mybatis 初始化时解析 mybatis-config.xml配置文件以及映射配置文件提供支持；另一个为处理动态 SQL 语句中的占位符提供支持。  
* 数据源模块  
&emsp; Mybatis 自身提供了相应的数据源实现，也提供了与第三方数据源集成的接口。数据源是开发中的常用组件之一，很多开源的数据源都提供了丰富的功能，如连接池、检测连接状态等，选择性能优秀的数据源组件，对于提供ORM 框架以及整个应用的性能都是非常重要的。  
* 事务管理模块  
&emsp; 一般地，Mybatis 与 Spring 框架集成，由 Spring 框架管理事务。但 Mybatis 自身对数据库事务进行了抽象，提供了相应的事务接口和简单实现。  
* 缓存模块  
&emsp; Mybatis 中有一级缓存和二级缓存，这两级缓存都依赖于缓存模块中的实现。但是需要注意，这两级缓存与Mybatis 以及整个应用是运行在同一个 JVM 中的，共享同一块内存，如果这两级缓存中的数据量较大，则可能影响系统中其它功能，所以需要缓存大量数据时，优先考虑使用 Redis、Memcache 等缓存产品。  
* Binding 模块  
&emsp; 在调用 SqlSession 相应方法执行数据库操作时，需要制定映射文件中定义的 SQL 节点，如果 SQL 中出现了拼写错误，那就只能在运行时才能发现。为了能尽早发现这种错误，Mybatis 通过 Binding 模块将用户自定义的Mapper 接口与映射文件关联起来，系统可以通过调用自定义 Mapper 接口中的方法执行相应的 SQL 语句完成数据库操作，从而避免上述问题。注意，在开发中，只是创建了 Mapper 接口，而并没有编写实现类，这是因为 Mybatis 自动为 Mapper 接口创建了动态代理对象。  