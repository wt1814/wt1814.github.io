

<!-- TOC -->

- [1. 比较排序](#1-比较排序)
    - [1.1. 冒泡排序](#11-冒泡排序)
        - [1.1.1. 算法描述](#111-算法描述)
        - [1.1.2. 编码](#112-编码)
    - [1.2. 快速排序](#12-快速排序)
        - [1.2.1. 算法描述](#121-算法描述)
        - [1.2.2. 编码](#122-编码)
    - [1.3. 直接选择排序](#13-直接选择排序)
        - [1.3.1. 算法描述](#131-算法描述)
        - [1.3.2. 编码](#132-编码)
    - [1.4. 堆排序](#14-堆排序)
        - [1.4.1. 算法描述](#141-算法描述)
        - [1.4.2. 编码](#142-编码)
    - [1.5. 直接插入排序](#15-直接插入排序)
        - [1.5.1. 算法描述](#151-算法描述)
        - [1.5.2. 编码](#152-编码)
        - [1.5.3. 二分插入排序](#153-二分插入排序)
        - [1.5.4. 二路插入排序](#154-二路插入排序)
    - [1.6. 希尔排序](#16-希尔排序)
        - [1.6.1. 算法描述](#161-算法描述)
        - [1.6.2. 编码](#162-编码)
    - [1.7. 归并排序](#17-归并排序)
        - [1.7.1. 算法描述](#171-算法描述)
        - [1.7.2. 编码](#172-编码)

<!-- /TOC -->


# 1. 比较排序  
<!-- 

手撕 9大排序算法
https://mp.weixin.qq.com/s/snBEpUsQ8ZhHLLuNarwqgA
-->

## 1.1. 冒泡排序  
&emsp; 冒泡排序是一种简单的排序算法。它重复地走访过要排序的数列，一次比较两个元素，如果它们的顺序错误就把它们交换过来。走访数列的工作是重复地进行直到没有再需要交换，也就是说该数列已经排序完成。这个算法的名字由来是因为越小的元素会经由交换慢慢“浮”到数列的顶端。   

### 1.1.1. 算法描述
1. 比较相邻的元素。如果第一个比第二个大，就交换它们两个；  
2. 对每一对相邻元素作同样的工作，从开始第一对到结尾的最后一对，这样在最后的元素会是最大的数；  
3. 针对所有的元素重复以上的步骤，除了最后一个；  
4. 重复步骤1~3，直到排序完成。  

&emsp; **优化一：**  
&emsp; 某一次排序后，整个排序都已经完成了，但是常规版还是会继续排序。可以设置一个标志位，用来表示当前第 i 趟是否有交换，如果有则要进行 i+1 趟，如果没有，则说明当前数组已经完成排序。  

&emsp; **优化二：**  
&emsp; 第i趟排的第 i 小或者大的元素已经在第 i 位上了，甚至可能第 i-1 位也已经归位了，那么在内层循环的时候，有这种情况出现就会导致多余的比较出现。例如：6，4，7，5，1，3，2，当进行第一次排序的时候，结果为6，7，5，4，3，2，1，实际上后面有很多次交换比较都是多余的，因为没有产生交换操作。
利用一个标志位，记录一下当前第 i 趟所交换的最后一个位置的下标，在进行第 i+1 趟的时候，只需要内循环到这个下标的位置就可以了，因为后面位置上的元素在上一趟中没有换位，这一次也不可能会换位置了。  

### 1.1.2. 编码  

```java
/**
 * 冒泡排序优化版
 * @param array
 */
public static void sort(int array[]){
    int tmp  = 0;
    //记录最后一次交换的位置
    int lastExchangeIndex = 0;
    //无序数列的边界，每次比较只需要比到这里为止
    int sortBorder = array.length - 1;
    for(int i = 0; i < array.length; i++)
    {
        //有序标记，每一轮的初始是true
        boolean isSorted = true;

        for(int j = 0; j < sortBorder; j++)
        {
            if(array[j] > array[j+1])
            {
                tmp = array[j];
                array[j] = array[j+1];
                array[j+1] = tmp;
                //有元素交换，所以不是有序，标记变为false
                isSorted = false;
                //把无序数列的边界更新为最后一次交换元素的位置
                lastExchangeIndex = j;
            }
        }
        sortBorder = lastExchangeIndex;
        if(isSorted){
            break;
        }
    }
}

public static void main(String[] args){
    int[] array = new int[]{3,4,2,1,5,6,7,8};
    sort(array);
    System.out.println(Arrays.toString(array));
}
```


## 1.2. 快速排序  
&emsp; 快速排序的基本思想：通过一趟排序将待排记录分隔成独立的两部分，其中一部分记录的关键字均比另一部分的关键字小，则可分别对这两部分记录继续进行排序，以达到整个序列有序。  

### 1.2.1. 算法描述  
1. 选择一个基准元素；  
2. 通过一趟排序将待排序的记录分割成独立的两部分，其中一部分记录的元素值均比基准元素值小。另一部分记录的元素值比基准值大；  
3. 此时基准元素在其排好序后的正确位置；  
4. 然后分别对这两部分记录用同样的方法继续进行排序，直到整个序列有序。  

&emsp; **基准元素的选择，以及元素的移动是快速排序的核心问题。**  
* 基准元素的选择：  
&emsp; 最简单的方式是选择数列的第一个元素。但是当它为最大值或最小值时，快速排序的效率会严重降低。折中的方法是找到数组中的第一个、最后一个以及处于中间位置的元素，选出三者的中值作为枢纽，既避免了枢纽是最值的情况，也不会像在全部元素中寻找中值那样费时间。这种方法被称为“三项取中法”
(median-of-three)。  
* 元素的移动：  
&emsp; 选定了基准元素以后，把其他元素当中小于基准元素的都移动到基准元素一边，大于基准元素的都移动到基准元素另一边。  
&emsp; 具体如何实现呢？有两种方法：双边循环法、单边循环法。  

### 1.2.2. 编码  
&emsp; **递归实现(包含单边循环、双边循环)：**  

```java
public class quickSort {

    public static void main(String[] args) {
        int[] arr = new int[] {4,4,6,5,3,2,8,1};
        quickSort(arr, 0, arr.length-1);
        System.out.println(Arrays.toString(arr));
    }

    /**
     * 快速排序(递归实现) 
     * @param arr
     * @param startIndex
     * @param endIndex
     */
    public static void quickSort(int[] arr, int startIndex, int endIndex) {
        // 递归结束条件：startIndex大等于endIndex的时候
        if (startIndex >= endIndex) {
            return;
        }
        // 得到基准元素位置
        //TODO 可以替换两种循环法
        int pivotIndex = partitionV2(arr, startIndex, endIndex);
        // 根据基准元素，分成两部分递归排序
        quickSort(arr, startIndex, pivotIndex - 1);
        quickSort(arr, pivotIndex + 1, endIndex);
    }


    /**
     * 分治(单边循环法)
     * @param arr     待交换的数组
     * @param startIndex    起始下标
     * @param endIndex    结束下标
     */
    private static int partitionV2(int[] arr, int startIndex, int endIndex) {
        // 取第一个位置的元素作为基准元素(也可以选择随机位置)
        int pivot = arr[startIndex];
        int mark = startIndex;

        for(int i=startIndex+1; i<=endIndex; i++){
            if(arr[i]<pivot){
                mark ++;
                int p = arr[mark];
                arr[mark] = arr[i];
                arr[i] = p;
            }
        }

        arr[startIndex] = arr[mark];
        arr[mark] = pivot;
        return mark;
    }

    /**
     * 分治(双边循环法)
     * @param arr     待交换的数组
     * @param startIndex    起始下标
     * @param endIndex    结束下标
     */
    private static int partition(int[] arr, int startIndex, int endIndex) {
        // 取第一个位置的元素作为基准元素(也可以选择随机位置)
        int pivot = arr[startIndex];
        int left = startIndex;
        int right = endIndex;

        while( left != right) {
            //控制right指针比较并左移
            while(left<right && arr[right] > pivot){
                right--;
            }
            //控制left指针比较并右移
            while( left<right && arr[left] <= pivot) {
                left++;
            }
            //交换left和right指向的元素
            if(left<right) {
                int p = arr[left];
                arr[left] = arr[right];
                arr[right] = p;
            }
        }

        //pivot和指针重合点交换
        arr[startIndex] = arr[left];
        arr[left] = pivot;

        return left;
    }

}
```
-----
## 1.3. 直接选择排序  
&emsp; 从第一个元素开始，扫描整个待排数组，找到最小的元素放之后再与第一个元素交换位置，然后再从第二个元素开始，继续寻找最小的元素与第二个元素交换位置，依次类推。  
&emsp; 表现最稳定的排序算法之一，因为无论什么数据进去都是O(n2)的时间复杂度，所以用到它的时候，数据规模越小越好。好处就是不占用额外的内存空间。  

### 1.3.1. 算法描述  
1. 首先在未排序序列中找到最小(大)元素，存放到排序序列的起始位置
2. 再从剩余未排序元素中继续寻找最小(大)元素，然后放到已排序序列的末尾。
3. 重复第二步，直到所有元素均排序完毕。  

&emsp; **优化：**  
&emsp; 每趟排序确定两个最值——最大值与最小值，这样就可以将排序趟数缩减一半。  

### 1.3.2. 编码  

```java
/**
* 选择排序
* @param array
* @return
*/
public static int[] selectionSort(int[] array) {
    if (array.length == 0)
        return array;
    for (int i = 0; i < array.length; i++) {
        int minIndex = i;
        for (int j = i; j < array.length; j++) {
            if (array[j] < array[minIndex]) //找到最小的数
                minIndex = j; //将最小数的索引保存
        }
        int temp = array[minIndex];
        array[minIndex] = array[i];
        array[i] = temp;
    }
    return array;
}


/**
* 选择排序改进版
* @param array
*/
public static void selectionSort_improvement(int[] array){
    int minPoint;  //存储最小元素的小标
    int maxPoint;  //存储最大元素的小标
    int len = array.length;
    int temp;
    int counter = 1;

    for(int i=0;i<len/2;i++){
        minPoint= i;
        maxPoint= i;
        for(int j=i+1;j<=len-1-i;j++){  //每完成一轮排序，就确定了两个最值，下一轮排序时比较范围减少两个元素
            if(array[j]<array[minPoint]){  //如果待排数组中的某个元素比当前元素小，minPoint指向该元素的下标
                minPoint= j;
                continue;
            }else if(array[j]>array[maxPoint]){  //如果待排数组中的某个元素比当前元素大，maxPoint指向该元素的下标
                maxPoint= j;
            }
        }

        if(minPoint!=i){  //如果发现了更小的元素，与第一个元素交换位置
            temp= array[i];
            array[i]= array[minPoint];
            array[minPoint]= temp;

            //原来的第一个元素已经与下标为minPoint的元素交换了位置
            //如果之前maxPoint指向的是第一个元素，那么需要将maxPoint重新指向array[minPoint]
            //因为现在array[minPoint]存放的才是之前第一个元素中的数据
            if(maxPoint== i){
                maxPoint= minPoint;
            }

        }

        if(maxPoint!=len-1-i){  //如果发现了更大的元素，与最后一个元素交换位置
            temp= array[len-1-i];
            array[len-1-i]= array[maxPoint];
            array[maxPoint]= temp;
        }
        System.out.print("第"+counter+"轮排序结果：");
        System.out.println(Arrays.toString(array));
        counter++;
    }
}


public static void main(String[] args) {
    int[] arr = new int[] {4,7,6,5,3,2,8,1};
    selectionSort_improvement(arr);
}
```


## 1.4. 堆排序  
<!--
Heap Sort 堆排序与 Top K 问题 
https://mp.weixin.qq.com/s?__biz=MzA4NDE4MzY2MA==&mid=2647523648&idx=1&sn=a2c93982ccfe331d4db33b61acadc08d&chksm=87d1be43b0a637558f6b84cdb77de72bd6890a1fc072714083b004e96398f7d3926a808131f3&scene=178#rd
https://mp.weixin.qq.com/s/D_RPGriu3xMGYA11TMzxeA
-->

&emsp; 首先了解二叉堆。堆(Heap)是一类基于完全二叉树的特殊数据结构。通常将堆分为两种类型：  

* <font color = "lime">大顶堆(Max Heap)：在大顶堆中，根结点的值必须大于它的孩子结点的值，对于二叉树中所有子树也应递归地满足这一特性。</font> 
* 小顶堆(Min Heap)：在小顶堆中，根结点的值必须小于它的孩子结点的值，且对于二叉树的所有子树也均递归地满足同一特性。  

&emsp; 二叉堆的构建、删除、自我调整等基本操作是实现堆排序的基础。  



### 1.4.1. 算法描述  
1. 把无序数组构建成二叉堆。需要从小到大排序，则构建成最大堆；需要从大到小排序，则构建成最小堆。  
2. 循环删除堆顶元素，替换到二叉堆大末尾，调整堆产生新堆堆顶。  

### 1.4.2. 编码  
&emsp; 最大堆排序  

```java
/**
* 下沉调整
* @param array     待调整的堆
* @param parentIndex    要下沉的父节点
* @param length    堆的有效大小
*/
public static void downAdjust(int[] array, int parentIndex, int length) {
    // temp保存父节点值，用于最后的赋值
    int temp = array[parentIndex];
    int childIndex = 2 * parentIndex + 1;
    while (childIndex < length) {
        // 如果有右孩子，且右孩子大于左孩子的值，则定位到右孩子
        if (childIndex + 1 < length && array[childIndex + 1] > array[childIndex]) {
            childIndex++;
        }
        // 如果父节点大于等于任何一个孩子的值，直接跳出
        if (temp >= array[childIndex])
            break;
        //无需真正交换，单向赋值即可
        array[parentIndex] = array[childIndex];
        parentIndex = childIndex;
        childIndex = 2 * childIndex + 1;
    }
    array[parentIndex] = temp;
}

/**
    * 堆排序(升序)
    * @param array     待调整的堆
    */
public static void heapSort(int[] array) {
    // 1.把无序数组构建成最大堆。
    for (int i = (array.length-2)/2; i >= 0; i--) {
        downAdjust(array, i, array.length);
    }
    System.out.println(Arrays.toString(array));
    // 2.循环交换集合尾部元素到堆顶，并调节堆产生新的堆顶。
    for (int i = array.length - 1; i > 0; i--) {
        // 最后一个元素和第一元素进行交换
        int temp = array[i];
        array[i] = array[0];
        array[0] = temp;
        // 下沉调整最大堆
        downAdjust(array, 0, i);
    }
}

public static void main(String[] args) {
    int[] arr = new int[] {1,3,2,6,5,7,8,9,10,0};
    heapSort(arr);
    System.out.println(Arrays.toString(arr));
}
```
-----
## 1.5. 直接插入排序  
&emsp; 每一趟将一个待排序的记录，按照其关键字的大小插入到有序队列的合适位置里，直到全部插入完成。  

### 1.5.1. 算法描述
1. 从第一个元素开始，该元素可以认为已经被排序；
2. 取出下一个元素，在已经排序的元素序列中从后向前扫描；
3. 如果该元素(已排序)大于新元素，将该元素移到下一位置；
4. 重复步骤3，直到找到已排序的元素小于或者等于新元素的位置；
5. 将新元素插入到该位置后；
6. 重复步骤2~5。  

### 1.5.2. 编码  

```java
public static void insertSort(int[] array) {
    if (array.length == 0)
        return;

    for (int i = 1; i < array.length; ++i) {
        int value = array[i];
        int j = i - 1;
        // 查找插入的位置
        for (; j >= 0; --j) {
            if (array[j] > value) {
                array[j + 1] = array[j];  // 数据移动
            } else {
                break;
            }
        }
        array[j + 1] = value; // 插入数据
    }
}
```


### 1.5.3. 二分插入排序   
&emsp; 二分插入排序：在插入某个元素之前需要先确定该元素在有序数组中的位置，上例的做法是对有序数组中的元素逐个扫描，当数据量比较大的时候，这是一个很耗时间的过程，可以采用二分查找法改进，这种排序也被称为二分插入排序。 

```java
/**
* 二分插入排序
* @param array
*/
public static void binaryInsertionSort(int[] array){

    int counter = 1;

    for(int i=1;i<array.length;i++){
        int temp = array[i];  //存储待排序的元素值
        if(array[i-1]>temp){  //比有序数组的最后一个元素要小
            int intinsertIndex = binarySearch(array,0, i-1, temp); //获取应插入位置的下标
            for(int j=i;j>intinsertIndex;j--){  //将有序数组中，插入点之后的元素后移一位
                array[j]= array[j-1];
            }
            array[intinsertIndex]= temp;  //插入待排序元素到正确的位置
        }
        System.out.print("第"+counter+"轮排序结果：");
        System.out.println(Arrays.toString(array));
        counter++;
    }
}

/**
    * 二分查找法
    * @param lowerBound 查找段的最小下标
    * @param upperBound 查找段的最大下标
    * @param target 目标元素
    * @return 目标元素应该插入位置的下标
    */
public static int binarySearch(int[] array,int lowerBound,int upperBound,int target){
    int curIndex;
    while(lowerBound<upperBound){
        curIndex= (lowerBound+upperBound)/2;
        if(array[curIndex]>target){
            upperBound= curIndex - 1;
        }else{
            lowerBound= curIndex + 1;
        }
    }
    return lowerBound;
}
```

### 1.5.4. 二路插入排序  
......

----
## 1.6. 希尔排序  
&emsp; 先将整个待排序的记录序列分割成为若干子序列，分别进行直接插入排序，待整个序列中的记录“基本有序”时，再对全体记录进行依次直接插入排序。  

### 1.6.1. 算法描述  
1. 选择一个增量序列t1，t2，…，tk，其中ti>tj，tk=1；
2. 按增量序列个数k，对序列进行k 趟排序；
3. 每趟排序，根据对应的增量ti，将待排序列分割成若干长度为m的子序列，分别对各子表进行直接插入排序。仅增量因子为1时，整个序列作为一个表来处理，表长度即为整个序列的长度。  

### 1.6.2. 编码  

```java
public static void sheelSort(int[] array){
    //增量每次都/2
    for (int step = array.length/2; step>0; step /=2){
        //从增量那组开始进行插入排序，直至完毕
        for (int i = step; i<array.length;i++){
            int j = i;
            int temp = array[i];

            //j-step 就是代表与它同组隔壁的元素
            while (j -step >=0 && array[j-step]>temp){

                array[j] = array[j-step];
                j = j-step;
            }
            array[j] = temp;
        }
    }
}
```

## 1.7. 归并排序
&emsp; 归并排序是分治算法的典型应用。  
&emsp; 归并排序先将一个无序的N长数组切成N个有序子序列(只有一个数据的序列认为是有序序列)，然后两两合并，再将合并后的N/2(或者N/2 + 1)个子序列继续进行两两合并，以此类推得到一个完整的有序数组。过程如下图所示：
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/function/function-3.png)  

&emsp; 归并排序其实要做两件事：  

* “分解”——将序列每次折半划分。  
* “合并”——将划分后的序列段两两合并后排序。  

### 1.7.1. 算法描述

1. 将数组分成A，B 两个数组，如果这2个数组都是有序的，那么就可以很方便的将这2个数组进行排序。  	
2. 让这2个数组有序，可以将A，B组各自再分成2个数组。依次类推，当分出来的数组只有1个数据时，可以认为数组已经达到了有序。  
3. 然后再合并相邻的2个数组。这样通过先递归的分解数组，再合并数组就完成了归并排序。   

### 1.7.2. 编码

```java
public static void mergeSort(int[] array, int start, int end){
    if(start < end){
        //折半成两个小集合，分别进行递归
        int mid=(start+end)/2;
        mergeSort(array, start, mid);
        mergeSort(array, mid+1, end);
        //把两个有序小集合，归并成一个大集合
        merge(array, start, mid, end);
    }
}

private static void merge(int[] array, int start, int mid, int end){

    //开辟额外大集合，设置指针
    int[] tempArray = new int[end-start+1];
    int p1=start, p2=mid+1, p=0;
    //比较两个小集合的元素，依次放入大集合
    while(p1<=mid && p2<=end){
        if(array[p1]<=array[p2])
            tempArray[p++]=array[p1++];
        else
            tempArray[p++]=array[p2++];
    }
    //左侧小集合还有剩余，依次放入大集合尾部
    while(p1<=mid)
        tempArray[p++]=array[p1++];
    //右侧小集合还有剩余，依次放入大集合尾部
    while(p2<=end)
        tempArray[p++]=array[p2++];

    //把大集合的元素复制回原数组
    for (int i=0; i<tempArray.length; i++)
        array[i+start]=tempArray[i];
}


public static void main(String[] args) {
    int[] arr = new int[] {1,3,2,6,5,7,8,9,10,0};
    mergeSort(arr,0,arr.length-1);
    System.out.println(Arrays.toString(arr));
}
```
