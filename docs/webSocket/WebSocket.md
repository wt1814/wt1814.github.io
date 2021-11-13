
<!-- TOC -->

- [1. WebSocket协议](#1-websocket协议)
    - [1.1. WebSocket协议是什么](#11-websocket协议是什么)
        - [1.1.1. WebSocket是应用层协议](#111-websocket是应用层协议)
        - [1.1.2. WebSocket与Http的区别](#112-websocket与http的区别)
    - [1.2. 为什么要使用WebSocket](#12-为什么要使用websocket)

<!-- /TOC -->


<!-- 
SpringBoot+WebSocket实时监控异常 
https://mp.weixin.qq.com/s/xdqer-DtZuaGsTloNkuB8w
分布式WebSocket集群解决方案 
https://mp.weixin.qq.com/s/5unniQ_b_MZZRRUkGoE4uA

关于即时通讯架构的一切
https://mp.weixin.qq.com/s/eynMy_1vqxrPkgw0r-7heg

-->

# 1. WebSocket协议
<!-- 
WebSocket协议入门介绍
https://www.cnblogs.com/nuccch/p/10947256.html
-->

## 1.1. WebSocket协议是什么
### 1.1.1. WebSocket是应用层协议
&emsp; WebSocket是基于TCP的应用层协议，用于在C/S架构的应用中实现双向通信，关于WebSocket协议的详细规范和定义参见[rfc6455](https://tools.ietf.org/html/rfc6455)。  
&emsp; 需要特别注意的是：虽然WebSocket协议在建立连接时会使用HTTP协议，但这并意味着WebSocket协议是基于HTTP协议实现的。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-135.png)  

### 1.1.2. WebSocket与Http的区别
&emsp; 实际上，WebSocket协议与Http协议有着本质的区别：  
1. 通信方式不同  
&emsp; WebSocket是双向通信模式，客户端与服务器之间只有在握手阶段是使用HTTP协议的“请求-响应”模式交互，而一旦连接建立之后的通信则使用双向模式交互，不论是客户端还是服务端都可以随时将数据发送给对方；而HTTP协议则至始至终都采用“请求-响应”模式进行通信。也正因为如此，HTTP协议的通信效率没有WebSocket高。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-136.png)  
2. 协议格式不同  
&emsp; WebSocket与HTTP的协议格式是完全不同的，具体来讲：  
&emsp; （1）HTTP协议（参见：rfc2616）比较臃肿，而WebSocket协议比较轻量。  
&emsp; （2）对于HTTP协议来讲，一个数据包就是一条完整的消息；而WebSocket客户端与服务端通信的最小单位是帧（frame），由1个或多个帧组成一条完整的消息（message）。即：发送端将消息切割成多个帧，并发送给服务端；服务端接收消息帧，并将关联的帧重新组装成完整的消息。  

## 1.2. 为什么要使用WebSocket
&emsp; 随着Web应用的发展，特别是动态网页的普及，越来越多的场景需要实现数据动态刷新。  
&emsp; 在早期的时候，实现数据刷新的方式通常有如下3种：  
1. 客户端定时查询  
&emsp; 客户端定时查询（如：每隔10秒钟查询一次）是最原始也是最简单的实现数据刷新的方法，服务端不用做任何改动，只需要在客户端添加一个定时器即可。但是这种方式的缺点也很明显：大量的定时请求都是无效的，因为服务端的数据并没有更新，相应地也导致了大量的带宽浪费。  
2. 长轮训机制  
&emsp; 长轮训机制是对客户端定时查询的一种改进，即：客户端依旧保持定时发送请求给服务端，但是服务端并不立即响应，而是等到真正有数据更新的时候才发送给客户端。实际上，并不是当没有数据更新时服务端就永远都不响应客户端，而是需要在等待一个超时时间之后结束该次长轮训请求。相对于客户端定时查询方式而言，当数据更新频率不确定时长轮训机制能够很明显地减少请求数。但是，在数据更新比较频繁的场景下，长轮训方式的优势就没那么明显了。  
&emsp; 在Web开发中使用得最为普遍的长轮训实现方案为Comet（Comet (web技术)），Tomcat和Jetty都有对应的实现支持，详见：WhatIsComet，Why Asynchronous Servlets。  
3. HTTP Streaming  
&emsp; 不论是长轮训机制还是传统的客户端定时查询方式，都需要客户端不断地发送请求以获取数据更新，而HTTP Streaming则试图改变这种方式，其实现机制为：客户端发送获取数据更新请求到服务端时，服务端将保持该请求的响应数据流一直打开，只要有数据更新就实时地发送给客户端。  
虽然这个设想是非常美好的，但这带来了新的问题：  
&emsp; (1)HTTP Streaming的实现机制违背了HTTP协议本身的语义，使得客户端与服务端不再是“请求-响应”的交互方式，而是直接在二者建立起了一个单向的“通信管道”。  
&emsp; (2)在HTTP Streaming模式下，服务端只要得到数据更新就发送给客户端，那么就需要客户端与服务端协商如何区分每一个更新数据包的开始和结尾，否则就可能出现解析数据错误的情况。  
&emsp; (3)另外，处于客户端与服务端的网络中介（如：代理）可能会缓存响应数据流，这可能会导致客户端无法真正获取到服务端的更新数据，这实际上与HTTP Streaming的本意是相违背的。  
&emsp; 鉴于上述原因，在实际应用中HTTP Streaming并没有真正流行起来，反之使用得最多的是长轮训机制。  

&emsp; 显然，上述几种实现数据动态刷新的方式都是基于HTTP协议实现的，或多或少地存在这样那样的问题和缺陷；而WebSocket是一个全新的应用层协议，专门用于Web应用中需要实现动态刷新的场景。  
&emsp; 相比起HTTP协议，WebSocket具备如下特点：  

1. 支持双向通信，实时性更强。
2. 更好的二进制支持。
3. 较少的控制开销：连接创建后，WebSockete客户端、服务端进行数据交换时，协议控制的数据包头部较小。
4. 支持扩展。

