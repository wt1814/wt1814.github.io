<!-- TOC -->

- [1. CompletableFuture<T> JDK1.8](#1-completablefuturet-jdk18)
    - [1.1. ForkJoinPool与CompletableFuture](#11-forkjoinpool与completablefuture)
    - [1.2. CompletableFuture使用](#12-completablefuture使用)

<!-- /TOC -->


# 1. CompletableFuture<T> JDK1.8  
&emsp; CompletableFuture，异步回调。

## 1.1. ForkJoinPool与CompletableFuture  
&emsp; parallelStream和CompletableFuture默认使用的都是 ForkJoinPool.commonPool()默认线程池；  
&emsp; 对集合进行并行计算有两种方式：  

* 转化为并行流，利用map开展工作。  
* 取出每一个元素，创建线程，在CompletableFuture内对其进行操作  

&emsp; 后者提供了更多的灵活性，可以调整线程池的大小，而这能使整体的计算不会因为线程都在等待I/O而发生阻塞。  
&emsp; 那么如何选择呢，建议如下：  

* 进行计算密集型的操作，并且没有I/O，那么推荐使用Stream接口，因为实现简单，同时效率也可能是最高的（如果所有的线程都是计算密集型的，那就没有必要创建比处理器核数更多的线程）。  
* 如果并行操作设计等到I/O的操作（网络连接，请求等），那么使用CompletableFuture灵活性更好，通过控制线程数量来优化程序的运行。  


<!-- 
&emsp; CompletableFuture还提供了了一些非常有用的操作例如，thenApply(),thenCompose(),thenCombine()等。  

* thenApply()是操作完成后将结果传入进行转换
* thenCompose()是对两个异步操作进行串联，第一个操作完成时，对第一个CompletableFuture对象调用thenCompose，并向其传递一个函数。当第一个* CompletableFuture执行完毕后，它的结果将作为该函数的参数，这个函数的返回值是以第一个CompletableFuture的返回做输入计算出第二个CompletableFuture对象。
* thenCombine()会异步执行两个CompletableFuture任务，然后等待它们计算出结果后再进行计算。
-->

## 1.2. CompletableFuture使用  
<!-- 
https://www.cnblogs.com/happyliu/p/9462703.html
-->

1. 创建CompletableFuture对象  
    &emsp; 四个静态方法用来为一段异步执行的代码创建CompletableFuture对象，方法的参数类型都是函数式接口，所以可以使用lambda表达式实现异步任务  

    * runAsync方法：它以Runnabel函数式接口类型为参数，所以CompletableFuture的计算结果为空。  
    * supplyAsync方法以Supplier<U\>函数式接口类型为参数，CompletableFuture的计算结果类型为U。  

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

4. 结合两个CompletionStage的结果，进行转化后返回  

    ```java
    public <U,V> CompletionStage<V> thenCombine(CompletionStage<? extends U> other,BiFunction<? super T,? super U,? extends V> fn);
    public <U,V> CompletionStage<V> thenCombineAsync(CompletionStage<? extends U> other,BiFunction<? super T,? super U,? extends V> fn);
    public <U,V> CompletionStage<V> thenCombineAsync(CompletionStage<? extends U> other,BiFunction<? super T,? super U,? extends V> fn,Executor executor);
    ```
    &emsp; 需要上一阶段的返回值，并且other代表的CompletionStage也要返回值之后，把这两个返回值，进行转换后返回指定类型的值。  
    &emsp; 说明：同样，也存在对两个CompletionStage结果进行消耗的一组方法，例如thenAcceptBoth，这里不再进行示例。  

5. 两个CompletionStage，谁计算的快，就用那个CompletionStage的结果进行下一步的处理  

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

