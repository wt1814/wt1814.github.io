---
title: Spring Boot启动全过程源码分析
date: 2020-04-14 00:00:00
tags:
    - SpringBoot
---

<!-- TOC -->

- [1. Spring Boot启动全过程源码分析](#1-spring-boot启动全过程源码分析)
- [2. SpringApplication初始化](#2-springapplication初始化)
    - [2.1. 主要流程解析](#21-主要流程解析)
        - [2.1.1. 推断当前 WEB 应用类型](#211-推断当前-web-应用类型)
        - [2.1.2. 设置应用上下文初始化器（SpringBoot的SPI机制原理）](#212-设置应用上下文初始化器springboot的spi机制原理)
            - [2.1.2.1. 获得类加载器](#2121-获得类加载器)
            - [2.1.2.2. 加载spring.factories配置文件中的SPI扩展类](#2122-加载springfactories配置文件中的spi扩展类)
            - [2.1.2.3. 实例化从spring.factories中加载的SPI扩展类](#2123-实例化从springfactories中加载的spi扩展类)
        - [2.1.3. 设置监听器](#213-设置监听器)

<!-- /TOC -->

![image](https://gitee.com/wt1814/pic-host/raw/master/images/sourceCode/springBoot/springBoot-4.png)  


# 1. Spring Boot启动全过程源码分析
&emsp; SpringBoot启动类代码如下：  

```java
public static void main(String[] args) {
    SpringApplication.run(DemoApplication.class, args);
}
```
&emsp; 进入SpringApplication#run方法中，一路点击#run方法。  

```java
public static ConfigurableApplicationContext run(Class<?>[] primarySources, String[] args) {
    return (new SpringApplication(primarySources)).run(args);
}
```
&emsp; SpringApplication.run()中首先new SpringApplication对象，然后调用该对象的run方法。即run()方法主要包括两大步骤：  
1. 创建SpringApplication 对象；  
2. 运行run()方法。  

# 2. SpringApplication初始化  
<!-- https://mp.weixin.qq.com/s/JcMRo6xuDEimKk-KZDKJ1g-->

&emsp; 构造过程一般是对构造函数一些成员属性赋值。  
&emsp; 构造SpringApplication对象时需要用到的一些成员属性：  

```
// SpringApplication.java

/**
 * SpringBoot的启动类即包含main函数的主类
 */
private Set<Class<?>> primarySources;
/**
 * 包含main函数的主类
 */
private Class<?> mainApplicationClass;
/**
 * 资源加载器
 */
private ResourceLoader resourceLoader;
/**
 * 应用类型
 */
private WebApplicationType webApplicationType;
/**
 * 初始化器
 */
private List<ApplicationContextInitializer<?>> initializers;
/**
 * 监听器
 */
private List<ApplicationListener<?>> listeners;
```
<!-- &emsp; 构建SpringApplication对象时主要是给上面代码中的六个成员属性赋值。-->  

&emsp; SpringApplication的构造函数：  

```java
public SpringApplication(ResourceLoader resourceLoader, Class... primarySources) {

    this.sources = new LinkedHashSet();
    this.bannerMode = Mode.CONSOLE;
    this.logStartupInfo = true;
    this.addCommandLineProperties = true;
    this.addConversionService = true;
    this.headless = true;
    this.registerShutdownHook = true;
    this.additionalProfiles = new HashSet();
    this.isCustomEnvironment = false;
    //1. 给resourceLoader属性赋值，注意传入的resourceLoader参数为null
    this.resourceLoader = resourceLoader;
    //断言主要加载资源类不能为 null，否则报错
    Assert.notNull(primarySources, "PrimarySources must not be null");
    //2. 初始化主要加载资源类集合并去重
    	// 【2】给primarySources属性赋值，传入的primarySources其实就是SpringApplication.run(MainApplication.class, args);中的MainApplication.class
    this.primarySources = new LinkedHashSet(Arrays.asList(primarySources));
    //3. 判断当前是否是一个 Web 应用
    this.webApplicationType = WebApplicationType.deduceFromClasspath();
    //4. 设置应用上下文初始化器
    // 从类路径下找到 META/INF/Spring.factories 配置的所有 ApplicationContextInitializer，然后保存起来
    	// 【4】给initializers属性赋值，利用SpringBoot自定义的SPI从spring.factories中加载ApplicationContextInitializer接口的实现类并赋值给initializers属性
    this.setInitializers(this.getSpringFactoriesInstances(ApplicationContextInitializer.class));
    //5. 设置监听器
    // 从类路径下找到 META/INF/Spring.factories 配置的所有 ApplicationListener，然后保存起来
    	// 【5】给listeners属性赋值，利用SpringBoot自定义的SPI从spring.factories中加载ApplicationListener接口的实现类并赋值给listeners属性
    this.setListeners(this.getSpringFactoriesInstances(ApplicationListener.class));
    //6. 推断主入口应用类。从多个配置类中找到有 main 方法的主配置类（只有一个）
    this.mainApplicationClass = this.deduceMainApplicationClass();
}
```
&emsp; 可以看到</font color = "red">构建SpringApplication对象时其实就是给前面讲的6个SpringApplication类的成员属性赋值，做一些初始化工作。</font>  
1. 给resourceLoader属性赋值，resourceLoader属性，资源加载器，此时传入的resourceLoader参数为null；  
2. 给primarySources属性赋值，primarySources属性即SpringApplication.run(MainApplication.class,args);中传入的MainApplication.class，该类为SpringBoot项目的启动类，主要通过该类来扫描Configuration类加载bean；
3. 给webApplicationType属性赋值，webApplicationType属性，代表应用类型，根据classpath存在的相应Application类来判断。因为后面要根据webApplicationType来确定创建哪种Environment对象和创建哪种ApplicationContext；
4. 给initializers属性赋值，initializers属性为List<ApplicationContextInitializer<?\>>集合，利用SpringBoot的SPI机制从spring.factories配置文件中加载，后面在初始化容器的时候会应用这些初始化器来执行一些初始化工作。因为SpringBoot自己实现的SPI机制比较重要；  
5. 给listeners属性赋值，listeners属性为List<ApplicationListener<?\>>集合，同样利用利用SpringBoot的SPI机制从spring.factories配置文件中加载。因为SpringBoot启动过程中会在不同的阶段发射一些事件，所以这些加载的监听器们就是来监听SpringBoot启动过程中的一些生命周期事件的；
6. 给mainApplicationClass属性赋值，mainApplicationClass属性表示包含main函数的类，即这里要推断哪个类调用了main函数，然后把这个类的全限定名赋值给mainApplicationClass属性，用于后面启动流程中打印一些日志，详细分析见后面的第3.2小节。

## 2.1. 主要流程解析  
&emsp; 从上述流程中，挑以下几个进行分析。  

### 2.1.1. 推断当前 WEB 应用类型  

```java
this.webApplicationType = deduceWebApplicationType();
```
&emsp; deduceWebApplicationType方法和相关的源码：  

```java
private WebApplicationType deduceWebApplicationType() {

    if (ClassUtils.isPresent(REACTIVE_WEB_ENVIRONMENT_CLASS, null) && !ClassUtils.isPresent(MVC_WEB_ENVIRONMENT_CLASS, null)) {
        return WebApplicationType.REACTIVE;
    }

    for (String className : WEB_ENVIRONMENT_CLASSES) {
        if (!ClassUtils.isPresent(className, null)) {
            return WebApplicationType.NONE;
        }
    }
    return WebApplicationType.SERVLET;
}

private static final String REACTIVE_WEB_ENVIRONMENT_CLASS = "org.springframework." 
        + "web.reactive.DispatcherHandler";
private static final String MVC_WEB_ENVIRONMENT_CLASS = "org.springframework."
        + "web.servlet.DispatcherServlet";
private static final String[] WEB_ENVIRONMENT_CLASSES = { "javax.servlet.Servlet",
        "org.springframework.web.context.ConfigurableWebApplicationContext" };

public enum WebApplicationType {
    /**
     * 非 WEB 项目
     */
    NONE,
    /**
     * SERVLET WEB 项目
     */
    SERVLET,
    /**
     * 响应式 WEB 项目
     */
    REACTIVE
}
```
&emsp; 这个就是根据类路径下是否有对应项目类型的类推断出不同的应用类型。  

### 2.1.2. 设置应用上下文初始化器（SpringBoot的SPI机制原理）  
&emsp; <font color = "red">SpringApplication初始化中第【4】步和第【5】步都是利用SpringBoot的SPI机制来加载扩展实现类。</font>  

&emsp; ***<font color = "red">SpringBoot通过以下步骤实现自己的SPI机制：</font>***  
1. 首先获取线程上下文类加载器;  
2. 然后利用上下文类加载器从spring.factories配置文件中加载所有的SPI扩展实现类并放入缓存中;  
3. 根据SPI接口从缓存中取出相应的SPI扩展实现类;  
4. 实例化从缓存中取出的SPI扩展实现类并返回。  


&emsp; ***设置应用上下文初始化器源码解读：***  

```java
this.setInitializers(this.getSpringFactoriesInstances(ApplicationContextInitializer.class));
```
1. 参数ApplicationContextInitializer.class用来初始化指定的 Spring 应用上下文，如注册属性资源、激活 Profiles 等。  
2. this.getSpringFactoriesInstances()方法和相关的源码：  

```java
private <T> Collection<T> getSpringFactoriesInstances(Class<T> type) {
    return this.getSpringFactoriesInstances(type, new Class[0]);
}

private <T> Collection<T> getSpringFactoriesInstances(Class<T> type, Class<?>[] parameterTypes, Object... args) {
    //获取当前线程上下文类加载器
    ClassLoader classLoader = this.getClassLoader();
    //获取 ApplicationContextInitializer 的实例名称集合并去重
      //根据类路径下的 META-INF/spring.factories 文件解析并获取 ApplicationContextInitializer 接口的所有配置的类路径名称
    Set<String> names = new LinkedHashSet(SpringFactoriesLoader.loadFactoryNames(type, classLoader));
    //根据以上类路径创建初始化器实例列表
    List<T> instances = this.createSpringFactoriesInstances(type, parameterTypes, classLoader, args, names);
    //初始化器实例列表排序
    AnnotationAwareOrderComparator.sort(instances);
    //返回初始化器实例列表
    return instances;
}
```
<!--
```java
// SpringApplication.java

private <T> Collection<T> getSpringFactoriesInstances(Class<T> type,
		Class<?>[] parameterTypes, Object... args) {
	// 【1】获得类加载器
	ClassLoader classLoader = getClassLoader();
	// Use names and ensure unique to protect against duplicates
	// 【2】将接口类型和类加载器作为参数传入loadFactoryNames方法，从spring.factories配置文件中进行加载接口实现类
	Set<String> names = new LinkedHashSet<>(
			SpringFactoriesLoader.loadFactoryNames(type, classLoader));
	// 【3】实例化从spring.factories中加载的接口实现类
	List<T> instances = createSpringFactoriesInstances(type, parameterTypes,
			classLoader, args, names);
	// 【4】进行排序
	AnnotationAwareOrderComparator.sort(instances);
	// 【5】返回加载并实例化好的接口实现类
	return instances;
}
```
 -->

&emsp; SpringBoot自定义实现的SPI机制代码中最重要的是上面代码的【1】,【2】,【3】步。  
<!-- https://mp.weixin.qq.com/s/szt8l6IbmjKnyRItQTjJCA -->  

#### 2.1.2.1. 获得类加载器  
&emsp; Java的SPI机制默认是利用线程上下文类加载器去加载扩展类的。那么，SpringBoot实现的SPI机制是利用哪种类加载器去加载spring.factories配置文件中的扩展实现类的？  
&emsp; ClassLoader classLoader = getClassLoader()解读：  

```
// SpringApplication.java

public ClassLoader getClassLoader() {
	// 前面在构造SpringApplicaiton对象时，传入的resourceLoader参数是null，因此不会执行if语句里面的逻辑
	if (this.resourceLoader != null) {
		return this.resourceLoader.getClassLoader();
	}
	// 获取默认的类加载器
	return ClassUtils.getDefaultClassLoader();
}
```

```
// ClassUtils.java

public static ClassLoader getDefaultClassLoader() {
	ClassLoader cl = null;
	try {
	        // 【重点】获取线程上下文类加载器
		cl = Thread.currentThread().getContextClassLoader();
	}
	catch (Throwable ex) {
		// Cannot access thread context ClassLoader - falling back...
	}
	// 这里的逻辑不会执行
	if (cl == null) {
		// No thread context class loader -> use class loader of this class.
		cl = ClassUtils.class.getClassLoader();
		if (cl == null) {
			// getClassLoader() returning null indicates the bootstrap ClassLoader
			try {
				cl = ClassLoader.getSystemClassLoader();
			}
			catch (Throwable ex) {
				// Cannot access system ClassLoader - oh well, maybe the caller can live with null...
			}
		}
	}
	// 返回刚才获取的线程上下文类加载器
	return cl;
}
```
&emsp; SpringBoot的SPI机制中也是用线程上下文类加载器去加载spring.factories文件中的扩展实现类的！  

#### 2.1.2.2. 加载spring.factories配置文件中的SPI扩展类  
&emsp; SpringFactoriesLoader.loadFactoryNames(type, classLoader)是如何加载spring.factories配置文件中的SPI扩展类的？   

```java
/ SpringFactoriesLoader.java

public static List<String> loadFactoryNames(Class<?> factoryClass, @Nullable ClassLoader classLoader) {
        // factoryClass即SPI接口，比如ApplicationContextInitializer,EnableAutoConfiguration等接口
	String factoryClassName = factoryClass.getName();
	// 【主线，重点关注】继续调用loadSpringFactories方法加载SPI扩展类
	return loadSpringFactories(classLoader).getOrDefault(factoryClassName, Collections.emptyList());
```

```java
// SpringFactoriesLoader.java

/**
 * The location to look for factories.
 * <p>Can be present in multiple JAR files.
 */
public static final String FACTORIES_RESOURCE_LOCATION = "META-INF/spring.factories";

private static Map<String, List<String>> loadSpringFactories(@Nullable ClassLoader classLoader) {
	// 以classLoader作为键先从缓存中取，若能取到则直接返回
	MultiValueMap<String, String> result = cache.get(classLoader);
	if (result != null) {
		return result;
	}
	// 若缓存中无记录，则去spring.factories配置文件中获取
	try {
		// 这里加载所有jar包中包含"MATF-INF/spring.factories"文件的url路径
		Enumeration<URL> urls = (classLoader != null ?
				classLoader.getResources(FACTORIES_RESOURCE_LOCATION) :
				ClassLoader.getSystemResources(FACTORIES_RESOURCE_LOCATION));
		result = new LinkedMultiValueMap<>();
		// 遍历urls路径，将所有spring.factories文件的键值对（key:SPI接口类名 value:SPI扩展类名）
		// 加载放到 result集合中
		while (urls.hasMoreElements()) {
			// 取出一条url
			URL url = urls.nextElement();
			// 将url封装到UrlResource对象中
			UrlResource resource = new UrlResource(url);
			// 利用PropertiesLoaderUtils的loadProperties方法将spring.factories文件键值对内容加载进Properties对象中
			Properties properties = PropertiesLoaderUtils.loadProperties(resource);
			// 遍历刚加载的键值对properties对象
			for (Map.Entry<?, ?> entry : properties.entrySet()) {
				// 取出SPI接口名
				String factoryClassName = ((String) entry.getKey()).trim();
				// 遍历SPI接口名对应的实现类即SPI扩展类
				for (String factoryName : StringUtils.commaDelimitedListToStringArray((String) entry.getValue())) {
					// SPI接口名作为key，SPI扩展类作为value放入result中
					result.add(factoryClassName, factoryName.trim());
				}
			}
		}
		// 以classLoader作为key，result作为value放入cache缓存
		cache.put(classLoader, result);
		// 最终返回result对象
		return result;
	}
	catch (IOException ex) {
		throw new IllegalArgumentException("Unable to load factories from location [" +
				FACTORIES_RESOURCE_LOCATION + "]", ex);
	}
}
```
&emsp; loadSpringFactories方法主要做的事情就是利用之前获取的线程上下文类加载器将classpath中的所有spring.factories配置文件中所有SPI接口的所有扩展实现类给加载出来，然后放入缓存中。注意，这里是一次性加载所有的SPI扩展实现类哈，所以之后根据SPI接口就直接从缓存中获取SPI扩展类了，就不用再次去spring.factories配置文件中获取SPI接口对应的扩展实现类了。比如之后的获取ApplicationListener,FailureAnalyzer和EnableAutoConfiguration接口的扩展实现类都直接从缓存中获取即可。  

&emsp; 将所有的SPI扩展实现类加载出来后，此时再调用getOrDefault(factoryClassName, Collections.emptyList())方法根据SPI接口名去筛选当前对应的扩展实现类，比如这里传入的factoryClassName参数名为ApplicationContextInitializer接口，那么这个接口将会作为key从刚才缓存数据中取出ApplicationContextInitializer接口对应的SPI扩展实现类。其中从spring.factories中获取的ApplicationContextInitializer接口对应的所有SPI扩展实现类如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/sourceCode/springBoot/springBoot-5.png)  

#### 2.1.2.3. 实例化从spring.factories中加载的SPI扩展类  
&emsp; 从spring.factories中获取到ApplicationContextInitializer接口对应的所有SPI扩展实现类后，此时会将这些SPI扩展类进行实例化。  
&emsp; List<T\> instances = createSpringFactoriesInstances(type, parameterTypes, classLoader, args, names)实例化SPI扩展类，代码解读：  

```
// SpringApplication.java

private <T> List<T> createSpringFactoriesInstances(Class<T> type,
		Class<?>[] parameterTypes, ClassLoader classLoader, Object[] args,
		Set<String> names) {
	// 新建instances集合，用于存储稍后实例化后的SPI扩展类对象
	List<T> instances = new ArrayList<>(names.size());
	// 遍历name集合，names集合存储了所有SPI扩展类的全限定名
	for (String name : names) {
		try {
			// 根据全限定名利用反射加载类
			Class<?> instanceClass = ClassUtils.forName(name, classLoader);
			// 断言刚才加载的SPI扩展类是否属于SPI接口类型
			Assert.isAssignable(type, instanceClass);
			// 获得SPI扩展类的构造器
			Constructor<?> constructor = instanceClass
					.getDeclaredConstructor(parameterTypes);
			// 实例化SPI扩展类
			T instance = (T) BeanUtils.instantiateClass(constructor, args);
			// 添加进instances集合
			instances.add(instance);
		}
		catch (Throwable ex) {
			throw new IllegalArgumentException(
					"Cannot instantiate " + type + " : " + name, ex);
		}
	}
	// 返回
	return instances;
}
```

### 2.1.3. 设置监听器  

```java
this.setListeners(this.getSpringFactoriesInstances(ApplicationListener.class));
```
&emsp; 参数ApplicationListener.class继承了JDK 的 java.util.EventListener 接口，实现了观察者模式，它一般用来定义感兴趣的事件类型，事件类型限定于 ApplicationEvent的子类。  

