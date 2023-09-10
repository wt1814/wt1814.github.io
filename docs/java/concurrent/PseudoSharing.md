
<!-- TOC -->

- [1. 伪共享](#1-伪共享)
    - [1.1. CPU缓存行读取](#11-cpu缓存行读取)
    - [1.2. 伪共享问题](#12-伪共享问题)
    - [1.3. 伪共享示例](#13-伪共享示例)
    - [1.4. 避免伪共享](#14-避免伪共享)
    - [1.5. 实际应用(LongAdder类)](#15-实际应用longadder类)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. CPU具有多级缓存，越接近CPU的缓存越小也越快；CPU缓存中的数据是以缓存行为单位处理的；CPU缓存行（通常是64字节）能带来免费加载数据的好处，所以处理数组性能非常高。  
2. **CPU缓存行也带来了弊端，多线程处理不相干的变量时会相互影响，也就是伪共享。**  
&emsp; 设想如果有个long类型的变量a，它不是数组的一部分，而是一个单独的变量，并且还有另外一个long类型的变量b紧挨着它，那么当加载a的时候将免费加载b。  
&emsp; 如果一个CPU核心的线程在对a进行修改，a和b都失效了，另一个CPU核心的线程却在对b进行读取。  
3. 避免伪共享的主要思路就是让不相干的变量不要出现在同一个缓存行中；一是在两个long类型的变量之间再加7个long类型(字节填充)；二是创建自己的long类型，而不是用原生的；三是使用java8提供的注解。  
&emsp; 高性能原子类[LongAdder](/docs/java/concurrent/LongAdder.md)可以解决类伪共享问题。    

# 1. 伪共享
<!-- 
https://blog.csdn.net/qq_28119741/article/details/102815659
-->

## 1.1. CPU缓存行读取
&emsp; 缓存是由缓存行组成的，通常是64字节(常用处理器的缓存行是64字节的，比较旧的处理器缓存行是 32 字节)，并且它有效地引用主内存中的一块地址。  
&emsp; <font color = "red">一个Java的long类型是8字节，因此在一个缓存行中可以存8个long类型的变量。</font>  
![image](http://182.92.69.8:8081/img/java/concurrent/multi-51.png)  

## 1.2. 伪共享问题
&emsp; <font color = "red">在程序运行的过程中，缓存每次更新都从主内存中加载连续的64个字节。因此，如果访问一个long类型的数组时，当数组中的一个值被加载到缓存中时，另外7个元素也会被加载到缓存中。</font>  
&emsp; 但是，如果使用的数据结构中的项在内存中不是彼此相邻的，比如链表，那么将得不到免费缓存加载带来的好处。  
&emsp; 不过，这种免费加载也有一个坏处。设想如果有个long类型的变量a，它不是数组的一部分，而是一个单独的变量，并且还有另外一个long类型的变量b紧挨着它，那么当加载a的时候将免费加载b。  
&emsp; 看起来似乎没有什么毛病，但是如果一个CPU核心的线程在对a进行修改，另一个CPU核心的线程却在对b进行读取。  
&emsp; 当前者修改 a 时，会把 a 和 b 同时加载到前者核心的缓存行中，更新完 a 后其它所有包含 a 的缓存行都将失效，因为其它缓存中的 a 不是最新值了。  
&emsp; 而当后者读取 b 时，发现这个缓存行已经失效了，需要从主内存中重新加载。  
&emsp; 请记住，缓存都是以缓存行作为一个单位来处理的，所以失效 a 的缓存的同时，也会把 b 失效，反之亦然。  
![image](http://182.92.69.8:8081/img/java/concurrent/multi-52.png)  
&emsp; 这样就出现了一个问题，<font color = "clime">b和a完全不相干，每次却要因为a的更新需要从主内存重新读取，它被缓存未命中给拖慢了。</font>  
&emsp; 这就是伪共享问题： **<font color = "clime">当多线程修改互相独立的变量时，如果这些变量共享同一个缓存行，就会无意中影响彼此的性能。</font>**  

## 1.3. 伪共享示例  
&emsp; 伪共享示例：  

```java
public class FalseSharingTest {

    public static void main(String[] args) throws InterruptedException {
        testPointer(new Pointer());
    }

    private static void testPointer(Pointer pointer) throws InterruptedException {
        long start = System.currentTimeMillis();
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100000000; i++) {
                pointer.x++;
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 100000000; i++) {
                pointer.y++;
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println(System.currentTimeMillis() - start);
        System.out.println(pointer);
    }
}

class Pointer {
    volatile long x;
    volatile long y;
}
```
&emsp; 这个例子中，声明了一个 Pointer 的类，它包含x和y两个变量，一个线程对 x 进行自增1亿次，一个线程对y进行自增1亿次。  
&emsp; 可以看到，x和y完全没有任何关系，但是更新 x 的时候会把其它包含x的缓存行失效，同时也就失效了y，运行这段程序输出的时间为3890ms。  

## 1.4. 避免伪共享  
&emsp; 伪共享的原理中，一个缓存行是 64 个字节，一个 long 类型是 8 个字节，所以避免伪共享也很简单，笔者总结了下大概有以下三种方式：

1. <font color = "red">字节填充：在两个long类型的变量之间再加7个long类型</font>  
    &emsp; 可以把上面的Pointer改成下面这个结构：

    ```java
    class Pointer {
        volatile long x;
        long p1, p2, p3, p4, p5, p6, p7;
        volatile long y;
    }
    ```
    &emsp; 再次运行程序，会发现输出时间神奇的缩短为了695ms。

2. <font color = "red">重新创建自己的long类型，而不是java自带的long</font>  
    &emsp; 修改Pointer如下：

    ```java
    class Pointer {
        MyLong x = new MyLong();
        MyLong y = new MyLong();
    }

    class MyLong {
        volatile long value;
        long p1, p2, p3, p4, p5, p6, p7;
    }
    ```
    &emsp; 同时把 pointer.x++; 修改为 pointer.x.value++;，把 pointer.y++; 修改为 pointer.y.value++;，再次运行程序发现时间是724ms。  
3. **<font color = "clime">使用@sun.misc.Contended注解(java8)</font>**  
    &emsp; 修改MyLong如下：

    ```java
    @sun.misc.Contended
    class MyLong {
        volatile long value;
    }
    ```
    &emsp; 默认使用这个注解是无效的，需要在JVM启动参数加上-XX:-RestrictContended才会生效，，再次运行程序发现时间是718ms。  

&emsp; 注意，以上三种方式中的前两种是通过加字段的形式实现的，加的字段又没有地方使用，可能会被jvm优化掉，所以建议使用第三种方式。  

## 1.5. 实际应用(LongAdder类)
&emsp; 高性能原子类[LongAdder](/docs/java/concurrent/LongAdder.md)也解决类伪共享问题。  

