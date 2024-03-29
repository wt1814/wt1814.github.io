
<!-- TOC -->

- [1. SDS字符串](#1-sds字符串)
    - [1.1. SDS代码结构](#11-sds代码结构)
    - [1.2. ★★★SDS动态扩展特点](#12-★★★sds动态扩展特点)
    - [1.3. Redis字符串的性能优势](#13-redis字符串的性能优势)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. **<font color = "clime">对于SDS中的定义在Redis的源码中有的三个属性int len、int free、char buf[]。</font>**  
    ![image](http://182.92.69.8:8081/img/microService/Redis/redis-77.png)  
    * len保存了字符串的长度；
    * free表示buf数组中未使用的字节数量；
    * buf数组则是保存字符串的每一个字符元素。  
2. Redis字符串追加会做以下三个操作：  
    1. 计算出大小是否足够；  
    2. 开辟空间至满足所需大小；  
    3. **<font color = "red">如果len < 1M，开辟与已使用大小len相同长度的空闲free空间；如果len >= 1M，开辟1M长度的空闲free空间。</font>**  
3. **Redis字符串的性能优势：**  
    * 动态扩展：拼接字符串时，计算出大小是否足够，开辟空间至满足所需大小。  
    * 避免缓冲区溢出。「c语言」中两个字符串拼接，若是没有分配足够长度的内存空间就「会出现缓冲区溢出的情况」。  
    * （内存分配优化）降低空间分配次数，提升内存使用效率。 **<font color = "blue">空间预分配和惰性空间回收。</font>** 
        * 空间预分配：对于追加操作来说，Redis不仅会开辟空间至够用，<font color = "red">而且还会预分配未使用的空间(free)来用于下一次操作。</font>  
        * 惰性空间回收：与上面情况相反，<font color = "red">惰性空间回收适用于字符串缩减操作。</font>比如有个字符串s1="hello world"，对s1进行sdstrim(s1," world")操作，<font color = "red">执行完该操作之后Redis不会立即回收减少的部分，而是会分配给下一个需要内存的程序。</font>  
    * 快速获取字符串长度。
    * 二进制安全。

# 1. SDS字符串  
<!-- 
*** embstr编码的简单动态字符串（SDS）

https://www.jianshu.com/p/738a0a17e723


sds 空间分配策略
https://mp.weixin.qq.com/s/R7Ux8ji6WpvfZ45VUt4Aiw
https://mp.weixin.qq.com/s/VY31lBOSggOHvVf54GzvYw
https://mp.weixin.qq.com/s/f71rakde6KBJ_ilRf1M8xQ
-->
<!-- 
1. 什么是 SDS？ Redis中字符串的实现。在 3.2 以后的版本中，SDS 又有多种结构(sds.h)：sdshdr5、sdshdr8、sdshdr16、sdshdr32、sdshdr64，用于存储不同的长度的字符串，分别代表 2^5=32byte， 2^8=256byte，2^16=65536byte=64KB，2^32byte=4GB。  

2. 为什么 Redis 要用 SDS 实现字符串？  
&emsp; C 语言本身没有字符串类型(只能用字符数组 char[]实现)。 
    1. 使用字符数组必须先给目标变量分配足够的空间，否则可能会溢出。  
    2. 如果要获取字符长度，必须遍历字符数组，时间复杂度是 O(n)。  
    3. C 字符串长度的变更会对字符数组做内存重分配。  
    4. 通过从字符串开始到结尾碰到的第一个'\0'来标记字符串的结束，因此不能保 存图片、音频、视频、压缩文件等二进制(bytes)保存的内容，二进制不安全。  

    &emsp; SDS的特点：  
    1. <font color = "red">不用担心内存溢出问题，如果需要，会对SDS进行扩容。</font>  
    2. <font color = "red">获取字符串长度时间复杂度为 O(1)，因为定义了 len 属性。</font>  
    3. 通过“空间预分配”( sdsMakeRoomFor)和“惰性空间释放”，防止多次重分配内存。  
    4. 判断是否结束的标志是 len 属性(它同样以'\0'结尾是因为这样就可以使用 C语言中函数库操作字符串的函数了)，可以包含'\0'。 
-->
&emsp; Redis是C语言开发的，C语言有字符类型，但是Redis却没直接采用C语言的字符串类型，而是自己构建了动态字符串(SDS)的抽象类型。  
 
## 1.1. SDS代码结构  

```c
struct sdshdr{
    // 记录已使用长度
    int len;
    // 记录空闲未使用的长度
    int free;
    // 字符数组
    char[] buf;
};
```
&emsp; **<font color = "clime">对于SDS中的定义在Redis的源码中有的三个属性int len、int free、char buf[]。</font>** <font color = "red">len保存了字符串的长度，free表示buf数组中未使用的字节数量，buf数组则是保存字符串的每一个字符元素。</font>  
![image](http://182.92.69.8:8081/img/microService/Redis/redis-77.png)  
&emsp; Redis的字符串也会遵守C语言的字符串的实现规则，即最后一个字符为空字符。然而这个空字符不会被计算在len里头。  

## 1.2. ★★★SDS动态扩展特点
&emsp; SDS的最厉害最奇妙之处在于它的Dynamic，动态变化长度。举个例子：  
![image](http://182.92.69.8:8081/img/microService/Redis/redis-78.png)  

&emsp; 如上图所示刚开始s1 只有5个空闲位子，后面需要追加' world' 6个字符，很明显是不够的。 

&emsp; Redis会做以下三个操作：  
1. 计算出大小是否足够；  
2. 开辟空间至满足所需大小；  
3. **<font color = "red">如果len < 1M，开辟与已使用大小len相同长度的空闲free空间；如果len >= 1M，开辟1M长度的空闲free空间。</font>**  

## 1.3. Redis字符串的性能优势  

* 动态扩展
* 快速获取字符串长度  
* 避免缓冲区溢出  
* 降低空间分配次数提升内存使用效率  
* 二进制安全

1. <font color = "clime">快速获取字符串长度</font>  

        c语言中的字符串并不会记录自己的长度，因此「每次获取字符串的长度都会遍历得到，时间的复杂度是O(n)」，而Redis中获取字符串只要读取len的值就可，时间复杂度变为O(1)。

2. <font color = "clime">避免缓冲区溢出</font>  
&emsp; **<font color = "red">「c语言」中两个字符串拼接，若是没有分配足够长度的内存空间就「会出现缓冲区溢出的情况」。</font>**    
&emsp; 对于Redis而言由于每次追加字符串时，<font color = "red">「SDS」会先根据len属性判断空间是否满足要求，若是空间不够，就会进行相应的空间扩展，所以「不会出现缓冲区溢出的情况」。</font>每次追加操作前都会做如下操作：  
    1. 计算出大小是否足够  
    2. 开辟空间至满足所需大小  
    3. 降低空间分配次数提升内存使用效率  


3. <font color = "clime">降低空间分配次数，提升内存使用效率</font>  
    &emsp; **字符串的追加、缩减操作会涉及到内存分配问题，** 然而内存分配问题会牵扯内存划分算法以及系统调用，所以如果频繁发生的话，会影响性能。所以采取了以下两种优化措施空间预分配、惰性空间回收。  

    1. <font color = "clime">空间预分配</font>   
        &emsp; 对于追加操作来说，Redis不仅会开辟空间至够用，<font color = "red">而且还会预分配未使用的空间(free)来用于下一次操作。</font>至于未使用的空间(free)的大小则由修改后的字符串长度决定。
        
        * 当修改后的字符串长度len < 1M，则会分配与len相同长度的未使用的空间(free)
        * 当修改后的字符串长度len >= 1M，则会分配1M长度的未使用的空间(free)

        有了这个预分配策略之后会减少内存分配次数，因为分配之前会检查已有的free空间是否够，如果够则不开辟了。
    2. <font color = "clime">惰性空间回收</font>  
        &emsp; 与上面情况相反，<font color = "red">惰性空间回收适用于字符串缩减操作。</font>比如有个字符串s1="hello world"，对s1进行sdstrim(s1," world")操作，<font color = "red">执行完该操作之后Redis不会立即回收减少的部分，而是会分配给下一个需要内存的程序。</font>

<!-- 
SDS还提供「空间预分配」和「惰性空间释放」两种策略。在为字符串分配空间时，分配的空间比实际要多，这样就能「减少连续的执行字符串增长带来内存重新分配的次数」。
当字符串被缩短的时候，SDS也不会立即回收不适用的空间，而是通过free属性将不使用的空间记录下来，等后面使用的时候再释放。
具体的空间预分配原则是：「当修改字符串后的长度len小于1MB，就会预分配和len一样长度的空间，即len=free；若是len大于1MB，free分配的空间大小就为1MB」。
-->

4. <font color = "clime">二进制安全</font>  
&emsp; SDS是二进制安全的，除了可以储存字符串以外还可以储存二进制文件(如图片、音频，视频等文件的二进制数据)；而c语言中的字符串是以空字符串作为结束符，一些图片中含有结束符，因此不是二进制安全的。  