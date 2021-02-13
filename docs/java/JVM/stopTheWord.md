


# Stop the Word

<!-- 
https://blog.csdn.net/u011918260/article/details/70047159?utm_medium=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-11.control&depth_1-utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-11.control
-->
&emsp; Java中Stop-The-World机制简称STW，是在执行垃圾收集时，Java应用程序的其他所有线程都被挂起(除了垃圾收集帮助器之外)。Java中一种全局暂停现象，全局停顿，所有Java代码停止，native代码可以执行，但不能与JVM交互；这些现象多半是由于gc引起。  
&emsp; GC时的Stop the World(STW)是Java开发最大的敌人。除了GC，JVM下还会发生停顿现象。  
&emsp; JVM里有一条特殊的线程－－VM Threads，专门用来执行一些特殊的VM Operation，比如分派GC，thread dump等，这些任务都需要整个Heap，以及所有线程的状态是静止的、一致的才能进行。所以JVM引入了安全点(Safe Point)的概念，想办法在需要进行VM Operation时，通知所有的线程进入一个静止的安全点。  

<!-- 
&emsp; 除了GC，其他触发安全点的VM Operation包括：  
1. JIT相关，比如Code deoptimization， Flushing code cache ；  
2. Class redefinition(e.g. javaagent，AOP代码植入的产生的instrumentation) ；  
3. Biased lock revocation 取消偏向锁 ；  
4. Various debug operation (e.g. thread dump or deadlock check)；  
-->
