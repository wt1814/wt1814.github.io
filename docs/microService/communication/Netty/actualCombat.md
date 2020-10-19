


# Netty实战  

<!-- 

使用 Netty 实现 IM 聊天贼简单，看不懂就锤爆艿艿的狗头~ 
https://mp.weixin.qq.com/s/5X1znb_G61CV6NxJ_MvmZw
实现分布式服务注册及简易的netty聊天 
https://mp.weixin.qq.com/s/AQjmwjz1HSW3KbZs28iQ1A
Springboot 整合 Netty 实战(附源码) 
https://mp.weixin.qq.com/s/a3r1hAm4xFkb-2W6rzHuVw
-->

Netty主要应用于以下领域：  

* 高性能的RPC框架：常用于微服务之间的高性能远程调用（如Dubbo）
* 游戏行业：Netty可以很轻松地定制和开发一个私有协议栈，
* 即时通讯：Netty基于Java NIO，并且做了一些优化，支持高性能的即时通讯


<!-- 

Netty 应用场景了解么？
凭借自己的了解，简单说一下吧！理论上来说，NIO 可以做的事情 ，使用 Netty 都可以做并且更好。Netty 主要用来做网络通信 :

    作为 RPC 框架的网络通信工具 ：我们在分布式系统中，不同服务节点之间经常需要相互调用，这个时候就需要 RPC 框架了。不同服务节点之间的通信是如何做的呢？可以使用 Netty 来做。比如我调用另外一个节点的方法的话，至少是要让对方知道我调用的是哪个类中的哪个方法以及相关参数吧！
    实现一个自己的 HTTP 服务器 ：通过 Netty 我们可以自己实现一个简单的 HTTP 服务器，这个大家应该不陌生。说到 HTTP 服务器的话，作为 Java 后端开发，我们一般使用 Tomcat 比较多。一个最基本的 HTTP 服务器可要以处理常见的 HTTP Method 的请求，比如 POST 请求、GET 请求等等。
    实现一个即时通讯系统 ：使用 Netty 我们可以实现一个可以聊天类似微信的即时通讯系统，这方面的开源项目还蛮多的，可以自行去 Github 找一找。
    实现消息推送系统 ：市面上有很多消息推送系统都是基于 Netty 来做的。
    ......
-->

## 基于Netty搭建消息推送系统  





