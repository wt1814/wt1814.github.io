



# SPI源码  

<!-- 

Dubbo SPI  
http://dubbo.apache.org/zh-cn/docs/source_code_guide/dubbo-spi.html

自适应拓展机制
http://dubbo.apache.org/zh-cn/docs/source_code_guide/adaptive-extension.html

-->


## 代码结构  
&emsp; Dubbo SPI 在 dubbo-common 的 extension 包实现，如下图所示：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-22.png)   

## ExtensionLoader  
com.alibaba.dubbo.common.extension.ExtensionLoader ，拓展加载器。这是Dubbo SPI的核心。  

### 属性  

```java
private static final String SERVICES_DIRECTORY = "META-INF/services/";
private static final String DUBBO_DIRECTORY = "META-INF/dubbo/";
private static final String DUBBO_INTERNAL_DIRECTORY = DUBBO_DIRECTORY + "internal/";
private static final Pattern NAME_SEPARATOR = Pattern.compile("\\s*[,]+\\s*");

// ============================== 静态属性 ==============================

/**
* 拓展加载器集合
*
* key：拓展接口
*/
private static final ConcurrentMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<Class<?>, ExtensionLoader<?>>();
/**
* 拓展实现类集合
*
* key：拓展实现类
* value：拓展对象。
*
* 例如，key 为 Class<AccessLogFilter>
*  value 为 AccessLogFilter 对象
*/
private static final ConcurrentMap<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<Class<?>, Object>();

// ============================== 对象属性 ==============================

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
/**
* 缓存的拓展名与拓展类的映射。
*
* 和 {@link #cachedClasses} 的 KV 对调。
*
* 通过 {@link #loadExtensionClasses} 加载
*/
private final ConcurrentMap<Class<?>, String> cachedNames = new ConcurrentHashMap<Class<?>, String>();
/**
* 缓存的拓展实现类集合。
*
* 不包含如下两种类型：
*  1. 自适应拓展实现类。例如 AdaptiveExtensionFactory
*  2. 带唯一参数为拓展接口的构造方法的实现类，或者说拓展 Wrapper 实现类。例如，ProtocolFilterWrapper 。
*   拓展 Wrapper 实现类，会添加到 {@link #cachedWrapperClasses} 中
*
* 通过 {@link #loadExtensionClasses} 加载
*/
private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<Map<String, Class<?>>>();

/**
* 拓展名与 @Activate 的映射
*
* 例如，AccessLogFilter。
*
* 用于 {@link #getActivateExtension(URL, String)}
*/
private final Map<String, Activate> cachedActivates = new ConcurrentHashMap<String, Activate>();
/**
* 缓存的拓展对象集合
*
* key：拓展名
* value：拓展对象
*
* 例如，Protocol 拓展
*      key：dubbo value：DubboProtocol
*      key：injvm value：InjvmProtocol
*
* 通过 {@link #loadExtensionClasses} 加载
*/
private final ConcurrentMap<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<String, Holder<Object>>();
/**
* 缓存的自适应( Adaptive )拓展对象
*/
private final Holder<Object> cachedAdaptiveInstance = new Holder<Object>();
/**
* 缓存的自适应拓展对象的类
*
* {@link #getAdaptiveExtensionClass()}
*/
private volatile Class<?> cachedAdaptiveClass = null;
/**
* 缓存的默认拓展名
*
* 通过 {@link SPI} 注解获得
*/
private String cachedDefaultName;
/**
 * 创建 {@link #cachedAdaptiveInstance} 时发生的异常。
 *
 * 发生异常后，不再创建，参见 {@link #createAdaptiveExtension()}
 */
private volatile Throwable createAdaptiveInstanceError;

/**
 * 拓展 Wrapper 实现类集合
 *
 * 带唯一参数为拓展接口的构造方法的实现类
 *
 * 通过 {@link #loadExtensionClasses} 加载
 */
private Set<Class<?>> cachedWrapperClasses;

/**
 * 拓展名 与 加载对应拓展类发生的异常 的 映射
 *
 * key：拓展名
 * value：异常
 *
 * 在 {@link #loadFile(Map, String)} 时，记录
 */
private Map<String, IllegalStateException> exceptions = new ConcurrentHashMap<String, IllegalStateException>();
```
* 第 1 至 5 行：在 META-INF/dubbo/internal/ 和 META-INF/dubbo/ 目录下，放置 接口全限定名 配置文件，每行内容为：拓展名=拓展实现类全限定名。

    * META-INF/dubbo/internal/ 目录下，从名字上可以看出，用于 Dubbo 内部提供的拓展实现。下图是一个例子：
    * META-INF/dubbo/ 目录下，用于用户自定义的拓展实现。
    * META-INF/service/ 目录下，Java SPI 的配置目录。在 「加载拓展配置」 中，会看到 Dubbo SPI 对 Java SPI 做了兼容。  

* 第 7 行：NAME_SEPARATOR ，拓展名分隔符，使用逗号。
* 第 9 至 124 行，我们将属性分成了两类：1）静态属性；2）对象属性。这是为啥呢？

    * 【静态属性】一方面，ExtensionLoader 是 ExtensionLoader 的管理容器。一个拓展( 拓展接口 )对应一个 ExtensionLoader 对象。例如，Protocol 和 Filter 分别对应一个 ExtensionLoader 对象。
    * 【对象属性】另一方面，一个拓展通过其 ExtensionLoader 对象，加载它的拓展实现们。我们会发现多个属性都是 “cached“ 开头。ExtensionLoader 考虑到性能和资源的优化，读取拓展配置后，会首先进行缓存。等到 Dubbo 代码真正用到对应的拓展实现时，进行拓展实现的对象的初始化。并且，初始化完成后，也会进行缓存。也就是说：
        * 缓存加载的拓展配置
        * 缓存创建的拓展实现对象 

### 获得拓展配置  
#### getExtensionClasses  
#getExtensionClasses() 方法，获得拓展实现类数组。  

```java
private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<Map<String, Class<?>>>();
private volatile Class<?> cachedAdaptiveClass = null;
private Set<Class<?>> cachedWrapperClasses;

/**
* 获得拓展实现类数组
*
* @return 拓展实现类数组
*/
private Map<String, Class<?>> getExtensionClasses() {
   // 从缓存中，获得拓展实现类数组
   Map<String, Class<?>> classes = cachedClasses.get();
   if (classes == null) {
       synchronized (cachedClasses) {
           classes = cachedClasses.get();
           if (classes == null) {
               // 从配置文件中，加载拓展实现类数组
               classes = loadExtensionClasses();
               // 设置到缓存中
               cachedClasses.set(classes);
           }
       }
   }
   return classes;
}
```

* cachedClasses 属性，缓存的拓展实现类集合。它不包含如下两种类型的拓展实现：
    * 自适应拓展实现类。例如 AdaptiveExtensionFactory 。
        * 拓展 Adaptive 实现类，会添加到 cachedAdaptiveClass 属性中。
    * 带唯一参数为拓展接口的构造方法的实现类，或者说拓展 Wrapper 实现类。例如，ProtocolFilterWrapper 。
        * 拓展 Wrapper 实现类，会添加到 cachedWrapperClasses 属性中。
    * 总结来说，cachedClasses + cachedAdaptiveClass + cachedWrapperClasses 才是完整缓存的拓展实现类的配置。
* 第 7 至 11 行：从缓存中，获得拓展实现类数组。
* 第 12 至 14 行：当缓存不存在时，调用 #loadExtensionClasses() 方法，从配置文件中，加载拓展实现类数组。
* 第 16 行：设置加载的实现类数组，到缓存中。

#### loadExtensionClasses  
#loadExtensionClasses() 方法，从多个配置文件中，加载拓展实现类数组。  

```java
/**
 * 加载拓展实现类数组
 *
 * 无需声明 synchronized ，因为唯一调用该方法的 {@link #getExtensionClasses()} 已经声明。
 * // synchronized in getExtensionClasses
 *
 * @return 拓展实现类数组
 */
private Map<String, Class<?>> loadExtensionClasses() {
    // 通过 @SPI 注解，获得默认的拓展实现类名
    final SPI defaultAnnotation = type.getAnnotation(SPI.class);
    if (defaultAnnotation != null) {
        String value = defaultAnnotation.value();
        if ((value = value.trim()).length() > 0) {
            String[] names = NAME_SEPARATOR.split(value);
            if (names.length > 1) {
                throw new IllegalStateException("more than 1 default extension name on extension " + type.getName()
                        + ": " + Arrays.toString(names));
            }
            if (names.length == 1) cachedDefaultName = names[0];
        }
    }

    // 从配置文件中，加载拓展实现类数组
    Map<String, Class<?>> extensionClasses = new HashMap<String, Class<?>>();
    loadFile(extensionClasses, DUBBO_INTERNAL_DIRECTORY);
    loadFile(extensionClasses, DUBBO_DIRECTORY);
    loadFile(extensionClasses, SERVICES_DIRECTORY);
    return extensionClasses;
}
```

* 第 10 至 22 行：通过 @SPI 注解，获得拓展接口对应的默认的拓展实现类名。在 「 @SPI」 详细解析。
* 第 25 至 29 行：调用 #loadFile(extensionClasses, dir) 方法，从配置文件中，加载拓展实现类数组。注意，此处配置文件的加载顺序。

#### loadFile  
#loadFile(extensionClasses, dir) 方法，从一个配置文件中，加载拓展实现类数组。代码如下：  

```java
/**
 * 缓存的自适应拓展对象的类
 *
 * {@link #getAdaptiveExtensionClass()}
 */
private volatile Class<?> cachedAdaptiveClass = null;

/**
 * 拓展 Wrapper 实现类集合
 *
 * 带唯一参数为拓展接口的构造方法的实现类
 *
 * 通过 {@link #loadExtensionClasses} 加载
 */
private Set<Class<?>> cachedWrapperClasses;

/**
 * 拓展名与 @Activate 的映射
 *
 * 例如，AccessLogFilter。
 *
 * 用于 {@link #getActivateExtension(URL, String)}
 */
private final Map<String, Activate> cachedActivates = new ConcurrentHashMap<String, Activate>();

/**
 * 缓存的拓展名与拓展类的映射。
 *
 * 和 {@link #cachedClasses} 的 KV 对调。
 *
 * 通过 {@link #loadExtensionClasses} 加载
 */
private final ConcurrentMap<Class<?>, String> cachedNames = new ConcurrentHashMap<Class<?>, String>();

/**
 * 拓展名 与 加载对应拓展类发生的异常 的 映射
 *
 * key：拓展名
 * value：异常
 *
 * 在 {@link #loadFile(Map, String)} 时，记录
 */
private Map<String, IllegalStateException> exceptions = new ConcurrentHashMap<String, IllegalStateException>();

/**
 * 从一个配置文件中，加载拓展实现类数组。
 *
 * @param extensionClasses 拓展类名数组
 * @param dir 文件名
 */
private void loadFile(Map<String, Class<?>> extensionClasses, String dir) {
    // 完整的文件名
    String fileName = dir + type.getName();
    try {
        Enumeration<java.net.URL> urls;
        // 获得文件名对应的所有文件数组
        ClassLoader classLoader = findClassLoader();
        if (classLoader != null) {
            urls = classLoader.getResources(fileName);
        } else {
            urls = ClassLoader.getSystemResources(fileName);
        }
        // 遍历文件数组
        if (urls != null) {
            while (urls.hasMoreElements()) {
                java.net.URL url = urls.nextElement();
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
                    try {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            // 跳过当前被注释掉的情况，例如 #spring=xxxxxxxxx
                            final int ci = line.indexOf('#');
                            if (ci >= 0) line = line.substring(0, ci);
                            line = line.trim();
                            if (line.length() > 0) {
                                try {
                                    // 拆分，key=value 的配置格式
                                    String name = null;
                                    int i = line.indexOf('=');
                                    if (i > 0) {
                                        name = line.substring(0, i).trim();
                                        line = line.substring(i + 1).trim();
                                    }
                                    if (line.length() > 0) {
                                        // 判断拓展实现，是否实现拓展接口
                                        Class<?> clazz = Class.forName(line, true, classLoader);
                                        if (!type.isAssignableFrom(clazz)) {
                                            throw new IllegalStateException("Error when load extension class(interface: " +
                                                    type + ", class line: " + clazz.getName() + "), class "
                                                    + clazz.getName() + "is not subtype of interface.");
                                        }
                                        // 缓存自适应拓展对象的类到 `cachedAdaptiveClass`
                                        if (clazz.isAnnotationPresent(Adaptive.class)) {
                                            if (cachedAdaptiveClass == null) {
                                                cachedAdaptiveClass = clazz;
                                            } else if (!cachedAdaptiveClass.equals(clazz)) {
                                                throw new IllegalStateException("More than 1 adaptive class found: "
                                                        + cachedAdaptiveClass.getClass().getName()
                                                        + ", " + clazz.getClass().getName());
                                            }
                                        } else {
                                            // 缓存拓展 Wrapper 实现类到 `cachedWrapperClasses`
                                            try {
                                                clazz.getConstructor(type);
                                                Set<Class<?>> wrappers = cachedWrapperClasses;
                                                if (wrappers == null) {
                                                    cachedWrapperClasses = new ConcurrentHashSet<Class<?>>();
                                                    wrappers = cachedWrapperClasses;
                                                }
                                                wrappers.add(clazz);
                                            // 缓存拓展实现类到 `extensionClasses`
                                            } catch (NoSuchMethodException e) {
                                                clazz.getConstructor();
                                                // 未配置拓展名，自动生成。例如，DemoFilter 为 demo 。主要用于兼容 Java SPI 的配置。
                                                if (name == null || name.length() == 0) {
                                                    name = findAnnotationName(clazz);
                                                    if (name == null || name.length() == 0) {
                                                        if (clazz.getSimpleName().length() > type.getSimpleName().length()
                                                                && clazz.getSimpleName().endsWith(type.getSimpleName())) {
                                                            name = clazz.getSimpleName().substring(0, clazz.getSimpleName().length() - type.getSimpleName().length()).toLowerCase();
                                                        } else {
                                                            throw new IllegalStateException("No such extension name for the class " + clazz.getName() + " in the config " + url);
                                                        }
                                                    }
                                                }
                                                // 获得拓展名，可以是数组，有多个拓展名。
                                                String[] names = NAME_SEPARATOR.split(name);
                                                if (names != null && names.length > 0) {
                                                    // 缓存 @Activate 到 `cachedActivates` 。
                                                    Activate activate = clazz.getAnnotation(Activate.class);
                                                    if (activate != null) {
                                                        cachedActivates.put(names[0], activate);
                                                    }
                                                    for (String n : names) {
                                                        // 缓存到 `cachedNames`
                                                        if (!cachedNames.containsKey(clazz)) {
                                                            cachedNames.put(clazz, n);
                                                        }
                                                        // 缓存拓展实现类到 `extensionClasses`
                                                        Class<?> c = extensionClasses.get(n);
                                                        if (c == null) {
                                                            extensionClasses.put(n, clazz);
                                                        } else if (c != clazz) {
                                                            throw new IllegalStateException("Duplicate extension " + type.getName() + " name " + n + " on " + c.getName() + " and " + clazz.getName());
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } catch (Throwable t) {
                                    // 发生异常，记录到异常集合
                                    IllegalStateException e = new IllegalStateException("Failed to load extension class(interface: " + type + ", class line: " + line + ") in " + url + ", cause: " + t.getMessage(), t);
                                    exceptions.put(line, e);
                                }
                            }
                        } // end of while read lines
                    } finally {
                        reader.close();
                    }
                } catch (Throwable t) {
                    logger.error("Exception when load extension class(interface: " +
                            type + ", class file: " + url + ") in " + url, t);
                }
            } // end of while urls
        }
    } catch (Throwable t) {
        logger.error("Exception when load extension class(interface: " +
                type + ", description file: " + fileName + ").", t);
    }
}
```

* 第 9 行：获得完整的文件名( 相对路径 )。例如："META-INF/dubbo/internal/com.alibaba.dubbo.common.extension.ExtensionFactory" 。
* 第 12 至 18 行：获得文件名对应的所有文件 URL 数组。例如：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-23.png)   
* 第 21 至 24 行：逐个文件 URL 遍历。
* 第 27 行：逐行遍历。
* 第 29 至 32 行：跳过当前被 "#" 注释掉的情况，例如 #spring=xxxxxxxxx 。
* 第 34 至 40 行：按照 key=value 的配置拆分。其中 name 为拓展名，line 为拓展实现类名。注意，上文我们提到过 Dubbo SPI 会兼容 Java SPI 的配置格式，那么按照此处的解析方式，name 会为空。这种情况下，拓展名会自动生成，详细见第 71 至 82 行的代码。
* 第 42 至 48 行：判断拓展实现类，需要实现拓展接口。
* 第 50 至 57 行：缓存自适应拓展对象的类到 cachedAdaptiveClass 属性。在 「6. @Adaptive」 详细解析。
* 第 59 至 67 行：缓存拓展 Wrapper 实现类到 cachedWrapperClasses 属性。
    * 第 61 行：调用 Class#getConstructor(Class<?>... parameterTypes) 方法，通过反射的方式，参数为拓展接口，判断当前配置的拓展实现类为拓展 Wrapper 实现类。若成功（未抛出异常），则代表符合条件。例如，ProtocolFilterWrapper(Protocol protocol) 这个构造方法。
* 第 69 至 105 行：若获得构造方法失败，则代表是普通的拓展实现类，缓存到 extensionClasses 变量中。
    * 第 70 行：调用 Class#getConstructor(Class<?>... parameterTypes) 方法，获得参数为空的构造方法。
    * 第 72 至 82 行：未配置拓展名，自动生成。适用于 Java SPI 的配置方式。例如，xxx.yyy.DemoFilter 生成的拓展名为 demo 。
        * 第 73 行：通过 @Extension 注解的方式设置拓展名的方式已经废弃，胖友可以无视该方法。
* 第 84 行：获得拓展名。使用逗号进行分割，即多个拓展名可以对应同一个拓展实现类。
* 第 86 至 90 行：缓存 @Activate 到 cachedActivates 。在 「7. @Activate」 详细解析。
* 第 93 至 95 行：缓存到 cachedNames 属性。
* 第 96 至 102 行：缓存拓展实现类到 extensionClasses 变量。注意，相同拓展名，不能对应多个不同的拓展实现。
* 第 108 至 112 行：若发生异常，记录到异常集合 exceptions 属性。


### 获得拓展加载器  
在 Dubbo 的代码里，常常能看到如下的代码：  

```java
ExtensionLoader.getExtensionLoader(Protocol.class).getExtension(name)
```

#### getExtensionLoader  
#getExtensionLoader(type) 静态方法，根据拓展点的接口，获得拓展加载器。代码如下：  

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

* 第 12 至 15 行：必须是接口。
* 第 16 至 20 行：调用 #withExtensionAnnotation() 方法，校验必须使用 @SPI 注解标记。
* 第 22 至 27 行：从 EXTENSION_LOADERS 静态中获取拓展接口对应的 ExtensionLoader 对象。若不存在，则创建 ExtensionLoader 对象，并添加到 EXTENSION_LOADERS。

#### 构造方法  
构造方法，代码如下：  

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

-----

### 获得指定拓展对象  
在 Dubbo 的代码里，常常能看到如下的代码：  

```java
ExtensionLoader.getExtensionLoader(Protocol.class).getExtension(name)
```

#### getExtension  
#getExtension() 方法，返回指定名字的扩展对象。如果指定名字的扩展不存在，则抛异常 IllegalStateException 。代码如下：  

```java

/**
 * 缓存的拓展对象集合
 *
 * key：拓展名
 * value：拓展对象
 *
 * 例如，Protocol 拓展
 *          key：dubbo value：DubboProtocol
 *          key：injvm value：InjvmProtocol
 *
 * 通过 {@link #loadExtensionClasses} 加载
 */
private final ConcurrentMap<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<String, Holder<Object>>();

/**
 * Find the extension with the given name. If the specified name is not found, then {@link IllegalStateException}
 * will be thrown.
 */
/**
 * 返回指定名字的扩展对象。如果指定名字的扩展不存在，则抛异常 {@link IllegalStateException}.
 *
 * @param name 拓展名
 * @return 拓展对象
 */
@SuppressWarnings("unchecked")
public T getExtension(String name) {
    if (name == null || name.length() == 0)
        throw new IllegalArgumentException("Extension name == null");
    // 查找 默认的 拓展对象
    if ("true".equals(name)) {
        return getDefaultExtension();
    }
    // 从 缓存中 获得对应的拓展对象
    Holder<Object> holder = cachedInstances.get(name);
    if (holder == null) {
        cachedInstances.putIfAbsent(name, new Holder<Object>());
        holder = cachedInstances.get(name);
    }
    Object instance = holder.get();
    if (instance == null) {
        synchronized (holder) {
            instance = holder.get();
            // 从 缓存中 未获取到，进行创建缓存对象。
            if (instance == null) {
                instance = createExtension(name);
                // 设置创建对象到缓存中
                holder.set(instance);
            }
        }
    }
    return (T) instance;
}
```

* 第 15 至 18 行：调用 #getDefaultExtension() 方法，查询默认的拓展对象。在该方法的实现代码中，简化代码为 getExtension(cachedDefaultName); 。
* 第 19 至 28 行：从缓存中，获得拓展对象。
* 第 29 至 31 行：当缓存不存在时，调用 #createExtension(name) 方法，创建拓展对象。
* 第 33 行：添加创建的拓展对象，到缓存中。

#### createExtension  
#createExtension(name) 方法，创建拓展名的拓展对象，并缓存。代码如下：  

```java
/**
 * 拓展实现类集合
 *
 * key：拓展实现类
 * value：拓展对象。
 *
 * 例如，key 为 Class<AccessLogFilter>
 *      value 为 AccessLogFilter 对象
 */
private static final ConcurrentMap<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<Class<?>, Object>();
    
/**
 * 创建拓展名的拓展对象，并缓存。
 *
 * @param name 拓展名
 * @return 拓展对象
 */
@SuppressWarnings("unchecked")
private T createExtension(String name) {
    // 获得拓展名对应的拓展实现类
    Class<?> clazz = getExtensionClasses().get(name);
    if (clazz == null) {
        throw findException(name); // 抛出异常
    }
    try {
        // 从缓存中，获得拓展对象。
        T instance = (T) EXTENSION_INSTANCES.get(clazz);
        if (instance == null) {
            // 当缓存不存在时，创建拓展对象，并添加到缓存中。
            EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.newInstance());
            instance = (T) EXTENSION_INSTANCES.get(clazz);
        }
        // 注入依赖的属性
        injectExtension(instance);
        // 创建 Wrapper 拓展对象
        Set<Class<?>> wrapperClasses = cachedWrapperClasses;
        if (wrapperClasses != null && !wrapperClasses.isEmpty()) {
            for (Class<?> wrapperClass : wrapperClasses) {
                instance = injectExtension((T) wrapperClass.getConstructor(type).newInstance(instance));
            }
        }
        return instance;
    } catch (Throwable t) {
        throw new IllegalStateException("Extension instance(name: " + name + ", class: " +
                type + ")  could not be instantiated: " + t.getMessage(), t);
    }
}
```
* 第 9 至 13 行：获得拓展名对应的拓展实现类。若不存在，调用 #findException(name) 方法，抛出异常。
* 第 16 行：从缓存 EXTENSION_INSTANCES 静态属性中，获得拓展对象。
* 第 17 至 21 行：当缓存不存在时，创建拓展对象，并添加到 EXTENSION_INSTANCES 中。因为 #getExtension(name) 方法中已经加 synchronized 修饰，所以此处不用同步。
* 第 23 行：调用 #injectExtension(instance) 方法，向创建的拓展注入其依赖的属性。
* 第 24 至 30 行：创建 Wrapper 拓展对象，将 instance 包装在其中。  

    Wrapper 类同样实现了扩展点接口，但是 Wrapper 不是扩展点的真正实现。它的用途主要是用于从 ExtensionLoader 返回扩展点时，包装在真正的扩展点实现外。即从 ExtensionLoader 中返回的实际上是 Wrapper 类的实例，Wrapper 持有了实际的扩展点实现类。
    扩展点的 Wrapper 类可以有多个，也可以根据需要新增。
    通过 Wrapper 类可以把所有扩展点公共逻辑移至 Wrapper 中。新加的 Wrapper 在所有的扩展点上添加了逻辑，有些类似 AOP，即 Wrapper 代理了扩展点。

* 例如：ListenerExporterWrapper、ProtocolFilterWrapper 。  

#### injectExtension  
#injectExtension(instance) 方法，注入依赖的属性。代码如下：  

```java
/**
 * 注入依赖的属性
 *
 * @param instance 拓展对象
 * @return 拓展对象
 */
private T injectExtension(T instance) {
    try {
        if (objectFactory != null) {
            for (Method method : instance.getClass().getMethods()) {
                if (method.getName().startsWith("set")
                        && method.getParameterTypes().length == 1
                        && Modifier.isPublic(method.getModifiers())) { // setting && public 方法
                    // 获得属性的类型
                    Class<?> pt = method.getParameterTypes()[0];
                    try {
                        // 获得属性
                        String property = method.getName().length() > 3 ? method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4) : "";
                        // 获得属性值
                        Object object = objectFactory.getExtension(pt, property);
                        // 设置属性值
                        if (object != null) {
                            method.invoke(instance, object);
                        }
                    } catch (Exception e) {
                        logger.error("fail to inject via method " + method.getName()
                                + " of interface " + type.getName() + ": " + e.getMessage(), e);
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

* 第 9 行：必须有 objectFactory 属性，即 ExtensionFactory 的拓展对象，不需要注入依赖的属性。
* 第 10 至 13 行：反射获得所有的方法，仅仅处理 public setting 方法。
* 第 15 行：获得属性的类型。
* 第 18 行：获得属性名。
* 第 20 行：获得属性值。注意，此处虽然调用的是 ExtensionFactory#getExtension(type, name) 方法，实际获取的不仅仅是拓展对象，也可以是 Spring Bean 对象。答案在 「ExtensionFactory」 揭晓。
* 第 21 至 24 行：设置属性值。

### 获得自适应的拓展对象  
在 Dubbo 的代码里，常常能看到如下的代码：  

```java
ExtensionLoader.getExtensionLoader(Protocol.class).getAdaptiveExtension()
```
#### getAdaptiveExtension  
#getAdaptiveExtension() 方法，获得自适应拓展对象。  

```java
/**
 * 缓存的自适应( Adaptive )拓展对象
 */
private final Holder<Object> cachedAdaptiveInstance = new Holder<Object>();

/**
 * 创建 {@link #cachedAdaptiveInstance} 时发生的异常。
 *
 * 发生异常后，不再创建，参见 {@link #createAdaptiveExtension()}
 */
private volatile Throwable createAdaptiveInstanceError;

/**
 * 获得自适应拓展对象
 *
 * @return 拓展对象
 */
@SuppressWarnings("unchecked")
public T getAdaptiveExtension() {
    // 从缓存中，获得自适应拓展对象
    Object instance = cachedAdaptiveInstance.get();
    if (instance == null) {
        // 若之前未创建报错，
        if (createAdaptiveInstanceError == null) {
            synchronized (cachedAdaptiveInstance) {
                instance = cachedAdaptiveInstance.get();
                if (instance == null) {
                    try {
                        // 创建自适应拓展对象
                        instance = createAdaptiveExtension();
                        // 设置到缓存
                        cachedAdaptiveInstance.set(instance);
                    } catch (Throwable t) {
                        // 记录异常
                        createAdaptiveInstanceError = t;
                        throw new IllegalStateException("fail to create adaptive instance: " + t.toString(), t);
                    }
                }
            }
        // 若之前创建报错，则抛出异常 IllegalStateException
        } else {
            throw new IllegalStateException("fail to create adaptive instance: " + createAdaptiveInstanceError.toString(), createAdaptiveInstanceError);
        }
    }
    return (T) instance;
}
```

* 第 9 行：从缓存 cachedAdaptiveInstance 属性中，获得自适应拓展对象。
* 第 28 至 30 行：若之前创建报错，则抛出异常 IllegalStateException 。
* 第 14 至 20 行：当缓存不存在时，调用 #createAdaptiveExtension() 方法，创建自适应拓展对象，并添加到 cachedAdaptiveInstance 中。
* 第 22 至 24 行：若创建发生异常，记录异常到 createAdaptiveInstanceError ，并抛出异常 IllegalStateException 。

#### createAdaptiveExtension  
#createAdaptiveExtension() 方法，创建自适应拓展对象。代码如下：  

```java
/**
 * 创建自适应拓展对象
 *
 * @return 拓展对象
 */
@SuppressWarnings("unchecked")
private T createAdaptiveExtension() {
    try {
        return injectExtension((T) getAdaptiveExtensionClass().newInstance());
    } catch (Exception e) {
        throw new IllegalStateException("Can not create adaptive extension " + type + ", cause: " + e.getMessage(), e);
    }
}
```

* 调用 #getAdaptiveExtensionClass() 方法，获得自适应拓展类。
* 调用 Class#newInstance() 方法，创建自适应拓展对象。
* 调用 #injectExtension(instance) 方法，向创建的自适应拓展对象，注入依赖的属性。

#### getAdaptiveExtensionClass  
#getAdaptiveExtensionClass() 方法，获得自适应拓展类。代码如下：  

```java
/**
 * @return 自适应拓展类
 */
private Class<?> getAdaptiveExtensionClass() {
    getExtensionClasses();
    if (cachedAdaptiveClass != null) {
        return cachedAdaptiveClass;
    }
    return cachedAdaptiveClass = createAdaptiveExtensionClass();
}
```

* 【@Adaptive 的第一种】第 6 至 8 行：若 cachedAdaptiveClass 已存在，直接返回。的第一种情况。
* 【@Adaptive 的第二种】第 9 行：调用 #createAdaptiveExtensionClass() 方法，自动生成自适应拓展的代码实现，并编译后返回该类。

#### createAdaptiveExtensionClassCode  
#createAdaptiveExtensionClassCode() 方法，自动生成自适应拓展的代码实现，并编译后返回该类。  

```java
/**
 * 自动生成自适应拓展的代码实现，并编译后返回该类。
 *
 * @return 类
 */
private Class<?> createAdaptiveExtensionClass() {
    // 自动生成自适应拓展的代码实现的字符串
    String code = createAdaptiveExtensionClassCode();
    // 编译代码，并返回该类
    ClassLoader classLoader = findClassLoader();
    com.alibaba.dubbo.common.compiler.Compiler compiler = ExtensionLoader.getExtensionLoader(com.alibaba.dubbo.common.compiler.Compiler.class).getAdaptiveExtension();
    return compiler.compile(code, classLoader);
}
```

* 第 8 行：调用 #createAdaptiveExtensionClassCode 方法，自动生成自适应拓展的代码实现的字符串。
    * 代码比较简单，已经添加详细注释，胖友点击查看。
    * 如下是 ProxyFactory 的自适应拓展的代码实现的字符串生成例子
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-24.png)   
* 第 9 至 12 行：使用 Dubbo SPI 加载 Compier 拓展接口对应的拓展实现对象，后调用 Compiler#compile(code, classLoader) 方法，进行编译。  

### 获得激活的拓展对象数组  
在 Dubbo 的代码里，看到使用代码如下：  

```java
List<Filter> filters = ExtensionLoader.getExtensionLoader(Filter.class).getActivateExtension(invoker.getUrl(), key, group);
```

#### getExtensionLoader  
#getExtensionLoader(url, key, group) 方法，获得符合自动激活条件的拓展对象数组。  

```java
/**
 * This is equivalent to {@code getActivateExtension(url, url.getParameter(key).split(","), null)}
 *
 * 获得符合自动激活条件的拓展对象数组
 *
 * @param url   url
 * @param key   url parameter key which used to get extension point names
 *              Dubbo URL 参数名
 * @param group group
 *              过滤分组名
 * @return extension list which are activated.
 * @see #getActivateExtension(com.alibaba.dubbo.common.URL, String[], String)
 */
public List<T> getActivateExtension(URL url, String key, String group) {
    // 从 Dubbo URL 获得参数值
    String value = url.getParameter(key);
    // 获得符合自动激活条件的拓展对象数组
    return getActivateExtension(url, value == null || value.length() == 0 ? null : Constants.COMMA_SPLIT_PATTERN.split(value), group);
}

/**
 * Get activate extensions.
 *
 * 获得符合自动激活条件的拓展对象数组
 *
 * @param url    url
 * @param values extension point names
 * @param group  group
 * @return extension list which are activated
 * @see com.alibaba.dubbo.common.extension.Activate
 */
public List<T> getActivateExtension(URL url, String[] values, String group) {
    List<T> exts = new ArrayList<T>();
    List<String> names = values == null ? new ArrayList<String>(0) : Arrays.asList(values);
    // 处理自动激活的拓展对象们
    // 判断不存在配置 `"-name"` 。例如，<dubbo:service filter="-default" /> ，代表移除所有默认过滤器。
    if (!names.contains(Constants.REMOVE_VALUE_PREFIX + Constants.DEFAULT_KEY)) {
        // 获得拓展实现类数组
        getExtensionClasses();
        // 循环
        for (Map.Entry<String, Activate> entry : cachedActivates.entrySet()) {
            String name = entry.getKey();
            Activate activate = entry.getValue();
            if (isMatchGroup(group, activate.group())) { // 匹配分组
                // 获得拓展对象
                T ext = getExtension(name);
                if (!names.contains(name) // 不包含在自定义配置里。如果包含，会在下面的代码处理。
                        && !names.contains(Constants.REMOVE_VALUE_PREFIX + name) // 判断是否配置移除。例如 <dubbo:service filter="-monitor" />，则 MonitorFilter 会被移除
                        && isActive(activate, url)) { // 判断是否激活
                    exts.add(ext);
                }
            }
        }
        // 排序
        Collections.sort(exts, ActivateComparator.COMPARATOR);
    }
    // 处理自定义配置的拓展对象们。例如在 <dubbo:service filter="demo" /> ，代表需要加入 DemoFilter （这个是笔者自定义的）。
    List<T> usrs = new ArrayList<T>();
    for (int i = 0; i < names.size(); i++) {
        String name = names.get(i);
        if (!name.startsWith(Constants.REMOVE_VALUE_PREFIX) && !names.contains(Constants.REMOVE_VALUE_PREFIX + name)) { // 判断非移除的
            // 将配置的自定义在自动激活的拓展对象们前面。例如，<dubbo:service filter="demo,default,demo2" /> ，则 DemoFilter 就会放在默认的过滤器前面。
            if (Constants.DEFAULT_KEY.equals(name)) {
                if (!usrs.isEmpty()) {
                    exts.addAll(0, usrs);
                    usrs.clear();
                }
            } else {
                // 获得拓展对象
                T ext = getExtension(name);
                usrs.add(ext);
            }
        }
    }
    // 添加到结果集
    if (!usrs.isEmpty()) {
        exts.addAll(usrs);
    }
    return exts;
}
```
* 第 16 行：从 Dubbo URL 获得参数值。例如说，若 XML 配置 Service \<dubbo:service filter="demo, demo2" /> ，并且在获得 Filter 自动激活拓展时，此处就能解析到 value=demo,demo2 。另外，value 可以根据逗号拆分。
* 第 18 行：调用 #getActivateExtension(url, values, group) 方法，获得符合自动激活条件的拓展对象数组。
* 第 35 至 56 行：处理自动激活的拓展对象们。

    * #isMatchGroup(group, groups) 方法，匹配分组。
    * #isActive(Activate, url) 方法，是否激活，通过 Dubbo URL 中是否存在参数名为 \`@Activate.value` ，并且参数值非空。

* 第 57 至 74 行：处理自定义配置的拓展对象们。
* 第 75 至 78 行：将 usrs 合并到 exts 尾部。

## @SPI  
com.alibaba.dubbo.common.extension.@SPI ，扩展点接口的标识。代码如下：  

```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SPI {

    /**
     * default extension name
     */
    String value() default "";

}
```

* value ，默认拓展实现类的名字。例如，Protocol 拓展接口，代码如下：  

    ```java
    @SPI("dubbo")
    public interface Protocol {
        // ... 省略代码
    }
    ```
* 其中 "dubbo" 指的是 DubboProtocol ，Protocol 默认的拓展实现类。

## @Adaptive  
com.alibaba.dubbo.common.extension.@Adaptive ，自适应拓展信息的标记。代码如下：  

```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Adaptive {

    /**
     * Decide which target extension to be injected. The name of the target extension is decided by the parameter passed
     * in the URL, and the parameter names are given by this method.
     * <p>
     * If the specified parameters are not found from {@link URL}, then the default extension will be used for
     * dependency injection (specified in its interface's {@link SPI}).
     * <p>
     * For examples, given <code>String[] {"key1", "key2"}</code>:
     * <ol>
     * <li>find parameter 'key1' in URL, use its value as the extension's name</li>
     * <li>try 'key2' for extension's name if 'key1' is not found (or its value is empty) in URL</li>
     * <li>use default extension if 'key2' doesn't appear either</li>
     * <li>otherwise, throw {@link IllegalStateException}</li>
     * </ol>
     * If default extension's name is not give on interface's {@link SPI}, then a name is generated from interface's
     * class name with the rule: divide classname from capital char into several parts, and separate the parts with
     * dot '.', for example: for {@code com.alibaba.dubbo.xxx.YyyInvokerWrapper}, its default name is
     * <code>String[] {"yyy.invoker.wrapper"}</code>. This name will be used to search for parameter from URL.
     *
     * @return parameter key names in URL
     */
    /**
     * 从 {@link URL }的 Key 名，对应的 Value 作为要 Adapt 成的 Extension 名。
     * <p>
     * 如果 {@link URL} 这些 Key 都没有 Value ，使用 缺省的扩展（在接口的{@link SPI}中设定的值）。<br>
     * 比如，<code>String[] {"key1", "key2"}</code>，表示
     * <ol>
     *      <li>先在URL上找key1的Value作为要Adapt成的Extension名；
     *      <li>key1没有Value，则使用key2的Value作为要Adapt成的Extension名。
     *      <li>key2没有Value，使用缺省的扩展。
     *      <li>如果没有设定缺省扩展，则方法调用会抛出{@link IllegalStateException}。
     * </ol>
     * <p>
     * 如果不设置则缺省使用Extension接口类名的点分隔小写字串。<br>
     * 即对于Extension接口 {@code com.alibaba.dubbo.xxx.YyyInvokerWrapper} 的缺省值为 <code>String[] {"yyy.invoker.wrapper"}</code>
     *
     * @see SPI#value()
     */
    String[] value() default {};

}
```
@Adaptive 注解，可添加类或方法上，分别代表了两种不同的使用方式。  

* 第一种，标记在类上，代表手动实现它是一个拓展接口的 Adaptive 拓展实现类。目前 Dubbo 项目里，只有 ExtensionFactory 拓展的实现类 AdaptiveExtensionFactory 有这么用。详细解析见 「AdaptiveExtensionFactory」 。
* 第二种，标记在拓展接口的方法上，代表自动生成代码实现该接口的 Adaptive 拓展实现类。
    * value ，从 Dubbo URL 获取参数中，使用键名( Key )，获取键值。该值为真正的拓展名。
        * 自适应拓展实现类，会获取拓展名对应的真正的拓展对象。通过该对象，执行真正的逻辑。
        * 可以设置多个键名( Key )，顺序获取直到有值。若最终获取不到，使用默认拓展名。
    * 在 「createAdaptiveExtensionClassCode」 详细解析。

## @Activate  
com.alibaba.dubbo.common.extension.@Activate ，自动激活条件的标记。代码如下：  

```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Activate {
    /**
     * Activate the current extension when one of the groups matches. The group passed into
     * {@link ExtensionLoader#getActivateExtension(URL, String, String)} will be used for matching.
     *
     * @return group names to match
     * @see ExtensionLoader#getActivateExtension(URL, String, String)
     */
    /**
     * Group过滤条件。
     * <br />
     * 包含{@link ExtensionLoader#getActivateExtension}的group参数给的值，则返回扩展。
     * <br />
     * 如没有Group设置，则不过滤。
     */
    String[] group() default {};

    /**
     * Activate the current extension when the specified keys appear in the URL's parameters.
     * <p>
     * For example, given <code>@Activate("cache, validation")</code>, the current extension will be return only when
     * there's either <code>cache</code> or <code>validation</code> key appeared in the URL's parameters.
     * </p>
     *
     * @return URL parameter keys
     * @see ExtensionLoader#getActivateExtension(URL, String)
     * @see ExtensionLoader#getActivateExtension(URL, String, String)
     */
    /**
     * Key过滤条件。包含{@link ExtensionLoader#getActivateExtension}的URL的参数Key中有，则返回扩展。
     * <p/>
     * 示例：<br/>
     * 注解的值 <code>@Activate("cache,validatioin")</code>，
     * 则{@link ExtensionLoader#getActivateExtension}的URL的参数有<code>cache</code>Key，或是<code>validatioin</code>则返回扩展。
     * <br/>
     * 如没有设置，则不过滤。
     */
    String[] value() default {};

    /**
     * Relative ordering info, optional
     *
     * @return extension list which should be put before the current one
     */
    /**
     * 排序信息，可以不提供。
     */
    String[] before() default {};

    /**
     * Relative ordering info, optional
     *
     * @return extension list which should be put after the current one
     */
    /**
     * 排序信息，可以不提供。
     */
    String[] after() default {};

    /**
     * Absolute ordering info, optional
     *
     * @return absolute ordering info
     */
    /**
     * 排序信息，可以不提供。
     */
    int order() default 0;
}
```
* 对于可以被框架中自动激活加载扩展，@Activate 用于配置扩展被自动激活加载条件。比如，Filter 扩展，有多个实现，使用 @Activate 的扩展可以根据条件被自动加载。
    * 这块的例子，可以看下 《Dubbo 开发指南 —— 扩展点加载》「扩展点自动激活」 文档提供的。

## ExtensionFactory  
com.alibaba.dubbo.common.extension.ExtensionFactory ，拓展工厂接口。代码如下：  

```java
/**
 * ExtensionFactory
 *
 * 拓展工厂接口
 */
@SPI
public interface ExtensionFactory {

    /**
     * Get extension.
     *
     * 获得拓展对象
     *
     * @param type object type. 拓展接口
     * @param name object name. 拓展名
     * @return object instance. 拓展对象
     */
    <T> T getExtension(Class<T> type, String name);

}
```

* ExtensionFactory 自身也是拓展接口，基于 Dubbo SPI 加载具体拓展实现类。
* #getExtension(type, name) 方法，在 「injectExtension」 中，获得拓展对象，向创建的拓展对象注入依赖属性。在实际代码中，我们可以看到不仅仅获得的是拓展对象，也可以是 Spring 中的 Bean 对象。
* ExtensionFactory 子类类图如下
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-25.png)  

### AdaptiveExtensionFactory  
com.alibaba.dubbo.common.extension.factory.AdaptiveExtensionFactory ，自适应 ExtensionFactory 拓展实现类。代码如下：  

```java
@Adaptive
public class AdaptiveExtensionFactory implements ExtensionFactory {

    /**
     * ExtensionFactory 拓展对象集合
     */
    private final List<ExtensionFactory> factories;

    public AdaptiveExtensionFactory() {
        // 使用 ExtensionLoader 加载拓展对象实现类。
        ExtensionLoader<ExtensionFactory> loader = ExtensionLoader.getExtensionLoader(ExtensionFactory.class);
        List<ExtensionFactory> list = new ArrayList<ExtensionFactory>();
        for (String name : loader.getSupportedExtensions()) {
            list.add(loader.getExtension(name));
        }
        factories = Collections.unmodifiableList(list);
    }

    public <T> T getExtension(Class<T> type, String name) {
        // 遍历工厂数组，直到获得到属性
        for (ExtensionFactory factory : factories) {
            T extension = factory.getExtension(type, name);
            if (extension != null) {
                return extension;
            }
        }
        return null;
    }

}
```

* @Adaptive 注解，为 ExtensionFactory 的自适应拓展实现类。
* 构造方法，使用 ExtensionLoader 加载 ExtensionFactory 拓展对象的实现类。若胖友没自己实现 ExtensionFactory 的情况下，factories 为 SpiExtensionFactory 和 SpringExtensionFactory 。
* #getExtension(type, name) 方法，遍历 factories ，调用其 #getExtension(type, name) 方法，直到获得到属性值。

### SpiExtensionFactory  
com.alibaba.dubbo.common.extension.factory.SpiExtensionFactory ，SPI ExtensionFactory 拓展实现类。代码如下：  

```java
public class SpiExtensionFactory implements ExtensionFactory {

    /**
     * 获得拓展对象
     *
     * @param type object type. 拓展接口
     * @param name object name. 拓展名
     * @param <T> 泛型
     * @return 拓展对象
     */
    public <T> T getExtension(Class<T> type, String name) {
        if (type.isInterface() && type.isAnnotationPresent(SPI.class)) { // 校验是 @SPI
            // 加载拓展接口对应的 ExtensionLoader 对象
            ExtensionLoader<T> loader = ExtensionLoader.getExtensionLoader(type);
            // 加载拓展对象
            if (!loader.getSupportedExtensions().isEmpty()) {
                return loader.getAdaptiveExtension();
            }
        }
        return null;
    }

}
```

### SpringExtensionFactory  
com.alibaba.dubbo.config.spring.extension.SpringExtensionFactory ，Spring ExtensionFactory 拓展实现类。代码如下：  

```java
public class SpringExtensionFactory implements ExtensionFactory {

    /**
     * Spring Context 集合
     */
    private static final Set<ApplicationContext> contexts = new ConcurrentHashSet<ApplicationContext>();

    public static void addApplicationContext(ApplicationContext context) {
        contexts.add(context);
    }

    public static void removeApplicationContext(ApplicationContext context) {
        contexts.remove(context);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getExtension(Class<T> type, String name) {
        for (ApplicationContext context : contexts) {
            if (context.containsBean(name)) {
                // 获得属性
                Object bean = context.getBean(name);
                // 判断类型
                if (type.isInstance(bean)) {
                    return (T) bean;
                }
            }
        }
        return null;
    }

}
```
* #getExtension(type, name) 方法，遍历 contexts ，调用其 ApplicationContext#getBean(name) 方法，获得 Bean 对象，直到成功并且值类型正确。  





