
<!-- TOC -->

- [1. git](#1-git)
    - [1.1. Git分支命名](#11-git分支命名)
    - [1.2. GIT本地库操作基本原理](#12-git本地库操作基本原理)
    - [1.3. 分支](#13-分支)
        - [1.3.1. 操作分支](#131-操作分支)
        - [1.3.2. 基于Gitlab Issues为导向的分支管理](#132-基于gitlab-issues为导向的分支管理)
    - [1.4. 命令](#14-命令)
        - [1.4.1. git clone 文件大](#141-git-clone-文件大)
        - [1.4.2. git tag](#142-git-tag)
        - [1.4.3. ★★★git回滚](#143-★★★git回滚)
            - [场景](#场景)
            - [撤销，commit](#撤销commit)
            - [回滚，push](#回滚push)
            - [删除某次提交](#删除某次提交)
    - [1.5. gitignore规则不生效](#15-gitignore规则不生效)
    - [1.6. octotree，树形展示Github项目代码](#16-octotree树形展示github项目代码)
    - [1.7. git迁移仓库](#17-git迁移仓库)

<!-- /TOC -->

# 1. git

<!--
Git 实用技巧记录 
https://mp.weixin.qq.com/s/vQ5uzwGmvvI844Ehj2iZ9w

用21张图，把Git 工作原理彻底说清楚 
https://mp.weixin.qq.com/s/tzq0dBTSqpp-V89L5Y1IOg

git clone时报RPC failed; curl 18 transfer closed with outstanding read data remaining
https://www.cnblogs.com/zjfjava/p/10392150.html

git书籍  
https://mp.weixin.qq.com/s/bT7VXffqHuzUZUY5c4ce7A
 如何自动同步博客到 Github 主页？ 
 https://mp.weixin.qq.com/s/J2sIku38WxL4ge4W5DP2hw
-->

<!-- 本地仓库的位置 -->
本地仓库的位置可设置为：\<localRepository\>${user.home}/.m2/repository\</localRepository\>

## 1.1. Git分支命名  
<!-- 
 别乱提交代码了，你必须知道的 Git 分支开发规范！ 
 https://mp.weixin.qq.com/s/w5gcDgQKYFmzel6Jnc0u4A
-->

## 1.2. GIT本地库操作基本原理  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/projectManage/git/git-4.png)  
&emsp; GIT作为分布式版本库软件，每个机器上都是一个版本库。  
&emsp; git初始化后，有三个区，分别是 工作区，暂存区，本地库；  

* 工作区是编辑代码的区别，包括新增，修改，删除代码操作，编辑代码后，添加到暂存区；  
* 暂存区是临时存储代码的地方，方便批量提交数据到本地库；  
* 本地库是最终的历史版本文件的存储地；  

## 1.3. 分支
<!-- 
https://jingyan.baidu.com/article/a17d52854e164dc098c8f2b0.html
-->
### 1.3.1. 操作分支
&emsp; 查看分支：  
* 查看本地分支：git branch
* 查看远程分支：git branch -r


&emsp; 删除分支：  
* 删除本地分支tmp：git branch -D tmp(分支名称)  
* 删除远程分支tmp：git push origin --delete tmp(分支名称) 
* Git删除在本地有但在远程库中已经不存在的分支，两种方式：  
    * 方式一：  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/projectManage/git/git-1.png)  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/projectManage/git/git-2.png)  
    * 方式二：git fetch -p    
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/projectManage/git/git-3.png)  


### 1.3.2. 基于Gitlab Issues为导向的分支管理
<!--
基于Gitlab Issues为导向的分支管理
https://blog.csdn.net/u011423145/article/details/107860812
-->

## 1.4. 命令

### 1.4.1. git clone 文件大
&emsp; git clone giturl --depth=1  


### 1.4.2. git tag  
&emsp; 常常在代码封板时，使用git 创建一个tag，这样一个不可修改的历史代码版本就像被封存起来一样，不论是运维发布拉取，或者以后的代码版本管理，都是十分方便的。  


### 1.4.3. ★★★git回滚
<!--
https://blog.csdn.net/ligang2585116/article/details/71094887
https://zhuanlan.zhihu.com/p/137856034
https://blog.csdn.net/tsq292978891/article/details/78965693

-->

&emsp; 强制推送到远程分支：  

```text
git push -f origin master ## 这里假设只有一个master分支
```

#### 场景  

#### 撤销，commit  


#### 回滚，push


#### 删除某次提交



## 1.5. gitignore规则不生效  
&emsp; .gitignore只能忽略那些原来没有被track的文件，如果某些文件已经被纳入了版本管理中，则修改.gitignore是无效的。所以一定要养成在项目开始就创建.gitignore文件的习惯。  
&emsp; 解决方法就是先把本地缓存删除(改变成未track状态)，然后再提交：  

```text
git rm -r --cached .
git add .
git commit -m "msg"
```

&emsp; 以上方案试了，有时直接把文件删了。  

&emsp; 使用`.ignore插件`。  

## 1.6. octotree，树形展示Github项目代码



## 1.7. git迁移仓库  
