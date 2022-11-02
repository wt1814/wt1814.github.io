



# Maven配置  
<!-- 

settings.xml 详解
https://mp.weixin.qq.com/s?__biz=MzI4Njc5NjM1NQ==&mid=2247489251&idx=2&sn=43e2084ef2ef76c576bf8f5a2adb2fe2&chksm=ebd629cfdca1a0d99ab5cb0d3d29fad3ba7b458420c868cf97a7a4bb09f780018b33ca331120&mpshare=1&scene=1&srcid=&sharer_sharetime=1565486942713&sharer_shareid=b256218ead787d58e0b58614a973d00d&key=36a99a852770fa03ccd58f4ea681c6882de8b313c1da1c5665f09e77e892b4f8b921e82a5cd0980d41ad6919098a279a5e7b37ae1ac0f3054d21abd09c5d3b238617dd31f7a3d0fefe0806933e9d859c&ascene=1&uin=MTE1MTYxNzY2MQ%3D%3D&devicetype=Windows+10&version=62060844&lang=zh_CN&pass_ticket=tqX0KeqJ7ignoZrbh8Avwa%2B7dWB6gav87csdBtWCJ2F66S58CI7FD3SHom4b6Cml


maven setting.xml 多仓库配置
https://blog.csdn.net/rockstar541/article/details/77165067

setting.xml中mirror和repository的关系
https://my.oschina.net/sunchp/blog/100634
https://www.cnblogs.com/lngo/p/11051148.html

setting.xml的mirror、mirrorOf和pom.xml的repositories、repository的关系关联
https://my.oschina.net/cjun/blog/881766?utm_medium=referral
Maven：mirror和repository 区别
https://my.oschina.net/sunchp/blog/100634
Maven 如何配置多 mirror？
https://www.v2ex.com/amp/t/462135


Maven Pom文件标签详解
https://www.cnblogs.com/sharpest/p/7738444.html





-->



	<profiles>
		<!-- 在tsf环境中，如果执行包中包括swagger-ui这个依赖（不管有没有jar），则无法在tsf的api列表中展现api接口，所以打包时不能包含swagger-ui，打包时请选择prod这个profile -->
		<profile>
			<id>dev</id>
			<properties>
				<env>dev</env>
			</properties>
			<activation>
				<activeByDefault>true</activeByDefault>
			</activation>
			<dependencies>
				<!-- 热启动 -->
				<dependency>
					<groupId>org.springframework.boot</groupId>
					<artifactId>spring-boot-devtools</artifactId>
					<scope>runtime</scope>
				</dependency>
			</dependencies>
		</profile>
		<profile>
			<id>prod</id>
			<properties>
				<env>prod</env>
			</properties>
		</profile>
	</profiles>