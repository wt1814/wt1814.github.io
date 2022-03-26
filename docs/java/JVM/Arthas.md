<!-- TOC -->

- [1. Arthas](#1-arthas)
    - [1.1. 简介](#11-简介)
    - [1.2. 下载并运行Arthas](#12-下载并运行arthas)
        - [1.2.1. 准备](#121-准备)
        - [1.2.2. 下载并运行Arthas](#122-下载并运行arthas)
        - [1.2.3. 访问 WebConsole](#123-访问-webconsole)
        - [1.2.4. Arthas命令介绍](#124-arthas命令介绍)
    - [1.3. 使用示例](#13-使用示例)
        - [1.3.1. 问题 1：这个类从哪个 jar 包加载的？为什么会报各种类相关的 Exception？](#131-问题-1这个类从哪个-jar-包加载的为什么会报各种类相关的-exception)
        - [1.3.2. 问题 2：我改的代码为什么没有执行到？难道是我没 commit？分支搞错了？](#132-问题-2我改的代码为什么没有执行到难道是我没-commit分支搞错了)
        - [1.3.3. 问题 3：遇到问题无法在线上debug，难道只能通过加日志再重新发布吗？](#133-问题-3遇到问题无法在线上debug难道只能通过加日志再重新发布吗)
        - [1.3.4. 问题 4：线上遇到某个用户的数据处理有问题，但线上同样无法 debug，线下无法重现！](#134-问题-4线上遇到某个用户的数据处理有问题但线上同样无法-debug线下无法重现)
        - [1.3.5. 问题 5：是否有一个全局视角来查看系统的运行状况？](#135-问题-5是否有一个全局视角来查看系统的运行状况)
        - [1.3.6. 问题 6：有什么办法可以监控到JVM的实时运行状态？](#136-问题-6有什么办法可以监控到jvm的实时运行状态)
        - [1.3.7. 问题 7：怎么快速定位应用的热点，生成火焰图？](#137-问题-7怎么快速定位应用的热点生成火焰图)
        - [1.3.8. 问题 8：怎样直接从JVM内查找某个类的实例？](#138-问题-8怎样直接从jvm内查找某个类的实例)

<!-- /TOC -->

# 1. Arthas
<!-- 

Java线上问题排查神器Arthas快速上手与原理浅谈 
https://mp.weixin.qq.com/s/s3qtr5hpB7Q3-Tu60R8gxA

-->

<!-- 
~~

***  带着8个问题5分钟教你学会Arthas诊断工具 
https://mp.weixin.qq.com/s/aUy7-90cxOukuFSiwEeA4A
还在为 Arthas 命令头疼？ 来看看这个插件吧！ 
https://mp.weixin.qq.com/s/OZT1wIfmzSa5TiMIbmZ5aQ
-->

&emsp; 官方文档：https://arthas.aliyun.com/zh-cn/  

## 1.1. 简介
&emsp; Arthas(阿尔萨斯)是Alibaba开源的Java诊断工具，深受开发者喜爱。  
&emsp; 当你遇到以下类似问题而束手无策时，Arthas可以解决：  
1. **<font color = "clime">这个类从哪个 jar 包加载的？为什么会报各种类相关的 Exception？</font>**  
2. 我改的代码为什么没有执行到？难道是我没 commit？分支搞错了？  
3. **<font color = "clime">遇到问题无法在线上 debug，难道只能通过加日志再重新发布吗？ </font>** 
4. 线上遇到某个用户的数据处理有问题，但线上同样无法 debug，线下无法重现！  
5. 是否有一个全局视角来查看系统的运行状况？  
6. 有什么办法可以监控到JVM的实时运行状态？  
7. **<font color = "clime">怎么快速定位应用的热点，生成火焰图？</font>**   
8. 怎样直接从JVM内查找某个类的实例？  

&emsp; Arthas支持JDK 6+，支持Linux/Mac/Windows，采用命令行交互模式，同时提供丰富的Tab自动补全功能，进一步方便进行问题的定位和诊断。  


## 1.2. 下载并运行Arthas
### 1.2.1. 准备
&emsp; 测试代码  

```java
import com.alibaba.fastjson.JSON;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ArthasDemo {
    public static void main(String[] args) {
        String s = "[{\"name\":\"zhangsan\",\"age\":\"10\",\"telephone\":\"123456\",\"interests\":[\"sing\",\"dance\",\"rap\"]},\n" +
                "{\"name\":\"lisi\",\"age\":\"20\",\"telephone\":\"123457\",\"interests\":[\"sing\",\"swim\"]},\n" +
                "{\"name\":\"wangwu\",\"age\":\"30\",\"telephone\":\"123458\",\"interests\":[\"sing\",\"program\"]}]";
        //模拟一遍遍的调用方法的过程
        for (; ; ) {
            System.out.println(new ArthasDemo().convert(s));
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private List<People> convert(String s) {
        return JSON.parseArray(s, People.class);
    }


    @Getter
    @Setter
    @ToString
    @FieldDefaults(level = AccessLevel.PRIVATE)
    private static class People {
        /**
         * 姓名
         */
        String name;
        /**
         * 年龄
         */
        String age;
        /**
         * 电话
         */
        String telephone;
        /**
         * 兴趣列表
         */
        List<String> interests;
    }
}
```

&emsp; 打印结果  

```java
/Library/Java/JavaVirtualMachines/jdk1.8.0_192.jdk/Contents/Home/bin/java ...
[ArthasDemo.People(name=zhangsan, age=10, telephone=123456, interests=[sing, dance, rap]), ArthasDemo.People(name=lisi, age=20, telephone=123457, interests=[sing, swim]), ArthasDemo.People(name=wangwu, age=30, telephone=123458, interests=[sing, program])]
[ArthasDemo.People(name=zhangsan, age=10, telephone=123456, interests=[sing, dance, rap]), ArthasDemo.People(name=lisi, age=20, telephone=123457, interests=[sing, swim]), ArthasDemo.People(name=wangwu, age=30, telephone=123458, interests=[sing, program])]
```

### 1.2.2. 下载并运行Arthas  
&emsp; 前提：有一个正在运行的java应用。  
![image](http://www.wt1814.com/static/view/images/java/JVM/JVM-146.png)  

1. wget(下载) https://alibaba.github.io/arthas/arthas-boot.jar
2. 启动：java -jar arthas-boot.jar

&emsp; ⚠️注意：如果java应用运行在docker容器中，进入docker容器，下载并运行Arthas。详情参见官方文档 https://arthas.aliyun.com/doc/docker.html#dockerjava  
  
<!-- 
https://blog.csdn.net/qq_39218530/article/details/117301113
-->

### 1.2.3. 访问 WebConsole
&emsp; attach 成功后可以打开谷歌浏览器输入http://127.0.0.1:3658/ 打开 WebConsole  
&emsp; （吐槽一句 Mac OS 的 Safari 浏览器不支持）  

    使用 WebConsole 最方便的是可以打开多个标签页同时操作

### 1.2.4. Arthas命令介绍
<!-- 
https://blog.csdn.net/qq_39218530/article/details/117301113
-->

&emsp; 官方文档：https://arthas.aliyun.com/doc/commands.html  
![image](http://www.wt1814.com/static/view/images/java/JVM/JVM-160.png)  

**1、基础命令**  
&emsp; help——查看命令帮助信息  
&emsp; cat——打印文件内容，和linux里的cat命令类似  
&emsp; echo–打印参数，和linux里的echo命令类似  
&emsp; grep——匹配查找，和linux里的grep命令类似  
&emsp; base64——base64编码转换，和linux里的base64命令类似  
&emsp; tee——复制标准输入到标准输出和指定的文件，和linux里的tee命令类似  
&emsp; pwd——返回当前的工作目录，和linux命令类似  
&emsp; cls——清空当前屏幕区域  
&emsp; session——查看当前会话的信息  
&emsp; reset——重置增强类，将被 Arthas 增强过的类全部还原，Arthas 服务端关闭时会重置所有增强过的类  
&emsp; version——输出当前目标 Java 进程所加载的 Arthas 版本号  
&emsp; history——打印命令历史  
&emsp; quit——退出当前 Arthas 客户端，其他 Arthas 客户端不受影响  
&emsp; stop——关闭 Arthas 服务端，所有 Arthas 客户端全部退出  
&emsp; keymap——Arthas快捷键列表及自定义快捷键  

**2、jvm 相关命令**  
&emsp; dashboard——当前系统的实时数据面板  
&emsp; thread——查看当前 JVM 的线程堆栈信息  
&emsp; jvm——查看当前 JVM 的信息  
&emsp; sysprop——查看和修改JVM的系统属性  
&emsp; sysenv——查看JVM的环境变量  
&emsp; vmoption——查看和修改JVM里诊断相关的option  
&emsp; perfcounter——查看当前 JVM 的Perf Counter信息  
&emsp; logger——查看和修改logger  
&emsp; getstatic——查看类的静态属性  
&emsp; ognl——执行ognl表达式  
&emsp; mbean——查看 Mbean 的信息  
&emsp; heapdump——dump java heap, 类似jmap命令的heap dump功能  
&emsp; vmtool——从jvm里查询对象，执行forceGc  

**3、class/classloader相关命令**  
&emsp; sc——查看JVM已加载的类信息  
&emsp; sm——查看已加载类的方法信息  
&emsp; jad——反编译指定已加载类的源码  
&emsp; mc——内存编译器，内存编译.java文件为.class文件  
&emsp; retransform——加载外部的.class文件，retransform到JVM里  
&emsp; redefine——加载外部的.class文件，redefine到JVM里  
&emsp; dump——dump 已加载类的 byte code 到特定目录  
&emsp; classloader——查看classloader的继承树，urls，类加载信息，使用classloader去getResource  

**4、monitor/watch/trace相关命令**  
&emsp; monitor——方法执行监控  
&emsp; watch——方法执行数据观测  
&emsp; trace——方法内部调用路径，并输出方法路径上的每个节点上耗时   
&emsp; stack——输出当前方法被调用的调用路径  
&emsp; tt——方法执行数据的时空隧道，记录下指定方法每次调用的入参和返回信息，并能对这些不同的时间下调用进行观测  

## 1.3. 使用示例
### 1.3.1. 问题 1：这个类从哪个 jar 包加载的？为什么会报各种类相关的 Exception？  
&emsp; 这个问题我经常在处理各种「依赖冲突」的时候遇到，有一些类的完全名称是一模一样，通过常规的办法无法解决类具体从哪个 jar 包加载。  
&emsp; 别急，看我下面的解决办法。  

1. sc  
&emsp; 通过 sc 命令 模糊查看当前 JVM 中是否加载了包含关键字的类，以及获取其完全名称。

    注意使用 sc -d 命令，获取 classLoaderHash，这个值在后面需要用到。

```text
sc -d *ArthasDemo*
```

![image](http://www.wt1814.com/static/view/images/java/JVM/JVM-147.png)  

2. classloader  
&emsp; 通过 classloader 查看 class 文件来自哪个 jar 包  
&emsp; 使用 cls 命令可以清空命令行，这个简单的命令官方文档居然找不到。。。
&emsp; 注意 classloader -c 后面的值填上面第一步中获取到的 Hash 值，class 文件路径使用'/'分割，且必须以.class 结尾。

```text
[arthas@3633]$ classloader -c 18b4aac2 -r com/shockang/study/ArthasDemo.class
file:/Users/shockang/code/concurrentbook/target/classes/com/shockang/study/ArthasDemo.class
Affect(row-cnt:1) cost in 0 ms.
```

&emsp; 上面是显示 class 文件路径的，如果 class 文件来自 jar 包，可以显示 jar 包路径，例如官方文档给的例子：  

```text
$ classloader -c 1b6d3586 -r java/lang/String.class
jar:file:/Library/Java/JavaVirtualMachines/jdk1.8.0_60.jdk/Contents/Home/jre/lib/rt.jar!/java/lang/String.class
```

### 1.3.2. 问题 2：我改的代码为什么没有执行到？难道是我没 commit？分支搞错了？
&emsp; 推荐使用 watch 和 tt 命令，非常好用。  

&emsp; 这两个命令都是用来查看方法调用过程的，不同的是 watch 命令是调用一次打印一次方法的调用情况，而 tt 命令可以先生成一个不断增加的调用列表，然后指定其中某一项进行观测。  

1. 使用 watch 命令查看方法调用情况。我们要查看 ArthasDemo 这个类里面的 convert 方法调用情况。
![image](http://www.wt1814.com/static/view/images/java/JVM/JVM-148.png)  

```text
watch com.shockang.study.ArthasDemo convert "{params,target,returnObj}" -f -x 4
```

&emsp; watch 后面跟上完全类名和方法名，以及一个 OGNL 的表达式，-f 表示不论正常返回还是异常返回都进行观察，-x 表示输出结果的属性遍历深度，默认为 1，  

    建议无脑写 4 就行，这是笔者经验来看最大的遍历深度，再大就不支持了

2. 使用 tt 命令来观测方法调用情况，tt 命令可以查看「多次调用」并选择其中一个进行观测，但是如果输出结果是多层嵌套就没办法看了，而 watch 可以查看「多层嵌套」的结果。  

    使用 tt -t 记录下当前方法的每次调用环境现场

![image](http://www.wt1814.com/static/view/images/java/JVM/JVM-149.png)  

```text
tt -t com.shockang.study.ArthasDemo convert
```

&emsp; TIMESTAMP表示方法调用发生的时间，COST 表示调用耗时（ms），IS-RET表示是否正常返回，IS-EXP 表示是否异常返回，OBJECT 表示对象的 HASH 值  

    对于具体一个时间片的信息而言，你可以通过 -i 参数后边跟着对应的 INDEX 编号查看到他的详细信息

![image](http://www.wt1814.com/static/view/images/java/JVM/JVM-150.png)  

    图中之所以可以打印兴趣列表，是调用了其 toString 方法，如果没有重写 java.lang.Object 类的 toString 方法，只会看到 hash 值。

3. 如何判断代码是否已经提交？  
&emsp; 通过 jad --source-only 可以查看源代码。

```text
[arthas@3633]$ jad --source-only com.shockang.study.ArthasDemo
       /*
        * Decompiled with CFR.
        */
       package com.shockang.study;

       import com.alibaba.fastjson.JSON;
       import java.util.List;
       import java.util.concurrent.TimeUnit;

       public class ArthasDemo {
           public static void main(String[] args) {
/*15*/         String s = "[{\"name\":\"zhangsan\",\"age\":\"10\",\"telephone\":\"123456\",\"interests\":[\"sing\",\"dance\",\"rap\"]},\n{\"name\":\"lisi\",\"age\":\"20
\",\"telephone\":\"123457\",\"interests\":[\"sing\",\"swim\"]},\n{\"name\":\"wangwu\",\"age\":\"30\",\"telephone\":\"123458\",\"interests\":[\"sing\",\"program\"]}]";
               while (true) {
/*20*/             System.out.println(new ArthasDemo().convert(s));
                   try {
/*22*/                 TimeUnit.SECONDS.sleep(10L);
/*25*/                 continue;
                   }
                   catch (InterruptedException e) {
/*24*/                 e.printStackTrace();
                       continue;
                   }
                   break;
               }
           }

           private List<People> convert(String s) {
/*30*/         return JSON.parseArray(s, People.class);
           }

           private static class People {
               private String name;
               private String age;
               private String telephone;
               private List<String> interests;

               private People() {
               }

               public String toString() {
                   return "ArthasDemo.People(name=" + this.getName() + ", age=" + this.getAge() + ", telephone=" + this.getTelephone() + ", interests=" + this.getIntere
sts() + ")";
               }

               public String getName() {
                   return this.name;
               }

               public void setName(String name) {
                   this.name = name;
               }

               public String getAge() {
                   return this.age;
               }

               public String getTelephone() {
                   return this.telephone;
               }

               public List<String> getInterests() {
                   return this.interests;
               }

               public void setAge(String age) {
                   this.age = age;
               }

               public void setTelephone(String telephone) {
                   this.telephone = telephone;
               }

               public void setInterests(List<String> interests) {
                   this.interests = interests;
               }
           }
       }

[arthas@3633]$
```

### 1.3.3. 问题 3：遇到问题无法在线上debug，难道只能通过加日志再重新发布吗？
&emsp; 通过上面问题 2 的 watch 和 tt 命令可以查看方法调用情况。  
&emsp; 此外，可以通过 redefine 命令「热替换」线上的代码，注意应用重启之后会失效，这在某些紧急情况下会有奇效。  
&emsp; 比如说我们修改一下方法体里面的代码，加了一行日志打印：  

```java
private List<People> convert(String s) {
    System.out.println(s);
    return JSON.parseArray(s, People.class);
}
```

&emsp; 这时我们就可以将新代码编译后的 class 文件热替换正在运行的 ArthasDemo 的代码。  
![image](http://www.wt1814.com/static/view/images/java/JVM/JVM-151.png)  
![image](http://www.wt1814.com/static/view/images/java/JVM/JVM-152.png)  
<center>热替换 JVM 内存中（方法区）加载的类</center>
&emsp; 从这张图可以明显的看出，明明源码中没有打印字符串 s 的逻辑，但是控制台还是打印了字符串，因为我们已经热替换了 JVM 内存中（方法区）加载的类。  

### 1.3.4. 问题 4：线上遇到某个用户的数据处理有问题，但线上同样无法 debug，线下无法重现！  
&emsp; 这个问题没有完美的解决办法  
&emsp; 参考一下问题 2 和问题 3的解决方案  
&emsp; 推荐使用 tt 命令并将命令行返回结果输出到一个文件中，后续可以选择异常的一行记录使用 tt -i 命令进行深入的分析。  
&emsp; tee指令会从标准输入设备读取数据，将其内容输出到标准输出设备，同时保存成文件。  

![image](http://www.wt1814.com/static/view/images/java/JVM/JVM-153.png)  

```text
tt -t com.shockang.study.ArthasDemo convert | tee /Users/shockang/Downloads/log
```

&emsp; 此外还可以使用 monitor 命令统计方法调用成功失败情况。  
![image](http://www.wt1814.com/static/view/images/java/JVM/JVM-154.png)  

```text
monitor -c 30 com.shockang.study.ArthasDemo convert | tee /Users/shockang/Downloads/log1
```

```text
-c 后面接统计周期，默认值为120秒
```

### 1.3.5. 问题 5：是否有一个全局视角来查看系统的运行状况？

&emsp; 使用 dashboard 命令可以查看当前系统的实时数据面板， 当运行在Ali-tomcat时，会显示当前tomcat的实时信息，如HTTP请求的qps, rt, 错误数, 线程池信息等等。  
![image](http://www.wt1814.com/static/view/images/java/JVM/JVM-155.png)  
<center>dashboard实时数据面板</center>

&emsp; 从图中可以看到线程情况，内存使用情况，系统参数等。  

### 1.3.6. 问题 6：有什么办法可以监控到JVM的实时运行状态？
&emsp; 使用 jvm 命令可以查看 JVM 的实时运行状态。   
![image](http://www.wt1814.com/static/view/images/java/JVM/JVM-156.png)  
<center>JVM 的实时运行状态</center>

### 1.3.7. 问题 7：怎么快速定位应用的热点，生成火焰图？
&emsp; profiler 命令支持生成应用热点的火焰图。本质上是通过不断的采样，然后把收集到的采样结果生成火焰图。

    默认情况下，生成的是 cpu 的火焰图，即 event 是 cpu，可以用--event 参数来指定。注意不同系统支持的 event 不同

![image](http://www.wt1814.com/static/view/images/java/JVM/JVM-157.png)  
&emsp; 默认情况下，arthas使用3658端口，则可以打开：http://localhost:3658/arthas-output/ 查看到arthas-output目录下面的profiler结果：  
![image](http://www.wt1814.com/static/view/images/java/JVM/JVM-158.png)  
<center>profiler目录</center>

&emsp; 选择一项点击  
![image](http://www.wt1814.com/static/view/images/java/JVM/JVM-159.png)  
<center>profiler结果图</center>  

### 1.3.8. 问题 8：怎样直接从JVM内查找某个类的实例？
&emsp; 使用 vmtool 可以达成目的  

    这个功能是 Arthas 3.5.1 新增的。可以参考官方文档 https://arthas.aliyun.com/doc/vmtool.html#id1

```text
$ vmtool --action getInstances --className java.lang.String --limit 10
@String[][
    @String[com/taobao/arthas/core/shell/session/Session],
    @String[com.taobao.arthas.core.shell.session.Session],
    @String[com/taobao/arthas/core/shell/session/Session],
    @String[com/taobao/arthas/core/shell/session/Session],
    @String[com/taobao/arthas/core/shell/session/Session.class],
    @String[com/taobao/arthas/core/shell/session/Session.class],
    @String[com/taobao/arthas/core/shell/session/Session.class],
    @String[com/],
    @String[java/util/concurrent/ConcurrentHashMap$ValueIterator],
    @String[java/util/concurrent/locks/LockSupport],
]
```

&emsp; 通过 --limit参数，可以限制返回值数量，避免获取超大数据时对JVM造成压力。默认值是10。  
&emsp; 如果想精确的定位到具体的类实例，可以通过指定 classloader name 或者 classloader hash，如下所示：  

```text
vmtool --action getInstances --classLoaderClass org.springframework.boot.loader.LaunchedURLClassLoader --className org.springframework.context.ApplicationContext
```

```text
vmtool --action getInstances -c 19469ea2 --className org.springframework.context.ApplicationContext
```

    获取 classloader hash 的方法请参考上面的问题 1

&emsp; vmtool 还有个不错的功能，可以「强制进行GC」，这在某些生产环境内存紧张的情况下有奇效。  

```text
vmtool --action forceGc
```
