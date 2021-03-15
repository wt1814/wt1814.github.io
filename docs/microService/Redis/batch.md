

<!-- TOC -->

- [1. redis批量操作数据](#1-redis批量操作数据)
    - [1.1. 使用scan代替keys指令](#11-使用scan代替keys指令)
    - [1.2. ~~Redis中的批量删除数据库中的Key~~](#12-redis中的批量删除数据库中的key)

<!-- /TOC -->


# 1. redis批量操作数据  

## 1.1. 使用scan代替keys指令  
&emsp; ......
<!-- 
在RedisTemplate中使用scan代替keys指令 
https://mp.weixin.qq.com/s/8hBrUb1Tn6cuSzQITCDReQ
-->

## 1.2. ~~Redis中的批量删除数据库中的Key~~  
<!-- 
https://www.cnblogs.com/kiko2014551511/p/11531584.html

https://www.cnblogs.com/DreamDrive/p/5772198.html
 熬了一个通宵终于把Key删完了 
 https://mp.weixin.qq.com/s/xb6USb3FLIDDloUPoqBnMw
-->
&emsp; Redis中有删除单个Key的指令 DEL，但似乎没有批量删除 Key 的指令。   

1. 借助 Linux 的 xargs 指令来完成这个动作。   
2. 使用管道命令。