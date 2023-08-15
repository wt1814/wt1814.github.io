<!-- TOC -->

- [1. 类加载的方式：类加载器](#1-类加载的方式类加载器)
    - [1.1. 类加载器的分类](#11-类加载器的分类)
    - [1.2. tomcat类加载器](#12-tomcat类加载器)
    - [1.3. 类加载器的加载机制](#13-类加载器的加载机制)
        - [1.3.1. 双亲委派模型](#131-双亲委派模型)
        - [1.3.2. ~~破坏双亲委派模型~~](#132-破坏双亲委派模型)
            - [1.3.2.1. 破坏示例](#1321-破坏示例)
                - [1.3.2.1.1. JDK破坏](#13211-jdk破坏)
                - [1.3.2.1.2. 热替换](#13212-热替换)
                - [1.3.2.1.3. Tomcat](#13213-tomcat)
                - [1.3.2.1.4. Spring](#13214-spring)
            - [1.3.2.2. 小结：两种破坏方式](#1322-小结两种破坏方式)
        - [1.3.3. 自己写的java.lang.String可以让jvm加载到吗？](#133-自己写的javalangstring可以让jvm加载到吗)
    - [1.4. 类加载器应用](#14-类加载器应用)
        - [1.4.1. 自定义类加载器](#141-自定义类加载器)
        - [1.4.2. 查看Boostrap ClassLoader 加载的类库](#142-查看boostrap-classloader-加载的类库)
        - [1.4.3. 如何在启动时观察加载了哪个jar包中的哪个类？](#143-如何在启动时观察加载了哪个jar包中的哪个类)
        - [1.4.4. 观察特定类的加载上下文](#144-观察特定类的加载上下文)
    - [1.5. 类加载错误](#15-类加载错误)
    - [1.6. java.lang.ClassLoader](#16-javalangclassloader)
        - [1.6.1. loadClass方法](#161-loadclass方法)
        - [1.6.2. findClass方法](#162-findclass方法)
        - [1.6.3. defineClass方法](#163-defineclass方法)
        - [1.6.4. 小结](#164-小结)

<!-- /TOC -->


&emsp; **<font color = "red">总结：</font>**  
1. JVM默认提供三个类加载器：启动类加载器、扩展类加载器、应用类加载器。  
2. `自定义类加载器`：需要继承自ClassLoader，`重写方法findClass()`。   
3. 双亲委派模型，一个类加载器首先将类加载请求转发到父类加载器，只有当父类加载器无法完成时才尝试自己加载。  
&emsp; 双亲委派模型中，类加载器之间的父子关系一般不会以继承（Inheritance）的关系来实现，而是使用组合（Composition）关系来复用父加载器的代码的。  
&emsp; 好处：避免类的重复加载；防止核心API被随意篡改。   
4. ~~破坏双亲委派模型：~~  
    1. 破坏双亲委派模型的例子  
        1. `双亲委派模型有一个问题：顶层ClassLoader无法加载底层ClassLoader的类，典型例子JNDI、JDBC。`
            * **<font color = "clime">JDBC是启动类加载器加载，但 mysql 驱动是应用类加载器，而 JDBC 运行时又需要去访问子类加载器加载的驱动，就破坏了该模型。所以加入了`线程上下文类加载器(Thread Context ClassLoader)`，</font>** 可以通过Thread.setContextClassLoaser()设置该类加载器，然后顶层ClassLoader再使用Thread.getContextClassLoader()获得底层的ClassLoader进行加载。  
        2. Tomcat中使用了自定义ClassLoader，使得一个Tomcat中可以加载多个应用。一个Tomcat可以部署N个web应用，但是每个web应用都有自己的classloader，互不干扰。比如web1里面有com.test.A.class，web2里面也有com.test.A.class，`如果没打破双亲委派模型的话，那么web1加载完后，web2再加载的话会冲突。`    
        3. Spring破坏类加载器  
    2. 破坏双亲委派模型的方式  
        1. 继承ClassLoader，重写loadClass()方法。  
        2. `使用线程上下文类加载器(Thread Context ClassLoader)`  
5. java.lang.ClassLoader   
    1. java.lang.ClassLoader3个主要方法  
        * loadClass() 就是主要进行类加载的方法，默认的双亲委派机制就实现在这个方法中。
        * findClass() 根据名称或位置加载.class字节码
        * definclass() 把字节码转化为Class
    2. `自定义类加载器`：需要继承自ClassLoader，`重写方法findClass()`。   
        1. 继承ClassLoader  
        2. 继承ClassLoader   
        3. 调用defineClass()方法 
    3. `破坏双亲委派模型`：继承ClassLoader，`重写loadClass()方法`；或`使用线程上下文类加载器(Thread Context ClassLoader)`。    


# 1. 类加载的方式：类加载器 
<!-- 
*** https://www.zhihu.com/question/45022217



--> 
![image](http://182.92.69.8:8081/img/java/JVM/JVM-6.png)  

## 1.1. 类加载器的分类  
&emsp; <font color = "red">类加载子系统也可以称之为类加载器，JVM默认提供三个类加载器：启动类加载器、扩展类加载器、应用类加载器。</font>  
1. 启动类加载器(BootStrap ClassLoader)，是最顶层的类加载器，加载 jre/lib包下面的jar文件(JDK中的核心类库，⽆法被java程序直接引⽤)，如rt.jar、resources.jar、charsets.jar等、被-Xbootclasspath参数所指定的路径中，并且是虚拟机会识别的jar类库加载到内存中。  
2. 扩展类加载器(Extension ClassLoader)，加载jre/lib/ext包下面的jar文件、或者被java.ext.dirs系统变量指定的路径中的所有类库。  
3. 应用类加载器(Application ClassLoader)，负责加载应用程序classpath目录下所有jar和class文件。  

&emsp; 三个类加载器的联系：除了BootStrap ClassLoader之外的另外两个默认加载器都是继承自java.lang.ClassLoader。BootStrap ClassLoader不是一个普通的Java类，它底层由C++编写，已嵌入到了JVM的内核当中，当JVM启动后，BootStrap ClassLoader也随之启动，负责加载完核心类库后，并构造Extension ClassLoader和App ClassLoader类加载器。  


&emsp; 双亲委派模型中，类加载器之间的父子关系一般不会以继承（Inheritance）的关系来实现，而是都使用组合（Composition）关系来复用父加载器的代码的。  
&emsp; 如下为ClassLoader中父加载器的定义：  

```java
public abstract class ClassLoader {

    // The parent class loader for delegation
    private final ClassLoader parent;

}
```

---------
<!-- 
https://mp.weixin.qq.com/s/_BtYDuMachG5YY6giOEMAg
-->
&emsp; 自定义类加载的目的是想要手动控制类的加载，那除了通过自定义的类加载器来手动加载类这种方式，还有其他的方式么?  
&emsp; 利用现成的类加载器进行加载  

&emsp; 利用URLClassLoader进行加载  

## 1.2. tomcat类加载器
&emsp; 参考[tomcat类加载器](/docs/webContainer/tomcat/tomcatClassLoader.md)  


## 1.3. 类加载器的加载机制  
&emsp; 类加载器之间的层级关系如上图所示。这种层次关系被称作双亲委派模型。  

### 1.3.1. 双亲委派模型  
<!-- 
首先，通过委派的方式，可以避免类的重复加载，当父加载器已经加载过某一个类时，子加载器就不会再重新加载这个类。
另外，通过双亲委派的方式，还保证了安全性。因为Bootstrap ClassLoader在加载的时候，只会加载JAVA_HOME中的jar包里面的类，如java.lang.Integer，那么这个类是不会被随意替换的，除非有人跑到你的机器上， 破坏你的JDK。
-->


&emsp; 如果一个类加载器收到了加载类的请求，它会先把请求委托给上层加载器去完成，上层加载器又会委托上上层加载器，一直到最顶层的类加载器；如果上层加载器无法完成类的加载工作时，当前类加载器才会尝试自己去加载这个类。如果都没加载到，则会抛出ClassNotFoundException异常。例子：父加载器已经加载了JDK中的String.class文件，所以不能定义同名的 String.java文件。  
&emsp; **<font color = "red">一句话概述：一个类加载器首先将类加载请求转发到父类加载器，只有当父类加载器无法完成时才尝试自己加载。</font>**  
&emsp; java.lang.ClassLoader源码分析：  

```java
public Class<?> loadClass(String name)throws ClassNotFoundException {
    return loadClass(name, false);
}

protected synchronized Class<?> loadClass(String name, boolean resolve)throws ClassNotFoundException {
    // 首先判断该类型是否已经被加载
    Class c = findLoadedClass(name);
    if (c == null) {
        //如果没有被加载，就委托给父类加载或者委派给启动类加载器加载
        try {
            if (parent != null) {
                //如果存在父类加载器，就委派给父类加载器加载
                c = parent.loadClass(name, false);
            } else {
                //如果不存在父类加载器，就检查是否是由启动类加载器加载的类，通过调用本地方法native Class findBootstrapClass(String name)
                c = findBootstrapClass0(name);
            }
        } catch (ClassNotFoundException e) {
            // 如果父类加载器和启动类加载器都不能完成加载任务，才调用自身的加载功能
            c = findClass(name);
        }
    }
    if (resolve) {
        resolveClass(c);
    }
    return c;
}
```

&emsp;代码不难理解，主要就是以下几个步骤：  
1. 先检查类是否已经被加载过   
2. 若没有加载则调用父加载器的loadClass()方法进行加载   
3. 若父加载器为空则默认使用启动类加载器作为父加载器。  
4. 如果父类加载失败，抛出ClassNotFoundException异常后，再调用自己的findClass()方法进行加载。  

&emsp; <font color = "red">双亲委派模型的好处：</font>  

* <font color = "clime">避免类的重复加载。</font> JVM中区分不同类，不仅仅是根据类名，相同的class文件被不同的ClassLoader加载就属于两个不同的类(比如，Java中的Object类，无论哪一个类加载器要加载这个类，最终都是委派给处于模型最顶端的启动类加载器进行加载，如果不采用双亲委派模型，由各个类加载器自己去加载的话，系统中会存在多种不同的Object类)。  
* <font color = "clime">防止核心API被随意篡改，</font>避免用户自己编写的类动态替换Java的一些核心类，比如自定义类：java.lang.String。  

### 1.3.2. ~~破坏双亲委派模型~~  
<!-- 

Java历史上有三次破坏双亲委派模型，是哪三次？ 
https://mp.weixin.qq.com/s/zZmsi7lpuQHECOHA2ohOvA

三次 jdk 破坏双亲委派模型
https://www.jianshu.com/p/166c5360a40b

双亲委派机制的破坏不是什么稀奇的事情，很多框架、容器等都会破坏这种机制来实现某些功能。
第一种被破坏的情况是在双亲委派出现之前。
由于双亲委派模型是在JDK1.2之后才被引入的，而在这之前已经有用户自定义类加载器在用了。所以，这些是没有遵守双亲委派原则的。
第二种，是JNDI、JDBC等需要加载SPI接口实现类的情况。
第三种是为了实现热插拔热部署工具。为了让代码动态生效而无需重启，实现方式时把模块连同类加载器一起换掉就实现了代码的热替换。
第四种时tomcat等web容器的出现。
第五种时OSGI、Jigsaw等模块化技术的应用。

-->
&emsp; 双亲委派模型并不是一个强制性的约束模型，而是Java设计者推荐给开发者的类加载器实现方式，可以“被破坏”。  

#### 1.3.2.1. 破坏示例

##### 1.3.2.1.1. JDK破坏
<!-- 
https://mp.weixin.qq.com/s/_BtYDuMachG5YY6giOEMAg
-->

* `双亲委派模型有一个问题：顶层ClassLoader无法加载底层ClassLoader的类，典型例子JNDI、JDBC。`
    *  **<font color = "clime">JDBC是启动类加载器加载，但 mysql 驱动是应用类加载器。所以加入了线程上下文类加载器(Thread Context ClassLoader)，</font>** 可以通过Thread.setContextClassLoaser()设置该类加载器，然后顶层ClassLoader再使用Thread.getContextClassLoader()获得底层的ClassLoader进行加载。  
    * ~~JNDI是Java标准服务，它的代码由启动类加载器去加载。但是JNDI需要回调独立厂商实现的代码，而类加载器无法识别这些回调代码（SPI）。~~   
    &emsp; ~~为了解决这个问题，引入了一个线程上下文类加载器。 可通过Thread.setContextClassLoader()设置。~~   
    &emsp; ~~利用线程上下文类加载器去加载所需要的SPI代码，即父类加载器请求子类加载器去完成类加载的过程，而破坏了双亲委派模型。~~  

##### 1.3.2.1.2. 热替换

* **利用破坏双亲委派来实现代码热替换(每次修改类文件，不需要重启服务)。因为一个Class只能被一个ClassLoader加载一次，否则会报java.lang.LinkageError。当要实现代码热部署时，可以每次都new一个自定义的ClassLoader来加载新的Class文件。** JSP的实现动态修改就是使用此特性实现。  

##### 1.3.2.1.3. Tomcat
* Tomcat中使用了自定义ClassLoader，并且也破坏了双亲委托机制。每个应用使用WebAppClassloader进行单独加载，它首先使用WebAppClassloader进行类加载，如果加载不了再委托父加载器去加载， **<font color = "red">这样可以保证每个应用中的类不冲突。每个tomcat中可以部署多个项目，每个项目中存在很多相同的class文件(很多相同的jar包)，加载到jvm中可以做到互不干扰。</font>**  

----------------

&emsp; Tomcat为什么要破坏双亲委派模型？  
&emsp; 因为一个Tomcat可以部署N个web应用，但是每个web应用都有自己的classloader，互不干扰。比如web1里面有com.test.A.class，web2里面也有com.test.A.class，如果没打破双亲委派模型的话，那么web1加载完后，web2再加载的话会冲突。  


##### 1.3.2.1.4. Spring
* Spring破坏双亲委派模型  
&emsp; Spring要对用户程序进行组织和管理，而用户程序一般放在WEB-INF目录下，由WebAppClassLoader类加载器加载，而Spring由Common类加载器或Shared类加载器加载。   
&emsp; 那么Spring是如何访问WEB-INF下的用户程序呢？   
&emsp; 使用线程上下文类加载器。 Spring加载类所用的classLoader都是通过Thread.currentThread().getContextClassLoader()获取的。当线程创建时会默认创建一个AppClassLoader类加载器（对应Tomcat中的WebAppclassLoader类加载器）：setContextClassLoader(AppClassLoader)。   
&emsp; 利用这个来加载用户程序。即任何一个线程都可通过getContextClassLoader()获取到WebAppclassLoader。  

#### 1.3.2.2. 小结：两种破坏方式
1. 继承ClassLoader，重写loadClass()方法。  
2. `使用线程上下文类加载器(Thread Context ClassLoader)`  


### 1.3.3. 自己写的java.lang.String可以让jvm加载到吗？  
<!-- 

https://zhuanlan.zhihu.com/p/311494771
-->

## 1.4. 类加载器应用  
### 1.4.1. 自定义类加载器  
<!-- 
*** 编码：https://www.jianshu.com/p/22d9c0a2d2e9
-->

&emsp; <font color = "red">什么情况下需要自定义类加载器？</font>  
1. **隔离加载类。**在某些框架内进行中间件与应用的模块隔离，把类加载到不同的环境。
2. **修改类加载方式。**类的加载模型并非强制，除了Bootstrap以外，其他的加载并非一定要引入，或者根据实际情况在某个时间点进行按需进行动态加载。
3. **扩展加载源。**比如从数据库、网络，甚至电视机机顶盒进行加载。
4. **防止源码泄露。**Java代码容易被编译和篡改，可以进行编译加密。那么类加载器也需要自定义，还原加密的字节码。

&emsp; java.lang.ClassLoader 的 loadClass() 实现了双亲委派模型的逻辑，自定义类加载器一般不去重写它，但是需要重写 findClass() 方法。  

        遵守双亲委派模型 继承ClassLoader，重写findClass()方法。
        破坏双亲委派模型 继承ClassLoader，重写loadClass()方法。

```java
public class CustomClassLoader extends ClassLoader {

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            byte[] result = getClassFromCustomPath(name);
            if (result == null) {
                throw new FileNotFoundException();
            } else {
                return defineClass(name, result, 0, result.length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new ClassNotFoundException(name);
    }

    private byte[] getClassFromCustomPath(String name) {
        // TODO 从自定义路径中加载指定类
        return null;
    }
}
```

### 1.4.2. 查看Boostrap ClassLoader 加载的类库  

```java
public static void main(String[] args) {
    URL[] urls = sun.misc.Launcher.getBootstrapClassPath().getURLs();
    for (URL url : urls) {
        System.out.println(url.toExternalForm());
    }
}
```
&emsp; 执行结果：  

```java
file:/C:/Program%20Files/Java/jdk1.8.0_131/jre/lib/resources.jar
file:/C:/Program%20Files/Java/jdk1.8.0_131/jre/lib/rt.jar
file:/C:/Program%20Files/Java/jdk1.8.0_131/jre/lib/sunrsasign.jar
file:/C:/Program%20Files/Java/jdk1.8.0_131/jre/lib/jsse.jar
file:/C:/Program%20Files/Java/jdk1.8.0_131/jre/lib/jce.jar
file:/C:/Program%20Files/Java/jdk1.8.0_131/jre/lib/charsets.jar
file:/C:/Program%20Files/Java/jdk1.8.0_131/jre/lib/jfr.jar
file:/C:/Program%20Files/Java/jdk1.8.0_131/jre/classes
```

### 1.4.3. 如何在启动时观察加载了哪个jar包中的哪个类？  
&emsp; 使用-XX:+TraceClassLoading参数，可以在启动时观察加载了哪个jar包中的哪个类。此参数在解决类冲突时特别实用。因为不同JVM环境对于加载类的顺序并非是一致的。  
&emsp; 部分示例：  

```java
[Opened C:\Program Files\Java\jdk1.8.0_131\jre\lib\rt.jar]
[Loaded java.lang.Object from C:\Program Files\Java\jdk1.8.0_131\jre\lib\rt.jar]
[Loaded java.io.Serializable from C:\Program Files\Java\jdk1.8.0_131\jre\lib\rt.jar]
[Loaded java.lang.Comparable from C:\Program Files\Java\jdk1.8.0_131\jre\lib\rt.jar]
[Loaded java.lang.CharSequence from C:\Program Files\Java\jdk1.8.0_131\jre\lib\rt.jar]
[Loaded java.lang.String from C:\Program Files\Java\jdk1.8.0_131\jre\lib\rt.jar]
[Loaded java.lang.reflect.AnnotatedElement from C:\Program Files\Java\jdk1.8.0_131\jre\lib\rt.jar]
[Loaded java.lang.reflect.GenericDeclaration from C:\Program Files\Java\jdk1.8.0_131\jre\lib\rt.jar]
[Loaded java.lang.reflect.Type from C:\Program Files\Java\jdk1.8.0_131\jre\lib\rt.jar]
[Loaded java.lang.Class from C:\Program Files\Java\jdk1.8.0_131\jre\lib\rt.jar]
[Loaded java.lang.Cloneable from C:\Program Files\Java\jdk1.8.0_131\jre\lib\rt.jar]
[Loaded java.lang.ClassLoader from C:\Program Files\Java\jdk1.8.0_131\jre\lib\rt.jar]
[Loaded java.lang.System from C:\Program Files\Java\jdk1.8.0_131\jre\lib\rt.jar]
[Loaded java.lang.Throwable from C:\Program Files\Java\jdk1.8.0_131\jre\lib\rt.jar]
......
```

### 1.4.4. 观察特定类的加载上下文  
&emsp; 由于加载的类数量众多，调试时很难捕捉到指定类的加载过程，这时可以使用条件断点功能。拿HashMap的加载过程为例，在ClassLoader#loadClass()处打个条件断点，效果如下：  
![image](http://182.92.69.8:8081/img/java/JVM/JVM-48.png)  


## 1.5. 类加载错误
&emsp; 在日常开发工作中，经常会遇到类冲突的情况，明明 classpath 下面的类有这个方法，但是一旦跑线上环境就出错，比如NoSuchMethodError、NoClassDefFoundError、NoClassDefFoundError 等。可以使用 jvm 参数 -verbose:class 方便地定位该问题，使用该参数可以快速地定位某个类是从哪个jar包加载的，而不是一味地埋头苦干，求百度，找Google。下面是使用 -verbose:class jvm 参数的部分日志输出  

    [Loaded org.springframework.context.annotation.CommonAnnotationBeanPostProcessor from file:/D:/tomcat/webapps/touch/WEB-INF/lib/spring-context-4.3.7.RELEASE.jar]
    [Loaded com.alibaba.dubbo.rpc.InvokerListener from file:/D:/tomcat/webapps/touch/WEB-INF/lib/dubbo-2.5.3.jar]
    [Loaded com.alibaba.dubbo.common.bytecode.Wrapper from file:/D:/tomcat/webapps/touch/WEB-INF/lib/dubbo-2.5.3.jar]


## 1.6. java.lang.ClassLoader  
&emsp; java.lang.ClassLoader中很重要的三个方法：loadClass方法、findClass方法、defineClass方法。  

### 1.6.1. loadClass方法  

```java
public Class<?> loadClass(String name) throws ClassNotFoundException {
    return loadClass(name, false);
}
protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException{
    //使用了同步锁，保证不出现重复加载
    synchronized (getClassLoadingLock(name)) {
        // 首先检查自己是否已经加载过
        Class<?> c = findLoadedClass(name);
        //没找到
        if (c == null) {
            long t0 = System.nanoTime();
            try {
                //有父类
                if (parent != null) {
                    //让父类去加载
                    c = parent.loadClass(name, false);
                } else {
                    //如果没有父类，则委托给启动加载器去加载
                    c = findBootstrapClassOrNull(name);
                }
            } catch (ClassNotFoundException e) {
                // ClassNotFoundException thrown if class not found
                // from the non-null parent class loader
            }

            if (c == null) {
                // If still not found, then invoke findClass in order
                // to find the class.
                long t1 = System.nanoTime();
                // 如果都没有找到，则通过自定义实现的findClass去查找并加载
                c = findClass(name);

                // this is the defining class loader; record the stats
                sun.misc.PerfCounter.getParentDelegationTime().addTime(t1 - t0);
                sun.misc.PerfCounter.getFindClassTime().addElapsedTimeFrom(t1);
                sun.misc.PerfCounter.getFindClasses().increment();
            }
        }
        //是否需要在加载时进行解析
        if (resolve) {
            resolveClass(c);
        }
        return c;
    }
}
```

&emsp; 正如loadClass方法所展示的，当类加载请求到来时，先从缓存中查找该类对象，如果存在直接返回，如果不存在则交给该类加载去的父加载器去加载，倘若没有父加载则交给顶级启动类加载器去加载，最后倘若仍没有找到，则使用findClass()方法去加载（关于findClass()稍后会进一步介绍）。从loadClass实现也可以知道如果不想重新定义加载类的规则，也没有复杂的逻辑，只想在运行时加载自己指定的类，那么我们可以直接使用this.getClass().getClassLoder.loadClass("className")，这样就可以直接调用ClassLoader的loadClass方法获取到class对象。  


### 1.6.2. findClass方法  

```java
protected Class<?> findClass(String name) throws ClassNotFoundException {
            throw new ClassNotFoundException(name);
}
```

&emsp; 在JDK1.2之前，在自定义类加载时，总会去继承ClassLoader类并重写loadClass方法，从而实现自定义的类加载类，但是在JDK1.2之后已不再建议用户去覆盖loadClass()方法，而是建议把自定义的类加载逻辑写在findClass()方法中，从前面的分析可知，findClass()方法是在loadClass()方法中被调用的，当loadClass()方法中父加载器加载失败后，则会调用自己的findClass()方法来完成类加载，这样就可以保证自定义的类加载器也符合双亲委托模式。  

&emsp; 需要注意的是ClassLoader类中并没有实现findClass()方法的具体代码逻辑，取而代之的是抛出ClassNotFoundException异常，同时应该知道的是findClass方法通常是和defineClass方法一起使用的。  


### 1.6.3. defineClass方法

```java
protected final Class<?> defineClass(String name, byte[] b, int off, int len,
                                ProtectionDomain protectionDomain) throws ClassFormatError{
    protectionDomain = preDefineClass(name, protectionDomain);
    String source = defineClassSourceLocation(protectionDomain);
    Class<?> c = defineClass1(name, b, off, len, protectionDomain, source);
    postDefineClass(c, protectionDomain);
    return c;
}
```

&emsp; defineClass()方法是用来将byte字节流解析成JVM能够识别的Class对象。通过这个方法不仅能够通过class文件实例化class对象，也可以通过其他方式实例化class对象，如通过网络接收一个类的字节码，然后转换为byte字节流创建对应的Class对象 。  


### 1.6.4. 小结  
&emsp; 用户根据需求自己定义的。需要继承自ClassLoader，重写方法findClass()。

&emsp; 如果想要编写自己的类加载器，只需要两步：

* 继承ClassLoader类
* 覆盖findClass(String className)方法

&emsp; ClassLoader超类的loadClass方法用于将类的加载操作委托给其父类加载器去进行，只有当该类尚未加载并且父类加载器也无法加载该类时，才调用findClass方法。  

&emsp; 如果要实现该方法，必须做到以下几点：  
1. 为来自本地文件系统或者其他来源的类加载其字节码。
2. 调用ClassLoader超类的defineClass方法，向虚拟机提供字节码。
