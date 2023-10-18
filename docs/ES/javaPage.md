

# 分页和聚合  

## 分页
ES分页模糊： https://blog.csdn.net/chaitao100/article/details/125889713
`searchSourceBulider` https://blog.csdn.net/qq_39601612/article/details/104680399
https://developer.aliyun.com/article/1234768

---

分页3种方案  
from/size方案的优点是简单，缺点是在深度分页的场景下系统开销比较大，占用较多内存。  
scroll方案也很高效，但是它基于快照，不能用在实时性高的业务场景，建议用在类似报表导出，或者ES内部的reindex等场景。  
search after基于ES内部排序好的游标，可以实时高效的进行分页查询，但是它只能做下一页这样的查询场景，不能随机的指定页数查询。  

## 聚合 
<!-- 



聚合(aggregations)
https://blog.csdn.net/napoay/article/details/56279658
-->



