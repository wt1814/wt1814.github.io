


<!-- TOC -->

- [1. Java基本数据类型](#1-java基本数据类型)
    - [1.1. 基本数据类型和其包装类](#11-基本数据类型和其包装类)
        - [1.1.1. 8种基本数据类型](#111-8种基本数据类型)
        - [1.1.2. ~~基本数据类型和其包装类~~](#112-基本数据类型和其包装类)
        - [1.1.3. 自动装箱和拆箱](#113-自动装箱和拆箱)
        - [1.1.4. 装箱和拆箱是如何实现的](#114-装箱和拆箱是如何实现的)
    - [1.2. ★★★常量池](#12-★★★常量池)
    - [==和equals()](#和equals)
    - [1.3. Long类型返回精度丢失](#13-long类型返回精度丢失)

<!-- /TOC -->



&emsp; **<font color = "red">总结：</font>**  
1. 基本数据类型和其包装类  
    1. 基本数据类型
    2. 包装类
    3. 自动装箱和拆箱 
2. 常量池
    ```java
    public static void main(String[] args) {

        Integer i1 = 33; // 在缓存
        Integer i2 = 33;// 在缓存
        System.out.println(i1 == i2);// 输出 true
        Integer i11 = 333;// 不在缓存
        Integer i22 = 333;// 不在缓存
        System.out.println(i11 == i22);// todo 输出 false

        Integer i3 = new Integer(33);
        System.out.println(i1 == i3); // todo 输出 false，i3新建了对象
        System.out.println(i1.equals(i3));
    }
    ```

    &emsp; 包装类的对象池(也有称常量池)和JVM的静态/运行时常量池没有任何关系。静态/运行时常量池有点类似于符号表的概念，与对象池相差甚远。  
    &emsp; 包装类的对象池是池化技术的应用，并非是虚拟机层面的东西，而是 Java 在类封装里实现的。

3. ==和equals()
    ```java
    static  Integer c = 3;
    public static void main(String[] args) {

        Integer a = 3;
        Integer b = 3;

        System.out.println(a == 3);
        System.out.println(a ==b);       // todo 结果true
        System.out.println(a.equals(b)); // todo 结果true

        System.out.println(c==3);  // todo 结果true
        System.out.println(a==c);  // todo 结果true
        System.out.println(a.equals(c));  // todo 结果true
    }
    ```
    1. 基本型和基本型封装型进行“==”运算符的比较，基本型封装型将会自动拆箱变为基本型后再进行比较。  
    2. 两个Integer类型进行“==”比较，如果其值在-128至127，那么返回true，否则返回false, 这跟Integer.valueOf()的缓冲对象有关。  
    3. 两个基本型的封装型进行equals()比较，首先equals()会比较类型，如果类型相同，则继续比较值，如果值也相同，返回true。  
    4. 基本型封装类型调用equals(),但是参数是基本类型，这时候，先会进行自动装箱，基本型转换为其封装类型，再进行比较。 


# 1. Java基本数据类型  
&emsp; Java数据类型分为基本数据类型和引用数据类型。除了8种基本类型外，全是引用类型。引用数据类型分3种：类（包含集合类、8种基本数据类型的封装类），接口，数组（数组一旦实例化，它的长度就固定了）。  

## 1.1. 基本数据类型和其包装类
### 1.1.1. 8种基本数据类型  
<!-- float和double的区别 https://www.runoob.com/w3cnote/float-and-double-different.html -->
<!-- 
单精度
https://baike.baidu.com/item/%E5%8D%95%E7%B2%BE%E5%BA%A6 

 【265期】面试官：为什么Integer用==比较时127相等而128不相等？ 
 https://mp.weixin.qq.com/s/fBaYxLnR9gOoQUQpUrsMBA
-->

|数据类型|字节|位数|默认值|取值范围|
|---|---|---|---|---|
|byte	|1	|8|0	|-128-127|
|short短整型数据类	|2	|16|0	|-32768-32767|
|int	|4	|32|0	| -2^31 ~ 2^31-1 (-2147483648-2147483647)|
|long	|8	|64|0| |	
|float	|4	|32|0.0f| |	
|double	|8	|64|0.0d| |	
|char	|2	|16|'\u0000'| |	
|boolean	|4|32	|false	| |

1. 4种整数类型  
&emsp; byte：占用内存1字节，8位。其中1位符号位，7位数据位，所以范围-128 \~ 127。  
&emsp; short：占用内存2字节，16位。  
&emsp; int：占用内存4字节，32位。  
&emsp; long：占用内存8字节，64位。  
2. ~~2种浮点数类型~~   
&emsp; 存储在内存的浮点数都包括3部分 : 符号位 指数位 尾数位(小数部分)。而float和double的区别就是各部分占的位数可能不同。  
&emsp; float：单精度类型。占用内存4字节，32位。1位符号位，8位指数位，23位尾数位。  
&emsp; double：双精度类型占用内存8字节，64位。1位符号位，11位指数位，64位尾数位。   
&emsp; 一般来说，CPU处理单精度浮点数的速度比处理双精度浮点数快。  
3. 1种字符类型：char  
4. 1种布尔类型：boolean  
&emsp; 对于boolean，官方文档未明确定义，它依赖于JVM厂商的具体实现。逻辑上理解是占有1位，但是实际中会考虑计算机高效存储因素。在Java虚拟机中没有任何供boolean值专用的字节码指令，Java语言表达式所操作的boolean值，在编译之后都使用Java虚拟机中的int数据类型来代替，而boolean数组将会被编码成Java虚拟机的byte数组，每个元素boolean元素占8位。这样可以得出boolean类型占了单独使用是4个字节，在数组中又是1个字节。使用int的原因是，对于当下32位的处理器(CPU)来说，一次处理数据是32位(这里不是指的是32/64位系统，而是指CPU硬件层面)，具有高效存取的特点。  

&emsp; 类型转换：  
1. 自动转换：byte-->short-->int-->long-->float-->double  
2. 强制转换：①会损失精度，产生误差，小数点以后的数字全部舍弃。②容易超过取值范围。  


&emsp; 基本类型所占的存储空间是不变的。这种不变性也是Java具有可移植性的原因之一。  
&emsp; **<font color = "clime">基本类型放在栈中，直接存储值。</font>**  

### 1.1.2. ~~基本数据类型和其包装类~~  
&emsp; **<font color = "red">这八种基本类型都有对应的包装类，分别为：Byte、Short、Integer、Long、Float、Double、Character、Boolean。</font>**  

&emsp; **基本数据类型和其包装类的区别：**   
1. 在类中，一般使用基本数据包装类。方法中，一般使用基本数据类型。   
2. 基本数据类型计算的效率高，可以使用“==”比较大小；  
3. 包装类型的初始值为null，用方法equals比较大小。

### 1.1.3. 自动装箱和拆箱  
<!-- 
https://www.cnblogs.com/dolphin0520/p/3780005.html
!!!!!!!!!!!!!!!!
https://www.cnblogs.com/dolphin0520/p/3780005.html
-->

&emsp; 在Java SE5之前，如果要生成一个数值为10的Integer对象，必须这样进行：

```java
Integer i = new Integer(10);
```
&emsp; 而在从Java SE5开始就<font color = "red">提供了自动装箱的特性，如果要生成一个数值为10的Integer对象，只需要这样就可以了：</font>  

```java
Integer i = 10;
```
&emsp; 这个过程中会自动根据数值创建对应的 Integer对象，这就是装箱。  
  
&emsp; 那什么是拆箱呢？顾名思义，跟装箱对应，就是自动将包装器类型转换为基本数据类型：  

```java
Integer i = 10;  //装箱
int n = i;   //拆箱
```
&emsp; 简单一点说，<font color = "red">装箱就是 自动将基本数据类型转换为包装器类型；拆箱就是  自动将包装器类型转换为基本数据类型。</font>  

### 1.1.4. 装箱和拆箱是如何实现的  

&emsp; 以Interger类为例，示例代码：  

```java
public class Main {
    public static void main(String[] args) {
         
        Integer i = 10;
        int n = i;
    }
}
```
&emsp; 反编译class文件之后得到如下内容：  
![image](http://182.92.69.8:8081/img/java/JDK/basics/java-6.png)  

&emsp; 从反编译得到的字节码内容可以看出，在装箱的时候自动调用的是Integer的valueOf(int)方法。而在拆箱的时候自动调用的是Integer的intValue方法。  
&emsp; 其他的也类似，比如Double、Character。  

&emsp; 一句话总结装箱和拆箱的实现过程：  
&emsp; <font color = "clime">装箱过程是通过调用包装器的valueOf方法实现的，而拆箱过程是通过调用包装器的 xxxValue方法实现的。(xxx代表对应的基本数据类型)。</font>  


## 1.2. ★★★常量池  
<!-- 
基本数据类型对常量池的使用
https://blog.csdn.net/jitianyu123/article/details/73555198
-->

```java
public static void main(String[] args) {

    Integer i1 = 33; // 在缓存
    Integer i2 = 33;// 在缓存
    System.out.println(i1 == i2);// 输出 true
    Integer i11 = 333;// 不在缓存
    Integer i22 = 333;// 不在缓存
    System.out.println(i11 == i22);// todo 输出 false

    Integer i3 = new Integer(33);
    System.out.println(i1 == i3); // todo 输出 false，i3新建了对象
    System.out.println(i1.equals(i3));
}
```

&emsp; <font color = "clime">Java基本类型的包装类的大部分都实现了常量池技术，</font><font color = "red">即Byte、Short、Integer、Long、Character、Boolean，前面4种包装类默认创建了数值[-128,127]的相应类型的缓存数据，Character创建了数值在[0,127]范围的缓存数据，Boolean直接返回True Or False。如果超出对应范围仍然会去创建新的对象。为什么把缓存设置为[-128,127]区间？是性能和资源之间的权衡。</font>  
&emsp; 两种浮点数类型的包装类Float、Double并没有实现常量池技术。  

-------

&emsp; 包装类的对象池(也有称常量池)和JVM的静态/运行时常量池没有任何关系。静态/运行时常量池有点类似于符号表的概念，与对象池相差甚远。  
&emsp; 包装类的对象池是池化技术的应用，并非是虚拟机层面的东西，而是 Java 在类封装里实现的。打开 Integer 的源代码，找到 cache 相关的内容：  

```java
/**
    * Cache to support the object identity semantics of autoboxing for values between
    * -128 and 127 (inclusive) as required by JLS.
    * <p>
    * The cache is initialized on first usage. The size of the cache
    * may be controlled by the {@code -XX:AutoBoxCacheMax=<size>} option.
    * During VM initialization, java.lang.Integer.IntegerCache.high property
    * may be set and saved in the private system properties in the
    * sun.misc.VM class.
    */

private static class IntegerCache {
    static final int low = -128;
    static final int high;
    static final Integer cache[];

    static {
// high value may be configured by property
        int h = 127;
        String integerCacheHighPropValue =
                sun.misc.VM.getSavedProperty("java.lang.Integer.IntegerCache.high");
        if (integerCacheHighPropValue != null) {
            try {
                int i = parseInt(integerCacheHighPropValue);
                i = Math.max(i, 127);
// Maximum array size is Integer.MAX_VALUE
                h = Math.min(i, Integer.MAX_VALUE - (-low) - 1);
            } catch (NumberFormatException nfe) {
// If the property cannot be parsed into an int, ignore it.
            }
        }
        high = h;

        cache = new Integer[(high - low) + 1];
        int j = low;
        for (int k = 0; k < cache.length; k++)
            cache[k] = new Integer(j++);

// range [-128, 127] must be interned (JLS7 5.1.7)
        assert IntegerCache.high >= 127;
    }

    private IntegerCache() {
    }
}

/**
    * Returns an {@code Integer} instance representing the specified
    * {@code int} value. If a new {@code Integer} instance is not
    * required, this method should generally be used in preference to
    * the constructor {@link #Integer(int)}, as this method is likely
    * to yield significantly better space and time performance by
    * caching frequently requested values.
    * <p>
    * This method will always cache values in the range -128 to 127,
    * inclusive, and may cache other values outside of this range.
    *
    * @param i an {@code int} value.
    * @return an {@code Integer} instance representing {@code i}.
    * @since 1.5
    */
public static Integer valueOf(int i) {
    if (i >= IntegerCache.low && i <= IntegerCache.high)
        return IntegerCache.cache[i + (-IntegerCache.low)];
    return new Integer(i);
}
```

&emsp; IntegerCache 是 Integer 在内部维护的一个静态内部类，用于对象缓存。通过源码知道，Integer 对象池在底层实际上就是一个变量名为 cache 的数组，里面包含了 -128 ～ 127 的 Integer 对象实例。  
&emsp; 使用对象池的方法就是通过 Integer.valueOf() 返回 cache 中的对象，像 Integer i = 10 这种自动装箱实际上也是调用 Integer.valueOf() 完成的。  
&emsp; 如果使用的是 new 构造器，则会跳过 valueOf()，所以不会使用对象池中的实例。  

```java
Integer i1 = 10;
Integer i2 = 10;
Integer i3 = new Integer(10);
Integer i4 = new Integer(10);
Integer i5 = Integer.valueOf(10);
 
System.out.println(i1 == i2); // true
System.out.println(i2 == i3); // false
System.out.println(i3 == i4); // false
System.out.println(i1 == i5); // true
```

&emsp; 注意到注释中的一句话 “The cache is initialized on first usage”，缓存池的初始化在第一次使用的时候已经全部完成，这涉及到设计模式的一些应用。这和常量池中字面量的保存有很大区别，Integer 不需要显示地出现在代码中才添加到池中，初始化时它已经包含了所有需要缓存的对象。  


## ==和equals()
<!-- 

https://blog.csdn.net/tc_1337/article/details/79836825
-->

1. 基本型和基本型封装型进行“==”运算符的比较，基本型封装型将会自动拆箱变为基本型后再进行比较。  
2. 两个Integer类型进行“==”比较，如果其值在-128至127，那么返回true，否则返回false, 这跟Integer.valueOf()的缓冲对象有关。  
3. 两个基本型的封装型进行equals()比较，首先equals()会比较类型，如果类型相同，则继续比较值，如果值也相同，返回true。  
4. 基本型封装类型调用equals(),但是参数是基本类型，这时候，先会进行自动装箱，基本型转换为其封装类型，再进行比较。  



## 1.3. Long类型返回精度丢失  

```java
@JsonFormat(shape = JsonFormat.Shape.STRING)
private Long zkShopId;
```

&emsp; **<font color = "clime">在字段上面添加：@JsonFormat(shape = JsonFormat.Shape.STRING)</font>**  

