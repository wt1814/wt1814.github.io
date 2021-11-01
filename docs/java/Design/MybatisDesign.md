


# 8种Mybatis里的设计模式
<!-- 
https://segmentfault.com/a/1190000038551120
https://cloud.tencent.com/developer/article/1447253
-->

1. Builder模式：例如 SqlSessionFactoryBuilder、XMLConfigBuilder、XMLMapperBuilder、XMLStatementBuilder、CacheBuilder；  
2. 工厂模式：例如SqlSessionFactory、ObjectFactory、MapperProxyFactory；  
3. 单例模式：例如ErrorContext和LogFactory；  
4. 代理模式：Mybatis实现的核心，比如MapperProxy、ConnectionLogger，用的jdk的动态代理；还有executor.loader包使用了cglib或者javassist达到延迟加载的效果；  
5. 组合模式：例如SqlNode和各个子类ChooseSqlNode等；  
模板方法模式: 例如BaseExecutor和SimpleExecutor，还有BaseTypeHandler和所有的子类例如IntegerTypeHandler；  
6. 适配器模式: 例如Log的Mybatis接口和它对jdbc、log4j等各种日志框架的适配实现；  
7. 装饰者模式: 例如cache包中的cache.decorators子包中等各个装饰者的实现；  
8. 迭代器模式: 例如迭代器模式PropertyTokenizer；  


### 单例模式
在Mybatis中有两个地方用到单例模式，ErrorContext和LogFactory，其中ErrorContext是用在每个线程范围内的单例，用于记录该线程的执行环境错误信息，而LogFactory则是提供给整个Mybatis使用的日志工厂，用于获得针对项目配置好的日志对象。  
