
<!-- TOC -->

- [1. 多路复用(select/poll/epoll)](#1-多路复用selectpollepoll)
    - [1.1. select](#11-select)
        - [1.1.1. select()函数说明](#111-select函数说明)
        - [1.1.2. 调用select()函数示例](#112-调用select函数示例)
        - [1.1.3. select机制的问题总结](#113-select机制的问题总结)
    - [1.2. poll](#12-poll)
    - [1.3. epoll](#13-epoll)
        - [1.3.1. ~~epoll()操作函数~~](#131-epoll操作函数)
        - [1.3.2. 调用epoll()函数](#132-调用epoll函数)
        - [1.3.3. epoll工作模式](#133-epoll工作模式)
    - [1.4. ~~三者区别联系~~](#14-三者区别联系)
    - [1.5. 两种IO多路复用模式：Reactor和Proactor](#15-两种io多路复用模式reactor和proactor)

<!-- /TOC -->


&emsp; **<font color = "red">总结：</font>**  
1. **<font color = "clime">`~~select,poll,epoll只是I/O多路复用模型中第一阶段，即获取网络数据、用户态和内核态之间的拷贝。~~`</font>** 此阶段会阻塞线程。  
2. **select()：**  
    1. **select运行流程：**  
        &emsp; **<font color = "red">select()运行时会将fd_set集合从用户态拷贝到内核态。</font>** 在内核态中线性扫描socket，即采用轮询。如果有事件返回，会将内核态的数组相应的FD置位。最后再将内核态的数据返回用户态。  
    2. **select机制的问题：（拷贝、两次轮询、FD置位）**  
        * 为了减少数据拷贝带来的性能损坏，内核对被监控的fd_set集合大小做了限制，并且这个是通过宏控制的，大小不可改变（限制为1024）。  
        * 每次调用select， **<font color = "red">1)需要把fd_set集合从用户态拷贝到内核态，</font>** **<font color = "clime">2)需要在内核遍历传递进来的所有fd_set（对socket进行扫描时是线性扫描，即采用轮询的方法，效率较低），</font>** **<font color = "red">3)如果有数据返回还需要从内核态拷贝到用户态。</font>** 如果fd_set集合很大时，开销比较大。 
        * 由于运行时，需要将FD置位，导致fd_set集合不可重用。  
        * **<font color = "clime">select()函数返回后，</font>** 调用函数并不知道是哪几个流（可能有一个，多个，甚至全部）， **<font color = "clime">还得再次遍历fd_set集合处理数据，即采用无差别轮询。</font>**   
        * ~~惊群~~   
3. **poll()：** 运行机制与select()相似。将fd_set数组改为采用链表方式pollfds，没有连接数的限制，并且pollfds可重用。   
4. **epoll()：**   
    1. **epoll的三个函数：**  
        ![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-119.png)  
        * 调用epoll_create，会在内核cache里建个红黑树，同时也会再建立一个rdllist双向链表。 
        * epoll_ctl将被监听的描述符添加到红黑树或从红黑树中删除或者对监听事件进行修改。
        * 双向链表，用于存储准备就绪的事件，当epoll_wait调用时，仅查看这个rdllist双向链表数据即可。epoll_wait阻塞等待注册的事件发生，返回事件的数目，并将触发的事件写入events数组中。   

        -------------------
        1. 执行epoll_create()时，创建了红黑树和就绪链表；
        2. 执行epoll_ctl()时，如果增加socket句柄，则检查在红黑树中是否存在，存在立即返回，不存在则添加到树干上，然后向内核注册回调函数，用于当中断事件来临时向准备就绪链表中插入数据；
        3. 执行epoll_wait()时立刻返回准备就绪链表里的数据即可。

        -----------
        首先epoll_create创建一个epoll文件描述符，底层同时创建一个红黑树，和一个就绪链表；红黑树存储所监控的文件描述符的节点数据，就绪链表存储就绪的文件描述符的节点数据；epoll_ctl将会添加新的描述符，首先判断是红黑树上是否有此文件描述符节点，如果有，则立即返回。如果没有， 则在树干上插入新的节点，并且告知内核注册回调函数。当接收到某个文件描述符过来数据时，那么内核将该节点插入到就绪链表里面。epoll_wait将会接收到消息，并且将数据拷贝到用户空间，清空链表。对于LT模式epoll_wait清空就绪链表之后会检查该文件描述符是哪一种模式，如果为LT模式，且必须该节点确实有事件未处理，那么就会把该节点重新放入到刚刚删除掉的且刚准备好的就绪链表，epoll_wait马上返回。ＥＴ模式不会检查，只会调用一次
    2. **epoll机制的工作模式：**  
        * LT模式（默认，水平触发，level trigger）：当epoll_wait检测到某描述符事件就绪并通知应用程序时，应用程序可以不立即处理该事件；下次调用epoll_wait时，会再次响应应用程序并通知此事件。    
        * ET模式（边缘触发，edge trigger）：当epoll_wait检测到某描述符事件就绪并通知应用程序时，应用程序必须立即处理该事件。如果不处理，下次调用epoll_wait时，不会再次响应应用程序并通知此事件。（直到做了某些操作导致该描述符变成未就绪状态了，也就是说 **<font color = "clime">边缘触发只在状态由未就绪变为就绪时只通知一次。</font>** ）   

        &emsp; 由此可见：ET模式的效率比LT模式的效率要高很多。只是如果使用ET模式，就要保证每次进行数据处理时，要将其处理完，不能造成数据丢失，这样对编写代码的人要求就比较高。  
        &emsp; 注意：ET模式只支持非阻塞的读写：为了保证数据的完整性。  

    3. **epoll机制的优点：**  
        * 调用epoll_ctl时拷贝进内核并保存，之后每次epoll_wait不拷贝。  
        * epoll()函数返回后，调用函数以O(1)复杂度遍历。  
5. 两种IO多路复用模式：[Reactor和Proactor](/docs/microService/communication/Netty/Reactor.md)  


# 1. 多路复用(select/poll/epoll)
<!--

epoll
★★★ 你管这破玩意叫 IO 多路复用？ 
https://mp.weixin.qq.com/s/Ok7SIROXu1THUbWsFu-UYw
★★★ epoll原理详解（最清晰）
https://blog.csdn.net/lyztyycode/article/details/79491419

对惊群效应的处理 epoll
https://blog.csdn.net/qq422431474/article/details/108244352

★★★ https://www.kancloud.cn/machh03/server/2095673
https://mp.weixin.qq.com/s/BYaORGezXycLUFHcvi3ZnQ
https://mp.weixin.qq.com/s/uEoGXbweCxu08AShhsw9Xg

深入Hotspot源码与Linux内核理解NIO与Epoll 
https://mp.weixin.qq.com/s/WhfnTtMpY4EgT65UezKjtw
IO多路复用的三种机制Select，Poll，Epoll
https://www.jianshu.com/p/397449cadc9a
https://www.bilibili.com/read/cv6134546?share_medium=android&share_plat=android&share_source=WEIXIN&share_tag=s_i&timestamp=1596386488&unique_k=aZsmwN

IO多路复用
https://mp.weixin.qq.com/s/i3He95cfzyLF_I4v-X3tCw
https://mp.weixin.qq.com/s/iVfLZJ89UMtu3Z5IgpoCoQ

视频
https://ke.qq.com/webcourse/index.html#cid=398381&term_id=100475149&taid=9526675549590573&type=1024&vid=5285890803916888161
-->
<!--

XXXX 再谈Linux epoll惊群问题的原因和解决方案 
https://mp.weixin.qq.com/s/9quhzwMBoyOjSR3nY6klkw

https://www.cnblogs.com/Joy-Hu/p/10762239.html
~~
https://mp.weixin.qq.com/s/JPcOKoWhBDW59GpO37Jq4w
-->
&emsp; select,poll,epoll都是IO多路复用的机制。I/O多路复用就是通过一种机制，一个进程可以监视多个描述符，一旦某个描述符就绪(一般是读就绪或者写就绪)，能够通知程序进行相应的读写操作。  
&emsp; **<font color = "clime">select,poll,epoll只是I/O多路复用模型中第一阶段，即获取网络数据、用户态和内核态之间的拷贝。</font>** 此阶段会阻塞线程。  

&emsp; select,poll,epoll之所以现在同时存在，其实它们也是不同历史时期的产物：  

* select出现是1984年在BSD里面实现的。  
* 14年之后也就是1997年才实现了poll，其实拖那么久也不是效率问题，而是那个时代的硬件实在太弱，一台服务器处理1000多个连接简直就是神一样的存在了，select很长段时间已经满足需求。  
* 2002, 大神Davide Libenzi实现了epoll。  

## 1.1. select  

### 1.1.1. select()函数说明
&emsp; 下面是select的函数接口：  

```c
int select (int n, fd_set *readfds, fd_set *writefds, fd_set *exceptfds, struct timeval *timeout); 
```
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-45.png)  

&emsp; 【参数说明】  
&emsp; int maxfdp1，指定待测试的文件描述符个数，它的值是待测试的最大描述符加1。  
&emsp; fd_set \*readset, fd_set \*writeset, fd_set \*exceptset，fd_set可以理解为一个集合(**<font color = "red">实际上是一个long类型的数组，最高1024位</font>**)，这个集合中存放的是文件描述符(file descriptor)，即文件句柄。中间的三个参数指定要让内核测试读、写和异常条件的文件描述符集合。如果对某一个的条件不感兴趣，就可以把它设为空指针。  
&emsp; const struct timeval *timeout timeout，告知内核等待所指定文件描述符集合中的任何一个就绪可花多少时间。其timeval结构用于指定这段时间的秒数和微秒数。  

&emsp; 【返回值】  
&emsp; int，若有就绪描述符返回其数目，若超时则为0，若出错则为-1。  

&emsp; 【运行机制】  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-103.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-94.png)  
&emsp; select()运行时会将fd_set集合从用户态拷贝到内核态。在内核态中线性扫描socket，即采用轮询。如果有事件返回，会将内核态的数组相应的FD置位。最后再将内核态的数据返回用户态。  


--------

1. 步骤1的解法：select创建3个文件描述符集，并将这些文件描述符拷贝到内核中，这里限制了文件句柄的最大的数量为1024（注意是全部传入---第一次拷贝）；  
2. 步骤2的解法：内核针对读缓冲区和写缓冲区来判断是否可读可写,这个动作和select无关；  
3. 步骤3的解法：内核在检测到文件句柄可读/可写时就产生中断通知监控者select，select被内核触发之后，就返回可读可写的文件句柄的总数；  
4. 步骤4的解法：select会将之前传递给内核的文件句柄再次从内核传到用户态（第2次拷贝），select返回给用户态的只是可读可写的文件句柄总数，再使用FD_ISSET宏函数来检测哪些文件I/O可读可写（遍历）；  
5. 步骤5的解法：select对于事件的监控是建立在内核的修改之上的，也就是说经过一次监控之后，内核会修改位，因此再次监控时需要再次从用户态向内核态进行拷贝（第N次拷贝）  

### 1.1.2. 调用select()函数示例
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-95.png)  


### 1.1.3. select机制的问题总结
&emsp; **<font color = "clime">select机制的问题：</font>**  

* 为了减少数据拷贝带来的性能损坏，内核对被监控的fd_set集合大小做了限制，并且这个是通过宏控制的，大小不可改变(限制为1024)。  
* 每次调用select， **<font color = "red">1)需要把fd_set集合从用户态拷贝到内核态，</font>** **<font color = "clime">2)需要在内核遍历传递进来的所有fd_set(对socket进行扫描时是线性扫描，即采用轮询的方法，效率较低)，</font>** **<font color = "clime">3)如果有数据返回还需要从内核态拷贝到用户态。</font>** 如果fd_set集合很大时，开销比较大。 
* 由于运行时，需要将FD置位，导致fd_set集合不可重用。  
* **<font color = "clime">select()函数返回后，</font>** 调用函数并不知道是哪几个流(可能有一个，多个，甚至全部)， **<font color = "clime">还得再次遍历fd_set集合处理数据，即采用无差别轮询。</font>**     


## 1.2. poll  
&emsp; poll的机制与select类似，与select在本质上没有多大差别，管理多个描述符也是进行轮询，根据描述符的状态进行处理。  


```c
int poll(struct pollfd *fds, nfds_t nfds, int timeout);

typedef struct pollfd {
        int fd;                         // 需要被检测或选择的文件描述符
        short events;                   // 对文件描述符fd上感兴趣的事件
        short revents;                  // 文件描述符fd上当前实际发生的事件
} pollfd_t;

```

<!-- 
【参数说明】  
struct pollfd *fds fds是一个struct pollfd类型的数组，用于存放需要检测其状态的socket描述符，并且调用poll函数之后fds数组不会被清空；一个pollfd结构体表示一个被监视的文件描述符，通过传递fds指示 poll() 监视多个文件描述符。其中，结构体的events域是监视该文件描述符的事件掩码，由用户来设置这个域，结构体的revents域是文件描述符的操作结果事件掩码，内核在调用返回时设置这个域  
nfds_t nfds 记录数组fds中描述符的总数量  
【返回值】  
int 函数返回fds集合中就绪的读、写，或出错的描述符数量，返回0表示超时，返回-1表示出错；  
-->

&emsp; **<font color = "red">在poll()函数中采用链表的方式替换原有fd_set数据结构。使其没有连接数的限制。</font>**  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-45.png)  
&emsp; **<font color = "red">在内核中只将revents字段置位，pollfds链表可重用。</font>**   

## 1.3. epoll
&emsp; epoll是在2.6内核中提出的，是之前的select和poll的增强版本。相对于select和poll来说，epoll更加灵活，没有描述符限制。 **epoll使用一个文件描述符管理多个描述符，将用户关心的文件描述符的事件存放到内核的一个事件表中，这样在用户空间和内核空间的copy只需一次。**  
<!-- 
★★★
https://zhuanlan.zhihu.com/p/159135478
一举拿下 I/O 多路复用！
https://mp.weixin.qq.com/s/qVUXY7t515xmXIL8gQ1nBQ
-->

### 1.3.1. ~~epoll()操作函数~~
&emsp; epoll主要有epoll_create,epoll_ctl和epoll_wait三个函数。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-119.png)  

* int epoll_create(int size)  
&emsp; 函数创建epoll文件描述符，参数size并不是限制了epoll所能监听的描述符最大个数，只是对内核初始分配内部数据结构的一个建议。返回是epoll描述符。-1表示创建失败。  
&emsp; **<font color = "clime">调用epoll_create，会在内核cache里建个红黑树，epoll_ctl将被监听的描述符添加到红黑树或从红黑树中删除或者对监听事件进行修改；同时也会再建立一个rdllist双向链表，用于存储准备就绪的事件，当epoll_wait调用时，仅查看这个rdllist双向链表数据即可。</font>**     
* int epoll_ctl(int epfd, int op, int fd, struct epoll_event *event)  
&emsp; 控制对指定描述符fd执行op操作，event是与fd关联的监听事件。op操作有三种：添加EPOLL_CTL_ADD，删除EPOLL_CTL_DEL，修改EPOLL_CTL_MOD。分别添加、删除和修改对fd的监听事件。  
&emsp; 即epoll_ctl将被监听的描述符添加到红黑树或从红黑树中删除或者对监听事件进行修改。  
&emsp; 对于需要监视的文件描述符集合，epoll_ctl对红黑树进行管理，红黑树中每个成员由描述符值和所要监控的文件描述符指向的文件表项的引用等组成。  
* int epoll_wait(int epfd, struct epoll_event * events, int maxevents, int timeout)  
&emsp; 等待epfd上的io事件，最多返回maxevents个事件。  
&emsp; **参数events用来从内核得到事件的集合，** maxevents告之内核这个events有多大，这个maxevents的值不能大于创建epoll_create()时的size，参数timeout是超时时间(毫秒，0会立即返回，-1将不确定，也有说法说是永久阻塞)。该函数返回需要处理的事件数目，如返回0表示已超时。  


&emsp; 在select/poll中，进程只有在调用一定的方法后，内核才对所有监视的文件描述符进行扫描， **而epoll事先通过epoll_ctl()来注册一个文件描述符，一旦基于某个文件描述符就绪时，内核会采用类似callback的回调机制，迅速激活这个文件描述符，当进程调用epoll_wait()时便得到通知。**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-104.png)  


---------

1. 步骤1的解法：首先执行epoll_create在内核专属于epoll的高速cache区，并在该缓冲区建立红黑树和就绪链表，用户态传入的文件句柄将被放到红黑树中（第一次拷贝）。  
2. 步骤2的解法：内核针对读缓冲区和写缓冲区来判断是否可读可写，这个动作与epoll无关；  
3. 步骤3的解法：epoll_ctl执行add动作时除了将文件句柄放到红黑树上之外，还向内核注册了该文件句柄的回调函数，内核在检测到某句柄可读可写时则调用该回调函数，回调函数将文件句柄放到就绪链表。  
4. 步骤4的解法： **<font color = "clime">epoll_wait只监控就绪链表就可以，如果就绪链表有文件句柄，则表示该文件句柄可读可写，并返回到用户态（少量的拷贝）；</font>**  
5. 步骤5的解法：由于内核不修改文件句柄的位，因此只需要在第一次传入就可以重复监控，直到使用epoll_ctl删除，否则不需要重新传入，因此无多次拷贝。  
&emsp; 简单说：epoll是继承了select/poll的I/O复用的思想，并在二者的基础上从监控IO流、查找I/O事件等角度来提高效率，具体地说就是内核句柄列表、红黑树、就绪list链表来实现的。  


### 1.3.2. 调用epoll()函数
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-97.png)  


### 1.3.3. epoll工作模式
<!-- 
&emsp; epoll有两种工作方式：1.水平触发(LT)；2.边缘触发(ET)。  

* LT模式：若就绪的事件一次没有处理完要做的事件，就会一直去处理。即就会将没有处理完的事件继续放回到就绪队列之中(即那个内核中的链表)，一直进行处理。   
* ET模式：就绪的事件只能处理一次，若没有处理完会在下次的其它事件就绪时再进行处理。而若以后再也没有就绪的事件，那么剩余的那部分数据也会随之而丢失。   
-->
&emsp; epoll对文件描述符的操作有两种模式：  

* LT模式(默认，水平触发，level trigger)：当epoll_wait检测到某描述符事件就绪并通知应用程序时，应用程序可以不立即处理该事件；下次调用epoll_wait时，会再次响应应用程序并通知此事件。    
* ET模式(边缘触发，edge trigger)：当epoll_wait检测到某描述符事件就绪并通知应用程序时，应用程序必须立即处理该事件。如果不处理，下次调用epoll_wait时，不会再次响应应用程序并通知此事件。(直到做了某些操作导致该描述符变成未就绪状态了，也就是说边缘触发只在状态由未就绪变为就绪时只通知一次)。   

&emsp; 由此可见：ET模式的效率比LT模式的效率要高很多。只是如果使用ET模式，就要保证每次进行数据处理时，要将其处理完，不能造成数据丢失，这样对编写代码的人要求就比较高。  
&emsp; 注意：ET模式只支持非阻塞的读写：为了保证数据的完整性。  
    
    LT和ET原本应该是用于脉冲信号的，可能用它来解释更加形象。Level和Edge指的就是触发点，Level为只要处于水平，那么就一直触发，而Edge则为上升沿和下降沿的时候触发。比如：0->1 就是Edge，1->1就是Level。    

## 1.4. ~~三者区别联系~~  
&emsp; select有最大文件描述符的限制，只能监听到有几个文件描述符就绪了，得遍历所有文件描述符获取就绪的IO。  
&emsp; poll没有最大文件描述符的限制，与select一样，只能监听到有几个文件描述符就绪了，得遍历所有文件描述符获取就绪的IO。  
&emsp; epoll没有最大文件描述符的限制，它通过回调的机制，一旦某个文件描述符就绪了，迅速激活这个文件描述符，当进程下一次调用epoll_wait()的时候便得到通知。  
&emsp; 所以，在有大量空闲连接的时候，epoll的效率要高很多。  

|	|select| 	poll| 	epoll|
|---|---|---|---|
|操作方式 	|遍历 	|遍历 	|回调|
|底层实现 	|数组 |	链表 	|红黑树|
|IO效率| 	每次调用都进行线性遍历，时间复杂度为O(n) |	每次调用都进行线性遍历，时间复杂度为O(n) |	事件通知方式，每当fd就绪，系统注册的回调函数就会被调用，将就绪fd放到readyList里面，时间复杂度O(1)|
|最大连接数 |	1024(x86)或2048(x64)| 	无上限 |	无上限|
|fd拷贝 |每次调用select，都需要把fd集合从用户态拷贝到内核态 |	每次调用poll，都需要把fd集合从用户态拷贝到内核态 |	调用epoll_ctl时拷贝进内核并保存，之后每次epoll_wait不拷贝|

&emsp; epoll是Linux目前大规模网络并发程序开发的首选模型。在绝大多数情况下性能远超select和poll。目前流行的高性能web服务器Nginx正式依赖于epoll提供的高效网络套接字轮询服务。但是，在并发连接不高的情况下，多线程+阻塞I/O方式可能性能更好。

<!-- 

 epoll总结

在 select/poll中，进程只有在调用一定的方法后，内核才对所有监视的文件描述符进行扫描，而epoll事先通过epoll_ctl()来注册一 个文件描述符，一旦基于某个文件描述符就绪时，内核会采用类似callback的回调机制，迅速激活这个文件描述符，当进程调用epoll_wait() 时便得到通知。(此处去掉了遍历文件描述符，而是通过监听回调的的机制。这正是epoll的魅力所在。)

epoll的优点主要是一下几个方面：
1. 监视的描述符数量不受限制，它所支持的FD上限是最大可以打开文件的数目，这个数字一般远大于2048,举个例子,在1GB内存的机器上大约是10万左 右，具体数目可以cat /proc/sys/fs/file-max察看,一般来说这个数目和系统内存关系很大。select的最大缺点就是进程打开的fd是有数量限制的。这对 于连接数量比较大的服务器来说根本不能满足。虽然也可以选择多进程的解决方案( Apache就是这样实现的)，不过虽然linux上面创建进程的代价比较小，但仍旧是不可忽视的，加上进程间数据同步远比不上线程间同步的高效，所以也不是一种完美的方案。

    IO的效率不会随着监视fd的数量的增长而下降。epoll不同于select和poll轮询的方式，而是通过每个fd定义的回调函数来实现的。只有就绪的fd才会执行回调函数。

    如果没有大量的idle -connection或者dead-connection，epoll的效率并不会比select/poll高很多，但是当遇到大量的idle- connection，就会发现epoll的效率大大高于select/poll。

-->






&emsp; select/poll/epoll之间的区别：  

| |select|poll|epoll|
|---|---|---|---|
|数据结构|bitmap|数组|红黑树|
|最大连接数|1024|无上限	|无上限|
|fd拷贝|每次调用select拷贝	|每次调用poll拷贝|fd首次调用epoll_ctl拷贝，每次调用epoll_wait不拷贝|
|工作效率|轮询：O(n)|轮询：O(n)|	回调：O(1)|


## 1.5. 两种IO多路复用模式：Reactor和Proactor
&emsp; 参考[Reactor与EventLoop](/docs/microService/communication/Netty/Reactor.md)  
