---
title: 非比较排序  
date: 2020-06-04 00:00:00
tags:
    - 算法
---

<!-- TOC -->

- [1. 计数排序](#1-计数排序)
    - [1.1. 算法描述](#11-算法描述)
    - [1.2. 编码](#12-编码)
- [2. 桶排序](#2-桶排序)
    - [2.1. 算法描述](#21-算法描述)
    - [2.2. 编码](#22-编码)
- [3. 基数排序](#3-基数排序)
    - [3.1. 算法描述](#31-算法描述)
    - [3.2. 编码](#32-编码)

<!-- /TOC -->

# 1. 计数排序  
&emsp; 计数排序适用于一定范围的整数排序。

## 1.1. 算法描述  

## 1.2. 编码  


# 2. 桶排序  
&emsp; 当数列取值范围过大，或者不是整数时，不能适用计数排序。但是可以使用桶排序来解决问题。  
&emsp; 桶排序同样是一种线性时间的排序算法。类似于计数排序所创建的统计数组，桶排序需要创建若干个桶来协助排序。  
&emsp; 每一个桶（bucket）代表一个区间范围，里面可以承载一个或多个元素。桶排序的第一步，就是创建这些桶，确定每一个桶的区间范围。具体建立多少个桶，如何确定桶的区间范围，有很多不同的方式。  
&emsp; 示例：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/function/function-7.png)  

## 2.1. 算法描述
1. 找出待排序数组中的最大值max和最小值min；
2. 使用动态数组ArrayList 作为桶，桶里放的元素也用 ArrayList 存储。桶的数量为 (max-min) / arr.length + 1；
3. 遍历数组 arr，计算每个元素 arr[i] 放的桶；
4. 每个桶各自排序；
5. 遍历桶数组，把排序好的元素放进输出数组。

## 2.2. 编码

```
public static double[] bucketSort(double[] array){
    //1.得到数列的最大值和最小值，并算出差值d
    double max = array[0];
    double min = array[0];
    for(int i=1; i<array.length; i++) {
        if(array[i] > max) {
            max = array[i];
        }
        if(array[i] < min) {
            min = array[i];
        }
    }

    double d = max - min;
    //2.初始化桶
    int bucketNum = array.length;
    ArrayList<LinkedList<Double>> bucketList = new ArrayList<LinkedList<Double>>(bucketNum);
    for(int i = 0; i < bucketNum; i++){
        bucketList.add(new LinkedList<Double>());
    }

    //3.遍历原始数组，将每个元素放入桶中
    for(int i = 0; i < array.length; i++){
        int num = (int)((array[i] - min)  * (bucketNum-1) / d);
        bucketList.get(num).add(array[i]);
    }

    //4.对每个通内部进行排序
    for(int i = 0; i < bucketList.size(); i++){
        //JDK底层采用了归并排序或归并的优化版本
        Collections.sort(bucketList.get(i));
    }

    //5.输出全部元素
    double[] sortedArray = new double[array.length];
    int index = 0;
    for(LinkedList<Double> list : bucketList){
        for(double element : list){
            sortedArray[index] = element;
            index++;
        }
    }
    return sortedArray;
}

public static void main(String[] args) {
    double[] array = new double[] {4.12,6.421,0.0023,3.0,2.123,8.122,4.12, 10.09};
    double[] sortedArray = bucketSort(array);
    System.out.println(Arrays.toString(sortedArray));
}
```  

# 3. 基数排序  
&emsp; 基数排序不但能处理整数排序，也能对字母、汉字进行排序。它把排序工作拆分成多个阶段，每一个阶段只根据一个字符进行计数排序，一共排序k轮（k是元素长度）。  
&emsp; 简单示例：数组中有若干个字符串元素，每个字符串元素都是由三个英文字母组成：  

    bda，cfd，qwe，yui，abc，rrr，uee
  
&emsp; 由于每个字符串的长度是3个字符，可以把排序工作拆分成3轮：  
&emsp; 第一轮：按照最低位字符排序。排序过程使用计数排序，把字母的ascii码对应到数组下标，第一轮排序结果如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/function/function-8.png)  
&emsp; 第二轮：在第一轮排序结果的基础上，按照第二位字符排序。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/function/function-9.png)  
&emsp; 需要注意的是，这里使用的计数排序必须是稳定排序，这样才能保证第一轮排出的先后顺序在第二轮还能继续保持。  
比如在第一轮排序后，元素uue在元素yui之前。那么第二轮排序时，两者的第二位字符虽然同样是u，但先后顺序万万不能变，否则第一轮排序就白做了。  

&emsp; 第三轮：在第二轮排序结果的基础上，按照最高位字符排序。   
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/function/function-10.png)  
&emsp; 如此一来，这些字符串的顺序就排好了。  
&emsp; 像这样把字符串元素按位拆分，每一位进行一次计数排序的算法，就是基数排序（Radix Sort）。  

&emsp; 基数排序既可以从高位优先进行排序（Most Significant Digit first，简称MSD），也可以从低位优先进行排序（Least Significant Digit first，简称LSD）。  

&emsp; 如果排序对字符串长度不规则，则以最长的字符串为准，其他长度不足的字符串，在末尾补0即可。  

## 3.1. 算法描述  
1. 取得数组中的最大数，并取得位数；
2. arr为原始数组，从最低位开始取每个位组成radix数组；
3. 对radix进行计数排序（利用计数排序适用于小范围数的特点）。

## 3.2. 编码  

```
//ascii码的取值范围
public static final int ASCII_RANGE = 128;

public static String[]  radixSort(String[] array,int maxLength) {

    //排序结果数组，用于存储每一次按位排序的临时结果
    String[] sortedArray = new String[array.length];

    //从个位开始比较，一直比较到最高位
    for(int k=maxLength-1;k>=0;k--){
        //计数排序的过程，分成三步：
        //1.创建辅助排序的统计数组，并把待排序的字符对号入座，
        //这里为了代码简洁，直接使用ascii码范围作为数组长度
        int[] count = new int[ASCII_RANGE];

        for(int i=0;i<array.length;i++) {

            int index = getCharIndex(array[i],k);
            count[index]++;
        }

        //2.统计数组做变形，后面的元素等于前面的元素之和
        for(int i=1;i<count.length;i++) {
            count[i] = count[i] + count[i-1];
        }

        //3.倒序遍历原始数列，从统计数组找到正确位置，输出到结果数组
        for(int i=array.length-1;i>=0;i--) {
            int index = getCharIndex(array[i],k);
            int sortedIndex = count[index]-1;
            sortedArray[sortedIndex] = array[i];
            count[index]--;
        }
        //下一轮排序需要以上一轮的排序结果为基础，因此把结果复制给array
        array = sortedArray.clone();
    }
    return array;
}

//获取字符串第k位字符所对应的ascii码序号
private static int getCharIndex(String str, int k){

    //如果字符串长度小于k，直接返回0，相当于给不存在的位置补0
    if(str.length() < k+1){
        return 0;
    }
    return str.charAt(k);

}

public static void main(String[] args) {

    String[] array = {"qd","abc", "qwe","hhh","a","cws", "ope"};
    System.out.println(Arrays.toString(radixSort(array, 3)));

}
```

