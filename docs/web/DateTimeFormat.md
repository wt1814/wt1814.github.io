

# @DateTimeFormat和@jsonFormat

<!-- 
@DateTimeFormat格式化时间出错
http://www.javashuo.com/article/p-qxvjmezb-pz.html
-->

```java
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",locale = "zh",timezone = "GMT+8")
```

