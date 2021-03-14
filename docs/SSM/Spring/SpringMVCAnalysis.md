

<!-- TOC -->

- [1. SpringMVC解析](#1-springmvc解析)
    - [1.1. SpringMVC的工作流程](#11-springmvc的工作流程)
    - [1.2. SpringMVC源码解析](#12-springmvc源码解析)
        - [1.2.1. 上下文在web容器中的启动](#121-上下文在web容器中的启动)
        - [1.2.2. DispatcherServlet初始化和使用](#122-dispatcherservlet初始化和使用)
            - [1.2.2.1. 初始化阶段](#1221-初始化阶段)
            - [1.2.2.2. 调用阶段](#1222-调用阶段)

<!-- /TOC -->

&emsp; **<font color = "red">总结：</font>**  
&emsp; **SpringMVC的工作流程：**  
1. 找到处理器：前端控制器DispatcherServlet ---> 处理器映射器HandlerMapping ---> 找到处理器Handler  
2. 处理器处理：前端控制器DispatcherServlet ---> 处理器适配器HandlerAdapter ---> 处理器Handler ---> 执行处理器Controller ---> Controller执行完成返回ModelAndView  
3. 返回前端控制器DispatcherServlet ---> 视图解析器ViewReslover  


&emsp; **SpringMVC解析：**  
1. 在SpringMVC.xml中定义一个DispatcherServlet和一个监听器ContextLoaderListener。  
2. 上下文在web容器中的启动：<font color = "red">由ContextLoaderListener启动的上下文为根上下文。在根上下文的基础上，还有一个与Web MVC相关的上下文用来保存控制器(DispatcherServlet)需要的MVC对象，作为根上下文的子上下文，构成一个层次化的上下文体系。</font>  
3. DispatcherServlet初始化和使用：  
    1. 完成Spring MVC的组件的初始化。  
    2. 调用阶段。这一步是由请求触发的。~~参考SPring MVC流程~~  


# 1. SpringMVC解析
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/SpringMVC/mvc-6.png)  

## 1.1. SpringMVC的工作流程  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/SpringMVC/springmvc-2.png)  
1. 用户发送请求至前端控制器DispatcherServlet。  
2. 前端控制器DispatcherServlet收到请求调用处理器映射器HandlerMapping。  
3. <font color = "red">处理器映射器HandlerMapping根据请求url找到具体的处理器Handler，生成处理器对象及处理器拦截器(如果有则生成)一并返回给DispatcherServlet。</font>  
4. <font color = "red">前端控制器DispatcherServlet通过处理器适配器HandlerAdapter调用处理器Handler。</font>  
5. 执行处理器(Controller，也叫后端控制器)。  
6. Controller执行完成返回ModelAndView。  
7. 处理器适配器HandlerAdapter将controller执行结果ModelAndView返回给DispatcherServlet。  
8. DispatcherServlet将ModelAndView传给视图解析器ViewReslover。  
9. 视图解析器ViewReslover解析后返回具体View。  
10. DispatcherServlet对View进行渲染视图(即将模型数据填充至视图中)。  
11. DispatcherServlet响应用户。  

&emsp; **核心组件：**  
1. **<font color = "red">前端控制器DispatcherServlet</font>**  
&emsp; Spring MVC的入口函数。接收请求，响应结果，相当于转发器，中央处理器。有了DispatcherServlet减少了其它组件之间的耦合度。用户请求到达前端控制器，它就相当于MVC模式中的C，DispatcherServlet是整个流程控制的中心，由它调用其它组件处理用户的请求，DispatcherServlet的存在降低了组件之间的耦合性。  
2. **<font color = "red">处理器映射器HandlerMapping</font>**  
&emsp; 根据请求的url查找Handler。HandlerMapping负责根据用户请求找到Handler即处理器(Controller)，SpringMVC提供了不同的映射器实现不同的映射方式，例如：配置文件方式，实现接口方式，注解方式等。  
3. **<font color = "red">处理器适配器HandlAdapter</font>**  
&emsp; 按照特定规则(HandlerAdapter要求的规则)去执行Handler。通过HandlerAdapter对处理器进行执行，这是适配器模式的应用，通过扩展适配器可以对更多类型的处理器进行执行。  
4. **<font color = "red">Handler处理器</font>**  
&emsp; 编写Handler时按照HandlerAdapter的要求去做，这样适配器才可以去正确执行Handler。Handler是继DispatcherServlet前端控制器的后端控制器，在DispatcherServlet 的控制下Handler对具体的用户请求进行处理。由于Handler涉及到具体的用户业务请求，所以一般情况需要工程师根据业务需求开发Handler。  
5. **<font color = "red">ViewResolver视图解析器</font>**  
&emsp; 进行视图解析，根据逻辑视图名解析成真正的视图(View )。View Resolver负责将处理结果生成View视图，View Resolver首先根据逻辑视图名解析成物理视图名即具体的页面地址，再生成View视图对象，最后对View进行渲染将处理结果通过页面展示给用户。SpringMVC框架提供了很多的View视图类型，包括：jstlView、freemarkerView、pdfView等。一般情况下需要通过页面标签或页面模版技术将模型数据通过页面展示给用户，需要由工程师根据业务需求开发具体的页面。  
6. **<font color = "red">视图View</font>**  
&emsp; View是一个接口，实现类支持不同的View类型(jsp、freemarker...)。  

&emsp; 注意：<font color = "red">处理器Handler以及视图层View都是需要手动开发的。其他的一些组件比如：前端控制器DispatcherServlet、处理器映射器HandlerMapping、处理器适配器HandlerAdapter等都是框架提供的，不需要自己手动开发。</font>  

## 1.2. SpringMVC源码解析  
&emsp; 原始SSM项目中可以在web.xml中看到SpringMVC的部署描述。  

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xmlns="http://java.sun.com/xml/ns/javaee"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
         id="WebApp_ID" version="3.0">
    <display-name>test</display-name>
    <!-- 这里配置Spring配置文件的位置，param-name是固定的，
    param-value是文件位置 这个配置可以省略，如果省略，
    系统默认去/WEB-INF/目录下查找applicationContext.xml作为Spring的配置文件 -->
    <context-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>classpath:applicationContext.xml</param-value>
    </context-param>
    <listener>
        <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
    </listener>
    <servlet>
        <servlet-name>springmvc</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
        <init-param>
            <param-name>contextConfigLocation</param-name>
            <param-value>classpath:spring-servlet.xml</param-value>
        </init-param>
    </servlet>
    <servlet-mapping>
        <servlet-name>springmvc</servlet-name>
        <url-pattern>/</url-pattern>
    </servlet-mapping>
    <filter>
        <filter-name>encoding</filter-name>
        <filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>
        <init-param>
            <param-name>encoding</param-name>
            <param-value>UTF-8</param-value>
        </init-param>
        <init-param>
            <param-name>forceRequestEncoding</param-name>
            <param-value>true</param-value>
        </init-param>
        <init-param>
            <param-name>forceResponseEncoding</param-name>
            <param-value>true</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>encoding</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
</web-app>
```
&emsp; 这个部署描述是Spring MVC与web容器的接口部分。<font color = "red">在这个部署文件中会定义一个DispatcherServlet。</font>这个DispatcherServlet是 MVC中很重要的一个类，起着分发请求的作用。<font color = "red">此外还定义一个监听器ContextLoaderListener，它是Spring MVC的启动类</font>，这个监听器是与Web服务器的生命周期相关联的，由ContextLoaderListener监听器负责完成IoC容器在Web环境中的启动工作。  
&emsp; DispatchServlet和ContextLoaderListener提供了在Web容器中对Spring的接口，也就是说，这些接口与Web容器耦合是通过ServletContext来实现的。这个ServletContext为Spring的IoC 容器提供了一个宿主环境，在宿主环境中，Spring MVC建立起一个IoC容器的体系。这个 IoC容器体系是通过ContextLoaderListener的初始化来建立的，在建立IoC容器体系后，把 DispatchServlet作为Spring MVC处理Web请求的转发器建立起来，从而完成响应HTTP请求的准备。有了这些基本配置，建立在IoC容器基础上的Spring MVC就可以正常地发挥作用了。  

### 1.2.1. 上下文在web容器中的启动  
&emsp; IOC容器的启动过程就是建立上下文的过程，该上下文是与ServletContext相伴而生的，同时也是IOC容器在Web应用环境中的具体表现之一。<font color = "red">由ContextLoaderListener启动的上下文为根上下文。在根上下文的基础上，还有一个与Web MVC相关的上下文用来保存控制器(DispatcherServlet)需要的MVC对象，作为根上下文的子上下文，构成一个层次化的上下文体系。</font>在Web容器中启动Spring应用程序时，首先建立根上下文，然后建立这个上下文体系，这个上下文体系的建立是由ContextLoder来完成的，具体过程如下图所示。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/SpringMVC/springmvc-3.png)  

&emsp; ContextLoaderListener监听器的源码：  

```java
package org.springframework.web.context;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ContextLoaderListener extends ContextLoader implements ServletContextListener {
    public ContextLoaderListener() {
    }

    public ContextLoaderListener(WebApplicationContext context) {
        super(context);
    }

    public void contextInitialized(ServletContextEvent event) {
        this.initWebApplicationContext(event.getServletContext());
    }

    public void contextDestroyed(ServletContextEvent event) {
        this.closeWebApplicationContext(event.getServletContext());
        ContextCleanupListener.cleanupAttributes(event.getServletContext());
    }
}
```
1. 继承ContextLoader类，ContextLoader可以在启动时载入IOC容器；
2. 实现ServletContextListener接口，ServletContextListener接口有两个抽象方法，contextInitialized和contextDestroyed。

&emsp; ContextLoaderListener加载并初始化Spring容器的开始，是在contextInitialized()方法中。  

```java
//这里开始对WebApplicationContext进行初始化
public WebApplicationContext initWebApplicationContext(ServletContext servletContext) {
    //判断是否在servletContext中是否已经有根上下文存在
    if (servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE) != null) {
        throw new IllegalStateException("Cannot initialize context because there is already a root application context present - check whether you have multiple ContextLoader* definitions in your web.xml!");
    } else {
        Log logger = LogFactory.getLog(ContextLoader.class);
        servletContext.log("Initializing Spring root WebApplicationContext");
        if (logger.isInfoEnabled()) {
            logger.info("Root WebApplicationContext: initialization started");
        }

        long startTime = System.currentTimeMillis();

        try {
            if (this.context == null) {
                //初始化上下文
                this.context = this.createWebApplicationContext(servletContext);
            }

            if (this.context instanceof ConfigurableWebApplicationContext) {
                ConfigurableWebApplicationContext cwac = (ConfigurableWebApplicationContext)this.context;
                if (!cwac.isActive()) {
                    if (cwac.getParent() == null) {
                        //载入根上下文的双亲上下文
                        ApplicationContext parent = this.loadParentContext(servletContext);
                        cwac.setParent(parent);
                    }

                    this.configureAndRefreshWebApplicationContext(cwac, servletContext);
                }
            }

            servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, this.context);
            ClassLoader ccl = Thread.currentThread().getContextClassLoader();
            if (ccl == ContextLoader.class.getClassLoader()) {
                currentContext = this.context;
            } else if (ccl != null) {
                currentContextPerThread.put(ccl, this.context);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Published root WebApplicationContext as ServletContext attribute with name [" + WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE + "]");
            }

            if (logger.isInfoEnabled()) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                logger.info("Root WebApplicationContext: initialization completed in " + elapsedTime + " ms");
            }

            return this.context;
        } catch (RuntimeException var8) {
            logger.error("Context initialization failed", var8);
            servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, var8);
            throw var8;
        } catch (Error var9) {
            logger.error("Context initialization failed", var9);
            servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, var9);
            throw var9;
        }
    }
}
```

### 1.2.2. DispatcherServlet初始化和使用  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/SSM/SpringMVC/springmvc-4.png)  


#### 1.2.2.1. 初始化阶段  

```java
public class DispatcherServlet extends FrameworkServlet 
```

```java
public abstract class FrameworkServlet extends HttpServletBean implements ApplicationContextAware 
```
&emsp; DispatcherServlet的初始化在HttpServletBean#init()方法中。  

```java
public final void init() throws ServletException {
    if (this.logger.isDebugEnabled()) {
        this.logger.debug("Initializing servlet '" + this.getServletName() + "'");
    }

    PropertyValues pvs = new HttpServletBean.ServletConfigPropertyValues(this.getServletConfig(), this.requiredProperties);
    if (!pvs.isEmpty()) {
        try {
            //定位资源
            BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(this);
            //加载配置信息
            ResourceLoader resourceLoader = new ServletContextResourceLoader(this.getServletContext());
            bw.registerCustomEditor(Resource.class, new ResourceEditor(resourceLoader, this.getEnvironment()));
            this.initBeanWrapper(bw);
            bw.setPropertyValues(pvs, true);
        } catch (BeansException var4) {
            if (this.logger.isErrorEnabled()) {
                this.logger.error("Failed to set bean properties on servlet '" + this.getServletName() + "'", var4);
            }

            throw var4;
        }
    }

    this.initServletBean();
    if (this.logger.isDebugEnabled()) {
        this.logger.debug("Servlet '" + this.getServletName() + "' configured successfully");
    }

}
```
&emsp; init()方法会调用initServletBean()方法。  

```java
protected final void initServletBean() throws ServletException {
    this.getServletContext().log("Initializing Spring FrameworkServlet '" + this.getServletName() + "'");
    if (this.logger.isInfoEnabled()) {
        this.logger.info("FrameworkServlet '" + this.getServletName() + "': initialization started");
    }

    long startTime = System.currentTimeMillis();

    try {
        this.webApplicationContext = this.initWebApplicationContext();
        this.initFrameworkServlet();
    } catch (ServletException var5) {
        this.logger.error("Context initialization failed", var5);
        throw var5;
    } catch (RuntimeException var6) {
        this.logger.error("Context initialization failed", var6);
        throw var6;
    }

    if (this.logger.isInfoEnabled()) {
        long elapsedTime = System.currentTimeMillis() - startTime;
        this.logger.info("FrameworkServlet '" + this.getServletName() + "': initialization completed in " + elapsedTime + " ms");
    }

}
```
&emsp; <font color = "clime">这段代码中最主要的逻辑就是初始化IOC容器。IOC 容器初始化之后，最后又调用了onRefresh()方法。</font>  

```java
protected void onRefresh(ApplicationContext context) {
    this.initStrategies(context);
}
//初始化策略
protected void initStrategies(ApplicationContext context) {
    //多文件上传的组件
    this.initMultipartResolver(context);
    //初始化本地语言环境
    this.initLocaleResolver(context);
    //初始化模板处理器
    this.initThemeResolver(context);
    //handlerMapping
    this.initHandlerMappings(context);
    //初始化参数适配器
    this.initHandlerAdapters(context);
    //初始化异常拦截器
    this.initHandlerExceptionResolvers(context);
    //初始化视图预处理器
    this.initRequestToViewNameTranslator(context);
    //初始化视图转换器
    this.initViewResolvers(context);
    //FlashMap 管理器
    this.initFlashMapManager(context);
}
```
&emsp; 到这一步就完成了Spring MVC的组件的初始化。  
&emsp; 其中在initHandlerMappings()中，保存urls和beanName的对应关系到Map中。  

#### 1.2.2.2. 调用阶段  
&emsp; <font color = "clime">这一步是由请求触发的，所以入口为 DispatcherServlet的核心方法为 doService()， doService()中的核心逻辑由 doDispatch()实现，</font>源代码如下：  

```java
/** 中央控制器,控制请求的转发 **/
protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
    HttpServletRequest processedRequest = request;
    HandlerExecutionChain mappedHandler = null;
    boolean multipartRequestParsed = false;
    WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);

    try {
        ModelAndView mv = null;
        Object dispatchException = null;

        try {
            // 1.检查是否是文件上传的请求
            processedRequest = this.checkMultipart(request);
            multipartRequestParsed = processedRequest != request;
            // 2.取得处理当前请求的 Controller,这里也称为 hanlder,处理器,
            // 第一个步骤的意义就在这里体现了.这里并不是直接返回 Controller,
            // 而是返回的 HandlerExecutionChain 请求处理器链对象,
            // 该对象封装了 handler 和 interceptors.
            mappedHandler = this.getHandler(processedRequest);
            // 如果 handler 为空,则返回 404
            if (mappedHandler == null || mappedHandler.getHandler() == null) {
                this.noHandlerFound(processedRequest, response);
                return;
            }
            //3. 获取处理 request 的处理器适配器 handler adapter
            HandlerAdapter ha = this.getHandlerAdapter(mappedHandler.getHandler());
            // 处理 last-modified 请求头
            String method = request.getMethod();
            boolean isGet = "GET".equals(method);
            if (isGet || "HEAD".equals(method)) {
                long lastModified = ha.getLastModified(request, mappedHandler.getHandler());
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Last-Modified value for [" + getRequestUri(request) + "] is: " + lastModified);
                }

                if ((new ServletWebRequest(request, response)).checkNotModified(lastModified) && isGet) {
                    return;
                }
            }

            if (!mappedHandler.applyPreHandle(processedRequest, response)) {
                return;
            }

            // 4.实际的处理器处理请求,返回结果视图对象
            mv = ha.handle(processedRequest, response, mappedHandler.getHandler());
            if (asyncManager.isConcurrentHandlingStarted()) {
                return;
            }

            // 结果视图对象的处理
            this.applyDefaultViewName(processedRequest, mv);
            mappedHandler.applyPostHandle(processedRequest, response, mv);
        } catch (Exception var20) {
            dispatchException = var20;
        } catch (Throwable var21) {
            dispatchException = new NestedServletException("Handler dispatch failed", var21);
        }

        this.processDispatchResult(processedRequest, response, mappedHandler, mv, (Exception)dispatchException);
    } catch (Exception var22) {
        this.triggerAfterCompletion(processedRequest, response, mappedHandler, var22);
    } catch (Throwable var23) {
        this.triggerAfterCompletion(processedRequest, response, mappedHandler, new NestedServletException("Handler processing failed", var23));
    } finally {
        if (asyncManager.isConcurrentHandlingStarted()) {
            if (mappedHandler != null) {
                // 请求成功响应之后的方法
                mappedHandler.applyAfterConcurrentHandlingStarted(processedRequest, response);
            }
        } else if (multipartRequestParsed) {
            this.cleanupMultipart(processedRequest);
        }

    }

}
```
&emsp; <font color = "red">getHandler(processedRequest)方法实际上就是从 HandlerMapping 中找到 url 和 Controller 的对应关系。也就是 Map<url,Controller\>。</font>  
<!-- 我们知道，最终处理 Request 的是 Controller 中的方法，我们现在只是知道了 Controller，我们如何确认 Controller 中处理 Request 的方法呢？继续往下看。-->   
&emsp; 从Map<urls,beanName\>中取得 Controller 后，经过拦截器的预处理方法，再通过反射获取该方法上的注解和参数，解析方法和参数上的注解，然后反射调用方法获取ModelAndView 结果视图。最后，调用的就是RequestMappingHandlerAdapter的handle()中的核心逻辑由 handleInternal(request, response, handler)实现。  

```java
protected ModelAndView handleInternal(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {
    this.checkRequest(request);
    ModelAndView mav;
    if (this.synchronizeOnSession) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object mutex = WebUtils.getSessionMutex(session);
            synchronized(mutex) {
                mav = this.invokeHandlerMethod(request, response, handlerMethod);
            }
        } else {
            mav = this.invokeHandlerMethod(request, response, handlerMethod);
        }
    } else {
        mav = this.invokeHandlerMethod(request, response, handlerMethod);
    }

    if (!response.containsHeader("Cache-Control")) {
        if (this.getSessionAttributesHandler(handlerMethod).hasSessionAttributes()) {
            this.applyCacheSeconds(response, this.cacheSecondsForSessionAttributeHandlers);
        } else {
            this.prepareResponse(response);
        }
    }

    return mav;
}
```
&emsp; 整个处理过程中最核心的逻辑其实就是拼接Controller的url和方法的url，与Request的url进行匹配，找到匹配的方法。

&emsp; 找到处理Request的Controller中的方法后，会解析该方法上的参数，并反射调用该方法。在RequestMappingHandlerAdapter#invokeHandlerMethod()方法中，invocableMethod.invokeAndHandle()最终要实现的目的就是：完成Request中的参数和方法参数上数据的绑定。  

&emsp; 到这里，方法的参数值列表也获取到了，就可以直接进行方法的调用了。整个请求过程中最复杂的一步就是在这里了。到这里整个请求处理过程的关键步骤都已了解。  