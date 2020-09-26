

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

## RPC与
<!-- 
 
https://blog.csdn.net/u013952133/article/details/79256799
-->

## RPC架构  

<!-- 
 “RPC好，还是RESTful好？”，这个问题不简单！ 
https://mp.weixin.qq.com/s/gLxFvOo82xybsribOIXKYA
-->

## 同步调用与异步调用  


## 流行的RPC框架

Dubbo  

Dubbo 是阿里巴巴公司开源的一个Java高性能优秀的服务框架，使得应用可通过高性能的 RPC 实现服务的输出和输入功能，可以和 Spring框架无缝集成。目前已经进入Apache孵化器。  

Motan  

Motan是新浪微博开源的一个Java RPC框架。2016年5月开源。Motan 在微博平台中已经广泛应用，每天为数百个服务完成近千亿次的调用。  

gRPC  

gRPC是Google开发的高性能、通用的开源RPC框架，其由Google主要面向移动应用开发并基于HTTP/2协议标准而设计，基于ProtoBuf(Protocol Buffers)序列化协议开发，且支持众多开发语言。本身它不是分布式的，所以要实现上面的框架的功能需要进一步的开发。  

Thrift  

Thrift是Apache的一个跨语言的高性能的服务框架，也得到了广泛的应用。  


