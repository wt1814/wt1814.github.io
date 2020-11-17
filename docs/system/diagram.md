

<!-- 
架构图
https://mp.weixin.qq.com/s/trmQjxfshsnnIJqNY7szqQ


部署多可用区异地灾备
组建多地域混合云架构



* 集群环境规划
    * 操作系统的选型
    * 磁盘规划
    * 磁盘容量规划
    * 内存规划
    * CPU规划
    * 带宽规划


想看看我们万亿级数据的存储架构，是怎么设计的吗？ 
https://mp.weixin.qq.com/s/f19qSsQS6Cn7--iAA1TYNA

-->


<!-- 

https://baike.baidu.com/item/UML%E5%9B%BE/6963758?ms=1&rid=10776198502112736869
https://blog.csdn.net/hit_the_lights/article/details/80558508

https://baike.baidu.com/item/%E7%BB%9F%E4%B8%80%E5%BB%BA%E6%A8%A1%E8%AF%AD%E8%A8%80/3160571?fromtitle=UML&fromid=446747&fr=aladdin&ms=1&rid=10776198502112736869

  几分钟几张图教你学会如何使用UML 
https://mp.weixin.qq.com/s?__biz=MzAwMjk5Mjk3Mw==&mid=2247484207&idx=1&sn=9bb4fc741975e2f21608d23dc43063eb&chksm=9ac0bd0dadb7341b3c63ba5b0550ae69832f7ce2d9b5b42b58d6b7078229d48b8ad1a973deaf&scene=21#wechat_redirect

 手把手教你画架构图，看一次就会了！
https://mp.weixin.qq.com/s?__biz=MzA3MjMwMzg2Nw==&mid=2247485328&idx=1&sn=95d71ffb1e4142dbd770c1c2ec85be4e&chksm=9f211204a8569b12435c14871d360b6ae6623583d0b9c66623b219ab31a5c9ddeb3df3af5217&mpshare=1&scene=1&srcid=&sharer_sharetime=1574232815465&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=0fd7b4fa2fb2f076851b3279b741bcd72b9ed4740c8fb887a26e2e56a187c0a436b0a67f13497a5cf16abc2845e1284a64908962faf3fc1574d137fd70c4bf53a02e09bc32d709dda3af98b703fa18b0&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070152&lang=zh_CN&pass_ticket=WfLUVSCdR759nVfSaPrEbDJ5pQBJLzYUdmc8DOJ9hHJHHUoxvw5vHgy0hzLZZoMW

-->

<!--
UML 规范
https://mp.weixin.qq.com/s?__biz=MzU2MTI4MjI0MQ==&mid=2247486248&idx=1&sn=e4cf84c9dd18c3d2ab893c5877c57f2e&chksm=fc7a6686cb0def901f34f70913aa4890fd567eb96b193aa6003019323a12d43060a829c6132e&mpshare=1&scene=1&srcid=&key=f7a27f56e40270208add09b9a3c65d80c2ac1b94ee4aade7f7d73e64f3322aea969ac45cc13d8fafe875aee2baa819006bbb1bf8ce0cab20c053cf6dc3b46d2c7daa2e83f0ca373f23a64f410ff5ab07&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060739&lang=zh_CN&pass_ticket=BNKYmI1tNowiVhznjogMkwIplgDfHGv1yoB2tKc%2FB0MtyhDIAJGhc5EsUwxxAE3o
从IDEA角度来看懂UML图 
 https://mp.weixin.qq.com/s?__biz=MzIzMzgxOTQ5NA==&mid=2247488511&idx=1&sn=6216f655bbca36017952b510d89b0703&chksm=e8fe8ff6df8906e05d56895cebd41b82570d270cb459ea0b3795db0a445a7f6e505c92ee3787&mpshare=1&scene=1&srcid=&sharer_sharetime=1568161413992&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=f394366f6bc7d2c5e0eee1810e0edda7a316daf5d922d1d76c3d887cc3a863c30d481e3c2c8e5cb78f79aab98734bc65a2f4c81b232fc7606fd70203e96afc0b569233b224732d2fd28ebbd27eee620f&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=ox7H9ybM%2FIl4L8AHb3uC8S26kM3ApuWhuUEwxVIgoRqXEvhiSBRtYtEhDCBYnTcF

-->