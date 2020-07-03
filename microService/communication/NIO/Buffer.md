---
title: NIO Buffer
date: 2020-05-30 00:00:00
tags:
    - 分布式通信
---

<!-- TOC -->

- [1. NIO缓冲区](#1-nio缓冲区)
    - [1.1. 缓冲区属性](#11-缓冲区属性)
    - [1.2. 缓冲区API成员方法](#12-缓冲区api成员方法)
        - [1.2.1. 创建缓冲区](#121-创建缓冲区)
        - [1.2.2. 缓冲区基本使用（注意模式切换）](#122-缓冲区基本使用注意模式切换)
        - [1.2.3. 其他方法](#123-其他方法)
    - [1.3. 字节缓冲区详解](#13-字节缓冲区详解)

<!-- /TOC -->

# 1. NIO缓冲区  
&emsp; 缓冲区实际上是一个容器对象，其实就是一个数组，是一块可以写入数据，然后可以从中读取数据的内存。这块内存被包装成NIO Buffer对象，并提供了一组方法，用来方便的访问该块内存。   
&emsp; 在NIO库中，所有数据都是用缓冲区处理的。在读取数据时，它是直接读到缓冲区中的；在写入数据时，它也是写入到缓冲区中的；任何时候访问NIO中的数据，都是将它放到缓冲区中。而在面向流I/O系统中，所有数据都是直接写入或者直接将数据读取到Stream对象中。  

## 1.1. 缓冲区属性  
&emsp; 缓冲区4个属性：  

```
// Invariants: mark <= position <= limit <= capacity
private int mark = -1;
private int position = 0;
private int limit;
private int capacity;
```
* 容量（Capacity)：缓冲区能够容纳的数据元素的最大数量。这一容量在缓冲区创建时被设定，并且不能被改变。Buffer满了，需要将其清空（通过读数据或者清除数据）才能继续写数据。  
* 上界（Limit)：缓冲区中现存元素的计数。代表最多能写入或者读取多少单位的数据。写模式下等于最大容量capacity。从写模式切换到读模式时，等于position，然后再将position置为0。所以，读模式下，limit表示最大可读取的数据量，这个值与实际写入的数量相等。读模式下：最多能往Buffer里读多少数据，写模式下：最多能往Buffer里写多少数据，limit大小跟数量大小和capacity有关。  
* 位置（Position)：下一个要被读或写的元素的索引。位置会自动由相应的和函数更新。记录当前读取或者写入的位置。写模式下等于当前写入的单位数据数量。从写模式切换到读模式时，置为0。在读的过程中等于当前读取单位数据的数量。  
* 标记（Mark)：一个备忘位置。调用来设定mark = postion。调用设定position = mark。标记在设定前是未定义的(undefined)。  

&emsp; 这四个属性之间遵循以下关系：0 <=mark <=position <=limit <=capacity。  
&emsp; 在写模式和读模式（先写后读）下，position和limit的位置有所不同，见下图：  
&emsp; ![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/communication/NIO-1.png)  

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

## 1.2. 缓冲区API成员方法  
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

### 1.2.1. 创建缓冲区  
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

&emsp; 这段代码隐含地从堆空间中分配了一个char型数组作为备份存储器来储存100个char变量。  
&emsp; 如果想提供自己的数组用做缓冲区的备份存储器，调用wrap()函数:   

    char [] myArray = new char [100];  
    CharBuffer charbuffer = CharBuffer.wrap (myArray);

&emsp; 这段代码构造了一个新的缓冲区对象，但数据元素会存在于数组中。这意味着通过调用put()函数造成的对缓冲区的改动会直接影响这个数组，而且对这个数组的任何改动也会对这 个缓冲区对象可见。带有offset和length作为参数的wrap()函数版本则会构造一个按照提供的offset和length参数值初始化位置和上界的缓冲区。这样做：  

    CharBuffer charbuffer = CharBuffer.wrap (myArray, 12, 42);

&emsp; 创建建了一个position值为12, limit值为54,容量为myArray.length的缓冲区。  
&emsp; 通过allocate()或者wrap()函数创建的缓冲区通常都是间接的。    

### 1.2.2. 缓冲区基本使用（注意模式切换）  
&emsp; <font color = "red">使用Buffer读写数据一般遵循以下四个步骤</font>：  
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

&emsp; 从Buffer读取数据到Channel的例子： 

```
//read from buffer into channel.  
int bytesWritten = inChannel.write(buf); 
```
&emsp; 使用get()方法从Buffer中读取数据的例子：  

```
byte aByte = buf.get(); 
```

### 1.2.3. 其他方法
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

## 1.3. 字节缓冲区详解  
&emsp; 所有的基本数据类型都有相应的缓冲区类 (布尔型除外），但字节缓冲区有自己的独特之处。字节是操作系统及其I/O设备使用的基本数据类型。
为什么Buffer能更方便的操作？Buffer记录了每个操作点。同时ByteBuffer为了提高性能，提供了两种实现：堆内存和直接内存。  
&emsp; 使用直接内存的优势：  
1. 少一次内存拷贝：在常规文件IO或者网络IO的情况下，需要调用操作系统的API，会先把堆内存的数据复制到堆外，至于为什么要复制一份数据到堆外，JVM有GC机制，在GC过程中，会移动对象的位置，这样操作系统在读取或写入数据就找不到数据了。但这样一来，显然增加了一次内存拷贝的过程，如果直接通过直接内存的方式，省略掉这次非必须的内存拷贝，可以达到优化性能的目的。创建直接内存的API：  

    ```
    public static ByteBuffer allocateDirect(int capacity) {
        return new DirectByteBuffer(capacity);
    }
    ```
2. 降低GC压力：如果直接使用直接内存（堆外内存）呢？首先申请一块不受GC管理的堆外内存，但需要手动回收堆外内存。在java.nio.DirectByteBuffer中，有一个虚引用的Cleaner对象，执行GC时会触发Deallocator回收内存。  

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
