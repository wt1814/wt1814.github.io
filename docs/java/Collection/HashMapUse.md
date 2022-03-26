
<!-- TOC -->

- [1. HashMap使用](#1-hashmap使用)
    - [1.1. Map介绍](#11-map介绍)
    - [1.2. Map集合遍历](#12-map集合遍历)
    - [1.3. 如何实现一个自定义的class作为HashMap的key？](#13-如何实现一个自定义的class作为hashmap的key)

<!-- /TOC -->

<!---
HashMap追魂二十三问
https://www.jianshu.com/p/f238874a8bb8?utm_campaign=haruki&utm_content=note&utm_medium=reader_share&utm_source=weixin
https://mp.weixin.qq.com/s/3yT4YkRtxXeu9Hv0EOtkpQ
https://mp.weixin.qq.com/s/qfm-Xq1ZNJFJdSZ58qSLLA
https://mp.weixin.qq.com/s/zKrpKLo1S2e0LuPJRDSiiQ
https://mp.weixin.qq.com/s/wIjAj4rAAZccAl-yhmj_TA
https://mp.weixin.qq.com/s/VpcgoV9JJWZz6tLagsIYnQ
https://mp.weixin.qq.com/s/yijuwQuuOBE8x1VKk6lhCQ
HashMap
https://mp.weixin.qq.com/s/50opoIzy--aGCDZPNvg7RA
https://mp.weixin.qq.com/s/sCbhQolu_BXBXsQwlFC81g

记录插入顺序的LinkHashMap、给key排序的TreeMap。
-->



# 1. HashMap使用

## 1.1. Map介绍
![image](http://www.wt1814.com/static/view/images/java/JDK/Collection/collection-3.png)  

* LikedHashMap  
&emsp; LinkedHashMap是HashMap的一个子类，保存了记录的插入顺序，在用Iterator遍历LinkedHashMap时，先得到的记录肯定是先插入的，也可以在构造时带参数，按照访问次序排序。  
&emsp; LinkedHashMap怎么实现有序的？  
&emsp; LinkedHashMap内部维护了一个单链表，有头尾节点，同时LinkedHashMap节点Entry内部除了继承HashMap的Node属性，还有before和after用于标识前置节点和后置节点。可以实现按插入的顺序或访问顺序排序。  

* TreeMap(可排序)  
&emsp; TreeMap实现SortedMap接口，能够把它保存的记录根据键排序，默认是按键值的升序排序，也可以指定排序的比较器，当用Iterator遍历TreeMap时，得到的记录是排过序的。  
&emsp; 如果使用排序的映射，建议使用TreeMap。  
&emsp; 在使用TreeMap时，key必须实现Comparable接口或者在构造TreeMap传入自定义的Comparator，否则会在运行时抛出java.lang.ClassCastException类型的异常。  


## 1.2. Map集合遍历  

1. map.keySet()  
2. map.values()  
3. map.entrySet()  
4. map.entrySet().iterator()  
5. JDK1.8中map.forEach((key, value) -> { })    

```java
public void testHashMap() {
    Map<String, String> map = new HashMap<>(4);
    map.put("1", "a");
    map.put("2", "b");
    map.put("3", "c");
    map.put("4", "d");

    System.out.println("----- map.keySet() -----");
    //获取所有的 key，根据 key 取出对应的value
    for (String key : map.keySet()) {
        System.out.println("key:" + key + ",value:" + map.get(key));
    }
    System.out.println("----- 获取map种所有的value：map.values() -----");
    //遍历所有的value
    for (String value : map.values()) {
        System.out.println("value:" + value);
    }
    System.out.println("----- 获取键值对：map.entrySet() -----");
    //取出对应的 key，value 键值对,容量大时推荐使用
    for (Map.Entry<String, String> entry : map.entrySet()) {
        System.out.println("键值对:" + entry);
        //获取 键值对的 key
        System.out.println("key:" + entry.getKey());
        //获取 键值对的 value
        System.out.println("value:" + entry.getValue());
    }

    System.out.println("----- 通过 Map.entrySet使用iterator遍历 key 和 value -----");
    Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
    while (iterator.hasNext()) {
        Map.Entry<String, String> entry = iterator.next();
        System.out.println("key:" + entry.getKey() + ",value:" + entry.getValue());
    }

    System.out.println("----- map.forEach JDK1.8 新特性 -----");
    map.forEach((key, value) -> {
        System.out.println("key=" + key + ",value=" + value);
    });
}
```
&emsp; 推荐：<font color = "clime">使用entrySet遍历Map类集合KV，而不是keySet方式进行遍历。</font>  
&emsp; 说明：keySet其实是遍历了2次，一次是转为Iterator对象，另一次是从hashMap中取出key所对应的value。而entrySet只是遍历了一次就把key和value都放到了entry中，效率更高。  
&emsp; <font color = "red">如果是 JDK8，使用 Map.foreach 方法。</font>  

## 1.3. 如何实现一个自定义的class作为HashMap的key？  
&emsp; ......


