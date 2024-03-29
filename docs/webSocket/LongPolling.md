

<!-- TOC -->

- [1. 通信技术](#1-通信技术)
    - [1.1. Http的长连接与短连接](#11-http的长连接与短连接)
    - [1.2. 传统轮询（Polling）](#12-传统轮询polling)
    - [1.3. COMET](#13-comet)
        - [1.3.1. 长轮询](#131-长轮询)
            - [1.3.1.1. 长轮询](#1311-长轮询)
            - [1.3.1.2. 长轮询和长短连接的联系、区别](#1312-长轮询和长短连接的联系区别)
        - [1.3.2. 基于iframe的长连接流（stream）模式](#132-基于iframe的长连接流stream模式)
    - [1.4. SSE (Server-Sent Events)](#14-sse-server-sent-events)
    - [1.5. WebSocket](#15-websocket)
    - [1.6. 四种Web即时通信技术](#16-四种web即时通信技术)

<!-- /TOC -->



# 1. 通信技术  
<!-- 
实时消息推送
https://mp.weixin.qq.com/s/-6A-VZEdT06_88v98wPFQQ

传统轮询、长轮询、服务器发送事件与WebSocket
https://www.cnblogs.com/huchong/p/8595644.html
https://cloud.tencent.com/developer/article/1821509

https://www.cnblogs.com/huchong/p/8595644.html

-->

&emsp; Web端即时通讯技术：即时通讯技术简单的说就是实现这样一种功能：服务器端可以即时地将数据的更新或变化反应到客户端，例如消息即时推送等功能都是通过这种技术实现的。但是在Web中，由于浏览器的限制，实现即时通讯需要借助一些方法。这种限制出现的主要原因是，一般的Web通信都是浏览器先发送请求到服务器，服务器再进行响应完成数据的现实更新。  
&emsp; 实现Web端即时通讯的方法：实现即时通讯主要有四种方式，它们分别是轮询、长轮询(comet)、长连接(SSE)、WebSocket。它们大体可以分为两类，一种是在HTTP基础上实现的，包括短轮询、comet和SSE；另一种不是在HTTP基础上实现是，即WebSocket。下面分别介绍一下这四种轮询方式，以及它们各自的优缺点。  



## 1.1. Http的长连接与短连接  
<!-- 
https://juejin.cn/post/6844903955240058893
https://www.cnblogs.com/knowledgesea/p/6813832.html
-->

## 1.2. 传统轮询（Polling）  
&emsp; 为了定时获取并刷新页面上的数据，客户端定时向服务器发送Ajax请求，服务器接到请求后马上返回响应信息并关闭连接。   

## 1.3. COMET
&emsp; **Alex Russell（Dojo Toolkit 的项目 Lead）** 称这种基于HTTP长连接、无须在浏览器端安装插件的“服务器推”技术为“Comet”。  
&emsp; 常用的COMET分为两种：基于HTTP的长轮询（long-polling）技术，以及基于iframe的长连接流（stream）模式。  


### 1.3.1. 长轮询  
#### 1.3.1.1. 长轮询  
<!-- 
上面所说的传统轮询方式都存在一个严重缺陷：程序每发出一次请求就要新建一个Http请求。因为发起Http请求时会有很多头部信息，真正的请求信息几乎很少，这样就会造成资源浪费，频繁的轮询使得Web服务器遭受"凌迟"之苦。  

而长轮询意味着浏览器只需启动一个HTTP请求，其连接的服务器会“hold”住此次连接，直到有新消息才返回响应信息并关闭连接，客户端处理完响应信息后再向服务器发送新的Http请求,以此类推。  
-->
&emsp; 客户端发送请求后服务器端不会立即返回数据，服务器端会阻塞请求连接不会立即断开，直到服务器端有数据更新或者是连接超时才返回，客户端才再次发出请求新建连接、如此反复从而获取最新数据。大致效果如下：  
![image](http://182.92.69.8:8081/img/websocket/websocket-1.png)  

&emsp; 客户端的代码如下：  

```java
function LongPolling() {
    fetch(url).then(data => {
        LongPolling();
    }).catch(err => {
        LongPolling();
        console.log(err);
    });
}
LongPolling();
```

&emsp; 优缺点：  

* 优点：长轮询和短轮询比起来，明显减少了很多不必要的http请求次数，相比之下节约了资源。  
* 缺点：连接挂起也会导致资源的浪费。   

#### 1.3.1.2. 长轮询和长短连接的联系、区别  
<!-- 

https://www.cnblogs.com/knowledgesea/p/6813832.html
-->

### 1.3.2. 基于iframe的长连接流（stream）模式
<!-- 
https://juejin.cn/post/6844903955240058893
-->

## 1.4. SSE (Server-Sent Events)  
<!-- 
https://juejin.cn/post/6844903955240058893
-->
&emsp; Server-Sent是HTML5提出一个标准。由客户端发起与服务器之间创建TCP连接，然后并维持这个连接，直到客户端或服务器中的任何一方断开，ServerSent使用的是"问"+"答"的机制，连接创建后浏览器会周期性地发送消息至服务器询问，是否有自己的消息。其实现原理类似于我们在上一节中提到的基于iframe的长连接模式。  
&emsp; HTTP响应内容有一种特殊的content-type —— text/event-stream，该响应头标识了响应内容为事件流，客户端不会关闭连接，而是等待服务端不断得发送响应结果。  
&emsp; SSE规范比较简单，主要分为两个部分：浏览器中的EventSource对象，以及服务器端与浏览器端之间的通讯协议。  


&emsp; SSE是HTML5新增的功能，全称为Server-Sent Events。它可以允许服务推送数据到客户端。SSE在本质上就与之前的长轮询、短轮询不同，虽然都是基于http协议的，但是轮询需要客户端先发送请求。而SSE最大的特点就是不需要客户端发送请求，可以实现只要服务器端数据有更新，就可以马上发送到客户端。  
&emsp; SSE的优势很明显，它不需要建立或保持大量的客户端发往服务器端的请求，节约了很多资源，提升应用性能。并且后面会介绍道，SSE的实现非常简单，并且不需要依赖其他插件。  

## 1.5. WebSocket
&emsp; [WebSocket协议](/docs/webSocket/WebSocket.md)  

&emsp; WebSocket是Html5定义的一个新协议，与传统的http协议不同，该协议可以实现服务器与客户端之间全双工通信。简单来说，首先需要在客户端和服务器端建立起一个连接，这部分需要http。连接一旦建立，客户端和服务器端就处于平等的地位，可以相互发送数据，不存在请求和响应的区别。  
&emsp; WebSocket的优点是实现了双向通信，缺点是服务器端的逻辑非常复杂。现在针对不同的后台语言有不同的插件可以使用。  


## 1.6. 四种Web即时通信技术
<!-- 
https://juejin.cn/post/6844903955240058893
-->

&emsp; 四种Web即时通信技术比较它们的实现方式和各自的优缺点。 对比优缺点如下：  
![image](http://182.92.69.8:8081/img/websocket/websocket-2.png)  

&emsp; 上面四种Web即时通信技术比较，可以从不同的角度考虑，它们的优先级是不同的，基本上可以分为两大类基于http和tcp两种通信中的一种。  

* 兼容性考虑：短轮询>长轮询>长连接SSE>WebSocket
* 从性能方面考虑：WebSocket>长连接SSE>长轮询>短轮询
* 服务端推送：WebSocket>长连接SSE>长轮询
