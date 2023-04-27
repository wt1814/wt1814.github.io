

# 测试  

<!-- 

推荐11个构建和测试API的顶级工具 
https://mp.weixin.qq.com/s?__biz=MzAxODcyNjEzNQ==&mid=2247488231&idx=1&sn=7a0684d1d474c0e241f3f0696a7cd298&chksm=9bd0bf7faca736697d50ffae446317f670d7558d270a2ad51b8c0fbb0cd30052cc54f821dd46&mpshare=1&scene=1&srcid=&sharer_sharetime=1567126010555&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=2a9daec5125aceb3414a0de9c8228ac4f58bdd5ec31971d13800fdbb6cca8814fd4e5aee2ae1363e0bb1c964de01c6eab150a20f974c9221e75158f6b990a1eef3639d34f700ecdabb606339b3efa502&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=U9kFG0IiYTcQO2jwoKWjL2A62Ww%2BHPpY0qMdaz1nyR956DNaf5luD%2BRfOHWjISaQ



6. 性能测试了解吗?说说你知道的性能测试工具?
性能测试指通过自动化的测试工具模拟多种正常、峰值以及异常负载条件来对系统的各项性能指标进行测试。性能测试是总称，通常细分为：
基准测试： 在给系统施加较低压力时，查看系统的运行状况并记录相关数做为基础参考
负载测试：是指对系统不断地增加压力或增加一定压力下的持续时间，直到系统的某项或多项性能指标达到安全临界值，例如某种资源已经达到饱和状态等 。此时继续加压，系统处理能力会下降。
压力测试： 超过安全负载情况下，不断施加压力（增加并发请求），直到系统崩溃或无法处理任何请求，依此获得系统最大压力承受能力。
稳定性测试： 被测试系统在特定硬件、软件、网络环境下，加载一定业务压力（模拟生产环境不同时间点、不均匀请求，呈波浪特性）运行一段较长时间，以此检测系统是否稳定。
后端程序员或者测试平常比较常用的测试工具是 JMeter（官网：https://jmeter.apache.org/）。Apache JMeter 是一款基于Java的压力测试工具(100％纯Java应用程序)，旨在加载测试功能行为和测量性能。它最初被设计用于 Web 应用测试但后来扩展到其他测试领域。

https://mp.weixin.qq.com/s?__biz=MzU4NzU0MDIzOQ==&mid=2247487005&idx=1&sn=9c8837261ce97f69f019fe909e09e789&chksm=fdeb3c7dca9cb56b6c142d52f08eae5143cfc38bf214c2e716f65202f83ffd9325b845da48dc&mpshare=1&scene=1&srcid=&key=521d039d5ed157bfafc85a14fbce3e33de3c21c1bd22ee57300b1b102db7e8166c5802304552130ca8529740921514ea64e1b498d4fbc762d8790db048bc90efcc1b6c17fbcd7638815f3d964711c48d&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060834&lang=zh_CN&pass_ticket=swkcy2j9QV%2B%2Fe6iryvrn25EqfMe45C1VIV4MzLASKKRDEbfsaT0QZlJSJCvsuujA
TPS
多任务和高并发是衡量一台计算机处理器的能力重要指标之一。一般衡量一个服务器性能的高低好坏，使用每秒事务处理数（Transactions Per Second，TPS）这个指标比较能说明问题，它代表着一秒内服务器平均能响应的请求数，而TPS值与程序的并发能力有着非常密切的关系。


---------------------------
压力测试，JMeter

JMeter篇01：JMeter在Mac下的安装
https://www.jianshu.com/p/bce9077d883c

---------------------------------------------------------JMeter----------------------------------------------------------
 
如何模拟超过 5 万的并发用户
https://mp.weixin.qq.com/s?__biz=MzAxMjY5NDU2Ng==&mid=2651854204&idx=1&sn=c937570afe5a83341171bf33ed8a2ac3&chksm=80495035b73ed9235bd883dc049a90f9b925f84f941c8347348fb07a9bd33865aeb86a047fe0&mpshare=1&scene=1&srcid=&sharer_sharetime=1569197398913&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=f394366f6bc7d2c562b63a6f4dbace4556d89a3b980b698f4cff52a4d9fb44c8ebfa7c840b23c03cc6bbd79338d53b4a6aa8c7afe9ac5a1ef669901cfe3cd38681313ad41a9260fe5c2f70a6b152a27a&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=vg289L84kZgMlfi%2BmdUdDpmOfKiTxtTyP4BsVqSgMtQOemVPkyPdcI1sdjZzhhyd
Jmeter环境变量配置详解
https://blog.csdn.net/qq_40646143/article/details/79578270
 JMeter 进行压力测试
https://mp.weixin.qq.com/s/NNHvtSvUlShzFmH0ZNzPtQ

 Jmeter与压测相关概念 
https://mp.weixin.qq.com/s/6sgRtSE-pCAbrKkR045S9g
【Jmeter套餐】超级福利干货，免费领取！！！
https://mp.weixin.qq.com/s?__biz=Mzg5MzI1NTI0Mw==&mid=2247484207&idx=1&sn=32e0872252852f889f9ae2deff709471&chksm=c030e80bf747611d6130f7648119a2343c981cb56014b22af589ffed332c913b8456f8ae8284&mpshare=1&scene=1&srcid=&sharer_sharetime=1570809715928&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=60decda1ebadad2d4c843cf5f31e340fb03da46e2cfb34af52218f4c8220e4c2170d3ec9ab72bc51feb820f14b8e46f8708081b435b41011b1e2612fe549c56a0041dbb619a3872a9707604cb4f65ce1&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=Tch8gYSKJ6p%2Bvqtn7v8E13fTGNPkCewU4zfkYDJDEQaeCtG%2FHn2o0Npk5pO2IVks
 【Jmeter篇】抓包导出Jmeter脚本及查看接口响应时间 
https://mp.weixin.qq.com/s/2GXTlZC1XDcIBemQxiScrg
Jmeter自动化、接口性能测试系列专题
https://mp.weixin.qq.com/s/aZ-rdtUt51UeXEd8TmRfRw
 Jmeter史上最全12种逻辑控制器详解
https://mp.weixin.qq.com/s?__biz=Mzg5MzI1NTI0Mw==&mid=2247484489&idx=1&sn=ddcc5373c7d345590dda5da5b6831c0b&chksm=c030ef6df747667ba4ca6932032e45517dba1588553aef1d7bafc89d0b3dd8216b0265f37320&mpshare=1&scene=1&srcid=&sharer_sharetime=1574532682412&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=0fd7b4fa2fb2f076058877abb36caa34b70f403ba1d4e71d2915dffeaa016287957411f545634d871820c44a834751af6dcd622ce7a22d0d7b1510e2c5b0e69d0be973311f09ffe1ec6ae60adc757cb2&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070152&lang=zh_CN&pass_ticket=n0BGvfruhii32Minmod%2BuiYrluBdezTdMXxcyRJwXKS3fag9M%2BO8mkX285a%2Fzbn8
 如何模拟超过 5 万的并发用户 
https://mp.weixin.qq.com/s/vsuUGvNxnd87rUzsZa1w6Q
 Jmeter之Http请求上传文件/上传图片
https://mp.weixin.qq.com/s?__biz=Mzg5MzI1NTI0Mw==&mid=2247484547&idx=1&sn=2cf430adcdea8380d067164a68a3bf4f&chksm=c030efa7f74766b122c7c82c4fad9df91f626a746ffb09f3cf13122123a46ed8cf3ecbbf2161&mpshare=1&scene=1&srcid=&sharer_sharetime=1575287912013&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=e2a6a5ccea4b8ce45e4a7d46ab1b865a39bd919ba13656b8cedb3de0253808f003d22df8823adc626e65814fbee98a328abca65d05d453ecc249eff95fce06d8362fa18074daae93789d29d923be077c&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070158&lang=zh_CN&pass_ticket=XhI9M9nIhrTxHIclQ8uxyPd2unJlQ4N%2BES6NrkpAI67hgwA8jaM03%2BH7QSdXE73Z

 Jmeter之json条件提取实战（三） 
https://mp.weixin.qq.com/s?__biz=Mzg5MzI1NTI0Mw==&mid=2247484576&idx=1&sn=3ab7e75d60efb03bff9469391d6603f5&chksm=c030ef84f74766926f4795cdd9ca9266653d1fac0575959a1620d02e6852203bb44fc33289e9&mpshare=1&scene=1&srcid=&sharer_sharetime=1577187712008&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=7e51d7974732a32b0a7a0f3674f47586faddfcc811d1d6e2766a3f132c23f90d5bd63f74ce7048237a902d83589d05df981abdb2fda0d6ac7e310f7b4821b3b4828e6c4f3dd2bdbe4805f41ad1955c61&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070158&lang=zh_CN&exportkey=AYFlGEfyI3jTIShENjzE6m0%3D&pass_ticket=fjo0TPQ4TftdXiH325uINjkxmTSYWN5xsY7SY8CPXJ8L70Z%2B9nqwLCPhjc61tfer

 25.后置处理器之JSON提取器
https://mp.weixin.qq.com/s/cryroPShCIwJpEWpTbPgKQ



-->

