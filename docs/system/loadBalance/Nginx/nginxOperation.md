<!-- TOC -->

- [1. Nginx运维](#1-nginx运维)
    - [1.1. Nginx目录结构](#11-nginx目录结构)
    - [1.2. Nginx部署](#12-nginx部署)
        - [1.2.1. Nginx+Keepalived](#121-nginxkeepalived)
    - [1.3. 采集Nginx的日志](#13-采集nginx的日志)
    - [1.4. Nginx监控](#14-nginx监控)

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
-->

## 1.4. Nginx监控  
<!-- 

又一款Nginx 管理可视化神器！通过界面完成配置监控 
https://mp.weixin.qq.com/s/sPntuPTCNTs6bXsJkO8g5g
-->
