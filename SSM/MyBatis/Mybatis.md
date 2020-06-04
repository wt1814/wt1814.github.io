---
title: Mybatis教程
date: 2020-04-15 00:00:00
tags:
    - Mybatis
---


# #和$的区别  
## 取值引用  
### #{}方式  
&emsp; #{}: 解析为SQL时，会将形参变量的值取出，并自动给其添加引号。例如：当实参username="Amy"时，传入下Mapper映射文件后  

```
<select id="findByName" parameterType="String" resultMap="studentResultMap">
    SELECT * FROM user WHERE username=#{value}
</select>
```

&emsp; SQL将解析为：  

    SELECT * FROM user WHERE username="Amy"  

### ${}方式  
&emsp; ${}: 解析为SQL时，将形参变量的值直接取出，直接拼接显示在SQL中  
&emsp; 例如：当实参username="Amy"时，传入下Mapper映射文件后  

```
<select id="findByName" parameterType="String" resultMap="studentResultMap">
    SELECT * FROM user WHERE username=${value}
</select>
```

&emsp; SQL将解析如下：  

    SELECT * FROM user WHERE username=Amy

&emsp; 显而该SQL无法正常执行，故需要在mppaer映射文件中的${value}前后手动添加引号，如下所示:  

```
<select id="findByName" parameterType="String" resultMap="studentResultMap">
    SELECT * FROM user WHERE username='${value}'
</select>
```

&emsp; SQL将解析为:

    SELECT * FROM user WHERE username='Amy'

## SQL注入  
&emsp; ${}方式是将形参和SQL语句直接拼接形成完整的SQL命令后，再进行编译，所以可以通过精心设计的形参变量的值，来改变原SQL语句的使用意图从而产生安全隐患，即为SQL注入攻击。现举例说明：  
&emsp; 现有Mapper映射文件如下：  

```
<select id="findByName" parameterType="String" resultMap="studentResultMap">
    SELECT * FROM user WHERE username='${value}'
</select>
```

&emsp; 当username = "' OR 1=1 OR '" 传入后，${}将变量内容直接和SQL语句进行拼接，结果如下:  

        SELECT * FROM user WHERE username='' OR 1=1 OR '';

&emsp; 显而易见，上述语句将把整个数据库内容直接暴露出来了。  

&emsp; #{}方式则是先用占位符代替参数将SQL语句先进行预编译，然后再将参数中的内容替换进来。由于SQL语句已经被预编译过，其SQL意图将无法通过非法的参数内容实现更改，其参数中的内容，无法变为SQL命令的一部分。故，***#{}可以防止SQL注入而${}却不行***。  

## 适用场景  
### #{} 和 ${} 均适用场景  
&emsp; 由于SQL注入的原因，${}和#{}在都可以使用的场景下，很明显推荐使用#{}。这里除了上文的WHERE语句例子，再介绍一个LIKE模糊查询的场景(username = "Amy"):  

```
<select id="findAddByName" parameterType="String" resultMap="studentResultMap">
    SELECT * FROM user WHERE username LIKE '%${value}%'
</select>
```

&emsp; 该SQL解析为：  

    SELECT * FROM user WHERE username LIKE '%Amy%';

&emsp; 上述通过${}虽然可以实现对包含"Amy"对模糊查询，但是不安全，可以改用#{}，如下所示：  

```
<select id="findAddByName" parameterType="String" resultMap="studentResultMap">
    SELECT * FROM USER WHERE username LIKE CONCAT('%', #{username}, '%')
</select>
```

&emsp; 该SQL解析为下文所示，其效果和上文方式一致  

    SELECT * FROM USER WHERE username LIKE CONCAT('%', 'Amy','%');  

### 只能使用${}的场景  
&emsp; 由于#{}会给参数内容自动加上引号，会在有些需要表示字段名、表名的场景下，SQL将无法正常执行。现举一例说明：  
&emsp; 期望查询结果按sex字段升序排列，参数String orderCol = "sex",mapper映射文件使用#{}：  

```
<select id="findAddByName3" parameterType="String" resultMap="studentResultMap">
    SELECT * FROM USER WHERE username LIKE '%Am%' ORDER BY #{value} ASC
</select>
```

&emsp; 则SQL解析及执行结果如下所示，很明显 ORDER 子句的字段名错误的被加上了引号，致使查询结果没有按期排序输出  

    SELECT * FROM USER WHERE username LIKE '%Am%' ORDER BY 'sex' ASC;
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Mybatis/mybatis-1.png)  

&emsp; 这时，现改为${}测试效果：  

```
<select id="findAddByName3" parameterType="String" resultMap="studentResultMap">
    SELECT * FROM USER WHERE username LIKE '%Am%' ORDER BY ${value} ASC
</select>
```

&emsp; 则SQL解析及执行结果如下所示：  

    SELECT * FROM USER WHERE username LIKE '%Am%' ORDER BY sex ASC;

![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Mybatis/mybatis-2.png)  


# 参数映射  











