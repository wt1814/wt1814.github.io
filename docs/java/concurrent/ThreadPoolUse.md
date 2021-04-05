
<!-- TOC -->

- [1. 线程池正确用法](#1-线程池正确用法)
    - [1.1. 线程池异常处理](#11-线程池异常处理)
        - [1.1.1. 异常处理问题](#111-异常处理问题)
        - [1.1.2. 直接catch](#112-直接catch)
        - [1.1.3. 线程池实现](#113-线程池实现)
            - [1.1.3.1. 自定义线程池](#1131-自定义线程池)
            - [1.1.3.2. 实现Thread.UncaughtExceptionHandler接口](#1132-实现threaduncaughtexceptionhandler接口)
            - [1.1.3.3. 继承ThreadGroup](#1133-继承threadgroup)
            - [1.1.3.4. 采用Future模式](#1134-采用future模式)
    - [1.2. 设置隔离的线程池](#12-设置隔离的线程池)
    - [1.3. 确定线程池的大小](#13-确定线程池的大小)
    - [1.4. ★★★线程池的监控](#14-★★★线程池的监控)
    - [1.5. 线程池关闭](#15-线程池关闭)
        - [1.5.1. ThreadPoolExecutor的shutdown()与shutdownNow()源码](#151-threadpoolexecutor的shutdown与shutdownnow源码)
        - [1.5.2. ThreadPoolExecutor#awaitTermination](#152-threadpoolexecutorawaittermination)
        - [1.5.3. 总结：优雅关闭线程池](#153-总结优雅关闭线程池)
    - [1.6. SpringBoot整合线程池](#16-springboot整合线程池)
        - [1.6.1. ※※※@Async没有执行的问题分析(@Async线程默认配置)](#161-※※※async没有执行的问题分析async线程默认配置)
        - [1.6.2. 重写spring默认线程池](#162-重写spring默认线程池)
        - [1.6.3. 自定义线程池](#163-自定义线程池)

<!-- /TOC -->


&emsp; **<font color = "red">总结：</font>**  
1. **<font color = "clime">线程池异常处理：</font>**  
&emsp; ThreadPoolExecutor中将异常传递给afterExecute()方法，而afterExecute()没有做任何处理。这种处理方式能够保证提交的任务抛出了异常不会影响其他任务的执行，同时也不会对用来执行该任务的线程产生任何影响。然而afterExecute()没有做任何处理，所以如果任务抛出了异常，也无法立刻感知到。 即使感知到了，也无法查看异常信息。  
&emsp; 解决方案：在提交的任务中将异常捕获并处理，不抛给线程池； 异常抛给线程池，但是要及时处理抛出的异常。如果提交任务的时候使用的方法是submit，那么该方法将返回一个Future对象，所有的异常以及处理结果都可以通过future对象获取。    
2. **<font color = "red">建议根据异步业务类型，合理设置隔离的线程池。</font>**  
3. 确定线程池的大小（CPU可同时处理线程数量大部分是CPU核数的两倍）  
    * 如果是CPU密集型应用(多线程处理复杂算法)，则线程池大小设置为N+1。
    * 如果是IO密集型应用(多线程用于数据库数据交互、文件上传下载、网络数据传输等)，则线程池大小设置为2N。
    * 如果是混合型，将任务分为CPU密集型和IO密集型，然后分别使用不同的线程池去处理，从而使每个线程池可以根据各自的工作负载来调整。 
4. **<font color = "clime">线程池的监控：</font>**  
&emsp; 通过重写线程池的beforeExecute、afterExecute和shutdown等方式就可以实现对线程的监控。  
5. @Async方法没有执行的问题分析：  
&emsp; @Async异步方法默认使用Spring创建ThreadPoolTaskExecutor(参考TaskExecutionAutoConfiguration)，其中默认核心线程数为8，默认最大队列和默认最大线程数都是Integer.MAX_VALUE，队列使用LinkedBlockingQueue，容量是：Integet.MAX_VALUE，空闲线程保留时间：60s，线程池拒绝策略：AbortPolicy。创建新线程的条件是队列填满时，而这样的配置队列永远不会填满，如果有@Async注解标注的方法长期占用线程(比如HTTP长连接等待获取结果)，在核心8个线程数占用满了之后，新的调用就会进入队列，外部表现为没有执行。  

# 1. 线程池正确用法
<!-- 
论如何优雅的自定义ThreadPoolExecutor线程池
https://www.cnblogs.com/wang-meng/p/10163855.html
-->

## 1.1. 线程池异常处理
### 1.1.1. 异常处理问题  
&emsp; java线程池ThreadPoolExecutor，真正执行代码的部分是runWorker()方法。  

```java
final void runWorker(Worker w) {
    //...
    try {
        beforeExecute(wt, task);
        Throwable thrown = null;
        try {
            task.run();//执行程序逻辑
        } catch (RuntimeException x) {//捕获RuntimeException
            thrown = x; throw x; //抛出异常
        } catch (Error x) {
            thrown = x; throw x;
        } catch (Throwable x) {
            thrown = x; throw new Error(x);
        } finally {
            afterExecute(task, thrown);//执行后续逻辑
        }
    } finally {
        task = null;
        w.completedTasks++;
        w.unlock();
    }
    //...
}
```
&emsp; 程序会捕获包括Error在内的所有异常，并且在程序最后，将出现过的异常和当前任务传递给afterExecute方法。而ThreadPoolExecutor中的afterExecute方法是没有任何实现的。  

&emsp; ThreadPoolExecutor这种处理方式会有什么问题？  
&emsp; 这样做能够保证提交的任务抛出了异常不会影响其他任务的执行，同时也不会对用来执行该任务的线程产生任何影响。然而afterExecute()没有做任何处理，所以如果任务抛出了异常，也无法立刻感知到。 即使感知到了，也无法查看异常信息。  
&emsp; 解决方案：  

* 在提交的任务中将异常捕获并处理，不抛给线程池。  
* 异常抛给线程池，但是要及时处理抛出的异常。  

### 1.1.2. 直接catch  
&emsp; 提交的任务，将所有可能的异常都Catch住，并且自行处理。  

### 1.1.3. 线程池实现  
&emsp; 有以下四种实现方式。  

#### 1.1.3.1. 自定义线程池  
&emsp; 自定义线程池，继承ThreadPoolExecutor并复写其afterExecute(Runnable r, Throwable t)方法。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/threadPool-7.png)  

#### 1.1.3.2. 实现Thread.UncaughtExceptionHandler接口  
&emsp; 实现Thread.UncaughtExceptionHandler接口，实现void uncaughtException(Thread t, Throwable e)方法，并将该handler传递给线程池的ThreadFactory。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/threadPool-8.png)  

#### 1.1.3.3. 继承ThreadGroup  
&emsp; 覆盖uncaughtException方法。(与实现Thread.UncaughtExceptionHandler接口类似，因为ThreadGroup类本身就实现了Thread.UncaughtExceptionHandler接口)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/threadPool-9.png)  
&emsp; 注意：上面三种方式针对的都是通过execute(xx)的方式提交任务，如果提交任务用的是submit()方法，那么上面的三种方式都将不起作用，而应该使用下面的方式。  

#### 1.1.3.4. 采用Future模式  
&emsp; <font color = "red">如果提交任务的时候使用的方法是submit，那么该方法将返回一个Future对象，所有的异常以及处理结果都可以通过future对象获取。</font>  
&emsp; 采用Future模式，将返回结果以及异常放到Future中，在Future中处理  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/threadPool-11.png)  


## 1.2. 设置隔离的线程池
&emsp; 一些业务代码做了Utils类型在整个项目中的各种操作共享使用一个线程池，一些业务代码大量使用parallel stream特性做一些耗时操作，但是没有使用自定义的线程池或是没有设置更大的线程数（没有意识到parallel stream的共享ForkJoinPool问题）。共享的问题在于会干扰，如果有一些异步操作的平均耗时是1秒，另外一些是100秒，这些操作放在一起共享一个线程池很可能会出现相互影响甚至饿死的问题。 **<font color = "red">建议根据异步业务类型，合理设置隔离的线程池。</font>**  

## 1.3. 确定线程池的大小
&emsp; **<font color = "clime">CPU可同时处理线程数量大部分是CPU核数的两倍。</font>**    
&emsp; **一般做法：**  

* 如果是CPU密集型应用(多线程处理复杂算法)，则线程池大小设置为N+1。  
* 如果是IO密集型应用(多线程用于数据库数据交互、文件上传下载、网络数据传输等)，则线程池大小设置为2N。  
* 如果是混合型，将任务分为CPU密集型和IO密集型，然后分别使用不同的线程池去处理，从而使每个线程池可以根据各自的工作负载来调整。   

&emsp; N表示CPU数量，可以根据Runtime.availableProcessors方法获取.   

```java
private static int corePoolSize = Runtime.getRuntime().availableProcessors();
```  

<!-- 
线程池参数如何设置？

线程池既然有这么多参数那么我们如何去根据自己的业务实际情况来去合理的设置每个参数？

    一般我们如果任务为耗时IO型比如读取数据库、文件读写以及网略通信的的话这些任务不会占据很多cpu的资源但是会比较耗时：线程数设置为2倍CPU数以上，充分的来利用CPU资源。
    一般我们如果任务为CPU密集型的话比如大量计算、解压、压缩等这些操作都会占据大量的cpu。所以针对于这种情况的话一般设置线程数为：1倍cpu+1。为啥要加1，很多说法是备份线程。
    如果既有IO密集型任务，又有CPU密集型任务，这种该怎么设置线程大小？这种的话最好分开用线程池处理，IO密集的用IO密集型线程池处理，CPU密集型的用cpu密集型处理。以上都只是理算情况下的估算而已，真正的合理参数还是需要看看实际生产运行的效果来合理的调整的。
-->
----
&emsp; Little's Law(利特尔法则)：一个系统请求数等于请求的到达率与平均每个单独请求花费的时间之乘积。使用利特尔法则(Little’s law)来判定线程池大小。只需计算请求到达率和请求处理的平均时间。估算公式如下：  
    
    线程池大小=((线程等待IO时间+ 线程CPU时间)/线程CPU时间 ) * CPU数目
    
&emsp; 通过公式，了解到需要3个具体数值：  
1. 一个请求所消耗的时间 (线程 IO time + 线程 CPU time)。 
2. 该请求计算时间 (线程 CPU time) 
3. CPU数目 

&emsp; 请求消耗时间：Web服务容器中，可以通过Filter来拦截获取该请求前后消耗的时间。  

```java
public class MoniterFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(MoniterFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        long start = System.currentTimeMillis();

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String uri = httpRequest.getRequestURI();
        String params = getQueryString(httpRequest);

        try {
            chain.doFilter(httpRequest, httpResponse);
        } finally {
            long cost = System.currentTimeMillis() - start;
            logger.info("access url [{}{}], cost time [{}] ms )", uri, params, cost);
        }

        private String getQueryString (HttpServletRequest req){
            StringBuilder buffer = new StringBuilder("?");
            Enumeration<String> emParams = req.getParameterNames();
            try {
                while (emParams.hasMoreElements()) {
                    String sParam = emParams.nextElement();
                    String sValues = req.getParameter(sParam);
                    buffer.append(sParam).append("=").append(sValues).append("&");
                }
                return buffer.substring(0, buffer.length() - 1);
            } catch (Exception e) {
                logger.error("get post arguments error", buffer.toString());
            }
            return "";
        }
    }
}
```
&emsp; CPU计算时间：CPU计算时间 = 请求总耗时 - CPU IO time。假设该请求有一个查询 DB 的操作，只要知道这个查询DB的耗时（CPU IO time），计算的时间不就出来了嘛，看一下怎么才能简洁明了的记录DB查询的耗时。通过（JDK 动态代理/ CGLIB）的方式添加AOP切面，来获取线程IO耗时。代码如下，请参考.  

```java
public class DaoInterceptor implements MethodInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(DaoInterceptor.class);

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        StopWatch watch = new StopWatch();
        watch.start();
        Object result = null;
        Throwable t = null;
        try {
            result = invocation.proceed();
        } catch (Throwable e) {
            t = e == null ? null : e.getCause();
            throw e;
        } finally {
            watch.stop();
            logger.info("({}ms)", watch.getTotalTimeMillis());

        }

        return result;
    }

}
```
&emsp; CPU数目：逻辑CPU个数。  

&emsp; 总结：合适的配置线程池大小其实很不容易，但是通过上述的公式和具体代码，就能快速、落地的算出这个线程池该设置的多大。不过还是需要通过压力测试来进行微调，只有经过压测测试的检验，才能最终保证的配置大小是准确的。 

## 1.4. ★★★线程池的监控  
&emsp; 如果在项目中大规模的使用了线程池，那么必须要有一套监控体系，来指导当前线程池的状态，当出现问题的时候可以快速定位到问题。而线程池提供了相应的扩展方法，**<font color = "clime">通过重写线程池的beforeExecute、afterExecute和shutdown等方式就可以实现对线程的监控。</font>**  

```java
public class Demo1 extends ThreadPoolExecutor {
    // 保存任务开始执行的时间,当任务结束时,用任务结束时间减去开始时间计算任务执行时间
    private ConcurrentHashMap<String,Date> startTimes;
    public Demo1(int corePoolSize, int maximumPoolSize, long
            keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        this.startTimes=new ConcurrentHashMap<>();
    }

    @Override
    public void shutdown() {
        System.out.println("已经执行的任务数：
                "+this.getCompletedTaskCount()+"," + "当前活动线程数:"+this.getActiveCount()+",当前排队线程数:"+this.getQueue().size());
        System.out.println();
        super.shutdown();
    }

    //任务开始之前记录任务开始时间
    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        startTimes.put(String.valueOf(r.hashCode()),new Date());
        super.beforeExecute(t, r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        Date startDate = startTimes.remove(String.valueOf(r.hashCode()));
        Date finishDate = new Date();
        long diff = finishDate.getTime() - startDate.getTime();
        // 统计任务耗时、初始线程数、核心线程数、正在执行的任务数量、
        // 已完成任务数量、任务总数、队列里缓存的任务数量、
        // 池中存在的最大线程数、最大允许的线程数、线程空闲时间、线程池是否关闭、线程池是否终止
        System.out.print("任务耗时:"+diff+"\n");
        System.out.print("初始线程数:"+this.getPoolSize()+"\n");
        System.out.print("核心线程数:"+this.getCorePoolSize()+"\n");
        System.out.print("正在执行的任务数量:"+this.getActiveCount()+"\n");
        System.out.print("已经执行的任务数:"+this.getCompletedTaskCount()+"\n");
        System.out.print("任务总数:"+this.getTaskCount()+"\n");
        System.out.print("最大允许的线程数:"+this.getMaximumPoolSize()+"\n");
        System.out.print("线程空闲时间:"+this.getKeepAliveTime(TimeUnit.MILLISECONDS)+"\n");
        System.out.println();
        super.afterExecute(r, t);
    }
    public static ExecutorService newCachedThreadPool() {
        return new Demo1(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new
                SynchronousQueue ());
    }

}
```

&emsp; 测试用例：  
```java
public class Test implements Runnable{
    private static ExecutorService es =Demo1.newCachedThreadPool();
    @Override
    public void run() {
        try {

            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } }
    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 100; i++) {
            es.execute(new Test());
        }
        es.shutdown();
    }
}
```

----

![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/threadPool-19.png)  

## 1.5. 线程池关闭  
&emsp; 线程池总共存在5种状态，分别为：RUNNING、SHUTDOWN、STOP、TIDYING、TERMINATED。    
&emsp; 当执行ThreadPoolExecutor#shutdown方法将会使线程池状态从 RUNNING 转变为 SHUTDOWN。而调用 ThreadPoolExecutor#shutdownNow 之后线程池状态将会从 RUNNING 转变为 STOP。从上面的图上还可以看到，当线程池处于 SHUTDOWN，还是可以继续调用 ThreadPoolExecutor#shutdownNow 方法，将其状态转变为 STOP 。    

### 1.5.1. ThreadPoolExecutor的shutdown()与shutdownNow()源码
&emsp; ThreadPoolExecutor#shutdown()方法源码：  

```java
public void shutdown() {
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        // 检查权限
        checkShutdownAccess();
        // 设置线程池状态
        advanceRunState(SHUTDOWN);
        // 中断空闲线程
        interruptIdleWorkers();
        // 钩子函数，主要用于清理一些资源
        onShutdown();
    } finally {
        mainLock.unlock();
    }
    tryTerminate();
}
```
&emsp; shutdown方法首先加锁，其次先检查系统安装状态。接着就会将线程池状态变为SHUTDOWN，在这之后线程池不再接受提交的新任务。此时如果还继续往线程池提交任务，将会使用线程池拒绝策略响应，默认情况下将会使用ThreadPoolExecutor.AbortPolicy，抛出RejectedExecutionException异常。  
&emsp; interruptIdleWorkers方法只会中断空闲的线程，不会中断正在执行任务的的线程。空闲的线程将会阻塞在线程池的阻塞队列上。  

&emsp; ThreadPoolExecutor#shutdownNow()源码如下：  

```java
public List<Runnable> shutdownNow() {
    List<Runnable> tasks;
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        // 检查状态
        checkShutdownAccess();
        // 将线程池状态变为 STOP
        advanceRunState(STOP);
        // 中断所有线程，包括工作线程以及空闲线程
        interruptWorkers();
        // 丢弃工作队列中存量任务
        tasks = drainQueue();
    } finally {
        mainLock.unlock();
    }
    tryTerminate();
    return tasks;
}
```
&emsp; shutdownNow 方法将会把线程池状态设置为 STOP，然后中断所有线程，最后取出工作队列中所有未完成的任务返回给调用者。  
&emsp; 对比 shutdown 方法，shutdownNow 方法比较粗暴，直接中断工作线程。不过这里需要注意，中断线程并不代表线程立刻结束。这里需要线程主动配合线程中断响应。  

### 1.5.2. ThreadPoolExecutor#awaitTermination   
&emsp; 线程池 shutdown 与 shutdownNow 方法都不会主动等待执行任务的结束，如果需要等到线程池任务执行结束，需要调用 awaitTermination 主动等待任务调用结束。  
&emsp; 调用方法如下：  

```java
threadPool.shutdown();
try {
        while (!threadPool.awaitTermination(60,TimeUnit.SECONDS)){
            System.out.println("线程池任务还未执行结束");
        }
    } catch (InterruptedException e) {
        e.printStackTrace();
}
```
&emsp; 如果线程池任务执行结束，awaitTermination方法将会返回true，否则当等待时间超过指定时间后将会返回false。  
&emsp; 如果需要使用这种进制，建议在上面的基础上增加一定重试次数。这个真的很重要！！！  

### 1.5.3. 总结：优雅关闭线程池  
&emsp; 处于SHUTDOWN的状态下的线程池依旧可以调用shutdownNow。所以可以结合 shutdown，shutdownNow，awaitTermination，更加优雅关闭线程池。  

```java
threadPool.shutdown(); // Disable new tasks from being submitted
// 设定最大重试次数
try {
    // 等待 60 s
    if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
        // 调用 shutdownNow 取消正在执行的任务
        threadPool.shutdownNow();
        // 再次等待 60 s，如果还未结束，可以再次尝试，或则直接放弃
        if (!threadPool.awaitTermination(60, TimeUnit.SECONDS))
        System.err.println("线程池任务未正常执行结束");
    }
} catch (InterruptedException ie) {
    // 重新调用 shutdownNow
    threadPool.shutdownNow();
}
```

## 1.6. SpringBoot整合线程池
&emsp; SpringBoot框架提供了@Async注解使用ThreadPoolExecutor。可以重写spring默认的线程池或自定义线程池。  

### 1.6.1. ※※※@Async没有执行的问题分析(@Async线程默认配置)  
<!-- 
~~
https://www.cnblogs.com/kiko2014551511/p/12754927.html
-->
&emsp; @Async异步方法默认使用Spring创建ThreadPoolTaskExecutor。默认核心线程数：8，最大线程数：Integet.MAX_VALUE，队列使用LinkedBlockingQueue，容量是：Integet.MAX_VALUE，空闲线程保留时间：60s，线程池拒绝策略：AbortPolicy。  
&emsp; 可以手动配置相应属性：  

&emsp; 现象：  
1. 表面现象：方法中输出的日志，日志文件中找不到，也没有任何报错(即@Async标注的方法没有执行，也没有报错)。 
2. 分析现象：日志中某个时刻之后没有了task-xxx线程的日志  

&emsp; 原因:   
&emsp; @Async异步方法默认使用Spring创建ThreadPoolTaskExecutor(参考TaskExecutionAutoConfiguration)，其中默认核心线程数为8，默认最大队列和默认最大线程数都是Integer.MAX_VALUE，队列使用LinkedBlockingQueue，容量是：Integet.MAX_VALUE，空闲线程保留时间：60s，线程池拒绝策略：AbortPolicy。创建新线程的条件是队列填满时，而这样的配置队列永远不会填满，如果有@Async注解标注的方法长期占用线程(比如HTTP长连接等待获取结果)，在核心8个线程数占用满了之后，新的调用就会进入队列，外部表现为没有执行。  

&emsp; 解决：  
&emsp; 手动配置相应属性即可. 比如  

```
#核心线程数
spring.task.execution.pool.core-size=
#最大线程数
spring.task.execution.pool.max-size=
#空闲线程保留时间
spring.task.execution.pool.keep-alive=3s
#队列容量
spring.task.execution.pool.queue-capacity=
#线程名称前缀
spring.task.execution.thread-name-prefix=test-thread-
```

&emsp; 配置类是TaskExecutionProperties【org.springframework.boot.autoconfigure.task.TaskExecutionProperties】  

### 1.6.2. 重写spring默认线程池

```java
@Slf4j
@Configuration
public class NativeAsyncTaskExecutePool implements AsyncConfigurer{

    //注入配置类
    @Autowired
    TaskThreadPoolConfig config;

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(config.getCorePoolSize()); //核心线程池大小
        executor.setMaxPoolSize(config.getMaxPoolSize()); //最大线程数
        executor.setQueueCapacity(config.getQueueCapacity()); //队列容量
        executor.setKeepAliveSeconds(config.getKeepAliveSeconds()); //活跃时间
        executor.setThreadNamePrefix("MyExecutor-"); //线程名字前缀
        // setRejectedExecutionHandler：当pool已经达到max size的时候，如何处理新任务
        // CallerRunsPolicy：不在新线程中执行任务，而是由调用者所在的线程来执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /**
     *  异步任务中异常处理
     * @return
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncUncaughtExceptionHandler() {
            @Override
            public void handleUncaughtException(Throwable arg0, Method arg1, Object... arg2) {
                log.error("======="+arg0.getMessage()+"========", arg0);
                log.error("exception method:"+arg1.getName());
            }
        };
    }
}
```
&emsp; 重写spring默认线程池的方式，使用时只需要加@Async注解。  

### 1.6.3. 自定义线程池
&emsp; 创建线程池配置类TaskExecutePool.java。使用@Configuration和@EnableAsync这两个注解，表示这是个配置类，并且是线程池的配置类。  
&emsp; <font color = "clime">SpringCloud如果自定义了异步任务的线程池，会导致无法新创建一个 Span，需要使用 Sleuth提供的LazyTraceExecutor来包装下。</font>   

```java
 @Configuration
 @EnableAsync
 public class TaskExecutePool {
     @Autowired
     private TaskThreadPoolConfig config;
     @Bean
     public Executor myTaskAsyncPool() {
         ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
         executor.setCorePoolSize(config.getCorePoolSize()); //核心线程池大小
         executor.setMaxPoolSize(config.getMaxPoolSize()); //最大线程数
         executor.setQueueCapacity(config.getQueueCapacity()); //队列容量
         executor.setKeepAliveSeconds(config.getKeepAliveSeconds()); //活跃时间
         executor.setThreadNamePrefix("MyExecutor-"); //线程名字前缀
         // setRejectedExecutionHandler：当pool已经达到max size的时候，如何处理新任务
         // CallerRunsPolicy：不在新线程中执行任务，而是由调用者所在的线程来执行
         executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
         executor.setWaitForTasksToCompleteOnShutdown(true);// 等待所有任务结束后再关闭线程池
         executor.initialize(); //加载
         return executor;
     }
 }
```
&emsp; 修改启动类，给启动类添加注解。    

```java
@EnableAsync
@EnableConfigurationProperties({TaskThreadPoolConfig.class} ) // 开启配置属性支持
```
&emsp; 使用：  
```java
@Async("myTaskAsyncPool")  //myTaskAsynPool即配置线程池的方法名，此处如果不写自定义线程池的方法名，会使用默认的线程池
```

```java
//通过注解引入配置
@Resource(name = "defaultThreadPool")  
private ThreadPoolTaskExecutor executor;
```