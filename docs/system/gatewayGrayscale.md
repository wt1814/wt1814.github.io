


# Zuul网关实现灰度发布   
&emsp; <flont color = "lime">编写类继承ZuulFilter，实现灰度发布的逻辑</font> 

&emsp; 以会员服务为例  
1. 会员服务当前版本和新版本分别部署，eureka.instance.metadata-map.version=current表示为当前版本，eureka.instance.metadata-map.version=new表示为新版本；  

```yaml
###服务启动端口号
server:
  port: 8300
###服务名称(服务注册到eureka名称)
spring:
  application:
    name: app-member
###服务注册到eureka地址
eureka:
  instance:
    #eureka server至上一次收到client的心跳之后，等待下一次心跳的超时时间，
    #在这个时间内若没收到下一次心跳，则将移除该instance
    lease-expiration-duration-in-seconds: 9
    #间隔多久向Eureka Server发送心跳
    lease-renewal-interval-in-seconds: 3
    #版本
    metadata-map:
      version: current
  client:
    service-url:
      defaultZone: http://localhost:8100/eureka
    #CacheRefreshThread线程的调度频率
    registry-fetch-interval-seconds: 3
```

2. zuul网关服务yml配置文件  

```yaml
###服务启动端口号
server:
  port: 80
###服务名称(服务注册到eureka名称)
spring:
  application:
    name: app-zuul
  #数据库配置
  datasource:
    url: jdbc:mysql://localhost:3306/test?autoReconnect=true&useUnicode=true&characterEncoding=utf-8
    username: root
    password: 123456
    driver-class-name: com.mysql.jdbc.Driver
###服务注册到eureka地址
eureka:
  instance:
    #eureka server至上一次收到client的心跳之后，等待下一次心跳的超时时间，
    #在这个时间内若没收到下一次心跳，则将移除该instance
    lease-expiration-duration-in-seconds: 9
    #间隔多久向Eureka Server发送心跳
    lease-renewal-interval-in-seconds: 3
  client:
    service-url:
      defaultZone: http://localhost:8100/eureka
    #CacheRefreshThread线程的调度频率
    registry-fetch-interval-seconds: 3
### 配置网关反向代理
zuul:
  routes:
    api-a:
      ### 以 /api-weixin/访问转发到微信服务
      path: /api-weixin/**
      serviceId: app-weixin
    api-b:
      ### 以 /api-member/访问转发到会员服务
      path: /api-member/**
      serviceId: app-member
```

3. 在数据库test中创建灰度发布的配置表

```text
CREATE TABLE `gray_release_config` (
   `id` int(11) NOT NULL AUTO_INCREMENT,
   `service_id` varchar(255) DEFAULT NULL, //网关中配置的serviceId
   `path` varchar(255) DEFAULT NULL,//网关中配置的path
   `enable_gray_release` int(11) DEFAULT NULL,//1为开启灰度发布，0为关闭灰度发布
   PRIMARY KEY (`id`)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8
```

4. 与配置表对应的类

```java
import lombok.Data;

@Data
public class GrayReleaseConfig {
    private int id;
    private String serviceId;
    private String path;
    private int enableGrayRelease;
}
```

5. 管理配置表信息的类，每隔一段时间同步配置表中的信息  

```java
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Configuration      
@EnableScheduling 
public class GrayReleaseConfigManager {
    
    private Map<String, GrayReleaseConfig> grayReleaseConfigs = 
            new ConcurrentHashMap<String, GrayReleaseConfig>();
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Scheduled(fixedRate = 1000) 
    private void refreshRoute() {
        List<GrayReleaseConfig> results = jdbcTemplate.query(
                "select * from gray_release_config", 
                new BeanPropertyRowMapper<>(GrayReleaseConfig.class));
        
        for(GrayReleaseConfig grayReleaseConfig : results) {
            grayReleaseConfigs.put(grayReleaseConfig.getPath(), grayReleaseConfig);
        }
    }
    
    public Map<String, GrayReleaseConfig> getGrayReleaseConfigs() {
        return grayReleaseConfigs;
    }

}
```

6. <flont color = "lime">编写类继承ZuulFilter，实现灰度发布的逻辑</font>  

```java
import org.springframework.context.annotation.Configuration;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import io.jmnarloch.spring.cloud.ribbon.support.RibbonFilterContextHolder;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.*;
import java.util.Map;
import java.util.Random;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@SuppressWarnings("unused")
@Configuration
public class GrayReleaseFilter extends ZuulFilter {
    
    @Resource
    private GrayReleaseConfigManager grayReleaseConfigManager;

    @Override
    public int filterOrder() {
        return PRE_DECORATION_FILTER_ORDER - 1;
    }
 
    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    /**
     * 返回true则启动了灰度发布功能，执行run方法
     * @return
     */
    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String requestURI = request.getRequestURI();

        Map<String, GrayReleaseConfig> grayReleaseConfigs = 
                grayReleaseConfigManager.getGrayReleaseConfigs();
        for(String path : grayReleaseConfigs.keySet()) {
            if(requestURI.contains(path)) {
                GrayReleaseConfig grayReleaseConfig = grayReleaseConfigs.get(path);
                if(grayReleaseConfig.getEnableGrayRelease() == 1) {
                    System.out.println("启用灰度发布功能");  
                    return true;
                }
            }
        }
        System.out.println("不启用灰度发布功能");
        return false;
    }
    
    @Override
    public Object run() {
        //这里实现流量控制的逻辑，示例是把1%的流量导入到新版服务上
        int seed = (int)(Math.random() * 100);
        if (seed == 50) {
            RibbonFilterContextHolder.getCurrentContext().add("version", "new");
        }  else {
            RibbonFilterContextHolder.getCurrentContext().add("version", "current");
        }
        return null;
    }
}
```

7. enable_gray_release字段为0时，流量会均匀分配到当天版本和新版本，把enable_gray_release改为1后，实现灰度发布，流量的百分之一会导入到新版本上。  
8. 当灰度测试完毕，将enable_gray_release重新改为1，将new版本改成current版本上线。  
