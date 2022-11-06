

&emsp; [MyBatis架构](/docs/SSM/MyBatis/MybatisFramework.md)  
&emsp; [MyBatis SQL执行解析](/docs/SSM/MyBatis/MybatisExecutor.md)  
&emsp; [Spring整合MyBatis原理](/docs/SSM/MyBatis/SpringMybatisPrinciple.md)  
&emsp; [MyBatis缓存](/docs/SSM/MyBatis/MybatisCache.md)  
&emsp; [MyBatis插件解析](/docs/SSM/MyBatis/MybatisPlugins.md)  
&emsp; [MyBatis中的设计模式](/docs/SSM/MyBatis/MybatisDesign.md)  

<!-- 
https://blog.csdn.net/qq32933432/category_9725651.html




源码

Mybatis解析动态sql原理分析
https://www.cnblogs.com/fangjian0423/tag/mybatis/

Mybatis源码分析
https://mp.weixin.qq.com/s?__biz=MzUxNDA1NDI3OA==&mid=2247485861&idx=1&sn=6f88119d44fc8fae4306ee5a3ee8aa79&chksm=f94a884cce3d015a650c26463240161455b77cc84aceb69eb0fe1fbc4a0ba8e913eaaeb9864a&mpshare=1&scene=1&srcid=&sharer_sharetime=1569000364628&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=2a4ff15fdd8463462afa723afd07ecb950051db23607b0b61294e6413a3dacfb4146d673dbc982ede80e1f49e035dfd1b4c371b2383ee1a0f13fb42b9fc7eae2d531b5fb06f7d472b69021d8844408c0&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=l152qY7UDy13%2FQ8lMQftZpzwON66UoS8zNnRNqU0gQ1B38kfpkeCoh6I%2F0Cu%2FOwX



MyBatis 核心组件
 面试官问你MyBatis SQL是如何执行的？把这篇文章甩给他 
https://mp.weixin.qq.com/s/4kSPt84s4KKQ4ZRu7RcZAQ


MyBatis 核心配置综述之 ResultSetHandler 
http://www.justdojava.com/2019/07/29/mybatis-resultsethandler/
MyBatis 核心配置综述之 Configuration详解 
http://www.justdojava.com/2019/08/03/mybatis-hearyouwanttorun/
MyBatis 核心配置综述之 ParameterHandlers 
http://www.justdojava.com/2019/07/22/mybatis-parameterhandler/
MyBatis 核心配置综述之 StatementHandler 
http://www.justdojava.com/2019/07/14/MyBatis-StatementHandler/
MyBatis 核心配置综述 之项目概述 
http://www.justdojava.com/2019/06/30/MyBatisArchitecture/
MyBatis 核心配置综述之 Executor 
http://www.justdojava.com/2019/07/06/MyBatis-Executor/

Java极客技术 
MyBatis 核心配置综述之 Executor 
https://mp.weixin.qq.com/s?__biz=MzU3NzczMTAzMg==&mid=2247484658&idx=2&sn=01e78084f34aa6fa52c456bb37719169&chksm=fd016445ca76ed53ea85e20bac30d0c286a0faa4fcebe84be29904953737018d8ff20661a3e7&mpshare=1&scene=1&srcid=&sharer_sharetime=1566314126255&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=a98b434d6faae6161256a4f868eb3ebe9fc122a20fec82240d79662700fce3fc8d7d000b3a792f912c6d772b2b8fb878ea5c6848d4b1a852dedf84e98414f42930267b5e912aba57ee271c4501f9c907&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=p6auQCRUdz3YfMwJ1xme47R%2BDCakEmcXa%2FfepST4E86ADUg6UT3f%2BojJW6kiKF6K
MyBatis 核心配置综述之StatementHandler
https://mp.weixin.qq.com/s?__biz=MzU3NzczMTAzMg==&mid=2247484752&idx=2&sn=3c3d13f0bcccb846830a26cfd603279c&chksm=fd0165e7ca76ecf154459c6575c46717e449570e678736a85923b4eb116759646b7952f9162f&mpshare=1&scene=1&srcid=&sharer_sharetime=1566314082185&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=36a99a852770fa0364a3d3d57f471953336bf5ae147d2b52c5695d5cd13e5b7491fb549e6e8708f33ed47008504d2ed49aefa569cd6239c703db222b236c4e1a8c5262a46cbfee744b95d54eca85cf91&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=p6auQCRUdz3YfMwJ1xme47R%2BDCakEmcXa%2FfepST4E86ADUg6UT3f%2BojJW6kiKF6K
MyBatis 核心配置综述之 ParameterHandlers 
https://mp.weixin.qq.com/s?__biz=MzU3NzczMTAzMg==&mid=2247484889&idx=2&sn=3b95f250c7c229eb2065d36c55345dd0&chksm=fd01656eca76ec781d28a3627da78be6a983fb9df866a09757803d81640c521839852d350588&mpshare=1&scene=1&srcid=&sharer_sharetime=1566314036581&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=a98b434d6faae6169892f3f8868465417daa1980e0271476d3f3960dc6a8ef90019e72f07c4c9455b1958902d54e2fea0147c67e7f05d5ae7d29e089361ad0acff0e339c0a76e5571d52a389066baadb&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=p6auQCRUdz3YfMwJ1xme47R%2BDCakEmcXa%2FfepST4E86ADUg6UT3f%2BojJW6kiKF6K
MyBatis 核心配置综述之 ResultSetHandler 
https://mp.weixin.qq.com/s?__biz=MzU3NzczMTAzMg==&mid=2247484921&idx=2&sn=767e949154e79ee295ed1f3fa2c3156b&chksm=fd01654eca76ec5846146e2790454e59d5bb0f40abdd1e8d8994cfcddb2694626b9b3f849e70&mpshare=1&scene=1&srcid=&sharer_sharetime=1566314021409&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=5ead8116cc3d8776d50defac29074d1af944fe86852de5aa61a4f6389b143502967cb126a20ec24fc1034ce9ab719329f9b4e57ad96a5f7d6f42ab36d5d38524fe52424527f86edcc9d554bb692731bf&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=p6auQCRUdz3YfMwJ1xme47R%2BDCakEmcXa%2FfepST4E86ADUg6UT3f%2BojJW6kiKF6K
MyBatis 核心配置综述之 Configuration详解 
https://mp.weixin.qq.com/s?__biz=MzU3NzczMTAzMg==&mid=2247485089&idx=1&sn=9c7651affa6bae85cbfe7cb1fa66399b&chksm=fd016616ca76ef00b359be4e19530025354bd7a99f65932401c9dd17bbf30b31465abb755d74&mpshare=1&scene=1&srcid=&sharer_sharetime=1566313997182&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=ecc4386bb884a7b1a4fab80aad9ca9b9c822514bdaca146bb60396cb9764500a51876c741a6b6d05a66bd71077646f2904d7f5a1f08d9abb80c87035c5fa963dcc260aa7e742153bcafc54084aa2fa53&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=p6auQCRUdz3YfMwJ1xme47R%2BDCakEmcXa%2FfepST4E86ADUg6UT3f%2BojJW6kiKF6K

类型转换器TypeHandler 
https://mp.weixin.qq.com/s?__biz=MzAxODcyNjEzNQ==&mid=2247487957&idx=3&sn=1766dbb8ef4e58a300275567fa1b92b1&chksm=9bd0bc4daca7355b9260a59d40f4603ccf6fd5d6cebfcbd68f10d4119c256e0c62a5086ddc5c&mpshare=1&scene=1&srcid=&sharer_sharetime=1564895120727&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=a98b434d6faae61634f833aa629e4ce790e7076ded67c0c21fd96cb532cffc02803cc5d6abaa4a838c45d1da9d015faa329867a8fbc63a39818545b6fffcb6c164a36edcffe1b5a892459ac02cd3972d&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060834&lang=zh_CN&pass_ticket=m8o3Qvyht9PTR90hDubZDHVsUbW3ybzIBw%2FSOhKgYfl%2BUh%2Fs8AN%2BYt3jijpiRXzg
缓存篇
[MyBatis] 缓存模块实现原理剖析
https://mp.weixin.qq.com/s/_V8d37Oo3PMubLu429K2Lw

 MyBatis 核心配置综述之 细说一级缓存 
http://www.justdojava.com/2019/07/13/MyBatis-OneLevelCache/
MyBatis 核心配置综述之 细说二级缓存 
http://www.justdojava.com/2019/07/29/mybatis-secondcache/
Mybatis源码分析之Cache一级缓存原理
https://mp.weixin.qq.com/s?__biz=MzUxNDA1NDI3OA==&mid=2247485847&idx=1&sn=eff52be6e3fb0482d3f1b0c3000c879e&chksm=f94a887ece3d0168bb779d3d8ae22d74a7d1df6229d5966564cd152b15e4ed79f755afac348f&scene=21#wechat_redirect
Mybatis源码分析之Cache二级缓存原理
https://mp.weixin.qq.com/s?__biz=MzUxNDA1NDI3OA==&mid=2247485852&idx=1&sn=0b9627ce7e610da811d3a5cb91d786bb&chksm=f94a8875ce3d016348790c14c5857423d4a324cc02c08a0a65dbac81e84cba30aa6a92df9c0d&scene=21#wechat_redirect



Mybatis插件
MyBatis大揭秘：Plugin 插件设计原理 
https://mp.weixin.qq.com/s?__biz=Mzg2MjEwMjI1Mg==&mid=2247490732&idx=3&sn=b14a24fa9d32972e743e2d4866c56cc9&chksm=ce0dab2ff97a2239ea21107fd63526da7bb1055ac180f035681cb792e28b089fb5b1643384eb&mpshare=1&scene=1&srcid=&sharer_sharetime=1575266424808&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=ba91437029ffb2657426b0a8d8c91f47359dd550a8b9227504fd17118a897902e6965748e8509c17fa046510c48edf61681febdb3ff18562b4deb11d6205390f18973c01a8671dbeefea88d87b8ff1d9&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62070158&lang=zh_CN&pass_ticket=XhI9M9nIhrTxHIclQ8uxyPd2unJlQ4N%2BES6NrkpAI67hgwA8jaM03%2BH7QSdXE73Z

面试官：说一下Mybatis插件的实现原理？ 
https://mp.weixin.qq.com/s/YB3i4jYe7uUPAJPCc0e4VQ

深入理解 Mybatis 插件开发
https://mp.weixin.qq.com/s/gEYGdB07TzFi8x6io3Zpfw



Spring和Mybatis整合
深入剖析-mybatis-整合Spring原理（三）
https://cloud.tencent.com/developer/article/1486154
mybatis-spring原理解析
https://segmentfault.com/a/1190000019255721?utm_source=tag-newest
spring源码剖析（八）spring整合mybatis原理
https://blog.csdn.net/fighterandknight/article/details/51448161?utm_medium=distribute.pc_relevant_t0.none-task-blog-BlogCommendFromMachineLearnPai2-1.nonecase&depth_1-utm_source=distribute.pc_relevant_t0.none-task-blog-BlogCommendFromMachineLearnPai2-1.nonecase
mybatis与spring集成原理
https://www.jianshu.com/p/da65f704639a


设计模式
MyBatis源码解读 9 种设计模式：
https://mp.weixin.qq.com/s/oIFZWXMj2z9lV6tf-nF2jQ


-->