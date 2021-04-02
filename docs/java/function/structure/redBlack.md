
<!-- TOC -->

- [1. 红黑树](#1-红黑树)
    - [1.1. 红黑树介绍](#11-红黑树介绍)
    - [1.2. 红黑树操作](#12-红黑树操作)

<!-- /TOC -->

# 1. 红黑树  
<!-- 

***30张图带你彻底理解红黑树
https://www.jianshu.com/p/e136ec79235c

红黑树
https://mp.weixin.qq.com/s/7qlH3OSyAs4HbDRrYmd9Qw
一文看懂 HashMap 中的红黑树实现原理 
https://mp.weixin.qq.com/s?__biz=MzU3NzczMTAzMg==&mid=2247485642&idx=1&sn=87686ada46171453fbf0775e9f79eb8e&chksm=fd01687dca76e16bcacbbe2f002adb7fadaac9781d77c4860b39f58cbb5f59d0963b0b17722f&mpshare=1&scene=1&srcid=&sharer_sharetime=1570550410430&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=20f7b87cb3d4d9a8dad7715b92149b376bd18366ce415977bbaf398548f32103b4d30bb2f6383d774381da41d484e6c964315d8e3f1d89fee1374ace541d9ac9cffdd9582b0adc77f024b7fce0f4519a&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=JBXpM50QiNs6zNRp9fK3mUz62dNuz3VUpafHOYmGm%2B8lF%2FexT03S%2FxJgW2UdnnDg

如果面试被问“红黑树”，可以这样回答 
https://mp.weixin.qq.com/s/2_G9dKF033_suehS0Fer1w


什么是红黑树？
https://mp.weixin.qq.com/s/DXh93cQaKRgsKccmoQOAjQ
什么是红黑树？
https://mp.weixin.qq.com/s/tnbbvgPyqz0pEpA76rn_1g
什么是红黑树？
https://mp.weixin.qq.com/s/oAyiRC_O-N5CHqAjt2va9w

在树的结构发生改变时(插入或者删除操作)，往往会破坏上述条件3或条件 4，需要通过调整使得查找树重新满足红黑树的条件。

红黑树主要有以下几个特性：
1. 每个节点要么是红色，要么是黑色，但根节点永远是黑色的；
2. 每个红色节点的两个子节点一定都是黑色；
3. 红色节点不能连续(也即是，红色节点的孩子和父亲都不能是红色)；
4. 从任一节点到其子树中每个叶子节点的路径都包含相同数量的黑色节点；
5. 所有的叶节点都是是黑色的(注意这里说叶子节点其实是上图中的 NIL 节点)；


-->

## 1.1. 红黑树介绍
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/function/function-31.png)  
&emsp; <font color = "red">红黑树是一种近似平衡的二叉查找树。在每个节点增加一个存储位表示节点的颜色，可以是红或黑(非红即黑)。其主要的优点就是“平衡”，</font>即左右子树高度几乎一致，以此来防止树退化为链表，通过这种方式来保障查找的时间复杂度为 log(n)。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/function/function-6.png)  

&emsp; **<font color = "red">红黑树主要有以下几个特性：</font>**   
1. 节点是红色或黑色。   
2. 根节点是黑色。  
3. <font color = "red">所有叶子都是黑色。(叶子是NUIL节点)</font>  
4. 每个红色节点的两个子节点都是黑色。(从每个叶子到根的所有路径上不能有两个连续的红色节点)  
5. 从任一节点到其每个叶子的所有路径都包含相同数目的黑色节点。

&emsp; **是性质4导致路径上不能有两个连续的红色节点确保了这个结果。**最短的可能路径都是黑色节点，最长的可能路径有交替的红色和黑色节点。因为根据性质5所有最长的路径都有相同数目的黑色节点，这就表明了没有路径能多于任何其他路径的两倍长。结果是这个树大致上是平衡的。  
&emsp; 因为操作比如插入、删除和查找某个值的最坏情况时间都要求与树的高度成比例，这个在高度上的理论上限允许红黑树在最坏情况下都是高效的，而不同于普通的二叉查找树。     

&emsp; **红黑树的应用：**  
&emsp; 红黑树的应用比较广泛，主要是用它来存储有序的数据，它的时间复杂度是O(lgn)，效率非常之高。  
&emsp; 例如，Java集合中的TreeSet和TreeMap，C++ STL中的set、map，以及Linux虚拟内存的管理，都是通过红黑树去实现的。  

&emsp; **红黑树与平衡二叉树的区别：**
1. 红黑树放弃了追求完全平衡，<font color = "red">追求大致平衡</font>，在与平衡二叉树的时间复杂度相差不大的情况下，<font color = "clime">保证每次插入最多只需要三次旋转就能达到平衡</font>，实现起来也更为简单。  
2. 平衡二叉树追求绝对平衡，条件比较苛刻，实现起来比较麻烦，每次插入新节点之后需要旋转的次数不能预知。  
3. 红黑树和AVL树的区别在于它使用颜色来标识结点的高度，它所追求的是局部平衡而不是AVL树中的非常严格的平衡。  
4. 红黑树是牺牲了严格的高度平衡的优越条件为代价，红黑树能够以O(log2 n)的时间复杂度进行搜索、插入、删除操作。  
5. 红黑树的算法时间复杂度和AVL相同，但统计性能比AVL树更高。  

## 1.2. 红黑树操作  
&emsp; 在树的结构发生改变时(插入或者删除操作)，往往会破坏上述条件4或条件5，需要通过调整使得查找树重新满足红黑树的条件。  
<!-- 
在一棵AVL树中，我们通过左旋和右旋来调整由于插入和删除所造成的不平衡问题。在红黑树中，可以使用两种方式进行平衡操作：

    重新着色
    旋转

当红黑树中出现不平衡的状态，我们首先会考虑重新着色，如果重新着色依旧不能使红黑树平衡，那么就考虑旋转。
-->
