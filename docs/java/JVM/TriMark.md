
<!-- TOC -->

- [1. ~~三色标记~~](#1-三色标记)
    - [1.1. 三色](#11-三色)
    - [1.2. ~~三色标记流程~~](#12-三色标记流程)
    - [1.3. 问题](#13-问题)
        - [1.3.1. 多标/错标（浮动垃圾）](#131-多标错标浮动垃圾)
        - [1.3.2. 漏标---读写屏障](#132-漏标---读写屏障)
            - [1.3.2.1. 漏标现象产生](#1321-漏标现象产生)
            - [1.3.2.2. ~~新增对象算漏标问题么？~~](#1322-新增对象算漏标问题么)
            - [1.3.2.3. 漏标解决方案](#1323-漏标解决方案)
                - [1.3.2.3.1. CMS---写屏障（tore Barrier）+增量更新](#13231-cms---写屏障tore-barrier增量更新)
                - [1.3.2.3.2. G1---写屏障（tore Barrier）+SATB](#13232-g1---写屏障tore-barriersatb)
                - [1.3.2.3.3. 读屏障（Load Barrier）](#13233-读屏障load-barrier)
                - [1.3.2.3.4. 小结](#13234-小结)
                - [1.3.2.3.5. ~~STAB详解~~](#13235-stab详解)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. 三色：  
    * 黑色：本对象已访问过，而且本对象 引用到 的其他对象 也全部访问过了。  
    * 灰色：本对象已访问过，但是本对象 引用到 的其他对象 尚未全部访问完。全部访问后，会转换为黑色。  
    * 白色：尚未访问过。  
2. 三色标记流程： 1).根对象黑色... **<font color = "clime">如果标记结束后对象仍为白色，意味着已经“找不到”该对象在哪了，不可能会再被重新引用。</font>**  
3. **<font color = "clime">`多标/错标`，本应该回收 但是 没有回收掉的内存，被称之为“浮动垃圾”</font>** ，并不会影响垃圾回收的正确性，只是需要等到下一轮垃圾回收才被清除。  
4. **<font color = "clime">漏标：把本来应该存活的垃圾，标记为了死亡。这就会导致非常严重的错误。</font>**   
	1. 两个必要条件：1). 灰色指向白色的引用消失。2). 黑色重新指向白色；  
  &emsp; 新增对象不算漏标。  
	2. CMS采用增量更新（针对新增的引用，将其记录下来等待遍历）， **<font color = "clime">关注引用的增加（黑色重新指向白色），把黑色重写标记为灰色，下次重新扫描属性。</font>** 破坏了条件“黑指向白”。  
  &emsp; CMS预清理：（`三色标记法的漏标问题处理`） **<font color = "red">这个阶段是用来</font><font color = "blue">处理</font><font color = "clime">前一个并发标记阶段因为引用关系改变导致没有标记到的存活对象的。如果发现对象的引用发生变化，则JVM会标记堆的这个区域为Dirty Card。那些能够从Dirty Card到达的对象也被标记（标记为存活），当标记做完后，这个Dirty Card区域就会消失。</font>**  
	3. G1采用开始时快照技术SATB， **<font color = "clime">关注引用的删除（灰色指向白色的引用消失），当B->D消失时，要把这个引用推到GC的堆栈，保证D还能被GC扫描到。破坏了条件“灰指向白的引用消失”。</font>** 保存在GC堆栈中的删除引用，会在最终标记remark阶段处理。    
	4. 使用SATB会大大减少扫描对象。  

# 1. ~~三色标记~~

&emsp; <font color = "red">观看了马士兵的《Java高级工程师就业班 标记算法》</font>  

<!-- 
三色标记  SATB
https://www.bilibili.com/video/BV17J411V7tz?from=search&seid=3673548210809646054
https://www.bilibili.com/video/BV1Uz4y1S798

此时，若用户线程和GC线程并发工作，用户也要修改对象间的引用关系结构，这样就会产生两种后果：一种是把原本消亡的对象标记为存活；另一种是会把原本存活的对象标记为消亡。

SATB快照的存在，就可以让G1采用如下办法解决上述问题：当灰色对象要删除指向白色对象的引用关系时，就将这个要删除的引用记录下来，在并发扫描结束后，再以记录过的引用关系中的灰色对象为根，重新扫描一次，这样无论引用关系删除与否，都会按照最初的对象结构图进行搜索。虚拟机通过写屏障，实现操作记录的记录和修改，避免并发带来的问题。


SATB
全称是Snapshot-At-The-Beginning，由字面理解，是GC开始时活着的对象的一个快照。它是通过Root Tracing得到的，作用是维持并发GC的正确性。 那么它是怎么维持并发GC的正确性的呢？根据三色标记算法，我们知道对象存在三种状态： * 白：对象没有被标记到，标记阶段结束后，会被当做垃圾回收掉。 * 灰：对象被标记了，但是它的field还没有被标记或标记完。 * 黑：对象被标记了，且它的所有field也被标记完了。

由于并发阶段的存在，Mutator和Garbage Collector线程同时对对象进行修改，就会出现白对象漏标的情况，这种情况发生的前提是： * Mutator赋予一个黑对象该白对象的引用。 * Mutator删除了所有从灰对象到该白对象的直接或者间接引用。

对于第一个条件，在并发标记阶段，如果该白对象是new出来的，并没有被灰对象持有，那么它会不会被漏标呢？Region中有两个top-at-mark-start（TAMS）指针，分别为prevTAMS和nextTAMS。在TAMS以上的对象是新分配的，这是一种隐式的标记。对于在GC时已经存在的白对象，如果它是活着的，它必然会被另一个对象引用，即条件二中的灰对象。如果灰对象到白对象的直接引用或者间接引用被替换了，或者删除了，白对象就会被漏标，从而导致被回收掉，这是非常严重的错误，所以SATB破坏了第二个条件。也就是说，一个对象的引用被替换时，可以通过write barrier 将旧引用记录下来。

//  share/vm/gc_implementation/g1/g1SATBCardTableModRefBS.hpp
// This notes that we don't need to access any BarrierSet data
// structures, so this can be called from a static context.
template <class T> static void write_ref_field_pre_static(T* field, oop newVal) {
  T heap_oop = oopDesc::load_heap_oop(field);
  if (!oopDesc::is_null(heap_oop)) {
    enqueue(oopDesc::decode_heap_oop(heap_oop));
  }
}
// share/vm/gc_implementation/g1/g1SATBCardTableModRefBS.cpp
void G1SATBCardTableModRefBS::enqueue(oop pre_val) {
  // Nulls should have been already filtered.
  assert(pre_val->is_oop(true), "Error");
  if (!JavaThread::satb_mark_queue_set().is_active()) return;
  Thread* thr = Thread::current();
  if (thr->is_Java_thread()) {
    JavaThread* jt = (JavaThread*)thr;
    jt->satb_mark_queue().enqueue(pre_val);
  } else {
    MutexLockerEx x(Shared_SATB_Q_lock, Mutex::_no_safepoint_check_flag);
    JavaThread::satb_mark_queue_set().shared_satb_queue()->enqueue(pre_val);
  }
}

SATB也是有副作用的，如果被替换的白对象就是要被收集的垃圾，这次的标记会让它躲过GC，这就是float garbage。因为SATB的做法精度比较低，所以造成的float garbage也会比较多。
-->
<!--
~~ 
https://www.bilibili.com/read/cv6830986/
https://mp.weixin.qq.com/s/pswlzZugDhNeZKerTTsCnQ
https://blog.csdn.net/waltonhuang/article/details/105550331
-->

&emsp; 三色标记法，并发标记阶段使用的算法，在CMS和G1中都有用到。     

## 1.1. 三色  
&emsp; 要找出存活对象，根据可达性分析，从GC Roots开始进行遍历访问，可达的则为存活对象。将遍历对象图过程中遇到的对象，按“是否访问过”这个条件标记成以下三种颜色：  

* 黑色：本对象已访问过，而且本对象 引用到 的其他对象 也全部访问过了。  
* 灰色：本对象已访问过，但是本对象 引用到 的其他对象 尚未全部访问完。全部访问后，会转换为黑色。  
* 白色：尚未访问过。  

## 1.2. ~~三色标记流程~~  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-135.png)  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-1.gif)  
<center>三色标记法的标记过程</center>


&emsp; 假设现在有白、灰、黑三个集合（表示当前对象的颜色），其遍历访问过程为：  
1. 初始时，所有对象都在【白色集合】中；  
2. 将GC Roots 直接引用到的对象 挪到 【灰色集合】中；  
3. 从灰色集合中获取对象：  
  1. 将本对象 引用到的 其他对象 全部挪到 【灰色集合】中； 
  2. 将本对象 挪到 【黑色集合】里面。  
4. 重复步骤3，直至【灰色集合】为空时结束。   
5. 结束后，仍在【白色集合】的对象即为GC Roots 不可达，可以进行回收。  

&emsp; 注： **<font color = "clime">如果标记结束后对象仍为白色，意味着已经“找不到”该对象在哪了，不可能会再被重新引用。</font>**  
&emsp; 当Stop The World（STW）时，对象间的引用是不会发生变化的，可以轻松完成标记。而当需要支持并发标记时，即标记期间应用线程还在继续跑，对象间的引用可能发生变化，多标和漏标的情况就有可能发生。  

## 1.3. 问题  

### 1.3.1. 多标/错标（浮动垃圾）
&emsp; 假设已经遍历到E（变为灰色了），此时应用执行了 objD.fieldE = null。    
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-136.png)  
<center>D 到 E 的引用断开</center>

&emsp; 此刻之后，对象E/F/G是“应该”被回收的。然而因为E已经变为灰色了，其仍会被当作存活对象继续遍历下去。最终的结果是：这部分对象仍会被标记为存活，即本轮GC不会回收这部分内存。  
&emsp; 这部分本应该回收 但是 没有回收掉的内存，被称之为“浮动垃圾”。 **<font color = "red">浮动垃圾并不会影响垃圾回收的正确性，只是需要等到下一轮垃圾回收中才被清除。</font>**   
&emsp; 另外，针对并发标记开始后的新对象，通常的做法是直接全部当成黑色，本轮不会进行清除。这部分对象期间可能会变为垃圾，这也算是浮动垃圾的一部分。  


### 1.3.2. 漏标---读写屏障
&emsp; **<font color = "clime">漏标问题，意思就是把本来应该存活的垃圾，标记为了死亡。这就会导致非常严重的错误。</font>**  

#### 1.3.2.1. 漏标现象产生
&emsp; 假设GC线程已经遍历到E（变为灰色了），此时应用线程先执行了：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-137.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-138.png)  
<center>E 到 G 断开，D引用 G</center>


&emsp; 此时切回GC线程继续跑，因为E已经没有对G的引用了，所以不会将G放到灰色集合；尽管因为D重新引用了G，但因为D已经是黑色了，不会再重新做遍历处理。  
&emsp; 最终导致的结果是：G会一直停留在白色集合中，最后被当作垃圾进行清除。这直接影响到了应用程序的正确性，是不可接受的。  

    对象消失问题：扫描过程中插入了一条或多条从黑色对象到白色对象的新引用，并且同时去掉了灰色对象到该白色对象的直接引用或间接引用。  


&emsp; 不难分析， **<font color = "clime">漏标只有同时满足以下两个条件时才会发生：</font>**  
&emsp; 1：至少有一个黑色对象在自己被标记之后指向了白色对象。  
&emsp; 2：删除了灰色对象到白色对象的直接或间接引用。  

&emsp; 从代码的角度看：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-140.png)  
1. 读取 对象E的成员变量fieldG的引用值，即对象G；
2. 对象E 往其成员变量fieldG，写入 null值。
3. 对象D 往其成员变量fieldG，写入 对象G ；

&emsp; 只要在上面这三步中的任意一步中做一些“手脚”，将对象G记录起来，然后作为灰色对象再进行遍历即可。比如放到一个特定的集合，等初始的GC Roots遍历完（并发标记），该集合的对象 遍历即可（重新标记）。  

&emsp; 写屏障用于拦截第二和第三步；而读屏障则是拦截第一步。它们的拦截的目的很简单：就是在读写前后，将对象G给记录下来。  
&emsp; 注：这里的屏障非内存屏障。   

#### 1.3.2.2. ~~新增对象算漏标问题么？~~
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-139.png)  

&emsp; 答案是：不算。  
&emsp; 不知大家在学习三色标记的时候是否有过这种疑惑。为什么漏标的对象必须要删除灰色对象到白色对象的直接或间接引用。 **<font color = "clime">那这种直接新增的对象为什么不会被三色标记判定为漏标呢？ </font>** 

&emsp; **G1中的TAMS**  
&emsp; 要达到与GC用户并发运行，必要要解决回收过程中新对象的分配（否则在一条GCRoots标记完成之后，新来的对象会被当做垃圾回收），所以G1在每个Region中设置了两个名为TAMS的指针，在Region划分出部分空间用于记录并发回收过程中的新对象，认为它们是存活的，不纳入垃圾回收。因此，在分析漏标问题时，便可以严格遵守漏标问题产生的两个条件了。  

#### 1.3.2.3. 漏标解决方案  
&emsp; 处理漏标问题，需要打破其中任意一个条件就可以了。对于以上问题，CMS与G1各采用了一套方案进行处理。  


##### 1.3.2.3.1. CMS---写屏障（tore Barrier）+增量更新
&emsp; 当对象D的成员变量的引用发生变化时（objD.fieldG = G;），可以利用写屏障，将D新的成员变量引用对象G记录下来。 **<font color = "clime">针对新增的引用，将其记录下来等待遍历，即增量更新（Incremental Update）。</font>**  
&emsp; 增量更新破坏了条件二：【黑色对象 重新引用了 该白色对象】，从而保证了不会漏标。  

----------

![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-141.png)  
&emsp; 增量更新打破了第一个条件。当A插入新的引用关系D时，就将这个插入的引用记录的A对象记录下来。等待扫描结束后的重新标记阶段，再把这些记录过的引用了新对象的对象（如上图A）再次变为灰色。就会重新对A再进行一次扫描。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-142.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-143.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-144.png)  
&emsp; 由图可见，此方法再次执行可以处理漏标的D对象。但是B，C对象属于被重复扫描的对象。  


##### 1.3.2.3.2. G1---写屏障（tore Barrier）+SATB
&emsp; 当对象E的成员变量的引用发生变化时（objE.fieldG = null;），可以利用写屏障，将E原来成员变量的引用对象G记录下来，记录下来的就叫原始快照（Snapshot At The Beginning，SATB），后续的标记也照着SATB走。  
&emsp; SATB破坏了条件一：【灰色对象 断开了 白色对象的引用】，从而保证了不会漏标。  

------

&emsp; 灰色对象指向白色对象，把删除记录保留下来。下次只扫描记录中的原白色对象是否变更。   

----------

&emsp; 使用SATB的原因  
1. 因为增量更新会在重新标记的时候将从黑色变成灰色的对象，再扫描一遍，会更费时。  
2. 使用SATB就会大大减少扫描对象，原因是只多扫描在GC堆栈中发生改变的引用（和G1的一个RSet进行配合，RSet为每个）   

<!-- 
为什么G1用SATB？  
灰色 -> 白色 引用消失时，如果没有黑色指向白色引用会被push到堆栈。  
下次扫描时拿到这个引用，由于有Rset的存在，不需要扫描整个堆去查找指向白色的引用，效率比较高。  
-->
----------


##### 1.3.2.3.3. 读屏障（Load Barrier）
&emsp; 读屏障是直接针对第一步：var G = objE.fieldG;，当读取成员变量时，一律记录下来。  
&emsp; ZGC采用读屏障。  

##### 1.3.2.3.4. 小结  
&emsp; CMS关注新增，但是会把新增对象的前一个引用变为灰色，重新对所有子对象进行遍历，有一定性能损耗。G1引用的打破，不需要重新遍历对象，因此效率较高。但是维护快照，以及其保守策略增加了一定的内存成本。

<!-- 
跟踪A -> D的增加；跟踪B -> D的删除。  


增量更新，关注引用的增加，把黑色重写标记为灰色，下次重新扫描属性。  
SATB snapshot at the beginning，关注引用的删除
当B->D消失时，要把这个引用推到GC的堆栈，保证D还能被GC扫描到。 
-->
##### 1.3.2.3.5. ~~STAB详解~~  
<!-- 
STAB
https://blog.csdn.net/f191501223/article/details/84726719
https://blog.csdn.net/qq_33460562/article/details/103202935
https://blog.csdn.net/weixin_30814223/article/details/95706567
-->