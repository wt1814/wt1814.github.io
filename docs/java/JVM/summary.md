<!-- 
Java类的生命周期
https://mp.weixin.qq.com/s/t2XP9s4HOu3LrE_g7G1VWA

从JVM设计者的角度来看.class文件结构，一文弄懂.class文件的身份地位 
https://mp.weixin.qq.com/s/BVoqaDn7HUNtdB5vZa0zug
 手把手教你 javap 反编译分解代码，授人以鱼不如授人以渔 
https://mp.weixin.qq.com/s/o0MFrSVS7fYLDo6UnfmA-A

虚拟机字节码执行引擎，JVM的马达图，是爱情呀 
https://mp.weixin.qq.com/s/d_AqDygFgKbdHTfz--Dy-A

JVM超神之路：年后跳槽需要的JVM知识点，周末给你整理了一份！！！ 
https://mp.weixin.qq.com/s/-ceS7--mpJyk6ILr7EtMXg
学习JVM是如何从入门到放弃的？
https://zhuanlan.zhihu.com/p/39536807


-->

&emsp; **<font color = "lime">加载：</font>**    
1. 类加载过程。  
2. 类加载器：  
    1. 类加载器分类  
    2. 双亲委派模型（避免类的重复加载、防止核心API被随意篡改）  

&emsp; **<font color = "lime">内存结构：</font>**   

&emsp; **<font color = "lime">GC：</font>**    
1. 堆中对象：  
    1. 存活标准：引用计数法、根可达性分析法  
    2. 四种引用
    3. 对象生存还是死亡？null与GC
2. GC算法：  
    1. GC算法
    2. Young GC与Full GC
    3. 垃圾回收器  

&emsp; **<font color = "lime">JVM调优：</font>**   
1. JVM参数：-Xms-Xmx-Xmn  
2. JVM命令行调优工具：  
    * Jps：虚拟机进程状况工具  
    * Jstack：java线程堆栈跟踪工具  
    * Jmap：java内存映像工具  
    * Jhat：虚拟机堆转储快照分析工具  
    * Jstat：虚拟机统计信息监视工具  
    * Jinfo：java配置信息工具  
2. JVM调优：  
    * CPU飚高  
    * 内存溢出

-------------
<!-- 
https://mp.weixin.qq.com/s/PFlZXXZU-zu_prCg5g4V0Q
jvm全套 链接: https://pan.baidu.com/s/1PcaER6cNiDt6teawLOwpgQ 提取码: c5fh
JVM学习目录
https://www.cnblogs.com/ding-dang/p/13129619.html
-->

[JDK、JRE、JVM](/docs/java/JVM/JDK、JRE、JVM.md)   
[字节码文件](/docs/java/JVM/Class.md)  
[JVM类的加载](/docs/java/JVM/classLoad.md)  
[JVM内存结构](/docs/java/JVM/JVMMemory.md)  
[GC垃圾回收](/docs/java/JVM/GC.md)  
[JVM调优-基础](/docs/java/JVM/TuningBasic.md)  
[JVM调优](/docs/java/JVM/JVM调优.md)  
&emsp; [JVM排查案例](/docs/java/JVM/case.md)  
[JAVA线上故障排查](/docs/Linux/problem.md)  
[Arthas](/docs/java/JVM/Arthas.md)  
[JMH](/docs/java/JVM/JMH.md)  


