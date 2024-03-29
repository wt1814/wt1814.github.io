
<!-- TOC -->

- [1. StreamAPI](#1-streamapi)
    - [1.1. 简介](#11-简介)
    - [1.2. Stream流的使用详解](#12-stream流的使用详解)
        - [1.2.1. 流的构造与转换](#121-流的构造与转换)
        - [1.2.2. ~~流的操作~~](#122-流的操作)
            - [1.2.2.1. 基本数值流](#1221-基本数值流)
                - [1.2.2.1.1. 流与数值流的转换](#12211-流与数值流的转换)
                - [1.2.2.1.2. 数值流方法](#12212-数值流方法)
            - [1.2.2.2. 集合Stream](#1222-集合stream)
                - [1.2.2.2.1. Map](#12221-map)
                - [1.2.2.2.2. Reduce聚合操作](#12222-reduce聚合操作)
                - [1.2.2.2.3. Collect收集结果](#12223-collect收集结果)
                - [stream分组排序高级使用](#stream分组排序高级使用)
            - [1.2.2.3. ParallelStream](#1223-parallelstream)
                - [1.2.2.3.1. ★★★parallelStream() 线程安全](#12231-★★★parallelstream-线程安全)
    - [1.3. intellij debug 技巧:java 8 stream](#13-intellij-debug-技巧java-8-stream)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**    
&emsp; **<font color = "clime">使用并行流parallelStream()有线程安全问题。例如：parallelStream().forEach()内部修改集合会有问题。解决方案：1.使用锁； 2.使用collect和reduce操作(Collections框架提供了同步的包装)。</font>**  

# 1. StreamAPI  

<!-- 
Java8 快速实现List转map 、分组、过滤等操作 
https://mp.weixin.qq.com/s/2idMa7lOzWFy2A8JyM2vtA
天天在用Stream，那你知道如此强大的Stream的实现原理吗？ 
https://mp.weixin.qq.com/s/QtWKAIRzNAza96MhfH526w
Stream流的这些操作，你得知道，对你工作有很大帮助 
https://juejin.im/post/6888549645908181005
-->

## 1.1. 简介  
&emsp; java8中的Stream是对集合对象功能的增强，它专注于对集合对象进行各种聚合操作，或者大批量数据操作。Stream API通过Lambda表达式，极大的提高编程效率和程序可读性。同时它提供串行和并行两种模式进行汇聚操作，并发模式能够充分利用多核处理器的优势，使用fork/join并行方式来拆分任务和加速处理过程。  
&emsp; Stream和Iterator区别：Stream如同一个迭代器(Iterator)，单向，不可往复，数据只能遍历一次。而和迭代器又不同的是，Stream可以并行化操作，迭代器只能命令式地、串行化操作。Stream的并行操作依赖于Java7中引入的Fork/Join框架(JSR166y)来拆分任务和加速处理过程。  
&emsp; lambda表达式是函数式接口的实现，参数行为化；stream流的参数是函数式接口，即lambda表达式的实例。  

## 1.2. Stream流的使用详解  
### 1.2.1. 流的构造与转换  
&emsp; 有多种方式生成Stream Source：  
1. 从Collection和数组：Collection.stream()、Collection.parallelStream()、Arrays.stream(T array)、Stream.of()；  
2. 从BufferedReader：java.io.BufferedReader.lines()；  
3. 静态工厂：java.util.stream.IntStream.range()、java.nio.file.Files.walk()；  
4. 自定义构建：java.util.Spliterator；  
5. 其它：Random.ints()、BitSet.stream()、Pattern.splitAsStream(java.lang.CharSequence)、JarFile.stream()；  

&emsp; 基本数值型对应的Stream：对于基本数值型，目前有三种对应的包装类型 Stream：IntStream、LongStream、DoubleStream。  

### 1.2.2. ~~流的操作~~  

![image](http://182.92.69.8:8081/img/java/JDK/java8/java-7.png)  

&emsp; **Intermediate和Terminal联系：**  
&emsp; 在对于一个Stream进行多次转换操作(Intermediate操作)，每次都对 Stream的每个元素进行转换，而且是执行多次，这样时间复杂度就是N(转换次数)个for循环里把所有操作的总和吗？  
&emsp; 其实不是这样的，转换操作都是lazy的，多个转换操作只会在Terminal操作的时候融合起来，一次循环完成。即Stream里有个操作函数的集合，每次转换操作就是把转换函数放入这个集合中，在Terminal操作的时候循环Stream对应的集合，然后对每个元素执行所有的函数。  

#### 1.2.2.1. 基本数值流  
&emsp; 数值流IntStream, DoubleStream, LongStream，这种流中的元素都是原始数据类型，分别是int，double，long。  

##### 1.2.2.1.1. 流与数值流的转换  
* 流转换为数值流：  
    * mapToInt(T -> int) : return IntStream  
    * mapToDouble(T -> double) : return DoubleStream  
    * mapToLong(T -> long) : return LongStream  

        ```java
        IntStream intStream = list.stream().mapToInt(Person::getAge);
        ```
* 数值流转换为流：boxed  
    * Stream<Integer> stream = intStream.boxed();  

##### 1.2.2.1.2. 数值流方法  
&emsp; sum()、max()、min()、average() 等…  


#### 1.2.2.2. 集合Stream  

&emsp; Filter过滤操作、Map/flatMap变换操作、Limit/skip/concat、Sorted流的排序、Min/Max/Distinct、Match、ForEach  

##### 1.2.2.2.1. Map  
&emsp; <font color = "red">Map把一种类型的Stream变为另一种类型的Stream。</font>map方法内需要一个Function接口，Function<? super String, ? extends String> mapper。  
&emsp; 使用场景：从对象列表中提取出单个字段的列表。  

```java
public static void main(String[] args) {

    List<Staff> staff = Arrays.asList(
            new Staff("mkyong", 30, new BigDecimal(10000)),
            new Staff("jack", 27, new BigDecimal(20000)),
            new Staff("lawrence", 33, new BigDecimal(30000))
    );

    //Before Java 8
    List<String> result = new ArrayList<>();
    for (Staff x : staff) {
        result.add(x.getName());
    }
    System.out.println(result); //[mkyong, jack, lawrence]

    //Java 8
    List<String> collect = staff.stream().map(x -> x.getName()).collect(Collectors.toList());
    System.out.println(collect); //[mkyong, jack, lawrence]

}
```

##### 1.2.2.2.2. Reduce聚合操作  
&emsp; reduce()根据一定的规则将Stream中的元素进行计算后返回一个唯一的值。它提供一个起始值(种子)，然后依照运算规则(BinaryOperator)，和前面Stream的第一个、第二个、第n个元素组合。在没有起始值时，会将Stream的前面两个元素组合，返回的是Optional。字符串拼接、数值的sum、min、max、average都是特殊的reduce。  
&emsp; reduce()方法有三种形式：  

```java
//对Stream中的数据通过累加器accumulator迭代计算，最终得到一个Optional对象。
//函数接口BinaryOperator<T>继承于BiFunction<T, T, T>，接收两个参数，返回一个结果。  
Optional<T> reduce(BinaryOperator<T> accumulator);    

//给定一个初始值identity，通过累加器accumulator迭代计算，得到一个同Stream中数据同类型的结果。  
T reduce(T identity, BinaryOperator<T> accumulator); 

//给定一个初始值identity，通过累加器accumulator迭代计算，得到一个identity类型的结果，第三个参数用于使用并行流时合并结果。
<U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner);  
```

&emsp; 三个参数：  
* identity: 初始化值。  
* accumulator: 其类型是BiFunction，输入是U与T两个类型的数据，而返回的是U类型；也就是说返回的类型与输入的第一个参数类型是一样的，而输入的第二个参数类型与Stream中元素类型是一样的。  
* combiner: 其类型是BinaryOperator，支持的是对U类型的对象进行操作。combiner主要是使用在并行计算的场景下；如果Stream是非并行时，第三个参数实际上是不生效的。因此针对这个方法的分析需要分并行与非并行两个场景。 

![image](http://182.92.69.8:8081/img/java/JDK/java8/java-2.png)  
&emsp; Stream.reduce常用的操作有average、sum、min、max、count，返回单个的结果值，并且reduce操作每处理一个元素总是创建一个新值。  

```java
// 字符串连接，concat = "ABCD"
String concat = Stream.of("A", "B", "C", "D").reduce("", String::concat);
// 求最小值，minValue = -3.0
double minValue = Stream.of(-1.5, 1.0, -3.0, -2.0).reduce(Double.MAX_VALUE, Double::min);
// 求和，sumValue = 10, 有起始值
int sumValue = Stream.of(1, 2, 3, 4).reduce(0, Integer::sum);
    //或
int sumValue = Stream.of(1, 2, 3, 4).reduce(0, (a, b) -> a+b);
// 求和，sumValue = 10, 无起始值
sumValue = Stream.of(1, 2, 3, 4).reduce(Integer::sum).get();
// 过滤，字符串连接，concat = "ace"
concat = Stream.of("a", "B", "c", "D", "e", "F").
filter(x -> x.compareTo("Z") > 0).
reduce("", String::concat);
```

##### 1.2.2.2.3. Collect收集结果  
&emsp; reduce()方法的处理方式一般是每次都产生新的数据集，而collect()方法是在原数据集的基础上进行更新，过程中不产生新的数据集。collect()是Stream接口方法中最灵活的一个。  
&emsp; Stream API有两种collect方法。  

* 方法一：

    ```java
    <R, A> R collect(Collector<? super T, A, R> collector);  
    ```
    &emsp; 主要使用Collectors(java.util.stream.Collectors)来进行各种reduction 操作。  Collections是java.util包的一个工具类，内涵各种处理集合的静态方法： 

        1. 将流中的数据转成集合类型: toList、toSet、toMap、toCollection   
        2. 将流中的数据(字符串)使用分隔符拼接在一起：joining  
        3. 对流中的数据求最大值maxBy、最小值minBy、求和summingInt、求平均值averagingDouble  
        4. 对流中的数据进行映射处理 mapping  
        5. 对流中的数据分组：groupingBy、partitioningBy  
        6. 对流中的数据累计计算：reducing  

* 方法二：  

    ```java
    <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner);
    ```  
    &emsp; 参数supplier是一个生成目标类型实例的方法，代表着目标容器是什么；accumulator是将操作的目标数据填充到supplier 生成的目标类型实例中去的方法，代表着如何将元素添加到容器中；而combiner是将多个supplier生成的实例整合到一起的方法，代表着规约操作，将多个结果合并。  

##### stream分组排序高级使用
<!-- 
https://blog.csdn.net/razorluo/article/details/106234628

-->

#### 1.2.2.3. ParallelStream   
&emsp; 数据并行处理，只需要在原来的基础上加一个parallel()就可以开启， **<font color = "clime">并行流底层使用[fork/join](/docs/java/concurrent/ForkJoinPool.md)。</font>**  
&emsp; parallelStream是什么，它是一个集合的并发处理流。其作用是把一个集合中的数据分片，进行一个多线程的处理，增快运行速度。  

&emsp; 默认的并行数是Ncpu个。并行线程数量：   
1. 并行流在启动线程上，默认会调用 Runtime.getRuntime().availableProcessors()，获取JVM底层最大设备线程数。  
2. 如果想设置并行线程启动数量，则需要全局设置System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "12");  

##### 1.2.2.3.1. ★★★parallelStream() 线程安全  
<!-- 
https://www.jianshu.com/p/e9a36f2802ae?from=timeline&isappinstalled=0
https://www.jianshu.com/p/32277e84dd1d
-->
&emsp; **<font color = "clime">使用并行流parallelStream()有线程安全问题。例如：parallelStream().forEach()内部修改集合会有问题。解决方案：使用锁Syschronize或Lock或其他方案保障线程安全。</font>**  
&emsp; 示例：非安全代码  

```java
//创建集合大小为100
List<Integer> integers = Lists.newArrayList();
for (int i = 0; i < 100; i++){
    integers.add(i);
}
//多管道遍历
List<Integer> integerList = Lists.newArrayList();
integers.parallelStream().forEach(e -> {
        //添加list的方法
        setInteger(integerList, e);
        try {
        //休眠100ms，假装执行某些任务
            Thread.sleep(100);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
});

private static void setInteger(List<Integer> integerList, Integer e) {

    integerList.add(e);
}
```
&emsp; 以上代码会发现，执行完了integerList的大小和预期的不一样。解决方案：在setInteger()方法上加个修饰synchronized。  

```java
// 加synchronized修饰的方法是线程安全的，某一线程在执行这个方法的时候，其他线程只能眼巴巴看着；
 private static synchronized void setInteger(List<Integer> integerList, Integer e) {

    integerList.add(e);
}
```

## 1.3. intellij debug 技巧:java 8 stream  
&emsp; 使用插件Java Stream Debugger。  
&emsp; 示例代码：  
![image](http://182.92.69.8:8081/img/java/JDK/java8/java-3.png)  
&emsp; 开始调试，打个断点：  
![image](http://182.92.69.8:8081/img/java/JDK/java8/java-4.png)  
&emsp; 然后在debug的窗口找到该按钮  
![image](http://182.92.69.8:8081/img/java/JDK/java8/java-5.png)  
&emsp; 然后可以看到每一步操作的结果  
![image](http://182.92.69.8:8081/img/java/JDK/java8/java-6.png)  
