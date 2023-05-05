<!-- TOC -->

- [1. ~~CompletableFuture<T>~~](#1-completablefuturet)
    - [1.1. CompletableFuture简介](#11-completablefuture简介)
    - [1.2. ~~CompletableFuture类分析~~](#12-completablefuture类分析)
    - [1.3. CompletableFuture使用](#13-completablefuture使用)
    - [1.4. CompletableFuture实战](#14-completablefuture实战)
        - [1.4.1. 背景](#141-背景)
        - [1.4.2. 通过CompletableFuture的allOf方法对多个异步执行结果进行处理](#142-通过completablefuture的allof方法对多个异步执行结果进行处理)

<!-- /TOC -->


&emsp; **<font color = "red">总结：</font>**  
&emsp; <font color = "red">从Java 8开始引入了CompletableFuture，它针对Future做了改进，可以传入回调对象，当异步任务完成或者发生异常时，自动调用回调对象的回调方法。</font>  
&emsp; ⚠️注：异步回调，主线程不会阻塞。  
&emsp; CompletableFuture提供了丰富的API对结果进行处理。  

-----------


&emsp; 在Java 8中，新增加了一个包含50个方法左右的类: CompletableFuture，默认依靠fork/join框架启动新的线程实现异步与并发的，提供了非常强大的Future的扩展功能，可以简化异步编程的复杂性，提供了函数式编程的能力，可以通过回调函数的方式处理返回结果，并且提供了转换和组合CompletableFuture的方法。   
&emsp; 主要是为了解决Future模式的缺点：   
1. Future虽然可以实现异步获取线程的执行结果，但是Future没有提供通知机制，调用方无法得知Future什么时候执行完的问题。  
2. 想要获取Future的结果，要么使用阻塞， 在future.get()的地方等待Future返回结果，这时会变成同步操作。要么使用isDone()方法进行轮询，又会耗费无谓的 CPU 资源。  
3. 从 Java 8 开始引入了CompletableFuture，它针对Future做了改进，可以传入回调对象，当异步任务完成或者发生异常时，自动调用回调对象的回调方法。  

&emsp; **CompletionStage介绍：**    
1. `CompletionStage（完成阶段）`  
    &emsp; CompletionStage: 代表异步任务执行过程中的某一个阶段，一个阶段完成以后可能会触发另外一个阶段  
    &emsp; 一个阶段的执行可以是一个Function，Consumer或者Runnable。比如：  
	```java
    stage.thenApply(x -> square(x))
    	 .thenAccept(x -> System.out.print(x))
    	 .thenRun(() -> System.out.println())
	```
    &emsp; 一个阶段的执行可能是被单个阶段的完成触发，也可能是由多个阶段一起触发  

2. CompletionStage接口实现流式编程  
    &emsp; 此接口包含38个方法、这些方法主要是为了支持函数式编程中流式处理。  

# 1. ~~CompletableFuture<T>~~  
<!--
*** CompletableFuture实现原理和使用场景 
https://mp.weixin.qq.com/s/vppkWAE42Rc8MCzOlWeFaw
https://blog.csdn.net/qq877728715/article/details/114446658

异步神器：CompletableFuture实现原理和使用场景
https://mp.weixin.qq.com/s/w5aRflM1rtzrSKNbHWuwiQ
CompletableFuture
https://mp.weixin.qq.com/s/QvStRoJNEhOz_4qR5H8iIA

使用CompletableFuture
https://www.liaoxuefeng.com/wiki/1252599548343744/1306581182447650
利用CompletableFuture优化程序的执行效率
https://www.cnblogs.com/hama1993/p/10534202.html
CompletableFuture基本用法
https://www.cnblogs.com/cjsblog/p/9267163.html


CompletableFuture 组合式异步编程与 并行流
https://blog.csdn.net/moonpure/article/details/81080427


手把手教你线程池配合CompletableFuture实现图片下载并压缩 
https://mp.weixin.qq.com/s/spmqWaS_8Oe-5TQlnksTVw

CompletableFuture详解 
https://mp.weixin.qq.com/s/ZVdAre6YAwCLXSotVDsc_Q
 从 CompletableFuture 到异步编程 
 https://mp.weixin.qq.com/s/PfYh5x1JuU1SSKI3x0_L0Q
异步神器CompletableFuture 
https://mp.weixin.qq.com/s/pQsWfME5QrHhp4xKS4WSrA
异步编程
https://mp.weixin.qq.com/s/gm9ps7YDqxkysCCNNUKFeA

除了串行执行外，多个CompletableFuture还可以并行执行。
https://www.liaoxuefeng.com/wiki/1252599548343744/1306581182447650

https://mp.weixin.qq.com/s/_T9xIYMKNXFLixTmKMD12A

CompletableFuture多线程并发异步编程
https://mp.weixin.qq.com/s/R_-MX85FbaO7VnQedQbc2A

上个礼拜我们线上有个接口比较慢，这个接口在刚开始响应时间是正常的。但随着数据量的增多，响应时间变慢了。

这个接口里面顺序调用了2个服务，且2个服务之间没有数据依赖。我就用CompletableFuture把调用2个服务的过程异步化了一下，响应时间也基本上缩短为原来的一半，问题解决。


&emsp; parallelStream和CompletableFuture默认使用的都是ForkJoinPool.commonPool()默认线程池；  
&emsp; 对集合进行并行计算有两种方式：  

* 转化为并行流，利用map开展工作。  
* 取出每一个元素，创建线程，在CompletableFuture内对其进行操作  

&emsp; 后者提供了更多的灵活性，可以调整线程池的大小，而这能使整体的计算不会因为线程都在等待I/O而发生阻塞。  
&emsp; 那么如何选择呢，建议如下：  

* 进行计算密集型的操作，并且没有I/O，那么推荐使用Stream接口，因为实现简单，同时效率也可能是最高的(如果所有的线程都是计算密集型的，那就没有必要创建比处理器核数更多的线程)。  
* 如果并行操作涉及到I/O的操作(网络连接，请求等)，那么使用CompletableFuture灵活性更好，通过控制线程数量来优化程序的运行。  

说说 CompletableFuture 的实现原理和使用场景？
https://mp.weixin.qq.com/s/UraKyZCsFyPITEujRUgG-Q

-->

&emsp; 为什么引入CompletableFuture?  
&emsp; 对于jdk1.5的Future，虽然提供了异步处理任务的能力，但是获取结果的方式很不优雅，还是需要通过阻塞（或者轮训）的方式。如何避免阻塞呢？其实就是注册回调。  
&emsp; 业界结合观察者模式实现异步回调。也就是当任务执行完成后去通知观察者。比如Netty的ChannelFuture，可以通过注册监听实现异步结果的处理。  


## 1.1. CompletableFuture简介
&emsp; **<font color = "blue">CompletableFuture，组合式异步编程，异步回调。</font>**    
&emsp;  **<font color = "blue">使用Future获得异步执行结果时，要么调用阻塞方法get()，要么轮询看isDone()是否为true，这两种方法都不是很好，因为主线程也会被迫等待。</font><font color = "red">从Java 8开始引入了CompletableFuture，它针对Future做了改进，可以传入回调对象，当异步任务完成或者发生异常时，自动调用回调对象的回调方法。</font>**  

&emsp; **<font color = "clime">使用场景：某个接口顺序调用了多个服务，且多个服务之间没有数据依赖。使用CompletableFuture会使响应时间缩短很多。</font>**    

## 1.2. ~~CompletableFuture类分析~~

```java
public class CompletableFuture<T> implements Future<T>, CompletionStage<T> {

}
```
&emsp; parallelStream和CompletableFuture默认使用的都是ForkJoinPool.commonPool()默认线程池；  

## 1.3. CompletableFuture使用  
<!-- 
&emsp; CompletableFuture还提供了了一些非常有用的操作例如，thenApply(),thenCompose(),thenCombine()等。  

* thenApply()是操作完成后将结果传入进行转换
* thenCompose()是对两个异步操作进行串联，第一个操作完成时，对第一个CompletableFuture对象调用thenCompose，并向其传递一个函数。当第一个* CompletableFuture执行完毕后，它的结果将作为该函数的参数，这个函数的返回值是以第一个CompletableFuture的返回做输入计算出第二个CompletableFuture对象。
* thenCombine()会异步执行两个CompletableFuture任务，然后等待它们计算出结果后再进行计算。
-->
<!-- 
https://www.cnblogs.com/happyliu/p/9462703.html
-->

1. 创建CompletableFuture对象  
    &emsp; 四个静态方法用来为一段异步执行的代码创建CompletableFuture对象，方法的参数类型都是函数式接口，所以可以使用lambda表达式实现异步任务  

    * runAsync方法：它以Runnabel函数式接口类型为参数，所以CompletableFuture的计算结果为空。  
    * <font color = "red">supplyAsync方法以Supplier<U\>函数式接口类型为参数，CompletableFuture的计算结果类型为U。</font>  

    ```java
    public static CompletableFuture<Void> runAsync(Runnable runnable)
    public static CompletableFuture<Void> runAsync(Runnable runnable, Executor executor)
    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier)
    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor)
    ```

2. 变换结果

    ```java
    public <U> CompletionStage<U> thenApply(Function<? super T,? extends U> fn);
    public <U> CompletionStage<U> thenApplyAsync(Function<? super T,? extends U> fn);
    public <U> CompletionStage<U> thenApplyAsync(Function<? super T,? extends U> fn,Executor executor);
    ```
    &emsp; 这些方法的输入是上一个阶段计算后的结果，返回值是经过转化后结果  


3. 消费结果  

    ```java
    public CompletionStage<Void> thenAccept(Consumer<? super T> action);
    public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> action);
    public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> action,Executor executor);
    ```
    &emsp; 这些方法只是针对结果进行消费，入参是Consumer，没有返回值

4. <font color = "red">结合两个CompletionStage的结果，进行转化后返回</font>  

    ```java
    public <U,V> CompletionStage<V> thenCombine(CompletionStage<? extends U> other,BiFunction<? super T,? super U,? extends V> fn);
    public <U,V> CompletionStage<V> thenCombineAsync(CompletionStage<? extends U> other,BiFunction<? super T,? super U,? extends V> fn);
    public <U,V> CompletionStage<V> thenCombineAsync(CompletionStage<? extends U> other,BiFunction<? super T,? super U,? extends V> fn,Executor executor);
    ```
    &emsp; 需要上一阶段的返回值，并且other代表的CompletionStage也要返回值之后，把这两个返回值，进行转换后返回指定类型的值。  
    &emsp; 说明：同样，也存在对两个CompletionStage结果进行消耗的一组方法，例如thenAcceptBoth，这里不再进行示例。  

5. <font color = "red">两个CompletionStage，谁计算的快，就用那个CompletionStage的结果进行下一步的处理</font>  

    ```java
    public <U> CompletionStage<U> applyToEither(CompletionStage<? extends T> other,Function<? super T, U> fn);
    public <U> CompletionStage<U> applyToEitherAsync(CompletionStage<? extends T> other,Function<? super T, U> fn);
    public <U> CompletionStage<U> applyToEitherAsync(CompletionStage<? extends T> other,Function<? super T, U> fn,Executor executor);
    ```
    &emsp; 两种渠道完成同一个事情，就可以调用这个方法，找一个最快的结果进行处理，最终有返回值。  

6. 运行时出现了异常，可以通过exceptionally进行补偿  

    ```java
    public CompletionStage<T> exceptionally(Function<Throwable, ? extends T> fn);
    ```

## 1.4. CompletableFuture实战

### 1.4.1. 背景  
&emsp; app改版，代码重构。  
&emsp; 还记得刚接手这块内容的时候，有个for循环【串行】调用远程，稍微改点东西，就会导致接口超时。  
&emsp; 这次重构，想着优化一下。看调用的对方接口说明：  

```java
@MethodDesc("查询用户在哪个分群，不传分群id则返回所有分群")
List<Integer> queryGroupsByUserId(String var1, List<Integer> var2);
```

&emsp; 敲代码，调试无果。再一次入炕。  
![image](http://www.wt1814.com/static/view/images/share/share-1.png)  


### 1.4.2. 通过CompletableFuture的allOf方法对多个异步执行结果进行处理
&emsp; CompletableFuture和CompletionService的知识，可以查看之前的文章。  

```java
List<Integer> groups= null;

if (list != null && list.size()>0){
    if (list.size() <=20){
        groups= groupServiceApi.queryGroupsByUserId(iid, list);
    }else {
        // 集合拆分
        List<List<Integer>> lists = this.splitList(list, 15);
        // 获取结果
        List<CompletableFuture<List<Integer>>> futures = this.getGroupIds(iid,lists);
        // 多个异步执行结果合并到该集合
        List<Integer> futureUsers = new ArrayList<>();

        // 通过allOf对多个异步执行结果进行处理
        CompletableFuture allFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).whenComplete((v, t) -> {
                // 所有CompletableFuture执行完成后进行遍历
                futures.forEach(future -> {
                    synchronized (this) {
                        // 查询结果合并
                        futureUsers.addAll(future.getNow(null));
                    }
                });
            });
        // 阻塞等待所有CompletableFuture执行完成
        allFuture.get();
        // 对合并后的结果集进行去重处理
        groups = futureUsers.stream().distinct().collect(Collectors.toList());
    }
}
```


```java
/**
    * list拆分
    * @param list
    * @param groupSize
    * @return
    */
private List<List<Integer>> splitList(List<Integer> list , int groupSize){
    int length = list.size();
    // 计算可以分成多少组
    int num = ( length + groupSize - 1 )/groupSize ; // TODO
    List<List<Integer>> newList = new ArrayList<>(num);
    for (int i = 0; i < num; i++) {
        // 开始位置
        int fromIndex = i * groupSize;
        // 结束位置
        int toIndex = (i+1) * groupSize < length ? ( i+1 ) * groupSize : length ;
        newList.add(list.subList(fromIndex,toIndex)) ;
    }
    return  newList ;
}
```

```java
/**
    * 异步获取结果
    * @param iid
    * @param lists
    * @return
    */
private List<CompletableFuture<List<Integer>>> getGroupIds(String iid,List<List<Integer>> lists){

    List<CompletableFuture<List<Integer>>> completableFutures =  new ArrayList<>();
    for (List<Integer> list:lists){

        CompletableFuture<List<Integer>> listCompletableFuture = CompletableFuture.supplyAsync(() -> groupServiceApi.queryGroupsByUserId(iid, list));
        completableFutures.add(listCompletableFuture);

    }
    return completableFutures;

}
```

