

<!-- TOC -->

- [1. 多数据源](#1-多数据源)
    - [1.1. Spring多数据源](#11-spring多数据源)
        - [1.1.1. 静态](#111-静态)
        - [1.1.2. 动态](#112-动态)
        - [1.1.3. 增加事务处理](#113-增加事务处理)
    - [1.2. JTA事务](#12-jta事务)

<!-- /TOC -->



# 1. 多数据源  
<!-- 
Spring 动态切换、添加数据源实现以及源码浅析
https://mp.weixin.qq.com/s/8O7uHAh03gCkgFE8arF-1A

SpringBoot + Mybatis配合AOP和注解实现动态数据源切换配置
https://mp.weixin.qq.com/s?__biz=MzI4Njc5NjM1NQ==&mid=2247489779&idx=2&sn=fa16447df368df0bc837358eaa7f31c9&chksm=ebd627dfdca1aec9cd3a514db47454bc4beef77f3c104637e98899e40e4473b0ce8aec66e492&mpshare=1&scene=1&srcid=&sharer_sharetime=1569341908826&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=20f7b87cb3d4d9a8845250dbbd99647f7cc2151918584db1e8c32b58f2f32e1af9ed35a908eca0422d6254ba8be59333ea835ebcfb158738bf01f77a36c80c5f42461874a45016ebdae9bb8241e3666e&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=%2BeyejsSYMk60ZUY%2FpK4YBKveSPIi8GUzrQvyveBksIipCvi8KhPkraBA4Eyx2jY%2F

-->

## 1.1. Spring多数据源  

### 1.1.1. 静态
<!-- 
https://juejin.im/post/5d773babe51d4561ba48fe68

-->

### 1.1.2. 动态  
<!-- 
https://mp.weixin.qq.com/s?__biz=MzI4Njc5NjM1NQ==&mid=2247489779&idx=2&sn=fa16447df368df0bc837358eaa7f31c9&chksm=ebd627dfdca1aec9cd3a514db47454bc4beef77f3c104637e98899e40e4473b0ce8aec66e492&mpshare=1&scene=1&srcid=&sharer_sharetime=1569341908826&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=20f7b87cb3d4d9a8845250dbbd99647f7cc2151918584db1e8c32b58f2f32e1af9ed35a908eca0422d6254ba8be59333ea835ebcfb158738bf01f77a36c80c5f42461874a45016ebdae9bb8241e3666e&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=%2BeyejsSYMk60ZUY%2FpK4YBKveSPIi8GUzrQvyveBksIipCvi8KhPkraBA4Eyx2jY%2F

https://mp.weixin.qq.com/s/8O7uHAh03gCkgFE8arF-1A

https://blog.csdn.net/qq_35830949/article/details/80885745
-->

### 1.1.3. 增加事务处理  



## 1.2. JTA事务  


