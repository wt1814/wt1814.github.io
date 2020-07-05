---
title: lazy-init
date: 2020-04-08 00:00:00
tags:
    - Spring
---

<!-- TOC -->

- [1. lazy-init属性和预实例化](#1-lazy-init属性和预实例化)

<!-- /TOC -->

# 1. lazy-init属性和预实例化  
&emsp; 在IOC容器的初始化过程中，主要的工作是对BeanDefinition的定位、载入、解析和注册。此时依赖注入并没有发生，依赖注入发生在应用第一次向容器索要Bean时。 向容器索要Bean是通过getBean的调用来完成的，该getBean是容器提供Bean服务的最基本的接口。  
&emsp; 对于容器的初始化，也有一种例外情况，就是用户可以通过设置Bean的lazy-init属性来控制预实例化的过程。这个预实例化在初始化容器时完成Bean的 依赖注入。毫无疑问，这种容器的使用方式会对容器初始化的性能有一些影响，但却能够提 高应用第一次取得Bean的性能。因为应用在第一次取得Bean时，依赖注入已经结束了，应用 可以取得已有的Bean。  

&emsp; 对lazy-init这个属性的处理是容器refresh的一部分。在finishBeanFactoryInitialization的方法中，封装了对lazy-init属性的处理，实际的处理是在DefaultListableBeanFactory这个基本容器的prelnstantiateSingletons方法中完成的。 该方法对单件Bean完成预实例化，这个预实例化的完成巧妙地委托给容器来实现。如果需要预实例化，那么就直接在这里采用getBean去触发依赖注入，与正常依赖注入的触发相比，只有触发的时间和场合不同。在这里，依赖注入发生在容器执行refresh的过程中，也就是发生在IOC容器初始化的过程中，而不像一般的依赖注入一样发生在loC容器初始化完成以后, 第一次向容器执行getBean时。源码如下：  

```
public void refresh() throws BeansException, IllegalStateException {
    synchronized(this.startupShutdownMonitor) {
        this.prepareRefresh();
        ConfigurableListableBeanFactory beanFactory = this.obtainFreshBeanFactory();
        this.prepareBeanFactory(beanFactory);

        try {
            this.postProcessBeanFactory(beanFactory);
            this.invokeBeanFactoryPostProcessors(beanFactory);
            this.registerBeanPostProcessors(beanFactory);
            this.initMessageSource();
            this.initApplicationEventMulticaster();
            this.onRefresh();
            this.registerListeners();
            //这里是对lazy-init属性进行处理的地方
            this.finishBeanFactoryInitialization(beanFactory);
            this.finishRefresh();
        } catch (BeansException var9) {
            if (this.logger.isWarnEnabled()) {
                this.logger.warn("Exception encountered during context initialization - cancelling refresh attempt: " + var9);
            }

            this.destroyBeans();
            this.cancelRefresh(var9);
            throw var9;
        } finally {
            this.resetCommonCaches();
        }

    }
}

//在finishBeanFactoryInitialization中进行具体的处理过程
protected void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) {
    if (beanFactory.containsBean("conversionService") && beanFactory.isTypeMatch("conversionService", ConversionService.class)) {
        beanFactory.setConversionService((ConversionService)beanFactory.getBean("conversionService", ConversionService.class));
    }

    if (!beanFactory.hasEmbeddedValueResolver()) {
        beanFactory.addEmbeddedValueResolver((strVal) -> {
            return this.getEnvironment().resolvePlaceholders(strVal);
        });
    }

    String[] weaverAwareNames = beanFactory.getBeanNamesForType(LoadTimeWeaverAware.class, false, false);
    String[] var3 = weaverAwareNames;
    int var4 = weaverAwareNames.length;

    for(int var5 = 0; var5 < var4; ++var5) {
        String weaverAwareName = var3[var5];
        this.getBean(weaverAwareName);
    }

    beanFactory.setTempClassLoader((ClassLoader)null);
    beanFactory.freezeConfiguration();
    //这里週用的是BeanFactory的 prelnstantiateSingletons, 这个方法是由DefaultListableBeanFactory实现的
    beanFactory.preInstantiateSingletons();
}

//在DefaultListableBeanFactory中的preInstantiateSingletons是这样的
public void preInstantiateSingletons() throws BeansException {
    if (this.logger.isDebugEnabled()) {
        this.logger.debug("Pre-instantiating singletons in " + this);
    }

    List<String> beanNames = new ArrayList(this.beanDefinitionNames);
    Iterator var2 = beanNames.iterator();

    //在这里就开始getBean，也就是触发Bean的依赖注入
    /*这个getBean和前面分析的触发依賴注入的过程是一样的,只是发生的地方不同.
    如果不设置 lazy-init属性，那么这个依赖注入是发生在容器初始化结束以后。第一次向容器发出getBean时，
    如果设置了lazy-init属性，那么依赖注入发生在容器初始化的过程中,会对 beanDefinitionMap中所有的Bean进行依赖注入，
    这样在初始化过程结束以后.容器执行 getBean得到的就是已经准备好的Bean，不需要进行依赖注入*/
    while(true) {
        String beanName;
        Object bean;
        do {
            while(true) {
                RootBeanDefinition bd;
                do {
                    do {
                        do {
                            if (!var2.hasNext()) {
                                var2 = beanNames.iterator();

                                while(var2.hasNext()) {
                                    beanName = (String)var2.next();
                                    Object singletonInstance = this.getSingleton(beanName);
                                    if (singletonInstance instanceof SmartInitializingSingleton) {
                                        SmartInitializingSingleton smartSingleton = (SmartInitializingSingleton)singletonInstance;
                                        if (System.getSecurityManager() != null) {
                                            AccessController.doPrivileged(() -> {
                                                smartSingleton.afterSingletonsInstantiated();
                                                return null;
                                            }, this.getAccessControlContext());
                                        } else {
                                            smartSingleton.afterSingletonsInstantiated();
                                        }
                                    }
                                }

                                return;
                            }

                            beanName = (String)var2.next();
                            bd = this.getMergedLocalBeanDefinition(beanName);
                        } while(bd.isAbstract());
                    } while(!bd.isSingleton());
                } while(bd.isLazyInit());

                if (this.isFactoryBean(beanName)) {
                    bean = this.getBean("&" + beanName);
                    break;
                }

                this.getBean(beanName);
            }
        } while(!(bean instanceof FactoryBean));

        FactoryBean<?> factory = (FactoryBean)bean;
        boolean isEagerInit;
        if (System.getSecurityManager() != null && factory instanceof SmartFactoryBean) {
            SmartFactoryBean var10000 = (SmartFactoryBean)factory;
            ((SmartFactoryBean)factory).getClass();
            isEagerInit = (Boolean)AccessController.doPrivileged(var10000::isEagerInit, this.getAccessControlContext());
        } else {
            isEagerInit = factory instanceof SmartFactoryBean && ((SmartFactoryBean)factory).isEagerInit();
        }

        if (isEagerInit) {
            this.getBean(beanName);
        }
    }
}
```
&emsp; 根据上面的分析得知，可以通过lazy-init属性来对整个IoC容器的初始化和依赖注入过程进行一些简单的控制。这些控制是可以由容器的使用者来决定的，具体来说，可以通过在 BeanDefinition中设置lazy-init属性来进行控制。  


