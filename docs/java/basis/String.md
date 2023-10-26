

<!-- TOC -->

- [1. ~~String~~](#1-string)
    - [1.1. java.lang.String类](#11-javalangstring类)
        - [1.1.1. ★★★String常见面试题](#111-★★★string常见面试题)
            - [1.1.1.1. ★★★为什么Java字符串是不可变的？](#1111-★★★为什么java字符串是不可变的)
            - [1.1.1.2. 字符串常量池](#1112-字符串常量池)
            - [1.1.1.3. String创建了几个对象？](#1113-string创建了几个对象)
            - [1.1.1.4. String是常量，为什么给字符串重新赋值，值却改变了呢？](#1114-string是常量为什么给字符串重新赋值值却改变了呢)
            - [1.1.1.5. String真的不可变吗?](#1115-string真的不可变吗)
        - [1.1.2. 源码](#112-源码)
            - [1.1.2.1. String类的定义](#1121-string类的定义)
            - [1.1.2.2. 字段属性](#1122-字段属性)
            - [1.1.2.3. 构造方法](#1123-构造方法)
                - [1.1.2.3.1. 字符串编码格式](#11231-字符串编码格式)
            - [1.1.2.4. 成员方法](#1124-成员方法)
                - [1.1.2.4.1. equals()方法](#11241-equals方法)
                - [1.1.2.4.2. hashCode()方法](#11242-hashcode方法)
    - [1.2. StringBuilder与StringBuffer](#12-stringbuilder与stringbuffer)
    - [1.3. StringJoiner，字符串拼接](#13-stringjoiner字符串拼接)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. String类是用final关键字修饰的，所以认为其是不可变对象。反射可以改变String对象。  
&emsp; **<font color = "clime">为什么Java字符串是不可变的？</font>** 原因大致有以下三个：  
    * 为了实现字符串常量池。字符串常量池可以节省大量的内存空间。  
    * 为了线程安全。  
    * 为了 HashCode 的不可变性。String类经常被用作HashMap的key。  
2. 字符串常量池 
    ```java
    public static void main(String[] args) {
        String a = "abc";
        String b = "abc";
        String c = new String("abc");
        System.out.println(a == b);  // todo 结果ture
        System.out.println(a == c);  // todo 结果false
        System.out.println(a.equals(b));
        System.out.println(a.equals(c));
    }
    ```
    1. 是什么？  
    &emsp; 存储编译期类中产生的字符串类型数据，准确来说存储的是字符串实例对象的引用。JDK7.0版本，字符串常量池被移到了堆中，被整个JVM共享。  
    2. 示例：  
    &emsp; String s =“abc”；在编译期间，会将等号右边的“abc”常量放在常量池中，在程序运行时，会将s变量压栈，栈中s变量直接指向元空间的字符串常量池abc项，没有经过堆内存。  
    &emsp; String s = new String(“abc”);在编译期间，会将等号右边的“abc”常量放在常量池中，在程序运行时，先在堆中创建一个String对象，该对象的内容指向常量池的“abc”项。然后将s变量压栈，栈中s变量指向堆中的String对象。  
3. String创建了几个对象？  
&emsp; `String str1 = "java";`创建一个对象放在常量池中。  
&emsp; `String str2 = new String("java");`创建两个对象，字面量"java"创建一个对象放在常量池中，new String()又创建一个对象放在堆中。如果常量池中已经存在，则是创建了一个对象。  
&emsp; `String str3 = "hello "+"java";`创建了一个对象。  
&emsp; `String str5 = str3 + "java";`创建了三个对象。  
4. String不可变，安全；StringBuilder可变，线程不安全；StringBuffer可变，线程安全。  

-------------------------------------------------------------------------------------------------------

# 1. ~~String~~
<!-- 

为什么Java字符串是不可变的？
https://mp.weixin.qq.com/s?__biz=MzIxNzQwNjM3NA==&mid=2247486419&idx=1&sn=9591dfe5b6fd42bfb21bcc9a60123c8e&scene=21#wechat_redirect

原因大致有以下三个：
为了实现字符串常量池
为了线程安全
为了 HashCode 的不可变性


为什么要重写hashcode和equals方法？ 
https://mp.weixin.qq.com/s?__biz=MzA5NzgzODI5NA==&mid=2454036621&idx=2&sn=0cee25d9cb8a74983f22001a9e590373&chksm=872bb534b05c3c2264329e604207d35730f9fc84e9685b8d053fb8c8e1557b8e6496428a4010&mpshare=1&scene=1&srcid=&sharer_sharetime=1564621712738&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=58e504541863490e8749d6ad72ccd7d423de24183327cfd9286d8dcb2da835454d1dfbab22f24716ca79d85eac49ee583ba350b29fc3379b3ad2e94db6fd63c6a934a6573f1ba8c21c41910887cdb473&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060834&lang=zh_CN&pass_ticket=Mbhb8FaRfUTIY8b9kkeu8wiUoom0HSJs9Ql%2FZjQ3YT5H9f0SJWiPLFSbnehO49gB
编写equals和hashcode方法
https://juejin.im/post/5d8b4b3df265da5b591b513d
https://mp.weixin.qq.com/s/Iix9FHKPhOu3ck7ysc6TJw
-->


## 1.1. java.lang.String类  
&emsp; **<font color = "clime">String对象一旦被创建就是固定不变的了，对String对象的任何改变都不影响到原对象，相关的任何change操作都会生成新的对象。</font>**  

### 1.1.1. ★★★String常见面试题  
#### 1.1.1.1. ★★★为什么Java字符串是不可变的？
&emsp; **<font color = "clime">为什么Java字符串是不可变的？</font>** 原因大致有以下三个：  

* 为了实现字符串常量池。字符串常量池可以节省大量的内存空间。  
* 为了线程安全。  
* 为了 HashCode 的不可变性。String类经常被用作HashMap的key。  

<!--
**为什么 Java 的 String 要设计成不可变的啊？
https://mp.weixin.qq.com/s/XV69p7yGrUY5dHjBJztNAA
https://mp.weixin.qq.com/s/WcHhpUq-M74sewtJ-DIBag
https://mp.weixin.qq.com/s/xx-aTQFqJHFPMtoXzBx8Fw
-->
<!--
&emsp; 那么，String 类为什么要这样设计成不可变呢？可以从性能以及安全方面来考虑：  
* 安全  
&emsp; 引发安全问题，譬如，数据库的用户名、密码都是以字符串的形式传入来获得数据库的连接，或者在socket编程中，主机名和端口都是以字符串的形式传入。因为字符串是不可变的，所以它的值是不可改变的，否则黑客们可以钻到空子，改变字符串指向的对象的值，造成安全漏洞。
&emsp; 保证线程安全，在并发场景下，多个线程同时读写资源时，会引竞态条件，由于 String 是不可变的，不会引发线程的问题而保证了线程。  
&emsp; &emsp; HashCode，当 String 被创建出来的时候，hashcode也会随之被缓存，hashcode的计算与value有关，若 String 可变，那么 hashcode 也会随之变化，针对于 Map、Set 等容器，他们的键值需要保证唯一性和一致性，因此，String 的不可变性使其比其他对象更适合当容器的键值。
* 性能  
&emsp; 当字符串是不可变时，字符串常量池才有意义。字符串常量池的出现，可以减少创建相同字面量的字符串，让不同的引用指向池中同一个字符串，为运行时节约很多的堆内存。若字符串可变，字符串常量池失去意义，基于常量池的String.intern()方法也失效，每次创建新的 String 将在堆内开辟出新的空间，占据更多的内存。 
-->

#### 1.1.1.2. 字符串常量池  
<!-- 
https://zhuanlan.zhihu.com/p/149055800?utm_id=0
-->
```java
public static void main(String[] args) {
    String a = "abc";
    String b = "abc";
    String c = new String("abc");
    System.out.println(a == b);  // todo 结果ture
    System.out.println(a == c);  // todo 结果false
    System.out.println(a.equals(b));
    System.out.println(a.equals(c));
}
```

1. 是什么？  
&emsp; 存储编译期类中产生的字符串类型数据，准确来说存储的是字符串实例对象的引用。JDK7.0版本，字符串常量池被移到了堆中，被整个JVM共享。  
2. 示例：  
&emsp; String s =“abc”；在编译期间，会将等号右边的“abc”常量放在常量池中，在程序运行时，会将s变量压栈，栈中s变量直接指向元空间的字符串常量池abc项，没有经过堆内存。  
&emsp; String s = new String(“abc”);在编译期间，会将等号右边的“abc”常量放在常量池中，在程序运行时，先在堆中创建一个String对象，该对象的内容指向常量池的“abc”项。然后将s变量压栈，栈中s变量指向堆中的String对象。  


#### 1.1.1.3. String创建了几个对象？
```java
String str1 = "java";
String str2 = new String("java");
String str3 = "hello "+"java";
String str4 = "hello ";
String str5 = str4 + "java";

String str6= "java";
String str7 = new String("java");
System.out.println(str1 == str5); //true
System.out.println(str2 == str6); //false
```  
&emsp; String str1 = "java";创建一个对象放在常量池中。  
&emsp; String str2 = new String("java");创建两个对象，字面量"java"创建一个对象放在常量池中，new String()又创建一个对象放在堆中。如果常量池中已经存在，则是创建了一个对象。  
&emsp; String str3 = "hello "+"java"; 创建了一个对象。  
&emsp; String str5 = str3 + "java";创建了三个对象。

#### 1.1.1.4. String是常量，为什么给字符串重新赋值，值却改变了呢？  
<!-- https://baijiahao.baidu.com/s?id=1692401311615162256&wfr=spider&for=pc -->
&emsp; 字符串s只是一个 String 对象的引用，并不是对象本身，当执行 s = “123”; 创建了一个新的对象 “123”，而原来的 “abc” 还存在于内存中，所以只是s的引用地址发生了变化。  

#### 1.1.1.5. String真的不可变吗?   
&emsp; String 类是用 final 关键字修饰的，所以认为其是不可变对象。但是真的不可变吗？  
&emsp; 每个字符串都是由许多单个字符组成的，其源码是由 char[] value字符数组构成。  

```java
public final class String
        implements java.io.Serializable, Comparable<String>, CharSequence {
    /** The value is used for character storage. */
    private final char value[];

    /** Cache the hash code for the string */
    private int hash; // Default to 0
```
&emsp; value被final修饰，只能保证引用不被改变，但是value所指向的堆中的数组，才是真实的数据，只要能够操作堆中的数组，依旧能改变数据。而且value是基本类型构成，那么一定是可变的，即使被声明为 private，也可以通过反射来改变。  

```java
String str = "vae";
//打印原字符串
System.out.println(str);//vae
//获取String类中的value字段
Field fieldStr = String.class.getDeclaredField("value");
//因为value是private声明的，这里修改其访问权限
fieldStr.setAccessible(true);
//获取str对象上的value属性的值
char[] value = (char[]) fieldStr.get(str);
//将第一个字符修改为 V(小写改大写)
value[0] = 'V';
//打印修改之后的字符串
System.out.println(str);//Vae
```
&emsp; 通过前后两次打印的结果，可以看到String被改变了，但是在代码里，几乎不会使用反射的机制去操作String字符串，所以认为String类型是不可变的。  


### 1.1.2. 源码
#### 1.1.2.1. String类的定义  

```java
public final class String
   implements java.io.Serializable, Comparable<String>, CharSequence {}
```
&emsp; String是一个用final声明的常量类，不能被任何类所继承，而且一旦<font color = "red">一个String对象被创建，包含在这个对象中的字符序列是不可改变的，包括该类后续的所有方法都是不能修改该对象的</font>，直至该对象被销毁，这是需要特别注意的(该类的一些方法看似改变了字符串，其实内部都是创建一个新的字符串)。  
&emsp; 接着实现了Serializable接口，这是一个序列化标志接口，还实现了Comparable接口，用于比较两个字符串的大小(按顺序比较单个字符的ASCII码)；最后实现了CharSequence接口，表示是一个有序字符的集合。  

#### 1.1.2.2. 字段属性  

```java
private final char value[];//存储字符串
private int hash; //字符串的hash code 默认是0
private static final long serialVersionUID = -6849794470754667710L;//序列化id
```
&emsp; String对象的字符串实际是维护在一个字符数组中的。操作字符串实际上就是操作这个字符数组，而且这个数组也是final修饰的不能够被改变。  

#### 1.1.2.3. 构造方法
&emsp; String类的构造方法很多。可以通过初始化一个字符串，或者字符数组，或者字节数组等来创建一个String对象。  

```java
public String(byte bytes[], String charsetName) throws UnsupportedEncodingException {
    this(bytes, 0, bytes.length, charsetName);
}
public String(byte bytes[], Charset charset) {
    this(bytes, 0, bytes.length, charset);
}
```
&emsp; 通过byte数组，指定字符集构造String对象。byte是网络传输或存储的序列化形式，所以在很多传输和存储的过程中需要将byte[] 数组和String进行相互转化，byte是字节，char是字符，字节流和字符流需要指定编码，不然可能会乱码，bytes字节流是使用charset进行编码的，想要将它转换成unicode的char[]数组，而又保证不出现乱码，那就要指定其解码方法。  

##### 1.1.2.3.1. 字符串编码格式  
&emsp; 改变String的编码格式：String的的构造方法String (btye [], String )。  

```java
String str = "任意字符串";
str = new String(str.getBytes("gbk"),"utf-8");
```  
&emsp; 注：str.getBytes("UTF-8");以UTF-8的编码取得字节；  
&emsp; new String(XXX,"UTF-8"); 以UTF-8的编码生成字符串。  

#### 1.1.2.4. 成员方法  
##### 1.1.2.4.1. equals()方法  

```java
public boolean equals(Object anObject) {
    if (this == anObject) {
        return true;
    }
    if (anObject instanceof String) {
        String anotherString = (String)anObject;
        int n = value.length;
        if (n == anotherString.value.length) {
            char v1[] = value;
            char v2[] = anotherString.value;
            int i = 0;
            while (n-- != 0) {
                if (v1[i] != v2[i])
                    return false;
                i++;
            }
            return true;
        }
    }
    return false;
}
```
&emsp; String类重写了equals方法，比较的是组成字符串的每一个字符是否相同，如果都相同则返回true，否则返回false。  
1. 首先会判断要比较的两个字符串它们的引用是否相等。如果引用相等的话，直接返回 true ，不相等的话继续下面的判断。  
2. 然后再判断被比较的对象是否是 String 的实例，如果不是的话直接返回 false，如果是的话，再比较两个字符串的长度是否相等，如果长度不想等的话也就没有比较的必要了；长度如果相同，会比较字符串中的每个 字符 是否相等，一旦有一个字符不相等，就会直接返回 false。  
![image](http://182.92.69.8:8081/img/java/JDK/basics/java-4.png)  

##### 1.1.2.4.2. hashCode()方法  

```java
public int hashCode() {
    int h = hash;
    if (h == 0 && value.length > 0) {
        char val[] = value;

        for (int i = 0; i < value.length; i++) {
            h = 31 * h + val[i];
        }
        hash = h;
    }
    return h;
}
```  
&emsp; String的hashCode()计算公式为：

    s\[0]*31^(n-1) + s\[1]*31^(n-2) + ... + s\[n-1]  
&emsp; **<font color = "clime">String的hashCode()计算过程中，为什么使用了数字31，主要有以下原因：</font>**   
1. <font color = "red">使用质数计算哈希码，</font>由于质数的特性，它与其他数字相乘之后，计算结果唯一的概率更大，<font color = "red">哈希冲突的概率更小。</font>  
2. <font color = "red">使用的质数越大，哈希冲突的概率越小，但是计算的速度也越慢；31是哈希冲突和性能的折中，实际上是实验观测的结果。</font>  
3. <font color = "red">JVM会自动对31进行优化：31 * i == (i << 5) - i</font>  

## 1.2. StringBuilder与StringBuffer  

&emsp; String、StringBuffer、StringBuilder的比较：  

* 可变与不可变：String不可变，StringBuffer和StringBuilder可变。  
* 线程安全：  
&emsp; String中的对象是不可变的，也就可以理解为常量，线程安全。  
&emsp; <font color = "red">StringBuffer线程安全，效率不高；StringBuilder线程不安全，效率高。</font>    

&emsp; 总结：  
&emsp; StringBuilder/StringBuffer的长度是可变的，如果对字符串中的内容经常进行操作，特别是内容要修改时，那么使用StringBuilder/ StringBuffer，如果最后需要String，那么使用StringBuilder/StringBuffer的toString()方法；   

&emsp; <font color = "red">StringBuffer和StringBuilder的3个区别：</font>  
&emsp; StringBuffer和StringBuilder的类结构：  
![image](http://182.92.69.8:8081/img/java/JDK/basics/java-7.png)  

&emsp; 继承了一个抽象的字符串父类：AbstractStringBuilder。  

* 区别1：线程安全  
    &emsp; StringBuffer：线程安全，StringBuilder：线程不安全。因为StringBuffer的所有公开方法都是synchronized修饰的，而StringBuilder并没有synchronized修饰。

    &emsp; StringBuffer代码片段：  
    ```java
    @Override
    public synchronized StringBuffer append(String str) {
        toStringCache = null;
        super.append(str);
        return this;
    }
    ```

* 区别2：缓冲区  

    &emsp; StringBuffer代码片段：  

    ```java
    private transient char[] toStringCache;

    @Override
    public synchronized String toString() {
        if (toStringCache == null) {
            toStringCache = Arrays.copyOfRange(value, 0, count);
        }
        return new String(toStringCache, true);
    }
    ```

    &emsp; StringBuilder代码片段：  
    ```java
    @Override
    public String toString() {
        // Create a copy, don't share the array
        return new String(value, 0, count);
    }
    ```

    &emsp; StringBuffer每次获取toString都会直接使用缓存区的toStringCache值来构造一个字符串。  
    &emsp; 而StringBuilder则每次都需要复制一次字符数组，再构造一个字符串。  
    &emsp; 所以，缓存冲这也是对StringBuffer的一个优化吧，不过StringBuffer的这个toString方法仍然是同步的。  

* 区别3：性能  
    &emsp; 既然StringBuffer是线程安全的，它的所有公开方法都是同步的，StringBuilder 是没有对方法加锁同步的，所以毫无疑问，StringBuilder 的性能要远大于 StringBuffer。  

## 1.3. StringJoiner，字符串拼接  
<!-- 


你只会用 StringBuilder？试试 StringJoiner，真香！ 
https://mp.weixin.qq.com/s/25PjDDuMKH3qv8LZimyUpg
字符串拼接还在用StringBuilder？快试试Java8中的StringJoiner吧，真香！ 
https://mp.weixin.qq.com/s/Zpyo1OCnAZLV4cwt1lpu9g
-->