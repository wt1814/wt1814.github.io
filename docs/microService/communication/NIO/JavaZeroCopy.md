
<!-- TOC -->

- [1. Java中的零拷贝](#1-java中的零拷贝)
    - [1.1. MappedByteBuffer，直接内存](#11-mappedbytebuffer直接内存)
    - [1.2. DirectByteBuffer](#12-directbytebuffer)
    - [1.3. Channel-to-Channel，零拷贝](#13-channel-to-channel零拷贝)

<!-- /TOC -->


&emsp; **<font color = "red">总结：</font>**  
1. Java中提供了MappedByteBuffe、DirectByteBuffer类来实现内存映射技术。基于mmap+write方式。  
2. Java中FileChannel类提供了transferFrom、transferTo()方法，来实现零拷贝。  

# 1. Java中的零拷贝  
<!-- 
 Java 中的内存映射 mmap 
 https://mp.weixin.qq.com/s/_TY_MzxiRzNnCoQJJkkaaQ
-->

## 1.1. MappedByteBuffer，直接内存
<!-- 
java NIO的零拷贝实现是基于mmap+write方式

FileChannel的map方法产生的MappedByteBuffer FileChannel提供了map()方法，该方法可以在一个打开的文件和MappedByteBuffer之间建立一个虚拟内存映射，MappedByteBuffer继承于ByteBuffer；

该缓冲器的内存是一个文件的内存映射区域。

map方法底层是通过mmap实现的，因此将文件内存从磁盘读取到内核缓冲区后，用户空间和内核空间共享该缓冲区。

用法如下

```java
   public void main(String[] args){
    try {
        FileChannel readChannel = FileChannel.open(Paths.get("./cscw.txt")， StandardOpenOption.READ);
        FileChannel writeChannel = FileChannel.open(Paths.get("./siting.txt")， StandardOpenOption.WRITE， StandardOpenOption.CREATE);
        MappedByteBuffer data = readChannel.map(FileChannel.MapMode.READ_ONLY， 0， 1024 * 1024 * 40);
        //数据传输
        writeChannel.write(data);
        readChannel.close();
        writeChannel.close();
    }catch (Exception e){
        System.out.println(e.getMessage());
    }
}
```
-->
&emsp; java nio提供的FileChannel提供了map()方法，该方法可以在一个打开的文件和MappedByteBuffer之间建立一个虚拟内存映射。 **<font color = "clime">基于mmap+write方式。</font>**  
&emsp; MappedByteBuffer继承于ByteBuffer，类似于一个基于内存的缓冲区，只不过该对象的数据元素存储在磁盘的一个文件中；调用get()方法会从磁盘中获取数据，此数据反映该文件当前的内容，调用put()方法会更新磁盘上的文件，并且对文件做的修改对其他阅读者也是可见的；下面看一个简单的读取实例，然后在对MappedByteBuffer进行分析：

```java
public class MappedByteBufferTest {

    public static void main(String[] args) throws Exception {
        File file = new File("D://db.txt");
        long len = file.length();
        byte[] ds = new byte[(int) len];
        MappedByteBuffer mappedByteBuffer = new FileInputStream(file).getChannel().map(FileChannel.MapMode.READ_ONLY， 0，
                len);
        for (int offset = 0; offset < len; offset++) {
            byte b = mappedByteBuffer.get();
            ds[offset] = b;
        }
        Scanner scan = new Scanner(new ByteArrayInputStream(ds)).useDelimiter(" ");
        while (scan.hasNext()) {
            System.out.print(scan.next() + " ");
        }
    }
}
```

&emsp; 主要通过FileChannel提供的map()来实现映射，map()方法如下：

```java
public abstract MappedByteBuffer map(MapMode mode，long position， long size) throws IOException;  
```

&emsp; 分别提供了三个参数，MapMode，Position和size；分别表示：MapMode：映射的模式，可选项包括：READ_ONLY，READ_WRITE，PRIVATE；Position：从哪个位置开始映射，字节数的位置；Size：从position开始向后多少个字节；  
&emsp; 重点看一下MapMode，请两个分别表示只读和可读可写，当然请求的映射模式受到Filechannel对象的访问权限限制，如果在一个没有读权限的文件上启用READ_ONLY，将抛出NonReadableChannelException；PRIVATE模式表示写时拷贝的映射，意味着通过put()方法所做的任何修改都会导致产生一个私有的数据拷贝并且该拷贝中的数据只有MappedByteBuffer实例可以看到；该过程不会对底层文件做任何修改，而且一旦缓冲区被施以垃圾收集动作(garbage collected)，那些修改都会丢失；大致浏览一下map()方法的源码：

```java
public MappedByteBuffer map(MapMode mode， long position， long size)
    throws IOException
{
        //...省略...
        int pagePosition = (int)(position % allocationGranularity);
        long mapPosition = position - pagePosition;
        long mapSize = size + pagePosition;
        try {
            // If no exception was thrown from map0， the address is valid
            addr = map0(imode， mapPosition， mapSize);
        } catch (OutOfMemoryError x) {
            // An OutOfMemoryError may indicate that we've exhausted memory
            // so force gc and re-attempt map
            System.gc();
            try {
                Thread.sleep(100);
            } catch (InterruptedException y) {
                Thread.currentThread().interrupt();
            }
            try {
                addr = map0(imode， mapPosition， mapSize);
            } catch (OutOfMemoryError y) {
                // After a second OOME， fail
                throw new IOException("Map failed"， y);
            }
        }

        // On Windows， and potentially other platforms， we need an open
        // file descriptor for some mapping operations.
        FileDescriptor mfd;
        try {
            mfd = nd.duplicateForMapping(fd);
        } catch (IOException ioe) {
            unmap0(addr， mapSize);
            throw ioe;
        }

        assert (IOStatus.checkAll(addr));
        assert (addr % allocationGranularity == 0);
        int isize = (int)size;
        Unmapper um = new Unmapper(addr， mapSize， isize， mfd);
        if ((!writable) || (imode == MAP_RO)) {
            return Util.newMappedByteBufferR(isize，
                                             addr + pagePosition，
                                             mfd，
                                             um);
        } else {
            return Util.newMappedByteBuffer(isize，
                                            addr + pagePosition，
                                            mfd，
                                            um);
        }
 }
```
&emsp; 大致意思就是通过native方法获取内存映射的地址，如果失败，手动gc再次映射；最后通过内存映射的地址实例化出MappedByteBuffer，MappedByteBuffer本身是一个抽象类，其实这里真正实例话出来的是DirectByteBuffer；

## 1.2. DirectByteBuffer
&emsp; 字节是操作系统及其I/O设备使用的基本数据类型。ByteBuffer提供了两种实现堆内存HeapByteBuffer和直接内存DirectByteBuffer。  
&emsp; DirectByteBuffer继承于MappedByteBuffer，从名字就可以猜测出开辟了一段直接的内存，并不会占用jvm的内存空间；上一节中通过Filechannel映射出的MappedByteBuffer其实际也是DirectByteBuffer，当然除了这种方式，也可以手动开辟一段空间：

```java
ByteBuffer directByteBuffer = ByteBuffer.allocateDirect(100);
```
&emsp; 如上开辟了100字节的直接内存空间。  

## 1.3. Channel-to-Channel，零拷贝
<!-- 

FileChannel的transferTo、transferFrom 如果操作系统底层支持的话，transferTo、transferFrom也会使用相关的零拷贝技术来实现数据的传输。用法如下

```java
public void main(String[] args) {
    try {
        FileChannel readChannel = FileChannel.open(Paths.get("./cscw.txt")， StandardOpenOption.READ);
        FileChannel writeChannel = FileChannel.open(Paths.get("./siting.txt")， StandardOpenOption.WRITE， StandardOpenOption.CREATE);
        long len = readChannel.size();
        long position = readChannel.position();
        //数据传输
        readChannel.transferTo(position， len， writeChannel);
        //效果和transferTo 一样的
        //writeChannel.transferFrom(readChannel， position， len， );
        readChannel.close();
        writeChannel.close();
    } catch (Exception e) {
        System.out.println(e.getMessage());
    }
}
```
-->
&emsp; 经常需要从一个位置将文件传输到另外一个位置，FileChannel提供了transferFrom、transferTo()方法。如果操作系统底层支持的话，transferTo、transferFrom也会使用相关的零拷贝技术来实现数据的传输。使用示例：  

```java
public class ChannelTransfer {
    public static void main(String[] argv) throws Exception {
        String files[]=new String[1];
        files[0]="D://db.txt";
        catFiles(Channels.newChannel(System.out)， files);
    }

    private static void catFiles(WritableByteChannel target， String[] files)
            throws Exception {
        for (int i = 0; i < files.length; i++) {
            FileInputStream fis = new FileInputStream(files[i]);
            FileChannel channel = fis.getChannel();
            channel.transferTo(0， channel.size()， target);
            channel.close();
            fis.close();
        }
    }
}
```
&emsp; 通过FileChannel的transferTo()方法将文件数据传输到System.out通道，接口定义如下：

```java
public abstract long transferTo(long position， long count，WritableByteChannel target)
  throws IOException;
```
&emsp; 几个参数也比较好理解，分别是开始传输的位置，传输的字节数，以及目标通道；transferTo()允许将一个通道交叉连接到另一个通道，而不需要一个中间缓冲区来传递数据；注：这里不需要中间缓冲区有两层意思：第一层不需要用户空间缓冲区来拷贝内核缓冲区，另外一层两个通道都有自己的内核缓冲区，两个内核缓冲区也可以做到无需拷贝数据；  