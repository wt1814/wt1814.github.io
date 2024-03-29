
<!-- TOC -->

- [1. git](#1-git)
    - [1.1. ★★★git访问慢](#11-★★★git访问慢)
    - [1.2. git多用户](#12-git多用户)
    - [1.3. Git分支命名](#13-git分支命名)
    - [1.4. GIT本地库操作基本原理](#14-git本地库操作基本原理)
    - [1.5. 分支](#15-分支)
        - [1.5.1. 操作分支](#151-操作分支)
        - [1.5.2. git克隆指定分支的代码](#152-git克隆指定分支的代码)
        - [1.5.3. 基于Gitlab Issues为导向的分支管理](#153-基于gitlab-issues为导向的分支管理)
    - [1.6. 命令](#16-命令)
        - [1.6.1. ★★★git clone 文件大](#161-★★★git-clone-文件大)
        - [1.6.2. git tag](#162-git-tag)
        - [1.6.3. ★★★git回滚](#163-★★★git回滚)
            - [1.6.3.1. 场景](#1631-场景)
            - [1.6.3.2. 撤销，commit](#1632-撤销commit)
            - [1.6.3.3. 回滚，push](#1633-回滚push)
                - [1.6.3.3.1. ★★★情况二：删除最后一次远程提交](#16331-★★★情况二删除最后一次远程提交)
                - [1.6.3.3.2. ★★★情况三：回滚某次提交](#16332-★★★情况三回滚某次提交)
            - [1.6.3.4. 删除某次提交](#1634-删除某次提交)
    - [1.7. ★★★.gitignore规则不生效](#17-★★★gitignore规则不生效)
    - [1.8. octotree，树形展示Github项目代码](#18-octotree树形展示github项目代码)
    - [1.9. git迁移仓库](#19-git迁移仓库)
    - [1.10. sourceTree](#110-sourcetree)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. 回滚  
    1. 删除最后一次远程提交
    2. 回滚某次提交
    3. 强制提交  


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

## 1.1. ★★★git访问慢
<!-- 


-->

## 1.2. git多用户
<!-- 
一台电脑上配置并使用两个github账号
https://zhuanlan.zhihu.com/p/191589172
https://www.cnblogs.com/xjnotxj/p/5845574.html
-->

## 1.3. Git分支命名  
<!-- 
 别乱提交代码了，你必须知道的 Git 分支开发规范！ 
 https://mp.weixin.qq.com/s/w5gcDgQKYFmzel6Jnc0u4A
-->

## 1.4. GIT本地库操作基本原理  

![image](http://182.92.69.8:8081/img/projectManage/git/git-4.png)  
&emsp; GIT作为分布式版本库软件，每个机器上都是一个版本库。  
&emsp; git初始化后，有三个区，分别是 工作区，暂存区，本地库；  

* 工作区是编辑代码的区别，包括新增，修改，删除代码操作，编辑代码后，添加到暂存区；  
* 暂存区是临时存储代码的地方，方便批量提交数据到本地库；  
* 本地库是最终的历史版本文件的存储地；  

## 1.5. 分支
<!-- 
https://jingyan.baidu.com/article/a17d52854e164dc098c8f2b0.html
-->
### 1.5.1. 操作分支
&emsp; 查看分支：  
* 查看本地分支：git branch
* 查看远程分支：git branch -r


&emsp; 删除分支：  
* 删除本地分支tmp：git branch -D tmp(分支名称)  
* 删除远程分支tmp：git push origin --delete tmp(分支名称) 
* Git删除在本地有但在远程库中已经不存在的分支，两种方式：  
    * 方式一：  
    ![image](http://182.92.69.8:8081/img/projectManage/git/git-1.png)  
    ![image](http://182.92.69.8:8081/img/projectManage/git/git-2.png)  
    * 方式二：git fetch -p    
    ![image](http://182.92.69.8:8081/img/projectManage/git/git-3.png)  


### 1.5.2. git克隆指定分支的代码
<!-- 

https://www.cnblogs.com/nylcy/p/6569284.html
--> 

### 1.5.3. 基于Gitlab Issues为导向的分支管理
<!--
9种提高 GitHub 国内访问速度的方案
https://juejin.cn/post/7043960479181438983?share_token=25e7cfba-e5e8-4a51-9237-6e922f9a15c4#heading-4

基于Gitlab Issues为导向的分支管理
https://blog.csdn.net/u011423145/article/details/107860812
-->

## 1.6. 命令

### 1.6.1. ★★★git clone 文件大
&emsp; git clone 【giturl】 --depth=1  


### 1.6.2. git tag  
&emsp; 常常在代码封板时，使用git 创建一个tag，这样一个不可修改的历史代码版本就像被封存起来一样，不论是运维发布拉取，或者以后的代码版本管理，都是十分方便的。  


### 1.6.3. ★★★git回滚
<!--
https://blog.csdn.net/ligang2585116/article/details/71094887
https://zhuanlan.zhihu.com/p/137856034
https://blog.csdn.net/tsq292978891/article/details/78965693

-->

&emsp; 强制推送到远程分支：  

```text
git push -f origin master ## 这里假设只有一个master分支
```

#### 1.6.3.1. 场景  

#### 1.6.3.2. 撤销，commit  


#### 1.6.3.3. 回滚，push

##### 1.6.3.3.1. ★★★情况二：删除最后一次远程提交  
&emsp; 方式一：使用revert  

```text
$ git revert HEAD
$ git push origin master
```

&emsp; 方式二：使用reset

```text
$ git reset --hard HEAD^
$ git push origin master -f
```

&emsp; 二者区别：  

&emsp; revert 是放弃指定提交的修改，但是会生成一次新的提交，需要填写提交注释，以前的历史记录都在；  
&emsp; reset 是指将HEAD指针指到指定提交，历史记录中不会出现放弃的提交记录。  


##### 1.6.3.3.2. ★★★情况三：回滚某次提交  

```text
# 找到要回滚的commitID
$ git log
$ git revert commitID
```


#### 1.6.3.4. 删除某次提交



## 1.7. ★★★.gitignore规则不生效  
<!-- 
idea忽略隐藏文件、文件夹的设置操作
https://www.cnblogs.com/sxdcgaq8080/p/9007883.html

https://blog.csdn.net/chao2016/article/details/81699358

-->
<!-- 
git添加.gitignore后不生效问题
https://blog.csdn.net/xuxu_123_/article/details/131710549
-->


git忽略idea生成的不必要文件，如.iml文件，.idea文件夹，target文件夹
https://blog.csdn.net/gaotanpan3666/article/details/83047856


&emsp; .gitignore只能忽略那些原来没有被track的文件，如果某些文件已经被纳入了版本管理中，则修改.gitignore是无效的。所以一定要养成在项目开始就创建.gitignore文件的习惯。  
&emsp; 解决方法就是先把本地缓存删除(改变成未track状态)，然后再提交：  

```text
git rm -r --cached .
git add .
git commit -m "msg"
```

&emsp; 以上方案试了，有时直接把文件删了。  

&emsp; 使用`.ignore插件`。  

## 1.8. octotree，树形展示Github项目代码



## 1.9. git迁移仓库  


## 1.10. sourceTree
sourceTree 添加 ssh key 方法
https://blog.csdn.net/tengdazhang770960436/article/details/54171911
sourceTree一直输入密码
https://www.jianshu.com/p/24c56a97f80c
https://www.jianshu.com/p/8f3254493cd6
https://blog.csdn.net/gang544043963/article/details/47614697
解决使用SourceTree下载GitLab服务器上的代码每次都需要输入密码问题
http://www.pianshen.com/article/9250357091/
