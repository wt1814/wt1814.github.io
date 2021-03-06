


# ~~高并发系统三高~~  

<!--
 如何模拟超过 5 万用户的并发访问？ 
https://mp.weixin.qq.com/s/CuA98cbQOkYLdT4FUIolVQ
面试官再问高并发
https://blog.csdn.net/youanyyou/article/details/107075116
-->

&emsp; 高并发绝不意味着只追求高性能，这是很多人片面的理解。从宏观角度看，高并发系统设计的目标有三个：高性能、高可用，以及高可扩展。  
1. **<font color = "red">`高性能`：性能体现了系统的并行处理能力，在有限的硬件投入下，提高性能意味着节省成本。**</font> 同时，性能也反映了用户体验，响应时间分别是100毫秒和1秒，给用户的感受是完全不同的。  
2. **<font color = "clime">`高可用`：表示系统可以正常服务的时间。</font>** 一个全年不停机、无故障；另一个隔三差五出线上事故、宕机，用户肯定选择前者。另外，如果系统只能做到90%可用，也会大大拖累业务。  
3. **<font color = "clime">`高扩展`：表示系统的扩展能力，流量高峰时能否在短时间内完成扩容，更平稳地承接峰值流量，比如双11活动、明星离婚等热点事件。</font>**  


![image](https://gitee.com/wt1814/pic-host/raw/master/images/system/availab/system-11.png)  

&emsp; 这3个目标是需要通盘考虑的，因为它们互相关联、甚至也会相互影响。  
&emsp; 比如说：考虑系统的扩展能力，会将服务设计成无状态的，这种集群设计保证了高扩展性，其实也间接提升了系统的性能和可用性。  
&emsp; 再比如说：为了保证可用性，通常会对服务接口进行超时设置，以防大量线程阻塞在慢请求上造成系统雪崩，那超时时间设置成多少合理呢？一般，我们会参考依赖服务的性能表现进行设置。  
