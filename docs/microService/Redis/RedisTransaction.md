

<!-- TOC -->

- [1. Redis事务](#1-redis事务)
    - [1.1. Redis事务简介](#11-redis事务简介)
    - [1.2. Redis事务的执行](#12-redis事务的执行)
    - [1.3. Redis事务使用案例](#13-redis事务使用案例)
    - [1.4. 事务中的错误](#14-事务中的错误)
    - [1.5. 为什么Redis不支持回滚](#15-为什么redis不支持回滚)
    - [1.6. 带Watch的事务](#16-带watch的事务)

<!-- /TOC -->


&emsp; **<font color = "red">总结：</font>**  
1. **<font color = "clime">Redis事务的三个阶段：</font>**  
    * 开始事务：以MULTI开启一个事务。   
    * **<font color = "clime">命令入队：将多个命令入队到事务中，接到这些命令不会立即执行，而是放到等待执行的事务队列里。</font>**    
    * 执行事务(exec)或取消事务(discard)：由EXEC/DISCARD命令触发事务。  
2. **使用Redis事务的时候，可能会遇上以下两种错误：**  
    * **<font color = "red">（类似于Java中的编译错误）事务在执行EXEC之前，入队的命令可能会出错。</font>** 比如说，命令可能会产生语法错误(参数数量错误，参数名错误等等)，或者其他更严重的错误，比如内存不足(如果服务器使用maxmemory设置了最大内存限制的话)。  
    * **<font color = "red">（类似于Java中的运行错误）命令可能在 EXEC 调用之后失败。</font>** 举个例子，事务中的命令可能处理了错误类型的键，比如将列表命令用在了字符串键上面，诸如此类。  

    1. Redis 针对如上两种错误采用了不同的处理策略，对于发生在 EXEC 执行之前的错误，服务器会对命令入队失败的情况进行记录，并在客户端调用 EXEC 命令时，拒绝执行并自动放弃这个事务（Redis 2.6.5 之前的做法是检查命令入队所得的返回值：如果命令入队时返回 QUEUED ，那么入队成功；否则，就是入队失败）  
    2. 对于那些在 EXEC 命令执行之后所产生的错误，并没有对它们进行特别处理：即使事务中有某个/某些命令在执行时产生了错误，事务中的其他命令仍然会继续执行。 
3. **带Watch的事务：**  
&emsp; WATCH命令用于在事务开始之前监视任意数量的键：当调用EXEC命令执行事务时，如果任意一个被监视的键已经被其他客户端修改了，那么整个事务将被打断，不再执行，直接返回失败。 


# 1. Redis事务  
<!-- 
不支持原子性的 Redis 事务也敢叫事务？ 
https://mp.weixin.qq.com/s/v2Ob6gZjmoJW1cEKb1Om0Q
-->
&emsp; 《Redis深度历险 核心原理与应用实践》  

## 1.1. Redis事务简介  
&emsp; **Redis事务的概念：**   
&emsp; **<font color = "clime">Redis事务的本质是一组命令的集合。</font>** 事务支持一次执行多个命令，一个事务中所有命令都会被序列化。在事务执行过程，会按照顺序串行化执行队列中的命令，其他客户端提交的命令请求不会插入到事务执行命令序列中。   

    redis事务就是一次性、顺序性、排他性的执行一个队列中的一系列命令。　　

&emsp; redis事务3个特性：  

* 单独的隔离操作：事务中的所有命令都会序列化、按顺序地执行。事务在执行的过程中，不会被其他客户端发送来的命令请求所打断。
* 没有隔离级别的概念：队列中的命令没有提交之前都不会实际的被执行，因为事务提交前任何指令都不会被实际执行，也就不存在“事务内的查询要看到事务里的更新，在事务外查询不能看到”这个让人万分头痛的问题。  
* 不保证原子性：Redis 同一个事务中如果有一条命令执行失败，其后的命令仍然会被执行，没有回滚。  

&emsp; 在传统的关系式数据库中，常常用 ACID 性质来检验事务功能的安全性。Redis事务保证了其中的一致性(C)和隔离性(I)，但并不保证原子性(A)和持久性(D)。  

<!-- 

&emsp; ~~Redis事务可以一次执行多个命令，本质是一组命令的集合。一个事务中的所有命令都会序列化，按顺序地串行化执行而不会被其它命令插入，不许加塞。~~  
&emsp; ~~可以保证一个队列中，一次性、顺序性、排他性的执行一系列命令(Redis 事务的主要作用其实就是串联多个命令防止别的命令插队)。~~  

&emsp; 官方文档是这么说的  

    事务可以一次执行多个命令， 并且带有以下两个重要的保证：  

        事务是一个单独的隔离操作：事务中的所有命令都会序列化、按顺序地执行。事务在执行的过程中，不会被其他客户端发送来的命令请求所打断。
        事务是一个原子操作：事务中的命令要么全部被执行，要么全部都不执行。  


&emsp; **Redis事务没有隔离级别的概念：**  
&emsp; 批量操作在发送EXEC命令前被放入队列缓存，并不会被实际执行，也就不存在事务内的查询要看到事务里的更新，事务外查询不能看到。  

&emsp; **Redis不保证原子性：**  
&emsp; Redis中，单条命令是原子性执行的，但事务不保证原子性，且没有回滚。事务中任意命令执行失败，其余的命令仍会被执行。  
-->

## 1.2. Redis事务的执行
&emsp; **<font color = "clime">Redis事务的三个阶段：</font>**  

* 开始事务：以MULTI开启一个事务。   
* 命令入队：将多个命令入队到事务中，接到这些命令不会立即执行，而是放到等待执行的事务队列里面。    
* 执行事务(exec)或取消事务(discard)：由EXEC/DISCARD命令触发事务。  

```
> multi
OK
> incr star
QUEUED
> incr star
QUEUED
> exec
(integer) 1
(integer) 2
```

&emsp; 上面的指令演示了一个完整的事务过程，所有的指令在exec之前不执行，而是缓存在服务器的一个事务队列中，服务器收到exec指令，才开始执行整个事务队列，执行完毕后一次性返回所有指令的运行结果。  

&emsp; **Redis事务相关命令：**  

* multi：标记一个事务块的开始(queued)
* exec：执行所有事务块的命令(一旦执行exec后，之前加的监控锁都会被取消掉)　
* discard：取消事务，放弃事务块中的所有命令
* watch key1 key2 ...：<font color = "red">监视一或多个key，如果在事务执行之前，被监视的key被其他命令改动，则事务被打断(类似乐观锁)</font>
* unwatch：取消watch对所有key的监控
![image](http://www.wt1814.com/static/view/images/microService/Redis/redis-98.png)  
![image](http://www.wt1814.com/static/view/images/microService/Redis/redis-99.png)  

## 1.3. Redis事务使用案例  
&emsp; (1)正常执行。  
![image](http://www.wt1814.com/static/view/images/microService/Redis/redis-91.png)  
&emsp; (2)放弃事务。  
![image](http://www.wt1814.com/static/view/images/microService/Redis/redis-92.png)  

## 1.4. 事务中的错误
<!-- 
不支持原子性的 Redis 事务也敢叫事务？ 
https://mp.weixin.qq.com/s/v2Ob6gZjmoJW1cEKb1Om0Q
Redis中基本的事务的错误处理办法
https://blog.csdn.net/csdn18740599042/article/details/109268025

&emsp; 在Java中有两种错误，一种是编译型异常，一种为运行时异常  
&emsp; 顾名思义： 
&emsp; 编译型就是代码语法错误，编译不能通过  
&emsp; 运行时异常就是逻辑错误，但编译时可以通过  
&emsp; 在redis中也是一样的，编译型异常即为命令错误，运行时异常即为逻辑错误  
-->

&emsp; 使用Redis事务的时候，可能会遇上以下两种错误：

* （类似于Java中的编译错误）事务在执行EXEC之前，入队的命令可能会出错。比如说，命令可能会产生语法错误（参数数量错误，参数名错误等等），或者其他更严重的错误，比如内存不足（如果服务器使用maxmemory设置了最大内存限制的话）。  
* （类似于Java中的运行错误）命令可能在EXEC调用之后失败。举个例子，事务中的命令可能处理了错误类型的键，比如将列表命令用在了字符串键上面，诸如此类。  

1. Redis 针对如上两种错误采用了不同的处理策略，对于发生在 EXEC 执行之前的错误，服务器会对命令入队失败的情况进行记录，并在客户端调用EXEC命令时，拒绝执行并自动放弃这个事务（Redis 2.6.5 之前的做法是检查命令入队所得的返回值：如果命令入队时返回 QUEUED ，那么入队成功；否则，就是入队失败）。  
2. 对于那些在 EXEC 命令执行之后所产生的错误， 并没有对它们进行特别处理：即使事务中有某个/某些命令在执行时产生了错误，事务中的其他命令仍然会继续执行。  

----

&emsp; (3)<font color = "clime">若在事务队列中存在命令性错误(类似于java编译性错误)，</font><font color = "red">则执行EXEC命令时，所有命令都不会执行。</font>  
![image](http://www.wt1814.com/static/view/images/microService/Redis/redis-93.png)  
&emsp; (4)<font color = "clime">若在事务队列中存在语法性错误(类似于java的1/0的运行时异常)，</font><font color = "red">则执行EXEC命令时，其他正确命令会被执行，错误命令抛出异常。</font>  
![image](http://www.wt1814.com/static/view/images/microService/Redis/redis-94.png)  

## 1.5. 为什么Redis不支持回滚  
&emsp; 如果有使用关系式数据库的经验，那么 “Redis 在事务失败时不进行回滚，而是继续执行余下的命令”，这种做法可能会让人觉得有点奇怪。  
&emsp; 以下是官方的自夸：  

        Redis 命令只会因为错误的语法而失败(并且这些问题不能在入队时发现)，或是命令用在了错误类型的键上面：这也就是说，从实用性的角度来说，失败的命令是由编程错误造成的，而这些错误应该在开发的过程中被发现，而不应该出现在生产环境中。
        因为不需要对回滚进行支持，所以 Redis 的内部可以保持简单且快速。

&emsp; 有种观点认为Redis处理事务的做法会产生bug，然而需要注意的是，在通常情况下，回滚并不能解决编程错误带来的问题。举个例子，如果本来想通过INCR命令将键的值加上1，却不小心加上了2，又或者对错误类型的键执行了INCR，回滚是没有办法处理这些情况的。  
&emsp; 鉴于没有任何机制能避免程序员自己造成的错误， 并且这类错误通常不会在生产环境中出现， 所以 Redis 选择了更简单、更快速的无回滚方式来处理事务。  

## 1.6. 带Watch的事务  
&emsp; **<font color = "red">WATCH命令用于在事务开始之前监视任意数量的键：当调用EXEC命令执行事务时，如果任意一个被监视的键已经被其他客户端修改了，那么整个事务将被打断，不再执行，直接返回失败。</font>**  
&emsp; WATCH命令可以被调用多次。对键的监视从WATCH执行之后开始生效，直到调用EXEC为止。  

&emsp; 用户还可以在单个 WATCH 命令中监视任意多个键，就像这样：  

```
redis> WATCH key1 key2 key3 
OK 
```
&emsp; 当 EXEC 被调用时， 不管事务是否成功执行，对所有键的监视都会被取消。另外， 当客户端断开连接时， 该客户端对键的监视也会被取消。  

&emsp; 使用无参数的 UNWATCH 命令可以手动取消对所有键的监视。对于一些需要改动多个键的事务，有时候程序需要同时对多个键进行加锁，然后检查这些键的当前值是否符合程序的要求。当值达不到要求时，就可以使用 UNWATCH 命令来取消目前对键的监视，中途放弃这个事务， 并等待事务的下次尝试。  
&emsp; watch指令类似于乐观锁，在事务提交时，<font color = "red">如果watch监控的多个KEY中任何KEY的值已经被其他客户端更改，则使用EXEC执行事务时，</font>事务队列将不会被执行，同时返回Nullmulti-bulk应答以通知调用者事务执行失败。  

-----

&emsp; (5)使用watch  
&emsp; 案例一：使用watch检测balance，事务期间balance数据未变动，事务执行成功  
![image](http://www.wt1814.com/static/view/images/microService/Redis/redis-95.png)  
&emsp; 案例二：使用watch检测balance，在开启事务后(标注1处)，在新窗口执行标注2中的操作，更改balance的值，模拟其他客户端在事务执行期间更改watch监控的数据，然后再执行标注1后命令，执行EXEC后，事务未成功执行。  
![image](http://www.wt1814.com/static/view/images/microService/Redis/redis-96.png)  
&emsp; 一但执行EXEC开启事务的执行后，无论事务使用执行成功，WARCH对变量的监控都将被取消。  
&emsp; 故当事务执行失败后，需重新执行WATCH命令对变量进行监控，并开启新的事务进行操作。  




<!-- 
&emsp; Redis 的事务有两个特点：  
1. 按进入队列的顺序执行。  
2. 不会受到其他客户端的请求影响。  

1.1. Redis事务的使用  
&emsp; Redis 的事务涉及到四个命令：multi(开启事务)，exec(执行事务)，discard (取消事务)，watch(监视)。  
1. 使用Multi命令表示开启一个事务；  
![image](http://www.wt1814.com/static/view/images/microService/Redis/redis-36.png)  
2. 开启一个事务过后中间输入的所有命令都不会被立即执行，而是被加入到队列中缓存起来，当收到Exec命令的时候Redis服务会按入队顺序依次执行命令。  
&emsp; 在multi命令后输入的命令不会被立即执行，而是被加入的队列中，并且加入成功redis会返回QUEUED，表示加入队列成功，如果这里的命令输入错误了，或者命令参数不对，Redis会返回ERR 如下图，并且此次事务无法继续执行了。这里需要注意的是在 Redis 2.6.5 版本后是会取消事务的执行，但是在 2.6.5 之前Redis是会执行所有成功加入队列的命令。详细信息可以看官方文档。  
![image](http://www.wt1814.com/static/view/images/microService/Redis/redis-37.png)  
3. 输入exec命令后会依次执行加入到队列中的命令。  

1.2. Redis事务中的错误  

1. 在Redis的事务中，命令在加入队列的时候如果出错，那么此次事务是会被取消执行的。这种错误在执行exec命令前Redis服务就可以探测到。  
2. 在 Redis 事务中还有一种错误，那就是所有命令都加入队列成功了，但是在执行exec命令的过程中出现了错误，这种错误 Redis 是无法提前探测到的，那么这种情况下 Redis 的事务是怎么处理的呢？  
![image](http://www.wt1814.com/static/view/images/microService/Redis/redis-38.png)  
&emsp; 上面测试的过程是先通过命令get a获取a的值为 5，然后开启一个事务，在事务中执行两个动作，第一个是自增a的值，另一个是通过命令hset a b 3来设置a中b的值，可以看到这里a的类型是字符串，但是第二个命令也成功的加入到了队列，Redis并没有报错。但是最后在执行exec命令的时候，第一条命令执行成功了，看到返回结果是6，第二条命令执行失败了，提示的错误信息表示类型不对。  
&emsp; 然后再通过get a命令发现a的值已经被改变了，不再是之前的5了，说明虽然事务失败了但是命令执行的结果并没有回滚！  

&emsp; **<font color = "red">Redis为什么不支持事务回滚？</font>**  
1. 在开发环境中就能避免掉语法错误或者类型不匹配的情况，在生产上是不会出现的；  
2. Redis的内部是简单的快速的，所以不需要支持回滚的能力。  

1.3. Redis的乐观锁Watch  
&emsp; 在 Redis 中提供了一个 watch 命令，它可以为 Redis 事务提供 CAS 乐观锁行为(Check and Set / Compare and Swap)，也就是多个线程更新变量的时候，会跟原值做比较，只有它没有被其他线程修 改的情况下，才更新成新的值。  

&emsp; Watch会在事务开始之前盯住1个或多个关键变量。  
&emsp; 当事务执行时，也就是服务器收到了exec指令要顺序执行缓存的事务队列时，Redis会检查关键变量自Watch 之后，是否被修改了。  
&emsp; 如果开启事务之后，至少有一个被监视 key 键在 exec 执行之前被修改了， 那么整个事务都会被取消(key 提前过期除外)。  
&emsp; 可以用 unwatch 取消。  
![image](http://www.wt1814.com/static/view/images/microService/Redis/redis-39.png)  
-->

---------

WATCH命令和基于CAS的乐观锁?  

在Redis的事务中，WATCH命令可用于提供CAS(check-and-set)功能。假设我们通过WATCH命令在事务执行之前监控了多个Keys，倘若在WATCH之后有任何Key的值发生了变化，EXEC命令执行的事务都将被放弃，同时返回Null multi-bulk应答以通知调用者事务  

执行失败。例如，我们再次假设Redis中并未提供incr命令来完成键值的原子性递增，如果要实现该功能，我们只能自行编写相应的代码。其伪码如下：  

val = GET mykey val = val + 1 SET mykey $val  

以上代码只有在单连接的情况下才可以保证执行结果是正确的，因为如果在同一时刻有多个客户端在同时执行该段代码，那么就会出现多线程程序中经常出现的一种错误场景--竞态争用(race condition)。比如，客户端A和B都在同一时刻读取了mykey的原有值，假设该值为10，此后两个客户端又均将该值加一后set回Redis服务器，这样就会导致mykey的结果为11，而不是我们认为的12。为了解决类似的问题，我们需要借助WATCH命令的帮助，见如下代码：  

WATCH mykey val = GET mykey val = val + 1 MULTI SET mykey $val EXEC   

和此前代码不同的是，新代码在获取mykey的值之前先通过WATCH命令监控了该键，此后又将set命令包围在事务中，这样就可以有效的保证每个连接在执行EXEC之前，如果当前连接获取的mykey的值被其它连接的客户端修改，那么当前连接的EXEC命令将执行失败。这样调用者在判断返回值后就可以获悉val是否被重新设置成功。  
