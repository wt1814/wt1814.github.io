


# 设计模式使用总结  
## 装饰器模式  
&emsp; 提供了比继承更有弹性的替代方案（扩展原有对象的功能）。装饰器模式的核心是功能拓展。使用装饰器模式可以透明且动态地扩展类的功能。  

&emsp; 项目中最初只有一个加解密方法，各种改造点，需求点，增加到了3个。  

```java
public interface DecryptService {

    /**
     * 报文信息解密 根据不同的实现类对应不同的参数个数args
     * args[0] 秘钥
     * args[1] CPI加解密向量值
     *
     * @throws Exception
     */
    String decrypt(String platformCode, String secCtx, String... args) throws Exception;

    String decryptOfGeneral(String biz, String msg, String secCtx, String... args) throws Exception;

    String decryptforOld(String platformCode, String secCtx, String... args) throws Exception;


}
```

&emsp; 剩余2个是不是可以改造成装饰器类呢？？？

