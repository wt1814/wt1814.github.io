---
title: Mybatis教程
date: 2020-04-15 00:00:00
tags:
    - Mybatis
---

<!-- TOC -->

- [1. #和$的区别](#1-和的区别)
    - [1.1. 取值引用](#11-取值引用)
        - [1.1.1. #{}方式](#111-方式)
        - [1.1.2. ${}方式](#112-方式)
    - [1.2. ※※※SQL注入](#12-※※※sql注入)
    - [1.3. 适用场景](#13-适用场景)
        - [1.3.1. #{} 和 ${} 均适用场景](#131--和--均适用场景)
        - [1.3.2. 只能使用${}的场景](#132-只能使用的场景)
- [2. 参数映射](#2-参数映射)
    - [2.1. parameterType与@Param](#21-parametertype与param)
        - [2.1.1. 占位符](#211-占位符)
        - [2.1.2. Map传参](#212-map传参)
        - [2.1.3. 映射器注解@Param](#213-映射器注解param)
        - [2.1.4. 标签paramType](#214-标签paramtype)
    - [2.2. resultType和resultMap](#22-resulttype和resultmap)
        - [2.2.1. resultType](#221-resulttype)
        - [2.2.2. resultMap](#222-resultmap)
        - [2.2.3. resultType与resultMap](#223-resulttype与resultmap)
- [3. Mybatis标签](#3-mybatis标签)
    - [3.1. 定义sql语句](#31-定义sql语句)
    - [3.2. 控制动态sql拼接](#32-控制动态sql拼接)
        - [3.2.1. Foreach标签](#321-foreach标签)
        - [3.2.2. if标签的使用](#322-if标签的使用)
        - [3.2.3. choose标签的使用](#323-choose标签的使用)
    - [3.3. 格式化输出](#33-格式化输出)
        - [3.3.1. Trim标签](#331-trim标签)
            - [3.3.1.1. where功能](#3311-where功能)
            - [3.3.1.2. set功能](#3312-set功能)
            - [3.3.1.3. insert可选择入库](#3313-insert可选择入库)
    - [3.4. Sql标签](#34-sql标签)
    - [3.5. Bind标签](#35-bind标签)
    - [3.6. selectKey标签，自动生成主键](#36-selectkey标签自动生成主键)
- [4. Spring整合Mybatis](#4-spring整合mybatis)
- [5. Intellij IDEA中Mybatis Mapper自动注入警告的解决方案](#5-intellij-idea中mybatis-mapper自动注入警告的解决方案)
- [6. mybatis-generator](#6-mybatis-generator)
- [7. MyBatis之分页插件](#7-mybatis之分页插件)

<!-- /TOC -->

![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Mybatis/mybatis-29.png)  


# 1. #和$的区别  
## 1.1. 取值引用  
### 1.1.1. #{}方式  
&emsp; <font color = "red">#{}: 解析为SQL时，会将形参变量的值取出，并自动给其添加引号。</font>  
&emsp; 例如：当实参username="Amy"时，传入以下Mapper映射文件后  

```
<select id="findByName" parameterType="String" resultMap="studentResultMap">
    SELECT * FROM user WHERE username=#{value}
</select>
```
&emsp; SQL将解析为：  

    SELECT * FROM user WHERE username="Amy"  

### 1.1.2. ${}方式  
&emsp; <font color = "red">${}: 解析为SQL时，将形参变量的值直接取出，直接拼接显示在SQL中。</font>    
&emsp; 例如：当实参username="Amy"时，传入以下Mapper映射文件后  

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

## 1.2. ※※※SQL注入  
&emsp; **<font color = "red"> ${}方式是将形参和SQL语句直接拼接形成完整的SQL命令后，再进行编译，所以可以通过精心设计的形参变量的值，来改变原SQL语句的使用意图从而产生安全隐患，即为SQL注入攻击。 </font>**  
&emsp; 现有Mapper映射文件如下：  

```
<select id="findByName" parameterType="String" resultMap="studentResultMap">
    SELECT * FROM user WHERE username='${value}'
</select>
```

&emsp; 当username = "' OR 1=1 OR '" 传入后，${}将变量内容直接和SQL语句进行拼接，结果如下:  

    SELECT * FROM user WHERE username='' OR 1=1 OR '';

&emsp; 显而易见，上述语句将把整个数据库内容直接暴露出来了。  

&emsp; <font color = "color">#{}方式则是先用占位符代替参数将SQL语句先进行预编译，然后再将参数中的内容替换进来。由于SQL语句已经被预编译过，其SQL意图将无法通过非法的参数内容实现更改，其参数中的内容，无法变为SQL命令的一部分。故，</font><font color = "lime">**#{}可以防止SQL注入而${}却不行** 。</font>  

## 1.3. 适用场景  
### 1.3.1. #{} 和 ${} 均适用场景  
&emsp; 由于SQL注入的原因，${}和#{}在都可以使用的场景下，推荐使用#{}。这里除了上文的WHERE语句例子，再介绍一个LIKE模糊查询的场景(username = "Amy"):  

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

### 1.3.2. 只能使用${}的场景  
&emsp; <font color = "red">由于#{}会给参数内容自动加上引号，会在有些需要表示字段名、表名的场景下，SQL将无法正常执行。例如：期望查询结果按sex字段升序排列。</font>参数String orderCol = "sex",mapper映射文件使用#{}：  

```sql
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

# 2. 参数映射  
## 2.1. parameterType与@Param  
&emsp; parameterType属性用于对应的mapper.java接口方法接收的参数类型。有多个入参时，可以省略不写；mapper接口方法可以接收一个参数，可以通过使用@Param注释将多个参数绑定到一个map作为输入参数。  
&emsp; <font color = "red">parameterType适用于JavaBean及List<JavaBean>。@param适用于基本类型。</font>  

### 2.1.1. 占位符  
&emsp; DAO层的函数方法  

```
Public User selectUser(String name,String area);
```
&emsp; 对应的Mapper.xml     

```
<select id="selectUser" resultMap="BaseResultMap">
    select * from user_user_t where user_name = #{0} and user_area=#{1}
</select>
```
&emsp; 其中，占位符#{0}代表接收的是dao层中的第一个参数，#{1}代表dao层中第二参数，更多参数一致往后加即可。  

### 2.1.2. Map传参  
&emsp; 使用Map类型，此方法采用Map传多参数。  
&emsp; Dao层的函数方法：  

    Public User selectUser(Map paramMap);

&emsp; 对应的Mapper.xml：  

```
<select id=" selectUser" resultMap="BaseResultMap">
    select  *  from user_user_t   where user_name = #{userName，jdbcType=VARCHAR} and user_area=#{userArea,jdbcType=VARCHAR}
</select>
```
&emsp; Service层调用：  

```
Private Userxxx SelectUser(){
    Map paramMap=new hashMap();
    paramMap.put(“userName”,”对应具体的参数值”);
    paramMap.put(“userArea”,”对应具体的参数值”);
    Useruser=xxx. selectUser(paramMap);
}
```

### 2.1.3. 映射器注解@Param  

&emsp; Dao层的函数方法  

```
Public User selectUser(@param(“userName”)String name,@param(“userArea”)String area);
```
&emsp; 对应的Mapper.xml  

```
<select id=" selectUser" resultMap="BaseResultMap">
    select  *  from user_user_t   where user_name = #{userName，jdbcType=VARCHAR} and user_area=#{userArea,jdbcType=VARCHAR}
</select>
```
&emsp; 映射器注解@Param：简化xml配置（使用了@pram注解时，在mapper.xml不加parameterType），作用是给参数命名,参数命名后就能根据名字得到参数值，正确的将参数传入sql语句中。  

```
public Student select(@Param("aaaa") String name,@Param("bbbb")int class_id);
```
&emsp; 给入参String name命名为aaaa,然后sql语句..where s_name= #{aaaa}中就可以根据aaaa得到参数值了。  
&emsp; 自定义对象也使用@param注解。在mapper.xml中使用时，#{对象别名.属性名}，如#{user.id}。  
&emsp; @Param注解JavaBean对象：  

```
public List<user> getUserInformation(@Param("user") User user);
```
```
<select id="getUserInformation" parameterType="com.github.demo.vo.User" resultMap="userMapper">  
    select <include refid="User_Base_Column_List" /> from mo_user t where 1=1  
    <!-- 因为传进来的是对象所以这样写是取不到值得 -->  
<if test="user.userName!=null  and user.userName!=''"> and t.user_name = #{user.userName} </if>  
    <if test="user.userAge!=null  and user.userAge!=''"> and t.user_age = #{user.userAge} </if>  
</select>
```
&emsp; @Param注解的四种使用场景：  
&emsp; MyBatis只有方法中存在多个参数的时候，才需要添加@Param注解，其实这个理解是不准确的。即使MyBatis方法只有一个参数，也可能会用到@Param注解。  

* 第一种：方法有多个参数，需要@Param注解。  
* 第二种：方法参数要取别名，需要@Param注解。  
* 第三种：XML中的SQL使用了$，那么参数中也需要@Param注解。  

&emsp; $会有注入漏洞的问题，但是有的时候不得不使用$符号，例如要传入列名或者表名的时候，这个时候必须要添加@Param注解，例如：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Mybatis/mybatis-3.png)  

&emsp; 对应的XML定义如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Mybatis/mybatis-4.png)  

&emsp; 第四种，那就是动态SQL，如果在动态SQL中使用了参数作为变量，那么也需要@Param注解，即使只有一个参数。
如果在动态SQL中用到了参数作为判断条件，那么也是一定要加@Param注解的，例如如下方法：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Mybatis/mybatis-5.png)  

&emsp; 定义出来的SQL如下  ：
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Mybatis/mybatis-6.png)  

### 2.1.4. 标签paramType  
&emsp; @Param与xml配置paramType的区别：@param适用于少量不同的参数。Xml配置paramType适用于list，多个复杂参数。    


## 2.2. resultType和resultMap  
&emsp; MyBatis中在查询进行select映射的时候，返回结果集使用resultType或resultMap。resultType是直接表示返回类型的，而resultMap则是对xml文件中ResultMap的引用。  
&emsp; 当实体类中的属性名和表中的字段名不一样，怎么办？  
&emsp; 第1种：通过在查询的sql语句中定义字段名的别名，让字段名的别名和实体类的属性名一致。  

```
<select id=”selectorder” parametertype=”int” resultetype=”me.gacl.domain.order”>
    select order_id id, order_no orderno ,order_price price form orders where order_id=#{id};
</select>
```

&emsp; 第2种：通过来映射字段名和实体类属性名的一一对应的关系。  

```
<select id="getOrder" parameterType="int" resultMap="orderresultmap">
    select * from orders where order_id=#{id}
</select>
<resultMap type=”me.gacl.domain.order” id=”orderresultmap”>
    <!–用id属性来映射主键字段–>
    <id property=”id” column=”order_id”>
    <!–用result属性来映射非主键字段，property为实体类属性名，column为数据表中的属性–>
    <result property = “orderno” column =”order_no”/>
    <result property=”price” column=”order_price” />
</reslutMap>
```

### 2.2.1. resultType  
&emsp; resultType是sql映射文件中定义返回值类型，返回值有基本类型，对象类型，List类型，Map类型等。  

* 基本类型：resultType=基本类型。  
* List类型：resultType=List中元素的类型。（select * 的查询结果集，可能为元素类型，也可能为List<元素类型>）  
* Map类型单条记录：resultType =map；多条记录：resultType =Map中value的类型。  
* 对象类型：对象类型resultType直接写对象的全类名。  

### 2.2.2. resultMap  
&emsp; resultMap多用于数据库字段名称和javaBean字段名称不一致（或者没有做相应的转化）。resultMap中的Class类与resultType中的Class类名一致。    
&emsp; 注：关联查询中<association\>标签和<collection\>标签有resultMap列属性。  

### 2.2.3. resultType与resultMap  
&emsp; resultType与resultMap不能同时使用。二者的使用场景：对于查询结构需要返回的简单pojo，结果都可以映射到一致的hashMap上，即数据库列名可以精确匹配到pojo属性的。一般都用resultType。其实这里有一个隐含的构建机制。映射到resultType的结果都是MyBatis在幕后自动创建了一个resultMap来处理的。简而言之，只要resultType能干的事情resultMap都能干。  
&emsp; 二者定位是：resultType用来处理非常简单的结果集，就是列名能够与pojo属性匹配的的结果集。  
&emsp; 示例：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Mybatis/mybatis-7.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Mybatis/mybatis-8.png)  

&emsp; 也可以如上述所说使用resultMap，然后将上面的resultType转换为resultMap即可。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Mybatis/mybatis-9.png)  

&emsp; resultMap更擅长来处理复杂映射的结果集。比如一对一、一对多的复杂关系。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Mybatis/mybatis-10.png)  

&emsp; 对应的映射处理：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Mybatis/mybatis-11.png)  

# 3. Mybatis标签  
&emsp; MyBatis通过OGNL来进行动态SQL的使用的。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Mybatis/mybatis-12.png)  

## 3.1. 定义sql语句  
* select标签：  
    * id：唯一的标识符。  
    * parameterType：传给此语句的参数的全路径名或别名。 例：com.test.poso.User或user  
    * resultType：语句返回值类型或别名。注意，如果是集合，那么这里填写的是集合的泛型，而不是集合本身。 

## 3.2. 控制动态sql拼接  
### 3.2.1. Foreach标签  
&emsp; foreach标签主要用于构建in条件，可以在sql中对集合进行迭代。通常可以将之用到批量删除、批量添加等操作中。  

```
<delete id="deleteBatch">
    delete from user where id in
    <foreach collection="array" item="id" index="index" open="(" close=")" separator=",">
        #{id}
    </foreach>
</delete>
```

&emsp; 如果参数为int[] ids = {1,2,3,4,5}，那么打印之后的SQL如下：  

```
delete form user where id in (1,2,3,4,5)
```

&emsp; 属性介绍：  
* collection：collection属性的值有三个分别是list、array、map三种，分别对应的参数类型为：List、数组、map集合；  
* item：表示在迭代过程中每一个元素的别名；  
* index：表示在迭代过程中每次迭代到的位置（下标），当迭代的对象为Map时，该值为Map中的Key；  
* open：循环开头的字符串；  
* close：循环结束的字符串；  
* separator：每次循环的分隔符；  

&emsp; 在使用foreach的时候最关键的也是最容易出错的就是collection属性，该属性是必须指定的，但是在不同情况下，该属性的值是不一样的，主要有以下3种情况：  

* 如果传入的是单参数且参数类型是一个List的时候，collection属性值为list。  
* 如果传入的是单参数且参数类型是一个array数组的时候，collection的属性值为array。  
* 如果传入的参数是多个的时候，就需要把它们封装成一个Map了，当然单参数也可以封装成map，实际上如果你在传入参数的时候，在MyBatis里面也是会把它封装成一个Map的，map的key就是参数名，所以这个时候collection属性值就是传入的List或array对象在自己封装的map里面的key。  

&emsp; 针对最后一条，看一下官方说法：  
&emsp; 注意：可以将一个List实例或者数组作为参数对象传给MyBatis，当这么做的时候，MyBatis 会自动将它包装在一个 Map 中并以名称为键。List 实例将会以“list”作为键，而数组实例的键将是“array”。  
&emsp; 所以，不管是多参数还是单参数的list,array类型，都可以封装为map进行传递。如果传递的是一个List，则mybatis会封装为一个list为key，list值为object的map，如果是array，则封装成一个array为key，array的值为object的map，如果自己封装呢，则colloection里放的是自己封装的map里的key值。  

```
//mapper中我们要为这个方法传递的是一个容器,将容器中的元素一个一个的
//拼接到xml的方法中就要使用这个forEach这个标签了
public List<Entity> queryById(List<String> userids);

//对应的xml中如下
<select id="queryById" resultMap="BaseReslutMap" >
    select * FROM entity
    where id in
    <foreach collection="userids" item="userid" index="index" open="(" separator="," close=")">
        #{userid}
    </foreach>
</select>
```

### 3.2.2. if标签的使用  
&emsp; if标签通常用于WHERE语句中，通过判断参数值来决定是否使用某个查询条件，也经常用于UPDATE语句中判断是否更新某一个字段，还可以在INSERT语句中用来判断是否插入某个字段的值。例：  

```
<select id="getStudentListLikeName" parameterType="StudentEntity" resultMap="studentResultMap">
    SELECT * from STUDENT_TBL ST WHERE ST.STUDENT_NAME LIKE CONCAT(CONCAT('%', #{studentName}),'%')
</select>
```

&emsp; 但是此时如果studentName是null或空字符串，此语句很可能报错或查询结果为空。此时使用if动态sql语句先进行判断，如果值为null或等于空字符串，就不进行此条件的判断。修改为：  

```
<select id=" getStudentListLikeName " parameterType="StudentEntity" resultMap="studentResultMap">
    SELECT * from STUDENT_TBL ST
    <if test="studentName!=null and studentName!='' ">
        WHERE ST.STUDENT_NAME LIKE CONCAT(CONCAT('%', #{studentName}),'%')
    </if>
</select>
```

### 3.2.3. choose标签的使用   
&emsp; choose when otherwise 标签实现if else的逻辑。一个choose标签至少有一个when，最多一个otherwise。    
&emsp; if是与(and)的关系，而choose是或（or）的关系。  


## 3.3. 格式化输出  
&emsp; 三种格式化输出：where、set、trim。  

### 3.3.1. Trim标签   
&emsp; trim是更灵活的去处多余关键字的标签，他可以实践where和set的效果。   
&emsp; trim标记是一个格式化的标记，是更灵活的去处多余关键字的标签。[在 WHERE条件中使用if标签] SQL的时候，where 1=1这个条件不希望存在。可以完成set或者是where标记的功能（where、set后的判断条件全为是否为null状态，没有一个固定的判断条件）。  

&emsp; trim的几个属性：  

* prefix: 当trim元素包含有内容时，增加prefix所指定的前缀；  
* prefixOverrides: 当trim元素包含有内容时，去除prefixOverrides指定的前缀；  
* suffix：当trim元素包含有内容时，增加suffix所指定的后缀；  
* suffixOverrides：当trim元素包含有内容时，去除suffixOverrides指定的后缀；  

#### 3.3.1.1. where功能  

```
select * from user
<trim prefix="WHERE" prefixoverride="AND |OR">
    <if test="name != null and name.length()>0"> AND name=#{name}</if>
    <if test="gender != null and gender.length()>0"> AND gender=#{gender}</if>
</trim>
```
&emsp; 假如说name和gender的值都不为null的话打印的SQL为：select * from user where name = 'xx' and gender = 'xx'。在红色标记的地方是不存在第一个and的。  

#### 3.3.1.2. set功能  

```
update user
<trim prefix="set" suffixoverride="," suffix=" where id = #{id} ">
    <if test="name != null and name.length()>0"> name=#{name} , </if>
    <if test="gender != null and gender.length()>0"> gender=#{gender} ,  </if>
</trim>
```
&emsp; 假如说name和gender的值都不为null的话打印的SQL为：update user set name='xx', gender='xx' where id='x'。在红色标记的地方不存在逗号，而且自动加了一个set前缀和where后缀。  

#### 3.3.1.3. insert可选择入库  

```
<!-- 可选择入库 -->
<insert id="insertSelective" parameterType="com.cgd.commodity.po.AgreementApproveLogPO">
    insert into C_AGREEMENT_APPROVE_LOG
    <trim prefix="(" suffix=")" suffixOverrides=",">
        <if test="changeId != null">
            CHANGE_ID,
        </if>
        <if test="agreementId != null">
            AGREEMENT_ID,
        </if>
        <if test="changeCode != null">
            CHANGE_CODE,
        </if>
    </trim>
    <trim prefix="values (" suffix=")" suffixOverrides=",">
        <if test="changeId != null">
            #{changeId,jdbcType=BIGINT},
        </if>
        <if test="agreementId != null">
            #{agreementId,jdbcType=BIGINT},
        </if>
        <if test="changeCode != null">
            #{changeCode,jdbcType=VARCHAR},
        </if>
    </trim>
</insert>
```

## 3.4. Sql标签  
```
<sql id="Base_Column_List" >  </sql>
select <include refid="Base_Column_List" /> from tableA
```

## 3.5. Bind标签  
&emsp; bind元素可以从OGNL表达式中创建一个变量并将其绑定到上下文。  

```
<select id="selectBlogsLike" resultType="Blog">
    <bind name="pattern" value="'%' + _parameter.getTitle() + '%'" />
    SELECT * FROM BLOG WHERE title LIKE #{pattern}
</select>
```

## 3.6. selectKey标签，自动生成主键
&emsp; 在insert语句中，在Oracle经常使用序列、在MySQL中使用函数来自动生成插入表的主键，而且需要方法能返回这个生成主键。使用myBatis的selectKey标签可以实现这个效果。  
&emsp; 下面例子，使用mysql数据库自定义函数nextval('student')，用来生成一个key，并把他设置到传入的实体类中的studentId属性上。所以在执行完此方法后，边可以通过这个实体类获取生成的key。  

```
<!-- 插入学生 自动主键-->
<insert id="createStudentAutoKey" parameterType="liming.student.manager.data.model.StudentEntity" keyProperty="studentId">
    <selectKey keyProperty="studentId" resultType="String" order="BEFORE">
        select nextval('student')
    </selectKey>
    INSERT INTO STUDENT_TBL(STUDENT_ID, STUDENT_NAME, STUDENT_SEX, STUDENT_BIRTHDAY, STUDENT_PHOTO, CLASS_ID, PLACE_ID)
    VALUES 
    (#{studentId}, #{studentName}, #{studentSex}, #{studentBirthday}, #{studentPhoto, javaType=byte[], jdbcType=BLOB, typeHandler=org.apache.ibatis.type.BlobTypeHandler}, #{classId}, #{placeId})
</insert>
```
&emsp; 调用接口方法，和获取自动生成key  

```
StudentEntity entity = new StudentEntity();  
entity.setStudentName("黎明你好");
entity.setStudentSex(1);
entity.setStudentBirthday(DateUtil.parse("1985-05-28"));
entity.setClassId("20000001");
entity.setPlaceId("70000001");
this.dynamicSqlMapper.createStudentAutoKey(entity);
System.out.println("新增学生ID: " + entity.getStudentId());
```

----
# 4. Spring整合Mybatis  
&emsp; Spring的 classpath 通配符加载Mybatis配置文件(支持指定多个文件写法)  

    classpath:app-Beans.xml
    说明：无通配符，必须完全匹配
    
    classpath:App?-Beans.xml
    说明：匹配一个字符，例如 App1-Beans.xml 、 App2-Beans.xml
    
    classpath:user/*/Base-Beans.xml
    说明：匹配零个或多个字符串（只针对名称，不匹配目录分隔符等），例如：user/a/Base-Beans.xml 、 user/b/Base-Beans.xml ，但是不匹配 user/Base-Beans.xml
    
    classpath:user/**/Base-Beans.xml
    说明：匹配路径中的零个或多个目录，例如：user/a/ab/abc/Base-Beans.xml，同时也能匹配 user/Base-Beans.xml
    
    classpath:**/*-Beans.xml
    说明：表示在所有的类路径中查找和加载文件名以“-Beans.xml”结尾的配置文件，但重复的文件名只加载其中一个，视加载顺序决定
    
    classpath*:user/**/*-Beans.xml
    classpath*:**/*-Beans.xml
    说明：“classpath*:”表示加载多个资源文件，即使重名也会被加载，比如app1.jar中有一个config-Beans.xml，app2.jar中也有一个config-Beans.xml，这个时候，两个都会加载。


----

# 5. Intellij IDEA中Mybatis Mapper自动注入警告的解决方案   
...

# 6. mybatis-generator  
&emsp; mybatis-generator，能够生成PO类，能生成mapper映射文件（其中包括基本的增删改查功能）、能生成mapper接口。  

# 7. MyBatis之分页插件  
...


