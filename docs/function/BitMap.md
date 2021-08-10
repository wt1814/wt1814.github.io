
<!-- TOC -->

- [1. 位图BitMap](#1-位图bitmap)
    - [1.1. BitMap介绍](#11-bitmap介绍)
    - [1.2. BitMap的实现思想](#12-bitmap的实现思想)
    - [1.3. BitMap应用](#13-bitmap应用)
    - [1.4. 问题及应用实例](#14-问题及应用实例)
    - [1.5. BitMap算法的拓展](#15-bitmap算法的拓展)

<!-- /TOC -->

# 1. 位图BitMap  
## 1.1. BitMap介绍  
&emsp; BitMap是什么?  
&emsp; BitMap是通过一个bit位来表示某个元素对应的值或者状态，其中的key就是对应元素本身。8个bit可以组成一个Byte，所以bitmap本身会极大的节省储存空间。  
&emsp; **BitMap算法基本描述：**  
&emsp; BitMap是使用bit位来标记某个元素对应的value，而key即是该元素，因此对于之前位数存储换成bit位存储数据能大大的节省存储空间。  

## 1.2. BitMap的实现思想  
&emsp; 假设要对0-7内的5个元素(4，7，2，5，3)排序(这里假设这些元素没有重复)。那么就可以采用Bit-map的方法来达到排序的目的。要表示8个数，就只需要8个Bit(1Bytes)，首先开辟1Byte的空间，将这些空间的所有Bit位都置为0(如下图：)  
![image](https://gitee.com/wt1814/pic-host/raw/master/algorithm/function-33.png)  
&emsp; 然后遍历这5个元素，首先第一个元素是4，那么就把4对应的位置为1(可以这样操作 p+(i/8)|(0×01<<(i%8)) 当然了这里的操作涉及到Big-ending和Little-ending的情况，这里默认为Big-ending。不过计算机一般是小端存储的，如intel。小端的话就是将倒数第5位置1)，因为是从零开始的，所以要把第五位置为一(如下图)：  
![image](https://gitee.com/wt1814/pic-host/raw/master/algorithm/function-34.png)  
&emsp; 然后再处理第二个元素7，将第八位置为1，接着再处理第三个元素，一直到最后处理完所有的元素，将相应的位置为1，这时候的内存的Bit位的状态如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/algorithm/function-35.png)  
&emsp; 然后现在遍历一遍Bit区域，将该位是一的位的编号输出(2，3，4，5，7)，这样就达到了排序的目的。  

## 1.3. BitMap应用

&emsp; 1)可进行数据的快速查找，判重，删除，一般来说数据范围是int的10倍以下。  
&emsp; 2)去重数据而达到压缩数据  
&emsp; 3)还可以用于爬虫系统中url去重、解决全组合问题。  

## 1.4. 问题及应用实例  
<!-- 
https://www.cnblogs.com/yswyzh/p/9600260.html
https://blog.csdn.net/pipisorry/article/details/62443757
-->

## 1.5. BitMap算法的拓展  
&emsp; Bloom filter可以看做是对bit-map的扩展。更大数据量的有一定误差的用来判断映射是否重复的算法。  
