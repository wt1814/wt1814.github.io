

# 1. 代理  
<!-- 
动态代理竟然如此简单
https://mp.weixin.qq.com/s/7YYcSkdhJMrvD9We9dsMNA

超全MyBatis动态代理详解！
https://mp.weixin.qq.com/s/RjRzacdmx3DMHlhjj1GM3g
-->
&emsp; **<font color = "red">总结：代理设计模式，在目标对象实现的基础上，增强额外的功能操作。JDK动态代理中，Proxy.newProxyInstance()生成代理对象；通过代理对象调用一个方法的时候，这个方法的调用会被转发为由InvocationHandler这个接口的invoke方法来进行调用。</font>**

&emsp; <font color = "red">代理设计模式，提供了对目标对象另外的访问方式；即通过代理访问目标对象。</font>代理好处：可以在目标对象实现的基础上，增强额外的功能操作(扩展目标对象的功能)。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/AOP/AOP-1.png)  
&emsp; 动态代理技术：  
&emsp; 代理类在程序运行时创建的代理方式被认为动态代理。在了解动态代理之前, 先回顾一下JVM的类加载机制中的加载阶段要做的三件事情：  
1. 通过一个类的全名或其它途径来获取这个类的二进制字节流；  
2. 将这个字节流所代表的静态存储结构转化为方法区的运行时数据结构；  
3. 在内存中生成一个代表这个类的Class对象, 作为方法区中对这个类访问的入口。  

&emsp; **动态代理，主要就发生在第一个阶段, 这个阶段类的二进制字节流的来源可以有很多, 比如zip包、网络、运行时计算生成、其它文件生成 (JSP)、数据库获取。**其中运行时计算生成就是所说的动态代理技术，在Proxy类中, 就是运用了ProxyGenerator，generateProxyClass来为特定接口生成形式为 *$Proxy 的代理类的二进制字节流。所谓的动态代理就是想办法根据接口或者目标对象计算出代理类的字节码然后加载进JVM 中。实际计算的情况会很复杂，可以借助一些诸如JDK动态代理实现、CGLIB第三方库来完成的。 

------

# 1. 动态代理(Proxy)模式  

&emsp; 代理模式，是指为其他对象提供一种代理，以控制对这个对象的访问。在某些情况下，一个对象不适合或者不能直接引用另一个对象，而代理对象可以在客户端和目标对象之间起到中介的作用。使用代理模式主要有两个目的：一保护目标对象，二增强目标对象。  
&emsp; 动态代理实现方式由JDK自带的代理和Cglib提供的类库。这里只讨论JDK代理的使用。  

&emsp; Java动态代理类位于java.lang.reflect包下，一般主要涉及到以下两个重要的类或接口，一个是 InvocationHandler(Interface)、另一个则是 Proxy(Class)。  

1. <font color = "red">Proxy类，该类即为动态代理类。</font>Proxy类是专门完成代理的操作类，可以通过此类为一个或多个接口动态地生成实现类，此类提供了如下的操作方法：  

    ```java
    //构造函数，用于给内部的invocation handler赋值。
    protected  Proxy(InvocationHandler h)
    //loader是类装载器，interfaces是真实类所拥有的全部接口的数组。
    static Class<?> getProxyClass(ClassLoader loader, Class<?>... interfaces)
    //返回代理类的一个实例，返回后的代理类可以当作被代理类使用(可使用被代理类在Subject接口中声明过的方法)。
    //ClassLoader loader,:指定当前目标对象使用类加载器,获取加载器的方法是固定的
    //Class<?>[] interfaces,:目标对象实现的接口的类型,使用泛型方式确认类型
    //InvocationHandler h:事件处理,执行目标对象的方法时,会触发事件处理器的方法,会把当前执行目标对象的方法作为参数传入
    static Object newProxyInstance(ClassLoader loader, Class<?>[] interfaces, InvocationHandler h)
    ```
    &emsp; 在编写动态代理之前，要明确两个概念：  
        1.代理对象拥有目标对象相同的方法【因为参数二指定了对象的接口】  
        2.用户调用代理对象的什么方法，都是在调用处理器的invoke方法。  

2. <font color = "red">InvocationHandler接口，在使用动态代理时，需要定义一个位于代理类与委托类之间的中介类，中介类被要求实现InvocationHandler接口。</font>这个接口中仅定义了一个方法：  

    ```java
    Object invoke(Object proxy, Method method, Object[] args) 
    //参数obj一般是指代理类，method是被代理的方法，args为该方法的参数数组(无参时设置为null)。这个抽象方法在代理类中动态实现。
    ```
    &emsp; 每一个动态代理类都必须要实现InvocationHandler这个接口，并且每个代理类的实例都关联到了一个handler，通过代理对象调用一个方法的时候，这个方法的调用就会被转发为由InvocationHandler这个接口的 invoke 方法来进行调用。  
    <!-- 
    &emsp; 实现这个接口的中介类用做“调用处理器”。当调用代理类对象的方法时，这个“调用”会转送到invoke方法中，代理类对象作为proxy参数传入，参数method标识了具体调用的是代理类的哪个方法，args为这个方法的参数。因此对代理类中的所有方法的调用都会变为对invoke的调用，这样可以在invoke方法中添加统一的处理逻辑(也可以根据method参数对不同的代理类方法做不同的处理)。  
    -->
 
## 1.1. 编码  
&emsp; 代码示例：  
&emsp; 首先需要定义一个接口：  

```java
public interface UserService {
    void query();
}
```
&emsp; 然后实现这个接口：  

```java
public class UserServiceImpl implements UserService {
    public void query() {
        System.out.println("查询用户信息");
    }
}
```
&emsp; 定义一个类，需要实现InvocationHandler：  

```java
public class MyInvocationHandler implements InvocationHandler {

    Object target;

    public MyInvocationHandler(Object target) {
        this.target = target;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("进入了invoke");
        method.invoke(target);
        System.out.println("执行了invoke");
        return null;
    }
}
```
&emsp; 然后就是Main方法了： 

```java
public class Main {
    public static void main(String[] args) {
        MyInvocationHandler myInvocationHandler = new MyInvocationHandler(new UserServiceImpl());
        Object o = Proxy.newProxyInstance(Main.class.getClassLoader(),
                new Class[]{UserService.class}
                , myInvocationHandler);
        ((UserService)o).query();
    }
}
```
&emsp; 运行：  

    进入了invoke
    查询用户信息
    执行了invoke

## ※※※源码分析-1
<!-- 
https://mp.weixin.qq.com/s/RoPuIgGlZg6h-Zk1YMwroA

jdk动态代理实现原理
https://www.cnblogs.com/zuidongfeng/p/8735241.html
动态代理原理解析
https://www.jianshu.com/p/85d181d7d09a
JDK动态代理实现原理(jdk8)：
https://blog.csdn.net/yhl_jxy/article/details/80586785


-->

&emsp; 基于JDK1.8.0_65  
&emsp; <font color = "lime">JDK动态代理的实现，大致流程：</font>  
1. <font color = "red">为接口创建代理类的字节码文件</font>  
2. <font color = "red">使用ClassLoader将字节码文件加载到JVM</font>  
3. <font color = "red">创建代理类实例对象，执行对象的目标方法</font>  

&emsp; 动态代理涉及到的主要类：  

* java.lang.reflect.Proxy  
* java.lang.reflect.InvocationHandler  
* java.lang.reflect.WeakCache  
* sun.misc.ProxyGenerator  

&emsp; 动态代理使用了Proxy.newProxyInstance方法动态创建代理类，newProxyInstance源码：  

```java
@CallerSensitive
public static Object newProxyInstance(ClassLoader loader, Class<?>[] interfaces, InvocationHandler h) throws IllegalArgumentException
{
    // 判断InvocationHandler是否为空，若为空，抛出空指针异常
    Objects.requireNonNull(h);

    final Class<?>[] intfs = interfaces.clone();
    final SecurityManager sm = System.getSecurityManager();
    if (sm != null) {
        checkProxyAccess(Reflection.getCallerClass(), loader, intfs);
    }

    /*
     * 生成接口的代理类的字节码文件
     */
    Class<?> cl = getProxyClass0(loader, intfs);

    /*
     * 使用自定义的InvocationHandler作为参数，调用构造函数获取代理类对象实例
     */
    try {
        if (sm != null) {
            checkNewProxyPermission(Reflection.getCallerClass(), cl);
        }

        final Constructor<?> cons = cl.getConstructor(constructorParams);
        final InvocationHandler ih = h;
        if (!Modifier.isPublic(cl.getModifiers())) {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                public Void run() {
                    cons.setAccessible(true);
                    return null;
                }
            });
        }
        return cons.newInstance(new Object[]{h});
    } catch (IllegalAccessException|InstantiationException e) {
        throw new InternalError(e.toString(), e);
    } catch (InvocationTargetException e) {
        Throwable t = e.getCause();
        if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        } else {
            throw new InternalError(t.toString(), t);
        }
    } catch (NoSuchMethodException e) {
        throw new InternalError(e.toString(), e);
    }
}
```
&emsp; newProxyInstance动态创建代理类的步骤是：  

* 校验代理类方法调用处理程序h不能为空
* 动态生成代理类class文件格式字节流
* 通过loader加载创建代表代理类的class对象
* 根据代理类的构造器创建代理类
* 返回动态创建生成的代理类

&emsp; newProxyInstance方法调用getProxyClass0方法生成代理类的字节码文件。getProxyClass0()源码  

```java
private static Class<?> getProxyClass0(ClassLoader loader, Class<?>... interfaces) {
    // 限定代理的接口不能超过65535个
    if (interfaces.length > 65535) {
        throw new IllegalArgumentException("interface limit exceeded");
    }
    // 如果缓存中已经存在相应接口的代理类，直接返回；否则，使用ProxyClassFactory创建代理类
    return proxyClassCache.get(loader, interfaces);
}
```
&emsp; 其中缓存使用的是WeakCache实现的，此处主要关注使用ProxyClassFactory创建代理的情况。ProxyClassFactory是Proxy类的静态内部类，实现了BiFunction接口，实现了BiFunction接口中的apply方法。  
&emsp; 当WeakCache中没有缓存相应接口的代理类，则会调用ProxyClassFactory类的 **<font color = "lime">apply方法使用反射来创建代理类。</font>**  

```java
private static final class ProxyClassFactory
        implements BiFunction<ClassLoader, Class<?>[], Class<?>>
{
    // 代理类前缀
    private static final String proxyClassNamePrefix = "$Proxy";
    // 生成代理类名称的计数器
    private static final AtomicLong nextUniqueNumber = new AtomicLong();
    @Override
    public Class<?> apply(ClassLoader loader, Class<?>[] interfaces) {

        Map<Class<?>, Boolean> interfaceSet = new IdentityHashMap<>(interfaces.length);
        for (Class<?> intf : interfaces) {
            /*
             * 校验类加载器是否能通过接口名称加载该类
             */
            Class<?> interfaceClass = null;
            try {
                interfaceClass = Class.forName(intf.getName(), false, loader);
            } catch (ClassNotFoundException e) {
            }
            if (interfaceClass != intf) {
                throw new IllegalArgumentException(
                        intf + " is not visible from class loader");
            }
            /*
             * 校验该类是否是接口类型
             */
            if (!interfaceClass.isInterface()) {
                throw new IllegalArgumentException(
                        interfaceClass.getName() + " is not an interface");
            }
            /*
             * 校验接口是否重复
             */
            if (interfaceSet.put(interfaceClass, Boolean.TRUE) != null) {
                throw new IllegalArgumentException(
                        "repeated interface: " + interfaceClass.getName());
            }
        }

        String proxyPkg = null;     // 代理类包名
        int accessFlags = Modifier.PUBLIC | Modifier.FINAL;

        /*
         * 非public接口，代理类的包名与接口的包名相同
         */
        for (Class<?> intf : interfaces) {
            int flags = intf.getModifiers();
            if (!Modifier.isPublic(flags)) {
                accessFlags = Modifier.FINAL;
                String name = intf.getName();
                int n = name.lastIndexOf('.');
                String pkg = ((n == -1) ? "" : name.substring(0, n + 1));
                if (proxyPkg == null) {
                    proxyPkg = pkg;
                } else if (!pkg.equals(proxyPkg)) {
                    throw new IllegalArgumentException(
                            "non-public interfaces from different packages");
                }
            }
        }

        if (proxyPkg == null) {
            // public代理接口，使用com.sun.proxy包名
            proxyPkg = ReflectUtil.PROXY_PACKAGE + ".";
        }

        /*
         * 为代理类生成名字
         */
        long num = nextUniqueNumber.getAndIncrement();
        String proxyName = proxyPkg + proxyClassNamePrefix + num;

        /*
         * 真正生成代理类的字节码文件的地方
         */
        byte[] proxyClassFile = ProxyGenerator.generateProxyClass(
                proxyName, interfaces, accessFlags);
        try {
            // 使用类加载器将代理类的字节码文件加载到JVM中
            return defineClass0(loader, proxyName,
                    proxyClassFile, 0, proxyClassFile.length);
        } catch (ClassFormatError e) {
            throw new IllegalArgumentException(e.toString());
        }
    }
}
```

