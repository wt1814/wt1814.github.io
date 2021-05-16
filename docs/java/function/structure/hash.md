

<!-- TOC -->

- [1. 哈希表](#1-哈希表)
    - [1.1. 哈希表基本概念](#11-哈希表基本概念)
        - [1.1.1. 哈希函数](#111-哈希函数)
        - [1.1.2. 哈希因子](#112-哈希因子)
        - [1.1.3. 处理冲突](#113-处理冲突)
    - [1.2. 几道和散列(哈希)表有关的题](#12-几道和散列哈希表有关的题)
        - [1.2.1. 两数之和](#121-两数之和)
        - [1.2.2. 无重复字符的最长子串](#122-无重复字符的最长子串)
        - [1.2.3. ~~三数之和~~](#123-三数之和)
        - [1.2.4. 两个数组的交集](#124-两个数组的交集)
        - [1.2.5. 两个数组的交集 II](#125-两个数组的交集-ii)
        - [1.2.6. 四数相加 II](#126-四数相加-ii)

<!-- /TOC -->

# 1. 哈希表  
<!-- 
https://mp.weixin.qq.com/s/OHROn0ya_nWR6qkaSFmacw
-->
## 1.1. 哈希表基本概念  
&emsp; 哈希表也称散列表，Hash表是一种根据关键字值(key - value)而直接进行访问的数据结构。  
&emsp; 哈希表基于数组，通过把关键字映射到数组的某个下标来加快查找速度，但是又和数组、链表、树等数据结构不同，在这些数据结构中查找某个关键字，通常要遍历整个数据结构，也就是O(N)的时间级，但是对于哈希表来说，只是O(1)的时间级。  
&emsp; **<font color = "clime">哈希数据结构的性能取决于三个因素：哈希函数、哈希因子、处理冲突方法。</font>**  

### 1.1.1. 哈希函数  
&emsp; 散列函数能使对一个数据序列的访问过程更加迅速有效，通过散列函数，数据元素将被更快地定位。常用Hash函数有：  
1. 直接寻址法。取关键字或关键字的某个线性函数值为散列地址。即H(key)=key或H(key) = a·key + b，其中a和b为常数(这种散列函数叫做自身函数)  
2. 数字分析法。分析一组数据，比如一组员工的出生年月日，这时发现出生年月日的前几位数字大体相同，这样的话，出现冲突的几率就会很大，但是发现年月日的后几位表示月份和具体日期的数字差别很大，如果用后面的数字来构成散列地址，则冲突的几率会明显降低。因此数字分析法就是找出数字的规律，尽可能利用这些数据来构造冲突几率较低的散列地址。  
3. 平方取中法。取关键字平方后的中间几位作为散列地址。  
4. 折叠法。将关键字分割成位数相同的几部分，最后一部分位数可以不同，然后取这几部分的叠加和(去除进位)作为散列地址。  
5. 随机数法。选择一随机函数，取关键字作为随机函数的种子生成随机值作为散列地址，通常用于关键字长度不同的场合。  
6. 除留余数法。取关键字被某个不大于散列表表长m的数p除后所得的余数为散列地址。即 H(key) = key MOD p，p<=m。不仅可以对关键字直接取模，也可在折叠、平方取中等运算之后取模。对p的选择很重要，一般取素数或m，若p选的不好，容易产生碰撞。  

&emsp; 注：HashMap并没有直接采用取模法(index = HashCode(Key)%Array.length)，而是利用了位运算来优化性能。  

### 1.1.2. 哈希因子  
&emsp; 哈希因子是表示Hsah表中元素的填满的程度，若哈希因子越大，则填满的元素越多，这样的好处是：空间利用率高了，但冲突的机会加大了。反之，加载因子越小，填满的元素越少，好处是冲突的机会减小了，但空间浪费多了。  

### 1.1.3. 处理冲突
&emsp; 理想情况下，两个不同的关键字映射到不同的单元，然而由于数组单元有限，关键字范围可能远超数组单元，因此就会出现两个关键字散列到同一个值得时候，这就是散列冲突。  
&emsp; **<font color = "clime">常用的哈希冲突解决方法有两类，</font><font color = "clime">开放寻址法和拉链法。</font>**  
&emsp; 处理冲突的方法决定了哈希表的数据结构。采用开放地址法，哈希表的数据结构是一维数组；采用链地址法，哈希表的数据结构是数组加链表。  
&emsp; 在Java中ThreaLocal所使用的是开放寻址法；HashMap使用拉链法。  
<!-- 
处理冲突方法:常用的哈希冲突解决方法有两类，开放寻址法和拉链法(chaining)。
1. 开放寻址法；Hi=(H(key) + di) MOD m,i=1,2,…，k(k<=m-1)，其中H(key)为散列函数，m为散列表长，di为增量序列，可有下列三种取法：
1). di=1,2,3,…，m-1，称线性探测再散列；
2). di=1^2,-1^2,2^2,-2^2,3^2,…，±k^2,(k<=m/2)称二次探测再散列；
3). di=伪随机数序列，称伪随机探测再散列。
2. 再散列法：Hi=RHi(key),i=1,2,…，k RHi均是不同的散列函数，即在同义词产生地址冲突时计算另一个散列函数地址，直到冲突不再发生，这种方法不易产生“聚集”，但增加了计算时间。
3. 链地址法(拉链法)
4. 建立一个公共溢出区
-->

## 1.2. 几道和散列(哈希)表有关的题  
<!-- 
几道和散列(哈希)表有关的面试题
https://www.cnblogs.com/fivestudy/p/10537611.html
-->

### 1.2.1. 两数之和  
&emsp; 题目来源于 LeetCode 上第 1 号问题：Two Sum。  
&emsp; 题目描述：给定一个整数数组 nums 和一个目标值 target，请你在该数组中找出和为目标值的那 两个 整数，并返回它们的数组下标。  
&emsp; 可以假设每种输入只会对应一个答案。但是，不能重复利用这个数组中同样的元素。  
示例：给定 nums = [2, 7, 11, 15], target = 9。因为 nums[0] + nums[1] = 2 + 7 = 9，所以返回 [0, 1]。  
&emsp; 题目解析：使用散列表来解决该问题。  
1. 首先设置一个 map 容器 record 用来记录元素的值与索引，然后遍历数组 nums 。  
2. 每次遍历时使用临时变量 complement 用来保存目标值与当前值的差值
3. 在此次遍历中查找 record ，查看是否有与 complement 一致的值，如果查找成功则返回4. 查找值的索引值与当前变量的值i
5. 如果未找到，则在 record 保存该元素与索引值 i

&emsp; 动画描述  
![Alt Text](https://gitee.com/wt1814/pic-host/raw/master/images/java/function/644.gif)  

&emsp; **代码实现**  

```java
// 1. Two Sum
// 时间复杂度：O(n)
// 空间复杂度：O(n)
class Solution {
public:
    vector<int> twoSum(vector<int>& nums, int target) {
        unordered_map<int,int> record;
        for(int i = 0 ; i < nums.size() ; i ++){
            int complement = target - nums[i];
            if(record.find(complement) != record.end()){
                int res[] = {i, record[complement]};
                return vector<int>(res, res + 2);
            }
            record[nums[i]] = i;
        }
    }
};
```

### 1.2.2. 无重复字符的最长子串  
&emsp; **题目来源：**LeetCode 上第 3 号问题： Longest Substring Without Repeating Characters 。  
&emsp; **题目描述：**给定一个字符串，请你找出其中不含有重复字符的 最长子串 的长度。  
&emsp; **题目解析：**建立一个 HashMap ，建立每个字符和其最后出现位置之间的映射，然后再定义两个变量 res 和 left ，其中 res 用来记录最长无重复子串的长度，left 指向该无重复子串左边的起始位置的前一个，一开始由于是前一个，所以在初始化时就是 -1。  
&emsp; 接下来遍历整个字符串，对于每一个遍历到的字符，如果该字符已经在 HashMap 中存在了，并且如果其映射值大于 left 的话，那么更新 left 为当前映射值，然后映射值更新为当前坐标i，这样保证了left始终为当前边界的前一个位置，然后计算窗口长度的时候，直接用 i-left 即可，用来更新结果 res 。  
&emsp; **代码实现：**  

```java
class Solution {
    public int lengthOfLongestSubstring(string s) {
        int res = 0, left = -1, n = s.size();
        unordered_map<int, int> m;
        for (int i = 0; i < n; ++i) {
            if (m.count(s[i]) && m[s[i]] > left) {
                left = m[s[i]];  
            }
            m[s[i]] = i;
            res = max(res, i - left);            
        }
        return res;
    }
};
```

&emsp; **拓展**  
&emsp; 此题也可以使用滑动窗口的概念来处理。  
&emsp; 建立一个 256 位大小的整型数组freg ，用来建立字符和其出现位置之间的映射。  
&emsp; 维护一个滑动窗口，窗口内的都是没有重复的字符，去尽可能的扩大窗口的大小，窗口不停的向右滑动。  
&emsp; (1)如果当前遍历到的字符从未出现过，那么直接扩大右边界；  
&emsp; (2)如果当前遍历到的字符出现过，则缩小窗口(左边索引向右移动)，然后继续观察当前遍历到的字符；  
&emsp; (3)重复(1)(2)，直到左边索引无法再移动；  
&emsp; (4)维护一个结果 res，每次用出现过的窗口大小来更新结果 res ，最后返回 res 获取结果。  
&emsp; 动画描述  
![Alt Text](https://gitee.com/wt1814/pic-host/raw/master/images/java/function/645.gif)  

&emsp; 代码实现  

```java
// 3. Longest Substring Without Repeating Characters
// 滑动窗口
// 时间复杂度: O(len(s))
// 空间复杂度: O(len(charset))
class Solution {
public:
    int lengthOfLongestSubstring(string s) {
        int freq[256] = {0};
        int l = 0, r = -1; //滑动窗口为s[l...r]
        int res = 0;
        // 整个循环从 l == 0; r == -1 这个空窗口开始
        // 到l == s.size(); r == s.size()-1 这个空窗口截止
        // 在每次循环里逐渐改变窗口, 维护freq, 并记录当前窗口中是否找到了一个新的最优值
        while(l < s.size()){
            if(r + 1 < s.size() && freq[s[r+1]] == 0){
                r++;
                freq[s[r]]++;
            }else {   //r已经到头 || freq[s[r+1]] == 1
                freq[s[l]]--;
                l++;
            }
            res = max(res, r-l+1);
        }
        return res;
    }
};
```

### 1.2.3. ~~三数之和~~  
&emsp; 题目来源于 LeetCode 上第 15 号问题： 3Sum 。  

&emsp; **题目描述**  
&emsp; 给定一个包含 n 个整数的数组 nums，判断 nums 中是否存在三个元素 a，b，c ，使得 a + b + c = 0 ？找出所有满足条件且不重复的三元组。  

&emsp; **题目解析**  
&emsp; 题目需要我们找出三个数且和为 0 ，那么除了三个数全是 0 的情况之外，肯定会有负数和正数，所以一开始可以先选择一个数，然后再去找另外两个数，这样只要找到两个数且和为第一个选择的数的相反数就行了。也就是说需要枚举 a 和 b ，将 c 的存入 map 即可。  
&emsp; 需要注意的是返回的结果中，不能有有重复的结果。这样的代码时间复杂度是 O(n^2)。在这里可以先将原数组进行排序，然后再遍历排序后的数组，这样就可以使用双指针以线性时间复杂度来遍历所有满足题意的两个数组合。  

&emsp; **代码实现**   

```java
class Solution {
public:
    vector<vector<int>> threeSum(vector<int>& nums) {
        vector<vector<int>> res;
        sort(nums.begin(), nums.end());
        if (nums.empty() || nums.back() < 0 || nums.front() > 0) return {};
        for (int k = 0; k < nums.size(); ++k) {
            if (nums[k] > 0) break;
            if (k > 0 && nums[k] == nums[k - 1]) continue;
            int target = 0 - nums[k];
            int i = k + 1, j = nums.size() - 1;
            while (i < j) {
                if (nums[i] + nums[j] == target) {
                    res.push_back({nums[k], nums[i], nums[j]});
                    while (i < j && nums[i] == nums[i + 1]) ++i;
                    while (i < j && nums[j] == nums[j - 1]) --j;
                    ++i; --j;
                } else if (nums[i] + nums[j] < target) ++i;
                else --j;
            }
        }
        return res;
    }
};
```

### 1.2.4. 两个数组的交集  
&emsp; 题目来源于 LeetCode 上第 349 号问题： Intersection of Two Arrays。  

&emsp; **题目描述**  
&emsp; 给定两个数组，编写一个函数来计算它们的交集。   

&emsp; **题目解析**  
&emsp; 容器类 set 的使用。  

* 遍历 num1，通过 set 容器 record 存储 num1 的元素  
* 遍历 num2，在 record 中查找是否有相同的元素，如果有，用 set 容器 resultSet 进行存储  
* 将 resultSet 转换为 vector 类型  


&emsp; **动画描述**  
![Alt Text](https://gitee.com/wt1814/pic-host/raw/master/images/java/function/646.gif)  

&emsp; **代码实现**   

```java
// 时间复杂度: O(nlogn)
// 空间复杂度: O(n)
class Solution {
public:
    vector<int> intersection(vector<int>& nums1, vector<int>& nums2) {
        set<int> record;
        for( int i = 0 ; i < nums1.size() ; i ++ ){
            record.insert(nums1[i]);
        }
        set<int> resultSet;
        for( int i = 0 ; i < nums2.size() ; i ++ ){
            if(record.find(nums2[i]) != record.end()){
                resultSet.insert(nums2[i]);
            }
        }
        vector<int> resultVector;
        for(set<int>::iterator iter = resultSet.begin(); iter != resultSet.end(); iter ++ ){
            resultVector.push_back(*iter);
        }

        return resultVector;
    }
};
```


### 1.2.5. 两个数组的交集 II
&emsp; 题目来源于 LeetCode 上第 350 号问题： Intersection of Two Arrays II。  
&emsp; 题目描述  
&emsp; 给定两个数组，编写一个函数来计算它们的交集。  

&emsp; 示例 1:  

```text
输入: nums1 = [1,2,2,1], nums2 = [2,2]
输出: [2,2]
```
&emsp; 示例 2:  

```text
输入: nums1 = [4,9,5], nums2 = [9,4,9,8,4]
输出: [4,9]
```
&emsp; **题目解析**  
&emsp; 与上题 两个数组的交集 类似。只不过这里使用的是 map 。 

* 遍历 num1，通过 map 容器 record 存储 num1 的元素与频率；
* 遍历 num2 ，在 record 中查找是否有相同的元素（该元素的存储频率大于 0 ），如果有，用 map 容器resultVector 进行存储，同时该元素的频率减一。

&emsp; **动画描述**  
![Alt Text](https://gitee.com/wt1814/pic-host/raw/master/images/java/function/647.gif)  

&emsp; **代码实现**  

```java
// 时间复杂度: O(nlogn)
// 空间复杂度: O(n)
class Solution {
public:
    vector<int> intersect(vector<int>& nums1, vector<int>& nums2) {
        map<int, int> record;
        for(int i = 0 ; i < nums1.size() ; i ++){
             record[nums1[i]] += 1;
        }
        vector<int> resultVector;
        for(int i = 0 ; i < nums2.size() ; i ++){
            if(record[nums2[i]] > 0){
                resultVector.push_back(nums2[i]);
                record[nums2[i]] --;
            }
        }
        return resultVector;
    }
};
```

### 1.2.6. 四数相加 II
&emsp; 题目来源于 LeetCode 上第 454 号问题： 4Sum II 。  
&emsp; **题目描述**  
给定四个包含整数的数组列表 A , B , C , D ,计算有多少个元组 (i, j, k, l) ，使得 A[i] + B[j] + C[k] + D[l] = 0。  
&emsp; 为了使问题简单化，所有的 A, B, C, D 具有相同的长度 N，且 0 ≤ N ≤ 500 。所有整数的范围在 -2^28 到 2^28- 1 之间，最终结果不会超过 2^31 - 1 。  

&emsp; **题目解析**  
&emsp; 与 Two Sum 极其类似，使用哈希表来解决问题。  

* 把 A 和 B 的两两之和都求出来，在哈希表中建立两数之和与其出现次数之间的映射；
* 遍历 C 和 D 中任意两个数之和，只要看哈希表存不存在这两数之和的相反数就行了。

&emsp; **动画描述**  
![Alt Text](https://gitee.com/wt1814/pic-host/raw/master/images/java/function/648.gif)  


&emsp; **代码实现**  

```java
// 时间复杂度: O(n^2)
// 空间复杂度: O(n^2)
class Solution {
public:
    int fourSumCount(vector<int>& A, vector<int>& B, vector<int>& C, vector<int>& D) {
        unordered_map<int,int> hashtable;
        for(int i = 0 ; i < A.size() ; i ++){
            for(int j = 0 ; j < B.size() ; j ++){
                 hashtable[A[i]+B[j]] += 1;
            }
        }
        int res = 0;
        for(int i = 0 ; i < C.size() ; i ++){
            for(int j = 0 ; j < D.size() ; j ++){
                if(hashtable.find(-C[i]-D[j]) != hashtable.end()){
                    res += hashtable[-C[i]-D[j]];
                }
            }
        }
        return res;
    }
};
```
