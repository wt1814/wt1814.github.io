

&emsp; &emsp; [TCP粘拆包与Netty编解码](/docs/microService/communication/Netty/Decoder.md)  
&emsp; &emsp; [Netty实战](/docs/microService/communication/Netty/actualCombat.md)  
&emsp; &emsp; [Netty多协议开发](/docs/microService/communication/Netty/MultiProtocol.md) 


&emsp; Netty应用场景，Netty主要用来做网络通信：  

* 作为 RPC 框架的网络通信工具 ：在分布式系统中，不同服务节点之间经常需要相互调用，这个时候就需要 RPC 框架了。不同服务节点之间的通信是如何做的呢？可以使用 Netty 来做。比如调用另外一个节点的方法的话，至少是要让对方知道我调用的是哪个类中的哪个方法以及相关参数吧！  
* 实现一个自己的 HTTP 服务器：通过 Netty 可以自己实现一个简单的 HTTP 服务器，这个大家应该不陌生。说到 HTTP 服务器的话，作为 Java 后端开发，我们一般使用 Tomcat 比较多。一个最基本的 HTTP 服务器可要以处理常见的 HTTP Method 的请求，比如 POST 请求、GET 请求等等。  
* 实现一个即时通讯系统：使用 Netty 我们可以实现一个可以聊天类似微信的即时通讯系统，这方面的开源项目还蛮多的，可以自行去 Github 找一找。  
* 实现消息推送系统：市面上有很多消息推送系统都是基于 Netty 来做的。......
* ...  
