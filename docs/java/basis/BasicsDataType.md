


<!-- TOC -->

- [1. Java基本数据类型](#1-java基本数据类型)
    - [1.1. 8种基本数据类型](#11-8种基本数据类型)
    - [1.2. ~~基本数据类型和其包装类~~](#12-基本数据类型和其包装类)
        - [1.2.1. 自动装箱和拆箱](#121-自动装箱和拆箱)
        - [1.2.2. 装箱和拆箱是如何实现的](#122-装箱和拆箱是如何实现的)
    - [1.3. ★★★常量池](#13-★★★常量池)
    - [Long类型返回精度丢失](#long类型返回精度丢失)

<!-- /TOC -->



&emsp; **<font color = "red">总结：</font>**  



# 1. Java基本数据类型  
&emsp; Java数据类型分为基本数据类型和引用数据类型。除了8种基本类型外，全是引用类型。引用数据类型分3种：类(包含集合类、8种基本数据类型的封装类)，接口，数组(数组一旦实例化，它的长度就固定了)。  

## 1.1. 8种基本数据类型  
<!-- float和double的区别 https://www.runoob.com/w3cnote/float-and-double-different.html -->
<!-- 
单精度
https://baike.baidu.com/item/%E5%8D%95%E7%B2%BE%E5%BA%A6 -->



|数据类型|字节|位数|默认值|取值范围|
|---|---|---|---|---|
|byte	|1	|8|0	|-128-127|
|short	|2	|16|0	|-32768-32767|
|int	|4	|32|0	|-2147483648-2147483647|
|long	|8	|64|0| |	
|float	|4	|32|0.0f| |	
|double	|8	|64|0.0d| |	
|char	|2	|16|'\u0000'| |	
|boolean	|4|32	|false	| |

1. 4种整数类型  
&emsp; byte：占用内存1字节，8位。其中1位符号位，7位数据位，所以范围-128 \~ 127  
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

## 1.2. ~~基本数据类型和其包装类~~  
&emsp; **<font color = "red">这八种基本类型都有对应的包装类，分别为：Byte、Short、Integer、Long、Float、Double、Character、Boolean。</font>**  

&emsp; **基本数据类型和其包装类的区别：**   
1. 在类中，一般使用基本数据包装类。方法中，一般使用基本数据类型。   
2. 基本数据类型计算的效率高，可以使用“==”比较大小；  
3. 包装类型的初始值为null，用方法equals比较大小。

### 1.2.1. 自动装箱和拆箱  
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

### 1.2.2. 装箱和拆箱是如何实现的  

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
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JDK/basics/java-6.png)  

&emsp; 从反编译得到的字节码内容可以看出，在装箱的时候自动调用的是Integer的valueOf(int)方法。而在拆箱的时候自动调用的是Integer的intValue方法。  
&emsp; 其他的也类似，比如Double、Character。  

&emsp; 一句话总结装箱和拆箱的实现过程：  
&emsp; <font color = "clime">装箱过程是通过调用包装器的valueOf方法实现的，而拆箱过程是通过调用包装器的 xxxValue方法实现的。(xxx代表对应的基本数据类型)。</font>  


## 1.3. ★★★常量池  
<!-- 
基本数据类型对常量池的使用
https://blog.csdn.net/jitianyu123/article/details/73555198
-->
&emsp; <font color = "clime">Java基本类型的包装类的大部分都实现了常量池技术，</font><font color = "red">即Byte、Short、Integer、Long、Character、Boolean，前面4种包装类默认创建了数值[-128,127]的相应类型的缓存数据，Character创建了数值在[0,127]范围的缓存数据，Boolean直接返回True Or False。如果超出对应范围仍然会去创建新的对象。为什么把缓存设置为[-128,127]区间？是性能和资源之间的权衡。</font>  
&emsp; 两种浮点数类型的包装类Float、Double并没有实现常量池技术。  


## Long类型返回精度丢失  

```java
@JsonFormat(shape = JsonFormat.Shape.STRING)
private Long zkShopId;
```

&emsp; **<font color = "clime">在字段上面添加：@JsonFormat(shape = JsonFormat.Shape.STRING)</font>**  

