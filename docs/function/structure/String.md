
<!-- TOC -->

- [1. 字符串算法题](#1-字符串算法题)
    - [1.1. 最长公共前缀](#11-最长公共前缀)
        - [1.1.1. 题目描述](#111-题目描述)
        - [1.1.2. 解法](#112-解法)
    - [1.2. 最长回文子串](#12-最长回文子串)
    - [1.3. 翻转字符串里的单词](#13-翻转字符串里的单词)
    - [1.4. 反转字符串（双指针方式）](#14-反转字符串双指针方式)

<!-- /TOC -->


# 1. 字符串算法题
<!-- 
https://blog.csdn.net/qq_17681679/article/details/108735222
https://www.jianshu.com/p/4ef3cfa01367
-->

## 1.1. 最长公共前缀  
### 1.1.1. 题目描述
&emsp; 编写一个函数来查找字符串数组中的最长公共前缀。  

&emsp; 如果不存在公共前缀，返回空字符串 ""。  

![image](https://gitee.com/wt1814/pic-host/raw/master/algorithm/function-50.png)  

### 1.1.2. 解法
&emsp; 解法一  
&emsp; 这种解法是暴力循环法，从题目可知：最长公共前缀的最长长度一定是字符串数组中长度最短的那个字符串。  

&emsp; 首先找出长度最短的字符串str，假如str="abcf"。  
&emsp; 依次对'abcf'、'abc'、'ab'、'a'进行筛选，判断哪个是所有其他字符串的前缀。  
&emsp; 具体代码如下：  

```java
public String longestCommonPrefix(String[] strs) {
    if(strs.length==0){
        return "";
    }
    String minLengthString = strs[0];
    int minlength = minLengthString.length();
    for(int i=1; i<strs.length; i++) {
        if(minlength>strs[i].length()){
            minLengthString = strs[i];
            minlength = strs[i].length();
        }
    }

    int index = minlength;
    String result = "";
    while(index>0){
        String subString = minLengthString.substring(0, index);
        boolean isMax = true;
        for(int i=1; i<strs.length; i++){
            if(strs[i].startsWith(subString) == false){
                isMax = false;
                break;
            }
        }
        if(isMax){
            result = subString;
            break;
        }
        index --;
    }
    return result;
}
```


------

&emsp; 解法四    
&emsp; 利用分治的思想。    

```java
//分治
public String longestCommonPrefix5(String[] strs) {
    if(strs.length == 0) {
        return "";
    }
    int length = strs.length;
    return this.commonPrefix(strs, 0, length-1);
}

public String commonPrefix(String[] strs, int left, int right) {
    if(left == right) {
        return strs[left];
    } 
    int mid = (left + right) / 2;
    String leftStr = commonPrefix(strs, left, mid);
    String rightStr = commonPrefix(strs, mid+1, right);
    return this.commonPrefix(leftStr, rightStr);
}
public String commonPrefix(String left, String right) {
    int minLength = Math.min(left.length(), right.length());
    for (int i=0; i<minLength; i++) {
        if( left.charAt(i)!=right.charAt(i) ) {
            return left.substring(0, i);
        }
    }
    return left.substring(0, minLength);
}
```

--------

&emsp; 解法五   
&emsp; 利用二分法的思想  

1. 找出最短的字符串记为m;
2. 对字符串m进行二分,分割点记为mid，用tmp表示m\[low...high]。
3. 如果tmp为所有字符串的前缀，则mid右移，否则左移，直到low>high。


&emsp; 代码如下：  

```java
public String longestCommontPrefix6(String[] strs) {
    if(strs.length == 0) {
        return "";
    }
    int min = Integer.MAX_VALUE;
    for(String str : strs) {
        min = Math.min(min, str.length());
    }
    int low = 1;
    int high = min;
    while(low <= high) {
        int middle = (low + high)/2;
        if(this.isCommontPrefix(strs, middle)) {
            low = middle + 1;
        } else {
            high = middle - 1;
        }
    }
    return strs[0].substring(0, (low + high)/2);
}

public boolean isCommontPrefix(String[] strs, int length) {
    String tmp = strs[0].substring(0, length);
    for (int i=0; i<strs.length; i++) {
        if(!strs[i].startsWith(tmp)) {
            return false;
        }
    }
    return true;
}
```



## 1.2. 最长回文子串  
&emsp; 给定一个字符串 s，找到 s 中最长的回文子串。你可以假设 s 的最大长度为 1000。  
![image](https://gitee.com/wt1814/pic-host/raw/master/algorithm/function-51.png)  

&emsp; 题解思路：  

&emsp; 动态规划方式：  

```java
/*
* 动态规划方式：最长回文字符串
*/
public static String longestPalindrome(String s) {
    // 特判
    if (s.length() == 0 || s.length() == 1) return s;
    int length = s.length();
    // 用来记录字符串长度内，任意两个索引范围内的字符是否是回文字符串
    boolean[][] em = new boolean[length][length];
    String result = "";
    for (int l = 0; l < length; l++) {// 循环截取不同长度字符串进行验证，l为j-i角标的差值
        for (int i = 0; i + l < length; i++) {// 截取的字符串验证两端(i,j)字符是否相同
            int j = i + l;
            if (l == 0) {// 截取的只有一个字符，最长回文就是当前一个字符
                em[i][j] = true;
            } else if (l == 1) {// 截取只有两个字符，判断两个字符是否相同
                em[i][j] = (s.charAt(i) == s.charAt(j));
            } else {// 截取字符数>2时，判断i j字符是否相等的同时，会取到之前判断过的i+1,j-1位置上的字符是否相同
                em[i][j] = (s.charAt(i) == s.charAt(j) && em[i + 1][j - 1]);
            }
            // 当前两个索引范围的字符串是回文的，并且用result记录长度更长的一个
            if (em[i][j] && l + 1 > result.length()) {
                result = s.substring(i, i + l + 1);
            }
        }
    }
    return result;
}
```

## 1.3. 翻转字符串里的单词  
&emsp; 给定一个字符串，逐个翻转字符串中的每个单词。  
![image](https://gitee.com/wt1814/pic-host/raw/master/algorithm/function-52.png)  
&emsp; 题解思路：  

&emsp; 调用splite()方法分割为String[]，再倒叙拼接为字符串，并在拼接每个单词后拼接空格，最后trim一下。  

```java
public String reverseWords(String s) {
    StringBuilder sb = new StringBuilder();
    String[] words = s.split(" ");
    for (int i = words.length - 1; i >= 0; i--) {
        sb.append(words[i]);
        if (words[i].length() > 0 && i != 0)
            sb.append(" ");
    }
    return sb.toString().trim();
}
```

## 1.4. 反转字符串（双指针方式）  
&emsp; 编写一个函数，其作用是将输入的字符串反转过来。输入字符串以字符数组 char[] 的形式给出。  
&emsp; 不要给另外的数组分配额外的空间，你必须原地修改输入数组、使用 O(1) 的额外空间解决这一问题。  
&emsp; 你可以假设数组中的所有字符都是 ASCII 码表中的可打印字符。   
![image](https://gitee.com/wt1814/pic-host/raw/master/algorithm/function-53.png)  


&emsp; 题解思路：  
&emsp; 利用双指针（两端反向）分别从头和尾循环进行元素互换，直到两个指针相遇或头大于尾指针。  

```java
public void reverseString(char[] s) {
    for (int i = 0; i < (s.length + 1) / 2; i++) {
        int j = s.length - i - 1;
        char temp = s[i];
        s[i] = s[j];
        s[j] = temp;
    }
}
```


------

&emsp; 反转字符串中的单词 III（双指针）  
&emsp; 给定一个字符串，你需要反转字符串中每个单词的字符顺序，同时仍保留空格和单词的初始顺序。  
![image](https://gitee.com/wt1814/pic-host/raw/master/algorithm/function-54.png)  

&emsp; 题解思路：  
&emsp; 采用双指针解法，定义双指针（同向快慢）fast、slow，fast先指针向后遍历字符数组当遇到空格时，记录空格的下一位next并从空格的前一位与slow进行循环互换元素。互换完毕后将slow与fast同时指向所记录的next的位置上，fast继续向后遍历与反转调换。  

```java
public String reverseWords(String s) {
    char[] chars = s.toCharArray();
    int fast = 0, slow = 0;
    int length = chars.length;
    while (fast < length) {
        while (fast < length && chars[fast] != ' ') {
            fast++;
        }
        // 记录下一个单词开始字符角标
        int next = fast + 1;
        fast--;
        // 循环翻转单词字符串
        while (fast > slow) {
            // 互换两者位置
            char temp = chars[slow];
            chars[slow] = chars[fast];
            chars[fast] = temp;
            fast--;
            slow++;
        }
        fast = next;
        slow = next;

    }
    return new String(chars);
}
```