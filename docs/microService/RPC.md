

<!-- 
SpringCloud远程服务调用的方式Rpc和Http
https://mp.weixin.qq.com/s/ZKdxCBEYsx5lmebp8me2Ow
体系化认识RPC
https://mp.weixin.qq.com/s?__biz=MzA4MTk3MjI0Mw==&mid=2247487041&idx=1&sn=7742a9e8df2bb47472c064e5ec25866e&chksm=9f8d933da8fa1a2b10e41ab0d31c75baeedb7f7c76e3660388e80b2a92e44063c4740195915a&mpshare=1&scene=1&srcid=&sharer_sharetime=1572221865284&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=dee829c9aae7a0c0f26a1e6ca75852420e491b0e4037182d8f43b979119437766463ad5116936c4113026a7e3d64d587231b537284aef4b381170f3cb6d95a93cc02bdff339ca2371f81c45b42a3fc7a&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070152&lang=zh_CN&pass_ticket=qog0AK97Tys6DnkOvI2yc17h%2FAdSYaR8ZyVIoCUAs7RkIqyc3NrdPFKrunhH3JpD
RPC定义和原理
http://www.360doc.cn/mip/805789876.html
RPC框架实现原理 
https://mp.weixin.qq.com/s/weJGI8awKcxHT9ppzKveBQ

 “RPC好，还是RESTful好？”，这个问题不简单！ 
https://mp.weixin.qq.com/s/gLxFvOo82xybsribOIXKYA

RPC
https://zhuanlan.zhihu.com/p/98423247


-->


<!-- 
既然有 HTTP ，为什么还要用 RPC ？

其实，这个问题本身就是有问题的！
HTTP 和 RPC 并不是两个并行的概念，虽然很多书或文章，都介绍 HTTP 和 RPC 是在“应用层”，但实际上可以把应用层细分成多层，RPC 的所处的位置是高于 HTTP 的；HTTP 是网络协议，而RPC 可以看做是一种编程模式或实现方案；
RPC 通常包含传输协议和序列化协议，单说传输协议，RPC 可以建立在 TCP 协议之上（比如 Dubbo），也可以建立在 HTTP 协议之上（比如 gRPC）；如果是基于数据形式分类，RPC 又可以分成基于二进制、XML 和 JSON 三种；
而现在非常流行的开源 RPC 框架，比如上文中提到的Dubbo 和 gRPC 分别出身于阿里和谷歌，它们更多地是封装了服务注册发现、负载均衡、链路跟踪等功能，也可以这么理解，RPC 框架是对服务更高级的封装。  

RPC VS Restful 风格的 API
RPC：面向过程，也就是要做一件什么事情，只发送 GET 和 POST 请求；GET 用来查询信息，其他情况下一律用 POST；请求参数是动词。
RESTful：面向资源，这里的资源可以是一段文字、一个文件、一张图片，总之是一个具体的存在，可以使用 GET、POST、DELETE、PUT 请求，对应了增删查改的操作；请求参数是名词。
比如按照id 查找用户：

    如果是 RPC 风格的 url 应该是这样的：GET /queryUser?userid=xxx；
    而 RESTful 风格通常是这样的：GET /user/{userid}
当然，对于遵守接口风格这一点，我个人是保留意见的，在实际的项目开发过程中，很多时候这些接口风格过于理想化；有些东西借鉴一下可以，更多的还需要结合项目实际使用。


-->

# RPC  

<!-- 

目录
https://time.geekbang.org/column/intro/100046201
-->

## RPC与REST  
<!-- 
 
https://blog.csdn.net/u013952133/article/details/79256799
-->

## RPC定义  

RPC是什么？  

RPC（Remote Procedure Call Protocol）——远程过程调用协议，它是一种通过网络从远程计算机程序上请求服务，而不需要了解底层网络技术的协议。RPC协议假定某些传输协议的存在，如TCP或UDP，为通信程序之间携带信息数据。在OSI网络通信模型中，RPC跨越了传输层和应用层。RPC使得开发包括网络分布式多程序在内的应用程序更加容易。  
RPC采用客户机/服务器模式。请求程序就是一 个客户机，而服务提供程序就是一个服务器。首先，客户机调用进程发送一个有进程参数的调用信息到服务进程，然后等待应答信息。在服务器端，进程保持睡眠状 态直到调用信息到达为止。当一个调用信息到达，服务器获得进程参数，计算结果，发送答复信息，然后等待下一个调用信息，最后，客户端调用进程接收答复信 息，获得进程结果，然后调用执行继续进行。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/RPC/rpc-4.png)   


## RPC架构  

<!-- 
 “RPC好，还是RESTful好？”，这个问题不简单！ 
https://mp.weixin.qq.com/s/gLxFvOo82xybsribOIXKYA
-->
Nelson 的论文中指出实现 RPC 的程序包括 5 个部分：1. User、2. User-stub、3. RPCRuntime、4. Server-stub、5. Server。  
这 5 个部分的关系如下图所示  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/RPC/rpc-1.png)   

这里 user 就是 client 端，当 user 想发起一个远程调用时，它实际是通过本地调用user-stub。user-stub 负责将调用的接口、方法和参数通过约定的协议规范进行编码并通过本地的 RPCRuntime 实例传输到远端的实例。远端 RPCRuntime 实例收到请求后交给 server-stub 进行解码后发起本地端调用，调用结果再返回给 user 端。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/RPC/rpc-2.png)   

粗粒度的 RPC 实现概念结构，这里我们进一步细化它应该由哪些组件构成，如下图所示。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/RPC/rpc-3.png)   


RPC 服务方通过 RpcServer 去导出（export）远程接口方法，而客户方通过 RpcClient 去引入（import）远程接口方法。客户方像调用本地方法一样去调用远程接口方法，RPC 框架提供接口的代理实现，实际的调用将委托给代理RpcProxy 。代理封装调用信息并将调用转交给RpcInvoker 去实际执行。在客户端的RpcInvoker 通过连接器RpcConnector 去维持与服务端的通道RpcChannel，并使用RpcProtocol 执行协议编码（encode）并将编码后的请求消息通过通道发送给服务方。  

RPC 服务端接收器 RpcAcceptor 接收客户端的调用请求，同样使用RpcProtocol 执行协议解码（decode）。解码后的调用信息传递给RpcProcessor 去控制处理调用过程，最后再委托调用给RpcInvoker 去实际执行并返回调用结果。如下是各个部分的详细职责：  

1. RpcServer  

负责导出（export）远程接口  

2. RpcClient  

负责导入（import）远程接口的代理实现  

3. RpcProxy  

远程接口的代理实现  

4. RpcInvoker  

客户方实现：负责编码调用信息和发送调用请求到服务方并等待调用结果返回  

服务方实现：负责调用服务端接口的具体实现并返回调用结果  

5. RpcProtocol  

负责协议编/解码  

6. RpcConnector  

负责维持客户方和服务方的连接通道和发送数据到服务方  

7. RpcAcceptor  

负责接收客户方请求并返回请求结果  

8. RpcProcessor  

负责在服务方控制调用过程，包括管理调用线程池、超时时间等  

9. RpcChannel  

数据传输通道  






## RPC核心技术点  

RPC框架实现的几个核心技术点：  

（1）服务暴露：  

远程提供者需要以某种形式提供服务调用相关的信息，包括但不限于服务接口定义、数据结构、或者中间态的服务定义文件。例如Facebook的Thrift的IDL文件，Web service的WSDL文件；服务的调用者需要通过一定的途径获取远程服务调用相关的信息。  

目前，大部分跨语言平台 RPC 框架采用根据 IDL 定义通过 code generator 去生成 stub 代码，这种方式下实际导入的过程就是通过代码生成器在编译期完成的。代码生成的方式对跨语言平台 RPC 框架而言是必然的选择，而对于同一语言平台的 RPC 则可以通过共享接口定义来实现。这里的导入方式本质也是一种代码生成技术，只不过是在运行时生成，比静态编译期的代码生成看起来更简洁些。  

java 中还有一种比较特殊的调用就是多态，也就是一个接口可能有多个实现，那么远程调用时到底调用哪个？这个本地调用的语义是通过 jvm 提供的引用多态性隐式实现的，那么对于 RPC 来说跨进程的调用就没法隐式实现了。如果前面DemoService 接口有 2 个实现，那么在导出接口时就需要特殊标记不同的实现需要，那么远程调用时也需要传递该标记才能调用到正确的实现类，这样就解决了多态调用的语义问题。  

（2）远程代理对象：  

服务调用者用的服务实际是远程服务的本地代理。说白了就是通过动态代理来实现。  

java 里至少提供了两种技术来提供动态代码生成，一种是 jdk 动态代理，另外一种是字节码生成。动态代理相比字节码生成使用起来更方便，但动态代理方式在性能上是要逊色于直接的字节码生成的，而字节码生成在代码可读性上要差很多。两者权衡起来，个人认为牺牲一些性能来获得代码可读性和可维护性显得更重要。  

（3）通信：  

RPC框架与具体的协议无关。RPC 可基于HTTP 或 TCP 协议，Web Service 就是基于 HTTP 协议的 RPC，它具有良好的跨平台性，但其性能却不如基于 TCP 协议的 RPC。  

1. TCP/HTTP：众所周知，TCP 是传输层协议，HTTP 是应用层协议，而传输层较应用层更加底层，在数据传输方面，越底层越快，因此，在一般情况下，TCP 一定比 HTTP 快。  

2. 消息ID：RPC 的应用场景实质是一种可靠的请求应答消息流，和 HTTP 类似。因此选择长连接方式的 TCP 协议会更高效，与 HTTP 不同的是在协议层面我们定义了每个消息的唯一 id，因此可以更容易的复用连接。  

3. IO方式：为了支持高并发，传统的阻塞式 IO 显然不太合适，因此我们需要异步的 IO，即 NIO。Java 提供了 NIO 的解决方案，Java 7 也提供了更优秀的 NIO.2 支持。  

4. 多连接：既然使用长连接，那么第一个问题是到底 client 和 server 之间需要多少根连接？实际上单连接和多连接在使用上没有区别，对于数据传输量较小的应用类型，单连接基本足够。单连接和多连接最大的区别在于，每根连接都有自己私有的发送和接收缓冲区，因此大数据量传输时分散在不同的连接缓冲区会得到更好的吞吐效率。所以，如果你的数据传输量不足以让单连接的缓冲区一直处于饱和状态的话，那么使用多连接并不会产生任何明显的提升，反而会增加连接管理的开销。  

5. 心跳： 连接是由 client 端发起建立并维持。如果 client 和 server 之间是直连的，那么连接一般不会中断（当然物理链路故障除外）。如果 client 和 server 连接经过一些负载中转设备，有可能连接一段时间不活跃时会被这些中间设备中断。为了保持连接有必要定时为每个连接发送心跳数据以维持连接不中断。心跳消息 是 RPC 框架库使用的内部消息，在前文协议头结构中也有一个专门的心跳位，就是用来标记心跳消息的，它对业务应用透明。  

（4）序列化：  

两方面会直接影响 RPC 的性能，一是传输方式，二是序列化。  

1. 序列化方式：毕竟是远程通信，需要将对象转化成二进制流进行传输。不同的RPC框架应用的场景不同，在序列化上也会采取不同的技术。 就序列化而言，Java 提供了默认的序列化方式，但在高并发的情况下，这种方式将会带来一些性能上的瓶颈，于是市面上出现了一系列优秀的序列化框架，比如：Protobuf、Kryo、Hessian、Jackson 等，它们可以取代 Java 默认的序列化，从而提供更高效的性能。  

2. 编码内容：出于效率考虑，编码的信息越少越好（传输数据少），编码的规则越简单越好（执行效率高）  


### 同步调用与异步调用  


## 流行的RPC框架

Dubbo  

Dubbo 是阿里巴巴公司开源的一个Java高性能优秀的服务框架，使得应用可通过高性能的 RPC 实现服务的输出和输入功能，可以和 Spring框架无缝集成。目前已经进入Apache孵化器。  

Motan  

Motan是新浪微博开源的一个Java RPC框架。2016年5月开源。Motan 在微博平台中已经广泛应用，每天为数百个服务完成近千亿次的调用。  

gRPC  

gRPC是Google开发的高性能、通用的开源RPC框架，其由Google主要面向移动应用开发并基于HTTP/2协议标准而设计，基于ProtoBuf(Protocol Buffers)序列化协议开发，且支持众多开发语言。本身它不是分布式的，所以要实现上面的框架的功能需要进一步的开发。  

Thrift  

Thrift是Apache的一个跨语言的高性能的服务框架，也得到了广泛的应用。  


目前常用的RPC框架如下：  
1. Thrift：thrift 是一个软件框架，用来进行可扩展且跨语言的服务的开发。它结合了功能强大的软件堆栈和代码生成引擎，以构建在 C++, Java, Python, PHP, Ruby, Erlang, Perl, Haskell, C#, Cocoa, JavaScript, Node.js, Smalltalk, and OCaml 这些编程语言间无缝结合的、高效的服务。  
2. Dubbo：Dubbo是一个分布式服务框架，以及SOA治理方案。其功能主要包括：高性能NIO通讯及多协议集成，服务动态寻址与路由，软负载均衡与容错，依赖分析与降级等。 Dubbo是阿里巴巴内部的SOA服务化治理方案的核心框架，Dubbo自2011年开源后，已被许多非阿里系公司使用。  
3. Spring Cloud：Spring Cloud由众多子项目组成，如Spring Cloud Config、Spring Cloud Netflix、Spring Cloud Consul 等，提供了搭建分布式系统及微服务常用的工具，如配置管理、服务发现、断路器、智能路由、微代理、控制总线、一次性token、全局锁、选主、分布式会话 和集群状态等，满足了构建微服务所需的所有解决方案。Spring Cloud基于Spring Boot, 使得开发部署极其简单。  



## RPC和消息队列的差异  
1. 功能差异  
    在架构上，RPC和Message的差异点是，Message有一个中间结点Message Queue，可以把消息存储。  
    消息的特点  
    1. Message Queue把请求的压力保存一下，逐渐释放出来，让处理者按照自己的节奏来处理。
    2. Message Queue引入一下新的结点，系统的可靠性会受Message Queue结点的影响。
    3. Message Queue是异步单向的消息。发送消息设计成是不需要等待消息处理的完成。
    所以对于有同步返回需求，用Message Queue则变得麻烦了。
    RPC的特点  
    同步调用，对于要等待返回结果/处理结果的场景，RPC是可以非常自然直觉的使用方式(RPC也可以是异步调用)。  
    由于等待结果，Consumer（Client）会有线程消耗。如果以异步RPC的方式使用，Consumer（Client）线程消耗可以去掉。但不能做到像消息一样暂存消息/请求，压力会直接传导到服务Provider。  
2. 适用场合差异  
    1. 希望同步得到结果的场合，RPC合适。  
    2. 希望使用简单，则RPC；RPC操作基于接口，使用简单，使用方式模拟本地调用。异步的方式编程比较复杂。  
    3. 不希望发送端（RPC Consumer、Message Sender）受限于处理端（RPC Provider、Message Receiver）的速度时，使用Message Queue。  
    随着业务增长，有的处理端处理量会成为瓶颈，会进行同步调用到异步消息的改造。这样的改造实际上有调整业务的使用方式。比如原来一个操作页面提交后就下一个页面会看到处理结果；改造后异步消息后，下一个页面就会变成“操作已提交，完成后会得到通知”。  
3. 不适用场合说明  
    1. RPC同步调用使用Message Queue来传输调用信息。 上面分析可以知道，这样的做法，发送端是在等待，同时占用一个中间点的资源。变得复杂了，但没有对等的收益。  
    2. 对于返回值是void的调用，可以这样做，因为实际上这个调用业务上往往不需要同步得到处理结果的，只要保证会处理即可。（RPC的方式可以保证调用返回即处理完成，使用消息方式后这一点不能保证了。）  
    3. 返回值是void的调用，使用消息，效果上是把消息的使用方式Wrap成了服务调用（服务调用使用方式成简单，基于业务接口）。  



