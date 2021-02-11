

# Resttemplate  
<!-- 
Httpclient4.3+ 连接池监控详细介绍
https://www.jianshu.com/p/2813af4eb0d3
RestTemplate请求的服务实例返回List类型，用数组接收
https://blog.csdn.net/horse_well/article/details/88879185
-->

<!-- 
springboot 注入 restTemplate
https://www.jianshu.com/p/583b798e8deb

RestTemplate返回泛型解决方案
https://www.jianshu.com/p/2196dd2c17a6
spring restTemplate 返回泛型
https://blog.csdn.net/a294039255/article/details/73850472

Springboot — 用更优雅的方式发HTTP请求(RestTemplate详解)
https://www.cnblogs.com/javazhiyin/p/9851775.html
resttemplate 发送get请求
https://blog.csdn.net/qq_35794202/article/details/102998621

发送json
        //封装请求头
        HttpHeaders headers = new HttpHeaders();
        headers.set("Souche-Std-Response","1");
        headers.set("AppName","cheyipai");
        //todo
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

        OpenAccountRequestDTO openAccountRequestDTO = new OpenAccountRequestDTO();
        openAccountRequestDTO.setOpenType("");
        openAccountRequestDTO.setSignature("");
        openAccountRequestDTO.setTradeNo("");
        openAccountRequestDTO.setExt("");
        String openAccountRequest = JsonUtils.toJson(openAccountRequestDTO);
        //todo
        HttpEntity<String> httpEntity = new HttpEntity(openAccountRequest,headers);

-->