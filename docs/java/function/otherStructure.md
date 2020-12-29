

<!-- TOC -->

- [1. 高级数据结构](#1-高级数据结构)
    - [1.1. 跳跃表](#11-跳跃表)
        - [1.1.1. 跳跃表介绍](#111-跳跃表介绍)
        - [1.1.2. 跳跃表的基本操作](#112-跳跃表的基本操作)
            - [1.1.2.1. 插入节点](#1121-插入节点)
            - [1.1.2.2. 删除节点](#1122-删除节点)
    - [1.2. 位图BitMap](#12-位图bitmap)
        - [1.2.1. BitMap介绍](#121-bitmap介绍)
        - [1.2.2. BitMap的实现思想](#122-bitmap的实现思想)
        - [1.2.3. BitMap应用](#123-bitmap应用)
        - [1.2.4. 问题及应用实例](#124-问题及应用实例)
        - [1.2.5. BitMap算法的拓展](#125-bitmap算法的拓展)
    - [1.3. HyperLogLog](#13-hyperloglog)
    - [1.4. 布隆过滤器](#14-布隆过滤器)
        - [1.4.1. 布隆算法实现](#141-布隆算法实现)
    - [1.5. trie，前缀树/字典树](#15-trie前缀树字典树)

<!-- /TOC -->


# 1. 高级数据结构  

## 1.1. 跳跃表  
### 1.1.1. 跳跃表介绍
&emsp; 跳跃表SkipList，一种基于有序链表的扩展，<font color = "red">是一个多层索引链表</font>，简称跳表。<font color = "red">跳表是一种空间换时间的数据结构，通过冗余数据，将链表一层一层索引，达到类似二分查找的效果。</font>  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/function/function-1.png)  

&emsp; 跳跃表和二叉查找树的区别：跳跃表的优点是维持结构平衡的成本比较低，完全依靠随机。而二叉查找树在多次插入删除后，需要自平衡来重新调整结构平衡。  

### 1.1.2. 跳跃表的基本操作  
#### 1.1.2.1. 插入节点  
&emsp; 跳跃表插入节点的步骤：  
1. 新节点和各层索引节点逐一比较，确定原链表的插入位置。O(logN）
2. 把索引插入到原链表。O(1）
3. 利用抛硬币的随机方式，决定新节点是否提升为上一级索引。结果为“正”则提升并继续抛硬币，结果为“负”则停止。O(logN）  

&emsp; 总体上，跳跃表插入操作的时间复杂度是O(logN)，而这种数据结构所占空间是2N，既空间复杂度是O(N)。  

#### 1.1.2.2. 删除节点  
&emsp; 跳跃表删除节点的步骤：  

1. 自上而下，查找第一次出现节点的索引，并逐层找到每一层对应的节点。O(logN）
2. 删除每一层查找到的节点，如果该层只剩下1个节点，删除整个一层(原链表除外)。O(logN）  

&emsp; 总体上，跳跃表删除操作的时间复杂度是O(logN）。  

## 1.2. 位图BitMap  
### 1.2.1. BitMap介绍  
&emsp; BitMap是什么?  
&emsp; BitMap是通过一个bit位来表示某个元素对应的值或者状态，其中的key就是对应元素本身。8个bit可以组成一个Byte，所以bitmap本身会极大的节省储存空间。  
&emsp; BitMap算法基本描述  
&emsp; BitMap是使用bit位来标记某个元素对应的value，而key即是该元素，因此对于之前位数存储换成bit位存储数据能大大的节省存储空间。  

### 1.2.2. BitMap的实现思想  
&emsp; 假设要对0-7内的5个元素(4,7,2,5,3)排序（这里假设这些元素没有重复）。那么就可以采用Bit-map的方法来达到排序的目的。要表示8个数，就只需要8个Bit（1Bytes），首先开辟1Byte的空间，将这些空间的所有Bit位都置为0(如下图：)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/function/function-33.png)  
&emsp; 然后遍历这5个元素，首先第一个元素是4，那么就把4对应的位置为1（可以这样操作 p+(i/8)|(0×01<<(i%8)) 当然了这里的操作涉及到Big-ending和Little-ending的情况，这里默认为Big-ending。不过计算机一般是小端存储的，如intel。小端的话就是将倒数第5位置1），因为是从零开始的，所以要把第五位置为一（如下图）：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/function/function-34.png)  
&emsp; 然后再处理第二个元素7，将第八位置为1，接着再处理第三个元素，一直到最后处理完所有的元素，将相应的位置为1，这时候的内存的Bit位的状态如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/function/function-35.png)  
&emsp; 然后现在遍历一遍Bit区域，将该位是一的位的编号输出（2，3，4，5，7），这样就达到了排序的目的。  

### 1.2.3. BitMap应用

&emsp; 1）可进行数据的快速查找，判重，删除，一般来说数据范围是int的10倍以下。  
&emsp; 2）去重数据而达到压缩数据  
&emsp; 3）还可以用于爬虫系统中url去重、解决全组合问题。  

### 1.2.4. 问题及应用实例  
<!-- 
https://www.cnblogs.com/yswyzh/p/9600260.html
https://blog.csdn.net/pipisorry/article/details/62443757
-->

### 1.2.5. BitMap算法的拓展  
&emsp; Bloom filter可以看做是对bit-map的扩展。更大数据量的有一定误差的用来判断映射是否重复的算法。  

## 1.3. HyperLogLog  

<!-- 
https://www.cnblogs.com/linguanh/p/10460421.html
https://www.jianshu.com/p/4748af30d194
https://www.jianshu.com/p/b517e976d953
-->

## 1.4. 布隆过滤器  

&emsp; BloomFilter是由一个固定大小的二进制向量或者位图（bitmap）和一系列（通常好几个）映射函数组成的。  
&emsp; **1. 布隆过滤器的原理：**  
&emsp; **<font color = "red">当一个变量被加入集合时，通过K个映射函数将这个变量映射成位图中的 K 个点，把它们置为1。</font>**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/function/function-2.png)  

&emsp; 查询某个变量的时候，只要看看这些点是不是都是1，就可以大概率知道集合中有没有它了。  

* 如果这些点有任何一个0，则被查询变量一定不在；
* 如果都是1，则被查询变量很可能在。  

&emsp; 注意，这里是<font color = "lime">可能存在，而不是一定存在！</font>  

&emsp; **2. 布隆过滤器的特点：**  
* 优点：占用内存少，新增、查询效率高。  
* 缺点： **<font color = "red">误判率和不能删除。</font>**  

        布隆过滤器的误判是指多个输入经过哈希之后在相同的bit位置1了，这样就无法判断究竟是哪个输入产生的，因此误判的根源在于相同的bit位被多次映射且置1。  
        这种情况也造成了布隆过滤器的删除问题，因为布隆过滤器的每一个bit并不是独占的，很有可能多个元素共享了某一位。如果直接删除这一位的话，会影响其他的元素。  

* 特点总结：  
    * **<font color = "lime">一个元素如果判断结果为存在的时候元素不一定存在（可能存在），但是判断结果为不存在的时候则一定不存在。</font>**  
    * **<font color = "red">布隆过滤器可以添加元素，但是不能删除元素。</font><font color = "lime">因为删掉元素会导致误判率增加。</font>**  

&emsp; **3. 布隆过滤器的使用场景：** 布隆过滤器适合于一些需要去重，但不一定要完全精确的场景。比如：  
&emsp; &emsp; 1. 黑名单  
&emsp; &emsp; 2. URL去重  
&emsp; &emsp; 3. 单词拼写检查  
&emsp; &emsp; 4. Key-Value缓存系统的Key校验   
&emsp; &emsp; 5. ID校验，比如订单系统查询某个订单ID是否存在，如果不存在就直接返回。

### 1.4.1. 布隆算法实现  
&emsp; 布隆算法实现有RedisBloom、guava的BloomFilter。  


## 1.5. trie，前缀树/字典树  
<!-- 

https://www.cnblogs.com/justinh/p/7716421.html
「算法与数据结构」Trie树之美 
https://juejin.im/post/6888451657504391181

Trie 树（前缀树）
https://mp.weixin.qq.com/s?__biz=MzI5MTU1MzM3MQ==&mid=2247484257&idx=1&sn=ef0104a011707ad1ab35bb4b8991ad79&scene=21#wechat_redirect

-->
&emsp; 常用字典数据结构如下所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/function/function-12.png)  
&emsp; Trie 的核心思想是空间换时间， 利用字符串的公共前缀来降低查询时间的开销以达到提 高效率的目的。它有 3 个基本性质：   
&emsp; 1、根节点不包含字符， 除根节点外每一个节点都只包含一个字符。  
&emsp; 2、从根节点到某一节点， 路径上经过的字符连接起来， 为该节点对应的字符串。  
&emsp; 3、每个节点的所有子节点包含的字符都不相同。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/function/function-13.png)  
&emsp; 1、可以看到， trie 树每一层的节点数是 26^i 级别的。所以为了节省空间， 我们还可以 用动态链表，或者用数组来模拟动态。而空间的花费，不会超过单词数× 单词长度。  
&emsp; 2、实现： 对每个结点开一个字母集大小的数组， 每个结点挂一个链表， 使用左儿子右 兄弟表示法记录这棵树；  
&emsp; 3、对于中文的字典树，每个节点的子节点用一个哈希表存储，这样就不用浪费太 大的空 间， 而且查询速度上可以保留哈希的复杂度 O(1)。  


----

&emsp; 在计算机科学中，trie，又称前缀树或字典树，是一种有序树，用于保存关联数组，其中的键通常是字符串。与二叉查找树不同，键不是直接保存在节点中，而是由节点在树中的位置决定。一个节点的所有子孙都有相同的前缀，也就是这个节点对应的字符串，而根节点对应空字符串。一般情况下，不是所有的节点都有对应的值，只有叶子节点和部分内部节点所对应的键才有相关的值。  
&emsp; 又称单词查找树，Trie树，是一种树形结构，是一种哈希树的变种。<font color = "lime">典型应用是用于统计，排序和保存大量的字符串（但不仅限于字符串），所以经常被搜索引擎系统用于文本词频统计。</font>它的优点是：利用字符串的公共前缀来减少查询时间，最大限度地减少无谓的字符串比较，查询效率比哈希树高。  
&emsp; Trie，又经常叫前缀树，字典树等等。它有很多变种，如后缀树，Radix Tree/Trie，PATRICIA tree，以及bitwise版本的crit-bit tree。当然很多名字的意义其实有交叉。  

&emsp; 它有3个基本性质：  
&emsp; <font color = "lime">根节点不包含字符，除根节点外每一个节点都只包含一个字符；从根节点到某一节点，路径上经过的字符连接起来，为该节点对应的字符串；每个节点的所有子节点包含的字符都不相同。</font>  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/function/function-11.png)  
&emsp; 红色节点表示一个单词的结尾。  


&emsp; 优点：  
&emsp; 可以最大限度地减少无谓的字符串比较，故可以用于词频统计和大量字符串排序。  
&emsp; 跟哈希表比较：  
1. 最坏情况时间复杂度比hash表好  
2. 没有冲突，除非一个key对应多个值（除key外的其他信息）  
3. 自带排序功能（类似Radix Sort），中序遍历trie可以得到排序。  

&emsp; 缺点：
1. 虽然不同单词共享前缀，但其实trie是一个以空间换时间的算法。其每一个字符都可能包含至多字符集大小数目的指针（不包含卫星数据）。  
每个结点的子树的根节点的组织方式有几种。1>如果默认包含所有字符集，则查找速度快但浪费空间（特别是靠近树底部叶子）。2>如果用链接法(如左儿子右兄弟)，则节省空间但查找需顺序（部分）遍历链表。3>alphabet reduction: 减少字符宽度以减少字母集个数。4>对字符集使用bitmap，再配合链接法。  
2. 如果数据存储在外部存储器等较慢位置，Trie会较hash速度慢（hash访问O(1)次外存，Trie访问O(树高)）。  
3. 长的浮点数等会让链变得很长。可用bitwise trie改进。  

&emsp; 应用  
1. 串的快速检索  
&emsp; 给出N个单词组成的熟词表，以及一篇全用小写英文书写的文章，请按最早出现的顺序写出所有不在熟词表中的生词。  
&emsp; 在这道题中，可以用数组枚举，用哈希，用字典树，先把熟词建一棵树，然后读入文章进行比较，这种方法效率是比较高的。  
2. “串”排序  
&emsp; 给定N个互不相同的仅由一个单词构成的英文名，让将它们按字典序从小到大输出。  
&emsp; 用字典树进行排序，采用数组的方式创建字典树，这棵树的每个结点的所有儿子很显然地按照其字母大小排序。对这棵树进行先序遍历即可。  
3. 最长公共前缀  
&emsp; 对所有串建立字典树，对于两个串的最长公共前缀的长度即它们所在的结点的公共祖先个数，于是，问题就转化为当时公共祖先问题。  

&emsp; 代码实现  

```java
packagecom.suning.search.test.tree.trie;
 
public class Trie
{
    private int SIZE=26;
    private TrieNode root;//字典树的根
 
    Trie() //初始化字典树
    {
        root=new TrieNode();
    }
 
    private class TrieNode //字典树节点
    {
        private int num;//有多少单词通过这个节点,即由根至该节点组成的字符串模式出现的次数
        private TrieNode[]  son;//所有的儿子节点
        private boolean isEnd;//是不是最后一个节点
        private char val;//节点的值
        private boolean haveSon;
 
        TrieNode()
        {
            num=1;
            son=new TrieNode[SIZE];
            isEnd=false;
            haveSon=false;
        }
    }
 
    //建立字典树
    public void insert(String str) //在字典树中插入一个单词
    {
        if(str==null||str.length()==0)
        {
            return;
        }
        TrieNode node=root;
        char[]letters=str.toCharArray();
        for(int i=0,len=str.length(); i<len; i++)
        {
            int pos=letters[i]-'a';
            if(node.son[pos]==null)
            {
                node.haveSon = true;
                node.son[pos]=newTrieNode();
                node.son[pos].val=letters[i];
            }
            else
            {
                node.son[pos].num++;
            }
            node=node.son[pos];
        }
        node.isEnd=true;
    }
 
    //计算单词前缀的数量
    public int countPrefix(Stringprefix)
    {
        if(prefix==null||prefix.length()==0)
        {
            return-1;
        }
        TrieNode node=root;
        char[]letters=prefix.toCharArray();
        for(inti=0,len=prefix.length(); i<len; i++)
        {
            int pos=letters[i]-'a';
            if(node.son[pos]==null)
            {
                return 0;
            }
            else
            {
                node=node.son[pos];
            }
        }
        return node.num;
    }
    //打印指定前缀的单词
    public String hasPrefix(String prefix)
    {
        if (prefix == null || prefix.length() == 0)
        {
            return null;
        }
        TrieNode node = root;
        char[] letters = prefix.toCharArray();
        for (int i = 0, len = prefix.length(); i < len; i++)
        {
            int pos = letters[i] - 'a';
            if (node.son[pos] == null)
            {
                return null;
            }
            else
            {
                node = node.son[pos];
            }
        }
        preTraverse(node, prefix);
        return null;
    }
    // 遍历经过此节点的单词.
    public void preTraverse(TrieNode node, String prefix)
    {
        if (node.haveSon)
        {
                     for (TrieNode child : node.son)
            {
                if (child!=null)
                {
                    preTraverse(child, prefix+child.val);
                }
            }
            return;
        }
        System.out.println(prefix);
    }
 
 
    //在字典树中查找一个完全匹配的单词.
    public boolean has(Stringstr)
    {
        if(str==null||str.length()==0)
        {
            return false;
        }
        TrieNode node=root;
        char[]letters=str.toCharArray();
        for(inti=0,len=str.length(); i<len; i++)
        {
            intpos=letters[i]-'a';
            if(node.son[pos]!=null)
            {
                node=node.son[pos];
            }
            else
            {
                return false;
            }
        }
        return node.isEnd;
    }
 
    //前序遍历字典树.
    public void preTraverse(TrieNodenode)
    {
        if(node!=null)
        {
            System.out.print(node.val+"-");
                        for(TrieNodechild:node.son)
            {
                preTraverse(child);
            }
        }
    }
 
    public TrieNode getRoot()
    {
        return this.root;
    }
 
    public static void main(String[]args)
    {
        Trietree=newTrie();
        String[]strs= {"banana","band","bee","absolute","acm",};
        String[]prefix= {"ba","b","band","abc",};
                for(Stringstr:strs)
        {
            tree.insert(str);
        }
        System.out.println(tree.has("abc"));
        tree.preTraverse(tree.getRoot());
        System.out.println();
                //tree.printAllWords();
                for(Stringpre:prefix)
        {
            int num=tree.countPrefix(pre);
            System.out.println(pre+""+num);
        }
    }
}
```