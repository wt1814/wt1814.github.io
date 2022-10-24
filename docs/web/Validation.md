

# Validation
<!--

@Validated @RequestBody @RequestParam配合使用校验参数
https://www.cnblogs.com/cjyboy/p/11465876.html

https://mp.weixin.qq.com/s/lPvFnpqpRPRT5DyOYYidyw
https://mp.weixin.qq.com/s/Vmbp5jEm_aKfo_O20gZqcg

工作几年了，原来我只用了数据校验的皮毛~ 
https://mp.weixin.qq.com/s/gzSqZIa6tQM6DsDYy3ll9g

@Validated和@Valid区别
https://blog.csdn.net/wangjiangongchn/article/details/86477386

Spring validation 用法说明
https://blog.csdn.net/qq_35206261/article/details/102608477

Spring Validation最佳实践及其实现原理，参数校验没那么简单！ 
https://mp.weixin.qq.com/s/UFT97SbygPMaFd7hYPyojg
这么写参数校验(Validator)就不会被劝退了~ 
https://mp.weixin.qq.com/s/eW8bdeVwgs3AAkMX6CMm4A
Spring Validation最佳实践及其实现原理，参数校验没那么简单！ 
https://mp.weixin.qq.com/s/q-Fh_YGPW7s8lyg6o9DXgA
https://mp.weixin.qq.com/s/bmVvqhzWmK2hA2gurYB4wg

https://mp.weixin.qq.com/mp/appmsgalbum?action=getalbum&__biz=MzI0MTUwOTgyOQ==&scene=1&album_id=1630480921197887488&count=3#wechat_redirect

 参数验证 @Validated 和 @Valid 的区别，Java Web 开发必备。 
 https://mp.weixin.qq.com/s/opK88MLb4he2IoV2Xa4wKA
 
Spring Boot 实现各种参数校验
https://mp.weixin.qq.com/s/VNrs1zGPgx9GsIqXEDZP0Q


http://www.cnblogs.com/softidea/p/9712571.html
https://blog.csdn.net/qq_17213067/article/details/84563486
https://m.jb51.net/article/143472.htm


@Pattern(regexp = "^(1[0123456789])\\d{9}$", message = "手机号码不正确")

这么写参数校验(validator)就不会被劝退了
https://mp.weixin.qq.com/s?__biz=MzI4Njc5NjM1NQ==&mid=2247489603&idx=2&sn=c7a766500122c02694860bd2470204cf&chksm=ebd6276fdca1ae7997a98d8c038ad3462c7ee9b492f28b4de8b7461ca0965fac55d94f96dd37&mpshare=1&scene=1&srcid=&sharer_sharetime=1567913152446&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=6f23511bf9e1c01f5467dd7d700e0369698145588b3a95bf71315c6d5b59af779d0ee3af27e548cc98731b551d83dfa4c83b0c43b0dcee9cbcb654836a8f483193945eee088c57ed3b549635086f6421&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=8bSW14XlKLDnRLX%2BuT5qX2HMWFaWagq6CFzQN36OW8cPhaBdbDgv555kpxmNuS5X

Validation有两种模式
springboot使用hibernate validator校验
https://www.cnblogs.com/mr-yang-localhost/p/7812038.html

帮你少写一大半参数校验代码的小技巧
https://mp.weixin.qq.com/s?__biz=MzI3NzE0NjcwMg==&mid=2650125026&idx=2&sn=14ee669c46d597c6e9e90e3ac343ec3a&chksm=f36bafc3c41c26d5345f74cbfc7290ec50cfd8cb6f4aab9e1347160327ea8e06cd69cb5db0d6&mpshare=1&scene=1&srcid=&sharer_sharetime=1572795722942&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=20f7b87cb3d4d9a801e33b993b4cde4995d5b7b0c811a2a39c30a136b6cee142e244f37f54fba81a5fbffb5cb4c13515e346b6e01b2571f3b264a78443d29f0bcec232f34a42bb7ea2b39caf2f83c78c&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070152&lang=zh_CN&pass_ticket=Lu%2FLBuTxuGaOTLq0CL9dO0ss3p9k%2BNlDhrOCgfGfCUsKTPyuc12lccq3vmkXvxfb

JAVA: 自定义注解来进行数据验证
https://mp.weixin.qq.com/s/Zcosnu5kBEtiJEoZXsjptg

如何实现Java后端数据校验？看这篇就足够！
https://mp.weixin.qq.com/s?__biz=MzAxNjM2MTk0Ng==&mid=2247489360&idx=2&sn=4f92835c6ad72a81cb4d27286693c846&chksm=9bf4a7e5ac832ef37ad4626571514078a4ed3471f316823fbd362d776b748802fd4471b39764&mpshare=1&scene=1&srcid=&sharer_sharetime=1574948430338&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=60d5a5d91d679e4cf27b7c2c8c4abd250b9dc994d0ed1f4a56c0ba8760ceaf6cf618cd98651a8c2df1ff8dbfc271c1a7d47928a64aa2b1f540a257b06e73ae38bc7463f0596682248f91e02ffa424c31&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070158&lang=zh_CN&pass_ticket=ezcbUfipjckHP1o%2BULEmYEzarwvvkv726%2B1sRiMT6Y7A8SEmr8B8ahDRSpFEpKBs
这么写参数校验(Validator)就不会被劝退了~ 
https://mp.weixin.qq.com/s?__biz=MzI4OTA3NDQ0Nw==&mid=2455547219&idx=1&sn=3f4894d8daef1ab55df8a4b9866dd17f&chksm=fb9cb133cceb38253b340e0071173716fb0950d6ca74fc55b060ac376140e6b1e286e559b4dc&mpshare=1&scene=1&srcid=&sharer_sharetime=1576074520453&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=9ad84b8c73b256ab8a3f0ecf43d179da4e5f7379f219602df56781125a41b47f0786ef0b89317fa2af2b220a80e28b96f16d3a4f7dd603cd98014e794bd9ca785e3fd028063b463256e75efe0df24afd&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070158&lang=zh_CN&exportkey=AYGjbAcMRXl5R%2FuHu%2BjkDgA%3D&pass_ticket=w5IvvD6dgNA8axgFhZP%2BU7giQ6CWw0goDHwMwgbzxYRvod1nCghXPMjeQ6Z3bocl
快速入手 Spring Boot 参数校验
https://mp.weixin.qq.com/s/dOeAywqnUQX33eBnBEOVLw
Spring Boot 如何做参数校验？
https://mp.weixin.qq.com/s?__biz=MzI3ODcxMzQzMw==&mid=2247488448&idx=2&sn=766d1d2dfd54f0723a5a1b9efdcbcd7e&chksm=eb5396f6dc241fe00126ebe4039da2bc837bbec8874c330997b66b4160d42f342e2e81294414&scene=21#wechat_redirect
一文带你快速入手 Spring Boot 参数校验
https://mp.weixin.qq.com/s?__biz=MzI4OTkwNDk2Ng==&mid=2247485155&idx=2&sn=73c4adf01b7ab7c4ce99710406beaa99&chksm=ec2946cadb5ecfdc7bd793a97fb89036c2eba8d7b739a36cb6e115326a35c8b33dc1e822c602&mpshare=1&scene=1&srcid=&sharer_sharetime=1576716437854&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=e2a6a5ccea4b8ce46f7db5c02f3cb43a8aa6fdf8b92762669e436571b1b870c99871ce315efa0d61b6e38c3815d5beb6620ef213c59e1a14c67020c7703326190affed8d3ceeb0ffbe3f3ae85f5e54a0&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070158&lang=zh_CN&exportkey=AVCtiK3czKPIuV9xrDwwiio%3D&pass_ticket=%2B4Cbinkzp1%2BfdkSCeUVIgfOzgrNzgshzfrrwTWqzkN8yjJi8AmuC3VBjrc8l%2BeDf
SpringBoot如何优雅的校验参数
https://mp.weixin.qq.com/s?__biz=Mzg3NjIxMjA1Ng==&mid=2247484055&idx=1&sn=56ef7f938e2668bca1ea838ba763d007&chksm=cf34f8a0f84371b65157382c575e5df799d2ed8c3abf392e6ae8869b65029fb2390002fa4fb6&mpshare=1&scene=1&srcid=&sharer_sharetime=1576800912755&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=cb18063c680e4f1037600be3ef413aba74eac00aad08bd05f72f77b31e30c8cd0222cc0216851e6d5464e0e05396aec49371aefa43e0a10a26570070b53c6c9fadc095e84ef8a717930cebb43744b9da&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070158&lang=zh_CN&exportkey=AX75a7pe6CUgpc54iZCX7rU%3D&pass_ticket=k6uZZByTo2fQFzVvUtvPyhmI5ViFLGhQCjyvg5cJhfo3p1d4O0tHI6%2F00fVufJCm

一文带你快速入手 Spring Boot 参数校验
https://mp.weixin.qq.com/s/fTkUbd9UhrbyDQJ4st9QNQ
参数校验 Validator ，建议这样写！（详细版） 
https://mp.weixin.qq.com/s/Y-6t9kzwQN8YPo-6uM6WnQ
SpringBoot 中使用 @Valid 注解 + Exception 全局处理器优雅处理参数验证
https://mp.weixin.qq.com/s/jxEaR70GwInTrz7D37aw0Q
SpringBoot--数据校验（普通校验、分组校验） 
https://mp.weixin.qq.com/s/FH6_Ppc5lcP9jiRjyYPf_A



-->

&emsp; Spring validation和hibernate validation一个重要区别：Spring validation只能用在controller层（Controller层参数是javaBean，如果使用Json传输数据 @valid @requestBody）；Hibernate validation可以用在任意层。  


