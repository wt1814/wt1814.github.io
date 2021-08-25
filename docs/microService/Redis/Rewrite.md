

<!-- TOC -->

- [1. ~~AOF重写阻塞~~](#1-aof重写阻塞)
    - [1.1. 问题背景](#11-问题背景)
    - [1.2. 什么是AOF重写](#12-什么是aof重写)
    - [1.3. 阻塞原因](#13-阻塞原因)
    - [1.4. 解决方案](#14-解决方案)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. 当Redis执行完一个写命令之后，它会同时将这个写命令发送给AOF缓冲区和AOF重写缓冲区。  
2. AOF重写阻塞原因：
	1. **<font color = "clime">当子进程完成AOF重写工作之后，它会向父进程发送一个信号，父进程在接收到该信号之后，`会调用一个信号处理函数，并执行相应工作：将AOF重写缓冲区中的所有内容写入到新的AOF文件中。`</font>**  
	2. **<font color = "clime">在整个AOF后台重写过程中，只有信号处理函数执行时会对Redis主进程造成阻塞，在其他时候，AOF后台重写都不会阻塞主进程。</font>**  
&emsp; 如果信号处理函数执行时间较长，即造成AOF阻塞时间长，就会对性能有影响。  
2. 解决方案：  
	* **<font color = "red">将no-appendfsync-on-rewrite设置为yes。</font>** 
	* master节点关闭AOF。  
    
&emsp; 折中方式：在master节点设置将no-appendfsync-on-rewrite设置为yes（表示在日志重写时，不进行命令追加操作，而只是将命令放在重写缓冲区里，避免与命令的追加造成磁盘IO上的冲突），同时auto-aof-rewrite-percentage参数设置为0，关闭主动重写。  
3. 虽然在everysec配置下aof的fsync是由子线程进行操作的，但是主线程会监控fsync的执行进度。  
&emsp; **<font color = "clime">主线程在执行时候如果发现上一次的fsync操作还没有返回，那么主线程就会阻塞。</font>**  


# 1. ~~AOF重写阻塞~~
<!-- 
Redis AOF重写阻塞问题分析
https://blog.csdn.net/github_32521685/article/details/106354737
https://www.yht7.com/news/89862

https://www.cnblogs.com/Brake/p/14352772.html

https://blog.csdn.net/github_32521685/article/details/106354737

*** https://article.itxueyuan.com/e19qlM
-->

## 1.1. 问题背景
&emsp; 某个业务线使用Redis集群保存用户session数据，数据量大约在4千万-5千万，每天发生3-4次AOF重写，每次时间持续30-40秒，AOF重写期间出现Redis主进程阻塞，应用端响应超时的问题。  

&emsp; 环境：Redis 2.8，一主一从。  

## 1.2. 什么是AOF重写
&emsp; AOF重写是AOF持久化的一个机制，用来压缩AOF文件，通过fork一个子进程，重新写一个新的AOF文件，该次重写不是读取旧的AOF文件进行复制，而是读取内存中的Redis数据库，重写一份AOF文件，有点类似于RDB的快照方式。  
&emsp; **<font color = "red">在子进程进行AOF重启期间，Redis主进程执行的命令会被保存在AOF重写缓冲区里面，</font>** 这个缓冲区在服务器创建子进程之后开始使用， **<font color = "clime">当Redis执行完一个写命令之后，它会同时将这个写命令发送给 AOF缓冲区和AOF重写缓冲区。</font>** 如下图：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-113.png)  


## 1.3. 阻塞原因
&emsp; **<font color = "clime">当子进程完成AOF重写工作之后，它会向父进程发送一个信号，</font><font color = "blue">父进程在接收到该信号之后，会调用一个信号处理函数，并执行以下工作：</font>**  

* 将AOF重写缓冲区中的所有内容写入到新的AOF文件中，保证新 AOF文件保存的数据库状态和服务器当前状态一致。  
* 对新的AOF文件进行改名，原子地覆盖现有AOF文件，完成新旧文件的替换。  
* 继续处理客户端请求命令。  

&emsp; **<font color = "blue">在整个AOF后台重写过程中，只有信号处理函数执行时会对Redis主进程造成阻塞，在其他时候，AOF后台重写都不会阻塞主进程，</font>** 如下图所示：
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-114.png)  

## 1.4. 解决方案
&emsp; 这是当时的Redis配置：

```text
127.0.0.1:6379> config get *append*
1) "no-appendfsync-on-rewrite"
2) "no"
3) "appendonly"
4) "yes"
5) "appendfsync"
6) "everysec"
```
&emsp; 从配置看，原因理论上就很清楚了：这个Redis实例使用AOF进行持久化(appendonly)，appendfsync策略采用的是everysec刷盘。但是AOF随着时间推移，文件会越来越大，因此，Redis还有一个rewrite策略，实现AOF文件的减肥，但是结果是幂等的。no-appendfsync-on-rewrite的策略是 no，这就会导致在进行rewrite操作时，appendfsync会被阻塞。如果当前AOF文件很大，那么相应的rewrite时间会变长，appendfsync被阻塞的时间也会更长。  

&emsp; 这不是什么新问题，很多开启AOF的业务场景都会遇到这个问题。解决的办法有这么几个：

* **<font color = "red">将no-appendfsync-on-rewrite设置为yes。</font>** 这样可以避免与appendfsync争用文件句柄，但是在rewrite期间的AOF有丢失的风险。
* 给当前Redis实例添加slave节点，当前节点设置为master，然后master节点关闭AOF，slave节点开启AOF。这样的方式的风险是如果master挂掉，尚没有同步到slave的数据会丢失。  

&emsp; 可以采取比较折中的方式：  

* 在master节点设置将no-appendfsync-on-rewrite设置为yes（表示在日志重写时，不进行命令追加操作，而只是将命令放在重写缓冲区里，避免与命令的追加造成磁盘IO上的冲突），同时auto-aof-rewrite-percentage参数设置为0关闭主动重写。  
* 在重写时为了避免硬盘空间不足或者IO使用率高影响重写功能，还添加了硬盘空间报警和IO使用率报警保障重写的正常进行

&emsp; 那么如果rewrite的时候对新的写操作不进行fsync，那么新的AOF文件里面是否会丢失这个写操作呢？

&emsp; 答案是不会的，Redis会将新的写操作放在重写缓存区中，等待rewrite操作完成的时候，将新操作直接追加到新的AOF中。
