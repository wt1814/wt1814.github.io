
<!-- TOC -->

- [1. Mybatis插件](#1-mybatis插件)
    - [1.1. Mybatis插件简介](#11-mybatis插件简介)
    - [1.2. Mybatis插件编码示例](#12-mybatis插件编码示例)
    - [1.3. Mybatis插件运行机制](#13-mybatis插件运行机制)
        - [1.3.1. 拦截器](#131-拦截器)
        - [1.3.2. 被拦截的对象](#132-被拦截的对象)
    - [1.4. Mybatis插件运行解析](#14-mybatis插件运行解析)
        - [1.4.1. 插件的加载](#141-插件的加载)
        - [1.4.2. 拦截链](#142-拦截链)
        - [1.4.3. 动态代理](#143-动态代理)

<!-- /TOC -->

![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Mybatis/mybatis-32.png)  

&emsp; **<font color = "red">总结：</font>**   
&emsp; **<font color = "clime">其实mybatis扩展性很强，基于插件机制，基本上可以控制SQL执行的各个阶段，如执行阶段，参数处理阶段，语法构建阶段，结果集处理阶段，具体可以根据项目业务来实现对应业务逻辑。</font>**  

&emsp; **<font color="clime">Mybaits插件的实现主要用了拦截器、责任链和动态代理。</font>** 动态代理可以对SQL语句执行过程中的某一点进行拦截，当配置多个插件时，责任链模式可以进行多次拦截。  

&emsp; 有哪些对象允许被代理？有哪些方法可以被拦截？  
* 执行器Executor（update、query、commit、rollback等方法）；  
* 参数处理器ParameterHandler（getParameterObject、setParameters方法）；  
* 结果集处理器ResultSetHandler（handleResultSets、handleOutputParameters等方法）；  
* SQL语法构建器StatementHandler（prepare、parameterize、batch、update、query等方法）；    


# 1. Mybatis插件  
<!-- 

http://www.mybatis.cn/726.html
-->

## 1.1. Mybatis插件简介   
&emsp; <font color="red">编写Mybatis插件前，需要对Mybatis的运行原理、Mybatis插件原理有一定的了解。</font>  

&emsp; **Mybatis插件典型适用场景：**  

* 分页功能  
&emsp; <font color = "red">Mybatis的分页默认是基于内存分页的（查出所有，再截取）</font>，数据量大的情况下效率较低，使用mybatis插件可以改变该行为，只需要拦截StatementHandler类的prepare方法，改变要执行的SQL语句为分页语句即可；  
* 公共字段统一赋值  
&emsp; 一般业务系统都会有创建者，创建时间，修改者，修改时间四个字段，对于这四个字段的赋值，实际上可以在DAO层统一拦截处理，可以用mybatis插件拦截Executor类的update方法，对相关参数进行统一赋值即可；  
* 性能监控  
&emsp; 对于SQL语句执行的性能监控，可以通过拦截Executor类的update, query等方法，用日志记录每个方法执行的时间；  

&emsp; **<font color = "clime">其实mybatis扩展性很强，基于插件机制，基本上可以控制SQL执行的各个阶段，如执行阶段，参数处理阶段，语法构建阶段，结果集处理阶段，具体可以根据项目业务来实现对应业务逻辑。</font>**  

## 1.2. Mybatis插件编码示例  
&emsp; 1. 写一个打印SQL执行时间的插件  

```java
@Intercepts({@Signature(type = StatementHandler.class, method = "query", args = { Statement.class, ResultHandler.class }),
        @Signature(type = StatementHandler.class, method = "update", args = { Statement.class }),
        @Signature(type = StatementHandler.class, method = "batch", args = { Statement.class })})
public class SqlCostTimeInterceptor implements Interceptor {

    public static final Logger logger = LoggerFactory.getLogger(SqlCostTimeInterceptor.class);

    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        long start = System.currentTimeMillis();
        try {
            // 执行被拦截的方法
            return invocation.proceed();
        } finally {
            BoundSql boundSql = statementHandler.getBoundSql();
            String sql = boundSql.getSql();
            long end = System.currentTimeMillis();
            long cost = end - start;
            logger.info("{}, cost is {}", sql, cost);
        }
    }

    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    public void setProperties(Properties properties) {

    }
}
```
&emsp; 插件必须实现org.apache.ibatis.plugin.Interceptor接口。Mybatis规定插件必须编写@Intercepts注解，是必须，而不是可选。  

&emsp; 2. 在mybatis配置文件中配置插件：  
&emsp; Mybatis的插件配置在configuration内部，初始化时，会读取这些插件，保存于Configuration对象的InterceptorChain中。  

```xml
<plugins>
    <plugin interceptor="com.javashitang.part1.plugins.SqlCostTimeInterceptor"></plugin>
</plugins>
```

&emsp; 此时就可以打印出执行的SQL和耗费的时间，效果如下：  

```sql
select id, role_name as roleName, note from role where id = ?, cost is 35  
```

## 1.3. Mybatis插件运行机制  
&emsp; **<font color="clime">Mybaits插件的实现主要用了拦截器、责任链和动态代理。</font>** 动态代理可以对SQL语句执行过程中的某一点进行拦截，当配置多个插件时，责任链模式可以进行多次拦截。  

### 1.3.1. 拦截器  
<!-- 
★★★手把手教你开发 MyBatis 插件 
https://mp.weixin.qq.com/s/qcVSVeKIQA4RD4vlrzut7w
-->

### 1.3.2. 被拦截的对象
&emsp; 有哪些对象允许被代理？有哪些方法可以被拦截？在 MyBatis官网有参考，www.mybatis.org/mybatis-3/zh/configuration.html#plugins 。  
&emsp; 支持拦截的方法：  

* 执行器Executor（update、query、commit、rollback等方法）；  
* 参数处理器ParameterHandler（getParameterObject、setParameters方法）；  
* 结果集处理器ResultSetHandler（handleResultSets、handleOutputParameters等方法）；  
* SQL语法构建器StatementHandler（prepare、parameterize、batch、update、query等方法）；    


## 1.4. Mybatis插件运行解析
### 1.4.1. 插件的加载  
&emsp; Mybatis初始化中，会解析mybatis配置文件。Mybatis配置文件的解析在XMLConfigBuilder的parseConfiguration方法中，里面有插件的解析过程。  

```java
pluginElement(root.evalNode("plugins"));
```

```java
private void pluginElement(XNode parent) throws Exception {
    if (parent != null) {
        for (XNode child : parent.getChildren()) {
            String interceptor = child.getStringAttribute("interceptor");
            // 解析拦截器中配置的属性，并封装成一个Properties对象
            Properties properties = child.getChildrenAsProperties();
            // 通过类名示例化一个Interceptor对象
            Interceptor interceptorInstance = (Interceptor) resolveClass(interceptor).newInstance();
            // 可以给拦截器的Properties属性赋值
            interceptorInstance.setProperties(properties);
            configuration.addInterceptor(interceptorInstance);
        }
    }
}
```

```java
public class Configuration {
    //...
    public void addInterceptor(Interceptor interceptor) {
        this.interceptorChain.addInterceptor(interceptor);
    }
}
```
&emsp; 实例化好的Interceptor对象，会被放到InterceptorChain对象的interceptors属性中。  

### 1.4.2. 拦截链  

```java
public class InterceptorChain {

    private final List<Interceptor> interceptors = new ArrayList<Interceptor>();

    /** 这里有个特别有意思的地方，先添加的拦截器最后才会执行，因为代理是一层一层套上去的，就像这个函数f(f(f(x))) */
    public Object pluginAll(Object target) {
        for (Interceptor interceptor : interceptors) {
            target = interceptor.plugin(target);
        }
        return target;
    }

    public void addInterceptor(Interceptor interceptor) {
        interceptors.add(interceptor);
    }

    public List<Interceptor> getInterceptors() {
        return Collections.unmodifiableList(interceptors);
    }

}
```
&emsp; 上面的for循环代表了只要是插件，都会以责任链的方式逐一执行。  
&emsp; InterceptorChain对象的pluginAll方法就是用来生成代理对象的。在以下地方调用  

```java
parameterHandler = (ParameterHandler) interceptorChain.pluginAll(parameterHandler);
resultSetHandler = (ResultSetHandler) interceptorChain.pluginAll(resultSetHandler);
statementHandler = (StatementHandler) interceptorChain.pluginAll(statementHandler);
executor = (Executor) interceptorChain.pluginAll(executor);
```

### 1.4.3. 动态代理  
&emsp; Mybatis是通过动态代理的方式来额外增加功能的，因此调用目标对象的方法后，走的是代理对象的方法而不是原方法。  

&emsp; 拦截器编码中@Intercepts注解里面主要放多个@Signature注解，而@Signature注解则定义了要拦截的类和方法，并且提供了Interceptor接口和Plugin类方便实现动态代理。  
&emsp; Interceptor接口分析：  

```java
public interface Interceptor {

    /** 执行拦截逻辑的方法,Invocation只是将动态代理中获取到的一些参数封装成一个对象 */
    Object intercept(Invocation invocation) throws Throwable;

    /**
     * target是被拦截的对象，它的作用是给被拦截对象生成一个代理对象，并返回它。
     * 为了方便，可以直接使用Mybatis中org.apache.ibatis.plugin.Plugin类的wrap方法（是静态方法）生成代理对象
     */
    Object plugin(Object target);

    /** 根据配置初始化Interceptor对象 */
    void setProperties(Properties properties);

}
```

&emsp; 其中plugin方法生成代理对象，通常直接调用Plugin.wrap(target, this);方法来生成代理对象。  
&emsp; Plugin类源码：  

```java
public class Plugin implements InvocationHandler {

    /** 目标对象 */
    private final Object target;
    /** Interceptor对象 */
    private final Interceptor interceptor;
    /** 记录了@Signature注解中的信息 */
    /** 被拦截的type->被拦截的方法 */
    private final Map<Class<?>, Set<Method>> signatureMap;

    private Plugin(Object target, Interceptor interceptor, Map<Class<?>, Set<Method>> signatureMap) {
        this.target = target;
        this.interceptor = interceptor;
        this.signatureMap = signatureMap;
    }

    public static Object wrap(Object target, Interceptor interceptor) {
        // 拿到拦截器要拦截的类及其方法
        Map<Class<?>, Set<Method>> signatureMap = getSignatureMap(interceptor);
        // 取得要改变行为的类 (ParameterHandler | ResultSetHandler | StatementHandler | Executor)
        Class<?> type = target.getClass();
        // 拿到被代理对象的拦截方法，所实现的接口
        Class<?>[] interfaces = getAllInterfaces(type, signatureMap);
        // 如果当前传入的Target的接口中有@Intercepts注解中定义的接口，那么为之生成代理，否则原Target返回
        if (interfaces.length > 0) {
            return Proxy.newProxyInstance(
                    type.getClassLoader(),
                    interfaces,
                    new Plugin(target, interceptor, signatureMap));
        }
        return target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            // 获取当前方法所在类或接口中，可被当前 Interceptor 拦截的方法
            Set<Method> methods = signatureMap.get(method.getDeclaringClass());
            // 如果当前调用的方法需要被拦截，则调用interceptor.intercept()方法进行拦截处理
            if (methods != null && methods.contains(method)) {
                return interceptor.intercept(new Invocation(target, method, args));
            }
            // 如果当前调用的方法不能被拦截，则调用target对象的相应方法
            return method.invoke(target, args);
        } catch (Exception e) {
            throw ExceptionUtil.unwrapThrowable(e);
        }
    }
}
```

&emsp; 实现代理类和拦截特定方法使用Plugin.wrap()方法就可以实现。  
&emsp; 在Plugin.invoke()方法中，最终调用了Interceptor接口的intercept方法，并把目标类，目标方法，参数封装成一个Invocation对象。

```java
return interceptor.intercept(new Invocation(target, method, args));  
```
&emsp; Invocation类源码：  

```java
/**
 * 将要调用的类，方法，参数封装成一个对象，方便传递给拦截器
 */
public class Invocation {

    private final Object target;
    private final Method method;
    private final Object[] args;

    public Invocation(Object target, Method method, Object[] args) {
        this.target = target;
        this.method = method;
        this.args = args;
    }

    public Object getTarget() {
        return target;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return args;
    }

    /** 这个方法是给拦截器调用的，拦截器最后会调用这个方法来执行本来要执行的方法，这样就可以在方法前后加上拦截的逻辑了 */
    public Object proceed() throws InvocationTargetException, IllegalAccessException {
        return method.invoke(target, args);
    }

}
```
&emsp; 只有一个方法proceed()方法，而proceed()只是执行被拦截的方法。这时清楚了应该在Interceptor对象的intercept方法中做哪些操作了，只需要写增强的逻辑，最后调用Invocation对象的proceed()方法即可。  