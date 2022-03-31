



<!-- TOC -->autoauto- [1. Java泛型](#1-java泛型)auto    - [1.1. 泛型简介](#11-泛型简介)auto    - [1.2. ★★★为什么要使用泛型？](#12-★★★为什么要使用泛型)auto    - [](#)auto    - [1.3. 使用范型](#13-使用范型)auto        - [1.3.1. 泛型类](#131-泛型类)auto        - [1.3.2. 泛型接口](#132-泛型接口)auto        - [1.3.3. 泛型方法](#133-泛型方法)auto    - [1.4. 范型通配符](#14-范型通配符)auto        - [1.4.1. 类型通配符T，E，K，V](#141-类型通配符tekv)auto        - [1.4.2. ？无界通配符](#142-无界通配符)auto        - [1.4.3. 上界通配符 < ? extends E>](#143-上界通配符---extends-e)auto        - [1.4.4. 下界通配符 < ? super E>](#144-下界通配符---super-e)auto        - [1.4.5. ？和 T 的区别](#145-和-t-的区别)auto        - [1.4.6. Class< T > 和 Class< ? > 区别](#146-class-t--和-class---区别)autoauto<!-- /TOC -->

<!--
Java 中的通配符 T，E，K，V，？，你确定都了解吗？ 
https://mp.weixin.qq.com/s/0AZY4XFO6AOyuihshKYtzQ

Java泛型的特点与优缺点，泛型擦除是怎么回事？ 
https://mp.weixin.qq.com/s/xW9PC88-OCbGSYI_897dow

面试官又来问：List<String>能否转化为List<Object>? 
https://mp.weixin.qq.com/s/UWeS1F1jCfyBvRlJPazbZA

原生态类型与泛型

https://www.jianshu.com/p/c999721756e7
-->


<!-- 

Java的“泛型”特性，你以为自己会了？
https://mp.weixin.qq.com/s/skxnaaPz2eN1YASUlfwMDA

https://www.jianshu.com/p/973bf08bf6ae
-->

1. 为什么使用范型？范型的优点：编译期类型检查。  



# 1. Java泛型
## 1.1. 泛型简介
<!-- 
https://mp.weixin.qq.com/s/ilqFpf5kE0XzJnOv9SsX7Q
-->
&emsp; 泛型：把类型明确的工作推迟到创建对象或调用方法的时候，才去明确的特殊的类型。  


## 1.2. ★★★为什么要使用泛型？泛型和Object的区别？  
<!-- 

Java 泛型优点之编译时类型检查
https://developer.aliyun.com/article/617432

https://mp.weixin.qq.com/s/skxnaaPz2eN1YASUlfwMDA
https://mp.weixin.qq.com/s/4QqRkHJ4NeZW9EgR4oOwhA
https://mp.weixin.qq.com/s/kXaXODEZcxSNkALE7Im8Aw

一分钟搞懂泛型和Object的区别？
https://wenku.baidu.com/view/8d1697e70f22590102020740be1e650e52eacf6b.html

https://www.cnblogs.com/xiaoxiong2015/p/12705815.html
-->
&emsp; 在Java SE 1.5之前，没有泛型的情况的下，通过对类型Object的引用来实现参数的“任意化”，“任意化”带来的缺点是要做显式的强制类型转换，而这种转换是要求开发者对实际参数类型可以预知的情况下进行的。对于强制类型转换错误的情况，编译器可能不提示错误，在运行的时候才出现异常，这是一个安全隐患。  
&emsp; 泛型和Object除了语法之外没有什么别的区别，不过为什么jdk1.5以后要出这个泛型；  
&emsp; 使用泛型最大的好处：不再需要强制类型转换，编译时自动检查类型安全，避免隐性的类型转换异常。  


## 1.3. 使用范型  
<!-- 
https://mp.weixin.qq.com/s/skxnaaPz2eN1YASUlfwMDA
https://mp.weixin.qq.com/s/kXaXODEZcxSNkALE7Im8Aw
https://mp.weixin.qq.com/s/4QqRkHJ4NeZW9EgR4oOwhA
https://www.cnblogs.com/lihaoyang/p/7104293.html
-->
&emsp; 泛型有三种使用方式，分别为：泛型类、泛型接口、泛型方法。  

### 1.3.1. 泛型类  
&emsp; ......

### 1.3.2. 泛型接口  
&emsp; ......

### 1.3.3. 泛型方法  
&emsp; ......

----
## 1.4. 范型通配符  
<!-- 
https://mp.weixin.qq.com/s/kXaXODEZcxSNkALE7Im8Aw
https://mp.weixin.qq.com/s/4QqRkHJ4NeZW9EgR4oOwhA
-->

### 1.4.1. 类型通配符T，E，K，V  

* T (type) 表示具体的一个java类型  
* K V (key value) 分别代表java键值中的Key Value  
* E (element) 代表Element  

### 1.4.2. ？无界通配符  
&emsp; ......

### 1.4.3. 上界通配符 < ? extends E>  
&emsp; ......

### 1.4.4. 下界通配符 < ? super E>  
&emsp; ......

### 1.4.5. ？和 T 的区别  
<!-- 
https://mp.weixin.qq.com/s/YDGfYDWop9lvCWKym66_qA
-->
&emsp; ......

### 1.4.6. Class< T > 和 Class< ? > 区别  
&emsp; ......
