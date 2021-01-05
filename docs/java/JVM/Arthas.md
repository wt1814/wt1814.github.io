

# Arthas
<!-- 
Java线上问题排查神器Arthas快速上手与原理浅谈 
https://mp.weixin.qq.com/s/s3qtr5hpB7Q3-Tu60R8gxA

还在为 Arthas 命令头疼？ 来看看这个插件吧！ 
https://mp.weixin.qq.com/s/OZT1wIfmzSa5TiMIbmZ5aQ
-->
&emsp; 官方文档：http://arthas.gitee.io/  
&emsp; Arthas(阿尔萨斯)是Alibaba开源的Java诊断工具，深受开发者喜爱。  
&emsp; 当你遇到以下类似问题而束手无策时，Arthas可以帮助你解决：  
1. 这个类从哪个 jar 包加载的？为什么会报各种类相关的 Exception？  
1. 我改的代码为什么没有执行到？难道是我没 commit？分支搞错了？  
1. 遇到问题无法在线上 debug，难道只能通过加日志再重新发布吗？  
1. 线上遇到某个用户的数据处理有问题，但线上同样无法 debug，线下无法重现！  
1. 是否有一个全局视角来查看系统的运行状况？  
1. 有什么办法可以监控到JVM的实时运行状态？  
1. 怎么快速定位应用的热点，生成火焰图？  

&emsp; Arthas支持JDK 6+，支持Linux/Mac/Windows，采用命令行交互模式，同时提供丰富的 Tab 自动补全功能，进一步方便进行问题的定位和诊断。  

