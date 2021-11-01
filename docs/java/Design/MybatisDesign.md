


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


## 单例模式
&emsp; 在Mybatis中有两个地方用到单例模式，ErrorContext和LogFactory，其中ErrorContext是用在每个线程范围内的单例，用于记录该线程的执行环境错误信息，而LogFactory则是提供给整个Mybatis使用的日志工厂，用于获得针对项目配置好的日志对象。  


## 装饰器模式
&emsp; 装饰器模式（Decorator Pattern）允许向一个现有的对象添加新的功能，同时又不改变其结构。这种类型的设计模式属于结构型模式，它是作为现有的类的一个包装。这种模式创建了一个装饰类，用来包装原有的类，并在保持类方法签名完整性的前提下，提供了额外的功能。  
&emsp; 实际开发中，大多数用于对老项目的某些功能进行扩展。新项目中一般不怎么用此模式。  
&emsp; 生活中的案例：人靠衣裳马靠鞍。美容照相机、没有摄影机，美图秀秀。  
&emsp; 此设计模式重点在于对已有的功能进行扩展。  
&emsp; 在Mybatis中，Cache的实现类LruCache、FifoCache等都是装饰一个类PerpetualCache。常见代码格式，就是装饰类中会有个被装饰类的属性，并且这个属性还是构造方法的参数。  