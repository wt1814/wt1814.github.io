
<!-- TOC -->

- [1. InitializingBean接口、DisposableBean接口](#1-initializingbean接口disposablebean接口)
    - [1.1. InitializingBean接口](#11-initializingbean接口)
    - [1.2. DisposableBean接口](#12-disposablebean接口)

<!-- /TOC -->

# 1. InitializingBean接口、DisposableBean接口  
## 1.1. InitializingBean接口  
&emsp; 当需要在bean的全部属性设置成功后做些特殊的处理，可以让该bean实现InitializingBean接口。效果等同于bean的init-method属性的使用或者@PostContsuct注解的使用，它只包括afterPropertiesSet方法，凡是继承该接口的类，在初始化bean的时候都会执行该方法。  

## 1.2. DisposableBean接口  
&emsp; 当需要在bean销毁之前做些特殊的处理，可以让该bean实现DisposableBean接口，该接口也只定义了一个destory方法。效果等同于bean的destroy-method属性的使用或者@PreDestory注解的使用。  
&emsp; 注解、DisposableBean、destroy-method三种方式的执行顺序：先注解，然后执行DisposableBean接口中定义的方法，最后执行destroy-method属性指定的方法。 



