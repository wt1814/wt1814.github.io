<!-- TOC -->

- [1. 类加载的方式：类加载器](#1-类加载的方式类加载器)
    - [1.1. 类加载器的分类](#11-类加载器的分类)
    - [1.2. 类加载器的加载机制](#12-类加载器的加载机制)
        - [1.2.1. 双亲委派模型](#121-双亲委派模型)
        - [1.2.2. ~~破坏双亲委派模型~~](#122-破坏双亲委派模型)
    - [1.3. 类加载器应用](#13-类加载器应用)
        - [1.3.1. 自定义类加载器](#131-自定义类加载器)
        - [1.3.2. 查看Boostrap ClassLoader 加载的类库](#132-查看boostrap-classloader-加载的类库)
        - [1.3.3. 如何在启动时观察加载了哪个jar包中的哪个类？](#133-如何在启动时观察加载了哪个jar包中的哪个类)
        - [1.3.4. 观察特定类的加载上下文](#134-观察特定类的加载上下文)

<!-- /TOC -->



&emsp; **<font color = "red">总结：</font>**  
1. JVM默认提供三个类加载器：启动类加载器、扩展类加载器、应用类加载器。  
&emsp; 双亲委派模型中，类加载器之间的父子关系一般不会以继承（Inheritance）的关系来实现，而是都使用组合（Composition）关系来复用父加载器的代码的。      
2. 双亲委派模型，一个类加载器首先将类加载请求转发到父类加载器，只有当父类加载器无法完成时才尝试自己加载。  
&emsp; 好处：避免类的重复加载；防止核心API被随意篡改。   
3. 破坏双亲委派模型的案例：  
    1. **<font color = "clime">JDBC是启动类加载器加载，但 mysql 驱动是应用类加载器。所以加入了线程上下文类加载器(Thread Context ClassLoader)，</font>** 可以通过Thread.setContextClassLoaser()设置该类加载器，然后顶层ClassLoader再使用Thread.getContextClassLoader()获得底层的ClassLoader进行加载。  
    2. Tomcat中使用了自定义ClassLoader，使得一个Tomcat中可以加载多个应用。一个Tomcat可以部署N个web应用，但是每个web应用都有自己的classloader，互不干扰。比如web1里面有com.test.A.class，web2里面也有com.test.A.class，如果没打破双亲委派模型的话，那么web1加载完后，web2再加载的话会冲突。    
    3. ......  


# 1. 类加载的方式：类加载器 
<!-- 

JDK为何自己先破坏双亲委派模型? 
https://mp.weixin.qq.com/s/_BtYDuMachG5YY6giOEMAg

--> 
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-6.png)  

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


## 1.2. 类加载器的加载机制  
&emsp; 类加载器之间的层级关系如上图所示。这种层次关系被称作双亲委派模型。  

### 1.2.1. 双亲委派模型  
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

### 1.2.2. ~~破坏双亲委派模型~~  
<!-- 
为什么需要破坏双亲委派模型
https://mp.weixin.qq.com/s/2iGaiOpxBIM3msAZYPUOnQ
Java历史上有三次破坏双亲委派模型，是哪三次？ 
https://mp.weixin.qq.com/s/zZmsi7lpuQHECOHA2ohOvA
我竟然被“双亲委派”给虐了 
https://mp.weixin.qq.com/s/Q0MqcvbeI7gAcJH5ZaQWgA
-->
&emsp; 双亲委派模型并不是一个强制性的约束模型，而是Java设计者推荐给开发者的类加载器实现方式，可以“被破坏”。  

&emsp; **<font color = "red">破坏双亲委派模型的案例：</font>**  

* 双亲委派模型有一个问题：顶层ClassLoader无法加载底层ClassLoader的类，典型例子JNDI、JDBC。**<font color = "clime">JDBC是启动类加载器加载，但 mysql 驱动是应用类加载器。所以加入了线程上下文类加载器(Thread Context ClassLoader)，</font>** 可以通过Thread.setContextClassLoaser()设置该类加载器，然后顶层ClassLoader再使用Thread.getContextClassLoader()获得底层的ClassLoader进行加载。  
* **利用破坏双亲委派来实现代码热替换(每次修改类文件，不需要重启服务)。因为一个Class只能被一个ClassLoader加载一次，否则会报java.lang.LinkageError。当要实现代码热部署时，可以每次都new一个自定义的ClassLoader来加载新的Class文件。** JSP的实现动态修改就是使用此特性实现。  
* Tomcat中使用了自定义ClassLoader，并且也破坏了双亲委托机制。每个应用使用WebAppClassloader进行单独加载，它首先使用WebAppClassloader进行类加载，如果加载不了再委托父加载器去加载， **<font color = "red">这样可以保证每个应用中的类不冲突。每个tomcat中可以部署多个项目，每个项目中存在很多相同的class文件(很多相同的jar包)，加载到jvm中可以做到互不干扰。</font>**  

----------------

&emsp; Tomcat为什么要破坏双亲委派模型？  
&emsp; 因为一个Tomcat可以部署N个web应用，但是每个web应用都有自己的classloader，互不干扰。比如web1里面有com.test.A.class，web2里面也有com.test.A.class，如果没打破双亲委派模型的话，那么web1加载完后，web2再加载的话会冲突。  


## 1.3. 类加载器应用  
### 1.3.1. 自定义类加载器  
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

### 1.3.2. 查看Boostrap ClassLoader 加载的类库  

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

### 1.3.3. 如何在启动时观察加载了哪个jar包中的哪个类？  
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

### 1.3.4. 观察特定类的加载上下文  
&emsp; 由于加载的类数量众多，调试时很难捕捉到指定类的加载过程，这时可以使用条件断点功能。拿HashMap的加载过程为例，在ClassLoader#loadClass()处打个条件断点，效果如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-48.png)  
