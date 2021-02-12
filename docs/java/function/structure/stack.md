

<!-- TOC -->

- [1. 栈与队列](#1-栈与队列)
    - [1.1. 栈Stack](#11-栈stack)
        - [1.1.1. 栈的实现](#111-栈的实现)
            - [1.1.1.1. 顺序栈](#1111-顺序栈)
            - [1.1.1.2. 链式栈](#1112-链式栈)
        - [1.1.2. 有关栈的算法题](#112-有关栈的算法题)
            - [1.1.2.1. 利用栈实现字符串逆序](#1121-利用栈实现字符串逆序)
            - [1.1.2.2. 利用栈判断分隔符是否匹配](#1122-利用栈判断分隔符是否匹配)
            - [1.1.2.3. 用两个栈实现队列](#1123-用两个栈实现队列)
            - [1.1.2.4. 栈的压入、弹出序列](#1124-栈的压入弹出序列)
            - [1.1.2.5. 使用两个栈进行排序](#1125-使用两个栈进行排序)
    - [1.2. 队列](#12-队列)

<!-- /TOC -->

&emsp; **小结：**  
&emsp; 栈通常用于“历史”的回溯，也就是逆流而上追溯“历史”。例如：实现递归的逻辑，就可以用栈来代替。  
&emsp; 队列通常用于对“历史”的回放，也就是按照“历史”顺序，把“历史”重演一遍。 

# 1. 栈与队列
## 1.1. 栈Stack  
<!-- https://www.cnblogs.com/ysocean/p/7911910.html 
&emsp; **栈与递归：**用非递归写递归，都需要用到栈。  
-->
&emsp; **栈的实现：**既可以用「数组」来实现一个栈，也可以用「链表」来实现一个栈。  
&emsp; **栈的基本操作：**栈有两种操作：Push和Pop。用Push(压入)来表示往栈中插入数据，也叫入栈，用Pop(弹出)来表示从栈中删除数据，也叫出栈。  
&emsp; **栈的应用：**栈的输出顺序和输入顺序相反，所以 **<font color = "red">栈通常用于“历史”的回溯，也就是逆流而上追溯“历史”。例如：实现递归的逻辑，就可以用栈来代替。</font>** 因为栈可以回溯方法的调用链。  

### 1.1.1. 栈的实现  
#### 1.1.1.1. 顺序栈  
&emsp; 用数组实现的栈，叫做顺序栈。顺序栈的实现非常简单。先初始化一个数组，然后再用一个变量给这个数组里的元素进行计数，当有新元素需要入栈的时候，将这个新元素写入到数组的最后一个元素的后面，然后计数器加一。当需要做出栈操作时，将数组中最后一个元素返回，计数器减一。  
&emsp; 在入栈前需要判断数组是否已经满了，如果数组大小等于计数器大小，则表明数组是满的。  
&emsp; 出栈的时候也需要判断数组是不是空数组，如果计数器是0，则表明数组是空的。  
&emsp; 从上面的实现流程可以看出，通过数组实现的栈，其入栈和出栈都是对单个元素进行操作，因此其入栈和出栈的时间复杂度都是O(1)，并且其入栈和出栈操作并没有额外开销更多空间，因此其空间复杂度也是O(1)的。  

#### 1.1.1.2. 链式栈  
&emsp; 用链表实现的栈，叫做链式栈。实现思路是先定义一个链表节点的类，基于这个类去定义一个头节点Head。当有新元素需要入栈的时候，将这个新元素的Next指针指向头结点Head的Next节点，然后再将Head的Next指向这个新节点。当需要做出栈操作时，直接将Head所指向的节点返回，同时让Head指向下一个节点。  
&emsp; 在入栈和出栈时都需要判断链表是否为空的情况。  
&emsp; 链式栈的入栈和出栈都是在处理头部节点，所以操作很简单，其时间和空间复杂度均为O(1)。  

&emsp; 用单向链表实现栈：栈的pop()方法和push()方法，对应于链表的在头部删除元素deleteHead()以及在头部增加元素addHead()。  

```java
public class StackSingleLink {
    private SingleLinkedList link;

    public StackSingleLink(){
        link = new SingleLinkedList();
    }

    //添加元素
    public void push(Object obj){
        link.addHead(obj);
    }
    //移除栈顶元素
    public Object pop(){
        Object obj = link.deleteHead();
        return obj;
    }
    //判断是否为空
    public boolean isEmpty(){
        return link.isEmpty();
    }
    //打印栈内元素信息
    public void display(){
        link.display();
    }

}
```

### 1.1.2. 有关栈的算法题  
#### 1.1.2.1. 利用栈实现字符串逆序  
&emsp; 可以将一个字符串分隔为单个的字符，然后将字符一个一个push()进栈，在一个一个pop()出栈就是逆序显示了。如：将字符串“how are you” 反转！！！

    ps：这里是用上面自定的栈来实现的，大家可以将ArrayStack替换为JDK自带的栈类Stack试试    

```java
//进行字符串反转
@Test
public void testStringReversal(){
    ArrayStack stack = new ArrayStack();
    String str = "how are you";
    char[] cha = str.toCharArray();
    for(char c : cha){
        stack.push(c);
    }
    while(!stack.isEmpty()){
        System.out.print(stack.pop());
    }
}

//结果：
//uoy era woh
```

#### 1.1.2.2. 利用栈判断分隔符是否匹配  
&emsp; 在xml标签或者html标签中，<必须和最近的>进行匹配，[ 也必须和最近的 ] 进行匹配。  
&emsp; 比如：<abc[123]abc>这是符号相匹配的，如果是 <abc[123>abc] 那就是不匹配的。  
&emsp; 对于 12<a[b{c}]>，分析在栈中的数据：遇到匹配正确的就消除
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/function/function-23.png)  
&emsp; 最后栈中的内容为空则匹配成功，否则匹配失败！！！  

```java
//分隔符匹配
//遇到左边分隔符了就push进栈，遇到右边分隔符了就pop出栈，看出栈的分隔符是否和这个有分隔符匹配
@Test
public void testMatch(){
    ArrayStack stack = new ArrayStack(3);
    String str = "12<a[b{c}]>";
    char[] cha = str.toCharArray();
    for(char c : cha){
        switch (c) {
            case '{':
            case '[':
            case '<':
                stack.push(c);
                break;
            case '}':
            case ']':
            case '>':
                if(!stack.isEmpty()){
                    char ch = stack.pop().toString().toCharArray()[0];
                    if(c=='}' && ch != '{'
                            || c==']' && ch != '['
                            || c==')' && ch != '('){
                        System.out.println("Error:"+ch+"-"+c);
                    }
                }
                break;
            default:
                break;
        }
    }
}
```

&emsp; 遍历输入字符串：

* 如果当前字符为左半边括号时，则将其压入栈中；  
* 如果遇到右半边括号时，分类讨论：  
    * 如栈不为空且为对应的左半边括号，则取出栈顶元素，继续循环  
    * 若此时栈为空，则直接返回 false  
    * 若不为对应的左半边括号，反之返回 false  

```java
class Solution {
    public boolean isValid(String s) {
        Stack<Character> stack = new Stack<>();
        char[] chars = s.toCharArray();
        for (char aChar : chars) {
            if (stack.size() == 0) {
                stack.push(aChar);
            } else if (isSym(stack.peek(), aChar)) {
                stack.pop();
            } else {
                stack.push(aChar);
            }
        }
        return stack.size() == 0;
    }

    private boolean isSym(char c1, char c2) {
        return (c1 == '(' && c2 == ')') || (c1 == '[' && c2 == ']') || (c1 == '{' && c2 == '}');
    }
}
```

#### 1.1.2.3. 用两个栈实现队列  
<!-- 
如何用两个栈实现一个队列？
https://mp.weixin.qq.com/s/alba6Zdn_3KyUrlI91Hx8g
-->
&emsp; 用两个栈来实现一个队列，完成队列的 Push 和 Pop 操作。  
&emsp; 解题思路：in 栈用来处理入栈(push)操作，out 栈用来处理出栈(pop)操作。一个元素进入 in 栈之后，出栈的顺序被反转。当元素要出栈时，需要先进入 out 栈，此时元素出栈顺序再一次被反转，因此出栈顺序就和最开始入栈顺序是相同的，先进入的元素先退出，这就是队列的顺序。  

&emsp; push元素时，始终是进入栈，pop和peek元素时始终是走出栈。  
&emsp; pop 和 peek 操作，如果出栈为空，则需要从入栈将所有元素移到出栈，也就是调换顺序，比如开始push的顺序是 3-2-1，1 是最先进入的元素，则到出栈的顺序是 1-2-3，那 pop 操作拿到的就是 1，满足了先进先出的特点。  
&emsp; pop和peek操作，如果出栈不为空，则不需要从入栈中移到数据到出栈。  

```java
Stack<Integer> in = new Stack<Integer>();
Stack<Integer> out = new Stack<Integer>();

public void push(int node) {
    in.push(node);
}

public int pop() throws Exception {
    if (out.isEmpty())
        while (!in.isEmpty())
            out.push(in.pop());

    if (out.isEmpty())
        throw new Exception("queue is empty");

    return out.pop();
}
```

#### 1.1.2.4. 栈的压入、弹出序列  
&emsp; **问题描述：**  
&emsp; 输入两个整数序列，第一个序列表示栈的压入顺序，请判断第二个序列是否为该栈的弹出顺序。假设压入栈的所有数字均不相等。例如序列1，2，3，4，5 是某栈的压入顺序，序列4，5，3，2，1是该压栈序列对应的一个弹出序列，但4，3，5，1，2就不可能是该压栈序列的弹出序列。(注意：这两个序列的长度是相等的)  
&emsp; **解题思路：**  
&emsp; 借用一个辅助的栈，遍历压栈顺序，先讲第一个放入栈中，这里是1，然后判断栈顶元素是不是出栈顺序的第一个元素，这里是4，很显然1≠4 ，所以需要继续压栈，直到相等以后开始出栈。  
&emsp; 出栈一个元素，则将出栈顺序向后移动一位，直到不相等，这样循环等压栈顺序遍历完成，如果辅助栈还不为空，说明弹出序列不是该栈的弹出顺序。  

```java
public boolean IsPopOrder(int[] pushSequence, int[] popSequence) {
    int n = pushSequence.length;
    Stack<Integer> stack = new Stack<>();
    for (int pushIndex = 0, popIndex = 0; pushIndex < n; pushIndex++) {
        stack.push(pushSequence[pushIndex]);
        while (popIndex < n && !stack.isEmpty()
                && stack.peek() == popSequence[popIndex]) {
            stack.pop();
            popIndex++;
        }
    }
    return stack.isEmpty();
}
```

#### 1.1.2.5. 使用两个栈进行排序  
<!-- 
https://www.nowcoder.com/questionTerminal/d0d0cddc1489476da6b782a6301e7dec
https://blog.csdn.net/windflybird/article/details/80186048
https://www.jianshu.com/p/6cf87a4fbebf
https://blog.csdn.net/MrLiar17/article/details/86704959
-->

## 1.2. 队列  
<!-- 
https://www.cnblogs.com/ysocean/p/7921930.html
https://www.jianshu.com/p/8439a9854932
-->
&emsp; **队列的实现：**与栈类似，队列即可以用数组实现，也可以用链表实现。  
&emsp; **队列的基本操作：**队列有两种操作入队(enqueue)、出队(dequeue)。  
&emsp; 如果队列不断出队，队头左边的空间失去作用，那队列的容量越来越小。 **<font color = "red">用数组实现的队列可以采用循环队列的方式来维持队列容量的恒定。</font>**  
&emsp; **队列的应用：**队列的输出顺序和输入顺序相同，所以队列通常用于对“历史”的回放，也就是按照“历史”顺序，把“历史”重演一遍。  

&emsp; **<font color = "red">双端队列，</font>** 可以结合栈和队列的特点，即可以先入先出，也可以先入后出。  
&emsp; **<font color = "red">优先级队列，</font>** 遵循的不是先入先出，而是谁的优先级最高，谁先出队列。优先级队列是基于二叉堆来实现的。  
