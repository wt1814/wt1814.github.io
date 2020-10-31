<!-- TOC -->

- [1. Netty编解码](#1-netty编解码)
    - [1.1. TCP粘包/拆包](#11-tcp粘包拆包)
        - [1.1.1. TCP粘包/拆包问题](#111-tcp粘包拆包问题)
        - [1.1.2. 常见解决方案](#112-常见解决方案)
    - [1.2. Netty粘包/拆包](#12-netty粘包拆包)
        - [1.2.1. Netty粘包/拆包问题](#121-netty粘包拆包问题)
        - [1.2.2. Netty解决粘包/拆包问题-编解码器](#122-netty解决粘包拆包问题-编解码器)
            - [1.2.2.1. 编解码技术](#1221-编解码技术)
            - [1.2.2.2. Netty提供的解码器](#1222-netty提供的解码器)
                - [1.2.2.2.1. FixedLengthFrameDecoder](#12221-fixedlengthframedecoder)
                - [1.2.2.2.2. LineBasedFrameDecoder与DelimiterBasedFrameDecoder](#12222-linebasedframedecoder与delimiterbasedframedecoder)
                - [1.2.2.2.3. LengthFieldBasedFrameDecoder与LengthFieldPrepender](#12223-lengthfieldbasedframedecoder与lengthfieldprepender)
                - [1.2.2.2.4. 自定义粘包与拆包器](#12224-自定义粘包与拆包器)

<!-- /TOC -->

# 1. Netty编解码  

## 1.1. TCP粘包/拆包  
&emsp; 在RPC框架中，粘包和拆包问题是必须解决一个问题，因为RPC框架中，各个微服务相互之间都是维系了一个TCP长连接，比如dubbo就是一个全双工的长连接。由于微服务往对方发送信息的时候，所有的请求都是使用的同一个连接，这样就会产生粘包和拆包的问题。  

### 1.1.1. TCP粘包/拆包问题  
&emsp; TCP 是基于流传输的协议，请求数据在其传输的过程中是没有界限区分，所以我们在读取请求的时候，不一定能获取到一个完整的数据包。如果一个包较大时，可能会切分成多个包进行多次传输。同时，如果存在多个小包时，可能会将其整合成一个大包进行传输。这就是 TCP 协议的粘包/拆包概念。  
&emsp; 产生粘包和拆包问题的主要原因是，操作系统在发送TCP数据的时候，底层会有一个缓冲区，例如1024个字节大小，如果一次请求发送的数据量比较小，没达到缓冲区大小，TCP则会将多个请求合并为同一个请求进行发送，这就形成了粘包问题；如果一次请求发送的数据量比较大，超过了缓冲区大小，TCP就会将其拆分为多次发送，这就是拆包，也就是将一个大的包拆分为多个小包进行发送。如下图展示了粘包和拆包的一个示意图：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-38.png)  

&emsp; 上图中演示了粘包和拆包的三种情况：  

* A和B两个包都刚好满足TCP缓冲区的大小，或者说其等待时间已经达到TCP等待时长，从而还是使用两个独立的包进行发送；  
* A和B两次请求间隔时间内较短，并且数据包较小，因而合并为同一个包发送给服务端；  
* B包比较大，因而将其拆分为两个包B_1和B_2进行发送，而这里由于拆分后的B_2比较小，其又与A包合并在一起发送。  

### 1.1.2. 常见解决方案  
&emsp; 对于粘包和拆包问题，常见的解决方案有四种：  

* 客户端在发送数据包的时候，每个包都固定长度，比如1024个字节大小，如果客户端发送的数据长度不足1024个字节，则通过补充空格的方式补全到指定长度；  
* 客户端在每个包的末尾使用固定的分隔符，例如\r\n，如果一个包被拆分了，则等待下一个包发送过来之后找到其中的\r\n，然后对其拆分后的头部部分与前一个包的剩余部分进行合并，这样就得到了一个完整的包；  
* 将消息分为头部和消息体，在头部中保存有当前整个消息的长度，只有在读取到足够长度的消息之后才算是读到了一个完整的消息；  
* 通过自定义协议进行粘包和拆包的处理。  

## 1.2. Netty粘包/拆包
### 1.2.1. Netty粘包/拆包问题  
&emsp; 为突出 Netty 的粘包/拆包问题，这里通过例子进行重现问题，以下为突出问题的主要代码：  
&emsp; 服务端：  

```java
/**
* 服务端网络事件的读写操作类
*/
public class ServerHandler extends ChannelHandlerAdapter {

    // 接收消息计数器
    private int i = 0;
    // client端消息

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        i++;
        System.out.print(msg);
        // 对每条读取到的消息进行打数标记
        System.out.println("================== ["+ i +"]");
        // 发送应答消息给客户端
        ByteBuf rmsg = Unpooled.copiedBuffer(String.valueOf(i).getBytes());
        ctx.write(rmsg);
    }
    // 其他操作 .......
}  
```

&emsp; 客户端：  

```java
/**
* 客户端发送数据
*/

public class NettyClient {

    public void send() {
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();

        try {
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<NioSocketChannel>() {

                        @Override
                        protected void initChannel(NioSocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast("logger", new LoggingHandler(LogLevel.INFO));
                            pipeline.addLast(new ClientHandler());
                        }
                    });

            Channel channel = bootstrap.connect(HOST, PORT).channel();
            int i = 1;
            while (i <= 300){
                channel.writeAndFlush(String.format("【时间 %s: \t%s】", new Date(), i));
                // 打印发送请求的次数
                System.out.println(i);
                i++;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
             if (group != null)
                 group.shutdownGracefully();
         }
    }

}
```

&emsp; 以上代码中，第一反应理解的是，如果非异常情况下客户端所有数据发送成功，并且服务端全部接收到。那么从打印信息中可以看到客户端的发送次数 i和服务端的接收消息计数 i应该是相同的数。那么下面通过运行程序，查看打印结果。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-39.png)  
&emsp; 如上图所示， 【】中的最后一个数字与 []中数字对上的是已独立完整的包接收到（粘包/拆包示意图中的情况 I）。但是 【】中为 37和 38的出现了粘包情况（粘包/拆包示意图中的情况 II），两条数据粘合在一起。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-40.png)  
&emsp; 上图中可以看到 【】中 167的数据被拆分为了两部分（图中画绿线数据），该情况为拆包（粘包/拆包示意图中的情况 III）。  

&emsp; 上面程序没有考虑到 TCP 的粘包/拆包问题，所以如果是我们实际应用的程序的话，不能保证数据的正常情况，就会导致程序异常。  

### 1.2.2. Netty解决粘包/拆包问题-编解码器
&emsp; 针对粘包和拆包的解决方案，对于拆包问题比较简单，用户可以自己定义自己的编码器进行处理，Netty并没有提供相应的组件。对于粘包的问题，由于拆包比较复杂，代码比较处理比较繁琐，Netty提供了4种解码器来解决。  

#### 1.2.2.1. 编解码技术  
&emsp; **编解码技术**  
&emsp; 通常习惯将编码（Encode）称为序列化（serialization），它将对象序列化为字节数组，用于网络传输、数据持久化或者其它用途。  
&emsp; 反之，解码（Decode）/反序列化（deserialization）把从网络、磁盘等读取的字节数组还原成原始对象（通常是原始对象的拷贝），以方便后续的业务逻辑操作。  
&emsp; 进行远程跨进程服务调用时（例如RPC调用），需要使用特定的编解码技术，对需要进行网络传输的对象做编码或者解码，以便完成远程调用。  

&emsp; **Netty为什么要提供编解码框架**  
&emsp; 作为一个高性能的异步、NIO通信框架，编解码框架是Netty的重要组成部分。尽管站在微内核的角度看，编解码框架并不是Netty微内核的组成部分，但是通过ChannelHandler定制扩展出的编解码框架却是不可或缺的。  
&emsp; Netty针对编解码功能，它既提供了通用的编解码框架供用户扩展，又提供了常用的编解码类库供用户直接使用。在保证定制扩展性的基础之上，尽量降低用户的开发工作量和开发门槛，提升开发效率。  
&emsp; Netty预置的编解码功能包括Base64、Bytes、Compresssion、JSON、Marshalling、Protobuf、Serializaton、XML等，如下图所示。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-41.png)  

#### 1.2.2.2. Netty提供的解码器
&emsp; Netty提供了4种解码器来解决，分别如下：

* 固定长度的拆包器 FixedLengthFrameDecoder，每个应用层数据包的都拆分成都是固定长度的大小
* 行拆包器 LineBasedFrameDecoder，每个应用层数据包，都以换行符作为分隔符，进行分割拆分
* 分隔符拆包器 DelimiterBasedFrameDecoder，每个应用层数据包，都通过自定义的分隔符，进行分割拆分
* 基于数据包长度的拆包器 LengthFieldBasedFrameDecoder，将应用层数据包的长度，作为接收端应用层数据包的拆分依据。按照应用层数据包的大小，拆包。这个拆包器，有一个要求，就是应用层协议中包含数据包的长度  

&emsp; 以上解码器在使用时只需要添加到Netty的责任链中即可，大多数情况下这4种解码器都可以满足了，当然除了以上4种解码器，用户也可以自定义自己的解码器进行处理。

##### 1.2.2.2.1. FixedLengthFrameDecoder  
&emsp; 对于使用固定长度的粘包和拆包场景，可以使用FixedLengthFrameDecoder，该解码一器会每次读取固定长度的消息，如果当前读取到的消息不足指定长度，那么就会等待下一个消息到达后进行补足。其使用也比较简单，只需要在构造函数中指定每个消息的长度即可。这里需要注意的是，FixedLengthFrameDecoder只是一个解码一器，Netty也只提供了一个解码一器，这是因为对于解码是需要等待下一个包的进行补全的，代码相对复杂，而对于编码器，用户可以自行编写，因为编码时只需要将不足指定长度的部分进行补全即可。下面的示例中展示了如何使用FixedLengthFrameDecoder来进行粘包和拆包处理：  

```java
public class EchoServer {

  public void bind(int port) throws InterruptedException {
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
      ServerBootstrap bootstrap = new ServerBootstrap();
      bootstrap.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .option(ChannelOption.SO_BACKLOG, 1024)
        .handler(new LoggingHandler(LogLevel.INFO))
        .childHandler(new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel ch) throws Exception {
            // 这里将FixedLengthFrameDecoder添加到pipeline中，指定长度为20
            ch.pipeline().addLast(new FixedLengthFrameDecoder(20));
            // 将前一步解码得到的数据转码为字符串
            ch.pipeline().addLast(new StringDecoder());
            // 这里FixedLengthFrameEncoder是我们自定义的，用于将长度不足20的消息进行补全空格
            ch.pipeline().addLast(new FixedLengthFrameEncoder(20));
            // 最终的数据处理
            ch.pipeline().addLast(new EchoServerHandler());
          }
        });

      ChannelFuture future = bootstrap.bind(port).sync();
      future.channel().closeFuture().sync();
    } finally {
      bossGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
    }
  }

  public static void main(String[] args) throws InterruptedException {
    new EchoServer().bind(8080);
  }
}
```
&emsp; 上面的pipeline中，对于入栈数据，这里主要添加了FixedLengthFrameDecoder和StringDecoder，前面一个用于处理固定长度的消息的粘包和拆包问题，第二个则是将处理之后的消息转换为字符串。最后由EchoServerHandler处理最终得到的数据，处理完成后，将处理得到的数据交由FixedLengthFrameEncoder处理，该编码器是我们自定义的实现，主要作用是将长度不足20的消息进行空格补全。下面是FixedLengthFrameEncoder的实现代码：  

```java
public class FixedLengthFrameEncoder extends MessageToByteEncoder<String> {
  private int length;

  public FixedLengthFrameEncoder(int length) {
    this.length = length;
  }

  @Override
  protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out)
      throws Exception {
    // 对于超过指定长度的消息，这里直接抛出异常
    if (msg.length() > length) {
      throw new UnsupportedOperationException(
          "message length is too large, it's limited " + length);
    }

    // 如果长度不足，则进行补全
    if (msg.length() < length) {
      msg = addSpace(msg);
    }

    ctx.writeAndFlush(Unpooled.wrappedBuffer(msg.getBytes()));
  }

  // 进行空格补全
  private String addSpace(String msg) {
    StringBuilder builder = new StringBuilder(msg);
    for (int i = 0; i < length - msg.length(); i++) {
      builder.append(" ");
    }

    return builder.toString();
  }
}
```
&emsp; 这里FixedLengthFrameEncoder实现了decode()方法，在该方法中，主要是将消息长度不足20的消息进行空格补全。EchoServerHandler的作用主要是打印接收到的消息，然后发送响应给客户端：  

```java
public class EchoServerHandler extends SimpleChannelInboundHandler<String> {

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
    System.out.println("server receives message: " + msg.trim());
    ctx.writeAndFlush("hello client!");
  }
}
```
&emsp; 对于客户端，其实现方式基本与服务端的使用方式类似，只是在最后进行消息发送的时候与服务端的处理方式不同。如下是客户端EchoClient的代码：

```java
public class EchoClient {

  public void connect(String host, int port) throws InterruptedException {
    EventLoopGroup group = new NioEventLoopGroup();
    try {
      Bootstrap bootstrap = new Bootstrap();
      bootstrap.group(group)
        .channel(NioSocketChannel.class)
        .option(ChannelOption.TCP_NODELAY, true)
        .handler(new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel ch) throws Exception {
            // 对服务端发送的消息进行粘包和拆包处理，由于服务端发送的消息已经进行了空格补全，
            // 并且长度为20，因而这里指定的长度也为20
            ch.pipeline().addLast(new FixedLengthFrameDecoder(20));
            // 将粘包和拆包处理得到的消息转换为字符串
            ch.pipeline().addLast(new StringDecoder());
            // 对客户端发送的消息进行空格补全，保证其长度为20
            ch.pipeline().addLast(new FixedLengthFrameEncoder(20));
            // 客户端发送消息给服务端，并且处理服务端响应的消息
            ch.pipeline().addLast(new EchoClientHandler());
          }
        });

      ChannelFuture future = bootstrap.connect(host, port).sync();
      future.channel().closeFuture().sync();
    } finally {
      group.shutdownGracefully();
    }
  }

  public static void main(String[] args) throws InterruptedException {
    new EchoClient().connect("127.0.0.1", 8080);
  }
}
```
&emsp; 对于客户端而言，其消息的处理流程其实与服务端是相似的，对于入站消息，需要对其进行粘包和拆包处理，然后将其转码为字符串，对于出站消息，则需要将长度不足20的消息进行空格补全。客户端与服务端处理的主要区别在于最后的消息处理handler不一样，也即这里的EchoClientHandler，如下是该handler的源码：

```java
public class EchoClientHandler extends SimpleChannelInboundHandler<String> {

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
    System.out.println("client receives message: " + msg.trim());
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    ctx.writeAndFlush("hello server!");
  }
}
```
&emsp; 这里客户端的处理主要是重写了channelActive()和channelRead0()两个方法，这两个方法的主要作用在于，channelActive()会在客户端连接上服务器时执行，也就是说，其连上服务器之后就会往服务器发送消息。而channelRead0()主要是在服务器发送响应给客户端时执行，这里主要是打印服务器的响应消息。对于服务端而言，前面我们我们可以看到，EchoServerHandler只重写了channelRead0()方法，这是因为服务器只需要等待客户端发送消息过来，然后在该方法中进行处理，处理完成后直接将响应发送给客户端。如下是分别启动服务端和客户端之后控制台打印的数据：  

```java
// server
server receives message: hello server!
```

```java
// client
client receives message: hello client!
```

##### 1.2.2.2.2. LineBasedFrameDecoder与DelimiterBasedFrameDecoder  
&emsp; 对于通过分隔符进行粘包和拆包问题的处理，Netty提供了两个编解码的类，LineBasedFrameDecoder和DelimiterBasedFrameDecoder。这里LineBasedFrameDecoder的作用主要是通过换行符，即\n或者\r\n对数据进行处理；而DelimiterBasedFrameDecoder的作用则是通过用户指定的分隔符对数据进行粘包和拆包处理。同样的，这两个类都是解码一器类，而对于数据的编码，也即在每个数据包最后添加换行符或者指定分割符的部分需要用户自行进行处理。这里以DelimiterBasedFrameDecoder为例进行讲解，如下是EchoServer中使用该类的代码片段，其余部分与前面的例子中的完全一致：  

```java
@Override
protected void initChannel(SocketChannel ch) throws Exception {
    String delimiter = "_$";
    // 将delimiter设置到DelimiterBasedFrameDecoder中，经过该解码一器进行处理之后，源数据将会
    // 被按照_$进行分隔，这里1024指的是分隔的最大长度，即当读取到1024个字节的数据之后，若还是未
    // 读取到分隔符，则舍弃当前数据段，因为其很有可能是由于码流紊乱造成的
    ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1024,
        Unpooled.wrappedBuffer(delimiter.getBytes())));
    // 将分隔之后的字节数据转换为字符串数据
    ch.pipeline().addLast(new StringDecoder());
    // 这是我们自定义的一个编码器，主要作用是在返回的响应数据最后添加分隔符
    ch.pipeline().addLast(new DelimiterBasedFrameEncoder(delimiter));
    // 最终处理数据并且返回响应的handler
    ch.pipeline().addLast(new EchoServerHandler());
}
```
&emsp; 上面pipeline的设置中，添加的解码一器主要有DelimiterBasedFrameDecoder和StringDecoder，经过这两个处理器处理之后，接收到的字节流就会被分隔，并且转换为字符串数据，最终交由EchoServerHandler处理。这里DelimiterBasedFrameEncoder是我们自定义的编码器，其主要作用是在返回的响应数据之后添加分隔符。如下是该编码器的源码：  

```java
public class DelimiterBasedFrameEncoder extends MessageToByteEncoder<String> {

  private String delimiter;

  public DelimiterBasedFrameEncoder(String delimiter) {
    this.delimiter = delimiter;
  }

  @Override
  protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out) 
      throws Exception {
    // 在响应的数据后面添加分隔符
    ctx.writeAndFlush(Unpooled.wrappedBuffer((msg + delimiter).getBytes()));
  }
}
```
&emsp; 对于客户端而言，这里的处理方式与服务端类似，其pipeline的添加方式如下：  

```java
@Override
protected void initChannel(SocketChannel ch) throws Exception {
    String delimiter = "_$";
    // 对服务端返回的消息通过_$进行分隔，并且每次查找的最大大小为1024字节
    ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1024, 
        Unpooled.wrappedBuffer(delimiter.getBytes())));
    // 将分隔之后的字节数据转换为字符串
    ch.pipeline().addLast(new StringDecoder());
    // 对客户端发送的数据进行编码，这里主要是在客户端发送的数据最后添加分隔符
    ch.pipeline().addLast(new DelimiterBasedFrameEncoder(delimiter));
    // 客户端发送数据给服务端，并且处理从服务端响应的数据
    ch.pipeline().addLast(new EchoClientHandler());
}
```
&emsp; 这里客户端的处理方式与服务端基本一致，关于这里没展示的代码，其与示例一中的代码完全一致，这里则不予展示。  

##### 1.2.2.2.3. LengthFieldBasedFrameDecoder与LengthFieldPrepender
&emsp; 这里LengthFieldBasedFrameDecoder与LengthFieldPrepender需要配合起来使用，其实本质上来讲，这两者一个是解码，一个是编码的关系。它们处理粘拆包的主要思想是在生成的数据包中添加一个长度字段，用于记录当前数据包的长度。LengthFieldBasedFrameDecoder会按照参数指定的包长度偏移量数据对接收到的数据进行解码，从而得到目标消息体数据；而LengthFieldPrepender则会在响应的数据前面添加指定的字节数据，这个字节数据中保存了当前消息体的整体字节数据长度。LengthFieldBasedFrameDecoder的解码过程如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-42.png)  
&emsp; LengthFieldPrepender的编码过程如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/netty/netty-43.png)  

&emsp; 关于LengthFieldBasedFrameDecoder，这里需要对其构造函数参数进行介绍：  

* maxFrameLength：指定了每个包所能传递的最大数据包大小；
* lengthFieldOffset：指定了长度字段在字节码中的偏移量；
* lengthFieldLength：指定了长度字段所占用的字节长度；
* lengthAdjustment：对一些不仅包含有消息头和消息体的数据进行消息头的长度的调整，这样就可以只得到消息体的数据，这里的lengthAdjustment指定的就是消息头的长度；
* initialBytesToStrip：对于长度字段在消息头中间的情况，可以通过initialBytesToStrip忽略掉消息头以及长度字段占用的字节。

&emsp; 这里以json序列化为例对LengthFieldBasedFrameDecoder和LengthFieldPrepender的使用方式进行讲解。如下是EchoServer的源码： 

```java
public class EchoServer {

  public void bind(int port) throws InterruptedException {
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
      ServerBootstrap bootstrap = new ServerBootstrap();
      bootstrap.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .option(ChannelOption.SO_BACKLOG, 1024)
        .handler(new LoggingHandler(LogLevel.INFO))
        .childHandler(new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel ch) throws Exception {
            // 这里将LengthFieldBasedFrameDecoder添加到pipeline的首位，因为其需要对接收到的数据
            // 进行长度字段解码，这里也会对数据进行粘包和拆包处理
            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 0, 2, 0, 2));
            // LengthFieldPrepender是一个编码器，主要是在响应字节数据前面添加字节长度字段
            ch.pipeline().addLast(new LengthFieldPrepender(2));
            // 对经过粘包和拆包处理之后的数据进行json反序列化，从而得到User对象
            ch.pipeline().addLast(new JsonDecoder());
            // 对响应数据进行编码，主要是将User对象序列化为json
            ch.pipeline().addLast(new JsonEncoder());
            // 处理客户端的请求的数据，并且进行响应
            ch.pipeline().addLast(new EchoServerHandler());
          }
        });

      ChannelFuture future = bootstrap.bind(port).sync();
      future.channel().closeFuture().sync();
    } finally {
      bossGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
    }
  }

  public static void main(String[] args) throws InterruptedException {
    new EchoServer().bind(8080);
  }
}
```
&emsp; 这里EchoServer主要是在pipeline中添加了两个编码器和两个解码一器，编码器主要是负责将响应的User对象序列化为json对象，然后在其字节数组前面添加一个长度字段的字节数组；解码一器主要是对接收到的数据进行长度字段的解码，然后将其反序列化为一个User对象。下面是JsonDecoder的源码：  

```java
public class JsonDecoder extends MessageToMessageDecoder<ByteBuf> {

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) 
      throws Exception {
    byte[] bytes = new byte[buf.readableBytes()];
    buf.readBytes(bytes);
    User user = JSON.parseObject(new String(bytes, CharsetUtil.UTF_8), User.class);
    out.add(user);
  }
}
```
&emsp; JsonDecoder首先从接收到的数据流中读取字节数组，然后将其反序列化为一个User对象。下面我们看看JsonEncoder的源码：  

```java
public class JsonEncoder extends MessageToByteEncoder<User> {

  @Override
  protected void encode(ChannelHandlerContext ctx, User user, ByteBuf buf)
      throws Exception {
    String json = JSON.toJSONString(user);
    ctx.writeAndFlush(Unpooled.wrappedBuffer(json.getBytes()));
  }
}
```
&emsp; JsonEncoder将响应得到的User对象转换为一个json对象，然后写入响应中。对于EchoServerHandler，其主要作用就是接收客户端数据，并且进行响应，如下是其源码：  

```java
public class EchoServerHandler extends SimpleChannelInboundHandler<User> {

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, User user) throws Exception {
    System.out.println("receive from client: " + user);
    ctx.write(user);
  }
}
```
&emsp; 对于客户端，其主要逻辑与服务端的基本类似，这里主要展示其pipeline的添加方式，以及最后发送请求，并且对服务器响应进行处理的过程：  

```java
@Override
protected void initChannel(SocketChannel ch) throws Exception {
    ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 0, 2, 0, 2));
    ch.pipeline().addLast(new LengthFieldPrepender(2));
    ch.pipeline().addLast(new JsonDecoder());
    ch.pipeline().addLast(new JsonEncoder());
    ch.pipeline().addLast(new EchoClientHandler());
}

public class EchoClientHandler extends SimpleChannelInboundHandler<User> {

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    ctx.write(getUser());
  }

  private User getUser() {
    User user = new User();
    user.setAge(27);
    user.setName("zhangxufeng");
    return user;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, User user) throws Exception {
    System.out.println("receive message from server: " + user);
  }
}
```
&emsp; 这里客户端首先会在连接上服务器时，往服务器发送一个User对象数据，然后在接收到服务器响应之后，会打印服务器响应的数据。  

##### 1.2.2.2.4. 自定义粘包与拆包器
&emsp; 对于粘包与拆包问题，其实前面三种基本上已经能够满足大多数情形了，但是对于一些更加复杂的协议，可能有一些定制化的需求。对于这些场景，其实本质上，我们也不需要手动从头开始写一份粘包与拆包处理器，而是通过继承LengthFieldBasedFrameDecoder和LengthFieldPrepender来实现粘包和拆包的处理。  
&emsp; 如果用户确实需要不通过继承的方式实现自己的粘包和拆包处理器，这里可以通过实现MessageToByteEncoder和ByteToMessageDecoder来实现。这里MessageToByteEncoder的作用是将响应数据编码为一个ByteBuf对象，而ByteToMessageDecoder则是将接收到的ByteBuf数据转换为某个对象数据。通过实现这两个抽象类，用户就可以达到实现自定义粘包和拆包处理的目的。如下是这两个类及其抽象方法的声明：  

```java
public abstract class ByteToMessageDecoder extends ChannelInboundHandlerAdapter {
    protected abstract void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) 
        throws Exception;
}
```

```java
public abstract class MessageToByteEncoder<I> extends ChannelOutboundHandlerAdapter {
    protected abstract void encode(ChannelHandlerContext ctx, I msg, ByteBuf out) 
        throws Exception;
}
```
