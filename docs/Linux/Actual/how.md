
<!-- TOC -->

- [1. 如何学习Linux性能优化？](#1-如何学习linux性能优化)
    - [1.1. 性能指标是什么？](#11-性能指标是什么)
    - [1.2. 学习的重点是什么？](#12-学习的重点是什么)

<!-- /TOC -->

# 1. 如何学习Linux性能优化？  
&emsp; 《Linux性能优化实战》—倪朋飞  

## 1.1. 性能指标是什么？  
&emsp; 学习性能优化的第一步，一定是了解“性能指标”这个概念。  
&emsp; 当看到性能指标时，你会首先想到什么呢？我相信“高并发”和“响应快”一定是最先出现在你脑海里的两个词，而它们也正对应着性能优化的两个核心指标——“吞吐”和“延时”。这两个指标是从应用负载的视角来考察性能，直接影响了产品终端的用户体验。跟它们对应的，是从系统资源的视角出发的指标，比如资源使用率、饱和度等。  
![image](http://182.92.69.8:8081/img/Linux/Actual/actual-1.png)  
&emsp; 我们知道，随着应用负载的增加，系统资源的使用也会升高，甚至达到极限。而性能问题的本质，就是系统资源已经达到瓶颈，但请求的处理却还不够快，无法支撑更多的请求。  
&emsp; 性能分析，其实就是找出应用或系统的瓶颈，并设法去避免或者缓解它们，从而更高效地利用系统资源处理更多的请求。这包含了一系列的步骤，比如下面这六个步骤。  

* 选择指标评估应用程序和系统的性能；  
* 为应用程序和系统设置性能目标；  
* 进行性能基准测试；  
* 性能分析定位瓶颈；  
* 优化系统和应用程序；  
* 性能监控和告警。  

&emsp; 了解了这些性能相关的基本指标和核心步骤后，该怎么学呢？接下来，我来说说要学好 Linux 性能优化的几个重要问题。


## 1.2. 学习的重点是什么？  
&emsp; 想要学习好性能分析和优化，建立整体系统性能的全局观是最核心的话题。因而，  

* 理解最基本的几个系统知识原理；  
* 掌握必要的性能工具；  
* 通过实际的场景演练，贯穿不同的组件。  

&emsp; 这三点，就是我们学习的重中之重。我会在专栏的每篇文章中，针对不同场景，把这三个方面给你讲清楚，你也一定要花时间和心思来消化它们。  
&emsp; 其实说到性能工具，就不得不提性能领域的大师布伦丹·格雷格（Brendan Gregg）。他不仅是动态追踪工具 DTrace 的作者，还开发了许许多多的性能工具。我相信你一定见过他所描绘的 Linux 性能工具图谱：  
![image](http://182.92.69.8:8081/img/Linux/Actual/actual-2.png)  
&emsp; 这个图是 Linux 性能分析最重要的参考资料之一，它告诉你，在 Linux 不同子系统出现性能问题后，应该用什么样的工具来观测和分析。  
&emsp; 比如，当遇到 I/O 性能问题时，可以参考图片最下方的 I/O 子系统，使用 iostat、iotop、blktrace 等工具分析磁盘 I/O 的瓶颈。你可以把这个图保存下来，在需要的时候参考查询。  
&emsp; 另外，我还要特别强调一点，就是性能工具的选用。有句话是这么说的，一个正确的选择胜过千百次的努力。虽然夸张了些，但是选用合适的性能工具，确实可以大大简化整个性能优化过程。在什么场景选用什么样的工具、以及怎么学会选择合适工具，都是我想教给你的东西。  
&emsp; 但是切记，千万不要把性能工具当成学习的全部。工具只是解决问题的手段，关键在于你的用法。只有真正理解了它们背后的原理，并且结合具体场景，融会贯通系统的不同组件，你才能真正掌握它们。  
&emsp; 最后，为了让你对性能有个全面的认识，我画了一张思维导图，里面涵盖了大部分性能分析和优化都会包含的知识，专栏中也基本都会讲到。你可以保存或者打印下来，每学会一部分就标记出来，记录并把握自己的学习进度。  
![image](http://182.92.69.8:8081/img/Linux/Actual/actual-3.png)  

