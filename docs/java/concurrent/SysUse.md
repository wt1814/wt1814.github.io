

<!-- TOC -->

- [1. ~~Synchronized使用~~](#1-synchronized使用)
    - [类、对象、实例、方法](#类对象实例方法)
    - [1.1. Synchronized使用简介](#11-synchronized使用简介)
    - [1.2. Synchronized同步方法](#12-synchronized同步方法)
        - [1.2.1. Synchronized同步普通方法](#121-synchronized同步普通方法)
        - [1.2.2. Synchronized同步静态方法](#122-synchronized同步静态方法)
    - [1.3. Synchronized同步语句块](#13-synchronized同步语句块)
        - [1.3.1. 同步this实例](#131-同步this实例)
        - [1.3.2. 同步对象实例](#132-同步对象实例)
        - [1.3.3. 同步类](#133-同步类)
    - [1.4. String锁](#14-string锁)
    - [1.5. 类锁和对象锁](#15-类锁和对象锁)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. `类锁：当Synchronized修饰静态方法或Synchronized修饰代码块传入某个class对象（Synchronized (XXXX.class)）时被称为类锁。`
2. `对象锁：当Synchronized修饰非静态方法或Synchronized修饰代码块时传入非class对象（Synchronized this）时被称为对象锁。`


# 1. ~~Synchronized使用~~  
<!--
https://www.cnblogs.com/huansky/p/8869888.html
https://juejin.cn/post/6844903920913874952
synchronized加锁this和class的区别！
https://mp.weixin.qq.com/s/ewXpaeMYNx7FAW_fYoQbPg


3. 对象锁  
在 Java 中，每个对象都会有一个 monitor 对象，这个对象其实就是 Java 对象的锁，通常会被称为“内置锁”或“对象锁”。类的对象可以有多个，所以每个对象有其独立的对象锁，互不干扰。  

3. 类锁  
在 Java 中，针对每个类也有一个锁，可以称为“类锁”，类锁实际上是通过对象锁实现的，即类的 Class 对象锁。每个类只有一个 Class 对象，所以每个类只有一个类锁。 



类锁和上面的对象锁唯一不同的区别是，类锁只有一把，无论你创建多少实例对象，它们都公用一把锁。而对象锁你可以动态的使用不同的锁，如果你能确保所有的同步都用同一个对象锁，那么对象锁也能实现类锁的功能。


线程间同时访问同一个锁的多个同步代码的执行顺序不定
当一个线程进入同步方法时，其他线程可以正常访问其他非同步方法
多个对象多个锁不会存在阻塞，多个对象一个锁会存在线程阻塞


3、对于静态方法，由于此时对象还未生成，所以只能采用类锁；

3、只要采用类锁，就会拦截所有线程，只能让一个线程访问。

3、对于对象锁（this），如果是同一个实例，就会按顺序访问，但是如果是不同实例，就可以同时访问。

4、如果对象锁跟访问的对象没有关系，那么就会都同时访问。
--> 


## 类、对象、实例、方法
* 类和对象
    * xxx.Class
    * 类名 对象名
    * 实例化：new 类名();
* 方法
    * 普通方法
    * 静态/类 方法

## 1.1. Synchronized使用简介
![image](http://www.wt1814.com/static/view/images/java/concurrent/multi-11.png)  
![image](http://www.wt1814.com/static/view/images/java/concurrent/multi-84.png)  
&emsp; Synchronized可以使用在普通方法、静态方法、同步块中。 **<font color = "clime">Synchronized作用的对象应该是唯一的。</font>** Synchronized使用在同步块中，锁粒度更小。根据锁的具体实例，又可以分为类锁和对象锁。  
&emsp; 关键字Synchronized取得的锁都是对象锁，而不是把一段代码或方法(函数)当作锁。  
<!-- 
锁非this对象具有一定的优点：如果在一个类中有很多个Synchronized方法，这时虽然能实现同步，但会受到阻塞，所以影响运行效率；但如果使用同步代码块锁非this对象，则Synchronized(非this)代码块中的程序与同步方法是异步的，不与其他锁this同步方法争抢this锁，则可 大大提高运行效率。  
在大多数的情况下，同步Synchronized代码块都不使用String作为锁对象，而改用其他，比如new Object()实例化一个 Object对象，但它并不放人缓存中。  
-->

## 1.2. Synchronized同步方法
### 1.2.1. Synchronized同步普通方法  
&emsp; 这种方法使用虽然最简单，但是只能作用在单例上面，如果不是单例，同步方法锁将失效。  

```java
/**
 * 用在普通方法
 */
private Synchronized void SynchronizedMethod() {
    System.out.println("SynchronizedMethod");
    try {
        Thread.sleep(2000);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}
```
&emsp; 此时，同一个实例只有一个线程能获取锁进入这个方法。  
&emsp; 对于普通同步方法，锁是当前实例对象，进入同步代码前要获得当前实例的锁。  
&emsp; 当两个线程同时对一个对象的一个方法进行操作，只有一个线程能够抢到锁。因为一个对象只有一把锁，一个线程获取了该对象的锁之后，其他线程无法获取该对象的锁，就不能访问该对象的其他Synchronized实例方法。可是，两个线程实例化两个不同的对象，获得的锁是不同的锁，所以互相并不影响。  


两个线程互不干扰（实际上他们是交替异步执行的）。当多个线程访问多个对象的，JVM会创建多个锁，每个锁只是锁着它对应的实例。不同的线程持有不同的锁，访问不同的对象。  
在调用synchronized修饰的方法时，线程一定是排队运行的，只有共享资源的读写才需要同步，如果不是共享资源，根本就没有同步的必要。  



### 1.2.2. Synchronized同步静态方法  
&emsp; 同步静态方法，不管有多少个类实例，同时只有一个线程能获取锁进入这个方法。  

```java
/**
 * 用在静态方法
 */
private Synchronized static void SynchronizedStaticMethod() {
    System.out.println("SynchronizedStaticMethod");
    try {
        Thread.sleep(2000);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}
```
&emsp; 同步静态方法是类级别的锁，一旦任何一个线程进入这个方法，其他所有线程将无法访问这个类的任何同步类锁的方法。  
&emsp; 对于静态同步方法，锁是当前类的Class对象，进入同步代码前要获得当前类对象的锁。  
&emsp; 注意：两个线程实例化两个不同的对象，但是访问的方法是静态的，此时获取的锁是同一个锁，两个线程发生了互斥(即一个线程访问，另一个线程只能等着)，因为静态方法是依附于类而不是对象的，当Synchronized修饰静态方法时，锁是class对象。  

## 1.3. Synchronized同步语句块  
&emsp; 对于同步代码块，锁是Synchronized括号里面配置的对象，对给定对象加锁，进入同步代码块前要获得给定对象的锁。  

### 1.3.1. 同步this实例  
&emsp; 这也是同步块的用法，表示锁住整个当前对象实例，只有获取到这个实例的锁才能进入这个方法。  

```java
/**
 * 用在this
 */
private void SynchronizedThis() {
    Synchronized (this) {
        System.out.println("SynchronizedThis");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```
&emsp; 用法和同步普通方法锁一样，都是锁住整个当前实例。  

### 1.3.2. 同步对象实例  
&emsp; 这也是同步块的用法，和上面的锁住当前实例一样，这里表示锁住整个LOCK 对象实例，只有获取到这个LOCK实例的锁才能进入这个方法。  

```java
/**
 * 用在对象
 */
private void SynchronizedInstance() {
    Synchronized (LOCK) {
        System.out.println("SynchronizedInstance");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```


### 1.3.3. 同步类
&emsp; 下面提供了两种同步类的方法，锁住效果和同步静态方法一样，都是类级别的锁，同时只有一个线程能访问带有同步类锁的方法。  

```java
/**
 * 用在类
 */
private void SynchronizedClass() {
    Synchronized (TestSynchronized.class) {
        System.out.println("SynchronizedClass");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

/**
 * 用在类
 */
private void SynchronizedGetClass() {
    Synchronized (this.getClass()) {
        System.out.println("SynchronizedGetClass");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```
&emsp; 这里的两种用法是同步块的用法，这里表示只有获取到这个类锁才能进入这个代码块。  

## 1.4. String锁
&emsp; 由于在JVM中具有String常量池缓存的功能，因此相同字面量是同一个锁。  

## 1.5. 类锁和对象锁
&emsp; **<font color = "clime">前提：是否是同一个类或同一个类的实例对象。</font>**    

&emsp; **Synchronized的范围：类锁和对象锁。** 
1. `类锁：当Synchronized修饰静态方法或Synchronized修饰代码块传入某个class对象（Synchronized (XXXX.class)）时被称为类锁。`某个线程得到了一个类锁之后，其他所有被该类锁加锁方法或代码块是锁定的，其他线程是无法访问的，但是其他线程还是可以访问没有被该类锁加锁的任何代码。  
2. `对象锁：当Synchronized修饰非静态方法或Synchronized修饰代码块时传入非class对象（Synchronized this）时被称为对象锁。`某个线程得到了对象锁之后，该对象的其他被Synchronized修饰的方法(同步方法)是锁定的，其他线程是无法访问的。但是其他线程还是可以访问没有进行同步的方法或者代码；当获取到与对象关联的内置锁时，并不能阻止其他线程访问该对象，当某个线程获得对象的锁之后，只能阻止其他线程获得同一个锁。  
3. 类锁和对象锁的关系：如同每个类只有一个class对象，而类的实例可以有很多个一样，每个类只有一个类锁，每个实例都有自己的对象锁，所以不同对象实例的对象锁是互不干扰的。但是有一点必须注意的是，其实类锁只是一个概念上的东西，并不是真实存在的，它只是用来理解锁定实例方法和静态方法的区别的。 **<font color = "clime">类锁和对象锁是不一样的锁，是互相独立的，两者不存在竞争关系，不相互阻塞。</font>**  

    * **<font color = "red">类锁与对象锁不相互阻塞。</font> 如果多线程同时访问同一类的 类锁(Synchronized 修饰的静态方法)以及对象锁(Synchronized 修饰的非静态方法)，这两个方法执行是异步的，原因：类锁和对象锁是两种不同的锁。<font color = "red">线程获得对象锁的同时，也可以获得该类锁，即同时获得两个锁，这是允许的。</font>**  
    * 相同的类锁，相同的对象锁会相互阻塞。
    * 类锁对该类的所有对象都能起作用，而对象锁不能。