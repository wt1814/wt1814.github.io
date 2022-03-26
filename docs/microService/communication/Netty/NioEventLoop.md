
<!-- TOC -->

- [1. NioEventLoop](#1-nioeventloop)
    - [1.1. NioEventLoop简介](#11-nioeventloop简介)
        - [1.1.1. 服务端](#111-服务端)
        - [1.1.2. 客户端](#112-客户端)
    - [1.2. NioEventLoop的继承体系](#12-nioeventloop的继承体系)
    - [1.3. NioEventLoop的创建](#13-nioeventloop的创建)
    - [1.4. NioEventLoop的启动](#14-nioeventloop的启动)
    - [1.5. NioEventLoop的执行，解决NIO的bug](#15-nioeventloop的执行解决nio的bug)

<!-- /TOC -->

# 1. NioEventLoop
<!-- 
![image](http://www.wt1814.com/static/view/images/microService/netty/netty-102.png)  

NioEventLoop
https://www.jianshu.com/p/5c6466510d3b

netty是怎么解决Nio中臭名昭著的bug（Netty源码分析之NioEventLoop）
https://www.jianshu.com/p/9acf36f7e025


https://www.jianshu.com/p/23820270e30a
-->
## 1.1. NioEventLoop简介  
&emsp; NioEventLoop做为Netty线程模型的核心部分，从本质上讲是一个事件循环执行器， **<font color = "red">每个NioEventLoop都会绑定一个对应的线程通过一个for(;;)循环来处理与 Channel 相关的IO操作，包括调用select等待就绪的 IO 事件、读写数据与数据的处理等；其次作为任务队列，执行taskQueue中的任务, 例如eventLoop.schedule提交的定时任务也是这个线程执行的。</font>** 而NioEventLoopGroup顾名思义，它是维护了一组这样的事件循环器，这也是Netty基于Reactor模式的具体设计体现。  

### 1.1.1. 服务端
&emsp; 在服务端程序中有两类 NioEventLoop。一类称为 parent (也称 boss)，这一类 NioEventLoop 一般也就只有一个，专门负责发现客户端连接。另一类是 child (也称 worker)，这一类 NioEventLoop 要设置有限的若干个，默认的个数只是为 CPU 核数的 2 倍，它负责处理客户端连接的信息读写。使用 NIO 模式的服务端程序，child 的个数可远远小于客户端连接数。作为 parent 或者 child 的 NioEventLoop 都可以注册不限制一个的 Channel 实例进行服务。  

* **<font color = "red">parent 要负责的是代表服务端侦听端口的 NioServerSocketChannel 实例，NioServerSocketChannel 实例只有一个。</font>** 把这个 NioServerSocketChannel 注册到 parent 后，parent 就开始负责发现该 NioServerSocketChannel 上新的客户端连接，触发对新来的客户端连接进行初始化工作，引导到后续的信息读写等处理程序。  
* **<font color = "red">child 要负责的是代表客户端连接的 NioSocketChannel 实例。</font>** NioSocketChannel 实例和客户端连接数一一对应，因此程序中同时会有很多 NioSocketChannel 实例。由于 child 数量有限（8 核 CPU 下默认是 16 个 child)，因此一个 child 要同时负责多个 NioSocketChannel，发现这些连接的可读可写事件，触发应用程序从系统缓冲区读取字节流、对消息进行编解码、调用逻辑程序处理、继续对先前中断的系统缓冲区的写入等。  


&emsp; 最简单地说，NioEventLoop 的首要职责就是为注册在它上的 channels 服务，发现这些 channels 上发生的新连接事件、读写等 I/O 事件，然后将事件转交 channel 流水线处理。  
&emsp; 下表是一个服务端程序的一个简单快照。每一行代表一个 NioEventLoop，第一行是 parent，负责处理NioServerSocketChannel。随后是16行的 child，每一个 child 负责多个NioSocketChannel。  
![image](http://www.wt1814.com/static/view/images/microService/netty/netty-109.png)  
<center>表1：一个服务端程序的 NioEventLoop及其负责的 channels</center>  
&emsp; 随着程序的进行，表格的行数并不会变化，但是每行的 channels 会随着连接的建立和断开不断地变化着。Netty 并不保证每个 NioEventLoop 负责的 channels 个数要进行均衡， Netty 对新建立的连接采用简单的寻找下一个 NioEventLoop 的策略，并且在连接断开后也不会进行重新分布；或许某些极端情况下，可能出现某些 NioEventLoop 负责的 channels 比其他 NioEventLoop 负责的要多很多，但这似乎不是问题。  

### 1.1.2. 客户端
&emsp; 客户端比服务端简单，客户端程序只有一类  NioEventLoop，直接就是 parent ，没有 child。一般地，由于客户端和服务端只保留一个连接，所以只有一个 channel，即一个 parent 只负责 NioEventChannel 的 I/O 事件。但对于需要同一个服务端保持多个连接的，或者需要和多个服务端保持连接的，则程序里依然会存在多个 NioEventChannel 实例，但在实践上还是建议这些不同的的连接使用同一个 NioEventLoop，即都统一归到 parent 负责：  
![image](http://www.wt1814.com/static/view/images/microService/netty/netty-110.png)  
<center>表2：客户端程序的 NioEventLoop 只有一个，负责多个 NioSocketChannel</center>  

## 1.2. NioEventLoop的继承体系  
&emsp; 下图清晰地呈现了 NioEventLoop 的继承体系：  
![image](http://www.wt1814.com/static/view/images/microService/netty/netty-111.png)  
<center>NioEventLoop的继承体系</center>  

&emsp; 图中呈现了 NioEventLoop 类的继承层级，虽然没有呈现它实现了哪些接口，不过不妨碍对这个图进行一些解读：  
&emsp; 1、NioEventLoop 可以被追溯到 java.util.concurrent.ExecutorService 接口。这个信息表明 NioEventLoop 能够执行 Runnable 任务，从而保证了这些要执行的任务是在 NioEventLoop 的线程中执行，而非外部线程来执行。NioEventLoop 内置的一些 Runnable 任务包括了对 channel 的 register、系统缓冲区满后而推迟的 flush 任务等。  
&emsp; 2、NioEventLoop 继承于 io.netty.util.concurrent.SingleThreadEventExecutor。这个信息表明，NioEventLoop 是单线程的。根据 SingleThreadEventExecutor 的说明，提交到 SingleThreadEventExecutor 的任务，将按照提交的先后顺序执行。  

&emsp; 需要注意的是，开发不要随便用 NioEventLoop 执行长任务以免造成 I/O 阻塞，给 NioEventLoop 履行其首要职责添堵。更不能在 NioEventLoop 执行含有类似 Thread.sleep()、Object.wait()、ReentrantLock.lock()、CountDownLatch.await() 等等之类的程序，尽量避免使用 synchronized 同步锁，这些都可能导致 NioEventLoop 线程中断（线程这口气要是断了，I/O 的命就没了）。  


&emsp; **SingleThreadEventExecutor简述**  
&emsp; io.netty.util.concurrent.SingleThreadEventExecutor 内部包含了一个任务 FIFO 队列，用于存储提交到该类的 Runnable 任务。它也创建了一个定制的单线程对象，规定了线程执行之后结束之前的一些处理，比如把未执行完成的任务执行完毕、关闭 NioEventLoop 的 Selector 等。它提供了addTask、offerTask、removeTask、runAllTasks等仅供子类可以调用的方法。但没有具体实现线程具体要执行的内容，它空出了run方法让子类来实现，所以 SingleThreadEventExecutor 类本身默认不会从它的任务队列中取出任务进行执行：  

```java
protected abstract void run();
```

&emsp; 作为子类的NioEventLoop将实现这个方法，它也就成了NioEventLoop的重要入口方法。NioEventLoop的run方法实现后，将被SingleThreadEventExecutor里创建的线程执行，NioEventLoop所负的对channels的职责，将在run方法中实现。  

## 1.3. NioEventLoop的创建
<!-- 
NioEventLoop的创建
https://www.cnblogs.com/dafanjoy/p/10486019.html
-->

## 1.4. NioEventLoop的启动  
<!-- 
https://www.jianshu.com/p/e577803f0fb8
https://www.cnblogs.com/dafanjoy/p/10507393.html
包含绑定channel
书籍netty4核心源码
https://www.jianshu.com/p/5c6466510d3b
-->
&emsp; 参考[NioEventLoop的启动](/docs/microService/communication/Netty/NioEventLoopStart.md)  
&emsp; 包含绑定channel。  


## 1.5. NioEventLoop的执行，解决NIO的bug  

&emsp; [NioEventLoop的执行](/docs/microService/communication/Netty/NioEventLoopRun.md)  