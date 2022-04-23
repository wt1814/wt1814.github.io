
<!-- TOC -->

- [1. 性能指标](#1-性能指标)
    - [1.1. 性能指标简介](#11-性能指标简介)
        - [1.1.1. 吞吐量](#111-吞吐量)
        - [1.1.2. QPS](#112-qps)
            - [1.1.2.1. ★★★多少QPS才算高并发？](#1121-★★★多少qps才算高并发)
        - [1.1.3. TPS](#113-tps)
        - [1.1.5. 并发数](#115-并发数)
            - [1.1.5.1. ★★★常用服务器的并发数](#1151-★★★常用服务器的并发数)
        - [1.1.4. RT](#114-rt)
        - [1.1.6. PV](#116-pv)
        - [1.1.7. UV](#117-uv)
        - [1.1.8. DAU](#118-dau)
        - [1.1.9. MAU](#119-mau)
    - [1.2. 系统吞吐量评估](#12-系统吞吐量评估)
    - [1.3. 高并发概念](#13-高并发概念)
    - [1.4. 性能调优](#14-性能调优)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. 吞吐量  
&emsp; 一般来说， **<font color = "clime">系统吞吐量指的是系统的抗压、负载能力，代表一个系统每秒钟能承受的最大用户访问量。</font>**   
&emsp; **<font color = "clime">`一个系统的吞吐量通常由qps(tps)、并发数来决定，`每个系统对这两个值都有一个相对极限值，只要某一项达到最大值，系统的吞吐量就上不去了。</font>**  
2. QPS  
&emsp; Queries Per Second，每秒查询数，即是每秒能够响应的查询次数，注意这里的查询是指用户发出请求到服务器做出响应成功的次数，简单理解可以认为查询=请求request。`qps=每秒钟request数量`  
3. TPS  
&emsp; Transactions Per Second 的缩写，每秒处理的事务数。一个事务是指一个客户机向服务器发送请求然后服务器做出反应的过程。客户机在发送请求时开始计时，收到服务器响应后结束计时，以此来计算使用的时间和完成的事务个数。  
&emsp; 针对单接口而言，TPS可以认为是等价于QPS的，比如访问一个页面/index.html，是一个TPS，而访问/index.html页面可能请求了3次服务器比如css、js、index接口，产生了3个QPS。  
4. 并发数  
&emsp; 简而言之，系统能同时处理的请求/事务数量。  
&emsp; 计算方式：QPS = 并发数 / RT 或者 并发数= QPS * RT  


# 1. 性能指标  
<!-- 
一文搞清楚QPS、TPS、并发用户数、吞吐量 
https://mp.weixin.qq.com/s/GM7_Q9sSdnt8gCznsE4Xbw

重要**** 怎么理解的并发量和QPS？
https://blog.csdn.net/lihuanlin/article/details/113039461
系统的平均并发用户数和并发数峰值如何估算
https://my.oschina.net/ydsakyclguozi/blog/398236

QPS计算
详解NGINX如何统计网站的PV、UV、独立IP
https://www.jb51.net/article/161419.htm

https://blog.csdn.net/u010325193/article/details/89817576
https://blog.csdn.net/seesun2012/article/details/79501038?utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromMachineLearnPai2%7Edefault-1.control&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EBlogCommendFromMachineLearnPai2%7Edefault-1.control

http://t.zoukankan.com/sunbeidan-p-8477196.html

https://blog.csdn.net/exceptional_derek/article/details/47617397
https://developer.aliyun.com/article/42063?spm=a2c6h.13813017.0.dArticle738638.6c782b8dDRJfLw

https://www.gonet.com.cn/webduirshow-153.html

https://www.cnblogs.com/yiwd/p/3711677.html
-->

## 1.1. 性能指标简介
<!-- 
不了解 QPS、TPS、RT、并发数、吞吐量，劝你简历别写熟悉高并发 
https://mp.weixin.qq.com/s/LFBK_3Mfo644mzXZXxsSTw

* 并发数：指系统同时能处理的请求数量，同样反应了系统的负载能力。这个数值可以分析机器1s内的访问日志数量来得到。  
* QPS：Queries Per Second，每秒查询数。每秒能够响应的查询次数。  
    &emsp; QPS是对一个特定的查询服务器在规定时间内所处理流量多少的衡量标准，在因特网上，作为域名系统服务器的机器的性能经常用每秒查询率来衡量。每秒的响应请求数，也即是最大吞吐能力。  
* TPS：Transactions Per Second 的缩写，每秒处理的事务数目。一个事务是指一个客户机向服务器发送请求然后服务器做出反应的过程。客户机在发送请求时开始计时，收到服务器响应后结束计时，以此来计算使用的时间和完成的事务个数，最终利用这些信息作出的评估分。  
    &emsp; TPS 的过程包括：客户端请求服务端、服务端内部处理、服务端返回客户端。
* 吐吞量：指系统在单位时间内处理请求的数量，TPS、QPS都是吞吐量的常用量化指标。  
    &emsp; 系统吞吐量几个重要参数：QPS(TPS)、并发数、响应时间
    1. QPS(TPS)：每秒钟 request / 事务数量
    2. 并发数：系统同时处理的 request / 事务数
    3. 响应时间：  一般取平均响应时间

    &emsp; 理解了上面三个要素的意义之后，就能推算出它们之间的关系：  
    &emsp; QPS(TPS)= 并发数/平均响应时间   或者：并发数 = QPS * 平均响应时间

* PV：页面浏览量，通常是衡量一个网络新闻频道或网站甚至一条网络新闻的主要指标。用户每一次对网站中的每个页面访问均被记录 1 次。用户对同一页面的多次刷新，访问量累计。  
* UV：访问数(Unique Visitor)指独立访客访问数，统计1天内访问某站点的用户数(以 cookie 为依据)，一台电脑终端为一个访客。  


----------
一个公司有7200名员工，每天上班打卡时间是早上8点到8点30分，每次打卡系统耗时5秒。请问RT、QPS、并发量分别是多少？

RT表示响应时间，问题已经告诉了我们答案：

    RT = 5

QPS表示每秒查询量，假设签到行为平均分布：

    QPS = 7200 / (30 * 60) = 4

并发量表示系统同时处理的请求数量：

    并发量 = QPS x RT = 4 x 5 = 20

根据上述实例引出如下公式：

    并发量 = QPS x RT

如果系统为每一个请求分配一个处理线程，那么并发量可以近似等于线程数。基于上述公式不难看出并发量受QPS和RT影响，这两个指标任意一个上升就会导致并发量上升。

但是这只是理想情况，因为并发量受限于系统能力而不可能持续上升，例如DUBBO线程池就对线程数做了限制，超出最大线程数限制则会执行拒绝策略，而拒绝策略会提示线程池已满，这就是DUBBO线程池打满问题的根源。


-->

### 1.1.1. 吞吐量  
&emsp; 在了解qps、tps、rt、并发数之前，首先应该明确一个系统的吞吐量到底代表什么含义。一般来说， **<font color = "clime">系统吞吐量指的是系统的抗压、负载能力，代表一个系统每秒钟能承受的最大用户访问量。</font>**   
&emsp; **<font color = "clime">`一个系统的吞吐量通常由qps(tps)、并发数来决定，`每个系统对这两个值都有一个相对极限值，只要某一项达到最大值，系统的吞吐量就上不去了。</font>**  

### 1.1.2. QPS
&emsp; Queries Per Second，每秒查询数，即是每秒能够响应的查询次数，注意这里的查询是指用户发出请求到服务器做出响应成功的次数，简单理解可以认为查询=请求request。`qps=每秒钟request数量`  

#### 1.1.2.1. ★★★多少QPS才算高并发？
<!-- 
多少QPS才算高并发？
https://blog.csdn.net/u011277123/article/details/100270009
QPS是什么意思？一般的服务器qps多少？
https://www.fujieace.com/jingyan/qps.html

★★★比如有人说：2C 4G机器单机一般1000QPS；8C 8G机器单机可承受7000QPS。  

具体多少QPS跟业务强相关，只读接口读缓存，将压力给到缓存单机3000+没问题，写请求1000+也正常，也复杂些可能也就几百+QPS。
-->

### 1.1.3. TPS
&emsp; Transactions Per Second 的缩写，每秒处理的事务数。一个事务是指一个客户机向服务器发送请求然后服务器做出反应的过程。客户机在发送请求时开始计时，收到服务器响应后结束计时，以此来计算使用的时间和完成的事务个数。  
&emsp; 针对单接口而言，TPS可以认为是等价于QPS的，比如访问一个页面/index.html，是一个TPS，而访问/index.html页面可能请求了3次服务器比如css、js、index接口，产生了3个QPS。  
 
### 1.1.5. 并发数
&emsp; 简而言之，系统能同时处理的请求/事务数量。  
&emsp; 计算方式：QPS = 并发数 / RT 或者 并发数= QPS * RT  

#### 1.1.5.1. ★★★常用服务器的并发数  
<!-- 
 tomcat支持多少并发 
 https://zhidao.baidu.com/question/1445941399668603020.html
 mysql的并发量是多少？
 https://ask.csdn.net/questions/1091683
-->

----

### 1.1.4. RT
&emsp; Response Time缩写，简单理解为系统从输入到输出的时间间隔，宽泛的来说，它代表从客户端发起请求到服务端接受到请求并响应所有数据的时间差。一般取平均响应时间。  


### 1.1.6. PV
&emsp; PV(Page View)：页面访问量，即页面浏览量或点击量，用户每次刷新即被计算一次。可以统计服务一天的访问日志得到。  

### 1.1.7. UV
&emsp; UV(Unique Visitor)：独立访客，统计1天内访问某站点的用户数。可以统计服务一天的访问日志并根据用户的唯一标识去重得到。  
&emsp; 响应时间(RT)：响应时间是指系统对请求作出响应的时间，一般取平均响应时间。可以通过Nginx、Apache之类的Web Server得到。  

### 1.1.8. DAU
&emsp; DAU(Daily Active User)，日活跃用户数量。常用于反映网站、互联网应用或网络游戏的运营情况。DAU通常统计一日(统计日)之内，登录或使用了某个产品的用户数(去除重复登录的用户)，与UV概念相似。  

### 1.1.9. MAU  
&emsp; MAU(Month Active User)：月活跃用户数量，指网站、app等去重后的月活跃用户数量。  

## 1.2. 系统吞吐量评估  
<!-- 
https://mp.weixin.qq.com/s/Lo3Pt1Z5T1aN6jaIrp_kJg
-->

## 1.3. 高并发概念  
&emsp; 一直在说高并发，多少QPS才算高并发？  
<!-- 
一直再说高并发，多少QPS才算高并发？
https://www.cnblogs.com/capacity-yang/p/13064775.html
-->

## 1.4. 性能调优 
<!-- 
https://mp.weixin.qq.com/s/LFBK_3Mfo644mzXZXxsSTw
-->

