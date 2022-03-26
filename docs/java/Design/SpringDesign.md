
<!-- TOC -->

- [1. Spring中经典的9种设计模式](#1-spring中经典的9种设计模式)
    - [1.1. 工厂设计模式](#11-工厂设计模式)
    - [1.2. 单例设计模式](#12-单例设计模式)
    - [1.3. 模板方法](#13-模板方法)
    - [1.4. 观察者](#14-观察者)
    - [1.5. 代理设计模式](#15-代理设计模式)
    - [1.6. 适配器模式](#16-适配器模式)
        - [1.6.1. Spring AOP中的适配器模式](#161-spring-aop中的适配器模式)
        - [1.6.2. Spring MVC中的适配器模式](#162-spring-mvc中的适配器模式)
    - [1.7. 装饰者模式](#17-装饰者模式)
    - [1.8. 小结](#18-小结)

<!-- /TOC -->




# 1. Spring中经典的9种设计模式
<!--

谈谈Spring中都用到了哪些设计模式？
https://www.cnblogs.com/kyoner/p/10949246.html

Spring
https://zhuanlan.zhihu.com/p/114244039
-->

1. 简单工厂(非23种设计模式中的一种)
2. 工厂方法
3. 单例模式
4. 适配器模式
5. 装饰器模式
6. 代理模式
7. 观察者模式
8. 策略模式
9. 模版方法模式


## 1.1. 工厂设计模式
&emsp; Spring使用工厂模式可以通过 BeanFactory 或 ApplicationContext 创建 bean 对象。  


## 1.2. 单例设计模式  
&emsp; Spring中bean的默认作用域就是singleton(单例)的。  

&emsp; Spring通过ConcurrentHashMap实现单例注册表的特殊方式实现单例模式。Spring实现单例的核心代码如下：  

```java
// 通过 ConcurrentHashMap（线程安全） 实现单例注册表
private final Map<String, Object> singletonObjects = new ConcurrentHashMap<String, Object>(64);

public Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
        Assert.notNull(beanName, "'beanName' must not be null");
        synchronized (this.singletonObjects) {
            // 检查缓存中是否存在实例  
            Object singletonObject = this.singletonObjects.get(beanName);
            if (singletonObject == null) {
                //...省略了很多代码
                try {
                    singletonObject = singletonFactory.getObject();
                }
                //...省略了很多代码
                // 如果实例对象在不存在，我们注册到单例注册表中。
                addSingleton(beanName, singletonObject);
            }
            return (singletonObject != NULL_OBJECT ? singletonObject : null);
        }
    }
    //将对象添加到单例注册表
    protected void addSingleton(String beanName, Object singletonObject) {
            synchronized (this.singletonObjects) {
                this.singletonObjects.put(beanName, (singletonObject != null ? singletonObject : NULL_OBJECT));

            }
        }
}
```

## 1.3. 模板方法  
Spring 中 jdbcTemplate、hibernateTemplate 等以 Template 结尾的对数据库操作的类，它们就使用到了模板模式。一般情况下，我们都是使用继承的方式来实现模板模式，但是 Spring 并没有使用这种方式，而是使用Callback 模式与模板方法模式配合，既达到了代码复用的效果，同时增加了灵活性。  

## 1.4. 观察者
Spring 事件驱动模型就是观察者模式很经典的一个应用。  



## 1.5. 代理设计模式
&emsp; Spring AOP就是基于动态代理的。    


## 1.6. 适配器模式  
<!-- 
适配器模式
https://www.cnblogs.com/kyoner/p/10949246.html
-->
&emsp; 适配器模式(Adapter Pattern) 将一个接口转换成客户希望的另一个接口，适配器模式使接口不兼容的那些类可以一起工作，其别名为包装器(Wrapper)。  

### 1.6.1. Spring AOP中的适配器模式
&emsp; Spring AOP的实现是基于代理模式，但是 Spring AOP 的增强或通知(Advice)使用到了适配器模式，与之相关的接口是AdvisorAdapter 。Advice 常用的类型有：BeforeAdvice（目标方法调用前,前置通知）、AfterAdvice（目标方法调用后,后置通知）、AfterReturningAdvice(目标方法执行结束后，return之前)等等。每个类型Advice（通知）都有对应的拦截器:MethodBeforeAdviceInterceptor、AfterReturningAdviceAdapter、AfterReturningAdviceInterceptor。Spring预定义的通知要通过对应的适配器，适配成 MethodInterceptor接口(方法拦截器)类型的对象（如：MethodBeforeAdviceInterceptor 负责适配 MethodBeforeAdvice）。  

### 1.6.2. Spring MVC中的适配器模式
&emsp; 在Spring MVC中，DispatcherServlet根据请求信息调用HandlerMapping，解析请求对应的Handler。解析到对应的Handler（也就是平常说的 Controller控制器）后，开始由HandlerAdapter适配器处理。HandlerAdapter作为期望接口，具体的适配器实现类用于对目标类进行适配，Controller 作为需要适配的类。   
&emsp; 为什么要在 Spring MVC 中使用适配器模式？ Spring MVC 中的 Controller 种类众多，不同类型的 Controller 通过不同的方法来对请求进行处理。如果不利用适配器模式的话，DispatcherServlet 直接获取对应类型的 Controller，需要的自行来判断，像下面这段代码一样：  

```java
if(mappedHandler.getHandler() instanceof MultiActionController){  
   ((MultiActionController)mappedHandler.getHandler()).xxx  
}else if(mappedHandler.getHandler() instanceof XXX){  
    //...  
}else if(...){  
   //...  
}  
```

## 1.7. 装饰者模式  
&emsp; 装饰者模式可以动态地给对象添加一些额外的属性或行为。相比于使用继承，装饰者模式更加灵活。简单点儿说就是当我们需要修改原有的功能，但我们又不愿直接去修改原有的代码时，设计一个Decorator套在原有代码外面。其实在 JDK 中就有很多地方用到了装饰者模式，比如 InputStream家族，InputStream 类下有 FileInputStream (读取文件)、BufferedInputStream (增加缓存,使读取文件速度大大提升)等子类都在不修改InputStream 代码的情况下扩展了它的功能。  
![image](http://www.wt1814.com/static/view/images/java/design/design-28.png)  
​<center>装饰者模式示意图</center>

&emsp; Spring中配置DataSource的时候，DataSource 可能是不同的数据库和数据源。我们能否根据客户的需求在少修改原有类的代码下动态切换不同的数据源？这个时候就要用到装饰者模式(这一点我自己还没太理解具体原理)。Spring 中用到的包装器模式在类名上含有 Wrapper或者 Decorator。这些类基本上都是动态地给一个对象添加一些额外的职责。  

## 1.8. 小结
&emsp; Spring框架中用到了哪些设计模式：  

* 工厂设计模式 : Spring使用工厂模式通过 BeanFactory、ApplicationContext 创建 bean 对象。  
* 单例设计模式 : Spring 中的 Bean 默认都是单例的。  
* 模板方法模式 : Spring 中 jdbcTemplate、hibernateTemplate 等以 Template 结尾的对数据库操作的类，它们就使用到了模板模式。  
* 观察者模式: Spring事件驱动模型就是观察者模式很经典的一个应用。  
* 代理设计模式 : Spring AOP 功能的实现。  
* 适配器模式 :Spring AOP 的增强或通知(Advice)使用到了适配器模式、spring MVC 中也是用到了适配器模式适配Controller。  
* 包装器设计模式 : 项目需要连接多个数据库，而且不同的客户在每次访问中根据需要会去访问不同的数据库。这种模式可以根据客户的需求能够动态切换不同的数据源。 
* ……


