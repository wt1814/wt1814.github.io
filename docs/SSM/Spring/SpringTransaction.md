
<!-- TOC -->

- [1. Spring事务](#1-spring事务)
    - [1.1. Spring事务简介](#11-spring事务简介)
    - [1.2. Spring事务实现](#12-spring事务实现)
        - [1.2.1. Spring Boot开启事务](#121-spring-boot开启事务)
        - [1.2.2. 注解@Transactional使用](#122-注解transactional使用)
    - [1.3. ★★★Spring事务属性详解](#13-★★★spring事务属性详解)
        - [1.3.1. 事务传播行为](#131-事务传播行为)
            - [1.3.1.1. ~~传播行为编码示例~~](#1311-传播行为编码示例)
        - [1.3.2. 事务隔离级别](#132-事务隔离级别)
        - [1.3.3. 事务超时](#133-事务超时)
        - [1.3.4. 事务只读属性](#134-事务只读属性)
    - [1.4. Spring事务其他使用](#14-spring事务其他使用)
    - [1.5. ★★★Spring事务失效](#15-★★★spring事务失效)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. `@Transactional(rollbackFor = Exception.class) `，Transactional`默认只回滚RuntimeException，`但是可以指定要回滚的异常类型。    
2. **<font color = "red">Spring事务属性通常由事务的传播行为、事务的隔离级别、事务的超时值、事务只读标志组成。</font>**  
    * 事务的传播行为主要分为支持当前事务和不支持当前事务。  
        &emsp; <font color = "red">PROPAGATION_REQUIRED：如果当前存在事务，则加入该事务，合并成一个事务；如果当前没有事务，则创建一个新的事务。这是默认值。</font>  
        &emsp; 下面的类型都是针对于被调用方法来说的，理解起来要想象成两个service 方法的调用才可以。  
        &emsp; **支持当前事务的情况：**  
        &emsp; 1. <font color = "red">PROPAGATION_REQUIRED：如果当前存在事务，则加入该事务，合并成一个事务；如果当前没有事务，则创建一个新的事务。这是默认值。</font>  
        &emsp; 2. PROPAGATION_SUPPORTS：如果当前存在事务，则加入该事务；如果当前没有事务，则以非事务的方式继续运行。  
        &emsp; 3. PROPAGATION_MANDATORY：如果当前存在事务，则加入该事务；如果当前没有事务，则抛出异常，即父级方法必须有事务。  

        &emsp; **不支持当前事务的情况：**  
        &emsp; 4. PROPAGATION_REQUIRES_NEW：创建一个新的事务，如果当前存在事务，则把当前事务挂起。这个方法会独立提交事务，不受调用者的事务影响，父级异常，它也是正常提交。  
        &emsp; 5. PROPAGATION_NOT_SUPPORTED：以非事务方式运行，如果当前存在事务，则把当前事务挂起。  
        &emsp; 6. PROPAGATION_NEVER：以非事务方式运行，如果当前存在事务，则抛出异常，即父级方法必须无事务。  

        &emsp; **其他情况：**  
        &emsp; 7. PROPAGATION_NESTED：如果当前存在事务，则创建一个事务作为当前事务的嵌套事务来运行；如果当前没有事务，则该取值等价于PROPAGATION_REQUIRED。  
        &emsp; 嵌套事务是外部事务的一部分，只有外部事务结束后它才会被提交。由此可见，PROPAGATION_REQUIRES_NEW和PROPAGATION_NESTED的最大区别在于：PROPAGATION_REQUIRES_NEW完全是一个新的事务，而PROPAGATION_NESTED则是外部事务的子事务，如果外部事务commit，嵌套事务也会被commit， 这个规则同样适用于roll back。  
    * 事务的隔离级别，默认使用底层数据库的默认隔离级别，其他四个隔离级别和数据库的隔离级别一致。  
    * 事务只读，相当于将数据库设置成只读数据库，此时若要进行写的操作，会出现错误。  


# 1. Spring事务   
<!--
分析@Transactional实现原理
https://mp.weixin.qq.com/s/Jq2NVorvGZV1qj_HRyj4ow

spring事务失效的12种场景
https://mp.weixin.qq.com/s/nMAsyH8z7E6XFYiKJTy4Cw
https://mp.weixin.qq.com/s/RpA3RvYv4I4zYSzBmZaDiA
-->

## 1.1. Spring事务简介  
&emsp; 事务分为业务事务和系统事务。业务事务也就是业务逻辑上操作的一致性，系统事务指真正的数据库事务。  
&emsp; Spring事务为业务逻辑进行事务管理，保证业务逻辑上数据的原子性。事务根据项目性质来细分，事务可以设置到三个层面(dao层、service层和web层)。  
1. web层事务，一般是针对那些安全性要求较高的系统来说的。例如电子商务网站。粒度小，一般系统用不着这么细。   
2. service层事务，这是一常见的事务划分，将事务设置在业务逻辑上，只要业务逻辑出错或异常就事务回滚。粒度较小，一般推荐这种方式。  
&emsp; 在service层配置事务，因为一个service层方法操作可以关联到多个DAO的操作。在service层执行这些dao操作，多dao操作有失败全部回滚，成功则全部提交。  
3. 数据持久层数据务，也就是常说的数据库事务。这种事务在安全性方面要求低。粒度大。  

&emsp; **<font color = "red">Spring事务的本质其实就是数据库对事务的支持，</font>** 没有数据库对事务支持，Spring是无法提供事务功能的。  

## 1.2. Spring事务实现  
&emsp; 在Spring中，事务有两种实现方式，分别是编程式事务管理和声明式事务管理两种方式。  

* 编程式事务管理：编程式事务管理使用TransactionTemplate或者直接使用底层的PlatformTransactionManager。对于编程式事务管理，Spring推荐使用TransactionTemplate。  
* <font color = "red">声明式事务管理：基于AOP配置的通知性事务管理。其本质是对方法前后进行拦截，然后在目标方法开始之前创建或者加入一个事务，在执行完目标方法之后根据执行情况提交或者回滚事务。</font>  

&emsp; 声明式事务最大的优点就是不需要通过编程的方式管理事务。不在业务逻辑代码中掺杂事务管理的代码，只需在配置文件中做相关的事务规则声明（或通过基于@Transactional注解的方式），便可以将事务规则应用到业务逻辑中。声明式事务管理也有两种常用的方式，一种是基于tx和aop名字空间的xml配置文件，另一种就是基于@Transactional注解。  

### 1.2.1. Spring Boot开启事务  
&emsp; ......

### 1.2.2. 注解@Transactional使用  
1. 位置：@Transactional可以作用于接口、接口方法、类以及类方法上。Spring建议不要在接口或者接口方法上使用该注解，因为只有在使用基于接口的代理时它才会生效。当作用于类上时，该类的所有public方法将都具有该类型的事务属性，同时也可以在方法级别使用注解来覆盖类级别的定义。  
&emsp; Spring团队建议在具体的类(或类的方法)上使用@Transactional注解，而不要使用在类所要实现的任何接口上。在接口上使用@Transactional注解，只能当设置了基于接口的代理时它才生效。因为注解是不能继承的，这就意味着如果正在使用基于类的代理时，那么事务的设置将不能被基于类的代理所识别，而且对象也将不会被事务代理所包装。  
2. 特性：  
&emsp; @Transactional注解只被应用到public方法上(Spring AOP的本质决定)。如果在protected、private或者默认可见性的方法上使用 @Transactional注解，注解将被忽略，也不会抛出任何异常。  
&emsp; SpringAOP声明式事务管理默认对unchecked exception回滚，即默认对RuntimeException()异常或是其子类进行事务回滚；对checked异常(Exception可try{}捕获的，包括自定义异常)，不会回滚，需要通过rollbackFor进行设置。  

&emsp; 全注解的使用方式：  

```java
@Transactional(value = "hikariCPTransactionManager"， propagation = Propagation.REQUIRED，rollbackFor = Exception.class，timeout = 1， isolation = Isolation.DEFAULT)
```

&emsp; 注解中属性详解：  

|参数名称|功能描述|
|---|---|
|value	|可选的限定描述符，指定使用的事务管理器。|
|propagation|该属性用于设置事务的传播行为。例如：@Transactional(propagation=Propagation.NOT_SUPPORTED，readOnly=true)。|
|isolation|该属性用于设置底层数据库的事务隔离级别，事务隔离级别用于处理多事务并发的情况，通常使用数据库的默认隔离级别即可，基本不需要进行设置。|
|timeout|该属性用于设置事务的超时秒数，默认值为-1表示永不超时。|
|readOnly|该属性用于设置当前事务是否为只读事务，默认值为false(可读写)。|
|rollbackFor|`该属性用于设置需要进行回滚的异常类数组`，当方法中抛出指定异常数组中的异常时，则进行事务回滚。例如：指定单一异常类：@Transactional(rollbackFor=RuntimeException.class)。可以指定多个异常类：@Transactional(rollbackFor={RuntimeException.class， Exception.class})。|
|rollbackForClassName|该属性用于设置需要进行回滚的异常类名称数组，当方法中抛出指定异常名称数组中的异常时，则进行事务回滚。例如：指定单一异常类名称@Transactional(rollbackForClassName=”RuntimeException”)。可以指定多个异常类名称：@Transactional(rollbackForClassName={“RuntimeException”，”Exception”})。|
|noRollbackFor|	该属性用于设置不需要进行回滚的异常类数组，当方法中抛出指定异常数组中的异常时，不进行事务回滚。例如：指定单一异常类：@Transactional(noRollbackFor=RuntimeException.class)指定多个异常类：@Transactional(noRollbackFor={RuntimeException.class， Exception.class})。|
|noRollbackForClassName	|该属性用于设置不需要进行回滚的异常类名称数组，当方法中抛出指定异常名称数组中的异常时，不进行事务回滚。例如：指定单一异常类名称：@Transactional(noRollbackForClassName=”RuntimeException”)指定多个异常类名称：@Transactional(noRollbackForClassName={“RuntimeException”，”Exception”})|  

## 1.3. ★★★Spring事务属性详解  
&emsp; **<font color = "red">Spring事务属性通常由事务的传播行为、事务的隔离级别、事务的超时值、事务只读标志组成。在进行事务划分时，须要进行事务定义，也就是配置事务的属性。</font>** Spring框架中PlatfromTransactionManager是spring事务管理的核心接口，TransactionDefinition接口定义事务属性。  

```java
public interface TransactionDefinition {
    int getPropagationBehavior(); //事务的传播行为
    int getIsolationLevel();      //事务的隔离级别
    int getTimeout();
    boolean isReadOnly();
}
```

### 1.3.1. 事务传播行为  
&emsp; 事务传播行为是多个事务方法相互调用时，事务如何在这些方法间传播。在开始当前事务之前，一个事务上下文已经存在，此时有若干选项可以指定一个事务性方法的执行行为。Spring在TransactionDefinition接口中规定了7种类型的事务传播行为，它们规定了事务方法和事务方法发生嵌套调用时事务如何进行传播，即协调已经有事务标识的方法之间的发生调用时的事务上下文的规则(是否要有独立的事务隔离级别和锁)。  

&emsp; 传播机制生效条件：因为spring是使用aop来代理事务控制，是针对于接口或类的，所以在同一个service类中两个方法的调用，传播机制是不生效的。  

&emsp; 下面的类型都是针对于被调用方法来说的，理解起来要想象成两个service 方法的调用才可以。  

&emsp; **支持当前事务的情况：**  
&emsp; 1. <font color = "red">PROPAGATION_REQUIRED：如果当前存在事务，则加入该事务，合并成一个事务；如果当前没有事务，则创建一个新的事务。这是默认值。</font>  
&emsp; 2. PROPAGATION_SUPPORTS：如果当前存在事务，则加入该事务；如果当前没有事务，则以非事务的方式继续运行。  
&emsp; 3. PROPAGATION_MANDATORY：如果当前存在事务，则加入该事务；如果当前没有事务，则抛出异常，即父级方法必须有事务。  

&emsp; **不支持当前事务的情况：**  
&emsp; 4. PROPAGATION_REQUIRES_NEW：创建一个新的事务，如果当前存在事务，则把当前事务挂起。这个方法会独立提交事务，不受调用者的事务影响，父级异常，它也是正常提交。  
&emsp; 5. PROPAGATION_NOT_SUPPORTED：以非事务方式运行，如果当前存在事务，则把当前事务挂起。  
&emsp; 6. PROPAGATION_NEVER：以非事务方式运行，如果当前存在事务，则抛出异常，即父级方法必须无事务。  

&emsp; **其他情况：**  
&emsp; 7. PROPAGATION_NESTED：如果当前存在事务，则创建一个事务作为当前事务的嵌套事务来运行；如果当前没有事务，则该取值等价于PROPAGATION_REQUIRED。  
&emsp; 嵌套事务是外部事务的一部分，只有外部事务结束后它才会被提交。由此可见，PROPAGATION_REQUIRES_NEW和PROPAGATION_NESTED的最大区别在于：PROPAGATION_REQUIRES_NEW完全是一个新的事务，而PROPAGATION_NESTED则是外部事务的子事务，如果外部事务commit，嵌套事务也会被commit， 这个规则同样适用于roll back。  

#### 1.3.1.1. ~~传播行为编码示例~~  
<!-- 
Spring 事务传播属性有那么难吗？看这一篇就够了！
https://mp.weixin.qq.com/s/Ta5GQYj2KtFIRDYLo4xAFg
-->


### 1.3.2. 事务隔离级别  
&emsp; 隔离级别是指若干个并发的事务之间的隔离程度。TransactionDefinition接口中定义了5个表示隔离级别的常量，默认值为ISOLATION_DEFAULT(使用数据库的设置)，其他四个隔离级别和数据库的隔离级别一致。  
1. <font color = "red">ISOLATION_DEFAULT：默认值，表示使用底层数据库的默认隔离级别。</font>Mysql默认采用的REPEATABLE_READ隔离级别；Oracle默认采用的 READ_COMMITTED隔离级别。   
2. ISOLATION_READ_UNCOMMITTED：该隔离级别表示一个事务可以读取另一个事务修改但还没有提交的数据。最低的隔离级别，允许读取尚未提交的数据变更，可能会导致脏读、幻读或不可重复读，因此很少使用该隔离级别。比如PostgreSQL实际上并没有此级别。   
3. ISOLATION_READ_COMMITTED：该隔离级别表示一个事务只能读取另一个事务已经提交的数据。允许读取并发事务已经提交的数据，可以阻止脏读，但是幻读或不可重复读仍有可能发生，这也是大多数情况下的推荐值。   
4. ISOLATION_REPEATABLE_READ：该隔离级别表示一个事务在整个过程中可以多次重复执行某个查询，并且每次返回的记录都相同。对同一字段的多次读取结果都是一致的，除非数据是被本身事务自己所修改，可以阻止脏读和不可重复读，但幻读仍有可能发生。  
5. ISOLATION_SERIALIZABLE：最高的隔离级别，完全服从ACID的隔离级别。所有的事务依次逐个执行，这样事务之间就完全不可能产生干扰，即该级别可以防止脏读、不可重复读以及幻读。但是这将严重影响程序的性能。通常情况下也不会用到该级别。  

### 1.3.3. 事务超时  
&emsp; 事务超时指一个事务所允许执行的最长时间，如果超过该时间限制但事务还没有完成，则自动回滚事务。在TransactionDefinition中以int的值来表示超时时间，其单位是秒。  
&emsp; 默认设置为底层事务系统的超时值，如果底层数据库事务系统没有设置超时值，那么就是none，没有超时限制。  

### 1.3.4. 事务只读属性  
&emsp; 只读事务用于客户代码只读但不修改数据的情形，只读事务用于特定情景下的优化，比如使用Hibernate的时候。默认为读写事务。  
&emsp; @Transactional(propagation=Propagation.NOT_SUPPORTED，readOnly=true)只读标志只在事务启动时应用，否则即使配置也会被忽略。启动事务会增加线程开销，数据库因共享读取而锁定(具体跟数据库类型和事务隔离级别有关)。通常情况下，仅是读取数据时，不必设置只读事务而增加额外的系统开销。  

&emsp; 从这一点设置的时间点开始(时间点a)到这个事务结束的过程中，其他事务所提交的数据，该事务将看不见！(查询中不会出现别人在时间点a之后提交的数据)  

&emsp; 应用场合：  

* 如果一次执行单条查询语句，则没有必要启用事务支持，数据库默认支持SQL执行期间的读一致性；   
* **<font color = "clime">如果一次执行多条查询语句，例如统计查询，报表查询，在这种场景下，多条查询SQL必须保证整体的读一致性，否则，在前条SQL查询之后，后条SQL查询之前，数据被其他用户改变，则该次整体的统计查询将会出现读数据不一致的状态，此时，应该启用事务支持。</font>**  
&emsp; 【注意是一次执行多次查询来统计某些信息，这时为了保证数据整体的一致性，要用只读事务】  

&emsp; **★★★<font color = "clime">在将事务设置成只读后，相当于将数据库设置成只读数据库，此时若要进行写的操作，会出现错误。</font>**  

## 1.4. Spring事务其他使用  
1. **如何通过日志判断事务是否已经被Spring所管理？**  
    1. 在logback或者log4j中对org.springframework.aop、org.springframework.transaction、org.springframework.jdbc、org.mybatis.spring.transaction进行DEBUG级别日志跟踪(开发期)。  
    2. 查看日志中是否有事务管理、开启、提交、回滚等字符，如：

            DEBUG o.m.spring.transaction.SpringManagedTransaction - JDBC Connection [com.alibaba.druid.proxy.jdbc.ConnectionProxyImpl@28cfe912] will be managed by Spring  
    3. 没有被控制的时候，日志如下：

            DEBUG o.m.spring.transaction.SpringManagedTransaction - JDBC Connection [com.alibaba.druid.proxy.jdbc.ConnectionProxyImpl@28cfe912] will not be managed by Spring

2.**如何通过程序判断是否存在事务？**  
&emsp; `boolean flag = TransactionSynchronizationManager.isActualTransactionActive();`返回true，则在事务控制下，否则不在控制下。  

<!-- 
1.6. Spring的事务管理器  
&emsp; Spring并不直接管理事务，而是提供了多种事务管理器，它们将事务管理的职责委托给JTA或其他持久化机制所提供的平台相关的事务实现。每个事务管理器都会充当某一特定平台的事务实现的门面，这使得用户在Spring中使用事务时，几乎不用关注实际的事务实现是什么。  

&emsp; Spring提供了许多内置事务管理器实现：  

* DataSourceTransactionManager：位于org.springframework.jdbc.datasource包中，数据源事务管理器，提供对单个javax.sql.DataSource事务管理，用于Spring JDBC抽象框架、iBATIS或MyBatis框架的事务管理；  
* JdoTransactionManager：位于org.springframework.orm.jdo包中，提供对单个javax.jdo.PersistenceManagerFactory事务管理，用于集成JDO框架时的事务管理；  
* JpaTransactionManager：位于org.springframework.orm.jpa包中，提供对单个javax.persistence.EntityManagerFactory事务支持，用于集成JPA实现框架时的事务管理；  
* HibernateTransactionManager：位于org.springframework.orm.hibernate3包中，提供对单个org.hibernate.SessionFactory事务支持，用于集成Hibernate框架时的事务管理；该事务管理器只支持Hibernate3+版本，且Spring3.0+版本只支持Hibernate 3.2+版本；  
* JtaTransactionManager：位于org.springframework.transaction.jta包中，提供对分布式事务管理的支持，并将事务管理委托给Java EE应用服务器事务管理器；  
* OC4JjtaTransactionManager：位于org.springframework.transaction.jta包中，Spring提供的对OC4J10.1.3+应用服务器事务管理器的适配器，此适配器用于对应用服务器提供的高级事务的支持；  
* WebSphereUowTransactionManager：位于org.springframework.transaction.jta包中，Spring提供的对WebSphere 6.0+应用服务器事务管理器的适配器，此适配器用于对应用服务器提供的高级事务的支持；  
* WebLogicJtaTransactionManager：位于org.springframework.transaction.jta包中，Spring提供的对WebLogic 8.1+应用服务器事务管理器的适配器，此适配器用于对应用服务器提供的高级事务的支持。  

&emsp; Spring不仅提供这些事务管理器，还提供对如JMS事务管理的管理器等，Spring提供一致的事务抽象如下图所示。  
![image](http://182.92.69.8:8081/img/SSM/AOP/aop-9.png)  
-->

## 1.5. ★★★Spring事务失效  
&emsp; 参考[Spring事务失效](/docs/SSM/Spring/SpringTransactionInvalid.md)  

