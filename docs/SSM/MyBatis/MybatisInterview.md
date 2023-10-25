
<!-- TOC -->

- [1. Mybatis面试题](#1-mybatis面试题)
    - [1.1. 解析和运行原理](#11-解析和运行原理)
        - [1.1.1. Mapper 接口的工作原理是什么？Mapper 接口里的方法，参数不同时，方法能重载吗？](#111-mapper-接口的工作原理是什么mapper-接口里的方法参数不同时方法能重载吗)
    - [1.2. 映射器](#12-映射器)
        - [1.2.1. Mybatis是如何将sql执行结果封装为目标对象并返回的？都有哪些映射形式？](#121-mybatis是如何将sql执行结果封装为目标对象并返回的都有哪些映射形式)
    - [1.3. 高级查询](#13-高级查询)
        - [1.3.1. Mybatis动态sql](#131-mybatis动态sql)
    - [1.4. 插件](#14-插件)
        - [1.4.1. Mybatis实现分页功能](#141-mybatis实现分页功能)
            - [1.4.1.1. RowBounds（逻辑分页）](#1411-rowbounds逻辑分页)
            - [1.4.1.2. MybatisPlus实现分页功能](#1412-mybatisplus实现分页功能)
        - [1.4.2. Mybatis 是如何进行分页的？分页插件的原理是什么？](#142-mybatis-是如何进行分页的分页插件的原理是什么)
        - [1.4.3. PageHelper踩坑：不安全分页导致的问题](#143-pagehelper踩坑不安全分页导致的问题)
            - [1.4.3.1. 分页错误](#1431-分页错误)
                - [1.4.3.1.1. 分页结果错误](#14311-分页结果错误)
                - [1.4.3.1.2. order by和limit一起使用时的BUG](#14312-order-by和limit一起使用时的bug)
            - [1.4.3.2. 数据量过大时 limit分页效率，物理分页](#1432-数据量过大时-limit分页效率物理分页)
        - [1.4.4. 简述Mybatis的插件运行原理，以及如何编写一个插件。](#144-简述mybatis的插件运行原理以及如何编写一个插件)
    - [1.5. 一级、二级缓存](#15-一级二级缓存)

<!-- /TOC -->

# 1. Mybatis面试题

<!-- 
MyBatis面试题
https://blog.csdn.net/ThinkWon/article/details/101292950
-->


## 1.1. 解析和运行原理

### 1.1.1. Mapper 接口的工作原理是什么？Mapper 接口里的方法，参数不同时，方法能重载吗？
Dao 接口即 Mapper 接口。接口的全限名，就是映射文件中的 namespace 的值；接口的方法名，就是映射文件中 Mapper 的 Statement 的 id 值；接口方法内的参数，就是传递给 sql 的参数。

Mapper 接口是没有实现类的，当调用接口方法时，接口全限名+方法名拼接字符串作为 key 值，可唯一定位一个 MapperStatement。在 Mybatis 中，每一个 <select>、<insert>、<update>、<delete>标签，都会被解析为一个MapperStatement 对象。

举例：com.mybatis3.mappers.StudentDao.findStudentById，可以唯一找到 namespace 为com.mybatis3.mappers.StudentDao 下面 id 为findStudentById 的 MapperStatement。

Mapper 接口里的方法，是不能重载的，因为是使用 全限名+方法名 的保存和寻找策略。Mapper 接口的工作原理是 JDK 动态代理，Mybatis 运行时会使用 JDK动态代理为 Mapper 接口生成代理对象 proxy，代理对象会拦截接口方法，转而执行 MapperStatement 所代表的 sql，然后将 sql 执行结果返回。

## 1.2. 映射器
### 1.2.1. Mybatis是如何将sql执行结果封装为目标对象并返回的？都有哪些映射形式？

第一种是使用标签，逐一定义数据库列名和对象属性名之间的映射关系。  

第二种是使用 sql 列的别名功能，将列的别名书写为对象属性名。  

有了列名与属性名的映射关系后，Mybatis 通过反射创建对象，同时使用反射给对象的属性逐一赋值并返回，那些找不到映射关系的属性，是无法完成赋值的。  


## 1.3. 高级查询
### 1.3.1. Mybatis动态sql
Mybatis动态sql是做什么的？都有哪些动态sql？能简述一下动态sql的执行原理不？ 
Mybatis动态sql可以让我们在Xml映射文件内，以标签的形式编写动态sql，完成逻辑判断和动态拼接sql的功能。  
Mybatis提供了9种动态sql标签：trim|where|set|foreach|if|choose|when|otherwise|bind。  
其执行原理为，使用OGNL从sql参数对象中计算表达式的值，根据表达式的值动态拼接sql，以此来完成动态sql的功能。  





## 1.4. 插件
### 1.4.1. Mybatis实现分页功能  
<!-- 

https://blog.csdn.net/weixin_51262054/article/details/131368161
-->
mybatis实现分页有：  
* 直接使用SQL语句，利用limit关键字分页（物理分页）  
* RowBounds（逻辑分页）  
* 第三方插件PageHelper（物理分页）  
* MybatisPlus实现分页功能  

#### 1.4.1.1. RowBounds（逻辑分页）   
在mapper接口中  

```java
@Select("select count(*) from role")
int allRoleCount();

@Select("select * from role")
List<Role>  pageRowBoundsRole(RowBounds rowBounds);
```
 

#### 1.4.1.2. MybatisPlus实现分页功能



### 1.4.2. Mybatis 是如何进行分页的？分页插件的原理是什么？  
Mybatis 使用 RowBounds 对象进行分页，它是针对 ResultSet 结果集执行的内存分页，而非物理分页。可以在 sql 内直接书写带有物理分页的参数来完成物理分页功能，也可以使用分页插件来完成物理分页。  
分页插件的基本原理是使用 Mybatis 提供的插件接口，实现自定义插件，在插件的拦截方法内拦截待执行的 sql，然后重写 sql，根据 dialect 方言，添加对应的物理分页语句和物理分页参数。    

### 1.4.3. PageHelper踩坑：不安全分页导致的问题
<!-- 
https://www.jianshu.com/p/88d1eca40271
https://blog.51cto.com/u_15127625/3892010
mybatis使用PageHelper的bug之第一次缓存后会自己分页
https://blog.csdn.net/qq_36635569/article/details/112674497
-->

#### 1.4.3.1. 分页错误

##### 1.4.3.1.1. 分页结果错误  
示例一：  
```java
page 
page 
mapper1.select()
mapper2.select();
```

示例二:  
```java
PageHelper.setPage(1,10);
if(param!=null){
    list=userMapper.selectIf(param)
}eles{
    list=new ArrayList<User>();
}
```

主要原因：PageHelper 使用了静态的 ThreadLocal 参数，让线程绑定了分页参数， 这个参数如果没被使用就会一直留在那儿，当这个线程再次被使用时，就可能导致不该分页的方法去消费这个分页参数，这就产生了莫名其妙的分页。  

如果你对此不放心，你可以手动清理 ThreadLocal 存储的分页参数：PageHelper.clearPage();


##### 1.4.3.1.2. order by和limit一起使用时的BUG
<!-- 

https://www.jianshu.com/p/88d1eca40271
https://www.cnblogs.com/goloving/p/15203934.html
-->

error2：sql语句中已经写了limit，pagehelper又拼接了一次，出现 'limit 1 limit 10’的情况；  

通过百度，了解到PageHelper使用了静态的ThreadLocal参数，分页参数和线程是绑定的；当分页参数没有被消费时，会一直存在threadlocal中，在下一次执行的sql中会拼接这些参数。  
那么怎么避免这种情况：分页参数紧跟 list 查询。如果先写分页，又写了别的判断逻辑，没有执行 list 查询时，那么分页参数就会在threadlocal中，下次执行sql会消费这些参数，就会导致“不安全分页”。  

#### 1.4.3.2. 数据量过大时 limit分页效率，物理分页
<!-- 

https://www.jianshu.com/p/88d1eca40271
-->


### 1.4.4. 简述Mybatis的插件运行原理，以及如何编写一个插件。

Mybatis仅可以编写针对ParameterHandler、ResultSetHandler、StatementHandler、Executor这4种接口的插件，Mybatis使用JDK的动态代理，为需要拦截的接口生成代理对象以实现接口方法拦截功能，每当执行这4种接口对象的方法时，就会进入拦截方法，具体就是InvocationHandler的invoke()方法，当然，只会拦截那些你指定需要拦截的方法。

实现Mybatis的Interceptor接口并复写intercept()方法，然后在给插件编写注解，指定要拦截哪一个接口的哪些方法即可，记住，别忘了在配置文件中配置你编写的插件。



## 1.5. 一级、二级缓存  

一级缓存: 基于 PerpetualCache 的 HashMap 本地缓存，其存储作用域为 Session，当 Session flush 或 close 之后，该 Session 中的所有 Cache 就 将清空，默认打开一级缓存。  

二级缓存与一级缓存其机制相同，默认也是采用 PerpetualCache，HashMap存储，不同在于其存储作用域为 Mapper(Namespace)，并且可自定义存储源，如 Ehcache。默认不打开二级缓存，要开启二级缓存，使用二级缓存属性类需要实现 Serializable 序列化接口(可用来保存对象的状态),可在它的映射文件中配置；  

对于缓存数据更新机制，当某一个作用域(一级缓存 Session/二级缓存Namespaces)的进行了 C/U/D 操作后，默认该作用域下所有 select 中的缓存将被 clear。  

