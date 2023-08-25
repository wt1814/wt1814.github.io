

&emsp; ~~使用平台补贴场景，此场景卖家电子账户会收到两笔收款，但需要统一进行提现；~~  


&emsp; 车易拍为商户代收代付，资金流不合规，面临二清问题。   
&emsp; 车易拍对接平安银行的见证宝来解决合规问题。   
&emsp;  二清解决方案：在二清方案中主要有资金流适配和资金流流向两大部分。 
1. 资金流适配  
&emsp; 资金流适配是支付系统抽离出来的系统，主要是对内部其他子系统减少侵入、进行解偶。   
&emsp; 新旧流程的区别：订单总额...    
&emsp; 资金适配流程：  
    1. 支付  
    &emsp; 一笔订单可能支付多次。  
    &emsp; 若有优惠，订单总额由买家和平台一起支付。
    2. 清分  
    &emsp; 按照一定的规则，对担保户进行提现。可以分成车款、平台服务费、门店服务费  
2. 资金流流向：`开户和绑卡、充值、支付、清分和提现、退款、对账`    
&emsp; 资金流流向包含交易前准备：开通账户、绑定银行卡；收款（交易进行中，充值、支付）、退款（交易进行中）、付款（交易确认：清分、提现）。  
&emsp; 怎么设计？是新开一个服务，还是旧服务？是复用原有表还是新建表？   
&emsp; 系统调用关系：资金适配 ---> 支付系统、   收银台 ---> 支付系统。 资金适配和收银台都是支付系统的前置系统  
    1. 绑卡流程  
    &emsp; 3张表：用户账户关系、银行卡、用户账户银行卡关系
    2. 充值 
    3. 支付流程  
    &emsp; 5个接口：创建支付单、支付、支付取消、支付回调、支付查询。    
    &emsp; 支付流程：支付金额的校验、拆分在资金适配系统中。支付接口的3个特性：幂等、安全、数据一致性。数据一致性：内部系统通过mq发送到财务系统；平安银行通过本地事物表法。支付过程中网络异常处理。   
    4. 退款
    

&emsp; @@什么情况下，会有多个清分单对一个提现单？  

&emsp; 前端嵌入银行sdk，主要用于加密密码。选择账户、选择银行卡、输入密码。其余流程与支付相似。    