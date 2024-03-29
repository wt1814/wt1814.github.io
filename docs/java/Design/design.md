
<!-- TOC -->

- [1. ~~设计模式~~](#1-设计模式)
    - [1.1. 如何判断哪里需要使用设计模式](#11-如何判断哪里需要使用设计模式)
    - [1.2. ~~分类~~](#12-分类)
    - [1.3. 设计模式详解](#13-设计模式详解)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. 常用设计模式有23种（不包含简单工厂模式）。 **<font color = "red">这23种设计模式的本质是面向对象设计原则的实际运用，是对类的封装性、继承性和多态性，以及类的关联关系和组合关系的充分理解。</font>**  
2.  **<font color = "red">结构型：多个类协同完成一个功能；行为型，算法模式。</font>**  



# 1. ~~设计模式~~

<!-- 
Java设计模式
https://mp.weixin.qq.com/s/u6FNbplobMff4TawPHgRjQ
可参考《设计模式 - 可复用面向对象软件的基础(高清版)》
结构可参考《Java与模式》
https://mp.weixin.qq.com/mp/appmsgalbum?__biz=MzA4MTk3MjI0Mw==&action=getalbum&album_id=1612455701786787847&scene=173&from_msgid=2247491293&from_itemidx=1&count=3#wechat_redirect

https://mp.weixin.qq.com/s/ewnxn3xIZNNIX84_kQ2qmg

公众号：《程序员涨薪基地》  https://mp.weixin.qq.com/s/jzPjOGhYLFVWUYIAD33lYA


23种设计模式及案例整理分享（建议收藏） 
https://mp.weixin.qq.com/s?__biz=MzI5ODI5NDkxMw==&mid=2247491308&idx=4&sn=6edbebb4445b4c5a9888983cdb41fc39&chksm=eca95502dbdedc14b0970bb520820f7b10af3f32383dd8551d9c9c4868f56d369003d09d8056&mpshare=1&scene=1&srcid=&sharer_sharetime=1575981916198&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=230ae5fe1ae034b7e2354455561be8ef54697b8d2224a141b4ac88cdf58959b11c6997a074d8c1936293e93278d527a9cd13149e3e85519e443d109e63d5be41f6ad76e2ba49a9619ee57437359b9f8b&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070158&lang=zh_CN&exportkey=AaIEdrg1ul3UAnIot7PvfIo%3D&pass_ticket=qZMK6abzstlbYjCf50eKgKnEyRFUSBrv1OZqpLAUyEY5YyVEDZS49ms3CGuq5S2j

-->
<!-- 
模板模式：一种体现多态的设计模式
https://mp.weixin.qq.com/s/EnkvEIVTLzOcuVd8s8fJTQ

-->
<!-- 
★★★
https://refactoringguru.cn/design-patterns/catalog
-->
&emsp; **<font color = "blue">有关设计模式可参考网址：[refactoringguru.cn](https://refactoringguru.cn/design-patterns/catalog)</font>**  

&emsp; 常用设计模式有23种(不包含简单工厂模式)。 **<font color = "red">这23种设计模式的本质是面向对象设计原则的实际运用，是对类的封装性、继承性和多态性，以及类的关联关系和组合关系的充分理解。</font>**  

&emsp; 定义、使用场景、优缺点、模式角色、示例、与其他模式  


## 1.1. 如何判断哪里需要使用设计模式  
![image](http://182.92.69.8:8081/img/java/design/design-1.png)  

&emsp; 在编码实现中，有一些代码是一次写好后续基本不会改变的，或者不太需要扩展的，比如一些工具类等。有一部分是会经常变得，设计模式大多都应用在需求会变化的这一部分。分析这些代码会如何变，选择合适的设计模式来优化这部分代码。  

## 1.2. ~~分类~~  
![image](http://182.92.69.8:8081/img/java/design/design-22.png)  

&emsp; 根据两条准则对模式进行分类：   
* 第一是目的准则，即模式是用来完成什么工作的。模式依据其目的可分为创建型（Creational）、结构型（Structural）、或行为型（Behavioral）三种。  
&emsp; 创建型模式与对象的创建有关；结构型模式处理类或对象的组合；行为型模式对类或对象怎样交互和怎样分配职责进行描述。  
&emsp; 创建型类模式将对象的部分创建工作延迟到子类，而创建型对象模式则将它延迟到另一个对象中。结构型类模式使用继承机制来组合类，而结构型对象模式则描述了对象的组装方式。行为型类模式使用继承描述算法和控制流，而行为型对象模式则描述一组对象怎样协作完成单个对象所无法完成的任务。  

<!-- 
&emsp; 创建型模式，共5种：创建型模式的主要关注点是“怎样创建对象？”，它的主要特点是“将对象的创建与使用分离”。这样可以降低系统的耦合度，使用者不需要关注对象的创建细节，对象的创建由相关的工厂来完成。  
&emsp; 结构型模式，共7种：结构型模式描述如何将类或对象按某种布局组成更大的结构。  
&emsp; 行为型模式，共11种：行为型模式用于描述程序在运行时复杂的流程控制，即描述多个类或对象之间怎样相互协作共同完成单个对象都无法单独完成的任务，它涉及算法与对象间职责的分配。关注对象之间的通信。  
&emsp; 创建型模式，这一类设计模式的目的是用于创建对象。  
&emsp; 在软件工程中，创建型模式是处理对象创建的设计模式，试图根据实际情况使用合适的方式创建对象。基本的对象创建方式可能会导致设计上的问题，或增加设计的复杂度。创建型模式通过以某种方式控制对象的创建来解决问题。创建型模式由两个主导思想构成。一是将系统使用的具体类封装起来，二是隐藏这些具体类的实例创建和结合的方式。创建型模式又分为对象创建型模式和类创建型模式。对象创建型模式处理对象的创建，类创建型模式处理类的创建。详细地说，对象创建型模式把对象创建的一部分推迟到另一个对象中，而类创建型模式将它对象的创建推迟到子类中。    
&emsp; **结构型模式，这一类设计模式的目的是优化不同类、对象、接口之间的结构关系。**通过组合类或对象产生更大结构以适应更高层次的逻辑需求。   
&emsp; 结构型模式涉及到如何组合类和对象以获得更大的结构。结构型模式采用继承机制来组合接口或实现。结构型对象模式不是对接口和实现进行组合，而是描述了如何对一些对象进行组合，从而实现新功能的一些方法。因为可以在运行时刻改变对象组合关系，所以对象组合方式具有更大的灵活性。  
&emsp; 行为型模式，这一类设计模式的目的是更好地实现类与类之间的交互以及算法的执行。  
&emsp; 行为型模式主要是用于描述类或者对象是怎样交互和怎样分配职责的。它涉及到算法和对象间的职责分配，不仅描述对象或者类的模式，还描述了它们之间的通信方式，它将注意力从控制流转移到了对象间的关系上来。行为型类模式采用继承机制在类间分派行为，而行为型对象模式使用对象复合而不是继承。  
-->

* 第二是范围准则，指定模式主要是用于类还是用于对象。类模式处理类和子类之间的关系，这些关系通过继承建立，是静态的，在编译时刻便确定下来了。对象模式处理对象间的关系，这些关系在运行时刻是可以变化的，更具动态性。从某种意义上来说，几乎所有模式都使用继承机制，所以"类模式"只指那些集中于处理类间关系的模式，而大部分模式都属于对象模式的范畴。  

&emsp; 还有其他组织模式的方式。有些模式经常会被绑在一起使用，例如，Composite常和Iterator或Visitor—起使用；有些模式是可替代的，例如，Prototype常用来替代Abstract Factory有些模式尽管使用意图不同，但产生的设计结果是很相似的，例如，Composite和Decorator的结构图是相似的。  

&emsp; 还有一种方式是根据模式的"相关模式"部分所描述的它们怎样互相引用来组织设计模式。下图给出了模式关系的图形说明。显然，存在着许多组织设计模式的方法。从多角度去思考模式有助于对它们的功能、差异和应用场合的更深入理解。   
![image](http://182.92.69.8:8081/img/java/design/design-21.png)  

<!--
&emsp; 根据模式是主要用于类上还是主要用于对象上来分，这种方式可分为类模式和对象模式两种。  

* <font color = "red">类模式：用于处理类与子类之间的关系，这些关系通过继承来建立，是静态的，在编译时刻便确定下来了。</font>GoF中的工厂方法、(类)适配器、模板方法、解释器属于该模式。  
* <font color = "red">对象模式：用于处理对象之间的关系，这些关系可以通过组合或聚合来实现，在运行时刻是可以变化的，更具动态性。</font>GoF中除了以上4种，其他的都是对象模式。  
&emsp; 注：适配器模式分为类结构型模式和对象结构型模式两种。  

&emsp; 创建型类模式将对象的部分创建工作延迟到子类，而创建型对象模式则将它延迟到另一个对象中。结构型类模式使用继承机制来组合类，而结构型对象模式则描述了对象的组装方式。行为型类模式使用继承描述算法和控制流，而行为型对象模式则描述一组对象怎样协作 完成单个对象所无法完成的任务。  
-->

---------------------

## 1.3. 设计模式详解  
&emsp; 根据模式是用来完成什么工作来划分，这种方式可分为创建型模式、结构型模式和行为型模式3种。  
![image](http://182.92.69.8:8081/img/java/design/design-2.png)  

&emsp; **创建型模式有5种：**  

* [简单工厂模式 /工厂(FactoryMethod)模式](/docs/java/Design/factory.md)：让子类来决定要创建哪个对象。  
* [抽象工厂(AbstractFactory)模式](/docs/java/Design/AbstractFactory.md)：创建多个产品族中的产品对象。  
* [单例(Singleton)模式](/docs/java/Design/singleton.md)：确保某一个类只有一个实例，并且提供一个全局访问点。  
* [建造者(Builder)模式](/docs/java/Design/build.md)：用来创建复杂的复合对象。  
* [原型(Prototype)模式](/docs/java/Design/prototype.md)：通过复制原型来创建新对象。  


&emsp; **结构型模式有7种：**   

* [外观/门面(Facade)模式](/docs/java/Design/facade.md)：对外提供一个统一的接口用来访问子系统。  
* [适配器(Adapter)模式](/docs/java/Design/adapter.md)：将原来不兼容的两个类融合在一起。  
* [代理(Proxy)模式](/docs/java/Design/proxy.md)：**<font color = "clime">控制客户端对对象的访问。</font>**  
    * [JDK动态代理](/docs/java/Design/DynamicProxy.md)   
    * [CGLIB代理](/docs/java/Design/CGLIB.md)   
* [装饰者(Decorator)模式](/docs/java/Design/decorator.md)：**<font color = "red">为对象添加新功能。</font>** 
* 桥接(Bridge)模式：将两个能够独立变化的部分分离开来。  
* 组合(Composite)模式：将整体与局部(树形结构)进行递归组合，让客户端能够以一种的方式对其进行处理。  
* 享元(Flyweight)模式： **使用对象池来减少重复对象的创建。**  


&emsp; **行为型模式有11种：**  

* [策略(Strategy)模式](/docs/java/Design/strategy.md)：封装不同的算法，算法之间能互相替换。  
* [观察者(Observer)模式](/docs/java/Design/observer.md)：状态发生改变时通知观察者，一对多的关系。  
* [模板方法(Template Method)模式](/docs/java/Design/template.md)：定义一套流程模板，根据需要实现模板中的操作。  
* [职责链(Chain of Responsibility)模式](/docs/java/Design/chain.md)：将事件沿着链去处理。  
* 命令(Command)模式：将请求封装成命令，并记录下来，能够撤销与重做。  
* 解释器(Interpreter)模式：定义语法，并对其进行解释。  
* 迭代器(Iterator)模式：提供一种方法顺序访问一个聚合对象中的各个元素。  
* 中介者(Mediator)模式：将网状结构转变为星型结构，所有行为都通过中介。  
* 备忘录(Memento)模式：保存对象的状态，在需要时进行恢复。  
* 状态(State)模式：根据不同的状态做出不同的行为。  
* 访问者(Visitor)模式：基于稳定数据结构，定义新的操作行为。  

