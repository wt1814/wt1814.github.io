---
title: Mybatis缓存解析
date: 2020-04-17 00:00:00
tags:
    - Mybatis
---

<!-- TOC -->

- [1. MyBatis缓存](#1-mybatis缓存)
    - [1.1. 一级缓存](#11-一级缓存)
        - [1.1.1. 一级缓存不足](#111-一级缓存不足)
        - [1.1.2. Spring整合MyBatis一条语句创建几个SqlSession会话](#112-spring整合mybatis一条语句创建几个sqlsession会话)
    - [1.2. 二级缓存](#12-二级缓存)
        - [1.2.1. 开启二级缓存](#121-开启二级缓存)
        - [1.2.2. 开启二级缓存的时机](#122-开启二级缓存的时机)
        - [1.2.3. 使用第三方缓存做二级缓存](#123-使用第三方缓存做二级缓存)
        - [1.2.4. 二级缓存不足](#124-二级缓存不足)
            - [1.2.4.1. 二级缓存不能存在一直增多的数据](#1241-二级缓存不能存在一直增多的数据)
            - [1.2.4.2. 二级缓存有可能存在脏读的问题（可避免）](#1242-二级缓存有可能存在脏读的问题可避免)
    - [1.3. MyBatis缓存的执行流程](#13-mybatis缓存的执行流程)
    - [1.4. Spring整合MyBatis缓存失效问题](#14-spring整合mybatis缓存失效问题)

<!-- /TOC -->

![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Mybatis/mybatis-31.png)  


# 1. MyBatis缓存  
&emsp; MyBatis支持声明式数据缓存（declarative data caching）。MyBatis提供了默认基于Java HashMap的缓存实现，以及用于与OSCache、Ehcache、Hazelcast和Memcached连接的默认连接器。MyBatis还提供API供其他缓存实现使用。  

## 1.1. 一级缓存  
&emsp; 一级缓存也叫本地缓存，<font color = "red">MyBatis 的一级缓存是在会话（SqlSession）层面进行缓存的。</font>MyBatis中的一级缓存，是默认开启且无法关闭的。  

&emsp; 一级缓存的生命周期：  
1. 如果SqlSession调用了close()方法，会释放掉一级缓存PerpetualCache对象，一级缓存将不可用。
2. 如果SqlSession调用了clearCache()，会清空PerpetualCache对象中的数据，但是该对象仍可使用。
3. SqlSession中执行了任何一个update操作(update()、delete()、insert()) ，都会清空PerpetualCache对象的数据，但是该对象可以继续使用。

### 1.1.1. 一级缓存不足  
&emsp; 如果跨会话，会出现什么问题？   
&emsp; 其他会话更新了数据，导致读取到脏数据（一级缓存不能跨会话共享）  

```
// 会话 2 更新了数据，会话 2 的一级缓存更新 
BlogMapper mapper2 = session2.getMapper(BlogMapper.class); 
mapper2.updateByPrimaryKey(blog); 
session2.commit(); 
// 会话 1 读取到脏数据，因为一级缓存不能跨会话共享 
System.out.println(mapper1.selectBlog(1));
```
&emsp; 一级缓存的不足：***<font color = "red">使用一级缓存的时候，因为缓存不能跨会话共享，不同的会话之间对于相同的数据可能有不一样的缓存。在有多个会话或者分布式环境下，会存在脏数据的问题。如果要解决这个问题，就要用到二级缓存。</font>***   

### 1.1.2. Spring整合MyBatis一条语句创建几个SqlSession会话  
&emsp; 同一个方法，Mybatis 多次请求数据库，是否要创建多个 SqlSession会话？  
&emsp; 先从两个 demo 说起，再切入 Mybatis 的源码。  
```
public void testSqlSession() throws Exception{
    System.out.println(this.xttblogMapper.findByName("aaa"));
    System.out.println(this.xttblogMapper.findByName("bbb"));
}
```
&emsp; 运行一下代码。查看控制台，有一下输出。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Mybatis/mybatis-21.png)  
&emsp; 这说明<font color = "red">在同一个方法，Mybatis 多次请求数据库且没有事务的情况下，创建了多个 SqlSession 会话！</font>  

&emsp; 然后，在 testSqlSession 方法上加上 @Transactional 注解看看效果。  
```
@Transactional
public void testSqlSession(){
    System.out.println(jmbRollbackRecordMapper.findByName("aaa"));
    System.out.println(jmbRollbackRecordMapper.findByName("bbb"));
}
```
&emsp; 运行效果截图如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Mybatis/mybatis-22.png)  
&emsp; 这说明，<font color = "red">在有事务的情况下，同一个方法，Mybatis多次请求数据库，只创建了一个SqlSession会话！</font>  

&emsp; 如果有事务，并且方法内存在多个线程的情况下，代码如下：  
```
@Transactional
public void testSqlSession(){
    new Thread(){
        @Override
        public void run() {
            System.out.println(jmbRollbackRecordMapper.findByName("aaa"));
        }
    }.start();
    new Thread(){
        @Override
        public void run() {
            System.out.println(jmbRollbackRecordMapper.findByName("bbb"));
        }
    }.start();
}
```
&emsp; 运行结果如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Mybatis/mybatis-23.png)  
&emsp; <font color= "red">在有事务的情况下，同一个方法内，有多个线程 Mybatis 多次请求数据库的情况下，创建了多个 SqlSession 会话！</font>  

&emsp; 为什么在同一个事务下，又开启两个SqlSession了呢？  
&emsp; 这就需要查看源码了，通过源码会发现，在启用的这两个线程中，在事务管理器 TransactionSynchronizationManager 中获取 SqlSessionHolder，再从 SqlSessionHolder 中获取 SqlSession。而这两个线程的 ThreadLocal 绑定的线程不一样，所以就重新 openSession 了一个 SqlSession。  


## 1.2. 二级缓存  
&emsp; 二级缓存是用来解决一级缓存不能跨会话共享的问题的，范围是namespace级别的，可以被多个SqlSession共享<font color = "red">（只要是同一个接口的同一方法，都可以共享）</font>， 生命周期和应用同步。    

### 1.2.1. 开启二级缓存  
&emsp; MyBatis的二级缓存是默认关闭的，如果要开启有两种方式：  
&emsp; 方式一：在mybatis-config.xml中加入如下配置片段    

```
<!-- 全局配置参数，需要时再设置 -->
<settings>
    <!-- 开启二级缓存  默认值为true -->
    <setting name="cacheEnabled" value="true"/>

</settings>
```
&emsp; 方式二：在mapper.xml中开启  

```
 <!--开启本mapper的namespace下的二级缓存-->
     <!--
             eviction:代表的是缓存回收策略，目前MyBatis提供以下策略。
             (1) LRU,最近最少使用的，一处最长时间不用的对象
             (2) FIFO,先进先出，按对象进入缓存的顺序来移除他们
             (3) SOFT,软引用，移除基于垃圾回收器状态和软引用规则的对象
             (4) WEAK,弱引用，更积极的移除基于垃圾收集器状态和弱引用规则的对象。
                 这里采用的是LRU，  移除最长时间不用的对形象

             flushInterval:刷新间隔时间，单位为毫秒，如果你不配置它，那么当
             SQL被执行的时候才会去刷新缓存。

             size:引用数目，一个正整数，代表缓存最多可以存储多少个对象，不宜设置过大。设置过大会导致内存溢出。
             这里配置的是1024个对象

             readOnly:只读，意味着缓存数据只能读取而不能修改，这样设置的好处是我们可以快速读取缓存，缺点是我们没有
             办法修改缓存，他的默认值是false，不允许我们修改
      -->
     <!-- 声明这个 namespace 使用二级缓存 -->
<cache type="org.apache.ibatis.cache.impl.PerpetualCache"
       size="1024" <!—最多缓存对象个数，默认 1024--> eviction="LRU" <!—回收策略-->
       flushInterval="120000" <!—自动刷新时间 ms，未配置时只有调用时刷新-->
       readOnly="false"/> <!—默认是 false（安全），改为 true 可读写时，对象必须支持序列 化 -->
```


&emsp; Mapper.xml 配置了<cache>之后，select()会被缓存。update()、delete()、insert() 会刷新缓存。  

&emsp; 如果 cacheEnabled=true，Mapper.xml 没有配置标签，还有二级缓存吗？ 还会出现 CachingExecutor 包装对象吗？ 只要 cacheEnabled=true 基本执行器就会被装饰。有没有配置<cache>，决定了在 启动的时候会不会创建这个 mapper 的 Cache 对象，最终会影响到 CachingExecutor query 方法里面的判断：  

    if (cache != null) {  

&emsp; 如果某些查询方法对数据的实时性要求很高，不需要二级缓存，怎么办？   
&emsp; 可以在单个 Statement ID 上显式关闭二级缓存（默认是 true）：   

```
<select id="selectBlog" resultMap="BaseResultMap" useCache="false">
```

### 1.2.2. 开启二级缓存的时机  
&emsp; 一级缓存默认是打开的，二级缓存需要配置才可以开启。***<font color = "red">在什么情况下才有必要去开启二级缓存？</font>***   
1. <font color = "red">因为所有的增删改都会刷新二级缓存，导致二级缓存失效，所以适合在查询为主的应用中使用，比如历史交易、历史订单的查询。</font>否则缓存就失去了意义。
2. 如果多个namespace中有针对于同一个表的操作，比如Blog表，如果在一个namespace中刷新了缓存，另一个 namespace 中没有刷新，就会出现读到脏数据的情 况。所以，推荐在一个Mapper里面只操作单表的情况使用。  

&emsp; 如果要让多个namespace共享一个二级缓存，应该怎么做？   
&emsp; 跨namespace的缓存共享的问题，可以使用<cache-ref\>来解决：  

```
<cache-ref namespace="com.gupaoedu.crud.dao.DepartmentMapper" /> 
```
&emsp; cache-ref 代表引用别的命名空间的 Cache 配置，两个命名空间的操作使用的是同 一个 Cache。在关联的表比较少，或者按照业务可以对表进行分组的时候可以使用。  
&emsp; 注意：在这种情况下，多个 Mapper 的操作都会引起缓存刷新，缓存的意义已经不大了。  

### 1.2.3. 使用第三方缓存做二级缓存  
&emsp; 除了MyBatis自带的二级缓存之外，也可以通过实现Cache接口来自定义二级缓存。MyBatis 官方提供了一些第三方缓存集成方式，比如 ehcache 和redis：https://github.com/mybatis/redis-cache  

&emsp; Mybatis缓存体系结构介绍：    
&emsp; MyBatis跟缓存相关的类都在cache包里面，其中有一个 Cache 接口，只有一个默认的实现类PerpetualCache，它是用HashMap实现的。除此之外，还有很多的装饰器，通过这些装饰器可以额外实现很多的功能：回收策略、日志记录、定时刷新等等。但是无论怎么装饰，经过多少层装饰，最后使用的还是基本的实现类（默认 PerpetualCache）。 
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Mybatis/mybatis-18.png)  
<!-- ![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Mybatis/mybatis-19.png) --> 
&emsp; 所有的缓存实现类总体上可分为三类：基本缓存、淘汰算法缓存、装饰器缓存。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Mybatis/mybatis-20.png)  

### 1.2.4. 二级缓存不足  
#### 1.2.4.1. 二级缓存不能存在一直增多的数据  
&emsp; 由于二级缓存的影响范围不是SqlSession而是namespace，所以二级缓存会在应用启动时一直存在直到应用关闭，所以二级缓存中不能存在随着时间数据量越来越大的数据，这样有可能会造成内存空间被占满。  

#### 1.2.4.2. 二级缓存有可能存在脏读的问题（可避免）  
&emsp; 由于二级缓存的作用域为namespace，那么就可以假设这么一个场景，有两个namespace操作一张表，第一个namespace查询该表并回写到内存中，第二个namespace往表中插一条数据，那么第一个namespace的二级缓存是不会清空这个缓存的内容的，在下一次查询中，还会通过缓存去查询，这样会造成数据的不一致。  
&emsp; 所以当项目里有多个命名空间操作同一张表的时候，最好不要用二级缓存，或者使用二级缓存时避免用两个namespace操作一张表。  

## 1.3. MyBatis缓存的执行流程  
&emsp; Demo：  

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

&emsp; 这里会执行到query()方法：  

```
public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql)
        throws SQLException {
    //二级缓存的Cache,通过MappedStatement获取
    Cache cache = ms.getCache();
    if (cache != null) {
        //是否需要刷新缓存
        //在<select>标签中也可以配置flushCache属性来设置是否查询前要刷新缓存，默认增删改刷新缓存查询不刷新
        flushCacheIfRequired(ms);
        //判断这个mapper是否开启了二级缓存
        if (ms.isUseCache() && resultHandler == null) {
            
            ensureNoOutParams(ms, boundSql);
            @SuppressWarnings("unchecked")
            //先从缓存拿
                    List<E> list = (List<E>) tcm.getObject(cache, key);
            if (list == null) {
                //如果缓存等于空，那么查询一级缓存
                list = delegate.query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
                //查询完毕后将数据放入二级缓存
                tcm.putObject(cache, key, list); // issue #578 and #116
            }
            //返回
            return list;
        }
    }
    //如果二级缓存为null，那么直接查询一级缓存
    return delegate.query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
}
```
&emsp; 可以看到首先MyBatis在查询数据时会先看看这个mapper是否开启了二级缓存，如果开启了，会先查询二级缓存，如果缓存中存在需要的数据，那么直接就从缓存返回数据，如果不存在，则继续往下走查询逻辑。  
&emsp; 接着往下走，如果二级缓存不存在，那么就直接查询数据了吗？答案是否定的，二级缓存如果不存在，MyBatis会再查询一次一级缓存，接着往下看。  

```
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
        queryStack++;
        //查询一级缓存（localCache）
        list = resultHandler == null ? (List<E>) localCache.getObject(key) : null;
        if (list != null) {
            //对于存储过程有输出资源的处理
            handleLocallyCachedOutputParameters(ms, key, parameter, boundSql);
        } else {
            //如果缓存为空，则从数据库拿
            list = queryFromDatabase(ms, parameter, rowBounds, resultHandler, key, boundSql);
            /**这个是queryFromDatabase的逻辑
             * //先往缓存中put一个占位符
             localCache.putObject(key, EXECUTION_PLACEHOLDER);
             try {
             list = doQuery(ms, parameter, rowBounds, resultHandler, boundSql);
             } finally {
             localCache.removeObject(key);
             }
             //往一级缓存中put真实数据
             localCache.putObject(key, list);
             if (ms.getStatementType() == StatementType.CALLABLE) {
             localOutputParameterCache.putObject(key, parameter);
             }
             return list;
             */
        }
    } finally {
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
    return list;
}
```

## 1.4. Spring整合MyBatis缓存失效问题  
&emsp; 一级缓存的作用域是SqlSession，而使用者可以自定义SqlSession什么时候出现什么时候销毁，在这段期间一级缓存都是存在的。当使用者调用close()方法之后，就会销毁一级缓存。  
&emsp; 但是，MyBatis和Spring整合之后，Spring跳过了SqlSessionFactory这一步，可以直接调用Mapper，导致在操作完数据库之后，Spring就将SqlSession就销毁了，一级缓存就随之销毁了，所以一级缓存就失效了。  

&emsp; 那么怎么能让缓存生效呢？  

* 开启事务，因为一旦开启事务，Spring就不会在执行完SQL之后就销毁SqlSession，因为SqlSession一旦关闭，事务就没了，一旦开启事务，在事务期间内，缓存会一直存在。  
* 使用二级缓存。  
