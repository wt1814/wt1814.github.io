
<!-- 
DDD 划分领域、子域、核心域、支撑域的目的 
https://mp.weixin.qq.com/s/oY_s7m_BQu_F2fxB1gbH3A

DDD为什么能火起来？和微服务有啥关系？ 
https://mp.weixin.qq.com/s/3eIxfCsTV64zoKjQRv2Zfg






如果你还不理解我说的话，请看一下 Vaughn Vernon 出的一本叫做《IMPLEMENTING DOMAIN-DRIVEN DESIGN》(实现领域驱动设计)这本书，书中讲解了贫血模型与领域模型的区别，相信你会受益匪浅。


书籍 
《架构师的自我修炼：技术、架构和未来》

DDD领域驱动模型
https://mp.weixin.qq.com/s/I3Ifg7UhrrTPz0OaMILUSA
一文揭秘领域驱动设计（DDD）：领域和子域！ 
https://mp.weixin.qq.com/s/dbQygXHgutM9haxAzfY1UA

DDD 领域驱动设计：贫血模型、充血模型的深入解读！ 
https://mp.weixin.qq.com/s/N4si9q7RXJk82VVv8kDfew

 DDD 到底适不适合微服务架构？一文彻底讲透！ 
 https://mp.weixin.qq.com/s/5E6yprErmr9DQAH42XAVEA
DDD领域驱动设计实战-分层架构 
https://mp.weixin.qq.com/s/yAeqKJ-qsRPGAmYvFAgafg
DDD里面的CQRS到底是什么？ 
https://mp.weixin.qq.com/s/or3R5KMJ1pKQznguJCUmQw
图解DDD建模六个问题与六个步骤 
https://mp.weixin.qq.com/s/H57jr6BReVTt18NdnLiwCA
领域驱动设计(DDD:Domain-Driven Design)
https://www.jdon.com/ddd.html
为什么在做微服务设计的时候需要DDD？ 
https://mp.weixin.qq.com/s/Ouxmbfo_QE9Ad4rZOgtycQ
这 3 种 DDD 分层架构的模式
https://mp.weixin.qq.com/s/UB7pti5zlbflY9E2iTqsXQ
DDD领域驱动设计-充血模型、贫血领域模型还不赶紧了解? 
https://mp.weixin.qq.com/s/18mX6kV84tebZOEZIJOhMQ
为什么说优秀的微服务设计一定少不了 DDD？ 
https://mp.weixin.qq.com/s/h5fTn4S7GM-BdPUgkyneww
DDD分层架构的三种模式 
https://mp.weixin.qq.com/s/eL-rM353ctMuEqV99yhnCw
什么是充血模型？什么又是贫血模型? 
https://mp.weixin.qq.com/s/qVRYsspLq7jXPQY1-josHg
 一文理解 DDD 领域驱动设计！ 
https://mp.weixin.qq.com/s/UOr0zRCxy1j__-uaEBt4fA
架构概述 
https://mp.weixin.qq.com/s/pVKNU1er7yMDBVQ3wg685w
都在聊DDD, 哪里超越了MVC? 
https://mp.weixin.qq.com/s/WBc6ZxL4jFa2K6j0-ydbPA
什么是充血模型？什么又是贫血模型? 
https://mp.weixin.qq.com/s/xLf8P2uUa1rhLFg_AdEnKw
京东平台研发：领域驱动设计（DDD）实践总结 
https://mp.weixin.qq.com/s/OKrDQz3AQLTyTA7XYTLTCQ
DDD 领域驱动设计：贫血模型、充血模型的深入解读！ 
https://mp.weixin.qq.com/s/urvyRPU7_t6dX4cT20h7sQ

从架构演进谈 DDD 兴起的原因以及与微服务的关系 
https://mp.weixin.qq.com/s/pSl4eMcuR8l3fy25TcNa_w



-->

DDD并不是一个重量级的设计方法和开发过程。DDD并不是画模型图，而是将领域专家的思维模型转化成 有用的业务模型。  
如果你连一个领域专家都找不到，那么你根本无法对一个领域有深入的理解。当你找到领域专家的时候，此时开发者应该表现出主动。开发者应该找领域专家交谈并仔细聆听，然后将你们的谈话转化成软件代码。  


DDD绝非是充满繁文缚节的笨重开发过程。事实上，DDD能够很 好地与敏捷项目框架结合起来，比如Scrum。DDD也倾向于“测试先行，逐步改 进”的设计思路。在你开发一个新的领域对象时，比如实体或值对象，你可以采用 以下步骤进行：  
1. 编写测试代码以模拟客户代码是如何使用该领域对象的。  
2. 创建该领域对象以使测试代码能够编译通过。  
3. 同时对测试和领域对象进行重构，直到测试代码能够正确地模拟客户代码， 同时领域对象拥有能够表明业务行为的方法签名。  
4. 实现领域对象的行为，直到测试通过为止，再对实现代码进行重构。  
5. 向你的团队成员展示代码，包括领域专家，以保证领域对象能够正确地反映 通用语言。  


在我们实施某个解决方案之前，我们需要对问题空间和解决方案空间进行评 估。为了保证你的项目朝着正确的方向行进，你需要先回答以下问题：  
* 这个战略核心域的名字是什么，它的目标是什么?  
* 这个战略核心域中包含哪些概念?  
* 这个核心域的支撑子域和通用子域是什么?  
* 如何安排项目人员?  
* 你能组建出一支合适的团队吗?  
