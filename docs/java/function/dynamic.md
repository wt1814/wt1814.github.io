

<!-- TOC -->

- [1. ~~动态规划~~](#1-动态规划)
    - [1.1. 动态规划题型](#11-动态规划题型)

<!-- /TOC -->
# 1. ~~动态规划~~  
<!-- 


一文学会动态规划解题技巧
https://mp.weixin.qq.com/s?__biz=MzI5MTU1MzM3MQ==&mid=2247483932&idx=1&sn=d9cd9d5a5ebf5f31e23f11c82b6465f1&scene=21#wechat_redirect
 动态规划之武林秘籍 
https://mp.weixin.qq.com/s?__biz=MzA4NDE4MzY2MA==&mid=2647523785&idx=1&sn=7df30854c688a51b01bd5e369900b4f5&scene=21#wechat_redirect
https://mp.weixin.qq.com/s?__biz=MzA4NDE4MzY2MA==&mid=2647523785&idx=1&sn=7df30854c688a51b01bd5e369900b4f5&scene=21#wechat_redirect
这才是真正的状态压缩动态规划好不好！！！ 
https://mp.weixin.qq.com/s/H2V3D0DMPbT8hQW9Cq6LjQ

https://blog.csdn.net/zw6161080123/article/details/80639932


递归、分治和动态规划的关系
https://blog.csdn.net/BillCYJ/article/details/78765584
https://blog.csdn.net/weixin_39030846/article/details/113781295
https://www.cnblogs.com/codeskiller/p/6477181.html


-->
&emsp; 动态规划的一般形式就是求最优值，比如最长公共子序列、最大子段和、最优二叉搜索树等等。  


&emsp; **动态规划与分治：**  
&emsp; 分治策略：将原问题分解为若干个规模较小但类似于原问题的子问题(Divide)，递归的求解这些子问题(Conquer)，然后再合并这些子问题的解来建立原问题的解。  
&emsp; 因为在求解大问题时，需要递归的求小问题，因此一般用递归的方法实现，即自顶向下。  
&emsp; 动态规划：动态规划其实和分治策略是类似的，也是将一个原问题分解为若干个规模较小的子问题，递归的求解这些子问题，然后合并子问题的解得到原问题的解。  
&emsp; <font color = "red">动态规划和分治策略的区别在于这些子问题会有重叠，一个子问题在求解后，可能会再次求解，可以将这些子问题的解存储起来，当下次再次求解这个子问题时，直接取过来用。</font>  

&emsp; **从递归到动态规划：**  
&emsp; 递归采用自顶向下的运算，比如：f(n) 是f(n-1)与f(n-2)相加，f(n-1) 是f(n-2)与f(n-3)相加。  
&emsp; 如果反过来，采取自底向上，用迭代的方式进行推导，则是动态规划。  


&emsp; **详解动态规划：**  
&emsp; **<font color = "red">动态规划求解最值问题。动态规划实质是求最优解，不过很多题目是简化版，只要求返回最大值/最小值。</font>** 最优解问题是指问题通常有多个可行解，需要从这些可行解中找到一个最优的可行解。  
&emsp; **<font color = "red">动态规划中包含三个重要的概念：</font>** 最优子结构( f(10) =f(9)+f(8) )、边界( f(1) 与 f(2) )、状态转移公式( f(n) =f(n-1)+f(n-2) )。  

## 1.1. 动态规划题型  
<!-- 
最长公共子串
https://mp.weixin.qq.com/s/0Mhe1NAZJIewbVy6A0HE4Q

回文子序列
https://mp.weixin.qq.com/s/2SWKifZJ3Gf1s5L2xBDJtg

KMP算法，字符串匹配：
动态规划之 KMP 算法详解(配代码版) 
https://mp.weixin.qq.com/s?__biz=MzUyNjQxNjYyMg==&mid=2247486490&idx=3&sn=35ba410818207a1bef83d6578f4b332c&chksm=fa0e639bcd79ea8dff1141a8729cf4b1243d23ac276652a58fc23d7b6b2ce01ca2666feab293&mpshare=1&scene=1&srcid=&sharer_sharetime=1569055567478&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=20f7b87cb3d4d9a8e94f75ad1bbd1fe8ed4af91513a424bebd0c4df328ea367a462e742f0885a4dbf9693a65560f764378ab2da5e0d620daa8cd627756a8d79b7b364eb9ccf4a8629e46dad4de38545d&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=l152qY7UDy13%2FQ8lMQftZpzwON66UoS8zNnRNqU0gQ1B38kfpkeCoh6I%2F0Cu%2FOwX

字符串匹配的KMP算法 
https://mp.weixin.qq.com/s?__biz=MzIwNTc4NTEwOQ==&mid=2247486950&idx=1&sn=61185c72b270891a0e1aa0db1f9a627f&chksm=972adc9ca05d558a3d5e8a505b29937768e6f47b6c318bc6cc478803d9634e2f4555c26179f9&mpshare=1&scene=1&srcid=&key=00a8e91eefd868fc0218770ad47efc19a0eb1837e26a2fde21a4e6ae367993f51dcfd216c2397954d6a4de33fa5f65dd63b8b620d6e981902b0f9ace3bbbf335784449a15c08450df602e9229d6857de&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060833&lang=zh_CN&pass_ticket=eg5OolRG8y0%2Bw9bavl09Uyc6GPxVmhjvDrWe622XQSg9XG10VZWa9GR31nV6T9cV
-->

