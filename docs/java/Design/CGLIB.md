

<!-- TOC -->

- [1. CGLIB](#1-cglib)
    - [1.1. Cglib是什么](#11-cglib是什么)
    - [1.2. 示例](#12-示例)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. 依赖ASM字节码工具，通过动态生成`实现接口或继承类`的类字节码，实现动态代理。  
&emsp; `针对接口，生成实现接口的类，即implements方式；针对类，生成继承父类的类，即extends方式。`    
2. **<font color = "clime">CGLIB基于类生成动态代理需要注意？</font><font color = "blue">(CGLIB生成的代理是继承类的)</font>**  
    1. final声明的类是不能被代理的；
    2. 类中的private,final方法不能被代理，static方法不生成代理方法。

# 1. CGLIB
<!--

https://mp.weixin.qq.com/s/Am4uccsBFpKFnswSmI3iuA
https://www.cnblogs.com/selfchange/p/9828097.html
https://www.cnblogs.com/xrq730/p/6661692.html
https://blog.csdn.net/xiaohai0504/article/details/6832990
https://blog.csdn.net/danchu/article/details/70238002

-->
&emsp; JDK的动态代理用起来非常简单，当它有一个限制，就是使用动态代理的对象必须实现一个或多个接口。如果想代理没有实现接口的继承的类，该怎么办？可以使用CGLIB包。  

## 1.1. Cglib是什么
&emsp; **<font color = "red">CGLIB是一个强大的高性能的代码生成包。它广泛的被许多AOP的框架使用，例如Spring AOP和dynaop，为它们提供方法的interception（拦截）。</font>**  
&emsp; **<font color = "red">CGLIB包的底层是通过使用一个小而快的字节码处理框架ASM，来转换字节码并生成新的类。</font>**  

&emsp; 依赖ASM字节码工具，通过动态生成实现接口或继承类的类字节码，实现动态代理。  
&emsp; **<font color = "blue">针对接口，生成实现接口的类，即implements方式；针对类，生成继承父类的类，即extends方式。</font>**    

&emsp; Cglib与字节码的关系：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/design/design-24.png)  
&emsp; 对此图总结一下：

* 最底层的是字节码Bytecode，字节码是Java为了保证“一次编译、到处运行”而产生的一种虚拟指令格式，例如iload_0、iconst_1、if_icmpne、dup等  
* 位于字节码之上的是ASM，这是一种直接操作字节码的框架，应用ASM需要对Java字节码、Class结构比较熟悉  
* 位于ASM之上的是CGLIB、Groovy、BeanShell，后两种并不是Java体系中的内容而是脚本语言，它们通过ASM框架生成字节码变相执行Java代码，这说明在JVM中执行程序并不一定非要写Java代码----只要你能生成Java字节码，JVM并不关心字节码的来源，当然通过Java代码生成的JVM字节码是通过编译器直接生成的，算是最“正统”的JVM字节码  
* 位于CGLIB、Groovy、BeanShell之上的就是Hibernate、Spring AOP这些框架了，这一层大家都比较熟悉  
* 最上层的是Applications，即具体应用，一般都是一个Web项目或者本地跑一个程序  

&emsp; **<font color = "clime">CGLIB基于类生成动态代理需要注意？</font><font color = "blue">(CGLIB生成的代理是继承类的)</font>**  
1. final声明的类是不能被代理的；
2. 类中的private,final方法不能被代理，static方法不生成代理方法。

## 1.2. 示例
<!-- 
https://blog.csdn.net/danchu/article/details/70238002
-->