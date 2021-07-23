

<!-- TOC -->

- [1. Java泛型](#1-java泛型)
    - [1.1. 泛型简介](#11-泛型简介)
    - [1.2. 使用方式](#12-使用方式)
        - [1.2.1. 泛型类](#121-泛型类)
        - [1.2.2. 泛型接口](#122-泛型接口)
        - [1.2.3. 泛型方法](#123-泛型方法)
    - [1.3. 范型通配符](#13-范型通配符)
        - [1.3.1. 类型通配符T，E，K，V](#131-类型通配符tekv)
        - [1.3.2. ？无界通配符](#132-无界通配符)
        - [1.3.3. 上界通配符 < ? extends E>](#133-上界通配符---extends-e)
        - [1.3.4. 下界通配符 < ? super E>](#134-下界通配符---super-e)
        - [1.3.5. ？和 T 的区别](#135-和-t-的区别)
        - [1.3.6. Class< T > 和 Class< ? > 区别](#136-class-t--和-class---区别)
    - [1.4. 类型擦除](#14-类型擦除)

<!-- /TOC -->

<!--
利用反射越过泛型检查
https://www.jianshu.com/p/6493fdab6ac5
ava泛型类型擦除以及类型擦除带来的问题
https://www.cnblogs.com/wuqinglong/p/9456193.html

Java泛型类型擦除：何时以及发生了什么？
http://www.cocoachina.com/articles/44971

XXXjava编译成class


https://mp.weixin.qq.com/s/skxnaaPz2eN1YASUlfwMDA
https://mp.weixin.qq.com/s/ilqFpf5kE0XzJnOv9SsX7Q
https://www.cnblogs.com/lihaoyang/p/7104293.html
Java-TypeToken原理及泛型擦除
https://mp.weixin.qq.com/s/oPnJGmw-fNwtgG6ioAJ-ZQ
精通 Java，却不了解泛型？  精通 Java，却不了解泛型？ 
https://mp.weixin.qq.com/s/4QqRkHJ4NeZW9EgR4oOwhA
https://mp.weixin.qq.com/s/kXaXODEZcxSNkALE7Im8Aw
Java泛型的特点与优缺点，泛型擦除是怎么回事？ 
https://mp.weixin.qq.com/s/xW9PC88-OCbGSYI_897dow
Java 中的通配符 T，E，K，V，？，你确定都了解吗？ 
https://mp.weixin.qq.com/s/0AZY4XFO6AOyuihshKYtzQ

面试官又来问：List<String>能否转化为List<Object>? 
https://mp.weixin.qq.com/s/UWeS1F1jCfyBvRlJPazbZA
Java的“泛型”特性，你以为自己会了？
https://mp.weixin.qq.com/s/skxnaaPz2eN1YASUlfwMDA

-->

# 1. Java泛型
## 1.1. 泛型简介
&emsp; 泛型：把类型明确的工作推迟到创建对象或调用方法的时候，才去明确的特殊的类型  

&emsp; 参数化类型:  

* 把类型当作是参数一样传递  
* <数据类型> 只能是引用类型  

## 1.2. 使用方式  
&emsp; 泛型有三种使用方式，分别为：泛型类、泛型接口、泛型方法。  

### 1.2.1. 泛型类  
......

### 1.2.2. 泛型接口  
......

### 1.2.3. 泛型方法  
......

----
## 1.3. 范型通配符  
### 1.3.1. 类型通配符T，E，K，V  

* T (type) 表示具体的一个java类型  
* K V (key value) 分别代表java键值中的Key Value  
* E (element) 代表Element  

### 1.3.2. ？无界通配符  
......

### 1.3.3. 上界通配符 < ? extends E>  
......

### 1.3.4. 下界通配符 < ? super E>  
......

### 1.3.5. ？和 T 的区别  
<!-- 
https://mp.weixin.qq.com/s/YDGfYDWop9lvCWKym66_qA
-->
......

### 1.3.6. Class< T > 和 Class< ? > 区别  
......


## 1.4. 类型擦除  
&emsp; 泛型是通过类型擦除来实现的，<font color = "red">编译器在编译时擦除了所有泛型类型相关的信息，所以在运行时不存在任何泛型类型相关的信息。</font>  
&emsp; 泛型擦除具体来说就是在编译成字节码时首先进行类型检查，接着进行类型擦除（即所有类型参数都用它们的限定类型替换，包括类、变量和方法），接着如果类型擦除和多态性发生冲突时就在子类中生成桥方法解决，接着如果调用泛型方法的返回类型被擦除则在调用该方法时插入强制类型转换。  

