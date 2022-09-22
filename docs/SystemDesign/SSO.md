

# 单点登录  
&emsp; 实现单点登录的关键在于，如何让Session ID（或 Token）在多个域中共享。   


<!-- 

https://www.bilibili.com/read/cv9021757/

单点登录协议有哪些？有何异同？ https://baijiahao.baidu.com/s?id=1681246200125098096&wfr=spider&for=pc
单点登录，百度百科 https://baike.baidu.com/item/%E5%8D%95%E7%82%B9%E7%99%BB%E5%BD%95/4940767?fr=aladdin
单点登陆的三种实现方式  https://www.bilibili.com/read/cv9021757/

-->

&emsp; 登录认证，无论是什么样的场景，还是喜欢使用security框架。  
&emsp; 单点登录，即多个系统共享登录信息。  

1. 什么是登录信息？前后端交互中，登录信息以session、cookie、token的形式体现。  
	1. 以session、cookie的形式的话，可以分为父域、跨域  
2. 登录信息可以在一个系统或多个系统中
3. 怎么达到共享？  
	1. 如果只有一个用户系统，后端将登录信息保存在数据库、redis
	2. SSO认证服务器（未理解透彻）
	3. 如果有多个用户系统  
	