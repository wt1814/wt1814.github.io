

<!-- 
 非常有必要了解的Springboot启动扩展点 
 https://mp.weixin.qq.com/s/H9hcQHZUNhuRodEPiVOHfQ
-->
<!-- 
https://blog.csdn.net/zzhuan_1/article/details/85312053
-->

&emsp; **<font color = "lime">总结：</font>**  
&emsp; **<font color = "lime">SpringBoot事件回调：</font>**  

* **<font color = "red">ApplicationContextInitializer，IOC容器初始化时被回调；</font>**  
* **<font color = "red">SpringApplicationRunListener，SpringBoot启动过程中多次被回调；</font>**  
* **<font color = "red">ApplicationRunner，容器启动完成后被回调；</font>**  
* **<font color = "red">CommandLineRunner，ApplicationRunner之后被回调。</font>**  

# SpringBoot事件回调  

## 前言  
&emsp; SpringBoot涉及了几个比较重要的事件回调机制，巧妙运用给开发者带来不一样的“福利”。   

&emsp; 回调接口：ApplicationContextInitializer、SpringApplicationRunListener、CommandLineRunner、ApplicationRunner。  
&emsp; 哪些场景会用到？  
1. 启动前环境检测？
2. 启动时配置初始化？  
3. 启动后数据初始化？  
...  

## 事件回调机制  

|类型|描述|获取方式|
|---|---|---|
|<font color = "red">ApplicationContextInitializer</font>|<font color = "lime">IOC容器初始化时被回调</font>|需要配置在META-INF/spring.factories，因为SpringBoot启动流程中是从spring.factories中获取的|
|<font color = "red">SpringApplicationRunListener</font>|<font color = "lime">SpringBoot启动过程中多次被回调</font>|需要配置在META-INF/spring.factories，因为SpringBoot启动流程中是从spring.factories中获取的|
|<font color = "red">ApplicationRunner</font>|<font color = "lime">容器启动完成后被回调</font>|需要放在IOC容器中，因为SpringBoot启动流程中是从IOC容器中取出的|
|<font color = "red">CommandLineRunner</font>|<font color = "lime">ApplicationRunner之后被回调</font>|需要放在IOC容器中，因为SpringBoot启动流程中是从IOC容器中取出的|

