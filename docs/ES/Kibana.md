

# 1. Kibana用户手册  
&emsp; Kibana文档：https://www.elastic.co/guide/cn/kibana/current/index.html  

## 1.1. 日志检索  
* 登录后界面  
&emsp; 如图所示，检索日志需点击红框内的菜单  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-52.png)  
* 日志搜索检索  
&emsp; 如图所示：   
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-53.png)  

* 日志索引选定  
&emsp; 点击下拉选项，可弹出全部索引列表  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-54.png)  

* 查询时间范围选定   
&emsp; 点击箭头处可打开日期选取界面，下方红框为设置自动刷新获取日志的时间间隔  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-55.png)  

* 日志内容搜索  
&emsp; 在箭头指向处可以搜索指定文本，搜索后在日志文本区匹配文本会高亮显示  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-56.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-57.png)  
&emsp; 如果搜索字段为已切割字段，则可以键入切割字段名称，搜索框下方会弹出提示框，类似于Key:Value的使用：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-58.png)  

* 日志已切割字段的使用  
&emsp; 鼠标移动到字段后方时，会出现添加按钮，点击后，右侧日志文本区会发生变化  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-59.png)  
&emsp; 日志会以选定字段展示，如果想恢复原状则可以在选定字段区域点击移除按钮  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-60.png)  
&emsp; 也可以将鼠标移动到字段名称上方，会出现是否继续选取该字段以及调整字段顺序的选项  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-61.png)  
* 日志正文查看  
&emsp; 默认切割字段内容和日志原文一同显示在页面中，如果需要查看详情可点击红框内箭头  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-62.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-63.png)  
* 仪表板的使用  
&emsp; 选取菜单栏中仪表板  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-64.png)  
&emsp; 会出现已配置好的仪表板，点击即可进入该仪表板界面，使用方式与日志检索界面相同  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-65.png)  
