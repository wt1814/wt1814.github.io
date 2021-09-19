 
<!-- TOC -->

- [1. ~~RPC~~](#1-rpc)
    - [1.1. 本地调用和远程调用](#11-本地调用和远程调用)
        - [1.1.1. 本地调用和远程调用](#111-本地调用和远程调用)
        - [1.1.2. RPC](#112-rpc)
    - [1.2. RPC起源](#12-rpc起源)
    - [1.3. ★★★RPC调用过程](#13-★★★rpc调用过程)
    - [1.4. ★★★RPC框架使用了哪些技术？](#14-★★★rpc框架使用了哪些技术)
        - [1.4.1. 远程代理对象(动态代理)](#141-远程代理对象动态代理)
        - [1.4.2. 序列化](#142-序列化)
        - [1.4.3. 通信](#143-通信)
            - [1.4.3.1. RPC中的通信协议](#1431-rpc中的通信协议)
        - [1.4.4. 服务暴露(服务注册中心)](#144-服务暴露服务注册中心)
    - [1.5. 流行的RPC框架](#15-流行的rpc框架)
    - [1.6. ~~RPC调用和HTTP调用~~](#16-rpc调用和http调用)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. RPC流程：  
2. RPC框架使用了哪些技术？  


# 1. ~~RPC~~  
&emsp; RPC，远程过程调用，屏蔽了传输协议，像本地调用一样进行远程通信。  

## 1.1. 本地调用和远程调用 
### 1.1.1. 本地调用和远程调用
&emsp; 远程是相对于本地来说的，有远程调用就有本地调用，那么先说说本地调用是什么。    
&emsp; 比如下图，代码在同一个进程中（或者说同一个地址空间）调用另外一个方法，得到需要的结果，这就是本地调用。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/RPC/rpc-8.png)   
&emsp; 那么想象一下，如果这里的add方法是一个很复杂的方法，很多系统都想用这个方法，那么可以把这个方法单独拆成一个服务，提供给各个系统进行调用，那么本地就会变成远程，就会变成这样：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/RPC/rpc-9.png)   

### 1.1.2. RPC
&emsp; **RPC：让远程调用变得和本地调用一样。**  
&emsp; 那么在 Server_A中怎么调用Server_B中的add方法呢？  
&emsp; 很多人都会想到Server_B封装一个接口，通过服务把这个方法暴露出去，比如通过HTTP请求，那么Server_A就可以调用Server_B中的add方法了。  
&emsp; 通过这种方法实现起来没有问题，也是一个不错的解决方法，就是在每次调用的时候，都要发起HTTP请求，代码里面要写HttpClient.sendRequest这样的代码，那么有没有可能像调用本地一样，去发起远程调用呢？让程序员不知道这是调用的远程方法呢？这时候就要提到RPC了。  

## 1.2. RPC起源  
&emsp; RPC这个概念术语在上世纪80年代由Bruce Jay Nelson提出。这里追溯下当初开发RPC的原动机是什么？在Nelson的论文"Implementing Remote Procedure Calls"中，他提到了几点：  

* 简单：RPC概念的语义十分清晰和简单，这样建立分布式计算就更容易。
* 高效：过程调用看起来十分简单而且高效。
* 通用：在单机计算中过程往往是不同算法部分间最重要的通信机制。

&emsp; 通俗一点说，就是一般程序员对于本地的过程调用很熟悉，那么把RPC作成和本地调用完全类似，那么就更容易被接受，使用起来毫无障碍。Nelson的论文其观点今天看来确实高瞻远瞩，今天使用的RPC框架基本就是按这个目标来实现的。  

## 1.3. ★★★RPC调用过程
<!--
https://blog.csdn.net/u013474436/article/details/105059839

RPC(三)《Implementing Remote Procedure Calls》译文
https://www.jianshu.com/p/91be39f72c74?utm_content=note&utm_medium=reader_share&utm_source=weixin
-->
&emsp; 一个基本的RPC架构里面应该至少包含以下4个组件：  
1. 客户端(Client)：服务调用方(服务消费者)。  
2. <font color = "red">客户端存根(Client Stub)：存放服务端地址信息，将客户端的请求参数数据信息打包成网络消息，再通过网络传输发送给服务端。</font>  
3. <font color = "red">服务端存根(Server Stub)：接收客户端发送过来的请求消息并进行解包，然后再调用本地服务进行处理。</font>  
4. 服务端(Server)：服务的真正提供者。  

&emsp; 具体的调用过程如下：  
1. 服务消费者(client客户端)通过本地调用的方式调用服务。  
2. **客户端存根(client stub)接收到调用请求后负责将方法、入参等信息序列化(组装)成能够进行网络传输的消息体。**  
3. 客户端存根(client stub)找到远程的服务地址，并且将消息通过网络发送给服务端。  
4. 服务端存根(server stub)收到消息后进行解码(反序列化操作)。  
5. 服务端存根(server stub)根据解码结果调用本地的服务进行相关处理。  
6. 本地服务执行具体业务逻辑。
7. 并将处理结果返回给服务端存根(server stub)。   
8. **服务端存根(server stub)将返回结果重新打包成消息(序列化)并通过网络发送至消费方。**  
9. 服务端(server)通过sockets将消息发送到客户端。
10. 客户端存根(client stub)接收到消息，并进行解码(反序列化)。  
11. 服务消费方得到最终结果。  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/RPC/rpc-6.png)  
&emsp; 而RPC框架的实现目标则是将上面的第2-10步完好地封装起来，也就是把调用、编码/解码的过程给封装起来，让用户感觉上像调用本地服务一样的调用远程服务。  


-----------

&emsp; Nelson的论文中指出 **<font color = "red">实现RPC的程序包括5个部分：1. User、2. User-stub、3. RPCRuntime(RPC通信包)、4. Server-stub、5. Server。</font>**  
&emsp; 这5个部分的关系如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/RPC/rpc-1.png)   
&emsp; 这里user就是client端，当user发起一个远程调用时，它实际是通过本地调用user-stub。user-stub负责将调用的接口、方法和参数通过约定的协议规范进行编码并通过本地的RPCRuntime实例传输到远端的实例。远端RPCRuntime实例收到请求后交给server-stub进行解码后发起本地端调用，调用结果再返回给user端。  


--------

&emsp; 完整的RPC过程，如图：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/RPC/rpc-10.png)  

* 服务调用方(Client)调用以本地调用方式调用服务；
* Client stub负责将方法名、参数组装成消息体并进行序列化，找到服务地址，将消息发送到服务端；
* Server stub收到消息后进行反序列化后调用本地的服务；
* 本地服务执行，将结果返回给Server stub；
* Server stub将运行结果打包成消息序列化后，发送调用方；
* Client stub接收到消息，并进行反序列化，调用方最终得到调用结果。

&emsp; 总结来说， **<font color = "red">RPC用于服务之间的调用问题，特别是分布式环境；RPC让远程调用时，像调用本地方法一样方便和无感知；RPC框架屏蔽了很多底层的细节，不需要开发人员关注这些细节，比如序列化和反序列化、网络传输协议的细节。</font>**  

----
~~RPC结构拆解~~  
&emsp; 如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/RPC/rpc-3.png)   
&emsp; RPC服务方通过RpcServer去导出(export)远程接口方法，而客户方通过RpcClient去引入(import)远程接口方法。客户方像调用本地方法一样去调用远程接口方法，RPC框架提供接口的代理实现，实际的调用将委托给代理RpcProxy。代理封装调用信息并将调用转交给RpcInvoker 去实际执行。在客户端的RpcInvoker通过连接器RpcConnector去维持与服务端的通道RpcChannel，并使用RpcProtocol执行协议编码(encode)并将编码后的请求消息通过通道发送给服务方。  
&emsp; RPC服务端接收器RpcAcceptor接收客户端的调用请求，同样使用RpcProtocol执行协议解码(decode)。解码后的调用信息传递给RpcProcessor去控制处理调用过程，最后再委托调用给RpcInvoker去实际执行并返回调用结果。  

&emsp; 如下是各个部分的详细职责：  

1. RpcServer：负责导出(export)远程接口。  
2. RpcClient：负责导入(import)远程接口的代理实现。  
3. RpcProxy：远程接口的代理实现。  
4. RpcInvoker：  
    * 客户方实现：负责编码调用信息和发送调用请求到服务方并等待调用结果返回。  
    * 服务方实现：负责调用服务端接口的具体实现并返回调用结果。  
5. RpcProtocol：负责协议编/解码。  
6. RpcConnector：负责维持客户方和服务方的连接通道和发送数据到服务方。  
7. RpcAcceptor：负责接收客户方请求并返回请求结果。  
8. RpcProcessor：负责在服务方控制调用过程，包括管理调用线程池、超时时间等。  
9. RpcChannel：数据传输通道。  

&emsp; RPC的设计由Client，Client stub，Network，Server stub，Server构成。其中Client就是用来调用服务的，Cient stub是用来把调用的方法和参数序列化的(因为要在网络中传输，必须要把对象转变成字节)，Network用来传输这些信息到Server stub，Server stub用来把这些信息反序列化的，Server就是服务的提供者，最终调用的就是Server提供的方法。  

1. Client像调用本地服务似的调用远程服务；
2. Client stub接收到调用后，将方法、参数序列化；
3. 客户端通过sockets将消息发送到服务端；
4. Server stub 收到消息后进行解码(将消息对象反序列化)；
5. Server stub 根据解码结果调用本地的服务本地服务执行(对于服务端来说是本地执行)并将结果返回给Server stub；
6. Server stub将返回结果打包成消息(将结果消息对象序列化)；
7. 服务端通过sockets将消息发送到客户端；
8. Client stub接收到结果消息，并进行解码(将结果消息发序列化)；
9. 客户端得到最终结果。

## 1.4. ★★★RPC框架使用了哪些技术？  
&emsp; 一个比较完善的RPC框架  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/RPC/rpc-7.png)  

&emsp; RPC框架需要解决的问题？  
1. 如何确定客户端和服务端之间的通信协议？  
2. 如何更高效地进行网络通信？  
3. 服务端提供的服务如何暴露给客户端？  
4. 客户端如何发现这些暴露的服务？  
5. 如何更高效地对请求对象和响应结果进行序列化和反序列化操作？  

### 1.4.1. 远程代理对象(动态代理)
&emsp; **<font color = "clime">服务调用者调用的服务实际是远程服务的本地代理。生成Client Stub(客户端存根)和Server Stub(服务端存根)的时候需要用到java动态代理技术，可以使用jdk提供的原生的动态代理机制，也可以使用开源的：Cglib代理，Javassist字节码生成技术。</font>**   

### 1.4.2. 序列化
&emsp; 在网络中，所有的数据都将会被转化为字节进行传送，所以为了能够使参数对象在网络中进行传输，需要对这些参数进行序列化和反序列化操作。

* 序列化：把对象转换为字节序列的过程称为对象的序列化，也就是编码的过程。
* 反序列化：把字节序列恢复为对象的过程称为对象的反序列化，也就是解码的过程。

&emsp; 目前比较高效的开源序列化框架：如Kryo、fastjson和Protobuf等。

### 1.4.3. 通信
&emsp; **通信方式：**  
&emsp; 出于并发性能的考虑，传统的阻塞式IO显然不太合适，因此需要异步的IO，即NIO。  
&emsp; Java提供了NIO的解决方案，Java 7也提供了更优秀的NIO.2支持。可以选择Netty或者mina来解决NIO数据传输的问题。
 
&emsp; **通信协议：**  
&emsp; <font color = "clime">RPC框架与具体的协议无关。RPC 可基于HTTP或TCP协议。</font>Web Service就是基于HTTP协议的RPC，它具有良好的跨平台性，但其性能却不如基于TCP协议的RPC。  

#### 1.4.3.1. RPC中的通信协议  
<!-- 
~~
https://blog.csdn.net/u013952133/article/details/79256799
-->

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/RPC/rpc-5.png)   
&emsp; RPC支持哪些协议？最早的CORBA、Java RMI，WebService方式的RPC风格，Hessian,Thrift甚至Rest API。  

* 基于TCP的RPC  
&emsp; 最简单的方式，例如基于Java Socket API实现RPC，一种非常典型的CS架构，client携带参数和调用方法名请求server，server使用一个while循环来监听客户端请求并予以处理；再往下延伸就涉及到当client请求并发数很大时，是用阻塞IO还是非阻塞IO？如何做服务路由以及负载均衡来将请求分发到多个server？轮询法？  
* 基于HTTP的RPC  
&emsp; 基于HTTP一定程度上就是为了节省在底层细节上的关注，而可以去利用更高层的协议和现有的开源库去实现RPC，例如基于HttpClient库去实现RPC，基于json或者xml作为序列化之后的传输格式，当然这也会带来效率低，定制化程度低等弊端。  

&emsp; **RESTFul和RPC形式url**  
&emsp; RESTFul把所有网络上的实体作为资源，具体的资源通过不同的格式作为表现层，例如图片的表现层可能是jpg，也可能是png；然后通过http协议的常用操作方式(例如GET、POST等)来改变和转换资源状态，也就是表现层转换  
&emsp; 而传统的RPC形式url会把操作类型、需要远程调用的服务接口名、参数都通过queryString携带到服务端，RESTFul则把操作类型放到了http请求方式中，使得url更加简洁，只留下一部分参数在url中  

&emsp; 比如按照id查找用户：  

* 如果是RPC风格的url应该是这样的：GET /queryUser?userid=xxx；
* 而RESTful风格通常是这样的：GET /user/{userid}

<!-- 
RPC：面向过程，也就是要做一件什么事情，只发送 GET 和 POST 请求；GET 用来查询信息，其他情况下一律用 POST；请求参数是动词。
RESTful：面向资源，这里的资源可以是一段文字、一个文件、一张图片，总之是一个具体的存在，可以使用 GET、POST、DELETE、PUT 请求，对应了增删查改的操作；请求参数是名词。

既然有 HTTP ，为什么还要用 RPC ？

其实，这个问题本身就是有问题的！

HTTP 和 RPC 并不是两个并行的概念，虽然很多书或文章，都介绍 HTTP 和 RPC 是在“应用层”，但实际上可以把应用层细分成多层，RPC 的所处的位置是高于 HTTP 的；HTTP 是网络协议，而RPC 可以看做是一种编程模式或实现方案；

RPC 通常包含传输协议和序列化协议，单说传输协议，RPC 可以建立在 TCP 协议之上(比如 Dubbo)，也可以建立在 HTTP 协议之上(比如 gRPC)；如果是基于数据形式分类，RPC 又可以分成基于二进制、XML 和 JSON 三种；

而现在非常流行的开源 RPC 框架，比如上文中提到的Dubbo 和 gRPC 分别出身于阿里和谷歌，它们更多地是封装了服务注册发现、负载均衡、链路跟踪等功能，也可以这么理解，RPC 框架是对服务更高级的封装。

03

RPC VS Restful 风格的 API


RPC：面向过程，也就是要做一件什么事情，只发送 GET 和 POST 请求；GET 用来查询信息，其他情况下一律用 POST；请求参数是动词。

RESTful：面向资源，这里的资源可以是一段文字、一个文件、一张图片，总之是一个具体的存在，可以使用 GET、POST、DELETE、PUT 请求，对应了增删查改的操作；请求参数是名词。

比如按照id 查找用户：

    如果是 RPC 风格的 url 应该是这样的：GET /queryUser?userid=xxx；
    而 RESTful 风格通常是这样的：GET /user/{userid}

当然，对于遵守接口风格这一点，我个人是保留意见的，在实际的项目开发过程中，很多时候这些接口风格过于理想化；有些东西借鉴一下可以，更多的还需要结合项目实际使用。
-->

### 1.4.4. 服务暴露(服务注册中心)
&emsp; 可选：Redis、Zookeeper、Consul、Etcd。  
&emsp; 一般使用ZooKeeper提供服务注册与发现功能，解决单点故障以及分布式部署的问题(注册中心)。  

## 1.5. 流行的RPC框架
<!-- 
Dubbo  
Dubbo 是阿里巴巴公司开源的一个Java高性能优秀的服务框架，使得应用可通过高性能的 RPC 实现服务的输出和输入功能，可以和 Spring框架无缝集成。目前已经进入Apache孵化器。  
Motan  
Motan是新浪微博开源的一个Java RPC框架。2016年5月开源。Motan 在微博平台中已经广泛应用，每天为数百个服务完成近千亿次的调用。  
Thrift  
Thrift是Apache的一个跨语言的高性能的服务框架，也得到了广泛的应用。  

阿里巴巴 Dubbo：https://github.com/alibaba/dubbo
新浪微博 Motan：https://github.com/weibocom/motan
gRPC：https://github.com/grpc/grpc
rpcx ：https://github.com/smallnest/rpcx
Apache Thrift ：https://thrift.apache.org/
-->
&emsp; 目前常用的RPC框架如下：  
1. Thrift：thrift是一个软件框架，用来进行可扩展且跨语言的服务的开发。它结合了功能强大的软件堆栈和代码生成引擎，以构建在C++, Java, Python, PHP, Ruby, Erlang, Perl, Haskell, C#, Cocoa, JavaScript, Node.js, Smalltalk, and OCaml这些编程语言间无缝结合的、高效的服务。  
2. Dubbo：Dubbo是一个分布式服务框架，以及SOA治理方案。其功能主要包括：高性能NIO通讯及多协议集成，服务动态寻址与路由，软负载均衡与容错，依赖分析与降级等。Dubbo是阿里巴巴内部的SOA服务化治理方案的核心框架，Dubbo自2011年开源后，已被许多非阿里系公司使用。  
3. gRPC是Google开发的高性能、通用的开源RPC框架，其由Google主要面向移动应用开发并基于HTTP/2协议标准而设计，基于ProtoBuf(Protocol Buffers)序列化协议开发，且支持众多开发语言。本身它不是分布式的，所以要实现上面的框架的功能需要进一步的开发。  


&emsp; SPringCloud与RPC：  
&emsp; Spring Cloud中feign的远程调用和rpc调用非常契合。但spring cloud更是一个微服务全家桶框架。    

<!-- 
 RPC和消息队列的差异  
1. 功能差异  
    &emsp; 在架构上，RPC和Message的差异点是，Message有一个中间结点Message Queue，可以把消息存储。  
    &emsp; 消息的特点：  
    1. Message Queue把请求的压力保存一下，逐渐释放出来，让处理者按照自己的节奏来处理。
    2. Message Queue引入一下新的结点，系统的可靠性会受Message Queue结点的影响。
    3. Message Queue是异步单向的消息。发送消息设计成是不需要等待消息处理的完成。
    所以对于有同步返回需求，用Message Queue则变得麻烦了。  

    &emsp; RPC的特点：  
    &emsp; 同步调用，对于要等待返回结果/处理结果的场景，RPC是可以非常自然直觉的使用方式(RPC也可以是异步调用)。  
    &emsp; 由于等待结果，Consumer(Client)会有线程消耗。如果以异步RPC的方式使用，Consumer(Client)线程消耗可以去掉。但不能做到像消息一样暂存消息/请求，压力会直接传导到服务Provider。  
2. 适用场合差异  
    1. 希望同步得到结果的场合，RPC合适。  
    2. 希望使用简单，则RPC；RPC操作基于接口，使用简单，使用方式模拟本地调用。异步的方式编程比较复杂。  
    3. 不希望发送端(RPC Consumer、Message Sender)受限于处理端(RPC Provider、Message Receiver)的速度时，使用Message Queue。  
    随着业务增长，有的处理端处理量会成为瓶颈，会进行同步调用到异步消息的改造。这样的改造实际上有调整业务的使用方式。比如原来一个操作页面提交后就下一个页面会看到处理结果；改造后异步消息后，下一个页面就会变成“操作已提交，完成后会得到通知”。  
3. 不适用场合说明  
    1. RPC同步调用使用Message Queue来传输调用信息。上面分析可以知道，这样的做法，发送端是在等待，同时占用一个中间点的资源。变得复杂了，但没有对等的收益。  
    2. 对于返回值是void的调用，可以这样做，因为实际上这个调用业务上往往不需要同步得到处理结果的，只要保证会处理即可。(RPC的方式可以保证调用返回即处理完成，使用消息方式后这一点不能保证了。)  
    3. 返回值是void的调用，使用消息，效果上是把消息的使用方式Wrap成了服务调用(服务调用使用方式成简单，基于业务接口)。  
-->


## 1.6. ~~RPC调用和HTTP调用~~
&emsp; RPC服务和HTTP服务还是存在很多的不同点的，一般来说，RPC服务主要是针对大型企业的，而HTTP服务主要是针对小企业的，因为RPC效率更高，而HTTP服务开发迭代会更快。  
