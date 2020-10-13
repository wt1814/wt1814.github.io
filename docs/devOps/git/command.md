# git

<!-- 

https://mp.weixin.qq.com/s/bT7VXffqHuzUZUY5c4ce7A
-->

## GIT本地库操作基本原理  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/projectManage/git/git-4.png)  
&emsp; GIT作为分布式版本库软件，每个机器上都是一个版本库。  
&emsp; git初始化后，有三个区，分别是 工作区，暂存区，本地库；  

* 工作区是编辑代码的区别，包括新增，修改，删除代码操作，编辑代码后，添加到暂存区；  
* 暂存区是临时存储代码的地方，方便批量提交数据到本地库；  
* 本地库是最终的历史版本文件的存储地；  

## 分支

&emsp; Git删除在本地有但在远程库中已经不存在的分支，两种方式：  
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



