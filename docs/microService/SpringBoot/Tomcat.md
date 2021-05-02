

<!-- TOC -->

- [1. SpringBoot内置Tomcat](#1-springboot内置tomcat)
    - [1.1. 怎么内嵌Tomcat？](#11-怎么内嵌tomcat)
    - [1.2. SpringBoot内置Tomcat应用使用](#12-springboot内置tomcat应用使用)
    - [1.3. SpringBoot内置Tomcat原理](#13-springboot内置tomcat原理)
        - [1.3.1. Tomcat的自动装配](#131-tomcat的自动装配)
        - [1.3.2. Tomcat的启动](#132-tomcat的启动)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
1. SpringBoot内置Tomcat，可以对比SpringBoot自动配置运行流程了解。  
2. Tomcat的自动装配：自动装配过程中，Web容器所对应的自动配置类为ServletWebServerFactoryAutoConfiguration，该类导入了EmbeddedTomcat，EmbeddedJetty，EmbeddedUndertow三个类，可以根据用户的需求去选择使用哪一个web服务器，默认情况下使用的是tomcat。  
3. Tomcat的启动：在容器刷新refreshContext(context)步骤完成。  

# 1. SpringBoot内置Tomcat
<!--
SpringBoot内嵌Tomcat的实现原理解析
https://blog.csdn.net/lveex/article/details/108942707?utm_medium=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-1.control&depth_1-utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-1.control 
SpringBoot内置tomcat启动原理
https://www.cnblogs.com/sword-successful/p/11383723.html
-->

<!-- 
~~
Spring Boot 内嵌Tomcat启动原理
https://blog.csdn.net/weixin_42440154/article/details/104943010
-->

## 1.1. 怎么内嵌Tomcat？    
<!-- 
https://blog.csdn.net/the_one_and_only/article/details/105177506
-->

## 1.2. SpringBoot内置Tomcat应用使用  
<!-- 
https://www.cnblogs.com/sword-successful/p/11383723.html
-->

## 1.3. SpringBoot内置Tomcat原理  
### 1.3.1. Tomcat的自动装配  
&emsp; 自动装配过程中，查找classpath上所有jar包中的META-INF/spring.factories，找出其中的自动配置类并导入到容器中，其中Web容器所对应的自动配置类为ServletWebServerFactoryAutoConfiguration。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/boot/boot-1.png)  
&emsp; ServletWebServerFactoryAutoConfiguration主要导入了BeanPostProcessorRegister，该类实现了ImportBeanDefinitionRegister接口，可以用来注册额外的BeanDefinition，同时，该类还导入了EmbeddedTomcat，EmbeddedJetty，EmbeddedUndertow三个类，可以根据用户的需求去选择使用哪一个web服务器，默认情况下使用的是tomcat。    
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/boot/boot-2.png)  
&emsp; EmbeddedTomcat本身是一个FactoryBean，用来实例化TomcatServletWebServerFactory。此时TomcatServletWebServerFactory中就包含了创建和启动Tomcat的方法getWebServer()。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/boot/boot-3.png)  

### 1.3.2. Tomcat的启动  
&emsp; SpringBoot是在项目启动的时候才同时启动Tomcat的，很显然getWebServer()是在项目启动的过程中调用的。跟踪SpringApplication的run()，其中存在refreshContext(context)，此时主要完成容器的刷新。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/boot/boot-4.png)  
&emsp; 容器刷新跟踪到最后是AbstractApplicationContext中的onRefresh()，显然这是一个钩子函数，应用了模板方法，查看所有的实现方法，其中有一个ServletWebServerApplicationContext，则是当前Web容器的实现。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/boot/boot-5.png)  
&emsp; 而ServletWebServerApplicationContext中主要是去获得ServletWebServerFactory对象，同时调用getWebServer创建WebServer对象。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/boot/boot-6.png)  

```java
private void createWebServer() {
	WebServer webServer = this.webServer;
	ServletContext servletContext = getServletContext();
	if (webServer == null && servletContext == null) {
        //获取servletWebServerFactory,从上下文注册bean中可以找到
		ServletWebServerFactory factory = getWebServerFactory();
        //获取servletContextInitializer,获取webServer
		this.webServer = factory.getWebServer(getSelfInitializer());
	}
	else if (servletContext != null) {
		try {
			getSelfInitializer().onStartup(servletContext);
		}
		catch (ServletException ex) {
			throw new ApplicationContextException("Cannot initialize servlet context", ex);
		}
	}
    //替换servlet相关的属性资源
	initPropertySources();
}
```

&emsp; 此时，主要处理的是Tomcat容器对象的创建、环境配置和启动。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/boot/boot-7.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/microService/boot/boot-8.png)  


<!-- 

1.3.3. 获取tomcat的bean的实例对象  
**ServletWebServerApplicationContext**  

```java
protected ServletWebServerFactory getWebServerFactory() {
    // Use bean names so that we don't consider the hierarchy
    String[] beanNames = getBeanFactory().getBeanNamesForType(ServletWebServerFactory.class);
    if (beanNames.length == 0) {
        throw new ApplicationContextException("Unable to start ServletWebServerApplicationContext due to missing "
                + "ServletWebServerFactory bean.");
    }
    if (beanNames.length > 1) {
        throw new ApplicationContextException("Unable to start ServletWebServerApplicationContext due to multiple "
                + "ServletWebServerFactory beans : " + StringUtils.arrayToCommaDelimitedString(beanNames));
    }
    return getBeanFactory().getBean(beanNames[0], ServletWebServerFactory.class);
}
```

```java
protected ServletWebServerFactory getWebServerFactory() {
    // Use bean names so that we don't consider the hierarchy
    String[] beanNames = getBeanFactory().getBeanNamesForType(ServletWebServerFactory.class);
    if (beanNames.length == 0) {
        throw new ApplicationContextException("Unable to start ServletWebServerApplicationContext due to missing "
                + "ServletWebServerFactory bean.");
    }
    if (beanNames.length > 1) {
        throw new ApplicationContextException("Unable to start ServletWebServerApplicationContext due to multiple "
                + "ServletWebServerFactory beans : " + StringUtils.arrayToCommaDelimitedString(beanNames));
    }
    return getBeanFactory().getBean(beanNames[0], ServletWebServerFactory.class);
}
```

**DefaultListableBeanFactoryf**

```java
/*
第一个参数type表示要查找的类型
第二个参数表示是否考虑非单例bean
第三个参数表示是否允许提早初始化
*/
@Override
public String[] getBeanNamesForType(@Nullable Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
    //配置还未被冻结或者类型为null或者不允许早期初始化
    if (!isConfigurationFrozen() || type == null || !allowEagerInit) {
        return doGetBeanNamesForType(ResolvableType.forRawClass(type), includeNonSingletons, allowEagerInit);
    }
    //此处注意isConfigurationFrozen为false的时候表示beanDefinition可能还会发生更改和添加，所以不能进行缓存，如果允许非单例bean，那么从保存所有bean的集合中获取，否则从单例bean中获取
    Map<Class<?>, String[]> cache =
            (includeNonSingletons ? this.allBeanNamesByType : this.singletonBeanNamesByType);
    String[] resolvedBeanNames = cache.get(type);
    if (resolvedBeanNames != null) {
        return resolvedBeanNames;
    }
    //如果缓存中没有获取到，那么只能重新获取，获取到之后就存入缓存
    resolvedBeanNames = doGetBeanNamesForType(ResolvableType.forRawClass(type), includeNonSingletons, true);
    if (ClassUtils.isCacheSafe(type, getBeanClassLoader())) {
        cache.put(type, resolvedBeanNames);
    }
    return resolvedBeanNames;
}
```

```jva
private String[] doGetBeanNamesForType(ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit) {
    List<String> result = new ArrayList<>();

    // Check all bean definitions.
    for (String beanName : this.beanDefinitionNames) {
        // Only consider bean as eligible if the bean name
        // is not defined as alias for some other bean.
        //如果时别名则跳过（当前集合会保存所有的主beanname，并且不会保存别名，别名由beanfactory中别名map维护）
        if (!isAlias(beanName)) {
            try {
                //获取合并的beandefinition，合并的beandefinition是指spring整合了父beandefinition的属性，将其beandefinition编程了rootBeanDefinition
                RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
                // Only check bean definition if it is complete.
                //抽象的beandefinition是不做考虑，抽象的就是拿来继承的，如果允许早期初始化，那么直接短路，进入方法体，如果不允许早期初始化，那么需要进一步判断，如果是不允许早期初始化的，并且beanClass已经被加载或者它是可以早期初始化的，那么如果当前bean是工厂bean，并且指定的bean又是工厂那么这个bean就必须被早期初始化，也就是说就不符合我们制定的allowEagerInit为false的情况，直接跳过

                if (!mbd.isAbstract() && (allowEagerInit ||
                        (mbd.hasBeanClass() || !mbd.isLazyInit() || isAllowEagerClassLoading()) &&
                                !requiresEagerInitForType(mbd.getFactoryBeanName()))) {
                    //如果当前bean是工厂bean
                    boolean isFactoryBean = isFactoryBean(beanName, mbd);
                    //如果允许早期初始化，那么基本上会调用最后的isTypeMatch方法，这个方法会导致工厂的实例化，但是当前不允许进行早期实例化在不允许早期实例化的情况下，如果当前bean是工厂bean，那么它只能在已经被创建的情况下调用isTypeMatch进行匹配判断否则只能宣告匹配失败，返回false
                    BeanDefinitionHolder dbd = mbd.getDecoratedDefinition();
                    boolean matchFound = false;
                    boolean allowFactoryBeanInit = allowEagerInit || containsSingleton(beanName);
                    boolean isNonLazyDecorated = dbd != null && !mbd.isLazyInit();
                    if (!isFactoryBean) {
                        if (includeNonSingletons || isSingleton(beanName, mbd, dbd)) {
                            matchFound = isTypeMatch(beanName, type, allowFactoryBeanInit);
                        }
                    }
                    else  {
                        //如果没有匹配到并且他是个工厂bean，那么加上&前缀，表示要获取factorybean类型的bean
                        if (includeNonSingletons || isNonLazyDecorated ||
                                (allowFactoryBeanInit && isSingleton(beanName, mbd, dbd))) {
                            matchFound = isTypeMatch(beanName, type, allowFactoryBeanInit);
                        }
                        if (!matchFound) {
                            // In case of FactoryBean, try to match FactoryBean instance itself next.
                            beanName = FACTORY_BEAN_PREFIX + beanName;
                            matchFound = isTypeMatch(beanName, type, allowFactoryBeanInit);
                        }
                    }
                    //找到便记录到result集合中，等待返回
                    if (matchFound) {
                        result.add(beanName);
                    }
                }
            }
            catch (CannotLoadBeanClassException | BeanDefinitionStoreException ex) {
                if (allowEagerInit) {
                    throw ex;
                }
                // Probably a placeholder: let's ignore it for type matching purposes.
                LogMessage message = (ex instanceof CannotLoadBeanClassException) ?
                        LogMessage.format("Ignoring bean class loading failure for bean '%s'", beanName) :
                        LogMessage.format("Ignoring unresolvable metadata in bean definition '%s'", beanName);
                logger.trace(message, ex);
                onSuppressedException(ex);
            }
        }
    }
// Check manually registered singletons too.
//从单例注册集合中获取，这个单例集合石保存spring内部注入的单例对象，他们的特点就是没有beanDefinition
    for (String beanName : this.manualSingletonNames) {
        try {
            // In case of FactoryBean, match object created by FactoryBean.
            //如果是工厂bean,那么调用其getObjectType去匹配是否符合指定类型
            if (isFactoryBean(beanName)) {
                if ((includeNonSingletons || isSingleton(beanName)) && isTypeMatch(beanName, type)) {
                    result.add(beanName);
                    // Match found for this bean: do not match FactoryBean itself anymore.
                    continue;
                }
                // In case of FactoryBean, try to match FactoryBean itself next.
                beanName = FACTORY_BEAN_PREFIX + beanName;
            }
            // Match raw bean instance (might be raw FactoryBean).
            //如果没有匹配成功，那么匹配工厂类
            if (isTypeMatch(beanName, type)) {
                result.add(beanName);
            }
        }
        catch (NoSuchBeanDefinitionException ex) {
            // Shouldn't happen - probably a result of circular reference resolution...
            logger.trace(LogMessage.format("Failed to check manually registered singleton with name '%s'", beanName), ex);
        }
    }

    return StringUtils.toStringArray(result);
}
```

1.3.4. 、tomcat对象的初始化、（ServletWebServerApplicationContext）

```java
private org.springframework.boot.web.servlet.ServletContextInitializer getSelfInitializer() {
		return this::selfInitialize;
	}

private void selfInitialize(ServletContext servletContext) throws ServletException {
    //使用给定的完全加载的servletContext准备WebApplicationContext
	prepareWebApplicationContext(servletContext);
	registerApplicationScope(servletContext);
    //使用给定的BeanFactory注册特定于web的作用域bean（contextParameters,contextAttributes）
	WebApplicationContextUtils.registerEnvironmentBeans(getBeanFactory(), servletContext);
	for (ServletContextInitializer beans : getServletContextInitializerBeans()) {
		beans.onStartup(servletContext);
	}
}
```

1.3.5. 、完成内嵌tomcat的api调用（TomcatServletWebServerFactory）

```java
@Override
public WebServer getWebServer(ServletContextInitializer... initializers) {
	if (this.disableMBeanRegistry) {
		Registry.disableRegistry();
	}
    //完成tomcat的api调用
	Tomcat tomcat = new Tomcat();
	File baseDir = (this.baseDirectory != null) ? this.baseDirectory : createTempDir("tomcat");
	tomcat.setBaseDir(baseDir.getAbsolutePath());
	Connector connector = new Connector(this.protocol);
	connector.setThrowOnFailure(true);
	tomcat.getService().addConnector(connector);
	customizeConnector(connector);
	tomcat.setConnector(connector);
	tomcat.getHost().setAutoDeploy(false);
	configureEngine(tomcat.getEngine());
	for (Connector additionalConnector : this.additionalTomcatConnectors) {
		tomcat.getService().addConnector(additionalConnector);
	}
    //准备tomcatEmbeddedContext并设置到tomcat中
	prepareContext(tomcat.getHost(), initializers);
    //构建tomcatWebServer
	return getTomcatWebServer(tomcat);
}
```

1.3.6. 、获取tomcat服务（TomcatServletWebServerFactory）

```java
protected TomcatWebServer getTomcatWebServer(Tomcat tomcat) {
	return new TomcatWebServer(tomcat, getPort() >= 0);
}
public TomcatWebServer(Tomcat tomcat, boolean autoStart) {
	Assert.notNull(tomcat, "Tomcat Server must not be null");
	this.tomcat = tomcat;
	this.autoStart = autoStart;
    //初始化
	initialize();
}
```

1.3.7. 、完成tomcat的初始化

```java
private void initialize() throws WebServerException {
		logger.info("Tomcat initialized with port(s): " + getPortsDescription(false));
		synchronized (this.monitor) {
			try {
                //engineName拼接instanceId
				addInstanceIdToEngineName();

				Context context = findContext();
				context.addLifecycleListener((event) -> {
					if (context.equals(event.getSource()) && Lifecycle.START_EVENT.equals(event.getType())) {
						// Remove service connectors so that protocol binding doesn't
						// happen when the service is started.
                        //删除Connectors，以便再启动服务时不发生协议绑定
						removeServiceConnectors();
					}
				});

				// Start the server to trigger initialization listeners
                //启动服务触发初始化监听器
				this.tomcat.start();

				// We can re-throw failure exception directly in the main thread
                //在主线程中重新抛出失败异常
				rethrowDeferredStartupExceptions();

				try {
					ContextBindings.bindClassLoader(context, context.getNamingToken(), getClass().getClassLoader());
				}
				catch (NamingException ex) {
					// Naming is not enabled. Continue
				}

				// Unlike Jetty, all Tomcat threads are daemon threads. We create a
				// blocking non-daemon to stop immediate shutdown
                //所有的tomcat线程都是守护线程，我们创建一个阻塞非守护线程来避免立即关闭
				startDaemonAwaitThread();
			}
			catch (Exception ex) {
                //异常停止tomcat
				stopSilently();
				destroySilently();
				throw new WebServerException("Unable to start embedded Tomcat", ex);
			}
		}
	}
-----------------------
    	private void removeServiceConnectors() {
		for (Service service : this.tomcat.getServer().findServices()) {
			Connector[] connectors = service.findConnectors().clone();
            //将将要移除的conntector放到缓存中暂存
			this.serviceConnectors.put(service, connectors);
			for (Connector connector : connectors) {
                //移除connector
				service.removeConnector(connector);
			}
		}
	}
```

1.3.8. 、除了refresh方法之外，在finishRefresh()方法中也对tomcat做了相关的处理（ServletWebServerApplicationContext）

```java
	protected void finishRefresh() {
        //调用父类的finishRefresh方法
		super.finishRefresh();
        //启动webServer
		WebServer webServer = startWebServer();
		if (webServer != null) {
            //发布webServer初始化完成事件
			publishEvent(new ServletWebServerInitializedEvent(webServer, this));
		}
	}
```

1.3.8.0.1. ServletWebServerApplicationContext

```java
	private WebServer startWebServer() {
		WebServer webServer = this.webServer;
		if (webServer != null) {
            //启动webserver
			webServer.start();
		}
		return webServer;
	}
```

**TomcatWebServer**

```java
	public void start() throws WebServerException {
		synchronized (this.monitor) {
			if (this.started) {
				return;
			}
			try {
                //添加之前移除的connector
				addPreviouslyRemovedConnectors();
				Connector connector = this.tomcat.getConnector();
				if (connector != null && this.autoStart) {
                    //延迟加载启动
					performDeferredLoadOnStartup();
				}
                //检查connector启动状态是否为失败，失败抛出异常
				checkThatConnectorsHaveStarted();
				this.started = true;
				logger.info("Tomcat started on port(s): " + getPortsDescription(true) + " with context path '"
						+ getContextPath() + "'");
			}
			catch (ConnectorStartFailedException ex) {
                //异常停止tomcat
				stopSilently();
				throw ex;
			}
			catch (Exception ex) {
				if (findBindException(ex) != null) {
					throw new PortInUseException(this.tomcat.getConnector().getPort());
				}
				throw new WebServerException("Unable to start embedded Tomcat server", ex);
			}
			finally {
				Context context = findContext();
                //context解绑classload
				ContextBindings.unbindClassLoader(context, context.getNamingToken(), getClass().getClassLoader());
			}
		}
	}
```

```java
private void addPreviouslyRemovedConnectors() {
		Service[] services = this.tomcat.getServer().findServices();
		for (Service service : services) {
            //从上面移除connector添加的缓存中取出connector
			Connector[] connectors = this.serviceConnectors.get(service);
			if (connectors != null) {
				for (Connector connector : connectors) {
                    //添加到tomcat service中
					service.addConnector(connector);
					if (!this.autoStart) {
                        //如果不是自动启动，则暂停connector
						stopProtocolHandler(connector);
					}
				}
                //添加完成后移除
				this.serviceConnectors.remove(service);
			}
		}
	}
```

```java
private void performDeferredLoadOnStartup() {
		try {
			for (Container child : this.tomcat.getHost().findChildren()) {
				if (child instanceof TomcatEmbeddedContext) {
                    //延迟加载启动
					((TomcatEmbeddedContext) child).deferredLoadOnStartup();
				}
			}
		}
		catch (Exception ex) {
			if (ex instanceof WebServerException) {
				throw (WebServerException) ex;
			}
			throw new WebServerException("Unable to start embedded Tomcat connectors", ex);
		}
	}
```

```java
	void deferredLoadOnStartup() throws LifecycleException {
		doWithThreadContextClassLoader(getLoader().getClassLoader(),
				() -> getLoadOnStartupWrappers(findChildren()).forEach(this::load));
	}
```

1.3.9. 、应用上下文关闭时会调用tomcat的关闭

在refreshContext中注册一个关闭的钩子函数，而钩子函数可以完成关闭的功能

**ServletWebServerApplicationContext**

```java
	@Override
	protected void onClose() {
		super.onClose();
		stopAndReleaseWebServer();
	}
```

```java
	private void stopAndReleaseWebServer() {
		WebServer webServer = this.webServer;
		if (webServer != null) {
			try {
				webServer.stop();
				this.webServer = null;
			}
			catch (Exception ex) {
				throw new IllegalStateException(ex);
			}
		}
	}
```

**TomcatWebServer**  

```java
@Override
	public void stop() throws WebServerException {
		synchronized (this.monitor) {
			boolean wasStarted = this.started;
			try {
				this.started = false;
				try {
					stopTomcat();
					this.tomcat.destroy();
				}
				catch (LifecycleException ex) {
					// swallow and continue
				}
			}
			catch (Exception ex) {
				throw new WebServerException("Unable to stop embedded Tomcat", ex);
			}
			finally {
				if (wasStarted) {
					containerCounter.decrementAndGet();
				}
			}
		}
	}
```
-->
