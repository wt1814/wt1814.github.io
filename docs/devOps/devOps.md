
<!-- TOC -->

- [1. Devops与CI/CD](#1-devops与cicd)
    - [1.1. DevOps](#11-devops)
        - [1.1.1. 定义](#111-定义)
        - [1.1.2. 流程](#112-流程)
        - [1.1.3. 工具集](#113-工具集)
    - [1.2. CI/CD](#12-cicd)
    - [1.3. CI/CD、DevOps的关系](#13-cicddevops的关系)

<!-- /TOC -->

# 1. Devops与CI/CD
<!-- 
CI/CD 工具选型：Jenkins 还是 GitLab CI/CD？ 
https://mp.weixin.qq.com/s/5RpZRJlkypdg4rMAisCYTA
-->

## 1.1. DevOps  
### 1.1.1. 定义  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/devops/devops-4.png)   
&emsp; **DevOps: Development和Operations的组合。可以把DevOps看作开发（软件工程）、技术运营和质量保障（QA）三者的交集。**  
&emsp; DevOps并没有被定义成一组最佳实践和流程。尽管如此，一些常用概念的内在关联性仍可被识别出来，如下图所示。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/devops/devops-8.png)  
&emsp; 这是其中的一个DevOps框架。这个框架只指出那些被认可的概念和它们在某种程度上的关系。以下是对每个概念的简要说明。  

* DevOps持续测试  
&emsp; 持续测试是在整个开发过程中协助测试管理的一个测试方法，包括单元测试、集成测试、系统测试和验收测试。测试用例最好在软件开发之前编写，而且除了执行常规测试类型外，测试管理也是高度自动化的。要达到这一点，就需要把需求管理、软件配置管理和测试管理高度集成起来。  
* DevOps敏捷  
&emsp; 敏捷开发指的是在 DevOps 中采用敏捷思想进行软件开发，敏捷宣言无疑是很重要的一项。有多种敏捷方法可以采用，比如 Scrum 、看板和极限编程。
* DevOps持续集成  
&emsp; 持续集成提供了让多个程序员可以同时运行应用程序的最佳实践，可以频繁合并源代码、验证代码（静态测试用例）、编译和测试代码（动态测试用例〉。  
* DevOps持续交付  
&emsp; 持续交忖关注从开发、测试、验收到生产环境的高频生产能力。基于高度的自动化，极端的发布上线时间可以达到分钟级
* DevOps持续监控  
&emsp; 持续监控是DevOps的重要组成部分，它不仅监控软件（资源），还监控开发人员（人员）和开发过程（方法）。资源在所有环境中被持续地监控，以便尽早发现问题 人员的衡量标准是能力发展（知识、技能和态度），方法层面的衡量则包括速率（处理能力）和效率。  
* DevOps敏捷流程  
&emsp; 敏捷流程重点关注在标准管理过程中，需要进行哪些调整改进，才能符合敏捷开发方法的要求。  

### 1.1.2. 流程  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/devops/devops-1.png)  
&emsp; 上图显示了一个DevOps流程。它不是 DevOps 流程的正式定义，而是表述了在大多数组织机构中，为了实现一个服务而会被循环执行的合乎逻辑顺序的一系列阶段。  
&emsp; 深色部分表示开发流程，浅色部分表示运维流程。这两个流程构成了 DevOps 方法的核心。    
&emsp; 这两部分流程的每一部分又可以进一步细分为一系列阶段、过程，或被称作另一系列流程，它们都是由反复出现的步骤组成的。这些步骤都是为了达到同一个结果，实现相同的目的。  

&emsp; DevOps框架结构关系  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/devops/devops-2.png)  
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

### 1.1.3. 工具集  
<!-- 
https://blog.csdn.net/hualinux/article/details/106586601?utm_medium=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-1.channel_param&depth_1-utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-1.channel_param


构建工具链真的会让研发流程高效起来吗
https://blog.gitee.com/2020/04/26/tool-chain/
-->
&emsp; 对于DevOps架构，可以参照DevOps过程逐步核查。按过程中每步决定的所需功能，经测量去选择可用工具。 DevOps一般包括版本控制&协作开发工具、自动化构建和测试工具、持续集成&交付工具、部署工具、维护工具、监控，警告&分析工具等。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/devops/devops-3.png)  

* 版本控制&协作开发：GitHub、GitLab、BitBucket、SubVersion、Coding、Bazaar
* 自动化构建和测试：Apache Ant、Maven 、Selenium、PyUnit、QUnit、JMeter、Gradle、PHPUnit、Nexus
* 持续集成&交付:Jenkins、Capistrano、BuildBot、Fabric、Tinderbox、Travis CI、flow.ci Continuum、LuntBuild、CruiseControl、Integrity、Gump、Go
* 容器平台: Docker、Rocket、Ubuntu（LXC）、第三方厂商如（AWS/阿里云）
* 配置管理：Chef、Puppet、CFengine、Bash、Rudder、Powershell、RunDeck、Saltstack、Ansible
* 微服务平台：OpenShift、Cloud Foundry、Kubernetes、Mesosphere
* 服务开通：Puppet、Docker Swarm、Vagrant、Powershell、OpenStack Heat
* 日志管理：Logstash、CollectD、StatsD
* 监控，警告&分析：Nagios、Ganglia、Sensu、zabbix、ICINGA、Graphite、Kibana

&emsp; 以下是关于Devops的工具链  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/devops/devops-5.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/devops/devops-6.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/devops/devops/devops-7.png)  


## 1.2. CI/CD  
<!-- 
手把手教你深入了解 GitLab CI/CD 原理及流程 
https://mp.weixin.qq.com/s/Mbd1d2FGE2-fZQfO3616xA
Jenkins vs GitLab CI：CI/CD工具之战 
https://mp.weixin.qq.com/s/7fQXM2vvO-ufBGDqzQWmzg
http://www.yunweipai.com/35643.html
https://www.cnblogs.com/ham-731/p/12231665.html
https://blog.csdn.net/yuanjunliang/article/details/81211684
https://www.redhat.com/zh/topics/devops/what-is-ci-cd
-->
&emsp; 软件开发的持续方法基于自动执行脚本，以最大程度地减少在开发应用程序时引入错误的机会。从开发新代码到部署新代码，它们几乎不需要人工干预，甚至根本不需要干预。  
&emsp; 它涉及到在每次小的迭代中就不断地构建、测试和部署代码更改，从而减少了基于已经存在bug或失败的先前版本开发新代码的机会。  
&emsp; **Continuous Integration（持续集成）**  
&emsp; 假设一个应用程序，其代码存储在GitLab的Git仓库中。开发人员每天都要多次推送代码更改。对于每次向仓库的推送，你都可以创建一组脚本来自动构建和测试你的应用程序，从而减少了向应用程序引入错误的机会。这种做法称为持续集成，对于提交给应用程序（甚至是开发分支）的每项更改，它都会自动连续进行构建和测试，以确保所引入的更改通过你为应用程序建立的所有测试，准则和代码合规性标准。  
&emsp; **Continuous Delivery（持续交付）**  
&emsp; 持续交付是超越持续集成的更进一步的操作。应用程序不仅会在推送到代码库的每次代码更改时进行构建和测试，而且，尽管部署是手动触发的，但作为一个附加步骤，它也可以连续部署。此方法可确保自动检查代码，但需要人工干预才能从策略上手动触发以必输此次变更。  
&emsp; **Continuous Deployment（持续部署）**  
&emsp; 与持续交付类似，但不同之处在于，你无需将其手动部署，而是将其设置为自动部署。完全不需要人工干预即可部署你的应用程序。 

## 1.3. CI/CD、DevOps的关系  
&emsp; **DevOps是CI、CD思想的延伸，CI、CD是DevOps的基础核心。**如果没有 CI、CD自动化的工具和流程，谈 DevOps是没有意义的。  


