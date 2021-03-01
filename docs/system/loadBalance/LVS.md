<!-- TOC -->

- [1. LVS](#1-lvs)
    - [1.1. LVS 的组成](#11-lvs-的组成)
    - [1.2. ~~LVS安装使用~~](#12-lvs安装使用)
    - [1.3. LVS的体系结构](#13-lvs的体系结构)
    - [1.4. 三种工作模式](#14-三种工作模式)
        - [1.4.1. NAT(网络地址转换)](#141-nat网络地址转换)
        - [1.4.2. DR(直接路由)](#142-dr直接路由)
        - [1.4.3. TUN(隧道)](#143-tun隧道)
    - [1.5. LVS的十种负载调度算法](#15-lvs的十种负载调度算法)
    - [1.6. LVS优缺点](#16-lvs优缺点)

<!-- /TOC -->

# 1. LVS
<!-- 
超详细！一文带你了解 LVS 负载均衡集群！
https://mp.weixin.qq.com/s/3Ahb299iBScC3Znrc7NUNQ

LVS
https://www.cnblogs.com/cq146637/p/8517818.html
https://blog.csdn.net/ghost_leader/article/details/55827729
-->
&emsp; LVS是Linux Virtual Server的简写，意即Linux虚拟服务器，是一个虚拟的服务器集群系统。  

## 1.1. LVS 的组成  
&emsp; LVS由两部分程序组成，包括ipvs和ipvsadm。  
1. ipvs(ip virtual server)：LVS 是基于内核态的netfilter框架实现的IPVS功能，工作在内核态。用户配置VIP等相关信息并传递到IPVS就需要用到ipvsadm工具。  
2. ipvsadm：ipvsadm 是 LVS 用户态的配套工具，可以实现VIP和RS的增删改查功能，是基于 netlink 或 raw socket 方式与内核 LVS 进行通信的，如果LVS类比于netfilter，那 ipvsadm就是类似iptables工具的地位。  

## 1.2. ~~LVS安装使用~~
<!--

https://blog.csdn.net/qq_37165604/article/details/79802390
-->
linux内核2.4版本以上的基本都支持LVS，要使用lvs，只需要再安装一个lvs的管理工具：ipvsadm  

    yum install ipvsadm


&emsp; ipvsadm组件定义规则的格式：  

```text
#virtual-service-address:是指虚拟服务器的ip 地址
#real-service-address:是指真实服务器的ip 地址
#scheduler：调度方法
 
#ipvsadm 的用法和格式如下：
ipvsadm -A|E -t|u|f virutal-service-address:port [-s scheduler] [-p[timeout]] [-M netmask]
ipvsadm -D -t|u|f virtual-service-address
ipvsadm -C
ipvsadm -R
ipvsadm -S [-n]
ipvsadm -a|e -t|u|f service-address:port -r real-server-address:port [-g|i|m] [-w weight]
ipvsadm -d -t|u|f service-address -r server-address
ipvsadm -L|l [options]
ipvsadm -Z [-t|u|f service-address]
ipvsadm --set tcp tcpfin udp
ipvsadm --start-daemon state [--mcast-interface interface]
ipvsadm --stop-daemon
ipvsadm -h
 
#命令选项解释：有两种命令选项格式，长的和短的，具有相同的意思。在实际使用时，两种都可以。
-A --add-service #在内核的虚拟服务器表中添加一条新的虚拟服务器记录。也就是增加一台新的虚拟服务器。
-E --edit-service #编辑内核虚拟服务器表中的一条虚拟服务器记录。
-D --delete-service #删除内核虚拟服务器表中的一条虚拟服务器记录。
-C --clear #清除内核虚拟服务器表中的所有记录。
-R --restore #恢复虚拟服务器规则
-S --save #保存虚拟服务器规则，输出为-R 选项可读的格式
-a --add-server #在内核虚拟服务器表的一条记录里添加一条新的真实服务器记录。也就是在一个虚拟服务器中增加一台新的真实服务器
-e --edit-server #编辑一条虚拟服务器记录中的某条真实服务器记录
-d --delete-server #删除一条虚拟服务器记录中的某条真实服务器记录
-L|-l --list #显示内核虚拟服务器表
-Z --zero #虚拟服务表计数器清零(清空当前的连接数量等)
--set tcp tcpfin udp #设置连接超时值
--start-daemon #启动同步守护进程。他后面可以是master 或backup，用来说明LVS Router 是master 或是backup。在这个功能上也可以采用keepalived 的VRRP 功能。
--stop-daemon #停止同步守护进程
-h --help #显示帮助信息
 
#其他的选项:
-t --tcp-service service-address #说明虚拟服务器提供的是tcp 的服务[vip:port] or [real-server-ip:port]
-u --udp-service service-address #说明虚拟服务器提供的是udp 的服务[vip:port] or [real-server-ip:port]
-f --fwmark-service fwmark #说明是经过iptables 标记过的服务类型。
-s --scheduler scheduler #使用的调度算法，有这样几个选项rr|wrr|lc|wlc|lblc|lblcr|dh|sh|sed|nq,默认的调度算法是： wlc.
-p --persistent [timeout] #持久稳固的服务。这个选项的意思是来自同一个客户的多次请求，将被同一台真实的服务器处理。timeout 的默认值为300 秒。
-M --netmask #子网掩码
-r --real-server server-address #真实的服务器[Real-Server:port]
-g --gatewaying 指定LVS 的工作模式为直接路由模式(也是LVS 默认的模式)
-i --ipip #指定LVS 的工作模式为隧道模式
-m --masquerading #指定LVS 的工作模式为NAT 模式
-w --weight weight #真实服务器的权值
--mcast-interface interface #指定组播的同步接口
-c --connection #显示LVS 目前的连接 如：ipvsadm -L -c
--timeout #显示tcp tcpfin udp 的timeout 值 如：ipvsadm -L --timeout
--daemon #显示同步守护进程状态
--stats #显示统计信息
--rate #显示速率信息
--sort #对虚拟服务器和真实服务器排序输出
--numeric -n #输出IP 地址和端口的数字形式
```

## 1.3. LVS的体系结构  
<!-- 
https://mp.weixin.qq.com/s/3Ahb299iBScC3Znrc7NUNQ
-->
&emsp; LVS 架设的服务器集群系统有三个部分组成：  
1. 最前端的负载均衡层，用 Load Balancer 表示
2. 中间的服务器集群层，用 Server Array 表示
3. 最底端的数据共享存储层，用 Shared Storage 表示

![image](https://gitee.com/wt1814/pic-host/raw/master/images/system/loadBalance/lvs/lvs-1.png)  
&emsp; LVS的各个层次的详细介绍：  
&emsp; Load Balancer层：位于整个集群系统的最前端，有一台或者多台负载调度器(Director Server)组成，LVS模块就安装在Director Server上，而Director的主要作用类似于一个路由器，它含有完成LVS功能所设定的路由表，通过这些路由表把用户的请求分发给Server Array层的应用服务器(Real Server)上。同时，在Director Server上还要安装对Real Server服务的监控模块Ldirectord，此模块用于监测各个Real Server服务的健康状况。在Real Server不可用时把它从LVS路由表中剔除，恢复时重新加入。  
&emsp; Server Array层：由一组实际运行应用服务的机器组成，Real Server可以是Web服务器、Mail服务器、FTP服务器、DNS服务器、视频服务器中的一个或者多个，每个Real Server之间通过高速的LAN或分布在各地的WAN相连接。在实际的应用中，Director Server也可以同时兼任Real Server的角色。  
&emsp; Shared Storage层：是为所有Real Server提供共享存储空间和内容一致性的存储区域，在物理上一般由磁盘阵列设备组成，为了提供内容的一致性，一般可以通过NFS网络文件系统共享数 据，但NFS在繁忙的业务系统中，性能并不是很好，此时可以采用集群文件系统，例如Red hat的GFS文件系统、Oracle提供的OCFS2文件系统等。  

&emsp; 从整个LVS结构可以看出，Director Server是整个LVS的核心，目前用于Director Server的操作系统只能是Linux和FreeBSD，Linux2.6内核不用任何设置就可以支持LVS功能，而FreeBSD作为 Director Server的应用还不是很多，性能也不是很好。对于Real Server，几乎可以是所有的系统平台，Linux、windows、Solaris、AIX、BSD系列都能很好地支持。  

## 1.4. 三种工作模式  
<!-- 
https://blog.csdn.net/qq_37165604/article/details/79802390

https://www.cnblogs.com/lixigang/p/5371815.html
https://mp.weixin.qq.com/s/3Ahb299iBScC3Znrc7NUNQ
-->
&emsp; LVS是四层(传输层)负载均衡，LVS支持TCP/UDP的负载均衡。  
&emsp; LVS 的转发主要通过修改 IP 地址(NAT 模式，分为源地址修改SNAT和目标地址修改 DNAT)、修改目标 MAC(DR 模式)来实现。  

### 1.4.1. NAT(网络地址转换)  
&emsp; NAT(Network Address Translation)是一种外网和内网地址映射的技术。  
&emsp; NAT 模式下，网络数据报的进出都要经过 LVS 的处理。LVS 需要作为 RS(真实服务器)的网关。  
&emsp; **工作方式：**  
<!-- 
1.用户请求VIP(也可以说是CIP请求VIP)
 
2,Director Server 收到用户的请求后,发现源地址为CIP请求的目标地址为VIP,那么Director Server会认为用户请求的是一个集群服务,那么Director Server 会根据此前设定好的调度算法将用户请求负载给某台Real Server。
  假如说此时Director Server 根据调度的结果会将请求分摊到RealServer1上去,那么Director Server 会将用户的请求报文中的目标地址,从原来的VIP改为RealServer1的IP,然后再转发给RealServer1
 
3,此时RealServer1收到一个源地址为CIP目标地址为自己的请求,那么RealServer1处理好请求后会将一个源地址为自己目标地址为CIP的数据包通过Director Server 发出去,
 
4.当Driector Server收到一个源地址为RealServer1 的IP 目标地址为CIP的数据包,此时Driector Server 会将源地址修改为VIP,然后再将数据包发送给用户
-->
![image](https://gitee.com/wt1814/pic-host/raw/master/images/system/loadBalance/lvs/lvs-2.png)  
&emsp; 当包到达 LVS 时，LVS 做目标地址转换(DNAT)，将目标 IP 改为 RS 的 IP。RS 接收到包以后，仿佛是客户端直接发给它的一样。RS 处理完，返回响应时，源 IP 是 RS IP，目标 IP 是客户端的 IP。这时 RS 的包通过网关(LVS)中转，LVS 会做源地址转换(SNAT)，将包的源地址改为 VIP，这样，这个包对客户端看起来就仿佛是 LVS 直接返回给它的。  

&emsp; **LVS-NAT的性能瓶颈：**  
&emsp; 在LVS/NAT的集群系统中，请求和响应的数据报文都需要通过负载调度器(Director)，当真实服务器(RealServer)的数目在10台和20台之间时，负载调度器(Director)将成为整个集群系统的新瓶颈。  
&emsp; 大多数Internet服务都有这样的特点：请求报文较短而响应报文往往包含大量的数据。如果能将请求和响应分开处理，即在负载调度器(Director)中只负责调度请求而响应直接(RealServer)返回给客户，将极大地提高整个集群系统的吞吐量。  

&emsp; **部署：**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/system/loadBalance/lvs/lvs-3.png)  
<!-- 
https://blog.csdn.net/qq_37165604/article/details/79802390
-->

### 1.4.2. DR(直接路由)  
&emsp; DR 模式下需要 LVS 和 RS 集群绑定同一个 VIP(RS 通过将 VIP 绑定在 loopback 实现)，但与 NAT 的不同点在于：请求由 LVS 接受，由真实提供服务的服务器(RealServer，RS)直接返回给用户，返回的时候不经过 LVS。  
&emsp; 详细来看，一个请求过来时，LVS 只需要将网络帧的 MAC 地址修改为某一台 RS 的 MAC，该包就会被转发到相应的 RS 处理，注意此时的源 IP 和目标 IP 都没变，LVS 只是做了一下移花接木。RS 收到 LVS 转发来的包时，链路层发现 MAC 是自己的，到上面的网络层，发现 IP 也是自己的，于是这个包被合法地接受，RS 感知不到前面有 LVS 的存在。而当 RS 返回响应时，只要直接向源 IP(即用户的 IP)返回即可，不再经过 LVS。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/system/loadBalance/lvs/lvs-4.png)  
&emsp; DR 负载均衡模式数据分发过程中不修改 IP 地址，只修改 mac 地址，由于实际处理请求的真实物理 IP 地址和数据请求目的 IP 地址一致，所以不需要通过负载均衡服务器进行地址转换，可将响应数据包直接返回给用户浏览器，避免负载均衡服务器网卡带宽成为瓶颈。因此，DR 模式具有较好的性能，也是目前大型网站使用最广泛的一种负载均衡手段。  

&emsp; **编辑DR有三种方式(目的是让用户请求的数据都通过Director Server)**  
&emsp; 第一种方式：在路由器上明显说明vip对应的地址一定是Director上的MAC，只要绑定，以后再跟vip通信也不用再请求了，这个绑定是静态的，所以它也不会失效，也不会再次发起请求，但是有个前提，我们的路由设备必须有操作权限能够绑定MAC地址，万一这个路由器是运行商操作的，我们没法操作怎么办？第一种方式固然很简便，但未必可行。  
&emsp; 第二种方式：在给别主机上(例如：红帽)它们引进的有一种程序arptables,它有点类似于iptables,它肯定是基于arp或基于MAC做访问控制的，很显然我们只需要在每一个real server上定义arptables规则，如果用户arp广播请求的目标地址是本机的vip则不予相应，或者说相应的报文不让出去，很显然网关(gateway)是接受不到的，也就是director相应的报文才能到达gateway，这个也行。第二种方式我们可以基于arptables。  
&emsp; 第三种方式：在相对较新的版本中新增了两个内核参数(kernelparameter)，第一个是arp_ignore定义接受到ARP请求时的相应级别;第二个是arp_announce定义将自己地址向外通告是的通告级别。【提示：很显然我们现在的系统一般在内核中都是支持这些参数的，我们用参数的方式进行调整更具有朴实性，它还不依赖于额外的条件，像arptables,也不依赖外在路由配置的设置，反而通常我们使用的是第三种配置】  

&emsp; arp_ignore：定义接受到ARP请求时的相应级别。  

```text
0：只要本地配置的有相应地址，就给予响应。
1：仅在请求的目标地址配置请求到达的接口上的时候，才给予响应(当别人的arp请求过来的时候，如果接收的设备上面没有这个ip，就不响应，默认是0，只要这台机器上面任何一个设备上面有这个ip，就响应arp请求，并发送MAC地址应答。)
2：只回答目标IP地址是来访网络接口本地地址的ARP查询请求,且来访IP必须在该网络接口的子网段内
3：不回应该网络界面的arp请求，而只对设置的唯一和连接地址做出回应
4-7：保留未使用
8：不回应所有(本地地址)的arp查询
```

&emsp; arp_announce：定义将自己地址向外通告是的通告级别。  

```text
0: 将本地任何接口上的任何地址向外通告
1：试图仅想目标网络通告与其网络匹配的地址
2：仅向与本地借口上地址匹配的网络进行通告
```

&emsp; **部署：**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/system/loadBalance/lvs/lvs-5.png)  

### 1.4.3. TUN(隧道)  
&emsp; **工作方式：**  
&emsp; TUN的工作机制跟DR一样，只不过在转发的时候，它需要重新包装IP报文。这里的real server(图中为RIP)离得都比较远。  

&emsp; 用户请求以后，到director上的VIP上，它跟DR模型一样，每个realserver上既有RIP又有VIP，Director就挑选一个real server进行响应，但director和real server并不在同一个网络上，这时候就用到隧道了，Director进行转发的时候，一定要记得CIP和VIP不能动。  
&emsp; 转发是这样的，让它的CIP和VIP不动，在它上面再加一个IP首部，再加的IP首部源地址是DIP，目标地址的RIP的IP地址。收到报文的RIP，拆掉报文以后发现了里面还有一个封装，它就知道了，这就是隧道。  
&emsp; 其实数据转发原理和DR是一样的，不过这个我个人认为主要是位于不同位置(不同机房)；LB是通过隧道进行了信息传输，虽然增加了负载，可是因为地理位置不同的优势，还是可以参考的一种方案；  

```text
优点：负载均衡器只负责将请求包分发给物理服务器，而物理服务器将应答包直接发给用户。所以，负载均衡器能处理很巨大的请求量，这种方式，一台负载均衡能为超过100台的物理服务器服务，负载均衡器不再是系统的瓶颈。
     使用VS-TUN方式，如果你的负载均衡器拥有100M的全双工网卡的话，就能使得整个Virtual Server能达到1G的吞吐量。
不足：但是，这种方式需要所有的服务器支持"IP Tunneling"(IP Encapsulation)协议；
```
&emsp; **LVS的健康状态检查**  
&emsp; 在LVS模型中，director不负责检查RS的健康状况，这就使得当有的RS出故障了，director还会将服务请求派发至此服务器，这种情况对用户、企业都是很不爽的，哪个用户倒霉说不定就遇到类似了。  
&emsp; 为了让Director更人性化、可靠还要给director提供健康检查功能；如何实现？Director没有自带检查工具，只有手动编写脚本给director实现健康状态检查功能！  

&emsp; **部署：**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/system/loadBalance/lvs/lvs-6.png)  

## 1.5. LVS的十种负载调度算法  
<!-- 
https://mp.weixin.qq.com/s/3Ahb299iBScC3Znrc7NUNQ
https://blog.csdn.net/qq_37165604/article/details/79802390
-->

&emsp; lvs调度算法(不区分大小写)可以分为两大类：  
1. Fixed Scheduling Method 静态调服方法  
    * 轮询：Round Robin，将收到的访问请求按顺序轮流分配给群集中的各节点真实服务器中，不管服务器实际的连接数和系统负载。  
    * 加权轮询：Weighted Round Robin，根据真实服务器的处理能力轮流分配收到的访问请求，调度器可自动查询各节点的负载情况，并动态跳转其权重，保证处理能力强的服务器承担更多的访问量。  
    * 目标地址散列调度算法：DH，该算法是根据目标 IP 地址通过散列函数将目标 IP 与服务器建立映射关系，出现服务器不可用或负载过高的情况下，发往该目标 IP 的请求会固定发给该服务器。  
    * 源地址散列调度算法：SH，与目标地址散列调度算法类似，但它是根据源地址散列算法进行静态分配固定的服务器资源。  
2. Dynamic Scheduling Method 动态调服方法  
    * 最少连接：Least Connections，根据真实服务器已建立的连接数进行分配，将收到的访问请求优先分配给连接数少的节点，如所有服务器节点性能都均衡，可采用这种方式更好的均衡负载。  
    * 加权最少连接：Weighted Least Connections，服务器节点的性能差异较大的情况下，可以为真实服务器自动调整权重，权重较高的节点将承担更大的活动连接负载。  
    * 基于局部性的最少连接：LBLC，基于局部性的最少连接调度算法用于目标 IP 负载平衡，通常在高速缓存群集中使用。如服务器处于活动状态且处于负载状态，此算法通常会将发往 IP 地址的数据包定向到其服务器。如果服务器超载(其活动连接数大于其权重)，并且服务器处于半负载状态，则将加权最少连接服务器分配给该 IP 地址。  
    * 复杂的基于局部性的最少连接：LBLCR，具有复杂调度算法的基于位置的最少连接也用于目标IP负载平衡，通常在高速缓存群集中使用。与 LBLC 调度有以下不同：负载平衡器维护从目标到可以为目标提供服务的一组服务器节点的映射。对目标的请求将分配给目标服务器集中的最少连接节点。如果服务器集中的所有节点都超载，则它将拾取群集中的最少连接节点，并将其添加到目标服务器群中。如果在指定时间内未修改服务器集群，则从服务器集群中删除负载最大的节点，以避免高度负载。  
    * 最短延迟调度：SED，最短的预期延迟调度算法将网络连接分配给具有最短的预期延迟的服务器。如果将请求发送到第 i 个服务器，则预期的延迟时间为(Ci +1)/ Ui，其中 Ci 是第 i 个服务器上的连接数，而 Ui 是第 i 个服务器的固定服务速率(权重) 。  
    * 永不排队调度：NQ，从不队列调度算法采用两速模型。当有空闲服务器可用时，请求会发送到空闲服务器，而不是等待快速响应的服务器。如果没有可用的空闲服务器，则请求将被发送到服务器，以使其预期延迟最小化(最短预期延迟调度算法)。  

## 1.6. LVS优缺点  
&emsp; **LVS 的优点**  

* 抗负载能力强、是工作在传输层上仅作分发之用，没有流量的产生，这个特点也决定了它在负载均衡软件里的性能最强的，对内存和 cpu 资源消耗比较低。
* 配置性比较低，这是一个缺点也是一个优点，因为没有可太多配置的东西，所以并不需要太多接触，大大减少了人为出错的几率。
* 工作稳定，因为其本身抗负载能力很强，自身有完整的双机热备方案，如 LVS + Keepalived。
* 无流量，LVS 只分发请求，而流量并不从它本身出去，这点保证了均衡器 IO 的性能不会受到大流量的影响。
* 应用范围比较广，因为 LVS 工作在传输层，所以它几乎可以对所有应用做负载均衡，包括 http、数据库、在线聊天室等等。

&emsp; **LVS 的缺点**  

* 软件本身不支持正则表达式处理，不能做动静分离；而现在许多网站在这方面都有较强的需求，这个是 Nginx、HAProxy + Keepalived 的优势所在。
* 如果是网站应用比较庞大的话，LVS/DR + Keepalived 实施起来就比较复杂了，相对而言，Nginx / HAProxy + Keepalived 就简单多了。


