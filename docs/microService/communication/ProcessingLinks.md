



# 服务器处理连接
<!-- 

 服务器处理连接的架构演变 
 https://server.51cto.com/article/662118.html
-->

## 单进程accept


## 多进程模式

### 主进程accept，子进程处理请求


### 子进程accept

## 进程池模式  

## 多线程模式
&emsp; 多线程模式和多进程模式是类似的，也是分为下面几种  

1. 主进程accept，创建子线程处理
2. 子线程accept
3. 线程池


