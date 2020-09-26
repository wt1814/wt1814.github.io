

<!-- TOC -->

- [1. Linux命令](#1-linux命令)
    - [1.1. 文件管理](#11-文件管理)
    - [1.2. 文档编辑](#12-文档编辑)
    - [1.3. 文件传输](#13-文件传输)
    - [1.4. 备份压缩](#14-备份压缩)
    - [1.5. 管理用户](#15-管理用户)
    - [1.6. 进程管理](#16-进程管理)
    - [1.7. 网络通讯](#17-网络通讯)

<!-- /TOC -->

# 1. Linux命令  

<!-- 
P47
-->
&emsp; shell提供了特殊字符来帮助快速指定一组文件名。这些特殊字符叫做通配符。  

## 1.1. 文件管理
![image](https://gitee.com/wt1814/pic-host/raw/master/images/Linux/Linux/linux-1.png)   

* df：显示磁盘使用情况。  
* du：显示文件系统使用情况。

&emsp; 文件和目录的操作

* ls，显示文件和目录列表  
* cd，切换目录  
* pwd，显示当前工作目录  
* mkdir，创建目录  
* rmdir，删除空目录  
* touch，生成一个空文件或更改文件的时间  
* cp，复制文件或目录  
* mv，移动文件或目录、文件或目录改名  
* ln，建立链接文件  
* find，查找文件  
* file/stat，查看文件类型或文件属性信息  
* echo，把内容重定向到指定的文件中 ，有则打开，无则创建  
* 管道命令 |，将前面的结果给后面的命令，例如：\`ls -la | wc `，将ls的结果加油wc命令来统计字数  
* 重定向 > 是覆盖模式，>> 是追加模式，例如：\`echo "Java3y,zhen de hen xihuan ni" > qingshu.txt `把左边的输出放到右边的文件里去  

&emsp; 查看文件  

* cat，查看文本文件内容  
* more，可以分页看  
* less，不仅可以分页，还可以方便的搜索，回翻等操作  
* tail -10，查看文件等尾部的10行  
* head -20，查看文件的头部20行  

&emsp; 权限管理  

* chmod，改变文件或目录的权限
* chown，改变文件或目录的属主（所有者）
* chgrp，改变文件或目录所属的组：
* umask，设置文件的缺省生成掩码：
* 文件扩展属性
	* 显示扩展属性：lsattr \[-adR] [文件|目录]
	* 修改扩展属性：chattr \[-R] [[-+=][属性]] <文件|目录>

## 1.2. 文档编辑  
* vim
* grep
* sed

![image](https://gitee.com/wt1814/pic-host/raw/master/images/Linux/Linux/linux-2.png)   


## 1.3. 文件传输  

<!--
Linux 上几种常用的文件传输方式 
https://mp.weixin.qq.com/s?__biz=MzU3NTgyODQ1Nw==&mid=2247486417&idx=1&sn=37be3b122f792b313d17cecce3c93291&chksm=fd1c7357ca6bfa415bb0a728986b1e9cc540194d60f95b4c537dd23c92086f4327082649760b&mpshare=1&scene=1&srcid=&sharer_sharetime=1564966405212&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=ecc4386bb884a7b15726e01906103213f69859b70aae98dd60e850fab62274c372e9f641e98e5b558225391ac3a71c7fe63e25bce0cac2c43166b704f6798cf5b611e8b9ff49a69a407482e74b53b4dc&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060834&lang=zh_CN&pass_ticket=hJ%2BSizr3tqhAq3hRq1pBmqb7SdoK%2FKh9k69UgvR0%2BAneIZhpFGHVYOqPaiRuo7Bc
-->

* ftp
* rcp
* scp
* wget
* curl
* rsync


## 1.4. 备份压缩  

<!-- 
Linux下查看压缩文件内容的 10 种方法 
https://mp.weixin.qq.com/s?__biz=MzU3NTgyODQ1Nw==&mid=2247487056&idx=2&sn=d84364a84306d9be2473ccd27cad8208&chksm=fd1c76d6ca6bffc03a97141cc39c709aaf96048cca599ec5c159fa7a43a8e356615bafe57897&mpshare=1&scene=1&srcid=&sharer_sharetime=1571788923905&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=f8a21a8df9909cbbfe1a5b69713bb2cf85ccf4e14a2f73ef27c0a6716bbf2d1aa63abe2f93e0426442d17b1f77e1881349fa3e6ce6bb5fc1e5ebbb0d88d0177cfb5d959b0cb36b2b5438bbcfd15d63a6&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070152&lang=zh_CN&pass_ticket=1pFLt59KHGlppvK5eZcMMThpSNCZs2HFk1wvvdkIRG%2BugtBpiQp3toK8kWvae6zE
-->

* 压缩	
	* gzip filename
	* bzip2 filename
	* tar -czvf filename
* 解压
	* gzip -d filename.gz
	* bzip2 -d filename.bz2
	* tar -xzvf filename.tar.gz



## 1.5. 管理用户  


* 切换用户
	* su
	* sudo
* 用户管理  
	* useradd，添加用户 
	* usermod，修改用户
	* userdel，删除用户
* 组管理
	* groupadd，添加组
	* groupmod，修改组
	* groupdel，删除组
* 批量管理用户
	* newusers，成批添加/更新一组账户
	* chpasswd，成批更新用户的口令
* 组成员管理
	* 向标准组中添加用户
		* gpasswd -a <用户账号名> <组账号名>
		* usermod -G <组账号名> <用户账号名>
	* 从标准组中删除用户，gpasswd -d <用户账号名> <组账号名>
* 口令管理
	* 口令时效设置，修改 /etc/login.defs 的相关配置参数。
	* 口令维护(禁用、恢复和删除用户口令)，passwd
	* 设置已存在用户的口令时效，change
* 用户相关的命令
	* id，显示用户当前的uid、gid和用户所属的组列表
	* groups，显示指定用户所属的组列表
	* whoami，显示当前用户的名称
	* w/who，显示登录用户及相关信息
	* newgrp，用于转换用户的当前组到指定的组账号，用户必须属于该组才可以正确执行该命令

## 1.6. 进程管理  

* ps，查找出进程的信息
* nice和renice，调整进程的优先级
* kill，杀死进程
* free，查看内存使用状况
* top，查看实时刷新的系统进程信息
* 作业管理  
	* jobs，列举作业号码和名称
	* bg，在后台恢复运行
	* fg，在前台恢复运行
	* ctrl+z，暂时停止某个进程
* 自动化任务  
	* at
	* cron
* 管理守护进程
	* chkconfig
	* service 
	* ntsysv  


## 1.7. 网络通讯  

<!-- 
面试官问我Linux下常见网络命令 
https://mp.weixin.qq.com/s/uMK6QnJKK2MFERkfx6b-QA
Linux网络流量监控工具 
https://mp.weixin.qq.com/s?__biz=MzU0NjEwMTg4Mg==&mid=2247485997&idx=2&sn=ac694beaf60cbf18d133eb6ebbc7a345&chksm=fb638538cc140c2e581b8c673a9c529359042a2262ec112f8c3476c7408f25e04856f0b37bbd&scene=21#wechat_redirect
-->
  
* 网络接口相关
	* ifconfig，查看网络接口信息
	* ifup/ifdown，开启或关闭接口

* 临时配置相关
	* route命令，可以临时地设置内核路由表
	* hostname命令，可以临时地修改主机名
	* sysctl命令，可以临时地开启内核的包转发
	* ifconfig命令，可以临时地设置网络接口的IP参数
* 网络检测的常用工具
	* ifconfig，检测网络接口配置
	* route，检测路由配置
	* ping，检测网络连通性
	* netstat，查看网络状态
	* lsof，查看指定IP 和/或 端口的进程的当前运行情况
	* host/dig/nslookup，检测DNS解析
	* traceroute，检测到目的主机所经过的路由器
	* tcpdump，显示本机网络流量的状态
* 安装软件
	* yum
	* rpm
	* wget


