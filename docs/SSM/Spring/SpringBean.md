---
title: SpringBean生命周期 
date: 2020-04-08 00:00:00
tags:
    - Spring
---

<!-- TOC -->

- [1. SpringBean生命周期的概要流程](#1-springbean生命周期的概要流程)
- [2. 实例演示](#2-实例演示)
- [3. 源码解析](#3-源码解析)
- [4. 总结](#4-总结)

<!-- /TOC -->

# 1. SpringBean生命周期的概要流程  
&emsp; **<font color = "red">SpringIOC阶段初始化容器，为SpringBean的生命周期提供环境准备。SpringDI阶段讲述了SpringBean生命周期的前半部分。</font>**  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Spring/spring-11.png)  
1. Bean容器在配置文件中找到Spring Bean的定义。
2. Bean容器使用Java Reflection API创建Bean的实例。
3. 如果声明了任何属性，声明的属性会被设置。如果属性本身是Bean，则将对其进行解析和设置。
4. 如果Bean类实现BeanNameAware接口，则将通过传递Bean的名称来调用setBeanName()方法。
5. 如果Bean类实现BeanClassLoaderAware接口，则将通过传递加载此Bean的ClassLoader对象的实例来调用setBeanClassLoader()方法。
6. 如果Bean类实现BeanFactoryAware接口，则将通过传递BeanFactory对象的实例来调用setBeanFactory()方法。
7. 如果有任何与BeanFactory关联的BeanPostProcessors对象已加载Bean，则将在设置Bean属性之前调用postProcessBeforeInitialization()方法。
8. 如果Bean类实现了InitializingBean接口，则在设置了配置文件中定义的所有Bean属性后，将调用afterPropertiesSet()方法。
9. 如果配置文件中的Bean定义包含init-method属性，则该属性的值将解析为Bean类中的方法名称，并将调用该方法。
10. 如果为Bean Factory对象附加了任何Bean 后置处理器，则将调用postProcessAfterInitialization()方法。
11. 如果Bean类实现DisposableBean接口，则当Application不再需要Bean引用时，将调用destroy()方法。
12. 如果配置文件中的Bean定义包含destroy-method属性，那么将调用Bean类中的相应方法定义。

# 2. 实例演示  
<!-- https://mp.weixin.qq.com/s/feokfxcB1WCMRAqVRm9HOQ -->

# 3. 源码解析  
&emsp; Bean的生命周期概括起来就是4个阶段：  
1. 实例化（Instantiation）；
2. 属性赋值（Populate）；
3. 初始化（Initialization）；
4. 销毁（Destruction）。  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/Spring/spring-10.png)  
1. 实例化：第 1 步，实例化一个 bean 对象；
2. 属性赋值：第 2 步，为 bean 设置相关属性和依赖；
3. 初始化：第 3~7 步，步骤较多，其中第 5、6 步为初始化操作，第 3、4 步为在初始化前执行，第 7 步在初始化后执行，该阶段结束，才能被用户使用；
4. 销毁：第 8~10步，第8步不是真正意义上的销毁（还没使用呢），而是先在使用前注册了销毁的相关调用接口，为了后面第9、10步真正销毁 bean 时再执行相应的方法。

&emsp; 在doCreateBean()方法中能看到依次执行了这 4 个阶段：  

```java
// AbstractAutowireCapableBeanFactory.java
protected Object doCreateBean(final String beanName, final RootBeanDefinition mbd, final @Nullable Object[] args)
        throws BeanCreationException {

    // 1. 实例化
    BeanWrapper instanceWrapper = null;
    if (instanceWrapper == null) {
        instanceWrapper = createBeanInstance(beanName, mbd, args);
    }

    Object exposedObject = bean;
    try {
        // 2. 属性赋值
        populateBean(beanName, mbd, instanceWrapper);
        // 3. 初始化
        exposedObject = initializeBean(beanName, exposedObject, mbd);
    }

    // 4. 销毁-注册回调接口
    try {
        registerDisposableBeanIfNecessary(beanName, bean, mbd);
    }
    return exposedObject;
}
```
&emsp; 初始化包含了第 3~7步，较复杂，所以进到 initializeBean() 方法里具体看下其过程（注释的序号对应图中序号）：  

```java
// AbstractAutowireCapableBeanFactory.java
protected Object initializeBean(final String beanName, final Object bean, @Nullable RootBeanDefinition mbd) {
    // 3. 检查 Aware 相关接口并设置相关依赖
    if (System.getSecurityManager() != null) {
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            invokeAwareMethods(beanName, bean);
            return null;

        }, getAccessControlContext());
    }
    else {
        invokeAwareMethods(beanName, bean);
    }

    // 4. BeanPostProcessor 前置处理
    Object wrappedBean = bean;
    if (mbd == null || !mbd.isSynthetic()) {
        wrappedBean = applyBeanPostProcessorsBeforeInitialization(wrappedBean, beanName);
    }

    // 5. 若实现 InitializingBean 接口，调用 afterPropertiesSet() 方法
    // 6. 若配置自定义的 init-method方法，则执行
    try {
        invokeInitMethods(beanName, wrappedBean, mbd);
    }
    catch (Throwable ex) {
        throw new BeanCreationException(
                (mbd != null ? mbd.getResourceDescription() : null),
                beanName, "Invocation of init method failed", ex);
    }

    // 7. BeanPostProceesor 后置处理
    if (mbd == null || !mbd.isSynthetic()) {
        wrappedBean = applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
    }
    return wrappedBean;
}
```
&emsp; 在 invokInitMethods() 方法中会检查 InitializingBean 接口和 init-method 方法，销毁的过程也与其类似：  

```java
// DisposableBeanAdapter.java
public void destroy() {

    // 9. 若实现 DisposableBean 接口，则执行 destory()方法
    if (this.invokeDisposableBean) {
        try {
            if (System.getSecurityManager() != null) {
                AccessController.doPrivileged((PrivilegedExceptionAction<Object>) () -> {
                    ((DisposableBean) this.bean).destroy();
                    return null;
                }, this.acc);
            }
            else {
                ((DisposableBean) this.bean).destroy();
            }
        }
    }

    // 10. 若配置自定义的 detory-method 方法，则执行
    if (this.destroyMethod != null) {
        invokeCustomDestroyMethod(this.destroyMethod);
    }
    else if (this.destroyMethodName != null) {
        Method methodToInvoke = determineDestroyMethod(this.destroyMethodName);
        if (methodToInvoke != null) {
            invokeCustomDestroyMethod(ClassUtils.getInterfaceMethodIfPossible(methodToInvoke));
        }
    }
}
```
&emsp; 从Spring的源码可以直观的看到其执行过程，而记忆其过程便可以从这 4 个阶段出发，实例化、属性赋值、初始化、销毁。其中细节较多的便是初始化，涉及了Aware、BeanPostProcessor、InitializingBean、init-method 的概念。这些都是Spring提供的扩展点。    

# 4. 总结
&emsp; 最后总结下如何记忆 Spring Bean 的生命周期：  

* 首先是实例化、属性赋值、初始化、销毁这 4 个大阶段；
* 再是初始化的具体操作，有 Aware 接口的依赖注入、BeanPostProcessor 在初始化前后的处理以及 InitializingBean 和 init-method 的初始化操作；
* 销毁的具体操作，有注册相关销毁回调接口，最后通过DisposableBean 和 destory-method 进行销毁。
