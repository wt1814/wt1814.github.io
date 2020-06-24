---
title: 多线程串线了  
date: 2020-06-29 00:00:00  
tags:  
    - ERROR  
---
&emsp; 遇到的一个问题，简单记下。  

1. 业务场景：系统改造，老系统使用xml作为数据传输。新系统追求时髦，使用json进行交互。新系统要适应老系统，能够将已有的业务从老系统切到新系统，好进行资源回收。针对部分接口，xml格式和json格式的报文连字段都不一致。因此采用了Velocity模版引擎，做一次转换。  
2. 问题产生：平时开发并没有遇到大问题。因前期资源有限，组里做压测时，也没产生大问题。因为面临上线，申请到资源了，又做了一次压测。使用同一份请求报文，并无问题。但是当采用两份不同报文压测时，问题产生了。压测人员告诉我们接口失败了，要了一份报文，本地、开发环境都无问题，不了了之了。可是第2天，<font color = "red">2份不同报文，并发数增加时，接口失败率越来越高了</font>，不得不重视了。  
3. 问题发现：因为权限受制，无法得到日志文件。请运维人员取出日志文件，发送给我们。拿到日志，首先也是取到请求报文，本地测试、开发环境测试2连，都无问题。可是压测环境，的确是报错很明显啊。<font color = "red">跟着线程走，把这次的请求日志搜集出来。发现问题了，模版转换前后的数据怎么不一样了？</font>啥子情况。  
4. 问题定位：因为Velocity模版转换是系统的一个功能点，所以轻车熟路的找到使用地方。只是简单调用，并不会产生问题。因此能确定是工具类VelocityUtil有bug。  
&emsp; 源码如下(公司保密，已删除部分信息)：  

    ```
    import com.alibaba.fastjson.JSONObject;
    import org.apache.velocity.VelocityContext;
    import org.apache.velocity.app.VelocityEngine;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;

    import javax.annotation.concurrent.ThreadSafe;
    import java.io.StringWriter;
    import java.util.Map;

    @ThreadSafe
    public class VelocityUtil {

        private static final Logger logger = LoggerFactory.getLogger(VelocityUtil.class);
        /* 持有私有静态实例，防止被引用，此处赋值为null，目的是实现延迟加载 */
        private static VelocityUtil velocityUtil;

        private static VelocityEngine ve = null;

        private static VelocityContext context = null;

        /**
        * 私有构造方法，防止实例化
        */
        private VelocityUtil(VelocityEngine ve, VelocityContext context) {
            this.ve = ve;           // 初始化并取得Velocity引擎
            this.context = context; // 初始化velocity的上下文context
        }

        /**
        * 静态工程方法，创建实例
        */
        public synchronized static VelocityUtil getInstance() {
            if (velocityUtil == null) {
                velocityUtil = new VelocityUtil(new VelocityEngine(), new VelocityContext());
            }
            return velocityUtil;
        }

        /**
        * xml转json
        *
        * @param xml         
        * @return
        */
        public String xml2JsonTemplate(String xml, VelocityTemplate template) {
            long a = System.currentTimeMillis();
            logger.debug("xml -> {}", xml);
            ve.init();
            JSONObjectTools xmlJSONObj = XMLTools.toJSONObject(xml,true);
            String jsonStr = xmlJSONObj.toString();
            context.put("xmlObj", JSONObject.parseObject(jsonStr, Map.class));
            StringWriter stringWriter = new StringWriter();
            ve.evaluate(context, stringWriter, template.getTemplatename(), template.getTemplatecontent());
            String writer = stringWriter.toString();
            logger.debug("targetJson -> {}", writer);
            long b = System.currentTimeMillis();
            logger.info("整体耗时：{}",(b - a));
            return writer;
        }

    }
    ```
5. 问题分析：  

    ```

    private static VelocityEngine ve = null;
    private static VelocityContext context = null;

    ve.init();
    JSONObjectTools xmlJSONObj = XMLTools.toJSONObject(xml,true);
    String jsonStr = xmlJSONObj.toString();
    context.put("xmlObj", JSONObject.parseObject(jsonStr, Map.class));
    ```
    &emsp; <font color = "red">每次调用实例方法，都会重新初始化，VelocityContext上下文作为类变量，没有做线程安全的措施（@ThreadSafe只是表示VelocityUtil类是线程安全的）。</font>

6. 问题解决：  
&emsp; <font color = "red">VelocityContext作为上下文，当然是放在Spring容器里里，也能保证是单例。</font>  

    ```
    @Service
    public class VelocityUtil {

        @PostConstruct
        public void init(){
            Velocity.init();
        }

        /**
        * xml转json
        *
        * @param xml         
        * @return
        */
        public String xml2JsonTemplate(String xml, VelocityTemplate template) {
            log.debug("xml -> {}", xml);
            JSONObjectTools xmlJSONObj = XMLTools.toJSONObject(xml,true);
            String jsonStr = xmlJSONObj.toString();
            VelocityContext context = new VelocityContext();
            context.put("xmlObj", JSONObject.parseObject(jsonStr, Map.class));
            StringWriter stringWriter = new StringWriter();
            Velocity.evaluate(context, stringWriter, template.getTemplatename(), template.getTemplatecontent());
            /*ve.init();
            ve.evaluate(context, stringWriter, template.getTemplatename(), template.getTemplatecontent());*/
            String writer = stringWriter.toString();
            log.info("targetJson -> {}", writer);
            return writer;
        }
    }
    ```
    &emsp; ***<font color = "red">@PostConstruct注解说明：</font>***  
    &emsp; @PostConstruct注解是Java自己的注解。  
    &emsp; Java中该注解的说明：@PostConstruct该注解被用来修饰一个非静态的void()方法。被@PostConstruct修饰的方法会在服务器加载Servlet的时候运行，并且只会被服务器执行一次。PostConstruct在构造函数之后执行，init（）方法之前执行。  

    &emsp; 通常在Spring框架中使用到@PostConstruct注解，该注解的方法在整个Bean初始化中的执行顺序：  
    &emsp; Constructor(构造方法) -> @Autowired(依赖注入) -> @PostConstruct(注释的方法)  



