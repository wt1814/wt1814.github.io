
<!-- TOC -->

- [1. 自定义注解](#1-自定义注解)
    - [1.1. 引入注解(注解和xml)](#11-引入注解注解和xml)
    - [1.3. 注解简介](#13-注解简介)
    - [1.2. JDK的注解描述](#12-jdk的注解描述)
    - [1.4. 自定义注解示例](#14-自定义注解示例)
    - [1.5. 元注解(和关键字@interface配合使用的注解)](#15-元注解和关键字interface配合使用的注解)
    - [1.6. Annotation接口](#16-annotation接口)
    - [1.7. 和代码关联](#17-和代码关联)
    - [1.8. 使用动态代理机制处理注解](#18-使用动态代理机制处理注解)
    - [1.10. 自定义注解加AOP](#110-自定义注解加aop)

<!-- /TOC -->


# 1. 自定义注解  

<!--
https://blog.51cto.com/14230003/2440990
注解就这么简单
https://mp.weixin.qq.com/s?__biz=MzI4Njg5MDA5NA==&mid=2247484113&idx=1&sn=f5fd35b2f7dc45a256fee170cad0cdc9&chksm=ebd743d0dca0cac6a8fed8caf09f0d561b0358dece9b8e746e1e6726bca093287cf515638349&scene=21#wechat_redirect
 JDK中注解的底层原来是这样实现的
https://mp.weixin.qq.com/s/fWeWHceRqNEEmi-O3GnV0A

https://docs.oracle.com/javase/tutorial/java/annotations/index.html
-->

## 1.1. 引入注解(注解和xml)  
&emsp; 使用Annotation之前(甚至在使用之后)，XML被广泛的应用于描述元数据。不知何时开始一些应用开发人员和架构师发现XML的维护越来越糟糕了。他们希望使用一些和代码紧耦合的东西，而不是像XML那样和代码是松耦合的(在某些情况下甚至是完全分离的)代码描述。  
&emsp; 如果在Google中搜索“XML vs. annotations”，会看到许多关于这个问题的辩论。最有趣的是XML配置其实就是为了分离代码和配置而引入的。上述两种观点可能会让你很疑惑，两者观点似乎构成了一种循环，但各有利弊。下面我们通过一个例子来理解这两者的区别。  
&emsp; 假如想为应用设置很多的常量或参数，这种情况下，XML是一个很好的选择，因为它不会同特定的代码相连。如果你想把某个方法声明为服务，那么使用Annotation会更好一些，因为这种情况下需要注解和方法紧密耦合起来，开发人员也必须认识到这点。  
&emsp; 另一个很重要的因素是Annotation定义了一种标准的描述元数据的方式。在这之前，开发人员通常使用他们自己的方式定义元数据。例如，使用标记interfaces，注释，transient关键字等等。每个程序员按照自己的方式定义元数据，而不像Annotation这种标准的方式。  
&emsp; 目前，许多框架将XML和Annotation两种方式结合使用，平衡两者之间的利弊。  

## 1.3. 注解简介  
&emsp; 注解其实就是代码中的特殊标记，这些标记可以在编译、类加载、运行时被读取，并执行相对应的处理。  
&emsp; Java注解是附加在代码中的一些元信息，用于一些工具在编译、运行时进行解析和使用，起到说明、配置的功能。  
&emsp; 注解不会也不能影响代码的实际逻辑，仅仅起到辅助性的作用。包含在java.lang.annotation包中。  

&emsp; java注解是JDK1.5引入的一种注释机制，java语言的类、方法、变量、参数和包都可以被注解标注。  
&emsp; 和Javadoc不同，java注解可以通过反射获取标注内容。  
&emsp; 在编译器生成.class文件时，注解可以被嵌入字节码中，而jvm也可以保留注解的内容，在运行时获取注解标注的内容信息。  
&emsp; java提供的注解可以有两类：功能注解和元注解。  

## 1.2. JDK的注解描述
<!-- 
https://mp.weixin.qq.com/s/fWeWHceRqNEEmi-O3GnV0A
-->
<!-- 
https://mp.weixin.qq.com/s/QoJnf2vPCWR5ClCjSkiPsQ
-->
&emsp; JDK5.0加入了下面三个内置注解：  

```
1. @Override：表示当前的方法定义将覆盖父类中的方法
2. @Deprecated：表示代码被弃用，如果使用了被@Deprecated注解的代码则编译器将发出警告
3. @SuppressWarnings：表示关闭编译器警告信息
```

## 1.4. 自定义注解示例

## 1.5. 元注解(和关键字@interface配合使用的注解)  

|元注解名称	|功能描述|
|---|---|
|@Retention	|标识这个注释解怎么保存，是只在代码中，还是编入类文件中，或者是在运行时可以通过反射访问|
|@Documented	|标识这些注解是否包含在用户文档中|
|@Target|	标识这个注解的作用范围|
|@Inherited	|标识注解可被继承类获取|
|@Repeatable	|标识某注解可以在同一个声明上使用多次|

&emsp; 自定义注解类型时，一般需要用@Retention指定注解保留范围RetentionPolicy，@Target指定使用范围ElementType。RetentionPolicy保留范围只能指定一个，ElementType使用范围可以指定多个。    

* @Retention：指定注解信息保留阶段，有如下三种枚举选择。只能选其一  

    ```java
    public enum RetentionPolicy {
        /** 注解将被编译器丢弃，生成的class不包含注解信息 */
        SOURCE,
        /** 注解在class文件中可用，但会被JVM丢弃;当注解未定义Retention值时，默认值是CLASS */
        CLASS,
        /** 注解信息在运行期(JVM)保留（.class也有），可以通过反射机制读取注解的信息,
        * 操作方法看AnnotatedElement(所有被注释类的父类) */
        RUNTIME
    }public enum RetentionPolicy {
        /** 注解将被编译器丢弃，生成的class不包含注解信息 */
        SOURCE,
        /** 注解在class文件中可用，但会被JVM丢弃;当注解未定义Retention值时，默认值是CLASS */
        CLASS,
        /** 注解信息在运行期(JVM)保留（.class也有），可以通过反射机制读取注解的信息,
        * 操作方法看AnnotatedElement(所有被注释类的父类) */
        RUNTIME
    }
    ```

* @Documented：作用是告诉JavaDoc工具，当前注解本身也要显示在Java Doc中(不常用)  
* @Target：指定注解作用范围，可指定多个  

    ```java
    public enum ElementType {
        /** 适用范围：类、接口、注解类型，枚举类型enum */
        TYPE,
        /** 作用于类属性 (includes enum constants) */
        FIELD,
        /** 作用于方法 */
        METHOD,
        /** 作用于参数声明 */
        PARAMETER,
        /** 作用于构造函数声明 */
        CONSTRUCTOR,
        /** 作用于局部变量声明 */
        LOCAL_VARIABLE,
        /** 作用于注解声明 */
        ANNOTATION_TYPE,
        /** 作用于包声明 */
        PACKAGE,
        /** 作用于类型参数（泛型参数）声明 */
        TYPE_PARAMETER,
        /** 作用于使用类型的任意语句(不包括class) */
        TYPE_USE
    }
    ```
* @Inherited：表示当前注解会被注解类的子类继承。即在子类Class<T>通过getAnnotations()可获取父类被@Inherited修饰的注解。而注解本身是不支持继承  

    ```java
    @Inherited
    @Retention( value = RetentionPolicy.RUNTIME)
    @Target(value = ElementType.TYPE)
    public @interface ATest {  }
    ----被ATest注解的父类PTest----
    @ATest
    public class PTest{ }

    ---Main是PTest的子类----
    public class Main extends PTest {
        public static void main(String[] args){
            Annotation an = Main.class.getAnnotations()[0];
        //Main可以拿到父类的注解ATest，因为ATest被元注解@Inherited修饰
            System.out.println(an);
        }
    }  
    ---result--
    @com.ATest()  
    ```

* @Repeatable：JDK1.8新加入的，表明自定义的注解可以在同一个位置重复使用。在没有该注解前，是无法在同一个类型上使用相同的注解多次   

    ```java
    //Java8前无法重复使用注解
    @FilterPath("/test/v2")
    @FilterPath("/test/v1")
    public class Test {}
    ```


## 1.6. Annotation接口  
&emsp; Annotation是所有注解类的共同接口，不用显示实现。注解类使用@interface定义（代表它实现Annotation接口），搭配元注解使用，如下  

```java
package java.lang.annotation;
public interface Annotation {
    boolean equals(Object obj);
    int hashCode();
    String toString();
    // 返回定义的注解类型，你在代码声明的@XXX,相当于该类型的一实例
    Class<? extends Annotation> annotationType();
}
-----自定义示例-----
@Retention( value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
public @interface ATest {
 String hello() default  "siting";
}
```

&emsp; ATest的字节码文件，编译器让自定义注解实现了Annotation接口  

```java
public abstract @interface com/ATest implements java/lang/annotation/Annotation {
  // compiled from: ATest.java
  @Ljava/lang/annotation/Retention;(value=Ljava/lang/annotation/RetentionPolicy;.RUNTIME)
  @Ljava/lang/annotation/Target;(value={Ljava/lang/annotation/ElementType;.TYPE})
  // access flags 0x401
  public abstract hello()Ljava/lang/String;
    default="siting"
}
```

## 1.7. 和代码关联
&emsp; 注解信息怎么和代码关联在一起，java所有事物都是类，注解也不例外，加入代码System.setProperty("sum.misc.ProxyGenerator.saveGeneratedFiles","true"); 可生成注解相应的代理类  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JDK/basics/java-8.png)  
在代码里定义的注解，会被jvm利用反射技术生成一个代理类，然后和被注释的代码（类，方法，属性等）关联起来    

## 1.8. 使用动态代理机制处理注解  
<!-- 
https://blog.csdn.net/baidu_36385172/article/details/79953410
把自定义注解的基本信息注入到方法上
https://mp.weixin.qq.com/s?__biz=MzI4Njg5MDA5NA==&mid=2247484113&idx=1&sn=f5fd35b2f7dc45a256fee170cad0cdc9&chksm=ebd743d0dca0cac6a8fed8caf09f0d561b0358dece9b8e746e1e6726bca093287cf515638349&scene=21#wechat_redirect
-->

&emsp; 反射机制获取注解信息  

```java
--- 作用于注解的注解----
@Inherited
@Retention( value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.ANNOTATION_TYPE})
public @interface AnnotationTest {
    String value() default "AnnotationTest";
}
------父类-------
public class PTest {}
------被注解修饰的package-info.java------
//package-info.java
@AnTest("com-package-info")
package com;
-------------
@AnnotationTest("AnnotationTest")
@Inherited
@Retention( value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE_USE,ElementType.PACKAGE,ElementType.FIELD,
        ElementType.TYPE_PARAMETER,ElementType.CONSTRUCTOR,ElementType.LOCAL_VARIABLE})
public @interface AnTest  {
    String value() default  "siting";
}--- 作用于注解的注解----
@Inherited
@Retention( value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.ANNOTATION_TYPE})
public @interface AnnotationTest {
    String value() default "AnnotationTest";
}
------父类-------
public class PTest {}
------被注解修饰的package-info.java------
//package-info.java
@AnTest("com-package-info")
package com;
-------------
@AnnotationTest("AnnotationTest")
@Inherited
@Retention( value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE_USE,ElementType.PACKAGE,ElementType.FIELD,
        ElementType.TYPE_PARAMETER,ElementType.CONSTRUCTOR,ElementType.LOCAL_VARIABLE})
public @interface AnTest  {
    String value() default  "siting";
}
```

&emsp; 运行示例  

```java
//注解类
@AnTest("mainClass")
//注解泛型参数                                       //注解继承父类
public class Main<@AnTest("parameter") T > extends @AnTest("parent") PTest {
    @AnTest("constructor")  //注解构造函数
    Main(){ }
    //注解字段域
    private @AnTest("name") String name;
    //注解泛型字段域
    private @AnTest("value") T value;
    //注解通配符
    private @AnTest("list")List<@AnTest("generic") ?>list;
    //注解方法
    @AnTest("method")                       //注解方法参数
    public String hello(@AnTest("methodParameter") String name)
            throws @AnTest("Exception") Exception { // 注解抛出异常
        //注解局部变量,现在运行时暂时无法获取（忽略）
        @AnTest("result") String result;
        result = "siting";
        System.out.println(name);
        return  result;
    }

    public static void main(String[] args) throws Exception {

        Main<String>  main = new Main<> ();
        Class<Main<Object>> clazz = (Class<Main<Object>>) main.getClass();
        //class的注解
        Annotation[] annotations = clazz.getAnnotations();
        AnTest testTmp = (AnTest) annotations[0];
        System.out.println("修饰Main.class注解value: "+testTmp.value());
        //构造器的注解
        Constructor<Main<Object>> constructor = (Constructor<Main<Object>>) clazz.getDeclaredConstructors()[0];
        testTmp = constructor.getAnnotation(AnTest.class);
        System.out.println("修饰构造器的注解value: "+testTmp.value());
        //继承父类的注解
        AnnotatedType annotatedType = clazz.getAnnotatedSuperclass();
        testTmp = annotatedType.getAnnotation(AnTest.class);
        System.out.println("修饰继承父类的注解value: "+testTmp.value());
        //注解的注解
        AnnotationTest annotationTest = testTmp.annotationType().getAnnotation(AnnotationTest.class);
        System.out.println("修饰注解的注解AnnotationTest-value: "+annotationTest.value());
        //泛型参数 T 的注解
        TypeVariable<Class<Main<Object>>> variable = clazz.getTypeParameters()[0];
        testTmp = variable.getAnnotation(AnTest.class);
        System.out.println("修饰泛型参数T注解value: "+testTmp.value());
        //普通字段域 的注解
        Field[] fields = clazz.getDeclaredFields();
        Field nameField = fields[0];
        testTmp = nameField.getAnnotation(AnTest.class);
        System.out.println("修饰普通字段域name注解value: "+testTmp.value());
        //泛型字段域 的注解
        Field valueField = fields[1];
        testTmp = valueField.getAnnotation(AnTest.class);
        System.out.println("修饰泛型字段T注解value: "+testTmp.value());
        //通配符字段域 的注解
        Field listField = fields[2];
        AnnotatedParameterizedType annotatedPType = (AnnotatedParameterizedType)listField.getAnnotatedType();
        testTmp = annotatedPType.getAnnotation(AnTest.class);
        System.out.println("修饰泛型注解value: "+testTmp.value());
        //通配符注解 的注解
        AnnotatedType[] annotatedTypes = annotatedPType.getAnnotatedActualTypeArguments();
        AnnotatedWildcardType annotatedWildcardType = (AnnotatedWildcardType) annotatedTypes[0];
        testTmp = annotatedWildcardType.getAnnotation(AnTest.class);
        System.out.println("修饰通配符注解value: "+testTmp.value());
        //方法的注解
        Method method = clazz.getDeclaredMethod("hello", String.class);
        annotatedType = method.getAnnotatedReturnType();
        testTmp = annotatedType.getAnnotation(AnTest.class);
        System.out.println("修饰方法的注解value: "+testTmp.value());
        //异常的注解
        annotatedTypes =  method.getAnnotatedExceptionTypes();
        testTmp = annotatedTypes[0].getAnnotation(AnTest.class);
        System.out.println("修饰方法抛出错误的注解value: "+testTmp.value());
        //方法参数的注解
        annotatedTypes = method.getAnnotatedParameterTypes();
        testTmp = annotatedTypes[0].getAnnotation(AnTest.class);
        System.out.println("修饰方法参数注解value: "+testTmp.value());
        //包的注解
        Package p = Package.getPackage("com");
        testTmp = p.getAnnotation(AnTest.class);
        System.out.println("修饰package注解value: "+testTmp.value());
        main.hello("hello");
    }
}//注解类
@AnTest("mainClass")
//注解泛型参数                                       //注解继承父类
public class Main<@AnTest("parameter") T > extends @AnTest("parent") PTest {
    @AnTest("constructor")  //注解构造函数
    Main(){ }
    //注解字段域
    private @AnTest("name") String name;
    //注解泛型字段域
    private @AnTest("value") T value;
    //注解通配符
    private @AnTest("list")List<@AnTest("generic") ?>list;
    //注解方法
    @AnTest("method")                       //注解方法参数
    public String hello(@AnTest("methodParameter") String name)
            throws @AnTest("Exception") Exception { // 注解抛出异常
        //注解局部变量,现在运行时暂时无法获取（忽略）
        @AnTest("result") String result;
        result = "siting";
        System.out.println(name);
        return  result;
    }

    public static void main(String[] args) throws Exception {

        Main<String>  main = new Main<> ();
        Class<Main<Object>> clazz = (Class<Main<Object>>) main.getClass();
        //class的注解
        Annotation[] annotations = clazz.getAnnotations();
        AnTest testTmp = (AnTest) annotations[0];
        System.out.println("修饰Main.class注解value: "+testTmp.value());
        //构造器的注解
        Constructor<Main<Object>> constructor = (Constructor<Main<Object>>) clazz.getDeclaredConstructors()[0];
        testTmp = constructor.getAnnotation(AnTest.class);
        System.out.println("修饰构造器的注解value: "+testTmp.value());
        //继承父类的注解
        AnnotatedType annotatedType = clazz.getAnnotatedSuperclass();
        testTmp = annotatedType.getAnnotation(AnTest.class);
        System.out.println("修饰继承父类的注解value: "+testTmp.value());
        //注解的注解
        AnnotationTest annotationTest = testTmp.annotationType().getAnnotation(AnnotationTest.class);
        System.out.println("修饰注解的注解AnnotationTest-value: "+annotationTest.value());
        //泛型参数 T 的注解
        TypeVariable<Class<Main<Object>>> variable = clazz.getTypeParameters()[0];
        testTmp = variable.getAnnotation(AnTest.class);
        System.out.println("修饰泛型参数T注解value: "+testTmp.value());
        //普通字段域 的注解
        Field[] fields = clazz.getDeclaredFields();
        Field nameField = fields[0];
        testTmp = nameField.getAnnotation(AnTest.class);
        System.out.println("修饰普通字段域name注解value: "+testTmp.value());
        //泛型字段域 的注解
        Field valueField = fields[1];
        testTmp = valueField.getAnnotation(AnTest.class);
        System.out.println("修饰泛型字段T注解value: "+testTmp.value());
        //通配符字段域 的注解
        Field listField = fields[2];
        AnnotatedParameterizedType annotatedPType = (AnnotatedParameterizedType)listField.getAnnotatedType();
        testTmp = annotatedPType.getAnnotation(AnTest.class);
        System.out.println("修饰泛型注解value: "+testTmp.value());
        //通配符注解 的注解
        AnnotatedType[] annotatedTypes = annotatedPType.getAnnotatedActualTypeArguments();
        AnnotatedWildcardType annotatedWildcardType = (AnnotatedWildcardType) annotatedTypes[0];
        testTmp = annotatedWildcardType.getAnnotation(AnTest.class);
        System.out.println("修饰通配符注解value: "+testTmp.value());
        //方法的注解
        Method method = clazz.getDeclaredMethod("hello", String.class);
        annotatedType = method.getAnnotatedReturnType();
        testTmp = annotatedType.getAnnotation(AnTest.class);
        System.out.println("修饰方法的注解value: "+testTmp.value());
        //异常的注解
        annotatedTypes =  method.getAnnotatedExceptionTypes();
        testTmp = annotatedTypes[0].getAnnotation(AnTest.class);
        System.out.println("修饰方法抛出错误的注解value: "+testTmp.value());
        //方法参数的注解
        annotatedTypes = method.getAnnotatedParameterTypes();
        testTmp = annotatedTypes[0].getAnnotation(AnTest.class);
        System.out.println("修饰方法参数注解value: "+testTmp.value());
        //包的注解
        Package p = Package.getPackage("com");
        testTmp = p.getAnnotation(AnTest.class);
        System.out.println("修饰package注解value: "+testTmp.value());
        main.hello("hello");
    }
}
```

&emsp; 结果  

```java
修饰Main.class注解value: mainClass
修饰构造器的注解value: constructor
修饰继承父类的注解value: parent
修饰注解的注解AnnotationTest-value: AnnotationTest
修饰泛型参数T注解value: parameter
修饰普通字段域name注解value: name
修饰泛型字段T注解value: value
修饰泛型注解value: list
修饰通配符注解value: generic
修饰方法的注解value: method
修饰方法抛出错误的注解value: Exception
修饰方法参数注解value: methodParameter
修饰package注解value: com-package-info
hello
```

## 1.10. 自定义注解加AOP  
<!-- 
https://mp.weixin.qq.com/s?__biz=Mzg3NDA4MjYyOQ==&mid=2247484180&idx=1&sn=eaf6841dd6fb5fa439939cb22eb7943d&chksm=ced77b3ef9a0f228fa1a1c02e0b10bc001074330d5bf7347ce4db77f93d969d579105f17421b&mpshare=1&scene=1&srcid=&sharer_sharetime=1576454959142&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=9ad84b8c73b256ab454ff3236e2adf563fd05891f9621b66002a4801c5cc07b7e42942bfc51b52be96e8411a42ca4093bed4af07d23a41f342d4ddd3d657cb1560993b71be5599ddc57c97c22fc1ecb7&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070158&lang=zh_CN&exportkey=ASj2f7syqgSgV3RMqH7GQoQ%3D&pass_ticket=bfh6Om%2FZtKWXqMXmAUqyFIYCxvpcWFuUU%2FsESm2I4eWOoa6HSoky8oPs67hVAuPg
-->
