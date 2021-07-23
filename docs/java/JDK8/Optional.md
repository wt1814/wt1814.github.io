

<!-- TOC -->

- [1. Optional类](#1-optional类)
    - [1.1. 常用API](#11-常用api)
    - [1.2. 正确使用Optional类](#12-正确使用optional类)
        - [1.2.1. 错误使用](#121-错误使用)
        - [1.2.2. 正确使用](#122-正确使用)

<!-- /TOC -->


# 1. Optional类  
<!-- 
*****很重要 Java8 Optional，可以这样用啊 
 https://mp.weixin.qq.com/s/-8W6-MwPURrwviJ2gJT4Tw

求求你，别再用 “ ! = null " 做判空了！ 
https://mp.weixin.qq.com/s/NXUWZjt2kYRG301CXehp0A
Java 8 中使用 Optional 处理 null 对象，超全指南 
https://mp.weixin.qq.com/s/Fq-dOg78VMyEfNFjCJ7Krw
-->
&emsp; Java8引入Optional类，是一个可以为null的容器对象，是一个包含有可选值的包装类，可以保存类型T的值，或者保存null。Optional类的引入解决空指针异常。防止编写不必要的null检查。快速定位NullPointException。  
&emsp; public final class Optional<T\>，<font color = "clime">构造函数私有化；不能new实例；不能被继承。</font>  

## 1.1. 常用API  

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
### 1.2.1. 错误使用  

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

### 1.2.2. 正确使用  
&emsp; 创建Optional对象：  

```java
Optional<Soundcard> sc = Optional.empty();
SoundCard soundcard = new Soundcard();
Optional<Soundcard> sc = Optional.of(soundcard);
Optional<Soundcard> sc = Optional.ofNullable(soundcard);
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
&emsp; **<font color = "red">存在才对它操作：</font>**  

```java
user.ifPresent(System.out::println);
/*而不要下边那样
if (user.isPresent()) {
    System.out.println(user.get());
}*/
```
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

&emsp; **<font color = "red">使用filter拒绝特定的值：</font>**  

```java
Optional<USB> maybeUSB = ...;
maybeUSB.filter(usb -> "3.0".equals(usb.getVersion()).ifPresent(() -> System.out.println("ok"));
```
&emsp; 使用isPresent()处理NullPointerException不叫优雅；使用orElse, orElseGet等，特别是map方法才叫优雅。其他几个，filter()把不符合条件的值变为empty()，flatMap()总是与map()方法成对的，orElseThrow()在有值时直接返回，无值时抛出想要的异常。   
&emsp; 小结：使用Optional时尽量不直接调用Optional.get()方法，Optional.isPresent()更应该被视为一个私有方法，应依赖于其他像Optional.orElse()，Optional.orElseGet()，Optional.map()等这样的方法。


&emsp; 抛出异常可以使用：  
&emsp; <font color = "clime">Optional.ofNullable(storeInfo).orElseThrow(()->new Exception("失败")); </font> 
