

<!-- TOC -->

- [1. Linux文本处理](#1-linux文本处理)
    - [1.1. grep](#11-grep)
        - [1.1.1. 简介](#111-简介)
        - [1.1.2. 实际使用](#112-实际使用)
    - [1.2. sed](#12-sed)
    - [1.3. awk](#13-awk)
        - [1.3.1. 语法](#131-语法)
        - [1.3.2. 域](#132-域)
        - [1.3.3. 模式&动作](#133-模式动作)
        - [1.3.4. 结合正则](#134-结合正则)
        - [1.3.5. 复合表达式](#135-复合表达式)
        - [1.3.6. printf格式化输出](#136-printf格式化输出)
        - [1.3.7. 内置变量](#137-内置变量)
        - [1.3.8. 内置函数](#138-内置函数)
        - [1.3.9. awk脚本](#139-awk脚本)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
&emsp; **<font color = "clime">grep是一款强大的文本搜索工具，支持正则表达式。</font>**    
&emsp; **<font color = "clime">sed是一种流编辑器(增、删、改、查)，是一款处理文本比较优秀的工具，可以结合正则表达式一起使用。</font>**  


# 1. Linux文本处理  
<!--
五分钟入门文本处理三剑客grep awk sed 
https://mp.weixin.qq.com/s/_s0xIVsB6Zz0PYrgeBurxw
Shell文本处理三剑客：grep、sed、awk 
https://mp.weixin.qq.com/s/2qLACYURVLEvWmobOG36fQ
史上最全的 Linux Shell 文本处理工具集锦，快收藏！ 
https://mp.weixin.qq.com/s/5rUZKKdjvPWGJEvptYjp_Q
Linux 三剑客之 grep 教程详解 
https://mp.weixin.qq.com/s/vYmOk2fWRHu6HQWPSt1XAg
-->
&emsp; grep、sed、awk是Linux处理文本常用的命令。  

## 1.1. grep  
### 1.1.1. 简介  
&emsp; <font color = "clime">grep是一款强大的文本搜索工具，支持正则表达式。</font>    
&emsp; 全称（ global search regular expression(RE) and print out the line）  
&emsp; 语法：grep \[option]... PATTERN \[FILE]...  
&emsp; 常用：  

    usage: grep [-abcDEFGHhIiJLlmnOoqRSsUVvwxZ] [-A num] [-B num] [-C[num]]
    [-e pattern] [-f file] [--binary-files=value] [--color=when]
    [--context[=num]] [--directories=action] [--label] [--line-buffered]
    [--null] [pattern] [file ...]

&emsp; 常用参数：  

    -v        取反
    -i        忽略大小写
    -c        符合条件的行数
    -n        输出的同时打印行号
    ^*        以*开头         
    *$         以*结尾 
    ^$         空行 

### 1.1.2. 实际使用

&emsp; 准备好一个小故事txt：  

```text
[root@iz2ze76ybn73dvwmdij06zz ~]# cat monkey
One day,a little monkey is playing by the well.一天,有只小猴子在井边玩儿.
He looks in the well and shouts :它往井里一瞧,高喊道：
“Oh!My god!The moon has fallen into the well!” “噢!我的天!月亮掉到井里头啦!”
An older monkeys runs over,takes a look,and says,一只大猴子跑来一看,说,
“Goodness me!The moon is really in the water!” “糟啦!月亮掉在井里头啦!”
And olderly monkey comes over.老猴子也跑过来.
He is very surprised as well and cries out:他也非常惊奇,喊道：
“The moon is in the well.” “糟了,月亮掉在井里头了!”
A group of monkeys run over to the well .一群猴子跑到井边来,
They look at the moon in the well and shout:他们看到井里的月亮,喊道：
“The moon did fall into the well!Come on!Let’get it out!”
“月亮掉在井里头啦!快来!让我们把它捞起来!”
Then,the oldest monkey hangs on the tree up side down ,with his feet on the branch .
然后,老猴子倒挂在大树上,
And he pulls the next monkey’s feet with his hands.拉住大猴子的脚,
All the other monkeys follow his suit,其他的猴子一个个跟着,
And they join each other one by one down to the moon in the well.
它们一只连着一只直到井里.
Just before they reach the moon,the oldest monkey raises his head and happens to see the moon in the sky,正好他们摸到月亮的时候,老猴子抬头发现月亮挂在天上呢
He yells excitedly “Don’t be so foolish!The moon is still in the sky!”
它兴奋地大叫：“别蠢了!月亮还好好地挂在天上呢!
```

&emsp; **直接查找符合条件的行**  

```text
[root@iz2ze76ybn73dvwmdij06zz ~]# grep moon monkey
“Oh!My god!The moon has fallen into the well!” “噢!我的天!月亮掉到井里头啦!”
“Goodness me!The moon is really in the water!” “糟啦!月亮掉在井里头啦!”
“The moon is in the well.” “糟了,月亮掉在井里头了!”
They look at the moon in the well and shout:他们看到井里的月亮,喊道：
“The moon did fall into the well!Come on!Let’get it out!”
And they join each other one by one down to the moon in the well.
Just before they reach the moon,the oldest monkey raises his head and happens to see the moon in the sky,正好他们摸到月亮的时候,老猴子抬头发现月亮挂在天上呢
He yells excitedly “Don’t be so foolish!The moon is still in the sky!”
```

&emsp; **查找反向符合条件的行**  

```text
[root@iz2ze76ybn73dvwmdij06zz ~]# grep -v  moon monkey
One day,a little monkey is playing by the well.一天,有只小猴子在井边玩儿.
He looks in the well and shouts :它往井里一瞧,高喊道：
An older monkeys runs over,takes a look,and says,一只大猴子跑来一看,说,
And olderly monkey comes over.老猴子也跑过来.
He is very surprised as well and cries out:他也非常惊奇,喊道：
A group of monkeys run over to the well .一群猴子跑到井边来,
“月亮掉在井里头啦!快来!让我们把它捞起来!”
Then,the oldest monkey hangs on the tree up side down ,with his feet on the branch .
然后,老猴子倒挂在大树上,
And he pulls the next monkey’s feet with his hands.拉住大猴子的脚,
All the other monkeys follow his suit,其他的猴子一个个跟着,
它们一只连着一只直到井里.
它兴奋地大叫：“别蠢了!月亮还好好地挂在天上呢!”
```

&emsp; **直接查找符合条件的行数**  

```text
[root@iz2ze76ybn73dvwmdij06zz ~]# grep -c  moon monkey
8
```

&emsp; **忽略大小写查找符合条件的行数**  

&emsp; 先来看一下直接查找的结果  

```text
[root@iz2ze76ybn73dvwmdij06zz ~]# grep my monkey
```

&emsp; 忽略大小写查看  

```text
[root@iz2ze76ybn73dvwmdij06zz ~]# grep -i my monkey
“Oh!My god!The moon has fallen into the well!” “噢!我的天!月亮掉到井里头啦!”
```

&emsp; **查找符合条件的行并输出行号**  

```text
[root@iz2ze76ybn73dvwmdij06zz ~]# grep -n monkey monkey
1:One day,a little monkey is playing by the well.一天,有只小猴子在井边玩儿.
4:An older monkeys runs over,takes a look,and says,一只大猴子跑来一看,说,
6:And olderly monkey comes over.老猴子也跑过来.
9:A group of monkeys run over to the well .一群猴子跑到井边来,
13:Then,the oldest monkey hangs on the tree up side down ,with his feet on the branch .
15:And he pulls the next monkey’s feet with his hands.拉住大猴子的脚,
16:All the other monkeys follow his suit,其他的猴子一个个跟着,
19:Just before they reach the moon,the oldest monkey raises his head and happens to see the moon in the sky,正好他们摸到月亮的时候,老猴子抬头发现月亮挂在天上呢
```

&emsp; **查找开头是J的行**  

```text
[root@iz2ze76ybn73dvwmdij06zz ~]# grep '^J' monkey
Just before they reach the moon,the oldest monkey raises his head and happens to see the moon in the sky,正好他们摸到月亮的时候,老猴子抬头发现月亮挂在天上呢
```

&emsp; **查找结尾是呢的行**  

```text
[root@iz2ze76ybn73dvwmdij06zz ~]# grep "呢$" monkey
Just before they reach the moon,the oldest monkey raises his head and happens to see the moon in the sky,正好他们摸到月亮的时候,老猴子抬头发现月亮挂在天上呢
```

## 1.2. sed  
&emsp; **<font color = "clime">sed是一种流编辑器，是一款处理文本比较优秀的工具，可以结合正则表达式一起使用。</font>**  
&emsp; **sed执行过程：**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/Linux/linux/linux-4.png)   

&emsp; **sed命令**  
&emsp; 语法 :  sed \[选项]... {命令集} \[输入文件]...  
&emsp; 常用命令:  

```text
    d  删除选择的行    
    s   查找    
    y  替换
    i   当前行前面插入一行
    a  当前行后面插入一行
    p  打印行       
    q  退出     
```

&emsp; 替换符:

    数字 ：替换第几处    
    g :  全局替换    
    \1:  子串匹配标记，前面搜索可以用元字符集\(..\)
    &:  保留搜索刀的字符用来替换其他字符

&emsp; 操作:  

&emsp; **替换**  

&emsp; 查看文件:  

```text
➜  happy cat word
Twinkle, twinkle, little star
How I wonder what you are
Up above the world so high
Like a diamond in the sky
When the blazing sun is gone
```

&emsp; 替换：

```text
➜  happy sed 's/little/big/' word
Twinkle, twinkle, big star
How I wonder what you are
Up above the world so high
Like a diamond in the sky
When the blazing sun is gone
```

&emsp; 查看文本:  

```text
➜  happy cat word1
Oh if there's one thing to be taught
it's dreams are made to be caught
and friends can never be bought
Doesn't matter how long it's been
I know you'll always jump in
'Cause we don't know how to quit
```

&emsp; 全局替换:  

```text
➜  happy sed 's/to/can/g' word1
Oh if there's one thing can be taught
it's dreams are made can be caught
and friends can never be bought
Doesn't matter how long it's been
I know you'll always jump in
'Cause we don't know how can quit
```

&emsp; 按行替换（替换2到最后一行)  

```text
➜  happy sed '2,$s/to/can/' word1
Oh if there's one thing to be taught
it's dreams are made can be caught
and friends can never be bought
Doesn't matter how long it's been
I know you'll always jump in
'Cause we don't know how can quit
```

&emsp; **删除:**  

&emsp; 查看文本:  

```text
➜  happy cat word
Twinkle, twinkle, little star
How I wonder what you are
Up above the world so high
Like a diamond in the sky
When the blazing sun is gone
```

&emsp; 删除:  

```text
➜  happy sed '2d' word
Twinkle, twinkle, little star
Up above the world so high
Like a diamond in the sky
When the blazing sun is gone
```

&emsp; 显示行号:  

```text
➜  happy sed '=;2d' word
1
Twinkle, twinkle, little star
2
3
Up above the world so high
4
Like a diamond in the sky
5
When the blazing sun is gone
```

&emsp; 删除第2行到第四行:  

```text
➜  happy sed '=;2,4d' word
1
Twinkle, twinkle, little star
2
3
4
5
When the blazing sun is gone
```

&emsp; **添加行：**  

&emsp; 向前插入:  

```text
➜  happy echo "hello" | sed 'i\kitty'
kitty
hello
```

&emsp; 向后插入:  

```text
➜  happy echo "kitty" | sed 'i\hello'
hello
kitty
```

&emsp; **修改行:**  

&emsp; 替换第二行为hello kitty  

```text
➜  happy sed '2c\hello kitty' word
Twinkle, twinkle, little star
hello kitty
Up above the world so high
Like a diamond in the sky
When the blazing sun is gone
```

&emsp; 替换第二行到最后一行为hello kitty  

```text
➜  happy sed '2,$c\hello kitty' word
Twinkle, twinkle, little star
hello kitty
```

&emsp; **写入行**  

```text
把带star的行写入c文件中,c提前创建  

➜  happy sed -n '/star/w c' word
➜  happy cat c
Twinkle, twinkle, little star
```

&emsp; 退出  

&emsp; 打印3行后，退出sed  

```text
➜  happy sed '3q' word
Twinkle, twinkle, little star
How I wonder what you are
Up above the world so high
```


## 1.3. awk
&emsp; 比起sed和grep，awk不仅仅是一个小工具，也可以算得上一种小型的编程语言了，支持if判断分支和while循环语句还有它的内置函数等，是一个要比grep和sed更强大的文本处理工具，但也就意味着要学习的东西更多了。  

### 1.3.1. 语法  

&emsp; 常用  

```text
Usage: awk [POSIX or GNU style options] -f progfile [--] file ...
Usage: awk [POSIX or GNU style options] [--] 'program' file ...
```

### 1.3.2. 域  
&emsp; 类似数据库列的概念，但它是按照序号来指定的，比如第一个列就是1，第二列就是2，依此类推。$0就是输出整个文本的内容。默认用空格作为分隔符，当然可以自己通过-F设置适合自己情况的分隔符。  
&emsp; 提前自己编了一段数据，学生以及学生成绩数据表。  

|列数	|名称|	描述|
|---|---|---|
|1	|Name	|姓名|
|2	|Math	|数学|
|3	|Chinese|	语文|
|4	|English|	英语|
|5	|History|	历史|
|6	|Sport	|体育|
|8	|Grade	|班级|

&emsp; "Name  Math  Chinese  English History  Sport grade 输出整个文本   

```text
[root@iz2ze76ybn73dvwmdij06zz ~]# awk '{print $0}' students_store
Xiaoka          60   80    40    90   77  class-1
Yizhihua        70    66   50    80   90  class-1
kerwin          80    90   60    70   60  class-2
Fengzheng       90    78    62   40   62  class-2
```

&emsp; 输出第一列（姓名列)  

```text
[root@iz2ze76ybn73dvwmdij06zz ~]# awk '{print $1}' students_store
Xiaoka
Yizhihua
kerwin
Fengzheng
```

### 1.3.3. 模式&动作

    awk '{[pattern] action}' {filenames}    

&emsp; **模式**  
&emsp; pattern 可以是 条件语句、正则。  

&emsp; 模式的两个特殊字段 BEGIN 和 END (不指定时匹配或打印行数)  

* BEGIN ：一般用来打印列名称。
* END : 一般用来打印总结性质的字符。

&emsp; **动作**

&emsp; action 在{}内指定，一般用来打印，也可以是一个代码段。  

&emsp; **示例**  

&emsp; 给上面的文本加入标题头:  

```text
[root@iz2ze76ybn73dvwmdij06zz ~]#  awk 'BEGIN {print "Name     Math  Chinese  English History  Sport grade\n----------------------------------------------"} {print $0}' students_store

Name         Math  Chinese  English History  Sport  grade
----------------------------------------------------------
Xiaoka       60    80       40      90     77    class-1
Yizhihua     70    66       50      80     90    class-1
kerwin       80    90       60      70     60    class-2
Fengzheng    90    78       62      40     62    class-2
```

&emsp; 仅打印姓名、数学成绩、班级信息，再加一个文尾(再接再厉):  

```text
[root@iz2ze76ybn73dvwmdij06zz ~]# awk 'BEGIN {print "Name   Math  grade\n---------------------"} {print $1 2 "\t" $7} END {print "continue to exert oneself"}' students_store

Name     Math  grade
---------------------
Xiaoka   60   class-1
Yizhihua 70   class-1
kerwin   80   class-2
Fengzheng 90  class-2
continue to exert oneself
```

### 1.3.4. 结合正则  
&emsp; 使用方法：  
&emsp; 符号 ~  后接正则表达式  
&emsp; 此时再加入一条后来的新同学，并且没有分班。  
&emsp; 先来看下现在的数据  

```
[root@iz2ze76ybn73dvwmdij06zz ~]# cat students_store
Xiaoka       60   80    40    90   77  class-1
Yizhihua     70    66   50    80   90  class-1
kerwin       80    90   60    70   60  class-2
Fengzheng    90    78   62    40   62  class-2
xman         -     -     -     -   -    -
```

&emsp; 模糊匹配|查询已经分班的学生  

```
[root@iz2ze76ybn73dvwmdij06zz ~]# awk '$0 ~/class/' students_store
Xiaoka       60   80    40    90   77  class-1
Yizhihua     70    66   50    80   90  class-1
kerwin       80    90   60     70  60  class-2
Fengzheng    90    78   62     40  62  class-2
```

&emsp; 精准匹配|查询1班的学生  

```
[root@iz2ze76ybn73dvwmdij06zz ~]# awk '$7=="class-1" {print $0}'  students_store
Xiaoka       60   80    40    90   77  class-1
Yizhihua     70    66   50    80   90  class-1
```

&emsp; 反向匹配|查询不是1班的学生  

```
[root@iz2ze76ybn73dvwmdij06zz ~]# awk '$7!="class-1" {print $0}'  students_store
kerwin       80    90   60     70   60  class-2
Fengzheng    90    78    62     40  62 class-2
xman         -     -     -     -   -    -
```

&emsp; 比较操作  

&emsp; 查询数学大于80的  

```
[root@iz2ze76ybn73dvwmdij06zz ~]# awk '$2>60 {print $0}'  students_store
Yizhihua     70    66   50    80   90  class-1
kerwin       80    90   60     70   60  class-2
Fengzheng    90    78    62     40  62 class-2
```

&emsp; 查询数学大于英语成绩的  

```
[root@iz2ze76ybn73dvwmdij06zz ~]# awk '$2 > $4  {print $0}'  students_store
Xiaoka       60   80    40    90   77  class-1
Yizhihua     70    66   50    80   90  class-1
kerwin       80    90   60    70   60  class-2
Fengzheng    90    78    62   40   62  class-2
```

&emsp; 匹配指定字符中的任意字符  
&emsp; 再加一列专业，让我们来看看憨憨们的专业，顺便给最后一个新来的同学分个班吧。  
&emsp; 然后再来看下此时的数据。  

```
[root@iz2ze76ybn73dvwmdij06zz ~]# cat students_store
Xiaoka       60   80    40    90   77  class-1  Java
Yizhihua     70    66   50    80   90  class-1  java
kerwin       80    90   60     70   60  class-2 Java
Fengzheng    90    78    62     40  62 class-2  java
xman         -     -     -     -   -    class-3 php
```

&emsp; 或关系匹配|查询1班和3班的学生  

```
root@iz2ze76ybn73dvwmdij06zz ~]# awk '$0 ~/(class-1|class-3)/' students_store
Xiaoka       60   80    40    90   77  class-1  Java
Yizhihua     70    66   50    80   90  class-1  java
xman         -     -     -     -   -   class-3 php
```


&emsp; 任意字符匹配|名字第二个字母是  

    字符解释：  
    ^ : 字段或记录的开头。  
    . : 任意字符。  

```
root@iz2ze76ybn73dvwmdij06zz ~]# awk '$0 ~/(class-1|class-3)/' students_store
Xiaoka       60   80    40    90   77  class-1  Java
Yizhihua     70    66   50    80   90  class-1  java
xman         -     -     -     -   -    class-3 php
```

### 1.3.5. 复合表达式  
&emsp; && AND  
&emsp; 查询数学成绩大于60并且语文成绩也大于60的童鞋。  

```
[root@iz2ze76ybn73dvwmdij06zz ~]# awk '{ if ($2 > 60 && $3 > 60) print $0}' students_store
Yizhihua     70    66   50    80   90  class-1  java
kerwin       80    90   60    70   60  class-2  Java
Fengzheng    90    78    62   40   62  class-2  java
```

&emsp; ||  OR
&emsp; 查询数学大于80或者语文大于80的童鞋。  

```
[root@iz2ze76ybn73dvwmdij06zz ~]#  awk '{ if ($2 > 80 || $4 > 80) print $0}' students_store
Fengzheng    90    78    62     40  62 class-2  java
```

### 1.3.6. printf格式化输出  
&emsp; 除了能达到功能以外，一个好看的格式也是必不可少的，因此格式化的输出看起来会更舒服。  
&emsp; 语法  

    printf ([格式]，参数)

&emsp; printf %x(格式) 具体参数 x代表具体格式  

|符号	|说明|
|---|---|
|-	|左对齐|
|Width	|域的步长|
|.prec	|最大字符串长度或小数点右边位数|

&emsp; 格式转化符  
&emsp; 其实和其他语言大同小异的    
&emsp; 常用格式  

|符号	|描述|
|---|---|
|%c|	ASCII|
|%d	|整数|
|%o	|八进制|
|%x	|十六进制数|
|%f	|浮点数|
|%e	|浮点数（科学记数法)|
|%s	|字符串|
|%g	|决定使用浮点转化e/f|

&emsp; **具体操作示例**  
&emsp; ASCII码  

    [root@iz2ze76ybn73dvwmdij06zz ~]# echo "66" | awk '{printf "%c\n",$0}'
    B

&emsp; 浮点数  

    [root@iz2ze76ybn73dvwmdij06zz ~]# awk 'BEGIN {printf "%f\n",100}'
    100.000000

&emsp; 16进制数  

    [root@iz2ze76ybn73dvwmdij06zz ~]# awk 'BEGIN {printf "%x",996}'
    3e4

### 1.3.7. 内置变量  

&emsp; **频率较高常用内置变量**  

&emsp; NF：记录浏览域的个数，在记录被读后设置。  
&emsp; NR：已读的记录数。  
&emsp; FS：设置输入域分隔符  
&emsp; ARGC：命令行参数个数，支持命令行传入。  
&emsp; RS：控制记录分隔符  
&emsp; FIlENAME：awk当前读文件的名称  

&emsp; **操作**  
&emsp; 输出学生成绩表和域个数以及已读记录数。  

```
[root@iz2ze76ybn73dvwmdij06zz ~]# awk '{print $0, NF , NR}' students_store
Xiaoka       60   80    40    90   77  class-1  Java 8 1
Yizhihua     70    66   50    80   90  class-1  java 8 2
kerwin       80    90   60     70  60  class-2  Java 8 3
Fengzheng    90    78   62     40  62  class-2  java 8 4
xman         -     -     -     -   -   class-3  php  8 5
```

### 1.3.8. 内置函数
&emsp; 常用函数  

* length(s)  返回s长度  
* index(s,t) 返回s中字符串t第一次出现的位置  
* match (s,r) s中是否包含r字符串  
* split(s,a,fs) 在fs上将s分成序列a  
* gsub(r,s) 用s代替r，范围全文本  
* gsub(r,s,t) 范围t中，s代替r  
* substr(s,p) 返回字符串s从第p个位置开始后面的部分（下标是从1 开始算的，大家可以自己试试）  
* substr(s,p,n) 返回字符串s从第p个位置开始后面n个字符串的部分  

&emsp; 操作  
&emsp; length  

    [root@iz2ze76ybn73dvwmdij06zz ~]# awk 'BEGIN {print length(" hello,im xiaoka")}'
    16

&emsp; index  

    [root@iz2ze76ybn73dvwmdij06zz ~]# awk 'BEGIN {print index("xiaoka","ok")}'
    4

&emsp; match  

    [root@iz2ze76ybn73dvwmdij06zz ~]# awk 'BEGIN {print match("姓名","va小")}'
    3

&emsp; gsub  

    [root@iz2ze76ybn73dvwmdij06zz ~]# awk 'gsub("Xiaoka","xk") {print $0}' students_store
    xk       60   80    40    90   77  class-1  Java

&emsp; substr(s,p)  

    [root@iz2ze76ybn73dvwmdij06zz ~]# awk 'BEGIN {print substr("xiaoka",3)}'
    aoka

&emsp; substr(s,p,n)  

    [root@iz2ze76ybn73dvwmdij06zz ~]# awk 'BEGIN {print substr("xiaoka",3,2)}'
    ao

&emsp; split  

    [root@iz2ze76ybn73dvwmdij06zz ~]# str="java,xiao,ka,xiu"
    [root@iz2ze76ybn73dvwmdij06zz ~]# awk 'BEGIN{split('"\"$str\""',ary,","); for(i in ary) {if(ary[i]>1) print ary[i]}}'
    xiu
    java
    xiao
    ka

### 1.3.9. awk脚本  
&emsp; 前面说过awk是可以说是一个小型编程语言。如果命令比较短我们可以直接在命令行执行，当命令行比较长的时候，可以使用脚本来处理，比命令行的可读性更高，还可以加上注释。  
&emsp; 写一个完整的awk脚本并执行步骤  

1. 先创建一个awk文件  

    [root@iz2ze76ybn73dvwmdij06zz ~]# vim printname.awk
2. 脚本第一行要指定解释器  

    #!/usr/bin/awk -f
3. 编写脚本内容，打印一下名称  

    [root@iz2ze76ybn73dvwmdij06zz ~]# cat printname.awk
    #!/usr/bin/awk -f
    #可以加注释了，哈哈
    BEGIN { print "my name is 姓名"}
4. 既然是脚本，必不可少的可执行权限安排上~  

    [root@iz2ze76ybn73dvwmdij06zz ~]# chmod +x printname.awk
    [root@iz2ze76ybn73dvwmdij06zz ~]# ll printname.awk
    -rwxr-xr-x 1 root root 60 7月   1 15:23 printname.awk
5. 有了可执行权限，我们来执行下看结果  

    [root@iz2ze76ybn73dvwmdij06zz ~]# ./printname.awk
    my name is 姓名
