
<!-- TOC -->

- [1. 建造者模式](#1-建造者模式)
    - [1.1. 建造者模式与工厂模式的区别](#11-建造者模式与工厂模式的区别)
    - [1.2. 建造者模式的链式写法](#12-建造者模式的链式写法)

<!-- /TOC -->

# 1. 建造者模式
&emsp; 建造者模式将一个复杂的构建与其表示相分离，使得同样的构建过程可以创建不同的表示。建造者返回给客户一个完整的的产品对象，而客户端无须关心该对象所包含的属性和组建方式。  
&emsp; **建造者模式适用于创建对象需要很多步骤，但是步骤的顺序不一定固定。如果一个对象有非常复杂的内部结构（很多属性），可以将复杂对象的创建和使用进行分离。**  

&emsp; **适用场景：**   
1. 相同的方法，不同的执行顺序，产生不同的结果时。  
2. 多个部件或零件，都可以装配到一个对象中，但是产生的结果又不相同。  
3. 产品类非常复杂，或者产品类中的调用顺序不同产生不同的作用。  
4. 当初始化一个对象特别复杂，参数多，而且很多参数都具有默认值时。  

&emsp; **<font color = "red">JDK中的StringBuilder#append()、Spring中BeanDefinitionBuilder#getBeanDefinition()方法获得一个BeanDefinition对象、Mybatis中CacheBuilder#build()获得一个Cache、Mybatis中SqlSessionFactoryBuilder#build()方法获得一个SqlSessionFactory。</font>**  

&emsp; **建造者模式的优点：**  
1. 封装性好，创建和使用分离；  
2. 扩展性好，建造类之间独立、一定程度上解耦。  

&emsp; **建造者模式的缺点：**  
1. 产生多余的Builder对象；  
2. 产品内部发生变化，建造者都需要修改，成本较大。  

&emsp; **模式角色组成：**   
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/design/design-10.png)  

* 产品（Product）角色：要创建的产品类对象。
* 抽象建造者（Builder）角色：建造者的抽象类，规范产品对象的各个组成部分的构造，一般由子类实现具体的构造过程。
* 具体建造者（ConcreteBuilder）角色：具体的Builder类，根据不同的业务逻辑，具体化对象的各个组成部分的创建。
* 导演者（Director）角色：调用具体的建造者，来创建对象的各个部分，在指导者中不涉及具体产品的信息，只负责保证对象各部分完整创建或按某种顺序创建。  

## 1.1. 建造者模式与工厂模式的区别
&emsp; 建造者模式与工厂模式的区别：  
1. 建造者模式更加注重方法的调用顺序，工厂模式注重于创建对象。
2. 创建对象的力度不同，建造者模式创建的对象，由各种复杂的部件组成，工厂模式创建出来的都一样。
3. 关注重点不一样，工厂模式只需要把对象创建出来就可以，而建造者模式中不仅要创建出这个对象，还要知道这个对象由哪些部件组成。
4. 建造者模式根据建造过程中的顺序不一样，最终的对象部件组成也不一样。  

## 1.2. 建造者模式的链式写法  
&emsp; 在日常开发中，可以看到下面这种代码:  

```java
return new Docket(DocumentationType.SWAGGER_2)
        .apiInfo(apiInfo())
        .select()
        .apis(RequestHandlerSelectors.basePackage("com.curry.springbootswagger.controller"))
        .paths(PathSelectors.any())
        .build();
```
&emsp; 这种写法是建造者模式的链式编程。  