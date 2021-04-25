<!-- TOC -->

- [1. JDK、JRE、JVM](#1-jdkjrejvm)
    - [1.1. JDK](#11-jdk)
    - [1.2. JRE](#12-jre)
    - [1.3. JVM](#13-jvm)

<!-- /TOC -->


&emsp; **<font color = "red">总结：</font>**  
1. <font color = "red">JVM由4大部分组成：类加载器ClassLoader，运行时数据区Runtime Data Area，执行引擎Execution Engine，本地方法调用Native Interface。</font>  
2. **<font color = "clime">JVM各组件的作用(JVM执行程序的过程)：</font>**   
    1. 首先通过类加载器(ClassLoader)会把Java代码转换成字节码；  
    2. 运行时数据区(Runtime Data Area)再把字节码加载到内存中；  
    2. <font color = "red">而字节码文件只是JVM的一套指令集规范，并不能直接交给底层操作系统去执行，因此需要特定的命令解析器执行引擎(Execution Engine)，将字节码翻译成底层系统指令，再交由CPU去执行；</font>  
    3. 而这个过程中需要调用其他语言的本地库接口(Native Interface)来实现整个程序的功能。  


# 1. JDK、JRE、JVM  
<!-- 
JDK、JRE、JVM，是什么关系？
https://mp.weixin.qq.com/s/-CbNU5uPH1cpMuZ-eQQgFw
-->
## 1.1. JDK  
&emsp; JDK(Java SE Development Kit)：是整个JAVA的核心，Java标准开发包。它提供了编译、运行Java程序所需的各种工具和资源，包括Java编译器即JDK中的javac.exe、Java运行环境JRE，以及Java基础的类库(即Java API，包括rt.jar)。  
&emsp; 下图是JDK的安装目录：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-1.png)  

* bin：一堆EXE可执行文件，java.exe、javac.exe、javadoc.exe，以及密钥管理工具等。  
* db：内置了Derby数据库，体积小，免安装。  
* include：Java和JVM交互的头文件，例如JVMTI写的C++工程时，就需要把这个include包引入进去jvmti.h。例如：基于jvmti设计非入侵监控  
* jre：Java运行环境，包含了运行时需要的可执行文件，以及运行时需要依赖的Java类库和动态链接库.so .dll .dylib  
* lib：Java类库，例如dt.jar、tools.jar  

&emsp; JDK是JRE的超集，JDK包含了JRE所有的开发、调试以及监视应用程序的工具。以及如下重要的组件：  

* java – 运行工具，运行 .class 的字节码
* **javac– 编译器，将后缀名为.java的源代码编译成后缀名为.class的字节码**
* **javap – 反编译程序**
* javadoc – 文档生成器，从源码注释中提取文档，注释需符合规范
* jar – 打包工具，将相关的类文件打包成一个文件
* jdb – debugger，调试工具
* jps – 显示当前java程序运行的进程状态
* appletviewer – 运行和调试applet程序的工具，不需要使用浏览器
* javah – 从Java类生成C头文件和C源文件。这些文件提供了连接胶合，使 Java 和 C 代码可进行交互。
* javaws – 运行 JNLP 程序
* extcheck – 一个检测jar包冲突的工具
* apt – 注释处理工具
* jhat – java堆分析工具
* jstack – 栈跟踪程序
* jstat – JVM检测统计工具
* jstatd – jstat守护进程
* jinfo – 获取正在运行或崩溃的java程序配置信息
* jmap – 获取java进程内存映射信息
* idlj – IDL-to-Java 编译器. 将IDL语言转化为java文件
* policytool – 一个GUI的策略文件创建和管理工具
* jrunscript – 命令行脚本运行
* appletviewer：小程序浏览器，一种执行HTML文件上的Java小程序的Java浏览器

&emsp; Java程序从源代码到运行一般有下面3步：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-2.png)  
&emsp; Java源文件，通过编译器，能够生产相应的.Class文件，也就是字节码文件；而字节码文件又通过Java虚拟机中的解释器，编译成特定机器上的机器码。  

## 1.2. JRE  
&emsp; JRE：Java运行时环境，如果仅仅是用来部署和运行Java程序，只需要安装jre。它主要包含两个部分，jvm的标准实现和Java的一些基本类库。  
&emsp; 下图是JRE的安装目录：里面有两个文件夹bin和lib，在这里可以认为bin里的就是jvm，lib中则是jvm工作所需要的类库，而jvm和lib合起来就称为jre。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-3.png)  
&emsp; 如果安装了JDK，会发现电脑中有两套JRE，一套位于/Java/jre.../下，一套位于/Java/jdk.../jre下。一台机器上有两套以上JRE，java.exe选择合适的JRE来运行java程序。java.exe按照以下的顺序来选择JRE，找到JVM的绝对路径：  
1. 自己目录下有没有JRE；  
2. 父目录下有没有JRE；  
3. 查询注册表：HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Runtime Environment\"当前JRE版本号"\JavaHome。  

## 1.3. JVM  
&emsp; JVM(Java Virtual Mechinal)：Java虚拟机，是JRE的一部分，是整个java实现跨平台的最核心的部分，是可运行java字节码文件.class的虚拟计算机。  
&emsp; **JVM标准结构：**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-4.png)  
&emsp; <font color = "red">JVM由4大部分组成：类加载器ClassLoader，运行时数据区Runtime Data Area，执行引擎Execution Engine，本地方法调用Native Interface。</font>  

<!-- 
类加载器(ClassLoader)，负责加载class文件，class文件在文件开头有特定的文件标示，并且ClassLoader只负责class文件的加载，至于它是否可以运行，则由Execution Engine决定。  
运行时数据区(Runtime Data Area)。  
执行引擎(Execution Engine)，也叫Interpreter。Class文件被加载后，会把指令和数据信息放入内存中，Execution Engine则负责把这些命令解释给操作系统。  
本地方法调用(Native Interface)，是负责调用本地接口的。作用是调用不同语言的接口给JAVA用，会在Native Method Stack中记录对应的本地方法，然后调用该方法时就通过Execution Engine加载对应的本地lib。原本多用于一些专业领域，如JAVA驱动，地图制作引擎等，现在关于这种本地方法接口的调用已经被类似于Socket通信，WebService等方式取代。  
-->

* 类加载器，在JVM启动时或者类运行时将需要的class加载到JVM中。  
* 运行时数据区，将内存划分成若干个区以模拟实际机器上的存储、记录和调度功能模块，如实际机器上的各种功能的寄存器或者 PC 指针的记录器等。  
* 执行引擎，执行引擎的任务是负责执行 class 文件中包含的字节码指令，相当于实际机器上的CPU。  
* 本地方法调用，调用C或C++实现的本地方法的代码返回结果。  
  

&emsp; **<font color = "clime">各组件的作用(JVM执行程序的过程)：</font>**  
1. 首先通过类加载器(ClassLoader)会把Java代码转换成字节码；  
2. 运行时数据区(Runtime Data Area)再把字节码加载到内存中；  
2. <font color = "red">而字节码文件只是JVM的一套指令集规范，并不能直接交给底层操作系统去执行，因此需要特定的命令解析器执行引擎(Execution Engine)，将字节码翻译成底层系统指令，再交由CPU去执行；</font>  
3. 而这个过程中需要调用其他语言的本地库接口(Native Interface)来实现整个程序的功能。  


&emsp; **JVM与不同系统(JVM的平台无关性)：** java能跨平台，实现一次编写，多处运行。Java能够跨平台运行的核心在于JVM 。不是Java能够跨平台，而是它的jvm能够跨平台。Java虚拟机屏蔽了与具体操作系统平台相关的信息，只要为不同平台实现了相应的虚拟机，编译后的Java字节码就可以在该平台上运行。  
