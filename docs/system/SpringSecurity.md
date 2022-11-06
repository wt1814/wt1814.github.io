
<!-- TOC -->

- [1. Spring Security](#1-spring-security)
    - [1.1. 前后端分离](#11-前后端分离)
    - [1.2. SpringSecurity+JWT](#12-springsecurityjwt)
    - [1.3. SpringCloud Gateway + Jwt + Oauth2实现网关的鉴权操作](#13-springcloud-gateway--jwt--oauth2实现网关的鉴权操作)
    - [1.4. 集成CAS（单点登录）](#14-集成cas单点登录)
    - [1.5. 手机登录](#15-手机登录)

<!-- /TOC -->

<!-- 

「Spring Security 系列：」
https://mp.weixin.qq.com/s/EyAMTbKPqNNnEtZACIsMVw


Spring Boot + Vue + CAS 前后端分离实现单点登录方案
https://mp.weixin.qq.com/s/EgyzAQePnCO64ST2W4gtYw
甭管什么登录都给你接入到项目中去
https://mp.weixin.qq.com/s/8uqTXYLZ8FdSCe3HPTTDUw
Spring Security 简单教程以及实现完全前后端分离
https://blog.51cto.com/u_13929722/3425964
Spring Security05--手机验证码登录
https://blog.csdn.net/fengxianaa/article/details/124717610  

 Spring Cloud Gateway + Oauth2 实现统一认证和鉴权！
 https://mp.weixin.qq.com/s/Hlb_BDESR0Gs7dkYy9JDDw

  Spring-Security & JWT 实现 token
 https://mp.weixin.qq.com/s/Dlrxei-hTXh6rVTdb8Ddaw

***系列文章  
https://blog.csdn.net/syc000666/category_9151932.html







https://mp.weixin.qq.com/mp/appmsgalbum?__biz=MzUzMzQ2MDIyMA==&action=getalbum&album_id=1319904585363980289&scene=173&from_msgid=2247484475&from_itemidx=1&count=3&nolastread=1#wechat_redirect


超级全面的权限系统设计方案面世了
https://mp.weixin.qq.com/s/wepzIbJT1O3etWL19vcXjQ
https://mp.weixin.qq.com/s/owlV6_iptEPucb38-KXKZA
如何设计一个完美的权限管理模块
https://mp.weixin.qq.com/s/WTgz07xDIf9FbAfCDyTheQ
超全面的权限系统设计方案！
https://mp.weixin.qq.com/s/-eOmwOaNTey6emtw27zSzw


多种登录方式的简单实现
https://mp.weixin.qq.com/s?__biz=MzUzMzQ2MDIyMA==&mid=2247484218&idx=1&sn=31f00d8945e59616b70eb8b9e215d3ba&chksm=faa2e6a9cdd56fbf698abd8777a0f354869f0fabfa70d6536bcf53333c0cb7bed38f689e17b9&mpshare=1&scene=1&srcid=&sharer_sharetime=1571330289520&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=35424a530b6d23de1afb14a2a38e65946e113b40b2977bfceb09911eea46f6199a3bdda3a0e301f80825d8d035d0e337004d0e640210f46d68ae06b20f398ebffd05adbc2145fd3943da7c28b7751f46&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=gL9a9SDhVpY9sQn3twlUj4hvjz%2Bde7ue53rVpyzN8bEbOVURem0uxgPnIEnlpF17
如何保护用户密码 
https://mp.weixin.qq.com/s?__biz=MzUzMzQ2MDIyMA==&mid=2247484183&idx=1&sn=95ae8348318c53c478c8386eda13e83c&chksm=faa2e684cdd56f927e08a2ef6aa9a913c2162e9713253935a2fcf15427060e63e21d3922c66a&mpshare=1&scene=1&srcid=&sharer_sharetime=1570792186820&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=f394366f6bc7d2c5734ab4a904bedb861c52f232b627366aadc57b21a4baf043f56f8fd8d54e5c2d1bfe0c83813d0836d8df1fa9fad4db990952be7bf19d4602f3931849d9b6905446b97831f4fc9a64&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=Tch8gYSKJ6p%2Bvqtn7v8E13fTGNPkCewU4zfkYDJDEQaeCtG%2FHn2o0Npk5pO2IVks

 【SpringSecurity系列（十二）】查看登录详情 
https://mp.weixin.qq.com/s/VK9qJjBaYYNwKwbUYhi2ig
 Spring Boot + Spring Security 实现自动登录功能
https://mp.weixin.qq.com/s/aSsGNBSWMTsAEXjn9wQnYQ
 Spring Boot + Vue 前后端分离项目，如何踢掉已登录用户？
https://mp.weixin.qq.com/s/nfqFDaLDH8UJVx7mqqgHmQ
 【SpringSecurity系列（十三）】只允许一台设备在线 
https://mp.weixin.qq.com/s/tR7Yf6oifQWXAD7KCvyIeg
 SpringSecurity登录添加验证码 
https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247484875&idx=1&sn=a1e0bf1748a063ca7f640215be944dfc&scene=21#wechat_redirect
Spring Security 实战：自定义异常处理
https://mp.weixin.qq.com/s/Dpk3EOMU398ehw70_Om_Fg
https://mp.weixin.qq.com/s/qDziEQTJaFiN5r9xAOVv5w
Spring Security 添加验证码
https://mp.weixin.qq.com/s?__biz=MzI1NDY0MTkzNQ==&mid=2247487167&idx=2&sn=21ca4e87814086580fbf2fd2c3ea00c7&chksm=e9c35cdfdeb4d5c98730cba97f2e50fc8e3b151b48811f7f11eefa39049e4ff4626122e7c75d&mpshare=1&scene=1&srcid=&sharer_sharetime=1574985330940&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=a7e998ad70d380e284d1195bf4f9b72a06135ff1ed6287c3d5cb6b679dff24918afc3c2eb145ce2fbcd241bbc00e7caad21cdee64670ec5d2ddd3f7b7ed890253b9097d39ec08dc7badf1c687e8ab9c9&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070158&lang=zh_CN&pass_ticket=ezcbUfipjckHP1o%2BULEmYEzarwvvkv726%2B1sRiMT6Y7A8SEmr8B8ahDRSpFEpKBs



实现自定义退出登录 
https://mp.weixin.qq.com/s?__biz=MzUzMzQ2MDIyMA==&mid=2247484265&idx=1&sn=ce96c06d2a1c5a3540486565cd42e67a&chksm=faa2e6facdd56fec1bcbebdb1879d1c7307754dcb5f9bcda710815afcf335d52895c20062c9e&mpshare=1&scene=1&srcid=&sharer_sharetime=1571845658660&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=6f23511bf9e1c01fdf70979249c0d4fa6fa0931b0530c55bae806d41c72e31019fdc1e6aba2a1735c3db2afe5748722ac00942f1459d6be17802a815ca3bc63220562e40810610f970ed7e7272d40936&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070152&lang=zh_CN&pass_ticket=tOysFrIpapzI%2FSWUdTcbYKvSoWjfuug2aUNd5keR9%2BIBFSeAaxr3gVVWD9yTgncJ

-->


# 1. Spring Security  

1. 前后端分离  
2. 使用JWT，是一种伪单点的登录，服务器压力全在用户服务中
3. 集成CAS     
3. 集成手机登录  

<!-- 
总结：
https://blog.csdn.net/fengxianaa?type=blog


https://zhuanlan.zhihu.com/p/479131101


SpringSecurity+JWT使用
https://blog.csdn.net/qq_42218187/article/details/123623413



SpringBoot+SpringSecurity+JWT整合实现单点登录SSO史上最全详解 伪单点（token的验证）
https://blog.csdn.net/weixin_46768610/article/details/112256432

-->

## 1.1. 前后端分离  
<!--
springcloud+gateway+springsecurity+vue前后端登录
https://www.jianshu.com/p/fbabb8684dfd

https://blog.51cto.com/u_13929722/3425964
https://blog.csdn.net/friendlytkyj/article/details/123886947

-->

## 1.2. SpringSecurity+JWT


## 1.3. SpringCloud Gateway + Jwt + Oauth2实现网关的鉴权操作  
<!--
SpringCloud Gateway + Jwt + Oauth2 实现网关的鉴权操作
https://mp.weixin.qq.com/s/8mpf5C1ySd5W_lUpfA2TrQ

-->



## 1.4. 集成CAS（单点登录）  

<!-- 

https://wenku.baidu.com/view/ec929aae8462caaedd3383c4bb4cf7ec4afeb612.html


https://www.jb51.net/article/240040.htm

-->


## 1.5. 手机登录

<!-- 
https://blog.csdn.net/fengxianaa/article/details/124717610
-->
