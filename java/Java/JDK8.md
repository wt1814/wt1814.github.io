---
title: JDK1.8
date: 2020-01-27 00:00:00
tags:
    - JDK
---




# 接口的默认方法与静态方法  
&emsp; 在接口中新增了default方法和static方法，这两种方法可以有方法体。  

## 接口中默认方法  
&emsp; JDK1.8引入了接口默认方法，其目的是为了解决接口的修改与已有的实现不兼容的问题，接口默认方法可以作为库、框架向前兼容的一种手段。  
&emsp; 在接口中定义默认方法，使用default关键字，并提供默认的实现。  

```
public interface DefaultFunctionInterface {
    default String defaultFunction() {
        return "default function";
    }
}
```
&emsp; default方法被子接口继承，也可以被其实现类所调用。default方法被继承时，可以被子接口覆写。  
&emsp; 如果一个类实现了多个接口，且这些接口中无继承关系，这些接口中若有相同的（同名，同参数）的default方法，则接口实现类会报错。接口实现类必须通过特殊语法指定该实现类要实现哪个接口的default方法，\<接口\>.super.\<方法名\>([参数])。  

## 接口中静态方法  
&emsp; 在接口中定义静态方法。  

```
public interface StaticFunctionInterface {
    static String staticFunction() {
        return "static function";
    }
}
```
&emsp; 接口中的static方法不能被继承，也不能被实现类调用，只能被自身调用。但是静态变量会被继承。  

# Lambda表达式  
## 函数式接口  
&emsp; 函数式接口：接口中只有一个抽象方法。可以有默认方法、静态方法，可以覆写Object类中的public方法。  
&emsp; 标记注解@FunctionalInterface用于声明接口是函数式接口，此接口中多于一个抽象方法，编译器会报错。但是创建函数式接口并不需@FunctionalInterface，此注解只是用来提供信息，也就是更显示的说明此接口是函数式接口。根据java8的定义，任何只有一个抽像方法的接口都是函数式接口（可以包括其它变量成员和方法，只要抽像方法是一个就可以），没有@FunctionalInterface注解，也是函数式接口。  

```
@FunctionalInterface
interface Converter<F, T> {
    T convert(F from);
}
```
&emsp; 函数式接口的实例创建三种方式：lambda表达式；方法引用；构造方法引用。   

## Lambda表达式  
### 简介:  
&emsp; Lambda表达式是一个匿名函数，即没有函数名的函数。用于创建一个函数式接口的实例。Lambda表达式会被匹配到函数式接口的抽象方法上（Lambda表达式是函数式接口中唯一抽象方法的方法体）。Lambda表达式的参数的类型和数量必须与函数式接口内的抽象方法的参数兼容；返回类型必须兼容；并且lambda表达式可能抛出的异常必须能被该方法接受。Lambda表达式只支持函数式接口。  
&emsp; Lambda表达式只是对函数式接口中抽象方法的引用，并不执行，将其赋值给了一个变量。若要执行，使用（变量.方法名称）。  

  
### 语法：    
&emsp; Lambda表达式语法：一个Lambda表达式由用逗号分隔的参数列表、–>符号与{}函数体三部分表示。函数体既可以是一个表达式，也可以是一个语句块。  
1. 方法体为表达式，该表达式的值作为返回值返回。  (parameters) -> expression  
2. 方法体为代码块，必须用{}来包裹起来。  

```
(parameters参数列表) -> { statements; }
<函数式接口>  <变量名> = (参数1，参数2...) -> {
    //方法体
}
```
&emsp; Lambda表达式的参数：     
1. 如果形参列表为空，只需保留()。  
2. 如果形参只有1个，()可以省略，只需要参数的名称即可。  
3. 对于Lambda表达式中的多个参数，如果需要显示声明一个参数的类型，那么必须为所有的参数都声明类型。  
4. 形参列表的数据类型会自动推断；参数的类型既可以明确声明，也可以根据上下文来推断。eg：(int a)与(a)效果相同。  

&emsp; 类型推断：在Lambda表达式中，不需要明确指出参数类型，javac编译器会通过上下文自动推断参数的类型信息。根据上下文推断类型的行为称为类型推断。Java8提升了Java中已经存在的类型推断系统，使得对Lambda表达式的支持变得更加强大。javac会寻找紧邻lambda表达式的一些信息通过这些信息来推断出参数的正确类型。  

&emsp; Lambda的表达式、方法体：  
1. 如果执行语句只有1句，无返回值，{}可以省略；若有返回值，则若想省去{}，则必须同时省略return，且执行语句也保证只有1句。  
2. 如果Lambda表达式的主体包含一条以上语句，则表达式必须包含在花括号{}中（形成代码块）。且需要一个return返回值，但若函数式接口里面方法返回值是void，则无需返回值。语句块中，return语句会把控制权交给匿名方法的调用者；break和continue只能在循环中使用；如果函数体有返回值，那么函数体内部的每一条路径都必须返回值。  

        可选类型声明：不需要声明参数类型，编译器可以统一识别参数值。
        可选的参数圆括号：一个参数无需定义圆括号，但多个参数需要定义圆括号。
        可选的大括号：如果主体包含了一个语句，就不需要使用大括号。
        可选的返回关键字：如果主体只有一个表达式返回值则编译器会自动返回值，大括号需要指定明表达式返回了一个数值

### 使用教程  
#### 变量作用域（调用Lambda表达式外部参数）：  
&emsp; 访问外层作用域定义的局部变量、类的属性：  
* 访问局部变量：lambda表达式若访问了局部变量，则局部变量必须是final的。若局部变量没有加final关键字，系统会自动添加，此后在修改该局部变量，会编译错误。  
* 访问类的属性：lambda内部使用this关键字（或不使用）访问或修改全局变量、实例方法。  

```
public class Test2 {
    public static void main(String[] args) {
        Test2 test = new Test2();
        test.method();
    }
    @Override
    public String toString() {
        return "Lambda";
    }
    public void method() {
        Runnable runnable = () -> {
            System.out.println(this.toString());
        };
        new Thread(runnable).start();
    }
}
```

#### 泛型函数式接口  
&emsp; Lambda表达式自身不能指定类型参数。因此Lambda表达式不能是泛型。但是与Lambda表达式关联的函数式接口可以泛型。此时，Lambda表达式的目标类型部分由声明函数式接口引用时指定的参数类型决定。  

```
interface SomeFunc<T> {
    T func(T t);
}
```

```
class GenericFunctionalInterfaceDemo {
    public static void main(String[] args) {
        SomeFunc<String> reverse = (str) -> {
            int i;
            String result = "";
            for (i = str.length() - 1; i >= 0; i--) {
                result += str.charAt(i);
            }
            return result;
        };
        System.out.println("lambda reserved is " + reverse.func("lambda"));

        SomeFunc<Integer> factorial = (n) -> {
            int result = 1;
            for (int i = 1; i <= n; i++) {
                result = result * i;
            }
            return result;
        };
        System.out.println("The factorial of 3 is " + factorial.func(3));
    }
}
```
结果：  

    lambda reserved is adbmal
    The factorial of 3 is 6
&emsp; 分析：T指定了func()函数的返回类型和参数类型。这意味着它与任何只接收一个参数，并返回一个相同类型的值的lambda表达式兼容。  
&emsp; SomeFunc接口用于提供对两种不同类型的lambda表达式的引用。第一种表达式使用String类型，第二种表达式使用Integer类型。因此，同一个函数式接口可以用于reserve lambda表达式和factorial lambda表达式。区别仅在于传递给SomeFunc的参数类型。  

#### Lambda表达式作为参数传递  
&emsp; 为了将lambda表达式作为参数传递，接收lambda表达式的参数的类型必须是与该lambda表达式兼容的函数式接口的类型。  
&emsp; 注：Lambda表达式作为方法参数使用。  

```
//Use lambda expressions as an argument to method
interface StringFunc {
    String func(String n);
}
```

```
class lambdasAsArgumentsDemo {
    static String stringOp(StringFunc sf, String s) {
        return sf.func(s);
    }
    public static void main(String[] args) {
        String inStr = "lambda add power to java";
        String outStr;
        System.out.println("Here is input string: " + inStr);
        
//Lambda表达式作为方法参数使用
        //第一种方式
        outStr = stringOp((str) -> str.toUpperCase(), inStr);
        System.out.println("The string in uppercase: " + outStr);
        //第二种方式
        outStr = stringOp((str) ->
        {
            String result = "";
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) != '') {
                    result += str.charAt(i);
                }
            }
            return result;
        }, inStr);
        System.out.println("The string with spaces removed: " + outStr);
        //第三种方式
        //当块lambda特别长，不适合嵌入到方法的调用中时，很容易把块lambda赋给一个函数式接口变量。然后，可以简单地把该引用传递给方法。
        StringFunc reverse = (str) ->
        {
            String result = "";
            for (int i = str.length() - 1; i >= 0; i--) {
                result += str.charAt(i);
            }
            result result;
        };
        System.out.println("The string reserved: " + stringOp(reverse, inStr));
    }
}
```
&emsp; 分析：当块lambda看上去特别长，不适合嵌入到方法的调用中时，很容易把块lambda赋给一个函数式接口变量。然后，可以简单地把该引用传递给方法。  

#### Lambda表达式作为返回值  
......

## 方法引用、构造方法引用  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JDK/java8/java-1.png)  
&emsp; 当Lambda表达式中只是执行一个方法调用时，可以舍弃Lambda表达式，直接通过方法引用的形式可读性更高一些。方法引用是一种更简洁易懂的Lambda表达式。方法引用是用来直接访问类或者实例的已经存在的方法或者构造方法（如果是类，不用实例化对象）。方法引用提供了一种引用而不执行方法的方式，它需要由兼容的函数式接口构成的目标类型上下文。计算时，方法引用会创建函数式接口的一个实例。  
&emsp; 方法引用的标准形式是：类名/对象的引用::方法名/new（注意：只需要写方法名，不需要写括号）。有以下四种形式的方法引用：  

|类型	|示例|
|---|---|	
|引用静态方法	|ContainingClass::staticMethodName	|接受一个Class类型的参数|
|引用某个对象的实例方法	|containingObject::instanceMethodName	|方法没有参数。|
|引用某个类型的任意对象的实例方法	|ContainingType::methodName	|方法接受一个参数。与以上不同的地方在于，以上是在列表元素上分别调用方法，而此是在某个对象上调用方法，将列表元素作为参数传入|
|引用构造方法	|ClassName::new	|构造器方法是没有参数|

## 常用函数式接口  
&emsp; Function，类型转换；  Supplier，供给型接口；  Consumer，消费接口；  Predicate，断言型接口；


------
# StreamAPI  
## 简介  
&emsp; java8中的Stream是对集合（Collection）对象功能的增强，它专注于对集合对象进行各种聚合操作，或者大批量数据操作。Stream API通过Lambda表达式，极大的提高编程效率和程序可读性。同时它提供串行和并行两种模式进行汇聚操作，并发模式能够充分利用多核处理器的优势，使用fork/join并行方式来拆分任务和加速处理过程。  
&emsp; Stream和Iterator区别：Stream如同一个迭代器（Iterator），单向，不可往复，数据只能遍历一次。而和迭代器又不同的是，Stream可以并行化操作，迭代器只能命令式地、串行化操作。Stream的并行操作依赖于Java7中引入的Fork/Join框架（JSR166y）来拆分任务和加速处理过程。  
&emsp; lambda表达式是函数式接口的实现，参数行为化；stream流的参数是函数式接口，即lambda表达式的实例。  

## Stream流的使用详解  
### 流的构造与转换  
&emsp; 有多种方式生成Stream Source：  
1. 从Collection和数组：Collection.stream()、Collection.parallelStream()、Arrays.stream(T array)、Stream.of()；  
2. 从BufferedReader：java.io.BufferedReader.lines()；  
3. 静态工厂：java.util.stream.IntStream.range()、java.nio.file.Files.walk()；  
4. 自定义构建：java.util.Spliterator；  
5. 其它：Random.ints()、BitSet.stream()、Pattern.splitAsStream(java.lang.CharSequence)、JarFile.stream()；  

&emsp; 基本数值型对应的Stream：对于基本数值型，目前有三种对应的包装类型 Stream：IntStream、LongStream、DoubleStream。  

### 流的操作  
&emsp; 数据结构包装成Stream，对Stream中元素进行操作。流的操作类型分为三种：  
* Intermediate（中间方法）：一个流可以后面跟随零个或多个intermediate 操作。其目的主要是打开流，做出某种程度的数据映射/过滤，然后返回一个新的流，交给下一个操作使用。这类操作都是惰性化的（lazy），仅仅调用到这类方法，并没有真正开始流的遍历。  
分类：map (mapToInt, flatMap等)、filter、distinct、sorted、peek、limit、skip、parallel、sequential、unordered；  
* short-circuiting：对于一个intermediate操作，如果它接受的是一个无限大（infinite/unbounded）的Stream，但返回一个有限的新Stream。对于一个terminal操作，如果它接受的是一个无限大的Stream，但能在有限的时间计算出结果。当操作一个无限大的Stream，而又希望在有限时间内完成操作，则在管道内拥有一个short-circuiting操作是必要非充分条件。  
分类：anyMatch、allMatch、noneMatch、findFirst、findAny、limit；  
* Terminal（最终方法）：一个流只能有一个terminal操作，当这个操作执行后，流就被使用“光”了，无法再被操作。所以这必定是流的最后一个操作。Terminal操作的执行，才会真正开始流的遍历，并且会生成一个结果，或者一个side effect。  
分类：forEach、forEachOrdered、toArray、reduce、collect、min、max、count、 anyMatch、allMatch、noneMatch、findFirst、findAny、iterator；  


&emsp; ***Intermediate和Terminal联系：***  
&emsp; 在对于一个Stream进行多次转换操作(Intermediate操作)，每次都对 Stream的每个元素进行转换，而且是执行多次，这样时间复杂度就是N（转换次数）个for循环里把所有操作的总和吗？  
&emsp; 其实不是这样的，转换操作都是lazy的，多个转换操作只会在Terminal操作的时候融合起来，一次循环完成。即Stream里有个操作函数的集合，每次转换操作就是把转换函数放入这个集合中，在Terminal操作的时候循环Stream对应的集合，然后对每个元素执行所有的函数。  

#### 数值流  
&emsp; 数值流IntStream, DoubleStream, LongStream，这种流中的元素都是原始数据类型，分别是int，double，long。  

##### 流与数值流的转换：  
* 流转换为数值流：  
    * mapToInt(T -> int) : return IntStream  
    * mapToDouble(T -> double) : return DoubleStream  
    * mapToLong(T -> long) : return LongStream  

    IntStream intStream = list.stream().mapToInt(Person::getAge);

* 数值流转换为流：boxed  
    * Stream<Integer> stream = intStream.boxed();  

##### 数值流方法：  
&emsp; sum()、max()、min()、average() 等…  


#### 集合Stream  

##### Map  
&emsp; Map把一种类型的Stream变为另一种类型的Stream，map方法内需要一个Function接口，Function<? super String, ? extends String> mapper。  
&emsp; 使用场景：从对象列表中提取出单个字段的列表。  

```
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

##### Reduce聚合操作  
&emsp; reduce()根据一定的规则将Stream中的元素进行计算后返回一个唯一的值。它提供一个起始值（种子），然后依照运算规则（BinaryOperator），和前面Stream的第一个、第二个、第n个元素组合。在没有起始值时，会将Stream的前面两个元素组合，返回的是Optional。字符串拼接、数值的sum、min、max、average都是特殊的reduce。  
&emsp; reduce()方法有三种形式：  

    Optional<T> reduce(BinaryOperator<T> accumulator);  
    对Stream中的数据通过累加器accumulator迭代计算，最终得到一个Optional对象。  
    函数接口BinaryOperator<T>继承于BiFunction<T, T, T>，接收两个参数，返回一个结果。  
    T reduce(T identity, BinaryOperator<T> accumulator);  
    给定一个初始值identity，通过累加器accumulator迭代计算，得到一个同Stream中数据同类型的结果。  
    <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner);  
    给定一个初始值identity，通过累加器accumulator迭代计算，得到一个identity类型的结果，第三个参数用于使用并行流时合并结果。  

三个参数：  
* identity: 初始化值。  
* accumulator: 其类型是BiFunction，输入是U与T两个类型的数据，而返回的是U类型；也就是说返回的类型与输入的第一个参数类型是一样的，而输入的第二个参数类型与Stream中元素类型是一样的。  
* combiner: 其类型是BinaryOperator，支持的是对U类型的对象进行操作。combiner主要是使用在并行计算的场景下；如果Stream是非并行时，第三个参数实际上是不生效的。因此针对这个方法的分析需要分并行与非并行两个场景。 

![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JDK/java8/java-2.png)  
&emsp; Stream.reduce常用的操作有average、sum、min、max、count，返回单个的结果值，并且reduce操作每处理一个元素总是创建一个新值。  

```
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

##### Collect收集结果  
&emsp; reduce()方法的处理方式一般是每次都产生新的数据集，而collect()方法是在原数据集的基础上进行更新，过程中不产生新的数据集。collect()是Stream接口方法中最灵活的一个。  
&emsp; Stream API有两种collect方法。  

* 方法一：

    <R, A> R collect(Collector<? super T, A, R> collector);  
&emsp; 主要使用Collectors（java.util.stream.Collectors）来进行各种reduction 操作。  
&emsp; Collections是java.util包的一个工具类，内涵各种处理集合的静态方法。  
* 将流中的数据转成集合类型: toList、toSet、toMap、toCollection  
* 将流中的数据(字符串)使用分隔符拼接在一起：joining  
* 对流中的数据求最大值maxBy、最小值minBy、求和summingInt、求平均值averagingDouble  
* 对流中的数据进行映射处理 mapping  
* 对流中的数据分组：groupingBy、partitioningBy  
* 对流中的数据累计计算：reducing  

* 方法二：  

    <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner);  
&emsp; 参数supplier是一个生成目标类型实例的方法，代表着目标容器是什么；accumulator是将操作的目标数据填充到supplier 生成的目标类型实例中去的方法，代表着如何将元素添加到容器中；而combiner是将多个supplier生成的实例整合到一起的方法，代表着规约操作，将多个结果合并。  


#### ParallelStream   
&emsp; 数据并行处理，只需要在原来的基础上加一个parallel()就可以开启，这里parallel()开启的底层并行框架是fork/join，默认的并行数是Ncpu个。  
&emsp; 并行线程数量：   
1. 并行流在启动线程上，默认会调用 Runtime.getRuntime().availableProcessors(),获取JVM底层最大设备线程数。  
2. 如果想设置并行线程启动数量,则需要全局设置     System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "12");  


## intellij debug 技巧:java 8 stream  
&emsp; 使用插件Java Stream Debugger。  
&emsp; 示例代码：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JDK/java8/java-3.png)  
&emsp; 开始调试，打个断点：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JDK/java8/java-4.png)  
&emsp; 然后在debug的窗口找到该按钮  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JDK/java8/java-5.png)  
&emsp; 然后可以看到每一步操作的结果  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JDK/java8/java-6.png)  

-----
# Optional类  
&emsp; Java8引入Optional类，是一个可以为null的容器对象，是一个包含有可选值的包装类，可以保存类型T的值，或者保存null。Optional类的引入解决空指针异常。防止编写不必要的null检查。快速定位NullPointException。  
&emsp; public final class Optional<T>，构造函数私有化；不能new实例；不能被继承；  

## 常用API  

|方法	|描述|
|---|---|
|empty()	|返回空的Optional实例|
|of(T value)	|返回一个指定非null值的Optional|
|ofNullable(T value)	|如果为非空，返回 Optional 描述的指定值，否则返回空的 Optional|
|   |    |	
|equals(Object obj)	|判断其他对象是否等于Optional|
|filter(Predicate<? super <T> predicate)	|如果值存在，并且这个值匹配给定的 predicate，返回一个Optional用以描述这个值，否则返回一个空的Optional|
|flatMap(Function<? super T,Optional<U>> mapper)	|如果值存在，返回基于Optional包含的映射方法的值，否则返回一个空的Optional|
|get()	|如果在这个Optional中包含这个值，返回值，否则抛出异常：NoSuchElementException|
|hashCode()	|返回存在值的哈希码，如果值不存在 返回0|
|ifPresent(Consumer<? super T> consumer)	|如果值存在则使用该值调用consumer, 否则不做任何事情|
|isPresent()	|如果值存在则方法会返回true，否则返回 false|
|map(Function<? super T,? extends U> mapper)	|如果存在该值，提供的映射方法，如果返回非null，返回一个Optional描述结果|
|orElse(T other)	|如果存在该值，返回值，否则返回 other|
|orElseGet(Supplier<? extends T> other)	|如果存在该值，返回值，否则触发 other，并返回other调用的结果|
|orElseThrow(Supplier<? extends X> exceptionSupplier)	|如果存在该值，返回包含的值，否则抛出由Supplier继承的异常|
|toString()	|返回一个Optional的非空字符串，用来调试|


## 正确使用Optional类  
### 错误使用：  

&emsp; 先看看错误的姿势：  

```
User user;
Optional<User> optional = Optional.of(user);
if (optional.isPresent()) {
    return optional.get().getOrders();
} else {
    return Collections.emptyList();;
}
```

&emsp; 和之前的写法没有任何区别。  

```
if (user != null) {
    return user.getOrders();
} else {
    return Collections.emptyList();;
}
```
&emsp; 慎重使用Optional类的以下方法：isPresent()方法、get()方法、Optional类型作为类/实例属性、Optional类型作为方法参数。isPresent()与obj!= null无任何分别。而没有isPresent()作铺垫的get()调用，在IntelliJ IDEA中会收到告警。把Optional类型用作属性或是方法参数在IntelliJ IDEA中更是强力不推荐的。所以Optional中真正可依赖的是除了isPresent()和get()的其他方法。  

### 正确使用：  
&emsp; 创建Optional对象：  

```
Optional<Soundcard> sc = Optional.empty();
SoundCard soundcard = new Soundcard();
Optional<Soundcard> sc = Optional.of(soundcard);
Optional<Soundcard> sc = Optional.ofNullable(soundcard);
```
&emsp; 存在即返回, 无则提供默认值：  

```
return user.orElse(null);  //而不是 return user.isPresent() ? user.get() : null;
return user.orElse(UNKNOWN_USER);
```
&emsp; 存在即返回, 无则由函数来产生：  

```
return user.orElseGet(() -> fetchAUserFromDatabase()); //而不要 return user.isPresent() ? user: fetchAUserFromDatabase();
```
&emsp; 存在才对它做点什么：  

```
user.ifPresent(System.out::println);
/*
而不要下边那样
if (user.isPresent()) {
    System.out.println(user.get());
}*/
```
&emsp; 使用map抽取特定的值或者做值的转换：  

```
return user.map(u -> u.getOrders()).orElse(Collections.emptyList())
//上面避免了我们类似 Java 8 之前的做法
if(user.isPresent()) {
    return user.get().getOrders();
} else {
    return Collections.emptyList();
}
```
&emsp; 级联使用map，避免了连续的空值判断:  

```
return user.map(u -> u.getUsername()).map(name -> name.toUpperCase()).orElse(null);
/*User user = .....
if(user != null) {
    String name = user.getUsername();
    if(name != null) {
        return name.toUpperCase();
    } else {
        return null;
    }
} else {
    return null;
}*/
```

&emsp; 级联的Optional对象使用flatMap：  

```
String version = computer.flatMap(Computer::getSoundcard)
        .flatMap(Soundcard::getUSB)
        .map(USB::getVersion)
        .orElse("UNKNOWN");
```

&emsp; 使用filter拒绝特定的值:  

```
Optional<USB> maybeUSB = ...;
maybeUSB.filter(usb -> "3.0".equals(usb.getVersion()).ifPresent(() -> System.out.println("ok"));
```
&emsp; 使用isPresent()处理NullPointerException不叫优雅；使用orElse, orElseGet等, 特别是map方法才叫优雅。其他几个，filter()把不符合条件的值变为empty()，flatMap()总是与map()方法成对的，orElseThrow()在有值时直接返回，无值时抛出想要的异常。
&emsp; 小结：使用Optional时尽量不直接调用Optional.get()方法, Optional.isPresent()更应该被视为一个私有方法，应依赖于其他像Optional.orElse()，Optional.orElseGet()，Optional.map()等这样的方法。



-----
# Date/Time API  
&emsp; 旧版的Java中，日期时间API存在诸多问题：  
&emsp; 非线程安全：java.util.Date是非线程安全的，所有的日期类都是可变的，这是Java日期类最大的问题之一。  
&emsp; 设计很差：Java的日期/时间类的定义并不一致，在java.util和java.sql的包中都有日期类，此外用于格式化和解析的类在java.text包中定义。java.util.Date同时包含日期和时间，而java.sql.Date仅包含日期，将其纳入java.sql包并不合理。另外这两个类都有相同的名字，这本身就是一个非常糟糕的设计。  
&emsp; 时区处理麻烦：日期类并不提供国际化，没有时区支持，因此Java引入了java.util.Calendar和java.util.TimeZone类，但他们同样存在上述所有的问题。  

&emsp; 新的日期API是JSR-310规范的实现，Joda-Time框架的作者正是JSR-310的规范的倡导者，所以能从Java 8的日期API中看到很多Joda-Time的特性。  

&emsp; Java日期/时间API包含以下相应的包。  
1. java.time包：这是新的Java日期/时间API的基础包，所有的主要基础类都是这个包的一部分，如：LocalDate，LocalTime，LocalDateTime，Instant, Period，Duration等等。所有这些类都是不可变的和线程安全的，在绝大多数情况下，这些类能够有效地处理一些公共的需求。  

    LocalTime、LocalDate、LocalDateTime；
    ZoneId、ZoneOffset、ZonedDateTime；
    MonthDay、YearMonth、Year；
    OffsetDateTime、OffsetTime；
    Clock，时钟，比如获取目前美国纽约的时间；
    Instant，时间戳瞬时时间；
    Period，时间段；   Duration，持续时间，时间差；
2. java.time.chrono包：这个包为非ISO的日历系统定义了一些泛化的API，可以扩展AbstractChronology类来创建自己的日历系统。  
3. java.time.format包：这个包包含能够格式化和解析日期时间对象的类，在绝大多数情况下，不应该直接使用它们。因为java.time包中相应的类已经提供了格式化和解析的方法。DateTimeFomatter：格式化类，解析日期对象的类。  
4. java.time.temporal包：这个包包含一些时态对象，可以用其找出关于日期/时间对象的某个特定日期或时间，比如说，可以找到某月的第一天或最后一天。可以非常容易地认出这些方法，因为它们都具有“withXXX”的格式。  
5. java.time.zone包：这个包包含支持不同时区以及相关规则的类。  

## 接口API  
### 本地化日期时间API  
&emsp; LocalDate/LocalTime和LocalDateTime类处理时区不是必须的情况。    

```
public class Java8Tester {
    public static void main(String args[]) {
        Java8Tester java8tester = new Java8Tester();
        java8tester.testLocalDateTime();
    }

    public void testLocalDateTime() {
        // 获取当前的日期时间
        LocalDateTime currentTime = LocalDateTime.now();
        System.out.println("当前时间: " + currentTime);
        LocalDate date1 = currentTime.toLocalDate();
        System.out.println("date1: " + date1);
        Month month = currentTime.getMonth();
        int day = currentTime.getDayOfMonth();
        int seconds = currentTime.getSecond();
        System.out.println("月: " + month + ", 日: " + day + ", 秒: " + seconds);
        LocalDateTime date2 = currentTime.withDayOfMonth(10).withYear(2012);
        System.out.println("date2: " + date2);
        // 12 december 2014
        LocalDate date3 = LocalDate.of(2014, Month.DECEMBER, 12);
        System.out.println("date3: " + date3);
        // 22小时15分钟
        LocalTime date4 = LocalTime.of(22, 15);
        System.out.println("date4: " + date4);
        // 解析字符串
        LocalTime date5 = LocalTime.parse("20:15:30");
        System.out.println("date5: " + date5);
    }
}
```

### 时区的日期时间API  

```
public class Java8Tester {
    public static void main(String args[]) {
        Java8Tester java8tester = new Java8Tester();
        java8tester.testZonedDateTime();
    }

    public void testZonedDateTime() {
        // 获取当前时间日期
        ZonedDateTime date1 = ZonedDateTime.parse("2015-12-03T10:15:30+05:30[Asia/Shanghai]");
        System.out.println("date1: " + date1);
        ZoneId id = ZoneId.of("Europe/Paris");
        System.out.println("ZoneId: " + id);
        ZoneId currentZone = ZoneId.systemDefault();
        System.out.println("当期时区: " + currentZone);
    }
}
```

### Instant  
当计算程序的运行时间时，应当使用时间戳Instant。  
Instant用于表示一个时间戳，它与我们常使用的System.currentTimeMillis()有些类似，不过Instant可以精确到纳秒（Nano-Second），System.currentTimeMillis()方法只精确到毫秒（Milli-Second）。如果查看Instant源码，发现它的内部使用了两个常量，seconds表示从1970-01-01 00:00:00开始到现在的秒数，nanos表示纳秒部分（nanos的值不会超过999,999,999）。Instant除了使用now()方法创建外，还可以通过ofEpochSecond方法创建：  
Instant instant = Instant.ofEpochSecond(120, 100000);
ofEpochSecond()方法的第一个参数为秒，第二个参数为纳秒，上面的代码表示从1970-01-01 00:00:00开始后两分钟的10万纳秒的时刻，控制台上的输出为：


## 与日期和日历（旧的时间API）的兼容性  

## 日期格式化  

```
LocalDateTime dateTime = LocalDateTime.now();
String strDate1 = dateTime.format(DateTimeFormatter.BASIC_ISO_DATE);   // 20170105
String strDate2 = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE);    // 2017-01-05
String strDate3 = dateTime.format(DateTimeFormatter.ISO_LOCAL_TIME);    // 14:20:16.998
String strDate4 = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // 2017-01-05
String strDate5 = dateTime.format(DateTimeFormatter.ofPattern("今天是：YYYY年 MMMM DD日 E", Locale.CHINESE)); // 今天是：2017年 一月 05日 星期四
```

## 计算时间差  
&emsp; Java8中使用以下类来计算日期时间差异：1.Period；2.Duration；3.ChronoUnit。  

### Period类  
&emsp; Period类方法getYears()、getMonths()和getDays()，3者连用得到today与oldDate两个日期相差的年、月、日信息。  

```
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;

public class Test {

    public static void main(String[] args) {
        LocalDate today = LocalDate.now();
        LocalDate birthDate = LocalDate.of(1993, Month.OCTOBER, 19);
        Period p = Period.between(birthDate, today);
        System.out.printf("年龄 : %d 年 %d 月 %d 日", p.getYears(), p.getMonths(), p.getDays());
    }
}
```
&emsp; 结果：  

    Today : 2017-06-16
    BirthDate : 1993-10-19
    年龄 : 23 年 7 月 28 日  

### Duration类  
&emsp; 计算两个时间点的时间差。between()计算两个时间的间隔，默认的单位是秒。方法toNanos()、toMillis()、toMinutes()、toHours()、toDays()等将两时间相差的秒数转化成纳秒数、毫秒数等。  

```
import java.time.Duration;
import java.time.Instant;

public class Test {

    public static void main(String[] args) {
        Instant inst1 = Instant.now();
        System.out.println("Inst1 : " + inst1);
        Instant inst2 = inst1.plus(Duration.ofSeconds(10));
        System.out.println("Inst2 : " + inst2);
        System.out.println("Difference in milliseconds : " + Duration.between(inst1, inst2).toMillis());
        System.out.println("Difference in seconds : " + Duration.between(inst1, inst2).getSeconds());
    }
}
```  

&emsp; 结果:  

    Inst1 : 2017-06-16T07:46:45.085Z
    Inst2 : 2017-06-16T07:46:55.085Z
    Difference in milliseconds : 10000
    Difference in seconds : 10
    ChronoUnit类，java.time.temporal包
    ChronoUnit类枚举类型，实现功能类型Period和Duration，在单个时间单位（秒、分、时）内测量一段时间。
    LocalDateTime oldDate = LocalDateTime.of(2017, Month.AUGUST, 31, 10, 20, 55);
    LocalDateTime newDate = LocalDateTime.of(2018, Month.NOVEMBER, 9, 10, 21, 56);


### ChronoUnit类，java.time.temporal包  
&emsp; ChronoUnit类枚举类型，实现功能类型Period和Duration，在单个时间单位（秒、分、时）内测量一段时间。  

    LocalDateTime oldDate = LocalDateTime.of(2017, Month.AUGUST, 31, 10, 20, 55);
    LocalDateTime newDate = LocalDateTime.of(2018, Month.NOVEMBER, 9, 10, 21, 56);

## SpringBoot中应用LocalDateTime  
&emsp; 将LocalDateTime字段以时间戳的方式返回给前端  
&emsp; 添加日期转化类  

```
public class LocalDateTimeConverter extends JsonSerializer<LocalDateTime> {

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    gen.writeNumber(value.toInstant(ZoneOffset.of("+8")).toEpochMilli());
    }
}
```
&emsp; 并在LocalDateTime字段上添加@JsonSerialize(using = LocalDateTimeConverter.class)注解，如下：  

    @JsonSerialize(using = LocalDateTimeConverter.class)
    protected LocalDateTime gmtModified;

&emsp; 将LocalDateTime字段以指定格式化日期的方式返回给前端 在LocalDateTime字段上添加@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")注解即可，如下：  

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
    protected LocalDateTime gmtModified;

&emsp; 对前端传入的日期进行格式化在LocalDateTime字段上添加@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")注解即可，如下：  

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    protected LocalDateTime gmtModified;



-----
# 异常捕获的改变   
&emsp; 新的try…cache可以自动关闭在try表达式中打开的对象，而无需开发者手动关闭。  
&emsp; 多个流对象打开语句，用分号分隔，不是逗号。  

```
try(ObjectInputStream in=new ObjectInputStream(new FileInputStream("p1.obj"))){
    System.out.println(Person.class.hashCode());
    Person person=(Person)in.readObject();
    System.out.println(person.staticString);
} catch (Exception e) {
    e.printStackTrace();
}
```
&emsp; 不再需要：  

```
finally{
    in.close();
}
```

# Base64  
&emsp; Java8内置了Base64编码的编码器和解码器。Base64类同时还提供了对URL、MIME友好的编码器与解码器。  
&emsp; 基本：输出被映射到一组字符A-Za-z0-9+/，编码不添加任何行标，输出的解码仅支持A-Za-z0-9+/。  
&emsp; URL：输出映射到一组字符A-Za-z0-9+_，输出是URL和文件。  
&emsp; MIME：输出隐射到MIME友好格式。输出每行不超过76字符，并且使用'\r'并跟随'\n'作为分割。编码输出最后没有行分割。  
&emsp; 内嵌类：  

    static class Base64.Decoder	该类实现一个解码器用于，使用Base64编码来解码字节数据。
    static class Base64.Encoder	该类实现一个编码器，使用Base64编码来编码字节数据。 

|方法 |描述|
|---|---|
|Decoder| |
|getDecoder()	|返回Base64.Decoder，解码使用基本型base64编码方案|
|getMimeDecoder()	|返回Base64.Decoder，解码使用MIME型base64 编码方案。|
|getUrlDecoder()	|返回Base64.Decoder，解码使用URL和文件名安全型 base64编码方案。|
|Encoder| | 
|getEncoder()	|返回Base64.Encoder，编码使用基本型base64编码方案。|
|getMimeEncoder()	|返回Base64.Encoder，编码使用MIME型base64编码方案。|
|getMimeEncoder(int lineLength, byte[] lineSeparator)	|返回Base64.Encoder，编码使用MIME型base64编码方案，可以通过参数指定每行的长度及行的分隔符。|
|getUrlEncoder()	|返回Base64.Encoder，编码使用URL和文件名安全型 base64 编码方案。|

```
// 使用基本编码
String base64encodedString = Base64.getEncoder().encodeToString("runoob?java8".getBytes("utf-8"));
System.out.println("Base64 编码字符串 (基本) :" + base64encodedString);
// 解码
byte[] base64decodedBytes = Base64.getDecoder().decode(base64encodedString);
System.out.println("原始字符串: " + new String(base64decodedBytes, "utf-8"));
```

