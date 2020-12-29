

<!-- TOC -->

- [1. Date/Time API](#1-datetime-api)
    - [1.1. 接口API](#11-接口api)
        - [1.1.1. 本地化日期时间API](#111-本地化日期时间api)
        - [1.1.2. 时区的日期时间API](#112-时区的日期时间api)
        - [1.1.3. Instant](#113-instant)
    - [1.2. 与日期和日历（旧的时间API）的兼容性](#12-与日期和日历旧的时间api的兼容性)
    - [1.3. 日期格式化](#13-日期格式化)
    - [1.4. 计算时间差](#14-计算时间差)
        - [1.4.1. Period类](#141-period类)
        - [1.4.2. Duration类](#142-duration类)
        - [1.4.3. ChronoUnit类，java.time.temporal包](#143-chronounit类javatimetemporal包)
    - [1.5. SpringBoot中应用LocalDateTime-1](#15-springboot中应用localdatetime-1)

<!-- /TOC -->

<!-- 
Springboot 关于日期时间格式化处理方式总结 
https://mp.weixin.qq.com/s/-jy0zEpeWXnvQELGv6PsgA

有关机器时间、UTC时间、本地时间的总结 
https://mp.weixin.qq.com/s/eGgwu-qrtLA-NqrAwAqocg


JDK8中的新时间API:Duration Period和ChronoUnit介绍 
https://mp.weixin.qq.com/s/9qk1-wO3x1fkQt-EcwX7SQ

-->

# 1. Date/Time API  
&emsp; 旧版的Java中，日期时间API存在诸多问题：  

* 非线程安全：java.util.Date是非线程安全的，所有的日期类都是可变的，这是Java日期类最大的问题之一。  
* 设计很差：Java的日期/时间类的定义并不一致，在java.util和java.sql的包中都有日期类，此外用于格式化和解析的类在java.text包中定义。java.util.Date同时包含日期和时间，而java.sql.Date仅包含日期，将其纳入java.sql包并不合理。另外这两个类都有相同的名字，这本身就是一个非常糟糕的设计。  
* 时区处理麻烦：日期类并不提供国际化，没有时区支持，因此Java引入了java.util.Calendar和java.util.TimeZone类，但它们同样存在上述所有的问题。  

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

## 1.1. 接口API  
### 1.1.1. 本地化日期时间API  
&emsp; LocalDate/LocalTime和LocalDateTime类处理时区不是必须的情况。    

```java
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

### 1.1.2. 时区的日期时间API  

```java
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

### 1.1.3. Instant  
&emsp; 当计算程序的运行时间时，应当使用时间戳Instant。  
&emsp; Instant用于表示一个时间戳，它与使用的System.currentTimeMillis()有些类似，不过Instant可以精确到纳秒（Nano-Second），System.currentTimeMillis()方法只精确到毫秒（Milli-Second）。如果查看Instant源码，发现它的内部使用了两个常量，seconds表示从1970-01-01 00:00:00开始到现在的秒数，nanos表示纳秒部分（nanos的值不会超过999,999,999）。Instant除了使用now()方法创建外，还可以通过ofEpochSecond方法创建：  

```java
Instant instant = Instant.ofEpochSecond(120, 100000);  
```
&emsp; ofEpochSecond()方法的第一个参数为秒，第二个参数为纳秒，上面的代码表示从1970-01-01 00:00:00开始后两分钟的10万纳秒的时刻，控制台上的输出为：


## 1.2. 与日期和日历（旧的时间API）的兼容性  

## 1.3. 日期格式化  

```java
LocalDateTime dateTime = LocalDateTime.now();
String strDate1 = dateTime.format(DateTimeFormatter.BASIC_ISO_DATE);   // 20170105
String strDate2 = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE);    // 2017-01-05
String strDate3 = dateTime.format(DateTimeFormatter.ISO_LOCAL_TIME);    // 14:20:16.998
String strDate4 = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // 2017-01-05
String strDate5 = dateTime.format(DateTimeFormatter.ofPattern("今天是：YYYY年 MMMM DD日 E", Locale.CHINESE)); // 今天是：2017年 一月 05日 星期四
```

## 1.4. 计算时间差  
&emsp; Java8中使用以下类来计算日期时间差异：1.Period；2.Duration；3.ChronoUnit。  

### 1.4.1. Period类  
&emsp; Period类方法getYears()、getMonths()和getDays()，3者连用得到today与oldDate两个日期相差的年、月、日信息。  

```java
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

### 1.4.2. Duration类  
&emsp; 计算两个时间点的时间差。between()计算两个时间的间隔，默认的单位是秒。方法toNanos()、toMillis()、toMinutes()、toHours()、toDays()等将两时间相差的秒数转化成纳秒数、毫秒数等。  

```java
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


### 1.4.3. ChronoUnit类，java.time.temporal包  
&emsp; ChronoUnit类枚举类型，实现功能类型Period和Duration，在单个时间单位（秒、分、时）内测量一段时间。  

```java
LocalDateTime oldDate = LocalDateTime.of(2017, Month.AUGUST, 31, 10, 20, 55);
LocalDateTime newDate = LocalDateTime.of(2018, Month.NOVEMBER, 9, 10, 21, 56);
```

## 1.5. SpringBoot中应用LocalDateTime-1  
&emsp; 将LocalDateTime字段以时间戳的方式返回给前端，添加日期转化类  

```java
public class LocalDateTimeConverter extends JsonSerializer<LocalDateTime> {

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    gen.writeNumber(value.toInstant(ZoneOffset.of("+8")).toEpochMilli());
    }
}
```
&emsp; 并在LocalDateTime字段上添加@JsonSerialize(using = LocalDateTimeConverter.class)注解，如下：  

```java
@JsonSerialize(using = LocalDateTimeConverter.class)
protected LocalDateTime gmtModified;
```

&emsp; 将LocalDateTime字段以指定格式化日期的方式返回给前端 在LocalDateTime字段上添加@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")注解即可，如下：  

```java
@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
protected LocalDateTime gmtModified;
```

&emsp; 对前端传入的日期进行格式化在LocalDateTime字段上添加@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")注解即可，如下：  

```java
@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
protected LocalDateTime gmtModified;
```




