

# ENTRYPOINT和CMD区别与联系

Dockerfile中RUN，CMD和ENTRYPOINT都能够用于执行命令，下面是三者的主要用途：

RUN命令执行命令并创建新的镜像层，通常用于安装软件包
CMD命令设置容器启动后默认执行的命令及其参数，但CMD设置的命令能够被docker run命令后面的命令行参数替换
ENTRYPOINT配置容器启动时的执行命令（不会被忽略，一定会被执行，即使运行 docker run时指定了其他命令）

<!-- 

ENTRYPOINT 和 CMD 区别与联系
https://zhuanlan.zhihu.com/p/582176283
https://blog.csdn.net/MssGuo/article/details/126348571

-->
