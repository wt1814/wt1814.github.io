
<!-- TOC -->

- [1. SpringMVC教程](#1-springmvc教程)
    - [1.1. SpringMVC注解](#11-springmvc注解)
        - [1.1.1. 地址映射](#111-地址映射)
            - [1.1.1.1. Content-Type介绍](#1111-content-type介绍)
        - [1.1.2. 绑定参数](#112-绑定参数)
            - [1.1.2.1. ★★★自定义转换](#1121-★★★自定义转换)
                - [1.1.2.1.1. 注解@DateTimeFormat、@JsonFormat](#11211-注解datetimeformatjsonformat)
                - [1.1.2.1.2. Converter，转换器](#11212-converter转换器)
                - [1.1.2.1.3. Formatter，格式化](#11213-formatter格式化)
        - [1.1.3. 返回模型和视图](#113-返回模型和视图)
    - [1.2. SpringMVC高级功能](#12-springmvc高级功能)
        - [1.2.1. SpringMVC中文乱码](#121-springmvc中文乱码)
        - [1.2.2. SpringMVC全局异常处理器](#122-springmvc全局异常处理器)
        - [1.2.3. SpringMVC拦截器](#123-springmvc拦截器)
        - [1.2.4. SpringMVC上传图片](#124-springmvc上传图片)
        - [1.2.5. SpringMVC解决跨域问题](#125-springmvc解决跨域问题)

<!-- /TOC -->

&emsp; 有些特殊数据类型无法直接进行数据绑定，必须先经过数据转换，例如日期。  


# 1. SpringMVC教程
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/SpringMVC/mvc-5.png)  

## 1.1. SpringMVC注解  
### 1.1.1. 地址映射  
&emsp; @RequestMapping、@GetMapping、@PostMapping、@PutMapping、@DeleteMapping、@PatchMapping  
&emsp; RequestMapping的定义：  

```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Mapping
public @interface RequestMapping {

    String name() default "";
    @AliasFor("path")
    String[] value() default {};
    @AliasFor("value")
    String[] path() default {};
    RequestMethod[] method() default {};
    String[] params() default {};
    String[] headers() default {};
    String[] consumes() default {};
    String[] produces() default {};
}
```

* value: 指定请求的实际地址， 如 /user/details/info；  
* method：指定请求的method类型， 如GET、POST、PUT、DELETE等；  
* consumes：指定处理请求的提交内容类型(Content-Type)，例如application/json、text/html等；  
* produces:  指定返回的内容类型，仅当request请求头中的(Accept)类型中包含该指定类型才返回；  
* params：指定request中必须包含某些参数值是，才让该方法处理请求；  
* headers：指定request中必须包含某些指定的header值，才能让该方法处理请求。  

&emsp; @RequestMapping中consumes和produces属性使用Content-Type进行过滤信息；headers中可以使用Content-Type进行过滤和判断。

#### 1.1.1.1. Content-Type介绍   
&emsp; MediaType即是Internet Media Type，互联网媒体类型；也叫做MIME类型。在Http协议消息头中，使用Content-Type来表示具体请求中的媒体类型信息。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/SpringMVC/springmvc-1.png)  

&emsp; 常见的媒体格式类型如下：  

    text/html ：HTML格式
    text/plain ：纯文本格式      
    text/xml ：  XML格式
    image/gif ：gif图片格式    
    image/jpeg ：jpg图片格式 
    image/png：png图片格式
&emsp; 以application开头的媒体格式类型：  

    application/xhtml+xml ：XHTML格式
    application/xml：XML数据格式
    application/atom+xml  ：Atom XML聚合格式    
    application/json ：JSON数据格式
    application/pdf ：pdf格式  
    application/msword  ：Word文档格式
    application/octet-stream ：二进制流数据(如文件下载)
    application/x-www-form-urlencoded ：<form encType="">中默认的encType，form表单数据被编码为key/value格式发送到服务器(表单默认的提交数据的格式)
    multipart/form-data ：需要在表单中进行文件上传时，就需要使用multipart/form-data 格式
&emsp; 以上就是在开发中经常会用到的Content-Type的内容格式。  

### 1.1.2. 绑定参数  
1. @RequestParam，@PathVariable，@RequestBody；  
2. 直接绑定POJO；
3. 绑定集合类List、Map、Set；
4. @ModelAttribute； 
5. @SessionAttributes；  

#### 1.1.2.1. ★★★自定义转换  
&emsp; 有些特殊数据类型无法直接进行数据绑定，必须先经过数据转换，例如日期。  
&emsp; 数据转换方式：1.硬编码(javaBean中日期类型为String)；2.SpringMVC自定义转换(javaBean中日期类型为Date)，需要自定义转换器(Converter)或格式化(Formatter)进行数据绑定。  

##### 1.1.2.1.1. 注解@DateTimeFormat、@JsonFormat  
&emsp; 在需要由string转Date的字段上加上@DateTimeFormat注解，代码如下：  

```java
@DateTimeFormat(pattern="yyyy-MM-dd")
private Date actionDate;
``` 

&emsp; 报错：Can not parse date while it seems to fit format 'yyyy-MM-dd'T'HH\:mm\:ss.SSS  
&emsp; 是因为在创建实体类的时候data默认的格式是：yyyy-MM-dd HH\:mm\:ss，但是在接收数据的时候默认的json格式是yyyy-MM-dd'T'HH\:mm\:ss.SSS。  
&emsp; 解决的办法是在实体类中加入注解：  

```java
@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
```

##### 1.1.2.1.2. Converter，转换器  
&emsp; Spring框架提供Converter将一种类型的对象转换为另一种类型的对象。自定义Converter类实现org.springframework.core.convert.converter.Converter接口。  
1. 接口代码如下：代码中，泛型中的S表示源类型，T表示目标类型，而convert(S source)表示接口中的方法。

    ```java
    public interface Converter<S, T> {
        T convert(S source);
    }
    ```
2. 日期转换类DateConverter代码如下所示：  

    ```java
    public class DateConverter implements Converter<String, Date> {
        //自定义日期格式
        private String datePattern = "yyyy-MM-dd HH:mm:ss";
        @Override
        public Date convert(String s) {
            //格式化日期
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);
            try {
                return simpleDateFormat.parse(s);
            } catch (ParseException e) {
                throw new IllegalArgumentException("无效的日期格式，请使用这种格式："+datePattern);
            }
        }
    }
    ```
3. 修改springmvc-config.xml文件，使用\<mvc:annotation-driven/>注解驱动加载处理器适配器，可以在此标签上进行配置，添加id为conversionService的Bean。  

    ```xml
        <!--配置注解驱动，显示的装配自定义类型转换器 -->
        <mvc:annotation-driven conversion-service="conversionService"/>
        <!-- 自定义类型转换器配置 -->
        <bean id="conversionService" class="org.springframework.context.support.ConversionServiceFactoryBean">
        <property name="converters">
            <set><bean class="com.ma.converter.DateConverter"/></set>
        </property>
        </bean>
    ```
4. 在controller包中新建一个日期控制器类DateConroller。  

    ```java
    @Controller
    public class DateController {
        @RequestMapping("/customeDate")
        public String customeDate(Date date) {
            System.out.println("date="+date);
            return "success";
        }
    }
    ```
&emsp; 地址栏访问：http://localhost:9999/date/test2?birthday=1990-01-02

##### 1.1.2.1.3. Formatter，格式化  
&emsp; Formatter与Converter的作用相同，只是Formatter的源类型必须是一个String类型，而Converter可以是任意类型。使用Formatter自定义转换器类需要实现org.springframework.format.Formatter接口。接口代码如下：  

    public interface Formatter<T> extends Printer<T>, Parser<T> {  }

&emsp; Formatter接口继承了Printer和Parser接口，其泛型T表示输入字符串要转换的目标类型。在Printer和Parser接口中，分别包含一个print()和parse()方法，所有的实现类必须覆盖这两个方法。

1. 在converter包中新建一个日期转换类DateFormatter，代码如下：  

    ```java
    public class DateFormatter implements Formatter<Date> {
        //定义日期格式
        private String datePattern = "yyyy-MM-dd HH:mm:ss";
        //声明SimpleDateFormat对象
        SimpleDateFormat simpleDateFormat;

        @Override
        public Date parse(String s, Locale locale) throws ParseException {
            simpleDateFormat = new SimpleDateFormat(datePattern);
            return simpleDateFormat.parse(s);
        }

        @Override
        public String print(Date date, Locale locale) {
            return new SimpleDateFormat().format(date);
        }
    }
    ```
2. 在配置文件中注册：  

    ```xml
    <!-- 自定义类型格式化转换器配置 -->
    <bean id="conversionService" class="org.springframework.format.support.FormattingConversionServiceFactoryBean">
        <property name="formatters">
            <set><bean class="com.itheima.convert.DateFormatter" /></set>
        </property>
    </bean>
    ```
&emsp; 运行结果和Converter的一样。  

### 1.1.3. 返回模型和视图  
&emsp; SpringMVC处理方法支持如下的返回方式：ModelAndView, Model, ModelMap, Map,View, String, void，ResponseEntity(作用比较强大,可以返回文件，字符串等)。无论何种方式都要清楚其返回的视图(或调用另一个controller，即转发和重定向的使用，涉及到怎么向另一个controller传参数)和模型。返回json字符串时使用@responseBody注解。  

## 1.2. SpringMVC高级功能  
### 1.2.1. SpringMVC中文乱码  
1. 解决POST请求乱码问题：在 web.xml 中配置一个CharacterEncodingFilter过滤器，设置成utf-8；  
2. GET请求中文参数出现乱码解决方法有两个：  
    * 修改 tomcat 配置文件添加编码与工程编码一致，如下：  

        ```xml
        <ConnectorURIEncoding="utf-8" connectionTimeout="20000" port="8080" protocol="HTTP/1.1" redirectPort="8443"/>
        ```
    * 对参数进行重新编码：  
    
        ```java
        String userName = new String(request.getParamter("userName").getBytes("ISO8859-1"),"utf-8")
        ```

### 1.2.2. SpringMVC全局异常处理器  
&emsp; 参考[异常处理](/docs/java/basis/JavaException.md)  

### 1.2.3. SpringMVC拦截器  
&emsp; 参考[过滤器、拦截器、监听器](docs/web/subassembly.md)  

### 1.2.4. SpringMVC上传图片  
&emsp; ....

### 1.2.5. SpringMVC解决跨域问题  
&emsp; ...
