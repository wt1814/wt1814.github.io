

<!-- TOC -->

- [1. Java SPI机制详解](#1-java-spi机制详解)
    - [1.1. 什么是SPI？](#11-什么是spi)
    - [1.2. SPI案例](#12-spi案例)
    - [1.3. SPI编码示例](#13-spi编码示例)
    - [1.4. SPI的用途](#14-spi的用途)
        - [1.4.1. Driver实现](#141-driver实现)
        - [1.4.2. Mysql DriverManager实现](#142-mysql-drivermanager实现)
    - [1.5. JDK中SPI解析（SPI与线程上下文类加载器）](#15-jdk中spi解析spi与线程上下文类加载器)
    - [Dubbo中的SPI](#dubbo中的spi)
    - [SpringBoot中的SPI](#springboot中的spi)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
&emsp; **<font color = "clime">JDK提供的SPI机制：</font>**  
1. 提供一个接口；  
2. 服务提供方实现接口，并在META-INF/services/中暴露实现类地址；  
3. 服务调用方依赖接口，使用java.util.ServiceLoader类调用。  


-----

&emsp; 个人的简单理解：  
&emsp; SPI（Service Provider Interface），服务提供发现接口。热插拔、动态替换。  
&emsp; 多态，一个接口在一个包中有多个实现；而SPI提供的接口的实现一般在多个包中，例如JDBC的实现mysql、oracle，web容器有tomcat、jetty等。  
&emsp; 一个接口在b、c包中有实现，在a包中可替换所依赖的包（b或c），动态实现某一个功能。  


# 1. Java SPI机制详解
<!--
https://blog.csdn.net/sigangjun/article/details/79071850
在java中使用SPI创建可扩展的应用程序 
https://mp.weixin.qq.com/s/B17Kvjb66iAAZmxDqBDWsA
为什么要有 SPI
https://mp.weixin.qq.com/s/4jA9MU-RFY8MttUbMmt76g


jdk和dubbo的SPI机制你应该了
https://mp.weixin.qq.com/s/lE2R-bPoB6OMZbm-GitYNQ
  Java是如何实现自己的SPI机制的？ JDK源码（一） 
 https://mp.weixin.qq.com/s/JMLQpNZzLPbPgXwS-nQXuA
 JAVA拾遗--关于SPI机制 
https://mp.weixin.qq.com/s?__biz=MzI0NzEyODIyOA==&mid=2247483791&idx=1&sn=0f2d9807c72ec8041e4ec28b10d75176&scene=21#wechat_redirect
https://mp.weixin.qq.com/s?__biz=MzAwMDczMjMwOQ==&mid=2247483837&idx=1&sn=af903350c3146ee009e24ae6a7563c10&chksm=9ae53c47ad92b551448d657a30a9c9180596e34ff57ceaa370c67cff4bb1c04812a9a35c2382&scene=21#wechat_redirect

https://mp.weixin.qq.com/s/lE2R-bPoB6OMZbm-GitYNQ
-->

## 1.1. 什么是SPI？

&emsp; SPI 全称为 (Service Provider Interface，服务提供发现接口) ，是JDK内置的一种服务提供发现机制。SPI是一种动态替换发现的机制，比如有个接口，想运行时动态的给它添加实现，只需要添加一个实现。经常遇到的就是java.sql.Driver接口，其他不同厂商可以针对同一接口做出不同的实现，mysql和postgresql都有不同的实现提供给用户，而Java的SPI机制可以为某个接口寻找服务实现。

![image](http://182.92.69.8:8081/img/java/JDK/basics/java-5.png)  
&emsp; 如上图所示： **接口对应的抽象SPI接口；实现方实现SPI接口；调用方依赖SPI接口。**

&emsp; <font color = "red">~~SPI接口的定义在调用方，在概念上更依赖调用方；组织上位于调用方所在的包中，实现位于独立的包中。~~</font>

&emsp; **<font color = "red">当服务的提供者提供了一种接口的实现之后，服务的消费者需要在classpath下的META-INF/services/目录里创建一个以服务接口命名的文件，这个文件里的内容就是这个接口的具体的实现类。服务消费者依赖服务提供者。</font>** 当服务消费者的程序需要这个服务的时候，就可以通过查找这个jar包(一般都是以jar包做依赖)的META-INF/services/中的配置文件，配置文件中有接口的具体实现类名，可以根据这个类名进行加载实例化，就可以使用该服务了。  
&emsp; **<font color = "clime">JDK中查找服务实现的工具类是：java.util.ServiceLoader。</font>**

## 1.2. SPI案例  
1. JDBC驱动加载案例：利用Java的SPI机制，可以根据不同的数据库厂商来引入不同的JDBC驱动包；  
2. SpringBoot的SPI机制：可以在spring.factories中加上自定义的自动配置类，事件监听器或初始化器等；  
3. Dubbo的SPI机制：Dubbo更是把SPI机制应用的淋漓尽致，Dubbo基本上自身的每个功能点都提供了扩展点，比如提供了集群扩展，路由扩展和负载均衡扩展等差不多接近30个扩展点。如果Dubbo的某个内置实现不符合的需求，那么只要利用其SPI机制将自定义的实现替换掉Dubbo的实现即可。  

## 1.3. SPI编码示例  
<!-- 
你应该了解的 Java SPI 机制 
https://mp.weixin.qq.com/s/CixQ4fglnonfFoC3A7Mn5g
-->

## 1.4. SPI的用途

&emsp; 数据库DriverManager、Spring、ConfigurableBeanFactory等都用到了SPI机制，这里以数据库DriverManager为例，看一下其实现的内幕。

&emsp; DriverManager是jdbc里管理和注册不同数据库driver的工具类。针对一个数据库，可能会存在着不同的数据库驱动实现。在使用特定的驱动实现时，不希望修改现有的代码，而希望通过一个简单的配置就可以达到效果。 在使用mysql驱动的时候，会有一个疑问，DriverManager是怎么获得某确定驱动类的？在运用Class.forName("com.mysql.jdbc.Driver")加载mysql驱动后，就会执行其中的静态代码把driver注册到DriverManager中，以便后续的使用。

### 1.4.1. Driver实现

```java
package com.mysql.jdbc;

import java.sql.DriverManager;
import java.sql.SQLException;

public class Driver extends NonRegisteringDriver implements java.sql.Driver {
    public Driver() throws SQLException {
    }

    static {
        try {
            DriverManager.registerDriver(new Driver());
        } catch (SQLException var1) {
            throw new RuntimeException("Can't register driver!");
        }
    }
}
```

&emsp; 驱动的类的静态代码块中，调用DriverManager的注册驱动方法new一个自己当参数传给驱动管理器。

### 1.4.2. Mysql DriverManager实现

```java
/**
    * Load the initial JDBC drivers by checking the System property
    * jdbc.properties and then use the {@code ServiceLoader} mechanism
    */
static {
    loadInitialDrivers();
    println("JDBC DriverManager initialized");
}
```

&emsp; 可以看到其内部的静态代码块中有一个`loadInitialDrivers`方法，`loadInitialDrivers`用法用到了上文提到的spi工具类`ServiceLoader`:

```java
private static void loadInitialDrivers() {
        String drivers;
        try {
            drivers = AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty("jdbc.drivers");
                }
            });
        } catch (Exception ex) {
            drivers = null;
        }
        // If the driver is packaged as a Service Provider, load it.
        // Get all the drivers through the classloader
        // exposed as a java.sql.Driver.class service.
        // ServiceLoader.load() replaces the sun.misc.Providers()

        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {

                ServiceLoader<Driver> loadedDrivers = ServiceLoader.load(Driver.class);
                Iterator<Driver> driversIterator = loadedDrivers.iterator();

                /* Load these drivers, so that they can be instantiated.
                 * It may be the case that the driver class may not be there
                 * i.e. there may be a packaged driver with the service class
                 * as implementation of java.sql.Driver but the actual class
                 * may be missing. In that case a java.util.ServiceConfigurationError
                 * will be thrown at runtime by the VM trying to locate
                 * and load the service.
                 *
                 * Adding a try catch block to catch those runtime errors
                 * if driver not available in classpath but it's
                 * packaged as service and that service is there in classpath.
                 */
                try{
                    while(driversIterator.hasNext()) {
                        driversIterator.next();
                    }
                } catch(Throwable t) {
                // Do nothing
                }
                return null;
            }
        });
    println("DriverManager.initialize: jdbc.drivers = " + drivers);

        if (drivers == null || drivers.equals("")) {
            return;
        }
        String[] driversList = drivers.split(":");
        println("number of Drivers:" + driversList.length);
        for (String aDriver : driversList) {
            try {
                println("DriverManager.Initialize: loading " + aDriver);
                Class.forName(aDriver, true,
                        ClassLoader.getSystemClassLoader());
            } catch (Exception ex) {
                println("DriverManager.Initialize: load failed: " + ex);
            }
        }
```

&emsp; 先查找jdbc.drivers属性的值，然后通过SPI机制查找驱动。  

```java
public final class ServiceLoader<S>
    implements Iterable<S>
{

    private static final String PREFIX = "META-INF/services/";
```

```java
private boolean hasNextService() {
            if (nextName != null) {
                return true;
            }
            if (configs == null) {
                try {
                    String fullName = PREFIX + service.getName();
                    if (loader == null)
                        configs = ClassLoader.getSystemResources(fullName);
                    else
                        configs = loader.getResources(fullName);
                } catch (IOException x) {
                    fail(service, "Error locating configuration files", x);
                }
            }
            while ((pending == null) || !pending.hasNext()) {
                if (!configs.hasMoreElements()) {
                    return false;
                }
                pending = parse(service, configs.nextElement());
            }
            nextName = pending.next();
            return true;
        }
```

&emsp; 可以看到加载META-INF/services/ 文件夹下类名为文件名(这里相当于Driver.class.getName())的资源，然后将其加载到虚拟机。

&emsp; 注释有这么一句“Load these drivers, so that they can be instantiated.” 意思是加载SPI扫描到的驱动来触发它们的初始化。即触发它们的static代码块。  

```java
/**
    * Registers the given driver with the {@code DriverManager}.
    * A newly-loaded driver class should call
    * the method {@code registerDriver} to make itself
    * known to the {@code DriverManager}. If the driver is currently
    * registered, no action is taken.
    *
    * @param driver the new JDBC Driver that is to be registered with the
    *               {@code DriverManager}
    * @param da     the {@code DriverAction} implementation to be used when
    *               {@code DriverManager#deregisterDriver} is called
    * @exception SQLException if a database access error occurs
    * @exception NullPointerException if {@code driver} is null
    * @since 1.8
    */
public static synchronized void registerDriver(java.sql.Driver driver,
        DriverAction da)
    throws SQLException {

    /* Register the driver if it has not already been added to our list */
    if(driver != null) {
        registeredDrivers.addIfAbsent(new DriverInfo(driver, da));
    } else {
        // This is for compatibility with the original DriverManager
        throw new NullPointerException();
    }

    println("registerDriver: " + driver);

}
```

&emsp; 将自己注册到驱动管理器的驱动列表中。  

```java
public class DriverManager {

    // List of registered JDBC drivers
    private final static CopyOnWriteArrayList<DriverInfo> registeredDrivers = new CopyOnWriteArrayList<>();
}
```

&emsp; 当获取连接的时候调用驱动管理器的连接方法从列表中获取。

```java
@CallerSensitive
public static Connection getConnection(String url,
    String user, String password) throws SQLException {
    java.util.Properties info = new java.util.Properties();

    if (user != null) {
        info.put("user", user);
    }
    if (password != null) {
        info.put("password", password);
    }

    return (getConnection(url, info, Reflection.getCallerClass()));
}
```

```java
private static Connection getConnection(
    String url, java.util.Properties info, Class<?> caller) throws SQLException {
    /*
        * When callerCl is null, we should check the application's
        * (which is invoking this class indirectly)
        * classloader, so that the JDBC driver class outside rt.jar
        * can be loaded from here.
        */
    ClassLoader callerCL = caller != null ? caller.getClassLoader() : null;
    synchronized(DriverManager.class) {
        // synchronize loading of the correct classloader.
        if (callerCL == null) {
            callerCL = Thread.currentThread().getContextClassLoader();
        }
    }

    if(url == null) {
        throw new SQLException("The url cannot be null", "08001");
    }

    println("DriverManager.getConnection(\"" + url + "\")");

    // Walk through the loaded registeredDrivers attempting to make a connection.
    // Remember the first exception that gets raised so we can reraise it.
    SQLException reason = null;

    for(DriverInfo aDriver : registeredDrivers) {
        // If the caller does not have permission to load the driver then
        // skip it.
        if(isDriverAllowed(aDriver.driver, callerCL)) {
            try {
                println("    trying " + aDriver.driver.getClass().getName());
                Connection con = aDriver.driver.connect(url, info);
                if (con != null) {
                    // Success!
                    println("getConnection returning " + aDriver.driver.getClass().getName());
                    return (con);
                }
            } catch (SQLException ex) {
                if (reason == null) {
                    reason = ex;
                }
            }

        } else {
            println("    skipping: " + aDriver.getClass().getName());
        }

    }

    // if we got here nobody could connect.
    if (reason != null)    {
        println("getConnection failed: " + reason);
        throw reason;
    }

    println("getConnection: no suitable driver found for "+ url);
    throw new SQLException("No suitable driver found for "+ url, "08001");
}
```

```java
private static boolean isDriverAllowed(Driver driver, ClassLoader classLoader) {
    boolean result = false;
    if(driver != null) {
        Class<?> aClass = null;
        try {
            aClass =  Class.forName(driver.getClass().getName(), true, classLoader);
        } catch (Exception ex) {
            result = false;
        }

            result = ( aClass == driver.getClass() ) ? true : false;
    }

    return result;
}
```

## 1.5. JDK中SPI解析（SPI与线程上下文类加载器）  
<!--

https://www.jianshu.com/p/304cb533ba2d

*** 重要https://zhuanlan.zhihu.com/p/38505998

https://mp.weixin.qq.com/s/6BhHBtoBlSqHlXduhzg7Pw
深入理解ServiceLoader类与SPI机制
https://blog.csdn.net/li_xunhuan/article/details/103017286?utm_medium=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-15.nonecase&depth_1-utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-15.nonecase
-->

&emsp; JDK的SPI内部使用线程上下文类加载器实现，破坏了双亲委派模型，是为了适用所有场景。ServiceLoader中的load方法：  

```java
public static <S> ServiceLoader<S> load(Class<S> service) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return ServiceLoader.load(service, cl);
}
```

&emsp; `Dubbo的SPI并没有破坏双亲委派模型。`自己实现的框架，接口类和实现类一般都是由SystemClassLoader加载器来加载的，这时候双亲委派模型仍然可以正常使用。很多框架使用SPI方式的原因，不是因为双亲委派模型满足不了类加载需求，而是看重了SPI的易扩展性。  

## Dubbo中的SPI  


## SpringBoot中的SPI  
<!-- 
https://blog.51cto.com/u_3631118/3121449

-->


