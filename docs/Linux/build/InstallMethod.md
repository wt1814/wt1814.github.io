



# Linux多种安装方式  

1. 源码编译安装（二进制安装）
    源码编译安装：  程序是由开发人员  写代码一个一个单词 敲出来的     （windows打包好了）

    源码： 编写好的 .c  .java  文件

    编译： 将人类写的  代码 翻译成二进制语言

    安装： 将二进制保存在键盘上

    缺点：

    如果编译出了问题，你看不懂源代码，无法解决

    安装过程复杂

    没有统一的管理人员

    优点

    1.契合系统兼容性强

    2.如果你可以看懂源代码，修改新增功能

    3.比较自由

4. bin 二进制执行文件安装

2. Rpm安装    安装包管理工具（约等于360软件管家）

    优点：

    1统一的安装包格式

    2.已经帮你编译完成

    3.使用简单

    缺点：

    1.有依赖关系（安装  a     我需要先安）    httpd      

                    所以RPM  包根本不用

3. yum仓库   解决依赖关系而诞生的

    1.安装简单

    2.自动解决依赖关系

    下载安装包：

    .rpm  结尾



