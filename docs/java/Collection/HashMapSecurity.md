


<!-- TOC -->

- [1. HashMap的线程安全问题](#1-hashmap的线程安全问题)
    - [1.1. JDK1.8](#11-jdk18)
    - [1.2. JDK1.7](#12-jdk17)
        - [1.2.1. ~~死循环~~](#121-死循环)
            - [1.2.1.1. 示例](#1211-示例)
            - [1.2.1.2. 死循环分析](#1212-死循环分析)
    - [1.3. 线程安全的map](#13-线程安全的map)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
&emsp; HashMap的线程不安全体现在会造成死循环、数据丢失、数据覆盖这些问题。其中死循环和数据丢失是在JDK1.7中出现的问题，在JDK1.8中已经得到解决，然而1.8中仍会有数据覆盖这样的问题。  
1. 在jdk1.8中，在多线程环境下，会发生数据覆盖的情况。  
&emsp; 假设两个线程A、B都在进行put操作，并且hash函数计算出的插入下标是相同的，当线程A执行完第六行代码后由于时间片耗尽导致被挂起，而线程B得到时间片后在该下标处插入了元素，完成了正常的插入，`然后线程A获得时间片，由于之前已经进行了hash碰撞的判断，所有此时不会再进行判断，而是直接进行插入，这就导致了线程B插入的数据被线程A覆盖了，从而线程不安全。`  
2. HashMap导致CPU100% 的原因是因为 HashMap 死循环导致的。导致死循环的根本原因是JDK 1.7扩容采用的是“头插法”，会导致同一索引位置的节点在扩容后顺序反掉。而JDK 1.8之后采用的是“尾插法”，扩容后节点顺序不会反掉，不存在死循环问题。  
&emsp; 导致死循环示例：线程1、2同时扩容。线程1指向节点和下一节点，线程挂起。线程2完成扩容，此时线程1唤醒。线程1继续完成头节点插入，形成闭环。    
&emsp; 发生死循环后，剩余元素无法搬运，并且线程不会停止，因此会造成CPU100%。  
3. 线程安全的hashMap：Hashtable、Collections.synchronizedMap、[ConcurrentHashMap](/docs/java/concurrent/ConcurrentHashMap.md)。  

# 1. HashMap的线程安全问题

<!-- 

踩坑！JDK8中HashMap依然会死循环！ 
https://mp.weixin.qq.com/s/BKPKvcENMEQSELPQ4C7ZvQ
-->


&emsp; HashMap在数组的元素过多时会进行扩容操作，扩容之后会把原数组中的元素拿到新的数组中，这时候在多线程情况下就有可能出现多个线程搬运一个元素。或者说一个线程正在进行扩容，但是另一个线程还想进来存或者读元素，这也可能会出现线程安全问题。   

&emsp; **<font color = "clime">HashMap的线程不安全体现在会造成死循环、数据丢失、数据覆盖这些问题。其中死循环和数据丢失是在JDK1.7中出现的问题，在JDK1.8中已经得到解决，然而1.8中仍会有数据覆盖这样的问题。</font>**  

## 1.1. JDK1.8
<!-- 
https://blog.csdn.net/swpu_ocean/article/details/88917958
-->

&emsp;  **<font color = "red">在jdk1.8中，在多线程环境下，会发生数据覆盖的情况。</font>**  
&emsp; 在jdk1.8中对HashMap进行了优化，在发生hash碰撞，不再采用头插法方式，而是直接插入链表尾部，因此不会出现JDK1.7环形链表的情况。  

&emsp; <font color = "red">其中putVal()的第6行代码（if ((p = tab[i = (n - 1) & hash]) == null) // 如果没有hash碰撞则直接插入元素）是判断是否出现hash碰撞，</font>假设两个线程A、B都在进行put操作，并且hash函数计算出的插入下标是相同的，<font color = "red">当线程A执行完第六行代码后由于时间片耗尽导致被挂起，而线程B得到时间片后在该下标处插入了元素，完成了正常的插入，</font>然后线程A获得时间片，由于之前已经进行了hash碰撞的判断，所有此时不会再进行判断，而是直接进行插入，这就导致了线程B插入的数据被线程A覆盖了，从而线程不安全。  

&emsp; 除此之外，还有就是putVal()的第38行处有个++size，还是线程A、B，这两个线程同时进行put操作时，假设当前HashMap的sise大小为10，当线程A执行到第38行代码时，从主内存中获得size的值为10后准备进行+1操作，但是由于时间片耗尽只好让出CPU，线程B拿到CPU还是从主内存中拿到size的值10进行+1操作，完成了put操作并将size=11写回主内存，然后线程A再次拿到CPU并继续执行(此时size的值仍为10)，当执行完put操作后，还是将size=11写回内存，此时，线程A、B都执行了一次put操作，但是size的值只增加了1，所以说还是由于数据覆盖又导致了线程不安全。  

## 1.2. JDK1.7

### 1.2.1. ~~死循环~~
<!--

**** https://blog.csdn.net/J080624/article/details/87923678
**** https://mp.weixin.qq.com/s/wIjAj4rAAZccAl-yhmj_TA
https://blog.csdn.net/swpu_ocean/article/details/88917958
https://mp.weixin.qq.com/s?__biz=MzkzODE3OTI0Ng==&mid=2247491120&idx=1&sn=44228b42b8f54508f2ee01af9a3a231e&source=41#wechat_redirect
https://mp.weixin.qq.com/s/ZL2tgDZ5RAZvqrHo2qHPiQ
-->

&emsp; HashMap 导致 CPU 100% 的原因就是因为 HashMap 死循环导致的。  
&emsp; 导致死循环的根本原因是 JDK 1.7 扩容采用的是“头插法”，会导致同一索引位置的节点在扩容后顺序反掉。而 JDK 1.8 之后采用的是“尾插法”，扩容后节点顺序不会反掉，不存在死循环问题。  

#### 1.2.1.1. 示例  
<!-- 
https://mp.weixin.qq.com/s?__biz=MzkzODE3OTI0Ng==&mid=2247491120&idx=1&sn=44228b42b8f54508f2ee01af9a3a231e&source=41#wechat_redirect
-->

#### 1.2.1.2. 死循环分析  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JDK/Collection/collection-21.png)  
&emsp; 例子：有1个容量为2的 HashMap，loadFactor=0.75，此时线程1和线程2 同时往该 HashMap 插入一个数据，都触发了扩容流程，接着有以下流程。   

&emsp; 1）在2个线程都插入节点，触发扩容流程之前，此时的结构如下图。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JDK/Collection/collection-22.png)  
&emsp; 2）线程1进行扩容，执行到代码：Entry<K,V> next = e.next 后被调度挂起，此时的结构如下图。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JDK/Collection/collection-23.png)  
&emsp; 3）线程1被挂起后，线程2进入扩容流程，并走完整个扩容流程，此时的结构如下图。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JDK/Collection/collection-24.png)  
&emsp; 由于两个线程操作的是同一个 table，所以该图又可以画成如下图。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JDK/Collection/collection-25.png)  
&emsp; 4）线程1恢复后，继续走完第一次的循环流程，此时的结构如下图。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JDK/Collection/collection-26.png)  
&emsp; 5）线程1继续走完第二次循环，此时的结构如下图。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JDK/Collection/collection-27.png)  
&emsp; 6）线程1继续执行第三次循环，执行到 e.next = newTable[i] 时形成环，执行完第三次循环的结构如下图。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JDK/Collection/collection-28.png)  


## 1.3. 线程安全的map
&emsp; 在多线程下安全的操作map，主要有以下解决方法：  

* 使用Hashtable线程安全类；  
* 使用Collections 包下的线程安全的容器比如Collections.synchronizedMap方法，对方法进行加同步锁；  
* 使用并发包中的[ConcurrentHashMap](/docs/java/concurrent/ConcurrentHashMap.md)类；  

