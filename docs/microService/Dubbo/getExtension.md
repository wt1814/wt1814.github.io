
<!-- TOC -->

- [1. SPI解析](#1-spi解析)
    - [1.1. 代码结构](#11-代码结构)
    - [1.2. 获得指定拓展对象](#12-获得指定拓展对象)
        - [1.2.1. 获得拓展加载器](#121-获得拓展加载器)
            - [1.2.1.1. getExtensionLoader](#1211-getextensionloader)
            - [1.2.1.2. 构造方法](#1212-构造方法)
        - [1.2.2. 获得指定拓展对象](#122-获得指定拓展对象)
            - [1.2.2.1. getExtension](#1221-getextension)
            - [1.2.2.2. createExtension](#1222-createextension)
                - [1.2.2.2.1. 获取所有的拓展类](#12221-获取所有的拓展类)
                    - [1.2.2.2.1.1. getExtensionClasses](#122211-getextensionclasses)
                    - [1.2.2.2.1.2. loadExtensionClasses](#122212-loadextensionclasses)
                    - [1.2.2.2.1.3. loadDirectory](#122213-loaddirectory)
                    - [1.2.2.2.1.4. loadResource](#122214-loadresource)
                    - [1.2.2.2.1.5. loadClass](#122215-loadclass)
        - [1.2.3. injectExtension，Dubbo IOC](#123-injectextensiondubbo-ioc)

<!-- /TOC -->

# 1. SPI解析
## 1.1. 代码结构  
&emsp; Dubbo SPI 在 dubbo-common 的 extension 包实现，如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-28.png)   
&emsp; Dubbo SPI 的相关逻辑被封装在了 ExtensionLoader 类(拓展加载器)中，通过 ExtensionLoader，可以加载指定的实现类。  

## 1.2. 获得指定拓展对象  
&emsp; <font color = "lime">Dubbo SPI 获得指定拓展对象，首先通过 ExtensionLoader 的 getExtensionLoader 方法获取一个 ExtensionLoader 实例，然后再通过 ExtensionLoader 的 getExtension 方法获取拓展类对象。</font>  

### 1.2.1. 获得拓展加载器  
&emsp; 在Dubbo SPI示例代码中：  

```java
ExtensionLoader<Robot> extensionLoader = ExtensionLoader.getExtensionLoader(Robot.class);
```

#### 1.2.1.1. getExtensionLoader  
&emsp; getExtensionLoader(type) 静态方法，根据拓展点的接口，获得拓展加载器。代码如下：  

```java
/**
 * 拓展加载器集合
 *
 * key：拓展接口
 */
 // 【静态属性】
private static final ConcurrentMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<Class<?>, ExtensionLoader<?>>(); 

/**
 * 根据拓展点的接口，获得拓展加载器
 *
 * @param type 接口
 * @param <T> 泛型
 * @return 加载器
 */
@SuppressWarnings("unchecked")
public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
    if (type == null)
        throw new IllegalArgumentException("Extension type == null");
    // 必须是接口
    if (!type.isInterface()) {
        throw new IllegalArgumentException("Extension type(" + type + ") is not interface!");
    }
    // 必须包含 @SPI 注解
    if (!withExtensionAnnotation(type)) {
        throw new IllegalArgumentException("Extension type(" + type +
                ") is not extension, because WITHOUT @" + SPI.class.getSimpleName() + " Annotation!");
    }

    // 获得接口对应的拓展点加载器
    ExtensionLoader<T> loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
    if (loader == null) {
        EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<T>(type));
        loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
    }
}
```
&emsp; 从 EXTENSION_LOADERS 静态中获取拓展接口对应的 ExtensionLoader 对象。若不存在，则创建 ExtensionLoader 对象，并添加到 EXTENSION_LOADERS。  

<!-- 
* 第 12 至 15 行：必须是接口。
* 第 16 至 20 行：调用 #withExtensionAnnotation() 方法，校验必须使用 @SPI 注解标记。
* 第 22 至 27 行：从 EXTENSION_LOADERS 静态中获取拓展接口对应的 ExtensionLoader 对象。若不存在，则创建 ExtensionLoader 对象，并添加到 EXTENSION_LOADERS。
-->

#### 1.2.1.2. 构造方法  
&emsp; 构造方法，代码如下：  

```java
/**
 * 拓展接口。
 * 例如，Protocol
 */
private final Class<?> type;
/**
 * 对象工厂
 *
 * 用于调用 {@link #injectExtension(Object)} 方法，向拓展对象注入依赖属性。
 *
 * 例如，StubProxyFactoryWrapper 中有 `Protocol protocol` 属性。
 */
private final ExtensionFactory objectFactory;

private ExtensionLoader(Class<?> type) {
    this.type = type;
    objectFactory = (type == ExtensionFactory.class ? null : ExtensionLoader.getExtensionLoader(ExtensionFactory.class).getAdaptiveExtension());
}
```

* objectFactory 属性，对象工厂，功能上和 Spring IOC 一致。
    * 用于调用 #injectExtension(instance) 方法时，向创建的拓展注入其依赖的属性。例如，CacheFilter.cacheFactory 属性。
    * 第 3 行：当拓展接口非 ExtensionFactory 时( 如果不加这个判断，会是一个死循环 )，调用 ExtensionLoader#getAdaptiveExtension() 方法，获得 ExtensionFactory 拓展接口的自适应拓展实现对象。为什么呢？在 「ExtensionFactory」 详细解析。

### 1.2.2. 获得指定拓展对象  

```java
Robot optimusPrime = extensionLoader.getExtension("optimusPrime");
```
#### 1.2.2.1. getExtension 

```java
public T getExtension(String name) {
    if (name == null || name.length() == 0)
        throw new IllegalArgumentException("Extension name == null");
    if ("true".equals(name)) {
        // 获取默认的拓展实现类
        return getDefaultExtension();
    }
    // Holder，顾名思义，用于持有目标对象
    Holder<Object> holder = cachedInstances.get(name);
    if (holder == null) {
        cachedInstances.putIfAbsent(name, new Holder<Object>());
        holder = cachedInstances.get(name);
    }
    Object instance = holder.get();
    // 双重检查
    if (instance == null) {
        synchronized (holder) {
            instance = holder.get();
            if (instance == null) {
                // 创建拓展实例
                instance = createExtension(name);
                // 设置实例到 holder 中
                holder.set(instance);
            }
        }
    }
    return (T) instance;
}
```
&emsp; 上面代码的逻辑比较简单，首先检查缓存，缓存未命中则创建拓展对象。下面来看一下创建拓展对象的过程是怎样的。  

#### 1.2.2.2. createExtension

```java
private T createExtension(String name) {
    // 从配置文件中加载所有的拓展类，可得到“配置项名称”到“配置类”的映射关系表
    Class<?> clazz = getExtensionClasses().get(name);
    if (clazz == null) {
        throw findException(name);
    }
    try {
        T instance = (T) EXTENSION_INSTANCES.get(clazz);
        if (instance == null) {
            // 通过反射创建实例
            EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.newInstance());
            instance = (T) EXTENSION_INSTANCES.get(clazz);
        }
        // 向实例中注入依赖
        injectExtension(instance);
        Set<Class<?>> wrapperClasses = cachedWrapperClasses;
        if (wrapperClasses != null && !wrapperClasses.isEmpty()) {
            // 循环创建 Wrapper 实例
            for (Class<?> wrapperClass : wrapperClasses) {
                // 将当前 instance 作为参数传给 Wrapper 的构造方法，并通过反射创建 Wrapper 实例。
                // 然后向 Wrapper 实例中注入依赖，最后将 Wrapper 实例再次赋值给 instance 变量
                instance = injectExtension(
                    (T) wrapperClass.getConstructor(type).newInstance(instance));
            }
        }
        return instance;
    } catch (Throwable t) {
        throw new IllegalStateException("...");
    }
}
```
&emsp; <font color = "lime">createExtension 方法包含了如下的步骤：</font>  

1. 通过 getExtensionClasses 获取所有的拓展类
2. 通过反射创建拓展对象
3. 向拓展对象中注入依赖
4. 将拓展对象包裹在相应的 Wrapper 对象中

&emsp; 以上步骤中，第一个步骤是加载拓展类的关键，<font color = "lime">第三和第四个步骤是 Dubbo IOC 与 AOP 的具体实现。</font>在接下来的章节中，将会重点分析 getExtensionClasses 方法的逻辑，以及简单介绍 Dubbo IOC 的具体实现。  

##### 1.2.2.2.1. 获取所有的拓展类  

###### 1.2.2.2.1.1. getExtensionClasses  
&emsp; 在通过名称获取拓展类之前，首先需要根据配置文件解析出拓展项名称到拓展类的映射关系表（Map<名称, 拓展类>），之后再根据拓展项名称从映射关系表中取出相应的拓展类即可。相关过程的代码分析如下：  

```java
private Map<String, Class<?>> getExtensionClasses() {
    // 从缓存中获取已加载的拓展类
    Map<String, Class<?>> classes = cachedClasses.get();
    // 双重检查
    if (classes == null) {
        synchronized (cachedClasses) {
            classes = cachedClasses.get();
            if (classes == null) {
                // 加载拓展类
                classes = loadExtensionClasses();
                cachedClasses.set(classes);
            }
        }
    }
    return classes;
}
```

&emsp; 这里也是先检查缓存，若缓存未命中，则通过 synchronized 加锁。加锁后再次检查缓存，并判空。此时如果 classes 仍为 null，则通过 loadExtensionClasses 加载拓展类。下面分析 loadExtensionClasses 方法的逻辑。  

###### 1.2.2.2.1.2. loadExtensionClasses

```java
private Map<String, Class<?>> loadExtensionClasses() {
    // 获取 SPI 注解，这里的 type 变量是在调用 getExtensionLoader 方法时传入的
    final SPI defaultAnnotation = type.getAnnotation(SPI.class);
    if (defaultAnnotation != null) {
        String value = defaultAnnotation.value();
        if ((value = value.trim()).length() > 0) {
            // 对 SPI 注解内容进行切分
            String[] names = NAME_SEPARATOR.split(value);
            // 检测 SPI 注解内容是否合法，不合法则抛出异常
            if (names.length > 1) {
                throw new IllegalStateException("more than 1 default extension name on extension...");
            }

            // 设置默认名称，参考 getDefaultExtension 方法
            if (names.length == 1) {
                cachedDefaultName = names[0];
            }
        }
    }

    Map<String, Class<?>> extensionClasses = new HashMap<String, Class<?>>();
    // 加载指定文件夹下的配置文件
    loadDirectory(extensionClasses, DUBBO_INTERNAL_DIRECTORY);
    loadDirectory(extensionClasses, DUBBO_DIRECTORY);
    loadDirectory(extensionClasses, SERVICES_DIRECTORY);
    return extensionClasses;
}
```
&emsp; <font color = "red">loadExtensionClasses 方法总共做了两件事情，一是对 SPI 注解进行解析，二是调用 loadDirectory 方法加载指定文件夹配置文件。</font>  

###### 1.2.2.2.1.3. loadDirectory

```java
private void loadDirectory(Map<String, Class<?>> extensionClasses, String dir) {
    // fileName = 文件夹路径 + type 全限定名 
    String fileName = dir + type.getName();
    try {
        Enumeration<java.net.URL> urls;
        ClassLoader classLoader = findClassLoader();
        // 根据文件名加载所有的同名文件
        if (classLoader != null) {
            urls = classLoader.getResources(fileName);
        } else {
            urls = ClassLoader.getSystemResources(fileName);
        }
        if (urls != null) {
            while (urls.hasMoreElements()) {
                java.net.URL resourceURL = urls.nextElement();
                // 加载资源
                loadResource(extensionClasses, classLoader, resourceURL);
            }
        }
    } catch (Throwable t) {
        logger.error("...");
    }
}
```

&emsp; loadDirectory 方法先通过 classLoader 获取所有资源链接，然后再通过 loadResource 方法加载资源。接下来，看一下 loadResource 方法的实现。  

###### 1.2.2.2.1.4. loadResource

```java
private void loadResource(Map<String, Class<?>> extensionClasses, 
	ClassLoader classLoader, java.net.URL resourceURL) {
    try {
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(resourceURL.openStream(), "utf-8"));
        try {
            String line;
            // 按行读取配置内容
            while ((line = reader.readLine()) != null) {
                // 定位 # 字符
                final int ci = line.indexOf('#');
                if (ci >= 0) {
                    // 截取 # 之前的字符串，# 之后的内容为注释，需要忽略
                    line = line.substring(0, ci);
                }
                line = line.trim();
                if (line.length() > 0) {
                    try {
                        String name = null;
                        int i = line.indexOf('=');
                        if (i > 0) {
                            // 以等于号 = 为界，截取键与值
                            name = line.substring(0, i).trim();
                            line = line.substring(i + 1).trim();
                        }
                        if (line.length() > 0) {
                            // 加载类，并通过 loadClass 方法对类进行缓存
                            loadClass(extensionClasses, resourceURL, 
                                      Class.forName(line, true, classLoader), name);
                        }
                    } catch (Throwable t) {
                        IllegalStateException e = new IllegalStateException("Failed to load extension class...");
                    }
                }
            }
        } finally {
            reader.close();
        }
    } catch (Throwable t) {
        logger.error("Exception when load extension class...");
    }
}
```
&emsp; loadResource 方法用于读取和解析配置文件，并通过反射加载类，最后调用 loadClass 方法进行其他操作。  

###### 1.2.2.2.1.5. loadClass
&emsp; loadClass 方法用于主要用于操作缓存，该方法的逻辑如下：  

```java
private void loadClass(Map<String, Class<?>> extensionClasses, java.net.URL resourceURL, 
    Class<?> clazz, String name) throws NoSuchMethodException {
    
    if (!type.isAssignableFrom(clazz)) {
        throw new IllegalStateException("...");
    }

    // 检测目标类上是否有 Adaptive 注解
    if (clazz.isAnnotationPresent(Adaptive.class)) {
        if (cachedAdaptiveClass == null) {
            // 设置 cachedAdaptiveClass缓存
            cachedAdaptiveClass = clazz;
        } else if (!cachedAdaptiveClass.equals(clazz)) {
            throw new IllegalStateException("...");
        }
        
    // 检测 clazz 是否是 Wrapper 类型
    } else if (isWrapperClass(clazz)) {
        Set<Class<?>> wrappers = cachedWrapperClasses;
        if (wrappers == null) {
            cachedWrapperClasses = new ConcurrentHashSet<Class<?>>();
            wrappers = cachedWrapperClasses;
        }
        // 存储 clazz 到 cachedWrapperClasses 缓存中
        wrappers.add(clazz);
        
    // 程序进入此分支，表明 clazz 是一个普通的拓展类
    } else {
        // 检测 clazz 是否有默认的构造方法，如果没有，则抛出异常
        clazz.getConstructor();
        if (name == null || name.length() == 0) {
            // 如果 name 为空，则尝试从 Extension 注解中获取 name，或使用小写的类名作为 name
            name = findAnnotationName(clazz);
            if (name.length() == 0) {
                throw new IllegalStateException("...");
            }
        }
        // 切分 name
        String[] names = NAME_SEPARATOR.split(name);
        if (names != null && names.length > 0) {
            Activate activate = clazz.getAnnotation(Activate.class);
            if (activate != null) {
                // 如果类上有 Activate 注解，则使用 names 数组的第一个元素作为键，
                // 存储 name 到 Activate 注解对象的映射关系
                cachedActivates.put(names[0], activate);
            }
            for (String n : names) {
                if (!cachedNames.containsKey(clazz)) {
                    // 存储 Class 到名称的映射关系
                    cachedNames.put(clazz, n);
                }
                Class<?> c = extensionClasses.get(n);
                if (c == null) {
                    // 存储名称到 Class 的映射关系
                    extensionClasses.put(n, clazz);
                } else if (c != clazz) {
                    throw new IllegalStateException("...");
                }
            }
        }
    }
}
```
&emsp; 如上，loadClass 方法操作了不同的缓存，比如 cachedAdaptiveClass、cachedWrapperClasses 和 cachedNames 等等。除此之外，该方法没有其他什么逻辑了。  
&emsp; 到此，关于缓存类加载的过程就分析完了。  

### 1.2.3. injectExtension，Dubbo IOC
&emsp; Dubbo IOC 是通过 setter 方法注入依赖。Dubbo 首先会通过反射获取到实例的所有方法，然后再遍历方法列表，检测方法名是否具有 setter 方法特征。若有，则通过 ObjectFactory 获取依赖对象，最后通过反射调用 setter 方法将依赖设置到目标对象中。整个过程对应的代码如下：  

```java
private T injectExtension(T instance) {
    try {
        if (objectFactory != null) {
            // 遍历目标类的所有方法
            for (Method method : instance.getClass().getMethods()) {
                // 检测方法是否以 set 开头，且方法仅有一个参数，且方法访问级别为 public
                if (method.getName().startsWith("set")
                    && method.getParameterTypes().length == 1
                    && Modifier.isPublic(method.getModifiers())) {
                    // 获取 setter 方法参数类型
                    Class<?> pt = method.getParameterTypes()[0];
                    try {
                        // 获取属性名，比如 setName 方法对应属性名 name
                        String property = method.getName().length() > 3 ? 
                            method.getName().substring(3, 4).toLowerCase() + 
                            	method.getName().substring(4) : "";
                        // 从 ObjectFactory 中获取依赖对象
                        Object object = objectFactory.getExtension(pt, property);
                        if (object != null) {
                            // 通过反射调用 setter 方法设置依赖
                            method.invoke(instance, object);
                        }
                    } catch (Exception e) {
                        logger.error("fail to inject via method...");
                    }
                }
            }
        }
    } catch (Exception e) {
        logger.error(e.getMessage(), e);
    }
    return instance;
}
```
&emsp; 在上面代码中，objectFactory 变量的类型为 AdaptiveExtensionFactory，AdaptiveExtensionFactory 内部维护了一个 ExtensionFactory 列表，用于存储其他类型的 ExtensionFactory。Dubbo 目前提供了两种 ExtensionFactory，分别是 SpiExtensionFactory 和 SpringExtensionFactory。前者用于创建自适应的拓展，后者是用于从 Spring 的 IOC 容器中获取所需的拓展。这两个类的类的代码不是很复杂，这里就不一一分析了。  
&emsp; Dubbo IOC 目前仅支持 setter 方式注入。  
