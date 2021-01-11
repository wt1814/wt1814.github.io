

<!-- TOC -->

- [1. 容器初始化详解](#1-容器初始化详解)
    - [1.1. 容器初始化时序图](#11-容器初始化时序图)
    - [1.2. 创建容器](#12-创建容器)
    - [1.3. 载入配置路径](#13-载入配置路径)
    - [1.4. 分配路径处理策略](#14-分配路径处理策略)
    - [1.5. 将配置载入内存](#15-将配置载入内存)
    - [1.6. 载入<bean>元素](#16-载入bean元素)
    - [1.7. 载入<property>元素](#17-载入property元素)
    - [1.8. 载入<property>的子元素](#18-载入property的子元素)
    - [1.9. 载入<list>的子元素](#19-载入list的子元素)
    - [1.10. 分配注册策略](#110-分配注册策略)
    - [1.11. 向容器注册](#111-向容器注册)

<!-- /TOC -->

# 1. 容器初始化详解  
&emsp; **<font color = "lime">DefaultListableBeanFactory中使用一个HashMap的集合对象存放IOC容器中注册解析的BeanDefinition。</font>**  

```java
private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);
```


## 1.1. 容器初始化时序图  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/sourceCode/Spring/Spring-1.png)  

## 1.2. 创建容器  
&emsp; AbstractApplicationContext#obtainFreshBeanFactory()方法调用子类容器的refreshBeanFactory()方法，启动容器载入Bean配置信息的过程，代码如下：  

```java
protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {
    //这里使用了委派设计模式，父类定义了抽象的refreshBeanFactory()方法，具体实现调用子类容器的refreshBeanFactory()方法
    this.refreshBeanFactory();
    ConfigurableListableBeanFactory beanFactory = this.getBeanFactory();
    if (this.logger.isDebugEnabled()) {
        this.logger.debug("Bean factory for " + this.getDisplayName() + ": " + beanFactory);
    }
    return beanFactory;
}
```
&emsp; AbstractApplicationContext类中只抽象定义了refreshBeanFactory()方法，容器真正调用的是其子类AbstractRefreshableApplicationContext实现的refreshBeanFactory()方法，方法的源码如下：  

```java
protected final void refreshBeanFactory() throws BeansException {
    //如果已经有容器，销毁容器中的 bean，关闭容器
    if (hasBeanFactory()) {
        destroyBeans(); closeBeanFactory();
    }
    try {
        //创建 IOC 容器
        DefaultListableBeanFactory beanFactory = createBeanFactory();
        beanFactory.setSerializationId(getId());
        //对 IOC 容器进行定制化，如设置启动参数，开启注解的自动装配等
        customizeBeanFactory(beanFactory);
        //调用载入 Bean 定义的方法，主要这里又使用了一个委派模式，在当前类中只定义了抽象的 loadBeanDefinitions 方法，具体 的实现调用子类容器
        loadBeanDefinitions(beanFactory);
        synchronized (this.beanFactoryMonitor) {
            this.beanFactory = beanFactory;
        } 
    }catch (IOException ex) { 
        throw new ApplicationContextException("I/O error parsing bean definition source for " + getDisplayName(), ex);
    } 
}
```
&emsp; 在这个方法中，先判断BeanFactory是否存在，如果存在则先销毁beans并关闭 beanFactory，接着创建 DefaultListableBeanFactory，并调用loadBeanDefinitions(beanFactory)装载bean定义。  

## 1.3. 载入配置路径  
&emsp; AbstractRefreshableApplicationContext 中只定义了抽象的 loadBeanDefinitions 方法，容器真正调用的是其子类 AbstractXmlApplicationContext 对该方法的实现，AbstractXmlApplicationContext 的主要源码如下：  
&emsp; loadBeanDefinitions() 方 法 同 样 是 抽 象 方 法 ， 是 由 其 子 类 实 现 的 ， 也 即 在 AbstractXmlApplicationContext 中。  

```java
public abstract class AbstractXmlApplicationContext extends AbstractRefreshableConfigApplicationContext {
    //...
    // 实现父类抽象的载入 Bean 定义方法
    @Override
    protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
        //创建 XmlBeanDefinitionReader，即创建 Bean 读取器，并通过回调设置到容器中去，容器使用该读取器读取 Bean 配置资源
        XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
        //为 Bean 读取器设置 Spring 资源加载器，AbstractXmlApplicationContext 的
        //祖先父类 AbstractApplicationContext 继承 DefaultResourceLoader，因此，容器本身也是一个资源加载器
        beanDefinitionReader.setEnvironment(this.getEnvironment());
        beanDefinitionReader.setResourceLoader(this);
        //为 Bean 读取器设置 SAX xml 解析器
        beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));
        //当 Bean 读取器读取 Bean 定义的 Xml 资源文件时，启用 Xml 的校验机制
        initBeanDefinitionReader(beanDefinitionReader);
        //Bean 读取器真正实现加载的方法
        loadBeanDefinitions(beanDefinitionReader);
    }

    protected void initBeanDefinitionReader(XmlBeanDefinitionReader reader) {
        reader.setValidating(this.validating);
    }

    //Xml Bean 读取器加载 Bean 配置资源
    protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws BeansException, IOException {
        //获取 Bean 配置资源的定位
        Resource[] configResources = getConfigResources();
        if (configResources != null) {
            //Xml Bean 读取器调用其父类 AbstractBeanDefinitionReader 读取定位的 Bean 配置资源
            reader.loadBeanDefinitions(configResources);
        }
        // 如果子类中获取的 Bean 配置资源定位为空，则获取 ClassPathXmlApplicationContext
        // 构造方法中 setConfigLocations 方法设置的资源
        String[] configLocations = getConfigLocations();
        if (configLocations != null) {
            //Xml Bean 读取器调用其父类 AbstractBeanDefinitionReader 读取定位的 Bean 配置资源
            reader.loadBeanDefinitions(configLocations);
        }
    }

    //这里又使用了一个委托模式，调用子类的获取 Bean 配置资源定位的方法
    // 该方法在 ClassPathXmlApplicationContext 中进行实现，对于我们
    // 举例分析源码的 ClassPathXmlApplicationContext 没有使用该方法 
    @Nullable
    protected Resource[] getConfigResources() {
        return null;
    }
}
```
&emsp; 以 XmlBean 读取器的其中一种策略 XmlBeanDefinitionReader 为例。XmlBeanDefinitionReader 调 用其父类AbstractBeanDefinitionReader的 reader.loadBeanDefinitions()方法读取Bean配置资源。 由于我们使用 ClassPathXmlApplicationContext 作为例子分析，因此 getConfigResources 的返回值 为 null，因此程序执行 reader.loadBeanDefinitions(configLocations)分支。  

## 1.4. 分配路径处理策略  
&emsp; 在 XmlBeanDefinitionReader 的抽象父类 AbstractBeanDefinitionReader 中定义了载入过程。  
&emsp; AbstractBeanDefinitionReader 的 loadBeanDefinitions()方法源码如下：  

```java
//重载方法，调用下面的 loadBeanDefinitions(String, Set<Resource>);方法
@Override
public int loadBeanDefinitions(String location) throws BeanDefinitionStoreException {
    return loadBeanDefinitions(location, null);
}

public int loadBeanDefinitions(String location, @Nullable Set<Resource> actualResources) throws BeanDefinitionStoreException {
    //获取在 IOC 容器初始化过程中设置的资源加载器
    ResourceLoader resourceLoader = getResourceLoader();
    if (resourceLoader == null) {
        throw new BeanDefinitionStoreException( "Cannot import bean definitions from location [" + location + "]: no ResourceLoader available");
    }
    if (resourceLoader instanceof ResourcePatternResolver) {
        // Resource pattern matching available.
        try {
            //将指定位置的 Bean 配置信息解析为 Spring IOC 容器封装的资源
            // 加载多个指定位置的 Bean 配置信息
            Resource[] resources = ((ResourcePatternResolver) resourceLoader).getResources(location);
            //委派调用其子类 XmlBeanDefinitionReader 的方法，实现加载功能
            int loadCount = loadBeanDefinitions(resources);
            if (actualResources != null) {
                for (Resource resource : resources) {
                    actualResources.add(resource);
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Loaded " + loadCount + " bean definitions from location pattern [" + location + "]");
            }
            return loadCount;
        }catch (IOException ex) {
            throw new BeanDefinitionStoreException( "Could not resolve bean definition resource pattern [" + location + "]", ex);
        }
    }else {
        // Can only load single resources by absolute URL.
        // 将指定位置的 Bean 配置信息解析为 Spring IOC 容器封装的资源
        // 加载单个指定位置的 Bean 配置信息
        Resource resource = resourceLoader.getResource(location);
        //委派调用其子类 XmlBeanDefinitionReader 的方法，实现加载功能
        int loadCount = loadBeanDefinitions(resource);
        if (actualResources != null) {
            actualResources.add(resource);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Loaded " + loadCount + " bean definitions from location [" + location + "]");
        }return loadCount;
    }
}

//重载方法，调用 loadBeanDefinitions(String);
@Override
public int loadBeanDefinitions(String... locations) throws BeanDefinitionStoreException {
    Assert.notNull(locations, "Location array must not be null");
    int counter = 0;
    for (String location : locations) {
        counter += loadBeanDefinitions(location);
    }
    return counter;
}
```
&emsp; AbstractRefreshableConfigApplicationContext的loadBeanDefinitions(Resource...resources)方法实际上是调用 AbstractBeanDefinitionReader的loadBeanDefinitions()方法。  
&emsp; 从对AbstractBeanDefinitionReader的loadBeanDefinitions()方法源码分析可以看出该方法就做了两件事：   
&emsp; 首先，调用资源加载器的获取资源方法 resourceLoader.getResource(location)，获取到要加载的资源。 其次，真正执行加载功能是其子类 XmlBeanDefinitionReader的loadBeanDefinitions()方法。在loadBeanDefinitions()方法中调用了AbstractApplicationContext的getResources()方法，跟进去之后发现getResources()方法其实定义在ResourcePatternResolver中，此时，有必要来看一下ResourcePatternResolver的全类图：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Spring/spring-12.png)  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Spring/spring-13.png)  
&emsp; 从上面可以看到ResourceLoader与ApplicationContext的继承关系，可以看出其实际调用的是DefaultResourceLoader中的getSource()方法定位Resource，因为ClassPathXmlApplicationContext本身就是DefaultResourceLoader的实现类，所以此时又回到了ClassPathXmlApplicationContext中来。  

## 1.5. 将配置载入内存  
&emsp; BeanDefinitionDocumentReader接口通过registerBeanDefinitions()方法调用其实现类DefaultBeanDefinitionDocumentReader对Document对象进行解析，解析的代码如下：  

```java
public void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) {
    this.readerContext = readerContext;
    this.logger.debug("Loading bean definitions");
    Element root = doc.getDocumentElement();
    this.doRegisterBeanDefinitions(root);
}

protected void doRegisterBeanDefinitions(Element root) {
    BeanDefinitionParserDelegate parent = this.delegate;
    this.delegate = this.createDelegate(this.getReaderContext(), root, parent);
    if (this.delegate.isDefaultNamespace(root)) {
        String profileSpec = root.getAttribute("profile");
        if (StringUtils.hasText(profileSpec)) {
            String[] specifiedProfiles = StringUtils.tokenizeToStringArray(profileSpec, ",; ");
            if (!this.getReaderContext().getEnvironment().acceptsProfiles(specifiedProfiles)) {
                if (this.logger.isInfoEnabled()) {
                    this.logger.info("Skipped XML bean definition file due to specified profiles [" + profileSpec + "] not matching: " + this.getReaderContext().getResource());
                }

                return;
            }
        }
    }

    this.preProcessXml(root);
    this.parseBeanDefinitions(root, this.delegate);
    this.postProcessXml(root);
    this.delegate = parent;
}

protected BeanDefinitionParserDelegate createDelegate(XmlReaderContext readerContext, Element root, @Nullable BeanDefinitionParserDelegate parentDelegate) {
    BeanDefinitionParserDelegate delegate = new BeanDefinitionParserDelegate(readerContext);
    delegate.initDefaults(root, parentDelegate);
    return delegate;
}

protected void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) {
    if (delegate.isDefaultNamespace(root)) {
        NodeList nl = root.getChildNodes();

        for(int i = 0; i < nl.getLength(); ++i) {
            Node node = nl.item(i);
            if (node instanceof Element) {
                Element ele = (Element)node;
                if (delegate.isDefaultNamespace(ele)) {
                    this.parseDefaultElement(ele, delegate);
                } else {
                    delegate.parseCustomElement(ele);
                }
            }
        }
    } else {
        delegate.parseCustomElement(root);
    }

}

private void parseDefaultElement(Element ele, BeanDefinitionParserDelegate delegate) {
    if (delegate.nodeNameEquals(ele, "import")) {
        this.importBeanDefinitionResource(ele);
    } else if (delegate.nodeNameEquals(ele, "alias")) {
        this.processAliasRegistration(ele);
    } else if (delegate.nodeNameEquals(ele, "bean")) {
        this.processBeanDefinition(ele, delegate);
    } else if (delegate.nodeNameEquals(ele, "beans")) {
        this.doRegisterBeanDefinitions(ele);
    }

}

protected void importBeanDefinitionResource(Element ele) {
    String location = ele.getAttribute("resource");
    if (!StringUtils.hasText(location)) {
        this.getReaderContext().error("Resource location must not be empty", ele);
    } else {
        location = this.getReaderContext().getEnvironment().resolveRequiredPlaceholders(location);
        Set<Resource> actualResources = new LinkedHashSet(4);
        boolean absoluteLocation = false;

        try {
            absoluteLocation = ResourcePatternUtils.isUrl(location) || ResourceUtils.toURI(location).isAbsolute();
        } catch (URISyntaxException var11) {
        }

        int importCount;
        if (absoluteLocation) {
            try {
                importCount = this.getReaderContext().getReader().loadBeanDefinitions(location, actualResources);
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Imported " + importCount + " bean definitions from URL location [" + location + "]");
                }
            } catch (BeanDefinitionStoreException var10) {
                this.getReaderContext().error("Failed to import bean definitions from URL location [" + location + "]", ele, var10);
            }
        } else {
            try {
                Resource relativeResource = this.getReaderContext().getResource().createRelative(location);
                if (relativeResource.exists()) {
                    importCount = this.getReaderContext().getReader().loadBeanDefinitions(relativeResource);
                    actualResources.add(relativeResource);
                } else {
                    String baseLocation = this.getReaderContext().getResource().getURL().toString();
                    importCount = this.getReaderContext().getReader().loadBeanDefinitions(StringUtils.applyRelativePath(baseLocation, location), actualResources);
                }

                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Imported " + importCount + " bean definitions from relative location [" + location + "]");
                }
            } catch (IOException var8) {
                this.getReaderContext().error("Failed to resolve current resource location", ele, var8);
            } catch (BeanDefinitionStoreException var9) {
                this.getReaderContext().error("Failed to import bean definitions from relative location [" + location + "]", ele, var9);
            }
        }

        Resource[] actResArray = (Resource[])actualResources.toArray(new Resource[0]);
        this.getReaderContext().fireImportProcessed(location, actResArray, this.extractSource(ele));
    }
}

protected void processAliasRegistration(Element ele) {
    String name = ele.getAttribute("name");
    String alias = ele.getAttribute("alias");
    boolean valid = true;
    if (!StringUtils.hasText(name)) {
        this.getReaderContext().error("Name must not be empty", ele);
        valid = false;
    }

    if (!StringUtils.hasText(alias)) {
        this.getReaderContext().error("Alias must not be empty", ele);
        valid = false;
    }

    if (valid) {
        try {
            this.getReaderContext().getRegistry().registerAlias(name, alias);
        } catch (Exception var6) {
            this.getReaderContext().error("Failed to register alias '" + alias + "' for bean with name '" + name + "'", ele, var6);
        }

        this.getReaderContext().fireAliasRegistered(name, alias, this.extractSource(ele));
    }

}

protected void processBeanDefinition(Element ele, BeanDefinitionParserDelegate delegate) {
    BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement(ele);
    if (bdHolder != null) {
        bdHolder = delegate.decorateBeanDefinitionIfRequired(ele, bdHolder);

        try {
            BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder, this.getReaderContext().getRegistry());
        } catch (BeanDefinitionStoreException var5) {
            this.getReaderContext().error("Failed to register bean definition with name '" + bdHolder.getBeanName() + "'", ele, var5);
        }

        this.getReaderContext().fireComponentRegistered(new BeanComponentDefinition(bdHolder));
    }

}
```
&emsp; 通过上述SpringIOC容器对载入的Bean定义Document解析可以看出，使用Spring时，在Spring配置文件中可以使用<import\>元素来导入IOC容器所需要的其他资源，Spring IOC容器在解析时会首先将指定导入的资源加载进容器中。使用<ailas\>别名时，Spring IOC容器首先将别名元素所定义的别名注册到容器中。  
&emsp; 对于既不是<import\>元素，又不是<alias\>元素的元素，即Spring配置文件中普通的<bean\>元素的解析由BeanDefinitionParserDelegate类的 parseBeanDefinitionElement()方法来实现。  

## 1.6. 载入<bean>元素
......

## 1.7. 载入<property>元素  
......

## 1.8. 载入<property>的子元素  
......

## 1.9. 载入<list>的子元素  
......

## 1.10. 分配注册策略  
&emsp; 让我们继续跟踪程序的执行顺序，接下来我们来分析DefaultBeanDefinitionDocumentReader对Bean定义转换的Document对象解析的流程中，在其 parseDefaultElement()方法中完成对Document对象的解析后得到封装BeanDefinition的BeanDefinitionHold对象，然后调用BeanDefinitionReaderUtils的 registerBeanDefinition()方法向IOC容器注册解析的Bean，BeanDefinitionReaderUtils的注册的源码如下：  

```java
//将解析的 BeanDefinitionHold 注册到容器中
public static void registerBeanDefinition(BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry) throws BeanDefinitionStoreException {
    //获取解析的 BeanDefinition 的名称
    String beanName = definitionHolder.getBeanName();
    //向 IOC 容器注册 BeanDefinition
    registry.registerBeanDefinition(beanName, definitionHolder.getBeanDefinition());
    //如果解析的 BeanDefinition 有别名，向容器为其注册别名
    String[] aliases = definitionHolder.getAliases();
    if (aliases != null) {
        String[] var4 = aliases;
        int var5 = aliases.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            String alias = var4[var6];
            registry.registerAlias(beanName, alias);
        }
    }

}
```
&emsp; 当调用BeanDefinitionReaderUtils向IOC容器注册解析的BeanDefinition时，真正完成注册功能的是DefaultListableBeanFactory。  

## 1.11. 向容器注册  
&emsp; DefaultListableBeanFactory中使用一个HashMap的集合对象存放IOC容器中注册解析的BeanDefinition，向IOC容器注册的主要源码如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Spring/spring-15.png)  

```java
//DefaultListableBeanFactory.java

//存储注册信息的 BeanDefinition
private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);

//向 IOC 容器注册解析的 BeanDefiniton
public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeanDefinitionStoreException {
    Assert.hasText(beanName, "Bean name must not be empty");
    Assert.notNull(beanDefinition, "BeanDefinition must not be null");
    //校验解析的 BeanDefiniton
    if (beanDefinition instanceof AbstractBeanDefinition) {
        try {
            ((AbstractBeanDefinition)beanDefinition).validate();
        } catch (BeanDefinitionValidationException var9) {
            throw new BeanDefinitionStoreException(beanDefinition.getResourceDescription(), beanName, "Validation of bean definition failed", var9);
        }
    }

    BeanDefinition existingDefinition = (BeanDefinition)this.beanDefinitionMap.get(beanName);
    if (existingDefinition != null) {
        if (!this.isAllowBeanDefinitionOverriding()) {
            throw new BeanDefinitionStoreException(beanDefinition.getResourceDescription(), beanName, "Cannot register bean definition [" + beanDefinition + "] for bean '" + beanName + "': There is already [" + existingDefinition + "] bound.");
        }

        if (existingDefinition.getRole() < beanDefinition.getRole()) {
            if (this.logger.isWarnEnabled()) {
                this.logger.warn("Overriding user-defined bean definition for bean '" + beanName + "' with a framework-generated bean definition: replacing [" + existingDefinition + "] with [" + beanDefinition + "]");
            }
        } else if (!beanDefinition.equals(existingDefinition)) {
            if (this.logger.isInfoEnabled()) {
                this.logger.info("Overriding bean definition for bean '" + beanName + "' with a different definition: replacing [" + existingDefinition + "] with [" + beanDefinition + "]");
            }
        } else if (this.logger.isDebugEnabled()) {
            this.logger.debug("Overriding bean definition for bean '" + beanName + "' with an equivalent definition: replacing [" + existingDefinition + "] with [" + beanDefinition + "]");
        }

        this.beanDefinitionMap.put(beanName, beanDefinition);
    } else {
        if (this.hasBeanCreationStarted()) {
            //注册的过程中需要线程同步，以保证数据的一致性
            synchronized(this.beanDefinitionMap) {
                this.beanDefinitionMap.put(beanName, beanDefinition);
                List<String> updatedDefinitions = new ArrayList(this.beanDefinitionNames.size() + 1);
                updatedDefinitions.addAll(this.beanDefinitionNames);
                updatedDefinitions.add(beanName);
                this.beanDefinitionNames = updatedDefinitions;
                if (this.manualSingletonNames.contains(beanName)) {
                    Set<String> updatedSingletons = new LinkedHashSet(this.manualSingletonNames);
                    updatedSingletons.remove(beanName);
                    this.manualSingletonNames = updatedSingletons;
                }
            }
        } else {
            this.beanDefinitionMap.put(beanName, beanDefinition);
            this.beanDefinitionNames.add(beanName);
            this.manualSingletonNames.remove(beanName);
        }

        this.frozenBeanDefinitionNames = null;
    }
    //检查是否有同名的 BeanDefinition 已经在 IOC 容器中注册
    if (existingDefinition != null || this.containsSingleton(beanName)) {
        //重置所有已经注册过的 BeanDefinition 的缓存
        this.resetBeanDefinition(beanName);
    }

}
```
&emsp; 至此，Bean配置信息中配置的Bean被解析过后，已经注册到IOC容器中，被容器管理起来，真正完成了IOC容器初始化所做的全部工作。现在IOC容器中已经建立了整个Bean的配置信息，这些BeanDefinition信息已经可以使用，并且可以被检索，IOC容器的作用就是对这些注册的 Bean定义信息进行处理和维护。这些的注册的Bean定义信息是IOC容器控制反转的基础，正是有了这些注册的数据，容器才可以进行依赖注入。  



