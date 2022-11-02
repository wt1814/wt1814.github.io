
<!-- TOC -->

- [1. Maven](#1-maven)
    - [1.1. 私服Nexus搭建](#11-私服nexus搭建)
    - [1.2. 统一依赖管理](#12-统一依赖管理)
    - [maven-resource插件](#maven-resource插件)
    - [1.3. Maven冲突](#13-maven冲突)
        - [1.3.1. 背景](#131-背景)
    - [Maven仓库](#maven仓库)
    - [IDEA强制清除Maven缓存](#idea强制清除maven缓存)

<!-- /TOC -->


# 1. Maven
<!-- 

Maven依赖的作用域你到底用对了没有
https://mp.weixin.qq.com/s/PxC-Siyxvt3GIkzh9VKKKg

Maven打包跳过测试的三种方法
https://www.jb51.net/article/199947.htm


Maven optional(可选) 关键字
https://mp.weixin.qq.com/s?__biz=MzkwNzI0MzQ2NQ==&mid=2247488941&idx=1&sn=ed6dc21082b3bd9ea1e807ab1c1d741b&source=41#wechat_redirect

编码gbk的不可映射字符解决办法
https://mp.weixin.qq.com/s?__biz=MzUyOTk5NDQwOA==&mid=2247489673&idx=3&sn=db4ce45b21188ec3461da7d8a3e34107&source=41#wechat_redirect

-->

## 1.1. 私服Nexus搭建  
[Maven私服搭建](/docs/devAndOps/maven/Nexus.md)  


## 1.2. 统一依赖管理  
<!-- 

Spring Cloud 统一的依赖管理（dependencies）
https://zhuanlan.zhihu.com/p/484975073
-->

## maven-resource插件  
<!-- 

https://www.jianshu.com/p/c7f81095397c#comments
https://blog.csdn.net/weixin_30896763/article/details/98988315
-->



## 1.3. Maven冲突

<!-- 

Maven 依赖管理
什么情况下会出现依赖冲突？出现依赖冲突常见的异常有哪些？怎么及时发现项目中的依赖冲突？出现依赖冲突具体要怎么解决？

查看依赖相关：
maven项目重复依赖检测，并解决冲突jar
https://blog.csdn.net/ChinaMuZhe/article/details/80407365
maven项目查看依赖树
https://blog.csdn.net/lzufeng/article/details/96857504
Maven检查存储库中的更新依赖关系？
https://cloud.tencent.com/developer/ask/52155
使用maven命令来分析jar包之间的依赖关系
https://www.cnblogs.com/duoshou/articles/7885630.html

maven解决冲突不仅一种途径。



Java依赖冲突高效解决之道 
https://mp.weixin.qq.com/s/0G5kLzz8Mtwf2hchB8ba7A
***高手解决 Maven Jar 包冲突是有技巧的 
https://mp.weixin.qq.com/s/Eu2SmJKC7LLkk9DnGzyM6w

一次Maven依赖冲突采坑，把依赖调解、类加载彻底整明白了
https://mp.weixin.qq.com/s/svXBS-D-GFlbMar6u9gdsA

解决Maven依赖冲突的好帮手，这款IDEA插件了解一下？ 
https://mp.weixin.qq.com/s/ueK8XgmzdlcH-CsKH8o33A


Maven中的-D（Properties属性）和-P（Profiles配置文件）
https://blog.csdn.net/yy193728/article/details/72847122

-->

### 1.3.1. 背景
&emsp; Jar包冲突在软件开发过程中是不可避免的，因此，如何快速定位冲突源，理解冲突导致的过程及底层原理，是每个程序员的必修课。也是提升工作效率、应对面试、在团队中脱颖而出的机会。  
&emsp; 实践中能够直观感受到的Jar包冲突表现往往有这几种：  

* 程序抛出java.lang.ClassNotFoundException异常；
* 程序抛出java.lang.NoSuchMethodError异常；
* 程序抛出java.lang.NoClassDefFoundError异常；
* 程序抛出java.lang.LinkageError异常等；

&emsp; 这是能够直观呈现的，当然还有隐性的异常，比如程序执行结果与预期不符等。下面，我们就分析一下Maven项目中Jar包的处理机制及引起冲突的原因。  


## Maven仓库
<!-- 

****maven配置多仓库的方法
https://mp.weixin.qq.com/s/_Of9mGw2Lcm-DnJ_10Nz6g
https://www.cnblogs.com/hepengju/p/11610451.html
-->


## IDEA强制清除Maven缓存  
IDEA强制清除Maven缓存
https://www.cnblogs.com/-beyond/p/11557196.html

