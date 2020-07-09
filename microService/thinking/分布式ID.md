---
title: 分布式ID
date: 2020-05-10 00:00:00
tags:
    - 分布式
---

<!-- TOC -->

- [1. 分布式ID](#1-分布式id)
- [2. 分布式ID常见生成方案](#2-分布式id常见生成方案)
    - [2.1. UUID](#21-uuid)
    - [2.2. 利用数据库生成](#22-利用数据库生成)
        - [2.2.1. MySql主键自增](#221-mysql主键自增)
        - [2.2.2. MySQL多实例主键自增](#222-mysql多实例主键自增)
        - [2.2.3. 基于数据库的号段模式](#223-基于数据库的号段模式)
    - [2.3. 利用中间件生成](#23-利用中间件生成)
        - [2.3.1. 基于Redis实现](#231-基于redis实现)
    - [2.4. 雪花SnowFlake算法](#24-雪花snowflake算法)
        - [2.4.1. snowflake算法实现](#241-snowflake算法实现)
    - [2.5. 开源分布式ID算法](#25-开源分布式id算法)
        - [2.5.1. 百度uid-generator](#251-百度uid-generator)
            - [2.5.1.1. uid-generator使用教程](#2511-uid-generator使用教程)
        - [2.5.2. 美团Leaf](#252-美团leaf)
        - [2.5.3. 滴滴Tinyid](#253-滴滴tinyid)

<!-- /TOC -->

# 1. 分布式ID  
&emsp; 在业务开发中需要使用一些id。分布式系统的全局唯一ID称为分布式ID。  
&emsp; 分布式ID需要满足的条件：  

* 全局唯一：必须保证ID是全局性唯一的，基本要求。  
* 高性能：高可用低延时，ID生成响应要块，否则反而会成为业务瓶颈。  
* 高可用。  
* 好接入：要秉着拿来即用的设计原则，在系统设计和实现上要尽可能的简单。  
* 趋势递增：根据具体业务场景，一般不严格要求。  
* 可反解：一个ID生成之后，就会伴随着信息终身。排错分析的时候，需要查验，这时候一个可反解的ID可以帮上很多忙。  

# 2. 分布式ID常见生成方案  

&emsp; 分布式ID常见生成方案有以下几种：  

* UUID
* 数据库自增ID
* 数据库多主模式
* 数据库号段模式
* Redis
* 雪花算法(SnowFlake)
* 百度(Uidgenerator)
* 美团(Leaf)
* 滴滴出品(TinyID)  

## 2.1. UUID  
&emsp; ***生产随机数的方式：***  

* Math.random()，0到1之间随机数；  
* java.util.Random，伪随机数（线性同余法生成）；  
* java.security.SecureRandom，真随机数；  
* java.util.concurrent.ThreadLocalRandom，每一个线程有一个独立的随机数生成器。  

&emsp; ***优点：***  

* 不需要第三方组件，无单点的风险，代码实现简单；  
* 本机生成，没有网络消耗；  
* 因为是全球唯一的ID，所以迁移数据容易。  

&emsp; ***缺点：***  

* 每次生成的ID是无序的，相对来说还会影响性能（比如 MySQL 的 InnoDB 引擎，如果UUID作为数据库主键，其无序性会导致数据位置频繁变动）；  
* UUID的字符串存储，查询效率慢；  
* 长度长，存储空间大；  
* ID本身无业务含义，不可读。  

&emsp; ***应用场景：***  

* 适用于类似生成token令牌的场景；  
* 不适用一些要求有趋势递增的ID场景。  

---
## 2.2. 利用数据库生成  
### 2.2.1. MySql主键自增  
&emsp; 这个方案利用了MySQL的主键自增auto_increment，默认每次ID加1。  
&emsp; ***优点：***  

* 数字化，id递增；  
* 查询效率高；  
* 具有一定的业务可读。  

&emsp; ***缺点：***  

* 存在单点问题，如果mysql挂了，就无法生成ID；  
* 数据库压力大，高并发抗不住。  

### 2.2.2. MySQL多实例主键自增  
&emsp; 这个方案解决了mysql的单点问题，在auto_increment基础上，设置step步长。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/problems/problem-18.png)  
&emsp; 每台的初始值分别为1,2,3...N，步长为N（这个案例步长为4）。  
&emsp; ***优点：*** 解决了单点问题。  
&emsp; ***缺点：*** 一旦把步长定好后，就无法扩容；而且单个数据库的压力大，数据库自身性能无法满足高并发。  
&emsp; ***应用场景：*** 数据不需要扩容的场景。  

### 2.2.3. 基于数据库的号段模式  
&emsp; 号段模式是当下分布式ID生成器的主流实现方式之一，号段模式可以理解为从数据库批量的获取自增ID，每次从数据库取出一个号段范围，例如 (1,1000] 代表1000个ID，具体的业务服务将本号段，生成1~1000的自增ID并加载到内存。表结构如下：  

```sql
CREATE TABLE id_generator (
  id int(10) NOT NULL,
  max_id bigint(20) NOT NULL COMMENT '当前最大id',
  step int(20) NOT NULL COMMENT '号段的布长',
  biz_type    int(20) NOT NULL COMMENT '业务类型',
  version int(20) NOT NULL COMMENT '版本号',
  PRIMARY KEY (`id`)
) 
```
&emsp; biz_type ：代表不同业务类型  
&emsp; max_id ：当前最大的可用id  
&emsp; step ：代表号段的长度  
&emsp; version ：是一个乐观锁，每次都更新version，保证并发时数据的正确性  

|id|biz_type|max_id|step|version|
|---|---|---|---|---|
|1|101|1000|2000|0|

&emsp; 等这批号段ID用完，再次向数据库申请新号段，对max_id字段做一次update操作，update max_id= max_id + step，update成功则说明新号段获取成功，新的号段范围是(max_id ,max_id +step]。

```sql
update id_generator set max_id = #{max_id+step}, version = version + 1 where version = # {version} and biz_type = XXX
```
&emsp; 由于多业务端可能同时操作，所以采用版本号version乐观锁方式更新，这种分布式ID生成方式不强依赖于数据库，不会频繁的访问数据库，对数据库的压力小很多。  

---
## 2.3. 利用中间件生成  
&emsp; 可以使用Redis、MongoDB、zookeeper生成分布式ID。  

### 2.3.1. 基于Redis实现  
&emsp; redis单机使用incr函数生成自增ID；<font color = "red">redis集群使用lua脚本生成，或使用org.springframework.data.redis.support.atomic.RedisAtomicLong生成。</font>  
&emsp; ***优点：*** 有序递增，可读性强。  
&emsp; ***缺点：*** 占用带宽，每次要向redis进行请求。

---
## 2.4. 雪花SnowFlake算法  
&emsp; snowflake是Twitter开源的分布式ID生成算法。可以本地生成，并且生成的long类型ID递增。  
&emsp; snowflake算法所生成的ID结构：正数位（占1比特）+ 时间戳（占41比特）+ 机器ID（占5比特）+ 数据中心（占5比特）+ 自增值（占12比特），总共64比特组成的一个Long类型。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/problems/problem-19.png)  
&emsp; 整个结构是64位，所以在Java中可以使用long来进行存储。该算法实现基本就是二进制操作，单机每秒内理论上最多可以生成1024*(2^12)，也就是409.6万个ID(1024 X 4096 = 4194304)  

* 1位标识符：由于long基本类型在Java中是带符号的，最高位是符号位，正数是0，负数是1，所以id一般是正数，最高位是0。  
* 41位时间戳(毫秒级)：41位时间截不是存储当前时间的时间截，而是存储时间截的差值（当前时间截 - 开始时间截 )得到的值，这里的的开始时间截，一般是id生成器开始使用的时间，由程序来指定。  
* 10位机器标识码：可以部署在1024个节点，如果机器分机房（IDC）部署，这10位可以由5位机房ID (datacenterId)和5位机器ID(workerId)组成。  
* 12位序列：毫秒内的计数，12位的计数顺序号支持每个节点每毫秒(同一机器，同一时间截)产生4096个ID序号。  

&emsp; ***snowflake算法优点：***  
1. 生成ID时不依赖于DB，完全在内存生成，高性能高可用。  
2. ID呈趋势递增，后续插入索引树的时候性能较好。  
3. 可以根据自身业务特性分配bit位，非常灵活。  

&emsp; ***snowflake算法缺点：*** <font color="red">依赖于系统时钟的一致性。如果某台机器的系统时钟回拨，有可能造成ID冲突，或者ID乱序。</font>  

### 2.4.1. snowflake算法实现  
&emsp; 算法代码：  

```java
/**
 * Twitter_Snowflake
 * SnowFlake的结构如下(每部分用-分开):
 * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000 <br>
 * 1位标识，由于long基本类型在Java中是带符号的，最高位是符号位，正数是0，负数是1，所以id一般是正数，最高位是0<br>
 * 41位时间截(毫秒级)，注意，41位时间截不是存储当前时间的时间截，而是存储时间截的差值（当前时间截 - 开始时间截)
 * 得到的值），这里的的开始时间截，一般是我们的id生成器开始使用的时间，由我们程序来指定的（如下下面程序IdWorker类的
 startTime属性）。41位的时间截，可以使用69年，年T = (1L << 41) / (1000L * 60 * 60 * 24 * 365) = 69<br>
 * 10位的数据机器位，可以部署在1024个节点，包括5位datacenterId和5位workerId<br>
 * 12位序列，毫秒内的计数，12位的计数顺序号支持每个节点每毫秒(同一机器，同一时间截)产生4096个ID序号<br>
 * 加起来刚好64位，为一个Long型。<br>
 * SnowFlake的优点是，整体上按照时间自增排序，并且整个分布式系统内不会产生ID碰撞(由数据中心ID和机器ID作区分)，并且效率较高，经测试，SnowFlake每秒能够产生26万ID左右。
 */
public class SnowflakeIdWorker {

    // ==============================Fields===========================================
    /** 开始时间截 (2015-01-01) */
    private final long twepoch = 1420041600000L;

    /** 机器id所占的位数 */
    private final long workerIdBits = 5L;

    /** 数据标识id所占的位数 */
    private final long datacenterIdBits = 5L;

    /** 支持的最大机器id，结果是31 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数) */
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);

    /** 支持的最大数据标识id，结果是31 */
    private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);

    /** 序列在id中占的位数 */
    private final long sequenceBits = 12L;

    /** 机器ID向左移12位 */
    private final long workerIdShift = sequenceBits;

    /** 数据标识id向左移17位(12+5) */
    private final long datacenterIdShift = sequenceBits + workerIdBits;

    /** 时间截向左移22位(5+5+12) */
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

    /** 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095) */
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    /** 工作机器ID(0~31) */
    private long workerId;

    /** 数据中心ID(0~31) */
    private long datacenterId;

    /** 毫秒内序列(0~4095) */
    private long sequence = 0L;

    /** 上次生成ID的时间截 */
    private long lastTimestamp = -1L;

    //==============================Constructors=====================================
    /**
     * 构造函数
     * @param workerId 工作ID (0~31)
     * @param datacenterId 数据中心ID (0~31)
     */
    public SnowflakeIdWorker(long workerId, long datacenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    // ==============================Methods==========================================
    /**
     * 获得下一个ID (该方法是线程安全的)
     * @return SnowflakeId
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        //如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                    String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        //如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            //毫秒内序列溢出
            if (sequence == 0) {
                //阻塞到下一个毫秒,获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        }
        //时间戳改变，毫秒内序列重置
        else {
            sequence = 0L;
        }

        //上次生成ID的时间截
        lastTimestamp = timestamp;

        //移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - twepoch) << timestampLeftShift) //
                | (datacenterId << datacenterIdShift) //
                | (workerId << workerIdShift) //
                | sequence;
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     * @param lastTimestamp 上次生成ID的时间截
     * @return 当前时间戳
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 返回以毫秒为单位的当前时间
     * @return 当前时间(毫秒)
     */
    protected long timeGen() {
        return System.currentTimeMillis();
    }

    //==============================Test=============================================
    /** 测试 */
    public static void main(String[] args) {
        //System.out.println(Long.toBinaryString(5));
        SnowflakeIdWorker idWorker = new SnowflakeIdWorker(1, 1);
        for (int i = 0; i < 1000; i++) {
            long id = idWorker.nextId();
            System.out.println(Long.toBinaryString(id));
            System.out.println(id);
        }
    }
}
```
----
&emsp; 可以引入hutool-captcha依赖快速使用snowflake算法。  

```xml
<dependency>
    <groupId>cn.hutool</groupId>
    <artifactId>hutool-captcha</artifactId>
    <version>${hutool.version}</version>
</dependency>
```

---
## 2.5. 开源分布式ID算法  
&emsp; 百度uid-generator、美团Leaf、滴滴Tinyid......
  
### 2.5.1. 百度uid-generator  
&emsp; uid-generator是由百度技术部开发，解决了时钟回拨问题。项目GitHub地址 https://github.com/baidu/uid-generator。  
1. uid-generator是基于Snowflake算法实现的，与原始的snowflake算法不同在于，uid-generator支持自定义时间戳、工作机器ID和序列号等各部分的位数，而且uid-generator中采用用户自定义workId的生成策略。  
2. 通过消费未来时间克服了雪花算法的并发限制。  
3. UidGenerator提前生成ID并缓存在RingBuffer中。  
4. 适合虚拟环境，比如：Docker。  

#### 2.5.1.1. uid-generator使用教程  

1. 引入依赖：  

```
<dependency>
    <groupId>cn.codesheep</groupId>
    <artifactId>uid-generator</artifactId>
    <version>1.0</version>
</dependency>
```

2. 创建表WORKER_NODE；  
3. 修改Spring配置；  
4. 运行示例单测：  

```
@Resource
private UidGenerator uidGenerator;

@Test
public void testSerialGenerate() {
    // Generate UID
    long uid = uidGenerator.getUID();

    // Parse UID into [Timestamp, WorkerId, Sequence]
    // {"UID":"180363646902239241","parsed":{    "timestamp":"2017-01-19 12:15:46",    "workerId":"4",    "sequence":"9"        }}
    System.out.println(uidGenerator.parseUID(uid));

}
```

### 2.5.2. 美团Leaf  
......

### 2.5.3. 滴滴Tinyid  
......



