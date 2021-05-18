

<!-- TOC -->

- [1. 动态规划](#1-动态规划)
    - [1.1. 从分治、递归到动态规划](#11-从分治递归到动态规划)
    - [1.2. 动态规划基本概念](#12-动态规划基本概念)
    - [1.3. 动态规划解题](#13-动态规划解题)

<!-- /TOC -->

<!-- 
备忘录方法
https://baike.baidu.com/item/%E5%A4%87%E5%BF%98%E5%BD%95%E6%96%B9%E6%B3%95/6756819?appJump=1&ivk_sa=1022817p
细谈递归，备忘录递归，动态规划，三种算法思想和运行原理
https://blog.csdn.net/qq_39046727/article/details/78966105
备忘录方法与动态规划比较
https://blog.csdn.net/annmi26002/article/details/101975994
动态规划题解（转）
https://www.cnblogs.com/wsw-seu/p/13381312.html
-->

<!-- 
~~
https://mp.weixin.qq.com/s/Lr4T3pYVrk96UK23TQwEsw
-->

# 1. 动态规划
## 1.1. 从分治、递归到动态规划
<!-- 
递归、分治和动态规划的关系
https://blog.csdn.net/BillCYJ/article/details/78765584
https://blog.csdn.net/weixin_39030846/article/details/113781295
https://www.cnblogs.com/codeskiller/p/6477181.html
-->

&emsp; **动态规划与分治：**  
&emsp; 分治策略：将原问题分解为若干个规模较小但类似于原问题的子问题(Divide)，递归的求解这些子问题(Conquer)，然后再合并这些子问题的解来建立原问题的解。  
&emsp; 因为在求解大问题时，需要递归的求小问题，因此一般用递归的方法实现，即自顶向下。  
&emsp; 动态规划：动态规划其实和分治策略是类似的，也是将一个原问题分解为若干个规模较小的子问题，递归的求解这些子问题，然后合并子问题的解得到原问题的解。  
&emsp; <font color = "red">动态规划和分治策略的区别在于这些子问题会有重叠，一个子问题在求解后，可能会再次求解，可以将这些子问题的解存储起来，当下次再次求解这个子问题时，直接取过来用。</font>  

&emsp; **从递归到动态规划：**  
&emsp; 递归采用自顶向下的运算，比如：f(n) 是f(n-1)与f(n-2)相加，f(n-1) 是f(n-2)与f(n-3)相加。  
&emsp; 如果反过来，采取自底向上，用迭代的方式进行推导，则是动态规划。  

&emsp; 动态规划最核心的思想，就在于拆分子问题，记住过往，减少重复计算。 


## 1.2. 动态规划基本概念
&emsp; 动态规划的一般形式就是求最优值，比如最长公共子序列、最大子段和、最优二叉搜索树等等。  
&emsp; 用一句话解释动态规划就是 `“记住你之前做过的事”`，如果更准确些，其实是 `“记住你之前得到的答案”`。  



## 1.3. 动态规划解题  

&emsp; &emsp; [备忘录与动态规划](/docs/functionrandumAndDynamic.md)  
&emsp; &emsp; [动态规划解题](/docs/functionmicSolve.md)  
