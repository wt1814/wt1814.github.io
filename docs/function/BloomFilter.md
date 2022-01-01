

<!-- TOC -->

- [1. 布隆过滤器](#1-布隆过滤器)
    - [1.1. 布隆过滤器介绍](#11-布隆过滤器介绍)
    - [1.2. 布隆算法实现](#12-布隆算法实现)
    - [bitmap和布隆过滤器的区别](#bitmap和布隆过滤器的区别)

<!-- /TOC -->


# 1. 布隆过滤器  
<!-- 
牛逼哄哄的 BitMap
https://mp.weixin.qq.com/s/8tmjHoYvPW61C9fCnJoFdQ

-->

## 1.1. 布隆过滤器介绍
&emsp; BloomFilter是由一个固定大小的二进制向量或者位图(bitmap)和一系列(通常好几个)映射函数组成的。  
&emsp; **1. 布隆过滤器的原理：**  
&emsp; **<font color = "red">当一个变量被加入集合时，通过K个映射函数将这个变量映射成位图中的 K 个点，把它们置为1。</font>**  
![image](https://gitee.com/wt1814/pic-host/raw/master/algorithm/function-2.png)  

&emsp; 查询某个变量的时候，只要看看这些点是不是都是1，就可以大概率知道集合中有没有它了。  

* 如果这些点有任何一个0，则被查询变量一定不在；
* 如果都是1，则被查询变量很可能在。  

&emsp; 注意，这里是<font color = "clime">可能存在，而不是一定存在！</font>  

&emsp; **2. 布隆过滤器的特点：**  
* 优点：占用内存少，新增、查询效率高。  
* 缺点： **<font color = "red">误判率和不能删除。</font>**  

        布隆过滤器的误判是指多个输入经过哈希之后在相同的bit位置1了，这样就无法判断究竟是哪个输入产生的，因此误判的根源在于相同的bit位被多次映射且置1。  
        这种情况也造成了布隆过滤器的删除问题，因为布隆过滤器的每一个bit并不是独占的，很有可能多个元素共享了某一位。如果直接删除这一位的话，会影响其他的元素。  

* 特点总结：  
    * **<font color = "clime">一个元素如果判断结果为存在的时候元素不一定存在(可能存在)，但是判断结果为不存在的时候则一定不存在。</font>**  
    * **<font color = "red">布隆过滤器可以添加元素，但是不能删除元素。</font><font color = "clime">因为删掉元素会导致误判率增加。</font>**  

&emsp; **3. 布隆过滤器的使用场景：** 布隆过滤器适合于一些需要去重，但不一定要完全精确的场景。比如：  
&emsp; &emsp; 1. 黑名单  
&emsp; &emsp; 2. URL去重  
&emsp; &emsp; 3. 单词拼写检查  
&emsp; &emsp; 4. Key-Value缓存系统的Key校验   
&emsp; &emsp; 5. ID校验，比如订单系统查询某个订单ID是否存在，如果不存在就直接返回。

## 1.2. 布隆算法实现  
&emsp; 布隆算法实现有RedisBloom、guava的BloomFilter。  


## bitmap和布隆过滤器的区别
<!--

http://www.javashuo.com/article/p-saxrpzqf-kp.html

1. bitmap更适合用于数字比较：  
&emsp; 比如比较两个数组是否有重叠，把第一个数组中的1,2,5,7,11分别映射到bitmap位置中  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/structure/structure-2.png)  
&emsp; 其他数组只需要把值当成索引号去bitmap中查看是否值=1  
&emsp; 确定就是假如我是 1,100000000，那么其实只需要用到2位，但是却需要100000000位内存
由此我们确定了布隆过滤  

2. 布隆过滤器适合非数字比较（有误判）  
&emsp; 当一个元素被加入集合时，通过 K 个 Hash函数将这个元素映射成一个位阵列（Bit array）中的 K 个点，把它们置为 1  
&emsp; 也就是说一个数据可能占用多个bit，hash函数越多误判越少 但是消耗内存越多  

-->

&emsp; bitmap虽然好用，可是对于不少实际状况下的大数据处理它仍是远远不够的， **<font color = "clime">例如若是要进行64bit的long型数据去重，那咱们须要含有2^61个byte的byte数组来存储，这显然是不现实的。</font>** 那咱们如何来优化呢，很明显假如咱们申请了这么打的byte数组来标记数据，可想而知其空间利用率是极地的。布隆过滤器正是经过提升空间利用率来进行标记的。   


