
<!-- TOC -->

- [1. Synchronized使用实例](#1-synchronized使用实例)
    - [1.1. synchronized类锁](#11-synchronized类锁)
        - [1.1.1. synchronized修饰同一个类的两个静态方法时互斥](#111-synchronized修饰同一个类的两个静态方法时互斥)
        - [1.1.2. synchronized分别修饰同一个类的静态方法和当前类时互斥](#112-synchronized分别修饰同一个类的静态方法和当前类时互斥)
        - [1.1.3. synchronized分别修饰同一个静态对象时互斥](#113-synchronized分别修饰同一个静态对象时互斥)
    - [1.2. synchronized对象锁](#12-synchronized对象锁)
        - [1.2.1. synchronized修饰同一个类对象的两个非静态方法时互斥](#121-synchronized修饰同一个类对象的两个非静态方法时互斥)
        - [1.2.2. synchronized分别修饰同一个类对象的非静态方法和当前对象时互斥](#122-synchronized分别修饰同一个类对象的非静态方法和当前对象时互斥)
        - [1.2.3. synchronized修饰不同对象的两个非静态方法时不会互斥](#123-synchronized修饰不同对象的两个非静态方法时不会互斥)
        - [1.2.4. synchronized代码块修饰同一个对象时互斥](#124-synchronized代码块修饰同一个对象时互斥)
        - [1.2.5. 场景四：两个线程分别同时访问（一个或两个）对象的同步方法和非同步方法](#125-场景四两个线程分别同时访问一个或两个对象的同步方法和非同步方法)
        - [1.2.6. 场景五：两个线程访问同一个对象中的同步方法，同步方法又调用一个非同步方法](#126-场景五两个线程访问同一个对象中的同步方法同步方法又调用一个非同步方法)
        - [1.2.7. 场景六：两个线程同时访问同一个对象的不同的同步方法](#127-场景六两个线程同时访问同一个对象的不同的同步方法)
        - [1.2.8. 场景七：两个线程分别同时访问静态synchronized和非静态synchronized方法](#128-场景七两个线程分别同时访问静态synchronized和非静态synchronized方法)
    - [1.3. 场景八：同步方法抛出异常后，JVM会自动释放锁的情况](#13-场景八同步方法抛出异常后jvm会自动释放锁的情况)
    - [1.4. synchronized修饰当前类和当前对象时不会互斥](#14-synchronized修饰当前类和当前对象时不会互斥)
    - [1.5. synchronized锁注意事项](#15-synchronized锁注意事项)
        - [1.5.1. synchronized锁不能被中断](#151-synchronized锁不能被中断)
        - [1.5.2. synchronized锁可重入](#152-synchronized锁可重入)
            - [1.5.2.1. 不同方法，synchronized是可重入的](#1521-不同方法synchronized是可重入的)
            - [1.5.2.2. 相同方法，synchronized是可重入的](#1522-相同方法synchronized是可重入的)
        - [1.5.3. synchronized锁不带超时功能](#153-synchronized锁不带超时功能)
        - [1.5.4. 唤醒/等待需要synchronized锁](#154-唤醒等待需要synchronized锁)
        - [1.5.5. 使用synchronized锁时尽量缩小范围以保证性能](#155-使用synchronized锁时尽量缩小范围以保证性能)
    - [1.6. 小结](#16-小结)

<!-- /TOC -->

# 1. Synchronized使用实例

<!--

~~
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

-----------

&emsp; 场景三：两个线程同时访问（一个或两个）对象的静态同步方法  
&emsp; 这个场景解决的是场景二中出现的线程不安全问题，即用类锁实现：  
&emsp; 「两个线程同时访问（一个或两个）对象的静态同步方法，是线程安全的。」  

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


-------------------

&emsp; 场景一：两个线程同时访问同一个对象的同步方法  
&emsp; 分析：这种情况是经典的对象锁中的方法锁，两个线程争夺同一个对象锁，所以会相互等待，是线程安全的。  
&emsp; 「两个线程同时访问同一个对象的同步方法，是线程安全的。」  

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


-------------------

&emsp; 场景二：两个线程同时访问两个对象的同步方法  

&emsp; 这种场景就是对象锁失效的场景，原因出在访问的是两个对象的同步方法，那么这两个线程分别持有的两个线程的锁，所以是互相不会受限的。加锁的目的是为了让多个线程竞争同一把锁，而这种情况多个线程之间不再竞争同一把锁，而是分别持有一把锁，所以我们的结论是：  

&emsp; 「两个线程同时访问两个对象的同步方法，是线程不安全的。」  
&emsp; 代码验证：  

```java
public class Condition2 implements Runnable {  
    // 创建两个不同的对象  
 static Condition2 instance1 = new Condition2();  
 static Condition2 instance2 = new Condition2();  
  
 @Override  
 public void run() {  
  method();  
 }  
  
 private synchronized void method() {  
  System.out.println("线程名：" + Thread.currentThread().getName() + "，运行开始");  
  try {  
   Thread.sleep(4000);  
  } catch (InterruptedException e) {  
   e.printStackTrace();  
  }  
  System.out.println("线程：" + Thread.currentThread().getName() + "，运行结束");  
 }  
  
 public static void main(String[] args) {  
  Thread thread1 = new Thread(instance1);  
  Thread thread2 = new Thread(instance2);  
  thread1.start();  
  thread2.start();  
  while (thread1.isAlive() || thread2.isAlive()) {  
  }  
  System.out.println("测试结束");  
 }  
}  
``` 

&emsp; 运行结果：  

&emsp; 两个线程是并行执行的，所以线程不安全。  

```java
线程名：Thread-0，运行开始  
线程名：Thread-1，运行开始  
线程：Thread-0，运行结束  
线程：Thread-1，运行结束  
测试结束  
```

&emsp; 代码分析：  
&emsp; 「问题在此：」  
&emsp; 两个线程（thread1、thread2），访问两个对象（instance1、instance2）的同步方法（method()）,两个线程都有各自的锁，不能形成两个线程竞争一把锁的局势，所以这时，synchronized修饰的方法method()和不用synchronized修饰的效果一样（不信去把synchronized关键字去掉，运行结果一样），所以此时的method()只是个普通方法。  
&emsp; 「如何解决这个问题：」  
&emsp; 若要使锁生效，只需将method()方法用static修饰，这样就形成了类锁，多个实例（instance1、instance2）共同竞争一把类锁，就可以使两个线程串行执行了。这也就是下一个场景要讲的内容。  


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

### 1.2.5. 场景四：两个线程分别同时访问（一个或两个）对象的同步方法和非同步方法

这个场景是两个线程其中一个访问同步方法，另一个访问非同步方法，此时程序会不会串行执行呢，也就是说是不是线程安全的呢？  
我们可以确定是线程不安全的，如果方法不加synchronized都是安全的，那就不需要同步方法了。验证下我们的结论：  

「两个线程分别同时访问（一个或两个）对象的同步方法和非同步方法，是线程不安全的。」  

```java
public class Condition4 implements Runnable {  
  
 static Condition4 instance = new Condition4();  
  
 @Override  
 public void run() {  
  //两个线程访问同步方法和非同步方法  
  if (Thread.currentThread().getName().equals("Thread-0")) {  
   //线程0,执行同步方法method0()  
   method0();  
  }  
  if (Thread.currentThread().getName().equals("Thread-1")) {  
   //线程1,执行非同步方法method1()  www.xttblog.com
   method1();  
  }  
 }  
      
    // 同步方法  
 private synchronized void method0() {  
  System.out.println("线程名：" + Thread.currentThread().getName() + "，同步方法，运行开始");  
  try {  
   Thread.sleep(4000);  
  } catch (InterruptedException e) {  
   e.printStackTrace();  
  }  
  System.out.println("线程：" + Thread.currentThread().getName() + "，同步方法，运行结束");  
 }  
      
    // 普通方法  
 private void method1() {  
  System.out.println("线程名：" + Thread.currentThread().getName() + "，普通方法，运行开始");  
  try {  
   Thread.sleep(4000);  
  } catch (InterruptedException e) {  
   e.printStackTrace();  
  }  
  System.out.println("线程：" + Thread.currentThread().getName() + "，普通方法，运行结束");  
 }  
  
 public static void main(String[] args) {  
  Thread thread1 = new Thread(instance);  
  Thread thread2 = new Thread(instance);  
  thread1.start();  
  thread2.start();  
  while (thread1.isAlive() || thread2.isAlive()) {  
  }  
  System.out.println("测试结束");  
 }  
  
}  
```

运行结果：  

两个线程是并行执行的，所以是线程不安全的。  

```java
线程名：Thread-0，同步方法，运行开始  
线程名：Thread-1，普通方法，运行开始  
线程：Thread-0，同步方法，运行结束  
线程：Thread-1，普通方法，运行结束  
测试结束  
```

结果分析  

问题在于此：method1没有被synchronized修饰，所以不会受到锁的影响。即便是在同一个对象中，当然在多个实例中，更不会被锁影响了。结论：  

「非同步方法不受其它由synchronized修饰的同步方法影响」  

你可能想到一个类似场景：多个线程访问同一个对象中的同步方法，同步方法又调用一个非同步方法，这个场景会是线程安全的吗？  


### 1.2.6. 场景五：两个线程访问同一个对象中的同步方法，同步方法又调用一个非同步方法

我们来实验下这个场景，用两个线程调用同步方法，在同步方法中调用普通方法；再用一个线程直接调用普通方法，看看是否是线程安全的？  

```java
public class Condition8 implements Runnable {  
  
 static Condition8 instance = new Condition8();  
  
 @Override  
 public void run() {  
  if (Thread.currentThread().getName().equals("Thread-0")) {  
   //直接调用普通方法  
   method2();  
  } else {  
   // 先调用同步方法，在同步方法内调用普通方法  
   method1();  
  }  
 }  
  
 // 同步方法  
 private static synchronized void method1() {  
  System.out.println("线程名：" + Thread.currentThread().getName() + "，同步方法，运行开始");  
  try {  
   Thread.sleep(2000);  
  } catch (InterruptedException e) {  
   e.printStackTrace();  
  }  
  System.out.println("线程：" + Thread.currentThread().getName() + "，同步方法，运行结束,开始调用普通方法");  
  method2();  
 }  
  
 // 普通方法  
 private static void method2() {  
  System.out.println("线程名：" + Thread.currentThread().getName() + "，普通方法，运行开始");  
  try {  
   Thread.sleep(4000);  
  } catch (InterruptedException e) {  
   e.printStackTrace();  
  }  
  System.out.println("线程：" + Thread.currentThread().getName() + "，普通方法，运行结束");  
 }  
  
 public static void main(String[] args) {  
  // 此线程直接调用普通方法  
  Thread thread0 = new Thread(instance);  
  // 这两个线程直接调用同步方法  
  Thread thread1 = new Thread(instance);  
  Thread thread2 = new Thread(instance);  
  thread0.start();  
  thread1.start();  
  thread2.start();  
  while (thread0.isAlive() || thread1.isAlive() || thread2.isAlive()) {  
  }  
  System.out.println("测试结束");  
 }  
  
}  
```

运行结果：  

```java
线程名：Thread-0，普通方法，运行开始  
线程名：Thread-1，同步方法，运行开始  
线程：Thread-1，同步方法，运行结束,开始调用普通方法  
线程名：Thread-1，普通方法，运行开始  
线程：Thread-0，普通方法，运行结束  
线程：Thread-1，普通方法，运行结束  
线程名：Thread-2，同步方法，运行开始  
线程：Thread-2，同步方法，运行结束,开始调用普通方法  
线程名：Thread-2，普通方法，运行开始  
线程：Thread-2，普通方法，运行结束  
测试结束  
```

结果分析：   

我们可以看出，普通方法被两个线程并行执行，不是线程安全的。这是为什么呢？  

因为如果非同步方法，有任何其他线程直接调用，而不是仅在调用同步方法时，才调用非同步方法，此时会出现多个线程并行执行非同步方法的情况，线程就不安全了。  

对于同步方法中调用非同步方法时，要想保证线程安全，就必须保证非同步方法的入口，仅出现在同步方法中。但这种控制方式不够优雅，若被不明情况的人直接调用非同步方法，就会导致原有的线程同步不再安全。所以不推荐大家在项目中这样使用，但我们要理解这种情况，并且我们要用语义明确的、让人一看就知道这是同步方法的方式，来处理线程安全的问题。  

所以，最简单的方式，是在非同步方法上，也加上synchronized关键字，使其变成一个同步方法，这样就变成了《场景五：两个线程同时访问同一个对象的不同的同步方法》，这种场景下，大家就很清楚的看到，同一个对象中的两个同步方法，不管哪个线程调用，都是线程安全的了。  

所以结论是：  

「两个线程访问同一个对象中的同步方法，同步方法又调用一个非同步方法，仅在没有其他线程直接调用非同步方法的情况下，是线程安全的。若有其他线程直接调用非同步方法，则是线程不安全的。」  

### 1.2.7. 场景六：两个线程同时访问同一个对象的不同的同步方法
这个场景也是在探讨对象锁的作用范围，对象锁的作用范围是对象中的所有同步方法。所以，当访问同一个对象中的多个同步方法时，结论是：  

「两个线程同时访问同一个对象的不同的同步方法时，是线程安全的。」  

```java
public class Condition5 implements Runnable {  
 static Condition5 instance = new Condition5();  
  
 @Override  
 public void run() {  
  if (Thread.currentThread().getName().equals("Thread-0")) {  
   //线程0,执行同步方法method0()  
   method0();  
  }  
  if (Thread.currentThread().getName().equals("Thread-1")) {  
   //线程1,执行同步方法method1()  www.xttblog.com
   method1();  
  }  
 }  
  
 private synchronized void method0() {  
  System.out.println("线程名：" + Thread.currentThread().getName() + "，同步方法0，运行开始");  
  try {  
   Thread.sleep(4000);  
  } catch (InterruptedException e) {  
   e.printStackTrace();  
  }  
  System.out.println("线程：" + Thread.currentThread().getName() + "，同步方法0，运行结束");  
 }  
  
 private synchronized void method1() {  
  System.out.println("线程名：" + Thread.currentThread().getName() + "，同步方法1，运行开始");  
  try {  
   Thread.sleep(4000);  
  } catch (InterruptedException e) {  
   e.printStackTrace();  
  }  
  System.out.println("线程：" + Thread.currentThread().getName() + "，同步方法1，运行结束");  
 }  
  
 //运行结果:串行  
 public static void main(String[] args) {  
  Thread thread1 = new Thread(instance);  
  Thread thread2 = new Thread(instance);  
  thread1.start();  
  thread2.start();  
  while (thread1.isAlive() || thread2.isAlive()) {  
  }  
  System.out.println("测试结束");  
 }  
}  
```

运行结果：  

是线程安全的。  

```java
线程名：Thread-1，同步方法1，运行开始  
线程：Thread-1，同步方法1，运行结束  
线程名：Thread-0，同步方法0，运行开始  
线程：Thread-0，同步方法0，运行结束  
测试结束  
```

结果分析：  

两个方法（method0()和method1()）的synchronized修饰符，虽没有指定锁对象，但默认锁对象为this对象为锁对象，所以对于同一个实例（instance），两个线程拿到的锁是同一把锁，此时同步方法会串行执行。这也是synchronized关键字的可重入性的一种体现。  

### 1.2.8. 场景七：两个线程分别同时访问静态synchronized和非静态synchronized方法
这种场景的本质也是在探讨两个线程获取的是不是同一把锁的问题。静态synchronized方法属于类锁，锁对象是（*.class）对象，非静态synchronized方法属于对象锁中的方法锁，锁对象是this对象。两个线程拿到的是不同的锁，自然不会相互影响。结论：  

「两个线程分别同时访问静态synchronized和非静态synchronized方法，线程不安全。」  
代码实现：  

```java
public class Condition6 implements Runnable {  
 static Condition6 instance = new Condition6();  
  
 @Override  
 public void run() {  
  if (Thread.currentThread().getName().equals("Thread-0")) {  
   //线程0,执行静态同步方法method0()  
   method0();  
  }  
  if (Thread.currentThread().getName().equals("Thread-1")) {  
   //线程1,执行非静态同步方法method1()  
   method1();  
  }  
 }  
  
 // 重点：用static synchronized 修饰的方法，属于类锁，锁对象为（*.class）对象。  
 private static synchronized void method0() {  
  System.out.println("线程名：" + Thread.currentThread().getName() + "，静态同步方法0，运行开始");  
  try {  
   Thread.sleep(4000);  
  } catch (InterruptedException e) {  
   e.printStackTrace();  
  }  
  System.out.println("线程：" + Thread.currentThread().getName() + "，静态同步方法0，运行结束");  
 }  
  
 // 重点：synchronized 修饰的方法，属于方法锁，锁对象为（this）对象。  
 private synchronized void method1() {  
  System.out.println("线程名：" + Thread.currentThread().getName() + "，非静态同步方法1，运行开始");  
  try {  
   Thread.sleep(4000);  
  } catch (InterruptedException e) {  
   e.printStackTrace();  
  }  
  System.out.println("线程：" + Thread.currentThread().getName() + "，非静态同步方法1，运行结束");  
 }  
  
 //运行结果:并行  
 public static void main(String[] args) {  
  //问题原因： 线程1的锁是类锁（*.class）对象，线程2的锁是方法锁（this）对象,两个线程的锁不一样，自然不会互相影响，所以会并行执行。  
  Thread thread1 = new Thread(instance);  
  Thread thread2 = new Thread(instance);  
  thread1.start();  
  thread2.start();  
  while (thread1.isAlive() || thread2.isAlive()) {  
  }  
  System.out.println("测试结束");  
 }  
```

运行结果：  

```java
线程名：Thread-0，静态同步方法0，运行开始  
线程名：Thread-1，非静态同步方法1，运行开始  
线程：Thread-1，非静态同步方法1，运行结束  
线程：Thread-0，静态同步方法0，运行结束  
测试结束  
```

## 1.3. 场景八：同步方法抛出异常后，JVM会自动释放锁的情况
本场景探讨的是synchronized释放锁的场景：  

「只有当同步方法执行完或执行时抛出异常这两种情况，才会释放锁。」  

所以，在一个线程的同步方法中出现异常的时候，会释放锁，另一个线程得到锁，继续执行。而不会出现一个线程抛出异常后，另一个线程一直等待获取锁的情况。这是因为JVM在同步方法抛出异常的时候，会自动释放锁对象。
代码实现：  

```java
public class Condition7 implements Runnable {  
  
 private static Condition7 instance = new Condition7();  
  
 @Override  
 public void run() {  
  if (Thread.currentThread().getName().equals("Thread-0")) {  
   //线程0,执行抛异常方法method0()  
   method0();  
  }  
  if (Thread.currentThread().getName().equals("Thread-1")) {  
   //线程1,执行正常方法method1()  
   method1();  
  }  
 }  
  
 private synchronized void method0() {  
  System.out.println("线程名：" + Thread.currentThread().getName() + "，运行开始");  
  try {  
   Thread.sleep(4000);  
  } catch (InterruptedException e) {  
   e.printStackTrace();  
  }  
  //同步方法中，当抛出异常时，JVM会自动释放锁，不需要手动释放，其他线程即可获取到该锁  
  System.out.println("线程名：" + Thread.currentThread().getName() + "，抛出异常，释放锁");  
  throw new RuntimeException();  
  
 }  
  
 private synchronized void method1() {  
  System.out.println("线程名：" + Thread.currentThread().getName() + "，运行开始");  
  try {  
   Thread.sleep(4000);  
  } catch (InterruptedException e) {  
   e.printStackTrace();  
  }  
  System.out.println("线程：" + Thread.currentThread().getName() + "，运行结束");  
 }  
  
 public static void main(String[] args) {  
  Thread thread1 = new Thread(instance);  
  Thread thread2 = new Thread(instance);  
  thread1.start();  
  thread2.start();  
  while (thread1.isAlive() || thread2.isAlive()) {  
  }  
  System.out.println("测试结束");  
 }  
  
}  
```

运行结果：  

```java
线程名：Thread-0，运行开始  
线程名：Thread-0，抛出异常，释放锁  
线程名：Thread-1，运行开始  
Exception in thread "Thread-0" java.lang.RuntimeException  
 at com.study.synchronize.conditions.Condition7.method0(Condition7.java:34)  
 at com.study.synchronize.conditions.Condition7.run(Condition7.java:17)  
 at java.lang.Thread.run(Thread.java:748)  
线程：Thread-1，运行结束  
测试结束  
```

结果分析：  
可以看出线程还是串行执行的，说明是线程安全的。而且出现异常后，不会造成死锁现象，JVM会自动释放出现异常线程的锁对象，其他线程获取锁继续执行。  

## 1.4. synchronized修饰当前类和当前对象时不会互斥  

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

## 1.5. synchronized锁注意事项
### 1.5.1. synchronized锁不能被中断
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

### 1.5.2. synchronized锁可重入
#### 1.5.2.1. 不同方法，synchronized是可重入的

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

#### 1.5.2.2. 相同方法，synchronized是可重入的

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

### 1.5.3. synchronized锁不带超时功能
&emsp; synchronized锁不带超时功能,而ReentrantLock的tryLock是具备带超时功能的，在指定时间没获取到锁，该线程会苏醒，有助于预防死锁的产生。  

### 1.5.4. 唤醒/等待需要synchronized锁

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

### 1.5.5. 使用synchronized锁时尽量缩小范围以保证性能
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


## 1.6. 小结  
&emsp; 本文总结了并用代码实现和验证了synchronized各种使用场景，以及各种场景发生的原因和结论。我们分析的理论基础都是synchronized关键字的锁对象究竟是谁？多个线程之间竞争的是否是同一把锁？根据这个条件来判断线程是否是安全的。所以，有了这些场景的分析锻炼后，我们在以后使用多线程编程时，也可以通过分析锁对象的方式，判断出线程是否是安全的，从而避免此类问题的出现。  
