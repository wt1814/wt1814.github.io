

<!-- TOC -->

- [1. SpringAOP](#1-springaop)
    - [1.1. SpringAOP编码](#11-springaop编码)
        - [1.1.1. SpringBoot整合AOP](#111-springboot整合aop)
        - [1.1.2. 切点Execution表达式详解](#112-切点execution表达式详解)
            - [1.1.2.1. 通配符](#1121-通配符)
            - [1.1.2.2. 分类](#1122-分类)
        - [1.1.3. AOP切面传参](#113-aop切面传参)
            - [1.1.3.1. 具体形参类型](#1131-具体形参类型)
            - [1.1.3.2. 不定形参类型](#1132-不定形参类型)
    - [1.2. ★★★SpringAOP使用](#12-★★★springaop使用)
        - [1.2.1. 切面和自定义注解配合使用](#121-切面和自定义注解配合使用)
        - [1.2.2. 实战：记录操作日志、异常日志](#122-实战记录操作日志异常日志)
    - [1.3. ★★★SpringAOP失效](#13-★★★springaop失效)
        - [1.3.1. 解决方案](#131-解决方案)
            - [1.3.1.1. 通过ApplicationContext来获得动态代理对象](#1311-通过applicationcontext来获得动态代理对象)

<!-- /TOC -->


&emsp; **<font color = "red">总结：</font>**
1. SpringAOP的主要功能是：日志记录，性能统计，安全控制，事务处理，异常处理等。 
    * 慢请求记录  
    * 使用aop + redis + Lua接口限流

2. **SpringAOP失效：**  
&emsp; <font color = "red">同一对象内部方法嵌套调用，慎用this来调用被@Async、@Transactional、@Cacheable等注解标注的方法，this下注解可能不生效。</font>async方法中的this不是动态代理的子类对象，而是原始的对象，故this调用无法通过动态代理来增强。 


# 1. SpringAOP  

<!-- 

又被逼着优化代码，这次我干掉了出入参 Log日志 
https://mp.weixin.qq.com/s/hZ7KiFyeDMRCPUoNlCXO6w
-->
<!-- 
~~
Spring AOP 设计思想与原理详解 
https://mp.weixin.qq.com/s/3Elf_HQXXl1tObq3HRlrfA
-->


## 1.1. SpringAOP编码  
### 1.1.1. SpringBoot整合AOP  
.......

### 1.1.2. 切点Execution表达式详解  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/AOP/AOP-3.png)  

#### 1.1.2.1. 通配符  

|通配符	|定义|
|---|---|
|..	|该通配符匹配方法定义中的任何数量的参数，此外还匹配类定义中任何数量的包|
|+	|该通配符匹配给定类的任何子类|
|*	|该通配符匹配任何数量的字符|

#### 1.1.2.2. 分类  
1. 通过方法签名定义切点：  
&emsp; execution(public * \*(..))，匹配所有目标类的public方法，但不匹配SmartSeller和protected void showGoods()方法。第一个\*代表返回类型，第二个\*代表方法名，而..代表任意入参的方法；    
&emsp; execution(* \*To(..))，匹配目标类所有以To为后缀的方法。它匹配NaiveWaiter和NaughtyWaiter的greetTo()和serveTo()方法。第一个\*代表返回类型，而*To代表任意以To为后缀的方法；  
2. 通过类定义切点：  
&emsp; execution(* com.baobaotao.Waiter.\*(..))，匹配Waiter接口的所有方法，它匹配NaiveWaiter和NaughtyWaiter类的greetTo()和serveTo()方法。第一个\*代表返回任意类型，com.baobaotao.Waiter.\*代表Waiter接口中的所有方法；  
&emsp; execution(\* com.baobaotao.Waiter+.*(..))，匹配Waiter接口及其所有实现类的方法，它不但匹配NaiveWaiter和NaughtyWaiter类的greetTo()和serveTo()这两个Waiter接口定义的方法，同时还匹配NaiveWaiter#smile()和NaughtyWaiter#joke()这两个不在Waiter接口中定义的方法。  
3. 通过类包定义切点：  
&emsp; 在类名模式串中，“.\*”表示包下的所有类，而“..\*”表示包、子孙包下的所有类。  
&emsp; execution(\* com.baobaotao.\*(..))，匹配com.baobaotao包下所有类的所有方法；  
&emsp; execution(\* com.baobaotao..(..))，匹配com.baobaotao包、子孙包下所有类的所有方法，如com.baobaotao.dao，com.baobaotao.servier以及 com.baobaotao.dao.user包下的所有类的所有方法都匹配。“..”出现在类名中时，后面必须跟“\*”，表示包、子孙包下的所有类；  
&emsp; execution(\* com..*.\*Dao.find*(..))，匹配包名前缀为com的任何包下类名后缀为Dao的方法，方法名必须以find为前缀。如com.baobaotao.UserDao#findByUserId()、com.baobaotao.dao.ForumDao#findById()的方法都匹配切点。  
4. 通过方法入参定义切点：  
&emsp; 切点表达式中方法入参部分比较复杂，可以使用“\*”和“ ..”通配符，其中“\*”表示任意类型的参数，而“..”表示任意类型参数且参数个数不限。  
&emsp;execution(* joke(String,int))),匹 配joke(String,int)方法，且joke()方法的第一个入参是String，第二个入参是int。它匹配 NaughtyWaiter#joke(String,int)方法。如果方法中的入参类型是java.lang包下的类，可以直接使用类名，否则必须使用全限定类名，如joke(java.util.List,int)；  
&emsp; execution(* joke(String,\*)))，匹配目标类中的joke()方法，该方法第一个入参为String，第二个入参可以是任意类型，如joke(String s1,String s2)和joke(String s1,double d2)都匹配，但joke(String s1,double d2,String s3)则不匹配；  
&emsp; execution(\* joke(String,..)))，匹配目标类中的joke()方法，该方法第一个入参为String，后面可以有任意个入参且入参类型不限，如joke(String s1)、joke(String s1,String s2)和joke(String s1,double d2,String s3)都匹配。  
&emsp; execution(\* joke(Object+)))，匹配目标类中的joke()方法，方法拥有一个入参，且入参是Object类型或该类的子类。它匹配joke(String s1)和joke(Client c)。如果定义的切点是execution(* joke(Object))，则只匹配joke(Object object)而不匹配joke(String cc)或joke(Client c)。  

&emsp; 在多个表达式之间使用||、or表示或；&&、and表示与；！表示非。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/AOP/AOP-4.png)  

### 1.1.3. AOP切面传参  
#### 1.1.3.1. 具体形参类型  
&emsp; 业务方法、Execution表达式、AOP方法3者形参类型一致。  

```java
public void biz(String name,String password){
    this.name=name;
    this.password=password;
}
```

```xml
<aop:pointcut expression="execution(* com.spring.TestBiz.biz(String,String)) and args(name,password)" id="testpoint"/>
<aop:aspect ref="testaspect">
<aop:before method="save" pointcut-ref="testpoint" arg-names="name,password"/>
```

&emsp; AOP  

```java
public void save(String name,String password){
    System.out.println("guo="+name);
    System.out.println("password="+password);
    this.name=name;
    this.password=password;
    System.out.println(this.name);
}
```

#### 1.1.3.2. 不定形参类型  
&emsp; Spring AOP提供使用org.aspectj.lang.JoinPoint类型获取连接点数据，任何通知方法的第一个参数都可以是JoinPoint（环绕通知是ProceedingJoinPoint，JoinPoint子类）。  
1. JoinPoint：提供访问当前被通知方法的目标对象、代理对象、方法参数等数据。  
2. ProceedingJoinPoint：只用于环绕通知，使用proceed()方法来执行目标方法。  

&emsp; 如参数类型是JoinPoint、ProceedingJoinPoint类型，可以从“argNames”属性省略掉该参数名(可选，写上也对)，这些类型对象会自动传入的，但必须作为第一个参数。 


## 1.2. ★★★SpringAOP使用  
&emsp; SpringAOP的主要功能是：日志记录，性能统计，安全控制，事务处理，异常处理等。将非业务的代码从业务逻辑代码中划分出来，通过对这些行为的分离，将它们独立到非指导业务逻辑的方法中，进而改变这些行为的时候不影响业务逻辑的代码。  

* 慢请求记录  
* 使用aop + redis + Lua接口限流  <!-- https://www.jianshu.com/p/f052054aab22 -->


------------------

### 1.2.1. 切面和自定义注解配合使用  
<!-- 
切面和自定义注解的配合使用
https://blog.csdn.net/huangyu1985/article/details/53449776
-->
&emsp; **<font color = "clime">切面和自定义注解的配合使用。自定义注解可以很灵活的控制切点。</font>**  

----

### 1.2.2. 实战：记录操作日志、异常日志
<!-- 
如何使用SpringBoot AOP 记录操作日志、异常日志？ 
https://mp.weixin.qq.com/s/hqRm9veJk4eUrzWrCI_Vlg
写了个牛逼的日志切面，甩锅更方便了！ 
https://mp.weixin.qq.com/s/mWiVsBk_7uUv15H4wVadAg

-->


## 1.3. ★★★SpringAOP失效  
&emsp; <font color = "red">同一对象内部方法嵌套调用，慎用this来调用被@Async、@Transactional、@Cacheable等注解标注的方法，this下注解可能不生效。</font>async方法中的this不是动态代理的子类对象，而是原始的对象，故this调用无法通过动态代理来增强。 

### 1.3.1. 解决方案  
#### 1.3.1.1. 通过ApplicationContext来获得动态代理对象  

```java
@Component
public class AsyncService implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    public void async1() {
        System.out.println("1:" + Thread.currentThread().getName());
        // 使用AppicationContext来获得动态代理的bean
        this.applicationContext.getBean(AsyncService.class).async2();
    }

    @Async
    public void async2() {
        System.out.println("2:" + Thread.currentThread().getName());
    }

    // 注入ApplicationContext
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
```
&emsp; 执行结果是：  

```text
1:main
2:SimpleAsyncTaskExecutor-2
2:SimpleAsyncTaskExecutor-3
```
&emsp; 可以看到完美达到目的。同理是用BeanFactoryAware可达到同样的效果。  

