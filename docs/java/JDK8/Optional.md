

<!-- TOC -->

- [1. Optional类](#1-optional类)
    - [1.1. 常用API](#11-常用api)
    - [1.2. 正确使用Optional类](#12-正确使用optional类)
        - [1.2.1. 基础理解](#121-基础理解)
        - [1.2.2. 错误使用](#122-错误使用)
        - [1.2.3. 正确使用](#123-正确使用)
    - [1.3. 参考](#13-参考)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
&emsp; 使用Optional时尽量不直接调用Optional.get()方法，Optional.isPresent()更应该被视为一个私有方法，应依赖于其他像Optional.orElse()，Optional.orElseGet()，Optional.map()等这样的方法。  

# 1. Optional类  
<!-- 
实例
https://mp.weixin.qq.com/s/sYCE05xYN0kycKgT_P6cJw
-->
&emsp; Java8引入Optional类，是一个可以为null的容器对象，是一个包含有可选值的包装类，可以保存类型T的值，或者保存null。Optional类的引入解决空指针异常。防止编写不必要的null检查。快速定位NullPointException。  
&emsp; public final class Optional<T\>，<font color = "clime">构造函数私有化；不能new实例；不能被继承。</font>  

## 1.1. 常用API  
<!-- 
Java 8 中使用 Optional 处理 null 对象，超全指南 
https://mp.weixin.qq.com/s/Fq-dOg78VMyEfNFjCJ7Krw
-->
&emsp; 使用Optional可以解决业务中，减少值动不动就抛出空指针异常问题，也减少 null 值的判断，提高代码可读性等，这里介绍下，如果使用这个 Optional 类。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JDK/java8/java-8.png)  

|方法|描述|
|---|---|
|empty()|返回空的Optional实例|
|of(T value)|返回一个指定非null值的Optional|
|ofNullable(T value)|如果为非空，返回 Optional 描述的指定值，否则返回空的 Optional|
|   |    |	
|equals(Object obj)	|判断其他对象是否等于Optional|
|filter(Predicate<? super <T> predicate)|如果值存在，并且这个值匹配给定的 predicate，返回一个Optional用以描述这个值，否则返回一个空的Optional|
|flatMap(Function<? super T,Optional<U>> mapper)|如果值存在，返回基于Optional包含的映射方法的值，否则返回一个空的Optional|
|get()|如果在这个Optional中包含这个值，返回值，否则抛出异常：NoSuchElementException|
|hashCode()	|返回存在值的哈希码，如果值不存在，返回0|
|ifPresent(Consumer<? super T> consumer)|如果值存在则使用该值调用consumer，否则不做任何事情|
|isPresent()|如果值存在则方法会返回true，否则返回 false|
|map(Function<? super T,? extends U> mapper)|如果存在该值，提供的映射方法，如果返回非null，返回一个Optional描述结果|
|orElse(T other)|如果存在该值，返回值，否则返回 other|
|orElseGet(Supplier<? extends T> other)	|如果存在该值，返回值，否则触发other，并返回other调用的结果|
|orElseThrow(Supplier<? extends X> exceptionSupplier)|如果存在该值，返回包含的值，否则抛出由Supplier继承的异常|
|toString()	|返回一个Optional的非空字符串，用来调试|


## 1.2. 正确使用Optional类  
<!-- 
*****很重要 Java8 Optional，可以这样用啊 
https://mp.weixin.qq.com/s/-8W6-MwPURrwviJ2gJT4Tw
-->

### 1.2.1. 基础理解  
&emsp; 首先，Optional是一个容器，用于放置可能为空的值，它可以合理而优雅的处理null。众所周知，null在编程历史上极具话题性，号称是计算机历史上最严重的错误，感兴趣可以读一下这篇文章：THE WORST MISTAKE OF COMPUTER SCIENCE，这里暂且不做过多讨论。在 Java 1.8 之前的版本，没有可以用于表示null官方 API，如果你足够的谨慎，你可能需要常常在代码中做如下的判断：  

```java
if (null != user) {
    //doing something
}
if (StringUtil.isEmpty(string)) {
    //doing something
}
```

&emsp; 确实，返回值是null的情况太多了，一不小心，就会产生 NPE，接踵而来的就是应用运行终止，产品抱怨，用户投诉。  
&emsp; 1.8 之后，jdk 新增了Optional来表示空结果。其实本质上什么也没变，只是增加了一个表达方式。Optional表示空的静态方法为Optional.empty()，跟null有什么本质区别吗？其实没有。翻看它的实现，Optional中的 value 就是null，只不过包了一层Optional，所以说它其实是个容器。用之后的代码可能长这样：  

```java
// 1
Optional<User> optionalUser = RemoteService.getUser();
if (!optionalUser.isPresent()) {
   //doing something 
}
User user = optionalUser.get();

// 2
User user = optionalUser.get().orElse(new User());
```

&emsp; 看起来，好像比之前好了一些，至少看起来没那么笨。但如果采用写法 1，好像更啰嗦了。  
&emsp; 如果你对 kotlin 稍有了解，kotlin 的非空类型是他们大肆宣传的"卖点"之一，通过var param!!在使用它的地方做强制的空检查，否则无法通过编译，最大程度上减少了 NPE。其实在我看来，Optional的方式更加优雅和灵活。同时，Optional也可能会带来一些误解。  



### 1.2.2. 错误使用  
&emsp; 先看看错误的姿势：  

```java
User user;
Optional<User> optional = Optional.of(user);
if (optional.isPresent()) {
    return optional.get().getOrders();
} else {
    return Collections.emptyList();;
}
```

&emsp; 和之前的写法没有任何区别。  

```java
if (user != null) {
    return user.getOrders();
} else {
    return Collections.emptyList();;
}
```
&emsp; <font color = "red">慎重使用Optional类的以下方法：isPresent()方法、get()方法、Optional类型作为类/实例属性、Optional类型作为方法参数。</font>isPresent()与obj!= null无任何分别。而没有isPresent()作铺垫的get()调用，在IntelliJ IDEA中会收到告警。把Optional类型用作属性或是方法参数在IntelliJ IDEA中更是强力不推荐的。所以Optional中真正可依赖的是除了isPresent()和get()的其他方法。  


### 1.2.3. 正确使用  
&emsp; 首先是一些基本原则：

* 不要声明任何Optional实例属性  
* 不要在任何 setter 或者构造方法中使用Optional  
* Optional属于返回类型，在业务返回值或者远程调用中使用  


-----------------------

&emsp; 创建Optional对象：  

```java
Optional<Soundcard> sc = Optional.empty();
SoundCard soundcard = new Soundcard();
Optional<Soundcard> sc = Optional.of(soundcard);
Optional<Soundcard> sc = Optional.ofNullable(soundcard);
```

------------  
&emsp; 业务上需要空值时，不要直接返回 null，使用Optional.empty()  

```java
public Optional<User> getUser(String name) {
    if (StringUtil.isNotEmpty(name)) {
        return RemoteService.getUser(name);
    } 
    return Optional.empty();
}
```


------------  

&emsp; 使用 orElseGet()  
&emsp; 获取 value 有三种方式：get() orElse() orElseGet()。这里推荐在需要用到的地方只用 orElseGet()。  
&emsp; 首先，get()不能直接使用，需要结合判空使用。这和!=null其实没多大区别，只是在表达和抽象上有所改善。  
&emsp; 其次，为什么不推荐orElse()呢？因为orElse()无论如何都会执行括号中的内容，orElseGet()只在主体 value 是空时执行，下面看个例子：  

```java
public String getName() {
    System.out.print("method called");
}

String name1 = Optional.of("String").orElse(getName()); //output: method called
String name2 = Optional.of("String").orElseGet(() -> getName()); //output:
```

&emsp; 如果上面的例子getName()方法是一个远程调用，或者涉及大量的文件 IO，代价可想而知。  
&emsp; 但 orElse()就一无是处吗？并不是。orElseGet()需要构建一个Supplier，如果只是简单的返回一个静态资源、字符串等等，直接返回静态资源即可。  

```java
public static final String USER_STATUS = "UNKNOWN";
...
public String findUserStatus(long id) {
    Optional<String> status = ... ; // 
    return status.orElse(USER_STATUS);
}

//不要这么写
public String findUserStatus(long id) {
    Optional<String> status = ... ; // 
    return status.orElse("UNKNOWN");//这样每次都会新建一个String对象
}
```


&emsp; **<font color = "red">存在即返回, 无则提供默认值：</font>**  

```java
return user.orElse(null);  //而不是 return user.isPresent() ? user.get() : null;
return user.orElse(UNKNOWN_USER);
```
&emsp; **<font color = "red">存在即返回, 无则由函数来产生：</font>**  

```java
return user.orElseGet(() -> fetchAUserFromDatabase()); 
//而不要 return user.isPresent() ? user: fetchAUserFromDatabase();
```

--------------
&emsp; 抛出异常可以使用：  
&emsp; <font color = "clime">Optional.ofNullable(storeInfo).orElseThrow(()->new Exception("失败")); </font>   

&emsp; 使用 orElseThrow()  
&emsp; 这个针对阻塞性的业务场景比较合适，例如没有从上游获取到用户信息，下面的所有操作都无法进行，那此时就应该抛出异常。正常的写法是先判空，再手动 throw 异常，现在可以集成为一行：  

```java
public String findUser(long id) {
    Optional<User> user = remoteService.getUserById(id) ;
    return user.orElseThrow(IllegalStateException::new);
}
```

------------
&emsp; 不为空则执行时，使用 ifPresent()  
&emsp; 这点没有性能上的优势，但可以使代码更简洁：  

```java
//之前是这样的
if (status.isPresent()) {
    System.out.println("Status: " + status.get());
}

//现在
status.ifPresent(System.out::println);
```

&emsp; **<font color = "red">存在才对它操作：</font>**  

```java
user.ifPresent(System.out::println);
/*而不要下边那样
if (user.isPresent()) {
    System.out.println(user.get());
}*/
```

-------------

&emsp; **<font color = "red">使用map抽取特定的值或者做值的转换：</font>**  

```java
return user.map(u -> u.getOrders()).orElse(Collections.emptyList())
//上面避免了类似 Java 8 之前的做法
if(user.isPresent()) {
    return user.get().getOrders();
} else {
    return Collections.emptyList();
}
```
&emsp; **<font color = "red">级联使用map，避免了连续的空值判断：</font>**  

```java
return user.map(u -> u.getUsername()).map(name -> name.toUpperCase()).orElse(null);
/*User user = .....
if(user != null) {
    String name = user.getUsername();
    if(name != null) {
        return name.toUpperCase();
    } else {
        return null;
    }
} else {
    return null;
}*/
```

&emsp; **<font color = "red">级联的Optional对象使用flatMap：</font>**  

```java
String version = computer.flatMap(Computer::getSoundcard)
        .flatMap(Soundcard::getUSB)
        .map(USB::getVersion)
        .orElse("UNKNOWN");
```

-----------
&emsp; **<font color = "red">使用filter拒绝特定的值：</font>**  

```java
Optional<USB> maybeUSB = ...;
maybeUSB.filter(usb -> "3.0".equals(usb.getVersion()).ifPresent(() -> System.out.println("ok"));
```
&emsp; 使用isPresent()处理NullPointerException不叫优雅；使用orElse, orElseGet等，特别是map方法才叫优雅。其他几个，filter()把不符合条件的值变为empty()，flatMap()总是与map()方法成对的，orElseThrow()在有值时直接返回，无值时抛出想要的异常。   

------------

&emsp; 不要滥用  
&emsp; 有些简单明了的方法，完全没必要增加Optional来增加复杂性。  

```java
public String fetchStatus() {
    String status = getStatus() ;
    return Optional.ofNullable(status).orElse("PENDING");
}

//判断一个简单的状态而已
public String fetchStatus() {
    String status = ... ;
    return status == null ? "PENDING" : status;
}
```

&emsp; 首先，null 可以作为集合的元素之一，它并不是非法的；其次，集合类型本身已经具备了完整的空表达，再去包装一层Optional也是徒增复杂，收益甚微。例如，map 已经有了getOrDefault()这样的类似orElse()的 API 了。  

---------
&emsp; 小结：使用Optional时尽量不直接调用Optional.get()方法，Optional.isPresent()更应该被视为一个私有方法，应依赖于其他像Optional.orElse()，Optional.orElseGet()，Optional.map()等这样的方法。


## 1.3. 参考  
