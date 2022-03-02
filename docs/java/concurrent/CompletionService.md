
<!-- TOC -->

- [1. ~~CompletionService~~](#1-completionservice)
    - [1.1. 使用场景](#11-使用场景)
    - [1.2. 使用示例](#12-使用示例)

<!-- /TOC -->

&emsp; CompletionService，内部通过阻塞队列+FutureTask，实现了任务先完成可优先获取到，即结果按照完成先后顺序排序。  

&emsp; java.util.concurrent.CompletionService是对ExecutorService封装的一个增强类，优化了获取异步操作结果的接口。主要解决了Future阻塞的问题。  

```java
private final Executor executor;
private final AbstractExecutorService aes;
private final BlockingQueue<Future<V>> completionQueue;
```


# 1. ~~CompletionService~~  
<!-- 
https://blog.csdn.net/qq877728715/article/details/114446658


-->

<!-- 
~~
https://blog.csdn.net/zangdaiyang1991/article/details/84333995

https://www.cnblogs.com/zhjh256/p/11829397.html
https://mp.weixin.qq.com/s/Eo-WR1agGETF0hE3Eaivrg

-->

&emsp; ~~JDK 8的CompletionService相对于之前版本的Future而言，其优势是能够尽可能快的得到执行完成的任务。例如有4个并发任务要执行，正常情况下通过Future.get()获取，通常只能按照提交的顺序获得结果，如果最后提交的最先完成的话，总执行时间会长很多。而通过CompletionService能够降低总执行时间。~~  
&emsp; **<font color = "red">JDK 8的CompletionService相对于之前版本的Future而言，其优势是能够尽可能快的得到执行完成的任务。</font>**  
&emsp; **<font color = "red">主线程同步等待结果。</font>**  
&emsp; CompletionService可以看作FutureTask的一个进阶版，通过FutureTask+阻塞队列的方式能够按照线程执行完毕的顺序获取线程执行结果，起到聚合的目的，这个其实跟CountDownLatch差不多，如果需要执行的线程次数是固定的且需要等待执行结果全部返回后统一处理，可以使用CompletionService。  

&emsp; java.util.concurrent.CompletionService 是对 ExecutorService 的一个功能增强封装，优化了获取异步操作结果的接口。  
&emsp; CompletionService接口的功能是以异步的方式一边生产新的任务，一边处理已完成任务的结果，这样就可以将执行任务与处理任务分离开。  

## 1.1. 使用场景
&emsp; 假设我们要向线程池提交一批任务，并获取任务结果。一般的方式是提交任务后，从线程池得到一批 Future 对象集合，然后依次调用其 get() 方法。  
&emsp; 这里有个问题：因为我们会要按固定的顺序来遍历 Future 元素，而 get() 方法又是阻塞的，因此如果某个 Future 对象执行时间太长，会使得遍历过程阻塞在该元素上，无法及时从后面早已完成的 Future 当中取得结果。  
&emsp; CompletionService 解决了这个问题。它本身不包含线程池，创建一个 CompletionService 需要先创建一个 Executor。下面是一个例子：  

```java
ExecutorService executor = Executors.newFixedThreadPool(4);
CompletionService<String> completionService = new ExecutorCompletionService<>(executor);
```

&emsp; 向 CompletionService 提交任务的方式与 ExecutorService 一样：  

```java
completionService.submit(() -> "Hello");
```

&emsp; 当你需要获得结果的时候，就不同了。有了 CompletionService，你不需要再持有 Future 集合。如果要得到最早的执行结果，只需要像下面这样：  

```java
String result = completionService.take().get();
```

&emsp; 这个 take() 方法返回的是最早完成的任务的结果，这个就解决了一个任务被另一个任务阻塞的问题。  


## 1.2. 使用示例  
&emsp; CompletionService 仅有一个实现类 ExecutorCompletionService，需要依赖Executor对象，其大部分实现是使用线程池 ThreadPoolExecutor实现的。  

&emsp; 构造方法有两个  

```java
// Linkedblockingqueue作为任务完成队列
ExecutorCompletionService(Executor executor)    


// 将所提供队列作为任务完成队列
ExecutorCompletionService(Executor executor, BlockingQueue<Future<V>> completionQueue)
```

&emsp; CompletionService 之所以能够做到这点，是因为它没有采取依次遍历 Future 的方式，而是在中间加上了一个结果队列，任务完成后马上将结果放入队列，那么从队列中取到的就是最早完成的结果。  
&emsp; 如果队列为空，那么 take() 方法会阻塞直到队列中出现结果为止。此外 CompletionService 还提供一个 poll() 方法，返回值与 take() 方法一样，不同之处在于它不会阻塞，如果队列为空则立刻返回 null。这算是给用户多一种选择。   

&emsp; 使用实例：  

```java
final SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

CompletionService<String> csv = new ExecutorCompletionService<String>(Executors.newFixedThreadPool(10));

// 此线程池运行5个线程
for (int i = 0; i < 5; i++) {
    final int index = i;
    csv.submit(new Callable<String>() {
        @Override
        public String call() throws Exception {
            System.out.println("Thread-" + index + "-begin-" + sf.format(new Date()));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Thread-" + index + "-end-" + sf.format(new Date()));
            return "index-" + index;
        }

    });
}

try {
    Future<String> f = csv.poll(); // poll方法 返回的Future可能为 null，因为poll 是非阻塞执行的
    if (f != null) {
        System.out.println(f.get());
    } else {
        System.out.println("使用poll 获取到的Future为 null");
    }
} catch (Exception e1) {
    e1.printStackTrace();
}

for (int i = 0; i < 5; i++) {
    try {
        // csv.take() 返回的是 最先完成任务的 Future 对象，take 方法时阻塞执行的
        System.out.println(csv.take().get());
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

&emsp; 运行结果：  

```text
使用poll 获取到的Future为 null
Thread-3-begin-2021-05-09 19:12:51.869
Thread-2-begin-2021-05-09 19:12:51.869
Thread-0-begin-2021-05-09 19:12:51.869
Thread-4-begin-2021-05-09 19:12:51.869
Thread-1-begin-2021-05-09 19:12:51.869
Thread-1-end-2021-05-09 19:12:52.869
Thread-3-end-2021-05-09 19:12:52.869
Thread-0-end-2021-05-09 19:12:52.869
Thread-4-end-2021-05-09 19:12:52.869
Thread-2-end-2021-05-09 19:12:52.869
index-1
index-0
index-3
index-4
index-2
```

&emsp; 这里有两个方法：    
&emsp; take()返回的是 最先完成任务的 Future 对象，take() 方法时阻塞执行的  
&emsp; pool()返回的Future可能为 null，因为poll() 是非阻塞执行的。  