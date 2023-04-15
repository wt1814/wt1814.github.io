

<!-- TOC -->

- [1. 延迟队列](#1-延迟队列)
    - [1.1. 背景](#11-背景)
    - [1.2. 解决方案](#12-解决方案)

<!-- /TOC -->


# 1. 延迟队列  
<!-- 
延迟队列
https://mp.weixin.qq.com/s/zLrsYLzq0tO2qaDKN02Ahw

https://www.cnblogs.com/xiaowei123/p/13222710.html
https://blog.csdn.net/why15732625998/article/details/104890079/

 延迟队列实现，定时任务，关闭订单 
https://mp.weixin.qq.com/s/XtjPANZhbgvDYz06Q41CgQ
延时队列实现的几种姿势
https://mp.weixin.qq.com/s/AElHaWWbbUp1UMHxZZPMtQ
你真的知道怎么实现一个延迟队列吗
https://mp.weixin.qq.com/s/DcyXPGxXFYcXCQJII1INpg

一口气说出 6种 延时队列的实现方案
https://my.oschina.net/u/4455409/blog/4714151

延时队列实现的几种姿势
https://mp.weixin.qq.com/s/Xs9J0wy16XILRAt7myaNAw

https://blog.csdn.net/deel_feel/article/details/91872366
https://zhuanlan.zhihu.com/p/104285758

 延时任务(三)-基于redis zset的完整实现 
https://mp.weixin.qq.com/s/-KcM9XhIbff-OiVt8c67bA

-->

## 1.1. 背景
&emsp; 看看以下业务场景：  

* 在淘宝、京东等购物平台上下单，超过一定时间未付款，订单会自动取消。
* 打车的时候，在规定时间没有车主接单，平台会取消你的单并提醒你暂时没有车主接单。
* 点外卖的时候，如果商家在10分钟还没接单，就会自动取消订单。
* 收快递的时候，如果我们没有点确认收货，在一段时间后程序会自动完成订单。
* 在平台完成订单后，如果没有在规定时间评论商品，会自动默认买家不评论。
* .......

## 1.2. 解决方案

* 最简单的方式，定时扫表。例如对于订单支付失效要求比较高的，每2S扫表一次检查过期的订单进行主动关单操作。优点是简单，缺点是每分钟全局扫表，浪费资源，如果遇到表数据订单量即将过期的订单量很大，会造成关单延迟。
* 使用RabbitMq或者其他MQ改造实现延迟队列，优点是，开源，现成的稳定的实现方案，缺点是：MQ是一个消息中间件，如果团队技术栈本来就有MQ，那还好，如果不是，那为了延迟队列而去部署一套MQ成本有点大
* 使用Redis的zset、list的特性，我们可以利用redis来实现一个延迟队列RedisDelayQueue

