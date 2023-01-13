

# 增加nginx配置文件confd-管理多个项目  
<!-- 

https://blog.csdn.net/weixin_43652507/article/details/124123540
-->

1. 建立conf.d文件夹  
2. 在原来的配置文件/usr/local/nginx/conf/nginx.conf的http{}内末尾处加一句：

    include /usr/local/nginx/conf.d/*.conf;
 
3.重启nginx

    ./nginx -s reload

