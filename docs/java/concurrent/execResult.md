

<!-- TOC -->

- [1. Java获取多线程执行结果方式的归纳与总结](#1-java获取多线程执行结果方式的归纳与总结)
    - [1.1. Thread.join](#11-threadjoin)
    - [1.2. CountDownLatch](#12-countdownlatch)
    - [1.3. Future](#13-future)
        - [1.3.1. Future与FutureTask](#131-future与futuretask)
        - [1.3.2. CompletionService](#132-completionservice)
    - [1.4. 生产者消费者模式](#14-生产者消费者模式)
    - [1.5. 异步回调](#15-异步回调)
        - [1.5.1. 多线程与回调](#151-多线程与回调)
        - [1.5.2. CompletableFuture](#152-completablefuture)
    - [1.6. 总结](#16-总结)

<!-- /TOC -->

# 1. Java获取多线程执行结果方式的归纳与总结  
&emsp; 在日常的项目开发中，我们会经常遇到通过多线程执行程序并需要返回执行结果的场景，下面我们就对获取多线程返回结果的几种方式进行一下归纳，并进行简要的分析与总结。  

## 1.1. Thread.join
&emsp; 在一些简单的应用场景中我们可以使用线程本身提供的join方法，我们知道join方法的目的是让一个线程等待另一个线程结束后才能执行，利用此原理我们可以设置一个监控线程用来等待程序线程执行完毕后输出返回结果，下面我们看下具体示例代码  

&emsp; 首先定义一个结果实体类  

```java
public class Result {
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
```
&emsp; 定义工作线程，模拟程序执行并输出线程执行结果  

```java
public class WorkThread extends Thread {

    private Result result ;

    public void init(Result result) {
        this.result = result;
    }

    public void run() {
        try {
            Thread.sleep(1000*10);//模拟程序执行
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        result.setValue("线程执行完毕，输出结果");
    }

}
```
&emsp; 主线程等待工作线程执行，并获取工作线程的执行成果  

```java
public class MainThread {
    public static void main(String[] args) throws InterruptedException {
        Result result = new Result();
        WorkThread workThread = new WorkThread();
        workThread.init(result);
        System.out.println("线程启动");
        workThread.start();
        System.out.println("线程等待");
        // 等待work线程运行完再继续运行
        workThread.join();
        System.out.println("线程执行结果："+result.getValue());
    }
}
```
&emsp; 输出结果  

```
线程启动
线程等待
线程执行结果：线程执行完毕，输出结果
```
&emsp; 以上代码通过Thread.join的方式，模拟了一个最基本的获取线程执行结果场景，采用Thread.join的方式虽然使用方便，但这种原生的方式只适用于一些简单的应用场景中，其主要存在以下问题：  

&emsp; 1、获取多个线程返回结果时较为繁琐，需要自己手动实现；  
&emsp; 2、与线程池无法配合使用；  
&emsp; 3、工作线程内部执行复杂度与耗时不确定，程序需要额外完善；  
&emsp; 4、本质上还是同步返回结果，主线程阻塞；  

## 1.2. CountDownLatch
&emsp; CountDownLatch做为jdk提供的多线程同步工具，CountDownLatch其实本质上可以看做一个线程计数器，统计多个线程执行完成的情况，适用于控制一个或多个线程等待，直到所有线程都执行完毕的场景，因此我们可以利用其功能特点实现获取多个线程的执行结果，一定程度上弥补了Thread.join的不足，代码示例如下：  

&emsp; 工作线程  

```java
public class WorkThread extends Thread {
    private Vector<Result> vectors ;

     private CountDownLatch countDownLatch;

    public WorkThread(CountDownLatch countDownLatch) {
        this.countDownLatch=countDownLatch;
    }

    public void init(Vector<Result> vectors) {
        this.vectors = vectors;
    }

    public void run() {
        try {
            Thread.sleep(1000*3);//模拟程序执行
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Result result = new Result();
        result.setValue(Thread.currentThread().getName()+"线程执行完毕，输出结果");
        vectors.add(result);//结果放入Vector中
        countDownLatch.countDown();
    }
}
```
&emsp; 主线程  

```java
public class MainThread {

    public static void main(String[] args) throws InterruptedException {
        Vector<Result> vectors = new Vector<Result>();//定义一个Vector做为存储返回结果的容器；
        final CountDownLatch countDownLatch = new CountDownLatch(5);
        // 启动多个工作线程
        for (int i = 0; i < 5; i++) {
            WorkThread workThread = new WorkThread(countDownLatch);
            workThread.init(vectors);
            workThread.start();
        }
        System.out.println("主线程等待工作线程执行");
        countDownLatch.await();
        for (int i=0; i<vectors.size(); i++) {
            System.out.println(vectors.get(i).getValue());        
        }

    }

}
```
&emsp; 输出结果  
```
主线程等待工作线程执行
Thread-0线程执行完毕，输出结果
Thread-1线程执行完毕，输出结果
Thread-2线程执行完毕，输出结果
Thread-4线程执行完毕，输出结果
Thread-3线程执行完毕，输出结果
```
&emsp; 通过利用jdk的多线程工具类CountDownLatch，我们可以等待多个线程执行完毕后获取结果，但这种方式局限性较多，如果你的应用场景中启动的线程次数是固定的且需要等待执行结果全部返回后统一处理，使用CountDownLatch是一个不错的选择。  

## 1.3. Future
### 1.3.1. Future与FutureTask
&emsp; 使用Future，包括 FutureTask、CompletionService、CompletableFuture等   
&emsp; 首先我们使用Future配合线程池，获取线程池执行线程的返回结果  
&emsp; 定义一个实现Callable接口的工作线程  

```java
public class WorkThread implements Callable<Result> {

    public Result call() throws Exception {
        Thread.sleep(5000);
        Result result = new Result();
        result.setValue(Thread.currentThread().getName()+"线程执行完毕，输出结果");
        return result;
    }
}
```

&emsp; 主线程  

```java
public class MainThread {
     public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
         ExecutorService taskPool = new ThreadPoolExecutor(5, 15, 1000, TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<>(10), new ThreadPoolExecutor.CallerRunsPolicy());
         Future<Result> future = taskPool.submit(new WorkThread());
         System.out.println("线程池执行工作线程");
         Result result = future.get();//注意这里get操作是阻塞，future仍属于同步返回，主线程需要阻塞等待结果返回
         //result = future.get(3,TimeUnit.SECONDS);//设置阻塞超时时间
         System.out.println(result.getValue());
     }
}
```
&emsp; Future与FutureTask实现方式基本类似，FutureTask其实是对Futue的进一步封装，通过上面的代码我们可以看到Future能够配合ExecutorService 线程池来获取线程执行的结果，使用起来也较为方便，同时可以设置获取结果的超时时间，避免长时间阻塞带来的问题，基本上能够满足大部分应用场景下的要求， 但Future获取结果的get方法是阻塞，本质上是个同步返回，如果希望获取结果所在线程不阻塞，需要引入其他模式相互配合，这个我们下面会说到。  

### 1.3.2. CompletionService
&emsp; CompletionService可以看作FutureTask的一个进阶版，通过FutureTask+阻塞队列的方式能够按照线程执行完毕的顺序获取线程执行结果，起到聚合的目的，这个其实跟CountDownLatch差不多，如果你需要执行的线程次数是固定的且需要等待执行结果全部返回后统一处理，可以使用CompletionService，下面我们通过示例代码进行演示  
&emsp; 同上先实现一个工作线程，这次我们为了能体现出结果输出的顺序，在工作线程内部定义一个编号，编号为偶数的线程阻塞一定时间  

```java
public class WorkThread implements Callable<Result>{
    int num;//线程编号

    public WorkThread(int num) {
        this.num=num;
    }

    public Result call() throws InterruptedException {
        int count = num;
        if(count%2==0) {//编号为偶数的线程阻塞3秒钟
            Thread.sleep(3*1000);
        }
        Result result = new Result();
        result.setValue(num+"号线程执行完毕，输出结果");
        return result;
    }
}
```

&emsp; 主线程中启动十个线程  

```java
public static void main(String[] args) throws InterruptedException, ExecutionException {

        ExecutorService exec = new ThreadPoolExecutor(10, 20, 1000, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(5), Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy());

        //定义一个阻塞队列
        BlockingQueue<Future<Result>> futureQueue =    new LinkedBlockingQueue<Future<Result>>();

        //传入ExecutorService与阻塞队列，构造一个completionService
        CompletionService<Result> completionService = new ExecutorCompletionService<Result>(
                exec,futureQueue);

        for(int i=0;i<10;i++) {
            completionService.submit(new WorkThread(i));
        }

        for(int i=0;i<10;i++) {
            Result res = completionService.take().get();//注意阻塞队列take操作，如果获取不到数据时处于阻塞状态的
            System.out.println(new Date()+ "--"+res.getValue());
        }
    }
}
```
&emsp; 输出结果如下，可以看到奇数编号的线程结果优先返回，偶数编号的线程由于阻塞3秒后才输出返回结果，符合程序预期；  

```
Sun Apr 11 18:38:46 CST 2021--3号线程执行完毕，输出结果
Sun Apr 11 18:38:46 CST 2021--1号线程执行完毕，输出结果
Sun Apr 11 18:38:46 CST 2021--7号线程执行完毕，输出结果
Sun Apr 11 18:38:46 CST 2021--9号线程执行完毕，输出结果
Sun Apr 11 18:38:46 CST 2021--5号线程执行完毕，输出结果
Sun Apr 11 18:38:49 CST 2021--2号线程执行完毕，输出结果
Sun Apr 11 18:38:49 CST 2021--4号线程执行完毕，输出结果
Sun Apr 11 18:38:49 CST 2021--0号线程执行完毕，输出结果
Sun Apr 11 18:38:49 CST 2021--8号线程执行完毕，输出结果
Sun Apr 11 18:38:49 CST 2021--6号线程执行完毕，输出结果
```
&emsp; 上面主线程代码中的completionService.take().get()操作，当获取不到数据也就是当偶数编号线程休眠时仍然会产生阻塞， 其实我们只要对上面代码进行稍微改造就能避免主线程的阻塞，这也就引出了我们下面要说的生产者与消费者模式；  

## 1.4. 生产者消费者模式
&emsp; 上面我们列举的几种获取多线程执行结果的方式，都是通过不同技术方法来实现的，而生产者消费者模式本身跟你运用的技术实现没有太多关系，接触过多线程开发的同学应该都有所了解；  

&emsp; 生产者消费者模式如下图所示  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/concurrent/multi-83.png)  
&emsp; 生产者消费者模式是一种能够解耦与同步生产线程、消费线程、数据集合的多线程设计模式，一个或一组生产者线程负责向数据队列中生产数据，也就是线程执行结果；另外一个或一组消费者线程负责消费处理数据队列中的数据，生产者线程与消费者线程相互之间并没有直接的关联，数据的交互都是通过数据队列，通过这种模式能够很好的在一定程度上解决多线程开发中存在线程同步与安全的问题，同时程序也会看起来更加清晰与方便理解；  
&emsp; 当然一个完善的生产者消费者模式我们需要考虑很多其他方面，但最关键的还是以下两个要素：  

&emsp; 1、线程安全，生产者与消费者分别执行读写操作，特别是在多个生产线程与消费线程时，一定会存在数据读写的并发操作，所以数据队列一定要保证线程安全；  
&emsp; 2、生产与消费的协调，数据队列满时生产线程是否停止写入，数据队列空时消费线程是否停止消费，这里一方面需要结合你的应用场景，同时也是需要考虑不必要的性能浪费；  

&emsp; 下面看下基本的代码实现  
&emsp; 首先定义一个全局的数据队列，这里我用的JDK提供的阻塞队列ArrayBlockingQueue，这里同样也直接可以上面讲到的completionService，当然也可以用其他线程安全的数据集合或者自己定义实现，但要注意无论使用哪种都要注意上面的两个关键要素，平常使用中JDK封装的阻塞队列已经基本满足要求；  

```java
public class Container {
    public static ArrayBlockingQueue<Result> arrayBlockingQueue = new ArrayBlockingQueue<>(100);//这里最好根据系统负载量评估一个阈值，避免OOM问题
}
```
&emsp; 生产者线程实现，队列数据插入时是采用put还是offer结合应用场景调整  

```java
public class ProducerThread extends Thread {

    public void run() {    
        try {
            Thread.sleep(1000*3);//模拟程序执行
            Result result = new Result();
            result.setValue(Thread.currentThread().getName()+"线程执行完毕，输出结果");
            Container.arrayBlockingQueue.put(result);//超过阻塞队列最大阈值时阻塞，一直阻塞
//            if(!Container.arrayBlockingQueue.offer(result, 5, TimeUnit.SECONDS)) {//规定时间内数据入队失败
//                System.err.println("数据入队失败");
//            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
```
&emsp; 消费者线程实现，消费者线程是常驻线程，队列中没有数据时就线程阻塞    

```java
public class ConsumerThread extends Thread {

    public void run() {
         while (!this.isInterrupted()) {
             try {
                Result result = Container.arrayBlockingQueue.take();//有数据就消费，没有就阻塞等待
                System.out.println(result.getValue());
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
         }
    }
}
```
&emsp; 主线程中同时启动生产线程与消费线程   

```java
public class MainThread  {
    public static void main(String[] args) {

        ExecutorService exec = new ThreadPoolExecutor(10, 20, 1000, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(5), Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy());

        for(int i=0;i<100;i++) {//使用线程池模拟生成者生产数据
            exec.execute(new ProducerThread());
        }

        for(int i=0;i<2;i++) {//启动两个消费者线程
            new ConsumerThread().start();
        }
    }
}
```
&emsp; 消费者线程中会轮询获取生产者线程执行并放到阻塞队列中的结果  

```
pool-1-thread-13线程执行完毕，输出结果
pool-1-thread-2线程执行完毕，输出结果
pool-1-thread-1线程执行完毕，输出结果
pool-1-thread-10线程执行完毕，输出结果
pool-1-thread-9线程执行完毕，输出结果
pool-1-thread-15线程执行完毕，输出结果
pool-1-thread-4线程执行完毕，输出结果
pool-1-thread-5线程执行完毕，输出结果
pool-1-thread-8线程执行完毕，输出结果
pool-1-thread-12线程执行完毕，输出结果
pool-1-thread-16线程执行完毕，输出结果
.....................................................
.....................................................
```
&emsp; 生产者消费者模式是程序开发当中一种十分常见且易于理解与掌握的开发设计模式，且适用场景广泛，希望大家都能够深入理解与掌握  

## 1.5. 异步回调
&emsp; **<font color = "red">上面列举的获取线程执行结果的方法都存在一个共性的问题，就是在等待结果的返回过程中，主线程或者消费者线程都是需要阻塞或轮询等待的，</font>** 但在一些应用场景下我们是希望线程执行的过程中，程序该干嘛干嘛，继续向下执行，等到结果返回了再通过回调来通知，这就是异步回调的必要性。 **<font color = "red">实现异步回调思路我这里列举两种，一种是多线程与回调，第二种JDK1.8中新加入了一个实现类CompletableFuture，通过这两种都能够实现异步获取线程执行结果的目标。</font>**  

### 1.5.1. 多线程与回调
&emsp; 这里其实是在多线程中通过回调的方式把结果返回的方式，我们看下具体实现  
&emsp; 首先声明一个回调接口  

```java
public interface CallBack {

    void notice(Result result);

}
```
&emsp; 定义工作线程，在构造函数中传入回调接口的实现对象

```java
public class WorkThread implements Runnable{
    int num;//线程编号

    CallBack callBack;

    public WorkThread(CallBack callBack, int num) {
        this.num=num;
        this.callBack = callBack;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        try {
            Thread.sleep((10-num)*1000);//模拟程序运行时间，倒序输出
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Result result = new Result();
        result.setValue(num+"号线程执行完毕，输出结果");
        callBack.notice(result);
    }
}
```
&emsp; 调用方及回调方法具体实现  

```java
public class MainThread implements CallBack {

    public void run(int num) {
         WorkThread workThread =  new WorkThread(this,num);
         new Thread(workThread).start();
    }

    @Override
    public void notice(Result result) {
        System.out.println("返回结果："+result.getValue());    
    }

}
```
&emsp; 程序执行及输出  

```java
public class App {
     public static void main(String[] args) {
         MainThread mainThread = new MainThread();
         for(int i=0;i<10;i++) {
             mainThread.run(i);
         }
         System.out.println("继续执行，表示异步操作");
     }
}
```
&emsp; 输出结果  

```
继续执行，表示异步操作
返回结果：9号线程执行完毕，输出结果
返回结果：8号线程执行完毕，输出结果
返回结果：7号线程执行完毕，输出结果
返回结果：6号线程执行完毕，输出结果
返回结果：5号线程执行完毕，输出结果
返回结果：4号线程执行完毕，输出结果
返回结果：3号线程执行完毕，输出结果
返回结果：2号线程执行完毕，输出结果
返回结果：1号线程执行完毕，输出结果
返回结果：0号线程执行完毕，输出结果
```
&emsp; 多线程+回调也是一种常见的异步回调实现方式，但需要注意的是我们自己手动实现异步回调时还是有很多细节需要考虑完善的，如异常、超时、线程开辟与管理等，这里就不再过多的展开。  

### 1.5.2. CompletableFuture
&emsp; JDK1.8中新增的CompletableFuture中通过函数式的编程方法提供了等同于异步回调的能力，下面我们进行具体实现  

&emsp; 工作线程  

```java
public class WorkThread {

    public static Result call(int num) throws InterruptedException  {
        Thread.sleep(5*1000);//模拟程序执行时间    
        Result result = new Result();
        result.setValue(String.valueOf(num));
        return result;
    }
}
```
&emsp; 主线程  

```java
public class MainThread {

     public static void main(String[] args) {
            List<String> reslist = new ArrayList<String>();
            ExecutorService exs = Executors.newFixedThreadPool(10);//定义一个线程池
            List<CompletableFuture<Result>> futureList = new ArrayList<>();
            try {
                for(int i=0;i<10;i++) {
                    final int k = i;
                    CompletableFuture<Result> future=CompletableFuture.supplyAsync(()->{
                        try {
                            return WorkThread.call(k);
                        } catch (InterruptedException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                        return null;
                    },exs).thenApply(e->mul(e)).whenComplete((v, e) -> {//thenApply 里面执行就是回调函数CallBack
                                    System.out.println("线程"+k+"完成! 结果："+v.getValue()+"，异常 ："+e+","+new Date());
                                    reslist.add(v.getValue());
                                });

                    futureList.add(future);//聚合返回结果
                }
                System.out.println("继续执行，表示异步操作");


            }catch (Exception e) {
                // TODO: handle exception
            }
        }


        public static Result mul(Result result){
            try {
                Integer val = Integer.valueOf(result.getValue())*2;
                result.setValue(val.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }
}
```
&emsp; 输出结果如下，可以看到主线程没有等待线程执行结果返回，继续向下执行  

```
直接输出，标识异步操作
线程9完成! 结果：18，异常 ：null,Sun Apr 18 17:27:29 CST 2021
线程2完成! 结果：4，异常 ：null,Sun Apr 18 17:27:29 CST 2021
线程5完成! 结果：10，异常 ：null,Sun Apr 18 17:27:29 CST 2021
线程1完成! 结果：2，异常 ：null,Sun Apr 18 17:27:29 CST 2021
线程6完成! 结果：12，异常 ：null,Sun Apr 18 17:27:29 CST 2021
线程3完成! 结果：6，异常 ：null,Sun Apr 18 17:27:29 CST 2021
线程0完成! 结果：0，异常 ：null,Sun Apr 18 17:27:29 CST 2021
线程4完成! 结果：8，异常 ：null,Sun Apr 18 17:27:29 CST 2021
线程8完成! 结果：16，异常 ：null,Sun Apr 18 17:27:29 CST 2021
线程7完成! 结果：14，异常 ：null,Sun Apr 18 17:27:29 CST 2021
```
&emsp; CompletableFuture中提供了丰富的API实现，提供了诸如聚合计算等一整套功能，这里就不做太多表述，有兴趣的小伙伴可以去多做了解。  

## 1.6. 总结
&emsp; 以上就是针对如何获取多线程执行结果进行的方法汇总与简要分析，虽然方法手段多样，但本质上都还是围绕线程同步、数据共享、异步回调等几个思路来进行实现的。在实际的日常开发中的应用，大家还是要结合业务场景具体问题具体分析，一方面固然是要注意程序性能的高效与实现方式的优雅，但另一方面也要注意避免简单的问题复杂化，反而过犹不及。特别是在多线程的开发过程中，多线程的使用并不就等同于处理效率的提高，要不断的深入学习与理解，结合应用场景，多分析，多总结，在实践与积累的过程中逐步提高，真正领会。在此我也希望与大家相互勉励，共同进步。希望本文对大家能有所帮助，其中如有不足与不正确的地方还望指正与海涵，十分感谢。  

