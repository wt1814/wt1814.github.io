
<!-- TOC -->

- [1. 多路复用(select poll epoll)](#1-多路复用select-poll-epoll)
    - [1.2. select](#12-select)
    - [1.3. poll](#13-poll)
    - [1.4. epoll](#14-epoll)
        - [epoll操作过程](#epoll操作过程)
        - [epoll工作模式](#epoll工作模式)
    - [1.5. 三者区别联系](#15-三者区别联系)

<!-- /TOC -->

<!-- 

https://mp.weixin.qq.com/s/JPcOKoWhBDW59GpO37Jq4w
-->

# 1. 多路复用(select poll epoll)
&emsp; 在Linux Socket服务器短编程时，为了处理大量客户的连接请求，需要使用非阻塞I/O和复用，select、poll和epoll是Linux API提供的I/O复用方式。  
&emsp; select，poll，epoll都是IO多路复用的机制。I/O多路复用就是通过一种机制，一个进程可以监视多个描述符，一旦某个描述符就绪（一般是读就绪或者写就绪），能够通知程序进行相应的读写操作。但select，poll，epoll本质上都是同步I/O，因为它们都需要在读写事件就绪后自己负责进行读写，也就是说这个读写过程是阻塞的，而异步I/O则无需自己负责进行读写，异步I/O的实现会负责把数据从内核拷贝到用户空间。  

## 1.2. select  
&emsp; 下面是select的函数接口：  

```c
int select (int n, fd_set *readfds, fd_set *writefds, fd_set *exceptfds, struct timeval *timeout); 
```
<!-- 
&emsp; 【参数说明】  
&emsp; int maxfdp1 指定待测试的文件描述字个数，它的值是待测试的最大描述字加1。  
&emsp; fd_set *readset , fd_set *writeset , fd_set *exceptset  
&emsp; fd_set可以理解为一个集合，这个集合中存放的是文件描述符(file descriptor)，即文件句柄。中间的三个参数指定我们要让内核测试读、写和异常条件的文件描述符集合。如果对某一个的条件不感兴趣，就可以把它设为空指针。  
&emsp; const struct timeval *timeout timeout告知内核等待所指定文件描述符集合中的任何一个就绪可花多少时间。其timeval结构用于指定这段时间的秒数和微秒数。  

&emsp; 【返回值】  
&emsp; int 若有就绪描述符返回其数目，若超时则为0，若出错则为-1  
-->
&emsp; select 函数监视的文件描述符分3类，分别是writefds、readfds、和exceptfds。调用后select函数会阻塞，直到有描述符就绪（有数据 可读、可写、或者有except），或者超时（timeout指定等待时间，如果立即返回设为null即可），函数返回。当select函数返回后，可以通过遍历fdset，来找到就绪的描述符。  
&emsp; select目前几乎在所有的平台上支持，其良好跨平台支持也是它的一个优点。select的一 个缺点在于单个进程能够监视的文件描述符的数量存在最大限制，在Linux上一般为1024，可以通过修改宏定义甚至重新编译内核的方式提升这一限制，但 是这样也会造成效率的降低。  

&emsp; **select运行机制**  
&emsp; select()的机制中提供一种fd_set的数据结构，实际上是一个long类型的数组，每一个数组元素都能与一打开的文件句柄（不管是Socket句柄,还是其他文件或命名管道或设备句柄）建立联系，建立联系的工作由程序员完成，当调用select()时，由内核根据IO状态修改fd_set的内容，由此来通知执行了select()的进程哪一Socket或文件可读。  
&emsp; 从流程上来看，使用select函数进行IO请求和同步阻塞模型没有太大的区别，甚至还多了添加监视socket，以及调用select函数的额外操作，效率更差。但是，使用select以后最大的优势是用户可以在一个线程内同时处理多个socket的IO请求。用户可以注册多个socket，然后不断地调用select读取被激活的socket，即可达到在同一个线程内同时处理多个IO请求的目的。而在同步阻塞模型中，必须通过多线程的方式才能达到这个目的。  

&emsp; **select机制的问题**  

* 每次调用select，都需要把fd_set集合从用户态拷贝到内核态，如果fd_set集合很大时，那这个开销也很大  
* 同时每次调用select都需要在内核遍历传递进来的所有fd_set，如果fd_set集合很大时，那这个开销也很大  
* 为了减少数据拷贝带来的性能损坏，内核对被监控的fd_set集合大小做了限制，并且这个是通过宏控制的，大小不可改变(限制为1024)  


![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-45.png)  

&emsp; 时间复杂度:O(n)  
&emsp; fd_set(监听的端口个数)：32位机默认是1024个，64位机默认是2048。  
&emsp; 缺点：  

* 单进程可以打开fd有限制；  
* 对socket进行扫描时是线性扫描，即采用轮询的方法，效率较低；  
* 用户空间和内核空间的复制非常消耗资源；  

## 1.3. poll  
&emsp; poll的机制与select类似，与select在本质上没有多大差别，管理多个描述符也是进行轮询，根据描述符的状态进行处理，但是poll没有最大文件描述符数量的限制。也就是说，poll只解决了上面的问题3，并没有解决问题1，2的性能开销问题。  
&emsp; 不同与select使用三个位图来表示三个fdset的方式，poll使用一个pollfd的指针实现。  

```c
int poll(struct pollfd *fds, nfds_t nfds, int timeout);

typedef struct pollfd {
        int fd;                         // 需要被检测或选择的文件描述符
        short events;                   // 对文件描述符fd上感兴趣的事件
        short revents;                  // 文件描述符fd上当前实际发生的事件
} pollfd_t;

```
&emsp; poll改变了文件描述符集合的描述方式，使用了pollfd结构而不是select的fd_set结构，使得poll支持的文件描述符集合限制远大于select的1024  
<!-- 
【参数说明】  
struct pollfd *fds fds是一个struct pollfd类型的数组，用于存放需要检测其状态的socket描述符，并且调用poll函数之后fds数组不会被清空；一个pollfd结构体表示一个被监视的文件描述符，通过传递fds指示 poll() 监视多个文件描述符。其中，结构体的events域是监视该文件描述符的事件掩码，由用户来设置这个域，结构体的revents域是文件描述符的操作结果事件掩码，内核在调用返回时设置这个域  
nfds_t nfds 记录数组fds中描述符的总数量  
【返回值】  
int 函数返回fds集合中就绪的读、写，或出错的描述符数量，返回0表示超时，返回-1表示出错；  
-->
&emsp; pollfd结构包含了要监视的event和发生的event，不再使用select“参数-值”传递的方式。同时，pollfd并没有最大数量限制（但是数量过大后性能也是会下降）。 和select函数一样，poll返回后，需要轮询pollfd来获取就绪的描述符。  
&emsp; 从上面看，select和poll都需要在返回后，通过遍历文件描述符来获取已经就绪的socket。事实上，同时连接的大量客户端在一时刻可能只有很少的处于就绪状态，因此随着监视的描述符数量的增长，其效率也会线性下降。  

&emsp; 调用过程和select类似。  
&emsp; 时间复杂度:O(n)。  
&emsp; 其和select不同的地方：采用链表的方式替换原有fd_set数据结构,而使其没有连接数的限制。

## 1.4. epoll
&emsp; epoll是在2.6内核中提出的，是之前的select和poll的增强版本。相对于select和poll来说，epoll更加灵活，没有描述符限制。epoll使用一个文件描述符管理多个描述符，将用户关系的文件描述符的事件存放到内核的一个事件表中，这样在用户空间和内核空间的copy只需一次。  

### epoll操作过程
&emsp; epoll的接口如下：  

```c
int epoll_create(int size)；  
int epoll_ctl(int epfd, int op, int fd, struct epoll_event *event)；  
            typedef union epoll_data {  
                void *ptr;  
                int fd;  
                __uint32_t u32;  
                __uint64_t u64;  
            } epoll_data_t;  
  
            struct epoll_event {  
                __uint32_t events;      /* Epoll events */  
                epoll_data_t data;      /* User data variable */  
            };  
  
int epoll_wait(int epfd, struct epoll_event * events, int maxevents, int timeout); 
```
&emsp; 主要是epoll_create,epoll_ctl和epoll_wait三个函数。epoll_create函数创建epoll文件描述符，参数size并不是限制了epoll所能监听的描述符最大个数，只是对内核初始分配内部数据结构的一个建议。返回是epoll描述符。-1表示创建失败。epoll_ctl 控制对指定描述符fd执行op操作，event是与fd关联的监听事件。op操作有三种：添加EPOLL_CTL_ADD，删除EPOLL_CTL_DEL，修改EPOLL_CTL_MOD。分别添加、删除和修改对fd的监听事件。epoll_wait 等待epfd上的io事件，最多返回maxevents个事件。  
&emsp; 在 select/poll中，进程只有在调用一定的方法后，内核才对所有监视的文件描述符进行扫描，而epoll事先通过epoll_ctl()来注册一 个文件描述符，一旦基于某个文件描述符就绪时，内核会采用类似callback的回调机制，迅速激活这个文件描述符，当进程调用epoll_wait() 时便得到通知。  

----
&emsp; epoll操作过程需要三个接口，分别如下：  

```c
int epoll_create(int size)；//创建一个epoll的句柄，size用来告诉内核这个监听的数目一共有多大
int epoll_ctl(int epfd, int op, int fd, struct epoll_event *event)；
int epoll_wait(int epfd, struct epoll_event * events, int maxevents, int timeout);
```

1. int epoll_create(int size);  
&emsp; &emsp; 创建一个epoll的句柄，size用来告诉内核这个监听的数目一共有多大，这个参数不同于select()中的第一个参数，给出最大监听的fd+1的值，参数size并不是限制了epoll所能监听的描述符最大个数，只是对内核初始分配内部数据结构的一个建议。  
&emsp; &emsp; 当创建好epoll句柄后，它就会占用一个fd值，在linux下如果查看/proc/进程id/fd/，是能够看到这个fd的，所以在使用完epoll后，必须调用close()关闭，否则可能导致fd被耗尽。  
2. int epoll_ctl(int epfd, int op, int fd, struct epoll_event *event)；  
&emsp; 函数是对指定描述符fd执行op操作。  
    - epfd：是epoll_create()的返回值。  
    - op：表示op操作，用三个宏来表示：添加EPOLL_CTL_ADD，删除EPOLL_CTL_DEL，修改EPOLL_CTL_MOD。分别添加、删除和修改对fd的监听事件。  
    - fd：是需要监听的fd（文件描述符）  
    - epoll_event：是告诉内核需要监听什么事，struct epoll_event结构如下：  

```c
struct epoll_event {
  __uint32_t events;  /* Epoll events */
  epoll_data_t data;  /* User data variable */
};

//events可以是以下几个宏的集合：
EPOLLIN ：表示对应的文件描述符可以读（包括对端SOCKET正常关闭）；
EPOLLOUT：表示对应的文件描述符可以写；
EPOLLPRI：表示对应的文件描述符有紧急的数据可读（这里应该表示有带外数据到来）；
EPOLLERR：表示对应的文件描述符发生错误；
EPOLLHUP：表示对应的文件描述符被挂断；
EPOLLET： 将EPOLL设为边缘触发(Edge Triggered)模式，这是相对于水平触发(Level Triggered)来说的。
EPOLLONESHOT：只监听一次事件，当监听完这次事件之后，如果还需要继续监听这个socket的话，需要再次把这个socket加入到EPOLL队列里
```
3. int epoll_wait(int epfd, struct epoll_event * events, int maxevents, int timeout);  
&emsp; 等待epfd上的io事件，最多返回maxevents个事件。  
&emsp; 参数events用来从内核得到事件的集合，maxevents告之内核这个events有多大，这个maxevents的值不能大于创建epoll_create()时的size，参数timeout是超时时间（毫秒，0会立即返回，-1将不确定，也有说法说是永久阻塞）。该函数返回需要处理的事件数目，如返回0表示已超时。  

----

&emsp; Linux中提供的epoll相关函数如下：  

```c
int epoll_create(int size);
int epoll_ctl(int epfd, int op, int fd, struct epoll_event *event);
int epoll_wait(int epfd, struct epoll_event * events, int maxevents, int timeout);
```

1. epoll_create 函数创建一个epoll句柄，参数size表明内核要监听的描述符数量。调用成功时返回一个epoll句柄描述符，失败时返回-1。  
2. epoll_ctl 函数注册要监听的事件类型。四个参数解释如下：  

    * epfd 表示epoll句柄
    * op 表示fd操作类型，有如下3种
        * EPOLL_CTL_ADD 注册新的fd到epfd中
        * EPOLL_CTL_MOD 修改已注册的fd的监听事件
        * EPOLL_CTL_DEL 从epfd中删除一个fd
    * fd 是要监听的描述符
    * event 表示要监听的事件

&emsp; epoll_event结构体定义如下：    

```c
struct epoll_event {
    __uint32_t events;  /* Epoll events */
    epoll_data_t data;  /* User data variable */
};

typedef union epoll_data {
    void *ptr;
    int fd;
    __uint32_t u32;
    __uint64_t u64;
} epoll_data_t;
```

3. epoll_wait 函数等待事件的就绪，成功时返回就绪的事件数目，调用失败时返回 -1，等待超时返回 0。

    * epfd 是epoll句柄
    * events 表示从内核得到的就绪事件集合
    * maxevents 告诉内核events的大小
    * timeout 表示等待的超时事件

epoll是Linux内核为处理大批量文件描述符而作了改进的poll，是Linux下多路复用IO接口select/poll的增强版本，它能显著提高程序在大量并发连接中只有少量活跃的情况下的系统CPU利用率。原因就是获取事件的时候，它无须遍历整个被侦听的描述符集，只要遍历那些被内核IO事件异步唤醒而加入Ready队列的描述符集合就行了。  

----

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-46.png)  

&emsp; 时间复杂度：O(1)  

### epoll工作模式
&emsp; **epoll的工作方式**  
&emsp; epoll有两种工作方式：1.水平触发（LT）2.边缘触发（ET）  

* LT模式：若就绪的事件一次没有处理完要做的事件，就会一直去处理。即就会将没有处理完的事件继续放回到就绪队列之中（即那个内核中的链表），一直进行处理。   
* ET模式：就绪的事件只能处理一次，若没有处理完会在下次的其它事件就绪时再进行处理。而若以后再也没有就绪的事件，那么剩余的那部分数据也会随之而丢失。   

&emsp; 由此可见：ET模式的效率比LT模式的效率要高很多。只是如果使用ET模式，就要保证每次进行数据处理时，要将其处理完，不能造成数据丢失，这样对编写代码的人要求就比较高。 
注意：ET模式只支持非阻塞的读写：为了保证数据的完整性。


&emsp; epoll对文件描述符的操作有两种模式：LT（level trigger）和ET（edge trigger）。LT模式是默认模式，LT模式与ET模式的区别如下：  

* LT模式：当epoll_wait检测到描述符事件发生并将此事件通知应用程序，应用程序可以不立即处理该事件。下次调用epoll_wait时，会再次响应应用程序并通知此事件。  
* ET模式：当epoll_wait检测到描述符事件发生并将此事件通知应用程序，应用程序必须立即处理该事件。如果不处理，下次调用epoll_wait时，不会再次响应应用程序并通知此事件。  

1. LT模式  
&emsp; LT(level triggered)是缺省的工作方式，并且同时支持block和no-block socket.在这种做法中，内核告诉你一个文件描述符是否就绪了，然后你可以对这个就绪的fd进行IO操作。如果你不作任何操作，内核还是会继续通知你的。  
2. ET模式  
&emsp; ET(edge-triggered)是高速工作方式，只支持no-block socket。在这种模式下，当描述符从未就绪变为就绪时，内核通过epoll告诉你。然后它会假设你知道文件描述符已经就绪，并且不会再为那个文件描述符发送更多的就绪通知，直到你做了某些操作导致那个文件描述符不再为就绪状态了(比如，你在发送，接收或者接收请求，或者发送接收的数据少于一定量时导致了一个EWOULDBLOCK 错误）。但是请注意，如果一直不对这个fd作IO操作(从而导致它再次变成未就绪)，内核不会发送更多的通知(only once)。 
&emsp; ET模式在很大程度上减少了epoll事件被重复触发的次数，因此效率要比LT模式高。epoll工作在ET模式的时候，必须使用非阻塞套接口，以避免由于一个文件句柄的阻塞读/阻塞写操作把处理多个文件描述符的任务饿死。  

----

epoll除了提供select/poll那种IO事件的水平触发（Level Triggered）外，还提供了边缘触发（Edge Triggered），这就使得用户空间程序有可能缓存IO状态，减少epoll_wait/epoll_pwait的调用，提高应用程序效率。  

* 水平触发（LT）：默认工作模式，即当epoll_wait检测到某描述符事件就绪并通知应用程序时，应用程序可以不立即处理该事件；下次调用epoll_wait时，会再次通知此事件  
* 边缘触发（ET）： 当epoll_wait检测到某描述符事件就绪并通知应用程序时，应用程序必须立即处理该事件。如果不处理，下次调用epoll_wait时，不会再次通知此事件。（直到你做了某些操作导致该描述符变成未就绪状态了，也就是说边缘触发只在状态由未就绪变为就绪时只通知一次）。  

LT和ET原本应该是用于脉冲信号的，可能用它来解释更加形象。Level和Edge指的就是触发点，Level为只要处于水平，那么就一直触发，而Edge则为上升沿和下降沿的时候触发。比如：0->1 就是Edge，1->1 就是Level。  
ET模式很大程度上减少了epoll事件的触发次数，因此效率比LT模式下高。  

## 1.5. 三者区别联系  

&emsp; select 有最大文件描述符的限制，只能监听到有几个文件描述符就绪了，得遍历所有文件描述符获取就绪的IO。  
&emsp; poll 没有最大文件描述符的限制，与select一样，只能监听到有几个文件描述符就绪了，得遍历所有文件描述符获取就绪的IO。  
&emsp; epoll 没有最大文件描述符的限制，它通过回调的机制，一旦某个文件描述符就绪了，迅速激活这个文件描述符，当进程下一次调用epoll_wait()的时候便得到通知。  
&emsp; 所以，在有大量空闲连接的时候，epoll的效率要高很多。  

&emsp; epoll的优点主要是一下几个方面  
1. 监视的描述符数量不受限制，它所支持的FD上限是最大可以打开文件的数目，这个数字一般远大于2048,举个例子,在1GB内存的机器上大约是10万左 右，具体数目可以cat /proc/sys/fs/file-max察看,一般来说这个数目和系统内存关系很大。select的最大缺点就是进程打开的fd是有数量限制的。这对 于连接数量比较大的服务器来说根本不能满足。虽然也可以选择多进程的解决方案( Apache就是这样实现的)，不过虽然linux上面创建进程的代价比较小，但仍旧是不可忽视的，加上进程间数据同步远比不上线程间同步的高效，所以也 不是一种完美的方案。  
2. IO的效率不会随着监视fd的数量的增长而下降。epoll不同于select和poll轮询的方式，而是通过每个fd定义的回调函数来实现的。只有就绪的fd才会执行回调函数。
3.支持电平触发和边沿触发（只告诉进程哪些文件描述符刚刚变为就绪状态，它只说一遍，如果我们没有采取行动，那么它将不会再次告知，这种方式称为边缘触发）两种方式，理论上边缘触发的性能要更高一些，但是代码实现相当复杂。  
4.mmap加速内核与用户空间的信息传递。epoll是通过内核于用户空间mmap同一块内存，避免了无畏的内存拷贝。  


<!-- 

 epoll总结

在 select/poll中，进程只有在调用一定的方法后，内核才对所有监视的文件描述符进行扫描，而epoll事先通过epoll_ctl()来注册一 个文件描述符，一旦基于某个文件描述符就绪时，内核会采用类似callback的回调机制，迅速激活这个文件描述符，当进程调用epoll_wait() 时便得到通知。(此处去掉了遍历文件描述符，而是通过监听回调的的机制。这正是epoll的魅力所在。)

epoll的优点主要是一下几个方面：
1. 监视的描述符数量不受限制，它所支持的FD上限是最大可以打开文件的数目，这个数字一般远大于2048,举个例子,在1GB内存的机器上大约是10万左 右，具体数目可以cat /proc/sys/fs/file-max察看,一般来说这个数目和系统内存关系很大。select的最大缺点就是进程打开的fd是有数量限制的。这对 于连接数量比较大的服务器来说根本不能满足。虽然也可以选择多进程的解决方案( Apache就是这样实现的)，不过虽然linux上面创建进程的代价比较小，但仍旧是不可忽视的，加上进程间数据同步远比不上线程间同步的高效，所以也不是一种完美的方案。

    IO的效率不会随着监视fd的数量的增长而下降。epoll不同于select和poll轮询的方式，而是通过每个fd定义的回调函数来实现的。只有就绪的fd才会执行回调函数。

    如果没有大量的idle -connection或者dead-connection，epoll的效率并不会比select/poll高很多，但是当遇到大量的idle- connection，就会发现epoll的效率大大高于select/poll。

-->
一张图总结一下select,poll,epoll的区别：  

|	|select| 	poll| 	epoll|
|---|---|---|---|
|操作方式 	|遍历 	|遍历 	|回调|
|底层实现 	|数组 |	链表 	|红黑树|
|IO效率| 	每次调用都进行线性遍历，时间复杂度为O(n) |	每次调用都进行线性遍历，时间复杂度为O(n) |	事件通知方式，每当fd就绪，系统注册的回调函数就会被调用，将就绪fd放到readyList里面，时间复杂度O(1)|
|最大连接数 |	1024（x86）或2048（x64）| 	无上限 |	无上限|
|fd拷贝 	|每次调用select，都需要把fd集合从用户态拷贝到内核态 |	每次调用poll，都需要把fd集合从用户态拷贝到内核态 |	调用epoll_ctl时拷贝进内核并保存，之后每次epoll_wait不拷贝|

epoll是Linux目前大规模网络并发程序开发的首选模型。在绝大多数情况下性能远超select和poll。目前流行的高性能web服务器Nginx正式依赖于epoll提供的高效网络套接字轮询服务。但是，在并发连接不高的情况下，多线程+阻塞I/O方式可能性能更好。

既然select，poll，epoll都是I/O多路复用的具体的实现，之所以现在同时存在，其实他们也是不同历史时期的产物

* select出现是1984年在BSD里面实现的
* 14年之后也就是1997年才实现了poll，其实拖那么久也不是效率问题， 而是那个时代的硬件实在太弱，一台服务器处理1千多个链接简直就是神一样的存在了，select很长段时间已经满足需求
* 2002, 大神 Davide Libenzi 实现了epoll

