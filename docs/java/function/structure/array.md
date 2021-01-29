
<!-- TOC -->

- [1. 数组和链表](#1-数组和链表)
    - [1.1. 数组](#11-数组)
    - [1.2. 链表](#12-链表)
        - [1.2.1. 单向链表](#121-单向链表)
        - [1.2.2. 链表解题思路](#122-链表解题思路)
            - [1.2.2.1. 链表翻转](#1221-链表翻转)
                - [1.2.2.1.1. 递归翻转链表](#12211-递归翻转链表)
                - [1.2.2.1.2. 非递归翻转链表（迭代解法）](#12212-非递归翻转链表迭代解法)
                - [1.2.2.1.3. 变形题](#12213-变形题)
            - [1.2.2.2. 快慢指针](#1222-快慢指针)
                - [1.2.2.2.1. 寻找/删除第K个结点](#12221-寻找删除第k个结点)
                - [1.2.2.2.2. 有关链表环问题的相关解法](#12222-有关链表环问题的相关解法)

<!-- /TOC -->

# 1. 数组和链表
<!-- 
链表反转的姿势
https://mp.weixin.qq.com/s/YVQvbhO0HJtnrocVg8-qmQ
-->

## 1.1. 数组  
&emsp; 这里讲解下二维数组。二维数组是一个元素为一维数组的数组。  

&emsp; 二维数组定义：数据类型[][] 变量名=new 数据类型[m][n]。m表示这个二维数组有多少个数组；n表示每一个一维数组的元素个数。  
    
    举例： 
    int[][] arr=new int[3][2];定义了一个二维数组arr。
    这个二维数组有3个一维数组，名称是ar[0],arr[1],arr[2]；每个一维数组有2个元素，可以通过arr[m][n]来获取。

&emsp; 二维数组基本操作：遍历一个行列变化的二维数组。  

```java
/*
* 遍历一个行列变化的数组
*/
public class bianlishuzu2 {
    public static void main(String[] args) {
        int [][] arr = { {1,2,3},{4,5},{6} };
        printArray(arr);
    }
    public static void printArray(int[][] arr){
        for(int x=0;x<arr.length;x++){
            for(int y=0;y<arr[x].length;y++){
                System.out.print(arr[x][y]);
            }
            System.out.println();
        }
    }
}
```

## 1.2. 链表  
<!-- 
 23张图！万字详解「链表」，从小白到大佬！ 
 https://mp.weixin.qq.com/s/BRcsDO9aAKtYWduCtST9gA
-->
&emsp; 链表分类：单向链表、双端链表、有序链表、双向链表。  

### 1.2.1. 单向链表  
&emsp; 链节点可以是一个单独的类，也可以是内部类。  

```java
public class SingleLinkedList {
    private int size;//链表节点的个数
    private Node head;//头节点

    public SingleLinkedList(){
        size = 0;
        head = null;
    }

    //链表的每个节点类
    private class Node{
        private Object data;//每个节点的数据
        private Node next;//每个节点指向下一个节点的连接

        public Node(Object data){
            this.data = data;
        }
    }

    //在链表头添加元素
    public Object addHead(Object obj){
        Node newHead = new Node(obj);
        if(size == 0){
            head = newHead;
        }else{
            newHead.next = head;
            head = newHead;
        }
        size++;
        return obj;
    }

    //在链表头删除元素
    public Object deleteHead(){
        Object obj = head.data;
        head = head.next;
        size--;
        return obj;
    }

    //查找指定元素，找到了返回节点Node，找不到返回null
    public Node find(Object obj){
        Node current = head;
        int tempSize = size;
        while(tempSize > 0){
            if(obj.equals(current.data)){
                return current;
            }else{
                current = current.next;
            }
            tempSize--;
        }
        return null;
    }

    //删除指定的元素，删除成功返回true
    public boolean delete(Object value){
        if(size == 0){
            return false;
        }
        Node current = head;
        Node previous = head;
        while(current.data != value){
            if(current.next == null){
                return false;
            }else{
                previous = current;
                current = current.next;
            }
        }
        //如果删除的节点是第一个节点
        if(current == head){
            head = current.next;
            size--;
        }else{//删除的节点不是第一个节点
            previous.next = current.next;
            size--;
        }
        return true;
    }

    //判断链表是否为空
    public boolean isEmpty(){
        return (size == 0);
    }

    //显示节点信息
    public void display(){
        if(size >0){
            Node node = head;
            int tempSize = size;
            if(tempSize == 1){//当前链表只有一个节点
                System.out.println("["+node.data+"]");
                return;
            }
            while(tempSize>0){
                if(node.equals(head)){
                    System.out.print("["+node.data+"->");
                }else if(node.next == null){
                    System.out.print(node.data+"]");
                }else{
                    System.out.print(node.data+"->");
                }
                node = node.next;
                tempSize--;
            }
            System.out.println();
        }else{//如果链表一个节点都没有，直接打印[]
            System.out.println("[]");
        }
    }

}
```

### 1.2.2. 链表解题思路  
&emsp; **<font color = "red">链表常见解题思路有翻转和快慢指针。</font>**  

#### 1.2.2.1. 链表翻转  
&emsp; 什么是链表的翻转：给定链表 head-->4--->3-->2-->1，将其翻转成 head-->1-->2-->3-->4。翻转链表可以用非递归和递归这两种方式来解题。  

##### 1.2.2.1.1. 递归翻转链表  
<!-- https://mp.weixin.qq.com/s/lGBg6AQk6G1iCR6LNgy4dg -->
&emsp; 首先要查看翻转链表是否符合递归规律：问题可以分解成具有相同解决思路的子问题，子子问题...，直到最终的子问题再也无法分解。  
&emsp; 要翻转 head--->4--->3-->2-->1 链表，不考虑 head 结点，分析 4--->3-->2-->1，仔细观察，发现只要先把 3-->2-->1 翻转成 3<----2<----1，之后再把 3 指向 4 即可（如下图示）  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/function/function-14.png)  
<center>图：翻转链表主要三步骤</center>  

&emsp; 只要按以上步骤定义好这个翻转函数的功能即可， 这样由于子问题与最初的问题具有相同的解决思路，拆分后的子问题持续调用这个翻转函数即可达到目的。  
&emsp; 注意看上面的步骤1，问题的规模是不是缩小了（如下图），从翻转整个链表变成了只翻转部分链表！问题与子问题都是从某个结点开始翻转，具有相同的解决思路，另外当缩小到只翻转一个结点时，显然是终止条件，符合递归的条件！之后的翻转 3-->2-->1, 2-->1 持续调用这个定义好的递归函数即可!  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/function/function-15.png)  
&emsp; 既然符合递归的条件，那就可以套用递归四步曲来解题了（注意翻转之后 head 的后继节点变了，需要重新设置！别忘了这一步）  

1. 定义递归函数，明确函数的功能 根据以上分析，这个递归函数的功能显然是翻转某个节点开始的链表，然后返回新的头结点  
```java
/**
 * 翻转结点 node 开始的链表
 */
public Node invertLinkedList(Node node) {
}
```
2. 寻找递推公式上文中已经详细画出了翻转链表的步骤，简单总结一下递推步骤如下  

    * 针对结点 node (值为 4)，先翻转 node 之后的结点 invert(node->next)，翻转之后 4--->3--->2--->1 变成了 4--->3<---2<---1
    * 再把 node 节点的下个节点（3）指向 node，node 的后继节点设置为空（避免形成环），此时变成了 4<---3<---2<---1
    * 返回新的头结点，因为此时新的头节点从原来的 4 变成了 1，需要重新设置一下head
3. 将递推公式代入第一步定义好的函数中，如下 (invertLinkedList)

```java
/**
 * 递归翻转结点 node 开始的链表
 */
public Node invertLinkedList(Node node) {
    if (node.next == null) {
        return node;
    }

    // 步骤 1: 先翻转 node 之后的链表
    Node newHead = invertLinkedList(node.next);

    // 步骤 2: 再把原 node 节点后继结点的后继结点指向 node (4)，node 的后继节点设置为空(防止形成环)
    node.next.next = node;
    node.next = null;

    // 步骤 3: 返回翻转后的头结点
    return newHead;
}

public static void main(String[] args) {
    LinkedList linkedList = new LinkedList();
    int[] arr = {4,3,2,1};
    for (int i = 0; i < arr.length; i++) {
        linkedList.addNode(arr[i]);
    }
    Node newHead = linkedList.invertLinkedList(linkedList.head.next);
    // 翻转后别忘了设置头结点的后继结点！
    linkedList.head.next = newHead;
    linkedList.printList();      // 打印 1，2，3，4
}
```
        画外音：翻转后由于 head 的后继结点变了，别忘了重新设置！

4. 计算时间/空间复杂度 由于递归调用了 n 次 invertLinkedList 函数，所以时间复杂度显然是 O(n)，空间复杂度呢，没有用到额外的空间，但是由于递归调用了 n 次 invertLinkedList 函数，压了 n 次栈，所以空间复杂度也是 O(n)。  

&emsp; 递归一定要从函数的功能去理解，从函数的功能看，定义的递归函数清晰易懂，定义好了之后，由于问题与被拆分的子问题具有相同的解决思路，所以子问题只要持续调用定义好的功能函数即可，切勿层层展开子问题，此乃递归常见的陷阱！仔细看函数的功能，其实就是按照下图实现的。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/function/function-16.png)  

##### 1.2.2.1.2. 非递归翻转链表（迭代解法）  
&emsp; 递归比较容易造成栈溢出，所以如果有其他时间/空间复杂度相近或更好的算法，应该优先选择非递归的解法，那看看如何用迭代来翻转链表，主要思路如下  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/function/function-17.png)  
&emsp; 步骤 1： 定义两个节点：pre, cur ，其中 cur 是 pre 的后继结点，如果是首次定义， 需要把 pre 指向 cur 的指针去掉，否则由于之后链表翻转，cur 会指向 pre， 就进行了一个环(如下)，这一点需要注意  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/function/function-18.png)  
&emsp; 步骤2：知道了cur 和 pre,翻转就容易了，把 cur 指向 pre 即可，之后把 cur 设置为 pre ，cur 的后继结点设置为 cur 一直往前重复此步骤即可，完整动图如下  
![Alt Text](https://gitee.com/wt1814/pic-host/raw/master/images/java/function/640.gif)  
&emsp; 注意：同递归翻转一样，迭代翻转完了之后 head 的后继结点从 4 变成了 1，记得重新设置一下。  

&emsp; 知道了解题思路，实现代码就容易多了，直接上代码  

```java
/**
 * 迭代翻转
 */
public void iterationInvertLinkedList() {
    // 步骤 1
    Node pre = head.next;
    Node cur = pre.next;
    pre.next = null;

    while (cur != null) {
        /**
         * 务必注意：在 cur 指向 pre 之前一定要先保留 cur 的后继结点，不然 cur 指向 pre 后就再也找不到后继结点了
         * 也就无法对 cur 后继之后的结点进行翻转了
         */
        Node next = cur.next;
        cur.next = pre;
        pre = cur;
        cur = next;
    }
    // 此时 pre 为头结点的后继结点
    head.next = pre;
}
```
&emsp; 用迭代的思路来做由于循环了 n 次，显然时间复杂度为 O(n)，另外由于没有额外的空间使用，也未像递归那样调用递归函数不断压栈，所以空间复杂度是 O(1),对比递归，显然应该使用迭代的方式来处理！

##### 1.2.2.1.3. 变形题  
......
<!--
一文学会链表解题 
https://mp.weixin.qq.com/s?__biz=MzI5MTU1MzM3MQ==&mid=2247483899&idx=1&sn=ab5b06d99e9652826ed9ed33763e1371&scene=21#wechat_redirect
-->

#### 1.2.2.2. 快慢指针  
&emsp; **<font color = "red">可以用快慢指针解决以下两大类问题：</font>**  
1. 寻找/删除第K个结点  
2. 有关链表环问题的相关解法    

##### 1.2.2.2.1. 寻找/删除第K个结点  

    LeetCode 876：给定一个带有头结点 head 的非空单链表，返回链表的中间结点。如果有两个中间结点，则返回第二个中间结点。  

&emsp; 这里引入快慢指针，主要有三步 1、 快慢指针同时指向 head 的后继结点 2、 慢指针走一步，快指针走两步 3、 不断地重复步骤2，什么时候停下来呢，这取决于链表的长度是奇数还是偶数  

* 如果链表长度为奇数，当 fast.next = null 时，slow 为中间结点   
![Alt Text](https://gitee.com/wt1814/pic-host/raw/master/images/java/function/641.gif)  
* 如果链表长度为偶数，当 fast = null 时，slow 为中间结点  
![Alt Text](https://gitee.com/wt1814/pic-host/raw/master/images/java/function/642.gif)  

&emsp; 由以上分析可知：当 fast = null 或者 fast.next = null 时，此时的 slow 结点即为我们要求的中间结点，否则不断地重复步骤 2， 知道了思路，代码实现就简单了  

```java
/**
 * 使用快慢指针查找找到中间结点
 * @return
 */
public Node findMiddleNodeWithSlowFastPointer() {
    Node slow = head.next;
    Node fast = head.next;
    while (fast != null && fast.next != null) {
        // 快指针走两步
        fast = fast.next.next;
        // 慢指针走一步
        slow = slow.next;
    }
    // 此时的 slow 结点即为哨兵结点
    return slow;
}
```

##### 1.2.2.2.2. 有关链表环问题的相关解法  
&emsp; 接下来看如何用快慢指针来判断链表是否有环，这是快慢指针最常见的用法。  
&emsp; 判断链表是否有环，如果有，找到环的入口位置（下图中的 2），要求空间复杂度为O(1)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/function/function-20.png)  
&emsp; 首先要看如果链表有环有什么规律，如果从 head 结点开始遍历，则这个遍历指针一定会在以上的环中绕圈子，所以可以分别定义快慢指针，慢指针走一步，快指针走两步， 由于最后快慢指针在遍历过程中一直会在圈中里绕，且快慢指针每次的遍历步长不一样，所以它们在里面不断绕圈子的过程一定会相遇，就像 5000 米长跑，一人跑的快，一人快的慢，跑得快的人一定会追上跑得慢的（即套圈）。  

&emsp; 简单证明一下  
1. 假如快指针离慢指针相差一个结点，则再一次遍历，慢指针走一步，快指针走两步，相遇    
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/function/function-21.png)   
2. 假如快指针离慢指针相差两个结点，则再一次遍历，慢指针走一步，快指针走两步，相差一个结点，转成上述 1 的情况   
3. 假如快指针离慢指针相差 N 个结点（N大于2），则下一次遍历由于慢指针走一步，快指针走两步，所以相差 N+1-2 = N-1 个结点，发现了吗，相差的结点从 N 变成了 N-1，缩小了！不断地遍历，相差的结点会不断地缩小，当 N 缩小为 2 时，即转为上述步骤 2 的情况，由此得证，如果有环，快慢指针一定会相遇！   

        画外音：如果慢指针走一步，快指针走的不是两步，而是大于两步，会有什么问题，大家可以考虑一下

```java
/**
 * 判断是否有环,返回快慢指针相遇结点,否则返回空指针
 */
public Node detectCrossNode() {
    Node slow = head;
    Node fast = head;

    while (fast != null && fast.next != null) {
        fast = fast.next.next;
        slow = slow.next;
        
        if (fast == null) {
            return null;
        }

        if (slow == fast) {
            return slow;
        }
    }
    return  null;
}
```
&emsp; 判断有环为啥要返回相遇的结点，而不是返回 true 或 false 呢。 因为题目中还有一个要求，判断环的入口位置，就是为了这个做铺垫的，一起来看看怎么找环的入口，需要一些分析的技巧。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/function/function-22.png)  
&emsp; 假设上图中的 7 为快慢指针相遇的结点，不难看出慢指针走了 L + S 步，快指针走得比慢指针更快，它除了走了 L + S 步外，还额外在环里绕了 n  圈，所以快指针走了 L+S+nR 步（R为图中环的长度），另外每遍历一次，慢指针走了一步，快指针走了两步，所以快指针走的路程是慢指针的两倍,即 2 (L+S) = L+S+nR，即  L+S = nR  

* 当 n = 1 时，则 L+S = R 时,则从相遇点 7 开始遍历走到环入口点 2 的距离为 R - S = L，刚好是环的入口结点,而 head 与环入口点 2 的距离恰好也为 L，所以只要在头结点定义一个指针，在相遇点（7）定义另外一个指针，两个指针同时遍历，每次走一步，必然在环的入口位置 2 相遇
* 当 n > 1 时，L + S = nR，即 L = nR - S,  nR-S 怎么理解？可以看作是指针从结点  7 出发，走了 n 圈后，回退 S 步，此时刚好指向环入口位置，也就是说如果设置一个指针指向 head（定义为p1）, 另设一个指针指向 7（定义为p2），不断遍历，p2 走了 nR-S 时（即环的入口位置），p1也刚好走到这里（此时 p1 走了 nR-S =  L步，刚好是环入口位置），即两者相遇！

&emsp; 综上所述，要找到入口结点，只需定义两个指针，一个指针指向head, 一个指针指向快慢指向的相遇点，然后这两个指针不断遍历（同时走一步），当它们指向同一个结点时即是环的入口结点  

```java
public Node getRingEntryNode() {
    // 获取快慢指针相遇结点
    Node crossNode = detectCrossNode();

    // 如果没有相遇点，则没有环
    if (crossNode == null) {
        return null;
    }

    // 分别定义两个指针，一个指向头结点，一个指向相交结点
    Node tmp1 = head;
    Node tmp2 = crossNode;

    // 两者相遇点即为环的入口结点
    while (tmp1.data != tmp2.data) {
        tmp1 = tmp1.next;
        tmp2 = tmp2.next;
    }
    return tmp1;
}
```
