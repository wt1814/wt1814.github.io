
<!-- TOC -->

- [1. DevOps](#1-devops)
    - [1.1. 定义](#11-定义)
    - [1.2. 流程](#12-流程)
    - [1.3. 工具集](#13-工具集)
    - [1.4. jenkins+kubernetes+harbor+gitlab构建企业级devops平台](#14-jenkinskubernetesharborgitlab构建企业级devops平台)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. DevOps框架  
&emsp; 以下是一个DevOps框架。这个框架只指出那些被认可的概念和它们在某种程度上的关系。
![image](http://182.92.69.8:8081/img/devops/devops/devops-8.png)  
&emsp; **<font color = "clime">敏捷开发指的是在 DevOps 中采用敏捷思想进行软件开发，敏捷宣言无疑是很重要的一项。有多种敏捷方法可以采用，比如Scrum、看板和极限编程。</font>**  
&emsp; **<font color = "clime">持续集成提供了让多个程序员可以同时运行应用程序的最佳实践，可以频繁合并源代码、验证代码(静态测试用例)、编译和测试代码(动态测试用例)。</font>**  
&emsp; **<font color = "clime">持续交忖关注从开发、测试、验收到生产环境的高频生产能力。基于高度的自动化，极端的发布上线时间可以达到分钟级。</font>**  
2. DevOps流程  
&emsp; 下图显示了一个DevOps流程。它不是DevOps流程的正式定义，而是表述了在大多数组织机构中，为了实现一个服务而会被循环执行的合乎逻辑顺序的一系列阶段。  
&emsp; 深色部分表示开发流程，浅色部分表示运维流程。这两个流程构成了DevOps方法的核心。  
![image](http://182.92.69.8:8081/img/devops/devops/devops-1.png)  
3. 工具集  
&emsp; **<font color = "clime">DevOps一般包括版本控制&协作开发工具、自动化构建和测试工具、持续集成&交付工具、部署工具、维护工具、监控，警告&分析工具等。</font>**  
![image](http://182.92.69.8:8081/img/devops/devops/devops-3.png)  


# 1. DevOps  
<!--


***一文弄懂什么是DevOps
https://mp.weixin.qq.com/s/-AFFoCs8hidM9vwzZ4iYjg

云原生
https://mp.weixin.qq.com/s/fVGTtXlurMfJV1gp93jUIQ

 顶级项目管理工具 Top 10 
 https://mp.weixin.qq.com/s?__biz=MzAxODcyNjEzNQ==&mid=2247488193&idx=1&sn=753ee08ec54dc63db9886af6698b5a87&chksm=9bd0bf59aca7364fc30784612ab94c2995ace78e12717813fdfc04225ce56dd6db1bfcf13f42&mpshare=1&scene=1&srcid=&sharer_sharetime=1566780211793&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=36a99a852770fa03a31695d06f4af51c338b407cd3dedd87bfff6a0a2c14027ee3e7893a0739e251255096fa4deb5661e7000578f7e240bfacb2ccd98e38925de8000ccf918785197351e84100ebea70&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=viJcMyKnG8E9pcB41SnLElL1MIu%2BCxzadbzYs10BZtVx0WMi%2BRAyUj98B%2BRYxGi5


-->
&emsp; **DevOps是CI、CD思想的延伸，CI、CD是DevOps的基础核心。**如果没有CI、CD自动化的工具和流程，谈DevOps是没有意义的。  

## 1.1. 定义  
![image](http://182.92.69.8:8081/img/devops/devops/devops-4.png)   
&emsp; **DevOps：Development和Operations的组合。可以把DevOps看作开发(软件工程)、技术运营和质量保障(QA)三者的交集。**  
&emsp; DevOps并没有被定义成一组最佳实践和流程。尽管如此，一些常用概念的内在关联性仍可被识别出来，如下图所示：  
![image](http://182.92.69.8:8081/img/devops/devops/devops-8.png)  
&emsp; 这是其中的一个DevOps框架。这个框架只指出那些被认可的概念和它们在某种程度上的关系。以下是对每个概念的简要说明。  

* DevOps持续测试  
&emsp; 持续测试是在整个开发过程中协助测试管理的一个测试方法，包括单元测试、集成测试、系统测试和验收测试。测试用例最好在软件开发之前编写，而且除了执行常规测试类型外，测试管理也是高度自动化的。要达到这一点，就需要把需求管理、软件配置管理和测试管理高度集成起来。  
* DevOps敏捷开发  
&emsp; **<font color = "clime">敏捷开发指的是在 DevOps 中采用敏捷思想进行软件开发，敏捷宣言无疑是很重要的一项。有多种敏捷方法可以采用，比如Scrum、看板和极限编程。</font>**
* DevOps持续集成  
&emsp; **<font color = "clime">持续集成提供了让多个程序员可以同时运行应用程序的最佳实践，可以频繁合并源代码、验证代码(静态测试用例)、编译和测试代码(动态测试用例)。</font>**  
* DevOps持续交付  
&emsp; **<font color = "clime">持续交忖关注从开发、测试、验收到生产环境的高频生产能力。基于高度的自动化，极端的发布上线时间可以达到分钟级。</font>**  
* DevOps持续监控  
&emsp; 持续监控是DevOps的重要组成部分，它不仅监控软件(资源)，还监控开发人员(人员)和开发过程(方法)。资源在所有环境中被持续地监控，以便尽早发现问题 人员的衡量标准是能力发展(知识、技能和态度)，方法层面的衡量则包括速率(处理能力)和效率。  
* DevOps敏捷流程  
&emsp; 敏捷流程重点关注在标准管理过程中，需要进行哪些调整改进，才能符合敏捷开发方法的要求。  

## 1.2. 流程  
![image](http://182.92.69.8:8081/img/devops/devops/devops-1.png)  
&emsp; 上图显示了一个DevOps流程。它不是DevOps流程的正式定义，而是表述了在大多数组织机构中，为了实现一个服务而会被循环执行的合乎逻辑顺序的一系列阶段。  
&emsp; 深色部分表示开发流程，浅色部分表示运维流程。这两个流程构成了DevOps方法的核心。    
&emsp; 这两部分流程的每一部分又可以进一步细分为一系列阶段、过程，或被称作另一系列流程，它们都是由反复出现的步骤组成的。这些步骤都是为了达到同一个结果，实现相同的目的。  

&emsp; DevOps框架结构关系：  
![image](http://182.92.69.8:8081/img/devops/devops/devops-2.png)  
&emsp; 上图是指示性的。图中没有画出清晰的线条，但它展示了其连贯性。以下是对上图的简要解释，需要说明的是这种关系不是纯粹的一对一的关系。  

* 规划  
&emsp; 规划包含所有DevOps活动，既包含最初的整个路线图，又包含服务最后的增量交付。
* 编码  
&emsp; 敏捷开发主要涉及编码流程中提及的各个方面。
* 构建  
&emsp; 持续集成主要包括构建流程，也包含单元测试。  
* 测试  
&emsp; 持续测试在本文中比测试流程范围更大，因为它包括全生命周期中所有测试类型，如构建流程中的单元测试用例。  
* 发布  
&emsp; 持续交付不仅是一次发布的推出，还包括部署流水线，这已经在敏捷开发可执行的测试用例中被定义了。  
* 运维  
&emsp; 敏捷流程实际包括所有DevOps流程，而不仅仅是运维流程整个DevOps流程就是敏捷流程。
* 监控  
&emsp; 持续监控不仅包括产品阶段，还包括整个DevOps流程。

## 1.3. 工具集  
<!-- 
https://blog.csdn.net/hualinux/article/details/106586601?utm_medium=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-1.channel_param&depth_1-utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-1.channel_param


构建工具链真的会让研发流程高效起来吗
https://blog.gitee.com/2020/04/26/tool-chain/
-->
&emsp; 对于DevOps架构，可以参照DevOps过程逐步核查。按过程中每步决定的所需功能，经测量去选择可用工具。 **<font color = "clime">DevOps一般包括版本控制&协作开发工具、自动化构建和测试工具、持续集成&交付工具、部署工具、维护工具、监控，警告&分析工具等。</font>**  
![image](http://182.92.69.8:8081/img/devops/devops/devops-3.png)  

* 版本控制&协作开发：GitHub、GitLab、BitBucket、SubVersion、Coding、Bazaar
* 自动化构建和测试：Apache Ant、Maven 、Selenium、PyUnit、QUnit、JMeter、Gradle、PHPUnit、Nexus
* 持续集成&交付：Jenkins、Capistrano、BuildBot、Fabric、Tinderbox、Travis CI、flow.ci Continuum、LuntBuild、CruiseControl、Integrity、Gump、Go
* 容器平台：Docker、Rocket、Ubuntu(LXC)、第三方厂商如(AWS/阿里云)
* 配置管理：Chef、Puppet、CFengine、Bash、Rudder、Powershell、RunDeck、Saltstack、Ansible
* 微服务平台：OpenShift、Cloud Foundry、Kubernetes、Mesosphere
* 服务开通：Puppet、Docker Swarm、Vagrant、Powershell、OpenStack Heat
* 日志管理：Logstash、CollectD、StatsD
* 监控，警告&分析：Nagios、Ganglia、Sensu、zabbix、ICINGA、Graphite、Kibana

&emsp; 以下是关于Devops的工具链  
![image](http://182.92.69.8:8081/img/devops/devops/devops-5.png)  
![image](http://182.92.69.8:8081/img/devops/devops/devops-6.png)  
![image](http://182.92.69.8:8081/img/devops/devops/devops-7.png)  


## 1.4. jenkins+kubernetes+harbor+gitlab构建企业级devops平台    

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

![image](http://182.92.69.8:8081/img/devops/devops/devops-15.png)  


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

