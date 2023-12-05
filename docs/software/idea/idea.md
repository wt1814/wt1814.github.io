
<!-- TOC -->

- [1. Idea](#1-idea)
    - [1.1. 安装](#11-安装)
    - [1.2. 激活](#12-激活)
        - [1.2.1. ★★★License Server](#121-★★★license-server)
    - [项目标识iml](#项目标识iml)
    - [idea本身的日志](#idea本身的日志)
    - [1.3. Idea更新本地仓库maven构建索引](#13-idea更新本地仓库maven构建索引)
    - [1.4. springboot在idea的RunDashboard如何显示](#14-springboot在idea的rundashboard如何显示)
    - [1.5. iml文件](#15-iml文件)
    - [1.6. 文件夹类型](#16-文件夹类型)
    - [Debug](#debug)
    - [1.7. 插件](#17-插件)
        - [1.7.1. 代码质量检测插件](#171-代码质量检测插件)
        - [1.7.2. 查看字节码插件](#172-查看字节码插件)
        - [1.7.3. IDEA的Redis插件](#173-idea的redis插件)
    - [1.8. 集成JIRA、UML类图插件、SSH、FTP、Database管理...](#18-集成jirauml类图插件sshftpdatabase管理)
    - [1.9. 快捷键](#19-快捷键)
        - [1.9.1. 批量修改变量名方法名](#191-批量修改变量名方法名)
    - [1.10. ***高级调试](#110-高级调试)
    - [模板](#模板)
        - [注释模板](#注释模板)
        - [代码模板](#代码模板)
    - [自动换行](#自动换行)

<!-- /TOC -->


# 1. Idea
<!-- 
Idea在debug模式下修改的java后，Recopile（ctrl+shift+f9）热部署失效
https://blog.csdn.net/weixin_42170236/article/details/121637717
-->


## 1.1. 安装
<!-- 

Mac idea 打不开
https://blog.csdn.net/sanmi8276/article/details/108522676
-->


## 1.2. 激活
<!-- 
IntelliJ IDEA 2020.2.3永久激活教程
https://www.yuque.com/docs/share/23fc9e41-ad96-4343-aced-a35419117d89


-->

### 1.2.1. ★★★License Server

<!-- 
*** https://www.cnblogs.com/xiang--liu/p/13883523.html
https://www.cnblogs.com/jie-fang/p/10214170.html

https://blog.csdn.net/sanmi8276/article/details/108522676

https://www.jianshu.com/p/46ac89620c0a

-->

## 项目标识iml
<!-- 
IDEA - 生成iml文件
https://blog.csdn.net/u012627861/article/details/83028437
idea如何生成iml文件
https://blog.csdn.net/qq_38225558/article/details/86023470
-->

## idea本身的日志  
<!-- 

https://jingyan.baidu.com/article/af9f5a2d4e7f9543140a45d5.html
-->


## 1.3. Idea更新本地仓库maven构建索引
<!-- 

Idea更新本地仓库maven构建索引
https://blog.csdn.net/weixin_42325659/article/details/105649218
https://www.cnblogs.com/lly001/p/9732201.html
-->

## 1.4. springboot在idea的RunDashboard如何显示
<!-- 
https://jingyan.baidu.com/article/ce4366495a1df73773afd3d3.html
-->

## 1.5. iml文件  

&emsp; IDEA中的.iml文件是项目标识文件，缺少了这个文件，IDEA就无法识别项目。跟Eclipse的.project文件性质是一样的。并且这些文件不同的设备上的内容也会有差异，所以在管理项目的时候，.project和.iml文件都需要忽略掉。  

&emsp; 在缺少.iml文件项目下运行mvn idea:module，完成后将自动生成.iml文件。  


## 1.6. 文件夹类型
<!-- 

https://blog.csdn.net/a772304419/article/details/79680775
-->

## Debug
<!-- 
debug：
https://mp.weixin.qq.com/s?__biz=MzIwMzY1OTU1NQ==&mid=2247486107&idx=1&sn=d70205970a84c2f0f812a5333594e1b0&chksm=96cd4ad7a1bac3c1e3f9e49efc6a96c265efbb057932e5938c2f6d4e9d665d878be395c0c0a9&mpshare=1&scene=1&srcid=&key=906dea2828a54f86e61d2eca5921596d543c1d2ae2e5f95bc37af86296d0fac1928f0b0ea7a1758cbbc3646beae5ce50cda05d1d3bba1e799dedfc4b78fb817d2379e9aad766459d0f192962d0206564&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060739&lang=zh_CN&pass_ticket=CI%2FqYGaQ%2B%2Bc4%2BSbpGIPym8tyErmLT25DOfcghAPkipCYW9ZmudoAS96XHefJOv%2BG
idea debug高级特性看这篇就够了 
http://www.justdojava.com/2019/08/14/idea-debug/
深入学习 Intellij IDEA 调试技巧 
http://www.justdojava.com/2019/07/20/debug-in-idea/
有 Bug 不会调试 ？
https://mp.weixin.qq.com/s?__biz=MzAwOTE3NDY5OA==&mid=2647908528&idx=1&sn=221c79ab90e0dda32ae864841a14312d&chksm=8344eb75b433626332895319d7a0f0da2403c8c1f19c4c5c559540eddcb1e6d0d8c99cae8fa5&mpshare=1&scene=1&srcid=&sharer_sharetime=1568249271686&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=6f23511bf9e1c01fd3706a0b0f16749dc74af0124c062cd8df5b876b587fee8c6ca4dac7b246f620e8af31d448433fd3bcd1809de5d8ed3a2e0832abef92e012dcc29f5a77804660a45ab15284980809&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=itx1gApiSjQ3hWB5NxczIuCswqlR4CHjqy8rNSbMiIlPrLAnYQ1%2BCdb6ALXoRgGH
最详细的 IDEA 中使用 Debug 教程 
https://mp.weixin.qq.com/s/QwLiIVGoz5f5KK6xlgHWag
IDEA高级调试技巧
https://mp.weixin.qq.com/s/16sEh4-8qGdHm-MK6kRhRw
有 Bug 不会调试 ？
https://mp.weixin.qq.com/s/Po3I-tK3x60wIgn52BO24Q
intellij debug 技巧:java 8 stream
https://mp.weixin.qq.com/s?__biz=MzU2NjIzNDk5NQ==&mid=2247486106&idx=1&sn=2e9c161dee6c166293cc07ddd486a466&chksm=fcaed086cbd9599044c4bd3412a53dfcdfd785342f1bbfaa5efc704c0f08e2e59796ab472cd8&mpshare=1&scene=1&srcid=&key=798968cdb5a0aac4a1a5d5a70f40fff190265a7affb4a47dc8fbfc3330b55ab1ec7c1a795215b0b968f6e12e6a4020f15588d9e6862f92cc3e9ff49a4501870da21e6efd5f1b9deae8011702fab23532&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060739&lang=zh_CN&pass_ticket=VPNH3cw66OjhxWCBa8dqbqeWqGZnjMQmgJ%2FCvHrG%2BX3ZBz4nJALVXpapcQ8dXMFT

https://www.cnblogs.com/jajian/p/9410844.html
-->


## 1.7. 插件  
<!-- 
 Stream Trace 
https://mp.weixin.qq.com/s/-IZ9jDMXUlL4kFt-OZ-qQw
https://mp.weixin.qq.com/s/jHSTmVR8NJtpaAEAxZayqw

IntelliJ IDEA18个常用插件，动图演示，让你效率翻倍！ 
https://mp.weixin.qq.com/s?__biz=MzAxNDMwMTMwMw==&mid=2247491451&idx=1&sn=3af83a3fa3dde223418023b84827fc6d&chksm=9b943e63ace3b775fc92f9fc0e4b36490994d153fc69fc1f04c896e5d84539de22b4ca85c2e7&mpshare=1&scene=1&srcid=&key=798968cdb5a0aac46f17bddd47af95b8e66dde100ef459dbb971e98e4efc30bf2e0f19d30dcdd8f0064882a6b777deecc66dfa0ebea68ad70c2713349516443e141b83c5353c44d584e51fd331f28f3a&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060833&lang=zh_CN&pass_ticket=THNI%2FhP%2FKSHLU%2F25yZyO80hoXm1HBbAml9fCjo4X9PO%2FSXHbogDm%2B3MEm2tynRCP
https://mp.weixin.qq.com/s/23Ua-P9qFfZ6iOt0IIV15g
 7个IntelliJ IDEA必备插件，提高编码效率 
https://mp.weixin.qq.com/s/ODiozM2qPhPUM8MWtvuNYg
阿里26. Java 代码规约扫描插件 P3C
项目包含三部分：PMD 实现、IntelliJ IDEA 插件、Eclipse 插件。
地址：https://github.com/alibaba/p3c

-->


插件	作用
-----------
Alibaba Java Coding Guidelines	检测代码规范
p3c
	阿里巴巴出品的java代码规范插件

可以扫描整个项目 找到不规范的地方 并且大部分可以自动修复 
https://github.com/alibaba/p3c/tree/master/idea-plugin

FindBugs	检测代码中可能的bug及不规范的位置，检测的模式相比p3c更多，

写完代码后检测下 避免低级bug，强烈建议用一下，一不小心就发现很多老代码的bug
	
GenerateAllSetter	一键调用类的所有set方法。
一键调用一个对象的所有set方法并且赋予默认值 在对象字段多的时候非常方便，在做项目时，每层都有各自的实体对象需要相互转换，但是考虑BeanUtil.copyProperties()等这些工具的弊端，有些地方就需要手动的赋值时，有这个插件就会很方便，创建完对象后在变量名上面按Alt+Enter就会出来 generate all setter选项。 
CamelCase	将不是驼峰格式的名称，快速转成驼峰格式，安装好后，选中要修改的名称，按快捷键shift+alt+u
CodeMaker	代码生成工具
开发过程中，经常手工编写重复代码。现在，可以通过 CodeMaker 来定义 Velocity 模版来支持自定义代码模板来生成代码。目前，CodeMaker 自带两个模板。Model：根据当前类生成一个与其拥有类似属性的类，用于自动生成持久类对应的领域类。Converter：该模板需要两个类作为输入的上下文，用于自动生成领域类与持久类的转化类。详细使用文档，参考：https://github.com/x-hansong/CodeMaker

Mybatis Code Helperpro	mybatis自动生成文件
MyBatis Log Plugin	mybatis打印日志sql语句可以直接执行
Free Mybatis plugin	方便进行Mapper接口和XML文件之间跳转。
betty-mybatis-generator	插件功能：在idea的database工具中使用，选择表，生成mybatis相关的代码。已支持的数据库：Mysql、Mysql8、Oracle、MariaDB。待验证：PostgreSQL与SQL Server
Step1: 连接Database: View > Tool Windows > Database。
Step2: 选择表（一或n,建议n小于10），右击 选择 mybatis generate 打开插件主页。
Step3: 填写配置，检查无误后点击 ok。
Step4: 首次使用时请提供账号密码。
Step5: 检查、使用生成的代码。
Setting: Tools > MyBatis generator Plugin 此处设置默认配置，未设置则使用程序默认配置。
更多的插件使用截图和注意事项见： 插件使用介绍
Mybatis-PageHelper	
Maven Helper	maven查看器
GsonFormat	json格式化工具插件。一键根据json文本生成java类
Lombok	开发神器，可以简化你的实体类，让你i不再写get/set方法，还能快速的实现builder模式，以及链式调用方法，总之就是为了简化实体类而生的插件
codehelper.generator	可以让你在创建一个对象并赋值的时候，快速的生成代码，不需要一个一个属性的向里面set,根据new关键字，自动生成掉用set方法的代码，还可以一键填入默认值。
Iedis	可视化：Iedis
参考：https://plugins.jetbrains.com/plugin/9228-iedis 使用参考：https://codesmagic.com/iedis/userguide/getting-started 可方便的执行增删查改及使用命令行进行操作。
Grep Console	日志工具
参考：https://plugins.jetbrains.com/plugin/7125-grep-console
不同级别日志通过颜色区分，一路了然
JRebel for IntelliJ	一款热部署插件，只要不是修改了项目的配置文件，用它都可以实现热部署。收费的，破解比较麻烦。不过功能确实很强大。算是开发必备神器了。热部署快捷键是control+F9/command+F9。
.ignore	git提交时过滤掉不需要提交的文件，很方便，有些本地文件是不需要提交到Git上的。
-----------
VisualVM Launcher	查看运行时jvm内存情况
Alibaba Cloud Toolkit	阿里巴巴远程终端一键部署项目
	
Rainbow Brackets	彩虹颜色的括号
translation	翻译插件
Key Promoter X	快捷键提示插件
Key promoter	快捷键提示
RestfulToolkit	根据url快速定位controller
-----------
Material Theme UI	主题插件。这是一款主题插件，可以让你的ide的图标变漂亮，配色搭配的很到位，还可以切换不同的颜色，甚至可以自定义颜色。默认的配色就很漂亮了，如果需要修改配色，可以在工具栏中Tools->Material Theme然后修改配色等。
Nyan progress bar	进度条皮肤
String Manipulation	处理字符串格式
AceJump	迅速定位光标
activate-power-mode	世界在颤抖，装逼插件
Background image Plus	这是一款可以设置idea背景图片的插件，不但可以设置固体的图片，还可以设置一段时间后随机变化背景图片，以及设置图片的透明度等等。
	
Gitee	开源中国的码云插件
Properties to YAML Converter	把 Properties 的配置格式改为 YAML 格式
	


### 1.7.1. 代码质量检测插件
<!-- 
https://mp.weixin.qq.com/s/UwS0oGaHR5yV5PIAHx6QZg
-->

### 1.7.2. 查看字节码插件
<!-- 

IDEA查看字节码插件
https://blog.csdn.net/qq_38826019/article/details/119273641
--> 


### 1.7.3. IDEA的Redis插件  
<!-- 

使用IDEA的Redis插件连接Redis服务器
https://blog.csdn.net/m0_47503416/article/details/121397584

-->


## 1.8. 集成JIRA、UML类图插件、SSH、FTP、Database管理... 
<!-- 

https://mp.weixin.qq.com/s/UcNy1hybHz5u3_fOn3FhHw
-->


## 1.9. 快捷键  
<!--

IDEA 重构快捷键
https://blog.csdn.net/zhoukikoo/article/details/79374675
 IntelliJ IDEA 常用快捷键
https://mp.weixin.qq.com/s?__biz=MzI4Njc5NjM1NQ==&mid=2247488011&idx=1&sn=6f21d63c4a3d1f434c9c28f11ac2aa8b&chksm=ebd62d27dca1a4319c0552a85133b297326818b2bc28283e0d3876b64d2b0b38f1cfb6051684&mpshare=1&scene=1&srcid=#rd

-->

### 1.9.1. 批量修改变量名方法名  
<!-- 

https://www.pianshen.com/article/83811617038/
-->

&emsp; 选中要修改的然后按shift+F6 出现了红框后修改好点击回车就行了  



## 1.10. ***高级调试   
<!--
 这几个 IntelliJ IDEA 高级调试技巧，用了都说爽！ 
https://mp.weixin.qq.com/s?__biz=Mzg2MjEwMjI1Mg==&mid=2247491131&idx=3&sn=d072a649696b5b07411642067d51367d&chksm=ce0da9b8f97a20ae787ac263de162bc8c1d1843902341e1f5c4a2db0ece02276b85c5b76cfc6&mpshare=1&scene=1&srcid=&sharer_sharetime=1577187607880&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=6f32c57123495b976074ebfd1a889da106f7c9e51dd9e4429295af6cf4ef294ca7377d41701d83de6900e2ad7a0d82fe20da55d14f325c1cd2296f52a3b45a7ac0ab989b3ae24465df8a53af9481378a&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070158&lang=zh_CN&exportkey=Af6pZ1ErrZndhXyF%2FrpKbKA%3D&pass_ticket=fjo0TPQ4TftdXiH325uINjkxmTSYWN5xsY7SY8CPXJ8L70Z%2B9nqwLCPhjc61tfer
-->


## 模板
### 注释模板  
https://blog.csdn.net/xiaoliulang0324/article/details/79030752

### 代码模板

## 自动换行  
<!-- 
idea markdown 自动换行
https://juejin.cn/s/idea%20markdown%20%E8%87%AA%E5%8A%A8%E6%8D%A2%E8%A1%8C
-->

