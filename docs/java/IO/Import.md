
<!-- TOC -->

- [1. 导入导出](#1-导入导出)
    - [1.1. EasyExcel](#11-easyexcel)
    - [1.2. 合并](#12-合并)

<!-- /TOC -->


# 1. 导入导出  

<!-- 
 100000 行级别数据的 Excel 导入优化之路 
 https://mp.weixin.qq.com/s/Y1feFfn8VeZsxXw65NYoWQ
-->

&emsp; EasyExcel + 缓存数据库查询操作 + 批量插入：  

* 逐行查询数据库校验的时间成本主要在来回的网络IO中，优化方法也很简单。将参加校验的数据全部缓存到 HashMap 中。直接到 HashMap 去命中。    


## 1.1. EasyExcel  
<!-- 
https://easyexcel.opensource.alibaba.com/docs/current/
-->
1. 读Excel   
2. 写Excel  
3. 填充Excel  


## 1.2. 合并  
<!-- 

https://blog.csdn.net/tang_sy/article/details/124018099
https://news.68idc.cn/makewebs/qitaasks/20220611812229.html
http://www.bubuko.com/infodetail-3613922.html?cf_chl_jschl_tk=3e4633a1a7393b9add9b809609dc1b065902757a-1620785914-0-AetcD7IGJN3edlQCOR_qMh22GK4KhXnfMUkxGx4PvHgc331sqg7iiwI03_oHwrXVwL3YSjhyq0TYiPN0ojJ8RI6fgO9oFR350ZjMuxrcebr59hS_T8nCGwhLGvRf2Mp7alobIIclge8Yyj6POQO_XE2yW6G6t6c7nOTmhSDkchrbn0Ok6BjmtSh4DPndJBpshjmF6xGiPtw6iOC_IUgTM2YQtEvMyVwckrUkjSdVWa4x9WlqzjFSxHAbSHBbuxuSDEIErtZABHy0uOL6FUeaWe5BFgDRI-0m9b7yzp0TJXi8yMuXj235qltBHra_CawI66qSTmP22wb8Var92FiOuonfonrAogun1dNakrbcHxm5akGSzAVnXJP7295lz5NtHjr3PzGTOGS_q8VRuERAXJ8
https://blog.csdn.net/qq_34789780/article/details/108620767
https://blog.csdn.net/u012143730/article/details/111035167
-->