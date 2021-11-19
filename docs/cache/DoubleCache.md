
# 双缓存  

<!-- 
多级缓存
https://mp.weixin.qq.com/s/02PsTTJl-90K888DEjReeQ

三层缓存架构
https://mp.weixin.qq.com/s/nhNht6La8rfPHTut2wMcew


-->

&emsp; 设置一级缓存和二级缓存，一级缓存过期时间短，二级缓存过期时间长或者不过期，一级缓存失效后访问二级缓存，同时刷新一级缓存和二级缓存。  
&emsp; 双缓存的方式不能将一级缓存和二级缓存中数据同时变成失效，当一级缓存失效后，有多个请求访问，彼此之间依然是竞争锁，抢到锁的线程查询数据库并刷新缓存，而其他没有抢到锁的线程，直接访问二级缓存（代码可以参考上文中的互斥锁），如图：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/problems/problem-20.png)  

