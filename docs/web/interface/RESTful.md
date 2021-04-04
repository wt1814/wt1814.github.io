

# RESTful
<!-- 
理解RESTful：理论与最佳实践 
https://mp.weixin.qq.com/s/HJzvx8Nv-KZUKQ83oqfXuw

REST架构风格详解 
https://mp.weixin.qq.com/s/kzZxaG_C6q4d16D0APMHkw

https://mp.weixin.qq.com/s/63sOcLwMYzuMFRWUe60PjQ

短短的 RESTful API 设计规范
https://mp.weixin.qq.com/s/vV4q2rU4-OYwdnITdQtRsw
无规矩不成方圆，聊一聊 Spring Boot 中 RESTful 接口设计规范
https://mp.weixin.qq.com/s/_XrdDgfUJ_RUO93QurJKLw
 前后端分离开发，RESTful 接口应该这样设计
https://mp.weixin.qq.com/s/s39A6s_oVm1yGXyMzO1V0w
避免自己写的 url 被diss！建议看看这篇RestFul API简明教程！
https://mp.weixin.qq.com/s/b9T7J4s7jUFwF_ZKnZ9vKQ
简洁 RESTful API 设计规范！整个人都清爽了！
https://mp.weixin.qq.com/s/h-1b_eF6r2XgNqQl44yr9w
如何设计好的RESTful API
https://mp.weixin.qq.com/s/GyJOGoan8E9L38jWQMbM2w
RESTful API设计规范
https://mp.weixin.qq.com/s?__biz=MzI4Njc5NjM1NQ==&mid=2247488615&idx=1&sn=e87a4d776cdf4b28682730ebbdfaef7b&chksm=ebd62b4bdca1a25daf61062938ddebccabfdb1c6d4f4225a8a5abe64d7771dbdb40fe13f3fc6&mpshare=1&scene=1&srcid=&key=798968cdb5a0aac4c5dfa467eaab8076834d3d5363b8700dcf93546141e5bc6bb85b4d1e651010c290c34f8d79624907834d32599618dee62ef935707e7a7267eb598871d9f834b17ed0855eea0fa7c4&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060833&lang=zh_CN&pass_ticket=cGOo%2BgWmZeJvJY%2BRqgaAG%2FXS2yQtWMsfmZWVodrCtgdumOqQAY6BJ93Wnzb848Sv
如何给老婆解释什么是Restful
https://mp.weixin.qq.com/s/kETBS8e5TU0OOJ76yPoKlg
 Spring Boot & Restful API 构建实战
https://mp.weixin.qq.com/s?__biz=MzI5ODI5NDkxMw==&mid=2247490989&idx=3&sn=1ab2563afc032bc230f92ceb111dadce&chksm=eca95643dbdedf552238e5cd64890b520fbc95b903f7016d8ab9770a10f8375e218ee59baa49&mpshare=1&scene=1&srcid=&sharer_sharetime=1574036780980&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=2459be73db906624f9fbe7796a7f583179eebcc957cbebc5a050011d6cd78858a9616db48ea4bf23db89ff8ec9d6dccd542fd4ba6ff9b0e1515b9e78774f2c903f02167796c83f7f9e2445b3ec140f16&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070152&lang=zh_CN&pass_ticket=d8cBK5BYC3%2FPH6N%2FO1t1ERoInW7SMj2H5DKJ3gQFPhjPmMb%2F37ZeW6lnFdH8QgEU
 面试官：你连RESTful都不知道我怎么敢要你？
https://mp.weixin.qq.com/s?__biz=MzI3NzE0NjcwMg==&mid=2650125242&idx=3&sn=13f4ac638f87bf854e7ac53b176f04c4&chksm=f36bae9bc41c278dd413cf34bc61dd329594ade3cce7bf854b73c55f238f096f8398e0b5f57b&mpshare=1&scene=1&srcid=&sharer_sharetime=1574531931669&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=0fd7b4fa2fb2f0769c45b03828802dc86609d8c5ec170967ff428bdafc9fd253a8221e8eb61432be7cfcdaf8dd025f604d9ce524914e258edf73579f60948698e74f2385761d23b67ba7eb5fee039530&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070152&lang=zh_CN&pass_ticket=n0BGvfruhii32Minmod%2BuiYrluBdezTdMXxcyRJwXKS3fag9M%2BO8mkX285a%2Fzbn8
实战：SpringBoot & Restful API 构建示例
https://mp.weixin.qq.com/s?__biz=MzU2MTI4MjI0MQ==&mid=2247488585&idx=1&sn=3291a2952ea8073ceb5e2ff2838f47d8&chksm=fc7a79e7cb0df0f147c3a4ba8bbd894d11bdfd47b7c05958d587e0768c701bd445252ac49d1a&mpshare=1&scene=1&srcid=&sharer_sharetime=1576407631263&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=cb18063c680e4f10f331b49d6560e67edb084a882f7e778f69deb0b9d4c41721cafacbcd8a774bf9d13f097ffa0d64be37ece7e2beb233e56dca4e70ea4340d4bd2397e4eda30859f00dd79ab19fb6cc&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070158&lang=zh_CN&exportkey=AQ%2FVaPDDiOAELnbLuhHwkdc%3D&pass_ticket=bfh6Om%2FZtKWXqMXmAUqyFIYCxvpcWFuUU%2FsESm2I4eWOoa6HSoky8oPs67hVAuPg

RESTful 接口这样设计，前后端分离更完美
https://mp.weixin.qq.com/s/JHP6mXWv7TlWPOBEkS_mHg
前后端分离开发，RESTful 接口应该这样设计
https://mp.weixin.qq.com/s/b40DHr0Wfc6t51M4lfi_Cw

Restful 架构 API 接口经典设计误区 
https://mp.weixin.qq.com/s/6EqfaiIwaYjEQS2F3ej-Lw
-->