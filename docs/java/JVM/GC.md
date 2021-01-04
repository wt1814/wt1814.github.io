

<!-- TOC -->

- [1. GC](#1-gc)
    - [1.1. 堆中对象的存活](#11-堆中对象的存活)
        - [1.1.1. GC的存活标准](#111-gc的存活标准)
            - [1.1.1.1. 引用计数法](#1111-引用计数法)
            - [1.1.1.2. 根可达性分析法](#1112-根可达性分析法)
            - [1.1.1.3. 对象的四种引用状态，强弱软虚](#1113-对象的四种引用状态强弱软虚)
                - [1.1.1.3.1. 强引用](#11131-强引用)
                - [1.1.1.3.2. 软引用](#11132-软引用)
                - [1.1.1.3.3. 弱引用](#11133-弱引用)
                - [1.1.1.3.4. 虚引用](#11134-虚引用)
                - [1.1.1.3.5. 软引用和弱引用的使用](#11135-软引用和弱引用的使用)
        - [1.1.2. 对象生存还是死亡](#112-对象生存还是死亡)
            - [1.1.2.1. null与GC](#1121-null与gc)
    - [1.2. GC算法](#12-gc算法)
        - [1.2.1. 标记-清除（Mark-Sweep）算法](#121-标记-清除mark-sweep算法)
        - [1.2.2. 标记-复制（Copying）算法](#122-标记-复制copying算法)
        - [1.2.3. 标记-整理（Mark-Compact）算法](#123-标记-整理mark-compact算法)
        - [1.2.4. 分代收集理论](#124-分代收集理论)
            - [1.2.4.1. HotSpot GC](#1241-hotspot-gc)
            - [1.2.4.2. Stop the world](#1242-stop-the-world)
    - [1.3. 垃圾回收器](#13-垃圾回收器)
        - [1.3.1. 收集器分类](#131-收集器分类)
        - [1.3.2. 收集器详解](#132-收集器详解)
            - [1.3.2.1. 新生代收集器](#1321-新生代收集器)
                - [1.3.2.1.1. Serial收集器](#13211-serial收集器)
                - [1.3.2.1.2. ParNew收集器](#13212-parnew收集器)
                - [1.3.2.1.3. Parallel Scavenge收集器](#13213-parallel-scavenge收集器)
            - [1.3.2.2. 老年代收集器](#1322-老年代收集器)
                - [1.3.2.2.1. Serial Old收集器](#13221-serial-old收集器)
                - [1.3.2.2.2. Parallel Old收集器](#13222-parallel-old收集器)
                - [1.3.2.2.3. CMS收集器](#13223-cms收集器)
            - [1.3.2.3. G1收集器](#1323-g1收集器)
                - [1.3.2.3.1. G1的内存布局](#13231-g1的内存布局)
                - [1.3.2.3.2. G1运行流程](#13232-g1运行流程)
                - [1.3.2.3.3. G1优缺点](#13233-g1优缺点)
                - [收集过程](#收集过程)
                - [1.3.2.3.4. 使用G1](#13234-使用g1)
            - [1.3.2.4. 常用的收集器组合](#1324-常用的收集器组合)
            - [1.3.2.5. ZGC](#1325-zgc)
            - [1.3.2.6. Epsilon](#1326-epsilon)
            - [1.3.2.7. Shenandoah](#1327-shenandoah)
        - [1.3.3. 选择合适的垃圾收集器](#133-选择合适的垃圾收集器)
        - [1.3.4. ~~垃圾收集器常用参数~~](#134-垃圾收集器常用参数)
    - [1.4. 方法区(类和常量)回收/类的卸载阶段](#14-方法区类和常量回收类的卸载阶段)

<!-- /TOC -->


<!-- 
亿级流量系统如何玩转 JVM 
https://mp.weixin.qq.com/s/_uKSQjI--eT3n04Voel5_g
CMS GC
https://mp.weixin.qq.com/s/WqfzZRlk2NMkNc5a_Yjpdw

&emsp; 可以作为GC ROOT的对象包括：  
1. 栈中引用的对象
2. 静态变量、常量引用的对象
3. 本地方法栈native方法引用的对象
-->
<!-- 
~~

https://mp.weixin.qq.com/s/IH9p5X7WveKGx1ujfHzFag
深度揭秘垃圾回收底层，这次让你彻底弄懂她 
https://mp.weixin.qq.com/s?__biz=MzU4Mjk0MjkxNA==&mid=2247487815&idx=2&sn=c94666e98b4f3c6e46f4e9378f3f4e39&chksm=fdb1f8eacac671fc587323800a0421cdf015b59b10ac8086936f28cfa9f6921410ab7fbd28c4&scene=21#wechat_redirect
炸了！一口气问了我18个JVM问题！ 
https://mp.weixin.qq.com/s/WVGZIBXsIVYPMfhkqToh_Q
-->

![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-56.png)  

&emsp; **<font color = "lime">总结：</font>**  
1. 堆中对象：  
    1. 存活标准：引用计数法、根可达性分析法  
    2. 四种引用
    3. 对象生存还是死亡？null与GC
2. GC算法：  
    1. GC算法
    2. Young GC与Full GC
    3. 垃圾回收器  
    

# 1. GC  
&emsp; GC主要解决下面的三个问题：  

* 哪些内存需要回收？&emsp; GC回收的主要区域是堆、方法区。  
* 什么时候回收？  
* 如何回收？  

## 1.1. 堆中对象的存活
<!-- https://juejin.im/post/5e151b38f265da5d495c8025 --> 


### 1.1.1. GC的存活标准  
&emsp; 对于如何判断对象是否可以回收，有两种比较经典的判断策略：引用计数算法、可达性分析算法。  

#### 1.1.1.1. 引用计数法  
&emsp; 给每个对象添加一个计数器，当有地方引用该对象时计数器加1，当引用失效时计数器减1。用对象计数器是否为0来判断对象是否可被回收。  
&emsp; 缺点： **<font color = "red">无法解决循环引用的问题。</font>** 如果出现A引用了B，B又引用了A，这时候就算它们都不再使用了，但因为相互引用，计算器=1，永远无法被回收。Java中没有使用这种算法。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-16.png)  

#### 1.1.1.2. 根可达性分析法  
&emsp; 从GC Roots开始向下搜索，搜索所走过的路径称为引用链。当一个对象到GC Roots没有任何引用链相连时，则证明此对象是不可用的，那么虚拟机就判断是可回收对象。可达性分析可以解决循环引用的问题。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-17.png)  
&emsp; 可作为GC ROOTs的对象：  

* 虚拟机栈中引用的对象；  
* 方法区中，类静态属性(static)引用的对象；  
* 方法区中，常量(final static)引用的对象；  
* 本地方法栈中，JNI（即Native方法）引用的对象；  

#### 1.1.1.3. 对象的四种引用状态，强弱软虚    
&emsp; 无论是通过引用计数算法判断对象的引用数量，还是通过根搜索算法判断对象的引用链是否可达，判定对象是否存活都与“引用”有关。  
&emsp; 在JDK中提供了四个级别的引用：强引用，软引用，弱引用和虚引用。在这四个引用类型中，只有强引用Final Reference类是包内可见，其他三种引用类型均为public，可以在应用程序中直接使用。引用类型的类结构如图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-22.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-18.png)  
&emsp; <font color = "lime">Java 中引入四种引用的目的是让程序自己决定对象的生命周期，JVM 是通过垃圾回收器对这四种引用做不同的处理，来实现对象生命周期的改变。</font>  

<!-- 
Java设计这四种引用的主要目的有两个：
    可以让程序员通过代码的方式来决定某个对象的生命周期；
    有利用垃圾回收。
-->

##### 1.1.1.3.1. 强引用  
&emsp; 代码中普遍存在的类似"Object obj = new Object()"这类的引用，只要强引用还存在，垃圾收集器永远不会回收掉被引用的对象。  
&emsp; 强引用的特点： 
 
* 强引用可以直接访问目标对象。  
* 强引用所指向的对象在任何时候都不会被系统回收。JVM宁愿抛出OOM异常，也不会回收强引用所指向的对象。  
* 强引用可能导致内存泄漏。  

##### 1.1.1.3.2. 软引用  
&emsp; 软引用是除了强引用外，最强的引用类型。可以通过java.lang.ref.SoftReference使用软引用。一个持有软引用的对象，不会被JVM很快回收，JVM会根据当前堆的使用情况来判断何时回收。 **<font color = "red">当堆使用率临近阈值时，才会去回收软引用的对象。</font>** 因此，软引用可以用于实现对内存敏感的高速缓存。  
&emsp; 软引用示例：  
&emsp; 在IDE设置参数-Xmx2m -Xms2m规定堆内存大小为2m。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-19.png)  
&emsp; 运行结果：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-20.png)  
&emsp; 打开被注释掉的new byte[1024*100]语句，这条语句请求一块大的堆空间，使堆内存使用紧张。并显式的再调用一次GC，结果如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-21.png)说明在系统内存紧张的情况下，软引用被回收。  

##### 1.1.1.3.3. 弱引用  
&emsp; 弱引用是一种比软引用较弱的引用类型。在系统GC时， **<font color = "red">只要发现弱引用，不管系统堆空间是否足够，都会将对象进行回收。</font>** <font color = "lime">它可以作为简单的缓存表解决方案。</font>  
&emsp; 在java中，可以用java.lang.ref.WeakReference实例来保存对一个Java对象的弱引用。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-23.png)  
&emsp; 运行结果：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-24.png)  

##### 1.1.1.3.4. 虚引用  
&emsp; 虚引用是所有类型中最弱的一个。 **<font color = "red">一个持有虚引用的对象，和没有引用几乎是一样的，随时可能被垃圾回收器回收。</font>** 当试图通过虚引用的get()方法取得强引用时，总是会失败。并且，虚引用必须和引用队列一起使用，它的作用在于跟踪垃圾回收过程。  
&emsp; 当垃圾回收器准备回收一个对象时，如果发现它还有虚引用，就会在垃圾回收后，销毁这个对象，将这个虚引用加入引用队列。程序可以通过判断引用队列中是否已经加入了虚引用，来了解被引用的对象是否将要被垃圾回收。如果程序发现某个虚引用已经被加入到引用队列，那么就可以在所引用的对象的内存被回收之前采取必要的行动。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-25.png)  
&emsp; 运行结果：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-26.png)  
&emsp; 对虚引用的get()操作，总是返回null，因为sf.get()方法的实现如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-27.png)  

##### 1.1.1.3.5. 软引用和弱引用的使用  
&emsp; **<font color>软引用，弱引用都非常适合来保存那些可有可无的缓存数据，如果这么做，当系统内存不足时，这些缓存数据会被回收，不会导致内存溢出。而当内存资源充足时，这些缓存数据又可以存在相当长的时间，从而起到加速系统的作用。</font>**  

&emsp; 假如⼀个应⽤需要读取⼤量的本地图⽚，如果每次读取图⽚都从硬盘读取会严重影响性能，如果⼀次性全部加载到内存⼜可能造成内存溢出，这时可以⽤软引⽤解决这个问题。  
&emsp; 设计思路：⽤⼀个HashMap来保存图⽚路径和相应图⽚对象关联的软引⽤之间的映射关系，在内存不⾜时，JVM会⾃动共回收这些缓存图⽚对象所占的空间，避免OOM。  

```java
Map<String, SoftReference<Bitmap>> imageCache = new HashMap<>();  
```  

### 1.1.2. 对象生存还是死亡

<!-- 
&emsp; 即使在可达性分析算法中不可达的对象，也并非一定是“非死不可”的，这时候他们暂时处于“缓刑”阶段，真正宣告一个对象死亡至少要经历两次标记过程：  
&emsp; 1）如果对象在可达性分析算法中不可达，那么它会被第一次标记并进行一次刷选，刷选的条件是是否需要执行finalize()方法（当对象没有覆盖finalize()或者finalize()方法已经执行过了（对象的此方法只会执行一次）），虚拟机将这两种情况都会视为没有必要执行）。  
&emsp; 2）如果这个对象有必要执行finalize()方法会将其放入F-Queue队列中，稍后GC将对F-Queue队列进行第二次标记，如果在重写finalize()方法中将对象自己赋值给某个类变量或者对象的成员变量，那么第二次标记时候就会将它移出“即将回收”的集合。  
&emsp; finalize()能做的工作，使用try-finally或者其他方式都能做到更好，更及时，所以不建议使用此方法。  


在可达性分析中被判定为不可达的对象，并不是立即赴死，至少要经历两次标记过程：如果对象在进行可达性分析后发现没有与 GC Root 相连接的引用链，那么它将被第一次标记，随后再进行一次筛选，筛选条件是对象是否有必要执行 finalize() 方法，如果对象没有覆盖 finalize() 方法或是 finalize() 方法已经被调用过，则都视为“没有必要执行”

如果对象被判定为有必要执行 finalize() 方法，那么该对象将会被放置在一个名为 F-Queue 的队列之中，并在稍后由一条由虚拟机自动创建的、低调度优先级的 Finalizer 线程去执行它们的 finalize() 方法。注意这里所说的执行是指虚拟机会触发这个方法开始运行，但并不承诺一定会等待它运行结束。这样做的原因是防止某个对象的 finalize() 方法执行缓慢，或者发生死循环，导致 F-Queue 队列中的其他对象永久处于等待状态

finalize() 方法是对象逃脱死亡命运的最后一次机会，稍后收集器将对 F-Queue 中的对象进行第二次小规模标记，如果对象希望在 finalize() 方法中成功拯救自己，只要重新与引用链上的任何一个对象建立关联即可，那么在第二次标记时它将被移出“即将回收”的集合；如果对象这时候还没有逃脱，那基本上就真的要被回收了

任何一个对象的 finalize() 方法都只会被系统自动调用一次，如果对象面临下一次回收，它的 finalize() 方法将不会再执行。finalize() 方法运行代价高，不确定性大，无法保证各个对象的调用顺序，因此已被官方明确声明为不推荐使用的语法
-->
<!-- 
https://www.jianshu.com/p/0618241f9f44
-->

&emsp; <font color = "red">在可达性分析算法中，不可达的对象也不是一定会死亡的，它们暂时都处于“缓刑”阶段，要真正宣告一个对象“死亡”，至少要经历两次标记过程。</font>  

    finalize()⽅法什么时候被调⽤？析构函数(finalization)的⽬的是什么？
    垃圾回收器(garbage colector)决定回收某对象时，就会运⾏该对象的finalize()⽅法，但是在Java中很不幸，如果内存总是充⾜的，那么垃圾回收可能永远不会进⾏，也就是说filalize() 可能永远不被执⾏，显然指望它做收尾⼯作是靠不住的。 那么finalize()究竟是做什么的呢？ 它最主要的⽤途是回收特殊渠道申请的内存。Java程序有垃圾回收器，所以⼀般情况下内存问题不⽤程序员操⼼。但有⼀种JNI(Java Native Interface)调⽤non-Java程序（C或C++）， finalize()的⼯作就是回收这部分的内存。
 
1. <font color = "red">如果对象在进行可达性分析后没有与GC Root相连接的引用链，那么它会被第一次标记并进行一次筛选，</font><font color = "lime">筛选的条件是是否有必要执行finalize()。</font>即：当对象没有被覆盖finalize()或者finalize()方法已经被虚拟机调用过了，虚拟机将这两种情况都是为没有必要执行。  
2. <font color = "red">当一个对象被判断为有必要执行finalize()方法，那么这个对象会被放置到F-Queue队列中，</font><font color = "lime">并且稍后JVM自动建立一个低优先级的Finalizer线程执行它，这里“执行”是虚拟机会触发这个方法，但不会承诺等待它运行结束</font>（万一这个方法运行缓慢或者死循环，F-Queue队列其他对象岂不是永久等待）。<font color = "red">finalize()是对象逃脱死亡的最后一次机会。稍后GC会对F-Queue进行第二次小规模标记。</font>如果对象能在finalize()方法中重新与引用链上任何一个方法建立关联（例如把自己this关键字赋值给某个类变量或者对象的成员变量）。那么第二次标记时，将会移出即将回收的集合。否则，这个对象就会被回收了。  
&emsp; 注：任何对象的finalize()方法都只会被系统调用一次。  

<!--
&emsp; 步骤1：<font color = "red">判断有没有必要执行Object#finalize()方法</font>  
&emsp; **<font color = "lime">如果对象在进行可达性分析后发现没有与GC Roots相连接的引用链，那它将会被第一次标记并且进行一次筛选，</font>** 筛选的条件是此对象是否有必要执行finalize()方法。  
&emsp; 另外，有两种情况都视为“没有必要执行”：对象没有覆盖finaliza()方法、finalize()方法已经被虚拟机调用过。  

&emsp; 步骤2：<font color = "red">如何执行？</font>F-Queue的队列  
&emsp; 如果这个对象被判定为有必要执行finalize()方法，那么此对象将会放置在一个叫做 F-Queue 的队列中，并在稍后由一个虚拟机自动建立的、低优先级的Finalizer线程去执行它。  

&emsp; 步骤3：<font color = "red">执行死亡还是逃脱死亡？</font>  
&emsp; 首先，需要知道，finalize()方法是对象逃脱死亡命运的最后一次机会，**<font color = "lime">稍后GC将对F-Queue 队列中的对象进行第二次小规模的标记。</font>**  
&emsp; 逃脱死亡：对象想在finalize()方法中成功拯救自己，只要重新与引用链上的任何一个对象建立关联即可，例如把自己（this关键字）赋值给某个类变量或者对象的成员变量，这样在第二次标记时它将被移出“即将回收”的集合。  
&emsp; 执行死亡：对象没有执行逃脱死亡，那就是死亡了。  
&emsp; 注：任何对象的finalize()方法都只会被系统调用一次。  
-->


&emsp; 一次对象的自我拯救：  

```java
/*此代码演示了两点
 * 对象可以在GC时自我拯救
 * 这种自救只会有一次，因为一个对象的finalize方法只会被自动调用一次
 * */
public class FinalizeEscapeGC {
	public static FinalizeEscapeGC SAVE_HOOK=null;
	public void isAlive(){
		System.out.println("yes我还活着");
	}
	public void finalize() throws Throwable{
		super.finalize();
		System.out.println("执行finalize方法");
		FinalizeEscapeGC.SAVE_HOOK=this;//自救
	}
	public static void main(String[] args) throws InterruptedException{
		SAVE_HOOK=new FinalizeEscapeGC();
		//对象的第一次回收
		SAVE_HOOK=null;
		System.gc();
		//因为finalize方法的优先级很低所以暂停0.5秒等它
		Thread.sleep(500);
		if(SAVE_HOOK!=null){
			SAVE_HOOK.isAlive();
		}else{
			System.out.println("no我死了");
		}
		//下面的代码和上面的一样，但是这次自救却失败了
		//对象的第一次回收
		SAVE_HOOK=null;
		System.gc();
		Thread.sleep(500);
		if(SAVE_HOOK!=null){
			SAVE_HOOK.isAlive();
		}else{
			System.out.println("no我死了");
		}
	}
}
```

#### 1.1.2.1. null与GC  
<!-- 
https://www.codebye.com/jiang-dui-xiang-shu-xing-fu-wei-null-gc-hui-hui-shou-ma.html

https://www.polarxiong.com/archives/Java-%E5%AF%B9%E8%B1%A1%E4%B8%8D%E5%86%8D%E4%BD%BF%E7%94%A8%E6%97%B6%E8%B5%8B%E5%80%BC%E4%B8%BAnull%E7%9A%84%E4%BD%9C%E7%94%A8%E5%92%8C%E5%8E%9F%E7%90%86.html

https://www.cnblogs.com/ouhaitao/p/9996374.html

java方法中把对象置null,到底能不能加速垃圾回收
https://blog.csdn.net/dfdsggdgg/article/details/52463882?utm_medium=distribute.wap_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-1.nonecase&depth_1-utm_source=distribute.wap_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-1.nonecase

https://www.cnblogs.com/ouhaitao/p/9996374.html
https://www.cnblogs.com/raylee2007/p/4944465.html
https://www.cnblogs.com/christmad/p/13124907.html
https://blog.csdn.net/shudaqi2010/article/details/53811992

-->
&emsp; **<font color = "lime">手动将不用的对象引用置为null，可以使得JVM在下一次GC时释放这部分内存。</font>**  
&emsp; 对于占用空间比较大的对象（比如大数组），推荐在确认不再使用的时候将其值为null，jvm在回收大对象的时候不如小对象来的及时，置为null就能强制在下次GC的时候回收掉它。  

&emsp; 一个对象的引用执行null，并不会被立即回收，还需要执行finalize()方法（必须要重写这个方法，且只能执行一次）。可执行过程中，可能会重新变为可达对象。但是并不鼓励使用这个方法！  


-------
## 1.2. GC算法  
<!-- 
分代收集算法 
https://mp.weixin.qq.com/s/34hXeHqklAkV4Qu2X0lw3w
-->
&emsp; GC常用的算法：标记-清除（Mark-Sweep）、复制（Copying）、标记-整理（Mark-Compact）、分代收集（新生用复制，老年用标记-整理）。  

### 1.2.1. 标记-清除（Mark-Sweep）算法  
1. <font color = "red">标记-清除算法是最基础的收集算法，是因为后续的收集算法大多都是以标记-清除算法为基础，对其缺点进行改进而得到的。</font>  
2. 标记-清除算法分为两个阶段：标记阶段和清除阶段。标记阶段是标记出所有需要被回收的对象，清除阶段就是回收被标记的对象所占用的空间。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-73.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-76.png)  
3. 特点：  
    * 优点：  
        1. 算法相对简单
        2. 存活对象比较多的情况下效率比较高
    * 缺点：
        1. (效率偏低，两遍扫描，标记和清除都比较耗时)执行效率不稳定，如果Java堆中包含大量对象，而且其中大部分是需要被回收的，这时必须进行大量标记和清除的动作，导致标记和清除两个过程的执行效率都随对象数量增长而降低；  
        2. (位置不连续，产生碎片)<font color = "lime">内存空间的碎片化问题，</font>清除后产生大量不连续的内存碎片。如果有大对象会出现空间不够的现象，从而不得不提前触发另一次垃圾收集动作。 

### 1.2.2. 标记-复制（Copying）算法 
<!-- 
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-13.png)  
&emsp; 标记-复制算法的执行过程：  
 ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-53.png)  
-->
1. 标记-复制算法常被简称为复制算法。<font color = "red">为了解决标记-清除算法面对大量可回收对象时执行效率低的问题。</font>  
2. 标记-复制算法的执行过程：  
&emsp; 将可用内存按容量划分为大小相等的两块，每次只使用其中的一块。当这一块的内存用完了，就将还存活着的对象复制到另外一块上面，然后再把已使用过的内存空间一次清理掉。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-74.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-77.png)  
&emsp; <font color = "red">如果内存中多数对象都是存活的，这种算法将会产生大量的内存间复制的开销</font>，但对于多数对象都是可回收的情况，算法需要复制的就是占少数的存活对象，而且每次都是针对整个半区进行内存回收，分配内存时也就不用考虑有空间碎片的复杂情况，只要移动堆顶指针，按顺序分配即可。这样实现简单，运行高效，不过其缺陷也显而易见，这种复制回收算法的代价是<font color = "red">将可用内存缩小为了原来的一半</font>，空间浪费多了一点。 
3. 特点：  
    * 适用于存活对象较少的情况  
    * 优点：  
        1. 只扫描一次，效率提高。  
        2. 没有碎片，空间连续。
    * 缺点：  
        1. 移动复制对象，需要调整对象引用。  
        2. 50%的内存空间始终空闲浪费，存活对象越多效率越低。

### 1.2.3. 标记-整理（Mark-Compact）算法  
<!-- 
&emsp; 标记-整理算法是一种老年代的回收算法。  
&emsp; 标记-整理算法的工作过程如图：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-15.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-54.png)  
-->
1. 为了解决标记-复制算法的缺陷，充分利用内存空间，提出了标记-整理算法。标记-清除算法与标记-整理算法的本质差异在于前者是一种非移动式的回收算法，而后者是移动式的。该算法标记阶段和 Mark-Sweep 一样，但是在完成标记之后，它不是直接清理可回收对象，而是将存活对象都向一端移动，然后清理掉端边界以外的内存。  
2. 标记-整理算法的标记过程仍然与“标记-清除”算法一样，但后续步骤不是直接对可回收对象进行清理，而是让所有存活的对象都向内存空间一端移动，然后直接清理掉边界以外的内存。  
&emsp; 标记-整理算法的执行过程：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-75.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-78.png)  

3. 特点：  
    * 优点：
        1. 没有碎片，空间连续，方便对象分配。  
        2. 不会产生内存减半
    * 缺点：<font color = "red">扫描两次，，指针需要调整（移动对象），效率偏低。</font>  

### 1.2.4. 分代收集理论  
<!-- 
https://mp.weixin.qq.com/s/_0IANOvyP_UNezDm0bxXmg
https://mp.weixin.qq.com/s/WVGZIBXsIVYPMfhkqToh_Q

分代收集理论

当前商业虚拟机的垃圾收集器大多数都遵循了“分代收集”的设计理论，分代收集理论其实是一套符合大多数程序运行实际情况的经验法则，主要建立在两个分代假说之上：

    弱分代假说：绝大多数对象都是朝生夕灭的

    强分代假说：熬过越多次垃圾收集过程的对象就越难以消亡

这两个分代假说共同奠定了多款常用垃圾收集器的一致设计原则：收集器应该将 Java 堆划分出不同的区域，将回收对象依据年龄（即对象熬过垃圾收集过程的次数）分配到不同的区域之中存储，把存活时间短的对象集中在一起，每次回收只关注如何保留少量存活的对象，即新生代（Young Generation）；把难以消亡的对象集中在一起，虚拟机就可以使用较低的频率来回收这个区域，即老年代（Old Generation）

正因为划出了不同的区域，垃圾收集器才可以每次只回收其中一个或多个区域，因此才有了“Minor GC”、“Major GC”、“Full GC”这样的回收类型划分，也才能够针对不同的区域采用不同的垃圾收集算法，因而有了“标记-复制”算法、“标记-清除”算法、“标记-整理”算法

分代收集并非只是简单划分一下内存区域，它至少存在一个明显的困难：对象之间不是孤立的，对象之间会存在跨代引用。假如现在要进行只局限于新生代的垃圾收集，根据前面可达性分析的知识，与 GC Roots 之间不存在引用链即为可回收，但新生代的对象很有可能会被老年代所引用，那么老年代对象将临时加入 GC Roots 集合中，我们不得不再额外遍历整个老年代中的所有对象来确保可达性分析结果的正确性，这无疑为内存回收带来很大的性能负担。为了解决这个问题，就需要对分代收集理论添加第三条经验法则：

    跨代引用假说：跨代引用相对于同代引用仅占少数

存在互相引用的两个对象，应该是倾向于同时生存或同时消亡的，举个例子，如果某个新生代对象存在跨代引用，由于老年代对象难以消亡，会使得新生代对象同样在收集时得以存活，进而年龄增长后晋升到老年代，那么跨代引用也随之消除了。既然跨代引用只是少数，那么就没必要去扫描整个老年代，也不必专门记录每一个对象是否存在哪些跨代引用，只需在新生代上建立一个全局的数据结构，称为记忆集（Remembered Set），这个结构把老年代划分为若干个小块，标识出老年代的哪一块内存会存在跨代引用。此后当发生 Minor GC 时，只有包含了跨代引用的小块内存里的对象才会被加入 GC Roots 进行扫描
-->
&emsp; 新生代采用复制算法、老年代采用标记-整理算法。  
&emsp; 

&emsp; 部分垃圾回收器使用的模型  
* 除Epsilon ZGC Shenandoah之外的GC都是使用逻辑分代模型  
* G1是逻辑分代，物理不分代
* 除此之外不仅逻辑分代，而且物理分代  

&emsp; <font color = "lime">分代收集流程：</font>  

* 对象首先分配在伊甸园区域
* 新生代空间不足时，触发minor gc，伊甸园和from存活的对象使用copy复制到to中，存活的对象年龄加1并且交换from to
* minor gc 会引发stop the word，暂停其它用户线程，等垃圾回收结束，用户线程才恢复运行
* 当对象寿命超过阈值时，会晋升至老年代，最大寿命15（4bit）
* 当老年代空间不足，那么触发full gc，STW的时间更长  

#### 1.2.4.1. HotSpot GC  
&emsp; GC经常发生的区域是堆区，堆区还可以细分为新生代、老年代，新生代还分为一个Eden区和两个Survivor区。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-33.png)  

&emsp; 针对HotSpot VM的实现，它里面的GC其实准确分类只有两大种：  
* Partial GC：并不收集整个GC堆的模式  
    * Young GC：只收集young gen的GC  
    * Old GC：只收集old gen的GC。只有CMS的concurrent collection是这个模式  
    * Mixed GC：收集整个young gen以及部分old gen的GC。只有G1有这个模式  
* Full GC：收集整个堆，包括young gen、old gen、perm gen（如果存在的话）等所有部分的模式。  
&emsp; Major GC通常是跟full GC是等价的，收集整个GC堆。但因为HotSpot VM发展了这么多年，外界对各种名词的解读已经完全混乱了，当有人说“major GC”的时候一定要问清楚指的是上面的full GC还是old GC。

1. Yong GC  
&emsp; 当young gen中的eden区分配满的时候触发。注意young GC中有部分存活对象会晋升到old gen，所以young GC后old gen的占用量通常会有所升高。  
&emsp; 具体流程：  
&emsp; 大部分对象在Eden区中生成。当Eden占用完时，垃圾回收器进行回收。回收时先将eden区存活对象复制到一个survivor0区，然后清空eden区，当这个survivor0区也存放满了时，则将eden区和survivor0区（使用的survivor中的对象也可能失去引用）存活对象复制到另一个survivor1区，然后清空eden和这个survivor0区，此时survivor0区是空的，然后将survivor0区和survivor1区交换，即保持survivor1区为空， 如此往复。  
2. Full GC  
当准备要触发一次young GC时，如果发现统计数据说之前young GC的平均晋升大小比目前old gen剩余的空间大，则不会触发young GC而是转为触发full GC（因为HotSpot VM的GC里，除了CMS的concurrent collection之外，其它能收集old gen的GC都会同时收集整个GC堆，包括young gen，所以不需要事先触发一次单独的young GC）。  

&emsp; **<font color = "red">Full GC的触发还可能有其他情况：</font>**  
1. <font color = "red">调用System.gc()</font>  
&emsp; 只是建议虚拟机执行Full GC，但是虚拟机不一定真正去执行。不建议使用这种方式，而是让虚拟机管理内存。  
2. 老年代空间不足  
&emsp; 老年代空间不足的常见场景为大对象直接进入老年代、长期存活的对象进入老年代等。  
&emsp; 为了避免以上原因引起的Full GC，应当尽量不要创建过大的对象以及数组。除此之外，可以通过-Xmn虚拟机参数调大新生代的大小，让对象尽量在新生代被回收掉，不进入老年代。还可以通过 -XX:MaxTenuringThreshold调大对象进入老年代的年龄，让对象在新生代多存活一段时间。  
3. 空间分配担保失败  
&emsp; 使用复制算法的Minor GC需要老年代的内存空间作担保，如果担保失败会执行一次Full GC。  
4. JDK 1.7及以前的永久代空间不足  
&emsp; 为避免以上原因引起的Full GC，可采用的方法为增大永久代空间或转为使用CMS GC。  
5. Concurrent Mode Failure  
&emsp; 执行CMS GC的过程中同时有对象要放入老年代，而此时老年代空间不足（可能是GC过程中浮动垃圾过多导致暂时性的空间不足），便会报Concurrent Mode Failure错误，并触发Full GC。  

#### 1.2.4.2. Stop the world  
&emsp; Java中Stop-The-World机制简称STW，是在执行垃圾收集时，Java应用程序的其他所有线程都被挂起（除了垃圾收集帮助器之外）。Java中一种全局暂停现象，全局停顿，所有Java代码停止，native代码可以执行，但不能与JVM交互；这些现象多半是由于gc引起。  
&emsp; GC时的Stop the World(STW)是Java开发最大的敌人。但可能很多人还不清楚，除了GC，JVM下还会发生停顿现象。  
&emsp; JVM里有一条特殊的线程－－VM Threads，专门用来执行一些特殊的VM Operation，比如分派GC，thread dump等，这些任务都需要整个Heap，以及所有线程的状态是静止的，一致的才能进行。所以JVM引入了安全点(Safe Point)的概念，想办法在需要进行VM Operation时，通知所有的线程进入一个静止的安全点。  

&emsp; 除了GC，其他触发安全点的VM Operation包括：  
1. JIT相关，比如Code deoptimization, Flushing code cache ；  
2. Class redefinition (e.g. javaagent，AOP代码植入的产生的instrumentation) ；  
3. Biased lock revocation 取消偏向锁 ；  
4. Various debug operation (e.g. thread dump or deadlock check)；  

## 1.3. 垃圾回收器  
&emsp; 垃圾收集算法是内存回收的理论基础，而垃圾收集器就是内存回收的具体实现。   

### 1.3.1. 收集器分类  
&emsp; <font color = "lime">1. 根据收集器的指标分类（两个关键指标，停顿时间和吞吐量）：</font>  
&emsp; 收集器性能考虑因素：  

* 吞吐量：程序的运行时间（程序的运行时间＋内存回收的时间）。  
* 暂停时间：执行垃圾收集时，程序的工作线程被暂停的时间。  
* 内存占有（堆空间）： Java 堆区所占的内存大小。  
* 垃圾收集开销：吞吐量的补数，垃圾收集器所占时间与总时间的比例。  
* 收集频率：相对于应用程序的执行，收集操作发生的频率。  
* 快速： 一个对象从诞生到被回收所经历的时间。  

&emsp; <font color  = "red">其中内存占用、吞吐量和停顿时间，三者共同构成了一个“不可能三角”。</font>    

&emsp; **吞吐量和停顿时间：**  
&emsp; 停顿时间->垃圾收集器进行垃圾回收终端应用执行响应的时间  
&emsp; 吞吐量->运行用户代码时间/(运行用户代码时间+垃圾收集时间)  

&emsp; 停顿时间越短就越适合需要和用户交互的程序，良好的响应速度能提升用户体验；  
&emsp; 高吞吐量则可以高效地利用CPU时间，尽快完成程序的运算任务，主要适合在后台运算而不需要太多交互的任务。  


&emsp; <font color = "lime">2. 根据运行时，线程执行方式分类：</font>  

* 串行收集器->Serial和Serial Old  
&emsp; **<font color = "red">只能有一个垃圾回收线程执行，用户线程暂停。</font>** 适用于内存比较小的嵌入式设备 。  
* 并行收集器[吞吐量优先]->Parallel Scanvenge、Parallel Old  
&emsp; **<font color = "red">多条垃圾收集线程并行工作，但此时用户线程仍然处于等待状态。</font>** 适用于科学计算、后台处理等若交互场景 。  
* 并发收集器[停顿时间优先]->CMS、G1  
&emsp; **<font color = "red">用户线程和垃圾收集线程同时执行(但并不一定是并行的，可能是交替执行的)，垃圾收集线程在执行的时候不会停顿用户线程的运行。</font>** 适用于相对时间有要求的场景，比如Web 。  

### 1.3.2. 收集器详解
&emsp; HotSpot虚拟机所包含的所有收集器如图：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-55.png)  
&emsp; 上图展示了多种作用于不同分代的收集器。如果两个收集器之间存在连线，那说明它们可以搭配使用。虚拟机所处的区域说明它是属于新生代收集器还是老年代收集器。选择对具体应用最合适的收集器。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-67.png)  

* Serial 串行收集器  
    新生代收集器、最基本、发展历史最久（jdk1.3之前）、单线程、基于复制算法  
* Serial Old 串行老年代收集器  
    老年代版本的Serial收集器、单线程、基于标记-整理算法  
* ParNew 收集器  
    Serial的多线程版本、新生代收集器、多线程、基于复制算法、关注用户停顿时间  
* Parallel Scavenge 收集器  
    新生代收集器，基于复制算法，并行的多线程、关注吞吐量  
* Parallel Old收集器  
    Parallel Scavenge的老年代版本，使用多线程和“标记-整理”算法  
* CMS（Conturrent Mark Sweep）收集器  
   并发、基于标记-清除算法  
* G1（Garbage-First）收集器  
    并行与并发、分代收集、空间整合  
* Shenandoah  
    支持并发的整理算法、基于读写屏障、旋转指针  
* ZGC  
     支持并发收集、基于动态Region、染色指针、虚拟内存映射  
* Epsilon垃圾收集器  
     没有操作的垃圾收集器、处理内存分配但不实现任何实际内存回收机制的GC  

#### 1.3.2.1. 新生代收集器  
##### 1.3.2.1.1. Serial收集器  
&emsp; 最基本、发展历史最久的收集器，这个收集器是一个采用复制算法的单线程的收集器。  
&emsp; 迄今为止，Serial收集器依然是虚拟机运行在Client模式下的默认新生代收集器，因为它简单而高效。用户桌面应用场景中，分配给虚拟机管理的内存一般来说不会很大，收集几十兆甚至一两百兆的新生代停顿时间在几十毫秒最多一百毫秒，只要不是频繁发生，这点停顿是完全可以接受的。  
&emsp; 参数控制：
  
    -XX:+UseSerialGC 串行收集器
    
&emsp; Serial收集器运行过程如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-29.png)  
<!-- &emsp; 说明：1.需要STW（Stop The World），停顿时间长。2.简单高效，对于单个CPU环境而言，Serial收集器由于没有线程交互开销，可以获取最高的单线程收集效率。  -->

&emsp; **<font color = "red">一句话概括：Serial收集器，采用复制算法的单线程的收集器，运行在Client模式下的默认新生代收集器，适用于用户桌面应用中。</font>**

##### 1.3.2.1.2. ParNew收集器  
&emsp; （相同）ParNew收集器是Serial收集器的多线程版本，除了使用多条线程进行垃圾收集外，其余行为和Serial收集器完全一样，包括使用的也是复制算法。  
&emsp; （不同）ParNew收集器除了多线程以外和Serial收集器并没有太多创新的地方，但是它却是JDK7之前Server模式下的虚拟机首选的新生代收集器，其中有一个很重要的和性能无关的原因是，除了Serial收集器外，目前只有它能与CMS收集器配合工作。  
&emsp; （对比）ParNew收集器在单CPU的环境中绝对不会有比Serial收集器更好的效果，甚至由于线程交互的开销，该收集器在两个CPU的环境中都不能百分之百保证可以超越Serial收集器。当然，随着可用CPU数量的增加，它对于GC时系统资源的有效利用还是很有好处的。它默认开启的收集线程数与CPU数量相同，在CPU数量非常多的情况下，可以使用-XX:ParallelGCThreads参数来限制垃圾收集的线程数。  
&emsp; 参数控制：  

    -XX:+UseParNewGC ParNew收集器
    -XX:ParallelGCThreads 限制线程数量
    
&emsp; ParNew收集器运行过程如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-30.png)  

&emsp; **<font color = "red">一句话概括：Serial收集器的多线程版本，降低停顿时间，JDK7之前Server模式下的虚拟机首选的新生代收集器，能与CMS收集器配合。</font>**

##### 1.3.2.1.3. Parallel Scavenge收集器  
&emsp; Parallel Scavenge收集器也是一个新生代收集器，也是用复制算法的收集器，也是并行的多线程收集器。Parallel Scavenge收集器是虚拟机运行在Server模式下的默认垃圾收集器。   
&emsp; 它的特点是它的关注点和其他收集器不同。<font color = "lime">Parallel Scavenge收集器的目标则是达到一个可控制的吞吐量（吞吐量=运行用户代码时间/(运行用户代码时间+垃圾收集时间)））。</font> 高吞吐量可以最高效率地利用 CPU 时间，尽快地完成程序的运算任务，主要适用于在后台运算而不需要太多交互的任务。<font color = "red">自适应调节策略也是 ParallelScavenge 收集器与 ParNew 收集器的一个重要区别。</font>   
&emsp; 参数控制：    

        -XX:+UseParallelGC 使用Parallel收集器+ 老年代串行。  
        Parallel Scavenge收集器提供了两个参数用于精确控制吞吐量，分别是控制最大垃圾收集停顿时间的-XX：MaxGCPauseMillis参数以及直接设置吞吐量大小的-XX：GCTimeRatio参数。  
        Parallel Scavenge收集器还有一个参数-XX：+UseAdaptiveSizePolicy值得我们关注。这是一个开关参数，当这个参数被激活之后，就不需要人工指定新生代的大小（-Xmn）、Eden与Survivor区的比例（-XX：SurvivorRatio）、晋升老年代对象大小（-XX：PretenureSizeThreshold）等细节参数 了，虚拟机会根据当前系统的运行情况收集性能监控信息，动态调整这些参数以提供最合适的停顿时间或者最大的吞吐量。这种调节方式称为垃圾收集的自适应的调节策略（GC Ergonomics）。  

&emsp; **<font color = "red"> Parallel Scavenge收集器，也是采用复制算法的并行的多线程收集器，Server模式下的默认垃圾收集器</font>，<font color = "lime">目标是达到一个可控制的吞吐量。</font>**

#### 1.3.2.2. 老年代收集器  
##### 1.3.2.2.1. Serial Old收集器  
&emsp; Serial收集器的老年代版本，同样是一个单线程收集器，使用“标记-整理算法”，这个收集器的主要意义也是在于给Client模式下的虚拟机使用。 

##### 1.3.2.2.2. Parallel Old收集器  
&emsp; Parallel Scavenge收集器的老年代版本，使用多线程和“标记-整理”算法。这个收集器在JDK 1.6之后的出现，“吞吐量优先收集器”终于有了比较名副其实的应用组合，在注重吞吐量以及CPU资源敏感的场合，都可以优先考虑Parallel Scavenge收集器+Parallel Old收集器的组合。  
&emsp; 数控制：  

    -XX:+UseParallelOldGC 使用Parallel收集器+ 老年代并行

&emsp; 运行过程如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-31.png)  

##### 1.3.2.2.3. CMS收集器  
&emsp; CMS（Conrrurent Mark Sweep）收集器是以 **<font color = "lime">获取最短回收停顿时间为目标</font>** 的收集器。  
&emsp; 使用标记-清除算法，收集过程分为如下四步：  
1. 初始标记，标记GCRoots能直接关联到的对象，时间很短。  
2. 并发标记，进行GCRoots Tracing（可达性分析）过程，过程耗时较长但是不需要停顿用户线程，可以与垃圾收集线程一起并发运行。  
3. 重新标记，修正并发标记期间，因用户程序继续运作而导致标记产生变动的那一部分对象的标记记录，停顿时间通常会比初始标记阶段稍长一些，但也远比并发标记阶段的时间短。  
4. 并发清除，理删除掉标记阶段判断的已经死亡的对象，由于不需要移动存活对象，所以这个阶段也是可以与用户线程同时并发的。  

&emsp; **<font color = "red">由于在整个过程中耗时最长的并发标记和并发清除阶段中，垃圾收集器线程都可以与用户线程一起工作，所以从总体上来说，CMS收集器的内存回收过程是与用户线程一起并发执行的。</font>**  

&emsp; CMS是一款优秀的收集器，它最主要的优点在名字上已经体现出来： **<font color = "lime">并发收集、低停顿。</font>** 但是也有以下 **<font color = "red">三个明显的缺点：</font>**  

* **<font color = "lime">吞吐量低</font>**    
&emsp; <font color = "red">由于CMS在垃圾收集过程使用用户线程和GC线程并行执行，从而线程切换会有额外开销，</font>因此CPU吞吐量就不如在GC过程中停止一切用户线程的方式来的高。
* **<font color = "lime">无法处理浮动垃圾，导致频繁Full GC</font>**  
&emsp; <font color = "red">由于垃圾清除过程中，用户线程和GC线程并发执行，也就是用户线程仍在执行，那么在执行过程中会产生垃圾，这些垃圾称为"浮动垃圾"。</font>  
&emsp; 如果CMS在垃圾清理过程中，用户线程需要在老年代中分配内存时发现空间不足，就需再次发起Full GC，而此时CMS正在进行清除工作，因此此时只能由Serial Old临时对老年代进行一次Full GC。  
* **<font color = "lime">使用"标记-清除"算法，产生碎片空间</font>**  
&emsp; 由于CMS使用了"标记-清除"算法, 因此清除之后会产生大量的碎片空间，不利于空间利用率。不过CMS提供了应对策略：开启-XX:+UseCMSCompactAtFullCollection，开启该参数后，每次FullGC完成后都会进行一次内存压缩整理，将零散在各处的对象整理到一块儿。但每次都整理效率不高，因此提供了另外一个参数，设置参数-XX:CMSFullGCsBeforeCompaction，本参数告诉CMS，经过了N次Full GC过后再进行一次内存整理。  

&emsp; 参数控制：  

    -XX:+UseConcMarkSweepGC 使用CMS收集器
    -XX:+ UseCMSCompactAtFullCollection Full GC后，进行一次碎片整理；整理过程是独占的，会引起停顿时间变长
    -XX:+CMSFullGCsBeforeCompaction 设置进行几次Full GC后，进行一次碎片整理-XX:ParallelCMSThreads 设定CMS的线程数量（一般情况约等于可用CPU数量）

#### 1.3.2.3. G1收集器  

<!-- 
https://www.cnblogs.com/cuizhiquan/articles/10961354.html
https://mp.weixin.qq.com/s/dWg5S7m-LUQhxUofHfqb3g
https://mp.weixin.qq.com/s/_0IANOvyP_UNezDm0bxXmg

-->
&emsp; G1(Garbage first)是目前技术发展的最前沿成果之一，HotSpot开发团队赋予它的使命是未来可以替换掉JDK1.5中发布的CMS收集器。  
&emsp; G1是一款而向服务端应用的垃圾收集器。G1回收器在jdk1.9后成为了JVM的默认垃圾回收器。  
&emsp; 通过把Java堆分成大小相等的多个独立区域，回收时计算出每个区域回收所获得的空间以及所需时间的经验值，根据记录两个值来判断哪个区域最具有回收价值，所以叫Garbage First（垃圾优先）。

##### 1.3.2.3.1. G1的内存布局  
<!-- 深入理解Java虚拟机 第3版 -->
&emsp; **<font color = "red">G1的内存布局：</font>** 在G1之前的垃圾收集器，收集的范围都是整个新生代或者老年代，而G1不再是这样。使用G1收集器时，Java堆的内存布局与其他收集器有很大差别，它<font color = "red">将整个Java堆划分为多个大小相等的独立区域（Region）</font>，虽然还保留有新生代和老年代的概念，但新生代和老年代不再是物理隔离的了，它们都是一部分（可以不连续）Region的集合。  
&emsp; **<font color = "red">G1收集器能建立可预测的停顿时间模型，</font>** 是因为它可以有计划地避免在整个Java堆中进行全区域的垃圾收集。<font color = "red">G1跟踪各个Region里面的垃圾堆积的价值大小（回收所获得的空间大小以及回收所需时间的经验值），在后台维护一个优先列表，</font><font color = "lime">每次根据允许的收集时间，优先回收价值最大的Region/这也就是Garbage-First名称的来由）。</font>这种使用Region划分内存空间以及有优先级的区域回收方式，保证子G1收集器在有限的时间内可以获取尽可能高的收集效率。  

        G1收集器避免全区域垃圾收集，它把堆内存划分为大小固定的几个独立区域，并且跟踪这些区域的垃圾收集进度，同时在后台维护一个优先级列表，每次根据所允许的收集时间，优先回收垃圾最多的区域。区域划分和优先级区域回收机制，确保G1收集器可以在有限时间获得最高的垃圾收集效率。 
    
&emsp; Region不可能是孤立的。一个对象分配在某个Region中，它并非只能被本Region中的其他对象引用，而是可以与整个Java堆任意的对象发生引用关系。  
&emsp; 在G1收集器中，Region之间的对象引用以及其他收集器中的新生代与老年代之间的对象引用，<font color = "red">虚拟机是使用Remembered Set 来避免全堆扫描的</font>。G1中每个Region都有一个与之对应的Remembered Set，虚拟机发现程序在对Reference类型的数据进行写操作时，会产生一个Write Barrier暂时中断写操作，检查Reference引用的对象是否处于不同的Region之中（在分代的例子中就是检查是否老年代中的对象引用了新生代中的对象），如果是，便通过CardTable 把相关引用信息记录到被引用对象所属的Region的Remembered Set之中。当进行内存回收时，在GC根节点的枚举范围中加入Remembered Set即可保证不对全堆扫描也不会有遗漏。  

##### 1.3.2.3.2. G1运行流程  
<!-- https://baijiahao.baidu.com/s?id=1663956888745443356&wfr=spider&for=pc-->
&emsp; 不去计算用户线程运行过程中的动作（如使用写屏障维护记忆集的操作），G1收集器的运作过程大致可划分为以下四个步骤：  

* 初始标记（Initial Marking）：仅仅只是标记一下GC Roots能直接关联到的对象，并且修改TAMS 指针的值，让下一阶段用户线程并发运行时，能正确地在可用的Region中分配新对象。这个阶段需要停顿线程，但耗时很短，而且是借用进行Minor GC的时候同步完成的，所以G1收集器在这个阶段实际并没有额外的停顿。    
* 并发标记（Concurrent Marking）：从GC Root开始对堆中对象进行可达性分析，递归扫描整个堆里的对象图，找出要回收的对象，这阶段耗时较长，但可与用户程序并发执行。当对象图扫描完成以后，还要重新处理SATB记录下的在并发时有引用变动的对象。  
* 最终标记（Final Marking）：对用户线程做另一个短暂的暂停，用于处理并发阶段结束后仍遗留下来的最后那少量的SATB记录。   
* 筛选回收（Live Data Counting and Evacuation）：负责更新Region的统计数据，对各个Region的回收价值和成本进行排序，根据用户所期望的停顿时间来制定回收计划，可以自由选择任意多个Region 构成回收集，然后把决定回收的那一部分Region的存活对象复制到空的Region中，再清理掉整个旧Region的全部空间。这里的操作涉及存活对象的移动，是必须暂停用户线程，由多条收集器线程并行完成的。    

&emsp; G1收集器除了并发标记外，其余阶段也是要完全暂停用户线程的， 换言之，它并非纯粹地追求低延迟， **<font color = "lime">官方给它设定的目标是在延迟可控的情况下获得尽可能高的吞吐量，</font>** 所以才能担当起“全功能收集器”的重任与期望。

##### 1.3.2.3.3. G1优缺点  

&emsp; G1收集器有以下特点：  

* **<font color = "lime">并行和并发：</font>** 使用多个CPU来缩短Stop The World停顿时间，与用户线程并发执行。  
* 分代收集：虽然G1可以不需要其他收集器配合就能独立管理整个GC堆，但是还是保留了分代的概念。它能够采用不同的方式去处理新创建的对象和已经存活了一段时间，熬过多次GC的旧对象以获取更好的收集效果。  
* **<font color = "lime">空间整合：</font>** 与CMS的“标记--清理”算法不同，<font color = "red">G1从整体来看是基于“标记整理”算法实现的收集器；从局部上来看是基于“复制”算法实现的。</font>这两种算法都意味着<font color = "lime">G1运作期不会产生内存空间碎片</font>，收集后能提供规整的可用内存。这种特性有利于程序长时间运行，分配大对象吋不会因为无法找到连续内存空而提前触发下一次GC。  
* **<font color = "lime">可预测的停顿：</font>** 这是G1相对于CMS的另一个大优势，<font color = "red">降低停顿时间是G1和CMS共同的关注点，但G1除了追求低停顿外，还能建立可预测的停顿时间模型，</font>能让使用者明确指定在一个长度为M毫秒的时间片段内，消耗在垃圾收集上的时间不得超过N毫秒。  

&emsp; 可以<font color = "red">由用户指定期望的停顿时间是G1收集器很强大的一个功能</font>，设置不同的期望停顿时间，可使得G1在不同应用场景中取得关注吞吐量和关注延迟之间的最佳平衡。不过，这里设置的“期望值”必须是符合实际的。  
&emsp; 相比CMS，G1的优点有很多，暂且不论可以指定最大停顿时间、分Region的内存布局、按收益动态确定回收集这些创新性设计带来的红利，单从最传统的算法理论上看，G1也更有发展潜力。与CMS 的“标记-清除”算法不同，G1从整体来看是基于“标记-整理”算法实现的收集器，但从局部（两个Region 之间）上看又是基于“标记-复制”算法实现，无论如何，这两种算法都意味着G1运作期间不会产生内存空间碎片，垃圾收集完成之后能提供规整的可用内存。这种特性有利于程序长时间运行，在程序为大对象分配内存时不容易因无法找到连续内存空间而提前触发下一次收集。  

&emsp; 比起CMS，G1的弱项也可以列举出不少，如在用户程序运行过程中，G1无论是为了垃圾收集产生的内存占用（Footprint）还是程序运行时的额外执行负载
（Overload）都要比CMS要高。 

---------
    G1 收集器两个最突出的改进是：  
    1. 基于标记-整理算法，不产生内存碎片。
    2. 可以非常精确控制停顿时间，在不牺牲吞吐量前提下，实现低停顿垃圾回收。  

##### 收集过程
&emsp; 收集过程：  
&emsp; ![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-95.png)  
&emsp; G1的回收过程主要分为 3 类：  
&emsp; （1）G1“年轻代”的垃圾回收，同样叫 Minor G1，这个过程和我们前面描述的类似，发生时机就是 Eden 区满的时候。  
（2）老年代的垃圾收集，严格上来说其实不算是收集，它是一个“并发标记”的过程，顺便清理了一点点对象。  
&emsp; （3）真正的清理，发生在“混合模式”，它不止清理年轻代，还会将老年代的一部分区域进行清理。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-96.png)  

##### 1.3.2.3.4. 使用G1  
&emsp; **开启G1：** 在JDK9之前，JDK7和JDK8默认都是ParallelGC垃圾回收。到了JDK9，G1才是默认的垃圾回收器。所以如果JDK7或者JDK8需要使用G1的话，需要通过参数（-XX:+UseG1GC）显示执行垃圾回收器。而JDK9以后的版本，不需要任何JVM参数，默认就是G1垃圾回收模式，显示指定G1运行一个Demo程序如下：  

```java
java -Xmx1g -Xms1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -jar demo.jar
```
  
#### 1.3.2.4. 常用的收集器组合  

| |新生代GC策略	|老年代GC策略	|说明|
|---|---|---|---|
|组合1|Serial|Serial Old|Serial和Serial Old都是单线程进行GC，特点就是GC时暂停所有应用线程。|
|组合2|Serial|CMS+Serial Old|CMS（Concurrent Mark Sweep）是并发GC，实现GC线程和应用线程并发工作，不需要暂停所有应用线程。另外，当CMS进行GC失败时，会自动使用Serial Old策略进行GC。|
|组合3|ParNew|CMS|使用-XX:+UseParNewGC选项来开启。ParNew是Serial的并行版本，可以指定GC线程数，默认GC线程数为CPU的数量。可以使用-XX:ParallelGCThreads选项指定GC的线程数。如果指定了选项-XX:+UseConcMarkSweepGC选项，则新生代默认使用ParNew GC策略。|
|组合4|ParNew|Serial Old|使用-XX:+UseParNewGC选项来开启。新生代使用ParNew GC策略，年老代默认使用Serial Old GC策略。|
|组合5|Parallel Scavenge|Serial Old	|Parallel Scavenge策略主要是关注一个可控的吞吐量：应用程序运行时间 / (应用程序运行时间 + GC时间)，可见这会使得CPU的利用率尽可能的高，适用于后台持久运行的应用程序，而不适用于交互较多的应用程序。|
|组合6|Parallel Scavenge|Parallel Old|Parallel Old是Serial Old的并行版本|
|组合7|G1GC|G1GC|-XX:+UnlockExperimentalVMOptions -XX:+UseG1GC  <br/>#开启  <br/>-XX:MaxGCPauseMillis =50  #暂停时间目标  <br/>-XX:GCPauseIntervalMillis =200  #暂停间隔目标  <br/>-XX:+G1YoungGenSize=512m  #年轻代大小  <br/>-XX:SurvivorRatio=6  #幸存区比例|

#### 1.3.2.5. ZGC  
<!-- 
 新一代垃圾回收器ZGC的探索与实践 
 https://mp.weixin.qq.com/s/ag5u2EPObx7bZr7hkcrOTg
 ZGC
https://mp.weixin.qq.com/s/5trCK-KlwikKO-R6kaTEAg
一文读懂Java 11的ZGC为何如此高效 
https://mp.weixin.qq.com/s/nAjPKSj6rqB_eaqWtoJsgw

-->
&emsp; 一款由Oracle公司研发的，以低延迟为首要目标的一款垃圾收集器。它是基于动态Region内存布局，（暂时）不设年龄分代，使用了读屏障、染色指针和内存多重映射等技术来实现可并发的标记-整理算法的收集器。在JDK 11新加入，还在实验阶段，主要特点是：回收TB级内存（最大4T），停顿时间不超过10ms  

* 优点：低停顿，高吞吐量，ZGC收集过程中额外耗费的内存小  
* 缺点：浮动垃圾  

&emsp; ZGC目前只在Linux/x64上可用，如果有足够的需求，将来可能会增加对其他平台的支持  
&emsp; 启动参数: -XX:+UnlockExperimentalVMOptions -XX:+UseZGC -Xmx10g -Xlog:gc  

&emsp; 1. ZGC收集器之动态Region  

* 小型Region（Small Region）：容量固定为2MB，用于放置小于256KB的小对象  
* 中型Region（Medium Region）：容量固定为32MB，用于放置大于等于256KB但小于4MB的对象  
* 大型Region（Large Region）：容量不固定，可以动态变化，但必须为2MB的整数倍，用于放置4MB或以上的大对象。每个大型Region中只会存放一个大对象，最小容量可低至4MB，所有大型Region可能小于中型Region  
* 大型Region在ZGC的实现中是不会被重分配的，因为复制一个大对象的代价非常高昂  

&emsp; 2. ZGC收集器之染色指针  
&emsp; HotSpot虚拟机的标记实现方案有如下几种：  

* 把标记直接记录在对象头上（如Serial收集器）；
* 把标记记录在与对象相互独立的数据结构上（如G1、Shenandoah使用了一种相当于堆内存的1/64大小的，称为BitMap的结构来记录标记信息）
* 直接把标记信息记在引用对象的指针上（如ZGC）

&emsp; 3. 染色指针是一种直接将少量额外的信息存储在指针上的技术  
&emsp; 目前在Linux下64位的操作系统中高18位是不能用来寻址的，但是剩余的46为却可以支持64T的空间，到目前为止我们几乎还用不到这么多内存。于是ZGC将46位中的高4位取出，用来存储4个标志位，剩余的42位可以支持4T的内存  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-62.png)  

&emsp; 4. ZGC收集器之三色标记  
&emsp; 在并发的可达性分析算法中我们使用三色标记（Tri-color Marking）来标记对象是否被收集器访问过  

* 白色：表示对象尚未被垃圾收集器访问过。显然在可达性分析刚刚开始的阶段，所有的对象都是白色的，若在分析结束的阶段，仍然是白色的对象，即代表不可达
* 黑色：表示对象已经被垃圾收集器访问过，且这个对象的所有引用都已经扫描过。黑色的对象代表已经扫描过，它是安全存活的，如果有其他对象引用指向了黑色对象，无须重新扫描一遍。黑色对象不可能直接（不经过灰色对象）指向某个白色对象。
* 灰色：表示对象已经被垃圾收集器访问过，但这个对象上至少存在一个引用还没有被扫描过

&emsp; 5. ZGC收集器之读屏障  
&emsp; 当对象从堆中加载的时候，就会使用到读屏障（Load Barrier）。这里使用读屏障的主要作用就是检查指针上的三色标记位，根据标记位判断出对象是否被移动过，如果没有可以直接访问，如果移动过就需要进行“自愈”（对象访问会变慢，但也只会有一次变慢），当“自愈”完成后，后续访问就不会变慢  
读写屏障可以理解成对象访问的“AOP”操作  

&emsp; 6. ZGC收集器之内存多重映射  
&emsp; ZGC使用了内存多重映射（Multi-Mapping）将多个不同的虚拟内存地址映射到同一个物理内存地址上，这是一种多对一映射，意味着ZGC在虚拟内存中看到的地址空间要比实际的堆内存容量来得更大。把染色指针中的标志位看作是地址的分段符，那只要将这些不同的地址段都映射到同一个物理内存空间，经过多重映射转换后，就可以使用染色指针正常进行寻址了  

&emsp; 7. ZGC收集器运作过程  

* 并发标记：与G1、Shenandoah一样，并发标记是遍历对象图做可达性分析的阶段，它的初始标记和最终标记也会出现短暂的停顿，整个标记阶段只会更新染色指针中的Marked 0、Marked 1标志位  
* 并发预备重分配：这个阶段需要根据特定的查询条件统计得出本次收集过程要清理哪些Region，将这些Region组成重分配集（Relocation Set）。ZGC每次回收都会扫描所有的Region，用范围更大的扫描成本换取省去G1中记忆集的维护成本  
* 并发重分配：重分配是ZGC执行过程中的核心阶段，这个过程要把重分配集中的存活对象复制到新的Region上，并为重分配集中的每个Region维护一个转发表（Forward Table），记录从旧对象到新对象的转向关系。ZGC收集器能仅从引用上就明确得知一个对象是否处于重分配集之中，如果用户线程此时并发访问了位于重分配集中的对象，这次访问将会被预置的内存屏障所截获，然后立即根据Region上的转发表记录将访问转发到新复制的对象上，并同时修正更新该引用的值，使其直接指向新对象，ZGC将这种行为称为指针的“自愈”（Self-Healing）能力  
* 并发重映射（Concurrent Remap）：重映射所做的就是修正整个堆中指向重分配集中旧对象的所有引用，但是ZGC中对象引用存在“自愈”功能，所以这个重映射操作并不是很迫切。ZGC很巧妙地把并发重映射阶段要做的工作，合并到了下一次垃圾收集循环中的并发标记阶段里去完成，反正它们都是要遍历所有对象的，这样合并就节省了一次遍历对象图的开销


#### 1.3.2.6. Epsilon  
&emsp; Epsilon（A No-Op Garbage Collector）垃圾回收器控制内存分配，但是不执行任何垃圾回收工作。一旦java的堆被耗尽，jvm就直接关闭。设计的目的是提供一个完全消极的GC实现，分配有限的内存分配，最大限度降低消费内存占用量和内存吞吐时的延迟时间。一个好的实现是隔离代码变化，不影响其他GC，最小限度的改变其他的JVM代码  
&emsp; 适用场景:  

* Performance testing,什么都不执行的GC非常适合用于差异性分析
* 在测试java代码时，确定分配内存的阈值有助于设置内存压力常量值。这时no-op就很有用，它可以简单地接受一个分配的内存分配上限，当内存超限时就失败。例如：测试需要分配小于1G的内存，就使用-Xmx1g参数来配置no-op GC，然后当内存耗尽的时候就直接crash

&emsp; 相关启动参数

    UnlockExperimentalVMOptions：解锁隐藏的虚拟机参数
    -XX:+UnlockExperimentalVMOptions -XX:+UseEpsilonGC -Xms100m -Xmx100m

#### 1.3.2.7. Shenandoah  
&emsp; 一款只有OpenJDK才会包含的收集器，最开始由RedHat公司独立发展后来贡献给了OpenJDK  
&emsp; Shenandoah与G1类似，也是使用基于Region的堆内存布局，同样有着用于存放大对象的Humongous Region，默认的回收策略也同样是优先处理回收价值最大的Region  
&emsp; 但是管理堆内存方面，与G1至少有三个明显的不同之处：  
1. Shenandoah 支持并发的整理算法;G1支持并行整理算法。
2. Shenandoah（目前）是默认不使用分代收集的；G1 有专门的新生代Region或者老年代Region的存在;
3. Shenandoah摒弃了在G1中耗费大量内存和计算资源去维护的记忆集，改用名为“连接矩阵”（Connection Matrix）的全局数据结构来记录跨Region的引用关系，降低了处理跨代指针时的记忆集维护消耗，也降低了伪共享问题的发生概率

* 优点：延迟低
* 缺点：高运行负担使得吞吐量下降；使用大量的读写屏障，尤其是读屏障，增大了系统的性能开销；

&emsp; 开启参数: -XX:+UnlockExperimentalVMOptions  -XX:+UseShenandoahGC

&emsp; Shenandoah 收集器之连接矩阵  
&emsp; 连接矩阵可以简单理解为一张二维表格，如果Region N有对象指向RegionM，就在表格的N行M列中打上一个标记，如右图所示，如果Region 5中的对象Baz引用了Region 3的Foo，Foo又引用了Region 1的Bar，那连接矩阵中的5行3列、3行1列就应该被打上标记。在回收时通过这张表格就可以得出哪些Region之间产生了跨代引用  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-64.png)  

&emsp; Shenandoah 收集器之转发指针  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-65.png)  
&emsp; 转发指针（Forwarding Pointer，也常被称为Indirection Pointer）来实现对象移动与用户程序并发的一种解决方案  
&emsp; Brooks提出的新方案不需要用到内存保护陷阱，而是在原有对象布局结构的最前面统一增加一个新的引用字段，在正常不处于并发移动的情况下，该引用指向对象自己。从结构上来看，Brooks提出的转发指针与某些早期Java虚拟机使用过的句柄定位，有一些相似之处，两者都是一种间接性的对象访问方式，差别是句柄通常会统一存储在专门的句柄池中，而转发指针是分散存放在每一个对象头前面  

&emsp; Shenandoah 收集器之读写屏障  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-66.png)  
&emsp; Brooks形式的转发指针在设计上决定了它是必然会出现多线程竞争问题的，如果收集器线程与用户线程发生的只是并发读取，那无论读到旧对象还是新对象上的字段，返回的结果都应该是一样的，这个场景还可以有一些“偷懒”的处理余地；但如果发生的是并发写入，就一定必须保证写操作只能发生在新复制的对象上，而不是写入旧对象的内存中  
&emsp; 解决方案：Shenandoah不得不同时设置读、写屏障去拦截  

### 1.3.3. 选择合适的垃圾收集器  
&emsp; 官网：https://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/collectors.html#sthref28

* 优先调整堆的大小让服务器自己来选择  
* 如果内存小于100M，使用串行收集器  
* 如果是单核，并且没有停顿时间要求，使用串行或JVM自己选  
* 如果允许停顿时间超过1秒，选择并行或JVM自己选  
* 如果响应时间最重要，并且不能超过1秒，使用并发收集器  
* 对于G1收集  

<!-- 
    如果你的堆大小不是很大（比如 100MB），选择串行收集器一般是效率最高的。

    参数：-XX:+UseSerialGC。

    如果你的应用运行在单核的机器上，或者你的虚拟机核数只有单核，选择串行收集器依然是合适的，这时候启用一些并行收集器没有任何收益。

    参数：-XX:+UseSerialGC。

    如果你的应用是“吞吐量”优先的，并且对较长时间的停顿没有什么特别的要求。选择并行收集器是比较好的。

    参数：-XX:+UseParallelGC。

    如果你的应用对响应时间要求较高，想要较少的停顿。甚至 1 秒的停顿都会引起大量的请求失败，那么选择G1、ZGC、CMS都是合理的。虽然这些收集器的 GC 停顿通常都比较短，但它需要一些额外的资源去处理这些工作，通常吞吐量会低一些。

    参数：

    -XX:+UseConcMarkSweepGC、

    -XX:+UseG1GC、

    -XX:+UseZGC 等。

从上面这些出发点来看，我们平常的 Web 服务器，都是对响应性要求非常高的。选择性其实就集中在 CMS、G1、ZGC上。而对于某些定时任务，使用并行收集器，是一个比较好的选择。
-->

### 1.3.4. ~~垃圾收集器常用参数~~  
&emsp; -XX:+UseSerialGC：在新生代和老年代使用串行收集器  
&emsp; -XX:+UseParNewGC：在新生代使用并行收集器  
&emsp; -XX:+UseParallelGC：新生代使用并行回收收集器，更加关注吞吐量  
&emsp; -XX:+UseParallelOldGC：老年代使用并行回收收集器  
&emsp; -XX:ParallelGCThreads：设置用于垃圾回收的线程数  
&emsp; -XX:+UseConcMarkSweepGC：新生代使用并行收集器，老年代使用CMS+串行收集器  
&emsp; -XX:ParallelCMSThreads：设定CMS的线程数量  
&emsp; -XX:+UseG1GC：启用G1垃圾回收器  




----
## 1.4. 方法区(类和常量)回收/类的卸载阶段
&emsp; 永久代中回收的内容主要是两部分：废弃的常量和无用的类。  
&emsp; <font color = "lime">类的卸载阶段，判断无用的类必须满足三个条件：</font>  

* 该类所以的实例都已经被回收  
* 加载该类的ClassLoader被回收  
* 该类对应的java.lang.Class对象没有在任何地方引用，无法在任何地方通过反射访问该类的方法  

&emsp; 虚拟机可以对上述3个条件对无用类进行回收，这里说的仅仅是“可以”，而不是和对象一样不使用了就会必然回收。  

&emsp; 如果以上三个条件全部满足，<font color = "lime">jvm会在方法区垃圾回收的时候对类进行卸载</font>，<font color = "red">类的卸载过程其实就是在方法区中清空类信息，java类的整个生命周期就结束了。</font>  

&emsp; 如何判断⼀个常量是废弃常量?   
&emsp; 运行时常量主要回收的是废弃的常量。假如在常量池中存在字符串"abc"，如果当前没有任何String对象引用该字符串常量的话，就说明常量"abc"就是废弃常量，如果这时发生内存回收的话而且有必要的话，"abc"就会被系统清理出常量池。  


<!-- 
方法区的垃圾收集主要回收两部分：废弃的常量和不再使用的类型。

废弃常量以及无用类
3.1，如何判断一个常量是废弃常量？
运行时常量池主要回收的是废弃的常量。那么，我们如何判断一个常量是废弃常量呢?
假如在常量池中存在字符串"abc" ,如果当前没有任何String对象引用该字符串常量的话，就说明常量"abc"就是废弃常量,如果这时发生内存回收的话而且有必要的话，" abc"就会被系统清理出常量池。
3.2，如何判断一个类是无用的类？
判定一个常量是否是“废弃常量”比较简单，而要判定一个类是否是“无用的类”的条件则相对苛刻许多。方法区主要回收无用的类，类需要同时满足下面3个条件才能算是 “无用的类” ：

    该类所有的实例都已经被回收，也就是 Java 堆中不存在该类的任何实例。
    加载该类的 ClassLoader 已经被回收。
    该类对应的 java.lang.Class 对象没有在任何地方被引用，无法在任何地方通过反射访问该类的方法。

虚拟机可以对满足上述3个条件的无用类进行回收，这里说的仅仅是“可以”，而并不是和对象一样不使用了就会必然被回收。
-->

