package com.wuw.common.server;

import com.wuw.common.api.config.ApplicationStartedEventListener;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;

import java.util.Set;

@SpringBootApplication(exclude= {
        DataSourceAutoConfiguration.class,
        //RedisAutoConfiguration.class, // todo redis
        //RedissonAutoConfiguration.class
})
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.wuw", "com.baidu.fsg"})
@MapperScan("com.wuw.common.server.dao")
@Slf4j
public class CommonServerApplication {

    public static void main(String[] args) {
        try {
            // SpringApplication.run(UcenterServerApplication.class, args);
            // log4j2日志：https://www.cnblogs.com/extjava/p/7553642.html
            SpringApplication app = new SpringApplication(CommonServerApplication.class);
            Set<ApplicationListener<?>> ls = app.getListeners();
            ApplicationStartedEventListener asel = new ApplicationStartedEventListener();
            app.addListeners(asel);
            app.run(args);

        }catch (Exception e){
            System.out.println(e.getMessage());
            log.error(e.getMessage());
        }
    }


}
