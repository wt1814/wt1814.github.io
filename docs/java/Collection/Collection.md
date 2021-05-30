

<!-- TOC -->

- [1. Collection](#1-collection)
    - [1.1. List](#11-list)
        - [1.1.1. ArrayList](#111-arraylist)
            - [1.1.1.1. 如何去掉list集合中重复的元素?](#1111-如何去掉list集合中重复的元素)
            - [1.1.1.2. ArrayList 类定义](#1112-arraylist-类定义)
            - [1.1.1.3. 字段属性](#1113-字段属性)
            - [1.1.1.4. 构造函数](#1114-构造函数)
            - [1.1.1.5. 成员方法（增、删、改、查）](#1115-成员方法增删改查)
                - [1.1.1.5.1. 增加，add(E e)](#11151-增加adde-e)
                - [1.1.1.5.2. 删除，remove(int index)、remove(Object o)](#11152-删除removeint-indexremoveobject-o)
                    - [1.1.1.5.2.1. 根据索引删除元素](#111521-根据索引删除元素)
                    - [1.1.1.5.2.2. 直接删除指定元素](#111522-直接删除指定元素)
    - [1.2. Set](#12-set)
        - [1.2.1. HashSet](#121-hashset)
            - [1.2.1.1. HashSet 定义](#1211-hashset-定义)
            - [1.2.1.2. 字段属性](#1212-字段属性)
            - [1.2.1.3. 构造函数](#1213-构造函数)
            - [1.2.1.4. 成员方法](#1214-成员方法)
                - [1.2.1.4.1. 添加元素(如何保证值不重复？)](#12141-添加元素如何保证值不重复)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. HashSet基于HashMap实现： **<font color = "clime">存储在HashSet中的数据作为Map的key，而Map的value统一为PRESENT。</font>**  
    &emsp; 添加元素(如何保证值不重复？)  
    &emsp; HashSet#add通过 map.put() 方法来添加元素。HashSet的add(E e)方法，会将e作为key，PRESENT 作为value插入到map集合中。  
    * 如果e(新插入的key)存在，HashMap#put返回原key对应的value值（注意新插入的value会覆盖原value值），Hashset#add返回false，表示插入值重复，插入失败。  
    * 如果e(新插入的key)不存在，HashMap#put返回null值，Hashset#add返回true，表示插入值不重复，插入成功。  


# 1. Collection
## 1.1. List  
### 1.1.1. ArrayList  
&emsp; ArrayList可以简单的认为是一个动态数组，<font color = "red">长度不够时，调用Arrays.copyOf方法，拷贝当前数组到一个新的长度更大的数组。</font>  
&emsp; ArrayList特点：随机访问速度快，插入和移除性能较差(数组的特点)；支持null元素；有顺序；元素可以重复；线程不安全。  

#### 1.1.1.1. 如何去掉list集合中重复的元素?   
<!-- 
https://mp.weixin.qq.com/s/SlxS3pQ-pcNH0LuSnmqm2g
-->
&emsp; 在实际开发的时候，经常会碰到这么一个困难：一个集合容器里面有很多重复的对象，里面的对象没有主键，但是根据业务的需求，实际上需要根据条件筛选出没有重复的对象。  
1. 比较暴力的方法，就是根据业务需求，通过两层循环来进行判断，没有重复的元素就加入到新集合中，新集合中已经有的元素就跳过。  
2. 利用list中contains方法去重  
&emsp; 在使用contains()之前，必须要对PenBean类重写equals()方法。  
3. java 8中去重操作  
&emsp; **<font color = "red">利用jdk1.8中提供的Stream.distinct()列表去重，Stream.distinct()使用hashCode()和equals()方法来获取不同的元素，因此使用这种写法，对象需要重写hashCode()和equals()方法！</font>**  
4. HashSet去重操作。  

#### 1.1.1.2. ArrayList 类定义  

```java
public class ArrayList<E> extends AbstractList<E> implements List<E>, RandomAccess, Cloneable, java.io.Serializable
```
&emsp; 注：AbstractList.java  

```java
/*
   modCount 成员变量，来记录修改次数，主要是在使用迭代器遍历的时候，用来检查列表中的元素是否发生结构性变化（列表元素数量发生改变）了，主要在多线程环境下需要使用，防止一个线程正在迭代遍历，另一个线程修改了这个列表的结构。否则抛出ConcurrentModificationException。
*/
protected transient int modCount = 0;
```

#### 1.1.1.3. 字段属性  

```java
//初始化容量，默认为 10
private static final int DEFAULT_CAPACITY = 10;
//空的数组实例
private static final Object[] EMPTY_ELEMENTDATA = {};
//这也是一个空的数组实例，和EMPTY_ELEMENTDATA空数组相比是用于了解添加元素时数组膨胀多少
private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
//存储 ArrayList集合的元素，集合的长度即这个数组的长度
//1、当 elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA 时将会清空 ArrayList
//2、当添加第一个元素时，elementData 长度会扩展为 DEFAULT_CAPACITY=10
transient Object[] elementData;
//表示集合的长度
private int size;
```

#### 1.1.1.4. 构造函数  

```java
/**
 * 创建一个 DEFAULTCAPACITY_EMPTY_ELEMENTDATA 声明的数组，注意此时初始容量是0，而不是10。
 */
public ArrayList() { }
/**
 * 使用默认容量构造一个数组
 * @throws IllegalArgumentException 如果初始化容量initialCapacity是一个负数，则会抛出这个IllegalArgumentException异常
 */
public ArrayList(int initialCapacity) { }
/**
 * 构造包含指定的元素的列表集合，按照顺序返回这个集合的迭代器。将已有的集合复制到 ArrayList 集合中去。
 * @throws NullPointerException 如果传入的Collection为null，则会抛出NullPointerException空指针异常
 */
public ArrayList(Collection<? extends E> c) { }
```

#### 1.1.1.5. 成员方法（增、删、改、查）  
##### 1.1.1.5.1. 增加，add(E e)  

```java
/**
 * 将指定元素追加到列表末尾。所以ArrayList是有序的，也就是元素的存储顺序和添加顺序是一致的。
 */
public boolean add(E e) {
    ensureCapacityInternal(size + 1);  //确保内部的容量
    elementData[size++] = e;//在list末尾追加元素
    return true;
}

/**
 * 在list的指定索引处插入指定元素
 */
public void add(int index, E element) {
    //检查索引是否越界
    rangeCheckForAdd(index);
    //确保内部的容量
    ensureCapacityInternal(size + 1);  // Increments modCount!!
    System.arraycopy(elementData, index, elementData, index + 1, size - index);
    elementData[index] = element;
    size++;
}
```
&emsp; 对于ArrayList集合添加元素：
1. **<font color = "red">当通过ArrayList()构造一个空集合，初始长度是为0的，第1次添加元素，会创建一个长度为10的数组，并将该元素赋值到数组的第一个位置。</font>**  
2. 第2次添加元素，集合不为空，而且由于集合的长度size+1是小于数组的长度10，所以直接添加元素到数组的第二个位置，不用扩容。  
3. 第11次添加元素，此时size+1 = 11，而数组长度是10，这时候创建一个长度为10+10*0.5 = 15 的数组（扩容1.5倍），然后将原数组元素引用拷贝到新数组。并将第11次添加的元素赋值到新数组下标为10的位置。  
4. 第Integer.MAX_VALUE - 8 = 2147483639，然后 2147483639%1.5=1431655759（这个数是要进行扩容）次添加元素，为了防止溢出，此时会直接创建一个1431655759+1大小的数组，这样一直，每次添加一个元素，都只扩大一个范围。  
5. 第Integer.MAX_VALUE - 7次添加元素时，创建一个大小为 Integer.MAX_VALUE的数组，在进行元素添加。  
6. 第Integer.MAX_VALUE + 1次添加元素时，抛出OutOfMemoryError异常。  
&emsp; 注意：能向集合中添加null的，因为数组可以有null值存在。  

##### 1.1.1.5.2. 删除，remove(int index)、remove(Object o)  
###### 1.1.1.5.2.1. 根据索引删除元素  

```java
public E remove(int index) {
    rangeCheck(index);//判断给定索引的范围，超过集合大小则抛出异常

    modCount++;
    E oldValue = elementData(index);//得到索引处的删除元素

    int numMoved = size - index - 1;
    if (numMoved > 0)//size-index-1 > 0 表示 0<= index < (size-1),即索引不是最后一个元素
        //通过 System.arraycopy()将数组elementData 的下标index+1之后长度为 numMoved的元素拷贝到从index开始的位置
        System.arraycopy(elementData, index+1, elementData, index,
                numMoved);
    elementData[--size] = null; //将数组最后一个元素置为 null，便于垃圾回收

    return oldValue;
}
```
&emsp; remove(int index)方法表示删除索引index处的元素，首先通过 rangeCheck(index)方法判断给定索引的范围，超过集合大小则抛出异常；接着通过System.arraycopy方法对数组进行自身拷贝。

###### 1.1.1.5.2.2. 直接删除指定元素  

```java
public boolean remove(Object o) {
    if (o == null) {//如果删除的元素为null
        for (int index = 0; index < size; index++)
            if (elementData[index] == null) {
                fastRemove(index);
                return true;
            }
    } else {//不为null，通过equals方法判断对象是否相等
        for (int index = 0; index < size; index++)
            if (o.equals(elementData[index])) {
                fastRemove(index);
                return true;
            }
    }
    return false;
}
```
&emsp; remove(Object o)方法是删除第一次出现的该元素。然后通过System.arraycopy进行数组自身拷贝。  

-------
## 1.2. Set  
&emsp; Set接口的实现类，最大特点是不允许出现重复元素。HashSet、LinkedHashSet、TreeSet底层由对应Map的方法来实现。  

* HashSet：基于HashMap实现，一个性能相对较好的Set；
* LinkedHashSet：基于LinkedHashMap实现，一个保存了插入顺序的Set；  
* TreeSet：基于TreeMap实现，一个实现了排序的Set；  

### 1.2.1. HashSet  
&emsp; HashSet元素无序且不能重复。<font color = "red">HashSet的实现依赖于HashMap，HashSet的值存储在HashMap中。</font>在HashSet的构造法中会初始化一个HashMap对象，HashSet不允许值重复，因此， **<font color = "clime">HashSet的值是作为HashMap的key存储在HashMap中的，</font>** 当存储的值已经存在时返回false。<font color = "red">而HashMap的value则存储了一个PRESENT，它是一个静态的Object对象。</font>  

#### 1.2.1.1. HashSet 定义  
```java
public class HashSet<E> extends AbstractSet<E> implements Set<E>, Cloneable, java.io.Serializable
```
&emsp; HashSet实现了Cloneable接口和Serializable接口，分别用来支持克隆以及支持序列化。还实现了Set 接口，该接口定义了Set集合类型的一套规范。

#### 1.2.1.2. 字段属性  
```java
//HashSet集合中的内容是通过 HashMap 数据结构来存储的
private transient HashMap<E,Object> map;
//向HashSet中添加数据，数据在上面的 map 结构是作为 key 存在的，而value统一都是 PRESENT
private static final Object PRESENT = new Object();
```
&emsp; 1)定义一个HashMap，作为实现HashSet的数据结构；2)定义一个PRESENT对象。 **<font color = "clime">存储在HashSet中的数据作为Map的key，而Map的value统一为PRESENT。</font>**  

#### 1.2.1.3. 构造函数  

```java
//无参构造
public HashSet() {
    map = new HashMap<>();
}
//构造包含指定集合中的元素
public HashSet(Collection<? extends E> c) {
    map = new HashMap<>(Math.max((int) (c.size()/.75f) + 1, 16));
    addAll(c);
}
//指定初始容量和加载因子
public HashSet(int initialCapacity, float loadFactor) {
    map = new HashMap<>(initialCapacity, loadFactor);
}
//指定初始容量
public HashSet(int initialCapacity) {
    map = new HashMap<>(initialCapacity);
}
//
HashSet(int initialCapacity, float loadFactor, boolean dummy) {
    map = new LinkedHashMap<>(initialCapacity, loadFactor);
}
```

#### 1.2.1.4. 成员方法  
##### 1.2.1.4.1. 添加元素(如何保证值不重复？)  

```java
public boolean add(E e) {
    return map.put(e, PRESENT)==null;
}
```
&emsp; HashSet的add()方法调用了HashMap中的put()方法。  

```java
public V put(K key, V value) {
    // 倒数第二个参数false：表示允许旧值替换
    // 最后一个参数true：表示HashMap不处于创建模式
    return putVal(hash(key), key, value, false, true);
}
```
&emsp; HashMap中的put()方法又调用了putVal()方法来实现功能。  

```java
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
               boolean evict) {
    Node<K, V>[] tab;
    Node<K, V> p;
    int n, i;
    //如果哈希表为空，调用resize()创建一个哈希表，并用变量n记录哈希表长度
    if ((tab = table) == null || (n = tab.length) == 0)
        n = (tab = resize()).length;
    /**
     * 如果指定参数hash在表中没有对应的桶，即为没有碰撞
     * Hash函数，(n - 1) & hash 计算key将被放置的槽位
     * (n - 1) & hash 本质上是hash % n，位运算更快
     */
    if ((p = tab[i = (n - 1) & hash]) == null)
        //直接将键值对插入到map中即可
        tab[i] = newNode(hash, key, value, null);
    else {// 桶中已经存在元素
        Node<K, V> e;
        K k;
        // 比较桶中第一个元素(数组中的结点)的hash值相等，key相等
        if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
            // 将第一个元素赋值给e，用e来记录
            e = p;
            // 当前桶中无该键值对，且桶是红黑树结构，按照红黑树结构插入
        else if (p instanceof TreeNode)
            e = ((TreeNode<K, V>) p).putTreeVal(this, tab, hash, key, value);
            // 当前桶中无该键值对，且桶是链表结构，按照链表结构插入到尾部
        else {
            for (int binCount = 0; ; ++binCount) {
                // 遍历到链表尾部
                if ((e = p.next) == null) {
                    p.next = newNode(hash, key, value, null);
                    // 检查链表长度是否达到阈值，达到将该槽位节点组织形式转为红黑树
                    if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                        treeifyBin(tab, hash);
                    break;
                }
                // 链表节点的<key, value>与put操作<key, value>相同时，不做重复操作，跳出循环
                if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                    break;
                p = e;
            }
        }
        // 找到或新建一个key和hashCode与插入元素相等的键值对，进行put操作
        if (e != null) { // existing mapping for key
            // 记录e的value
            V oldValue = e.value;
            /**
             * onlyIfAbsent为false或旧值为null时，允许替换旧值
             * 否则无需替换
             */
            if (!onlyIfAbsent || oldValue == null)
                e.value = value;
            // 访问后回调
            afterNodeAccess(e);
            // 返回旧值
            return oldValue;
        }
    }
    // 更新结构化修改信息
    ++modCount;
    // 键值对数目超过阈值时，进行rehash
    if (++size > threshold)
        resize();
    // 插入后回调
    afterNodeInsertion(evict);
    return null;
}
```
1. Hashset#add通过 map.put() 方法来添加元素。 **<font color = "clime">HashMap#put方法中如果新插入的key不存在，则返回null；如果新插入的key存在，则返回原key对应的value值（注意新插入的value会覆盖原value值）。</font>**  
2. HashSet的add(E e)方法，会将e作为key，PRESENT 作为value插入到map集合中。  
    1. 如果e存在，HashMap#put返回原key对应的value值，Hashset#add返回false，表示插入值重复，插入失败。  
    2. 如果e不存在，HashMap#put返回null值，Hashset#add返回true，表示插入值不重复，插入成功。  
