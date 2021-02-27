

# ★★★字节缓冲区详解  
&emsp; 所有的基本数据类型都有相应的缓冲区类 (布尔型除外)，但字节缓冲区有自己的独特之处。字节是操作系统及其I/O设备使用的基本数据类型。
&emsp; 为什么Buffer能更方便的操作？Buffer记录了每个操作点。**<font color = "clime">同时ByteBuffer为了提高性能，提供了两种实现：堆内存HeapByteBuffer和直接内存DirectByteBuffer(继承MappedByteBuffer)。</font>**  
&emsp; 使用直接内存的优势：  
1. 少一次内存拷贝：在常规文件IO或者网络IO的情况下，需要调用操作系统的API，会先把堆内存的数据复制到堆外，至于为什么要复制一份数据到堆外，JVM有GC机制，在GC过程中，会移动对象的位置，这样操作系统在读取或写入数据就找不到数据了。但这样一来，显然增加了一次内存拷贝的过程，如果直接通过直接内存的方式，省略掉这次非必须的内存拷贝，可以达到优化性能的目的。创建直接内存的API：  

    ```java
    public static ByteBuffer allocateDirect(int capacity) {
        return new DirectByteBuffer(capacity);
    }
    ```
2. 降低GC压力：如果直接使用直接内存(堆外内存)呢？首先申请一块不受GC管理的堆外内存，但需要手动回收堆外内存。在java.nio.DirectByteBuffer中，有一个虚引用的Cleaner对象，执行GC时会触发Deallocator回收内存。  

    ```java
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


## 内存映射  
&emsp; **NIO中内存映射主要用到以下两个类：java.nio.MappedByteBuffer、java.nio.channels.FileChannel。**  
1. 通过FileChannel.map()获取MappedByteBuffer  

```java
public abstract MappedByteBuffer map(MapMode mode, long position, long size) throws IOException;  
```

&emsp; 调用FileChannel类的map方法进行内存映射，第一个参数设置映射模式，现在支持3种模式：  

    * FileChannel.MapMode.READ_ONLY：只读缓冲区，在缓冲区中如果发生写操作则会产生ReadOnlyBufferException；  
    * FileChannel.MapMode.READ_WRITE：读写缓冲区，任何时刻如果通过内存映射的方式修改了文件则立刻会对磁盘上的文件执行相应的修改操作。别的进程如果也共享了同一个映射，则也会同步看到变化。而不是像标准IO那样每个进程有各自的内核缓冲区，比如JAVA代码中，没有执行IO输出流的flush()或者close()操作，那么对文件的修改不会更新到磁盘去，除非进程运行结束；  
    * FileChannel.MapMode.PRIVATE：可写缓冲区，但任何修改是缓冲区私有的，不会回到文件中。  

&emsp; PRIVATE模式表示写时拷贝的映射，意味着通过put()方法所做的任何修改都会导致产生一个私有的数据拷贝并且该拷贝中的数据只有MappedByteBuffer实例可以看到；  
&emsp; 该过程不会对底层文件做任何修改，而且一旦缓冲区被施以垃圾收集动作(garbage collected)，那些修改都会丢失；  
2. MappedByteBuffer是ByteBuffer的子类，其扩充了三个方法：  
&emsp; force()：缓冲区是READ_WRITE模式下，此方法对缓冲区内容的修改强行写入文件；  
&emsp; load()：将缓冲区的内容载入内存，并返回该缓冲区的引用；  
&emsp; isLoaded()：如果缓冲区的内容在物理内存中，则返回真，否则返回假；  

### 1.2.2. 示例  
&emsp; 下面通过一个例子来看一下内存映射读取文件和普通的IO流读取一个150M大文件的速度对比：  

```java
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
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/NIO/NIO-10.png)  

&emsp; 复制文件的例子：  

```java
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
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/NIO/NIO-11.png)  

