# git

<!--


git clone时报RPC failed; curl 18 transfer closed with outstanding read data remaining
https://www.cnblogs.com/zjfjava/p/10392150.html

git书籍  
https://mp.weixin.qq.com/s/bT7VXffqHuzUZUY5c4ce7A
 如何自动同步博客到 Github 主页？ 
 https://mp.weixin.qq.com/s/J2sIku38WxL4ge4W5DP2hw
-->

<!-- 本地仓库的位置 -->
本地仓库的位置可设置为：\<localRepository\>${user.home}/.m2/repository\</localRepository\>

## Git分支命名  
<!-- 
 别乱提交代码了，你必须知道的 Git 分支开发规范！ 
 https://mp.weixin.qq.com/s/w5gcDgQKYFmzel6Jnc0u4A
-->

## GIT本地库操作基本原理  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/projectManage/git/git-4.png)  
&emsp; GIT作为分布式版本库软件，每个机器上都是一个版本库。  
&emsp; git初始化后，有三个区，分别是 工作区，暂存区，本地库；  

* 工作区是编辑代码的区别，包括新增，修改，删除代码操作，编辑代码后，添加到暂存区；  
* 暂存区是临时存储代码的地方，方便批量提交数据到本地库；  
* 本地库是最终的历史版本文件的存储地；  

## 分支
<!-- 
https://jingyan.baidu.com/article/a17d52854e164dc098c8f2b0.html
-->
&emsp; 查看分支：  
* 查看本地分支：git branch
* 查看远程分支：git branch -r


&emsp; 删除分支：  
* 删除本地分支tmp：git branch -D tmp  
* 删除远程分支tmp：git push origin --delete tmp 
* Git删除在本地有但在远程库中已经不存在的分支，两种方式：  
    * 方式一：  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/projectManage/git/git-1.png)  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/projectManage/git/git-2.png)  
    * 方式二：  
    ![image](https://gitee.com/wt1814/pic-host/raw/master/images/projectManage/git/git-3.png)  


## git clone 文件大
&emsp; git clone giturl --depth=1  


## .gitignore规则不生效  
&emsp; .gitignore只能忽略那些原来没有被track的文件，如果某些文件已经被纳入了版本管理中，则修改.gitignore是无效的。所以一定要养成在项目开始就创建.gitignore文件的习惯。  
&emsp; 解决方法就是先把本地缓存删除(改变成未track状态)，然后再提交：  

```text
git rm -r --cached .
git add .
git commit -m "msg"
```

## git tag  
&emsp; 常常在代码封板时，使用git 创建一个tag，这样一个不可修改的历史代码版本就像被封存起来一样，不论是运维发布拉取，或者以后的代码版本管理，都是十分方便的。  


## 基于Gitlab Issues为导向的分支管理
<!--
基于Gitlab Issues为导向的分支管理
https://blog.csdn.net/u011423145/article/details/107860812
-->

## octotree，树形展示Github项目代码



## git迁移仓库  
