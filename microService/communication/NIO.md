---
title: NIO
date: 2020-05-30 00:00:00
tags:
    - 分布式通信
---

<!-- TOC -->

- [1. IO模型](#1-io模型)
    - [1.1. 同步、异步](#11-同步异步)
    - [1.2. 阻塞、非阻塞](#12-阻塞非阻塞)
    - [1.3. IO模型，BIO、NIO、AIO](#13-io模型bionioaio)
- [2. NIO](#2-nio)
    - [2.1. NIO基本组件：](#21-nio基本组件)
- [3. NIO缓冲区](#3-nio缓冲区)
    - [3.1. 缓冲区属性：](#31-缓冲区属性)
    - [3.2. 缓冲区API成员方法：](#32-缓冲区api成员方法)
        - [3.2.1. 缓冲区基本使用（注意模式切换）](#321-缓冲区基本使用注意模式切换)
        - [3.2.2. 其他方法：](#322-其他方法)
    - [3.3. 字节缓冲区详解：](#33-字节缓冲区详解)
- [4. NIO通道](#4-nio通道)
    - [4.1. 通道分类及创建通道](#41-通道分类及创建通道)
    - [4.2. 分散Scatter/聚集Gather](#42-分散scatter聚集gather)
        - [4.2.1. 分散读取](#421-分散读取)
        - [4.2.2. 聚集写入](#422-聚集写入)
        - [4.2.3. 基本散点/聚集示例](#423-基本散点聚集示例)
        - [4.2.4. 通道之间的数据传输](#424-通道之间的数据传输)
            - [4.2.4.1. FileChannel.transferFrom()方法](#4241-filechanneltransferfrom方法)
            - [4.2.4.2. FileChannel.transferTo()方法](#4242-filechanneltransferto方法)
            - [4.2.4.3. 基本通道到通道数据传输示例](#4243-基本通道到通道数据传输示例)
    - [4.3. 内存映射文件](#43-内存映射文件)
        - [4.3.1. MappedByteBuffer API：](#431-mappedbytebuffer-api)
        - [4.3.2. 示例：](#432-示例)
    - [4.4. 通道工具类](#44-通道工具类)
- [5. NIO选择器](#5-nio选择器)
    - [5.1. 选择器基础：选择器、可选择通道、选择键类](#51-选择器基础选择器可选择通道选择键类)
    - [5.2. 选择器教程](#52-选择器教程)
        - [5.2.1. 建立选择器（选择器、通道、选择键建立连接）](#521-建立选择器选择器通道选择键建立连接)
        - [5.2.2. 选择键的使用，SelectionKey类的API](#522-选择键的使用selectionkey类的api)
        - [5.2.3. 选择器的使用，selector类的API](#523-选择器的使用selector类的api)
        - [5.2.4. Selector完整实例](#524-selector完整实例)

<!-- /TOC -->


# 1. IO模型  

## 1.1. 同步、异步  
&emsp; 同步请求，A调用B，B的处理是同步的，在处理完之前不会通知A，只有处理完之后才会明确的通知A。  
&emsp; 异步请求，A调用B，B的处理是异步的，B在接到请求后先告诉A已经接到请求了，然后异步去处理，处理完之后通过回调等方式再通知A。  
&emsp; 所以说，同步和异步最大的区别就是被调用方的执行方式和返回时机。同步指的是被调用方做完事情之后再返回，异步指的是被调用方先返回，然后再做事情，做完之后再想办法通知调用方。  

## 1.2. 阻塞、非阻塞  
&emsp; 阻塞请求，A调用B，A一直等着B的返回，别的事情什么也不干。  
&emsp; 非阻塞请求，A调用B，A不用一直等着B的返回，先去处理其他事情。  
&emsp; 所以说，阻塞非阻塞最大的区别就是在被调用方返回结果之前的这段时间内，调用方是否一直等待。阻塞指的是调用方一直等待别的事情什么都不做。非阻塞指的是调用方先去忙别的事情。  

&emsp; ***阻塞、非阻塞和同步、异步的区别：***  
&emsp; 阻塞、非阻塞和同步、异步其实针对的对象是不一样的。阻塞、非阻塞说的是调用者，同步、异步说的是被调用者。  


## 1.3. IO模型，BIO、NIO、AIO  
&emsp; 同步阻塞IO（BIO，Block-IO）：用户进程在发起一个IO操作以后，必须等待IO操作的完成。只有当真正完成了IO操作以后，用户进程才能运行。Java传统的IO模型属于此种方式！  
&emsp; 同步非阻塞IO（NIO，Non-Block IO）：在此种方式下，用户进程发起一个IO操作以后便可返回做其它事情，但是用户进程需要时不时的询问IO操作是否就绪，这就要求用户进程不停的去询问，从而引入不必要的CPU资源浪费。其中目前Java的NIO就属于同步非阻塞IO。  
&emsp; 异步阻塞IO：此种方式下是指应用发起一个IO操作以后，不等待内核IO操作的完成，等内核完成IO操作以后会通知应用程序，这其实就是同步和异步最关键的区别，同步必须等待或者主动的去询问IO是否完成，那么为什么说是阻塞的呢？因为此时是通过select系统调用来完成的，而select函数本身的实现方式是阻塞的，而采用select函数有个好处就是它可以同时监听多个文件句柄，从而提高系统的并发性！  
&emsp; 异步非阻塞IO（AIO，Asynchronous IO）：在此种模式下，用户进程只需要发起一个IO操作然后立即返回，等IO操作真正的完成以后，应用程序会得到IO操作完成的通知，此时用户进程只需要对数据进行处理就好了，不需要进行实际的IO读写操作，因为真正的IO读取或者写入操作已经由内核完成了。  

&emsp; 适用场景：  
&emsp; BIO方式适用于连接数目比较小且固定的架构，这种方式对服务器资源要求比较高，并发局限于应用中，JDK1.4以前的唯一选择，但程序直观简单易理解。  
&emsp; NIO方式适用于连接数目多且连接比较短（轻操作）的架构，比如聊天服务器，并发局限于应用中，编程比较复杂，JDK1.4开始支持。  
&emsp; AIO方式适用于连接数目多且连接比较长（重操作）的架构，比如相册服务器，充分调用OS参与并发操作，编程比较复杂，JDK1.7开始支持。  


# 2. NIO  
## 2.1. NIO基本组件：  
&emsp; NIO支持面向缓冲的，基于通道的I/O操作方法。NIO读写是I/O的基本过程。读写操作中使用的核心部件有：Channels、Buffers、Selectors。  

&emsp; 通道和缓冲区：在标准I/O中，使用字符流和字节流。在NIO中使用通道和缓冲区。  
* Channel(通道)：在缓冲区和位于通道另一侧的服务之间进行数据传输，支持单向或双向传输，支持阻塞或非阻塞模式。  
* Buffer(缓冲区)：高效数据容器。本质上是一块内存区，用于数据的读写，NIO Buffer将其包裹并提供开发时常用的接口，便于数据操作。  
N&emsp; IO中的所有I/O都是通过一个通道开始的。数据总是从缓冲区写入通道，并从通道读取到缓冲区。从通道读取：创建一个缓冲区，然后请求通道读取数据。通道写入：创建一个缓冲区，填充数据，并要求通道写入数据。  
&emsp; 通道列表：  

|通道类型 |作用|
|---|---|
|FileChannel	|从文件中读写数据|
|SocketChannel	|能通过TCP读写网络中的数据|
|DatagramChannel	|能通过UDP读写网络中的数据|
|ServerSocketChannel	|可以监听新进来的TCP连接，对每一个新进来的连接都会创建一个SocketChannel|

&emsp; 缓冲列表：  
&emsp; 在NIO中使用的核心缓冲区如下：CharBuffer、DoubleBuffer、IntBuffer、LongBuffer、ByteBuffer、ShortBuffer、FloatBuffer。上述缓冲区覆盖了通过I/O发送的基本数据类型：characters，double，int，long，byte，short和float。

* Selectors（选择器）  
&emsp; Selector(IO复用器/选择器)：多路复用的重要组成部分，检查一个或多个Channel(通道)是否是可读、写状态，实现单线程管理多Channel(通道)，优于使用多线程或线程池产生的系统资源开销。如果应用程序有多个通道(连接)打开，但每个连接的流量都很低，则可考虑使用Selectors。

# 3. NIO缓冲区  
&emsp; 缓冲区实际上是一个容器对象，其实就是一个数组，是一块可以写入数据，然后可以从中读取数据的内存。这块内存被包装成NIO Buffer对象，并提供了一组方法，用来方便的访问该块内存。   
&emsp; 在NIO库中，所有数据都是用缓冲区处理的。在读取数据时，它是直接读到缓冲区中的；在写入数据时，它也是写入到缓冲区中的；任何时候访问NIO中的数据，都是将它放到缓冲区中。而在面向流I/O系统中，所有数据都是直接写入或者直接将数据读取到Stream对象中。  

## 3.1. 缓冲区属性：  
&emsp; 缓冲区4个属性：  

```
// Invariants: mark <= position <= limit <= capacity
private int mark = -1;
private int position = 0;
private int limit;
private int capacity;
```
&emsp; 在写模式和读模式（先写后读）下，position和limit的位置有所不同，见下图：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/communication/NIO-1.png)  
&emsp; 容量（Capacity)：缓冲区能够容纳的数据元素的最大数量。这一容量在缓冲区创建时被设定，并且不能被改变。Buffer满了，需要将其清空（通过读数据或者清除数据）才能继续写数据。  
&emsp; 上界（Limit)：缓冲区中现存元素的计数。代表最多能写入或者读取多少单位的数据。写模式下等于最大容量capacity。从写模式切换到读模式时，等于position，然后再将position置为0。所以，读模式下，limit表示最大可读取的数据量，这个值与实际写入的数量相等。读模式下：最多能往Buffer里读多少数据，写模式下：最多能往Buffer里写多少数据，limit大小跟数量大小和capacity有关。  
&emsp; 位置（Position)：下一个要被读或写的元素的索引。位置会自动由相应的和函数更新。记录当前读取或者写入的位置。写模式下等于当前写入的单位数据数量。从写模式切换到读模式时，置为0。在读的过程中等于当前读取单位数据的数量。  
&emsp; 标记（Mark)：一个备忘位置。调用来设定mark = postion。调用设定position = mark。标记在设定前是未定义的(undefined)。
这四个属性之间遵循以下关系：0 <=mark <=position <=limit <=capacity。  

&emsp; ***图解：***  
1. 新建一个大小为8个字节的缓冲区，此时position为 0，而limit = capacity = 8。capacity变量不会改变。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/communication/NIO-2.png)  
2. 从输入通道中读取 5 个字节数据写入缓冲区中，此时position为5，limit保持不变。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/communication/NIO-3.png)  
3. 在将缓冲区的数据写到输出通道之前，需要先调用 flip() 方法，这个方法将 limit 设置为当前 position，并将 position 设置为 0。
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/communication/NIO-4.png)  
4. 从缓冲区中取 4 个字节到输出缓冲中，此时 position 设为 4。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/communication/NIO-5.png)  
5. 最后需要调用 clear() 方法来清空缓冲区，此时position和limit都被设置为最初位置。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/communication/NIO-6.png)  

## 3.2. 缓冲区API成员方法：  
&emsp; 缓冲区API： 

|名称| 作用|
|---|---|
|allocate()|	分配一块缓冲区|
|put()	|向缓冲区写数据|
|get()	|向缓冲区读数据|
|filp()	|将缓冲区从写模式切换到读模式|
|clear()	|从读模式切换到写模式，不会清空数据，但后续写数据会覆盖原来的数据，即使有部分数据没有读，也会被遗忘。|
|compact()	|从读数据切换到写模式，数据不会被清空，会将所有未读的数据copy到缓冲区头部，后续写数据不会覆盖，而是在这些数据之后写数据|
|mark() 	|对position做出标记，配合reset使用|
|reset()	|将position置为标记值|  
 

 ### 创建缓冲区：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/communication/NIO-7.png)  
&emsp; 有七种主要的缓冲区类，每一种都具有一种Java语言中的非布尔类型的原始类型数据。（第8种也在图中显示出来，MappedByteBuffer是ByteBuffer专门用于内存映射文件的一种特例。）  
&emsp; 创建缓冲区的关键函数（对所有的缓冲区类通用，按照需要替换类名）：  

```
public abstract class CharBuffer extends Buffer implements CharSequence, Comparable {
    // This is a partial API listing 
    public static CharBuffer allocate (int capacity);
    public static CharBuffer wrap (char [] array);
    public static CharBuffer wrap (char [] array, int offset, int length);
    public final boolean hasArray( );
    public final char [] array( );
    public final int arrayOffset( );
}
```
&emsp; 创建缓冲区详解：  
&emsp; 新的缓冲区是由分配或包装操作创建的。分配操作创建一个缓冲区对象并分配一个私有的空间来储存容量大小的数据元素。包装操作创建一个缓冲区对象但是不分配任何空间来储存数 据元素。它使用提供的数组作为存储空间来储存缓冲区中的数据元素。  
&emsp; 要分配一个容量为100个char变量的Charbuffer:  

    CharBuffer charBuffer = CharBuffer.allocate (100);  
这&emsp; 段代码隐含地从堆空间中分配了一个char型数组作为备份存储器来储存100个char变量。  
&emsp; 如果想提供自己的数组用做缓冲区的备份存储器，调用wrap()函数:  

    char [] myArray = new char [100];  
    CharBuffer charbuffer = CharBuffer.wrap (myArray);  
&emsp; 这段代码构造了一个新的缓冲区对象，但数据元素会存在于数组中。这意味着通过调用put()函数造成的对缓冲区的改动会直接影响这个数组，而且对这个数组的任何改动也会对这 个缓冲区对象可见。带有offset和length作为参数的wrap()函数版本则会构造一个按照提供的offset和length参数值初始化位置和上界的缓冲区。这样做：  

    CharBuffer charbuffer = CharBuffer.wrap (myArray, 12, 42);
&emsp; 创建建了一个position值为12, limit值为54,容量为myArray.length的缓冲区。  
&emsp; 通过allocate()或者wrap()函数创建的缓冲区通常都是间接的。    

### 3.2.1. 缓冲区基本使用（注意模式切换）  
&emsp; 使用Buffer读写数据一般遵循以下四个步骤：  
1. 写入数据到Buffer；  
2. 调用flip()方法；  
3. 从Buffer中读取数据；  
4. 调用clear()方法或者compact()方法。  

&emsp; 当向Buffer写入数据时，Buffer会记录写了多少数据。一旦要读取数据，需要通过flip()方法将Buffer从写模式切换到读模式。在读模式下，可以读取之前写入到Buffer的所有数据。  
&emsp; 一旦读完了所有的数据，就需要清空缓冲区，让它可以再次被写入。有两种方式能清空缓冲区：调用clear()或compact()方法。clear()方法会清空整个缓冲区。compact()方法只会清除已经读过的数据。任何未读的数据都被移到缓冲区的起始处，新写入的数据将放到缓冲区未读数据的后面。  
&emsp; 总结：Client向Buffer写入数据后，调用flip()将Buffer由写模式更改为读模式，此时Channel可以读取Buffer内数据，读取完成后可调用clear()或compact()重置缓冲区并允许数据写入。  

```
RandomAccessFile aFile = new RandomAccessFile("data/nio-data.txt", "rw");
FileChannel inChannel = aFile.getChannel();

//create buffer with capacity of 48 bytes  
ByteBuffer buf = ByteBuffer.allocate(48);

int bytesRead = inChannel.read(buf); //read into buffer.  
while (bytesRead != -1) {
    buf.flip();  //make buffer ready for read  
    
    while(buf.hasRemaining()){
        System.out.print((char) buf.get()); // read 1 byte at a time  
    }
    buf.clear(); //make buffer ready for writing  
    bytesRead = inChannel.read(buf);
}
aFile.close(); 
```

&emsp; ***向Buffer中写数据：*** 写数据到Buffer有两种方式：  
* 从Channel写到Buffer。
* 通过Buffer的put()方法写到Buffer里。  
&emsp; 从Channel写到Buffer的例子  

```
int bytesRead = inChannel.read(buf); //read into buffer.  
```
&emsp; 通过put方法写Buffer的例子： 

```
buf.put(127); 
```

&emsp; ***flip()方法：***  
&emsp; flip()方法将Buffer从写模式切换到读模式。调用flip()方法会将position设回0，并将limit设置成之前position的值。  
换句话说，position现在用于标记读的位置，limit表示之前写进了多少个byte、char等 —— 现在能读取多少个byte、char等。   

&emsp; ***从Buffer中读取数据：*** 从Buffer中读取数据有两种方式：  
* 从Buffer读取数据到Channel。
* 使用get()方法从Buffer中读取数据。
从Buffer读取数据到Channel的例子： 

```
//read from buffer into channel.  
int bytesWritten = inChannel.write(buf); 
```
&emsp; 使用get()方法从Buffer中读取数据的例子  

```
byte aByte = buf.get(); 
```

### 3.2.2. 其他方法：
&emsp; ***rewind()方法***  
&emsp; Buffer.rewind()将position设回0，所以可以重读Buffer中的所有数据。limit保持不变，仍然表示能从Buffer中读取多少个元素（byte、char等）。  

&emsp; ***clear()与compact()方法***  
&emsp; 一旦读完Buffer中的数据，需要让Buffer准备好再次被写入。可以通过clear()或compact()方法来完成。  
&emsp; 如果调用的是clear()方法，position将被设回0，limit被设置成capacity的值。换句话说，Buffer被清空了。Buffer中的数据并未清除，这些标记表示可以从哪里开始往Buffer里写数据。  
&emsp; 如果Buffer中有一些未读的数据，调用clear()方法，数据将“被遗忘”，意味着不再有任何标记会告诉你哪些数据被读过，哪些还没有。  
&emsp; 如果Buffer中仍有未读的数据，且后续还需要这些数据，但是此时想要先先写些数据，那么使用compact()方法。  
&emsp; compact()方法将所有未读的数据拷贝到Buffer起始处。然后将position设到最后一个未读元素正后面。limit属性依然像clear()方法一样，设置成capacity。现在Buffer准备好写数据了，但是不会覆盖未读的数据。   

&emsp; ***mark()与reset()方法***  
&emsp; 通过调用Buffer.mark()方法，可以标记Buffer中的一个特定position。之后可以通过调用Buffer.reset()方法恢复到这个position。例如： 

```
buffer.mark();  
//call buffer.get() a couple of times, e.g. during parsing.  
buffer.reset();  //set position back to mark. 
```

&emsp; ***equals()***  
&emsp; 当满足下列条件时，表示两个Buffer相等：  
&emsp; 1). 有相同的类型（byte、char、int等）。  
&emsp; 2). Buffer中剩余的byte、char等的个数相等。  
&emsp; 3). Buffer中所有剩余的byte、char等都相同。  
&emsp; equals只是比较Buffer的一部分，不是每一个在它里面的元素都比较。实际上，它只比较Buffer中的剩余元素。  

&emsp; ***compareTo()方法***  
&emsp; compareTo()方法比较两个Buffer的剩余元素(byte、char等)， 如果满足下列条件，则认为一个Buffer“小于”另一个Buffer：  
&emsp; 第一个不相等的元素小于另一个Buffer中对应的元素。  
&emsp; 所有元素都相等，但第一个Buffer比另一个先耗尽(第一个Buffer的元素个数比另一个少)。  
&emsp; （译注：剩余元素是从 position到limit之间的元素） 

## 3.3. 字节缓冲区详解：  
&emsp; 所有的基本数据类型都有相应的缓冲区类 (布尔型除外），但字节缓冲区有自己的独特之处。字节是操作系统及其I/O设备使用的基本数据类型。
为什么Buffer能更方便的操作？Buffer记录了每个操作点。同时ByteBuffer为了提高性能，提供了两种实现：堆内存和直接内存。  
&emsp; 使用直接内存的优势：  
&emsp; 1).少一次内存拷贝：在常规文件IO或者网络IO的情况下，需要调用操作系统的API，会先把堆内存的数据复制到堆外，至于为什么要复制一份数据到堆外，JVM有GC机制，在GC过程中，会移动对象的位置，这样操作系统在读取或写入数据就找不到数据了。但这样一来，显然增加了一次内存拷贝的过程，如果直接通过直接内存的方式，省略掉这次非必须的内存拷贝，可以达到优化性能的目的。创建直接内存的API：  

```
public static ByteBuffer allocateDirect(int capacity) {
    return new DirectByteBuffer(capacity);
}
```
&emsp; 2).降低GC压力：如果直接使用直接内存（堆外内存）呢？首先申请一块不受GC管理的堆外内存，但需要手动回收堆外内存。在java.nio.DirectByteBuffer中，有一个虚引用的Cleaner对象，执行GC时会触发Deallocator回收内存。  

```
private static class Deallocator implements Runnable {
 
    public void run() {
        if (address == 0) {
            // Paranoia
            return;
        }
        unsafe.freeMemory(address);
        address = 0;
        Bits.unreserveMemory(size, capacity);
    }

}
```
&emsp; 虽然堆外内存好处很多，但还是建议在网络传输，文件读取等大数据量的IO密集操作时才使用，分配一些大文件大数据读取时才使用。同时设置MaxDirectMemorySize避免耗尽整个机器内存。


# 4. NIO通道  
&emsp; 实体--->通道--->缓冲区。Channel用于在字节缓冲区和位于通道另一侧的实体（通常是一个文件或套接字）之间有效地传输数据。它从一个实体读取数据，并将其放在缓冲区块中以供消费。  
&emsp; 通道与操作系统文件描述符具有一对一关系，用于提供平台独立操作功能。  
&emsp; Channel(通道)与Stream(流)的不同之处在于通道是双向的，流只能在一个方向上操作(一个流必须是InputStream或者OutputStream的子类)，而通道可以用于读，写或者二者同时进行，最关键的是可以和多路复用器结合起来，提供状态位，多路复用器可识别Channel所处的状态。  
&emsp; 通道可以以阻塞（Wocfcwg)或非阻塞（wowWochwg)模式运行。非阻塞模式的通道永远不会让调用的线程休眠。请求的操作要么立即完成，要么返回一个结果表明未进行任何操作。只有面向流的（stream-oriented)的通道，如sockets和pipes才能使用非阻塞模式。  

## 4.1. 通道分类及创建通道  
&emsp; I/O可以分为广义的两大类别：File I/O和Stream I/O。那么相应地有两种类型的通道，它们是文件(file)通道和套接字(socket)通道。包含一个FileChannel类和三个socket通道类：SocketChannel、ServerSocketChannel和DatagramChannel。  
&emsp; Channel接口的完整源码：  

```
package java.nio.channels;
public interface Channel{
    public boolean isclose();
    public void Open() throws IOException;
}
```
&emsp; 所有通道只有两个常用操作：  
* 检查通道是否关闭(isclose())  
* 打开关闭通道(close())  

1. FileChannel：文件通道用于从文件读取数据。  
&emsp; jdk1.7通过使用一个InputStream、OutputStream或RandomAccessFile来获取一个FileChannel实例。  

```
//通过RandomAccessFile打开FileChannel的示例
RandomAccessFile aFile = new RandomAccessFile("data/nio-data.txt", "rw");
FileChannel inChannel = aFile.getChannel(); 

//jdk1.7以前NIO的获取通道的写法
FileInputStream fis = new FileInputStream("./resource/Java NIO.pdf");
FileOutputStream fos = new FileOutputStream("./resource/demoCopyTest.jpeg");
//1.获取通道
FileChannel inChannel = fis.getChannel();
FileChannel outChannel = fos.getChannel();
```
&emsp; JDK1.7以后：直接通过Open方法获得到channel。  

```
@Test
public void test2() {
    long start = System.currentTimeMillis();
    FileChannel inChannel = null;
    FileChannel outChannel = null;
    try {
        //建立通道
        inChannel = FileChannel.open(Paths.get("./resource/Java NIO.pdf"), StandardOpenOption.READ);
        outChannel = FileChannel.open(Paths.get("./resource/Java NIOCopyTest2.pdf"), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        //inChannel.transferTo(0, inChannel.size(), outChannel);
        outChannel.transferFrom(inChannel, 0, inChannel.size());
    } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    } finally {
        try {
            if (outChannel != null) {
                outChannel.close();
            }
            if (inChannel != null) {
                inChannel.close();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    long end = System.currentTimeMillis();
    System.out.println("耗费时间直接缓冲区" + (end - start));
}
```
2. SocketChannel：数据报通道可以通过TCP(传输控制协议)通过网络读取和写入数据。它还使用工厂方法来创建新对象。  
&emsp; 用于打开SocketChannel的语法：  

```
SocketChannel ch = SocketChannel.open();
ch.connect(new InetSocketAddress("somehost", someport));
```
&emsp; 用于关闭SocketChannel的语法：  

```
SocketChannel ch = SocketChannel.close();
ch.connect(new InetSocketAddress("somehost", someport));
```
3. DatagramChannel：数据报通道可以通过UDP(用户数据报协议)通过网络读取和写入数据。它使用工厂方法来创建新对象。  
&emsp; 下面是打开DatagramChannel的语法：  
```
DatagramChannel ch = DatagramChannel.open();
```
&emsp; 用于关闭DatagramChannel的语法：  
```
DatagramChannel ch = DatagramChannel.close();
```
4. ServerSocketChannel：ServerSocketChannel允许用户监听传入的TCP连接，与Web服务器相同。对于每个传入连接，都会为连接创建一个SocketChannel。  
&emsp; 下面是打开ServerSocketChannel的语法：  

```
ServerSocketChannel ch = ServerSocketChannel.open();
ch.socket().bind (new InetSocketAddress (somelocalport));
```
&emsp; 下面是关闭ServerSocketChannel的语法：  

```
ServerSocketChannel ch = ServerSocketChannel.close();
ch.socket().bind (new InetSocketAddress (somelocalport));
```

## 4.2. 分散Scatter/聚集Gather  
&emsp; 在Java NIO中，通道提供了称为分散/聚集（或向量I/O）的重要功能。使用单个write()函数将字节从一组缓冲区写入流，并且可以使用单个read()函数将字节从流读取到一组缓冲区中。  
&emsp; 对于一个write操作而言，数据是从几个缓冲区按顺序抽取（称为gather)并沿着通道发送的。对于read操作而言，从通道读取的数据会按顺序被散布（称为scatter)到多个缓冲区，将每个缓冲区填满直至通道中的数据或者缓冲区的最大空间被消耗完。  
&emsp; 分散（scatter）从Channel中读取是指在读操作时将读取的数据写入多个buffer中。因此，Channel将从Channel中读取的数据“分散（scatter）”到多个Buffer中。  
&emsp; 聚集（gather）写入Channel是指在写操作时将多个buffer的数据写入同一个Channel，因此，Channel将多个Buffer中的数据“聚集（gather）”后发送到Channel。  
&emsp; 分散scatter/聚集gather经常用于需要将传输的数据分开处理的场合。例如传输一个由消息头和消息体组成的消息，可能会将消息体和消息头分散到不同的buffer中，这样就可以方便的处理消息头和消息体。   

### 4.2.1. 分散读取   
&emsp; “分散读取”用于将数据从单个通道读取多个缓冲区中的数据。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/communication/NIO-8.png)  
&emsp; Rread()的API：  

```
public interface ScatteringByteChannel extends ReadableByteChannel{
    public long read (ByteBuffer [] argv) throws IOException;
    public long read (ByteBuffer [] argv, int length, int offset) throws IOException;
}
```
&emsp; 执行分射读取操作的代码示例：  

```
ByteBuffer header = ByteBuffer.allocate(128);
ByteBuffer body   = ByteBuffer.allocate(1024);
ByteBuffer[] bufferArray = { header, body };  
  
channel.read(bufferArray); 
```
&emsp; 注意buffer首先被插入到数组，然后再将数组作为channel.read() 的输入参数。read()方法按照buffer在数组中的顺序将从channel中读取的数据写入到buffer，当一个buffer被写满后，channel紧接着向另一个buffer中写。  
&emsp; Scattering Reads在移动下一个buffer前，必须填满当前的buffer，这也意味着它不适用于动态消息(消息大小不固定)。换句话说，如果存在消息头和消息体，消息头必须完成填充（例如 128byte），Scattering Reads才能正常工作。   

### 4.2.2. 聚集写入  
&emsp; “聚集写入”用于将数据从多个缓冲区写入单个通道。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/communication/NIO-9.png)  
&emsp; write()的API：  

```
public interface GatheringByteChannel extends WritableByteChannel{
    public long write(ByteBuffer[] argv) throws IOException;
    public long write(ByteBuffer[] argv, int length, int offset) throws IOException;
}
```
&emsp; 执行聚集写入操作的代码示例：  

```
ByteBuffer header = ByteBuffer.allocate(128);
ByteBuffer body   = ByteBuffer.allocate(1024);

//write data into buffers  
ByteBuffer[] bufferArray = { header, body };  
channel.write(bufferArray);  
```
&emsp; buffers数组是write()方法的入参，write()方法会按照buffer在数组中的顺序，将数据写入到channel，注意只有position和limit之间的数据才会被写入。因此，如果一个buffer的容量为128byte，但是仅仅包含58byte的数据，那么这58byte的数据将被写入到channel中。因此与Scattering Reads相反，Gathering Writes能较好的处理动态消息。  

### 4.2.3. 基本散点/聚集示例  
&emsp; 下面来看看两个缓冲区的简单例子。第一个缓冲区保存随机数，第二个缓冲区使用分散/聚集机制保存写入的数据：  
```
package com.yiibai;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.GatheringByteChannel;

public class ScatterGatherIO {
    public static void main(String params[]) {
        String data = "Scattering and Gathering example shown in yiibai.com";
        gatherBytes(data);
        scatterBytes();
    }

    /*
     * gatherBytes() is used for reading the bytes from the buffers and write it
     * to a file channel.
     */
    public static void gatherBytes(String data) {
        String relativelyPath = System.getProperty("user.dir");
        // The First Buffer is used for holding a random number
        ByteBuffer buffer1 = ByteBuffer.allocate(8);
        // The Second Buffer is used for holding a data that we want to write
        ByteBuffer buffer2 = ByteBuffer.allocate(400);
        buffer1.asIntBuffer().put(420);
        buffer2.asCharBuffer().put(data);
        GatheringByteChannel gatherer = createChannelInstance(relativelyPath+"/testout.txt", true);
        // Write the data into file
        try {
            gatherer.write(new ByteBuffer[] { buffer1, buffer2 });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * scatterBytes() is used for reading the bytes from a file channel into a
     * set of buffers.
     */
    public static void scatterBytes() {
        String relativelyPath = System.getProperty("user.dir");
        // The First Buffer is used for holding a random number
        ByteBuffer buffer1 = ByteBuffer.allocate(8);
        // The Second Buffer is used for holding a data that we want to write
        ByteBuffer buffer2 = ByteBuffer.allocate(400);
        ScatteringByteChannel scatter = createChannelInstance(relativelyPath+"/testout.txt", false);
        // Reading a data from the channel
        try {
            scatter.read(new ByteBuffer[] { buffer1, buffer2 });
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Read the two buffers seperately
        buffer1.rewind();
        buffer2.rewind();

        int bufferOne = buffer1.asIntBuffer().get();
        String bufferTwo = buffer2.asCharBuffer().toString();
        // Verification of content
        System.out.println(bufferOne);
        System.out.println(bufferTwo);
    }

    public static FileChannel createChannelInstance(String file, boolean isOutput) {
        FileChannel FChannel = null;
        try {
            if (isOutput) {
                FChannel = new FileOutputStream(file).getChannel();
            } else {
                FChannel = new FileInputStream(file).getChannel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return FChannel;
    }
}
```
&emsp; 在上述程序中，第一个缓冲区在控制台上打印随机输出，第二个缓冲区在控制台上打印“Scattering and Gathering example shown in yiibai.com”。它还用“Scattering and Gathering example shown in yiibai.com”替换testout.txt文件的内容。  

    420
    Scattering and Gathering example shown in yiibai.com  

### 4.2.4. 通道之间的数据传输  
&emsp; 在Java NIO中，如果两个通道中有一个是FileChannel，那可以直接将数据从一个channel传输到另外一个channel。 通道之间的数据传输在FileChannel类中的两种方法是：FileChannel.transferTo()方法、FileChannel.transferFrom()方法。  

#### 4.2.4.1. FileChannel.transferFrom()方法  
&emsp; transferFrom()的API：  

```
public abstract class Channel extends AbstractChannel{
    public abstract long transferFrom (ReadableByteChannel src, long position, long count);
}
```
&emsp; 方法的输入参数position表示从position处开始向目标文件写入数据，count表示最多传输的字节数。如果源通道的剩余空间小于count个字节，则所传输的字节数要小于请求的字节数。此外要注意，在SoketChannel的实现中，SocketChannel只会传输此刻准备好的数据（可能不足count字节）。因此，SocketChannel可能不会将请求的所有数据(count个字节)全部传输到FileChannel中。  

&emsp; transferFrom()方法可以将数据从源通道传输到FileChannel中。transferFrom()方法的示例：  
```
RandomAccessFile fromFile = new RandomAccessFile("fromFile.txt", "rw");
FileChannel  fromChannel = fromFile.getChannel();

RandomAccessFile toFile = new RandomAccessFile("toFile.txt", "rw");
FileChannel  toChannel = toFile.getChannel();

long position = 0;
long count = fromChannel.size();  

toChannel.transferFrom(position, count, fromChannel); 
```

#### 4.2.4.2. FileChannel.transferTo()方法  
&emsp; transferTo()方法的API：  

```
public abstract class Channel extends AbstractChannel{
    public abstract long transferTo (long position, long count, WritableByteChannel target);
}
```
&emsp; 上面所说的关于SocketChannel的问题在transferTo()方法中同样存在。SocketChannel会一直传输数据直到目标buffer被填满。  

&emsp; transferTo()方法将数据从FileChannel传输到其他的channel中。用来从FileChannel到其他通道的数据传输。transferTo()方法的示例：  
```
RandomAccessFile fromFile = new RandomAccessFile("fromFile.txt", "rw");
FileChannel fromChannel = fromFile.getChannel();

RandomAccessFile toFile = new RandomAccessFile("toFile.txt", "rw");
FileChannel toChannel = toFile.getChannel();

long position = 0;
long count = fromChannel.size();  
  
fromChannel.transferTo(position, count, toChannel);  
```

#### 4.2.4.3. 基本通道到通道数据传输示例  
&emsp; 下面来看看从4个不同文件读取文件内容的简单示例，并将它们的组合输出写入第五个文件：  

```
package com.yiibai;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.FileChannel;

public class TransferDemo {
    public static void main(String[] argv) throws Exception {
        String relativelyPath = System.getProperty("user.dir");
        // Path of Input files
        String[] iF = new String[] { relativelyPath + "/input1.txt", relativelyPath + "/input2.txt",
                relativelyPath + "/input3.txt", relativelyPath + "/input4.txt" };
        // Path of Output file and contents will be written in this file
        String oF = relativelyPath + "/combine_output.txt";
        // Acquired the channel for output file
        FileOutputStream output = new FileOutputStream(new File(oF));
        WritableByteChannel targetChannel = output.getChannel();
        for (int j = 0; j < iF.length; j++) {
            // Get the channel for input files
            FileInputStream input = new FileInputStream(iF[j]);
            FileChannel inputChannel = input.getChannel();
            // The data is tranfer from input channel to output channel
            inputChannel.transferTo(0, inputChannel.size(), targetChannel);
            // close an input channel
            inputChannel.close();
            input.close();
        }
        // close the target channel
        targetChannel.close();
        output.close();
        System.out.println("All jobs done...");
    }
}
```
&emsp; 在上述程序中，将4个不同的文件(即input1.txt，input2.txt，input3.txt和input4.txt)的内容读取并将其组合的输出写入第五个文件，即：combine_output.txt文件的中。combine_output.txt文件的内容如下  
```
this is content from input1.txt
this is content from input2.txt
this is content from input3.txt
this is content from input4.txt
```

## 4.3. 内存映射文件  
&emsp; 通道映射技术：一种快速读写技术。  
&emsp; 1).用户进程将文件数据视为内存，不需要发出read()或write()系统调用。  
&emsp; 2).可以映射非常大的文件（文件可能无法全部读入内存），而不消耗大量内存来复制数据。  
&emsp; java nio提供的FileChannel提供了map()方法，该方法可以在一个打开的文件和MappedByteBuffer之间建立一个虚拟内存映射，MappedByteBuffer继承于ByteBuffer，类似于一个基于内存的缓冲区，只不过该对象的数据元素存储在磁盘的一个文件中；  

### 4.3.1. MappedByteBuffer API：  
&emsp; NIO中内存映射主要用到以下两个类：java.nio.MappedByteBuffer、java.nio.channels.FileChannel。  
1. 通过FileChannel.map()获取MappedByteBuffer  

```
public abstract MappedByteBuffer map(MapMode mode, long position, long size) throws IOException;  
```
&emsp; 调用FileChannel类的map方法进行内存映射，第一个参数设置映射模式，现在支持3种模式：  
* FileChannel.MapMode.READ_ONLY：只读缓冲区，在缓冲区中如果发生写操作则会产生ReadOnlyBufferException；  
* FileChannel.MapMode.READ_WRITE：读写缓冲区，任何时刻如果通过内存映射的方式修改了文件则立刻会对磁盘上的文件执行相应的修改操作。别的进程如果也共享了同一个映射，则也会同步看到变化。而不是像标准IO那样每个进程有各自的内核缓冲区，比如JAVA代码中，没有执行IO输出流的flush()或者close()操作，那么对文件的修改不会更新到磁盘去，除非进程运行结束；  
* FileChannel.MapMode.PRIVATE：可写缓冲区，但任何修改是缓冲区私有的，不会回到文件中。  
&emsp; PRIVATE模式表示写时拷贝的映射，意味着通过put()方法所做的任何修改都会导致产生一个私有的数据拷贝并且该拷贝中的数据只有MappedByteBuffer实例可以看到；  
&emsp; 该过程不会对底层文件做任何修改，而且一旦缓冲区被施以垃圾收集动作（garbage collected），那些修改都会丢失；  

2. MappedByteBuffer是ByteBuffer的子类，其扩充了三个方法：  
&emsp; force()：缓冲区是READ_WRITE模式下，此方法对缓冲区内容的修改强行写入文件；  
&emsp; load()：将缓冲区的内容载入内存，并返回该缓冲区的引用；  
&emsp; isLoaded()：如果缓冲区的内容在物理内存中，则返回真，否则返回假；  

### 4.3.2. 示例：  
&emsp; 下面通过一个例子来看一下内存映射读取文件和普通的IO流读取一个150M大文件的速度对比：  
```
public class MemMap {
    public static void main(String[] args) {
        try {
            RandomAccessFile file = new RandomAccessFile("c://1.pdf","rw");
            FileChannel channel = file.getChannel();
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY,0,channel.size());
            ByteBuffer buffer1 = ByteBuffer.allocate(1024);
            byte[] b = new byte[1024];

            long len = file.length();
            long startTime = System.currentTimeMillis();
            //读取内存映射文件
            for(int i=0;i<file.length();i+=1024*10){
                if (len - i > 1024) {
                    buffer.get(b);
                } else {
                    buffer.get(new byte[(int)(len - i)]);
                }
            }
            long endTime = System.currentTimeMillis();
            System.out.println("使用内存映射方式读取文件总耗时： "+(endTime - startTime));

            //普通IO流方式
            long startTime1 = System.currentTimeMillis();
            while(channel.read(buffer1) > 0){
                buffer1.flip();
                buffer1.clear();
            }

            long endTime1 = System.currentTimeMillis();
            System.out.println("使用普通IO流方式读取文件总耗时： "+(endTime1 - startTime1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```
&emsp; 实验结果为：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/communication/NIO-10.png)  

&emsp; 复制文件的例子：  
```
public class MemMapReadWrite {

    private static int len;

    /**
     * 读文件
     */
    public static ByteBuffer readFile(String fileName) {
        try {
            RandomAccessFile file = new RandomAccessFile(fileName, "rw");
            len = (int) file.length();
            FileChannel channel = file.getChannel();
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, len);

            return buffer.get(new byte[(int) file.length()]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 写文件
     */
    public static void writeFile(String readFileName, String writeFileName) {
        try {
            RandomAccessFile file = new RandomAccessFile(writeFileName, "rw");
            FileChannel channel = file.getChannel();
            ByteBuffer buffer = readFile(readFileName);

            MappedByteBuffer bytebuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, len);
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < len; i++) {
                bytebuffer.put(i, buffer.get(i));
            }
            bytebuffer.flip();
            long endTime = System.currentTimeMillis();
            System.out.println("写文件耗时： " + (endTime - startTime));


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String readFileName = "c://1.pdf";
        String writeFileName = "c://2.pdf";

        writeFile(readFileName, writeFileName);
    }
}
```
&emsp; 结果为：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/communication/NIO-11.png)  



## 4.4. 通道工具类  
&emsp; NIO通道提供了一个全新的类似流的I/O隐喻，但是字节流以及字符读写器仍然存在并被广泛使用。一个工具类java.nio.channels.Channels定义了几种静态的工厂方法以使通道可以更加容易地同流和读写器互联。  
&emsp; java.nio.channels.Channels工具方法汇总：  

|方法	|返回	描述|
|---|---|---|
|newChannel (InputStream in)	|ReadableByteChannel	|返回一个将从给定的输入流读取数据的通道。|
|newChannel (OutputStream out)	|WritableByteChannel	|返回一个将向给定的输出流写入数据的通道。|
|newInputStream (ReadableByteChannel ch)	|InputStream|	返回一个将从给定的通道读取字节的流。|
|newOutputStream (WritableByteChannel ch)	|OutputStream	|返回一个将向给定的通道写入字节的流。|
|newReader (ReadableByteChannel ch, CharsetDecoder dec, int minBufferCap)	|Reader	|返回一个reader，它将从给定的通道读取字节并依据提供的CharsetDecoder对读取到的字节进行解码。|
|newReader (ReadableByteChannel ch, CharsetDecoder dec, int minBufferCap)	|Reader	|返回一个reader，它将从给定的通道读取字节并依据提供的CharsetDecoder对读取到的字节进行解码。|
|newWriter (WritableByteChannel ch, CharsetEncoder dec, int minBufferCap)	|Writer	|返回一个writer，它将使用提供的CharsetEncoder对象对字符编码并写到给定的通道中。|
|newWriter (WritableByteChannel ch, String csName)	|Writer	|返回一个writer，它将依据提供的字符集名称对字符编码并写到给定的通道中。|  

&emsp; 常规的流仅传输字节，readers和writers则作用于字符数据。表中前四行描述了用于连接流、通道的方法。因为流和通道都是运行在字节流基础上的，所以这四个方法直接将流封装在通道上，反之亦然。  
&emsp; Readers和Writers运行在字符的基础上，在Java的世界里字符同字节是完全不同的。将一个通道（仅了解字节）连接到一个reader或writer需要一个中间对话来处理字节/字符（byte/char）阻抗失配。为此，表中的后半部分描述的工厂方法使用了字符集编码器和解码器。  
&emsp; 这些方法返回的包封Channel对象可能会也可能不会实现 InterruptibleChannel 接口，它们也可能不是从SelectableChannel引申而来。因此，可能无法将这些包封通道同java.nio.channels包中定义的其他通道类型交换使用。细节是依赖实现的。如果您的程序依赖这些语义，那么请使用操作器实例测试一下返回的通道对象。  



# 5. NIO选择器  

&emsp; Selector是NIO多路复用的重要组成部分。它负责检查一个或多个Channel(通道)是否是可读、写状态，实现单线程管理多Channel(通道)，优于使用多线程或线程池产生的系统资源开销。 

## 5.1. 选择器基础：选择器、可选择通道、选择键类   
&emsp; 选择器(Selector)使用单个线程处理多个通道。 流程结构如图：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/communication/NIO-12.png)  
* 选择器(Selector)：在Java NIO中，选择器(Selector)是可选择通道的多路复用器。选择器类管理着一个被注册的通道集合的信息和它们的就绪状态。通道是和选择器一起被注册的，并且使用选择器来更新通道的就绪状态。当这样使用的时候，可以选择将被激发的线程挂起，直到有就绪的的通道。  
当Channel(通道)注册至Selector内后，便会产生一个对应的SelectionKey，存储与此Channel相关的数据。
* 可选择通道(SelectableChannel)：这个抽象类提供了实现通道的可选择性所需要的公共方法。它是所有支持就绪检查的通道类的父类。FileChannel对象不是可选择的，因为它们没有继承SelectableChannel。所有socket通道都是可选择的，包括从管道(Pipe)对象中获得的通道。SelectableChannel可以被注册到Selector对象上，同时可以指定对那个选择器而言，那种操作是感兴趣的。一个通道可以被注册到多个选择器上，但对每个选择器而言只能被注册一次。  
* 选择键(SelectionKey)：选择键封装了特定的通道与特定的选择器的注册关系。选择键对象被SelectableChannel.register()返回并提供一个表示这种注册关系的标记。选择键包含了两个比特集（以整数的形式进行编码），指示了该注册关系所关心的通道操作，以及通道己经准备好的操作。每个channel对应一个 SelectionKey。  

## 5.2. 选择器教程  
### 5.2.1. 建立选择器（选择器、通道、选择键建立连接）  
&emsp; selector的API：  

|方法	|描述|
|---|---|
|Selector open()	|打开一个选择器|
|void close()	|关闭此选择器|
&emsp; 建立监控三个Socket通道的选择器：  
```
Selector selector = Selector.open( );
channel1.register (selector, SelectionKey.OP_READ);
channel2.register (selector, SelectionKey.OP_WRITE);
channel3.register (selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
// Wait up to 10 seconds for a channel to become ready
readyCount = selector.select (10000);
```
&emsp; select方法是阻塞方法，直到过了十秒或者至少有一个通道的I/O操作准备好。  
&emsp; 这些代码创建了一个新的选择器，然后将这三个(己经存在的)socket通道注册到选择器上，而且感兴趣的操作各不相同。方法在将线程置于睡眠状态，直到这些刚兴趣的事情中的操作中的一个发生或者10秒钟的时间过去。  

1. 创建Selector对象  
```
Selector selector = Selector.open();
```
2. 将Channel注册到选择器中。为了使用选择器管理Channel，需要将Channel注册到选择器中:  
```
channel.configureBlocking(false);
SelectionKey key =channel.register(selector,SelectionKey.OP_READ);
```
&emsp; 注意，注册的Channel必须设置成异步模式才可以，否则异步IO就无法工作。这就意味着不能把一个FileChannel注册到Selector，因为FileChannel没有异步模式，但是网络编程中的SocketChannel可以。  
&emsp; 1). register()方法的第二个参数，它是一个“interest set”，意思是注册的Selector对Channel中的哪些事件感兴趣，事件类型有四种，这四种事件用SelectionKey的四个常量来表示：  
```
SelectionKey.OP_CONNECT
SelectionKey.OP_ACCEPT
SelectionKey.OP_READ
SelectionKey.OP_WRITE
```
&emsp; 通道触发了一个事件是指该事件已经Ready(就绪）。所以某个Channel成功连接到另一个服务器称为“连接就绪”Connect Ready。一个ServerSocketChannel  
&emsp; 准备好接收新连接称为“接收就绪”Accept Ready，一个有数据可读的通道可以说是Read Ready，等待写数据的通道可以说是Write Ready。  

&emsp; 2). 如果对多个事件感兴趣，可以通过or操作符来连接这些常量：  
```
int interestSet = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
```

### 5.2.2. 选择键的使用，SelectionKey类的API  
&emsp; SelectionKey类的API：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/communication/NIO-13.png)  
&emsp; 请注意对register()的调用的返回值是一个SelectionKey。SelectionKey代表这个通道在此Selector上的这个注册。当某个Selector通知某个传入事件时，它是通过提供对应于该事件的SelectionKey来进行的。SelectionKey还可以用于取消通道的注册。   
&emsp; SelectionKey内包含有如下属性：  

    interest Set：兴趣集合，当前 channel感兴趣的操作
    ready Set：就绪集合，此SelectionKey 已经准备就绪的操作集合
    Channel：通道，获取此 SelectionKey 对应的 channel
    Selector：选择器，管理此 channel 的 Selector
    Attach：附加对象，向SelectionKey中添加更多的信息，方便之后的数据操作判断或获取
&emsp; SelectionKey还有几个重要的方法，用于检测Channel中什么事件或操作已经就绪，它们都会返回一个布尔类型：selectionKey.isAcceptable();selectionKey.isConnectable();selectionKey.isReadable();selectionKey.isWritable();   

### 5.2.3. 选择器的使用，selector类的API  
&emsp; selector的API：  

|方法	|描述|
|---|---|
|Selector open()	|打开一个选择器|
|boolean isOpen()	|判断选择器是否已打开|
|SelectorProvider provider()	|返回创建此通道的提供者|
|Set<SelectionKey\> keys()	|返回此选择器的键集|
|Set<SelectionKey\> selectedKeys()	|返回此选择器上相应的通道I/O操作准备就绪的选择键集|
|int selectNow()	|select()方法的非阻塞形式。不等于select(0)（无限期阻塞）。|
|int select(long timeout)| |	
|int select()	|返回一组键的个数，其相应的通道已为I/O操作准备就绪|
|Selector wakeup()	|使尚未返回的第一个选择操作立即返回|
|void close()	|关闭此选择器|

&emsp; Selector的基本使用流程：  

1. 通过Selector.open() 打开一个 Selector.
2. 将Channel注册到Selector中, 并设置需要监听的事件(interest set)
3. 不断重复:
    1. 调用select()方法
    2. 调用selector.selectedKeys() 获取selected keys
    3. 迭代每个 selected key:
        * 从selected key中获取对应的Channel和附加信息(如果有的话)。
        * 判断是哪些IO事件已经就绪, 然后处理它们。如果是OP_ACCEPT事件, 则调用"SocketChannel clientChannel = ((ServerSocketChannel) key.channel()).accept()" 获取SocketChannel, 并将它设置为 非阻塞的, 然后将这个Channel注册到Selector中。
        * 根据需要更改selected key的监听事件。
        * 将已经处理过的key从selected keys 集合中删除。

### 5.2.4. Selector完整实例  
```
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
public class TCPServer{
    // 超时时间，单位毫秒
    private static final int TimeOut = 3000;
    // 本地监听端口
    private static final int ListenPort = 1978;

    public static void main(String[] args) throws IOException{
        // 创建选择器
        Selector selector = Selector.open();
        // 打开监听信道
        ServerSocketChannel listenerChannel = ServerSocketChannel.open();
        // 与本地端口绑定
        listenerChannel.socket().bind(new InetSocketAddress(ListenPort));
        // 设置为非阻塞模式
        listenerChannel.configureBlocking(false);
        // 将选择器绑定到监听信道,只有非阻塞信道才可以注册选择器.并在注册过程中指出该信道可以进行Accept操作
        // 一个serversocket channel准备好接收新进入的连接称为“接收就绪”
        listenerChannel.register(selector, SelectionKey.OP_ACCEPT);

        // 反复循环,等待IO
        while (true){
            // 等待某信道就绪(或超时)
            int keys = selector.select(TimeOut);
            //刚启动时连续输出0，client连接后一直输出1
            if (keys == 0){
                System.out.println("独自等待.");
                continue;
            }

            // 取得迭代器，遍历每一个注册的通道
            Set<SelectionKey> set = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = set.iterator();

            while (keyIterator.hasNext()){
                SelectionKey key = keyIterator.next();
                if(key.isAcceptable()){
                    // a connection was accepted by a ServerSocketChannel.
                    // 可通过Channel()方法获取就绪的Channel并进一步处理
                    SocketChannel channel = (SocketChannel)key.channel();
                    // TODO
                }
                else if (key.isConnectable()){
                    // TODO
                }
                else if (key.isReadable()){
                    // TODO
                }
                else if (key.isWritable()){
                    // TODO
                }
                // 删除处理过的事件
                keyIterator.remove();
            }
        }
    }
}
```

特别说明：例子中selector只注册了一个Channel，注册多个Channel操作类似。如下：
for (int i=0; i<3; i++){
    // 打开监听信道
    ServerSocketChannel listenerChannel = ServerSocketChannel.open();
    // 与本地端口绑定
    listenerChannel.socket().bind(new InetSocketAddress(ListenPort+i));
    // 设置为非阻塞模式
    listenerChannel.configureBlocking(false);
    // 注册到selector中
    listenerChannel.register(selector, SelectionKey.OP_ACCEPT);
}

&emsp; 在上面的例子中，对于通道IO事件的处理并没有给出具体方法，在此，举一个更详细的例子：  

```
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
public class NIO_Learning{
    private static final int BUF_SIZE = 256;
    private static final int TIMEOUT = 3000;

    public static void main(String args[]) throws Exception{
        // 打开服务端 Socket
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 打开 Selector
        Selector selector = Selector.open();
        // 服务端 Socket 监听8080端口, 并配置为非阻塞模式
        serverSocketChannel.socket().bind(new InetSocketAddress(8080));
        serverSocketChannel.configureBlocking(false);
        // 将 channel 注册到 selector 中.
        // 通常我们都是先注册一个 OP_ACCEPT 事件, 然后在 OP_ACCEPT 到来时, 再将这个 Channel 的 OP_READ 注册到 Selector 中.
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true){
            // 通过调用 select 方法, 阻塞地等待 channel I/O 可操作
            if (selector.select(TIMEOUT) == 0){
                System.out.print("超时等待...");
                continue;
            }
            // 获取 I/O 操作就绪的 SelectionKey, 通过 SelectionKey 可以知道哪些 Channel 的哪类 I/O 操作已经就绪.
            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
            while (keyIterator.hasNext()){
                SelectionKey key = keyIterator.next();
                // 当获取一个 SelectionKey 后, 就要将它删除, 表示我们已经对这个 IO 事件进行了处理.
                keyIterator.remove();
                if (key.isAcceptable()){
                    // 当 OP_ACCEPT 事件到来时, 我们就有从 ServerSocketChannel 中获取一个 SocketChannel,
                    // 代表客户端的连接
                    // 注意, 在 OP_ACCEPT 事件中, 从 key.channel() 返回的 Channel 是 ServerSocketChannel.
                    // 而在 OP_WRITE 和 OP_READ 中, 从 key.channel() 返回的是 SocketChannel.
                    SocketChannel clientChannel = ((ServerSocketChannel) key.channel()).accept();
                    clientChannel.configureBlocking(false);
                    //在 OP_ACCEPT 到来时, 再将这个 Channel 的 OP_READ 注册到 Selector 中.
                    // 注意, 这里我们如果没有设置 OP_READ 的话, 即 interest set 仍然是 OP_CONNECT 的话, 那么 select 方法会一直直接返回.
                    clientChannel.register(key.selector(), SelectionKey.OP_READ,ByteBuffer.allocate(BUF_SIZE));
                }

                if (key.isReadable()){
                    SocketChannel clientChannel = (SocketChannel) key.channel();
                    ByteBuffer buf = (ByteBuffer) key.attachment();
                    long bytesRead = clientChannel.read(buf);
                    if (bytesRead == -1){
                        clientChannel.close();
                    }
                    else if (bytesRead > 0){
                        key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        System.out.println("Get data length: " + bytesRead);
                    }
                }
                if (key.isValid() && key.isWritable()){
                    ByteBuffer buf = (ByteBuffer) key.attachment();
                    buf.flip();
                    SocketChannel clientChannel = (SocketChannel) key.channel();
                    clientChannel.write(buf);
                    if (!buf.hasRemaining()){
                        key.interestOps(SelectionKey.OP_READ);
                    }
                    buf.compact();
                }
            }
        }
    }
}
```
&emsp; 如从上述实例所示，可以将多个 Channel 注册到同一个Selector对象上，实现一个线程同时监控多个Channel的请求状态，但有一个不容忽视的缺陷：所有读/写请求以及对新连接请求的处理都在同一个线程中处理，无法充分利用多CPU的优势，同时读/写操作也会阻塞对新连接请求的处理。因此，有必要进行优化，可以引入多线程，并行处理多个读/写操作。  
&emsp; 一种优化策略是：将Selector进一步分解为Reactor，从而将不同的感兴趣事件分开，每一个Reactor只负责一种感兴趣的事件。这样做的好处是：分离阻塞级别，减少了轮询的时间；线程无需遍历set以找到自己感兴趣的事件，因为得到的set中仅包含自己感兴趣的事件。  




