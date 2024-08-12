package com.wuw.gateway;

import com.wuw.common.api.config.ApplicationStartedEventListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationListener;
import springfox.documentation.oas.annotations.EnableOpenApi;

import java.util.Set;


@SpringBootApplication(exclude= {
        DataSourceAutoConfiguration.class

})
@EnableDiscoveryClient
@EnableFeignClients
@Slf4j
@EnableOpenApi // 开启swagger
public class GatewayApplication {

    public static void main(String[] args) {

        try {
            //https://blog.csdn.net/m0_45406092/article/details/120386450
            System.setProperty("csp.sentinel.app.type", "1");
            // SpringApplication.run(GatewayApplication.class, args);
            SpringApplication app = new SpringApplication(GatewayApplication.class);
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
