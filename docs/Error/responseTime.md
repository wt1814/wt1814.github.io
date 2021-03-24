
&emsp; 接口的响应时间过长，你会怎么办？（此处只针对最简单的场景，抛开STW那些复杂的问题。）以下是我目前想到的：  
1. 异步化（Runnable、Future）  
2. 缓存  
3. 并行（ForkJoinPool、CyclicBarrier）  
4. 干掉锁（空间换时间）  

&emsp; 一句话总结：xxxxx，删代码，删到只有Controller，然后拼个返回值。一步搞定响应时间问题。  



<!-- 

杜绝假死，Tomcat容器设置最大连接数
https://blog.csdn.net/binglong_world/article/details/80748520

http是有保持长连接的
https://blog.csdn.net/vterdfmqj/article/details/54848225

httpclient使用不当产生大量CLOSE_WAIT的解决过程
https://www.jianshu.com/p/83bbeb33ed20
-->
