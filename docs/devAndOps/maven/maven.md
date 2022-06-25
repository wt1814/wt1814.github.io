
<!-- TOC -->

- [1. Maven](#1-maven)
    - [1.1. 私服Nexus搭建](#11-私服nexus搭建)
    - [1.2. 统一依赖管理](#12-统一依赖管理)
    - [maven-resource插件](#maven-resource插件)
    - [1.3. Maven冲突](#13-maven冲突)
        - [1.3.1. 背景](#131-背景)

<!-- /TOC -->


# 1. Maven
<!-- 



Maven打包跳过测试的三种方法
https://www.jb51.net/article/199947.htm
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
Java依赖冲突高效解决之道 
https://mp.weixin.qq.com/s/0G5kLzz8Mtwf2hchB8ba7A
***高手解决 Maven Jar 包冲突是有技巧的 
https://mp.weixin.qq.com/s/Eu2SmJKC7LLkk9DnGzyM6w

一次Maven依赖冲突采坑，把依赖调解、类加载彻底整明白了
https://mp.weixin.qq.com/s/svXBS-D-GFlbMar6u9gdsA

解决Maven依赖冲突的好帮手，这款IDEA插件了解一下？ 
https://mp.weixin.qq.com/s/ueK8XgmzdlcH-CsKH8o33A
-->

### 1.3.1. 背景
&emsp; Jar包冲突在软件开发过程中是不可避免的，因此，如何快速定位冲突源，理解冲突导致的过程及底层原理，是每个程序员的必修课。也是提升工作效率、应对面试、在团队中脱颖而出的机会。  
&emsp; 实践中能够直观感受到的Jar包冲突表现往往有这几种：  

* 程序抛出java.lang.ClassNotFoundException异常；
* 程序抛出java.lang.NoSuchMethodError异常；
* 程序抛出java.lang.NoClassDefFoundError异常；
* 程序抛出java.lang.LinkageError异常等；

&emsp; 这是能够直观呈现的，当然还有隐性的异常，比如程序执行结果与预期不符等。下面，我们就分析一下Maven项目中Jar包的处理机制及引起冲突的原因。  


