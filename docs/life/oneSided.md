

&emsp; 学习不要学的片面，告诫自己！！！

&emsp; 先自我介绍一下，小编是非名校毕业，非名企的小员工。每天学一点点，然后就形成了自己的笔记。笔记内容良心制作，毕竟是我自己要看的，我炕别人，也不会炕自己啊。虽然笔记都是抄各路大神的。

<center>图片1</center>

&emsp; “金三银四”招聘季，好多小伙伴都有想法，然后我就向身边的小伙伴，推荐我的笔记。也有想完善笔记的想法。  

&emsp; 接下来的事件，就引发了我写这篇博客的冲动。  

&emsp; **<font color = "clime">事件一：</font>**  

&emsp; 我的Volatile写的不是很好，大概是因为我的笔记Volatile没有从JMM讲起，而是直接说的内存屏障。

<center>图片2</center>

&emsp; 基于此，然后再结合我平时学的时候的困惑。然后就想写这篇文章。   



&emsp; 首先说明一下，为什么我的笔记Volatile直接从内存屏障讲起，把JMM单独写的？  

&emsp; 在这里，我也想问我自己，为什么人家取名叫Java内存模型，而不是Volatile模型或Volatile原则？难道整个JMM就只是为了Volatile抽象出来的？  
&emsp; 多线程编程涉及到线程通信、线程同步这些操作，同时会引发线程安全、线程活跃性等问题。解决线程安全的方案有多种：悲观锁、乐观锁、线程封闭(包含栈封闭、本地存储)、不可变对象。<font color = "red">难道这些都跟JMM没关系？</font>  
&emsp; ​**<font color = "clime">事件二：</font>**  
&emsp; 学习ReentrantLock，肯定都会提到AQS。可是作为JUC包基石的同步工具类，也不是只是为ReentrantLock设计的，<font color = "red">这些或多或少都得重点提一下。</font>  
&emsp; **<font color = "clime">事件三：</font>**   
&emsp; 分布式事务的本地消息表方案，服务A调用服务B，有的介绍是用mq，有的介绍是用定时任务。是不是介绍这种方案的时候，得简单介绍下《Base：An Acid Alternative》论文，好让我这种小白明白点。  
<center>图片3</center>

&emsp; 在介绍分布式事务的时候，有些文章连saga都不提(诚然对于此方案，我也没有透彻)。  

