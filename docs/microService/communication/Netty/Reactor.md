
<!-- TOC -->

- [1. Reactor线程模型](#1-reactor线程模型)
    - [1.1. Reactor是什么](#11-reactor是什么)
        - [1.1.1. thread-based architecture(基于线程)](#111-thread-based-architecture基于线程)
        - [1.1.2. event-driven architecture(事件驱动)](#112-event-driven-architecture事件驱动)
    - [1.2. ~~Reactor模式架构~~](#12-reactor模式架构)
    - [1.3. Reactor线程模型详解](#13-reactor线程模型详解)
        - [1.3.1. 单线程模型](#131-单线程模型)
            - [1.3.1.1. 单线程模型简介](#1311-单线程模型简介)
            - [1.3.1.2. 单线程模型缺点](#1312-单线程模型缺点)
        - [1.3.2. 多线程模型](#132-多线程模型)
            - [1.3.2.1. 多线程模型简介](#1321-多线程模型简介)
            - [1.3.2.2. 多线程模型缺点](#1322-多线程模型缺点)
        - [1.3.3. 主从多线程模型](#133-主从多线程模型)
    - [1.4. Proactor介绍](#14-proactor介绍)
    - [1.5. 参考](#15-参考)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. 前言：`一次网络I/O事件，包含网络连接 -> 读写数据 -> 处理事件。`  
1. Reactor线程模型跟NIO中Selctor类似，多路复用，事件分发。  
2. **<font color = "red">Reactor模式核心组成部分包括Reactor线程和worker线程池，</font><font color = "blue">`其中Reactor负责监听和分发事件，线程池负责处理事件。`</font>** **<font color = "clime">而根据Reactor的数量和线程池的数量，又将Reactor分为三种模型。</font>**  
3. **单线程模型(单Reactor单线程)**  
&emsp; ~~这是最基本的单Reactor单线程模型。其中Reactor线程，负责多路分离套接字，有新连接到来触发connect 事件之后，交由Acceptor进行处理，有IO读写事件之后交给hanlder处理。~~  
&emsp; ~~Acceptor主要任务就是构建handler，在获取到和client相关的SocketChannel之后，绑定到相应的hanlder上，对应的SocketChannel有读写事件之后，基于racotor分发,hanlder就可以处理了（所有的IO事件都绑定到selector上，有Reactor分发）。~~  
&emsp; **<font color = "red">Reactor单线程模型，指的是所有的IO操作都在同一个NIO线程上面完成。</font>** 单个NIO线程会成为系统瓶颈，并且会有节点故障问题。   
4. **多线程模型(单Reactor多线程)**  
&emsp; ~~相对于第一种单线程的模式来说，在处理业务逻辑，也就是获取到IO的读写事件之后，交由线程池来处理，这样可以减小主reactor的性能开销，从而更专注的做事件分发工作了，从而提升整个应用的吞吐。~~  
&emsp; Rector多线程模型与单线程模型最大的区别就是有一组NIO线程处理IO操作。在极个别特殊场景中，一个NIO线程(Acceptor线程)负责监听和处理所有的客户端连接可能会存在性能问题。    
5. **主从多线程模型(多Reactor多线程)**    
&emsp; 主从Reactor多线程模型中，Reactor线程拆分为mainReactor和subReactor两个部分， **<font color = "clime">mainReactor只处理连接事件，读写事件交给subReactor来处理。</font>** 业务逻辑还是由线程池来处理，mainRactor只处理连接事件，用一个线程来处理就好。处理读写事件的subReactor个数一般和CPU数量相等，一个subReactor对应一个线程，业务逻辑由线程池处理。  
&emsp; ~~第三种模型比起第二种模型，是将Reactor分成两部分：~~  
&emsp; ~~mainReactor负责监听server socket，用来处理新连接的建立，将建立的socketChannel指定注册给subReactor。~~  
&emsp; ~~subReactor维护自己的selector, 基于mainReactor 注册的socketChannel多路分离IO读写事件，读写网 络数据，对业务处理的功能，另其扔给worker线程池来完成。~~  
&emsp; Reactor主从多线程模型中，一个连接accept专门用一个线程处理。  
&emsp; 主从Reactor线程模型的特点是：服务端用于接收客户端连接的不再是个1个单独的NIO线程，而是一个独立的NIO线程池。Acceptor接收到客户端TCP连接请求处理完成后(可能包含接入认证等)，将新创建的SocketChannel注册到IO线程池(sub reactor线程池)的某个IO线程上，由它负责SocketChannel的读写和编解码工作。Acceptor线程池仅仅只用于客户端的登陆、握手和安全认证，一旦链路建立成功，就将链路注册到后端subReactor线程池的IO线程上，由IO线程负责后续的IO操作。  
&emsp; 利用主从NIO线程模型，可以解决1个服务端监听线程无法有效处理所有客户端连接的性能不足问题。  



# 1. Reactor线程模型  
&emsp; **<font color = "clime">《Reactor论文》</font>**  

<!-- 

https://zhuanlan.zhihu.com/p/264355987?utm_source=wechat_session
https://mp.weixin.qq.com/s/7CvrYsuLSFF7eCAQ3svihw
-->

## 1.1. Reactor是什么
&emsp; 在处理web请求时，通常有两种体系结构，分别为：thread-based architecture(基于线程)、event-driven architecture(事件驱动)。  

### 1.1.1. thread-based architecture(基于线程)
&emsp; 基于线程的体系结构通常会使用多线程来处理客户端的请求，每当接收到一个请求，便开启一个独立的线程来处理。这种方式虽然是直观的，但是仅适用于并发访问量不大的场景，因为线程需要占用一定的内存资源，且操作系统在线程之间的切换也需要一定的开销，当线程数过多时显然会降低web服务器的性能。并且，当线程在处理I/O操作，在等待输入的这段时间线程处于空闲的状态，同样也会造成cpu资源的浪费。一个典型的设计如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-36.png)  

### 1.1.2. event-driven architecture(事件驱动)
&emsp; 事件驱动体系结构是目前比较广泛使用的一种。这种方式会定义一系列的事件处理器来响应事件的发生，并且将服务端接受连接与对事件的处理分离。其中，事件是一种状态的改变。比如，tcp中socket的new incoming connection、ready for read、ready for write。  

## 1.2. ~~Reactor模式架构~~  
<!-- 
http://www.360doc.com/content/18/0427/15/11253639_749194481.shtml
-->
<!-- 
https://www.jianshu.com/p/eef7ebe28673
-->
&emsp; Reactor设计模式是event-driven architecture的一种实现方式，处理多个客户端并发的向服务端请求服务的场景。每种服务在服务端可能由多个方法组成。reactor会解耦并发请求的服务并分发给对应的事件处理器来处理。目前，许多流行的开源框架都用到了reactor模式，如：netty、node.js等，包括java的nio。  

        维基百科上的定义：“反应堆设计模式是一种事件处理模式，用于处理由一个或多个输入同时发送的服务请求。然后，服务处理程序将传入的请求多路分解，并同步地将其分发到关联的请求处理程序”。

&emsp; 总体图示如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-37.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-105.png)  
<center>Reactor网络模型</center>  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-106.png)  

&emsp; **Reactor主要由以下几个角色构成：**handle、Synchronous Event Demultiplexer、Initiation Dispatcher、Event Handler、Concrete Event Handler。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-107.png)  
* Handle，在linux中一般称为文件描述符，而在window称为句柄，两者的含义一样。handle是事件的发源地。比如一个网络socket、磁盘文件等。而发生在handle上的事件可以有connection、ready for read、ready for write等。  
* Synchronous Event Demultiplexer，同步事件分离器，本质上是系统调用。比如linux中的select、poll、epoll等。比如，select方法会一直阻塞直到handle上有事件发生时才会返回。  
* **Event Handler，事件处理器，**其会定义一些回调方法或者称为钩子函数，当handle上有事件发生时，回调方法便会执行，一种事件处理机制。  
* **Concrete Event Handler，具体的事件处理器，**实现了Event Handler。在回调方法中会实现具体的业务逻辑。  
* **<font color = "clime">Initiation Dispatcher，初始分发器，也是reactor角色，</font>** 提供了注册、删除与转发event handler的方法。当Synchronous Event Demultiplexer检测到handle上有事件发生时，便会通知initiation dispatcher调用特定的event handler的回调方法。  

&emsp; **<font color = "clime">Reactor的一般处理流程：</font>**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-108.png)  
1. 当应用向Initiation Dispatcher注册Concrete Event Handler时，应用会标识出该事件处理器希望Initiation Dispatcher在某种类型的事件发生发生时向其通知，事件与handle关联。  
2. Initiation Dispatcher要求注册在其上面的Concrete Event Handler传递内部关联的handle，该handle会向操作系统标识。
3. 当所有的Concrete Event Handler都注册到Initiation Dispatcher上后，应用会调用handle_events方法来启动Initiation Dispatcher的事件循环，这时Initiation Dispatcher会将每个Concrete Event Handler关联的handle合并，并使用Synchronous Event Demultiplexer来等待这些handle上事件的发生。
4. 当与某个事件源对应的handle变为ready时，Synchronous Event Demultiplexer便会通知 Initiation Dispatcher。比如tcp的socket变为ready for reading。
5. Initiation Dispatcher会触发事件处理器的回调方法。当事件发生时，Initiation Dispatcher会将被一个“key”(表示一个激活的handle)定位和分发给特定的Event Handler的回调方法。
6. Initiation Dispatcher调用特定的Concrete Event Handler的回调方法来响应其关联的handle上发生的事件。

<!-- 
Reactor的一般流程
1)应用程序在事件分离器注册读写就绪事件和读写就绪事件处理器
2)事件分离器等待读写就绪事件发生
3)读写就绪事件发生，激活事件分离器，分离器调用读写就绪事件处理器
4)事件处理器先从内核把数据读取到用户空间，然后再处理数据
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-82.png)  
-->

## 1.3. Reactor线程模型详解  
<!-- 
https://blog.csdn.net/bingxuesiyang/article/details/89888664
-->
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-90.png)  

&emsp; Reactor线程模型跟NIO中Selctor类似，多路复用，事件分发。  
&emsp; **<font color = "red">Reactor模式核心组成部分包括Reactor线程和worker线程池，</font><font color = "clime">其中Reactor负责监听和分发事件，线程池负责处理事件。</font><font color = "red">而根据Reactor的数量和线程池的数量，又将Reactor分为三种模型：</font>**

* 单线程模型(单Reactor单线程)  
* 多线程模型(单Reactor多线程)  
* 主从多线程模型(多Reactor多线程)  

### 1.3.1. 单线程模型  
#### 1.3.1.1. 单线程模型简介  
&emsp; 对于并发不是很高的应用，可以使用单线程模型。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-91.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-120.png)  

&emsp; 这是最基本的单Reactor单线程模型。其中Reactor线程，负责多路分离套接字，有新连接到来触发connect 事件之后，交由Acceptor进行处理，有IO读写事件之后交给hanlder处理。  
&emsp; Acceptor主要任务就是构建handler，在获取到和client相关的SocketChannel之后 ，绑定到相应的hanlder上，对应的SocketChannel有读写事件之后，基于racotor分发,hanlder就可以处理了（所有的IO事件都绑定到selector上，有Reactor分发）。  


&emsp; **<font color = "red">Reactor单线程模型，指的是所有的IO操作都在同一个NIO线程上面完成，</font>** NIO线程的职责如下：  

&emsp; 1)作为NIO服务端，接收客户端的TCP请求；  
&emsp; 2)作为NIO客户端，向服务端发起TCP请求；  
&emsp; 3)读取通信对端的请求或者应答消息；  
&emsp; 4)向通信对端发送消息请求或者应答消息。  

&emsp; 由于Reactor模式使用的是同步非阻塞IO，所有的IO操作都不会导致阻塞(或者是短暂的阻塞)，理论上一个线程可以独立处理所有IO相关的操作。从架构层面看，一个NIO线程确实可以完成其承担的职责。例如，通过Acceptor类接收客户端的TCP连接请求消息，链路建立成功之后，通过Dispatch将对应的ByteBuffer派发到指定的Handler上进行消息解码。用户线程可以通过消息编码通过NIO线程将消息发送给客户端。  

#### 1.3.1.2. 单线程模型缺点
&emsp; 对于一些小容量应用场景，可以使用单线程模型。但是对于高负载、大并发的应用场景却不合适，主要原因如下：  
1. 一个NIO线程同时处理成百上千的链路，性能上无法支撑，即便NIO线程的CPU负荷达到100%，也无法满足海量消息的编码、解码、读取和发送；  
2. 当NIO线程负载过重之后，处理速度将变慢，这会导致大量客户端连接超时，超时之后往往会进行重发，这就加重了NIO线程的负载，最终会导致大量消息积压和处理超时，NIO线程会成为系统的性能瓶颈； 
3. 可靠性问题：一旦NIO线程意外跑飞，或者进入死循环，会导致整个系统通信模块不可用，不能接收和处理外部消息，造成节点故障。  

&emsp; 为了解决这些问题，演进出了Reactor多线程模型。  

<!-- 
&emsp; 一个线程需要执行处理所有的accept、read、decode、process、encode、send事件。对于高负载、高并发，并且对性能要求比较高的场景不适用。  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-10.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-83.png)  

* Reactor内部通过selector监控连接事件，收到事件后通过dispatch进行分发，如果是连接建立的事件，则由Acceptor处理，Acceptor通过accept接受连接，并创建一个Handler来处理连接后续的各种事件，如果是读写事件，直接调用连接对应的Handler来处理。  
* Handler完成read->(decode->compute->encode)->send的业务流程。  
* 这种模型好处是简单，坏处却很明显，当某个Handler阻塞时，会导致其他客户端的handler和accpetor都得不到执行，无法做到高性能，只适用于业务处理非常快速的场景。  

Reactor 单线程模型示意图如下所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-10.png)  
-->

### 1.3.2. 多线程模型  
#### 1.3.2.1. 多线程模型简介
&emsp; Rector多线程模型与单线程模型最大的区别就是有一组NIO线程处理IO操作，它的原理图如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-92.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-121.png)  
&emsp; 相对于第一种单线程的模式来说，在处理业务逻辑，也就是获取到IO的读写事件之后，交由线程池来处理，这样可以减小主reactor的性能开销，从而更专注的做事件分发工作了，从而提升整个应用的吞吐。  

&emsp; Reactor多线程模型的特点：  

1. 有专门一个NIO线程(Acceptor线程)用于监听服务端，接收客户端的TCP连接请求；  
2. 网络IO操作(读、写等)由一个NIO线程池负责，线程池可以采用标准的JDK线程池实现，它包含一个任务队列和N个可用的线程，由这些NIO线程负责消息的读取、解码、编码和发送；    
3. 1个NIO线程可以同时处理N条链路，但是1个链路只对应1个NIO线程，防止发生并发操作问题。    

#### 1.3.2.2. 多线程模型缺点
&emsp; ~~在绝大多数场景下，Reactor多线程模型都可以满足性能需求；但是，在极个别特殊场景中，一个NIO线程负责监听和处理所有的客户端连接可能会存在性能问题。例如并发百万客户端连接，或者服务端需要对客户端握手进行安全认证，但是认证本身非常损耗性能。在这类场景下，单独一个Acceptor线程可能会存在性能不足问题，为了解决性能问题，产生了第三种Reactor线程模型-主从Reactor多线程模型。~~  

<!-- 
&emsp; 一个Acceptor线程只负责监听客户端的连接，一个NIO线程池负责具体处理：accept、read、decode、process、encode、send事件。满足绝大部分应用场景，并发连接量不大的时候没啥问题，但是遇到并发连接大的时候就可能会出现问题，成为性能瓶颈。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-11.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-84.png)  

* 主线程中，Reactor对象通过selector监控连接事件，收到事件后通过dispatch进行分发，如果是连接建立事件，则由Acceptor处理，Acceptor通过accept接收连接，并创建一个Handler来处理后续事件，而Handler只负责响应事件，不进行业务操作，也就是只进行read读取数据和write写出数据，业务处理交给一个线程池进行处理
* 线程池分配一个线程完成真正的业务处理，然后将响应结果交给主进程的Handler处理，Handler将结果send给client (下面是核心代码)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-13.png)  

&emsp; 单Reactor承当所有事件的监听和响应，而当服务端遇到大量的客户端同时进行连接，或者在请求连接时执行一些耗时操作，比如身份认证，权限检查等，这种瞬时的高并发就容易成为性能瓶颈  
-->

--------------


### 1.3.3. 主从多线程模型  
<!-- 
Reactor主从多线程
https://www.cnblogs.com/-wenli/p/13343397.html
https://blog.csdn.net/Jack__iT/article/details/107010486
-->

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-122.png)  
&emsp; 第三种模型比起第二种模型，是将Reactor分成两部分：  

* mainReactor负责监听server socket，用来处理新连接的建立，将建立的socketChannel指定注册给subReactor。  
* subReactor维护自己的selector, 基于mainReactor 注册的socketChannel多路分离IO读写事件，读写网 络数据，对业务处理的功能，另其扔给worker线程池来完成。  


-----------

&emsp; Reactor主从多线程模型中，一个连接accept专门用一个线程处理。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-93.png)  
&emsp; 主从Reactor线程模型的特点是：服务端用于接收客户端连接的不再是个1个单独的NIO线程，而是一个独立的NIO线程池。Acceptor接收到客户端TCP连接请求处理完成后(可能包含接入认证等)，将新创建的SocketChannel注册到IO线程池(sub reactor线程池)的某个IO线程上，由它负责SocketChannel的读写和编解码工作。Acceptor线程池仅仅只用于客户端的登陆、握手和安全认证，一旦链路建立成功，就将链路注册到后端subReactor线程池的IO线程上，由IO线程负责后续的IO操作。  
&emsp; 利用主从NIO线程模型，可以解决1个服务端监听线程无法有效处理所有客户端连接的性能不足问题。  
<!-- 
&emsp; 从一个 主线程 NIO 线程池中选择一个线程作为 Acceptor 线程，绑定监听端口，接收客户端连接的连接，其他线程负责后续的接入认证等工作。连接建立完成后，Sub NIO 线程池负责具体处理 I/O 读写。如果多线程模型无法满足需求的时候，可以考虑使用主从多线程模型 。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-12.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-85.png)  

* 存在多个Reactor，每个Reactor都有自己的selector选择器，线程和dispatch
* 主线程中的mainReactor通过自己的selector监控连接建立事件，收到事件后通过Accpetor接收，将新的连接分配给某个子线程
* 子线程中的subReactor将mainReactor分配的连接加入连接队列中通过自己的selector进行监听，并创建一个Handler用于处理后续事件
* Handler完成read->业务处理->send的完整业务流程
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-14.png)  


它的线程模型如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-12.png)  



它的工作流程总结如下：

    从主线程池中随机选择一个 Reactor 线程作为 Acceptor 线程，用于绑定监听端口，接收客户端连接；

    Acceptor 线程接收客户端连接请求之后创建新的 SocketChannel，将其注册到主线程池的其它 Reactor 线程上，由其负责接入认证、IP 黑白名单过滤、握手等操作；

    步骤 2 完成之后，业务层的链路正式建立，将 SocketChannel 从主线程池的 Reactor 线程的多路复用器上摘除，重新注册到 Sub 线程池的线程上，用于处理 I/O 的读写操作。
-->


## 1.4. Proactor介绍  
<!-- 
Proactor
http://www.yeolar.com/note/2012/12/10/proactor/
-->
&emsp; **Proactor和Reactor的区别：**  

* Proactor是基于异步I/O的概念，而Reactor一般则是基于多路复用I/O的概念  
* Proactor不需要把数据从内核复制到用户空间，这步由系统完成  

&emsp; Proactor模型的一般流程：   
1. 应用程序在事件分离器注册读完成事件和读完成事件处理器，并向系统发出异步读请求。  
2. 事件分离器等待读事件的完成。  
3. 在分离器等待过程中，系统利用并行的内核线程执行实际的读操作，并将数据复制进程缓冲区，最后通知事件分离器读完成到来。  
4. 事件分离器监听到读完成事件，激活读完成事件的处理器。  
5. 读完成事件处理器直接处理用户进程缓冲区中的数据。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-86.png)  

## 1.5. 参考  
&emsp; https://www.bilibili.com/video/BV1ft4y1B74d?p=2  
&emsp; https://www.bilibili.com/video/BV17t41137su?p=61  