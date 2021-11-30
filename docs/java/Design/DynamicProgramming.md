


# 动态代理与动态编译
<!-- 

https://www.programminghunter.com/article/61611120787/
-->

## 动态编程
&emsp; 动态编程是相对于静态编程而言，平时我们大多讨论的都是静态编程，java便是一种静态编程语言，它 的类型检查是在编译期间完成的。而动态编程是绕过了编译期间，在运行时完成类型检查。java有如下方法实现动态编程：动态代理，动态编译。

## 动态代理
&emsp; [JDK动态代理](/docs/java/Design/DynamicProxy.md)   

## 动态编译
&emsp; 动态编译就是利用字节码修改技术，来操作java字节码在运行期间jvm中动态生成新类或者对已有类进行修改。动态编译时在java 6开始支持的，主要是通过一个JavaCompiler接口来完成的。可以解决需要动态插入代码的场景，比如动态代理的实现，实现AOP编程。  
&emsp; 操作java字节码的工具有两个比较流行，一个是ASM，一个是Javassit。  

* ASM：直接操作字节码指令，执行效率高，要是使用者掌握Java类字节码文件格式及指令，对使用者的要求比较高。
* Javassit 提供了更高级的API，执行效率相对较差，但无需掌握字节码指令的知识，对使用者要求较低。

&emsp; 应用层面来讲一般使用建议优先选择Javassit，如果后续发现Javassit 成为了整个应用的效率瓶颈的话可以再考虑ASM.当然如果开发的是一个基础类库，或者基础平台，还是直接使用ASM吧，相信从事这方面工作的开发者能力应该比较高。

### javassit使用：
&emsp; Java字节码以二进制的形式存储在 .class 文件中，每一个 .class 文件包含一个 Java 类或接口。Javaassist 就是一个用来 处理 Java 字节码的类库。它可以在一个已经编译好的类中添加新的方法，或者是修改已有的方法，并且不需要对字节码方面有深入的了解。同时也可以去生成一个新的类对象，通过完全手动的方式。  

&emsp; 首先需要引入jar包：  

```xml
<dependency>
  <groupId>org.javassist</groupId>
  <artifactId>javassist</artifactId>
  <version>3.25.0-GA</version>
</dependency>
```

&emsp; Javassist中最为重要的是ClassPool，CtClass ，CtMethod 以及 CtField这几个类。  
&emsp; ClassPool：一个基于HashMap实现的CtClass对象容器，其中键是类名称，值是表示该类的CtClass对象。默认的ClassPool使用与底层JVM相同的类路径，因此在某些情况下，可能需要向ClassPool添加类路径或类字节。  
&emsp; CtClass：表示一个类，这些CtClass对象可以从ClassPool获得。  
&emsp; CtMethods：表示类中的方法。  
&emsp; CtFields ：表示类中的字段。  


