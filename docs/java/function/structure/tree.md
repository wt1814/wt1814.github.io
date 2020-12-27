

<!-- TOC -->

- [1. 树](#1-树)
    - [1.1. 二叉树](#11-二叉树)
        - [1.1.1. 二叉树简介及各种类型](#111-二叉树简介及各种类型)
        - [1.1.2. 二叉树的三种遍历方式](#112-二叉树的三种遍历方式)
        - [1.1.3. 有关二叉树的算法题](#113-有关二叉树的算法题)
            - [1.1.3.1. 两个通用方法和思路](#1131-两个通用方法和思路)
                - [1.1.3.1.1. 自顶向下的递归遍历](#11311-自顶向下的递归遍历)
                - [1.1.3.1.2. 自底向上的分治](#11312-自底向上的分治)
                - [1.1.3.1.3. ***两种方式比较](#11313-两种方式比较)
            - [1.1.3.2. 递归转非递归](#1132-递归转非递归)
    - [1.2. 二叉堆](#12-二叉堆)
    - [1.3. 红黑树](#13-红黑树)
    - [1.4. B树](#14-b树)
        - [1.4.1. B树](#141-b树)
        - [1.4.2. B+树](#142-b树)

<!-- /TOC -->

# 1. 树  
<!--
AVL树
https://mp.weixin.qq.com/s/7MJWagl_L-ZFlLtKdJwbFQ

-->

## 1.1. 二叉树  
### 1.1.1. 二叉树简介及各种类型
&emsp; **二叉树有两种特殊形式：**一个叫满二叉树，一个叫完全二叉树。  
&emsp; **二叉树的实现：**树是一种逻辑数据结构。二叉树即可以用数组实现，也可以用链表实现。二叉树一般使用链表实现；二叉堆，一种特殊的完全二叉树，使用数组来操作。  
&emsp; **二叉树的遍历：**二叉树的遍历方式有深度优先遍历、广度优先遍历。  
&emsp; **二叉树的应用：**二叉树包含许多特殊的形式，每一种形式都有自己的应用。但是其最主要的应用还在于进行查找操作和维持相对顺序这两个方面。有一种特殊的二叉树叫做**二叉查找树或二叉排序树**。  
&emsp; 二叉查找树有可能退化成一个链表，可以采用自平衡方式优化结构。<font color = "red">二叉树自平衡的方式有多种，如红黑树、平衡二叉树（AVL）等。</font>  
&emsp; **树与递归：**对于二分搜索树这种结构我们要明确的是，树是一种天然的可递归的结构。  

### 1.1.2. 二叉树的三种遍历方式  
<!-- 

https://mp.weixin.qq.com/s?__biz=MzUyNjQxNjYyMg==&mid=2247483881&idx=1&sn=3b1de7f74aaaade96ee0f71960a80609&chksm=fa0e6e68cd79e77e45633b52731e83262dd7ad70a0fd4d97e3c1e44170cc69a62f870526568e&scene=21#wechat_redirect
图文详解 树的DFS 和 BFS 
https://mp.weixin.qq.com/s?__biz=MzI5MTU1MzM3MQ==&mid=2247484022&idx=1&sn=9890a47b9a08809c9a66e613aa8fe311&scene=21#wechat_redirect

-->

### 1.1.3. 有关二叉树的算法题
<!-- 
~~
LeetCode二叉树问题小总结
https://mp.weixin.qq.com/s?__biz=MzUyNjQxNjYyMg==&mid=2247486350&idx=3&sn=f847d84a0c2553d2854b37b6202cb923&chksm=fa0e640fcd79ed19006e12d9d4e330fca44db451413a5870de3758515be60f387d43a1f80ef1&mpshare=1&scene=1&srcid=&sharer_sharetime=1567642529620&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=a1704a04d6cad8d086b12ea2ff25a231d33902d1a964dc54bf8fa7ac6214cc54031a9fadea253fc8d1458fefa4d791da5f09b8d8b6e4ee35369d746f3486560b6d10bdfdd164449b259756a720157dd7&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=OvcJlS3excB3jnhYEzWG32VCQK0zodStiSrRfXXkqPZtu4LVEri6wuSl7MUtsbkU -->
&emsp; LeetCode 上面的二叉树问题一般可以看成是简单的深度优先搜索问题，一般的实现方式是使用递归，也会有非递归的实现方法，本节主要介绍一下解决二叉树问题的几个常规方法和思路，然后会给一个从递归转换到非递归的小技巧。  

#### 1.1.3.1. 两个通用方法和思路  
&emsp; 两个通用方法和思路：拿到一道二叉树的问题，多半需要遍历这个树，只不过是在遍历的过程中，不同的题目要求做的计算不一样。  
&emsp; 这里有两个遍历方法，自顶向下的递归遍历，以及自底向上的分治。  
&emsp; 两种方法都用到了递归，在代码实现上面，差别不是特别大，但是思路却截然相反，拿树的中序遍历这道题目来作为示例。  

##### 1.1.3.1.1. 自顶向下的递归遍历

```java
public List<Integer> inorderTraversal(TreeNode root) {
    List<Integer> result = new ArrayList<>();
    if (root == null) {
        return result;
    }

    helper(root, result);

    return result;
}

private void helper(TreeNode root, List<Integer> result) {
    if (root == null) {
        return;
    }

    helper(root.left, result);
    result.add(root.val);
    helper(root.right, result);
}
```
&emsp; 代码非常的简短，上面代码的重心全放在了 helper 函数上，这个函数没有返回值，它做的事情也非常的简单，就是去到对应的树节点，然后把节点的值加到 result 中。  
&emsp; 这里要求的是树的 中序遍历，因此，要先去到当前树节点的左边，把左边所有的节点的值放到 result 中，才会继续放当前树节点，放完当前树节点的值后，我们会去到右边进行同样的操作。  
&emsp; 对于这种实现方法其实有点类似于循环遍历，只不过循环遍历只作用于数组还有链表这样的线性结构，对于树的话，这里采用了递归的方式去遍历。  

##### 1.1.3.1.2. 自底向上的分治
```java
public List<Integer> inorderTraversal(TreeNode root) {
    List<Integer> result = new ArrayList<>();

    if (root == null) {
        return result;
    }

    result.addAll(inorderTraversal(root.left));
    result.add(root.val);
    result.addAll(inorderTraversal(root.right));

    return result;
}
```
&emsp; 同一个问题，再来看看我们之前提到的另外一种思路实现。  
这里也使用了递归，但是这次的递归函数是有返回值的，而且没有将保存结果的 list 传入函数。  
&emsp; 正因为是自底向上，所以对于一个树节点来说，它这里会知道它子节点的返回值，也就是子节点的记录结果，在它这里会把左右子节点的结果，和它自己本身的结果汇总，然后将汇总的结果返回给上一层节点。  
&emsp; 和之前的递归遍历的思路相比的话，代码实现上面的区别可能就是，是将 result list 放在参数中，还是放在返回值中，但是思考方向是截然相反的。  

##### 1.1.3.1.3. ***两种方式比较
&emsp; 这两种方法没有好坏之分，有的题目使用自顶向下递归遍历的方式会比较直接一点，比如求最大最小值，有些题目则使用自底向上分治的方式会比较好一些，比如说 subtree 的问题。对于不同的题目，需要选择不同的方法，但是思考方式可以考虑从这两个方向去思考。  
&emsp; 一般来说，二叉树问题的时间复杂度都是 O(n) ，这个时间复杂的怎么理解呢？可以看成是在每个树节点上的操作的时间复杂度是 O(1)，但是要遍历所有的节点，因此 O(1) * n = O(n)。  

#### 1.1.3.2. 递归转非递归  
&emsp; 对于树的问题，也可以使用非递归的方式求解，其实任何一个递归的解法，都可以转换为非递归，而且就性能和稳定性来说的话，非递归的方式要比递归来的好。    
&emsp; 这里给出了一个递归转非递归的通用方法，不仅仅适用于树的问题，对于任何的递归问题都适用。还是拿上面中序遍历作为例子，之前的代码实现：  

```java
public List<Integer> inorderTraversal(TreeNode root) {
    List<Integer> result = new ArrayList<>();
    if (root == null) {
        return result;
    }

    helper(root, result);

    return result;
}

private void helper(TreeNode root, List<Integer> result) {
    if (root == null) {
        return;
    }

    helper(root.left, result); // line 0
    result.add(root.val);      // line 1
    helper(root.right, result);// line 2
}
```
&emsp; 在 helper 函数最后 3 行代码标上了序号，后面的非递归实现的程序中会用到，这里主要是要实现 helper 函数中的东西。  
&emsp; **非递归代码如下：**  

```java
public List<Integer> inorderTraversal(TreeNode root) {
    List<Integer> result = new ArrayList<>();
    if (root == null) {
        return result;
    }

    Stack<Integer> systemStack = new Stack<>(); // 表示函数进度
    Stack<TreeNode> paramStack = new Stack<>(); // 表示函数输入参数

    systemStack.push(0);
    paramStack.push(root);

    while (!systemStack.isEmpty()) {
        int curLine = systemStack.pop();
        TreeNode curParam = paramStack.peek();

        // 提前将当前进度后移，因为后面可能会有其他函数入栈
        systemStack.push(curLine + 1);

        if (curLine == 0) {
            if (curParam.left != null) {
                systemStack.push(0);
                paramStack.push(curParam.left);                    
            }
        } else if (curLine == 1) {
            result.add(curParam.val);
        } else if (curLine == 2) {
            if (curParam.right != null) {
                systemStack.push(0);
                paramStack.push(curParam.right);
            }
        } else {
            systemStack.pop();
            paramStack.pop();
        }
    }

    return result;
}
```
&emsp; 一般来说，用非递归写递归，都需要用到一个数据结构-栈。  
&emsp; 这个好解释，递归的解法是利用了系统中提供的函数栈，非递归需要手动创建这么一个数据结构，但是可能会问的是，这里为什么要用到两个栈？  
&emsp; 可以这样认为，一个栈用来表示函数的运行进度，里面的元素表示此时该函数运行到了第几行代码，另一个栈用来记录函数的传入参数，当然也可以把这两个栈合成一个栈，里面装的是封装好的对象。  

    首先，根据之前的递归解法，我们最开始是把 root 传入 helper 函数，因此这时也把 root 加入函数栈，另外一个表示函数进度的栈往里面添加 0，表示当前函数运行到了第 0 行，然后就是 while 循环里面的东西，while 循环一开始我们就获取当前函数的输入参数和进度，然后根据函数的进度去看需要执行哪一段代码，因为有的代码会继续往栈里面添加函数，因此，需要提前把函数进度往后移动，可以对应之前的递归的代码和我标的序号，可以看到，整个 if-else if…else 部分就表示了之前的递归函数中的代码，只不过这里需要根据函数的进度去判断它要执行哪一行。
    使用这种方法后，递归转非递归只需要往上套就行，不需要单独分析。

## 1.2. 二叉堆  
<!-- 

https://mp.weixin.qq.com/s?__biz=MzA4NDE4MzY2MA==&mid=2647523614&idx=1&sn=cf4ee3f0d66a1a7878b655351709ff95&scene=21#wechat_redirect
-->

## 1.3. 红黑树  
<!-- 
如果面试被问“红黑树”，可以这样回答 
https://mp.weixin.qq.com/s/2_G9dKF033_suehS0Fer1w
https://www.cnblogs.com/skywang12345/p/3245399.html

一文看懂 HashMap 中的红黑树实现原理 
https://mp.weixin.qq.com/s?__biz=MzU3NzczMTAzMg==&mid=2247485642&idx=1&sn=87686ada46171453fbf0775e9f79eb8e&chksm=fd01687dca76e16bcacbbe2f002adb7fadaac9781d77c4860b39f58cbb5f59d0963b0b17722f&mpshare=1&scene=1&srcid=&sharer_sharetime=1570550410430&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=20f7b87cb3d4d9a8dad7715b92149b376bd18366ce415977bbaf398548f32103b4d30bb2f6383d774381da41d484e6c964315d8e3f1d89fee1374ace541d9ac9cffdd9582b0adc77f024b7fce0f4519a&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=JBXpM50QiNs6zNRp9fK3mUz62dNuz3VUpafHOYmGm%2B8lF%2FexT03S%2FxJgW2UdnnDg
红黑树主要有以下几个特性：
1. 每个节点要么是红色，要么是黑色，但根节点永远是黑色的；
2. 每个红色节点的两个子节点一定都是黑色；
3. 红色节点不能连续（也即是，红色节点的孩子和父亲都不能是红色）；
4. 从任一节点到其子树中每个叶子节点的路径都包含相同数量的黑色节点；
5. 所有的叶节点都是是黑色的（注意这里说叶子节点其实是上图中的 NIL 节点）；

在树的结构发生改变时（插入或者删除操作），往往会破坏上述条件3或条件 4，需要通过调整使得查找树重新满足红黑树的条件。
-->
&emsp; <font color = "red">红黑树是一种近似平衡的二叉查找树。在每个节点增加一个存储位表示节点的颜色，可以是红或黑（非红即黑）。其主要的优点就是“平衡”，</font>即左右子树高度几乎一致，以此来防止树退化为链表，通过这种方式来保障查找的时间复杂度为 log(n)。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/function/function-6.png)  

&emsp; **<font color = "red">红黑树主要有以下几个特性：</font>**   
1. 节点是红色或黑色。   
2. 根节点是黑色。  
3. <font color = "red">所有叶子都是黑色。（叶子是NUIL节点）</font>  
4. 每个红色节点的两个子节点都是黑色。（从每个叶子到根的所有路径上不能有两个连续的红色节点）  
5. 从任一节点到其每个叶子的所有路径都包含相同数目的黑色节点。

&emsp; 这些约束强制了红黑树的关键性质: 从根到叶子的最长的可能路径不多于最短的可能路径的两倍长。结果是这个树大致上是平衡的。因为操作比如插入、删除和查找某个值的最坏情况时间都要求与树的高度成比例，这个在高度上的理论上限允许红黑树在最坏情况下都是高效的，而不同于普通的二叉查找树。   
&emsp; 是性质4导致路径上不能有两个连续的红色节点确保了这个结果。最短的可能路径都是黑色节点，最长的可能路径有交替的红色和黑色节点。因为根据性质5所有最长的路径都有相同数目的黑色节点，这就表明了没有路径能多于任何其他路径的两倍长。  
&emsp; 因为红黑树是一种特化的二叉查找树，所以红黑树上的只读操行与普通二叉查找树相同。  

&emsp; 在树的结构发生改变时（插入或者删除操作），往往会破坏上述条件4或条件5，需要通过调整使得查找树重新满足红黑树的条件。  

&emsp; **红黑树与平衡二叉树的区别：**
1. 红黑树放弃了追求完全平衡，<font color = "red">追求大致平衡</font>，在与平衡二叉树的时间复杂度相差不大的情况下，<font color = "lime">保证每次插入最多只需要三次旋转就能达到平衡</font>，实现起来也更为简单。  
2. 平衡二叉树追求绝对平衡，条件比较苛刻，实现起来比较麻烦，每次插入新节点之后需要旋转的次数不能预知。  
3. 红黑树和AVL树的区别在于它使用颜色来标识结点的高度，它所追求的是局部平衡而不是AVL树中的非常严格的平衡。  
4. 红黑树是牺牲了严格的高度平衡的优越条件为代价，红黑树能够以O(log2 n)的时间复杂度进行搜索、插入、删除操作。  
5. 红黑树的算法时间复杂度和AVL相同，但统计性能比AVL树更高。  

## 1.4. B树  
<!-- 
https://mp.weixin.qq.com/s?__biz=MjM5ODI5Njc2MA==&mid=2655825290&idx=1&sn=455202c916d0a7bd9e3fbbef95ddde76&chksm=bd74e05d8a03694b028c2276d3ddda70e9e966ac98962d503e0c8c5c30bf5f4b6042165387d0&mpshare=1&scene=1&srcid=&key=cc7d6364edc5eb3e38ee4b8744025c5b8304677bd8a7197d95df3a0b3b4ad88320f3d2d20bb52f598836b6b9eb5a197c509b3269acb2b0b364a3284d20b55e6d00cf3e09b4c852e6e24530514e5810d6&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060834&lang=zh_CN&pass_ticket=%2BsFXjUmklNmvFptz4WXQ6M2h%2BrUqyTCeblH3SKK7yV7hbPgMA1x9kiOqCmMdYFI5

https://mp.weixin.qq.com/s?__biz=MzUxNDA1NDI3OA==&mid=2247485500&idx=1&sn=5bc6205dd0c33a4db668ac10de582ae9&chksm=f94a89d5ce3d00c38578fe9e2929515fb5c0a34b803dfd1a5ab8bce9596882fa82a1730e9159&scene=0&xtrack=1&key=cc7d6364edc5eb3e44037378c313b78dfd6436acac192ebc6bf3673892f7a9c2e44750f425f9e51f51231c87215c8358667ad6f8090b0df5ade6e57eb18e715cc5c81c206390ed5cec8151788d6536fa&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060833&lang=zh_CN&pass_ticket=WvQRNfXTbzo8YbNsaaP3bvOrF4WWy2nhzya3QiHsgSx6qD6EFNaOiTxgL7MHqDsT

B-Tree：
一棵m阶的B-Tree有如下特性：
每个结点最多m个子结点。 
除了根结点和叶子结点外，每个结点最少有 m/2（向上取整）个子结点。 
如果根结点不是叶子结点，那根结点至少包含两个子结点。 
所有的叶子结点都位于同一层。 
每个结点都包含k个元素（关键字），这里 m/2≤k。
每个节点中的元素（关键字）从小到大排列。 
每个元素（关键字）字左结点的值，都小于或等于该元素（关键字）。右结点的值都大于或等于该元素（关键字）。

B+树的好处主要体现在查询性能上。B+树相比B-树的优势有3个：
https://mp.weixin.qq.com/s/cK_GIhCuGoUwJpDpoaETxw?
1.单一节点存储更多的元素，使得查询的IO次数更少。
2.所有查询都要查找到叶子节点，查询性能稳定。
3.所有叶子节点形成有序链表，便于范围查询。

B+Tree是在B-Tree基础上的一种优化，使其更适合实现外存储索引结构。
B+Tree与B-Tree的结构很像，但是也有自己的特性：
所有的非叶子节点只存储关键字信息。 
所有卫星数据（具体数据）都存在叶子结点中。 
所有的叶子结点中包含了全部元素的信息。 
所有叶子节点之间都有一个链指针。
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/function/function-24.png)  
非叶子结点上已经只有Key信息了，满足上面第1点特性！ 
所有叶子结点下面都有一个Data区域，满足上面第 2 点特性！ 
非叶子结点的数据在叶子结点上都能找到，如根结点的元素4、8在最底层的叶子结点上也能找到，满足上面第3点特性！
注意图中叶子结点之间的箭头，满足上面第4点特性！
-->

### 1.4.1. B树  
&emsp; 一棵m阶B树（或B-树、B_树）是一棵平衡的m路搜索树。它或者是空树，或者是满足下列性质的树：  

1. 根结点至少有两个子女；  
2. 每个非根节点所包含的关键字个数 j 满足：(m/2) - 1 <= j <= m - 1；  
3. 除根结点以外的所有结点（不包括叶子结点）的度数正好是关键字总数加1，故内部子树个数 k 满足：(m/2) <= k <= m ；  
4. 所有的叶子结点都位于同一层。  

&emsp; 在B-树中，每个结点中关键字从小到大排列，并且当该结点的孩子是非叶子结点时，该k-1个关键字正好是k个孩子包含的关键字的值域的分划。  
&emsp; 因为叶子结点不包含关键字，所以可以把叶子结点看成在树里实际上并不存在外部结点，指向这些外部结点的指针为空，叶子结点的数目正好等于树中所包含的关键字总个数加1。  
&emsp; B-树中的一个包含n个关键字，n+1个指针的结点的一般形式为：（n,P0,K1,P1,K2,P2,…,Kn,Pn）  
&emsp; 其中，Ki为关键字，K1<K2<…<Kn, Pi 是指向包括Ki到Ki+1之间的关键字的子树的指针。

### 1.4.2. B+树  
&emsp; B+树是B树的一种变形形式，<font color = "lime">B+树上的叶子结点存储关键字以及相应记录的地址，叶子结点以上各层作为索引使用。</font>一棵m阶的B+树定义如下:    
1. 每个结点至多有m个子女；   
2. 除根结点外，每个结点至少有[m/2]个子女，根结点至少有两个子女；  
3. 有k个子女的结点必有k个关键字。  
  
&emsp; <font color = "red">B+树的查找与B树不同，当索引部分某个结点的关键字与所查的关键字相等时，并不停止查找，应继续沿着这个关键字左边的指针向下，一直查到该关键字所在的叶子结点为止。</font>  

