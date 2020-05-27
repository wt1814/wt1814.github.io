---
title: 一致性哈希算法
date: 2020-05-09 00:00:00
tags:
    - 分布式
---
- [1. 一致性哈希](#1-%e4%b8%80%e8%87%b4%e6%80%a7%e5%93%88%e5%b8%8c)
  - [1.1. 一致性哈希函数：](#11-%e4%b8%80%e8%87%b4%e6%80%a7%e5%93%88%e5%b8%8c%e5%87%bd%e6%95%b0)
  - [1.2. 优点：](#12-%e4%bc%98%e7%82%b9)
  - [1.3. 缺点：](#13-%e7%bc%ba%e7%82%b9)
  - [1.4. 算法实现：](#14-%e7%ae%97%e6%b3%95%e5%ae%9e%e7%8e%b0)

# 1. 一致性哈希  
&emsp; 一致性哈希简称DHT，可以有效解决分布式存储结构下动态增加和删除节点所带来的问题。  
&emsp; 一致性哈希算法依赖于普通的哈希算法。原理中node相当于普通哈希算法中数组的值；key会缓存到环形存储结构里，属于顺时针顺序到下一个节点。  

## 1.1. 一致性哈希函数：  
1. 首先，把全量的缓存空间当做一个环形存储结构。环形空间总共分成2^32个缓存区，在Redis中则是把缓存key分配到16384个slot。  
2. 每一个缓存key都可以通过Hash算法转化为一个32位的二进制数，也就对应着环形空间的某一个缓存区。把所有的缓存key映射到环形空间的不同位置。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/functions/function-1.png)  
3. 每一个缓存节点（Shard）也遵循同样的Hash算法，比如利用IP做Hash，映射到环形空间当中。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/functions/function-2.png)  
4. 让key和节点相对应：每一个key的顺时针方向最近节点，就是key所归属的存储节点。所以图中key1存储于node1，key2，key3存储于node2，key4存储于node3。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/functions/function-3.png)  

## 1.2. 优点：  
&emsp; 增加和删除节点，只会影响部分数据。  
1. 增加节点  
&emsp; 当缓存集群的节点有所增加的时候，整个环形空间的映射仍然会保持一致性哈希的顺时针规则，所以有一小部分key的归属会受到影响。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/functions/function-4.png)  
&emsp; 有哪些key会受到影响呢？图中加入了新节点node4，处于node1和node2之间，按照顺时针规则，从node1到node4之间的缓存不再归属于node2，而是归属于新节点node4。因此受影响的key只有key2。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/functions/function-5.png)  
&emsp; 最终把key2的缓存数据从node2迁移到node4，就形成了新的符合一致性哈希规则的缓存结构。  

2. 删除节点  
&emsp; 当缓存集群的节点需要删除的时候（比如节点挂掉），整个环形空间的映射同样会保持一致性哈希的顺时针规则，同样有一小部分key的归属会受到影响。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/functions/function-7.png)  
&emsp; 有哪些key会受到影响呢？图中删除了原节点node3，按照顺时针规则，原本node3所拥有的缓存数据就需要“托付”给node3的顺时针后继节点node1。因此受影响的key只有key4。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/functions/function-8.png)  
&emsp; 最终把key4的缓存数据从node3迁移到node1，就形成了新的符合一致性哈希规则的缓存结构。  

&emsp; 问题：既然节点node3已经挂掉了，它怎么迁移数据到节点node1？  
&emsp; 迁移并不是直接到数据迁移，而是在查询时去找顺时针到后继节点，因缓存未命中而刷新缓存。  

## 1.3. 缺点：  
&emsp; 如像下图这样，按顺时针规则，所有的key都归属于同一个节点，会造成数据倾斜。如果节点太少或分布不均匀的时候，都会造成数据倾斜。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/functions/function-9.png)  
&emsp; 为了优化这种节点太少而产生的不均衡情况，一致性哈希算法引入了“虚拟节点”的概念。  
&emsp; 所谓虚拟节点，就是基于原来的物理节点映射出N个子节点，最好把所有的子节点映射到环形空间上。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/functions/function-10.png)  
&emsp; 如上图所示，假如node1的ip是192.168.1.109，那么原node1节点在环形空间的位置就是hash（“192.168.1.109”）。  
&emsp; 基于node1构建两个虚拟节点，node1-1和node1-2，虚拟节点在环形空间的位置可以利用（IP+后缀）计算，例如：hash(“192.168.1.109#1”)，hash(“192.168.1.109#2”)。  
&emsp; 此时，环形空间中不再有物理节点node1，node2，只有虚拟节点node1-1，node1-2，node2-1，node2-2。由于虚拟节点数量较多，缓存key与虚拟节点的映射关系也变得相对均衡了。  

## 1.4. 算法实现：  
&emsp; 算法接口类：  

```java
public interface IHashService {
    Long hash(String key);
}
```
&emsp; 算法接口实现类：  

```java
public class HashService implements IHashService {

    /**
     * MurMurHash算法,性能高,碰撞率低
     *
     * @param key String
     * @return Long
     */
    public Long hash(String key) {
        ByteBuffer buf = ByteBuffer.wrap(key.getBytes());
        int seed = 0x1234ABCD;

        ByteOrder byteOrder = buf.order();
        buf.order(ByteOrder.LITTLE_ENDIAN);

        long m = 0xc6a4a7935bd1e995L;
        int r = 47;

        long h = seed ^ (buf.remaining() * m);

        long k;
        while (buf.remaining() >= 8) {
            k = buf.getLong();

            k *= m;
            k ^= k >>> r;
            k *= m;

            h ^= k;
            h *= m;
        }

        if (buf.remaining() > 0) {
            ByteBuffer finish = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            finish.put(buf).rewind();
            h ^= finish.getLong();
            h *= m;
        }

        h ^= h >>> r;
        h *= m;
        h ^= h >>> r;

        buf.order(byteOrder);
        return h;

    }
}
```
&emsp; 模拟机器节点：  

```java
public class Node<T> {
    private String ip;
    private String name;

    //get、set方法

    /**
     * 使用IP当做hash的Key
     * @return String
     */
    @Override
    public String toString() {
        return ip;
    }
}
```
&emsp; 一致性Hash操作：  

```java
public class ConsistentHash<T> {
    // Hash函数接口
    private final IHashService iHashService;
    // 每个机器节点关联的虚拟节点数量
    private final int  numberOfReplicas;
    // 环形虚拟节点
    private final SortedMap<Long, T> circle = new TreeMap<Long, T>();

    public ConsistentHash(IHashService iHashService, int numberOfReplicas, Collection<T> nodes) {
        this.iHashService = iHashService;
        this.numberOfReplicas = numberOfReplicas;
        for (T node : nodes) {
            add(node);
        }
    }

    /**
     * 增加真实机器节点
     * @param node T
     */
    public void add(T node) {
        for (int i = 0; i < this.numberOfReplicas; i++) {
            circle.put(this.iHashService.hash(node.toString() + i), node);
        }
    }

    /**
     * 删除真实机器节点
     * @param node T
     */
    public void remove(T node) {
        for (int i = 0; i < this.numberOfReplicas; i++) {
            circle.remove(this.iHashService.hash(node.toString() + i));
        }
    }

    public T get(String key) {
        if (circle.isEmpty()) return null;

        long hash = iHashService.hash(key);

        // 沿环的顺时针找到一个虚拟节点
        if (!circle.containsKey(hash)) {
            SortedMap<Long, T> tailMap = circle.tailMap(hash);
            hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
        }
        return circle.get(hash);
    }
}
```
&emsp; 测试类：  

```java
public class TestHashCircle {
    // 机器节点IP前缀
    private static final String IP_PREFIX = "192.168.0.";

    public static void main(String[] args) {
        // 每台真实机器节点上保存的记录条数
        Map<String, Integer> map = new HashMap<String, Integer>();

        // 真实机器节点, 模拟10台
        List<Node<String>> nodes = new ArrayList<Node<String>>();
        for (int i = 1; i <= 10; i++) {
            map.put(IP_PREFIX + i, 0); // 初始化记录
            Node<String> node = new Node<String>(IP_PREFIX + i, "node" + i);
            nodes.add(node);
        }

        IHashService iHashService = new HashService();
        // 每台真实机器引入100个虚拟节点
        ConsistentHash<Node<String>> consistentHash = new ConsistentHash<Node<String>>(iHashService, 500, nodes);

        // 将5000条记录尽可能均匀的存储到10台机器节点上
        for (int i = 0; i < 5000; i++) {
            // 产生随机一个字符串当做一条记录，可以是其它更复杂的业务对象,比如随机字符串相当于对象的业务唯一标识
            String data = UUID.randomUUID().toString() + i;
            // 通过记录找到真实机器节点
            Node<String> node = consistentHash.get(data);
            // 再这里可以能过其它工具将记录存储真实机器节点上，比如MemoryCache等
            // ...
            // 每台真实机器节点上保存的记录条数加1
            map.put(node.getIp(), map.get(node.getIp()) + 1);
        }

        // 打印每台真实机器节点保存的记录条数
        for (int i = 1; i <= 10; i++) {
            System.out.println(IP_PREFIX + i + "节点记录条数：" + map.get(IP_PREFIX + i));
        }
    }
}
```
&emsp; 运行结果如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/functions/function-11.png)  
