
<!-- TOC -->

- [1. 适配器(Adapter)模式](#1-适配器adapter模式)
    - [1.1. 简介](#11-简介)
    - [1.2. 结构](#12-结构)
    - [1.3. Mybatis日志的适配器](#13-mybatis日志的适配器)

<!-- /TOC -->

# 1. 适配器(Adapter)模式  
<!--
适配器模式在Java日志体系中的应用 
https://mp.weixin.qq.com/s/Tvo0C1ptPMyoNaggCjphuQ
萌新发问：MyBatis日志到底是如何做到兼容所有常用日志框架的？ 
https://mp.weixin.qq.com/s/hUA-GEbRYH0-qgcEKCUylg
适配器模式在Mybatis中的妙用 
https://mp.weixin.qq.com/s/vdwDCk5GY-vL8V8K_TBoTg
-->
## 1.1. 简介
&emsp; 适配器模式(Adapter Pattern)，将一个类的接口转换成客户端期望的另一种接口，从而使原本因接口不匹配而导致无法在一起工作的两个类能够一起工作。  

&emsp; **使用场景：**  
1. <font color = "clime">已经存在的类，它的方法和需求不匹配(方法结果相同或相似)的情况。</font>  
2. 适配器模式不是软件设计阶段考虑的设计模式，是随着软件维护，由于不同产品、不同厂家造成功能类似而接口不相同情况下的解决方案。  


&emsp; **比较形象的例子：**  
1. 用电器做例子，笔记本电脑的插头一般都是三相的，即除了阳极、阴极外，还有一个地极。而有些地方的电源插座却只有两极，没有地极。电源插座与笔记本电脑的电源插头不匹配使得笔记本电脑无法使用。这时候一个三相到两相的转换器（适配器）就能解决此问题，而这正像是本模式所做的事情。  
2. 有一个 MediaPlayer 接口和一个实现了 MediaPlayer 接口的实体类 AudioPlayer。默认情况下，AudioPlayer 可以播放 mp3 格式的音频文件。  
&emsp; 还有另一个接口 AdvancedMediaPlayer 和实现了 AdvancedMediaPlayer 接口的实体类。该类可以播放 vlc 和 mp4 格式的文件。  
&emsp; 想要让AudioPlayer播放其他格式的音频文件。为了实现这个功能，需要创建一个实现了MediaPlayer接口的适配器类MediaAdapter，并使用AdvancedMediaPlayer对象来播放所需的格式。  

&emsp; **优点：**   
1. 能提高类的透明性和复用，现有的类复用但不需要改变。   
2. 目标类和适配器类解耦，提高程序的扩展性。   
3. 在很多业务场景中符合开闭原则。  

&emsp; **缺点：**   
1. 适配器编写过程需要全面考虑，可能会增加系统的复杂性。   
2. 增加代码阅读难度，降低代码可读性，过多使用适配器会使系统代码变得凌乱。  

## 1.2. 结构  
&emsp; **模式角色组成：**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/design/design-13.png)  

* Target，目标接口，即期望转换成的接口。  
* Adaptee，现有类，源角色，它是已经存在的，运行良好的类或对象，经过适配器角色的包装，转换成目标接口。  
* Adapter，适配器类，即实现目标接口Target，又继承Adaptee类。适配器模式的核心角色，其他两个角色都是已经存在的角色，而适配器角色是需要新建立的，它的职责非常简单：把源角色转换为目标角色。转换的方式有：通过继承或者类关联的方式。  

&emsp; **适配器模式有3种形式：类适配器、对象适配器、接口适配器。**  


## 1.3. Mybatis日志的适配器
&emsp; 参考[MyBatis日志体系](/docs/SSM/MyBatis/MybatisLog.md)  
