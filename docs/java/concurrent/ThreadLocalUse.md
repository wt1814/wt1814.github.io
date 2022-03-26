
<!-- TOC -->

- [1. xxxThreadLocal应用xxx](#1-xxxthreadlocal应用xxx)
    - [1.1. ThreadLocal使用](#11-threadlocal使用)
        - [1.1.1. ※※※正确使用](#111-※※※正确使用)
        - [1.1.2. 线程安全问题](#112-线程安全问题)
            - [1.1.2.1. SimpleDateFormat非线程安全问题](#1121-simpledateformat非线程安全问题)
            - [1.1.2.2. ThreadLocal<DecimalFormat>](#1122-threadlocaldecimalformat)
        - [1.1.3. 业务中变量传递](#113-业务中变量传递)
            - [1.1.3.1. ThreadLocal实现同一线程下多个类之间的数据传递](#1131-threadlocal实现同一线程下多个类之间的数据传递)
            - [1.1.3.2. ThreadLocal实现线程内的缓存，避免重复调用](#1132-threadlocal实现线程内的缓存避免重复调用)
        - [1.1.4. ThreadLocal+MDC实现链路日志增强](#114-threadlocalmdc实现链路日志增强)
    - [1.2. ThreadLocal局限性(变量不具有传递性)](#12-threadlocal局限性变量不具有传递性)
        - [1.2.1. 类InheritableThreadLocal的使用](#121-类inheritablethreadlocal的使用)
    - [1.3. ThreadLocal和线程池](#13-threadlocal和线程池)
        - [1.3.1. 类TransmittableThreadLocal(alibaba)的使用](#131-类transmittablethreadlocalalibaba的使用)
    - [1.4. FastThreadLocal](#14-fastthreadlocal)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. ThreadLocal使用场景：  
    1. 线程安全问题。
    2. 业务中变量传递。1)ThreadLocal实现同一线程下多个类之间的数据传递；2)ThreadLocal实现线程内的缓存，避免重复调用。
    3. ThreadLocal+MDC实现链路日志增强。
    4. ThreadLocal 实现数据库读写分离下强制读主库。
2. ~~ThreadLocal三大坑~~
    1. 内存泄露
    2. ThreadLocal无法在`父子线程（new Thread()）`之间传递。使用类InheritableThreadLocal可以在子线程中取得父线程继承下来的值。   
    3. 线程池中线程上下文丢失。TransmittableThreadLocal是阿里巴巴开源的专门解决InheritableThreadLocal的局限性，实现线程本地变量在线程池的执行过程中，能正常的访问父线程设置的线程变量。  
    4. 并行流中线程上下文丢失。问题同线程池中线程上下文丢失。  
3. ThreadLocal优化：FastThreadLocal


# 1. xxxThreadLocal应用xxx 

<!-- 

&&&& 细数ThreadLocal三大坑，内存泄露仅是小儿科
* 内存泄露
* 线程池中线程上下文丢失
* 并行流中线程上下文丢失
https://mp.weixin.qq.com/s/1YzMHrr26jKl0R_o9l9GiQ

面试官再问你 ThreadLocal，就这样狠狠 “怼” 回去！ 
https://mp.weixin.qq.com/s/9gXSrw6llYy29OPH-rQuxQ

-->

## 1.1. ThreadLocal使用  
![image](http://www.wt1814.com/static/view/images/java/concurrent/multi-54.png)   

&emsp; 常见的ThreadLocal用法主要有两种：
1. 在线程级别传递变量。  
&emsp; 在日常Web开发中会遇到需要把一个参数层层的传递到最内层，然后中间层根本不需要使用这个参数，或者是仅仅在特定的工具类中使用，这样完全没有必要在每一个方法里面都传递这样一个通用的参数。如果有一个办法能够在任何一个类里面想用的时候直接拿来使用就太好了。Java Web项目大部分都是基于Tomcat，每次访问都是一个新的线程，可以使用ThreadLocal，每一个线程都独享一个ThreadLocal，在接收请求的时候set特定内容，在需要的时候get这个值。  
&emsp; 最常见的ThreadLocal使用场景为用来解决数据库连接、Session管理等。  
2. 保证线程安全。  
&emsp; ThreadLocal为解决多线程程序的并发问题提供了一种新的思路。但是ThreadLocal也有局限性，阿里规范中  
![image](http://www.wt1814.com/static/view/images/java/concurrent/multi-19.png)   
&emsp; 每个线程往ThreadLocal中读写数据是线程隔离，互相之间不会影响的，所以ThreadLocal无法解决共享对象的更新问题！  
&emsp; 由于不需要共享信息，自然就不存在竞争问题了，从而保证了某些情况下线程的安全，以及避免了某些情况需要考虑线程安全必须同步带来的性能损失！  

### 1.1.1. ※※※正确使用  
![image](http://www.wt1814.com/static/view/images/java/concurrent/multi-20.png)   

1. **<font color = "red">使用static定义threadLocal变量，是为了确保全局只有一个保存Integer对象的ThreadLocal实例。</font>**  
2. **<font color = "clime">finally语句里调用threadLocal.remove()。</font>**


### 1.1.2. 线程安全问题
#### 1.1.2.1. SimpleDateFormat非线程安全问题  

```java
public class Foo{
    // SimpleDateFormat is not thread-safe, so give one to each thread
    private static final ThreadLocal<SimpleDateFormat> formatter = newThreadLocal<SimpleDateFormat>(){
        @Override
        protected SimpleDateFormat initialValue(){
            return new SimpleDateFormat("yyyyMMdd HHmm");
        }
    };

    public String formatIt(Date date){
        return formatter.get().format(date);
    }
}
```
&emsp; final确保ThreadLocal的实例不可更改，防止被意外改变，导致放入的值和取出来的不一致，另外还能防止ThreadLocal的内存泄漏。  

#### 1.1.2.2. ThreadLocal<DecimalFormat>

```java
private static ThreadLocal<DecimalFormat> df = ThreadLocal.withInitial(()->new DecimalFormat("0.00"));

public static String formatAsPerson(Long one){
    if (null == one){
        return null;
    }
    //亿
    if (one >= 1_0000_0000L){
        return String.format("%s亿",df.get().format(one * 1.00d / 1_0000_0000.00d));
    }
}
```

### 1.1.3. 业务中变量传递
<!-- 
https://blog.csdn.net/u010889990/article/details/115206051
-->

#### 1.1.3.1. ThreadLocal实现同一线程下多个类之间的数据传递  
&emsp; ......

#### 1.1.3.2. ThreadLocal实现线程内的缓存，避免重复调用  
&emsp; ......

### 1.1.4. ThreadLocal+MDC实现链路日志增强  
&emsp; ......


## 1.2. ThreadLocal局限性(变量不具有传递性)  
&emsp; <font color = "red">ThreadLocal无法在父子线程之间传递，</font>示例代码如下：  

```java
public class Service {
    private static ThreadLocal<Integer> requestIdThreadLocal = new ThreadLocal<>();
    public static void main(String[] args) {
        Integer reqId = new Integer(5);
        Service a = new Service();
        a.setRequestId(reqId);
    }

    public void setRequestId(Integer requestId) {
        requestIdThreadLocal.set(requestId);
        doBussiness();
    }

    public void doBussiness() {
        System.out.println("首先打印requestId:" + requestIdThreadLocal.get());
        (new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("子线程启动");
                System.out.println("在子线程中访问requestId:" + requestIdThreadLocal.get());
            }
        })).start();
    }
}
```
&emsp; 运行结果如下：  
![image](http://www.wt1814.com/static/view/images/java/concurrent/multi-25.png)   

### 1.2.1. 类InheritableThreadLocal的使用  
&emsp; 使用类InheritableThreadLocal可以在子线程中取得父线程继承下来的值。  
&emsp; InheritableThreadLocal主要用于子线程创建时，需要自动继承父线程的ThreadLocal变量，实现子线程访问父线程的threadlocal变量。  
&emsp; InheritableThreadLocal继承了ThreadLocal，并重写了childValue、getMap、createMap三个方法。  


## 1.3. ThreadLocal和线程池
&emsp; **ThreadLocal和线程池一起使用？**  
&emsp; ThreadLocal对象的生命周期跟线程的生命周期一样长，那么如果将ThreadLocal对象和线程池一起使用，就可能会遇到这种情况：一个线程的ThreadLocal对象会和其他线程的ThreadLocal对象串掉，一般不建议将两者一起使用。  

### 1.3.1. 类TransmittableThreadLocal(alibaba)的使用  
&emsp; InheritableThreadLocal支持子线程访问在父线程中设置的线程上下文环境的实现，原理是在创建子线程时将父线程中的本地变量值复制到子线程，即复制的时机为创建子线程时。  
&emsp; 但并发、多线程就离不开线程池的使用，因为线程池能够复用线程，减少线程的频繁创建与销毁，如果使用InheritableThreadLocal，那么线程池中的线程拷贝的数据来自于第一个提交任务的外部线程，即后面的外部线程向线程池中提交任务时，子线程访问的本地变量都来源于第一个外部线程，造成线程本地变量混乱。  
&emsp; TransmittableThreadLocal是阿里巴巴开源的专门解决InheritableThreadLocal的局限性，实现线程本地变量在线程池的执行过程中，能正常的访问父线程设置的线程变量。  

## 1.4. FastThreadLocal  
&emsp; Netty对ThreadLocal进行了优化，优化方式是继承了Thread类，实现了自己的FastThreadLocal。FastThreadLocal的吞吐量是jdk的ThreadLocal的3倍左右。 

<!-- 
 FastThreadLocal 是什么鬼？吊打 ThreadLocal 的存在！！ 
 https://mp.weixin.qq.com/s/aItosqUu1aMvWqJ2ZMqy5Q
-->
