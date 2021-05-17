


# 双指针法
<!-- 
算法一招鲜——双指针问题
https://zhuanlan.zhihu.com/p/71643340
https://www.cnblogs.com/kyoner/p/11087755.html
-->


&emsp; 双指针技巧还可以分为两类，一类是「快慢指针」，另一类是「左右指针」。前者解决主要解决链表中的问题，比如典型的判定链表中是否包含环；后者主要解决数组（或者字符串）中的问题，比如二分查找。  


## 什么是双指针（对撞指针、快慢指针）
&emsp; 双指针，指的是在遍历对象的过程中，不是普通的使用单个指针进行访问，而是使用两个相同方向（快慢指针）或者相反方向（对撞指针）的指针进行扫描，从而达到相应的目的。  
&emsp; 换言之，双指针法充分使用了数组有序这一特征，从而在某些情况下能够简化一些运算。  
&emsp; 在LeetCode题库中，关于双指针的问题还是挺多的。![双指针](https://leetcode-cn.com/tag/two-pointers/problemset/)     

![image](https://gitee.com/wt1814/pic-host/raw/master/algorithm/function-55.png)  
<center>截图来之LeetCode中文官网</center>  


## 用法
### 对撞指针
&emsp; 对撞指针是指在有序数组中，将指向最左侧的索引定义为左指针(left)，最右侧的定义为右指针(right)，然后从两头向中间进行数组遍历。  
&emsp; 对撞数组适用于有序数组，也就是说当你遇到题目给定有序数组时，应该第一时间想到用对撞指针解题。  
&emsp; 伪代码大致如下：

```
function fn (list) {
  var left = 0;
  var right = list.length - 1;

  //遍历数组
  while (left <= right) {
    left++;
    // 一些条件判断 和处理
    ... ...
    right--;
  }
}
```

&emsp; 举个LeetCode上的例子：  
&emsp; 以LeetCode 881救生艇问题为例  
&emsp; 由于本题只要求计算出最小船数，所以原数组是否被改变，和元素索引位置都不考虑在内，所以可以先对于给定数组进行排序，再从数组两侧向中间遍历。所以解题思路如下：  

1. 对给定数组进行升序排序  
2. 初始化左右指针
3. 每次都用一个”最重的“和一个”最轻的“进行配对，如果二人重量小于Limit，则此时的”最轻的“上船，即（left++）。不管”最轻的“是否上船，”最重的“都要上船，即（right--）并且所需船数量加一，即（num++）  

&emsp; 代码如下：  

```
var numRescueBoats = function(people, limit) {
  people.sort((a, b) => (a - b));
  var num = 0
  let left = 0
  let right = people.length - 1
  while (left <= right) {
    if ((people[left] + people[right]) <= limit) {
      left++
    }
    right--
    num++
  }
  return num
};
```

### 快慢指针  
&emsp; 快慢指针也是双指针，但是两个指针从同一侧开始遍历数组，将这两个指针分别定义为快指针（fast）和慢指针（slow），两个指针以不同的策略移动，直到两个指针的值相等（或其他特殊条件）为止，如fast每次增长两个，slow每次增长一个。  
&emsp; 以LeetCode 141.环形链表为例,，判断给定链表中是否存在环，可以定义快慢两个指针，快指针每次增长一个，而慢指针每次增长两个，最后两个指针指向节点的值相等，则说明有环。就好像一个环形跑道上有一快一慢两个运动员赛跑，如果时间足够长，跑地快的运动员一定会赶上慢的运动员。  

&emsp; 解题代码如下：  

```
/**
 * Definition for singly-linked list.
 * function ListNode(val) {
 *     this.val = val;
 *     this.next = null;
 * }
 */

/**
 * @param {ListNode} head
 * @return {boolean}
 */
var hasCycle = function(head) {
  if (head === null || head.next === null) {
    return false
  }

  let slow = head
  let fast = head.next

  while (slow !== fast) {
    if (fast === null || fast.next === null) {
      return false
    }
    slow = slow.next
    fast = fast.next.next
  }
  return true
};
```

&emsp; 再比如LeetCode 26 删除排序数组中的重复项，这里还是定义快慢两个指针。快指针每次增长一个，慢指针只有当快慢指针上的值不同时，才增长一个（由于是有序数组，快慢指针值不等说明找到了新值）。  

&emsp; 真实代码：  

```
var removeDuplicates = function (nums) {
  if (nums.length === 0) {
    return 0;
  }
  let slow = 0;
  for (let fast = 0; fast < nums.length; fast++) {
    if (nums[fast] !== nums[slow]) {
      slow++;
      nums[slow] = nums[fast];
    }
  }
  return slow + 1;
};
```

## 总结  
&emsp; 当遇到有序数组时，应该优先想到双指针来解决问题，因两个指针的同时遍历会减少空间复杂度和时间复杂度。  