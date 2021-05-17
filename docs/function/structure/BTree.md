<!-- TOC -->

- [1. B树](#1-b树)
    - [1.1. B树](#11-b树)
    - [1.2. B+树](#12-b树)

<!-- /TOC -->

# 1. B树  
<!-- 
用动图讲解 MySQL 索引底层B+树，清晰明了多了 
https://mp.weixin.qq.com/s/IfiYbWd-YLxO2TvLB8_jcQ

https://mp.weixin.qq.com/s?__biz=MjM5ODI5Njc2MA==&mid=2655825290&idx=1&sn=455202c916d0a7bd9e3fbbef95ddde76&chksm=bd74e05d8a03694b028c2276d3ddda70e9e966ac98962d503e0c8c5c30bf5f4b6042165387d0&mpshare=1&scene=1&srcid=&key=cc7d6364edc5eb3e38ee4b8744025c5b8304677bd8a7197d95df3a0b3b4ad88320f3d2d20bb52f598836b6b9eb5a197c509b3269acb2b0b364a3284d20b55e6d00cf3e09b4c852e6e24530514e5810d6&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060834&lang=zh_CN&pass_ticket=%2BsFXjUmklNmvFptz4WXQ6M2h%2BrUqyTCeblH3SKK7yV7hbPgMA1x9kiOqCmMdYFI5

https://mp.weixin.qq.com/s?__biz=MzUxNDA1NDI3OA==&mid=2247485500&idx=1&sn=5bc6205dd0c33a4db668ac10de582ae9&chksm=f94a89d5ce3d00c38578fe9e2929515fb5c0a34b803dfd1a5ab8bce9596882fa82a1730e9159&scene=0&xtrack=1&key=cc7d6364edc5eb3e44037378c313b78dfd6436acac192ebc6bf3673892f7a9c2e44750f425f9e51f51231c87215c8358667ad6f8090b0df5ade6e57eb18e715cc5c81c206390ed5cec8151788d6536fa&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060833&lang=zh_CN&pass_ticket=WvQRNfXTbzo8YbNsaaP3bvOrF4WWy2nhzya3QiHsgSx6qD6EFNaOiTxgL7MHqDsT

B-Tree：
一棵m阶的B-Tree有如下特性：
每个结点最多m个子结点。 
除了根结点和叶子结点外，每个结点最少有 m/2(向上取整)个子结点。 
如果根结点不是叶子结点，那根结点至少包含两个子结点。 
所有的叶子结点都位于同一层。 
每个结点都包含k个元素(关键字)，这里 m/2≤k。
每个节点中的元素(关键字)从小到大排列。 
每个元素(关键字)字左结点的值，都小于或等于该元素(关键字)。右结点的值都大于或等于该元素(关键字)。

B+树的好处主要体现在查询性能上。B+树相比B-树的优势有3个：
https://mp.weixin.qq.com/s/cK_GIhCuGoUwJpDpoaETxw?
1.单一节点存储更多的元素，使得查询的IO次数更少。
2.所有查询都要查找到叶子节点，查询性能稳定。
3.所有叶子节点形成有序链表，便于范围查询。

B+Tree是在B-Tree基础上的一种优化，使其更适合实现外存储索引结构。
B+Tree与B-Tree的结构很像，但是也有自己的特性：
所有的非叶子节点只存储关键字信息。 
所有卫星数据(具体数据)都存在叶子结点中。 
所有的叶子结点中包含了全部元素的信息。 
所有叶子节点之间都有一个链指针。
![image](https://gitee.com/wt1814/pic-host/raw/master/algorithm/function-24.png)  
非叶子结点上已经只有Key信息了，满足上面第1点特性！ 
所有叶子结点下面都有一个Data区域，满足上面第 2 点特性！ 
非叶子结点的数据在叶子结点上都能找到，如根结点的元素4、8在最底层的叶子结点上也能找到，满足上面第3点特性！
注意图中叶子结点之间的箭头，满足上面第4点特性！
-->

## 1.1. B树  
&emsp; 一棵m阶B树(或B-树、B_树)是一棵平衡的m路搜索树。它或者是空树，或者是满足下列性质的树：  

1. 根结点至少有两个子女；  
2. 每个非根节点所包含的关键字个数 j 满足：(m/2) - 1 <= j <= m - 1；  
3. 除根结点以外的所有结点(不包括叶子结点)的度数正好是关键字总数加1，故内部子树个数 k 满足：(m/2) <= k <= m ；  
4. 所有的叶子结点都位于同一层。  

&emsp; 在B-树中，每个结点中关键字从小到大排列，并且当该结点的孩子是非叶子结点时，该k-1个关键字正好是k个孩子包含的关键字的值域的分划。  
&emsp; 因为叶子结点不包含关键字，所以可以把叶子结点看成在树里实际上并不存在外部结点，指向这些外部结点的指针为空，叶子结点的数目正好等于树中所包含的关键字总个数加1。  
&emsp; B-树中的一个包含n个关键字，n+1个指针的结点的一般形式为：(n,P0,K1,P1,K2,P2,…,Kn,Pn)  
&emsp; 其中，Ki为关键字，K1<K2<…<Kn, Pi 是指向包括Ki到Ki+1之间的关键字的子树的指针。

## 1.2. B+树  
&emsp; B+树是B树的一种变形形式，<font color = "clime">B+树上的叶子结点存储关键字以及相应记录的地址，叶子结点以上各层作为索引使用。</font>一棵m阶的B+树定义如下:    
1. 每个结点至多有m个子女；   
2. 除根结点外，每个结点至少有[m/2]个子女，根结点至少有两个子女；  
3. 有k个子女的结点必有k个关键字。  


&emsp; <font color = "red">B+树的查找与B树不同，当索引部分某个结点的关键字与所查的关键字相等时，并不停止查找，应继续沿着这个关键字左边的指针向下，一直查到该关键字所在的叶子结点为止。</font>  
