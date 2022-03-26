

# Kubemetes集群监控  
<!-- 
 10个常用监控Kubernetes性能的Prometheus Operator指标 
 https://mp.weixin.qq.com/s/idQgb0GC2yhaVYwgGj5gcA
-->
<!-- 
应该监控哪些Kubernetes健康指标？ 
https://mp.weixin.qq.com/s/9MRvBGDlEKKbUabhNsVIHQ
-->

&emsp; 在大规模容器集群中，需要对所有Node和全部容器进行性能监控，Kubemetes建议使用一套工具来实现集群性能数据的釆集、存储和展示。  

* Heapster：对集群中各Node上cAdvisor的数据釆集汇聚的系统，通过访问每个Node上kubelet的APL再通过kubelet调用cAdvisor的API来釆集该节点上所有容器的性 能数据。Heapster对性能数据进行聚合，并将结果保存到后端存储系统中。Heaspter支持多种后端存储系统，包括memory(保存在内存中)、InfluxDB、BigQuery＞谷歌云平台提供的Google Cloud Monitoring (https://cloud.google.com/monitoring/ )和Google Cloud Logging(https://cloud.google.com/logging/ )等。Heapster项目的主页为 https://github.com/kubemetes/heapstero 。  
* InfluxDB：是分布式时序数据库(每条记录都带有时间戳属性)，主要用于实时数据釆集、事件跟踪记录、存储时间图表、原始数据等。InfluxDB提供了REST API用于数据的存储和查询。InfluxDB的主页为http://influxdb.com 。
* Grafana：通过Dashboard将InfluxDB中的时序数据展现成图表或曲线等形式，便于运维人员查看集群的运行状态。Grafana的主页为http://grafana.orgo  

&emsp; 基于Heapster+InfluxDB+Grafana的集群监控系统总体架构如下图所示。  
![image](http://www.wt1814.com/static/view/images/devops/k8s/k8s-3.png)  

&emsp; HeapsterInfluxDB和Grafana均以Pod的形式启动和运行。由于Heapster需要与Kubemetes Master进行安全连接，所以需要设置Master的CA证书安全策略。  
