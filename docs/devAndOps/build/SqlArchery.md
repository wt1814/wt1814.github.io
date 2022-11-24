
<!-- TOC -->

- [1. mysql审核平台Archery](#1-mysql审核平台archery)
    - [1.1. 搭建](#11-搭建)
    - [1.2. 配置](#12-配置)
        - [1.2.1. 添加数据库](#121-添加数据库)
            - [1.2.1.1. 添加资源组](#1211-添加资源组)
            - [1.2.1.2. 添加实例](#1212-添加实例)
        - [1.2.2. 用户](#122-用户)
            - [1.2.2.1. 权限组管理](#1221-权限组管理)
            - [1.2.2.2. 用户管理](#1222-用户管理)
        - [1.2.3. 设置工单上线](#123-设置工单上线)

<!-- /TOC -->


# 1. mysql审核平台Archery
<!-- 
https://gitee.com/rtttte/Archery?utm_source=alading&utm_campaign=repo

https://blog.csdn.net/zhengchaooo/article/details/108275185

https://archerydms.com/
-->


## 1.1. 搭建  



## 1.2. 配置  
<!-- 

https://github.com/hhyo/Archery/wiki
-->


### 1.2.1. 添加数据库  
#### 1.2.1.1. 添加资源组  

#### 1.2.1.2. 添加实例

### 1.2.2. 用户
#### 1.2.2.1. 权限组管理  
1. 新增权限组“开发”和“管理”   


#### 1.2.2.2. 用户管理  


### 1.2.3. 设置工单上线  
https://github.com/hhyo/archery/wiki/sql_check#SQL%E4%B8%8A%E7%BA%BF  
MySQL支持比较完整的SQL审核功能，且依赖Inception/goInception工具，oracle、mongodb支持部分审核规则，其他数据库仅支持语句切分和执行，不做审核  

系统管理 ---> 配置项管理 ---> goInception配置（https://github.com/hhyo/archery/wiki/configuration#goInception%E9%85%8D%E7%BD%AE）（目前配置的有问题）  
系统管理 ---> 配置项管理 ---> 工单审核流配置   


