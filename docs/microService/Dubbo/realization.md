


# Dubbo实现细节  
## 初始化过程细节
### 解析服务  

基于 dubbo.jar 内的 META-INF/spring.handlers 配置，Spring 在遇到 dubbo 名称空间时，会回调 DubboNamespaceHandler。  

所有 dubbo 的标签，都统一用 DubboBeanDefinitionParser 进行解析，基于一对一属性映射，将 XML 标签解析为 Bean 对象。  

在 ServiceConfig.export() 或 ReferenceConfig.get() 初始化时，将 Bean 对象转换 URL 格式，所有 Bean 属性转成 URL 的参数。  

然后将 URL 传给 协议扩展点，基于扩展点的 扩展点自适应机制，根据 URL 的协议头，进行不同协议的服务暴露或引用。  

### 暴露服务  
1. 只暴露服务端口：
在没有注册中心，直接暴露提供者的情况下 [1]，ServiceConfig 解析出的 URL 的格式为： dubbo://service-host/com.foo.FooService?version=1.0.0。  
基于扩展点自适应机制，通过 URL 的 dubbo:// 协议头识别，直接调用 DubboProtocol的 export() 方法，打开服务端口。  
2. 向注册中心暴露服务：  
在有注册中心，需要注册提供者地址的情况下 [2]，ServiceConfig 解析出的 URL 的格式为: registry://registry-host/org.apache.dubbo.registry.RegistryService?export=URL.encode("dubbo://service-host/com.foo.FooService?version=1.0.0")，  
基于扩展点自适应机制，通过 URL 的 registry:// 协议头识别，就会调用 RegistryProtocol 的 export() 方法，将 export 参数中的提供者 URL，先注册到注册中心。  
再重新传给 Protocol 扩展点进行暴露： dubbo://service-host/com.foo.FooService?version=1.0.0，然后基于扩展点自适应机制，通过提供者 URL 的 dubbo:// 协议头识别，就会调用 DubboProtocol 的 export() 方法，打开服务端口。  

### 引用服务
1. 直连引用服务：
在没有注册中心，直连提供者的情况下 [3]，ReferenceConfig 解析出的 URL 的格式为：dubbo://service-host/com.foo.FooService?version=1.0.0。
基于扩展点自适应机制，通过 URL 的 dubbo:// 协议头识别，直接调用 DubboProtocol 的 refer() 方法，返回提供者引用。  
2. 从注册中心发现引用服务：  
在有注册中心，通过注册中心发现提供者地址的情况下 [4]，ReferenceConfig 解析出的 URL 的格式为： registry://registry-host/org.apache.dubbo.registry.RegistryService?refer=URL.encode("consumer://consumer-host/com.foo.FooService?version=1.0.0")。  
基于扩展点自适应机制，通过 URL 的 registry:// 协议头识别，就会调用 RegistryProtocol 的 refer() 方法，基于 refer 参数中的条件，查询提供者 URL，如： dubbo://service-host/com.foo.FooService?version=1.0.0。  
基于扩展点自适应机制，通过提供者 URL 的 dubbo:// 协议头识别，就会调用 DubboProtocol 的 refer() 方法，得到提供者引用。  
然后 RegistryProtocol 将多个提供者引用，通过 Cluster 扩展点，伪装成单个提供者引用返回。  

### 拦截服务
基于扩展点自适应机制，所有的 Protocol 扩展点都会自动套上 Wrapper 类。  
基于 ProtocolFilterWrapper 类，将所有 Filter 组装成链，在链的最后一节调用真实的引用。  
基于 ProtocolListenerWrapper 类，将所有 InvokerListener 和 ExporterListener 组装集合，在暴露和引用前后，进行回调。  
包括监控在内，所有附加功能，全部通过 Filter 拦截实现。  