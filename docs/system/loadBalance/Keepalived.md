<!-- TOC -->

- [1. Keepalived](#1-keepalived)
    - [1.1. 介绍](#11-介绍)
    - [1.2. VRRP，虚拟路由器冗余协议](#12-vrrp虚拟路由器冗余协议)
    - [1.3. Keepalived原理](#13-keepalived原理)
    - [1.4. 配置文件详解](#14-配置文件详解)
        - [1.4.1. 全局配置](#141-全局配置)
        - [1.4.2. VRRPD配置](#142-vrrpd配置)
        - [1.4.3. LVS配置](#143-lvs配置)
    - [1.5. 使用](#15-使用)
        - [1.5.1. 抢占式配置](#151-抢占式配置)
        - [1.5.2. 非抢占式配置](#152-非抢占式配置)
        - [1.5.3. nginx+keepalived](#153-nginxkeepalived)
    - [1.6. 脑裂](#16-脑裂)

<!-- /TOC -->


# 1. Keepalived  

<!-- 
http://www.yunweipai.com/35361.html
    http://www.yunweipai.com/35361.html
-->

## 1.1. 介绍  
&emsp; Keepalived起初是为LVS设计的，专门用来监控集群系统中各个服务节点的状态，它根据TCP/IP参考模型的第三、第四层、第五层交换机制检测每个服务节点的状态，如果某个服务器节点出现异常，或者工作出现故障，Keepalived将检测到，并将出现的故障的服务器节点从集群系统中剔除，这些工作全部是自动完成的，不需要人工干涉，需要人工完成的只是修复出现故障的服务节点。  
&emsp; 后来Keepalived又加入了VRRP的功能，VRRP(VritrualRouterRedundancyProtocol,虚拟路由冗余协议)出现的目的是解决静态路由出现的单点故障问题，通过VRRP可以实现网络不间断稳定运行，因此Keepalvied一方面具有服务器状态检测和故障隔离功能，另外一方面也有HAcluster功能。  
&emsp; 健康检查和失败切换是keepalived的两大核心功能。所谓的健康检查，就是采用tcp三次握手，icmp请求，http请求，udp echo请求等方式对负载均衡器后面的实际的服务器(通常是承载真实业务的服务器)进行保活；而失败切换主要是应用于配置了主备模式的负载均衡器，利用VRRP维持主备负载均衡器的心跳，当主负载均衡器出现问题时，由备负载均衡器承载对应的业务，从而在最大限度上减少流量损失，并提供服务的稳定性。  

## 1.2. VRRP，虚拟路由器冗余协议  
&emsp; 在现实的网络环境中。主机之间的通信都是通过配置静态路由或者(默认网关)来完成的，而主机之间的路由器一旦发生故障，通信就会失效，因此这种通信模式当中，路由器就成了一个单点瓶颈，为了解决这个问题，就引入了VRRP协议。  
&emsp; VRRP协议是一种容错的主备模式的协议，保证当主机的下一跳路由出现故障时，由另一台路由器来代替出现故障的路由器进行工作，通过VRRP可以在网络发生故障时透明的进行设备切换而不影响主机之间的数据通信。   
<!-- 
VRRP是一种选择协议，它可以把一个虚拟路由器的责任动态分配到局域网上的 VRRP 路由器中的一台。控制虚拟路由器 IP 地址的 VRRP 路由器称为主路由器，它负责转发数据包到这些虚拟 IP 地址。一旦主路由器不可用，这种选择过程就提供了动态的故障转移机制，这就允许虚拟路由器的 IP 地址可以作为终端主机的默认第一跳路由器。是一种LAN接入设备备份协议。一个局域网络内的所有主机都设置缺省网关，这样主机发出的目的地址不在本网段的报文将被通过缺省网关发往三层交换机，从而实现了主机和外部网络的通信。 [1] 
VRRP是一种路由容错协议，也可以叫做备份路由协议。一个局域网络内的所有主机都设置缺省路由，当网内主机发出的目的地址不在本网段时，报文将被通过缺省路由发往外部路由器，从而实现了主机与外部网络的通信。当缺省路由器down掉(即端口关闭)之后，内部主机将无法与外部通信，如果路由器设置了VRRP时，那么这时，虚拟路由将启用备份路由器，从而实现全网通信。
在VRRP协议中，有两组重要的概念：VRRP路由器和虚拟路由器，主控路由器和备份路由器。VRRP路由器是指运行VRRP的路由器，是物理实体；虚拟路由器是指VRRP协议创建的，是逻辑概念。一组VRRP路由器协同工作，共同构成一台虚拟路由器。该虚拟路由器对外表现为一个具有唯一固定的IP地址和MAC地址的逻辑路由器。处于同一个VRRP组中的路由器具有两种互斥的角色：主控路由器和备份路由器，一个VRRP组中有且只有一台处于主控角色的路由器，可以有一个或者多个处于备份角色的路由器VRRP协议从路由器组中选出一台作为主控路由器，负责ARP解析和转发IP数据包，组中的其他路由器作为备份的角色并处于待命状态，当由于某种原因主控路由器发生故障时，其中的一台备份路由器能在瞬间的时延后升级为主控路由器，由于此切换非常迅速而且不用改变IP地址和MAC地址，故对终端使用者系统是透明的。
-->
![image](https://gitee.com/wt1814/pic-host/raw/master/images/system/loadBalance/keep/keep-1.png)  
&emsp; **VRRP术语：**  
1. Virtual Router  
&emsp; 虚拟路由器，一个抽象对象，基于子网接口，包括一个虚拟路由器标识符(VRID)和一个或多个IP地址，这个(些)IP地址又称为虚拟IP地址，虚拟IP地址作为主机的默认网关。  
2. VRRP Router  
&emsp; VRRP路由器，即运行VRRP协议的路由器，一个VRRP路由器可以加入到一个或多个虚拟路由器中。  
3. IP Address Owner  
&emsp; IP地址拥有者，虚拟路由器的虚拟IP地址与接口的真实IP地址相同的VRRP路由器。  
4. Virtual Router Master  
&emsp; 虚拟主路由器，负责转发通过虚拟路由器的三层数据包，对虚拟路由器的IP地址的ARP请求进行回应。如果某个VRRP路由器是IP地址拥有者，则它总是虚拟主路由器。  
5. Virtual Router Backup  
&emsp; 虚拟备份路由器，不转发三层数据包，不应答虚拟IP地址的ARP请求，当虚拟主路由器出现故障时接替虚拟主路由器的工作。  

&emsp; **VRRP状态机VRRP状态机**  
&emsp; 协议对VRRP规定了3种状态：INITIALIZE，MASTER和BACKUP。简单地说，INITIALIZE即初始态，MASTER即主用状态，也就是在VRRP备份组中真正起作用的路由器，BACKUP即备用状态，是MASTER的备份。对于不同状态的实现，有如下要求：  
1. INITIALIZE：  
    &emsp; 路由器启动时，如果路由器的优先级是255(最高优先级，当且仅当配置的VRRP虚拟IP地址和接口IP相同，即所谓IP地址拥有者)，要发送VRRP通告信息，并发送广播ARP信息通告路由器IP地址对应的MAC地址为路由虚拟MAC，设置通告信息定时器准备定时发送VRRP通告信息，转为MASTER状态：否则进入BACKUP状态，设置定时器检查定时检查是否收到MASTER的通告信息。  
2. MASTER  
    &emsp; 主机状态下的路由器要完成如下功能：  
    * 设置定时通告定时器；  
    * 用VRRP虚拟MAC地址响应路由器IP地址的ARP请求；  
    * 转发目的MAC是VRRP虚拟MAC的数据包；  
    * 如果是虚拟路由器IP的拥有者，将接受目的地址是虚拟路由器IP的数据包，否则丢弃；  
    * 当收到shutdown的事件时删除定时通告定时器，发送优先值级为0的通告包，转初始化状态；  
    * 如果定时通告定时器超时时，发送VRRP通告信息；  
    * 收到VRRP通告信息时，如果优先值为0，发送VRRP通告信息：否则，判断数据的优先级是否高于本机，或相等而且实际IP地址大于本地实际IP，设置定时通告定时器，复位主机超时定时器，转BACKUP状态；否则的话，丢弃该通告包。  
3. BACKUP  
    &emsp; 备机状态下的路由器要实现以下功能：  
    * 设置主机超时定时器；  
    * 不能响应针对虚拟路由器IP的ARP请求信息；  
    * 丢弃所有目的MAC地址是虚拟路由器MAC地址的数据包；  
    * 不接受目的是虚拟路由器IP的所有数据包；  
    * 当收到shutdown的事件时删除主机超时定时器，转初始化状态；  
    * 主机超时定时器超时的时候，发送VRRP通告信息，广播ARP地址信息，转MASTER状态；  
    * 收到VRRP通告信息时。如果优先值为0，表示进入与MASTER选举，否则判断数据的优先级是否高于本机，如果高的话承认MASTER有效，复位主机超时定时器；否则的话，丢弃该通告包。  

&emsp; **VRRP选举机制**  
&emsp; VRRP使用选举机制来确定路由器的状态，优先级选举：  
1. VRRP组中IP拥有者。如果虚拟IP地址与VRRP组中的某台VRRP路由器IP地址相同，则此路由器为IP地址拥有者，这台路由器将被定位主路由器。
2. 比较优先级。如果没有IP地址拥有者，则比较路由器的优先级，优先级的范围是0~255，优先级大的作为主路由器
3. 比较IP地址。在没有Ip地址拥有者和优先级相同的情况下，IP地址大的作为主路由器。

&emsp; 如下图所示，虚拟IP为10.1.1.254，在VRRP组中没有IP地址拥有者，则比较优先级，很明显RB和RA的优先级要大于RC，则比较RA和RB的IP地址，RB的IP地址大。所以RB为组中的主路由器。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/system/loadBalance/keep/keep-2.png)  

&emsp; **工作过程**  
&emsp; 路由器使用VRRP 功能后，会根据优先级确定自己在备份组中的角色。优先级高的路由器成为Master 路由器，优先级低的成为Backup 路由器。Master 拥有对外服务的虚拟IP，提供各种网络功能，并定期发送VRRP 报文，通知备份组内的其他设备自己工作正常；Backup 路由器只接收Master 发来的报文信息，用来监控Master 的运行状态。当Master 失效时，Backup 路由器进行选举，优先级高的Backup 将成为新的Master。  
&emsp; 在抢占方式下，当Backup 路由器收到VRRP 报文后，会将自己的优先级与报文中的优先级进行比较。如果大于通告报文中的优先级，则成为Master 路由器；否则将保持Backup状态；  
&emsp; 在非抢占方式下，只要Master 路由器没有出现故障，备份组中的路由器始终保持Master 或Backup 状态，Backup 路由器即使随后被配置了更高的优先级也不会成为Master 路由器；  
&emsp; 如果Backup路由器的定时器超时后仍未收到Master路由器发送来的VRRP报文，则认为Master路由器已经无法正常工作，此时Backup路由器会认为自己是Master 路由器，并对外发送VRRP报文。备份组内的路由器根据优先级选举出Master路由器，承担报文的转发功能。  

## 1.3. Keepalived原理  
&emsp; keepalived是一个类似于layer3, 4 & 5交换机制的软件，也就是平时说的第3层、第4层和第5层交换。Keepalived是自动完成，不需人工干涉。  
&emsp; Keepalived工作在TCP/IP参考模型的三层、四层、五层(物理层，链路层)：   
&emsp; 网络层(3)：Keepalived通过ICMP协议向服务器集群中的每一个节点发送一个ICMP数据包(有点类似与Ping的功能)，如果某个节点没有返回响应数据包，那么认为该节点发生了故障，Keepalived将报告这个节点失效，并从服务器集群中剔除故障节点。  
&emsp; 传输层(4)：Keepalived在传输层里利用了TCP协议的端口连接和扫描技术来判断集群节点的端口是否正常，比如对于常见的WEB服务器80端口。或者SSH服务22端口，Keepalived一旦在传输层探测到这些端口号没有数据响应和数据返回，就认为这些端口发生异常，然后强制将这些端口所对应的节点从服务器集群中剔除掉。  
&emsp; 应用层(5)：Keepalived的运行方式也更加全面化和复杂化，用户可以通过自定义Keepalived工作方式，例如：可以通过编写程序或者脚本来运行Keepalived，而Keepalived将根据用户的设定参数检测各种程序或者服务是否允许正常，如果Keepalived的检测结果和用户设定的不一致时，Keepalived将把对应的服务器从服务器集群中剔除。  

## 1.4. 配置文件详解  
<!-- 
https://www.cnblogs.com/yyxianren/p/10955538.html
-->
&emsp; /etc/keepalived/keepalived.conf，有三类配置区域：

* 全局配置(Global Configuration)
* VRRPD配置
* LVS配置

### 1.4.1. 全局配置
&emsp; 全局配置又包括两个子配置：  

* 全局定义(global definition)
* 静态路由配置(static ipaddress/routes)

1. 全局定义(global definition)配置范例  

```text
global_defs #全局配置标识，表明这个区域{}是全局配置
{
    notification_email
        {
        admin@example.com #表示发送通知邮件时邮件源地址是谁
        }
    notification_email_from admin@example.com #表示keepalived在发生诸如切换操作时需要发送email通知，以及email发送给哪些邮件地址，邮件地址可以多个，每行一个notification_email_from admin@example.com
    smtp_server 127.0.0.1 #表示发送email时使用的smtp服务器地址，这里可以用本地的sendmail来实现
    stmp_connect_timeout 30 #连接smtp连接超时时间
    router_id node1 #机器标识
}
```
2. 静态地址和路由配置范例

```text
static_ipaddress
{
    192.168.1.1/24 brd + dev eth0 scope global #相当于: ip addr add 192.168.1.1/24 brd + dev eth0 scope global
    192.168.1.2/24 brd + dev eth1 scope global #就是给eth1配置IP地址
}
static_routes
{
    src $SRC_IP to $DST_IP dev $SRC_DEVICE #路由和ip同理，一般这个区域不需要配置
    src $SRC_IP to $DST_IP via $GW dev $SRC_DEVICE
}
```

&emsp; 这里实际上就是给服务器配置真实的IP地址和路由的，在复杂的环境下可能需要配置，一般不会用这个来配置，可以直接用vi /etc/sysconfig/network-script/ifcfg-eth1来配置！  

### 1.4.2. VRRPD配置  
...

### 1.4.3. LVS配置  
...

## 1.5. 使用  
<!-- 
https://www.jianshu.com/p/a6b5ab36292a
-->
### 1.5.1. 抢占式配置  
...

### 1.5.2. 非抢占式配置  
...

### 1.5.3. nginx+keepalived  
&emsp; 实现思路：将keepalived 中的vip作为nginx负载均衡的监听地址，并且域名绑定的也是vip的地址。  
&emsp; 说明：Nginx 负载均衡实现高可用，需要借助Keepalived地址漂移功能。  

## 1.6. 脑裂  
&emsp; 由于某些原因，导致两台keepalived高可用服务器在指定时间内，无法检测到对方存活心跳信息，从而导致互相抢占对方的资源和服务所有权，然而此时两台高可用服务器有都还存活。  
&emsp; 可能出现的原因：  
1. 服务器网线松动等网络故障；  
2. 服务器硬件故障发生损坏现象而崩溃；  
3. 主备都开启了firewalld 防火墙。  
4. 在Keepalived+nginx 架构中，当Nginx宕机，会导致用户请求失败，但是keepalived不会进行切换，  
&emsp; 所以需要编写一个检测nginx的存活状态的脚本，如果nginx不存活，则kill掉宕掉的nginx主机上面的keepalived。(所有的keepalived都要配置)  

&emsp; 架构如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/system/loadBalance/keep/keep-3.png)  

&emsp; 脚本如下：  

```text
[root@lb01 /server/scripts]# cat /server/scripts/check_list 
#!/bin/sh

nginxpid=$(ps -C nginx --no-header|wc -l)
#1.判断Nginx是否存活,如果不存活则尝试启动Nginx
if [ $nginxpid -eq 0 ];then
    systemctl start nginx
    sleep 3
    #2.等待3秒后再次获取一次Nginx状态
    nginxpid=$(ps -C nginx --no-header|wc -l) 
    #3.再次进行判断, 如Nginx还不存活则停止Keepalived,让地址进行漂移,并退出脚本  
    if [ $nginxpid -eq 0 ];then
        systemctl stop keepalived
   fi
fi
[root@lb01 /server/scripts]# 
```
&emsp; 配置文件如下：  

```text
[root@lb01 /server/scripts]# cat /etc/keepalived/keepalived.conf
! Configuration File for keepalived
global_defs {
    router_id lb01
}

vrrp_script check {
    script "/server/scripts/check_list"
    interval  10


}

vrrp_instance VI_1 {
    state MASTER
    interface eth0
    virtual_router_id 50
    priority 150
    advert_int 1
    authentication {
        auth_type PASS
        auth_pass 1111
}
    virtual_ipaddress {
        10.0.0.3
    }
    track_script  {
    check
}
}
[root@lb01 /server/scripts]# 
```
