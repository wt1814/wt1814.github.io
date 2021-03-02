

<!-- TOC -->

- [1. Shell编程](#1-shell编程)
    - [1.1. Shell环境](#11-shell环境)
    - [1.2. Shell脚本运行](#12-shell脚本运行)
    - [1.3. Shell脚本语法](#13-shell脚本语法)
        - [1.3.1. Shell变量](#131-shell变量)
        - [1.3.2. Shell注释](#132-shell注释)
        - [1.3.3. Shell基本运算符](#133-shell基本运算符)
        - [1.3.4. Shell流程控制语句](#134-shell流程控制语句)
        - [1.3.5. 自定义Shell函数](#135-自定义shell函数)
        - [1.3.6. Shell中的一些命令](#136-shell中的一些命令)
            - [1.3.6.1. echo命令](#1361-echo命令)
            - [1.3.6.2. printf命令](#1362-printf命令)
            - [1.3.6.3. test命令](#1363-test命令)
        - [1.3.7. Shell向脚本传递参数](#137-shell向脚本传递参数)
        - [1.3.8. Shell输入/输出重定向](#138-shell输入输出重定向)
        - [1.3.9. Shell文件包含外部脚本](#139-shell文件包含外部脚本)

<!-- /TOC -->

&emsp; **<font color = "red">Shell编程：</font>**  
&emsp; 变量、自定义函数、命令、向脚本传递参数......  

# 1. Shell编程  

## 1.1. Shell环境  
&emsp; Shell编程跟JavaScript、php编程一样，只要有一个能编写代码的文本编辑器和一个能解释执行的脚本解释器就可以了。  

&emsp; Linux的Shell种类众多，常见的有：  

* Bourne Shell（/usr/bin/sh或/bin/sh）
* Bourne Again Shell（/bin/bash）
* C Shell（/usr/bin/csh）
* K Shell（/usr/bin/ksh）
* Shell for Root（/sbin/sh）
* …… 

&emsp; Bourne Again Shell，易用和免费，Bash在日常工作中被广泛使用。同时，Bash也是大多数Linux系统默认的Shell。  
&emsp; 在一般情况下，并不区分Bourne Shell和Bourne Again Shell，所以，像 #!/bin/sh，它同样也可以改为 #!/bin/bash。  
&emsp; #! 告诉系统其后路径所指定的程序即是解释此脚本文件的Shell程序。  

## 1.2. Shell脚本运行  
&emsp; 运行Shell脚本有两种方法：  

1. 作为可执行程序  
    &emsp; 将上面的代码保存为test.sh，并cd到相应目录：  

    ```text
    chmod +x ./test.sh  #使脚本具有执行权限
    ./test.sh  #执行脚本
    ```

    &emsp; 注意，一定要写成./test.sh，而不是test.sh，运行其它二进制的程序也一样，直接写 test.sh，linux 系统会去 PATH 里寻找有没有叫 test.sh 的，而只有/bin, /sbin, /usr/bin，/usr/sbin 等在PATH里，当前目录通常不在PATH里，所以写成 test.sh 是会找不到命令的，要用 ./test.sh 告诉系统说，就在当前目录找。  

2. 作为解释器参数  
    &emsp; 这种运行方式是，直接运行解释器，其参数就是 shell 脚本的文件名，如：  

    ```text
    /bin/sh test.sh
    /bin/php test.php
    ```

    &emsp; 这种方式运行的脚本，不需要在第一行指定解释器信息，写了也没用。   

## 1.3. Shell脚本语法  

### 1.3.1. Shell变量  
&emsp; **使用变量**  
&emsp; 使用一个定义过的变量，只要在变量名前面加美元符号即可，如：  

    your_name="qinjx"
    echo $your_name
    echo ${your_name}

&emsp; **删除变量**  
&emsp; 使用 unset 命令可以删除变量。语法：  

    unset variable_name

&emsp; **变量类型**  
&emsp; 运行shell时，会同时存在三种变量：  
&emsp; 1) 局部变量：局部变量在脚本或命令中定义，仅在当前shell实例中有效，其他shell启动的程序不能访问局部变量。  
&emsp; 2) 环境变量：所有的程序，包括shell启动的程序，都能访问环境变量，有些程序需要环境变量来保证其正常运行。必要的时候shell脚本也可以定义环境变量。  
&emsp; 3) shell变量：shell变量是由shell程序设置的特殊变量。shell变量中有一部分是环境变量，有一部分是局部变量，这些变量保证了shell的正常运行。  

&emsp; **将linux命令执行结果赋值给变量**  

```text
#!/bin/bash
path=$(pwd)
files=`ls -al`
echo current path: $path
echo files: $files
```
&emsp; 以上2行和第3行分别演示了两种方式来将Linux命令执行结果保存到变量。  
&emsp; 第2行将pwd执行结果（当前所在目录）赋值给path变量。  
&emsp; 第3行将ls -al命令执行结果（列出当前目录下所有的文件及文件夹）赋值给变量。  

    注意：第三行的符号不是单引号，是键盘上“～”这个按键

### 1.3.2. Shell注释  
<!-- 
https://www.runoob.com/linux/linux-shell-variable.html
-->
* 单行注释使用#
* 多行注释使用：<\<EOF 注释内容 EOF




### 1.3.3. Shell基本运算符

&emsp; Shell 和其他编程语言一样，支持多种运算符，包括：  

* 算数运算符
* 关系运算符
* 布尔运算符
* 字符串运算符
* 文件测试运算符

&emsp; 原生bash不支持简单的数学运算，但是可以通过其他命令来实现，例如 awk 和 expr，expr最常用。  
&emsp; expr是一款表达式计算工具，使用它能完成表达式的求值操作。  


### 1.3.4. Shell流程控制语句  

&emsp; if else、for 循环 、while 语句......  

&emsp; if else-if else 语法格式：  

    if condition1
    then
        command1
    elif condition2 
    then 
        command2
    else
        commandN
    fi

&emsp; for循环一般格式为：  

    for var in item1 item2 ... itemN
    do
        command1
        command2
        ...
        commandN
    done


### 1.3.5. 自定义Shell函数
&emsp; linux shell 可以用户定义函数，然后在shell脚本中可以随便调用。  

&emsp; **函数参数**  

&emsp; 在Shell中，调用函数时可以向其传递参数。在函数体内部，通过 $n 的形式来获取参数的值，例如，$1表示第一个参数，$2表示第二个参数...  

&emsp; 带参数的函数示例：  

    #!/bin/bash

    funWithParam(){
        echo "第一个参数为 $1 !"
        echo "第二个参数为 $2 !"
        echo "第十个参数为 $10 !"
        echo "第十个参数为 ${10} !"
        echo "第十一个参数为 ${11} !"
        echo "参数总数有 $# 个!"
        echo "作为一个字符串输出所有参数 $* !"
    }
    funWithParam 1 2 3 4 5 6 7 8 9 34 73

&emsp; 输出结果：  

    第一个参数为 1 !
    第二个参数为 2 !
    第十个参数为 10 !
    第十个参数为 34 !
    第十一个参数为 73 !
    参数总数有 11 个!
    作为一个字符串输出所有参数 1 2 3 4 5 6 7 8 9 34 73 !

&emsp; 注意，$10不能获取第十个参数，获取第十个参数需要${10}。当n>=10时，需要使用${n}来获取参数。  

### 1.3.6. Shell中的一些命令  
#### 1.3.6.1. echo命令  
&emsp; Shell的echo指令用于字符串的输出。命令格式：

    echo string

#### 1.3.6.2. printf命令  
&emsp; Shell的另一个输出命令printf。  
&emsp; printf使用引用文本或空格分隔的参数，外面可以在printf中使用格式化字符串，还可以制定字符串的宽度、左右对齐方式等。默认printf不会像echo自动添加换行符，可以手动添加 \n。  

&emsp; printf 命令的语法：  

    printf  format-string  [arguments...]

&emsp; 参数说明：  

    format-string: 为格式控制字符串
    arguments: 为参数列表。


#### 1.3.6.3. test命令  
&emsp; Shell中的test命令用于检查某个条件是否成立，它可以进行数值、字符和文件三个方面的测试。  



### 1.3.7. Shell向脚本传递参数  
&emsp; 可以在执行 Shell脚本时，向脚本传递参数，脚本内获取参数的格式为：$n。n 代表一个数字，1 为执行脚本的第一个参数，2 为执行脚本的第二个参数，以此类推……  

<!-- 
https://www.runoob.com/linux/linux-shell-passing-arguments.html
-->

&emsp; **实例**  
&emsp; 以下实例向脚本传递三个参数，并分别输出，其中 $0 为执行的文件名（包含文件路径）：  

    #!/bin/bash

    echo "Shell 传递参数实例！";
    echo "执行的文件名：$0";
    echo "第一个参数为：$1";
    echo "第二个参数为：$2";
    echo "第三个参数为：$3";

&emsp; 为脚本设置可执行权限，并执行脚本，输出结果如下所示：  

    $ chmod +x test.sh 
    $ ./test.sh 1 2 3
    Shell 传递参数实例！
    执行的文件名：./test.sh
    第一个参数为：1
    第二个参数为：2
    第三个参数为：3

-----

### 1.3.8. Shell输入/输出重定向  
&emsp; 大多数 UNIX 系统命令从终端接受输入并将所产生的输出发送回​​到终端。一个命令通常从一个叫标准输入的地方读取输入，默认情况下，这恰好是终端。同样，一个命令通常将其输出写入到标准输出，默认情况下，这也是终端。  

### 1.3.9. Shell文件包含外部脚本  
&emsp; 和其他语言一样，Shell也可以包含外部脚本。这样可以很方便的封装一些公用的代码作为一个独立的文件。  
&emsp; Shell文件包含的语法格式如下：  

```text
. filename   # 注意点号(.)和文件名中间有一空格
```
&emsp; 或
    
```text
source filename
```
