

<!-- TOC -->

- [1. NIO通道](#1-nio通道)
    - [1.1. 通道分类及创建通道](#11-通道分类及创建通道)
    - [1.2. 内存映射文件](#12-内存映射文件)
    - [1.3. 分散Scatter/聚集Gather](#13-分散scatter聚集gather)
        - [1.3.1. 分散读取](#131-分散读取)
        - [1.3.2. 聚集写入](#132-聚集写入)
        - [1.3.3. 基本散点/聚集示例](#133-基本散点聚集示例)
    - [1.4. ★★★通道之间的数据传输](#14-★★★通道之间的数据传输)
        - [1.4.1. FileChannel.transferFrom()方法](#141-filechanneltransferfrom方法)
        - [1.4.2. FileChannel.transferTo()方法](#142-filechanneltransferto方法)
        - [1.4.3. 基本通道到通道数据传输示例](#143-基本通道到通道数据传输示例)
    - [1.5. 通道工具类](#15-通道工具类)

<!-- /TOC -->

# 1. NIO通道  
&emsp; 实体 ---> 通道 ---> 缓冲区。Channel用于在缓冲区和位于通道另一侧的实体（通常是一个文件或套接字）之间有效地传输数据。它从一个实体读取数据，并将其放在缓冲区块中以供消费。  
&emsp; Channel（通道）与Stream（流）的不同之处在于通道是双向的，流只能在一个方向上操作（一个流必须是InputStream或者OutputStream的子类），而通道可以用于读，写或者二者同时进行，最关键的是可以和多路复用器结合起来，提供状态位，多路复用器可识别Channel所处的状态。  
&emsp; 通道可以以阻塞（Wocfcwg）或非阻塞（wowWochwg）模式运行。 **<font color = "red">非阻塞模式的通道永远不会让调用的线程休眠。请求的操作要么立即完成，要么返回一个结果表明未进行任何操作。</font><font color = "clime">只有面向流的（stream-oriented）的通道，如sockets和pipes才能使用非阻塞模式。</font>** 

## 1.1. 通道分类及创建通道  
&emsp; <font color = "red">I/O可以分为广义的两大类别：File I/O和Stream I/O。相应地有两种类型的通道，它们是文件(file)通道和套接字(socket)通道。包含一个FileChannel类和三个socket通道类：SocketChannel、ServerSocketChannel和DatagramChannel。</font>  
&emsp; Channel接口的完整源码：  

```java
package java.nio.channels;
public interface Channel{
    public boolean isclose();
    public void Open() throws IOException;
}
```
&emsp; <font color = "clime">所有通道只有两个常用操作：检查通道是否关闭(isclose())、打开关闭通道(close())。</font>  

1. FileChannel：文件通道用于从文件读取数据。  
    &emsp; jdk1.7通过使用一个InputStream、OutputStream或RandomAccessFile来获取一个FileChannel实例。  

    ```java
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

    ```java
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

    ```java
    SocketChannel ch = SocketChannel.open();
    ch.connect(new InetSocketAddress("somehost", someport));
    ```
    &emsp; 用于关闭SocketChannel的语法：  

    ```java
    SocketChannel ch = SocketChannel.close();
    ch.connect(new InetSocketAddress("somehost", someport));
    ```
3. DatagramChannel：数据报通道可以通过UDP(用户数据报协议)通过网络读取和写入数据。它使用工厂方法来创建新对象。  
    &emsp; 下面是打开DatagramChannel的语法：  
    
    ```java
    DatagramChannel ch = DatagramChannel.open();
    ```
    &emsp; 用于关闭DatagramChannel的语法：  

    ```java
    DatagramChannel ch = DatagramChannel.close();
    ```
4. ServerSocketChannel：ServerSocketChannel允许用户监听传入的TCP连接，与Web服务器相同。对于每个传入连接，都会为连接创建一个SocketChannel。  
    &emsp; 下面是打开ServerSocketChannel的语法：  

    ```java
    ServerSocketChannel ch = ServerSocketChannel.open();
    ch.socket().bind (new InetSocketAddress (somelocalport));
    ```
    &emsp; 下面是关闭ServerSocketChannel的语法：  

    ```java
    ServerSocketChannel ch = ServerSocketChannel.close();
    ch.socket().bind (new InetSocketAddress (somelocalport));
    ```

## 1.2. 内存映射文件  
&emsp; 通道映射技术：一种快速读写技术。用户进程将文件数据视为内存，不需要发出read()或write()系统调用；可以映射非常大的文件(文件可能无法全部读入内存)，而不消耗大量内存来复制数据。  
&emsp; java nio提供的FileChannel提供了map()方法，该方法可以在一个打开的文件和MappedByteBuffer之间建立一个虚拟内存映射，MappedByteBuffer继承于[ByteBuffer](/docs/microService/communication/NIO/ByteBuffer.md)，类似于一个基于内存的缓冲区，只不过该对象的数据元素存储在磁盘的一个文件中；  

## 1.3. 分散Scatter/聚集Gather  
&emsp; 在Java NIO中，通道提供了称为分散/聚集(或向量I/O)的重要功能。使用单个write()函数将字节从一组缓冲区写入流，并且可以使用单个read()函数将字节从流读取到一组缓冲区中。  
&emsp; 对于一个write操作而言，数据是从几个缓冲区按顺序抽取(称为gather)并沿着通道发送的。对于read操作而言，从通道读取的数据会按顺序被散布(称为scatter)到多个缓冲区，将每个缓冲区填满直至通道中的数据或者缓冲区的最大空间被消耗完。  
&emsp; 分散(scatter)从Channel中读取是指在读操作时将读取的数据写入多个buffer中。因此，Channel将从Channel中读取的数据“分散(scatter)”到多个Buffer中。  
&emsp; 聚集(gather)写入Channel是指在写操作时将多个buffer的数据写入同一个Channel，因此，Channel将多个Buffer中的数据“聚集(gather)”后发送到Channel。  
&emsp; 分散scatter/聚集gather经常用于需要将传输的数据分开处理的场合。例如传输一个由消息头和消息体组成的消息，可能会将消息体和消息头分散到不同的buffer中，这样就可以方便的处理消息头和消息体。   

### 1.3.1. 分散读取   
&emsp; “分散读取”用于将数据从单个通道读取多个缓冲区中的数据。  
![image](http://182.92.69.8:8081/img/microService/netty/NIO/NIO-8.png)  
&emsp; Rread()的API：  

```java
public interface ScatteringByteChannel extends ReadableByteChannel{
    public long read (ByteBuffer [] argv) throws IOException;
    public long read (ByteBuffer [] argv, int length, int offset) throws IOException;
}
```
&emsp; 执行分射读取操作的代码示例：  

```java
ByteBuffer header = ByteBuffer.allocate(128);
ByteBuffer body   = ByteBuffer.allocate(1024);
ByteBuffer[] bufferArray = { header, body };  
  
channel.read(bufferArray); 
```
&emsp; 注意buffer首先被插入到数组，然后再将数组作为channel.read() 的输入参数。read()方法按照buffer在数组中的顺序将从channel中读取的数据写入到buffer，当一个buffer被写满后，channel紧接着向另一个buffer中写。  
&emsp; Scattering Reads在移动下一个buffer前，必须填满当前的buffer，这也意味着它不适用于动态消息(消息大小不固定)。换句话说，如果存在消息头和消息体，消息头必须完成填充(例如 128byte)，Scattering Reads才能正常工作。   

### 1.3.2. 聚集写入  
&emsp; “聚集写入”用于将数据从多个缓冲区写入单个通道。  
![image](http://182.92.69.8:8081/img/microService/netty/NIO/NIO-9.png)  
&emsp; write()的API：  

```java
public interface GatheringByteChannel extends WritableByteChannel{
    public long write(ByteBuffer[] argv) throws IOException;
    public long write(ByteBuffer[] argv, int length, int offset) throws IOException;
}
```
&emsp; 执行聚集写入操作的代码示例：  

```java
ByteBuffer header = ByteBuffer.allocate(128);
ByteBuffer body   = ByteBuffer.allocate(1024);

//write data into buffers  
ByteBuffer[] bufferArray = { header, body };  
channel.write(bufferArray);  
```
&emsp; buffers数组是write()方法的入参，write()方法会按照buffer在数组中的顺序，将数据写入到channel，注意只有position和limit之间的数据才会被写入。因此，如果一个buffer的容量为128byte，但是仅仅包含58byte的数据，那么这58byte的数据将被写入到channel中。因此与Scattering Reads相反，Gathering Writes能较好的处理动态消息。  

### 1.3.3. 基本散点/聚集示例  
&emsp; 下面来看看两个缓冲区的简单例子。第一个缓冲区保存随机数，第二个缓冲区使用分散/聚集机制保存写入的数据： 

```java
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.GatheringByteChannel;

public class ScatterGatherIO {
    public static void main(String params[]) {
        String data = "Scattering and Gathering example shown";
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
&emsp; 在上述程序中，第一个缓冲区在控制台上打印随机输出，第二个缓冲区在控制台上打印“Scattering and Gathering example shown”。它还用“Scattering and Gathering example shown”替换testout.txt文件的内容。  

    420
    Scattering and Gathering example shown  

## 1.4. ★★★通道之间的数据传输  
&emsp; 在Java NIO中，<font color = "red">如果两个通道中有一个是FileChannel，那可以直接将数据从一个channel传输到另外一个channel。</font>通道之间的数据传输在FileChannel类中的两种方法是：FileChannel.transferTo()方法、FileChannel.transferFrom()方法。  

### 1.4.1. FileChannel.transferFrom()方法  
&emsp; transferFrom()的API：  

```java
public abstract class Channel extends AbstractChannel{
    public abstract long transferFrom (ReadableByteChannel src, long position, long count);
}
```
&emsp; 方法的输入参数position表示从position处开始向目标文件写入数据，count表示最多传输的字节数。如果源通道的剩余空间小于count个字节，则所传输的字节数要小于请求的字节数。此外要注意，在SoketChannel的实现中，SocketChannel只会传输此刻准备好的数据(可能不足count字节)。因此，SocketChannel可能不会将请求的所有数据(count个字节)全部传输到FileChannel中。  

&emsp; transferFrom()方法可以将数据从源通道传输到FileChannel中。transferFrom()方法的示例：  

```java
RandomAccessFile fromFile = new RandomAccessFile("fromFile.txt", "rw");
FileChannel  fromChannel = fromFile.getChannel();

RandomAccessFile toFile = new RandomAccessFile("toFile.txt", "rw");
FileChannel  toChannel = toFile.getChannel();

long position = 0;
long count = fromChannel.size();  

toChannel.transferFrom(position, count, fromChannel); 
```

### 1.4.2. FileChannel.transferTo()方法  
&emsp; transferTo()方法的API：  

```java
public abstract class Channel extends AbstractChannel{
    public abstract long transferTo (long position, long count, WritableByteChannel target);
}
```
&emsp; 上面所说的关于SocketChannel的问题在transferTo()方法中同样存在。SocketChannel会一直传输数据直到目标buffer被填满。  

&emsp; transferTo()方法将数据从FileChannel传输到其他的channel中。用来从FileChannel到其他通道的数据传输。transferTo()方法的示例：  

```java
RandomAccessFile fromFile = new RandomAccessFile("fromFile.txt", "rw");
FileChannel fromChannel = fromFile.getChannel();

RandomAccessFile toFile = new RandomAccessFile("toFile.txt", "rw");
FileChannel toChannel = toFile.getChannel();

long position = 0;
long count = fromChannel.size();  
  
fromChannel.transferTo(position, count, toChannel);  
```

### 1.4.3. 基本通道到通道数据传输示例  
&emsp; 下面来看看从4个不同文件读取文件内容的简单示例，并将它们的组合输出写入第五个文件：  

```java
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

```java
this is content from input1.txt
this is content from input2.txt
this is content from input3.txt
this is content from input4.txt
```

## 1.5. 通道工具类  
&emsp; NIO通道提供了一个全新的类似流的I/O隐喻，但是字节流以及字符读写器仍然存在并被广泛使用。 **一个工具类java.nio.channels.Channels定义了几种静态的工厂方法以使通道可以更加容易地同流和读写器互联。**  
&emsp; java.nio.channels.Channels工具方法汇总：  

|方法|返回|描述|
|---|---|---|
|newChannel (InputStream in)|ReadableByteChannel	|返回一个将从给定的输入流读取数据的通道。|
|newChannel (OutputStream out)	|WritableByteChannel	|返回一个将向给定的输出流写入数据的通道。|
|newInputStream (ReadableByteChannel ch)	|InputStream|	返回一个将从给定的通道读取字节的流。|
|newOutputStream (WritableByteChannel ch)	|OutputStream	|返回一个将向给定的通道写入字节的流。|
|newReader (ReadableByteChannel ch, CharsetDecoder dec, int minBufferCap)	|Reader	|返回一个reader，它将从给定的通道读取字节并依据提供的CharsetDecoder对读取到的字节进行解码。|
|newReader (ReadableByteChannel ch, CharsetDecoder dec, int minBufferCap)	|Reader	|返回一个reader，它将从给定的通道读取字节并依据提供的CharsetDecoder对读取到的字节进行解码。|
|newWriter (WritableByteChannel ch, CharsetEncoder dec, int minBufferCap)	|Writer	|返回一个writer，它将使用提供的CharsetEncoder对象对字符编码并写到给定的通道中。|
|newWriter (WritableByteChannel ch, String csName)|Writer|返回一个writer，它将依据提供的字符集名称对字符编码并写到给定的通道中。|  

&emsp; 常规的流仅传输字节，readers和writers则作用于字符数据。表中前四行描述了用于连接流、通道的方法。因为流和通道都是运行在字节流基础上的，所以这四个方法直接将流封装在通道上，反之亦然。  
&emsp; Readers和Writers运行在字符的基础上，在Java的世界里字符同字节是完全不同的。将一个通道(仅了解字节)连接到一个reader或writer需要一个中间对话来处理字节/字符(byte/char)阻抗失配。为此，表中的后半部分描述的工厂方法使用了字符集编码器和解码器。  
&emsp; 这些方法返回的包封Channel对象可能会也可能不会实现 InterruptibleChannel 接口，它们也可能不是从SelectableChannel引申而来。因此，可能无法将这些包封通道同java.nio.channels包中定义的其他通道类型交换使用。细节是依赖实现的。如果您的程序依赖这些语义，那么请使用操作器实例测试一下返回的通道对象。  
