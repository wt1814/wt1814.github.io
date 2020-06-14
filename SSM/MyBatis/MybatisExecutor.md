---
title: Mybatis SQL执行流程解析
date: 2020-04-16 00:00:00
tags:
    - Mybatis
---


<!-- TOC -->

- [1. Mybatis工作流程](#1-mybatis工作流程)
- [2. MyBatis架构分层与模块划分](#2-mybatis架构分层与模块划分)
    - [2.1. 接口层](#21-接口层)
    - [2.2. 核心处理层](#22-核心处理层)
    - [2.3. 基础支持层](#23-基础支持层)
- [3. Mybaits源码解析](#3-mybaits源码解析)
    - [3.1. Mybatis初始化](#31-mybatis初始化)
    - [3.2. 配置文件加载](#32-配置文件加载)
    - [3.3. 创建SqlSessionFactory](#33-创建sqlsessionfactory)
    - [3.4. 创建SqlSession](#34-创建sqlsession)
    - [3.5. 执行具体的sql请求](#35-执行具体的sql请求)
        - [3.5.1. 查询语句执行逻辑](#351-查询语句执行逻辑)
        - [3.5.2. SQL执行（二级缓存）](#352-sql执行二级缓存)
        - [3.5.3. SQL查询（一级缓存）](#353-sql查询一级缓存)
        - [3.5.4. SQL执行（数据库查询）](#354-sql执行数据库查询)
        - [3.5.5. 参数赋值](#355-参数赋值)
        - [3.5.6. 正式执行](#356-正式执行)
        - [3.5.7. 结果集处理](#357-结果集处理)
    - [3.6. 执行阶段总结](#36-执行阶段总结)

<!-- /TOC -->


# 1. Mybatis工作流程  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Mybatis/mybatis-13.png)  
1. 读取核心配置文件并返回InputStream流对象。
2. 根据InputStream流对象解析出Configuration对象，然后创建SqlSessionFactory工厂对象。
3. 根据一系列属性从SqlSessionFactory工厂中创建SqlSession。
4. 从SqlSession中调用Executor执行数据库操作和生成具体SQL指令。
5. 对执行结果进行二次封装。
6. 提交与事务。

# 2. MyBatis架构分层与模块划分  
&emsp; 在 MyBatis 的主要工作流程里面，不同的功能是由很多不同的类协作完成的，它们 分布在 MyBatis jar 包的不同的 package 里面。  
&emsp; MyBatis(基于3.5.1)jar 包结构是这样的（21 个包）：  

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

&emsp; 跟 Spring 一样，MyBatis 按照功能职责的不同，所有的 package 可以分成不同的 工作层次。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Mybatis/mybatis-14.png)  
&emsp; Mybatis的功能架构分为三层：  

* API接口层：提供给外部使用的接口API，开发人员通过这些本地API来操纵数据库。接口层一接收到调用请求就会调用数据处理层来完成具体的数据处理。  
* 核心处理层：负责具体的SQL查找、SQL解析、SQL执行和执行结果映射处理等。它主要的目的是根据调用的请求完成一次数据库操作。  
* 基础支持层：负责最基础的功能支撑，包括连接管理、事务管理、配置加载和缓存处理，这些都是共用的东西，将他们抽取出来作为最基础的组件。为上层的数据处理层提供最基础的支撑。  

## 2.1. 接口层  
&emsp; 在不与Spring 集成的情况下，使用 MyBatis 执行数据库的操作主要如下：  

```
InputStream is = Resources.getResourceAsStream("myBatis-config.xml");
SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
SqlSessionFactory factory = builder.build(is);
sqlSession = factory.openSession();
```
&emsp; 其中的SqlSessionFactory，SqlSession是 MyBatis 接口的核心类。SqlSession是上层应用和 MyBatis 打交道的桥梁，SqlSession 上定义了非常多的对数据库的操作方法。  
&emsp; 接口层在接收到调 用请求的时候，会调用核心处理层的相应模块来完成具体的数据库操作。  

## 2.2. 核心处理层  
&emsp; 核心处理层功能如下：  

* 配置解析  
&emsp; 在 Mybatis 初始化过程中，会加载 mybatis-config.xml 配置文件、映射配置文件以及 Mapper 接口中的注解信息，解析后的配置信息会形成相应的对象并保存到 Configration 对象中。之后，根据该对象创建SqlSessionFactory 对象。待 Mybatis 初始化完成后，可以通过 SqlSessionFactory 创建 SqlSession 对象并开始数据库操作。  
* SQL 解析与 scripting 模块  
&emsp; Mybatis 实现的动态 SQL 语句，几乎可以编写出所有满足需要的 SQL。  
&emsp; Mybatis 中 scripting 模块会根据用户传入的参数，解析映射文件中定义的动态 SQL 节点，形成数据库能执行的SQL 语句。  
* SQL 执行  
&emsp; 执行 SQL 语句；处理结果集，并映射成 Java 对象。  
&emsp; SQL 语句的执行涉及多个组件Configuration 、 SqlSessionFactory 、 Session 、 Executor 、 MappedStatement 、 
StatementHandler、ResultSetHandler。包括 MyBatis 的四大核心，它们是: Executor、StatementHandler、ParameterHandler、ResultSetHandler。  

|名称 |意义 |
|---|---|
|Configuration |管理 mysql-config.xml 全局配置关系类 |
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


&emsp; `插件也属于核心层，这是由它的工作方式和拦截的对象决定的。`    

## 2.3. 基础支持层  
&emsp; 最后一个就是基础支持层。基础支持层主要是一些抽取出来的通用的功能（实现复 用），用来支持核心处理层的功能。比如数据源、缓存、日志、xml 解析、反射、IO、 事务等等这些功能。  

* 反射模块  
&emsp; Mybatis 中的反射模块，对 Java 反射进行了很好的封装，提供了简易的 API，方便上层调用，并且对反射操作进行了一系列的优化，比如，缓存了类的 元数据（MetaClass）和对象的元数据（MetaObject），提高了反射操作的性能。  
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


# 3. Mybaits源码解析  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Mybatis/mybatis-15.png)  

## 3.1. Mybatis初始化  
&emsp; MyBatis的初始化可以有两种方式：  
&emsp; 基于XML配置文件：基于XML配置文件的方式是将MyBatis的所有配置信息放在XML文件中，MyBatis通过加载并XML配置文件，将配置文信息组装成内部的Configuration对象。  
&emsp; 基于Java API：这种方式不使用XML配置文件，需要MyBatis使用者在Java代码中，手动创建Configuration对象，然后将配置参数set 进入Configuration对象中。  
&emsp; 本文基于XML配置文件方式的MyBatis初始化。示例如下：  

```
public static void main(String[] args) throws Exception {
    // 加载配置文件
    String resource = "mybatis-config.xml";
    InputStream inputStream = Resources.getResourceAsStream(resource);
    // 创建SqlSessionFacory
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    // 从SqlSessionFactory对象中获取 SqlSession对象
    SqlSession sqlSession = sqlSessionFactory.openSession();
    // 获取Mapper
    DemoMapper mapper = sqlSession.getMapper(DemoMapper.class);
    Map<String,Object> map = new HashMap<>();
    map.put("id","123");
    // 执行操作
    mapper.selectAll(map);
    // 提交操作
    sqlSession.commit();
    // 关闭SqlSession
    sqlSession.close();
}
```

## 3.2. 配置文件加载  
&emsp; Resources.getResourceAsStream(resource);源码分析：  
&emsp; Resources是mybatis提供的一个加载资源文件的工具类。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Mybatis/mybatis-16.png)  
&emsp; #getResourceAsStream方法:  

```
public static InputStream getResourceAsStream(String resource) throws IOException {
    return getResourceAsStream((ClassLoader)null, resource);
}

public static InputStream getResourceAsStream(ClassLoader loader, String resource) throws IOException {
    InputStream in = classLoaderWrapper.getResourceAsStream(resource, loader);
    if (in == null) {
        throw new IOException("Could not find resource " + resource);
    } else {
        return in;
    }
}
```

&emsp; 获取到自身的ClassLoader对象，然后交给ClassLoader(lang包下的)来加载：  
```
InputStream getResourceAsStream(String resource, ClassLoader[] classLoader) {
    ClassLoader[] arr$ = classLoader;
    int len$ = classLoader.length;

    for(int i$ = 0; i$ < len$; ++i$) {
        ClassLoader cl = arr$[i$];
        if (null != cl) {
            InputStream returnValue = cl.getResourceAsStream(resource);
            if (null == returnValue) {
                returnValue = cl.getResourceAsStream("/" + resource);
            }

            if (null != returnValue) {
                return returnValue;
            }
        }
    }
}
```

## 3.3. 创建SqlSessionFactory  
&emsp; SqlSessionFactory对象生成使用了建造者模式。  

```
public SqlSessionFactory build(InputStream inputStream) {
    return this.build((InputStream)inputStream, (String)null, (Properties)null);
}

public SqlSessionFactory build(InputStream inputStream, String environment, Properties properties) {
    SqlSessionFactory var5;
    try {
        // 进行XML配置文件的解析
        XMLConfigBuilder parser = new XMLConfigBuilder(inputStream, environment, properties);
        //parse(): 解析mybatis-config.xml里面的节点
        var5 = this.build(parser.parse());
    } catch (Exception var14) {
        throw ExceptionFactory.wrapException("Error building SqlSession.", var14);
    } finally {
        ErrorContext.instance().reset();

        try {
            inputStream.close();
        } catch (IOException var13) {
            ;
        }

    }

    return var5;
}
```
&emsp; 通过Document对象来解析配置文件，然后返回InputStream对象，然后交给XMLConfigBuilder构造成org.apache.ibatis.session.Configuration对象，然后交给build()方法构造程SqlSessionFactory。  

&emsp; #parse()方法解析：  
```
public Configuration parse() {
    //查看该文件是否已经解析过
    if (parsed) {
        throw new BuilderException("Each XMLConfigBuilder can only be used once.");
    }
    //如果没有解析过，则继续往下解析，并且将标识符置为true
    parsed = true;
    //解析<configuration>节点
    parseConfiguration(parser.evalNode("/configuration"));
    return configuration;
}
```

```
private void parseConfiguration(XNode root) {
    try {
        //解析<Configuration>下的节点
        //issue #117 read properties first
        //<properties>
        propertiesElement(root.evalNode("properties"));
        //<settings>
        Properties settings = settingsAsProperties(root.evalNode("settings"));
        loadCustomVfs(settings);
        loadCustomLogImpl(settings);
        //别名<typeAliases>解析
        // 所谓别名 其实就是把你指定的别名对应的class存储在一个Map当中
        typeAliasesElement(root.evalNode("typeAliases"));
        //插件 <plugins>
        pluginElement(root.evalNode("plugins"));
        //自定义实例化对象的行为<objectFactory>
        objectFactoryElement(root.evalNode("objectFactory"));
        //MateObject   方便反射操作实体类的对象
        objectWrapperFactoryElement(root.evalNode("objectWrapperFactory"));
        reflectorFactoryElement(root.evalNode("reflectorFactory"));
        settingsElement(settings);
        // read it after objectFactory and objectWrapperFactory issue #631
        //<environments>
        environmentsElement(root.evalNode("environments"));
        databaseIdProviderElement(root.evalNode("databaseIdProvider"));
        // typeHandlers
        typeHandlerElement(root.evalNode("typeHandlers"));
        //主要 <mappers> 指向存放SQL的xxxxMapper.xml文件
        mapperElement(root.evalNode("mappers"));
    } catch (Exception e) {
        throw new BuilderException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
    }
}
```

&emsp; 注：mapperElement(root.evalNode("mappers"))，mapperElemet()方法是解析mapper映射文件的。  
&emsp; mapper标签配置方式：  
```xml
<mappers>
    <!-- 通过配置文件路径 -->
  <mapper resource="mapper/DemoMapper.xml" ></mapper>
    <!-- 通过Java全限定类名 -->
  <mapper class="com.mybatistest.TestMapper"/>
   <!-- 通过url 通常是mapper不在本地时用 -->
  <mapper url=""/>
    <!-- 通过包名 -->
  <package name="com.mybatistest"/>
    <!-- 注意 mapper节点中，可以使用resource/url/class三种方式获取mapper-->
</mappers>
```

```
private void mapperElement(XNode parent) throws Exception {
    if (parent != null) {
        //遍历解析mappers下的节点
        for (XNode child : parent.getChildren()) {
            //首先解析package节点
            if ("package".equals(child.getName())) {
                //获取包名
                String mapperPackage = child.getStringAttribute("name");
                configuration.addMappers(mapperPackage);
            } else {
                //如果不存在package节点，那么扫描mapper节点
                //resource/url/mapperClass三个值只能有一个值是有值的
                String resource = child.getStringAttribute("resource");
                String url = child.getStringAttribute("url");
                String mapperClass = child.getStringAttribute("class");
                //优先级 resource>url>mapperClass
                if (resource != null && url == null && mapperClass == null) {
                    //如果mapper节点中的resource不为空
                    ErrorContext.instance().resource(resource);
                    //那么直接加载resource指向的XXXMapper.xml文件为字节流
                    InputStream inputStream = Resources.getResourceAsStream(resource);
                    //通过XMLMapperBuilder解析XXXMapper.xml，可以看到这里构建的XMLMapperBuilde还传入了configuration,所以之后肯定是会将mapper封装到configuration对象中去的。
                    XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, resource, configuration.getSqlFragments());
                    //解析
                    mapperParser.parse();
                } else if (resource == null && url != null && mapperClass == null) {
                    //如果url!=null，那么通过url解析
                    ErrorContext.instance().resource(url);
                    InputStream inputStream = Resources.getUrlAsStream(url);
                    XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, url, configuration.getSqlFragments());
                    mapperParser.parse();
                } else if (resource == null && url == null && mapperClass != null) {
                    //如果mapperClass!=null，那么通过加载类构造Configuration
                    Class<?> mapperInterface = Resources.classForName(mapperClass);
                    configuration.addMapper(mapperInterface);
                } else {
                    //如果都不满足  则直接抛异常  如果配置了两个或三个  直接抛异常
                    throw new BuilderException("A mapper element may only specify a url, resource or class, but not more than one.");
                }
            }
        }
    }
}
```

&emsp; 根据以上代码可以分析，在写mapper映射文件的地址时不仅可以写成resource，还可以写成url和mapperClass的形式。如果配置文件中写的是通过resource来加载mapper.xml的，会通过XMLMapperBuilder来进行解析。  
&emsp; XMLMapperBuilder#parse()方法  

```
public void parse() {
    //判断文件是否之前解析过
    if (!configuration.isResourceLoaded(resource)) {
        //解析mapper文件节点（主要）(下面贴了代码)
      configurationElement(parser.evalNode("/mapper"));
      configuration.addLoadedResource(resource);
      //绑定Namespace里面的Class对象
      bindMapperForNamespace();
    }
    //重新解析之前解析不了的节点，先不看，最后填坑。
    parsePendingResultMaps();
    parsePendingCacheRefs();
    parsePendingStatements();
  }


//解析mapper文件里面的节点
// 拿到里面配置的配置项 最终封装成一个MapperedStatemanet
private void configurationElement(XNode context) {
  try {
      //获取命名空间 namespace，这个很重要，后期mybatis会通过这个动态代理我们的Mapper接口
    String namespace = context.getStringAttribute("namespace");
    if (namespace == null || namespace.equals("")) {
        //如果namespace为空则抛一个异常
      throw new BuilderException("Mapper's namespace cannot be empty");
    }
    builderAssistant.setCurrentNamespace(namespace);
    //解析缓存节点
    cacheRefElement(context.evalNode("cache-ref"));
    cacheElement(context.evalNode("cache"));

    //解析parameterMap（过时）和resultMap  <resultMap></resultMap>
    parameterMapElement(context.evalNodes("/mapper/parameterMap"));
    resultMapElements(context.evalNodes("/mapper/resultMap"));
    //解析<sql>节点 
    //<sql id="staticSql">select * from test</sql> （可重用的代码段）
    //<select> <include refid="staticSql"></select>
    sqlElement(context.evalNodes("/mapper/sql"));
    //解析增删改查节点<select> <insert> <update> <delete>
    buildStatementFromContext(context.evalNodes("select|insert|update|delete"));
  } catch (Exception e) {
    throw new BuilderException("Error parsing Mapper XML. The XML location is '" + resource + "'. Cause: " + e, e);
  }
}
```
&emsp; 在这个parse()方法中，调用了一个configuationElement代码，用于解析XXXMapper.xml文件中的各种节点，包括<cache\>、<cache-ref\>、<paramaterMap\>（已过时）、<resultMap\>、<sql\>、还有增删改查节点。  
&emsp; 其中具体解析每一个sql语句节点的是buildStatementFromContext(context.evalNodes("select|insert|update|delete"));   

```
private void buildStatementFromContext(List<XNode> list) {
    if (configuration.getDatabaseId() != null) {
        buildStatementFromContext(list, configuration.getDatabaseId());
    }
    //解析xml
    buildStatementFromContext(list, null);
}

private void buildStatementFromContext(List<XNode> list, String requiredDatabaseId) {
    for (XNode context : list) {
        final XMLStatementBuilder statementParser = new XMLStatementBuilder(configuration, builderAssistant, context, requiredDatabaseId);
        try {
            //解析xml节点
            statementParser.parseStatementNode();
        } catch (IncompleteElementException e) {
            //xml语句有问题时 存储到集合中 等解析完能解析的再重新解析
            configuration.addIncompleteStatement(statementParser);
        }
    }
}


public void parseStatementNode() {
    //获取<select id="xxx">中的id
    String id = context.getStringAttribute("id");
    //获取databaseId 用于多数据库，这里为null
    String databaseId = context.getStringAttribute("databaseId");

    if (!databaseIdMatchesCurrent(id, databaseId, this.requiredDatabaseId)) {
        return;
    }
    //获取节点名  select update delete insert
    String nodeName = context.getNode().getNodeName();
    //根据节点名，得到SQL操作的类型
    SqlCommandType sqlCommandType = SqlCommandType.valueOf(nodeName.toUpperCase(Locale.ENGLISH));
    //判断是否是查询
    boolean isSelect = sqlCommandType == SqlCommandType.SELECT;
    //是否刷新缓存 默认:增删改刷新 查询不刷新
    boolean flushCache = context.getBooleanAttribute("flushCache", !isSelect);
    //是否使用二级缓存 默认值:查询使用 增删改不使用
    boolean useCache = context.getBooleanAttribute("useCache", isSelect);
    //是否需要处理嵌套查询结果 group by

    // 三组数据 分成一个嵌套的查询结果
    boolean resultOrdered = context.getBooleanAttribute("resultOrdered", false);

    // Include Fragments before parsing
    XMLIncludeTransformer includeParser = new XMLIncludeTransformer(configuration, builderAssistant);
    //替换Includes标签为对应的sql标签里面的值
    includeParser.applyIncludes(context.getNode());

    //获取parameterType名
    String parameterType = context.getStringAttribute("parameterType");
    //获取parameterType的Class
    Class<?> parameterTypeClass = resolveClass(parameterType);

    //解析配置的自定义脚本语言驱动 这里为null
    String lang = context.getStringAttribute("lang");
    LanguageDriver langDriver = getLanguageDriver(lang);

    // Parse selectKey after includes and remove them.
    //解析selectKey
    processSelectKeyNodes(id, parameterTypeClass, langDriver);

    // Parse the SQL (pre: <selectKey> and <include> were parsed and removed)
    //设置主键自增规则
    KeyGenerator keyGenerator;
    String keyStatementId = id + SelectKeyGenerator.SELECT_KEY_SUFFIX;
    keyStatementId = builderAssistant.applyCurrentNamespace(keyStatementId, true);
    if (configuration.hasKeyGenerator(keyStatementId)) {
        keyGenerator = configuration.getKeyGenerator(keyStatementId);
    } else {
        keyGenerator = context.getBooleanAttribute("useGeneratedKeys",
                configuration.isUseGeneratedKeys() && SqlCommandType.INSERT.equals(sqlCommandType))
                ? Jdbc3KeyGenerator.INSTANCE : NoKeyGenerator.INSTANCE;
    }
/************************************************************************************/
    //解析Sql（重要）  根据sql文本来判断是否需要动态解析 如果没有动态sql语句且 只有#{}的时候 直接静态解析使用?占位 当有 ${} 不解析
    SqlSource sqlSource = langDriver.createSqlSource(configuration, context, parameterTypeClass);
    //获取StatementType，可以理解为Statement和PreparedStatement
    StatementType statementType = StatementType.valueOf(context.getStringAttribute("statementType", StatementType.PREPARED.toString()));
    //没用过
    Integer fetchSize = context.getIntAttribute("fetchSize");
    //超时时间
    Integer timeout = context.getIntAttribute("timeout");
    //已过时
    String parameterMap = context.getStringAttribute("parameterMap");
    //获取返回值类型名
    String resultType = context.getStringAttribute("resultType");
    //获取返回值烈性的Class
    Class<?> resultTypeClass = resolveClass(resultType);
    //获取resultMap的id
    String resultMap = context.getStringAttribute("resultMap");
    //获取结果集类型
    String resultSetType = context.getStringAttribute("resultSetType");
    ResultSetType resultSetTypeEnum = resolveResultSetType(resultSetType);
    if (resultSetTypeEnum == null) {
        resultSetTypeEnum = configuration.getDefaultResultSetType();
    }
    String keyProperty = context.getStringAttribute("keyProperty");
    String keyColumn = context.getStringAttribute("keyColumn");
    String resultSets = context.getStringAttribute("resultSets");

    //将刚才获取到的属性，封装成MappedStatement对象（代码贴在下面）
    builderAssistant.addMappedStatement(id, sqlSource, statementType, sqlCommandType,
            fetchSize, timeout, parameterMap, parameterTypeClass, resultMap, resultTypeClass,
            resultSetTypeEnum, flushCache, useCache, resultOrdered,
            keyGenerator, keyProperty, keyColumn, databaseId, langDriver, resultSets);
}


//将刚才获取到的属性，封装成mao变量MappedStatement对象
public MappedStatement addMappedStatement(
        String id,
        SqlSource sqlSource,
        StatementType statementType,
        SqlCommandType sqlCommandType,
        Integer fetchSize,
        Integer timeout,
        String parameterMap,
        Class<?> parameterType,
        String resultMap,
        Class<?> resultType,
        ResultSetType resultSetType,
        boolean flushCache,
        boolean useCache,
        boolean resultOrdered,
        KeyGenerator keyGenerator,
        String keyProperty,
        String keyColumn,
        String databaseId,
        LanguageDriver lang,
        String resultSets) {

    if (unresolvedCacheRef) {
        throw new IncompleteElementException("Cache-ref not yet resolved");
    }

    //id = namespace
    id = applyCurrentNamespace(id, false);
    boolean isSelect = sqlCommandType == SqlCommandType.SELECT;

    //通过构造者模式+链式变成，构造一个MappedStatement的构造者
    MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration, id, sqlSource, sqlCommandType)
            .resource(resource)
            .fetchSize(fetchSize)
            .timeout(timeout)
            .statementType(statementType)
            .keyGenerator(keyGenerator)
            .keyProperty(keyProperty)
            .keyColumn(keyColumn)
            .databaseId(databaseId)
            .lang(lang)
            .resultOrdered(resultOrdered)
            .resultSets(resultSets)
            .resultMaps(getStatementResultMaps(resultMap, resultType, id))
            .resultSetType(resultSetType)
            .flushCacheRequired(valueOrDefault(flushCache, !isSelect))
            .useCache(valueOrDefault(useCache, isSelect))
            .cache(currentCache);

    ParameterMap statementParameterMap = getStatementParameterMap(parameterMap, parameterType, id);
    if (statementParameterMap != null) {
        statementBuilder.parameterMap(statementParameterMap);
    }

    //通过构造者构造MappedStatement
    MappedStatement statement = statementBuilder.build();
    //将MappedStatement对象封装到Configuration对象中
    configuration.addMappedStatement(statement);
    return statement;
}
```

&emsp; 这个代码段虽然很长，但是一句话形容它就是繁琐但不复杂，里面主要也就是对xml的节点进行解析。举个比上面简单的例子吧，假设有这样一段配置：  

```xml
<select id="selectDemo" parameterType="java.lang.Integer" resultType='Map'>
    SELECT * FROM test
</select>
```
&emsp; MyBatis需要做的就是，先判断这个节点是用来干什么的，然后再获取这个节点的id、parameterType、resultType等属性，封装成一个MappedStatement对象，由于这个对象很复杂，所以MyBatis使用了构造者模式来构造这个对象，最后当MappedStatement对象构造完成后，将其封装到Configuration对象中。  

<!-- 还有没看的 -->

## 3.4. 创建SqlSession  
&emsp; openSession中实际上对SqlSession做了进一步的加工封装，包括增加了事务、执行器等。  

```
public SqlSession openSession() {
    return this.openSessionFromDataSource(this.configuration.getDefaultExecutorType(), (TransactionIsolationLevel)null, false);
}
```

```
private SqlSession openSessionFromDataSource(ExecutorType execType, TransactionIsolationLevel level, boolean autoCommit) {
    Transaction tx = null;

    DefaultSqlSession var8;
    try {
        Environment environment = this.configuration.getEnvironment();
        // 根据Configuration的Environment属性来创建事务工厂
        TransactionFactory transactionFactory = this.getTransactionFactoryFromEnvironment(environment);
        // 从事务工厂中创建事务，默认等级为null，autoCommit=false
        tx = transactionFactory.newTransaction(environment.getDataSource(), level, autoCommit);
        // 创建执行器
        Executor executor = this.configuration.newExecutor(tx, execType);
        // 根据执行器创建返回对象 SqlSession
        var8 = new DefaultSqlSession(this.configuration, executor, autoCommit);
    } catch (Exception var12) {
        this.closeTransaction(tx);
        throw ExceptionFactory.wrapException("Error opening session.  Cause: " + var12, var12);
    } finally {
        ErrorContext.instance().reset();
    }
    return var8;
}
```

```
//返回一个SqlSession，默认使用DefaultSqlSession 
public DefaultSqlSession(Configuration configuration, Executor executor, boolean autoCommit) {
    this.configuration = configuration;
    this.executor = executor;
    this.dirty = false;
    this.autoCommit = autoCommit;
}
```
&emsp; executor在这一步得到创建，具体的使用在下一步。  

## 3.5. 执行具体的sql请求  
&emsp; 平时使用MyBatis的时候，DAO层编码：  

```
public interface DemoMapper {
    public List<Map<String,Object>>  selectAll(Map<String,Object> map);
}
```

&emsp; 实际上它是一个接口，而且并没有实现类，而我们却可以直接对它进行调用，如下：  

```
DemoMapper mapper = sqlSession.getMapper(DemoMapper.class);
Map<String,Object> map = new HashMap();
map.put("id","123");
mapper.selectAll(map);
```

&emsp; MyBatis底层使用了动态代理，来对这个接口进行代理，实际上调用的是MyBatis生成的代理对象。  

&emsp; 入口：在获取Mapper的时候，需要调用SqlSession的getMapper()方法。    

```
//getMapper方法最终会调用到这里，这个是MapperRegistry的getMapper方法
@SuppressWarnings("unchecked")
public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
    //MapperProxyFactory  在解析的时候会生成一个map  map中会有我们的DemoMapper的Class
    final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
    if (mapperProxyFactory == null) {
        throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
    }
    try {
        return mapperProxyFactory.newInstance(sqlSession);
    } catch (Exception e) {
        throw new BindingException("Error getting mapper instance. Cause: " + e, e);
    }
}
```

&emsp; 可以看到这里mapperProxyFactory对象会从一个叫做knownMappers的对象中以type为key取出值，这个knownMappers是一个HashMap，存放了DemoMapper对象，而这里的type，就是上面写的Mapper接口。那么就有人会问了，这个knownMappers是在什么时候生成的呢？在解析的时候，会调用parse()方法，这个方法内部有一个bindMapperForNamespace方法，而就是这个方法完成了knownMappers的生成，并且将Mapper接口put进去。  

```
public void parse() {
    //判断文件是否之前解析过
    if (!configuration.isResourceLoaded(resource)) {
        //解析mapper文件
        configurationElement(parser.evalNode("/mapper"));
        configuration.addLoadedResource(resource);
        //这里：绑定Namespace里面的Class对象*
        bindMapperForNamespace();
    }

    //重新解析之前解析不了的节点
    parsePendingResultMaps();
    parsePendingCacheRefs();
    parsePendingStatements();
}
private void bindMapperForNamespace() {
    String namespace = builderAssistant.getCurrentNamespace();
    if (namespace != null) {
        Class<?> boundType = null;
        try {
            boundType = Resources.classForName(namespace);
        } catch (ClassNotFoundException e) {
        }
        if (boundType != null) {
            if (!configuration.hasMapper(boundType)) {
                configuration.addLoadedResource("namespace:" + namespace);
                //这里将接口class传入
                configuration.addMapper(boundType);
            }
        }
    }
}
public <T> void addMapper(Class<T> type) {
    if (type.isInterface()) {
        if (hasMapper(type)) {
            throw new BindingException("Type " + type + " is already known to the MapperRegistry.");
        }
        boolean loadCompleted = false;
        try {
            //这里将接口信息put进konwMappers。
            knownMappers.put(type, new MapperProxyFactory<>(type));
            MapperAnnotationBuilder parser = new MapperAnnotationBuilder(config, type);
            parser.parse();
            loadCompleted = true;
        } finally {
            if (!loadCompleted) {
                knownMappers.remove(type);
            }
        }
    }
}
```
&emsp; 所以在getMapper之后，获取到的是一个Class，之后的代码就简单了，就是生成标准的代理类了，调用newInstance()方法。  

```
public T newInstance(SqlSession sqlSession) {
    //首先会调用这个newInstance方法
    //动态代理逻辑在MapperProxy里面
    final MapperProxy<T> mapperProxy = new MapperProxy<>(sqlSession, mapperInterface, methodCache);
    //通过这里调用下面的newInstance方法
    return newInstance(mapperProxy);
}
@SuppressWarnings("unchecked")
protected T newInstance(MapperProxy<T> mapperProxy) {
    //jdk自带的动态代理
    return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[] { mapperInterface }, mapperProxy);
}
```
&emsp; 代理模式的执行逻辑在MapperProxy类中。  

```
/**
 * @author Clinton Begin
 * @author Eduardo Macarron
 */
public class MapperProxy<T> implements InvocationHandler, Serializable {

    public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface, Map<Method, MapperMethod> methodCache) {
        //构造
        this.sqlSession = sqlSession;
        this.mapperInterface = mapperInterface;
        this.methodCache = methodCache;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //这就是一个很标准的JDK动态代理了
        //执行的时候会调用invoke方法
        try {
            if (Object.class.equals(method.getDeclaringClass())) {
                //判断方法所属的类
                //是不是调用的Object默认的方法
                //如果是  则不代理，不改变原先方法的行为
                return method.invoke(this, args);
            } else if (method.isDefault()) {
                //对于默认方法的处理
                //判断是否为default方法，即接口中定义的默认方法。
                //如果是接口中的默认方法则把方法绑定到代理对象中然后调用。
                //这里不详细说
                if (privateLookupInMethod == null) {
                    return invokeDefaultMethodJava8(proxy, method, args);
                } else {
                    return invokeDefaultMethodJava9(proxy, method, args);
                }
            }
        } catch (Throwable t) {
            throw ExceptionUtil.unwrapThrowable(t);
        }
        //如果不是默认方法，则真正开始执行MyBatis代理逻辑。
        //获取MapperMethod代理对象
        final MapperMethod mapperMethod = cachedMapperMethod(method);
        //执行
        return mapperMethod.execute(sqlSession, args);
    }

    private MapperMethod cachedMapperMethod(Method method) {
        //动态代理会有缓存，computeIfAbsent 如果缓存中有则直接从缓存中拿
        //如果缓存中没有，则new一个然后放入缓存中
        //因为动态代理是很耗资源的
        return methodCache.computeIfAbsent(method,
                k -> new MapperMethod(mapperInterface, method, sqlSession.getConfiguration()));
    }
}
```
&emsp; 在方法开始代理之前，首先会先判断是否调用了Object类的方法，如果是，那么MyBatis不会去改变其行为，直接返回，如果是默认方法，则绑定到代理对象中然后调用（不是本文的重点），如果都不是，那么就是定义的mapper接口方法了，那么就开始执行。  
&emsp; 执行方法需要一个MapperMethod对象，这个对象是MyBatis执行方法逻辑使用的，MyBatis这里获取MapperMethod对象的方式是，首先去方法缓存中看看是否已经存在了，如果不存在则new一个然后存入缓存中，因为创建代理对象是十分消耗资源的操作。总而言之，这里会得到一个MapperMethod对象，然后通过MapperMethod的excute()方法，来真正地执行逻辑。  

### 3.5.1. 查询语句执行逻辑  
&emsp; 这里首先会判断SQL的类型：SELECT|DELETE|UPDATE|INSERT，示例中是SELECT，其它的其实都差不多。判断SQL类型为SELECT之后，就开始判断返回值类型，根据不同的情况做不同的操作。然后开始获取参数--》执行SQL。  

```
//execute() 这里是真正执行SQL的地方
public Object execute(SqlSession sqlSession, Object[] args) {
    //判断是哪一种SQL语句
    Object result;
    switch (command.getType()) {
        case INSERT: {
            Object param = method.convertArgsToSqlCommandParam(args);
            result = rowCountResult(sqlSession.insert(command.getName(), param));
            break;
        }
        case UPDATE: {
            Object param = method.convertArgsToSqlCommandParam(args);
            result = rowCountResult(sqlSession.update(command.getName(), param));
            break;
        }
        case DELETE: {
            Object param = method.convertArgsToSqlCommandParam(args);
            result = rowCountResult(sqlSession.delete(command.getName(), param));
            break;
        }
        case SELECT:
            //我们的例子是查询

            //判断是否有返回值
            if (method.returnsVoid() && method.hasResultHandler()) {
                //无返回值
                executeWithResultHandler(sqlSession, args);
                result = null;
            } else if (method.returnsMany()) {
                //返回值多行 这里调用这个方法
                result = executeForMany(sqlSession, args);
            } else if (method.returnsMap()) {
                //返回Map
                result = executeForMap(sqlSession, args);
            } else if (method.returnsCursor()) {
                //返回Cursor
                result = executeForCursor(sqlSession, args);
            } else {
                Object param = method.convertArgsToSqlCommandParam(args);
                result = sqlSession.selectOne(command.getName(), param);
                if (method.returnsOptional()
                        && (result == null || !method.getReturnType().equals(result.getClass()))) {
                    result = Optional.ofNullable(result);
                }
            }
            break;
        case FLUSH:
            result = sqlSession.flushStatements();
            break;
        default:
            throw new BindingException("Unknown execution method for: " + command.getName());
    }
    if (result == null && method.getReturnType().isPrimitive() && !method.returnsVoid()) {
        throw new BindingException("Mapper method '" + command.getName()
                + " attempted to return null from a method with a primitive return type (" + method.getReturnType() + ").");
    }
    return result;
}

//返回值多行 这里调用这个方法
private <E> Object executeForMany(SqlSession sqlSession, Object[] args) {
    //返回值多行时执行的方法
    List<E> result;
    //param是我们传入的参数，如果传入的是Map，那么这个实际上就是Map对象
    Object param = method.convertArgsToSqlCommandParam(args);
    if (method.hasRowBounds()) {
        //如果有分页
        RowBounds rowBounds = method.extractRowBounds(args);
        //执行SQL的位置
        result = sqlSession.selectList(command.getName(), param, rowBounds);
    } else {
        //如果没有
        //执行SQL的位置
        result = sqlSession.selectList(command.getName(), param);
    }
    // issue #510 Collections & arrays support
    if (!method.getReturnType().isAssignableFrom(result.getClass())) {
        if (method.getReturnType().isArray()) {
            return convertToArray(result);
        } else {
            return convertToDeclaredCollection(sqlSession.getConfiguration(), result);
        }
    }
    return result;
}

/**
 *  获取参数名的方法
 */
public Object getNamedParams(Object[] args) {
    final int paramCount = names.size();
    if (args == null || paramCount == 0) {
        //如果传过来的参数是空
        return null;
    } else if (!hasParamAnnotation && paramCount == 1) {
        //如果参数上没有加注解例如@Param，且参数只有一个，则直接返回参数
        return args[names.firstKey()];
    } else {
        //如果参数上加了注解，或者参数有多个。
        //那么MyBatis会封装参数为一个Map，但是要注意，由于jdk的原因，我们只能获取到参数下标和参数名，但是参数名会变成arg0,arg1.
        //所以传入多个参数的时候，最好加@Param，否则假设传入多个String，会造成#{}获取不到值的情况
        final Map<String, Object> param = new ParamMap<>();
        int i = 0;
        for (Map.Entry<Integer, String> entry : names.entrySet()) {
            //entry.getValue 就是参数名称
            param.put(entry.getValue(), args[entry.getKey()]);
            //如果传很多个String，也可以使用param1，param2.。。
            // add generic param names (param1, param2, ...)
            final String genericParamName = GENERIC_NAME_PREFIX + String.valueOf(i + 1);
            // ensure not to overwrite parameter named with @Param
            if (!names.containsValue(genericParamName)) {
                param.put(genericParamName, args[entry.getKey()]);
            }
            i++;
        }
        return param;
    }
}
```

### 3.5.2. SQL执行（二级缓存）  
&emsp; 执行SQL的核心方法就是selectList，即使是selectOne，底层实际上也是调用了selectList方法，然后取第一个而已。  

```
@Override
public <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
    try {
        //MappedStatement:解析XML时生成的对象， 解析某一个SQL  会封装成MappedStatement，里面存放了我们所有执行SQL所需要的信息
        MappedStatement ms = configuration.getMappedStatement(statement);
        //查询,通过executor
        return executor.query(ms, wrapCollection(parameter), rowBounds, Executor.NO_RESULT_HANDLER);
    } catch (Exception e) {
        throw ExceptionFactory.wrapException("Error querying database.  Cause: " + e, e);
    } finally {
        ErrorContext.instance().reset();
    }
}
```
&emsp; MappedStatement对象，这个对象是解析Mapper.xml配置而产生的，用于存储SQL信息，执行SQL需要这个对象中保存的关于SQL的信息，而selectList内部调用了Executor对象执行SQL语句，这个对象作为MyBatis四大对象之一。  

```
public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
    //获取sql语句
    BoundSql boundSql = ms.getBoundSql(parameterObject);
    //生成一个缓存的key  
    //这里是-1181735286:4652640444:com.DemoMapper.selectAll:0:2147483647:select * from test WHERE id =?:2121:development
    CacheKey key = createCacheKey(ms, parameterObject, rowBounds, boundSql);
    return query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
}

@Override
//二级缓存查询
public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql)
        throws SQLException {
    //二级缓存的Cache
    Cache cache = ms.getCache();
    if (cache != null) {
        //如果Cache不为空则进入
        //如果有需要的话，就刷新缓存（有些缓存是定时刷新的，需要用到这个）
        flushCacheIfRequired(ms);
        //如果这个statement用到了缓存（二级缓存的作用域是namespace，也可以理解为这里的ms）
        if (ms.isUseCache() && resultHandler == null) {
            ensureNoOutParams(ms, boundSql);
            @SuppressWarnings("unchecked")
            //先从缓存拿
                    List<E> list = (List<E>) tcm.getObject(cache, key);
            if (list == null) {
                //如果缓存的数据等于空，那么查询数据库
                list = delegate.query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
                //查询完毕后将数据放入二级缓存
                tcm.putObject(cache, key, list); // issue #578 and #116
            }
            //返回
            return list;
        }
    }
    //如果cache根本就不存在，那么直接查询一级缓存
    return delegate.query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
}
```
&emsp; 首先MyBatis在查询时，不会直接查询数据库，而是会进行二级缓存的查询，由于二级缓存的作用域是namespace，也可以理解为一个mapper，所以还会判断一下这个mapper是否开启了二级缓存，如果没有开启，则进入一级缓存继续查询。  


### 3.5.3. SQL查询（一级缓存）  

```
//一级缓存查询
@Override
public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
    ErrorContext.instance().resource(ms.getResource()).activity("executing a query").object(ms.getId());
    if (closed) {
        throw new ExecutorException("Executor was closed.");
    }
    if (queryStack == 0 && ms.isFlushCacheRequired()) {
        clearLocalCache();
    }
    List<E> list;
    try {
        //查询栈+1
        queryStack++;
        //一级缓存
        list = resultHandler == null ? (List<E>) localCache.getObject(key) : null;
        if (list != null) {
            //对于存储过程有输出资源的处理
            handleLocallyCachedOutputParameters(ms, key, parameter, boundSql);
        } else {
            //如果缓存为空，则从数据库拿
            list = queryFromDatabase(ms, parameter, rowBounds, resultHandler, key, boundSql);
        }
    } finally {
        //查询栈-1
        queryStack--;
    }
    if (queryStack == 0) {
        for (DeferredLoad deferredLoad : deferredLoads) {
            deferredLoad.load();
        }
        // issue #601
        deferredLoads.clear();
        if (configuration.getLocalCacheScope() == LocalCacheScope.STATEMENT) {
            // issue #482
            clearLocalCache();
        }
    }
    //结果返回
    return list;
}
```
&emsp; 如果一级缓存查到了，那么直接就返回结果了，如果一级缓存没有查到结果，那么最终会进入数据库进行查询。  

### 3.5.4. SQL执行（数据库查询）  

```
//数据库查询
private <E> List<E> queryFromDatabase(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
    List<E> list;
    //先往一级缓存中put一个占位符
    localCache.putObject(key, EXECUTION_PLACEHOLDER);
    try {
        //调用doQuery方法查询数据库
        list = doQuery(ms, parameter, rowBounds, resultHandler, boundSql);
    } finally {
        localCache.removeObject(key);
    }
    //往缓存中put真实数据
    localCache.putObject(key, list);
    if (ms.getStatementType() == StatementType.CALLABLE) {
        localOutputParameterCache.putObject(key, parameter);
    }
    return list;
}
//真实数据库查询
@Override
public <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
    Statement stmt = null;
    try {
        Configuration configuration = ms.getConfiguration();
        //封装，StatementHandler也是MyBatis四大对象之一
        StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, resultHandler, boundSql);
        //#{} -> ? 的SQL在这里初始化
        stmt = prepareStatement(handler, ms.getStatementLog());
        //参数赋值完毕之后，才会真正地查询。
        return handler.query(stmt, resultHandler);
    } finally {
        closeStatement(stmt);
    }
}
```
&emsp; 在真正的数据库查询之前，语句还是这样的：select * from test where id = ?，所以要先将占位符换成真实的参数值，所以接下来会进行参数的赋值。  

### 3.5.5. 参数赋值  
&emsp; 因为MyBatis底层封装的就是java最基本的jdbc，所以赋值一定也是调用jdbc的putString()方法。  

```
/********************************参数赋值部分*******************************/
//由于是#{}，所以使用的是prepareStatement，预编译SQL
private Statement prepareStatement(StatementHandler handler, Log statementLog) throws SQLException {
    Statement stmt;
    //拿连接对象
    Connection connection = getConnection(statementLog);
    //初始化prepareStatement
    stmt = handler.prepare(connection, transaction.getTimeout());
    //获取了PrepareStatement之后，这里给#{}赋值
    handler.parameterize(stmt);
    return stmt;
}

/**
 * 预编译SQL进行put值
 */
@Override
public void setParameters(PreparedStatement ps) {
    ErrorContext.instance().activity("setting parameters").object(mappedStatement.getParameterMap().getId());
    //参数列表
    List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
    if (parameterMappings != null) {
        for (int i = 0; i < parameterMappings.size(); i++) {
            ParameterMapping parameterMapping = parameterMappings.get(i);
            if (parameterMapping.getMode() != ParameterMode.OUT) {
                Object value;
                //拿到xml中#{}   参数的名字  例如 #{id}  propertyName==id
                String propertyName = parameterMapping.getProperty();
                if (boundSql.hasAdditionalParameter(propertyName)) { // issue #448 ask first for additional params
                    value = boundSql.getAdditionalParameter(propertyName);
                } else if (parameterObject == null) {
                    value = null;
                } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                    value = parameterObject;
                } else {
                    //metaObject存储了参数名和参数值的对应关系
                    MetaObject metaObject = configuration.newMetaObject(parameterObject);
                    value = metaObject.getValue(propertyName);
                }
                TypeHandler typeHandler = parameterMapping.getTypeHandler();
                JdbcType jdbcType = parameterMapping.getJdbcType();
                if (value == null && jdbcType == null) {
                    jdbcType = configuration.getJdbcTypeForNull();
                }
                try {
                    //在这里给preparedStatement赋值，通过typeHandler，setParameter最终会调用一个叫做setNonNullParameter的方法。代码贴在下面了。
                    typeHandler.setParameter(ps, i + 1, value, jdbcType);
                } catch (TypeException | SQLException e) {
                    throw new TypeException("Could not set parameters for mapping: " + parameterMapping + ". Cause: " + e, e);
                }
            }
        }
    }
}
//jdbc赋值
public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType)
        throws SQLException {
    //这里就是最最原生的jdbc的赋值了
    ps.setString(i, parameter);
}
/********************************参数赋值部分*******************************/
```

### 3.5.6. 正式执行  
&emsp; 当参数赋值完毕后，SQL就可以执行了，在上文中的代码可以看到当参数赋值完毕后，直接通过hanler.query()方法进行数据库查询。  

```
@Override
public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
    //通过jdbc进行数据库查询。
    PreparedStatement ps = (PreparedStatement) statement;
    ps.execute();
    //处理结果集 resultSetHandler 也是MyBatis的四大对象之一
    return resultSetHandler.handleResultSets(ps);
}
```

### 3.5.7. 结果集处理

```
@Override
public List<Object> handleResultSets(Statement stmt) throws SQLException {
    ErrorContext.instance().activity("handling results").object(mappedStatement.getId());
    //resultMap可以通过多个标签指定多个值，所以存在多个结果集
    final List<Object> multipleResults = new ArrayList<>();

    int resultSetCount = 0;
    //拿到当前第一个结果集
    ResultSetWrapper rsw = getFirstResultSet(stmt);

    //拿到所有的resultMap
    List<ResultMap> resultMaps = mappedStatement.getResultMaps();
    //resultMap的数量
    int resultMapCount = resultMaps.size();
    validateResultMapsCount(rsw, resultMapCount);
    //循环处理每一个结果集
    while (rsw != null && resultMapCount > resultSetCount) {
        //开始封装结果集 list.get(index) 获取结果集
        ResultMap resultMap = resultMaps.get(resultSetCount);
        //传入resultMap处理结果集 rsw 当前结果集（主线）
        handleResultSet(rsw, resultMap, multipleResults, null);
        rsw = getNextResultSet(stmt);
        cleanUpAfterHandlingResultSet();
        resultSetCount++;
    }

    String[] resultSets = mappedStatement.getResultSets();
    if (resultSets != null) {
        while (rsw != null && resultSetCount < resultSets.length) {
            ResultMapping parentMapping = nextResultMaps.get(resultSets[resultSetCount]);
            if (parentMapping != null) {
                String nestedResultMapId = parentMapping.getNestedResultMapId();
                ResultMap resultMap = configuration.getResultMap(nestedResultMapId);
                handleResultSet(rsw, resultMap, null, parentMapping);
            }
            rsw = getNextResultSet(stmt);
            cleanUpAfterHandlingResultSet();
            resultSetCount++;
        }
    }
    //如果只有一个结果集，那么从多结果集中取出第一个
    return collapseSingleResultList(multipleResults);
}
//处理结果集
private void handleResultSet(ResultSetWrapper rsw, ResultMap resultMap, List<Object> multipleResults, ResultMapping parentMapping) throws SQLException {
    //处理结果集
    try {
        if (parentMapping != null) {
            handleRowValues(rsw, resultMap, null, RowBounds.DEFAULT, parentMapping);
        } else {
            if (resultHandler == null) {
                //判断resultHandler是否为空，如果为空建立一个默认的。
                //结果集处理器
                DefaultResultHandler defaultResultHandler = new DefaultResultHandler(objectFactory);
                //处理行数据
                handleRowValues(rsw, resultMap, defaultResultHandler, rowBounds, null);
                multipleResults.add(defaultResultHandler.getResultList());
            } else {
                handleRowValues(rsw, resultMap, resultHandler, rowBounds, null);
            }
        }
    } finally {
        // issue #228 (close resultsets)
        //关闭结果集
        closeResultSet(rsw.getResultSet());
    }
}
```

## 3.6. 执行阶段总结  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Mybatis/mybatis-17.png)  

