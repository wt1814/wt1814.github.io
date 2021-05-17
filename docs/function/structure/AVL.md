

# 平衡二叉树  
<!--
AVL树
https://mp.weixin.qq.com/s/7MJWagl_L-ZFlLtKdJwbFQ

https://mp.weixin.qq.com/s/9no2Ge0hWo1lZHRm_JS0hA


-->
&emsp; 什么是平衡二叉树？  
&emsp; 平衡二叉树(Balanced Binary Tree 或 Height-Balanced Tree)又称为 AVL 树，其实是一颗平衡的二叉排序树 ，解决了二叉排序树的不平衡问题，即斜树。AVL树或者是一颗空树，或者是具有下列性质的二叉排序树：  
&emsp; 它的左子树和右子树都是平衡二叉树，且左子树和右子树的深度之差的绝对值不超过1。  

&emsp; 什么是平衡因子？  
&emsp; 平衡二叉树上结点的 平衡因子BF(Balanced Factor)定义为该结点的左子树深度减去它的右子树的深度，平衡二叉树上所有结点的平衡因子只可能是 -1，0，1。  
