

<!-- TOC -->

- [1. Dubbo中的时间轮](#1-dubbo中的时间轮)
    - [1.1. 定时任务功能](#11-定时任务功能)
    - [1.2. 时间轮](#12-时间轮)
    - [1.3. Dubbo中时间轮](#13-dubbo中时间轮)
        - [1.3.1. 类图](#131-类图)
        - [1.3.2. HashedWheelTimer](#132-hashedwheeltimer)
        - [1.3.3. 单测](#133-单测)

<!-- /TOC -->

# 1. Dubbo中的时间轮  
<!-- 

定时任务实现原理详解 
https://mp.weixin.qq.com/s/w_26-slajnM57HIbu4b5CA
-->

## 1.1. 定时任务功能  
&emsp; Netty、Quartz、Kafka 以及 Linux 都有定时任务功能。  
&emsp; JDK 自带的 java.util.Timer 和 DelayedQueue 可实现简单的定时任务，底层用的是堆，存取复杂度都是 O(nlog(n))，但无法支撑海量定时任务。  
&emsp; 在任务量大、性能要求高的场景，为了将任务存取及取消操作时间复杂度降为 O(1)，会采用时间轮算法。  
<!-- 
现实开发中有许多的延迟操作，比如定时清理过期数据等，在JDK中自带的Timer或者DelayQueue来实现延迟的功能，但很多开源的中间件中并没有使用Timer或者DelayQueue来实现而是使用基于时间轮算法来实现执行延迟任务功能，例如2.7.0以上的Dubbo基于时间轮实现了，失败定时重发，心跳检测等延迟操作。JDK的Timer和DelayQueue插入和删除操作的平均时间复杂度为O(nlog(n))并不能满足的高性能插入删除的要求，而而基于时间轮可以将插入和删除操作的时间复杂度都降为O(1) 。  
-->
## 1.2. 时间轮  

<!-- 
Timing Wheel 定时轮算法
https://blog.csdn.net/mindfloating/article/details/8033340
-->
&emsp; 时间轮是一种高效利用线程资源进行批量化调度的一种调度模型。把大批量的调度任务全部绑定到同一个调度器上，使用这一个调度器来进行所有任务的管理、触发、以及运行。所以时间轮的模型能够高效管理各种延时任务、周期任务、通知任务。 在工作中遇到类似的功能，可以采用时间轮机制。  
&emsp; 时间轮，从图片上来看，就和手表的表圈是一样，所以称为时间轮，是因为它是以时间作为刻度组成的一个环形队列，这个环形队列采用数组来实现，数组的每个元素称为槽，每个槽可以放一个定时任务列表，叫HashedWheelBucket，它是一个双向链表，量表的每一项表示一个定时任务项（HashedWhellTimeout），其中封装了真正的定时任务TimerTask。时间轮是由多个时间格组成，下图中有8个时间格，每个时间格代表当前时间轮的基本时间跨度（tickDuration），其中时间轮的时间格的个数是固定的。  
&emsp; 在下图中，有8个时间格（槽），假设每个时间格的单位为1s，那么整个时间轮走完一圈需要8s钟。每秒钟指针会沿着顺时针方向移动一格，这个单位可以设置，比如以秒为单位，可以以一小时为单位，这个单位可以代表时间精度。  
&emsp; 通过指针移动，来获得每个时间格中的任务列表，然后遍历这一个时间格中的双向链表来执行任务，以此循环。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-47.png)   
&emsp; **时间轮的运行逻辑：**  
&emsp; 首先，时间轮在启动的时候，会记录一下当前启动时间，并赋值给一个叫startTime的变量。然后当需要添加任务的时候，首先会计算延迟时间(deadline)，比如一个任务的延迟时间是24ms，那么在添加任务时，会将当前时间(currentTime)+24ms-时间轮的启动时间(startTime)，然后把这个任务封装成HashedWheelTimeout加入到链表中。  
&emsp; 那么这个任务应该放在哪个时间格里面呢？ 通过  deadline%wheel.length 计算。时间轮在运行的时候，会从任务队列中取出10W个进行遍历处理。  


## 1.3. Dubbo中时间轮  
<!--

Dubbo源码——时间轮算法
https://wangguoping.blog.csdn.net/article/details/108293948
Dubbo/Netty中时间轮算法的原理
https://blog.csdn.net/dbqb007/article/details/90740839
https://www.cnblogs.com/wuzhenzhao/p/13784469.html
-->
&emsp; 在Dubbo中，为增强系统的容错能力，在很多地方需要用到只需进行一次执行的任务调度。比如RPC调用的超时机制的实现，消费者需要各个RPC调用是否超时，如果超时会将超时结果返回给应用层。在Dubbo最开始的实现中，是采用将所有的返回结果（DefaultFuture）都放入一个集合中，并且通过一个定时任务，每隔一定时间间隔就扫描所有的future，逐个判断是否超时。  
&emsp; 这样的实现方式实现起来比较简单，但是存在一个问题就是会有很多无意义的遍历操作。比如一个RPC调用的超时时间是10秒，而我的超时判定定时任务是2秒执行一次，那么可能会有4次左右无意义的轮询操作。  
&emsp; 为了解决类似的场景中的问题，Dubbo借鉴Netty，引入了时间轮算法，用来对只需要执行一次的任务进行调度。  

&emsp; **定时任务应用**  
&emsp;并不直接用于周期性操作，而是只向时间轮提交执行单次的定时任务，在上一次任务执行完成的时候，调用 newTimeout() 方法再次提交当前任务，这样就会在下个周期执行该任务。即使在任务执行过程中出现了 GC、I/O 阻塞等情况，导致任务延迟或卡住，也不会有同样的任务源源不断地提交进来，导致任务堆积。  

&emsp; **Dubbo 时间轮应用主要在如下方面：**   

* 失败重试， 例如，Provider 向注册中心进行注册失败时的重试操作，或是 Consumer 向注册中心订阅时的失败重试等。  
* 周期性定时任务， 例如，定期发送心跳请求，请求超时的处理，或是网络连接断开后的重连机制。  

### 1.3.1. 类图  
&emsp; dubbo里面涉及到定时任务调度的是HashedWheelTimer。位于org.apache.dubbo.common.timer。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-48.png)   
&emsp; UML类图：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-49.png)   

### 1.3.2. HashedWheelTimer
&emsp; HashedWheelTimer的构造：    
&emsp; 调用createWheel创建一个时间轮，时间轮数组一定是2的幂次方，比如传入的ticksPerWheel=6，那么初始化的wheel长度一定是8，这样是便于时间格的计算。tickDuration，表示时间轮的跨度，代表每个时间格的时间精度，以纳秒的方式来表现。把工作线程Worker封装成WorkerThread，从名字可以知道，它就是最终那个负责干活的线程。  


```java
/**
 * A {@link Timer} optimized for approximated I/O timeout scheduling.
 *
 * <h3>每个滴答耗时  通常 100ms</h3>
 * <p>
 * As described with 'approximated', this timer does not execute the scheduled
 * {@link TimerTask} on time.  {@link HashedWheelTimer}, on every tick, will
 * check if there are any {@link TimerTask}s behind the schedule and execute
 * them.
 * <p>
 * You can increase or decrease the accuracy of the execution timing by
 * specifying smaller or larger tick duration in the constructor.  In most
 * network applications, I/O timeout does not need to be accurate.  Therefore,
 * the default tick duration is 100 milliseconds and you will not need to try
 * different configurations in most cases.
 *
 * <h3>时间轮大小 通常512</h3>
 * <p>
 * {@link HashedWheelTimer} maintains a data structure called 'wheel'.
 * To put simply, a wheel is a hash table of {@link TimerTask}s whose hash
 * function is 'dead line of the task'.  The default number of ticks per wheel
 * (i.e. the size of the wheel) is 512.  You could specify a larger value
 * if you are going to schedule a lot of timeouts.
 *
 * <h3>保持单例模式使用</h3>
 * <p>
 * {@link HashedWheelTimer} creates a new thread whenever it is instantiated and
 * started.  Therefore, you should make sure to create only one instance and
 * share it across your application.  One of the common mistakes, that makes
 * your application unresponsive, is to create a new instance for every connection.
 *
 * <h3>Implementation Details</h3>
 * <p>
 * {@link HashedWheelTimer} is based on
 * <a href="http://cseweb.ucsd.edu/users/varghese/">George Varghese</a> and
 * Tony Lauck's paper,
 * <a href="http://cseweb.ucsd.edu/users/varghese/PAPERS/twheel.ps.Z">'Hashed
 * and Hierarchical Timing Wheels: data structures to efficiently implement a
 * timer facility'</a>.  More comprehensive slides are located
 * <a href="http://www.cse.wustl.edu/~cdgill/courses/cs6874/TimingWheels.ppt">here</a>.
 */
public class HashedWheelTimer implements Timer {

    /**
     * may be in spi?
     */
    public static final String NAME = "hased";

    private static final Logger logger = LoggerFactory.getLogger(HashedWheelTimer.class);

    private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger();
    private static final AtomicBoolean WARNED_TOO_MANY_INSTANCES = new AtomicBoolean();
    private static final int INSTANCE_COUNT_LIMIT = 64;
    private static final AtomicIntegerFieldUpdater<HashedWheelTimer> WORKER_STATE_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(HashedWheelTimer.class, "workerState");
    /** 真正执行定时任务的逻辑封装这个 Runnable 对象中 */
    private final Worker worker = new Worker();
    /** 时间轮内部真正执行定时任务的线程 */
    private final Thread workerThread;

    /** worker状态机 */
    private static final int WORKER_STATE_INIT = 0;
    private static final int WORKER_STATE_STARTED = 1;
    private static final int WORKER_STATE_SHUTDOWN = 2;

    /**
     * 0 - init, 1 - started, 2 - shut down
     */
    @SuppressWarnings({"unused", "FieldMayBeFinal"})
    private volatile int workerState;

    // 每个tick的时间，时间轮精度
    private final long tickDuration;
    // 时间轮桶
    private final HashedWheelBucket[] wheel;
    // 掩码， mask = wheel.length - 1，执行 ticks & mask 便能定位到对应的时钟槽
    private final int mask;
    private final CountDownLatch startTimeInitialized = new CountDownLatch(1);
    /** timeouts 队列用于缓冲外部提交时间轮中的定时任务 */
    private final Queue<HashedWheelTimeout> timeouts = new LinkedBlockingQueue<>();
    /** cancelledTimeouts 队列用于暂存取消的定时任务 */
    private final Queue<HashedWheelTimeout> cancelledTimeouts = new LinkedBlockingQueue<>();
    // 统计待定的Timeouts数量
    private final AtomicLong pendingTimeouts = new AtomicLong(0);
    private final long maxPendingTimeouts;

    /** 当前时间轮的启动时间，提交到该时间轮的定时任务的 deadline 字段值均以该时间戳为起点进行计算 */
    private volatile long startTime;

    public HashedWheelTimer() {
        this(Executors.defaultThreadFactory());
    }
    public HashedWheelTimer(long tickDuration, TimeUnit unit) {
        this(Executors.defaultThreadFactory(), tickDuration, unit);
    }
    public HashedWheelTimer(long tickDuration, TimeUnit unit, int ticksPerWheel) {
        this(Executors.defaultThreadFactory(), tickDuration, unit, ticksPerWheel);
    }
    public HashedWheelTimer(ThreadFactory threadFactory) {
        this(threadFactory, 100, TimeUnit.MILLISECONDS);
    }
    public HashedWheelTimer(ThreadFactory threadFactory, long tickDuration, TimeUnit unit) {
        this(threadFactory, tickDuration, unit, 512);
    }
    public HashedWheelTimer(ThreadFactory threadFactory, long tickDuration, TimeUnit unit, int ticksPerWheel) {
        this(threadFactory, tickDuration, unit, ticksPerWheel, -1);
    }
    public HashedWheelTimer(ThreadFactory threadFactory, long tickDuration, TimeUnit unit, int ticksPerWheel, long maxPendingTimeouts) {

        if (threadFactory == null) {
            throw new NullPointerException("threadFactory");
        }
        if (unit == null) {
            throw new NullPointerException("unit");
        }
        if (tickDuration <= 0) {
            throw new IllegalArgumentException("tickDuration must be greater than 0: " + tickDuration);
        }
        if (ticksPerWheel <= 0) {
            throw new IllegalArgumentException("ticksPerWheel must be greater than 0: " + ticksPerWheel);
        }

        // wheel大小处理为2的指数，并创建时间轮——桶数组
        wheel = createWheel(ticksPerWheel);
        mask = wheel.length - 1;

        // Convert tickDuration to nanos.
        this.tickDuration = unit.toNanos(tickDuration);

        // Prevent overflow.
        if (this.tickDuration >= Long.MAX_VALUE / wheel.length) {
            throw new IllegalArgumentException(String.format(
                    "tickDuration: %d (expected: 0 < tickDuration in nanos < %d",
                    tickDuration, Long.MAX_VALUE / wheel.length));
        }
        workerThread = threadFactory.newThread(worker);

        this.maxPendingTimeouts = maxPendingTimeouts;

        if (INSTANCE_COUNTER.incrementAndGet() > INSTANCE_COUNT_LIMIT &&
                WARNED_TOO_MANY_INSTANCES.compareAndSet(false, true)) {
            reportTooManyInstances();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            super.finalize();
        } finally {
            // This object is going to be GCed and it is assumed the ship has sailed to do a proper shutdown. If
            // we have not yet shutdown then we want to make sure we decrement the active instance count.
            if (WORKER_STATE_UPDATER.getAndSet(this, WORKER_STATE_SHUTDOWN) != WORKER_STATE_SHUTDOWN) {
                INSTANCE_COUNTER.decrementAndGet();
            }
        }
    }

    /**
     * 创建时间轮——桶数组
     * @param ticksPerWheel
     * @return
     */
    private static HashedWheelBucket[] createWheel(int ticksPerWheel) {
        if (ticksPerWheel <= 0) {
            throw new IllegalArgumentException(
                    "ticksPerWheel must be greater than 0: " + ticksPerWheel);
        }
        if (ticksPerWheel > 1073741824) {
            throw new IllegalArgumentException(
                    "ticksPerWheel may not be greater than 2^30: " + ticksPerWheel);
        }

        ticksPerWheel = normalizeTicksPerWheel(ticksPerWheel);
        HashedWheelBucket[] wheel = new HashedWheelBucket[ticksPerWheel];
        for (int i = 0; i < wheel.length; i++) {
            wheel[i] = new HashedWheelBucket();
        }
        return wheel;
    }

    private static int normalizeTicksPerWheel(int ticksPerWheel) {
        int normalizedTicksPerWheel = ticksPerWheel - 1;
        normalizedTicksPerWheel |= normalizedTicksPerWheel >>> 1;
        normalizedTicksPerWheel |= normalizedTicksPerWheel >>> 2;
        normalizedTicksPerWheel |= normalizedTicksPerWheel >>> 4;
        normalizedTicksPerWheel |= normalizedTicksPerWheel >>> 8;
        normalizedTicksPerWheel |= normalizedTicksPerWheel >>> 16;
        return normalizedTicksPerWheel + 1;
    }

    /**
     * 显式启动后台线程
     * 即使未调用此方法，后台线程也将根据需要自动启动。
     * 1. 确定时间轮的 startTime 字段；
     * 2. 启动 workerThread 线程，开始执行 worker 任务
     *
     * @throws IllegalStateException if this timer has been {@linkplain #stop() stopped} already
     */
    public void start() {
        switch (WORKER_STATE_UPDATER.get(this)) {
            case WORKER_STATE_INIT:
                if (WORKER_STATE_UPDATER.compareAndSet(this, WORKER_STATE_INIT, WORKER_STATE_STARTED)) {
                    workerThread.start();
                }
                break;
            case WORKER_STATE_STARTED:
                break;
            case WORKER_STATE_SHUTDOWN:
                throw new IllegalStateException("cannot be started once stopped");
            default:
                throw new Error("Invalid WorkerState");
        }

        // Wait until the startTime is initialized by the worker.
        while (startTime == 0) {
            try {
                startTimeInitialized.await();
            } catch (InterruptedException ignore) {
                // Ignore - it will be ready very soon.
            }
        }
    }

    @Override
    public Set<Timeout> stop() {
        if (Thread.currentThread() == workerThread) {
            throw new IllegalStateException(
                    HashedWheelTimer.class.getSimpleName() +
                            ".stop() cannot be called from " +
                            TimerTask.class.getSimpleName());
        }

        if (!WORKER_STATE_UPDATER.compareAndSet(this, WORKER_STATE_STARTED, WORKER_STATE_SHUTDOWN)) {
            // workerState can be 0 or 2 at this moment - let it always be 2.
            if (WORKER_STATE_UPDATER.getAndSet(this, WORKER_STATE_SHUTDOWN) != WORKER_STATE_SHUTDOWN) {
                INSTANCE_COUNTER.decrementAndGet();
            }

            return Collections.emptySet();
        }

        try {
            boolean interrupted = false;
            while (workerThread.isAlive()) {
                workerThread.interrupt();
                try {
                    workerThread.join(100);
                } catch (InterruptedException ignored) {
                    interrupted = true;
                }
            }

            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        } finally {
            INSTANCE_COUNTER.decrementAndGet();
        }
        // 返回未处理的Timeouts
        return worker.unprocessedTimeouts();
    }

    @Override
    public boolean isStop() {
        return WORKER_STATE_SHUTDOWN == WORKER_STATE_UPDATER.get(this);
    }

    /**
     * 新建Timeout，并加入缓冲队列
     */
    @Override
    public Timeout newTimeout(TimerTask task, long delay, TimeUnit unit) {
        if (task == null) {
            throw new NullPointerException("task");
        }
        if (unit == null) {
            throw new NullPointerException("unit");
        }

        long pendingTimeoutsCount = pendingTimeouts.incrementAndGet();

        // 超过maxPendingTimeouts，直接拒绝
        if (maxPendingTimeouts > 0 && pendingTimeoutsCount > maxPendingTimeouts) {
            pendingTimeouts.decrementAndGet();
            throw new RejectedExecutionException("Number of pending timeouts ("
                    + pendingTimeoutsCount + ") is greater than or equal to maximum allowed pending "
                    + "timeouts (" + maxPendingTimeouts + ")");
        }

        start();

        // 将该timeout添加至timeouts队列中，下一个tick会进行处理
        // During processing all the queued HashedWheelTimeouts will be added to the correct HashedWheelBucket.
        long deadline = System.nanoTime() + unit.toNanos(delay) - startTime;

        // 防止溢出
        if (delay > 0 && deadline < 0) {
            deadline = Long.MAX_VALUE;
        }
        HashedWheelTimeout timeout = new HashedWheelTimeout(this, task, deadline);
        timeouts.add(timeout);
        return timeout;
    }

    /**
     * Returns the number of pending timeouts of this {@link Timer}.
     */
    public long pendingTimeouts() {
        return pendingTimeouts.get();
    }

    private static void reportTooManyInstances() {
        String resourceType = ClassUtils.simpleClassName(HashedWheelTimer.class);
        logger.error("You are creating too many " + resourceType + " instances. " +
                resourceType + " is a shared resource that must be reused across the JVM," +
                "so that only a few instances are created.");
    }
}
```

### 1.3.3. 单测 

```java
public class HashedWheelTimerTest {

    private class PrintTask implements TimerTask {

        @Override
        public void run(Timeout timeout) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            System.out.println("task :" + LocalDateTime.now().format(formatter));
        }
    }

    @Test
    public void newTimeout() throws InterruptedException {
        final Timer timer = newTimer();
        // 每隔1s向时间轮添加任务。定时任务也是1s
        for (int i = 0; i < 10; i++) {
            timer.newTimeout(new PrintTask(), 3, TimeUnit.SECONDS);
            System.out.println("task" + i + "added into the timer");
            Thread.sleep(1000);
        }
        Thread.sleep(5000);
    }

    @Test
    public void stop() throws InterruptedException {
        final Timer timer = newTimer();
        for (int i = 0; i < 10; i++) {
            timer.newTimeout(new PrintTask(), 5, TimeUnit.SECONDS);
            Thread.sleep(100);
        }
        //stop timer
        timer.stop();

        try {
            //this will throw a exception
            timer.newTimeout(new PrintTask(), 5, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Timer newTimer() {
        // 100ms间隔的时间轮
        return new HashedWheelTimer(
                new NamedThreadFactory("dubbo-future-timeout", true),
                100,
                TimeUnit.MILLISECONDS);
    }
}
```
&emsp; 运行结果   
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-50.png)   


