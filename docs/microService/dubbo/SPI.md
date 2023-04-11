<!-- TOC -->

- [1. Dubbo SPI](#1-dubbo-spi)
    - [1.1. SPI简介](#11-spi简介)
    - [1.2. dubbo的spi](#12-dubbo的spi)
    - [1.3. SPI示例](#13-spi示例)
        - [1.3.1. Java SPI 示例](#131-java-spi-示例)
        - [1.3.2. Dubbo SPI示例](#132-dubbo-spi示例)
    - [1.4. 扩展点特性](#14-扩展点特性)
        - [1.4.1. 扩展点自动包装，Wrapper机制](#141-扩展点自动包装wrapper机制)
        - [1.4.2. 扩展点自动装配](#142-扩展点自动装配)
        - [1.4.3. 扩展点自适应](#143-扩展点自适应)
        - [1.4.4. 扩展点自动激活](#144-扩展点自动激活)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
0. ExtensionLoader是Dubbo SPI中用来加载扩展类的，有如下三个重要方法，搞懂这3个方法基本上就搞懂Dubbo SPI了。  
&emsp; 加载扩展类的三种方法如下  
    * getExtension()，获取普通扩展类
    * getAdaptiveExtension()，获取自适应扩展类
    * getActivateExtension()，获取自动激活的扩展类
2. 扩展点特性
    * 扩展点自动包装，Wrapper机制
    * 扩展点自动装配
    * 扩展点自适应
    * 扩展点自动激活
1. **<font color = "clime">~~Dubbo改进了JDK标准的SPI的以下问题：~~</font>**  
    * JDK标准的SPI会一次性实例化扩展点所有实现。 将该实现类直接作为默认实现，不再自动生成代码 标记在方法上：生成接口对应的Adaptive类，通过url中的参数来确定最终的实现类。    
    * 如果扩展点加载失败，连扩展点的名称都拿不到了。  
    * 增加了对扩展点IoC和AOP的支持，一个扩展点可以直接setter注入其它扩展点。  

--------------  
1. JDK SPI的缺点：  
&emsp; JDK标准的SPI会一次性实例化扩展点所有实现。如果扩展点加载失败，连扩展点的名称都拿不到了。   
&emsp; 而Dubbo的SPI不会一次性全部加载并实例化，它会按需加载（指定、自适应、自动激活）。  

    1. JDK标准的SPI会一次性实例化扩展点的所有实现。而Dubbo SPI能实现按需加载
    2. Dubbo SPI增加了对扩展点Ioc和Aop的支持

2. 扩展点自适应  
&emsp; 使用@Adaptive注解，动态的通过URL中的参数来确定要使用哪个具体的实现类。  
3. 扩展点自动激活  
&emsp; 使用@Activate注解，可以标记对应的扩展点默认被激活使用，可以通过指定group或者value，在不同条件下获取自动激活的扩展点。  
&emsp; Dubbo的激活扩展是指根据分组和url参数中的key，结合扩展类上的注解Activate，生成符合匹配条件的扩展实例，得到一个实例集合。  
&emsp; 激活扩展在Dubbo中一个典型的应用场景就是过滤器(Filter), 在服务端收到请求之后，经过一系列过滤器去拦截请求，做一些处理工作，然后在真正去调用实现类。  



# 1. Dubbo SPI  
&emsp; **<font color = "red">官网：http://dubbo.apache.org/</font>** 
<!--

https://www.javashitang.com/md/dubbo/Dubbo%20SPI%E6%98%AF%E5%A6%82%E4%BD%95%E5%AE%9E%E7%8E%B0%20AOP%EF%BC%8CIOC%EF%BC%8C%E8%87%AA%E9%80%82%E5%BA%94%EF%BC%8C%E8%87%AA%E5%8A%A8%E6%BF%80%E6%B4%BB%E7%9A%84.html

https://www.javashitang.com/md/dubbo/5%E5%88%86%E9%92%9F%E4%BA%86%E8%A7%A3%20Dubbo%20SPI%20%E7%9A%84%E7%89%B9%E6%80%A7.html

Dubbo 扩展点加载机制：从 Java SPI 到 Dubbo SPI 
https://mp.weixin.qq.com/s/PMF2kqT-XnAVmrxoutE0eQ
来说说Dubbo SPI 机制 
https://mp.weixin.qq.com/s/hR8hlJyGxnn3wIfBhlNbuQ

DUBBO SPI机制六个特性
https://mp.weixin.qq.com/s/Bf8zxRAv3jvxOIFFIe2ZwA

-->

## 1.1. SPI简介  
&emsp; **<font color = "red">SPI全称为Service Provider Interface，是一种服务发现机制。[SPI](/docs/java/basis/SPI.md)的本质是将接口实现类的全限定名配置在文件中，并由服务加载器读取配置文件，加载实现类。这样可以在运行时，动态的为接口替换实现类。</font>** 正因此特性，可以很容易的通过SPI机制为程序提供拓展功能。  
&emsp; SPI机制在第三方框架中也有所应用，比如Dubbo就是通过SPI机制加载所有的组件。不过，Dubbo并未使用Java原生的SPI机制，而是对其进行了增强，使其能够更好的满足需求。 **<font color = "clime">Dubbo改进了JDK标准的SPI的以下问题：</font>**  

* JDK标准的SPI会一次性实例化扩展点所有实现，如果有扩展实现初始化很耗时，但如果没用上也加载，会很浪费资源。
* 如果扩展点加载失败，连扩展点的名称都拿不到了。比如：JDK标准的ScriptEngine，通过getName()获取脚本类型的名称，但如果RubyScriptEngine因为所依赖的jruby.jar不存在，导致RubyScriptEngine类加载失败，这个失败原因被吃掉了，和ruby对应不起来，当用户执行ruby脚本时，会报不支持ruby，而不是真正失败的原因。  
* 增加了对扩展点IoC和AOP的支持，一个扩展点可以直接setter注入其它扩展点。  

<!-- 

&emsp; Java SPI和Dubbo SPI的区别总结如下：

* 使用上的区别Dubbo使用ExtensionLoader而不是ServiceLoader了，其主要逻辑都封装在这个类中
* 配置文件存放目录不一样，Java的在META-INF/services，Dubbo在META-INF/dubbo，META-INF/dubbo/internal
* **Java SPI 会一次性实例化扩展点所有实现，**如果有扩展实现初始化很耗时，并且又用不上，会造成大量资源被浪费
* **Dubbo SPI增加了对扩展点IOC和AOP的支持，**一个扩展点可以直接 setter 注入其它扩展点
* **Java SPI加载过程失败，扩展点的名称是拿不到的。**比如：JDK标准的ScriptEngine，getName() 获取脚本类型的名称，如果RubyScriptEngine 因为所依赖的 jruby.jar 不存在，导致 RubyScriptEngine类加载失败，这个失败原因是不会有任何提示的，当用户执行 ruby 脚本时，会报不支持 ruby，而不是真正失败的原因  

Dubbo改进了JDK标准的SPI的以下问题：  

* <font color = "red">JDK标准的SPI会一次性实例化扩展点所有实现，</font>如果有扩展实现初始化很耗时，但如果没用上也加载，会很浪费资源。
* <font color = "red">如果扩展点加载失败，连扩展点的名称都拿不到了。</font>比如：JDK标准的ScriptEngine，通过getName()获取脚本类型的名称，但如果 RubyScriptEngine因为所依赖的jruby.jar不存在，导致RubyScriptEngine类加载失败，这个失败原因被吃掉了，和ruby对应不起来，当用户执行ruby脚本时，会报不支持ruby，而不是真正失败的原因。
* <font color = "red">增加了对扩展点IoC和AOP的支持，</font>一个扩展点可以直接setter注入其它扩展点。
-->

## 1.2. dubbo的spi
&emsp; dubbo的spi有如下几个概念：  
&emsp; （1）扩展点：一个接口。  
&emsp; （2）扩展：扩展（接口）的实现。  
&emsp; （3）扩展自适应实例：其实就是一个Extension的代理，它实现了扩展点接口。在调用扩展点的接口方法时，会根据实际的参数来决定要使用哪个扩展。dubbo会根据接口中的参数，自动地决定选择哪个实现。  
&emsp; （4）@SPI:该注解作用于扩展点的接口上，表明该接口是一个扩展点。  
&emsp; （5）@Adaptive：@Adaptive注解用在扩展接口的方法上。表示该方法是一个自适应方法。Dubbo在为扩展点生成自适应实例时，如果方法有@Adaptive注解，会为该方法生成对应的代码。   

&emsp; Dubbo中的扩展点有哪些？
&emsp; [Dubbo中已经实现的扩展](https://dubbo.apache.org/zh/docs/v2.7/dev/impls/)：  
&emsp; 协议扩展 、调用拦截扩展 、引用监听扩展 、暴露监听扩展 、集群扩展 、路由扩展 、负载均衡扩展 、合并结果扩展 、注册中心扩展 、监控中心扩展 、扩展点加载扩展 、动态代理扩展 、编译器扩展 、Dubbo 配置中心扩展 、消息派发扩展 、线程池扩展 、序列化扩展 、网络传输扩展 、信息交换扩展 、组网扩展 、Telnet 命令扩展 、状态检查扩展 、容器扩展 、缓存扩展 、验证扩展 、日志适配扩展。  

## 1.3. SPI示例  
&emsp; 接下来，先来了解一下Java SPI与Dubbo SPI的用法。   

### 1.3.1. Java SPI 示例  
&emsp; 定义一个接口，名称为Robot。  

```java
public interface Robot {
    void sayHello();
}
```
&emsp; 接下来定义两个实现类，分别为OptimusPrime和Bumblebee。  

```java
public class OptimusPrime implements Robot {
    
    @Override
    public void sayHello() {
        System.out.println("Hello, I am Optimus Prime.");
    }
}

public class Bumblebee implements Robot {

    @Override
    public void sayHello() {
        System.out.println("Hello, I am Bumblebee.");
    }
}
```
&emsp; 接下来META-INF/services文件夹下创建一个文件，名称为Robot的全限定名org.apache.spi.Robot。文件内容为实现类的全限定的类名，如下：  

```text
org.apache.spi.OptimusPrime
org.apache.spi.Bumblebee
```
&emsp; 做好所需的准备工作，接下来编写代码进行测试。  

```java
public class JavaSPITest {

    @Test
    public void sayHello() throws Exception {
        ServiceLoader<Robot> serviceLoader = ServiceLoader.load(Robot.class);
        System.out.println("Java SPI");
        serviceLoader.forEach(Robot::sayHello);
    }
}
```
&emsp; 最后来看一下测试结果，如下：  
![image](http://182.92.69.8:8081/img/microService/Dubbo/dubbo-27.png)   
&emsp; 从测试结果可以看出，两个实现类被成功的加载，并输出了相应的内容。关于Java SPI的演示先到这里，接下来演示Dubbo SPI。  

### 1.3.2. Dubbo SPI示例  
&emsp; <font color = "red">Dubbo SPI的相关逻辑被封装在了ExtensionLoader类中，通过ExtensionLoader，可以加载指定的实现类。Dubbo SPI所需的配置文件需放置在META-INF/dubbo路径下，</font>配置内容如下。  

```properties
optimusPrime = org.apache.spi.OptimusPrime
bumblebee = org.apache.spi.Bumblebee
```
&emsp; 与Java SPI实现类配置不同，Dubbo SPI是通过键值对的方式进行配置，这样可以按需加载指定的实现类。另外，在测试Dubbo SPI时，需要在Robot接口上标注@SPI注解。下面来演示Dubbo SPI的用法：  

```java
public class DubboSPITest {

    @Test
    public void sayHello() throws Exception {
        ExtensionLoader<Robot> extensionLoader = 
            ExtensionLoader.getExtensionLoader(Robot.class);
        Robot optimusPrime = extensionLoader.getExtension("optimusPrime");
        optimusPrime.sayHello();
        Robot bumblebee = extensionLoader.getExtension("bumblebee");
        bumblebee.sayHello();
    }
}
```
&emsp; 测试结果如下：  
![image](http://182.92.69.8:8081/img/microService/Dubbo/dubbo-22.png)   

&emsp; Dubbo SPI中常用的注解：  

* @SPI标记为扩展接口  
* @Adaptive自适应拓展实现类标志  
* @Activate自动激活条件的标记  

## 1.4. 扩展点特性  
&emsp; Dubbo的SPI主要有两种：

* [获得指定拓展对象](/docs/microService/dubbo/getExtension.md)  
* [获得自适应的拓展对象](/docs/microService/dubbo/getAdaptiveExtension.md)  

### 1.4.1. 扩展点自动包装，Wrapper机制  
<!-- 
Dubbo扩展机制（三）Wrapper【代理】
https://www.cnblogs.com/caoxb/p/13140345.html
-->
&emsp; <font color = "color">自动包装扩展点的Wrapper类。</font>ExtensionLoader在加载扩展点时，如果加载到的扩展点有拷贝构造函数，则判定为扩展点Wrapper类。  
&emsp; Wrapper类内容：  

```java
import org.apache.dubbo.rpc.Protocol;
 
public class XxxProtocolWrapper implements Protocol {
    Protocol impl;
 
    public XxxProtocolWrapper(Protocol protocol) { impl = protocol; }
 
    // 接口方法做一个操作后，再调用extension的方法
    public void refer() {
        //... 一些操作
        impl.refer();
        // ... 一些操作
    }
 
    // ...
}
```
&emsp; **Wrapper类同样实现了扩展点接口，但是Wrapper不是扩展点的真正实现。它的用途主要是用于从ExtensionLoader返回扩展点时，包装在真正的扩展点实现外。即从ExtensionLoader中返回的实际上是Wrapper类的实例，Wrapper持有了实际的扩展点实现类。**  
&emsp; 扩展点的Wrapper类可以有多个，也可以根据需要新增。  
&emsp; 通过Wrapper类可以把所有扩展点公共逻辑移至Wrapper中。新加的Wrapper在所有的扩展点上添加了逻辑，有些类似AOP，即Wrapper代理了扩展点。  

&emsp; **Wrapper的规范：**  
&emsp; Wrapper机制不是通过注解实现的，而是通过一套Wrapper规范实现的。Wrapper类在定义时需要遵循如下规范：  

* 该类要实现 SPI 接口
* 该类中要有 SPI 接口的引用
* 该类中必须含有一个含参的构造方法且参数只能有一个类型为SPI借口
* 在接口实现方法中要调用SPI接口引用对象的相应方法
* 该类名称以Wrapper结尾

### 1.4.2. 扩展点自动装配  
&emsp; **加载扩展点时，自动注入依赖的扩展点。**加载扩展点时，扩展点实现类的成员如果为其它扩展点类型，ExtensionLoader会自动注入依赖的扩展点。ExtensionLoader通过扫描扩展点实现类的所有setter方法来判定其成员。即ExtensionLoader会执行扩展点的拼装操作。  
&emsp; 示例：有两个为扩展点CarMaker（造车者）、WheelMaker(造轮者)  
&emsp; 接口类如下：  

```java
public interface CarMaker {
    Car makeCar();
}
 
public interface WheelMaker {
    Wheel makeWheel();
}
```
&emsp; CarMaker 的一个实现类：  

```java
public class RaceCarMaker implements CarMaker {
    WheelMaker wheelMaker;
 
    public setWheelMaker(WheelMaker wheelMaker) {
        this.wheelMaker = wheelMaker;
    }
 
    public Car makeCar() {
        // ...
        Wheel wheel = wheelMaker.makeWheel();
        // ...
        return new RaceCar(wheel, ...);
    }
}
```
&emsp; ExtensionLoader加载CarMaker的扩展点实现RaceCarMaker时，setWheelMaker方法的WheelMaker也是扩展点则会注入WheelMaker的实现。  
&emsp; 这里带来另一个问题，ExtensionLoader要注入依赖扩展点时，如何决定要注入依赖扩展点的哪个实现。在这个示例中，即是在多个WheelMaker的实现中要注入哪个。这个问题在下面一点扩展点自适应中说明。  

### 1.4.3. 扩展点自适应  
<!--
Adaptive【URL-动态适配】
https://www.cnblogs.com/caoxb/p/13140329.html
-->
&emsp; ExtensionLoader注入的依赖扩展点是一个Adaptive实例，直到扩展点方法执行时才决定调用是哪一个扩展点实现。Dubbo使用URL对象（包含了Key-Value）传递配置信息。扩展点方法调用会有URL参数（或是参数有URL成员），这样依赖的扩展点也可以从URL拿到配置信息，所有的扩展点自己定好配置的Key后，配置信息从URL上从最外层传入。URL在配置传递上即是一条总线。  
&emsp; 示例：有两个为扩展点CarMaker、WheelMaker  
&emsp; 接口类如下：  

```java
public interface CarMaker {
    Car makeCar(URL url);
}
 
public interface WheelMaker {
    Wheel makeWheel(URL url);
}
```

&emsp; CarMaker 的一个实现类：  

```java
public class RaceCarMaker implements CarMaker {
    WheelMaker wheelMaker;
 
    public setWheelMaker(WheelMaker wheelMaker) {
        this.wheelMaker = wheelMaker;
    }
 
    public Car makeCar(URL url) {
        // ...
        Wheel wheel = wheelMaker.makeWheel(url);
        // ...
        return new RaceCar(wheel, ...);
    }
}
```
&emsp; 当上面执行  

```java
// ...
Wheel wheel = wheelMaker.makeWheel(url);
// ...
```
&emsp; 注入的Adaptive实例可以提取约定Key来决定使用哪个WheelMaker实现来调用对应实现的真正的makeWheel方法。如提取wheel.type, key即url.get("wheel.type") 来决定 WheelMake实现。Adaptive 实例的逻辑是固定，指定提取的URL的Key，即可以代理真正的实现类上，可以动态生成。  
&emsp; 在Dubbo的ExtensionLoader的扩展点类对应的Adaptive实现是在加载扩展点里动态生成。指定提取的URL的Key通过@Adaptive注解在接口方法上提供。  
&emsp; 下面是Dubbo的Transporter扩展点的代码：  

```java
public interface Transporter {
    @Adaptive({"server", "transport"})
    Server bind(URL url, ChannelHandler handler) throws RemotingException;
 
    @Adaptive({"client", "transport"})
    Client connect(URL url, ChannelHandler handler) throws RemotingException;
}
```
&emsp; 对于bind()方法，Adaptive实现先查找server key，如果该Key没有值则找 transport key值，来决定代理到哪个实际扩展点。  

### 1.4.4. 扩展点自动激活  
<!-- 

https://blog.csdn.net/yuanshangshenghuo/article/details/106437399
-->

&emsp; Dubbo的激活扩展是指根据分组和url参数中的key，结合扩展类上的注解Activate，生成符合匹配条件的扩展实例，得到一个实例集合。  
&emsp; 激活扩展在Dubbo中一个典型的应用场景就是过滤器(Filter), 在服务端收到请求之后，经过一系列过滤器去拦截请求，做一些处理工作，然后在真正去调用实现类。  

&emsp; 对于集合类扩展点，比如：Filter, InvokerListener, ExportListener, TelnetHandler, StatusChecker等，可以同时加载多个实现，此时，可以用自动激活来简化配置，如：  

```java
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
 
@Activate // 无条件自动激活
public class XxxFilter implements Filter {
    // ...
}
```
&emsp; 或：  

```java
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
 
@Activate("xxx") // 当配置了xxx参数，并且参数为有效值时激活，比如配了cache="lru"，自动激活CacheFilter。
public class XxxFilter implements Filter {
    // ...
}
```
&emsp; 或：

```java
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
 
@Activate(group = "provider", value = "xxx") // 只对提供方激活，group可选"provider"或"consumer"
public class XxxFilter implements Filter {
    // ...
}
```

1. 注意：这里的配置文件是放在程序中的jar包内，不是dubbo本身的jar包内，Dubbo会全 ClassPath扫描所有jar包内同名的这个文件，然后进行合并。
2. 注意：扩展点使用单一实例加载（请确保扩展实现的线程安全性），缓存在 ExtensionLoader中。
