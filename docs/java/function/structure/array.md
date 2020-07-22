

<!-- TOC -->

- [1. 数组](#1-数组)
- [2. 链表](#2-链表)
    - [2.1. 单向链表](#21-单向链表)
    - [2.2. 链表解题思路](#22-链表解题思路)
        - [2.2.1. 链表翻转](#221-链表翻转)
            - [2.2.1.1. 非递归翻转链表（迭代解法）](#2211-非递归翻转链表迭代解法)
            - [2.2.1.2. 递归翻转](#2212-递归翻转)
            - [2.2.1.3. 变形题](#2213-变形题)
        - [2.2.2. 快慢指针](#222-快慢指针)
            - [2.2.2.1. 寻找/删除第 K 个结点](#2221-寻找删除第-k-个结点)
            - [2.2.2.2. 有关链表环问题的相关解法](#2222-有关链表环问题的相关解法)
                - [2.2.2.2.1. 判断是否有环？](#22221-判断是否有环)
                - [2.2.2.2.2. 找到入口结点](#22222-找到入口结点)

<!-- /TOC -->


# 1. 数组  
&emsp; 这里稍微讲解下二维数组。二维数组是一个元素为一维数组的数组。  

&emsp; 二维数组定义：

    数据类型[][] 变量名=new 数据类型[m][n];
        m表示这个二维数组有多少个数组
        n表示每一个一维数组的元素个数
    举例：
        int[][] arr=new int[3][2];定义了一个二维数组arr。这个二维数组有3个一维数组，名称是ar[0],arr[1],arr[2]；每个一维数组有2个元素，可以通过arr[m][n]来获取。

&emsp; 二维数组基本操作：遍历一个行列变化的二维数组。  

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


# 2. 链表  
&emsp; 链表分类：单向链表、双端链表、有序链表、双向链表。  

## 2.1. 单向链表  
&emsp; 链节点可以是一个单独的类，也可以是内部类。  

```
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

## 2.2. 链表解题思路  
&emsp; **<font color = "red">链表常见解题思路有翻转和快慢指针。</font>**  

### 2.2.1. 链表翻转  
&emsp; 翻转链表可以用非递归和递归这两种方式来解题。  

#### 2.2.1.1. 非递归翻转链表（迭代解法）  


#### 2.2.1.2. 递归翻转  

#### 2.2.1.3. 变形题  


### 2.2.2. 快慢指针  
&emsp; **<font color = "red">可以用快慢指针解决以下两大类问题：</font>**  
1. 寻找/删除第K个结点  
2. 有关链表环问题的相关解法    

#### 2.2.2.1. 寻找/删除第 K 个结点  


#### 2.2.2.2. 有关链表环问题的相关解法  
##### 2.2.2.2.1. 判断是否有环？  


##### 2.2.2.2.2. 找到入口结点  



