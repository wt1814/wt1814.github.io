
<!-- 
https://mp.weixin.qq.com/s/PFlZXXZU-zu_prCg5g4V0Q
jvm全套 链接: https://pan.baidu.com/s/1PcaER6cNiDt6teawLOwpgQ 提取码: c5fh

-->


1. **<font color = "clime">加载：</font>**    
    1. 类加载过程。  
    2. 类加载器：  
        1. 类加载器分类  
        2. 双亲委派模型（避免类的重复加载、防止核心API被随意篡改）  
2. **<font color = "clime">内存结构：</font>**   

3. **<font color = "clime">GC：</font>**    
    1. 堆中对象：  
        1. 存活标准：引用计数法、根可达性分析法  
        2. 四种引用
        3. 对象生存还是死亡？null与GC
    2. GC算法：  
        1. GC算法
        2. Young GC与Full GC
        3. 垃圾回收器  
4. **<font color = "clime">JVM调优：</font>**   
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
