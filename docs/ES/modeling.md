<!-- TOC -->

- [1. ES建模](#1-es建模)
    - [1.1. 分片数和副本数如何设计？](#11-分片数和副本数如何设计)
        - [1.1.1. 索引设置多少分片？](#111-索引设置多少分片)
        - [1.1.2. 索引设置多少副本？](#112-索引设置多少副本)
    - [1.2. Mapping如何设计？](#12-mapping如何设计)
    - [1.3. 分词的选型](#13-分词的选型)
    - [1.4. 文档document设计](#14-文档document设计)
    - [1.5. 多表关联如何设计？](#15-多表关联如何设计)
    - [1.6. 冷热集群架构](#16-冷热集群架构)

<!-- /TOC -->



# 1. ES建模  
<!-- 
Elasticsearch 索引设计实战指南
https://mp.weixin.qq.com/s/Fc5LhiLJIeCtstl9OFeqdQ

* 每天几百 GB 增量实时数据的TB级甚至PB级别的大索引如何设计？
* 分片数和副本数大小如何设计，才能提升 ES 集群的性能？
* ES 的 Mapping 该如何设计，才能保证检索的高效？
* 检索类型 term/match/matchphrase/querystring /match_phrase _prefix /fuzzy 那么多，设计阶段如何选型呢？
* 分词该如何设计，才能满足复杂业务场景需求？
* 传统数据库中的多表关联在 ES 中如何设计？
* ......

-->

<!-- 
~~
从一个实战问题再谈 Elasticsearch 数据建模 
https://mp.weixin.qq.com/s/lGrNd-O_hmlEQcTc7OykmQ
干货 | 论Elasticsearch数据建模的重要性 
https://mp.weixin.qq.com/s?__biz=MzI2NDY1MTA3OQ==&mid=2247484159&idx=1&sn=731562a8bb89c9c81b4fd6a8e92e1a99&chksm=eaa82ad7dddfa3c11e5b63a41b0e8bc10d12f1b8439398e490086ddc6b4107b7864dbb9f891a&scene=21#wechat_redirect
-->
<!--
&emsp; 公司es的集群架构，索引数据大小，分片有多少。  
如实结合自己的实践场景回答即可。比如： ES 集群架构 13 个节点， 索引根据通道不同共 20+索引， 根据日期， 每日递增 20+， 索引： 10 分片， 每日递增 1 亿+数据， 每个通道每天索引大小控制： 150GB 之内。  
-->

&emsp; 数据在 Elasticsearch 怎么建模？  
&emsp; Mapping 如何设置？  
&emsp; 哪些字段需要全文检索？需要分词？哪些不需要？  
&emsp; 哪些字段需要建索引？  
&emsp; 哪些字段不需要存储？  
&emsp; 类型选择：integer 还是 keyword？  
&emsp; 哪些需要做多表关联？使用：宽表冗余存储？还是 Array，Object，Nested，Join ？都需要深入考虑。  
&emsp; 哪些字段需要预处理？  
&emsp; 预处理是通过logstash filter 环节实现？还是 Mysql 视图实现？还是 ingest 管道实现？还是其他？  
&emsp; ......  


## 1.1. 分片数和副本数如何设计？  
<!--
分片/副本认知
1. 分片：分片本身是一个功能齐全且独立的“索引”，可以托管在集群中的任何节点上。  
&emsp; 数据切分分片的主要目的：  
&emsp; (1)水平分割/缩放内容量 。  
&emsp; (2)跨分片(可能在多个节点上)分布和并行化操作，提高性能/吞吐量。   
&emsp; 注意：分片一旦创建，不可以修改大小。  
2. 副本：它在分片/节点出现故障时提供高可用性。  
&emsp; 副本的好处：因为可以在所有副本上并行执行搜索——因此扩展了搜索量/吞吐量。  
&emsp; 注意：副本分片与主分片存储在集群中不同的节点。副本的大小可以通过：number_of_replicas动态修改。  

分片和副本实战中设计  


　　选择合适的分片数和副本数。ES的分片分为两种，主分片(Primary Shard)和副本(Replicas)。默认情况下，ES会为每个索引创建5个分片，即使是在单机环境下，这种冗余被称作过度分配(Over Allocation)，目前看来这么做完全没有必要，仅在散布文档到分片和处理查询的过程中就增加了更多的复杂性，好在ES的优秀性能掩盖了这一点。假设一个索引由一个分片构成，那么当索引的大小超过单个节点的容量的时候，ES不能将索引分割成多份，因此必须在创建索引的时候就指定好需要的分片数量。此时我们所能做的就是创建一个新的索引，并在初始设定之中指定这个索引拥有更多的分片。反之如果过度分配，就增大了Lucene在合并分片查询结果时的复杂度，从而增大了耗时，所以我们得到了以下结论：  

　　我们应该使用最少的分片！  

　　主分片，副本和节点最大数之间数量存在以下关系：  

　　节点数<=主分片数*(副本数+1)  

 　  控制分片分配行为。以上是在创建每个索引的时候需要考虑的优化方法，然而在索引已创建好的前提下，是否就是没有办法从分片的角度提高了性能了呢？当然不是，首先能做的是调整分片分配器的类型，具体是在elasticsearch.yml中设置cluster.routing.allocation.type属性，共有两种分片器even_shard,balanced(默认)。even_shard是尽量保证每个节点都具有相同数量的分片，balanced是基于可控制的权重进行分配，相对于前一个分配器，它更暴漏了一些参数而引入调整分配过程的能力。  

　　每次ES的分片调整都是在ES上的数据分布发生了变化的时候进行的，最有代表性的就是有新的数据节点加入了集群的时候。当然调整分片的时机并不是由某个阈值触发的，ES内置十一个裁决者来决定是否触发分片调整，这里暂不赘述。另外，这些分配部署策略都是可以在运行时更新的，更多配置分片的属性也请大家自行Google。  


集群分片设置：  
ES一旦创建好索引后，就无法调整分片的设置，而在ES中，一个分片实际上对应一个lucene 索引，而lucene索引的读写会占用很多的系统资源，因此，分片数不能设置过大；所以，在创建索引时，合理配置分片数是非常重要的。一般来说，遵循一些原则：  
1. 控制每个分片占用的硬盘容量不超过ES的最大JVM的堆空间设置(一般设置不超过32G，参加上文的JVM设置原则)，因此，如果索引的总容量在500G左右，那分片大小在16个左右即可；当然，最好同时考虑原则2。  
2. 考虑一下node数量，一般一个节点有时候就是一台物理机，如果分片数过多，大大超过了节点数，很可能会导致一个节点上存在多个分片，一旦该节点故障，即使保持了1个以上的副本，同样有可能会导致数据丢失，集群无法恢复。所以， 一般都设置分片数不超过节点数的3倍。 
-->
### 1.1.1. 索引设置多少分片？
&emsp; **<font color = "clime">分片大小官方推荐值为20-40GB。</font>** Elasticsearch对数据的隔离和迁移是以分片为单位进行的，分片太大，会加大迁移成本。  
&emsp; 一个分片就是一个Lucene的库，一个Lucene目录里面包含很多Segment，每个Segment有文档数的上限，Segment内部的文档ID目前使用的是Java的整型，也就是2的31次方，所以能够表示的总的文档数为Integer.MAXVALUE - 128 = 2^31 - 128 = 2147483647 - 1 = 2,147,483,519，也就是21.4亿条。  
&emsp; 同样，如果不forcemerge成一个Segment，单个shard的文档数能超过这个数。单个Lucene越大，索引会越大，查询的操作成本自然要越高，IO压力越大，自然会影响查询体验。  

&emsp; **具体一个分片多少数据合适，还是需要结合实际的业务数据和实际的查询来进行测试以进行评估。综合实战+网上各种经验分享，梳理如下：**  

* 第一步：预估一下数据量的规模。一共要存储多久的数据，每天新增多少数据？两者的乘积就是总数据量。  
* 第二步：预估分多少个索引存储。索引的划分可以根据业务需要。  
* 第三步：考虑和衡量可扩展性，预估需要搭建几台机器的集群。存储主要看磁盘空间，假设每台机器2TB，可用：2TB0.85(磁盘实际利用率)0.85(ES 警戒水位线)。  
* 第四步：单分片的大小建议最大设置为 30GB。此处如果是增量索引，可以结合大索引的设计部分的实现一起规划。  

&emsp; **前三步能得出一个索引的大小。分片数考虑维度：1)分片数 = 索引大小/分片大小，经验值30GB。2)分片数建议和节点数一致。**  
&emsp; **<font color = "clime">设计的时候1)、2)两者权衡考虑+rollover动态更新索引结合。</font>**  

&emsp; **附录：**  
&emsp; 每个shard大小是按照经验值30G到50G，因为在这个范围内查询和写入性能较好。  
&emsp; 经验值的探推荐阅读：[Elasticsearch究竟要设置多少分片数？](https://mp.weixin.qq.com/s?__biz=MzI2NDY1MTA3OQ==&mid=2247483700&idx=1&sn=1549fc794f77da2d2194c991e1ce029b&chksm=eaa8291cdddfa00ae765d5fc5298e252a0f848197348123266afb84751d9fe8907aff65d7aea&scene=21#wechat_redirect) 、[探究 | Elasticsearch集群规模和容量规划的底层逻辑](https://mp.weixin.qq.com/s?__biz=MzI2NDY1MTA3OQ==&mid=2247484628&idx=1&sn=666e416ae28b93e42c26f26b208dea84&chksm=eaa82cfcdddfa5eacfcddb0cf54edcecb3ad86ca2cafd6f4f2d90cf8a4033d83eb16cb2a56f0&scene=21#wechat_redirect)  

### 1.1.2. 索引设置多少副本？
&emsp; 结合集群的规模，对于集群数据节点 >=2 的场景：建议副本至少设置为1。  
&emsp; 注意：单节点的机器设置了副本也不会生效的。副本数的设计结合数据的安全需要，对于数据安全性要求非常高的业务场景，建议做好：增强备份(结合ES官方备份方案)。  

----
---

<!-- 
Elasticsearch集群规模和容量规划的底层逻辑 
https://mp.weixin.qq.com/s?__biz=MzI2NDY1MTA3OQ==&mid=2247484628&idx=1&sn=666e416ae28b93e42c26f26b208dea84&chksm=eaa82cfcdddfa5eacfcddb0cf54edcecb3ad86ca2cafd6f4f2d90cf8a4033d83eb16cb2a56f0&scene=21#wechat_redirect

---
集群分片设置：

ES一旦创建好索引后，就无法调整分片的设置，而在ES中，一个分片实际上对应一个lucene 索引，而lucene索引的读写会占用很多的系统资源，因此，分片数不能设置过大；所以，在创建索引时，合理配置分片数是非常重要的。一般来说，我们遵循一些原则：

1. 控制每个分片占用的硬盘容量不超过ES的最大JVM的堆空间设置(一般设置不超过32G，参加上文的JVM设置原则)，因此，如果索引的总容量在500G左右，那分片大小在16个左右即可；当然，最好同时考虑原则2。

2. 考虑一下node数量，一般一个节点有时候就是一台物理机，如果分片数过多，大大超过了节点数，很可能会导致一个节点上存在多个分片，一旦该节点故障，即使保持了1个以上的副本，同样有可能会导致数据丢失，集群无法恢复。所以， 一般都设置分片数不超过节点数的3倍。


-----

ES在分配单个索引的分片时会将每个分片尽可能分配到更多的节点上。但是，实际情况取决于集群拥有的分片和索引的数量以及它们的大小，所以这种情况只是理想状况。
ES不允许Primary和它的Replica放在同一个节点中(为了容错)，并且同一个节点不接受完全相同的两个Replica，也就是说，因为同一个节点存放两个相同的副本既不能提升吞吐量，也不能提升查询速度，徒耗磁盘空间。  
每台节点的shard数量越少，每个shard分配的CPU、内存和IO资源越多，单个shard的性能越好，当一台机器一个Shard时，单个Shard性能最好。  
相同资源分配相同的前提下，单个shard的体积越大，查询性能越低，速度越慢  
稳定的Master节点对于群集健康非常重要！理论上讲，应该尽可能的减轻Master节点的压力，分片数量越多，Master节点维护管理shard的任务越重，并且节点可能就要承担更多的数据转发任务，可增加“仅协调”节点来缓解Master节点和Data节点的压力，但是在集群中添加过多的仅协调节点会增加整个集群的负担，因为选择的主节点必须等待每个节点的集群状态更新确认。  
如果相同资源分配相同的前提下，shard数量越少，单个shard的体积越大，查询性能越低，速度越慢，这个取舍应根据实际集群状况和结合应用场景等因素综合考虑  
数据节点和Master节点一定要分开，集群规模越大，这样做的意义也就越大  
数据节点处理与数据相关的操作，例如CRUD，搜索和聚合。这些操作是I / O，内存和CPU密集型的，所以他们需要更高配置的服务器以及更高的带宽，并且集群的性能冗余非常重要  
由于投票节不参与Master竞选，所以和真正的Master节点相比，它需要的内存和CPU较少。但是，所有候选节点以及仅投票节点都可能是数据节点，所以他们都需要快速稳定低延迟的网络
高可用性(HA)群集至少需要三个主节点，其中至少两个不是仅投票节点。即使其中一个节点发生故障，这样的群集也将能够选举一个主节点。生产环境最好设置3台仅Master候选节点(node.master = true	 node.data = true)。
为确保群集仍然可用，集群不能同时停止投票配置中的一半或更多节点。只要有一半以上的投票节点可用，群集仍可以正常工作。这意味着，如果存在三个或四个主节点合格的节点，则群集可以容忍其中一个节点不可用。如果有两个或更少的主机资格节点，则它们必须都保持可用  

![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-77.png)  

-->


## 1.2. Mapping如何设计？  
<!-- 
 Mapping认知  
&emsp; Mapping 是定义文档及其包含的字段的存储和索引方式的过程。例如，使用映射来定义：  

* 应将哪些字符串字段定义为全文检索字段；  
* 哪些字段包含数字，日期或地理位置；  
* 定义日期值的格式(时间戳还是日期类型等)；  
* 用于控制动态添加字段的映射的自定义规则。  


Mapping建模:
    1. 尽量避免使用nested或 parent/child，能不用就不用；nested query慢， parent/child query 更慢，比nested query慢上百倍；因此能在mapping设计阶段搞定的(大宽表设计或采用比较smart的数据结构)，就不要用父子关系的mapping。
    2. 如果一定要使用nested fields，保证nested fields字段不能过多，目前ES默认限制是50。参考：
    index.mapping.nested_fields.limit ：50
    因为针对1个document, 每一个nested field, 都会生成一个独立的document, 这将使Doc数量剧增，影响查询效率，尤其是JOIN的效率。
    3. 避免使用动态值作字段(key),  动态递增的mapping，会导致集群崩溃；同样，也需要控制字段的数量，业务中不使用的字段，就不要索引。控制索引的字段数量、mapping深度、索引字段的类型，对于ES的性能优化是重中之重。以下是ES关于字段数、mapping深度的一些默认设置：
    index.mapping.nested_objects.limit :10000
    index.mapping.total_fields.limit:1000
    index.mapping.depth.limit: 20

-->

&emsp; 索引分为静态 Mapping(自定义字段)+动态 Mapping(ES 自动根据导入数据适配)。  
&emsp; 实战业务场景建议：选用静态Mapping，根据业务类型自己定义字段类型。好处：可控；节省存储空间(默认string是text+keyword，实际业务不一定需要)。  
&emsp; **Mapping建议结合[索引模板](/docs/ES/index.md)定义。**  

&emsp; 设置字段的时候，务必过一下如下图示的流程。根据实际业务需要，主要关注点：数据类型选型 ---> 是否需要检索 ---> 是否需要排序+聚合分析 ---> 是否需要另行存储。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-73.png)  

&emsp; 核心参数的含义，梳理如下：  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-69.png)  

&emsp; ⚠️ **<font color = "red">设计Mapping的注意事项：</font>**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-67.png)  

* ES支持增加字段   

  ```text
  //新增字段
  PUT new_index
    {
      "mappings": {
        "_doc": {
          "properties": {
            "status_code": {
              "type":       "keyword"
            }
          }
        }
      }
    }
  ```

* ES 不支持直接删除字段  
* ES 不支持直接修改字段  
* ES 不支持直接修改字段类型。如果非要做灵活设计，ES有其他方案可以替换，借助reindex。但是数据量大会有性能问题，建议设计阶段综合权衡考虑。    


## 1.3. 分词的选型
&emsp; 主要以ik来说明，最新版本的ik支持两种类型。ik_maxword细粒度匹配，适用切分非常细的场景。ik_smart 粗粒度匹配，适用切分粗的场景。  

1. 坑1：分词选型
&emsp; 实际业务中：建议适用ik_max_word分词 + match_phrase短语检索。  
&emsp; 原因：ik_smart有覆盖不全的情况，数据量大了以后，即便 reindex 能满足要求，但面对极大的索引的情况，reindex 的耗时比较长。建议ik_max_word一步到位。  

2. 坑2：ik要装集群的所有机器吗？
&emsp; 建议：安装在集群的所有节点上。  

3. 坑3：ik匹配不到怎么办？  
  * 方案1：扩充ik开源自带的词库+动态更新词库；原生的词库分词数量级很小，基础词库尽量更大更全，网上搜索一下“搜狗词库“。  
  &emsp; 动态更新词库：可以结合mysql+ik 自带的更新词库的方式动态更新词库。  
  &emsp; 更新词库仅对新创建的索引生效，部分老数据索引建议使用 reindex 升级处理。  
  * 方案2：采用字词混合索引的方式，避免“明明存在，但是检索不到的”场景。  
  &emsp; [探究 | 明明存在，怎么搜索不出来呢？](https://mp.weixin.qq.com/s?__biz=MzI2NDY1MTA3OQ==&mid=2247484287&idx=1&sn=e1f4b24f61d37d556828bbcd211707ac&chksm=eaa82b57dddfa241570730dde38b74a3ff9c36927fd84513b0136e4dc3ee7af18154290a27b2&scene=21#wechat_redirect)  

## 1.4. 文档document设计  
<!-- 
Elasticsearch系列---数据建模实战
https://mp.weixin.qq.com/s/hTGqpCl4KYXlvD74fj8l5Q
-->
&emsp; es更类似面向对象的数据模型，将所有关联的数据放在一个document里。  


## 1.5. 多表关联如何设计？  
&emsp; 主要原因：常规基于关系型数据库开发，多多少少都会遇到关联查询。而关系型数据库设计的思维很容易带到ES的设计中。  
&emsp; **多表关联如何实现？**    
&emsp; **方案一：多表关联视图，视图同步ES**  
&emsp; MySQL宽表导入ES，使用ES查询+检索。适用场景：基础业务都在MySQL，存在几十张甚至几百张表，准备同步到ES，使用ES做全文检索。  
&emsp; 将数据整合成一个宽表后写到ES，宽表的实现可以借助关系型数据库的视图实现。  
&emsp; 宽表处理在处理一对多、多对多关系时，会有字段冗余问题，如果借助：logstash_input_jdbc，关系型数据库如MySQL中的每一个字段都会自动帮你转成ES中对应索引下的对应document下的某个相同字段下的数据。  

* 步骤 1：提前关联好数据，将关联的表建立好视图，一个索引对应你的一个视图，并确认视图中数据的正确性。
* 步骤 2：ES 中针对每个视图定义好索引名称及 Mapping。
* 步骤 3：以视图为单位通过 logstash_input_jdbc 同步到 ES 中。

&emsp; **方案二：1 对 1 同步 ES**  
&emsp; MySQL+ES结合，各取所长。适用场景：关系型数据库全量同步到ES存储，没有做冗余视图关联。  
&emsp; ES擅长的是检索，而MySQL才擅长关系管理。所以可以考虑二者结合，使用ES多索引建立相同的别名，针对别名检索到对应ID后再回MySQL通过关联ID join出需要的数据。  

&emsp; **方案三：使用 Nested 做好关联**  
&emsp; 适用场景：1 对少量的场景。举例：有一个文档描述了一个帖子和一个包含帖子上所有评论的内部对象评论。可以借助 Nested 实现。  
&emsp; Nested 类型选型——如果需要索引对象数组并保持数组中每个对象的独立性，则应使用嵌套 Nested 数据类型而不是对象 Oject 数据类型。  
&emsp; 当使用嵌套文档时，使用通用的查询方式是无法访问到的，必须使用合适的查询方式(nested query、nested filter、nested facet等)，很多场景下，使用嵌套文档的复杂度在于索引阶段对关联关系的组织拼装。  

&emsp; **方案四：使用 ES6.X+ 父子关系 Join 做关联**   
&emsp; 适用场景：1 对多量的场景。举例：1个产品和供应商之间是1对N的关联关系。  
&emsp; Join 类型：join数据类型是一个特殊字段，用于在同一索引的文档中创建父/子关系。关系部分定义文档中的一组可能关系，每个关系是父名称和子名称。  
&emsp; 当使用父子文档时，使用has_child 或者has_parent做父子关联查询。  

&emsp; **方案三、方案四选型对比：**  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-70.png)  
&emsp; 注意：方案三&方案四选型必须考虑性能问题。文档应该尽量通过合理的建模来提升检索效率。  
&emsp; Join类型应该尽量避免使用。nested 类型检索使得检索效率慢几倍，父子Join类型检索会使得检索效率慢几百倍。  
&emsp; 尽量将业务转化为没有关联关系的文档形式，在文档建模处多下功夫，以提升检索效率。  

&emsp; [干货 | 论Elasticsearch数据建模的重要性](https://mp.weixin.qq.com/s?__biz=MzI2NDY1MTA3OQ==&mid=2247484159&idx=1&sn=731562a8bb89c9c81b4fd6a8e92e1a99&chksm=eaa82ad7dddfa3c11e5b63a41b0e8bc10d12f1b8439398e490086ddc6b4107b7864dbb9f891a&scene=21#wechat_redirect)  
&emsp; [干货 | Elasticsearch多表关联设计指南](https://mp.weixin.qq.com/s?__biz=MzI2NDY1MTA3OQ==&mid=2247484382&idx=1&sn=da073a257575867b8d979dac850c3f8e&chksm=eaa82bf6dddfa2e0bf920f0a3a63cb635277be2ae286a2a6d3fff905ad913ebf1f43051609e8&scene=21#wechat_redirect)  

&emsp; 小结  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-71.png)  


----
## 1.6. 冷热集群架构
<!-- 
Elasticsearch 冷热集群架构实战
https://mp.weixin.qq.com/s?__biz=MzI2NDY1MTA3OQ==&mid=2247484554&idx=1&sn=ed691dbd91966cb0f9da4efff0e9d79e&chksm=eaa82ca2dddfa5b4ef36ad43146c130750f551fcf15cab88a439fc292f5d184992964c44c8c0&scene=178&cur_album_id=1340073242396114944#rd
-->
