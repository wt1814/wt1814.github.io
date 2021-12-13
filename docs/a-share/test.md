

&emsp; 第1点是铺垫，第2点是重点。

1. 线程上下文类加载器的需求场景是这样的，拿JDBC的driver加载来举例：  
&emsp; java.sql.DriverManager类是JDK的核心类，由BootClassLoader加载器来加载。  
&emsp; java.sql.DriverManager类里，加载具体实现类的源码：

```java
private static void loadInitialDrivers() {
       //省略无关代码....
       ServiceLoader<Driver> loadedDrivers = ServiceLoader.load(Driver.class);
       Iterator<Driver> driversIterator = loadedDrivers.iterator();
       //省略无关代码....
```

&emsp; 可以看到，在java.sql.DriverManager类里需要加载具体实现类，但java.sql.DriverManager类是由BootClassLoader加载器加载的，所以这个时候就无法使用双亲委派模型来加载具体实现类了。  
&emsp; 这时就只能使用SPI方式的线程上下文类加载器来加载具体实现类。  

2. 自己实现的框架，接口类和实现类一般都是由SystemClassLoader加载器来加载的，这时候双亲委派模型仍然可以正常使用。很多框架使用SPI方式的原因，不是因为双亲委派模型满足不了类加载需求，而是看重了SPI的易扩展性。

