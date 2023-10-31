
<!-- TOC -->

- [1. 容器间通信](#1-容器间通信)
    - [1.1. 问题](#11-问题)
    - [1.2. Docker的网络基础](#12-docker的网络基础)
    - [1.3. Docker的网络实现：Docker四种网络模式](#13-docker的网络实现docker四种网络模式)
    - [1.4. 容器间单向通信(Link)](#14-容器间单向通信link)
    - [1.5. 容器间双向通信(bridge)](#15-容器间双向通信bridge)
        - [1.5.1. Docker中的虚拟网桥](#151-docker中的虚拟网桥)
        - [1.5.2. 借助网桥进行容器间通信](#152-借助网桥进行容器间通信)
        - [1.5.3. 网桥通信原理](#153-网桥通信原理)
    - [1.6. 不同主机间容器通信](#16-不同主机间容器通信)

<!-- /TOC -->


# 1. 容器间通信  
<!-- 

Docker使用Link与newwork在容器之间建立连接
https://www.bbsmax.com/A/RnJWwE8YJq/
clickhouse docker, as it does not belong to the default network
https://blog.csdn.net/The_Time_Runner/article/details/114434710


使用已存在的网络
https://blog.csdn.net/gezhonglei2007/article/details/51627969

-->

--link clickhouse_name:clickhouse-server --network compose_default   

--link  和 --network

<!-- 

Docker容器网络
http://www.itmuch.com/docker/15-docker-network/
network命令
http://www.itmuch.com/docker/16-docker-network-command/
默认bridge网络中配置DNS
http://www.itmuch.com/docker/17-docker-bridge-dns/
用户定义网络中的内嵌DNS服务器
http://www.itmuch.com/docker/18-docker-user-network-embeded-dns/
-->


## 1.1. 问题  
<!-- 

Docker使用Link与newwork在容器之间建立连接
https://www.bbsmax.com/A/RnJWwE8YJq/
-->

## 1.2. Docker的网络基础
<!-- 
k8s权威指南  3.7.2章节  
-->


## 1.3. Docker的网络实现：Docker四种网络模式  
<!-- 
初探Docker的网络模式 
https://www.docker.org.cn/docker/187.html
-->
&emsp; docker run创建Docker容器时，可以用 --net 选项指定容器的网络模式，Docker 有以下 4 种网络模式：

* host模式，使用 --net=host 指定。
* container模式，使用 --net=container:NAMEorID 指定。
* none模式，使用 --net=none 指定。
* bridge模式，使用 --net=bridge 指定，默认设置。

&emsp; 可以使用docker network ls查看当前主机的网络模式。  
![image](http://182.92.69.8:8081/img/devops/docker/docker-44.png)  

* host 模式  
&emsp; 如果启动容器的时候使用 host 模式，那么这个容器将不会获得一个独立的Network Namespace，而是和宿主机共用一个Network Namespace。容器将不会虚拟出自己的网卡，配置自己的IP等，而是使用宿主机的IP和端口。  
&emsp; 例如，在 10.10.101.105/24 的机器上用 host 模式启动一个含有 web 应用的Docker容器，监听tcp 80端口。当容器中执行任何类似 ifconfig 命令查看网络环境时，看到的都是宿主机上的信息。而外界访问容器中的应用，则直接使用 10.10.101.105:80 即可，不用任何 NAT 转换，就如直接跑在宿主机中一样。但是，容器的其他方面，如文件系统、进程列表等还是和宿主机隔离的。  
* container 模式  
&emsp; 这个模式指定新创建的容器和已经存在的一个容器共享一个Network Namespace，而不是和宿主机共享。新创建的容器不会创建自己的网卡，配置自己的IP，而是和一个指定的容器共享IP、端口范围等。同样，两个容器除了网络方面，其他的如文件系统、进程列表等还是隔离的。两个容器的进程可以通过 lo 网卡设备通信。  
* none模式  
&emsp; 这个模式和前两个不同。在这种模式下，Docker 容器拥有自己的 Network Namespace，但是，并不为 Docker容器进行任何网络配置。也就是说，这个Docker容器没有网卡、IP、路由等信息。需要为Docker容器添加网卡、配置IP等。  
* bridge模式  
&emsp; bridge 模式是 Docker 默认的网络设置，此模式会为每一个容器分配 Network Namespace、设置 IP 等，并将一个主机上的 Docker 容器连接到一个虚拟网桥上。当 Docker server 启动时，会在主机上创建一个名为 docker0 的虚拟网桥，此主机上启动的 Docker 容器会连接到这个虚拟网桥上。虚拟网桥的工作方式和物理交换机类似，这样主机上的所有容器就通过交换机连在了一个二层网络中。接下来就要为容器分配 IP 了，Docker 会从 RFC1918 所定义的私有 IP 网段中，选择一个和宿主机不同的IP地址和子网分配给 docker0，连接到 docker0 的容器就从这个子网中选择一个未占用的 IP 使用。如一般 Docker 会使用 172.17.0.0/16 这个网段，并将 172.17.42.1/16 分配给 docker0 网桥(在主机上使用 ifconfig 命令是可以看到 docker0 的，可以认为它是网桥的管理接口，在宿主机上作为一块虚拟网卡使用)。   

## 1.4. 容器间单向通信(Link)  
&emsp; 在docker环境下，容器创建后，都会默认分配一个虚拟ip，该ip无法从外界直接访问，但是在docker环境下各个ip直接是互通互联的。下图假设Tomcat分配的虚拟ip为107.1.31.22，Mysql分配的虚拟ip为107.1.31.24。  
&emsp; 虽然在Docker环境下可以通过虚拟ip互相访问，但是局限性很大，原因是容器是随时可能宕机，重启的，宕机重启后会为容器重新分配ip，这样原来直接通信的ip就消失了。所以容器间通信不建议通过ip进行通信。  
![image](http://182.92.69.8:8081/img/devops/docker/docker-42.png)  
&emsp; 可以通过为容器命名，通过名称通信，这样无论该容器重启多少次，ip如何改变，都不会存在通信不可用的情形。  

* 通过docker run -d --name myweb tomcat命令，使用tomcat镜像运行一个名称为myweb的容器。通过docker run -d --name mydatabases mysql命令，使用mysql镜像运行一个名称为mydatabases的容器。  
* 通过docker inspect myweb查看myweb容器的详细配置。其中NetworkSettings下的IPAddress的信息即为docker为该容器分配的虚拟ip地址。  

&emsp; 记录mydatabases容器的ip地址，进入myweb容器，用ping命令访问mydatabases的ip。发现是没问题的，说明docker之间运用虚拟ip地址可以直接互相访问。  

&emsp; **配置容器间通过名称访问**

* 把之前的myweb容器。运用docker rm myweb移除掉，保留mydatabases容器。通过docker run -d --name myweb --link mydatabases tomcat来运行容器。其中--link是指定该容器需要和名称为databases的容器通信。  
* 进入myweb容器docker exec -it myweb sh,运行命令ping mydatabases，即可通信。配置mysql连接的时候，把ip换成mydatabases即可，docker会自动维护mydatabases和该容器ip的映射，即使该容器的ip改变也不会影响访问。  

## 1.5. 容器间双向通信(bridge)  
&emsp; 通过上文，配置容器间互相link，也是可以实现双向通信的，但是有点麻烦。Docker提供了网桥，用来快速实现容器间双向通信。  

### 1.5.1. Docker中的虚拟网桥  
&emsp; docker网桥组件概览图：  
![image](http://182.92.69.8:8081/img/devops/docker/docker-41.png)  
&emsp; Docker中的网桥也是虚拟出来的，网桥的主要用途是用来实现docker环境和外部的通信。在某个容器内部ping外部的ip例如百度。是可以ping通的。实际上是容器发出的数据包，通过虚拟网桥，发送给宿主机的物理网卡，实际上是借助物理网卡与外界通信的，反之物理网卡也会通过虚拟网桥把相应的数据包送回给相应的容器。  

### 1.5.2. 借助网桥进行容器间通信
&emsp; 可以借助网桥实现容器间的双向通信。docker虚拟网桥的另一个作用是为容器在网络层面上进行分组，把不同容器都绑定到网桥上，这样容器间就可以天然互联互通。  

* docker run -d --name myweb tomcat运行myweb容器；docker run -d -it --name mydatabases centos sh交互模式挂起一个databases容器(这里用一个linux容器模拟数据库服务器)。由于没配置网桥，此时容器间无法通过名称互相通信  
* docker network ls列出docker网络服务明细，其中bridge的条目就是docker默认的网桥。接着新建立一个网桥docker network create -d bridge my-bridge命名为my-bridge  
* 把容器与新建立的my-bridge网桥进行绑定: docker network connect my-bridge myweb,同理: docker network connect my-bridge mydatabases。需要让哪些容器互联互通，就把容器绑定到该网桥上,用来标识这些容器同处在my-bridge网桥的分组中。至此容器间可以通过名称互相访问  

```text
~/ docker network ls
NETWORK ID          NAME                DRIVER              SCOPE
a31f18ae4b97        bridge              bridge              local
9e311308c3ce        host                host                local
2c5f89509739        none                null                local
~/ docker network create -d bridge my-bridge
5e678ed577b120f0d95e87ce43d44bab8e15e47f4002428168cad61120c54cc7
~/ docker network ls
NETWORK ID          NAME                DRIVER              SCOPE
a31f18ae4b97        bridge              bridge              local
9e311308c3ce        host                host                local
5e678ed577b1        my-bridge           bridge              local
2c5f89509739        none                null                local
~/ docker network connect my-bridge myweb
~/ docker network connect my-bridge mydatabases
~/ docker ps
CONTAINER ID        IMAGE               COMMAND             CREATED             STATUS              PORTS               NAMES
92888ef080a9        centos              "sh"                2 minutes ago       Up 2 minutes                            mydatabases
03fc2187d4ef        tomcat              "catalina.sh run"   43 minutes ago      Up 43 minutes       8080/tcp            myweb
~/ docker exec -it 03fc2187d4ef sh
# ping mydatabases
PING mydatabases (172.18.0.3) 56(84) bytes of data.
64 bytes from mydatabases.my-bridge (172.18.0.3): icmp_seq=1 ttl=64 time=0.278 ms
64 bytes from mydatabases.my-bridge (172.18.0.3): icmp_seq=2 ttl=64 time=0.196 ms
64 bytes from mydatabases.my-bridge (172.18.0.3): icmp_seq=3 ttl=64 time=0.417 ms
^C
--- mydatabases ping statistics ---
3 packets transmitted, 3 received, 0% packet loss, time 55ms
rtt min/avg/max/mdev = 0.196/0.297/0.417/0.091 ms
# exit
```

### 1.5.3. 网桥通信原理  
&emsp; 网桥为什么可以实现容器间的互联互通？实际上，每当创建一个网桥，docker都会在宿主机上安装一个虚拟网卡，该虚拟网卡也充当了一个网关的作用。与该网桥(虚拟网关)绑定的容器，相当于处在一个局域网，所以可以通信。虚拟网卡毕竟是虚拟的，如果容器想要和外部通信，仍然需要借助外部(宿主机)的物理网卡。虚拟网卡的数据包进行地址转换，转换为物理网卡的数据包发送出去，反之外部和内部容器通信，也需要进行数据包地址转换。  
![image](http://182.92.69.8:8081/img/devops/docker/docker-44.png)  

## 1.6. 不同主机间容器通信  
<!-- 
docker同宿主机容器和不同宿主机容器之间怎么通信？
https://blog.51cto.com/2367685/2349762
Docker学习笔记：Docker 网络配置 
https://www.docker.org.cn/dockerppt/111.html
-->
