

<!-- TOC -->

- [1. ~~Dubbo初始化~~](#1-dubbo初始化)
    - [1.1. Dubbo中对Spring配置标签扩展](#11-dubbo中对spring配置标签扩展)
    - [1.2. 服务提供者初始化](#12-服务提供者初始化)
        - [1.2.1. 解析配置文件](#121-解析配置文件)
        - [1.2.2. ServiceBean执行export](#122-servicebean执行export)
    - [1.3. 服务消费者初始化](#13-服务消费者初始化)
        - [1.3.1. RefrenceBean生成过程](#131-refrencebean生成过程)
        - [1.3.2. 代理bean生成过程](#132-代理bean生成过程)

<!-- /TOC -->


# 1. ~~Dubbo初始化~~  

## 1.1. Dubbo中对Spring配置标签扩展  
<!-- 
Dubbo中对Spring配置标签扩展
https://blog.csdn.net/weixin_33769207/article/details/86361060
-->
&emsp; Spring提供了可扩展Schema的支持，完成一个自定义配置一般需要以下步骤：

* 设计配置属性和JavaBean（dubbo的ServiceBean定义了dubbo每个服务的信息）
* 编写XSD文件
* 编写NamespaceHandler和BeanDefinitionParser完成解析工作
* 编写spring.handlers和spring.schemas串联起所有部件
* 在Bean文件中应用

&emsp; dubbo中所有dubbo的标签，都统一用DubboBeanDefinitionParser进行解析，基于一对一属性映射，将XML标签解析为Bean对象。

## 1.2. 服务提供者初始化  
<!-- 
Dubbo-服务提供者初始化
https://www.cnblogs.com/caoxb/p/13140261.html
-->
&emsp; 服务提供者初始化过程，即ServiceBean 初始化过程。   

### 1.2.1. 解析配置文件  
&emsp; Spring解析Dubbo配置文件时，会找到dubbo命名空间对应的handler，DubboNamespaceHandler。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-42.png)   

```java
public class DubboNamespaceHandler extends NamespaceHandlerSupport {

    static {
        Version.checkDuplicate(DubboNamespaceHandler.class);
    }

    public void init() {
        registerBeanDefinitionParser("application", new DubboBeanDefinitionParser(ApplicationConfig.class, true));
        registerBeanDefinitionParser("module", new DubboBeanDefinitionParser(ModuleConfig.class, true));
        registerBeanDefinitionParser("registry", new DubboBeanDefinitionParser(RegistryConfig.class, true));
        registerBeanDefinitionParser("monitor", new DubboBeanDefinitionParser(MonitorConfig.class, true));
        registerBeanDefinitionParser("provider", new DubboBeanDefinitionParser(ProviderConfig.class, true));
        registerBeanDefinitionParser("consumer", new DubboBeanDefinitionParser(ConsumerConfig.class, true));
        registerBeanDefinitionParser("protocol", new DubboBeanDefinitionParser(ProtocolConfig.class, true));
        registerBeanDefinitionParser("service", new DubboBeanDefinitionParser(ServiceBean.class, true));
        registerBeanDefinitionParser("reference", new DubboBeanDefinitionParser(ReferenceBean.class, false));
        registerBeanDefinitionParser("annotation", new DubboBeanDefinitionParser(AnnotationBean.class, true));
    }

}
```
&emsp; 启动大致流程  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-43.png)   

&emsp; **流程如下：**  

1. spring容器启动
2. ServiceBean初始化
3. 事件发布，执行onApplicationEvent，开始执行ServiceBean暴露export操作
4. 启动NettyServer
5. 注册中心服务暴露export

### 1.2.2. ServiceBean执行export  
&emsp; 当Spring容器处理完\<dubbo:service>标签后，会在Spring容器中生成一个ServiceBean，服务的发布也会在ServiceBean中完成。ServiceBean的定义如下：  

```java
public class ServiceBean<T> extends ServiceConfig<T> 
    implements InitializingBean, DisposableBean, ApplicationContextAware, ApplicationListener, BeanNameAware {

}
```
 
&emsp; 而在Spring初始化完成Bean的组装，会调用InitializingBean的afterPropertiesSet方法，在Spring容器加载完成，会接收到事件ContextRefreshedEvent，调用ApplicationListener的onApplicationEvent方法。  

```java
public void afterPropertiesSet() throws Exception {
    // 
    if (!isDelay()) {
        // 暴露服务
        export();
    }
}
```
 
&emsp; ServiceBean实现了ApplicationListener接口，实现onApplicationEvent方法，该方法在spring容器启动完成后“自动”执行

```java	
public void onApplicationEvent(ApplicationEvent event) {
    if (ContextRefreshedEvent.class.getName().equals(event.getClass().getName())) {
        if (isDelay() && !isExported() && !isUnexported()) {
            if (logger.isInfoEnabled()) {
                logger.info("The service ready on spring started. service: " + getInterface());
            }
            //
            export();
        }
    }
}　　
```

## 1.3. 服务消费者初始化  
<!-- 
Dubbo-服务消费者初始化
https://www.cnblogs.com/caoxb/p/13140244.html
-->
&emsp; 服务消费者初始化过程，即生成RefrenceBean的过程。    
&emsp; 整体执行流程  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-44.png)   

    备注：Dubbo默认使用Javassist框架为服务接口生成动态代理类，可以使用使用阿里开源Java应用诊断工具Arthas反编译代理类。  

&emsp; dubbo官网提供的时序图  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-45.png)  

### 1.3.1. RefrenceBean生成过程  
&emsp; RefrenceBean是一个FactoryBean  
&emsp; spring容器在启动过程中执行  
1. bean定义注册  
&emsp; 客户端在启动的过程中会注册4种类型的bean定义  

    * com.alibaba.dubbo.config.RegistryConfig（注册中心相关）
    * com.alibaba.dubbo.config.ProviderConfig（服务提供者相关）
    * demoService
    * demo-consumer

2. 单例非懒加载bean的创建  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/Dubbo/dubbo-46.png)  
    1. 以demoService的bean创建为例，demoService属于ReferenceBean类型的工厂bean
    2. demoService在实例化后（earlySingletonExposure为true提前暴露）准备开始进行属性注入，即将其所有暴露的method作为属性；
    3. demoService将demoService.sayHello方法当成它的属性，并始对它进行属性注入（emoService.sayHello是一个Method类型的bean）；
    4. 开始创建名为demoService.sayHello的bean，这个Bean属于MethodConfig；

### 1.3.2. 代理bean生成过程  
&emsp; 当执行context.getBean("demoService") 方法时  

```java
protected <T> T doGetBean(
    final String name, final Class<T> requiredType, final Object[] args, boolean typeCheckOnly)
    throws BeansException {

    final String beanName = transformedBeanName(name);
    Object bean;

    // Eagerly check singleton cache for manually registered singletons.
    // spring容器在启动完成后FactoryBean就已经创建好了，demoService属于ReferenceBean工厂bean
    Object sharedInstance = getSingleton(beanName);
    if (sharedInstance != null && args == null) {
        if (logger.isDebugEnabled()) {
            if (isSingletonCurrentlyInCreation(beanName)) {
                logger.debug("Returning eagerly cached instance of singleton bean '" + beanName +
                             "' that is not fully initialized yet - a consequence of a circular reference");
            }
            else {
                logger.debug("Returning cached instance of singleton bean '" + beanName + "'");
            }
        }
        // 关键代码 sharedInstance为ReferenceBean
        bean = getObjectForBeanInstance(sharedInstance, name, beanName, null);
    }
    // 省略若干代码...
}
```

&emsp; 从FactoryBean获取对象  

```java
protected Object getObjectForBeanInstance(
    Object beanInstance, String name, String beanName, RootBeanDefinition mbd) {

    // Don't let calling code try to dereference the factory if the bean isn't a factory.
    if (BeanFactoryUtils.isFactoryDereference(name) && !(beanInstance instanceof FactoryBean)) {
        throw new BeanIsNotAFactoryException(transformedBeanName(name), beanInstance.getClass());
    }

    // Now we have the bean instance, which may be a normal bean or a FactoryBean.
    // If it's a FactoryBean, we use it to create a bean instance, unless the
    // caller actually wants a reference to the factory.
    if (!(beanInstance instanceof FactoryBean) || BeanFactoryUtils.isFactoryDereference(name)) {
        return beanInstance;
    }

    Object object = null;
    if (mbd == null) {
        // 第一次执行时，取出的结果为null
        object = getCachedObjectForFactoryBean(beanName);
    }
    if (object == null) {
        // Return bean instance from factory.
        FactoryBean<?> factory = (FactoryBean<?>) beanInstance;
        // Caches object obtained from FactoryBean if it is a singleton.
        if (mbd == null && containsBeanDefinition(beanName)) {
            mbd = getMergedLocalBeanDefinition(beanName);
        }
        boolean synthetic = (mbd != null && mbd.isSynthetic());
        // 关键代码
        object = getObjectFromFactoryBean(factory, beanName, !synthetic);
    }
    return object;
}
```

```java
protected Object getObjectFromFactoryBean(FactoryBean<?> factory, String beanName, boolean shouldPostProcess) {
    if (factory.isSingleton() && containsSingleton(beanName)) {
        synchronized (getSingletonMutex()) {
            Object object = this.factoryBeanObjectCache.get(beanName);
            if (object == null) {
                // 关键代码...
                object = doGetObjectFromFactoryBean(factory, beanName);
                // Only post-process and store if not put there already during getObject() call above
                // (e.g. because of circular reference processing triggered by custom getBean calls)
                Object alreadyThere = this.factoryBeanObjectCache.get(beanName);
                if (alreadyThere != null) {
                    object = alreadyThere;
                }
                else {
                    if (object != null && shouldPostProcess) {
                        try {
                            object = postProcessObjectFromFactoryBean(object, beanName);
                        }
                        catch (Throwable ex) {
                            throw new BeanCreationException(beanName,
                                                            "Post-processing of FactoryBean's singleton object failed", ex);
                        }
                    }
                    this.factoryBeanObjectCache.put(beanName, (object != null ? object : NULL_OBJECT));
                }
            }
            return (object != NULL_OBJECT ? object : null);
        }
    }
    else {
        // 关键代码...
        Object object = doGetObjectFromFactoryBean(factory, beanName);
        if (object != null && shouldPostProcess) {
            try {
                object = postProcessObjectFromFactoryBean(object, beanName);
            }
            catch (Throwable ex) {
                throw new BeanCreationException(beanName, "Post-processing of FactoryBean's object failed", ex);
            }
        }
        return object;
    }
}
```

```java
private Object doGetObjectFromFactoryBean(final FactoryBean<?> factory, final String beanName)
    throws BeanCreationException {

    Object object;
    try {
        if (System.getSecurityManager() != null) {
            AccessControlContext acc = getAccessControlContext();
            try {
                object = AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                    @Override
                    public Object run() throws Exception {
                        return factory.getObject();
                    }
                }, acc);
            }
            catch (PrivilegedActionException pae) {
                throw pae.getException();
            }
        }
        else {
            // 核心方法，这个factory为ReferenceBean，因此直接调用ReferenceBean中getObject方法
            object = factory.getObject();
        }
    }
    
    // 省略若干代码...
    return object;
}
```

&emsp; ReferenceBean 是一个FactoryBean，而FactoryBean中有三个重要的方法，其中一个就是getObject()  

```java
public Object getObject() throws Exception {
    return get();
}
```

&emsp; ReferenceConfig 中的get 方法  

```java
public synchronized T get() {
    if (destroyed) {
        throw new IllegalStateException("Already destroyed!");
    }
    if (ref == null) {
        // 关键，所以这时间稍长一点的话，得到的结果为null
        init();
    }
    return ref;
}
```

&emsp; 上面采用非阻塞异步的方式  
&emsp; ReferenceConfig 中的init 方法  

```java
private void init() {
    // 省略若干代码...
  
    StaticContext.getSystemContext().putAll(attributes);
    // 核心方法
    ref = createProxy(map);
}
```

```java
private transient volatile Invoker<?> invoker;

private T createProxy(Map<String, String> map) {
    // 省略若干代码...
    if (isJvmRefer) {
        URL url = new URL(Constants.LOCAL_PROTOCOL, NetUtils.LOCALHOST, 0, interfaceClass.getName()).addParameters(map);
        // 核心方法，拿到invoker对象        
        invoker = refprotocol.refer(interfaceClass, url);
    } else {
        // 省略若干代码...
        if (urls.size() == 1) {
            // 核心方法，拿到invoker对象
            // refprotocol为Protocol$Adpative对象
            invoker = refprotocol.refer(interfaceClass, urls.get(0));
        } else {
            // 省略若干代码...
        }
    }
    // 核心方法create service proxy 
    // 将下面这段代码替换成 ProxyFactory$Adaptive 中 getProxy中的部分
    return (T) proxyFactory.getProxy(invoker); 
}
```

&emsp; proxyFactory是一个ProxyFactory$Adaptive  
&emsp; invoker是一个MockClusterInvoker对象  

```java
public class ProxyFactory$Adaptive implements ProxyFactory {
    public Object getProxy(Invoker invoker) throws RpcException {
        if (invoker == null) 
            throw new IllegalArgumentException("com.alibaba.dubbo.rpc.Invoker argument == null");
        if (invoker.getUrl() == null) 
            throw new IllegalArgumentException("com.alibaba.dubbo.rpc.Invoker argument getUrl() == null");
        com.alibaba.dubbo.common.URL url = invoker.getUrl();
        String extName = url.getParameter("proxy", "javassist");
        if(extName == null) 
            throw new IllegalStateException("Fail to get extension(ProxyFactory) name from url(" + url.toString() + ") use keys([proxy])");
        ProxyFactory extension = (ProxyFactory)ExtensionLoader.getExtensionLoader(ProxyFactory.class).getExtension(extName);
        return extension.getProxy(invoker);
    }
    public Invoker getInvoker(Object arg0, Class arg1, com.alibaba.dubbo.common.URL arg2) throws RpcException {
        if (arg2 == null) 
            throw new IllegalArgumentException("url == null");
        com.alibaba.dubbo.common.URL url = arg2;
        String extName = url.getParameter("proxy", "javassist");
        if(extName == null) 
            throw new IllegalStateException("Fail to get extension(ProxyFactory) name from url(" + url.toString() + ") use keys([proxy])");
        ProxyFactory extension = (ProxyFactory)ExtensionLoader.getExtensionLoader(ProxyFactory.class).getExtension(extName);
        return extension.getInvoker(arg0, arg1, arg2);
    } 
}
```
&emsp; 即：

```java
// create service proxy
// 下面的代码直接替换(T) proxyFactory.getProxy(invoker) 方法
com.alibaba.dubbo.common.URL url = invoker.getUrl();
String extName = url.getParameter("proxy", "javassist");    // extName为 javassist
ProxyFactory extension = (ProxyFactory)ExtensionLoader.getExtensionLoader(ProxyFactory.class).getExtension(extName);
// extension为StubProxyFactoryWrapper对象
// return (T) proxyFactory.getProxy(invoker);
return (T) extension.getProxy(invoker);
```

&emsp; 进入到StubProxyFactoryWrapper，再来分析一下getProxy 方法

```java
public <T> T getProxy(Invoker<T> invoker) throws RpcException {
    T proxy = proxyFactory.getProxy(invoker);    // 关键部分...
    if (GenericService.class != invoker.getInterface()) {
       // 
    }
    return proxy;
}
```

&emsp; 上面 proxyFactory为JavassistProxyFactory类型，所以进入到JavassistProxyFactory类，分析一下它的getProxy方法

```java
public <T> T getProxy(Invoker<T> invoker, Class<?>[] interfaces) {
    return (T) Proxy.getProxy(interfaces).newInstance(new InvokerInvocationHandler(invoker));
}
```

&emsp; 先来分析一下前面一部分的Proxy.getProxy(interfaces) 方法

```java
public static Proxy getProxy(Class<?>... ics) {
    return getProxy(ClassHelper.getClassLoader(Proxy.class), ics);
}
```
&emsp; 这个方法特点长，但是非常重要  

```java
public static Proxy getProxy(ClassLoader cl, Class<?>... ics) {
    if (ics.length > 65535)
        throw new IllegalArgumentException("interface limit exceeded");

    // 省略了若干代码
    // use interface class name list as key.
    String key = sb.toString();
    // key = com.alibaba.dubbo.demo.DemoService;com.alibaba.dubbo.rpc.service.EchoService;


    long id = PROXY_CLASS_COUNTER.getAndIncrement();
    String pkg = null;
    ClassGenerator ccp = null, ccm = null;
    try {
        ccp = ClassGenerator.newInstance(cl);

        Set<String> worked = new HashSet<String>();
        List<Method> methods = new ArrayList<Method>();

        for (int i = 0; i < ics.length; i++) {
            if (!Modifier.isPublic(ics[i].getModifiers())) {
                String npkg = ics[i].getPackage().getName();
                if (pkg == null) {
                    pkg = npkg;
                } else {
                    if (!pkg.equals(npkg))
                        throw new IllegalArgumentException("non-public interfaces from different packages");
                }
            }
            ccp.addInterface(ics[i]);
            // --------------------- 代理的关键部分 START ---------------
            // 
            for (Method method : ics[i].getMethods()) {
                String desc = ReflectUtils.getDesc(method);
                if (worked.contains(desc))
                    continue;
                worked.add(desc);

                int ix = methods.size();
                Class<?> rt = method.getReturnType();
                Class<?>[] pts = method.getParameterTypes();

                StringBuilder code = new StringBuilder("Object[] args = new Object[").append(pts.length).append("];");
                for (int j = 0; j < pts.length; j++)
                    code.append(" args[").append(j).append("] = ($w)$").append(j + 1).append(";");
                code.append(" Object ret = handler.invoke(this, methods[" + ix + "], args);");
                if (!Void.TYPE.equals(rt))
                    code.append(" return ").append(asArgument(rt, "ret")).append(";");

                methods.add(method);
                // 生成方法，就是给上面的字符串添加 方法名、方法返回类型及{、} 
                ccp.addMethod(method.getName(), method.getModifiers(), rt, pts, method.getExceptionTypes(), code.toString());
            }
            // --------------------- 代理的关键部分 START ---------------
        }

        if (pkg == null)
            pkg = PACKAGE_NAME;

        // create ProxyInstance class.
        String pcn = pkg + ".proxy" + id;    // 如生成com.alibaba.dubbo.common.bytecode.proxy0
        ccp.setClassName(pcn);
        ccp.addField("public static java.lang.reflect.Method[] methods;");
        ccp.addField("private " + InvocationHandler.class.getName() + " handler;");
        ccp.addConstructor(Modifier.PUBLIC, new Class<?>[]{InvocationHandler.class}, new Class<?>[0], "handler=$1;");
        ccp.addDefaultConstructor();
        Class<?> clazz = ccp.toClass();
        clazz.getField("methods").set(null, methods.toArray(new Method[0]));

        // create Proxy class.
        String fcn = Proxy.class.getName() + id;
        ccm = ClassGenerator.newInstance(cl);
        ccm.setClassName(fcn);
        ccm.addDefaultConstructor();
        ccm.setSuperClass(Proxy.class);
        ccm.addMethod("public Object newInstance(" + InvocationHandler.class.getName() + " h){ return new " + pcn + "($1); }");
        // 这里直接调用toClass方法生成Class，使用了 Javaassist 框架
        Class<?> pc = ccm.toClass();
        proxy = (Proxy) pc.newInstance();
    } catch (RuntimeException e) {
        throw e;
    } catch (Exception e) {
        throw new RuntimeException(e.getMessage(), e);
    } finally {
        // release ClassGenerator
        if (ccp != null)
            ccp.release();
        if (ccm != null)
            ccm.release();
        synchronized (cache) {
            if (proxy == null)
                cache.remove(key);
            else
                cache.put(key, new WeakReference<Proxy>(proxy));
            cache.notifyAll();
        }
    }
    return proxy;
}
```
 
&emsp; 如通过接口中的String sayHello(String arg0) 方法生成代理类的sayHello方法  

```java
public java.lang.String sayHello(java.lang.String arg0){
    Object[] args = new Object[1]; 
    args[0] = ($w)$1; 
    Object ret = handler.invoke(this, methods[0], args); 
    return (java.lang.String)ret;
}
```