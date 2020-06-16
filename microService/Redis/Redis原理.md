---
title: Redis原理
date: 2020-05-16 00:00:00
tags:
    - Redis
---

<!-- TOC -->

- [1. Redis持久化](#1-redis持久化)
    - [1.1. RDB（Redis DataBase），快照](#11-rdbredis-database快照)
        - [1.1.1. RDB的触发](#111-rdb的触发)
        - [1.1.2. RDB的流程](#112-rdb的流程)
        - [1.1.3. RDB的优势和劣势](#113-rdb的优势和劣势)
    - [1.2. AOF（Append-only file）](#12-aofappend-only-file)
        - [1.2.1. 开启AOF](#121-开启aof)
        - [1.2.2. AOF持久化流程](#122-aof持久化流程)
            - [1.2.2.1. 重启加载（数据恢复流程）](#1221-重启加载数据恢复流程)
        - [1.2.3. AOF文件损坏](#123-aof文件损坏)
        - [1.2.4. AOF的优势和劣势](#124-aof的优势和劣势)
    - [1.3. 混合持久化](#13-混合持久化)
    - [1.4. Redis集群持久化策略](#14-redis集群持久化策略)
- [2. Redis过期键删除策略](#2-redis过期键删除策略)
    - [2.1. Key生存期：](#21-key生存期)
    - [2.2. 常见的删除策略](#22-常见的删除策略)
        - [2.2.1. 定时删除策略（主动淘汰）](#221-定时删除策略主动淘汰)
        - [2.2.2. 惰性删除策略（被动淘汰）](#222-惰性删除策略被动淘汰)
        - [2.2.3. 定期删除策略（主动淘汰）](#223-定期删除策略主动淘汰)
    - [2.3. Redis使用的过期键删除策略](#23-redis使用的过期键删除策略)
- [3. Redis内存](#3-redis内存)
    - [3.1. 内存设置](#31-内存设置)
    - [3.2. 内存淘汰策略](#32-内存淘汰策略)
        - [3.2.1. redis内存淘汰使用的算法](#321-redis内存淘汰使用的算法)
            - [3.2.1.1. LRU算法](#3211-lru算法)
                - [3.2.1.1.1. Redis中的LRU算法](#32111-redis中的lru算法)
                - [3.2.1.1.2. 手写LRU算法](#32112-手写lru算法)
            - [3.2.1.2. LFU算法：](#3212-lfu算法)
        - [3.2.2. 内存淘汰策略](#322-内存淘汰策略)
- [4. Redis事务](#4-redis事务)
    - [4.1. Redis事务的使用](#41-redis事务的使用)
    - [4.2. Redis事务中的错误](#42-redis事务中的错误)
    - [4.3. Redis的乐观锁Watch](#43-redis的乐观锁watch)
- [5. Redis的事件](#5-redis的事件)
    - [5.1. 文件事件](#51-文件事件)
    - [5.2. 时间事件](#52-时间事件)
    - [5.3. 事件的调度与执行](#53-事件的调度与执行)
- [6. Redis为什么这么快？](#6-redis为什么这么快)

<!-- /TOC -->

![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-55.png)  

# 1. Redis持久化  
&emsp; Redis是一种内存数据库。一旦进程退出，Redis的数据就会丢失。Redis持久化拥有以下三种方式：  
1. 快照方式（RDB, Redis DataBase）将某一个时刻的内存数据，以二进制的方式写入磁盘，RDB方式是redis默认的持久化方式；  
2. 文件追加方式（AOF, Append Only File），记录所有的操作命令，并以文本的形式追加到文件中；  
3. 混合持久化方式，Redis 4.0之后新增的方式，混合持久化是结合了RDB和 AOF的优点，在写入的时候，先把当前的数据以RDB的形式写入文件的开头，再将后续的操作命令以AOF的格式存入文件，这样既能保证Redis重启时的速度，又能简单数据丢失的风险。  

## 1.1. RDB（Redis DataBase），快照
&emsp; RDB持久化是Redis默认的持久化方式。  
&emsp; RDB是一种快照存储持久化方式，将Redis某一时刻的所有内存数据保存到硬盘的文件当中，默认保存的文件名为dump.rdb，dump.rdb文件默认生成在%REDIS_HOME%etc目录下（如/usr/local/redis/etc/），可以修改redis.conf文件中的dir指定dump.rdb的保存路径。也可以将快照复制到其他服务器从而创建具有相同数据的服务器副本。  
&emsp; 在Redis服务器启动时，会重新加载dump.rdb文件的数据到内存当中恢复数据，即通过该文件可以还原生成RDB文件时的数据库状态（数据库状态是指 Redis服务器的非空数据库以及键值对的统称）。  

### 1.1.1. RDB的触发  
&emsp; RDB触发机制分为使用指令手动触发和自动触发。  

* 自动触发RDB持久化：
    1. 修改redis.conf文件，默认配置如下所示：  

            save 900 1 # 表示900 秒内如果至少有 1 个 key 的值变化，则触发RDB
            save 300 10 # 表示300 秒内如果至少有 10 个 key 的值变化，则触发RDB
            save 60 10000 # 表示60 秒内如果至少有 10000 个 key 的值变化，则触发RDB
        
    &emsp; 如果不需要Redis进行持久化，可以注释掉所有的save行来停用保存功能，也可以直接一个空字符串来停用持久化：save ""。  
    &emsp; Redis服务器周期操作函数serverCron默认每个100毫秒就会执行一次，该函数用于正在运行的服务器进行维护，它的一项工作就是检查save选项所设置的条件是否有一项被满足，如果满足的话，就执行bgsave指令。 
    2. shutdown 触发，保证服务器正常关闭。 

* 手动触发RDB持久化：客户端通过向Redis服务器发送Save或Bgsave命令让服务器生成RDB文件。  
    1. save命令：是一个同步操作。  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-31.png)  
    &emsp; 当客户端向服务器发送Save命令请求进行持久化时，服务器会阻塞Save命令之后的其他客户端的请求，直到数据同步完成。  
    &emsp; 如果数据量太大，同步数据会执行很久，而这期间Redis服务器也无法接收其他请求，所以，最好不要在生产环境使用Save命令。  
        
    2. Bgsave命令：  
    &emsp; 与Save命令不同，Bgsave命令是一个异步操作。  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-32.png)  
    &emsp; bgsave，执行该命令时，Redis会在后台异步执行快照操作，此时Redis仍然可以相应客户端请求。具体操作是当客户端发服务发出Bgsave命令时，Redis服务器主进程会Forks操作创建一个子进程来数据同步问题，在将数据保存到RDB 文件之后，子进程会退出。新RDB文件就会原子地替换旧的RDB文件。所以，与Save命令相比，Redis服务器在处理Bgsave采用子线程进行IO写入。而主进程仍然可以接收其他请求，但Forks子进程是同步的，所以Forks子进程时，一样不能接收其他请求。这意味着，如果Forks一个子进程花费的时间太久（一般是很快的），而且占用内存会加倍，Bgsave命令仍然有阻塞其他客户的请求的情况发生。  

    <font color = "red">&emsp; 自动间隔性保存</font>   
    &emsp; 对于RDB持久化而言，一般都会使用BGSAVE来持久化，因为不会阻塞服务器进程。  
    &emsp; 在Redis的配置文件，有提供设置服务器每隔多久时间来执行BGSAVE命令。  
    &emsp; Redis默认是如下配置：  

        save 900 1      // 900 秒内，对数据库至少修改 1 次。下面同理    
        save 300 10     
        save 60 10000
    
&emsp; 只要满足其中一种情况，服务器就会执行BGSAVE命令。  

### 1.1.2. RDB的流程  
&emsp; bgsave是主流的触发RDB持久化方式。它的运行流程如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-53.png)  
1. 执行bgsave命令，Redis父进程判断当前是否存在正在执行的子进 程，如RDB/AOF子进程，如果存在bgsave命令直接返回。  
2. 父进程执行fork操作创建子进程，fork操作过程中父进程会阻塞，通 过info stats命令查看latest_fork_usec选项，可以获取最近一个fork操作的耗 时，单位为微秒。  
3. 父进程fork完成后，bgsave命令返回“Background saving started”信息 并不再阻塞父进程，可以继续响应其他命令。  
4. 子进程创建RDB文件，根据父进程内存生成临时快照文件，完成后 对原有文件进行原子替换。执行lastsave命令可以获取最后一次生成RDB的 时间，对应info统计的rdb_last_save_time选项。  
5. 进程发送信号给父进程表示完成，父进程更新统计信息，具体见 info Persistence下的rdb_*相关选项。  

### 1.1.3. RDB的优势和劣势  
* 优势 
1. RDB 是一个非常紧凑(compact)的文件，它保存了 redis 在某个时间点上的数据 集。这种文件非常适合用于进行备份和灾难恢复。  
2. 生成 RDB 文件的时候，redis 主进程会 fork()一个子进程来处理所有保存工作，主 进程不需要进行任何磁盘 IO 操作。  
3. RDB 在恢复大数据集时的速度比 AOF 的恢复速度要快。 

* 劣势  
1. RDB 方式数据没办法做到实时持久化/秒级持久化。因为 bgsave 每次运行都要 执行 fork 操作创建子进程，频繁执行成本过高。 
2. 在一定间隔时间做一次备份，所以如果 redis 意外 down 掉的话，就会丢失最后 一次快照之后的所有修改（数据有丢失）。 如果数据相对来说比较重要，希望将损失降到最小，则可以使用 AOF 方式进行持久化。  


## 1.2. AOF（Append-only file）  
&emsp; AOF持久化机制是以日志的形式记录Redis中的每一次的增删改操作，不会记录查询操作，以文本的形式记录，打开记录的日志文件就可以查看操作记录。  
&emsp; AOF是默认不开启的。  

&emsp; AOF总体流程和RDB持久化一样。都是创建一个xxx文件、在服务器下次启动时就载入这个文件来还原数据。  
&emsp; AOF持久化方式会记录客户端对服务器的每一次写操作命令，并将这些写操作以Redis协议追加保存到后缀为AOF文件末尾。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-33.png)  

### 1.2.1. 开启AOF
&emsp; Redis默认不开启AOF持久化方式，可以在配置文件中开启并进行更加详细的配置，如下面的redis.conf文件：  

    # 开启aof机制
    appendonly yes
    # aof文件名
    appendfilename "appendonly.aof"
    # 写入策略,always表示每个写操作都保存到aof文件中,也可以是everysec或no
    appendfsync always
    # 默认不重写aof文件
    no-appendfsync-on-rewrite no
    # 保存目录
    dir ~/redis/

&emsp; 在上面的配置文件中，<font color = "red">通过 appendfsync选项指定写入策略</font>，有三个选项：  

    appendfsync always
    # appendfsync everysec
    # appendfsync no

* always：客户端的每一个写操作都保存到 AOF 文件当中，这种策略很安全，但是每个写操作都有 IO 操作，所以也很慢。  
* everysec：appendfsync 的默认写入策略，每秒写入一次AOF文件，因此，最多可能会丢失1s的数据。  
* no：Redis 服务器不负责写入 AOF，而是交由操作系统来处理什么时候写入 AOF文件。更快，但也是最不安全的选择，不推荐使用。 

### 1.2.2. AOF持久化流程  
&emsp; AOF的工作流程操作：命令写入 （append）、文件同步（sync）、文件重写（rewrite）、重启加载 （load）。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-52.png)  
&emsp; 流程如下：  
1. 所有的写入命令会追加到aof_buf（缓冲区）中。   
2. AOF缓冲区根据对应的策略向硬盘做同步操作。   
3. 随着AOF文件越来越大，需要定期对AOF文件进行重写，达到压缩 的目的。  
4. 当Redis服务器重启时，可以加载AOF文件进行数据恢复。  

<!-- 
&emsp; AOF持久化功能的实现可以分为3个步骤：命令追加、文件写入、文件同步。  

* 命令追加：将写命令追加到AOF缓冲区的末尾。  
* 文件写入：缓冲区内容写到AOF文件。  
* 文件同步：将AOF文件保存到磁盘。  

&emsp; 文件写入、文件同步需要根据一定的条件来执行，而这些条件由Redis配置文件中的appendfsync选项来决定。  
-->

#### 1.2.2.1. 重启加载（数据恢复流程）  
&emsp; AOF和RDB文件都可以用于服务器重启时的数据恢复。如下图所示， 表示Redis持久化文件加载流程。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-54.png)  
&emsp; 流程说明： 
1. AOF持久化开启且存在AOF文件时，优先加载AOF文件。  
2. AOF关闭或者AOF文件不存在时，加载RDB文件。  
3. 加载AOF/RDB文件成功后，Redis启动成功。 
4. AOF/RDB文件存在错误时，Redis启动失败并打印错误信息。  

### 1.2.3. AOF文件损坏  
&emsp; 在写入AOF日志文件时，如果Redis服务器宕机，则AOF日志文件文件会出格式错误。  
&emsp; 在重启Redis服务器时，Redis服务器会拒绝载入这个AOF文件，可以通过以下步骤修复AOF 并恢复数据：  

* 备份现在 AOF 文件，以防万一。
* 使用 redis-check-aof 命令修复 AOF 文件，该命令格式如下：  

        # 修复aof日志文件
        $ redis-check-aof -fix file.aof
* 重启 Redis 服务器，加载已经修复的AOF文件，恢复数据。  

### 1.2.4. AOF的优势和劣势  
* 优势  
1. AOF 持久化的方法提供了多种的同步频率，即使使用默认的同步频率每秒同步 一次，Redis 最多也就丢失 1 秒的数据而已。  

* 缺点： 
1. 对于具有相同数据的的 Redis，AOF 文件通常会比 RDF 文件体积更大（RDB 存的是数据快照）。  
2. 虽然 AOF 提供了多种同步的频率，默认情况下，每秒同步一次的频率也具有较 高的性能。在高并发的情况下，RDB 比 AOF 具好更好的性能保证。  

<!-- 
 1.3. RDB方式与AOF方式的优势对比  
 1.3.1. RDB优缺点：  
&emsp; RDB的几个优点：
* 与AOF方式相比，通过RDB文件恢复数据比较快。  
* RDB文件非常紧凑，适合于数据备份。  
* 通过RDB进行数据备份，由于使用子进程生成，所以对Redis服务器性能影响较小。  

&emsp; RDB的几个缺点：
* 如果服务器宕机的话，采用RDB的方式会造成某个时段内数据的丢失，比如设置10分钟同步一次或5分钟达到1000次写入就同步一次，那么如果还没达到触发条件服务器就死机了，那么这个时间段的数据会丢失。  
* 使用Save命令会造成服务器阻塞，直接数据同步完成才能接收后续请求。  
* 使用Bgsave命令在Forks子进程时，如果数据量太大，Forks 的过程也会发生阻塞，另外，Forks 子进程会耗费内存。  

 1.3.2. AOF优缺点：  
&emsp; AOF的优点：  
* AOF只是追加日志文件，因此对服务器性能影响较小，速度比RDB要快，消耗的内存较少。   

&emsp; AOF的缺点：
* AOF方式生成的日志文件太大，即使通过AOF重写，文件体积仍然很大。  
* 恢复数据的速度比RDB慢。  
-->
## 1.3. 混合持久化  
&emsp; 在redis4.0后混合持久化（RDB+AOF）对重写的优化，4.0版本的混合持久化默认是关闭的，可以通过以下的配置开启混合持久化：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-34.png)  
&emsp; 混合持久化也是通过bgre write aof来完成的，不同的是当开启混合持久化时，fork出的子进程先将共享内存的数据以RDB方式写入aof文件中，然后再将重写缓冲区的增量命令以AOF方式写入文件中。  
&emsp; 写入完成后通知主进程统计信息，并将新的含有RDB格式和AOF格式的AOF文件替换旧的AOF文件。简单的说：新的AOF文件前半段是以RDB格式的全量数据后半段是AOF格式的增量数据。  

&emsp; 优点：混合持久化结合RDB持久化和AOF持久化的优点，由于绝大部分的格式是RDB格式，加载速度快，增量数据以AOF方式保存，数据更少的丢失。  

## 1.4. Redis集群持久化策略  
&emsp; 官方建议：如果要想提供很高的数据保障性，那么建议同时使用两种持久化方式。如果可以接受灾难带来的几分钟的数据丢失，那么可以仅使用RDB。  

&emsp; 一般在生产上采用的持久化策略为：1).master关闭持久化；2).slave开RDB即可，必要的时候AOF和RDB都开启。  
&emsp; 其实，如果想要数据足够安全，可以两种方式都开启，但两种持久化方式同时进行IO操作，会严重影响服务器性能，因此有时候不得不做出选择。  

&emsp; 该策略能够适应绝大部分场景，绝大部分集群架构。  
1. 为什么是绝大部分场景？  
&emsp; 因为这套策略存在部分的数据丢失可能性。redis的主从复制是异步的，master执行完客户端请求的命令后会立即返回结果给客户端，然后异步的方式把命令同步给slave。因此master可能还未来得及将命令传输给slave，就宕机了，此时slave变为master，数据就丢了。  
&emsp; 幸运的是，绝大部分业务场景，都能容忍数据的部分丢失。假设，真的遇到缓存雪崩的情况，代码中也有熔断器来进行资源保护，不至于所有的请求都转发到数据库上，导致服务崩溃！  
&emsp; 注：这里的缓存雪崩是指同一时间来了一堆请求，请求的key在redis中不存在，导致请求全部转发到数据库上。  
2. 为什么是绝大部分集群架构？  
&emsp; 因为在集群中存在redis读写分离的情况，就不适合这套方案了。  
&emsp; 幸运的是，由于采用redis读写分离架构，就必须要考虑主从同步的延迟性问题，徒增系统复杂度。目前业内采用redis读写分离架构的项目，比较少。  

----
# 2. Redis过期键删除策略
## 2.1. Key生存期：  
&emsp; 在Redis当中，有生存期的key被称为volatile。在创建缓存时，要为给定的key设置生存期，当key过期的时候（生存期为0），它可能会被删除。  
1. 影响生存时间的一些操作：  
&emsp; 生存时间可以通过使用DEL命令来删除整个key来移除，或者被SET和GETSET命令覆盖原来的数据。也就是说，修改key对应value和使用另外相同key和value来覆盖以后，当前数据的生存时间不同。  
&emsp; 比如说，对一个key执行INCR命令，对一个列表进行LPUSH命令，或者对一个哈希表执行HSET命令，这类操作都不会修改key 本身的生存时间。另一方面，如果使用RENAME对一个key进行改名，那么改名后的key的生存时间和改名前一样。  
&emsp; RENAME命令的另一种可能是，尝试将一个带生存时间的key改名成另一个带生存时间的another_key，这时旧的another_key(以及它的生存时间)会被删除，然后旧的key会改名为another_key，因此，新的another_key的生存时间也和原本的key一样。使用PERSIST命令可以在不删除key的情况下，移除key的生存时间，让key重新成为一个persistent key。  

2. 如何更新生存时间：  
&emsp; 可以对一个已经带有生存时间的key执行EXPIRE命令，新指定的生存时间会取代旧的生存时间。过期时间的精度已经被控制在1ms之内，主键失效的时间复杂度是O（1），EXPIRE和TTL命令搭配使用，TTL可以查看key的当前生存时间。设置成功返回1；当 key 不存在或者不能为key设置生存时间时，返回0。  

## 2.2. 常见的删除策略  
&emsp; 常见的删除策略有3种：定时删除、惰性删除、定期删除。  

### 2.2.1. 定时删除策略（主动淘汰）  
&emsp; 在设置键的过期时间的同时，创建一个定时器，让定时器在键的过期时间来临时，立即执行对键的删除操作。  

&emsp; 优点：对内存非常友好。  
&emsp; 缺点：对CPU时间非常不友好，会占用大量的 CPU 资源去处理过期的 数据，从而影响缓存的响应时间和吞吐量。  

&emsp; 举个例子，如果有大量的命令请求等待服务器处理，并且服务器当前不缺少内存，如果服务器将大量的CPU时间用来删除过期键，那么服务器的响应时间和吞吐量就会受到影响。  
&emsp; 也就是说，如果服务器创建大量的定时器，服务器处理命令请求的性能就会降低，因此Redis目前并没有使用定时删除策略。  

### 2.2.2. 惰性删除策略（被动淘汰）  
&emsp; 只有当访问一个 key 时，才会判断该 key 是否已过期，过期则清除。  
&emsp; 因此，惰性删除策略的优缺点如下所示：  

&emsp; 优点：对CPU时间非常友好，可以最 大化地节省 CPU 资源。  
&emsp; 缺点：对内存非常不友好，极端情况可能出现大量的过期 key 没有再 次被访问，从而不会被清除，占用大量内存。  

&emsp; 举个例子，如果数据库有很多的过期键，而这些过期键又恰好一直没有被访问到，那这些过期键就会一直占用着宝贵的内存资源，造成资源浪费。  

### 2.2.3. 定期删除策略（主动淘汰）  
&emsp; 定期删除策略是定时删除策略和惰性删除策略的一种整合折中方案。  
&emsp; 定期删除策略每隔一段时间执行一次删除过期键操作，并通过限制删除操作执行的时长和频率来减少删除操作对CPU时间的影响，同时，通过定期删除过期键，也有效地减少了因为过期键而带来的内存浪费。  

<!-- 
每隔一定的时间，会扫描一定数量的数据库的 expires 字典中一定数量的 key，并清 除其中已过期的 key。该策略是前两者的一个折中方案。通过调整定时扫描的时间间隔和 每次扫描的限定耗时，可以在不同情况下使得 CPU 和内存资源达到最优的平衡效果。
-->

## 2.3. Redis使用的过期键删除策略  
&emsp; <font color = "red">Redis服务器使用的是惰性删除策略和定期删除策略。</font>  

# 3. Redis内存
&emsp; <font color = "red">如果大量非过期key堆积在内存里，导致redis内存块耗尽了。redis会采用内存淘汰机制。</font>  
&emsp; Redis 的内存淘汰策略，是指当内存使用达到最大内存极限时，需要使用淘汰算法来决定清理掉哪些数据，以保证新数据的存入。  

## 3.1. 内存设置  
&emsp; 默认情况下，在32位OS中，Redis最大使用3GB的内存，在64位OS中则没有限制。  
&emsp; 在使用Redis时，应该对数据占用的最大空间有一个基本准确的预估，并为Redis设定最大使用的内存。否则在64位OS中Redis会无限制地占用内存（当物理内存被占满后会使用swap空间），容易引发各种各样的问题。  

&emsp; 在redis中，允许用户设置最大使用内存大小server.maxmemory。默认为0，没有指定最大缓存，如果有新的数据添加，超过最大内存，则会使redis崩溃，所以一定要设置。  

&emsp; Redis是基于内存的key-value数据库，因为系统的内存大小有限，所以在使用Redis的时候可以配置Redis能使用的最大的内存大小。  

1. 通过配置文件配置  
&emsp; 通过在Redis安装目录下面的redis.conf配置文件中添加以下配置设置内存大小  

        //设置Redis最大占用内存大小为100M
        maxmemory 100mb
    &emsp; redis的配置文件不一定使用的是安装目录下面的redis.conf文件，启动redis服务的时候是可以传一个参数指定redis的配置文件的  

2. 通过命令修改  
&emsp; Redis支持运行时通过命令动态修改内存大小  

        //设置Redis最大占用内存大小为100M  
        127.0.0.1:6379> config set maxmemory 100mb  
        //获取设置的Redis能使用的最大内存大小  
        127.0.0.1:6379> config get maxmemory

    &emsp; 如果不设置最大内存大小或者设置最大内存大小为0，在64位操作系统下不限制内存大小，在32位操作系统下最多使用3GB内存。  

## 3.2. 内存淘汰策略  
&emsp; https://redis.io/topics/lru-cache  

### 3.2.1. redis内存淘汰使用的算法  
&emsp; redis内存淘汰使用的算法有：  

* LRU，Least Recently Used：最近最少使用。判断最近被使用的时间，目前最远的 数据优先被淘汰。  
* LFU，Least Frequently Used，最不常用，4.0 版本新增。  
* random，随机删除。  

#### 3.2.1.1. LRU算法  
##### 3.2.1.1.1. Redis中的LRU算法  
&emsp; 如果基于传统 LRU 算法实现 Redis LRU 会有什么问题？  
&emsp; 需要额外的数据结构存储，消耗内存。  
&emsp; Redis LRU 对传统的 LRU 算法进行了改良，通过随机采样来调整算法的精度。 如果淘汰策略是 LRU，则根据配置的采样值 maxmemory_samples（默认是 5 个）, 随机从数据库中选择 m 个 key, 淘汰其中热度最低的 key 对应的缓存数据。所以采样参数m配置的数值越大, 就越能精确的查找到待淘汰的缓存数据,但是也消耗更多的CPU计 算,执行效率降低。  

&emsp; 如何找出热度最低的数据？  
&emsp; Redis 中所有对象结构都有一个 lru 字段, 且使用了 unsigned 的低 24 位，这个字段 用来记录对象的热度。对象被创建时会记录 lru 值。在被访问的时候也会更新 lru 的值。 但是不是获取系统当前的时间戳，而是设置为全局变量 server.lruclock 的值。  

##### 3.2.1.1.2. 手写LRU算法  
&emsp;基于LinkedHashMap实现一个简单版本的LRU算法。  

```java
class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int CACHE_SIZE;
    /**
     * @param cacheSize 缓存大小
     */
    // true表示让linkedHashMap按照访问顺序来进行排序，最近访问的放在头部，最老访问的放在尾部。
    public LRUCache(int cacheSize) {
        super((int) Math.ceil(cacheSize / 0.75) + 1, 0.75f, true);
        CACHE_SIZE = cacheSize;
    }

    @Override
    // 当map中的数据量大于指定的缓存个数的时候，就自动删除最老的数据。
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > CACHE_SIZE;
    }
}
```

```java
public class LRUCache<k, v> {
    //容量
    private int capacity;
    //当前有多少节点的统计
    private int count;
    //缓存节点
    private Map<k, node> nodeMap;
    private Node head;
    private Node tail;

    public LRUCache(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException(String.valueOf(capacity));
        }
        this.capacity = capacity;
        this.nodeMap = new HashMap<>();
        //初始化头节点和尾节点，利用哨兵模式减少判断头结点和尾节点为空的代码
        Node headNode = new Node(null, null);
        Node tailNode = new Node(null, null);
        headNode.next = tailNode;
        tailNode.pre = headNode;
        this.head = headNode;
        this.tail = tailNode;
    }

    public void put(k key, v value) {
        Node node = nodeMap.get(key);
        if (node == null) {
            if (count >= capacity) {
                //先移除一个节点
                removeNode();
            }
            node = new Node<>(key, value);
            //添加节点
            addNode(node);
        } else {
            //移动节点到头节点
            moveNodeToHead(node);
        }
    }

    public Node get(k key) {
        Node node = nodeMap.get(key);
        if (node != null) {
            moveNodeToHead(node);
        }
        return node;
    }

    private void removeNode() {
        Node node = tail.pre;
        //从链表里面移除
        removeFromList(node);
        nodeMap.remove(node.key);
        count--;
    }

    private void removeFromList(Node node) {
        Node pre = node.pre;
        Node next = node.next;

        pre.next = next;
        next.pre = pre;

        node.next = null;
        node.pre = null;
    }

    private void addNode(Node node) {
        //添加节点到头部
        addToHead(node);
        nodeMap.put(node.key, node);
        count++
    }

    private void addToHead(Node node) {
        Node next = head.next;
        next.pre = node;
        node.next = next;
        node.pre = head;
        head.next = node;
    }

    public void moveNodeToHead(Node node) {
        //从链表里面移除
        removeFromList(node);
        //添加节点到头部
        addToHead(node);
    }

    class Node<k, v> {
        k key;
        v value;
        Node pre;
        Node next;

        public Node(k key, v value) {
            this.key = key;
            this.value = value;
        }
    }
}
```

#### 3.2.1.2. LFU算法：  
&emsp; LFU算法是Redis4.0里面新加的一种淘汰策略。它的全称是Least Frequently Used。它的核心思想是根据key的最近被访问的频率进行淘汰，很少被访问的优先被淘汰，被访问的多的则被留下来。  
&emsp; LFU算法能更好的表示一个key被访问的热度。假如使用的是LRU算法，一个key很久没有被访问到，只刚刚是偶尔被访问了一次，那么它就被认为是热点数据，不会被淘汰，而有些key将来是很有可能被访问到的则被淘汰了。  
&emsp; 如果使用LFU算法则不会出现这种情况，因为使用一次并不会使一个key成为热点数据。  
&emsp; LFU一共有两种策略：  

* volatile-lfu：在设置了过期时间的key中使用LFU算法淘汰key  
* allkeys-lfu：在所有的key中使用LFU算法淘汰数据  


### 3.2.2. 内存淘汰策略 
&emsp; redis提供6种数据淘汰策略：  
* volatile-lru：从已设置过期时间的数据集（server.db[i].expires）中挑选最近最少使用的数据淘汰。  
* volatile-ttl：从已设置过期时间的数据集（server.db[i].expires）中挑选将要过期的数据淘汰。  
* volatile-random：从已设置过期时间的数据集（server.db[i].expires）中任意选择数据淘汰。  
* allkeys-lru：从数据集（server.db[i].dict）中挑选最近最少使用的数据淘汰。  
* allkeys-random：从数据集（server.db[i].dict）中任意选择数据淘汰。  
* no-enviction（驱逐）：禁止驱逐数据，永不回收。redis默认不采用no-enviction，直接返回错误。  


|策略 |含义|
|---|---|
|volatile-lru |根据 LRU 算法删除设置了超时属性（expire）的键，直到腾出足够内存为止。如果没有可删除的键对象，回退到 noeviction 策略。| |allkeys-lru |根据 LRU 算法删除键，不管数据有没有设置超时属性，直到腾出足够内存为止。|
|volatile-lfu |在带有过期时间的键中选择最不常用的。| 
|allkeys-lfu |在所有的键中选择最不常用的，不管数据有没有设置超时属性。| 
|volatile-random |在带有过期时间的键中随机选择。 allkeys-random 随机删除所有键，直到腾出足够内存为止。| 
|volatile-ttl |根据键值对象的 ttl 属性，删除最近将要过期数据。如果没有，回退到 noeviction 策略。| 
|noeviction |默认策略，不会删除任何数据，拒绝所有写入操作并返回客户端错误信息（error）OOM command not allowed when used memory，此时 Redis 只响应读操作。| 

&emsp; 建议使用 volatile-lru，在保证正常服务的情况下，优先删除最近最少使用的 key。  

&emsp; 注：volatile和allkeys规定了是对已设置过期时间的数据集淘汰数据还是从全部数据集淘汰数据。

&emsp; ***使用策略规则：***  

* 如果数据呈现幂律分布，也就是一部分数据访问频率高，一部分数据访问频率低，则使用allkeys-lru。  
* 如果数据呈现平等分布，也就是所有的数据访问频率都相同，则使用allkeys-random。  

<!-- 
&emsp; ***Redis中设置置换策略：***  
&emsp; 在redis.conf配置文件中或通过CONFIG SET动态修改最大缓存maxmemory、置换策略maxmemory-policy。  
-->
&emsp; ***如何获取及设置内存淘汰策略***  
&emsp; 获取当前内存淘汰策略：  

    127.0.0.1:6379> config get maxmemory-policy

&emsp; 通过配置文件设置淘汰策略（修改redis.conf文件）：  

    maxmemory-policy allkeys-lru
    
&emsp; 通过命令修改淘汰策略：  

    127.0.0.1:6379> config set maxmemory-policy allkeys-lru

# 4. Redis事务  
&emsp; Redis 的事务有两个特点：  
1. 按进入队列的顺序执行。  
2. 不会受到其他客户端的请求的影响。  

## 4.1. Redis事务的使用  
&emsp; Redis 的事务涉及到四个命令：multi（开启事务），exec（执行事务），discard （取消事务），watch（监视）。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-36.png)  
1. 使用Multi命令表示开启一个事务；  
2. 开启一个事务过后中间输入的所有命令都不会被立即执行，而是被加入到队列中缓存起来，当收到Exec命令的时候Redis服务会按入队顺序依次执行命令。  
&emsp; 在multi命令后输入的命令不会被立即执行，而是被加入的队列中，并且加入成功redis会返回QUEUED，表示加入队列成功，如果这里的命令输入错误了，或者命令参数不对，Redis会返回ERR 如下图，并且此次事务无法继续执行了。这里需要注意的是在 Redis 2.6.5 版本后是会取消事务的执行，但是在 2.6.5 之前Redis是会执行所有成功加入队列的命令。详细信息可以看官方文档。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-37.png)  
3. 输入exec命令后会依次执行加入到队列中的命令。  

## 4.2. Redis事务中的错误  

1. 在Redis的事务中，命令在加入队列的时候如果出错，那么此次事务是会被取消执行的。这种错误在执行exec命令前Redis服务就可以探测到。  
2. 在 Redis 事务中还有一种错误，那就是所有命令都加入队列成功了，但是在执行exec命令的过程中出现了错误，这种错误 Redis 是无法提前探测到的，那么这种情况下 Redis 的事务是怎么处理的呢？  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-38.png)  
&emsp; 上面测试的过程是先通过命令get a获取a的值为 5，然后开启一个事务，在事务中执行两个动作，第一个是自增a的值，另一个是通过命令hset a b 3来设置a中b的值，可以看到这里a的类型是字符串，但是第二个命令也成功的加入到了队列，Redis并没有报错。但是最后在执行exec命令的时候，第一条命令执行成功了，看到返回结果是6，第二条命令执行失败了，提示的错误信息表示类型不对。  
&emsp; 然后再通过get a命令发现a的值已经被改变了，不再是之前的5了，说明虽然事务失败了但是命令执行的结果并没有回滚！  

&emsp; Redis为什么不支持事务回滚？  
1. 在开发环境中就能避免掉语法错误或者类型不匹配的情况，在生产上是不会出现的；  
2. Redis的内部是简单的快速的，所以不需要支持回滚的能力。  

## 4.3. Redis的乐观锁Watch  
&emsp; 在 Redis 中提供了一个 watch 命令，它可以为 Redis 事务提供 CAS 乐观锁行为（Check and Set / Compare and Swap），也就是多个线程更新变量的时候，会跟原值做比较，只有它没有被其他线程修 改的情况下，才更新成新的值。  

&emsp; Watch会在事务开始之前盯住1个或多个关键变量。  
&emsp; 当事务执行时，也就是服务器收到了exec指令要顺序执行缓存的事务队列时，Redis会检查关键变量自Watch 之后，是否被修改了。  
&emsp; 如果开启事务之后，至少有一个被监视 key 键在 exec 执行之前被修改了， 那么整个事务都会被取消（key 提前过期除外）。  
&emsp; 可以用 unwatch 取消。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-39.png)  

# 5. Redis的事件  
&emsp; Redis 服务器是一个事件驱动程序。  

## 5.1. 文件事件
&emsp; 服务器通过套接字与客户端或者其它服务器进行通信，文件事件就是对套接字操作的抽象。  
&emsp; Redis 基于 Reactor 模式开发了自己的网络事件处理器，使用 I/O 多路复用程序来同时监听多个套接字，并将到达的事件传送给文件事件分派器，分派器会根据套接字产生的事件类型调用相应的事件处理器。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-56.png)  

## 5.2. 时间事件
&emsp; 服务器有一些操作需要在给定的时间点执行，时间事件是对这类定时操作的抽象。  
&emsp; 时间事件又分为：  

* 定时事件：是让一段程序在指定的时间之内执行一次；  
* 周期性事件：是让一段程序每隔指定时间就执行一次。Redis 将所有时间事件都放在一个无序链表中，通过遍历整个链表查找出已到达的时间事件，并调用相应的事件处理器。  

## 5.3. 事件的调度与执行
&emsp; 服务器需要不断监听文件事件的套接字才能得到待处理的文件事件，但是不能一直监听，否则时间事件无法在规定的时间内执行，因此监听时间应该根据距离现在最近的时间事件来决定。  
&emsp; 事件调度与执行由 aeProcessEvents 函数负责，伪代码如下：  

```
def aeProcessEvents():
    # 获取到达时间离当前时间最接近的时间事件
    time_event = aeSearchNearestTimer()
    # 计算最接近的时间事件距离到达还有多少毫秒
    remaind_ms = time_event.when - unix_ts_now()
    # 如果事件已到达，那么 remaind_ms 的值可能为负数，将它设为 0
    if remaind_ms < 0:
        remaind_ms = 0
    # 根据 remaind_ms 的值，创建 timeval
    timeval = create_timeval_with_ms(remaind_ms)
    # 阻塞并等待文件事件产生，最大阻塞时间由传入的 timeval 决定
    aeApiPoll(timeval)
    # 处理所有已产生的文件事件
    procesFileEvents()
    # 处理所有已到达的时间事件
    processTimeEvents()
```
&emsp; 将 aeProcessEvents 函数置于一个循环里面，加上初始化和清理函数，就构成了 Redis 服务器的主函数，伪代码如下：  

```
def main():
    # 初始化服务器
    init_server()
    # 一直处理事件，直到服务器关闭为止
    while server_is_not_shutdown():
        aeProcessEvents()
    # 服务器关闭，执行清理操作
    clean_server()
```
&emsp; 从事件处理的角度来看，服务器运行流程如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Redis/redis-57.png)  

# 6. Redis为什么这么快？  
&emsp; 1）纯内存结构、2）单线程、3）多路复用  

1. 内存  
&emsp; KV 结构的内存数据库，时间复杂度 O(1)。  

2. 单线程  
    &emsp; 单线程的好处：   
    * 没有创建线程、销毁线程带来的消耗  
    * 避免了上线文切换导致的 CPU 消耗  
    * 避免了线程之间带来的竞争问题，例如加锁释放锁死锁等 

3. 异步非阻塞  
&emsp; 异步非阻塞 I/O，多路复用处理并发连接。  





