

# 构建devops平台  
&emsp; jenkins+kubernetes+harbor+gitlab构建企业级devops平台   

<!-- 


**** 基于 Docker 实现一个 DevOps 开发环境
https://mp.weixin.qq.com/s/aTpnY4AdUHFj8R8zcaVCMQ


牛批！全自动发布jar到Maven中央仓库 
https://mp.weixin.qq.com/s/nvAwhdDEhXyyeaKd3N-gGQ
-->

<!--
https://blog.csdn.net/zbbkeepgoing/category_7969146.html
使用Jenkins Pipeline插件和Docker打造容器化构建环境
https://blog.csdn.net/sisiy2015/article/details/51024608
jenkins+kubernetes+harbor+gitlab构建企业级devops平台 
https://mp.weixin.qq.com/s?__biz=MzU0NjEwMTg4Mg==&mid=2247483962&idx=1&sn=595eac335f7fb523bbb60cbbede109ca&chksm=fb638d2fcc14043990967471ba8a2aaf389ab483594fd81395b5afda1c23a9eff4371df6bca4&scene=21#wechat_redirect
基于 Jenkins、Gitlab、Harbor、Helm 和 K8S 的 CI/CD(一)
https://zhuanlan.zhihu.com/p/62284485
手把手教你使用 Jenkins 配合 Github hook 持续集成 
https://mp.weixin.qq.com/s/vqCpsTTm1p7-KvGp-9y2Aw
GitLab持续集成 
https://mp.weixin.qq.com/s/lS6BZ9PvyReTleJ_CuQrsA
-->


一个 DevOps 开发环境需要满足以下 8 点需求。

1、环境一致性：在本地开发出来的功能，无论在什么环境下部署都应该能得到一致的结果。  
2、代码自动检查：为了尽早发现问题，每一次代码提交后，系统都应该自动对代码进行检查，及早发现潜在的问题，并运行自动化测试。  
3、持续集成：每次代码提交后系统可以自动进行代码的编译和打包，无需运维人员手动进行。  
4、持续部署：代码集成完毕后，系统可以自动将运行环境中的旧版本应用更新成新版本的应用并且整个过程中不会让系统不可用。  
5、持续反馈：在代码自动检查、持续集成、持续部署的过程中，一旦出现问题，要能及时将问题反馈给开发人员以及运维人员。开发和运维人员收到反馈后对问题及时进行修复。  
6、快速回滚：当发现本次部署的版本出现问题时，系统应能快速回退到上一个可用版本。  
7、弹性伸缩：当某个服务访问量增大时，系统应可以对这个服务快速进行扩容，保证用户的访问。当访问量回归正常时，系统能将扩容的资源释放回去，实现根据访问情况对系统进行弹性伸缩。  
8、可视化运维：提供可视化的页面，可实时监控应用、集群、硬件的各种状态。  
为了满足以上 8 点要求，设计出的 DevOps 开发环境如下图所示。  

![image](http://www.wt1814.com/static/view/images/devops/devops/devops-15.png)  


整个环境主要由 6 部分组成。   

1、代码仓库 Gitlab 。  
2、容器技术 Docker 。  
3、持续集成工具 Jenkins 。  
4、代码质量检测平台 SonarQube 。  
5、镜像仓库 Harbor 。  
6、容器集群管理系统 Kubernetes 。  

整个环境的运行流程主要分为以下 6 步。  

1、开发人员在本地开发并验证好功能后，将代码提交到代码仓库。  
2、通过事先配置好的 Webhook 通知方式，当开发人员提交完代码后，部署在云端的持续集成工具 Jenkins 会实时感知，并从代码仓库中获取最新的代码。  
3、获取到最新代码后，Jenkins 会启动测试平台 SonarQube 对最新的代码进行代码检查以及执行单元测试，执行完成后在 SonarQube 平台上生成测试报告。如果测试没通过，则以邮件的方式通知研发人员进行修改，终止整个流程。若测试通过，将结果反馈给 Jenkins 并进行下一步。  
4、代码检查以及单元测试通过后， Jenkins 会将代码发送到持续集成服务器中，在服务器上对代码进行编译、构建然后打包成能在容器环境上运行的镜像文件。如果中间有步骤出现问题，则通过邮件的方式通知开发人员和运维人员进行处理，并终止整个流程。  
5、将镜像文件上传到私有镜像仓库 Harbor 中保存。  
6、镜像上传完成后， Jenkins 会启动持续交付服务器，对云环境中运行的应用进行版本更新，整个更新过程会确保服务的访问不中断。持续交付服务器会将最新的镜像文件拉取到 Kubernetes 集群中，并采用逐步替换容器的方式进行对应用进行更新，在服务不中断的前提下完成更新。  

通过上述几步，我们就可以简单实现一个 DevOps 开发环境，实现代码从提交到最终部署的全流程自动化。  

