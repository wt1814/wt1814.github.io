
<!-- TOC -->

- [通配符](#通配符)
- [占位符](#占位符)

<!-- /TOC -->

# 通配符  
&emsp; 通配符包括星号“*”和问号“？”。星号表示匹配的数量不受限制，而后者的匹配字符数则受到限制。  

# 占位符  
<!-- 
 String.format() 图文详解，写得非常好！ 
 https://mp.weixin.qq.com/s/r5l93TERuT1hBDbU2WlDpg
-->

&emsp; 两种java中的占位符的使用：  
&emsp; 第一种：使用%s占位，使用String.format转换  

```java
public class Test {
    public static void main(String[] args) {
        String url = "我叫%s,今年%s岁。";
        String name = "小明";
        String age = "28";
        url = String.format(url,name,age);
        System.out.println(url);
    }
}
```
&emsp; 控制台输出：  

    我叫小明，年28岁。
 
&emsp; 第二种：使用{1}占位，使用MessageFormat.format转换  

```java
public class Test {
    public static void main(String[] args) {
        String url02 = "我叫{0},今年{1}岁。";
        String name = "小明";
        String age = "28";
        url02 = MessageFormat.format(url02,name,age);
        System.out.println(url02);
    }
}
```
&emsp; 控制台同样输出：  

    我叫小明，今年28岁。

