

<!-- TOC -->

- [1. ES优化](#1-es优化)
    - [1.1. ES的一些优化点](#11-es的一些优化点)
        - [1.1.1. 集群搭建阶段](#111-集群搭建阶段)
            - [1.1.1.1. 分片与副本](#1111-分片与副本)
            - [1.1.1.2. JVM设置](#1112-jvm设置)
        - [1.1.2. 开发阶段](#112-开发阶段)
            - [1.1.2.1. Document 模型设计](#1121-document-模型设计)
            - [1.1.2.2. 索引优化设置：](#1122-索引优化设置)
            - [1.1.2.3. 查询优化：](#1123-查询优化)
        - [1.1.3. 其他优化](#113-其他优化)
            - [1.1.3.1. Filesystem Cache](#1131-filesystem-cache)
            - [1.1.3.2. 数据预热](#1132-数据预热)
            - [1.1.3.3. 冷热分离](#1133-冷热分离)
            - [1.1.3.4. 重建索引](#1134-重建索引)
    - [1.2. 使用ES中的一些问题](#12-使用es中的一些问题)
        - [1.2.1. cpu使用率过高](#121-cpu使用率过高)
        - [1.2.2. Elasticsearch OOM](#122-elasticsearch-oom)
        - [1.2.3. 聚合数据结果不精确](#123-聚合数据结果不精确)

<!-- /TOC -->

# 1. ES优化  
<!-- 
你不得不关注的 Elasticsearch Top X 关键指标 
https://mp.weixin.qq.com/s/ns05CQF_wHysq4HXyMd2MQ

-->

## 1.1. ES的一些优化点  
### 1.1.1. 集群搭建阶段
#### 1.1.1.1. 分片与副本  
&emsp; 在创建索引的时候，应该创建多少个分片与副本数呢？  
&emsp; 对于副本数，比较好确定，可以根据集群节点的多少与存储空间决定，集群服务器多，并且有足够大多存储空间，可以多设置副本数，一般是 1-3 个副本数，如果集群服务器相对较少并且存储空间没有那么宽松，则可以只设定一份副本以保证容灾（副本数可以动态调整）。  
&emsp; 对于分片数，是比较难确定的。因为一个索引分片数一旦确定，就不能更改，所以在创建索引前，要充分的考虑到，以后创建的索引所存储的数据量，否则创建了不合适的分片数，会对性能造成很大的影响。  
&emsp; 对于分片数的大小，业界一致认为分片数的多少与内存挂钩，认为 1GB 堆内存对应 20-25 个分片，而一个分片的大小不要超过 50G，这样的配置有助于集群的健康。  
&emsp; 查询大量小分片使得每个分片处理数据速度更快了，那是不是分片数越多，查询就越快，ES 性能就越好呢？其实也不是，因为在查询过程中，有一个分片合并的过程，如果分片数不断的增加，合并的时间则会增加，而且随着更多的任务需要按顺序排队和处理，更多的小分片不一定要比查询较小数量的更大的分片更快。如果有多个并发查询，则有很多小碎片也会降低查询吞吐量。  
&emsp; 如果现在场景是分片数不合适了，但是又不知道如何调整，那么有一个好的解决方法就是按照时间创建索引，然后进行通配查询。如果每天的数据量很大，则可以按天创建索引，如果是一个月积累起来导致数据量很大，则可以一个月创建一个索引。如果要对现有索引进行重新分片，则需要重建索引。  

<!--
　　选择合适的分片数和副本数。ES的分片分为两种，主分片（Primary Shard）和副本（Replicas）。默认情况下，ES会为每个索引创建5个分片，即使是在单机环境下，这种冗余被称作过度分配（Over Allocation），目前看来这么做完全没有必要，仅在散布文档到分片和处理查询的过程中就增加了更多的复杂性，好在ES的优秀性能掩盖了这一点。假设一个索引由一个分片构成，那么当索引的大小超过单个节点的容量的时候，ES不能将索引分割成多份，因此必须在创建索引的时候就指定好需要的分片数量。此时我们所能做的就是创建一个新的索引，并在初始设定之中指定这个索引拥有更多的分片。反之如果过度分配，就增大了Lucene在合并分片查询结果时的复杂度，从而增大了耗时，所以我们得到了以下结论：  

　　我们应该使用最少的分片！  

　　主分片，副本和节点最大数之间数量存在以下关系：  

　　节点数<=主分片数*（副本数+1）  

 　  控制分片分配行为。以上是在创建每个索引的时候需要考虑的优化方法，然而在索引已创建好的前提下，是否就是没有办法从分片的角度提高了性能了呢？当然不是，首先能做的是调整分片分配器的类型，具体是在elasticsearch.yml中设置cluster.routing.allocation.type属性，共有两种分片器even_shard,balanced（默认）。even_shard是尽量保证每个节点都具有相同数量的分片，balanced是基于可控制的权重进行分配，相对于前一个分配器，它更暴漏了一些参数而引入调整分配过程的能力。  

　　每次ES的分片调整都是在ES上的数据分布发生了变化的时候进行的，最有代表性的就是有新的数据节点加入了集群的时候。当然调整分片的时机并不是由某个阈值触发的，ES内置十一个裁决者来决定是否触发分片调整，这里暂不赘述。另外，这些分配部署策略都是可以在运行时更新的，更多配置分片的属性也请大家自行Google。  


集群分片设置：  
ES一旦创建好索引后，就无法调整分片的设置，而在ES中，一个分片实际上对应一个lucene 索引，而lucene索引的读写会占用很多的系统资源，因此，分片数不能设置过大；所以，在创建索引时，合理配置分片数是非常重要的。一般来说，遵循一些原则：  
1. 控制每个分片占用的硬盘容量不超过ES的最大JVM的堆空间设置（一般设置不超过32G，参加上文的JVM设置原则），因此，如果索引的总容量在500G左右，那分片大小在16个左右即可；当然，最好同时考虑原则2。  
2. 考虑一下node数量，一般一个节点有时候就是一台物理机，如果分片数过多，大大超过了节点数，很可能会导致一个节点上存在多个分片，一旦该节点故障，即使保持了1个以上的副本，同样有可能会导致数据丢失，集群无法恢复。所以， 一般都设置分片数不超过节点数的3倍。  

-->

#### 1.1.1.2. JVM设置  
&emsp; ElasticSearch是基于Lucene开发的，而Lucene是使用Java开发的，所以也需要针对JVM进行调优。

* 确保堆内存最小值（ Xms ）与最大值（ Xmx ）的大小是相同的，防止程序在运行时改变堆内存大小。  
* **Elasticsearch 默认安装后设置的堆内存是 1GB。可通过 ../config/jvm.option 文件进行配置，但是最好不要超过物理内存的50%和超过 32GB。**  
* GC 默认采用 CMS 的方式，并发但是有 STW 的问题，可以考虑使用 G1 收集器。  

<!-- 
由于ES构建基于lucene, 而lucene设计强大之处在于lucene能够很好的利用操作系统内存来缓存索引数据，以提供快速的查询性能。lucene的索引文件segements是存储在单文件中的，并且不可变，对于OS来说，能够很友好地将索引文件保持在cache中，以便快速访问；因此，我们很有必要将一半的物理内存留给lucene ; 另一半的物理内存留给ES（JVM heap )。所以， 在ES内存设置方面，可以遵循以下原则：
1. 当机器内存小于64G时，遵循通用的原则，50%给ES，50%留给lucene。
2.  当机器内存大于64G时，遵循以下原则：
a. 如果主要的使用场景是全文检索, 那么建议给ES Heap分配 4~32G的内存即可；其它内存留给操作系统, 供lucene使用（segments cache), 以提供更快的查询性能。
b.  如果主要的使用场景是聚合或排序， 并且大多数是numerics, dates, geo_points 以及not_analyzed的字符类型， 建议分配给ES Heap分配 4~32G的内存即可，其它内存留给操作系统，供lucene使用(doc values cache)，提供快速的基于文档的聚类、排序性能。
c.  如果使用场景是聚合或排序，并且都是基于analyzed 字符数据，这时需要更多的 heap size, 建议机器上运行多ES实例，每个实例保持不超过50%的ES heap设置(但不超过32G，堆内存设置32G以下时，JVM使用对象指标压缩技巧节省空间)，50%以上留给lucene。
3. 禁止swap，一旦允许内存与磁盘的交换，会引起致命的性能问题。 通过： 在elasticsearch.yml 中 bootstrap.memory_lock: true， 以保持JVM锁定内存，保证ES的性能。
-->

### 1.1.2. 开发阶段
#### 1.1.2.1. Document 模型设计  
&emsp; 对于 MySQL，经常有一些复杂的关联查询。ES 里面的复杂的关联查询尽量别用，一旦用了性能一般都不太好。  
&emsp; 最好是先在 Java 系统里就完成关联，将关联好的数据直接写入 ES 中。搜索的时候，就不需要利用 ES 的搜索语法来完成 Join 之类的关联搜索了。  
&emsp; Document 模型设计是非常重要的，很多操作，不要在搜索的时候才想去执行各种复杂的乱七八糟的操作。  
&emsp; ES 能支持的操作就那么多，不要考虑用 ES 做一些它不好操作的事情。如果真的有那种操作，尽量在 Document 模型设计的时候，写入的时候就完成。  
&emsp; 另外对于一些太复杂的操作，比如 join/nested/parent-child 搜索都要尽量避免，性能都很差的。  

<!-- 
Mapping建模:
    1. 尽量避免使用nested或 parent/child，能不用就不用；nested query慢， parent/child query 更慢，比nested query慢上百倍；因此能在mapping设计阶段搞定的（大宽表设计或采用比较smart的数据结构），就不要用父子关系的mapping。
    2. 如果一定要使用nested fields，保证nested fields字段不能过多，目前ES默认限制是50。参考：
    index.mapping.nested_fields.limit ：50
    因为针对1个document, 每一个nested field, 都会生成一个独立的document, 这将使Doc数量剧增，影响查询效率，尤其是JOIN的效率。
    3. 避免使用动态值作字段(key),  动态递增的mapping，会导致集群崩溃；同样，也需要控制字段的数量，业务中不使用的字段，就不要索引。控制索引的字段数量、mapping深度、索引字段的类型，对于ES的性能优化是重中之重。以下是ES关于字段数、mapping深度的一些默认设置：
    index.mapping.nested_objects.limit :10000
    index.mapping.total_fields.limit:1000
    index.mapping.depth.limit: 20
-->

#### 1.1.2.2. 索引优化设置：
1. 设置refresh_interval 为-1，同时设置number_of_replicas 为0，通过关闭refresh间隔周期，同时不设置副本来提高写性能。  
2. 修改index_buffer_size 的设置，可以设置成百分数，也可设置成具体的大小，大小可根据集群的规模做不同的设置测试。  

        indices.memory.index_buffer_size：10%（默认）
        indices.memory.min_index_buffer_size： 48mb（默认）
        indices.memory.max_index_buffer_size
3. 修改translog相关的设置：
    1. 控制数据从内存到硬盘的操作频率，以减少硬盘IO。可将sync_interval的时间设置大一些。  

            index.translog.sync_interval：5s(默认)。

    2. 控制tranlog数据块的大小，达到threshold大小时，才会flush到lucene索引文件。  

            index.translog.flush_threshold_size：512mb(默认)
            
4. _id字段的使用，应尽可能避免自定义_id, 以避免针对ID的版本管理；建议使用ES的默认ID生成策略或使用数字类型ID做为主键。
5. _all字段及_source字段的使用，应该注意场景和需要，_all字段包含了所有的索引字段，方便做全文检索，如果无此需求，可以禁用；_source存储了原始的document内容，如果没有获取原始文档数据的需求，可通过设置includes、excludes 属性来定义放入_source的字段。
6. 合理的配置使用index属性，analyzed 和not_analyzed，根据业务需求来控制字段是否分词或不分词。只有 groupby需求的字段，配置时就设置成not_analyzed, 以提高查询或聚类的效率。

#### 1.1.2.3. 查询优化：
1. query_string 或 multi_match的查询字段越多， 查询越慢。可以在mapping阶段，利用copy_to属性将多字段的值索引到一个新字段，multi_match时，用新的字段查询。
2. 日期字段的查询， 尤其是用now 的查询实际上是不存在缓存的，因此， 可以从业务的角度来考虑是否一定要用now, 毕竟利用query cache 是能够大大提高查询效率的。
3. 查询结果集的大小不能随意设置成大得离谱的值， 如query.setSize不能设置成 Integer.MAX_VALUE， 因为ES内部需要建立一个数据结构来放指定大小的结果集数据。
4. 尽量避免使用script，万不得已需要使用的话，选择painless & experssions 引擎。一旦使用script查询，一定要注意控制返回，千万不要有死循环（如下错误的例子），因为ES没有脚本运行的超时控制，只要当前的脚本没执行完，该查询会一直阻塞。  
如：  

```json
{
    “script_fields”：{
        “test1”：{
            “lang”：“groovy”，
            “script”：“while（true）{print 'don’t use script'}”
        }
    }
}
```
5. 避免层级过深的聚合查询，层级过深的group by , 会导致内存、CPU消耗，建议在服务层通过程序来组装业务，也可以通过pipeline的方式来优化。  
6. 复用预索引数据方式来提高AGG性能：  
&emsp; 如通过 terms aggregations 替代 range aggregations， 如要根据年龄来分组，分组目标是: 少年（14岁以下） 青年（14-28） 中年（29-50） 老年（51以上）， 可以在索引的时候设置一个age_group字段，预先将数据进行分类。从而不用按age来做range aggregations, 通过age_group字段就可以了。
7. Cache的设置及使用：  
    1. QueryCache: ES查询的时候，使用filter查询会使用query cache, 如果业务场景中的过滤查询比较多，建议将querycache设置大一些，以提高查询速度。  
    &emsp; indices.queries.cache.size： 10%（默认），可设置成百分比，也可设置成具体值，如256mb。  
    &emsp; 当然也可以禁用查询缓存（默认是开启）， 通过index.queries.cache.enabled：false设置。  
    2. FieldDataCache: 在聚类或排序时，field data cache会使用频繁，因此，设置字段数据缓存的大小，在聚类或排序场景较多的情形下很有必要，可&emsp; 通过indices.fielddata.cache.size：30% 或具体值10GB来设置。但是如果场景或数据变更比较频繁，设置cache并不是好的做法，因为缓存加载的开销也是特别大的。
    3. ShardRequestCache: 查询请求发起后，每个分片会将结果返回给协调节点(Coordinating Node), 由协调节点将结果整合。   
    &emsp; 如果有需求，可以设置开启;  通过设置index.requests.cache.enable: true来开启。  
    &emsp; 不过，shard request cache只缓存hits.total, aggregations, suggestions类型的数据，并不会缓存hits的内容。也可以通过设置indices.requests.cache.size: 1%（默认）来控制缓存空间大小。  

### 1.1.3. 其他优化
#### 1.1.3.1. Filesystem Cache  
<!-- 
https://www.cnblogs.com/huanglog/p/9021073.html
-->

&emsp; 往 ES 里写的数据，实际上都写到磁盘文件里去了，查询的时候，操作系统会将磁盘文件里的数据自动缓存到 Filesystem Cache 里面去。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/ES/es-14.png)  
&emsp; ES 的搜索引擎严重依赖于底层的 Filesystem Cache，如果给 Filesystem Cache 更多的内存，尽量让内存可以容纳所有的 IDX Segment File 索引数据文件，那么搜索的时候就基本都是走内存的，性能会非常高。  
&emsp; 真实的案例：某个公司 ES 节点有 3 台机器，每台机器看起来内存很多 64G，总内存就是 64 * 3 = 192G。  
每台机器给 ES JVM Heap 是 32G，那么剩下来留给 Filesystem Cache 的就是每台机器才 32G，总共集群里给 Filesystem Cache 的就是 32 * 3 = 96G 内存。  
&emsp; 而此时，整个磁盘上索引数据文件，在 3 台机器上一共占用了 1T 的磁盘容量，ES 数据量是 1T，那么每台机器的数据量是 300G。这样性能好吗？  
&emsp; Filesystem Cache 的内存才 100G，十分之一的数据可以放内存，其他的都在磁盘，然后执行搜索操作，大部分操作都是走磁盘，性能肯定差。  
&emsp; 归根结底，要让 ES 性能好，最佳的情况下，就是机器的内存，至少可以容纳总数据量的一半。  
&emsp; 根据实践经验，最佳的情况下，是仅仅在 ES 中就存少量的数据，就是要用来搜索的那些索引，如果内存留给 Filesystem Cache 的是 100G，那么就将索引数据控制在 100G 以内。  
&emsp; 这样的话，数据几乎全部走内存来搜索，性能非常之高，一般可以在1秒以内。  
&emsp; 比如说现在有一行数据：id，name，age .... 30 个字段。但是现在搜索，只需要根据 id，name，age 三个字段来搜索。
如果往 ES 里写入一行数据所有的字段，就会导致说 90% 的数据是不用来搜索的。  
&emsp; 结果硬是占据了 ES 机器上的 Filesystem Cache 的空间，单条数据的数据量越大，就会导致 Filesystem Cahce 能缓存的数据就越少。  
&emsp; 其实，仅仅写入 ES 中要用来检索的少数几个字段就可以了，比如说就写入 es id，name，age 三个字段。  
&emsp; 然后可以把其他的字段数据存在 MySQL/HBase 里，一般是建议用 ES + HBase 这么一个架构。  
&emsp; HBase 的特点是适用于海量数据的在线存储，就是对 HBase 可以写入海量数据，但是不要做复杂的搜索，做很简单的一些根据 id 或者范围进行查询的这么一个操作就可以了。  
&emsp; 从 ES 中根据 name 和 age 去搜索，拿到的结果可能就 20 个 doc id，然后根据 doc id 到 HBase 里去查询每个 doc id 对应的完整的数据，给查出来，再返回给前端。  
&emsp; 写入 ES 的数据最好小于等于，或者是略微大于 ES 的 Filesystem Cache 的内存容量。  
&emsp; 然后从 ES 检索可能就花费 20ms，然后再根据 ES 返回的 id 去 HBase 里查询，查 20 条数据，可能也就耗费 30ms。  
&emsp; 设置 ES 集群内存的时候，还有一点就是**避免交换内存**，可以在配置文件中对内存进行锁定，以避免交换内存（也可以在操作系统层面进行关闭内存交换）。对应的参数：bootstrap.mlockall: true。  

#### 1.1.3.2. 数据预热  
&emsp; 假如说，哪怕是按照上述的方案去做了，ES 集群中每个机器写入的数据量还是超过了 Filesystem Cache 一倍。  
&emsp; 比如说写入一台机器 60G 数据，结果 Filesystem Cache 就 30G，还是有 30G 数据留在了磁盘上。  
&emsp; 其实可以做数据预热。举个例子，拿微博来说，可以把一些大 V，平时看的人很多的数据，提前在后台搞个系统。  
&emsp; 每隔一会儿，自己的后台系统去搜索一下热数据，刷到 Filesystem Cache 里去，后面用户实际上来看这个热数据的时候，他们就是直接从内存里搜索了，很快。  
&emsp; 或者是电商，可以将平时查看最多的一些商品，比如说 iPhone 8，热数据提前后台搞个程序，每隔 1 分钟自己主动访问一次，刷到 Filesystem Cache 里去。  
&emsp; 对于那些觉得比较热的、经常会有人访问的数据，最好做一个专门的缓存预热子系统。  
&emsp; 就是对热数据每隔一段时间，就提前访问一下，让数据进入 Filesystem Cache 里面去。这样下次别人访问的时候，性能一定会好很多。  

#### 1.1.3.3. 冷热分离  
&emsp; ES 可以做类似于 MySQL 的水平拆分，就是说将大量的访问很少、频率很低的数据，单独写一个索引，然后将访问很频繁的热数据单独写一个索引。  
&emsp; 最好是将冷数据写入一个索引中，然后热数据写入另外一个索引中，这样可以确保热数据在被预热之后，尽量都让它们留在 Filesystem OS Cache 里，别让冷数据给冲刷掉。  
&emsp; 假设有 6 台机器，2 个索引，一个放冷数据，一个放热数据，每个索引 3 个 Shard。3 台机器放热数据 Index，另外 3 台机器放冷数据 Index。  
&emsp; 这样的话，大量的时间是在访问热数据 Index，热数据可能就占总数据量的 10%，此时数据量很少，几乎全都保留在 Filesystem Cache 里面了，就可以确保热数据的访问性能是很高的。  
&emsp; 但是对于冷数据而言，是在别的 Index 里的，跟热数据 Index 不在相同的机器上，大家互相之间都没什么联系了。  
&emsp; 如果有人访问冷数据，可能大量数据是在磁盘上的，此时性能差点，就 10% 的人去访问冷数据，90% 的人在访问热数据，也无所谓了。  

#### 1.1.3.4. 重建索引  
&emsp; 在重建索引之前，首先要考虑一下重建索引的必要性，因为重建索引是非常耗时的。ES 的 reindex api 不会去尝试设置目标索引，不会复制源索引的设置，所以应该在运行_reindex 操作之前设置目标索引，包括设置映射（mapping），分片，副本等。  

&emsp; 第一步，和创建普通索引一样创建新索引。当数据量很大的时候，需要设置刷新时间间隔，把 refresh_intervals 设置为-1，即不刷新,number_of_replicas 副本数设置为 0（因为副本数可以动态调整，这样有助于提升速度）。  

```json
{
	"settings": {

		"number_of_shards": "50",
		"number_of_replicas": "0",
		"index": {
			"refresh_interval": "-1"
		}
	},
	"mappings": {
    }
}
```
&emsp; 第二步，调用 reindex 接口，建议加上 wait_for_completion=false 的参数条件，这样 reindex 将直接返回 taskId。
POST _reindex?wait_for_completion=false  

```json
{
  "source": {
    "index": "old_index",   //原有索引
    "size": 5000            //一个批次处理的数据量
  },
  "dest": {
    "index": "new_index"   //目标索引
  }
}
```

&emsp; 第三步，等待。可以通过 GET _tasks?detailed=true&actions=*reindex 来查询重建的进度。如果要取消 task 则调用_tasks/node_id:task_id/_cancel。  
&emsp; 第四步，删除旧索引，释放磁盘空间。更多细节可以查看 ES 官网的 reindex api。  

&emsp; **如果ES 是实时写入的，该怎么办？**  
&emsp; 这个时候，需要重建索引的时候，在参数里加上上一次重建索引的时间戳，直白的说就是，比如数据是 100G，这时候重建索引了，但是这个 100G 在增加，那么重建索引的时候，需要记录好重建索引的时间戳，记录时间戳的目的是下一次重建索引跑任务的时候不用全部重建，只需要在此时间戳之后的重建就可以，如此迭代，直到新老索引数据量基本一致，把数据流向切换到新索引的名字。  

```text
POST /_reindex
{
    "conflicts": "proceed",          //意思是冲突以旧索引为准，直接跳过冲突，否则会抛出异常，停止task
    "source": {
        "index": "old_index"         //旧索引
        "query": {
            "constant_score" : {
                "filter" : {
                    "range" : {
                        "data_update_time" : {
                            "gte" : 123456789   //reindex开始时刻前的毫秒时间戳
                            }
                        }
                    }
                }
            }
        },
    "dest": {
        "index": "new_index",       //新索引
        "version_type": "external"  //以旧索引的数据为准
        }
}
```

## 1.2. 使用ES中的一些问题  
### 1.2.1. cpu使用率过高  
&emsp; cpu使用率，该如何调节呢。cpu使用率高，有可能是写入导致的，也有可能是查询导致的，那要怎么查看呢？  
&emsp; 可以先通过 GET _nodes/{node}/hot_threads 查看线程栈，查看是哪个线程占用 cpu 高，如果是 elasticsearch[{node}][search][T#10] 则是查询导致的，如果是 elasticsearch[{node}][bulk][T#1] 则是数据写入导致的。  
&emsp; 在实际调优中，cpu 使用率很高，如果不是 SSD，建议把 index.merge.scheduler.max_thread_count: 1 索引 merge 最大线程数设置为 1 个，该参数可以有效调节写入的性能。因为在存储介质上并发写，由于寻址的原因，写入性能不会提升，只会降低。  


### 1.2.2. Elasticsearch OOM  
<!-- 
公司银子不多，遇到Elasticsearch OOM（内存溢出），除了瞪白眼，还能干啥... 
https://mp.weixin.qq.com/s/W3mSgShSgoqkGz6otZRE5Q
-->
......

### 1.2.3. 聚合数据结果不精确  
<!-- 
 Elasticsearch 聚合数据结果不精确，怎么破？ 
https://mp.weixin.qq.com/s/V4cGqvkQ7-DgeSvPSketgQ
-->



