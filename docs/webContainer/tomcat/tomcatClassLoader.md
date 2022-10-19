


# tomcat类加载器
<!-- 

https://blog.csdn.net/Dwade_mia/article/details/80140585
-->

&emsp; 根据实际的应用场景，分析下 tomcat 类加载器需要解决的几个问题

* 为了避免类冲突，每个 webapp 项目中各自使用的类库要有隔离机制
* 不同 webapp 项目支持共享某些类库
* 类加载器应该支持热插拔功能，比如对 jsp 的支持、webapp 的 reload 操作

&emsp; 为了解决以上问题，tomcat设计了一套类加载器，如下图所示。在 Tomcat 里面最重要的是 Common 类加载器，它的父加载器是应用程序类加载器，负责加载 ${catalina.base}/lib、${catalina.home}/lib 目录下面所有的 .jar 文件和 .class 文件。下图的虚线部分，有 catalina 类加载器、share 类加载器，并且它们的 parent 是 common 类加载器，默认情况下被赋值为 Common 类加载器实例，即 Common 类加载器、catalina 类加载器、 share 类加载器都属于同一个实例。当然，如果通过修改 catalina.properties 文件的 server.loader 和 shared.loader 配置，从而指定其创建不同的类加载器  

![image](http://www.wt1814.com/static/view/images/tomcat/tomcat-1.png)  


