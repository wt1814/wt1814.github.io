

# BeanUtils
## 集合拷贝问题  
<!-- 
BeanUtils 如何拷贝 List？
https://juejin.im/post/6844904046956904456#heading-4

MapStruct
https://mp.weixin.qq.com/s/Ya3EZCWvyzUbH6NiLn2BKw

Bean映射工具之Apache BeanUtils VS Spring BeanUtils 
https://mp.weixin.qq.com/s/dio08z4TqNBxoXqvZXTcjg
两难！到底用 Spring BeanUtils 还是 Apache BeanUtils？ 
https://mp.weixin.qq.com/s/xfyx5ux7VadCbOUrkzMVLQ

优雅的对象转换解决方案-MapStruct使用进阶 
https://mp.weixin.qq.com/s/eGSoYQC4E2NuvlHQzj_RHg
Java 浅拷贝性能大比拼，对象拷贝哪家强？ 
https://mp.weixin.qq.com/s/vWUnxd38RYX5_2nmc0ch5A

为什么阿里巴巴禁止使用Apache Beanutils进行属性的copy？ 
https://mp.weixin.qq.com/s/jRxR94jPcIvu4AoxkLEQOw

Java中的深浅拷贝问题你清楚吗？ 
https://mp.weixin.qq.com/s/Nw4h2KDNvDIsiq89zZUY4Q

-->

拷贝一组UerDO.java，是一个集合的时候就不能这样直接赋值了。如果还按照这种逻辑，如下：  

```java
@Test
public void listCopyFalse() {
    List<UserDO> userDOList = new ArrayList();
    userDOList.add(new UserDO(1L, "Van", 18, 1));
    userDOList.add(new UserDO(2L, "VanVan", 18, 2));
    List<UserVO> userVOList = new ArrayList();
    BeanUtils.copyProperties(userDOList, userVOList);
    log.info("userVOList:{}",userVOList);
}
```

日志打印如下：  

```text
.... userVOList:[]
```
通过日志可以发现，直接拷贝集合是无效的。  



