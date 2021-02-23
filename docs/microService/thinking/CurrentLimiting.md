

<!-- TOC -->

- [1. 限流](#1-限流)
    - [1.1. 限流介绍](#11-限流介绍)
    - [1.2. 限流对象分类](#12-限流对象分类)
    - [1.3. 限流算法详解](#13-限流算法详解)
        - [1.3.1. 固定窗口计数器，控制并发数量](#131-固定窗口计数器控制并发数量)
        - [1.3.2. 滑动窗口](#132-滑动窗口)
        - [1.3.3. 漏桶算法，控制访问速率](#133-漏桶算法控制访问速率)
        - [1.3.4. 令牌桶算法](#134-令牌桶算法)
    - [1.4. 限流方式](#14-限流方式)
    - [1.5. 设计要点](#15-设计要点)
    - [1.6. 分布式限流实现](#16-分布式限流实现)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  

1. 限流对象分类：基于请求限流、基于资源限流。  
2. 限流算法：  
    * 固定窗口算法有时会让通过请求量允许为限制的两倍。  
    * 滑动窗口算法避免了固定窗口计数器带来的双倍突发请求。但时间区间的精度越高，算法所需的空间容量就越大。  
    * 漏桶算法实现流量整形和流量控制。漏洞底部的设计大小固定，水流速度固定。 漏桶算法的缺陷也很明显，当短时间内有大量的突发请求时，即便此时服务器没有任何负载，每个请求也都得在队列中等待一段时间才能被响应。  
    * 令牌桶算法，1. 只要令牌桶中存在令牌，那么就允许突发地传输数据直到达到用户配置的门限，所以它适合于具有突发特性的流量。 **<font color = "red">2. 此外，两者算法在实现上的区别：漏桶算法中“水滴”代表请求，令牌桶中“水滴”代表请求令牌。</font>**  
3. 限流方式有服务降级、服务拒绝。 

# 1. 限流  
<!-- 
后端服务不得不了解之限流
https://mp.weixin.qq.com/s/RTrpnVovjhC1iZYuFttWhg
-->
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/problems/problem-28.png)  

&emsp; **<font color = "lime">在开发高并发系统时有三把利器用来保护系统：缓存、降级和限流。</font>**   

## 1.1. 限流介绍  
&emsp; 由于API接口无法控制调用方的行为，因此当遇到瞬时请求量激增时，会导致接口占用过多服务器资源，使得其他请求响应速度降低或是超时，更有甚者可能导致服务器宕机。接口限流可以解决此问题。  
&emsp; 限流(Rate limiting)指对应用服务的请求进行限制，例如某一接口的请求限制为100个每秒，对超过限制的请求则进行快速失败或丢弃。  

&emsp; <font color="red">限流可以应对：热点业务带来的突发请求、调用方bug导致的突发请求、恶意攻击请求。</font>因此，对于公开的接口最好采取限流措施。  

&emsp; **<font color = "red">一个限流系统的设计要考虑限流对象、限流算法、限流方式、限流设计的要点。</font>**  

## 1.2. 限流对象分类
&emsp; 按照对象类型分类：

* 基于请求限流  
&emsp; 基于请求限流，一般的实现方式有限制总量和限制QPS。限制总量就是限制某个指标的上限，比如抢购某一个商品，放量是10w，那么最多只能卖出10w件。微信的抢红包，群里发一个红包拆分为10个，那么最多只能有10人可以抢到，第十一个人打开就会显示『手慢了，红包派完了』。  
&emsp; 限制QPS，也是常说的限流方式，只要在接口层级进行，某一个接口只允许1秒只能访问100次，那么它的峰值QPS只能为100。限制QPS的方式最难的点就是如何预估阈值，如何定位阈值，下文中会说到。  
* 基于资源限流  
&emsp; 基于资源限流是基于服务资源的使用情况进行限制，需要定位到服务的关键资源有哪些，并对其进行限制，如限制TCP连接数、线程数、内存使用量等。限制资源更能有效地反映出服务当前地清理，但与限制QPS类似，面临着如何确认资源的阈值为多少。这个阈值需要不断地调优，不停地实践才可以得到一个较为满意地值。  

## 1.3. 限流算法详解  
&emsp; 常见的限流算法有：固定窗口计数器、滑动窗口计数器、漏桶法和令牌桶算法。  

### 1.3.1. 固定窗口计数器，控制并发数量  
&emsp; 单位时间内，所接受的QPS的请求数目，如果超过阈值，则直接拒绝服务。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/problems/problem-23.png)  

&emsp; 固定窗口计数器算法概念如下：  

* 将时间划分为多个窗口；  
* 在每个窗口内每有一次请求就将计数器加一；  
* 如果计数器超过了限制数量，则本窗口内所有的请求都被丢弃当时间到达下一个窗口时，计数器重置。  

&emsp; 固定窗口计数器是最为简单的算法，但<font color="red">固定窗口算法有时会让通过请求量允许为限制的两倍</font>。考虑如下情况：限制1秒内最多通过5个请求，在第一个窗口的最后半秒内通过了5个请求，第二个窗口的前半秒内又通过了5个请求。这样看来就是在1秒内通过了10个请求。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/problems/problem-24.png)  
&emsp; Java内部可以通过原子类计数器AtomicInteger、信号量Semaphore来做简单的限流。  

```java
// 限流的个数
private int maxCount = 5;
// 指定的时间内
private long interval = 60;
// 原子类计数器
private AtomicInteger atomicInteger = new AtomicInteger(0);
// 起始时间
private long startTime = System.currentTimeMillis();

public boolean limit(int maxCount, int interval) {
    atomicInteger.addAndGet(1);
    if (atomicInteger.get() == 1) {
        startTime = System.currentTimeMillis();
        atomicInteger.addAndGet(1);
        return true;
    }
    // 超过了间隔时间，直接重新开始计数
    if (System.currentTimeMillis() - startTime > interval * 1000) {
        startTime = System.currentTimeMillis();
        atomicInteger.set(1);
        return true;
    }
    // 还在间隔时间内,check有没有超过限流的个数
    if (atomicInteger.get() > maxCount) {
        return false;
    }
    return true;
}
```

### 1.3.2. 滑动窗口  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/problems/problem-25.png)  
&emsp; 滑动窗口计数器算法概念如下：  

* 将时间划分为多个区间；  
* 维持一个时间窗口，占据多个区间。在每个区间内每有一次请求就将计数器加一；  
* 每经过一个区间的时间，则抛弃最老的一个区间，并纳入最新的一个区间；  
* 如果当前窗口内区间的请求计数总和超过了限制数量，则本窗口内所有的请求都被丢弃。  

&emsp; 服务最多只能每秒钟处理5个请求。可以设置一个1秒钟的滑动窗口，窗口中有5个格子，每个格子200毫秒，每200毫秒移动一次，每次移动都需要记录当前服务请求的次数。  
&emsp; 内存中需要保存5次的次数。可以用数据结构LinkedList来实现。格子每次移动的时候判断一次，当前访问次数和LinkedList中最后一个相差是否超过200，如果超过就需要限流了。  
&emsp; 示例代码如下：  

```java
//服务访问次数，可以放在Redis中，实现分布式系统的访问计数
Long counter = 0L;
//使用LinkedList来记录滑动窗口的5个格子。
LinkedList<Long> ll = new LinkedList<Long>();

public static void main(String[] args){
    Counter counter = new Counter();

    counter.doCheck();
}

private void doCheck(){
    while (true){
        ll.addLast(counter);

        if (ll.size() > 5){
            ll.removeFirst();
        }

        //比较最后一个和第一个，两者相差一秒
        if ((ll.peekLast() - ll.peekFirst()) > 200){
            //To limit rate
        }

        Thread.sleep(200);
    }
}
```
&emsp; **计数器算法与滑动窗口算法比较：**  
&emsp; 计数器算法可以看成是滑动窗口的低精度实现。滑动窗口计数器是通过将窗口再细分，并且按照时间"滑动"，这种算法<font color = "red">避免了固定窗口计数器带来的双倍突发请求。但时间区间的精度越高，算法所需的空间容量就越大。</font>  

### 1.3.3. 漏桶算法，控制访问速率  
&emsp; 漏桶算法也是一种非常常用的限流算法，可以用来<font color = "red">实现流量整形(Traffic Shaping)和流量控制(Traffic Policing)。</font><font color = "lime">漏洞底部的设计大小固定，水流速度固定。</font>  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/problems/problem-26.png)  

&emsp; 漏桶算法概念如下：  

* 将每个请求视作"水滴"放入"漏桶"进行存储；  
* "漏桶"以固定速率向外"漏"出请求来执行；  
* 如果"漏桶"空了则停止"漏水"；如果"漏桶"满了则多余的"水滴"会被直接丢弃。  

&emsp; <font color = "red">漏桶算法的缺陷也很明显，当短时间内有大量的突发请求时，即便此时服务器没有任何负载，每个请求也都得在队列中等待一段时间才能被响应。</font>  
&emsp; 漏桶算法多使用队列实现，服务的请求会存到队列中，服务的提供方则按照固定的速率从队列中取出请求并执行，过多的请求则放在队列中排队或直接拒绝。   

### 1.3.4. 令牌桶算法  
<!-- https://baike.baidu.com/item/%E4%BB%A4%E7%89%8C%E6%A1%B6%E7%AE%97%E6%B3%95/6597000?fr=aladdin
-->
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/problems/problem-27.png)  

&emsp; 令牌桶算法概念如下：  

* 令牌以固定速率生成，生成的令牌放入令牌桶中存放；  
* 当请求到达时，会尝试从令牌桶中取令牌，取到了令牌的请求可以执行；  
* 如果桶空了，那么尝试取令牌的请求会被直接丢弃；如果令牌桶满了则多余的令牌会直接丢弃。  

&emsp; **漏桶和令牌桶的比较：**  
&emsp; 两者主要区别在于“漏桶算法”能够强行限制数据的传输速率，而“令牌桶算法”在能够限制数据的平均传输速率外，还允许某种程度的突发传输。 **<font color = "red">在“令牌桶算法”中，只要令牌桶中存在令牌，那么就允许突发地传输数据直到达到用户配置的门限，所以它适合于具有突发特性的流量。</font>**  
&emsp; 需要说明的是：在某些情况下，漏桶算法不能够有效地使用网络资源。因为漏桶的漏出速率是固定的，所以即使网络中没有发生拥塞，漏桶算法也不能使某一个单独的数据流达到端口速率。因此，漏桶算法对于存在突发特性的流量来说缺乏效率。而令牌桶算法则能够满足这些具有突发特性的流量。通常，漏桶算法与令牌桶算法结合起来为网络流量提供更高效的控制。  
&emsp; **<font color = "red">此外，两者算法在实现上的区别：漏桶算法中“水滴”代表请求，令牌桶中“水滴”代表请求令牌。</font>**     

&emsp; ~~实现思路：可以准备一个队列，用来保存令牌，另外通过一个线程池定期生成令牌放到队列中，每来一个请求，就从队列中获取一个令牌，并继续执行。~~  
&emsp; 可以使用Guava的RateLimiter来实现。  

## 1.4. 限流方式
&emsp; 限流方式有服务降级、服务拒绝。    

## 1.5. 设计要点   

* 手动开关，主动运维和应急使用；  
* 监控通知，限流发生时有关人员要清楚；  
* 用户感知，如返回特定错误信息；  
* 链路标识，RPC链路加入限流标识方便上下游业务识别限流场景做不同处理；  

## 1.6. 分布式限流实现  
&emsp; Java单机限流可以使用AtomicInteger、Semaphore或Guava的RateLimiter来实现，但是上述方案都不支持集群限流。集群限流的应用场景有两个，一个是网关，常用的方案有Nginx限流和Spring Cloud Gateway，另一个场景是与外部或者下游服务接口的交互，可以使用redis+lua实现。阿里巴巴的开源限流系统Sentinel也可以针对接口限流。

<!-- 
Guava的RateLimite
https://mp.weixin.qq.com/s/zGSvz6LyoenQw1TeWFLpuQ
-->