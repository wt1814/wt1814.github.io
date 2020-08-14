
# SpringBoot事件回调  
<!-- 
https://blog.csdn.net/zzhuan_1/article/details/85312053
-->

## 前言  
&emsp; SpringBoot涉及了几个比较重要的事件回调机制，巧妙运用给开发者带来不一样的“福利”。   

&emsp; 回调：ApplicationContextInitializer、SpringApplicationRunListener、CommandLineRunner、ApplicationRunner。  
&emsp; 哪些场景会用到？  
1. 启动前环境检测？
2. 启动时配置初始化？  
3. 启动后数据初始化？  
...  

## 事件回调机制  

|类型|描述|获取方式|
|---|---|---|
|ApplicationContextInitializer|IOC容器初始化时被回调|需要配置在META-INF/spring.factories，因为SpringBoot启动流程中是从spring.factories中获取的|
|SpringApplicationRunListener|SpringBoot启动过程中多次被回调|需要配置在META-INF/spring.factories，因为SpringBoot启动流程中是从spring.factories中获取的|
|ApplicationRunner|容器启动完成后被回调|需要放在IOC容器中，因为SpringBoot启动流程中是从IOC容器中取出的|
|CommandLineRunner|ApplicationRunner之后被回调|需要放在IOC容器中，因为SpringBoot启动流程中是从IOC容器中取出的|





