

<!-- TOC -->

- [1. 数据库分布式实现](#1-数据库分布式实现)
    - [1.1. 数据库中间件](#11-数据库中间件)
        - [1.1.1. 设计方案](#111-设计方案)
            - [1.1.1.1. 代理模式](#1111-代理模式)
            - [1.1.1.2. 客户端分片模式](#1112-客户端分片模式)
        - [1.1.2. 业界产品](#112-业界产品)
    - [1.2. SpringAOP实现分布式数据源](#12-springaop实现分布式数据源)
    - [1.3. MyBatis插件实现分布式数据源](#13-mybatis插件实现分布式数据源)

<!-- /TOC -->

&emsp; **<font color = "clime">总结：</font>**  
1. **<font color = "red">典型的数据库中间件设计方案有2种：代理proxy、客户端分片smart-client。</font>** 
2. SpringAOP实现分布式数据源
3. MyBatis插件实现分布式数据源


# 1. 数据库分布式实现  
<!-- 
sharding-proxy实战：解救分表后痛苦的测试小姐姐
https://mp.weixin.qq.com/s?__biz=MzU5ODUwNzY1Nw==&mid=2247484440&idx=1&sn=ddc246e4d5d6c8ac2d5b8debb5ebaeae&chksm=fe426dfec935e4e8f7b831f7b00f3e79f34341f67ee27e440b7c617d0a75c01366a2b5356496&mpshare=1&scene=1&srcid=&sharer_sharetime=1566434225077&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=a98b434d6faae61683de99c3737998f4d1f939ad8d7e26f07d46cbfc361389785f9b4717785a9262c4e6f819f005d8fb1fc5144dd05699bec5dd22d93a868baed6ed9a1fb1ad6024e0d62affe87a143e&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=OzH8r4s9Va6DEZaWxmB%2BZFeLRG%2Fr0XoTKeHpvWhKxz6B1yLq0M6Eiym92%2FXw0mmm

-->


## 1.1. 数据库中间件  
&emsp; 数据库中间件的主要作用是向应用程序开发人员屏蔽读写分离和分库分表面临的挑战，并隐藏底层实现细节，使得开发人员可以像操作单库单表那样去操作数据。  
&emsp; **<font color = "clime">除了数据库中间件之外还可以使用SpringAOP、ORM框架代理来实现多数据源读写分离。</font>**  

### 1.1.1. 设计方案  
&emsp; **<font color = "red">典型的数据库中间件设计方案有2种：代理proxy、客户端分片smart-client。</font>** 下图演示了这两种方案的架构：  
![image](http://182.92.69.8:8081/img/SQL/sql-25.png)  
&emsp; 不论是proxy还是smart-client，底层都操作了多个数据库实例。不论是分库分表，还是读写分离，都是在数据库中间件层面对业务开发同学进行屏蔽。  

#### 1.1.1.1. 代理模式  
&emsp; 独立部署一个代理服务，这个代理服务背后管理多个数据库实例。而在应用中，通过一个普通的数据源(c3p0、druid、dbcp等)与代理服务器建立连接，所有的sql操作语句都是发送给这个代理，由这个代理去操作底层数据库，得到结果并返回给应用。在这种方案下，分库分表和读写分离的逻辑对开发人员是完全透明的。  

&emsp; **优点：**  
1. 多语言支持。不论使用的是php、java或是其他语言，都可以支持。以mysql数据库为例，如果proxy本身实现了mysql的通信协议，那么可以就将其看成一个mysql 服务器。mysql官方团队为不同语言提供了不同的客户端却动，如java语言的mysql-connector-java，python语言的mysql-connector-python等等。因此不同语言的开发者都可以使用mysql官方提供的对应的驱动来与这个代理服务器建通信。  
2. 对业务开发人员透明。由于可以把proxy当成mysql服务器，理论上业务同学不需要进行太多代码改造，既可以完成接入。  

&emsp; **缺点：**  
1. 实现复杂。因为proxy需要实现被代理的数据库server端的通信协议，实现难度较大。通常看到一些proxy模式的数据库中间件，实际上只能代理某一种数据库，如mysql。几乎没有数据库中间件可以同时代理多种数据库(sqlserver、PostgreSQL、Oracle)。  
2. proxy本身需要保证高可用。由于应用本来是直接访问数据库，现在改成了访问proxy，意味着proxy必须保证高可用。否则，数据库没有宕机，proxy挂了，会导致数据库无法正常访问。  
3. 租户隔离。可能有多个应用访问proxy代理的底层数据库，必然会对proxy自身的内存、网络、cpu等产生资源竞争，proxy需要需要具备隔离的能力。  

#### 1.1.1.2. 客户端分片模式  
&emsp; 业务代码需要进行一些改造，引入支持读写分离或者分库分表的功能的sdk，这个就是smart-client。通常smart-client是在连接池或者driver的基础上进行了一层封装，smart-client内部与不同的库建立连接。应用程序产生的sql交给smart-client进行处理，其内部对sql进行必要的操作，例如在读写分离情况下，选择走从库还是主库；在分库分表的情况下，进行sql解析、sql改写等操作，然后路由到不同的分库，将得到的结果进行合并，返回给应用。  

&emsp; **优点：**  
1. 实现简单。proxy需要实现数据库的服务端协议，但是smart-client不需要实现客户端通信协议。原因在于，大多数据数据库厂商已经针对不同的语言提供了相应的数据库驱动driver，例如mysql针对java语言提供了mysql-connector-java驱动，针对python提供了mysql-connector-python驱动，客户端的通信协议已经在driver层面做过了。因此smart-client模式的中间件，通常只需要在此基础上进行封装即可。  
2. 天然去中心化。smart-client的方式，由于本身以sdk的方式，被应用直接引入，随着应用部署到不同的节点上，且直连数据库，中间不需要有代理层。因此相较于proxy而言，除了网络资源之外，基本上不存在任何其他资源的竞争，也不需要考虑高可用的问题。只要应用的节点没有全部宕机，就可以访问数据库。(这里的高可用是相比proxy而言，数据库本身的高可用还是需要保证的)  

&emsp; **缺点：**  
1. 通常仅支持某一种语言。例如tddl、zebra、sharding-jdbc都是使用java语言开发，因此对于使用其他语言的用户，就无法使用这些中间件。如果其他语言要使用，那么就要开发多语言客户端。  
2. 版本升级困难。因为应用使用数据源代理就是引入一个jar包的依赖，在有多个应用都对某个版本的jar包产生依赖时，一旦这个版本有bug，所有的应用都需要升级。而数据库代理升级则相对容易，因为服务是单独部署的，只要升级这个代理服务器，所有连接到这个代理的应用自然也就相当于都升级了。  

### 1.1.2. 业界产品  
&emsp; 无论是proxy，还是smart-client，二者的作用都是类似的。以下列出了这两种方案目前已有的实现以及各自的优缺点：  
![image](http://182.92.69.8:8081/img/SQL/sql-26.png)  
&emsp; proxy实现，目前的已有的实现方案有：  

    阿里巴巴开源的cobar  
    阿里云上的drds  
    mycat团队在cobar基础上开发的mycat  
    mysql官方提供的mysql-proxy  
    奇虎360在mysql-proxy基础开发的atlas(只支持分表，不支持分库)  
    当当网开源的sharing-sphere  
    目前除了mycat、sharing-sphere，其他几个开源项目基本已经没有维护。  

&emsp; smart-client实现，目前的实现方案有：  

    阿里巴巴开源的tddl，已很久没维护
    大众点评开源的zebra，大众点评的zebra开源版本代码已经很久没有更新，不过最近美团上市，重新开源大量内部新的功能特性，并计划长期维持。
    当当网开源的sharding-jdbc，目前算是做的比较好的，文档资料比较全。和sharding-sphere一起进入了Apache孵化器。
    蚂蚁金服的zal

&emsp; 综上，现在其实建议考量的，就是sharding-jdbc和mycat，这两个都可以去考虑使用。  
&emsp; sharding-jdbc这种client层方案的优点在于不用部署，运维成本低，不需要代理层的二次转发请求，性能很高，但是如果遇到升级啥的需要各个系统都重新升级版本再发布，各个系统都需要耦合sharding-jdbc的依赖；  
&emsp; mycat这种proxy层方案的缺点在于需要部署，自己运维一套中间件，运维成本高，但是好处在于对于各个项目是透明的，如果遇到升级之类的都是自己中间件那里搞就行了。  
&emsp; 通常来说，这两个方案其实都可以选用，但是个人建议中小型公司选用sharding-jdbc，client层方案轻便，而且维护成本低，不需要额外增派人手，而且中小型公司系统复杂度会低一些，项目也没那么多；但是中大型公司最好还是选用 mycat 这类 proxy 层方案，因为可能大公司系统和项目非常多，团队很大，人员充足，那么最好是专门弄个人来研究和维护 mycat，然后大量项目直接透明使用即可。  

---
## 1.2. SpringAOP实现分布式数据源   
1. Spring整合多数据源：  
    1. 静态配置。多个不同的数据源，不同的sessionFactory。不支持分布式事务。  
    2. 建立动态数据源类(<font color = "red">类继承AbstractRoutingDataSource，且实现方法determineCurrentLookupKey</font>)，一个数据源类管理多个数据库。支付分布式事务。  
    
    可以使用SpringAOP拦截、自定义注解实现动态切换数据源。  
    
2. SpringAOP也可以实现读写分离。  
    1. AOP和注解实现动态数据源切换配置  
    2. AOP实现读写分离  

---
## 1.3. MyBatis插件实现分布式数据源   
&emsp; ...
