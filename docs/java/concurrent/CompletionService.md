

# CompletionService  
<!-- 
https://mp.weixin.qq.com/s/Eo-WR1agGETF0hE3Eaivrg
-->

&emsp; CompletionService可以看作FutureTask的一个进阶版，通过FutureTask+阻塞队列的方式能够按照线程执行完毕的顺序获取线程执行结果，起到聚合的目的，这个其实跟CountDownLatch差不多，如果你需要执行的线程次数是固定的且需要等待执行结果全部返回后统一处理，可以使用CompletionService。  


