


# 装饰（Decorator）模式  
<!-- 
https://mp.weixin.qq.com/s?__biz=MzUzODU4MjE2MQ==&mid=2247484592&idx=1&sn=42a14aef3707745f570488be2fe77326&chksm=fad4c94ecda34058648b394acca654520e97f080aa156fb281c26eaad6f910c55e67b3063364&scene=21#wechat_redirect
-->
&emsp; 装饰者模式（Decorator Pattern）是指在不改变原有对象的基础之上，将功能附加到对象上， **<font color = "red">提供了比继承更有弹性的替代方案（扩展原有对象的功能）。</font>**  
&emsp; <font color = "red">装饰器模式的核心是功能拓展。使用装饰器模式可以透明且动态地扩展类的功能。</font>  

&emsp; **使用场景：**  
1. 用于扩展一个类的功能或给一个类添加附加职责。
2. 动态的给一个对象添加功能，这些功能可以再动态的撤销。
3. 需要为一批兄弟类进行改装或添加功能。  

&emsp; **优点：**  
1. 装饰器是继承的有力补充，比继承灵活，不改变原有对象的情况下动态地给一个对象扩展功能，即插即用。  
2. 通过使用不同装饰类以及这些装饰类的排列组合，可以实现不同效果。  
3. 装饰器完全遵守开闭规则。  

&emsp; **缺点：**  
1. 会出现更多的代码，更多的类，增加程序复杂性。  
2. 动态装饰时，多层装饰时会更复杂。  

&emsp; **模式角色组成：**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/design/design-11.png)  

* Component，抽象构件角色，是一个abstract class，组件对象的接口，可以给这些对象动态的添加职责。  
* ConcreteComponent，具体的处理类，用于实现operation方法。具体的组件对象，实现了组件接口。该对象通常就是被装饰器装饰的原始对象，可以给这个对象添加职责。  
* Decorator，所有装饰器的父类，是一个abstract class，需要定义一个与组件接口一致的接口(主要是为了实现装饰器功能的复用，即具体的装饰器A可以装饰另外一个具体的装饰器B，因为装饰器类也是一个Component)，并持有一个Component对象，该对象其实就是被装饰的对象。如果不继承组件接口类，则只能为某个组件添加单一的功能，即装饰器对象不能在装饰其他的装饰器对象。  
* ConcreteDecorator，具体的装饰器类，实现具体要向被装饰对象添加的功能。用来装饰具体的组件对象或者另外一个具体的装饰器对象。  

## 在JDK中的应用，IO流  
&emsp; java IO流是典型的装饰器模式。  
&emsp; FilterInputStream，FilterOutputStream，FilterRead，FilterWriter分别为具体装饰器的父类，相当于Decorator类，它们分别实现了InputStream，OutputStream，Reader，Writer类(这些类相当于Component，是其他组件类的父类，也是Decorator类的父类)。继承自InputStream，OutputStream，Reader，Writer这四个类的其他类是具体的组件类，每个都有相应的功能，相当于ConcreteComponent类。而继承自FilterInputStream，FilterOutputStream，FilterRead，FilterWriter这四个类的其他类就是具体的装饰器对象类，即ConcreteDecorator类。通过这些装饰器类，可以提供更加具体的有用的功能。如FileInputStream是InputStream的一个子类，从文件中读取数据流，BufferedInputStream是继承自FilterInputStream的具体的装饰器类，该类提供一个内存的缓冲区类保存输入流中的数据。使用如下的代码来使用BufferedInputStream装饰FileInputStream，就可以提供一个内存缓冲区来保存从文件中读取的输入流。  

