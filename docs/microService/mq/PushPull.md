
<!-- TOC -->

- [1. 消息的推拉机制](#1-消息的推拉机制)
    - [1.1. 简介](#11-简介)
    - [1.2. 推模式以及优缺点](#12-推模式以及优缺点)
    - [1.3. 拉模式以及优缺点](#13-拉模式以及优缺点)
    - [1.4. 常见MQ的选择](#14-常见mq的选择)

<!-- /TOC -->

**<font color = "red">总结：</font>**  
1. **<font color = "red">一般说的推拉模式指的是broker和consumer之间的，producer和broker之间的模式是推的模式，也就是每次producer每次生产了消息，会主动推给broker。</font>**  
2. 推模式以及优缺点
    1. 优点
        1. 一个优点就是延迟小，实时性比较好，broker接收到消息之后就会立刻推送到Consumer，实时性相对来说是比较高的。  
        2. 还有一个优点其实就是简化了Consumer端的逻辑，消费端不需要自己去处理这个拉取的逻辑，只需要监听这个消息的topic，然后去专心的处理这个消息的业务逻辑即可。  
    2. 缺点
        1. 第二点简化了Consumer消费端的逻辑的同时，也就复杂化了broker端的逻辑，这其实也不算是优点或者缺点吧，算是这个模式的一个特点，需要根据场景来选择自己合适的模式。
        2. 最大的一个缺点就是推送的速率和消费的速率不好去匹配，这样就是很糟糕的，你想，如果broker拿到消息就推给Consumer，不在乎Consumer的消费能力如何，就往Consumer直接扔，那Consumer有可能会崩溃。
        3. 还有一个缺点就是消费者推出去之后，无法保证消息发送成功，push采用的是广播模式，也就是只有服务端和客户端都在同一个频道的时候，推模式才可以成功的将消息推到消费者。
3. 拉模式以及优缺点  
    1. 优点
        1. 最大的优点就是主动权掌握在Consumer这边了，每个消费者的消费能力可能不一样，消费者可以根据自身的情况来拉取消息的请求，如果消费者真的出现那种忙不过来的情况下，可以根据一定的策略去暂停拉取。
        2. 拉模式也更适合批量消息的发送，推模式是来一个消息就推一个，当然也可以缓存一部分消息再推送，但是无法确定Consumer是否能够处理这批推送的消息，拉模式则是Consumer主动来告诉broker，这样broker也可以更好的决定缓存多少消息用于批量发送。
    2. 缺点
        1. 拉模式需要Consumer对于服务端有一定的了解，主要的缺点就是实时性较差，针对于服务器端的实时更新的信息，客户端还是难以获取实时的信息。
        &emsp; 不能频繁的去拉取，这样也耗费性能，因此就必须降低请求的频率，请求间隔时间也就意味着消息的延迟。  
4. 常见MQ的选择
&emsp; RocketMQ最终决定的拉模式，kafka也是如此。  

# 1. 消息的推拉机制   
<!-- 
必须知道的消息的推拉机制 
https://mp.weixin.qq.com/s/R404jL45InVcf_3bWefxxw
-->


## 1.1. 简介
&emsp; 我们下面 **<font color = "red">要说的推拉模式指的是broker和consumer之间的，producer和broker之间的模式是推的模式，也就是每次producer每次生产了消息，会主动推给broker。</font>**  
&emsp; 其实这个大家也应该好理解，如果producer和broker之间交互用broker来拉取，就会怪怪的，每次消息都要存储到producer的本地，然后等待broker来拉取，这个要取决于多个producer的可靠性，显然这种设计是很糟糕的。  
&emsp; 我们下面要讨论的是broker和consumer之间的交互是推还是拉，大家也可以自己先思考下到底是推还是拉。  


## 1.2. 推模式以及优缺点
&emsp; 推模式指的是broker将消息推向Consumer，也就是Consumer是被动的去接收这个消息，broker来将消息主动的去推给Consumer。  
&emsp; 那么这种模式的优缺点呢，大家可以想一下。  
&emsp; 很明显的 **<font color = "red">一个优点就是延迟小，实时性比较好，broker接收到消息之后就会立刻推送到Consumer，实时性相对来说是比较高的。</font>**  
&emsp; **<font color = "red">还有一个优点其实就是简化了Consumer端的逻辑，消费端不需要自己去处理这个拉取的逻辑，只需要监听这个消息的topic，然后去专心的处理这个消息的业务逻辑即可。</font>**   


&emsp; 上面说的两点是优点，那么有优点就肯定也会伴随相应的缺点。  
&emsp; **<font color = "red">第二点简化了Consumer消费端的逻辑的同时，也就复杂化了broker端的逻辑，这其实也不算是优点或者缺点吧，算是这个模式的一个特点，需要根据场景来选择自己合适的模式。</font>**  
&emsp; **<font color = "red">最大的一个缺点就是推送的速率和消费的速率不好去匹配，这样就是很糟糕的，你想，如果broker拿到消息就推给Consumer，不在乎Consumer的消费能力如何，就往Consumer直接扔，那Consumer有可能会崩溃。</font>**  
&emsp; 就像一个生产线，本来只能接收的最大速度是10立方米每秒，结果呢，你每秒往生产线上扔100立方米每秒，那这个生产线可能就因为无法处理而直接崩盘。  
&emsp; 当推送速率很快的时候，甚至都像DDos的攻击一样，消费者就更难受了，不同的消费者的消费速率也是不一样的，broker也很难平衡每个消费者的速率，如果broker需要记住每个Consumer的消费能力和速度的话，那broker的复杂度可就直线上升。  
&emsp; **<font color = "red">还有一个缺点就是消费者推出去之后，无法保证消息发送成功，push采用的是广播模式，也就是只有服务端和客户端都在同一个频道的时候，推模式才可以成功的将消息推到消费者。</font>**  

## 1.3. 拉模式以及优缺点  
&emsp; 拉模式，也是同样的道理，就是Consumer是主动从broker拉取消息，哎，这次我Consumer主动了，我不需要你来喂我了，我每过一段时间去你那里拿消息就好了，你也别在乎我的消费速率了。  
&emsp; 咋回事知道了，想想这样的优缺点，知道了优缺点就对这个模式肯定了解的八九不离十了。  
&emsp; **<font color = "red">最大的优点就是主动权掌握在Consumer这边了，每个消费者的消费能力可能不一样，消费者可以根据自身的情况来拉取消息的请求，如果消费者真的出现那种忙不过来的情况下，可以根据一定的策略去暂停拉取。</font>**  
&emsp; 服务端也相对来说轻松了，不需要去进行消息的处理逻辑了，你来了我就给你就好了，你要多少我就给你就好了，broker就是一个没得感情的存储机器。  
&emsp; **<font color = "red">拉模式也更适合批量消息的发送，推模式是来一个消息就推一个，当然也可以缓存一部分消息再推送，但是无法确定Consumer是否能够处理这批推送的消息，拉模式则是Consumer主动来告诉broker，这样broker也可以更好的决定缓存多少消息用于批量发送。</font>**    

&emsp; 说完了优点，就需要说缺点了， **<font color = "red">拉模式需要Consumer对于服务端有一定的了解，主要的缺点就是实时性较差，针对于服务器端的实时更新的信息，客户端还是难以获取实时的信息。</font>**    
&emsp; 毕竟消费者是去拉取消息，消费者怎么知道消息到了呢，所以消费者能做的就是不断的去拉取，但是又 **<font color = "red">不能频繁的去拉取，这样也耗费性能，因此就必须降低请求的频率，请求间隔时间也就意味着消息的延迟。</font>**  

## 1.4. 常见MQ的选择  
&emsp; RocketMQ最终决定的拉模式，kafka也是如此。  

<!-- 
必须知道的消息的推拉机制 
https://mp.weixin.qq.com/s/R404jL45InVcf_3bWefxxw
-->