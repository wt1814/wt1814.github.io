

<!-- TOC -->

- [1. JDK动态代理](#1-jdk动态代理)
    - [1.1. 编码](#11-编码)
    - [1.2. 源码分析-1](#12-源码分析-1)
    - [1.3. JDK动态代理为什么只能使用接口？](#13-jdk动态代理为什么只能使用接口)
        - [1.3.1. 编码示例](#131-编码示例)
        - [1.3.2. 解析](#132-解析)
        - [1.3.3. 小结](#133-小结)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. Java动态代理类位于java.lang.reflect包下，一般主要涉及到以下两个重要的类或接口，`一个是InvocationHandler接口、另一个则是Proxy类。`  
    * Proxy类。该类即为动态代理类。Proxy.newProxyInstance()生成代理对象；  
    * InvocationHandler接口。 **<font color = "clime">在使用动态代理时，需要定义一个位于代理类与委托类之间的中介类，中介类被要求实现InvocationHandler接口。</font>** 通过代理对象调用一个方法的时候，这个方法的调用会被转发为由InvocationHandler这个接口的invoke方法来进行调用。  
2. <font color = "clime">JDK动态代理的实现，大致流程：</font>  
    1. <font color = "red">为接口创建代理类的字节码文件。</font> 使用`反射`来创建代理类。  
    2. <font color = "red">使用ClassLoader将字节码文件加载到JVM。</font>  
    3. <font color = "red">创建代理类实例对象，执行对象的目标方法。</font>  
3. `JDK动态代理为什么只能使用接口？`  
&emsp; JDK动态代理是为接口生成代理对象，该代理对象继承了JAVA标准类库Proxy.java类并且实现了目标对象。由于JAVA遵循单继承多实现原则，所以JDK无法利用继承来为目标对象生产代理对象。   

# 1. JDK动态代理
<!-- 
  利用拦截器(拦截器必须实现InvocationHanlder)加上反射机制生成一个实现代理接口的匿名类，在调用具体方法前调用InvokeHandler来处理。


动态代理在我在设计模式中已经介绍过了，主要是通过 Proxy类的newProxyInstance方法和接口InvocationHandler来实现动态代理。代理对象的的生产过程在这里简单说一下：

1、ProxyGenerator.generateProxyClass方法负责生成代理类的字节码，生成逻辑比较复杂，了解原理继续分析源码 sun.misc.ProxyGenerator；

byte[] proxyClassFile = ProxyGenerator.generateProxyClass(proxyName, interfaces, accessFlags);
2、native方法Proxy.defineClass0负责字节码加载的实现，并返回对应的Class对象。

Class clazz = defineClass0(loader, proxyName, proxyClassFile, 0, proxyClassFile.length);
3、利用clazz.newInstance反射机制生成代理类的对象；

　　动态代理的局限性：它必须要求委托类实现一个接口，但是并非所有的类都有接口，对于没有实现接口的类，无法使用该方法实现代理；而且该方法无法 对委托类的方法内部逻辑作修改。

private方法可以被代理吗？
https://mp.weixin.qq.com/s/o45-1ceWtbJid0E4kcehWg

-->
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

## 1.2. 源码分析-1
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
&emsp; <font color = "clime">JDK动态代理的实现，大致流程：</font>  
1. <font color = "red">为接口创建代理类的字节码文件。</font>使用反射来创建代理类。  
2. <font color = "red">使用ClassLoader将字节码文件加载到JVM。</font>  
3. <font color = "red">创建代理类实例对象，执行对象的目标方法。</font>  

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


&emsp; newProxyInstance方法调用getProxyClass0方法生成代理类的字节码文件。getProxyClass0()源码：  

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
&emsp; 当WeakCache中没有缓存相应接口的代理类，则会调用ProxyClassFactory类的 **<font color = "clime">apply方法使用反射来创建代理类。</font>**  

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

## 1.3. JDK动态代理为什么只能使用接口？

<!-- 
关于代理：为什么 JDK 动态代理只能为接口生成代理？
https://segmentfault.com/a/1190000021821314

深入分析JDK动态代理为什么只能使用接口
https://blog.csdn.net/u014301265/article/details/102832131
-->

### 1.3.1. 编码示例  

### 1.3.2. 解析
&emsp; JDK动态代理的源码中，java.lang.reflect.Proxy 类中有一个内部类 ProxyClassFactory。该内部类有如下一个方法：  

```java
public Class<?> apply(ClassLoader loader, Class<?>[] interfaces) {}
```

&emsp; 在该方法中调用了 sun.misc.ProxyGenerator 类的如下方法：  

```java
public static byte[] generateProxyClass(final String var0, Class<?>[] var1, int var2){}
```

&emsp; 即代码如下：  

```java
/**
 * Generate the specified proxy class.
 */
byte[] proxyClassFile = ProxyGenerator.generateProxyClass(proxyName, interfaces, accessFlags);
```

&emsp; 这条代码就是为目标类在内存中生成一个代理类，可以看到返回的类型是 byte[]。所以，现在要做的就是利用该语句为 UserService 生成一个代理对象，并将二进制数据生成为一个 class 文件！我们只需要利用反编译工具查看一个该代码即可一幕了然了。  

&emsp; 现在开始：  

```java
public class App {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        UserService userService = (UserService) context.getBean("userService");
     
        Class<?>[] interfaces = new Class[]{UserService.class};
        byte[] bytes = ProxyGenerator.generateProxyClass("UserService", interfaces);

        File file = new File("/<path>/UserService.class");
        try {
            OutputStream outputStream = new FileOutputStream(file);
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

&emsp; 最后，就会在我们的磁盘中生成一个 UserService.class 字节码文件，我们只需要反编译即可（可以直接利用 IDE 查看，如 IntelliJ IDEA）。打开字节码文件（省略无关内容）如下所示：  

```java
public final class UserService extends Proxy implements com.mingrn.proxy.service.UserService {
    private static Method m1;
    private static Method m3;
    private static Method m2;
    private static Method m0;

    public UserService(InvocationHandler var1) throws  {
        super(var1);
    }

    public final boolean equals(Object var1) throws  {
        // ...
    }

    public final List find() throws  {
        // ...
    }

    public final String toString() throws  {
        // ...
    }

    public final int hashCode() throws  {
        // ...
    }

    static {
        try {
            m1 = Class.forName("java.lang.Object").getMethod("equals", Class.forName("java.lang.Object"));
            m3 = Class.forName("com.mingrn.proxy.service.UserService").getMethod("find");
            m2 = Class.forName("java.lang.Object").getMethod("toString");
            m0 = Class.forName("java.lang.Object").getMethod("hashCode");
        } catch (NoSuchMethodException var2) {
            throw new NoSuchMethodError(var2.getMessage());
        } catch (ClassNotFoundException var3) {
            throw new NoClassDefFoundError(var3.getMessage());
        }
    }
}
```

&emsp; 我们需要关心的仅仅是生成的 UserService 类的继承与实现关系即可：  

```java
class UserService extends Proxy implements com.mingrn.proxy.service.UserService {}  
```
&emsp; 现在明白为什么 JDK 动态代理一定是只能为接口生成代理类而不是使用继承了吗？  

### 1.3.3. 小结
&emsp; JDK 动态代理是为接口生成代理对象，该代理对象继承了 JAVA 标准类库 Proxy.java 类并且实现了目标对象。由于 JAVA 遵循单继承多实现原则所以 JDK 无法利用继承来为目标对象生产代理对象。  
