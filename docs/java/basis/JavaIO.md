

<!-- TOC -->

- [1. 大批量数据处理](#1-大批量数据处理)
    - [1.1. Java 读写大文本文件](#11-java-读写大文本文件)
        - [1.1.1. java读取大文件的困难](#111-java读取大文件的困难)
        - [1.1.2. 解决方案](#112-解决方案)
            - [1.1.2.1. 使用BufferedInputStream进行包装](#1121-使用bufferedinputstream进行包装)
            - [1.1.2.2. 逐行读取](#1122-逐行读取)
            - [1.1.2.3. 并发读取](#1123-并发读取)
                - [1.1.2.3.1. 逐行批次打包](#11231-逐行批次打包)
                - [1.1.2.3.2. 大文件拆分成小文件](#11232-大文件拆分成小文件)
            - [1.1.2.4. 使用FileChannel](#1124-使用filechannel)
            - [1.1.2.5. 内存文件映射](#1125-内存文件映射)
    - [1.2. 大批量数据导入Excel](#12-大批量数据导入excel)
    - [1.3. 多线程断点续传](#13-多线程断点续传)

<!-- /TOC -->

# 1. 大批量数据处理  
<!--
超赞，压缩20M文件从30秒到1秒的优化过程 
https://mp.weixin.qq.com/s/jxGzGBVNkeL5SKDHRvNJ4A
100000 行级别数据的 Excel 导入优化之路 
https://mp.weixin.qq.com/s/Y1feFfn8VeZsxXw65NYoWQ
https://mp.weixin.qq.com/s/A6C5ttVCroZ4xaDAmdRskg
Java 设置Excel条件格式（高亮条件值、应用单元格值/公式/数据条等类型） 
https://mp.weixin.qq.com/s/h3M2wiJU-QYONi4ewJnVyA
-->

## 1.1. Java 读写大文本文件  
&emsp; **<font color = "lime">将大文件数据全部读取到内存中，会发生OOM异常。</font>**  

### 1.1.1. java读取大文件的困难  
&emsp; java 读取文件的一般操作是将文件数据全部读取到内存中，然后再对数据进行操作。例如：  

```java
Path path = Paths.get("file path");
byte[] data = Files.readAllBytes(path);
```
&emsp; 这对于小文件是没有问题的，但是对于稍大一些的文件就会抛出OOM异常。 

```
Exception in thread "main" java.lang.OutOfMemoryError: Required array size too large at java.nio.file.Files.readAllBytes(Files.java:3156)
```
&emsp; 从错误定位看出，Files.readAllBytes 方法最大支持 Integer.MAX_VALUE - 8 大小的文件，也即最大2GB的文件。一旦超过了这个限度，java 原生的方法就不能直接使用了。  



### 1.1.2. 解决方案 
#### 1.1.2.1. 使用BufferedInputStream进行包装  
&emsp; 文件流边读边用，使用文件流的 BufferedInputStream#read() 方法每次读取指定长度的数据到内存中。这种方法可行但是效率不高。示例代码如下：  

```java
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class StreamFileReader {
    private BufferedInputStream fileIn;
    private long fileLength;
    private int arraySize;
    private byte[] array;
 
    public StreamFileReader(String fileName, int arraySize) throws IOException {
        this.fileIn = new BufferedInputStream(new FileInputStream(fileName), arraySize);
        this.fileLength = fileIn.available();
        this.arraySize = arraySize;
    }
 
    public int read() throws IOException {
        byte[] tmpArray = new byte[arraySize];
        int bytes = fileIn.read(tmpArray);// 暂存到字节数组中
        if (bytes != -1) {
            array = new byte[bytes];// 字节数组长度为已读取长度
            System.arraycopy(tmpArray, 0, array, 0, bytes);// 复制已读取数据
            return bytes;
        }
        return -1;
    }
 
    public void close() throws IOException {
        fileIn.close();
        array = null;
    }
 
    public byte[] getArray() {
        return array;
    }
 
    public long getFileLength() {
        return fileLength;
    }
 
    public static void main(String[] args) throws IOException {
        StreamFileReader reader = new StreamFileReader("xxxx", 65536);
        long start = System.nanoTime();
        while (reader.read() != -1) ;
        long end = System.nanoTime();
        reader.close();
        System.out.println("StreamFileReader: " + (end - start));
    }
}
```

#### 1.1.2.2. 逐行读取  
<!-- https://mp.weixin.qq.com/s/K7oGTdN1UI3oXiFYYZ51pA -->

#### 1.1.2.3. 并发读取  

<!-- https://mp.weixin.qq.com/s/K7oGTdN1UI3oXiFYYZ51pA -->

##### 1.1.2.3.1. 逐行批次打包  


##### 1.1.2.3.2. 大文件拆分成小文件  


#### 1.1.2.4. 使用FileChannel
&emsp; 对大文件建立 NIO 的 FileChannel，每次调用 read() 方法时会先将文件数据读取到已分配固定长度的 java.nio.ByteBuffer 中，接着从中获取读取的数据。这种用 NIO 通道的方法比传统文件流读取理论上要快一点。示例代码如下：  

```java
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class ChannelFileReader {
    private FileInputStream fileIn;
    private ByteBuffer byteBuf;
    private long fileLength;
    private int arraySize;
    private byte[] array;
 
    public ChannelFileReader(String fileName, int arraySize) throws IOException {
        this.fileIn = new FileInputStream(fileName);
        this.fileLength = fileIn.getChannel().size();
        this.arraySize = arraySize;
        this.byteBuf = ByteBuffer.allocate(arraySize);
    }
 
    public int read() throws IOException {
        FileChannel fileChannel = fileIn.getChannel();
        int bytes = fileChannel.read(byteBuf);// 读取到ByteBuffer中
        if (bytes != -1) {
            array = new byte[bytes];// 字节数组长度为已读取长度
            byteBuf.flip();
            byteBuf.get(array);// 从ByteBuffer中得到字节数组
            byteBuf.clear();
            return bytes;
        }
        return -1;
    }
 
    public void close() throws IOException {
        fileIn.close();
        array = null;
    }
 
    public byte[] getArray() {
        return array;
    }
 
    public long getFileLength() {
        return fileLength;
    }
 
    public static void main(String[] args) throws IOException {
        ChannelFileReader reader = new ChannelFileReader("xxxx", 65536);
        long start = System.nanoTime();
        while (reader.read() != -1) ;
        long end = System.nanoTime();
        reader.close();
        System.out.println("ChannelFileReader: " + (end - start));
    }
}
```

#### 1.1.2.5. 内存文件映射  
<!-- 
Java逐行读取文件
https://cloud.tencent.com/developer/article/1578606
https://blog.csdn.net/chunshaihuo5061/article/details/101030196

-->

&emsp; 把文件内容映射到虚拟内存的一块区域中，从而可以直接操作内存当中的数据而无需每次都通过 I/O  去物理硬盘读取文件，这种方式可以提高速度。示例代码如下：  

```java
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MappedFileReader {
    private FileInputStream fileIn;
    private MappedByteBuffer mappedBuf;
    private long fileLength;
    private int arraySize;
    private byte[] array;
 
    public MappedFileReader(String fileName, int arraySize) throws IOException {
        this.fileIn = new FileInputStream(fileName);
        FileChannel fileChannel = fileIn.getChannel();
        this.fileLength = fileChannel.size();
        this.mappedBuf = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileLength);
        this.arraySize = arraySize;
    }
 
    public int read() throws IOException {
        int limit = mappedBuf.limit();
        int position = mappedBuf.position();
        if (position == limit) {
            return -1;
        }
        if (limit - position > arraySize) {
            array = new byte[arraySize];
            mappedBuf.get(array);
            return arraySize;
        } else {// 最后一次读取数据
            array = new byte[limit - position];
            mappedBuf.get(array);
            return limit - position;
        }
    }
 
    public void close() throws IOException {
        fileIn.close();
        array = null;
    }
 
    public byte[] getArray() {
        return array;
    }
 
    public long getFileLength() {
        return fileLength;
    }
 
    public static void main(String[] args) throws IOException {
        MappedFileReader reader = new MappedFileReader("xxx", 65536);
        long start = System.nanoTime();
        while (reader.read() != -1);
        long end = System.nanoTime();
        reader.close();
        System.out.println("MappedFileReader: " + (end - start));
    }
}
```
&emsp; 采用内存文件映射的方法去处理大文件，不能读取超过2GB的文件，明明 FileChannel.map() 方法传递的文件长度是 long 类型的，怎么和 Integer.MAX_VALUE 有关系？  

&emsp; **<font color = "lime">采用内存文件映射不能读取超过2GB的文件。文件超过2GB，会报异常。</font>**  

```java
Exception in thread "main" java.lang.IllegalArgumentException: Size exceeds Integer.MAX_VALUE at sun.nio.ch.FileChannelImpl.map(FileChannelImpl.java:868)
```
&emsp; 这可以归结到一些历史原因，还有 int 类型在 java 中的深入程度，但是本质上由于 java.nio.MappedByteBuffer 是直接继承自 java.nio.ByteBuffer 的，而后者的索引变量是 int 类型的，所以前者也只能最大索引到 Integer.MAX_VALUE 的位置。  

&emsp; **解决方案：多次内存映射。**  

```java
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MappedBiggerFileReader {
    private MappedByteBuffer[] mappedBufArray;
    private int count = 0;
    private int number;
    private FileInputStream fileIn;
    private long fileLength;
    private int arraySize;
    private byte[] array;
 
    public MappedBiggerFileReader(String fileName, int arraySize) throws IOException {
        this.fileIn = new FileInputStream(fileName);
        FileChannel fileChannel = fileIn.getChannel();
        this.fileLength = fileChannel.size();
        this.number = (int) Math.ceil((double) fileLength / (double) Integer.MAX_VALUE);
        this.mappedBufArray = new MappedByteBuffer[number];// 内存文件映射数组
        long preLength = 0;
        long regionSize = (long) Integer.MAX_VALUE;// 映射区域的大小
        for (int i = 0; i < number; i++) {// 将文件的连续区域映射到内存文件映射数组中
            if (fileLength - preLength < (long) Integer.MAX_VALUE) {
                regionSize = fileLength - preLength;// 最后一片区域的大小
            }
            mappedBufArray[i] = fileChannel.map(FileChannel.MapMode.READ_ONLY, preLength, regionSize);
            preLength += regionSize;// 下一片区域的开始
        }
        this.arraySize = arraySize;
    }
 
    public int read() throws IOException {
        if (count >= number) {
            return -1;
        }
        int limit = mappedBufArray[count].limit();
        int position = mappedBufArray[count].position();
        if (limit - position > arraySize) {
            array = new byte[arraySize];
            mappedBufArray[count].get(array);
            return arraySize;
        } else {// 本内存文件映射最后一次读取数据
            array = new byte[limit - position];
            mappedBufArray[count].get(array);
            if (count < number) {
                count++;// 转换到下一个内存文件映射
            }
            return limit - position;
        }
    }
 
    public void close() throws IOException {
        fileIn.close();
        array = null;
    }
 
    public byte[] getArray() {
        return array;
    }
 
    public long getFileLength() {
        return fileLength;
    }
 
    public static void main(String[] args) throws IOException {
        MappedBiggerFileReader reader = new MappedBiggerFileReader("xxx", 65536);
        long start = System.nanoTime();
        while (reader.read() != -1) ;
        long end = System.nanoTime();
        reader.close();
        System.out.println("MappedBiggerFileReader: " + (end - start));
    }
}
```


## 1.2. 大批量数据导入Excel  

<!-- 
 100000 行级别数据的 Excel 导入优化之路 
 https://mp.weixin.qq.com/s/Y1feFfn8VeZsxXw65NYoWQ
-->

&emsp; EasyExcel + 缓存数据库查询操作 + 批量插入：  

* 逐行查询数据库校验的时间成本主要在来回的网络IO中，优化方法也很简单。将参加校验的数据全部缓存到 HashMap 中。直接到 HashMap 去命中。    


## 1.3. 多线程断点续传  

<!-- 
https://mp.weixin.qq.com/s/JYOa7iFhBLeG8vqIOr5tjA
-->
<!-- 
大文件上传：秒传、断点续传、分片上传 
https://mp.weixin.qq.com/s/S8ff0SBRcccqtoWteqo6QA

springboot断点续传的两种方法 
https://mp.weixin.qq.com/s/hZU3QOJ_ON0O6Sr2LbpU4A
-->
  