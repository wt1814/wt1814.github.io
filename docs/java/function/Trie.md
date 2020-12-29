



# 1. trie，前缀树/字典树  
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