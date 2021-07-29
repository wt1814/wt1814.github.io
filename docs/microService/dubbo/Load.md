

<!-- TOC -->

- [1. Dubbo](#1-dubbo)
    - [1.1. ★★★负载均衡](#11-★★★负载均衡)
    - [1.2. ★★★集群容错策略](#12-★★★集群容错策略)
    - [1.3. 服务降级](#13-服务降级)

<!-- /TOC -->

# 1. Dubbo
## 1.1. ★★★负载均衡  
<!-- https://mp.weixin.qq.com/s/xkwwAUV9ziabPNUMEr5DPQ -->
* <font color = "red">Random(缺省)，随机，按权重设置随机概率。</font>在一个截面上碰撞的概率高，但调用量越大分布越均匀，而且按概率使用权重后也比较均匀，有利于动态调整提供者权重。  
* <font color = "red">RoundRobin，轮循，按公约后的权重设置轮循比率。</font>  
&emsp; 轮询负载均衡算法的不足：存在慢的提供者累积请求的问题，比如：第二台机器很慢，但没挂，当请求调到第二台时就卡在那，久而久之，所有请求都卡在调到第二台上。  
* <font color = "red">LeastActive，最少活跃调用数，活跃数指调用前后计数差。</font>相同活跃数的随机。使慢的提供者收到更少请求，因为越慢的提供者的调用前后计数差会越大。  
* <font color = "clime">ConsistentHash，[分布式一致性哈希算法](/docs/microService/thinking/分布式算法-consistent.md)。</font>相同参数的请求总是发到同一提供者；当某一台提供者崩溃时，原本发往该提供者的请求，基于虚拟节点，平摊到其它提供者，不会引起剧烈变动。  

    * 缺省只对第一个参数Hash，如果要修改，请配置`<dubbo:parameter key="hash.arguments" value="0,1" />`  
    * 缺省用160份虚拟节点，如果要修改，请配置`<dubbo:parameter key="hash.nodes" value="320" />`  

## 1.2. ★★★集群容错策略  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-13.png)   
&emsp; <font color = "red">在集群调用失败时，Dubbo 提供了多种容错方案，缺省为 failover 重试。</font>下面列举dubbo支持的容错策略：  

* Failover(默认) - 失败自动切换，当出现失败，重试其它服务器。通常用于读操作，但重试会带来更长延迟。可通过 retries="2" 来设置重试次数(不含第一次)。  
* Failfast - 快速失败，只发起一次调用，失败立即报错。通常用于非幂等性的写操作，比如新增记录。
* Failsafe - 失败安全，出现异常时，直接忽略。通常用于写入审计日志等操作。  
* Failback - 失败自动恢复，后台记录失败请求，定时重发。通常用于消息通知操作。  
* Forking - 并行调用多个服务器，只要一个成功即返回。通常用于实时性要求较高的读操作，但需要浪费更多服务资源。可通过 forks="2" 来设置最大并行数。  
* Broadcast - 广播调用所有提供者，逐个调用，任意一台报错则报错。通常用于通知所有提供者更新缓存或日志等本地资源信息。  

<!-- 
Failover Cluster失败自动切换：dubbo的默认容错方案，当调用失败时自动切换到其他可用的节点，具体的重试次数和间隔时间可用通过引用服务的时候配置，默认重试次数为1也就是只调用一次。
Failback Cluster快速失败：在调用失败，记录日志和调用信息，然后返回空结果给consumer，并且通过定时任务每隔5秒对失败的调用进行重试

Failfast Cluster失败自动恢复：只会调用一次，失败后立刻抛出异常

Failsafe Cluster失败安全：调用出现异常，记录日志不抛出，返回空结果

Forking Cluster并行调用多个服务提供者：通过线程池创建多个线程，并发调用多个provider，结果保存到阻塞队列，只要有一个provider成功返回了结果，就会立刻返回结果

Broadcast Cluster广播模式：逐个调用每个provider，如果其中一台报错，在循环调用结束后，抛出异常。
-->
## 1.3. 服务降级  
&emsp; 当服务器压力过大时，可以通过服务降级来使某些非关键服务的调用变得简单；可以对其直接进行屏蔽，即客户端不发送请求直接返回null；也可以正常发送请求当请求超时或不可达时再返回null。  
&emsp; 服务降级的相关配置可以直接在dubbo-admin的监控页面进行配置；通常是基于消费者来配置的，在dubbo-admin找到对应的消费者想要降级的服务，点击其后面的屏蔽或容错按钮即可生效；其中，屏蔽按钮点击表示放弃远程调用直接返回空，而容错按钮点击表示继续尝试进行远程调用当调用失败时再返回空。  