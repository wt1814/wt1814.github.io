

# Mybatis中的设计模式  

<!-- 
MyBatis源码解读 9 种设计模式：
https://mp.weixin.qq.com/s/oIFZWXMj2z9lV6tf-nF2jQ
-->

|设计模式| 类|
|---|---|
|工厂 |SqlSessionFactory、ObjectFactory、MapperProxyFactory |
|建造者 |XMLConfigBuilder、XMLMapperBuilder、XMLStatementBuidler|
|单例模式 |SqlSessionFactory、Configuration、ErrorContext |
|代理模式 |绑定：MapperProxy<br/> 延迟加载：ProxyFactory（CGLIB、JAVASSIT）<br/> 插件：Plugin <br/>Spring 集成 MyBaits：SqlSessionTemplate 的内部类 SqlSessionInterceptor <br/>MyBatis 自带连接池：PooledDataSource 管理的 PooledConnection <br/>日志打印：ConnectionLogger、StatementLogger |
|适配器模式 |logging 模块，对于 Log4j、JDK logging 这些没有直接实现 slf4j 接口的日志组件，需要适配器|
|模板方法 |BaseExecutor 与子类 SimpleExecutor、BatchExecutor、ReuseExecutor|
|装饰器模式 |LoggingCache、LruCache 等对 PerpectualCache 的装饰 <br/>CachingExecutor 对其他 Executor 的装饰 |
|责任链模式 |InterceptorChain|

