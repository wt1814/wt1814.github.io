
<!-- TOC -->

- [1. synchronized锁的各种用法](#1-synchronized锁的各种用法)
    - [1.1. synchronized类锁](#11-synchronized类锁)
        - [1.1.1. synchronized修饰同一个类的两个静态方法时互斥](#111-synchronized修饰同一个类的两个静态方法时互斥)
        - [1.1.2. synchronized分别修饰同一个类的静态方法和当前类时互斥](#112-synchronized分别修饰同一个类的静态方法和当前类时互斥)
        - [1.1.3. synchronized分别修饰同一个静态对象时互斥](#113-synchronized分别修饰同一个静态对象时互斥)
    - [1.2. synchronized对象锁](#12-synchronized对象锁)
        - [1.2.1. synchronized修饰同一个类对象的两个非静态方法时互斥](#121-synchronized修饰同一个类对象的两个非静态方法时互斥)
        - [1.2.2. synchronized分别修饰同一个类对象的非静态方法和当前对象时互斥](#122-synchronized分别修饰同一个类对象的非静态方法和当前对象时互斥)
        - [1.2.3. synchronized修饰不同对象的两个非静态方法时不会互斥](#123-synchronized修饰不同对象的两个非静态方法时不会互斥)
        - [1.2.4. synchronized代码块修饰同一个对象时互斥](#124-synchronized代码块修饰同一个对象时互斥)
    - [1.3. synchronized修饰当前类和当前对象时不会互斥](#13-synchronized修饰当前类和当前对象时不会互斥)
    - [1.4. synchronized锁注意事项](#14-synchronized锁注意事项)
        - [1.4.1. synchronized锁不能被中断](#141-synchronized锁不能被中断)
        - [1.4.2. synchronized锁可重入](#142-synchronized锁可重入)
            - [1.4.2.1. 不同方法，synchronized是可重入的](#1421-不同方法synchronized是可重入的)
            - [1.4.2.2. 相同方法，synchronized是可重入的](#1422-相同方法synchronized是可重入的)
        - [1.4.3. synchronized锁不带超时功能](#143-synchronized锁不带超时功能)
        - [1.4.4. 唤醒/等待需要synchronized锁](#144-唤醒等待需要synchronized锁)
        - [1.4.5. 使用synchronized锁时尽量缩小范围以保证性能](#145-使用synchronized锁时尽量缩小范围以保证性能)

<!-- /TOC -->


# 1. synchronized锁的各种用法

<!--
***Synchronized 的 8 种用法，真是绝了！ 
https://mp.weixin.qq.com/s/TOkDyqAE5TToriOMX6I6tg
详解synchronized锁的各种用法及注意事项 
https://mp.weixin.qq.com/s/gKsD1U38h4MJczEFC33ydw
-->

&emsp; 本文主要通过简单的demo来阐述synchronized锁的各种用法以及使用synchronized锁的相关注意事项，记录下来同时也方便自己记忆。  
&emsp; synchronized锁是jvm内置的锁，不同于ReentrantLock锁。synchronized关键字可以修饰方法，也可以修饰代码块。synchronized关键字修饰方法时可以修饰静态方法，也可以修饰非静态方法；同样，synchronized关键字修饰代码块时可以修饰对象，也可以修饰类。当然，synchronized修饰静态方法/类和非静态方法/对象时的作用范围是不同的。下面通过各种demo来详解synchronized的各种用法及注意事项。  

## 1.1. synchronized类锁
&emsp; 这里所说的synchronized类锁的作用范围是类级别的，不会因为同一个类的不同对象执行而失效。  

### 1.1.1. synchronized修饰同一个类的两个静态方法时互斥

```java
public class SynchronizeAndClassLock {
    public static void main(String[] args) throws Exception {
        new Thread(() -> {
            // new了一个ClassLock对象
            new ClassLock().test1();
        }).start();

        new Thread(() -> {
            // new了另一个ClassLock对象
            new ClassLock().test2();
        }).start();
    }

}
class ClassLock {
    public synchronized static void test1(){
        System.out.println(new Date() + " " + Thread.currentThread().getName() + " begin...");
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (Exception e) {}
        System.out.println(new Date() + " " + Thread.currentThread().getName() + " end...");
    }
    // 【注意】public static void test2(){ 不会互斥，因为此时test2没有使用类锁。
    public synchronized static void test2(){
        System.out.println(new Date() + " " + Thread.currentThread().getName() + " begin...");
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (Exception e) {}
        System.out.println(new Date() + " " + Thread.currentThread().getName() + " end...");
    }
}
```

&emsp; 运行结果：  

```java
Wed Aug 18 22:30:36 GMT+08:00 2021 Thread-0 begin...
Wed Aug 18 22:30:37 GMT+08:00 2021 Thread-0 end...
Wed Aug 18 22:30:37 GMT+08:00 2021 Thread-1 begin...
Wed Aug 18 22:30:38 GMT+08:00 2021 Thread-1 end...
```

&emsp; 【结论】两个线程分别同时执行同一个类产生的不同对象的两个不同 synchronized static方法，类锁生效，虽然是不同对象，因为两个线程使用的是同一个类锁。反过来，假如test2方法没有synchronized修饰的话，只有test1方法有被synchronized修饰，此时两个方法也不会互斥，一个有锁，一个没有锁，自然不会互斥。    

### 1.1.2. synchronized分别修饰同一个类的静态方法和当前类时互斥

```java
public class SynchronizeAndClassLock2 {
    public static void main(String[] args) throws Exception {
        new Thread(() -> {
            // new了一个ClassLock2对象
            new ClassLock2().test1();
            // ClassLock2.test1();
        }).start();

        new Thread(() -> {
            // new了另一个ClassLock2对象
            new ClassLock2().test2();
            // ClassLock2.test2();
        }).start();
    }

}
class ClassLock2 {
    public synchronized static void test1(){
        System.out.println(new Date() + " " + Thread.currentThread().getName() + " begin...");
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (Exception e) {}
        System.out.println(new Date() + " " + Thread.currentThread().getName() + " end...");
    }

    public static void test2(){
     // 【注意】synchronized (SynchronizeAndClassLock2.class)不会互斥
        synchronized (ClassLock2.class) {
            System.out.println(new Date() + " " + Thread.currentThread().getName() + " begin...");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {}
            System.out.println(new Date() + " " + Thread.currentThread().getName() + " end...");
        }
    }
}
```

&emsp; 运行结果：  

```java
Wed Aug 18 22:31:32 GMT+08:00 2021 Thread-0 begin...
Wed Aug 18 22:31:33 GMT+08:00 2021 Thread-0 end...
Wed Aug 18 22:31:33 GMT+08:00 2021 Thread-1 begin...
Wed Aug 18 22:31:34 GMT+08:00 2021 Thread-1 end...
```

&emsp; 【结论】两个线程同时分别执行一个被synchronized修饰static方法，一个有synchnized(该类)代码块的static方法，锁生效，虽然是不同对象，因为两个线程使用的同一个类锁。反过来，如果是修饰的不同类，因为类锁不同，肯定不会互斥，比如将test2方法的synchronized (ClassLock2.class)这句代码改成synchronized (SynchronizeAndClassLock2.class),此时不会互斥。  

### 1.1.3. synchronized分别修饰同一个静态对象时互斥

```java
public class SynchronizeAndClassLock10 {

    public static void main(String[] args) throws Exception {
        new Thread(() -> {
            new RunObject1().test1();
        }).start();

        new Thread(() -> {
            new RunObject2().test2();
        }).start();
    }
}

class RunObject1 {
    public static void test1(){
     // 【1】synchronized (StaticLock2.staticLock1) {
        synchronized (StaticLock2.staticLock) {
            System.out.println(new Date() + " " + Thread.currentThread().getName() + " begin...");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {}
            System.out.println(new Date() + " " + Thread.currentThread().getName() + " end...");
        }
    }
}


class RunObject2 {
    public static void test2() {
     // 【2】synchronized (StaticLock2.staticLock2) {
        synchronized (StaticLock2.staticLock) {
            System.out.println(new Date() + " " + Thread.currentThread().getName() + " begin...");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {}
            System.out.println(new Date() + " " + Thread.currentThread().getName() + " end...");
        }
    }
}

class StaticLock2 {
    public static Object staticLock = new Object();
}
```

运行结果：  

```java
Wed Aug 18 22:32:33 GMT+08:00 2021 Thread-0 begin...
Wed Aug 18 22:32:34 GMT+08:00 2021 Thread-0 end...
Wed Aug 18 22:32:34 GMT+08:00 2021 Thread-1 begin...
Wed Aug 18 22:32:35 GMT+08:00 2021 Thread-1 end...
```

&emsp; 【结论】synchronized分别修饰同一个类的静态对象时互斥,反过来，如果是修饰不同的静态对象，肯定不会互斥，比如将上面代码中标【1】和【2】的synchronized代码结合使用。    
 
## 1.2. synchronized对象锁  
&emsp; 这里说的synchronized对象锁的作用范围是对象级别的即仅仅作用于同一个对象，如果是同一个类的两个不同的对象是不会互斥的，即没有效果的。  

### 1.2.1. synchronized修饰同一个类对象的两个非静态方法时互斥  

```java
public class SynchronizeAndObjectLock2 {
    public static void main(String[] args) throws Exception {
        // 【注意】当且仅当是同一个SynchronizeAndObjectLock2对象
        SynchronizeAndObjectLock2 synchronizeAndObjectLock2 = new SynchronizeAndObjectLock2();
        new Thread(() -> {
            synchronizeAndObjectLock2.test1();
        }).start();

        new Thread(() -> {
            synchronizeAndObjectLock2.test2();
        }).start();
    }
    public synchronized void test1(){
        System.out.println(new Date() + " " + Thread.currentThread().getName() + " begin...");
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (Exception e) {}
        System.out.println(new Date() + " " + Thread.currentThread().getName() + " end...");
    }

    public synchronized void test2(){
        System.out.println(new Date() + " " + Thread.currentThread().getName() + " begin...");
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (Exception e) {}
        System.out.println(new Date() + " " + Thread.currentThread().getName() + " end...");
    }
}
```

&emsp; 运行结果：  

```java
Wed Aug 18 22:08:51 GMT+08:00 2021 Thread-0 begin...
Wed Aug 18 22:08:52 GMT+08:00 2021 Thread-0 end...
Wed Aug 18 22:08:52 GMT+08:00 2021 Thread-1 begin...
Wed Aug 18 22:08:53 GMT+08:00 2021 Thread-1 end...
```

&emsp; 【结论】两个线程同时执行被synchronized修饰的相同对象的不同（相同）方法，锁生效，因为两个线程使用的是相同的对象锁。  

### 1.2.2. synchronized分别修饰同一个类对象的非静态方法和当前对象时互斥

```java
public class SynchronizeAndObjectLock3 {
    public static void main(String[] args) throws Exception {
        // 【注意】当且仅当是同一个SynchronizeAndObjectLock3对象
        SynchronizeAndObjectLock3 synchronizeAndObjectLock3 = new SynchronizeAndObjectLock3();
        new Thread(() -> {
            synchronizeAndObjectLock3.test1();
        }).start();

        new Thread(() -> {
            synchronizeAndObjectLock3.test2();
        }).start();
    }
    public void test1(){
        synchronized(this) {
            System.out.println(new Date() + " " + Thread.currentThread().getName() + " begin...");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {}
            System.out.println(new Date() + " " + Thread.currentThread().getName() + " end...");
        }

    }

    public synchronized void test2(){
        System.out.println(new Date() + " " + Thread.currentThread().getName() + " begin...");
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (Exception e) {}
        System.out.println(new Date() + " " + Thread.currentThread().getName() + " end...");
    }
}
```

&emsp; 运行结果：  

```java
Wed Aug 18 22:07:46 GMT+08:00 2021 Thread-0 begin...
Wed Aug 18 22:07:47 GMT+08:00 2021 Thread-0 end...
Wed Aug 18 22:07:47 GMT+08:00 2021 Thread-1 begin...
Wed Aug 18 22:07:48 GMT+08:00 2021 Thread-1 end...
```

### 1.2.3. synchronized修饰不同对象的两个非静态方法时不会互斥

```java
public class SynchronizeAndObjectLock {
    public static void main(String[] args) throws Exception {
        new Thread(() -> {
            // 这里new 了一个SynchronizeAndObjectLock对象
            new SynchronizeAndObjectLock().test1();
        }).start();

        new Thread(() -> {
            // 这里new 了另一个SynchronizeAndObjectLock对象
            new SynchronizeAndObjectLock().test2();
        }).start();
    }
    public synchronized void test1(){
        System.out.println(new Date() + " " + Thread.currentThread().getName() + " begin...");
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (Exception e) {}
        System.out.println(new Date() + " " + Thread.currentThread().getName() + " end...");
    }

    public synchronized void test2(){
        System.out.println(new Date() + " " + Thread.currentThread().getName() + " begin...");
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (Exception e) {}
        System.out.println(new Date() + " " + Thread.currentThread().getName() + " end...");
    }
}
```

&emsp; 运行结果：  

```java
Wed Aug 18 22:11:45 GMT+08:00 2021 Thread-0 begin...
Wed Aug 18 22:11:45 GMT+08:00 2021 Thread-1 begin...
Wed Aug 18 22:11:46 GMT+08:00 2021 Thread-0 end...
Wed Aug 18 22:11:46 GMT+08:00 2021 Thread-1 end...
```

&emsp; 【结论】两个线程同时执行被synchronized修饰的不同对象的不同（相同）方法，锁未生效，因为两个线程使用的是不同的对象锁。  

### 1.2.4. synchronized代码块修饰同一个对象时互斥

```java
public class SynchronizeAndObjectLock5 {
    private Object objectLock = new Object();

    public static void main(String[] args) throws Exception {
        
        SynchronizeAndObjectLock5 synchronizeAndObjectLock5 = new SynchronizeAndObjectLock5();
        new Thread(() -> {
            synchronizeAndObjectLock5.test1();
        }).start();

        new Thread(() -> {
            synchronizeAndObjectLock5.test2();
        }).start();
    }
    public void test1(){
        synchronized(objectLock) {
            System.out.println(new Date() + " " + Thread.currentThread().getName() + " begin...");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {}
            System.out.println(new Date() + " " + Thread.currentThread().getName() + " end...");
        }

    }

    public void test2(){
        synchronized(objectLock) {
            System.out.println(new Date() + " " + Thread.currentThread().getName() + " begin...");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {}
            System.out.println(new Date() + " " + Thread.currentThread().getName() + " end...");
        }
    }
}
```

&emsp; 运行结果：  

```java
Wed Aug 18 22:18:01 GMT+08:00 2021 Thread-0 begin...
Wed Aug 18 22:18:02 GMT+08:00 2021 Thread-0 end...
Wed Aug 18 22:18:02 GMT+08:00 2021 Thread-1 begin...
Wed Aug 18 22:18:03 GMT+08:00 2021 Thread-1 end...
```

&emsp; 【结论】synchronized代码块修饰同一个对象时互斥，若synchronized代码块修饰的是不同对象，那么不会互斥。  

## 1.3. synchronized修饰当前类和当前对象时不会互斥  

```java
public class ClassAndObjectLock {
    public static void main(String[] args) throws Exception {
        new Thread(() -> {
            ClassAndObjectLock.test1();
        }).start();

        new Thread(() -> {
            new ClassAndObjectLock().test2();
        }).start();
    }
    public static void test1(){
        synchronized (ClassAndObjectLock.class) {
            System.out.println(new Date() + " " + Thread.currentThread().getName() + " begin...");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {}
            System.out.println(new Date() + " " + Thread.currentThread().getName() + " end...");
        }
    }

    public void test2(){
        synchronized (this) {
            System.out.println(new Date() + " " + Thread.currentThread().getName() + " begin...");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {}
            System.out.println(new Date() + " " + Thread.currentThread().getName() + " end...");
        }
    }
}
```

&emsp; 运行结果：  

```java
Wed Aug 18 22:27:07 GMT+08:00 2021 Thread-1 begin...
Wed Aug 18 22:27:07 GMT+08:00 2021 Thread-0 begin...
Wed Aug 18 22:27:08 GMT+08:00 2021 Thread-1 end...
Wed Aug 18 22:27:08 GMT+08:00 2021 Thread-0 end...
```

&emsp; 【结论】可见，类锁和对象锁是相互独立的，互不相斥。  

## 1.4. synchronized锁注意事项
### 1.4.1. synchronized锁不能被中断
&emsp; 为了模拟synchronized锁不可中断，下面先让两个线程进入死锁，然后再用main线程去中断其中一个线程，看被中断的线程能否释放锁并被唤醒。  

```java
public class DeadLockCannotInterruptDemo {
    private static Object lock1 = new Object();
    private static Object lock2 = new Object();

    public static void main(String[] args) throws Exception {
        Thread threadA = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (lock1) {
                    System.out.println(Thread.currentThread().getName() + " get lock1");
                    try {
                        Thread.sleep(10);
                        synchronized (lock2) {
                            System.out.println(Thread.currentThread().getName() + " get lock2");
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        Thread threadB = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (lock2) {
                    System.out.println(Thread.currentThread().getName() + " get lock2");
                    try {
                        Thread.sleep(10);
                        synchronized (lock1) {
                            System.out.println(Thread.currentThread().getName() + " get lock1");
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        threadA.start();
        threadB.start();

        TimeUnit.SECONDS.sleep(3);
        System.out.println("main thread begin to interrupt " + threadA.getName() + " and " + threadA.getName() + " will release lock1...");
        threadA.interrupt();
    }
}
```

&emsp; 运行结果：  

```java
Thread-1 get lock2
Thread-0 get lock1
main thread begin to interrupt Thread-0 and Thread-0 will release lock1...
```

&emsp; 【结论】如上图，main线程中断Thread-0后，Thread-0并不会释放锁并醒过来。同样的，ReentrantLock的tryLock或lockInterruptibly是可以被中断的。  

### 1.4.2. synchronized锁可重入
#### 1.4.2.1. 不同方法，synchronized是可重入的

```java
public class SynchronizeAndReentrant {
    public static void main(String[] args) throws Exception {
        SynchronizeAndReentrant synchronizeAndReentrant = new SynchronizeAndReentrant();
        synchronizeAndReentrant.test1();
    }
    public synchronized void test1(){
        System.out.println(" test1 method is called...");
        test2();
    }

    public synchronized void test2(){
        System.out.println(" test2 method is called...");
    }
}
```

&emsp; 运行结果：  

```java
test1 method is called...
test2 method is called...
```

#### 1.4.2.2. 相同方法，synchronized是可重入的

```java
public class SynchronizeAndReentrant2 {
    int i = 1;
    public static void main(String[] args) throws Exception {
        SynchronizeAndReentrant2 synchronizeAndReentrant = new SynchronizeAndReentrant2();
        synchronizeAndReentrant.test1();
    }
    public synchronized void test1(){

        System.out.println(" test1 method is called " + i++ + "st time..." );
        while(i < 5) {
            test1();
        }
    }
}
```

&emsp; 运行结果：  

```java
 test1 method is called 1st time...
 test1 method is called 2st time...
 test1 method is called 3st time...
 test1 method is called 4st time...
```

### 1.4.3. synchronized锁不带超时功能
&emsp; synchronized锁不带超时功能,而ReentrantLock的tryLock是具备带超时功能的，在指定时间没获取到锁，该线程会苏醒，有助于预防死锁的产生。  

### 1.4.4. 唤醒/等待需要synchronized锁

```java
public class NotifyNeedSynchronized {
    public static Object lock = new Object();
    public static void main(String[] args) throws Exception{
        // 抛出IllegalMonitorStateException
        //lock.notify();
        lock.wait();
    }
}
```

&emsp; 运行结果：  

```java
Exception in thread "main" java.lang.IllegalMonitorStateException
	at java.lang.Object.wait(Native Method)
	at java.lang.Object.wait(Object.java:502)
	at aTest.NotifyNeedSynchronized.main(NotifyNeedSynchronized.java:9)
```

&emsp; 【结论】使用Object的notify和wait等方法时，必须要使用synchronized锁，否则会抛出IllegalMonitorStateException。  

### 1.4.5. 使用synchronized锁时尽量缩小范围以保证性能
&emsp; 使用synchronized锁时，为了尽可能提高性能，我们应该尽量缩小锁的范围。能不锁方法就不锁方法，推荐尽量使用synchronized代码块来降低锁的范围。以下面的一段netty源码为例：  

```java
// ServerBootstrap.java

public <T> ServerBootstrap childOption(ChannelOption<T> childOption, T value) {
    if (childOption == null) {
        throw new NullPointerException("childOption");
    }
    if (value == null) {
        synchronized (childOptions) {
            childOptions.remove(childOption);
        }
    } else {
        synchronized (childOptions) {
            childOptions.put(childOption, value);
        }
    }
    return this;
}
```

&emsp; 可见，找到并发访问代码的临界区，并不用synchronized锁全部代码，尽量避免使用synchronized来修饰方法。  
