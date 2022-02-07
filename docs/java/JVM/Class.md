

# 字节码文件  
<!-- 
类编译
https://mp.weixin.qq.com/s/ogAm51tRmEJZgyDjcaka2g

编译
https://zhuanlan.zhihu.com/p/39536807
https://mp.weixin.qq.com/s/36GeZelS5GU_PKZimes10g

认识JVM和字节码文件 
https://mp.weixin.qq.com/s/2g1-YZXRrzBsD1QaKGnnNQ
https://mp.weixin.qq.com/s/z0BmJz6dk9VNHalicgN2rg

从JVM设计者的角度来看.class文件结构，一文弄懂.class文件的身份地位 
https://mp.weixin.qq.com/s/BVoqaDn7HUNtdB5vZa0zug
 手把手教你 javap 反编译分解代码，授人以鱼不如授人以渔 
https://mp.weixin.qq.com/s/o0MFrSVS7fYLDo6UnfmA-A

https://mp.weixin.qq.com/s/AnRtCQDIkKgIdhKTk2sWeA

2W 字的Java class类文件结构详解！
https://mp.weixin.qq.com/s/RndRd9apu3tXEDGBZF7EbQ
-->
&emsp; 根据Java虚拟机规范，类文件由单个 ClassFile 结构组成：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-91.png)  
&emsp; Class文件字节码结构组织示意图  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JVM/JVM-90.png)  
&emsp; 下面会按照上图结构按顺序详细介绍一下 Class 文件结构涉及到的一些组件。  
1. 魔数: 确定这个文件是否为一个能被虚拟机接收的 Class 文件。
2. Class 文件版本：Class 文件的版本号，保证编译正常执行。  
3. 常量池：常量池主要存放两大常量：字面量和符号引用。  
4. 访问标志：标志用于识别一些类或者接口层次的访问信息，包括：这个 Class 是类还是接口，是否为 public 或者 abstract 类型，如果是类的话是否声明为 final 等等。  
5. 当前类索引，父类索引：类索引用于确定这个类的全限定名，父类索引用于确定这个类的父类的全限定名，由于 Java 语言的单继承，所以父类索引只有一个，除了
java.lang.Object之外，所有的 java 类都有父类，因此除了
java.lang.Object 外，所有 Java 类的父类索引都不为 0。  
6. 接口索引集合：接口索引集合用来描述这个类实现了那些接口，这些被实现的接口将按implents(如果这个类本身是接口的话则是extends) 后的接口顺序从左到右排列在接口索引集合中。  
7. 字段表集合：描述接口或类中声明的变量。字段包括类级变量以及实例变量，但不包括在方法内部声明的局部变量。  
8. 方法表集合：类中的方法。 
9. 属性表集合：在 Class 文件，字段表，方法表中都可以携带自己的属性表集合。
