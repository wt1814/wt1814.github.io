
<!-- TOC -->

- [1. 硬件层的并发安全](#1-硬件层的并发安全)
    - [1.1. 解决缓存一致性/【可见性】](#11-解决缓存一致性可见性)
        - [1.1.1. 总线锁](#111-总线锁)
        - [1.1.2. 缓存锁](#112-缓存锁)
        - [1.1.3. MESI缓存一致性协议](#113-mesi缓存一致性协议)
            - [1.1.3.1. 总线嗅探](#1131-总线嗅探)
            - [1.1.3.2. 总线风暴](#1132-总线风暴)
    - [1.2. 硬件层的内存屏障](#12-硬件层的内存屏障)

<!-- /TOC -->


&emsp; **<font color = "red">总结：</font>**  
1. 缓存一致性协议/【可见性】  
    1. 怎么解决缓存一致性问题呢？使用总线锁或缓存锁。  
        * 总线锁：cpu从主内存读取数据到高速缓存，会在总线对这个数据加锁，这样其他cpu无法去读或写这个数据，直到这个cpu使用完数据释放锁之后其他cpu才能读取该数据。  
        * 缓存锁：只要保证多个CPU缓存的同一份数据是一致的就可以了，基于缓存一致性协议来实现。  
    2. MESI缓存一致性协议  
        1. 缓存一致性协议有很多种，MESI(Modified-Exclusive-Shared-Invalid)协议其实是目前使用很广泛的缓存一致性协议，x86处理器所使用的缓存一致性协议就是基于MESI的。  
        2. 其他cpu通过 总线嗅探机制 可以感知到数据的变化从而将自己缓存里的数据失效。总线嗅探，**<font color = "red">每个CPU不断嗅探总线上传播的数据来检查自己缓存值是否过期了，如果处理器发现自己的缓存行对应的内存地址被修改，就会将当前处理器的缓存行设置为无效状态，当处理器对这个数据进行修改操作的时候，会重新从内存中把数据读取到处理器缓存中。</font>**    
        2. 总线嗅探会带来总线风暴。  
2. 内存屏障，禁止处理器重排序 / 【有序性】  
    &emsp; Java中如何保证底层操作的有序性和可见性？可以通过内存屏障。   
    &emsp; 内存屏障，禁止处理器重排序，保障缓存一致性。  
    1. 内存屏障的作用：（~~原子性~~、可见性、有序性）  
        1. （`保障可见性`）它会强制将对缓存的修改操作立即写入主存； 如果是写操作，会触发总线嗅探机制(MESI)，会导致其他CPU中对应的缓存行无效，也有 [伪共享问题](/docs/java/concurrent/PseudoSharing.md)。  
        2. （`保障有序性`）阻止屏障两侧的指令重排序。 

# 1. 硬件层的并发安全  
<!-- 

*** https://mp.weixin.qq.com/s/WTqdSz-lc5zzelJgk4Co8g
https://blog.csdn.net/w1453114339/article/details/107563613
https://www.jianshu.com/p/06717ac8312c
https://www.freesion.com/article/73021012217/

-->

<!-- 
https://www.cnblogs.com/Courage129/p/14401680.html
-->




## 1.1. 解决缓存一致性/【可见性】
&emsp; **<font color = "clime">怎么解决缓存一致性问题呢？使用总线锁或缓存锁。</font>**  

### 1.1.1. 总线锁  
&emsp; 早期，cpu从主内存读取数据到高速缓存，会在总线对这个数据加锁，这样其他cpu无法去读或写这个数据，直到这个cpu使用完数据释放锁之后其他cpu才能读取该数据。  
![image](http://www.wt1814.com/static/view/images/java/concurrent/multi-46.png)  

### 1.1.2. 缓存锁  
&emsp; 缓存锁：只要保证多个CPU缓存的同一份数据是一致的就可以了，基于缓存一致性协议来实现。  


### 1.1.3. MESI缓存一致性协议  
<!-- 
~~
https://mp.weixin.qq.com/s/yWifJmirZNnBrAIZrpJwyg
看懂这篇，才能说了解并发底层技术！ 
https://mp.weixin.qq.com/s/SZl2E5NAhpYM4kKv9gyQOQ
-->
&emsp; 缓存一致性协议有很多种，MESI(Modified-Exclusive-Shared-Invalid)协议其实是目前使用很广泛的缓存一致性协议，x86处理器所使用的缓存一致性协议就是基于MESI的。  

&emsp; **<font color = "clime">多个cpu从主内存读取同一个数据到各自的高速缓存，当其中某个cpu修改了缓存里的数据，该数据会马上同步回主内存，其他cpu通过</font>** **<font color = "clime">总线嗅探机制</font>** **<font color = "red">可以感知到数据的变化从而将自己缓存里的数据失效。</font>**  

![image](http://www.wt1814.com/static/view/images/java/concurrent/multi-47.png)  

#### 1.1.3.1. 总线嗅探  
&emsp; **<font color = "red">每个CPU不断嗅探总线上传播的数据来检查自己缓存值是否过期了，如果处理器发现自己的缓存行对应的内存地址被修改，就会将当前处理器的缓存行设置为无效状态，当处理器对这个数据进行修改操作的时候，会重新从内存中把数据读取到处理器缓存中。</font>**  
![image](http://www.wt1814.com/static/view/images/java/concurrent/multi-46.png)  

#### 1.1.3.2. 总线风暴
&emsp; 总线嗅探技术有哪些缺点？  
&emsp; 由于MESI缓存一致性协议，需要不断对主线进行内存嗅探，大量的交互会导致总线带宽达到峰值。   
&emsp; 因此不要滥用volatile，可以用锁来替代，看使用场景。  


## 1.2. 硬件层的内存屏障

<!-- 
https://blog.csdn.net/breakout_alex/article/details/94379895

-->

* 不同CPU硬件对于JVM的内存屏障规范实现指令不一样
* Intel CPU硬件级内存屏障实现指令
	* Ifence：是一种Load Barrier读屏障，实现LoadLoad屏障
	* sfence：是一种Store Barrier写屏障，实现StoreStore屏障
	* mfence：是一种全能型的屏障，具备Ifencce和sfence的能留，具备所有屏障能力
* JVM底层简化了内存屏障硬件指令的实现
	* lock前缀：lock指令不是一种内存屏障，但是它能完成类似内存屏障的功能
