package com.wuw.jenkinstest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JenkinstestApplication {

	public static void main(String[] args) {

		SpringApplication.run(JenkinstestApplication.class, args);
		System.out.println("服务启动成功");
	}

}
