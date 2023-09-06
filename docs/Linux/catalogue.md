
<!-- TOC -->

- [1. Linux](#1-linux)
    - [1.1. Linux系统目录结构](#11-linux系统目录结构)
    - [1.2. Linux版本](#12-linux版本)
        - [1.2.1. 发行版本](#121-发行版本)
        - [1.2.2. 版本比较](#122-版本比较)

<!-- /TOC -->


# 1. Linux

## 1.1. Linux系统目录结构  
<!-- 

https://www.runoob.com/linux/linux-system-contents.html
-->

* /home：用户的主目录，在 Linux 中，每个用户都有一个自己的目录，一般该目录名是以用户的账号命名的，如上图中的 alice、bob 和 eve。  
* /usr：usr 是 unix shared resources(共享资源) 的缩写，这是一个非常重要的目录，用户的很多应用程序和文件都放在这个目录下，类似于 windows 下的 program files 目录。  
* /var：var 是 variable(变量) 的缩写，这个目录中存放着在不断扩充着的东西，我们习惯将那些经常被修改的目录放在这个目录下。包括各种日志文件。  






## 1.2. Linux版本  
<!-- 
https://baike.baidu.com/item/Linux%E7%89%88%E6%9C%AC/3345274?fr=aladdin

-->


### 1.2.1. 发行版本
&emsp; 简介  
&emsp; 发行版为许多不同的目的而制作, 包括对不同计算机结构的支持, 对一个具体区域或语言的本地化，实时应用，和嵌入式系统，甚至许多版本故意地只加入免费软件。已经有超过三百个发行版被积极的开发，最普遍被使用的发行版有大约十二个。  

&emsp; Fedora Core  
&emsp; Fedora Core（自第七版直接更名为Fedora）是众多 Linux 发行版之一。它是一套从Red Hat Linux发展出来的免费Linux系统。Fedora Core 的前身就是Red Hat Linux。Fedora是一个开放的、创新的、前瞻性的操作系统和平台，基于Linux。它允许任何人自由地使用、修改和重发布，无论现在还是将来。它由一个强大的社群开发，这个社群的成员以自己的不懈努力，提供并维护自由、开放源码的软件和开放的标准。Fedora 项目由 Fedora 基金会管理和控制，得到了 Red Hat, Inc. 的支持。Fedora 是一个独立的操作系统，是Linux的一个发行版，可运行的体系结构包括 x86(即i386-i686), x86_64 和 PowerPC。  

&emsp; Debian  
&emsp; Debian Project诞生于1993年8月13日，它的目标是提供一个稳定容错的Linux版本。支持Debian的不是某家公司，而是许多在其改进过程中投入了大量时间的开发人员，这种改进吸取了早期Linux的经验。  
Debian以其稳定性著称，虽然它的早期版本Slink有一些问题，但是它的现有版本Potato已经相当稳定了。这个版本更多的使用了 pluggable authentication modules (PAM)，综合了一些更易于处理的需要认证的软件（如winbind for Samba）。  
&emsp; Debian的安装完全是基于文本的，对于其本身来说这不是一件坏事。但对于初级用户来说却并非这样。因为它仅仅使用fdisk 作为分区工具而没有自动分区功能，所以它的磁盘分区过程令人十分讨厌。磁盘设置完毕后，软件工具包的选择通过一个名为dselect的工具实现，但它不向用户提供安装基本工具组（如开发工具）的简易设置步骤。最后需要使用anXious工具配置X Windows，这个过程与其他版本的X Windows配置过程类似。完成这些配置后，Debian就可以使用了。  
&emsp; Debian主要通过基于Web的论坛和邮件列表来提供技术支持。作为服务器平台，Debian提供一个稳定的环境。为了保证它的稳定性，开发者不会在其中随意添加新技术，而是通过多次测试之后才选定合适的技术加入。当前最新正式版本是Debian 6，采用的内核是Linux 2.6.32。Debian 6 第一次 包含了一个100%开源的Linux内核，这个内核中不再包含任何闭源的硬件驱动。所有的闭源软件都被隔离成单独的软件包，放到Debian软件源的 "non-free" 部分。由此，Debian用户便可以自由地选择是使用一个完全开源的系统还是添加一些闭源驱动。  

&emsp; Mandrake  
&emsp; MandrakeSoft，Linux Mandrake的发行商，在1998年由一个推崇Linux的小组创立，它的目标是尽量让工作变得更简单。最终，Mandrake给人们提供了一个优秀的图形安装界面，它的最新版本还包含了许多Linux软件包。  
&emsp; 作为Red Hat Linux的一个分支，Mandrake将自己定位在桌面市场的最佳Linux版本上。但该公司还是支持服务器上的安装，而且成绩并不坏。Mandrake的安装非常简单明了，为初级用户设置了简单的安装选项。它完全使用GUI界面，还为磁盘分区制作了一个适合各类用户的简单GUI界面。软件包的选择非常标准，另外还有对软件组和单个工具包的选项。安装完毕后，用户只需重启系统并登录进入即可。  
&emsp; Mandrake主要通过邮件列表和Mandrak 自己的Web论坛提供技术支持。Mandrak对桌面用户来说是一个非常不错的选择，它还可作为一款优秀的服务器系统，尤其适合Linux新手使用。它使用最新版本的内核，拥有许多用户需要在Linux服务器环境中使用的软件——数据库和Web服务器。  
&emsp; Mandrak没有重大的软件缺陷，只是它更加关注桌面市场，较少关注服务器市场。  

&emsp; Ubuntu  
&emsp; Ubuntu是一个以桌面应用为主的Linux操作系统，其名称来自非洲南部祖鲁语或豪萨语的“ubuntu”一词（译为吾帮托或乌班图），意思是“人性”、“我的存在是因为大家的存在”，是非洲传统的一种价值观，类似华人社会的“仁爱”思想。Ubuntu基于Debian发行版和unity桌面环境，与Debian的不同在于它每6个月会发布一个新版本。Ubuntu的目标在于为一般用户提供一个最新的、同时又相当稳定的主要由自由软件构建而成的操作系统。Ubuntu具有庞大的社区力量，用户可以方便地从社区获得帮助。随着云计算的流行，ubuntu推出了一个云计算环境搭建的解决方案，可以在其官方网站找到相关信息。于2012年4月26日发布最终版ubuntu 12.04，ubuntu 12.04是长期支持的版本。  

&emsp; Red Hat Linux  
&emsp; 可能这是最著名的Linux版本了，Red Hat Linux已经创造了自己的品牌，越来越多的人听说过它。Red Hat在1994年创业，当时聘用了全世界500多名员工，他们都致力于开放的源代码体系。  
Red Hat Linux是公共环境中表现上佳的服务器。它拥有自己的公司，能向用户提供一套完整的服务，这使得它特别适合在公共网络中使用。这个版本的Linux也使用最新的内核，还拥有大多数人都需要使用的主体软件包。  
Red Hat Linux的安装过程也十分简单明了。它的图形安装过程提供简易设置服务器的全部信息。磁盘分区过程可以自动完成，还可以选择GUI工具完成，即使对于 Linux新手来说这些都非常简单。选择软件包的过程也与其他版本类似；用户可以选择软件包种类或特殊的软件包。系统运行起来后，用户可以从Web站点和 Red Hat那里得到充分的技术支持。我发现Red Hat是一个符合大众需求的最优版本。在服务器和桌面系统中它都工作得很好。Red Hat的唯一缺陷是带有一些不标准的内核补丁，这使得它难于按用户的需求进行定制。 Red Hat通过论坛和邮件列表提供广泛的技术支持，它还有自己公司的电话技术支持，后者对要求更高技术支持水平的集团客户更有吸引力。  

&emsp; SuSE  
&emsp; 总部设在德国的SuSE AG在商界已经奋斗了8年多，它一直致力于创建一个连接数据库的最佳Linux版本。为了实现这一目的，SuSE与Oracle 和IBM合作，以使他们的产品能稳定地工作。SuSE还开发了SuSE Linux eMail Server III，一个非常稳定的电子邮件群组应用。  
&emsp; 基于2.4.10内核的SuSE 7.3，在原有版本的基础上提高了易用性。安装过程通过GUI完成，磁盘分区过程也非常简单，但它没有为用户提供更多的控制和选择。  
&emsp; 在SuSE 操作系统下，可以非常方便地访问Windows磁盘，这使得两种平台之间的切换，以及使用双系统启动变得更容易。SuSE的硬件检测非常优秀，该版本在服务器和工作站上都用得很好。  
&emsp; SuSE拥有界面友好的安装过程，还有图形管理工具，可方便地访问Windows磁盘，对于终端用户和管理员来说使用它同样方便，这使它成为了一个强大的服务器平台。 SuSE也通过基于Web的论坛提供技术支持，另外我还发现它有电话技术支持。  

&emsp; Linux Mint  
&emsp; Linux Mint是一份基于Ubuntu的发行版，其目标是提供一种更完整的即刻可用体验，这包括提供浏览器插件、多媒体编解码器、对DVD播放的支持、Java和其他组件。它与Ubuntu软件仓库兼容。Linux Mint 是一个为pc和X86电脑设计的操作系统。  
&emsp; 因此，一个可以跑得动Windows的电脑也可以使用Linux Mint来代替Windows，或者两个都跑。既有Windows又有Linux的系统就是传说中的“双系统”。同样，MAC，BSD或者其他的Linux版本也可以和Linux Mint 共存。一台装有多系统的电脑在开机的时候会出现一个供你选择操作系统的菜单。Linux Mint可以很好的在一个单系统的电脑上运行，但是它也可以自动检测其他操作系统并与其互动，例如，如果你安装Linux Mint在一个安装了Windows版本的（xp，vista或者其他版本），它会自动检测并建立双启动以供您在开机的时候选择启动哪个系统。并且你可以在Linux Mint下访问Windows分区。Linux是更安全，更稳定，更有效并且日益易于操作的甚至可以和Windows相媲美的系统，它越来越让人感到难以抉择了。  

&emsp; Gentoo  
&emsp; Gentoo是Linux世界最年轻的发行版本，正因为年轻，所以能吸取在她之前的所有发行版本的优点。Gentoo最初由Daniel Robbins（FreeBSD的开发者之一）创建，首个稳定版本发布于2002年。由于开发者对FreeBSD的熟识，所以Gentoo拥有媲美FreeBSD的广受美誉的ports系统 ——Portage包管理系统。  

&emsp; centos  
&emsp; CentOS（Community ENTerprise Operating System）是Linux发行版之一，它是来自于Red Hat Enterprise Linux依照开放源代码规定释出的源代码所编译而成。由于出自同样的源代码，因此有些要求高度稳定性的服务器以CentOS替代商业版的Red Hat Enterprise Linux使用。两者的不同，在于CentOS并不包含封闭源代码软件,CentOS 是一个基于Red Hat Linux 提供的可自由使用源代码的企业级Linux发行版本。每个版本的 CentOS都会获得十年的支持（通过安全更新方式）。新版本的 CentOS 大约每两年发行一次，而每个版本的 CentOS 会定期（大概每六个月）更新一次，以便支持新的硬件。这样，建立一个安全、低维护、稳定、高预测性、高重复性的 Linux 环境。CentOS是Community Enterprise Operating System的缩写。  
&emsp; CentOS 是RHEL（Red Hat Enterprise Linux）源代码再编译的产物，而且在RHEL的基础上修正了不少已知的 Bug ，相对于其他 Linux 发行版，其稳定性值得信赖。  
&emsp; RHEL 在发行的时候，有两种方式。一种是二进制的发行方式，另外一种是源代码的发行方式。  


### 1.2.2. 版本比较  
&emsp; Linux的发行版本可以大体分为两类，一类是商业公司维护的发行版本，一类是社区组织维护的发行版本，前者以著名的Redhat（RHEL）为代表，后者以Debian为代表。  
&emsp; 下面介绍一下各个发行版本的特点：  
&emsp; Redhat，应该称为Redhat系列，包括RHEL(Redhat Enterprise Linux，也就是所谓的Redhat Advance Server收费版本)、FedoraCore(由原来的Redhat桌面版本发展而来，免费版本)、CentOS(RHEL的社区克隆版本，免费)。Redhat应该说是在国内使用人群最多的Linux版本，甚至有人将Redhat等同于Linux，而有些老鸟更是只用这一个版本的Linux。所以这个版本的特点就是使用人群数量大，资料非常多，言下之意就是如果你有什么不明白的地方，很容易找到人来问，而且网上的一般Linux教程都是以Redhat为例来讲解的。Redhat系列的包管理方式采用的是基于RPM包的YUM包管理方式，包分发方式是编译好的二进制文件。稳定性方面RHEL和CentOS的稳定性非常好，适合于服务器使用，但是Fedora Core的稳定性较差，最好只用于桌面应用。  
&emsp; Debian，或者称Debian系列，包括Debian和Ubuntu等。Debian是社区类Linux的典范，是迄今为止最遵循GNU规范的Linux系统。Debian最早由Ian Murdock于1993年创建，分为三个版本分支（branch）： stable, testing和unstable。其中，unstable为最新的测试版本，其中包括最新的软件包，但是也有相对较多的bug，适合桌面用户。testing的版本都经过unstable中的测试，相对较为稳定，也支持了不少新技术（比如SMP等）。而stable一般只用于服务器，上面的软件包大部分都比较过时，但是稳定和安全性都非常的高。Debian最具特色的是apt-get /dpkg包管理方式，其实Redhat的YUM也是在模仿Debian的APT方式，但在二进制文件发行方式中，APT应该是最好的了。Debian的资料也很丰富，有很多支持的社区，有问题求教也有地方可去。  
&emsp; Ubuntu严格来说不能算一个独立的发行版本，Ubuntu是基于Debian的unstable版本加强而来，可以这么说Ubuntu就是一个拥有Debian所有的优点，以及自己所加强的优点的近乎完美的Linux桌面系统。根据选择的桌面系统不同，有多个版本可供选择，比如基于unity的Ubuntu，基于Gnome的Ubuntu Gnome，基于KDE的Kubuntu，基于LXDE的Lubuntu以及基于Xfce的Xubuntu等。特点是界面非常友好，容易上手，对硬件的支持非常全面，是最适合做桌面系统的Linux发行版本。  
&emsp; Gentoo，伟大的Gentoo是Linux世界最年轻的发行版本，正因为年轻，所以能吸取在她之前的所有发行版本的优点，这也是Gentoo被称为最完美的Linux发行版本的原因之一。  
&emsp; FreeBSD，需要强调的是：FreeBSD并不是一个Linux系统！但FreeBSD与Linux的用户群有相当一部分是重合的，二者支持的硬件环境也比较一致，所采用的软件也比较类似，所以可以将FreeBSD视为一个Linux版本来比较。  
&emsp; FreeBSD拥有两个分支：stable和current。顾名思义，stable是稳定版，而current则是添加了新技术的测试版。FreeBSD采用Ports包管理系统，与Gentoo类似，基于源代码分发，必须在本地机器编后后才能运行，但是Ports系统没有Portage系统使用简便，使用起来稍微复杂一些。FreeBSD的最大特点就是稳定和高效，是作为服务器操作系统的最佳选择，但对硬件的支持没有Linux完备，所以并不适合作为桌面系统。  
&emsp; 下面给为选择一个Linux发行版本犯愁的朋友一些建议：  
&emsp; 如果你只是需要一个桌面系统，而且既不想使用盗版，又不想花大量的钱购买商业软件，那么你就需要一款适合桌面使用的Linux发行版本了，如果你不想自己定制任何东西，不想在系统上浪费太多时间，那么很简单，你就根据自己的爱好在ubuntu、kubuntu以及xubuntu中选一款吧，三者的区别仅仅是桌面程序的不一样。  
&emsp; 如果你需要一个桌面系统，而且还想非常灵活的定制自己的Linux系统，想让自己的机器跑得更欢，不介意在Linux系统安装方面浪费一点时间，那么你的唯一选择就是Gentoo，尽情享受Gentoo带来的自由快感吧！  
&emsp; 如果你需要的是一个服务器系统，而且你已经非常厌烦各种Linux的配置，只是想要一个比较稳定的服务器系统而已，那么你最好的选择就是CentOS了，安装完成后，经过简单的配置就能提供非常稳定的服务了。  
&emsp; 如果你需要的是一个坚如磐石的非常稳定的服务器系统，那么你的唯一选择就是FreeBSD。  
&emsp; 如果你需要一个稳定的服务器系统，而且想深入摸索一下Linux的各个方面的知识，想自己定制许多内容，那么我推荐你使用Gentoo。  


