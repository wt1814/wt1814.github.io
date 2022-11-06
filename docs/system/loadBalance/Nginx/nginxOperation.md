<!-- TOC -->

- [1. Nginx运维](#1-nginx运维)
    - [1.1. Nginx目录结构](#11-nginx目录结构)
    - [1.2. Nginx部署](#12-nginx部署)
        - [1.2.1. Nginx+Keepalived](#121-nginxkeepalived)
    - [1.3. 采集Nginx的日志](#13-采集nginx的日志)
    - [1.4. Nginx监控](#14-nginx监控)
    - [1.5. Nginx常见的优化配置](#15-nginx常见的优化配置)
    - [Nginx访问控制](#nginx访问控制)

<!-- /TOC -->

# 1. Nginx运维  

<!--

***书籍   《Nginx应用与运维》

Nginx系列：后端服务应用健康检测 
https://mp.weixin.qq.com/s/5NIu2fGkr9ZYqBks1UI6Lg
-->

## 1.1. Nginx目录结构  

```
[root@localhost ~]# tree /usr/local/nginx
/usr/local/nginx
├── client_body_temp
├── conf                             # Nginx所有配置文件的目录
│   ├── fastcgi.conf                 # fastcgi相关参数的配置文件
│   ├── fastcgi.conf.default         # fastcgi.conf的原始备份文件
│   ├── fastcgi_params               # fastcgi的参数文件
│   ├── fastcgi_params.default       
│   ├── koi-utf
│   ├── koi-win
│   ├── mime.types                   # 媒体类型
│   ├── mime.types.default
│   ├── nginx.conf                   # Nginx主配置文件
│   ├── nginx.conf.default
│   ├── scgi_params                  # scgi相关参数文件
│   ├── scgi_params.default  
│   ├── uwsgi_params                 # uwsgi相关参数文件
│   ├── uwsgi_params.default
│   └── win-utf
├── fastcgi_temp                     # fastcgi临时数据目录
├── html                             # Nginx默认站点目录
│   ├── 50x.html                     # 错误页面优雅替代显示文件，例如当出现502错误时会调用此页面
│   └── index.html                   # 默认的首页文件
├── logs                             # Nginx日志目录
│   ├── access.log                   # 访问日志文件
│   ├── error.log                    # 错误日志文件
│   └── nginx.pid                    # pid文件，Nginx进程启动后，会把所有进程的ID号写到此文件
├── proxy_temp                       # 临时目录
├── sbin                             # Nginx命令目录
│   └── nginx                        # Nginx的启动命令
├── scgi_temp                        # 临时目录
└── uwsgi_temp                       # 临时目录
```

## 1.2. Nginx部署  
### 1.2.1. Nginx+Keepalived  
<!-- 
【Nginx】如何搭建Nginx+Keepalived双机热备环境？这是最全的一篇了！！ 
 https://mp.weixin.qq.com/s/mnB3Xw1ju6l5-YiS6sF82g
-->


## 1.3. 采集Nginx的日志  

<!-- 
如何采集Nginx的日志？
https://mp.weixin.qq.com/s/x-IqAk7zTAFf7tpXRE0O5g
推荐一款日志切割神器！我常用~ 
https://mp.weixin.qq.com/s/aFbKCQBnrK3GaQsXjIFAOw

对Nginx的日志进行切割和分析处理。

Nginx日志配置
https://mp.weixin.qq.com/s?__biz=MzI4Njc5NjM1NQ==&mid=2247490106&idx=1&sn=04734125058974fd0c42620082530b03&chksm=ebd62516dca1ac00de3cdcff111250136840464ddbb44e6f22c1507a2a47667e913941b866ac&mpshare=1&scene=1&srcid=&sharer_sharetime=1571562653368&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=a858a32477a98f3f66cac0df36db221645e83c591491fc01847069565bb00a139ada5fffeebf19647683b4964c3299612adee95fb614cfe944ea13aef4153cc2c3034f847cf6c9e91998c4fe72fa4e5c&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070152&lang=zh_CN&pass_ticket=x8l2zA%2Bj3bHvxuk7XMyf8AEtxTkuRNF39Km3scXr%2FsZZeRJRAX9XYczqHgHWdCt3

利用ELK分析Nginx日志生产实战
https://mp.weixin.qq.com/s?__biz=MzI0MDQ4MTM5NQ==&mid=2247488853&idx=1&sn=82a3d04d81d254bbb445fd10ad215dd9&chksm=e91b7049de6cf95f15df8264437786eb992519c45770c841bb902c01992192330424b94daf2a&scene=21#wechat_redirect

-->

## 1.4. Nginx监控  
<!-- 

又一款Nginx 管理可视化神器！通过界面完成配置监控 
https://mp.weixin.qq.com/s/sPntuPTCNTs6bXsJkO8g5g

Ngxtop
https://mp.weixin.qq.com/s?__biz=MzI0MDQ4MTM5NQ==&mid=2247490118&idx=2&sn=31bd9afa62b27afcacbca1be0675dc7a&chksm=e91b7f5ade6cf64c8a05ab3f7a2379a2ae101097a1a363690aee261c4ab3d82243fd36a17eb2&mpshare=1&scene=1&srcid=&sharer_sharetime=1566359762836&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=36a99a852770fa03d8d4a74a093c3a2fa124515be7d9e0738edbb66967c41a8f5553b5ca3e3332d7c9c6cdbd10e4849e3a2b649ffc2c0b923b0c0b63e8ab9d4d4743e74a5184ffb87eaf2c3d0f865d26&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=OzH8r4s9Va6DEZaWxmB%2BZFeLRG%2Fr0XoTKeHpvWhKxz6B1yLq0M6Eiym92%2FXw0mmm

-->


## 1.5. Nginx常见的优化配置
<!-- 

nginx可以对数据进行压缩，对一些图片、html、css、js等文件进行缓存。

百万并发下 Nginx 的优化之道
https://zhuanlan.zhihu.com/p/49415781

Nginx优化配置详解
https://mp.weixin.qq.com/s?__biz=MzI0MDQ4MTM5NQ==&mid=2247484627&idx=1&sn=8626a39ab0a9dc7d2a7bf26db7fd1ce8&chksm=e91b61cfde6ce8d924fd82809f5faeb334d50d371a4912dd0e1619c4d297937623e4cc09159e&scene=21#wechat_redirect
提高Nginx服务器硬度的12个技巧 
https://mp.weixin.qq.com/s?__biz=MzI0MDQ4MTM5NQ==&mid=2247486476&idx=1&sn=44e306911ae9291fdbf7732f25884c18&chksm=e91b6910de6ce006feef2cfbc1764601203363a5bccbe504621a66460501081ec9e3b47baa53&scene=21#wechat_redirect
-->
  
* 调整 worker_processes：指 Nginx 要生成的 Worker 数量，最佳实践是每个 CPU 运行 1 个工作进程。  
* 最大化 worker_connections。  
* 启用 Gzip 压缩：压缩文件大小，减少了客户端 HTTP 的传输带宽，因此提高了页面加载速度。  
* 为静态文件启用缓存。  
* 禁用 access_logs：访问日志记录，它记录每个 Nginx 请求，因此消耗了大量 CPU 资源，从而降低了 Nginx 性能。  

## Nginx访问控制
参考《nginx安全配置指南技术手册》。  
