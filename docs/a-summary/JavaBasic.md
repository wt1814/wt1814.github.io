
<!-- TOC -->

- [1. Java](#1-java)
    - [1.1. Java基础](#11-java基础)
    - [1.2. Java基础数据类型](#12-java基础数据类型)
        - [1.2.1. Object](#121-object)
        - [1.2.2. String](#122-string)
        - [1.2.3. Java基本数据类型](#123-java基本数据类型)
    - [1.3. Java集合框架](#13-java集合框架)
        - [1.3.1. Java集合框架](#131-java集合框架)
        - [1.3.2. HashMap](#132-hashmap)
            - [1.3.2.1. HashMap源码](#1321-hashmap源码)
            - [1.3.2.2. HashMap安全](#1322-hashmap安全)
        - [1.3.3. Collection](#133-collection)
    - [1.4. JDK1.8](#14-jdk18)
        - [1.4.1. 接口的默认方法与静态方法](#141-接口的默认方法与静态方法)
        - [1.4.2. Lambda表达式](#142-lambda表达式)
        - [1.4.3. Stream](#143-stream)
        - [1.4.4. Optional](#144-optional)
    - [1.5. Java异常](#15-java异常)
    - [1.6. Java范型](#16-java范型)
    - [1.7. IO](#17-io)
    - [1.8. SPI与线程上下文类加载器](#18-spi与线程上下文类加载器)
    - [1.9. 反射](#19-反射)
    - [1.10. 自定义注解](#110-自定义注解)

<!-- /TOC -->

# 1. Java  
## 1.1. Java基础
1. static关键字：  
    &emsp; **Java中static块执行时机：<font color = "red">static块的执行发生在“初始化”的阶段。</font>类被加载了不一定就会执行静态代码块，只有一个类被主动使用的时候，静态代码才会被执行！**   
    1. 方便在没有创建对象的情况下来进行调用（方法/变量）。  
    2. static使用： 1). static修饰变量、 2). 修饰方法、 3). static 可以修饰代码块，主要分为两种，一种直接定义在类中，使用static{}，这种被称为静态代码块，一种是在类中定义静态内部类，使用static class xxx来进行定义、 4). static可以和单例模式一起使用，通过双重检查锁来实现线程安全的单例模式、 5).静态导包。     
    3. 静态方法可以调用成员变量吗？ `注⚠️：static静态方法引用类变量，变量需要static修饰。`  
    4. static与JVM： 
        1. 类的加载流程中，准备阶段、初始化阶段。  
        2. static作为类变量，只被加载一次。  
        3. static变量被存放在方法区中。    
2. Java方法是值传递还是引用传递？   
&emsp; 传入的参数是testDemo2【对象地址值的一个拷贝】，但是形参和实参的值都是一样的，都指向同一个对象，所以对象内容的改变会影响到实参。  
&emsp; 结论：JAVA的参数传递确实是值传递，不管是基本参数类型还是引用类型，形参值的改变都不会影响实参的值。如果是引用类型，形参值所对应的对象内部值的改变会影响到实参。  

## 1.2. Java基础数据类型
### 1.2.1. Object  
1. ==和equals()    
2. 为什么equals方法重写，建议也一起重写hashcode方法？   
&emsp; **<font color = "blue">在某些业务场景下，需要使用自定义类作为哈希表的键。用HashMap存入自定义的类时，如果不重写这个自定义类的equals和hashCode方法，得到的结果会和预期的不一样。</font>**  
3. notify()/notifyAll()/wait()   


### 1.2.2. String
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


### 1.2.3. Java基本数据类型
1. 基本数据类型
    
|数据类型|字节|位数|默认值|取值范围|
|---|---|---|---|---|
|byte	|1	|8|0	|-128-127|
|short	|2	|16|0	|-32768-32767|
|int	|4	|32|0	| -2^31 ~ 2^31-1 (-2147483648-2147483647)|
|long	|8	|64|0| |	
|float	|4	|32|0.0f| |	
|double	|8	|64|0.0d| |	
|char	|2	|16|'\u0000'| |	
|boolean	|4|32	|false	| |

2. 包装类  
    &emsp; char的包装类型是Character。  
3. 自动装箱和拆箱 
4. 常量池
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

5. ==和equals()
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
    3. `两个基本型的【封装型】进行equals()比较，首先equals()会比较类型，如果类型相同，则继续比较值，如果值也相同，返回true。` 代码中，a.equals(b)
    4. 基本型封装类型调用equals(),但是参数是基本类型，这时候，先会进行自动装箱，基本型转换为其封装类型，再进行比较。 

## 1.3. Java集合框架
### 1.3.1. Java集合框架
2. Java集合框架：  
    * List：有序，可重复。List有ArrayList、LinkedList、Vector。
    * Set：无序，不可重复（唯一）。Set有HashSet、LinkedHashSet、TreeSet。
    * Map：存储键值对。Map有HashMap、LinkedHashMap、TreeMap、HashTable。     
3. 快速失败机制：单线程迭代器中直接删除元素或多线程使用非安全的容器都会抛出ConcurrentModificationException异常。  
&emsp; **<font color = "clime">采用安全失败(fail-safe)机制的集合容器，在遍历时不是直接在集合内容上访问的，而是先复制原有集合内容，再在拷贝的集合上进行遍历。</font>**  
4. 排序：  
    * Comparable，自然排序（自身属性，整数按照大小排序，字符串按照字典序）。  
    * Comparator，定制排序。  

### 1.3.2. HashMap
#### 1.3.2.1. HashMap源码  
1. 小结：属性  ---  成员方法  
![image](http://182.92.69.8:8081/img/draw/1.HashMap.png)  
1. HashMap属性：  
    1. Hash表数据结构：  
        1. Hash数据结构  
            &emsp; 哈希数据结构的性能取决于三个因素：哈希函数、处理冲突方法、哈希因子。  
            1. 常用Hash函数有：直接寻址法、数字分析法、平方取中法....  
            2. 常用的哈希冲突解决方法有两类，开放寻址法和拉链法。  
            &emsp; 处理冲突的方法决定了哈希表的数据结构。采用开放地址法，哈希表的数据结构是一维数组；采用链地址法，哈希表的数据结构是数组加链表。    
        2. HashMap的Hash结构              
        &emsp; 哈希函数见下面章节。初始容量为16；  
        &emsp; HashMap在发生hash冲突的时候用的是链地址法。JDK1.7中使用头插法，JDK1.8使用尾插法。  
        &emsp; loadFactor哈希因子0.75f；  
    2. 树形化结构：  
        1. ~~为什么使用[红黑树](/docs/function/redBlack.md)？~~  
        &emsp; 这样可以利用链表对内存的使用率以及红黑树的高效检索，是一种很有效率的数据结构。AVL树是一种高度平衡的二叉树，所以查找的非常高，但是，有利就有弊，AVL树为了维持这种高度的平衡，就要付出更多代价。每次插入、删除都要做调整，复杂、耗时。所以，hashmap用红黑树。  
        2. 树形化结构：
            1. 树形化：把链表转换成红黑树，树化需要满足以下两个条件：链表长度大于等于8；table数组长度大于等于64。  
            2. 解除树形化：阈值6。
2. HashMap成员方法：  
    1. hash()函数/扰动函数：3个步骤  
    &emsp; hash函数会根据传递的key值进行计算， 1)首先`计算`key的hashCode值， 2)然后再对hashcode进行`无符号右移`操作， 3)最后再和hashCode进行`异或 ^` 操作。（即让hashcode的高16位和低16位进行异或操作。）   
    &emsp; **<font color = "clime">`看似“多余”的2、3步`的好处是`增加了随机性`，`减少了碰撞冲突`的可能性。</font>**    
    2. put()函数：
        1. 在put的时候，首先对key做hash运算，计算出该key所在的index。
        2. 如果没碰撞，直接放到数组中；
        3. 如果碰撞了，如果key是相同的，则替掉到原来的值；
        4. 如果key不同，需要判断目前数据结构是链表还是红黑树，根据不同的情况来进行插入。
        5. 最后判断哈希表是否满了(当前哈希表大小*负载因子)，如果满了，则【扩容】。  
    3. 扩容  
        1. 先生成新数组
        2. 遍历老数组中的每个位置上的链表或红黑树
        3. 如果是链表，则直接将链表中的每个元素重新计算下标，并添加到新数组中去
        4. 如果是红黑树，则先遍历红黑树，先计算出红黑树中每个元素对应在新数组中的下标位置  
        &emsp; a.统计每个下标位置的元素个数  
        &emsp; b.如果该位置下的元素个数超过了8，则生成一个新的红黑树，并将根节点的添加到新数组的对应位置  
        &emsp; c.如果该位置下的元素个数没有超过8，那么则生成一个链表，并将链表的头节点添加到新数组的对应位置  
        5. 所有元素转移完了之后，将新数组赋值给HashMap对象的table属性

        &emsp; table容量变为2倍，但是不需要像之前一样计算下标，只需要将hash值和旧数组长度相与即可确定位置。

        1. 如果 Node 桶的数据结构是链表会生成 low 和 high 两条链表，是红黑树则生成 low 和 high 两颗红黑树
        2. 依靠 (hash & oldCap) == 0 判断 Node 中的每个结点归属于 low 还是 high。
        3. 把 low 插入到 新数组中 当前数组下标的位置，把 high 链表插入到 新数组中 [当前数组下标 + 旧数组长度] 的位置
        4. 如果生成的 low，high 树中元素个数小于等于6退化成链表再插入到新数组的相应下标的位置
    3. ~~put()中，最后流程`扩容`机制：JDK 1.8扩容条件是数组长度大于阈值或链表转为红黑树且数组元素小于64时。~~（重新总结）  
        https://blog.csdn.net/qq_49217297/article/details/126304736  
        https://blog.csdn.net/Gangangan_/article/details/130462623  
        https://blog.csdn.net/upstream480/article/details/120251036  
        ![image](http://182.92.69.8:8081/img/java/JDK/Collection/collection-15.png)
        1. 遍历节点 ---> 计算节点位置(e.hash & oldCap)  
        2. 节点迁移  
            * 单节点迁移。  
            * 如果节点是红黑树类型的话则需要进行红黑树的拆分：`拆分成高低位链表，如果链表长度大于6，需要把链表升级成红黑树。`
            * 对链表进行迁移。会对链表中的节点进行分组，进行迁移后，一类的节点位置在原索引，一类在原索引+旧数组长度。 ~~通过 hash & oldCap(原数组大小)的值来判断，若为0则索引位置不变，不为0则新索引=原索引+旧数组长度~~  
            &emsp; <font color = "clime">对链表进行迁移的注意点：</font>JDK1.8HashMap扩容阶段重新映射元素时不需要像1.7版本那样重新去一个个计算元素的 hash 值，<font color = "clime">而是通过 hash & oldCap（原数组大小）的值来判断，若为0则索引位置不变，不为0则新索引=原索引+旧数组长度，</font>为什么呢？具体原因如下：  
            &emsp; 因为使用的是2次幂的扩展(指长度扩为原来2倍)，所以，元素的位置要么是在原位置，要么是在原位置再移动2次幂的位置。因此，在扩充HashMap的时候，不需要像 JDK1.7 的实现那样重新计算 hash，只需要看看原来的 hash 值新增的那个 bit 是 1 还是 0 就好了，是 0 的话索引没变，是 1 的话索引变成“原索引 +oldCap。  
            &emsp; 这点其实也可以看做长度为 2 的幂次方的一个好处，也是 HashMap 1.7 和 1.8 之间的一个区别。  
            &emsp; 示例：扩容前 table 的容量为16，a 节点和 b 节点在扩容前处于同一索引位置。  
            ![image](http://182.92.69.8:8081/img/java/JDK/Collection/collection-19.png)  
            &emsp; 扩容后，table 长度为32，新表的 n - 1 只比老表的 n - 1 在高位多了一个1（图中标红）。  
            ![image](http://182.92.69.8:8081/img/java/JDK/Collection/collection-20.png)  
            &emsp; 因为 2 个节点在老表是同一个索引位置，因此计算新表的索引位置时，只取决于新表在高位多出来的这一位(图中标红)，而这一位的值刚好等于oldCap。  
            &emsp; 因为只取决于这一位，所以只会存在两种情况：1)  (e.hash & oldCap) == 0 ，则新表索引位置为“原索引位置” ；2)(e.hash & oldCap) == 1，则新表索引位置为“原索引 + oldCap 位置”。  

#### 1.3.2.2. HashMap安全
1. `3类错误`：HashMap的线程不安全体现在会造成死循环、数据丢失、数据覆盖这些问题。其中死循环和数据丢失是在JDK1.7中出现的问题，在JDK1.8中已经得到解决，然而1.8中仍会有数据覆盖这样的问题。  
2. jdk1.7，HashMap导致CPU100% 是因为【HashMap死循环】导致的。  
&emsp; 导致死循环的根本原因是JDK 1.7扩容采用的是“头插法”，会导致同一索引位置的节点在扩容后顺序反掉。而JDK 1.8之后采用的是“尾插法”，扩容后节点顺序不会反掉，不存在死循环问题。  
&emsp; 导致死循环示例：线程1、2同时扩容。线程1指向节点和下一节点，线程挂起。线程2完成扩容，此时线程1唤醒。线程1继续完成头节点插入，形成闭环。    
&emsp; 发生死循环后，剩余元素无法搬运，并且线程不会停止，因此会造成CPU100%。  
3. jdk1.8，在多线程环境下，会发生【HashMap数据覆盖】的情况。  
&emsp; 假设两个线程A、B都在进行put操作，并且hash函数计算出的插入下标是相同的，当线程A执行完第六行代码后由于时间片耗尽导致被挂起，而线程B得到时间片后在该下标处插入了元素，完成了正常的插入，`然后线程A获得时间片，由于之前已经进行了hash碰撞的判断，所以此时不会再进行判断，而是直接进行插入，这就导致了线程B插入的数据被线程A覆盖了，从而线程不安全。`  
3. `线程安全的HashMap`：Hashtable、Collections.synchronizedMap、[ConcurrentHashMap](/docs/java/concurrent/ConcurrentHashMap.md)。  

### 1.3.3. Collection
1. HashSet基于HashMap实现： **<font color = "clime">存储在HashSet中的数据作为Map的key，而Map的value统一为PRESENT。</font>**  
    &emsp; 添加元素，如何保证值不重复？  
    &emsp; HashSet#add通过 map.put() 方法来添加元素。HashSet的add(E e)方法，会将e作为key，PRESENT作为value插入到map集合中。  
    * 如果e（新插入的key）存在，HashMap#put返回原key对应的value值（注意新插入的value会覆盖原value值），Hashset#add返回false，表示插入值重复，插入失败。  
    * 如果e（新插入的key）不存在，HashMap#put返回null值，Hashset#add返回true，表示插入值不重复，插入成功。  

## 1.4. JDK1.8
### 1.4.1. 接口的默认方法与静态方法
1. 接口的默认方法与静态方法  
    * <font color = "clime">接口中的default方法会被子接口继承，也可以被其实现类所调用。default方法被继承时，可以被子接口覆写。</font>  
    * <font color = "clime">接口中的`static方法`不能被继承，也不能被实现类调用，`只能被自身调用`。即不能通过接口实现类的方法调用静态方法，直接通过接口名称调用。但是静态变量会被继承。</font>  

### 1.4.2. Lambda表达式
1. **<font color = "clime">函数式接口的实例创建三种方式：lambda表达式；方法引用；构造方法引用。</font>**   
2. Lambda表达式作用域，访问外层作用域定义的局部变量、类的属性：  
    * <font color = "clime">访问局部变量：lambda表达式若访问了局部变量，则局部变量必须是final的。若局部变量没有加final关键字，系统会自动添加，此后再修改该局部变量，会编译错误。</font>  
    * <font color = "clime">访问类的属性：lambda内部使用this关键字（或不使用）访问或修改全局变量、实例方法。</font>    

### 1.4.3. Stream
&emsp; **<font color = "clime">使用并行流parallelStream()有线程安全问题。例如：parallelStream().forEach()内部修改集合会有问题。解决方案：1.使用锁； 2.使用collect和reduce操作(Collections框架提供了同步的包装)。</font>**  

### 1.4.4. Optional
&emsp; 使用Optional时尽量不直接调用Optional.get()方法，Optional.isPresent()更应该被视为一个私有方法，应依赖于其他像Optional.orElse()，Optional.orElseGet()，Optional.map()等这样的方法。  

&emsp; 抛出异常可以使用：  

```java
//todo Optional.ofNullable(storeInfo) 创建对象
Optional.ofNullable(storeInfo).orElseThrow(()->new Exception("失败"));  
```

## 1.5. Java异常
1. throws和throw：  
    * throws用在`函数上`，后面跟的是`异常类`，可以跟多个；
    * throw用在`函数内`，后面跟的是`异常对象`。  
2. 异常捕获后再次抛出。
    * 捕获后抛出原来的异常，希望保留最新的异常抛出点。 
    * 捕获后抛出新的异常，希望抛出完整的异常链。  
3. 自定义异常
4. 统一异常处理

## 1.6. Java范型
1. 为什么使用范型？范型的优点：编译期类型检查。  
&emsp; 在Java SE 1.5之前，没有泛型的情况的下，通过对类型Object的引用来实现参数的“任意化”，“任意化”带来的缺点是要做显式的强制类型转换，而这种转换是要求开发者对实际参数类型可以预知的情况下进行的。对于强制类型转换错误的情况，编译器可能不提示错误，在运行的时候才出现异常，这是一个安全隐患。  
&emsp; 泛型和Object除了语法之外没有什么别的区别，不过为什么jdk1.5以后要出这个泛型；  
&emsp; `使用泛型最大的好处：不再需要强制类型转换，编译时自动检查类型安全，避免隐性的类型转换异常。`  
2. 范型擦除
    1. 范型擦除：Java会在编译Class文件时，将范型擦除成原始类型Object。  
    2. 运行时，如何获取范型信息？  
    3. 利用反射越过泛型检查  
    &emsp; 反射是获取类Class文件进行操作。通过反射获取对象后可以获得相应的add方法，并向方法里面传入任何对象。  

## 1.7. IO
1. **<font color = "clime">将大文件数据全部读取到内存中，可能会发生OOM异常。</font>** I/O读写大文件解决方案：  
    * 使用BufferedInputStream进行包装。
    * 逐行读取。
    * 并发读取：1)逐行批次打包；2)大文件拆分成小文件。
    * 零拷贝方案：
        * FileChannel，分配读取到已分配固定长度的 java.nio.ByteBuffer。
        * 内存文件映射，MappedByteBuffer。采用内存文件映射不能读取超过2GB的文件。文件超过2GB，会报异常。

## 1.8. SPI与线程上下文类加载器
&emsp; SPI，service provider interface，服务提供者接口，一种扩展机制。`相比面向接口的多态，实现动态编译。面向接口的多态，加载的实体类是在编码中，而SPI是写在配置文件中。`    
&emsp; **<font color = "clime">JDK提供的SPI机制：</font>**  
1. 提供一个接口；  
2. 服务提供方实现接口，并在META-INF/services/中暴露实现类地址；  
3. 服务调用方依赖接口，使用java.util.ServiceLoader类调用。  

&emsp; 这个是针对厂商或者插件的，第三方提供服务的功能。  

--------

&emsp; `JDK的SPI内部使用线程上下文类加载器Thread.currentThread().getContextClassLoader()实现，破坏了双亲委派模型，是为了适用所有场景。`ServiceLoader中的load方法：  

```java
public static <S> ServiceLoader<S> load(Class<S> service) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return ServiceLoader.load(service, cl);
}
```

&emsp; `Dubbo的SPI并没有破坏双亲委派模型。`自己实现的框架，接口类和实现类一般都是由SystemClassLoader加载器来加载的，这时候双亲委派模型仍然可以正常使用。很多框架使用SPI方式的原因，不是因为双亲委派模型满足不了类加载需求，而是看重了SPI的易扩展性。  


-----

&emsp; 个人的简单理解：  
&emsp; SPI（Service Provider Interface），服务提供发现接口。热插拔、动态替换。  
&emsp; `SPI与多态比较：`  
&emsp; 多态，一个接口在一个包中有多个实现；  
&emsp; 而SPI提供的接口的实现一般在多个包中，例如JDBC的实现mysql、oracle，web容器有tomcat、jetty等。  
&emsp; 一个接口在b、c包中有实现，在a包中可替换所依赖的包（b或c），动态实现某一个功能。  

## 1.9. 反射
1. 什么是反射？  
	&emsp; 反射是在`运行状态`能够动态的获取该类的属性和方法，并且能够任意的使用该类的属性和方法，这种动态获取类信息以及动态的调用对象的方法的功能就是反射。  
2. 反射的适用场景？ **<font color = "clime">平常开发涉及的框架中使用反射的有：动态代理、JDBC中的加载数据库驱动程序、Spring框架中加载bean对象。</font>**    
	1. 情景一，不得已而为之
	2. 情景二，动态加载（可以最大限度的体现Java的灵活性，并降低类的耦合性：多态）
	3. 情景三：避免将程序写死到代码里  
	4. `开发通用框架 - 反射最重要的用途就是开发各种通用框架。`很多框架（比如 Spring）都是配置化的（比如通过 XML 文件配置 JavaBean、Filter 等），为了保证框架的通用性，它们`可能需要根据配置文件加载不同的对象或类，调用不同的方法，这个时候就必须用到反射——运行时动态加载需要加载的对象。`  
3. 反射的优缺点？  
	* 优点：  
		1）能够运行时动态获取类的实例，提高灵活性；
		2）与动态编译结合
	* 缺点：  
		1）使用反射性能较低，`需要解析字节码，将内存中的对象进行解析`。
			解决方案：
			1、通过setAccessible(true)关闭JDK的安全检查来提升反射速度；
			2、多次创建一个类的实例时，有缓存会快很多
			3、ReflflectASM工具类，通过字节码生成的方式加快反射速度
			4、尽量不要getMethods()后再遍历筛选，而直接用getMethod(methodName)来根据方法名获取方法。
		2）`相对不安全，破坏了封装性`（因为通过反射可以获得私有方法和属性）  
    * 优点：运行期类型的判断，动态加载类，提高代码灵活度。  
    * 缺点：性能瓶颈，`反射相当于一系列解释操作，通知JVM要做的事情，性能比直接的java代码要慢很多`。  
4. 反射的原理：  
    1. 调用反射的总体流程如下：  
        * 准备阶段：编译期装载所有的类，将每个类的元信息保存至Class类对象中，每一个类对应一个Class对象。  
        * 获取Class对象：调用x.class/x.getClass()/Class.forName() 获取x的Class对象clz（这些方法的底层都是native方法，是在JVM底层编写好的，涉及到了JVM底层，就先不进行探究了）。  
        * 进行实际反射操作：通过clz对象获取Field/Method/Constructor对象进行进一步操作。  
    2. 源码流程：  
        * Class.forName 
            * 通过JNI调用到C层，再将类名转换成Descriptor
            * 通过Runtime获取ClassLinker对象
            * 通过LookupClass在boot_class_path中寻找Class，找到则返回
            * 通过BootClassLoader中寻找class，找到则返回
            * 判断当前线程是否允许回调Java层函数，如果允许则开始校验描述符规则
            * 通过VMStack.getCallingClassLoader获取当前ClassLoader，接着调用ClassLoader.loadClass返回Class
            * 更新ClassLoader的ClassTable
        * Class.getDeclaredMethods 
            * 通过Class对象找到method_的值，即为方法区的地址  
            * 通过8bit的大小来分割Method的地址  

## 1.10. 自定义注解
&emsp; 自定义注解+反射实现AOP

