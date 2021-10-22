

<!-- TOC -->

- [1. GC](#1-gc)
    - [1.1. 堆中对象的存活](#11-堆中对象的存活)
        - [1.1.1. GC的存活标准](#111-gc的存活标准)
            - [1.1.1.1. 引用计数法](#1111-引用计数法)
            - [1.1.1.2. 根可达性分析法](#1112-根可达性分析法)
                - [1.1.1.2.1. java 全局变量 内存不回收](#11121-java-全局变量-内存不回收)
            - [1.1.1.3. 对象的四种引用状态，强弱软虚](#1113-对象的四种引用状态强弱软虚)
                - [1.1.1.3.1. 强引用](#11131-强引用)
                - [1.1.1.3.2. 软引用](#11132-软引用)
                - [1.1.1.3.3. 弱引用](#11133-弱引用)
                - [1.1.1.3.4. 虚引用](#11134-虚引用)
                - [1.1.1.3.5. ★★★软引用和弱引用的使用](#11135-★★★软引用和弱引用的使用)
        - [1.1.2. 对象生存还是死亡](#112-对象生存还是死亡)
    - [1.2. 方法区(类和常量)回收/类的卸载阶段](#12-方法区类和常量回收类的卸载阶段)
    - [1.3. ~~null与GC~~](#13-null与gc)

<!-- /TOC -->


&emsp; **<font color = "red">总结：</font>**  
1. 堆中对象的存活：  
	1. 存活标准
		1. 引用计数法、根可达性分析法  
			1. **<font color = "clime">不可回收对象包含 1). 方法区中，类静态属性(static)引用的对象； 2). 方法区中，常量(final static)引用的对象；</font>** 
			2. `由以上可得java 全局变量 不可被回收。`  
		2. 四种引用  
			* **<font color = "red">软引用：当堆使用率临近阈值时，才会去回收软引用的对象。</font>**  
			* **<font color = "red">弱引用：只要发现弱引用，不管系统堆空间是否足够，都会将对象进行回收。</font>**  

					软引用和弱引用的使用：
					软引用，弱引用都非常适合来保存那些可有可无的缓存数据，如果这么做，当系统内存不足时，这些缓存数据会被回收，不会导致内存溢出。而当内存资源充足时，这些缓存数据又可以存在相当长的时间，从而起到加速系统的作用。  
					假如⼀个应⽤需要读取⼤量的本地图⽚，如果每次读取图⽚都从硬盘读取会严重影响性能，如果⼀次性全部加载到内存⼜可能造成内存溢出，这时可以⽤软引⽤解决这个问题。  

			* 虚引用：虚引用是所有类型中最弱的一个。 **<font color = "red">一个持有虚引用的对象，和没有引用几乎是一样的，随时可能被垃圾回收器回收。</font>**    
	2. 对象生存还是死亡？  
	&emsp; **<font color = "clime">如果有必要执行父类`Object#finalize()`方法，放入F-Queue队列；收集器将对F-Queue队列中的对象进行第二次小规模的标记；如果对象在执行finalize()方法时重新与引用链上的任何一个对象建立关联则逃脱死亡，否则执行死亡。</font>**  
2. 方法区(类和常量)回收/类的卸载阶段

3. null与GC：  
&emsp; 《深入理解Java虚拟机》作者的观点：在需要“不使用的对象应手动赋值为null”时大胆去用，但不应当对其有过多依赖，更不能当作是一个普遍规则来推广。  
&emsp; **<font color = "red">虽然代码片段已经离开了变量xxx的`作用域`，但在此之后，没有任何对运行时栈的读写，placeHolder所在的索引还没有被其他变量重用，所以GC判断其为存活。</font>**    
&emsp; 加上`int replacer = 1;`和将placeHolder赋值为null起到了同样的作用：断开堆中placeHolder和栈的联系，让GC判断placeHolder已经死亡。    
&emsp; “不使用的对象应手动赋值为null”的原理，一切根源都是来自于JVM的一个“bug”：代码离开变量作用域时，并不会自动切断其与堆的联系。    


# 1. GC  
<!-- 
亿级流量系统如何玩转 JVM 
https://mp.weixin.qq.com/s/_uKSQjI--eT3n04Voel5_g

&emsp; 可以作为GC ROOT的对象包括：  
1. 栈中引用的对象
2. 静态变量、常量引用的对象
3. 本地方法栈native方法引用的对象
-->

<!-- 
~~
先动手验证垃圾回收
https://mp.weixin.qq.com/s/SMkovwz5fZP5obsUtgQAUg

https://mp.weixin.qq.com/s/IH9p5X7WveKGx1ujfHzFag
深度揭秘垃圾回收底层，这次让你彻底弄懂她 
https://mp.weixin.qq.com/s?__biz=MzU4Mjk0MjkxNA==&mid=2247487815&idx=2&sn=c94666e98b4f3c6e46f4e9378f3f4e39&chksm=fdb1f8eacac671fc587323800a0421cdf015b59b10ac8086936f28cfa9f6921410ab7fbd28c4&scene=21#wechat_redirect
炸了！一口气问了我18个JVM问题！ 
https://mp.weixin.qq.com/s/WVGZIBXsIVYPMfhkqToh_Q
-->
&emsp; GC主要解决下面的三个问题：  

* 哪些内存需要回收？ GC回收的主要区域是堆、方法区。  
* 什么时候回收？  
* 如何回收？  

## 1.1. 堆中对象的存活
<!-- 
https://juejin.im/post/5e151b38f265da5d495c8025 
--> 

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
* 本地方法栈中，JNI(即Native方法)引用的对象；  



##### 1.1.1.2.1. java 全局变量 内存不回收
<!-- 
java 全局变量 内存不回收
https://blog.csdn.net/weixin_39565910/article/details/110721496
-->


#### 1.1.1.3. 对象的四种引用状态，强弱软虚    
&emsp; 无论是通过引用计数算法判断对象的引用数量，还是通过根搜索算法判断对象的引用链是否可达，判定对象是否存活都与“引用”有关。  
&emsp; 在JDK中提供了四个级别的引用：强引用，软引用，弱引用和虚引用。在这四个引用类型中，只有强引用Final Reference类是包内可见，其他三种引用类型均为public，可以在应用程序中直接使用。引用类型的类结构如图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-22.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-18.png)  
&emsp; **<font color = "clime">Java中引入四种引用的目的是让程序自己决定对象的生命周期，JVM是通过垃圾回收器对这四种引用做不同的处理，来实现对象生命周期的改变。</font>**  

<!-- 
Java设计这四种引用的主要目的有两个：
    可以让程序员通过代码的方式来决定某个对象的生命周期；
    有利用垃圾回收。
-->

##### 1.1.1.3.1. 强引用  
&emsp; 代码中普遍存在的类似"Object obj = new Object()"这类的引用，只要强引用还存在，垃圾收集器永远不会回收被引用的对象。  
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
&emsp; 弱引用是一种比软引用较弱的引用类型。在系统GC时， **<font color = "red">只要发现弱引用，不管系统堆空间是否足够，都会将对象进行回收。</font>** <font color = "clime">它可以作为简单的缓存表解决方案。</font>  
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

##### 1.1.1.3.5. ★★★软引用和弱引用的使用  
&emsp; **<font color = "red">软引用，弱引用都非常适合来保存那些可有可无的缓存数据，如果这么做，当系统内存不足时，这些缓存数据会被回收，不会导致内存溢出。而当内存资源充足时，这些缓存数据又可以存在相当长的时间，从而起到加速系统的作用。</font>**  
&emsp; **<font color = "clime">假如⼀个应⽤需要读取⼤量的本地图⽚，如果每次读取图⽚都从硬盘读取会严重影响性能，如果⼀次性全部加载到内存⼜可能造成内存溢出，这时可以⽤软引⽤解决这个问题。</font>**  
&emsp; 设计思路：⽤⼀个HashMap来保存图⽚路径和相应图⽚对象关联的软引⽤之间的映射关系，在内存不⾜时，JVM会⾃动共回收这些缓存图⽚对象所占的空间，避免OOM。  

```java
Map<String, SoftReference<Bitmap>> imageCache = new HashMap<>();  
```  

### 1.1.2. 对象生存还是死亡

<!-- 
&emsp; 即使在可达性分析算法中不可达的对象，也并非一定是“非死不可”的，这时候他们暂时处于“缓刑”阶段，真正宣告一个对象死亡至少要经历两次标记过程：  
&emsp; 1)如果对象在可达性分析算法中不可达，那么它会被第一次标记并进行一次刷选，刷选的条件是是否需要执行finalize()方法(当对象没有覆盖finalize()或者finalize()方法已经执行过了(对象的此方法只会执行一次))，虚拟机将这两种情况都会视为没有必要执行)。  
&emsp; 2)如果这个对象有必要执行finalize()方法会将其放入F-Queue队列中，稍后GC将对F-Queue队列进行第二次标记，如果在重写finalize()方法中将对象自己赋值给某个类变量或者对象的成员变量，那么第二次标记时候就会将它移出“即将回收”的集合。  
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
    垃圾回收器(garbage colector)决定回收某对象时，就会运⾏该对象的finalize()⽅法，但是在Java中很不幸，如果内存总是充⾜的，那么垃圾回收可能永远不会进⾏，也就是说filalize() 可能永远不被执⾏，显然指望它做收尾⼯作是靠不住的。 那么finalize()究竟是做什么的呢？ 它最主要的⽤途是回收特殊渠道申请的内存。Java程序有垃圾回收器，所以⼀般情况下内存问题不⽤程序员操⼼。但有⼀种JNI(Java Native Interface)调⽤non-Java程序(C或C++)， finalize()的⼯作就是回收这部分的内存。

<!--
1. <font color = "red">如果对象在进行可达性分析后没有与GC Root相连接的引用链，那么它会被第一次标记，并且进行一次筛选，</font><font color = "clime">筛选的条件是是否有必要执行finalize()。</font>即：当对象没有被覆盖finalize()或者finalize()方法已经被虚拟机调用过了，虚拟机将这两种情况都视为“没有必要执行”。  
2. <font color = "red">当一个对象被判断为有必要执行finalize()方法，那么这个对象会被放置到F-Queue队列中，</font><font color = "clime">并且稍后JVM自动建立一个低优先级的Finalizer线程执行它，这里“执行”是虚拟机会触发这个方法，但不会承诺等待它运行结束</font>(万一这个方法运行缓慢或者死循环，F-Queue队列其他对象岂不是永久等待)。<font color = "red">finalize()是对象逃脱死亡的最后一次机会。稍后GC会对F-Queue进行第二次小规模标记。</font>如果对象能在finalize()方法中重新与引用链上任何一个方法建立关联(例如把自己this关键字赋值给某个类变量或者对象的成员变量)。那么第二次标记时，将会移出即将回收的集合。否则，这个对象就会被回收了。  
&emsp; 注：任何对象的finalize()方法都只会被系统调用一次。  
-->

1. <font color = "red">判断有没有必要执行Object#finalize()方法</font>  
&emsp; **<font color = "clime">如果对象在进行可达性分析后发现没有与GC Roots相连接的引用链，那它将会被第一次标记并且进行一次筛选，</font>** 筛选的条件是此对象是否有必要执行finalize()方法。  
&emsp; 另外，有两种情况都视为“没有必要执行”：1)对象没有覆盖finaliza()方法；2)finalize()方法已经被虚拟机调用过。  
2. <font color = "red">如何执行？</font>F-Queue的队列  
&emsp; 如果这个对象被判定为有必要执行finalize()方法，那么此对象将会放置在一个叫做F-Queue的队列中，并在稍后由一个虚拟机自动建立的、低优先级的Finalizer线程去执行它。  
3. <font color = "red">执行死亡还是逃脱死亡？</font>  
&emsp; **<font color = "clime">首先，需要知道，finalize()方法是对象逃脱死亡命运的最后一次机会，稍后收集器将对F-Queue队列中的对象进行第二次小规模的标记。</font>**  
&emsp; 逃脱死亡：对象想在finalize()方法中成功拯救自己，只要重新与引用链上的任何一个对象建立关联即可，例如把自己(this关键字)赋值给某个类变量或者对象的成员变量，这样在第二次标记时它将被移出“即将回收”的集合。  
&emsp; 执行死亡：对象没有执行逃脱死亡，那就是死亡了。  
&emsp; 注：任何对象的finalize()方法都只会被系统调用一次。  


&emsp; **<font color = "clime">小结：如果有必要执行Object#finalize()方法，放入F-Queue队列；收集器将对F-Queue队列中的对象进行第二次小规模的标记；如果对象在执行finalize()方法方法时重新与引用链上的任何一个对象建立关联则逃脱死亡，否则执行死亡。</font>**  

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

## 1.2. 方法区(类和常量)回收/类的卸载阶段
&emsp; 永久代中回收的内容主要是两部分：废弃的常量和无用的类。  
&emsp; <font color = "clime">类的卸载阶段，判断无用的类必须满足三个条件：</font>  

* 该类所以的实例都已经被回收；  
* 加载该类的ClassLoader被回收；  
* 该类对应的java.lang.Class对象没有在任何地方引用，无法在任何地方通过反射访问该类的方法。  

&emsp; 虚拟机可以对上述3个条件对无用类进行回收，这里说的仅仅是“可以”，而不是和对象一样不使用了就会必然回收。  

&emsp; 如果以上三个条件全部满足，<font color = "clime">jvm会在方法区垃圾回收的时候对类进行卸载</font>，<font color = "red">类的卸载过程其实就是在方法区中清空类信息，java类的整个生命周期就结束了。</font>  

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

## 1.3. ~~null与GC~~  
<!-- 
重要  https://mp.weixin.qq.com/s/NaZe23qnezTiOw76UjJlSQ

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
&emsp; **有利于GC更早回收内存，减少内存占用。**  

&emsp; **~~<font color = "clime">手动将不用的对象引用置为null，可以使得JVM在下一次GC时释放这部分内存。</font>~~**  
&emsp; ~~对于占用空间比较大的对象(比如大数组)，推荐在确认不再使用的时候将其值为null，jvm在回收大对象的时候不如小对象来的及时，置为null就能强制在下次GC的时候回收掉它。~~  

&emsp; ~~一个对象的引用执行null，并不会被立即回收，还需要执行finalize()方法(必须要重写这个方法，且只能执行一次)。可执行过程中，可能会重新变为可达对象。但是并不鼓励使用这个方法！~~  


