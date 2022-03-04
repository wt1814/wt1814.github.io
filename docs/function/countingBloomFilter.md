


# 计数式布隆过滤器
<!-- 

*** https://www.codenong.com/counting-bloom-filter/

https://blog.csdn.net/zhaoyunxiang721/article/details/41123007

https://cloud.tencent.com/developer/article/1136056

计数式布隆过滤器(counting bloom filter)Redis实现分析
https://blog.csdn.net/vipshop_fin_dev/article/details/102647115


计数布隆过滤
https://www.codenong.com/counting-bloom-filter/
-->

标准Bloom filter对于需要精确检测结果的场景将不再适用，而带计数器的Bloom filter的出现解决了这个问题。Counting Bloom filter实际只是在标准Bloom filter的每一个位上都额外对应得增加了一个计数器，在插入元素时给对应的 k （k 为哈希函数个数）个 Counter 的值分别加 1，删除元素时给对应的 k 个 Counter 的值分别减 1。
在这里插入图片描述
Counting Bloom Filter通过多占用几倍的存储空间的代价，给Bloom Filter增加了删除操作。



