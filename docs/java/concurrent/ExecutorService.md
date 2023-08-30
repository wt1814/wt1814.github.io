
<!-- TOC -->

- [1. 线程池执行，ExecutorService的API](#1-线程池执行executorservice的api)
    - [1.1. execute()，提交不需要返回值的任务](#11-execute提交不需要返回值的任务)
    - [1.2. submit()，提交需要返回值的任务](#12-submit提交需要返回值的任务)
    - [1.3. 线程池正确用法](#13-线程池正确用法)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. 线程池执行，ExecutorService的API：execute()，提交不需要返回值的任务；`submit()，提交需要返回值的任务，返回值类型是Future`。    


# 1. 线程池执行，ExecutorService的API  
![image](http://182.92.69.8:8081/img/java/concurrent/pool-14.png)   

## 1.1. execute()，提交不需要返回值的任务  
&emsp; void execute(Runnable command); execute()的参数是一个Runnable，也没有返回值。因此提交后无法判断该任务是否被线程池执行成功。  

```java
ExecutorService executor = Executors.newCachedThreadPool();
executor.execute(new Runnable() {
    @Override
    public void run() {
        //do something
    }
});
```

## 1.2. submit()，提交需要返回值的任务  
```java
<T> Future<T> submit(Callable<T> task);  
<T> Future<T> submit(Runnable task, T result);  
Future<?> smit(Runnable task);
```
&emsp; submit()有三种重载，参数可以是Callable也可以是Runnable。同时它会返回一个Funture对象，通过它可以判断任务是否执行成功。获得执行结果调用Future.get()方法，这个方法会阻塞当前线程直到任务完成。  

```java
//提交一个Callable任务时，需要使用FutureTask包一层
FutureTask futureTask = new FutureTask(new Callable<String>(){ //创建Callable任务
    @Override
    public String call() throws Exception {
    String result = "";
    //do something
    return result;
    }
});
Future<?> submit = executor.submit(futureTask); //提交到线程池
try{
    Object result = submit.get();//获取结果
}catch(InterruptedException e) {
    e.printStackTrace();
}catch(ExecutionException e) {
    e.printStackTrace();
}
```

## 1.3. 线程池正确用法
&emsp; [线程池的正确使用](/docs/java/concurrent/ThreadPoolUse.md)  
