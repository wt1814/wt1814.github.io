---
title: NIO Selector
date: 2020-05-30 00:00:00
tags:
    - 分布式通信
---

<!-- TOC -->

- [1. NIO选择器](#1-nio选择器)
    - [1.1. 选择器基础：选择器、可选择通道、选择键类](#11-选择器基础选择器可选择通道选择键类)
    - [1.2. 选择器教程](#12-选择器教程)
        - [1.2.1. 建立选择器（选择器、通道、选择键建立连接）](#121-建立选择器选择器通道选择键建立连接)
        - [1.2.2. 选择键的使用，SelectionKey类的API](#122-选择键的使用selectionkey类的api)
        - [1.2.3. 选择器的使用，selector类的API](#123-选择器的使用selector类的api)
        - [1.2.4. Selector完整实例](#124-selector完整实例)

<!-- /TOC -->

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/communication/NIO-14.png)  


# 1. NIO选择器  
&emsp; Selector是NIO多路复用的重要组成部分。它负责检查一个或多个Channel(通道)是否是可读、写状态，实现单线程管理多通道，优于使用多线程或线程池产生的系统资源开销。 

## 1.1. 选择器基础：选择器、可选择通道、选择键类   
&emsp; 选择器(Selector)使用单个线程处理多个通道。 流程结构如图：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/communication/NIO-12.png)  

* 选择器(Selector)：在Java NIO中，选择器(Selector)是可选择通道的多路复用器。选择器类管理着一个被注册的通道集合的信息和它们的就绪状态。通道是和选择器一起被注册的，并且使用选择器来更新通道的就绪状态。当这样使用的时候，可以选择将被激发的线程挂起，直到有就绪的的通道。  
当Channel(通道)注册至Selector内后，便会产生一个对应的SelectionKey，存储与此Channel相关的数据。
* 可选择通道(SelectableChannel)：这个抽象类提供了实现通道的可选择性所需要的公共方法。它是所有支持就绪检查的通道类的父类。FileChannel对象不是可选择的，因为它们没有继承SelectableChannel。所有socket通道都是可选择的，包括从管道(Pipe)对象中获得的通道。SelectableChannel可以被注册到Selector对象上，同时可以指定对那个选择器而言，那种操作是感兴趣的。一个通道可以被注册到多个选择器上，但对每个选择器而言只能被注册一次。  
* 选择键(SelectionKey)：选择键封装了特定的通道与特定的选择器的注册关系。选择键对象被SelectableChannel.register()返回并提供一个表示这种注册关系的标记。选择键包含了两个比特集（以整数的形式进行编码），指示了该注册关系所关心的通道操作，以及通道己经准备好的操作。每个channel对应一个 SelectionKey。  

## 1.2. 选择器教程  
### 1.2.1. 建立选择器（选择器、通道、选择键建立连接）  
&emsp; selector的API：  

|方法|描述|
|---|---|
|Selector open()|打开一个选择器|
|void close()|关闭此选择器|

&emsp; 建立监控三个Socket通道的选择器：  

```
Selector selector = Selector.open( );
channel1.register (selector, SelectionKey.OP_READ);
channel2.register (selector, SelectionKey.OP_WRITE);
channel3.register (selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
// Wait up to 10 seconds for a channel to become ready
readyCount = selector.select (10000);
```
&emsp; select方法是阻塞方法，直到过了十秒或者至少有一个通道的I/O操作准备好。  
&emsp; 这些代码创建了一个新的选择器，然后将这三个(己经存在的)socket通道注册到选择器上，而且感兴趣的操作各不相同。方法在将线程置于睡眠状态，直到这些刚兴趣的事情中的操作中的一个发生或者10秒钟的时间过去。  

1. 创建Selector对象  
```
Selector selector = Selector.open();
```
2. 将Channel注册到选择器中。为了使用选择器管理Channel，需要将Channel注册到选择器中:  

    ```
    channel.configureBlocking(false);
    SelectionKey key =channel.register(selector,SelectionKey.OP_READ);
    ```
    &emsp; 注意，注册的Channel必须设置成异步模式才可以，否则异步IO就无法工作。这就意味着不能把一个FileChannel注册到Selector，因为FileChannel没有异步模式，但是网络编程中的SocketChannel可以。  
    &emsp; 1). register()方法的第二个参数，它是一个“interest set”，意思是注册的Selector对Channel中的哪些事件感兴趣，事件类型有四种，这四种事件用SelectionKey的四个常量来表示：  

    ```
    SelectionKey.OP_CONNECT
    SelectionKey.OP_ACCEPT
    SelectionKey.OP_READ
    SelectionKey.OP_WRITE
    ```
    &emsp; 通道触发了一个事件是指该事件已经Ready(就绪）。所以某个Channel成功连接到另一个服务器称为“连接就绪”Connect Ready。一个ServerSocketChannel  
    &emsp; 准备好接收新连接称为“接收就绪”Accept Ready，一个有数据可读的通道可以说是Read Ready，等待写数据的通道可以说是Write Ready。  

    &emsp; 2). 如果对多个事件感兴趣，可以通过or操作符来连接这些常量：  

    ```
    int interestSet = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
    ```

### 1.2.2. 选择键的使用，SelectionKey类的API  
&emsp; SelectionKey类的API：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/communication/NIO-13.png)  
&emsp; 请注意对register()的调用的返回值是一个SelectionKey。SelectionKey代表这个通道在此Selector上的这个注册。当某个Selector通知某个传入事件时，它是通过提供对应于该事件的SelectionKey来进行的。SelectionKey还可以用于取消通道的注册。   
&emsp; SelectionKey内包含有如下属性：  

    interest Set：兴趣集合，当前 channel感兴趣的操作
    ready Set：就绪集合，此SelectionKey 已经准备就绪的操作集合
    Channel：通道，获取此 SelectionKey 对应的 channel
    Selector：选择器，管理此 channel 的 Selector
    Attach：附加对象，向SelectionKey中添加更多的信息，方便之后的数据操作判断或获取

&emsp; SelectionKey还有几个重要的方法，用于检测Channel中什么事件或操作已经就绪，它们都会返回一个布尔类型：selectionKey.isAcceptable();selectionKey.isConnectable();selectionKey.isReadable();selectionKey.isWritable();   

### 1.2.3. 选择器的使用，selector类的API  
&emsp; selector的API：  

|方法	|描述|
|---|---|
|Selector open()	|打开一个选择器|
|boolean isOpen()	|判断选择器是否已打开|
|SelectorProvider provider()	|返回创建此通道的提供者|
|Set<SelectionKey\> keys()	|返回此选择器的键集|
|Set<SelectionKey\> selectedKeys()	|返回此选择器上相应的通道I/O操作准备就绪的选择键集|
|int selectNow()	|select()方法的非阻塞形式。不等于select(0)（无限期阻塞）。|
|int select(long timeout)| |	
|int select()	|返回一组键的个数，其相应的通道已为I/O操作准备就绪|
|Selector wakeup()	|使尚未返回的第一个选择操作立即返回|
|void close()	|关闭此选择器|

&emsp; Selector的基本使用流程：  

1. 通过Selector.open() 打开一个 Selector.
2. 将Channel注册到Selector中, 并设置需要监听的事件(interest set)
3. 不断重复:
    1. 调用select()方法
    2. 调用selector.selectedKeys() 获取selected keys
    3. 迭代每个 selected key:
        * 从selected key中获取对应的Channel和附加信息(如果有的话)。
        * 判断是哪些IO事件已经就绪, 然后处理它们。如果是OP_ACCEPT事件, 则调用"SocketChannel clientChannel = ((ServerSocketChannel) key.channel()).accept()" 获取SocketChannel, 并将它设置为 非阻塞的, 然后将这个Channel注册到Selector中。
        * 根据需要更改selected key的监听事件。
        * 将已经处理过的key从selected keys 集合中删除。

### 1.2.4. Selector完整实例  

```
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
public class TCPServer{
    // 超时时间，单位毫秒
    private static final int TimeOut = 3000;
    // 本地监听端口
    private static final int ListenPort = 1978;

    public static void main(String[] args) throws IOException{
        // 创建选择器
        Selector selector = Selector.open();
        // 打开监听信道
        ServerSocketChannel listenerChannel = ServerSocketChannel.open();
        // 与本地端口绑定
        listenerChannel.socket().bind(new InetSocketAddress(ListenPort));
        // 设置为非阻塞模式
        listenerChannel.configureBlocking(false);
        // 将选择器绑定到监听信道,只有非阻塞信道才可以注册选择器.并在注册过程中指出该信道可以进行Accept操作
        // 一个serversocket channel准备好接收新进入的连接称为“接收就绪”
        listenerChannel.register(selector, SelectionKey.OP_ACCEPT);

        // 反复循环,等待IO
        while (true){
            // 等待某信道就绪(或超时)
            int keys = selector.select(TimeOut);
            //刚启动时连续输出0，client连接后一直输出1
            if (keys == 0){
                System.out.println("独自等待.");
                continue;
            }

            // 取得迭代器，遍历每一个注册的通道
            Set<SelectionKey> set = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = set.iterator();

            while (keyIterator.hasNext()){
                SelectionKey key = keyIterator.next();
                if(key.isAcceptable()){
                    // a connection was accepted by a ServerSocketChannel.
                    // 可通过Channel()方法获取就绪的Channel并进一步处理
                    SocketChannel channel = (SocketChannel)key.channel();
                    // TODO
                }
                else if (key.isConnectable()){
                    // TODO
                }
                else if (key.isReadable()){
                    // TODO
                }
                else if (key.isWritable()){
                    // TODO
                }
                // 删除处理过的事件
                keyIterator.remove();
            }
        }
    }
}
```

&emsp; 特别说明：例子中selector只注册了一个Channel，注册多个Channel操作类似。如下：  

```
for (int i=0; i<3; i++){
    // 打开监听信道
    ServerSocketChannel listenerChannel = ServerSocketChannel.open();
    // 与本地端口绑定
    listenerChannel.socket().bind(new InetSocketAddress(ListenPort+i));
    // 设置为非阻塞模式
    listenerChannel.configureBlocking(false);
    // 注册到selector中
    listenerChannel.register(selector, SelectionKey.OP_ACCEPT);
}
```

&emsp; 在上面的例子中，对于通道IO事件的处理并没有给出具体方法，在此，举一个更详细的例子：  

```
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
public class NIO_Learning{
    private static final int BUF_SIZE = 256;
    private static final int TIMEOUT = 3000;

    public static void main(String args[]) throws Exception{
        // 打开服务端 Socket
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 打开 Selector
        Selector selector = Selector.open();
        // 服务端 Socket 监听8080端口, 并配置为非阻塞模式
        serverSocketChannel.socket().bind(new InetSocketAddress(8080));
        serverSocketChannel.configureBlocking(false);
        // 将 channel 注册到 selector 中.
        // 通常我们都是先注册一个 OP_ACCEPT 事件, 然后在 OP_ACCEPT 到来时, 再将这个 Channel 的 OP_READ 注册到 Selector 中.
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true){
            // 通过调用 select 方法, 阻塞地等待 channel I/O 可操作
            if (selector.select(TIMEOUT) == 0){
                System.out.print("超时等待...");
                continue;
            }
            // 获取 I/O 操作就绪的 SelectionKey, 通过 SelectionKey 可以知道哪些 Channel 的哪类 I/O 操作已经就绪.
            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
            while (keyIterator.hasNext()){
                SelectionKey key = keyIterator.next();
                // 当获取一个 SelectionKey 后, 就要将它删除, 表示我们已经对这个 IO 事件进行了处理.
                keyIterator.remove();
                if (key.isAcceptable()){
                    // 当 OP_ACCEPT 事件到来时, 我们就有从 ServerSocketChannel 中获取一个 SocketChannel,
                    // 代表客户端的连接
                    // 注意, 在 OP_ACCEPT 事件中, 从 key.channel() 返回的 Channel 是 ServerSocketChannel.
                    // 而在 OP_WRITE 和 OP_READ 中, 从 key.channel() 返回的是 SocketChannel.
                    SocketChannel clientChannel = ((ServerSocketChannel) key.channel()).accept();
                    clientChannel.configureBlocking(false);
                    //在 OP_ACCEPT 到来时, 再将这个 Channel 的 OP_READ 注册到 Selector 中.
                    // 注意, 这里我们如果没有设置 OP_READ 的话, 即 interest set 仍然是 OP_CONNECT 的话, 那么 select 方法会一直直接返回.
                    clientChannel.register(key.selector(), SelectionKey.OP_READ,ByteBuffer.allocate(BUF_SIZE));
                }

                if (key.isReadable()){
                    SocketChannel clientChannel = (SocketChannel) key.channel();
                    ByteBuffer buf = (ByteBuffer) key.attachment();
                    long bytesRead = clientChannel.read(buf);
                    if (bytesRead == -1){
                        clientChannel.close();
                    }
                    else if (bytesRead > 0){
                        key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        System.out.println("Get data length: " + bytesRead);
                    }
                }
                if (key.isValid() && key.isWritable()){
                    ByteBuffer buf = (ByteBuffer) key.attachment();
                    buf.flip();
                    SocketChannel clientChannel = (SocketChannel) key.channel();
                    clientChannel.write(buf);
                    if (!buf.hasRemaining()){
                        key.interestOps(SelectionKey.OP_READ);
                    }
                    buf.compact();
                }
            }
        }
    }
}
```
&emsp; 如从上述实例所示，可以将多个 Channel 注册到同一个Selector对象上，实现一个线程同时监控多个Channel的请求状态，但有一个不容忽视的缺陷：所有读/写请求以及对新连接请求的处理都在同一个线程中处理，无法充分利用多CPU的优势，同时读/写操作也会阻塞对新连接请求的处理。因此，有必要进行优化，可以引入多线程，并行处理多个读/写操作。  
&emsp; 一种优化策略是：将Selector进一步分解为Reactor，从而将不同的感兴趣事件分开，每一个Reactor只负责一种感兴趣的事件。这样做的好处是：分离阻塞级别，减少了轮询的时间；线程无需遍历set以找到自己感兴趣的事件，因为得到的set中仅包含自己感兴趣的事件。  

