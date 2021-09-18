
<!-- TOC -->

- [1. MyBatis大数据量查询](#1-mybatis大数据量查询)
    - [1.1. 流式查询](#11-流式查询)
        - [1.1.1. 基本概念](#111-基本概念)
        - [1.1.2. MyBatis流式查询使用教程](#112-mybatis流式查询使用教程)
            - [1.1.2.1. MyBatis流式查询接口](#1121-mybatis流式查询接口)
            - [1.1.2.2. 流式查询保持连接方案](#1122-流式查询保持连接方案)
    - [1.2. 游标查询](#12-游标查询)
    - [1.3. 针对Oracle使用批处理fetchSize](#13-针对oracle使用批处理fetchsize)

<!-- /TOC -->


# 1. MyBatis大数据量查询 
<!--

 炸！使用 MyBatis 查询千万数据量？ 
 https://mp.weixin.qq.com/s/-gljMMrP0RcALfvigFXT1Q

JDBC三种读取方式：
1. 一次全部（默认）：一次获取全部。
2. 流式：多次获取，一次一行。
3. 游标：多次获取，一次多行

新技能 MyBatis 千万数据表，快速分页！ 
https://mp.weixin.qq.com/s/RFgPkpyCPQQOo0SKZHA9Eg

-->

&emsp; `大数据量查询，非复杂运算造成的超时。`  

## 1.1. 流式查询  
&emsp; 流式查询指的是查询成功后不是返回一个集合而是返回一个迭代器，应用每次从迭代器取一条查询结果。流式查询的好处是能够降低内存使用。  
&emsp; **<font color = "clime">如果没有流式查询，想要从数据库取 1000 万条记录而又没有足够的内存时，就不得不分页查询，而分页查询效率取决于表设计，如果设计的不好，就无法执行高效的分页查询。因此流式查询是一个数据库访问框架必须具备的功能。</font>**  
&emsp; 流式查询的过程当中，数据库连接是保持打开状态的，因此要注意的是： **<font color = "clime">执行一个流式查询后，数据库访问框架就不负责关闭数据库连接了，需要应用在取完数据后自己关闭。</font>**  

### 1.1.1. 基本概念
&emsp; 流式查询指的是查询成功后不是返回一个集合而是返回一个迭代器，应用每次从迭代器取一条查询结果。流式查询的好处是能够降低内存使用。  
&emsp; 如果没有流式查询，我们想要从数据库取 1000 万条记录而又没有足够的内存时，就不得不分页查询，而分页查询效率取决于表设计，如果设计的不好，就无法执行高效的分页查询。因此流式查询是一个数据库访问框架必须具备的功能。  
&emsp; 流式查询的过程当中，数据库连接是保持打开状态的，因此要注意的是：执行一个流式查询后，数据库访问框架就不负责关闭数据库连接了，需要应用在取完数据后自己关闭。   

### 1.1.2. MyBatis流式查询使用教程
#### 1.1.2.1. MyBatis流式查询接口
&emsp; MyBatis提供了一个叫 org.apache.ibatis.cursor.Cursor 的接口类用于流式查询，这个接口继承了 java.io.Closeable 和 java.lang.Iterable 接口，由此可知：

* Cursor是可关闭的；
* Cursor是可遍历的。

&emsp; 除此之外，Cursor还提供了三个方法：  

    isOpen()：用于在取数据之前判断 Cursor 对象是否是打开状态。只有当打开时 Cursor 才能取数据；
    isConsumed()：用于判断查询结果是否全部取完。
    getCurrentIndex()：返回已经获取了多少条数据

&emsp; 因为 Cursor 实现了迭代器接口，因此在实际使用当中，从 Cursor 取数据非常简单：  

```text
cursor.forEach(rowObject -> {...});
```

&emsp; 但构建 Cursor 的过程不简单  
&emsp; 举个实际例子。下面是一个 Mapper 类：  

```java
@Mapper
public interface FooMapper {
    @Select("select * from foo limit #{limit}")
    Cursor<Foo> scan(@Param("limit") int limit);
}
```
&emsp; 方法 scan() 是一个非常简单的查询。通过指定 Mapper 方法的返回值为 Cursor 类型，MyBatis 就知道这个查询方法一个流式查询。  
&emsp; 然后我们再写一个 SpringMVC Controller 方法来调用 Mapper（无关的代码已经省略）：  

```java
@GetMapping("foo/scan/0/{limit}")
public void scanFoo0(@PathVariable("limit") int limit) throws Exception {
    try (Cursor<Foo> cursor = fooMapper.scan(limit)) {  // 1
        cursor.forEach(foo -> {});                      // 2
    }
}
```
&emsp; 上面的代码中，fooMapper 是 @Autowired 进来的。注释 1 处调用 scan 方法，得到 Cursor 对象并保证它能最后关闭；2 处则是从 cursor 中取数据。  
&emsp; 上面的代码看上去没什么问题，但是执行 scanFoo0() 时会报错：  

```java
java.lang.IllegalStateException: A Cursor is already closed.
```
&emsp; 这是因为我们前面说了在取数据的过程中需要保持数据库连接，而 Mapper 方法通常在执行完后连接就关闭了，因此 Cusor 也一并关闭了。  

#### 1.1.2.2. 流式查询保持连接方案
&emsp; 解决以上问题的思路不复杂，保持数据库连接打开即可。我们至少有三种方案可选。  
&emsp; 方案一：SqlSessionFactory  

&emsp; 我们可以用 SqlSessionFactory 来手工打开数据库连接，将 Controller 方法修改如下：  

```java
@GetMapping("foo/scan/1/{limit}")
public void scanFoo1(@PathVariable("limit") int limit) throws Exception {
    try (
        SqlSession sqlSession = sqlSessionFactory.openSession();  // 1
        Cursor<Foo> cursor = 
              sqlSession.getMapper(FooMapper.class).scan(limit)   // 2
    ) {
        cursor.forEach(foo -> { });
    }
}
```
&emsp; 上面的代码中，1 处我们开启了一个 SqlSession （实际上也代表了一个数据库连接），并保证它最后能关闭；2 处我们使用 SqlSession 来获得 Mapper 对象。这样才能保证得到的 Cursor 对象是打开状态的。  

&emsp; 方案二：TransactionTemplate  

&emsp; 在 Spring 中，我们可以用 TransactionTemplate 来执行一个数据库事务，这个过程中数据库连接同样是打开的。代码如下：  

```java
@GetMapping("foo/scan/2/{limit}")
public void scanFoo2(@PathVariable("limit") int limit) throws Exception {
    TransactionTemplate transactionTemplate = 
            new TransactionTemplate(transactionManager);  // 1

    transactionTemplate.execute(status -> {               // 2
        try (Cursor<Foo> cursor = fooMapper.scan(limit)) {
            cursor.forEach(foo -> { });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    });
}
```
&emsp; 上面的代码中，1 处我们创建了一个 TransactionTemplate 对象（此处 transactionManager 是怎么来的不用多解释，本文假设读者对 Spring 数据库事务的使用比较熟悉了），2 处执行数据库事务，而数据库事务的内容则是调用 Mapper 对象的流式查询。注意这里的 Mapper 对象无需通过 SqlSession 创建。  

&emsp; 方案三：@Transactional 注解  

&emsp; 这个本质上和方案二一样，代码如下：

```java
@GetMapping("foo/scan/3/{limit}")
@Transactional
public void scanFoo3(@PathVariable("limit") int limit) throws Exception {
    try (Cursor<Foo> cursor = fooMapper.scan(limit)) {
        cursor.forEach(foo -> { });
    }
}
```
&emsp; 它仅仅是在原来方法上面加了个 @Transactional 注解。这个方案看上去最简洁，但请注意 Spring 框架当中注解使用的坑：只在外部调用时生效。在当前类中调用这个方法，依旧会报错。  

&emsp; 以上是三种实现 MyBatis 流式查询的方法。  

## 1.2. 游标查询
&emsp; ...  


## 1.3. 针对Oracle使用批处理fetchSize
<!-- 

mybatis大数据查询优化：fetchSize
https://www.jianshu.com/p/2ba501063556

https://www.oschina.net/question/3488884_2271532?sort=time
Mybatis针对Oracle使用fetchSize
https://blog.csdn.net/onlyusc2/article/details/108042167
https://my.oschina.net/qalong/blog/3123826
-->
&emsp; ...  