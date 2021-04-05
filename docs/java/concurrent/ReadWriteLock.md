<!-- TOC -->

- [1. ReadWriteLock](#1-readwritelock)
    - [1.1. ReentrantReadWriteLock，读写锁](#11-reentrantreadwritelock读写锁)
    - [1.2. StampedLock，读写锁的升级](#12-stampedlock读写锁的升级)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1.  ReentrantReadWriteLock  
&emsp; **<font color = "red">ReentrantReadWriteLock缺点：读写锁互斥，只有当前没有线程持有读锁或者写锁时，才能获取到写锁，</font><font color = "clime">这可能会导致写线程发生饥饿现象，</font><font color = "red">即读线程太多导致写线程迟迟竞争不到锁而一直处于等待状态。StampedLock()可以解决这个问题。</font>**  
2. StampedLock  
    1. StampedLock有3种模式：写锁 writeLock、悲观读锁 readLock、乐观读锁 tryOptimisticRead。  
    StampedLock通过乐观读锁 tryOptimisticRead解决ReentrantReadWriteLock的写锁饥饿问题。乐观读锁模式下，一个线程获取的乐观读锁之后，不会阻塞线程获取写锁。    
    2. 同时允许多个乐观读和一个写线程同时进入临界资源操作，那读取的数据可能是错的怎么办？  
    &emsp; **<font color = "clime">通过版本号控制。</font>** 乐观读不能保证读取到的数据是最新的，所以将数据读取到局部变量的时候需要通过 lock.validate(stamp) 校验是否被写线程修改过，若是修改过则需要上悲观读锁，再重新读取数据到局部变量。 **<font color = "clime">即乐观读失败后，再次使用悲观读锁。</font>**    


# 1. ReadWriteLock  
<!-- 
面试官：读写锁了解吗？它的升降级啥时候用？ 
https://mp.weixin.qq.com/s/JwEkiH6WlQd-UfyAPbltBA
-->

## 1.1. ReentrantReadWriteLock，读写锁
&emsp; ReentrantReadWriteLock维护了两个锁，读锁和写锁，所以一般称其为读写锁。写锁是独占的(写操作只能由一个线程来操作)。读锁是共享的，如果没有写锁，读锁可以由多个线程共享。  
&emsp; 优点：与互斥锁相比，虽然一次只能有一个写线程可以修改共享数据，但大量读线程可以同时读取共享数据，所以，读写锁适用于共享数据很大，且读操作远多于写操作的情况。  
&emsp; **<font color = "red">缺点：读写锁互斥，只有当前没有线程持有读锁或者写锁时才能获取到写锁，</font><font color = "clime">这可能会导致写线程发生饥饿现象，</font><font color = "red">即读线程太多导致写线程迟迟竞争不到锁而一直处于等待状态。StampedLock()可以解决这个问题。</font>**  


&emsp; 编码示例：  

```java
private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
private Lock readLock = readWriteLock.readLock();
private Lock writeLock = readWriteLock.writeLock();
public Object handleRead() throws InterruptedException {
    try {
        readLock.lock();
        Thread.sleep(1000);
        return value;
    }finally{
        readLock.unlock();
    }
}
public Object handleRead() throws InterruptedException {
    try {
        writeLock.lock();
        Thread.sleep(1000);
        return value;
    }finally{
        writeLock.unlock();
    }
}
```

## 1.2. StampedLock，读写锁的升级
<!-- 
StampedLock
https://mp.weixin.qq.com/s/vwvcgBPOnW7M2GrgVDDdGg
-->

&emsp; <font color = "clime">StampedLock提供了三种模式来控制读写操作：写锁 writeLock、悲观读锁readLock、乐观读锁Optimistic reading。</font>

* 写锁 writeLock  
&emsp; 类似ReentrantReadWriteLock的写锁，独占锁，当一个线程获取该锁后，其它请求的线程必须等待。  
&emsp; 获取：没有线程持有悲观读锁或者写锁的时候才可以获取到该锁。  
&emsp; 释放：请求该锁成功后会返回一个 stamp 票据变量用来表示该锁的版本，当释放该锁时候需要将这个 stamp 作为参数传入解锁方法。  
* 悲观读锁 readLock  
&emsp; 类似ReentrantReadWriteLock的读锁，共享锁，同时多个线程可以获取该锁。悲观的认为在具体操作数据前其他线程会对自己操作的数据进行修改，所以当前线程获取到悲观读锁的之后会阻塞线程获取写锁。  
&emsp; 获取：在没有线程获取独占写锁的情况下，同时多个线程可以获取该锁。  
&emsp; 释放：请求该锁成功后会返回一个 stamp 票据变量用来表示该锁的版本，当释放该锁时候需要 unlockRead 并传递参数 stamp。  
* 乐观读锁 tryOptimisticRead  
&emsp; 获取：不需要通过 CAS 设置锁的状态，如果当前没有线程持有写锁，直接简单的返回一个非 0 的 stamp 版本信息，表示获取锁成功。  
&emsp; 释放：并没有使用 CAS 设置锁状态所以不需要显示的释放该锁。  


&emsp; **StampedLock通过乐观读锁Optimistic reading解决ReentrantReadWriteLock的写锁解饿问题，即一个线程获取乐观读锁之后，不会阻塞线程获取写锁。**  
&emsp; **乐观读锁如何保证数据一致性呢？** 使用版本号。  
&emsp; 乐观读锁在获取 stamp 时，会将需要的数据拷贝一份出来。在真正进行读取操作时，验证 stamp 是否可用。如何验证 stamp 是否可用呢？从获取 stamp 到真正进行读取操作这段时间内，如果有线程获取了写锁，stamp 就失效了。如果 stamp 可用就可以直接读取原来拷贝出来的数据，如果 stamp 不可用，就重新拷贝一份出来用。操作的是方法栈里面的数据，也就是一个快照，所以最多返回的不是最新的数据，但是一致性还是得到保障的。  

&emsp; 同时允许多个乐观读和一个写线程同时进入临界资源操作，那读取的数据可能是错的怎么办？  
&emsp; 通过版本号控制。乐观读不能保证读取到的数据是最新的，所以将数据读取到局部变量的时候需要通过 lock.validate(stamp) 校验是否被写线程修改过，若是修改过则需要上悲观读锁，再重新读取数据到局部变量。 **<font color = "clime">即乐观读失败后，再次使用悲观读锁。</font>**    

&emsp; **StampedLock特点：**  

* StampedLock是不可重入的，如果一个线程已经持有了写锁，再去获取写锁的话就会造成死锁。  
* StampedLock支持读锁和写锁的相互转换。使用ReentrantReadWriteLock，当线程获取到写锁后，可以降级为读锁，但是读锁是不能直接升级为写锁的。而StampedLock提供了读锁和写锁相互转换的功能，使得该类支持更多的应用场景。  

&emsp; 编码示例：  

```java
class Point {
    private double x, y;// 成员变量
    private final StampedLock sl = new StampedLock();// 锁实例

    /**
     * 写锁writeLock
     * 添加增量，改变当前point坐标的位置。
     * 先获取到了写锁，然后对point坐标进行修改，然后释放锁。
     * 写锁writeLock是排它锁，保证了其他线程调用move函数时候会被阻塞，直到当前线程显示释放了该锁，也就是保证了对变量x,y操作的原子性。
     */
    void move(double deltaX, double deltaY) {
        long stamp = sl.writeLock();
        try {
            x += deltaX;
            y += deltaY;
        } finally {
            sl.unlockWrite(stamp);
        }
    }

    /**
     * 乐观读锁tryOptimisticRead
     * 计算当前位置到原点的距离
     */
    double distanceFromOrigin() {
        long stamp = sl.tryOptimisticRead();    // 尝试获取乐观读锁(1)
        double currentX = x, currentY = y;      // 将全部变量拷贝到方法体栈内(2)

        // 检查票据是否可用，即写锁有没有被占用(3)
        if (!sl.validate(stamp)) {
            // 如果写锁被抢占，即数据进行了写操作，则重新获取
            stamp = sl.readLock();// 获取悲观读锁(4)
            try {
                // 将全部变量拷贝到方法体栈内(5)
                currentX = x;
                currentY = y;
            } finally {
                sl.unlockRead(stamp);// 释放悲观读锁(6)
            }
        }

        return Math.sqrt(currentX * currentX + currentY * currentY);// 真正读取操作，返回计算结果(7)
    }

    /**
     * 悲观读锁readLock
     * 如果当前坐标为原点则移动到指定的位置
     */
    void moveIfAtOrigin(double newX, double newY) {
        long stamp = sl.readLock();// 获取悲观读锁(1)
        try {
            // 如果当前点在原点则移动(2)
            while (x == 0.0 && y == 0.0) {
                long ws = sl.tryConvertToWriteLock(stamp);// 尝试将获取的读锁升级为写锁(3)

                if (ws != 0L) {
                    // 升级成功，则更新票据，并设置坐标值，然后退出循环(4)
                    stamp = ws;
                    x = newX;
                    y = newY;
                    break;
                } else {
                    // 读锁升级写锁失败，则释放读锁，显示获取独占写锁，然后循环重试(5)
                    sl.unlockRead(stamp);
                    stamp = sl.writeLock();
                }
            }
        } finally {
            sl.unlock(stamp);// 释放写锁(6)
        }
    }
}
```