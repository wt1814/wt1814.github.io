

# 最近的项目  


1. 分库分表：  

    0. 主键设计：用户id，项目id，量表id，测评结果表（分8个库，主键id后3位使用用户id），采用hash分片  
    1. 根据主键查，
    2. 根据用户id查询，
    3. 根据量表id查？？？？
    4. 全表查，使用ES查。  


2. ES使用：  
    1. 采用定时同步到ES。  
    2. ES分页模糊：https://blog.csdn.net/chaitao100/article/details/125889713   https://developer.aliyun.com/article/1234768     


3. 并发高  
    1. 支持5W人同时在线测。5题一提交，最后一题提交时，查询所有题是否做完。  
    2. 性能：  



