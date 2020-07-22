


&emsp; 接口的响应时间过长，你会怎么办？（此处只针对最简单的场景，抛开STW那些复杂的问题。）以下是我目前想到的：  
1. 异步化（Runnable、Future）  
2. 缓存 
3. 并行（ForkJoinPool、CyclicBarrier）  
4. 干掉锁  

&emsp; 总结一句话：xxxxx，删代码，删到只有Controller，然后拼个返回值。一步搞定响应时间问题。  

