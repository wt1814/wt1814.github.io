<!-- TOC -->

- [1. 通信](#1-通信)
    - [1.1. 线程通信](#11-线程通信)
        - [1.1.1. wait() / notify()](#111-wait--notify)
        - [1.1.2. 实现生产者和消费者问题](#112-实现生产者和消费者问题)
            - [1.1.2.1. 仓库类](#1121-仓库类)
            - [1.1.2.2. 生产者](#1122-生产者)
            - [1.1.2.3. 消费者](#1123-消费者)
            - [1.1.2.4. 测试](#1124-测试)
    - [1.2. 进程通信](#12-进程通信)

<!-- /TOC -->

# 1. 通信
## 1.1. 线程通信  
&emsp; 分布式系统中说的两种通信机制：共享内存机制和消息通信机制。单机中实现线程通信的方式：  
1. 等待、通知机制。wait/notify\notifyAll(synchronized同步方法或同步块中使用)实现内存可见性，及生产消费模式的相互唤醒机制；  
2. 等待、通知机制。同步锁(Lock)的Condition(await\signal\signalAll)；  
3. Thread#join()；  
4. 管道，共享内存，实现数据的共享，满足读写模式。管道通信就是使用java.io.PipedInputStream和java.io.PipedOutputStream进行通信。  

&emsp; 生产者消费者问题，Java能实现的几种方法：  

* wait() / notify()方法
* await() / signal()方法
* BlockingQueue阻塞队列方法
* 信号量
* 管道

### 1.1.1. wait() / notify()  
1. wait()、notify/notifyAll() 方法是Object的本地final方法，无法被重写。  
2. wait()使当前线程阻塞，前提是 必须先获得锁，一般配合synchronized 关键字使用，即，一般在synchronized 同步代码块里使用 wait()、notify/notifyAll() 方法。  
3. 由于 wait()、notify/notifyAll() 在synchronized 代码块执行，说明当前线程一定是获取了锁的。  
&emsp; 当线程执行wait()方法时候，会释放当前的锁，然后让出CPU，进入等待状态。  
&emsp; 只有当 notify/notifyAll() 被执行时候，才会唤醒一个或多个正处于等待状态的线程，然后继续往下执行，直到执行完synchronized 代码块的代码或是中途遇到wait() ，再次释放锁。  
&emsp; 也就是说，notify/notifyAll() 的执行只是唤醒沉睡的线程，而不会立即释放锁，锁的释放要看代码块的具体执行情况。所以在编程中，尽量在使用了notify/notifyAll() 后立即退出临界区，以唤醒其他线程让其获得锁。  
4. wait() 需要被try catch包围，以便发生异常中断也可以使wait等待的线程唤醒。
5. notify 和wait 的顺序不能错，如果A线程先执行notify方法，B线程在执行wait方法，那么B线程是无法被唤醒的。
6. notify 和 notifyAll的区别  
&emsp; notify方法只唤醒一个等待(对象的)线程并使该线程开始执行。所以如果有多个线程等待一个对象，这个方法只会唤醒其中一个线程，选择哪个线程取决于操作系统对多线程管理的实现。notifyAll 会唤醒所有等待(对象的)线程，尽管哪一个线程将会第一个处理取决于操作系统的实现。如果当前情况下有多个线程需要被唤醒，推荐使用notifyAll 方法。比如在生产者-消费者里面的使用，每次都需要唤醒所有的消费者或是生产者，以判断程序是否可以继续往下执行。
7. 在多线程中要测试某个条件的变化，使用if 还是while？  
&emsp; 要注意，notify唤醒沉睡的线程后，线程会接着上次的执行继续往下执行。所以在进行条件判断时候，可以先把 wait 语句忽略不计来进行考虑；显然，要确保程序一定要执行，并且要保证程序直到满足一定的条件再执行，要使用while进行等待，直到满足条件才继续往下执行。如下代码：  

    ```java
    public class K {
        //状态锁
        private Object lock;
        //条件变量
        private int now,need;
        public void produce(int num){
            //同步
            synchronized (lock){
            //当前有的不满足需要，进行等待，直到满足条件
                while(now < need){
                    try {
                        //等待阻塞
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("我被唤醒了！");
                }
            // 做其他的事情
            }
        }
    }
    ```
    &emsp; 显然，只有当前值满足需要值的时候，线程才可以往下执行，所以，必须使用while 循环阻塞。注意，wait() 当被唤醒时候，只是让while循环继续往下走。如果此处用if的话，意味着if继续往下走，会跳出if语句块。  

### 1.1.2. 实现生产者和消费者问题   

&emsp; 什么是生产者-消费者问题呢？  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-37.png)  
&emsp; 如上图，假设有一个公共的容量有限的池子，有两种人，一种是生产者，另一种是消费者。需要满足如下条件：  
1. 生产者产生资源往池子里添加，前提是池子没有满，如果池子满了，则生产者暂停生产，直到自己的生成能放下池子。  
2. 消费者消耗池子里的资源，前提是池子的资源不为空，否则消费者暂停消耗，进入等待直到池子里有资源数满足自己的需求。  


#### 1.1.2.1. 仓库类  

```java
import java.util.LinkedList;

/**
 *  生产者和消费者的问题
 *  wait、notify/notifyAll() 实现
 */
public class Storage1 implements AbstractStorage {
    //仓库最大容量
    private final int MAX_SIZE = 100;
    //仓库存储的载体
    private LinkedList list = new LinkedList();

    //生产产品
    public void produce(int num){
        //同步
        synchronized (list){
            //仓库剩余的容量不足以存放即将要生产的数量，暂停生产
            while(list.size()+num > MAX_SIZE){
                System.out.println("【要生产的产品数量】:" + num + "\t【库存量】:"
                        + list.size() + "\t暂时不能执行生产任务!");
                try {
                    //条件不满足，生产阻塞
                    list.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            for(int i=0;i<num;i++){
                list.add(new Object());
            }

            System.out.println("【已经生产产品数】:" + num + "\t【现仓储量为】:" + list.size());

            list.notifyAll();
        }
    }

    //消费产品
    public void consume(int num){
        synchronized (list){

            //不满足消费条件
            while(num > list.size()){
                System.out.println("【要消费的产品数量】:" + num + "\t【库存量】:"
                        + list.size() + "\t暂时不能执行生产任务!");

                try {
                    list.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //消费条件满足，开始消费
            for(int i=0;i<num;i++){
                list.remove();
            }

            System.out.println("【已经消费产品数】:" + num + "\t【现仓储量为】:" + list.size());

            list.notifyAll();
        }
    }
}
```

&emsp; 抽象仓库类  

```java
public interface AbstractStorage {
    void consume(int num);
    void produce(int num);
}
```

#### 1.1.2.2. 生产者  

```java
public class Producer extends Thread{
    //每次生产的数量
    private int num ;

    //所属的仓库
    public AbstractStorage abstractStorage;

    public Producer(AbstractStorage abstractStorage){
        this.abstractStorage = abstractStorage;
    }

    public void setNum(int num){
        this.num = num;
    }

    // 线程run函数
    @Override
    public void run()
    {
        produce(num);
    }

    // 调用仓库Storage的生产函数
    public void produce(int num)
    {
        abstractStorage.produce(num);
    }
}
```

#### 1.1.2.3. 消费者  

```java
public class Consumer extends Thread{
    // 每次消费的产品数量
    private int num;

    // 所在放置的仓库
    private AbstractStorage abstractStorage1;

    // 构造函数，设置仓库
    public Consumer(AbstractStorage abstractStorage1)
    {
        this.abstractStorage1 = abstractStorage1;
    }

    // 线程run函数
    public void run()
    {
        consume(num);
    }

    // 调用仓库Storage的生产函数
    public void consume(int num)
    {
        abstractStorage1.consume(num);
    }

    public void setNum(int num){
        this.num = num;
    }
}
```

#### 1.1.2.4. 测试  

```java
public class Test{
    public static void main(String[] args) {
        // 仓库对象
        AbstractStorage abstractStorage = new Storage1();

        // 生产者对象
        Producer p1 = new Producer(abstractStorage);
        Producer p2 = new Producer(abstractStorage);
        Producer p3 = new Producer(abstractStorage);
        Producer p4 = new Producer(abstractStorage);
        Producer p5 = new Producer(abstractStorage);
        Producer p6 = new Producer(abstractStorage);
        Producer p7 = new Producer(abstractStorage);

        // 消费者对象
        Consumer c1 = new Consumer(abstractStorage);
        Consumer c2 = new Consumer(abstractStorage);
        Consumer c3 = new Consumer(abstractStorage);

        // 设置生产者产品生产数量
        p1.setNum(10);
        p2.setNum(10);
        p3.setNum(10);
        p4.setNum(10);
        p5.setNum(10);
        p6.setNum(10);
        p7.setNum(80);

        // 设置消费者产品消费数量
        c1.setNum(50);
        c2.setNum(20);
        c3.setNum(30);

        // 线程开始执行
        c1.start();
        c2.start();
        c3.start();

        p1.start();
        p2.start();
        p3.start();
        p4.start();
        p5.start();
        p6.start();
        p7.start();
    }
}
```

&emsp; 输出  

```java
【要消费的产品数量】:50    【库存量】:0    暂时不能执行生产任务!
【要消费的产品数量】:20    【库存量】:0    暂时不能执行生产任务!
【要消费的产品数量】:30    【库存量】:0    暂时不能执行生产任务!
【已经生产产品数】:10    【现仓储量为】:10
【要消费的产品数量】:30    【库存量】:10    暂时不能执行生产任务!
【要消费的产品数量】:20    【库存量】:10    暂时不能执行生产任务!
【要消费的产品数量】:50    【库存量】:10    暂时不能执行生产任务!
【已经生产产品数】:10    【现仓储量为】:20
【已经生产产品数】:10    【现仓储量为】:30
【要消费的产品数量】:50    【库存量】:30    暂时不能执行生产任务!
【已经消费产品数】:20    【现仓储量为】:10
【要消费的产品数量】:30    【库存量】:10    暂时不能执行生产任务!
【已经生产产品数】:10    【现仓储量为】:20
【要消费的产品数量】:50    【库存量】:20    暂时不能执行生产任务!
【要消费的产品数量】:30    【库存量】:20    暂时不能执行生产任务!
【已经生产产品数】:10    【现仓储量为】:30
【已经消费产品数】:30    【现仓储量为】:0
【要消费的产品数量】:50    【库存量】:0    暂时不能执行生产任务!
【已经生产产品数】:10    【现仓储量为】:10
【要消费的产品数量】:50    【库存量】:10    暂时不能执行生产任务!
【已经生产产品数】:80    【现仓储量为】:90
【已经消费产品数】:50    【现仓储量为】:40
```

## 1.2. 进程通信  
<!-- 
https://mp.weixin.qq.com/s/mblyh6XrLj1bCwL0Evs-Vg
-->

&emsp; 进程间通信的主要方法有：  
1. 管道(Pipe)：管道是一种半双工的通信方式，数据只能单向流动。管道可用于具有亲缘关系进程间的通信，允许一个进程和另一个与它有共同祖先的进程之间进行通信。   
2. 命名管道(named pipe)：命名管道克服了管道没有名字的限制，因此，除具有管道所具有的功能外，它还允许无亲缘关系进程间的通信。命名管道在文件系统中有对应的文件名。命名管道通过命令mkfifo或系统调用mkfifo来创建。   
3. 信号(Signal)：信号是比较复杂的通信方式，用于通知接受进程有某种事件发生，除了用于进程间通信外，进程还可以发送信号给进程本身；linux除了支持Unix早期信号语义函数sigal外，还支持语义符合Posix.1标准的信号函数sigaction(实际上，该函数是基于BSD的，BSD为了实现可靠信号机制，又能够统一对外接口，用sigaction函数重新实现了signal函数)。Linux中可以使用kill -12 进程号，像当前进程发送信号，但前提是发送信号的进程要注册该信号。   
4. 消息(Message)队列：消息队列是消息的链接表，包括Posix消息队列system V消息队列。有足够权限的进程可以向队列中添加消息，被赋予读权限的进程则可以读走队列中的消息。消息队列克服了信号承载信息量少，管道只能承载无格式字节流以及缓冲区大小受限等缺限。 
5. 共享内存：使得多个进程可以访问同一块内存空间，是最快的可用IPC形式。是针对其他通信机制运行效率较低而设计的。往往与其它通信机制，如信号量结合使用，来达到进程间的同步及互斥。   
6. 内存映射(mapped memory)：内存映射允许任何多个进程间通信，每一个使用该机制的进程通过把一个共享的文件映射到自己的进程地址空间来实现它。 Java中有类 MappedByteBuffer实现内存映射。  
7. 信号量(semaphore)：信号量是一个计数器，可以用来控制多个进程对共享资源的访问。它常作为一种锁机制，防止某进程正在访问共享资源时，其他进程也访问该资源。因此，主要作为进程间以及同一进程内不同线程之间的同步手段。  
8. 套接口(Socket)：更为一般的进程间通信机制，可用于不同机器之间的进程间通信。起初是由Unix系统的BSD分支开发出来的，但现在一般可以移植到其它类Unix系统上：Linux和System V的变种都支持套接字。  




