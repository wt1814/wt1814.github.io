

<!-- TOC -->

- [1. ES优化](#1-es优化)
    - [1.1.1. 集群搭建阶段](#111-集群搭建阶段)
        - [1.1.1.1. 分片与副本](#1111-分片与副本)
        - [1.1.1.2. JVM设置](#1112-jvm设置)
    - [1.1.2. 开发阶段](#112-开发阶段)
        - [1.1.2.1. 索引优化设置](#1121-索引优化设置)
        - [1.1.2.2. 查询优化](#1122-查询优化)
    - [1.1.3. 其他优化](#113-其他优化)
        - [1.1.3.1. Filesystem Cache](#1131-filesystem-cache)
        - [1.1.3.2. 数据预热](#1132-数据预热)
        - [1.1.3.3. 冷热分离](#1133-冷热分离)
        - [1.1.3.4. 重建索引](#1134-重建索引)

<!-- /TOC -->

# 1. ES优化  
<!-- 
 30 个 ElasticSearch 调优知识点，都给你整理好了！ 
 https://mp.weixin.qq.com/s/ORhWtUNQ--kGKuBPu00v7w

写入调优  查询调优
https://www.zhihu.com/column/c_1452677051903381504

-->

## 1.1.1. 集群搭建阶段  
### 1.1.1.1. 分片与副本  
&emsp; 参考[ES建模](/docs/ES/modeling.md)    

### 1.1.1.2. JVM设置  
&emsp; ElasticSearch是基于Lucene开发的，而Lucene是使用Java开发的，所以也需要针对JVM进行调优。

* 确保堆内存最小值(Xms)与最大值(Xmx)的大小是相同的，防止程序在运行时改变堆内存大小。  
* **Elasticsearch 默认安装后设置的堆内存是 1GB。可通过 ../config/jvm.option 文件进行配置，但是最好`不要超过物理内存的50%和超过32GB`。**  
* GC 默认采用 CMS 的方式，并发但是有 STW 的问题，可以考虑使用 G1 收集器。  

<!-- 
由于ES构建基于lucene, 而lucene设计强大之处在于lucene能够很好的利用操作系统内存来缓存索引数据，以提供快速的查询性能。lucene的索引文件segements是存储在单文件中的，并且不可变，对于OS来说，能够很友好地将索引文件保持在cache中，以便快速访问；因此，我们很有必要将一半的物理内存留给lucene ; 另一半的物理内存留给ES(JVM heap )。所以， 在ES内存设置方面，可以遵循以下原则：
1. 当机器内存小于64G时，遵循通用的原则，50%给ES，50%留给lucene。
2.  当机器内存大于64G时，遵循以下原则：
a. 如果主要的使用场景是全文检索, 那么建议给ES Heap分配 4~32G的内存即可；其它内存留给操作系统, 供lucene使用(segments cache), 以提供更快的查询性能。
b.  如果主要的使用场景是聚合或排序， 并且大多数是numerics, dates, geo_points 以及not_analyzed的字符类型， 建议分配给ES Heap分配 4~32G的内存即可，其它内存留给操作系统，供lucene使用(doc values cache)，提供快速的基于文档的聚类、排序性能。
c.  如果使用场景是聚合或排序，并且都是基于analyzed 字符数据，这时需要更多的 heap size, 建议机器上运行多ES实例，每个实例保持不超过50%的ES heap设置(但不超过32G，堆内存设置32G以下时，JVM使用对象指标压缩技巧节省空间)，50%以上留给lucene。
3. 禁止swap，一旦允许内存与磁盘的交换，会引起致命的性能问题。 通过： 在elasticsearch.yml 中 bootstrap.memory_lock: true， 以保持JVM锁定内存，保证ES的性能。
-->
## 1.1.2. 开发阶段
### 1.1.2.1. 索引优化设置
1. 设置refresh_interval 为-1，同时设置number_of_replicas 为0，通过关闭refresh间隔周期，同时不设置副本来提高写性能。  
2. 修改index_buffer_size 的设置，可以设置成百分数，也可设置成具体的大小，大小可根据集群的规模做不同的设置测试。  

        indices.memory.index_buffer_size：10%(默认)
        indices.memory.min_index_buffer_size： 48mb(默认)
        indices.memory.max_index_buffer_size
3. 修改translog相关的设置：
    1. 控制数据从内存到硬盘的操作频率，以减少硬盘IO。可将sync_interval的时间设置大一些。  

            index.translog.sync_interval：5s(默认)。

    2. 控制tranlog数据块的大小，达到threshold大小时，才会flush到lucene索引文件。  

            index.translog.flush_threshold_size：512mb(默认)
            
4. \_id字段的使用，应尽可能避免自定义\_id, 以避免针对ID的版本管理；建议使用ES的默认ID生成策略或使用数字类型ID做为主键。
5. \_all字段及\_source字段的使用，应该注意场景和需要，\_all字段包含了所有的索引字段，方便做全文检索，如果无此需求，可以禁用；\_source存储了原始的document内容，如果没有获取原始文档数据的需求，可通过设置includes、excludes属性来定义放入\_source的字段。
6. 合理的配置使用index属性，analyzed 和not_analyzed，根据业务需求来控制字段是否分词或不分词。只有 groupby需求的字段，配置时就设置成not\_analyzed, 以提高查询或聚类的效率。

### 1.1.2.2. 查询优化
1. query_string 或 multi_match的查询字段越多， 查询越慢。可以在mapping阶段，利用copy_to属性将多字段的值索引到一个新字段，multi_match时，用新的字段查询。
2. 日期字段的查询，尤其是用now的查询实际上是不存在缓存的，因此，可以从业务的角度来考虑是否一定要用now, 毕竟利用query cache是能够大大提高查询效率的。
3. 查询结果集的大小不能随意设置成大得离谱的值，如query.setSize不能设置成Integer.MAX_VALUE，因为ES内部需要建立一个数据结构来放指定大小的结果集数据。
4. 尽量避免使用script，万不得已需要使用的话，选择painless & experssions引擎。一旦使用script查询，一定要注意控制返回，千万不要有死循环(如下错误的例子)，因为ES没有脚本运行的超时控制，只要当前的脚本没执行完，该查询会一直阻塞。  
如：  

```json
{
    “script_fields”：{
        “test1”：{
            “lang”：“groovy”，
            “script”：“while(true){print 'don’t use script'}”
        }
    }
}
```
5. 避免层级过深的聚合查询，层级过深的group by , 会导致内存、CPU消耗，建议在服务层通过程序来组装业务，也可以通过pipeline的方式来优化。  
6. 复用预索引数据方式来提高AGG性能：  
&emsp; 如通过 terms aggregations 替代 range aggregations， 如要根据年龄来分组，分组目标是: 少年(14岁以下) 青年(14-28) 中年(29-50) 老年(51以上)， 可以在索引的时候设置一个age_group字段，预先将数据进行分类。从而不用按age来做range aggregations, 通过age_group字段就可以了。
7. Cache的设置及使用：  
    1. QueryCache: ES查询的时候，使用filter查询会使用query cache, 如果业务场景中的过滤查询比较多，建议将querycache设置大一些，以提高查询速度。  
    &emsp; indices.queries.cache.size： 10%(默认)，可设置成百分比，也可设置成具体值，如256mb。  
    &emsp; 当然也可以禁用查询缓存(默认是开启)， 通过index.queries.cache.enabled：false设置。  
    2. FieldDataCache: 在聚类或排序时，field data cache会使用频繁，因此，设置字段数据缓存的大小，在聚类或排序场景较多的情形下很有必要，可通过indices.fielddata.cache.size：30% 或具体值10GB来设置。但是如果场景或数据变更比较频繁，设置cache并不是好的做法，因为缓存加载的开销也是特别大的。
    3. ShardRequestCache: 查询请求发起后，每个分片会将结果返回给协调节点(Coordinating Node), 由协调节点将结果整合。   
    &emsp; 如果有需求，可以设置开启;  通过设置index.requests.cache.enable: true来开启。  
    &emsp; 不过，shard request cache只缓存hits.total, aggregations, suggestions类型的数据，并不会缓存hits的内容。也可以通过设置indices.requests.cache.size: 1%(默认)来控制缓存空间大小。  

## 1.1.3. 其他优化
### 1.1.3.1. Filesystem Cache  
<!-- 
https://www.cnblogs.com/huanglog/p/9021073.html
-->

&emsp; 往 ES 里写的数据，实际上都写到磁盘文件里去了，查询的时候，操作系统会将磁盘文件里的数据自动缓存到 Filesystem Cache 里面去。  
![image](http://182.92.69.8:8081/img/ES/es-14.png)  
&emsp; ES 的搜索引擎严重依赖于底层的 Filesystem Cache，如果给 Filesystem Cache 更多的内存，尽量让内存可以容纳所有的 IDX Segment File 索引数据文件，那么搜索的时候就基本都是走内存的，性能会非常高。  
&emsp; 真实的案例：某个公司 ES 节点有 3 台机器，每台机器看起来内存很多 64G，总内存就是 64 * 3 = 192G。  
&emsp; 每台机器给ES JVM Heap是 32G，那么剩下来留给Filesystem Cache的就是每台机器才32G，总共集群里给Filesystem Cache 的就是 32 * 3 = 96G 内存。  
&emsp; 而此时，整个磁盘上索引数据文件，在 3 台机器上一共占用了 1T 的磁盘容量，ES 数据量是 1T，那么每台机器的数据量是 300G。这样性能好吗？  
&emsp; Filesystem Cache 的内存才 100G，十分之一的数据可以放内存，其他的都在磁盘，然后执行搜索操作，大部分操作都是走磁盘，性能肯定差。  
&emsp; 归根结底，要让 ES 性能好， **<font color = "red">最佳的情况下，就是机器的内存，至少可以容纳总数据量的一半。</font>**  
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
&emsp; 设置 ES 集群内存的时候，还有一点就是**避免交换内存**，可以在配置文件中对内存进行锁定，以避免交换内存(也可以在操作系统层面进行关闭内存交换)。对应的参数：bootstrap.mlockall: true。  

### 1.1.3.2. 数据预热  
&emsp; 假如说，哪怕是按照上述的方案去做了，ES 集群中每个机器写入的数据量还是超过了 Filesystem Cache 一倍。  
&emsp; 比如说写入一台机器 60G 数据，结果 Filesystem Cache 就 30G，还是有 30G 数据留在了磁盘上。  
&emsp; 其实可以做数据预热。举个例子，拿微博来说，可以把一些大 V，平时看的人很多的数据，提前在后台搞个系统。  
&emsp; 每隔一会儿，自己的后台系统去搜索一下热数据，刷到 Filesystem Cache 里去，后面用户实际上来看这个热数据的时候，就是直接从内存里搜索了，很快。  
&emsp; 或者是电商，可以将平时查看最多的一些商品，比如说 iPhone 8，热数据提前后台搞个程序，每隔 1 分钟自己主动访问一次，刷到 Filesystem Cache 里去。  
&emsp; 对于那些觉得比较热的、经常会有人访问的数据，最好做一个专门的缓存预热子系统。  
&emsp; 就是对热数据每隔一段时间，就提前访问一下，让数据进入 Filesystem Cache 里面去。这样下次别人访问的时候，性能一定会好很多。  

### 1.1.3.3. 冷热分离  
&emsp; ES 可以做类似于 MySQL 的水平拆分，就是说将大量的访问很少、频率很低的数据，单独写一个索引，然后将访问很频繁的热数据单独写一个索引。  
&emsp; 最好是将冷数据写入一个索引中，然后热数据写入另外一个索引中，这样可以确保热数据在被预热之后，尽量都让它们留在 Filesystem OS Cache 里，别让冷数据给冲刷掉。  
&emsp; 假设有 6 台机器，2 个索引，一个放冷数据，一个放热数据，每个索引 3 个 Shard。3 台机器放热数据 Index，另外 3 台机器放冷数据 Index。  
&emsp; 这样的话，大量的时间是在访问热数据 Index，热数据可能就占总数据量的 10%，此时数据量很少，几乎全都保留在 Filesystem Cache 里面了，就可以确保热数据的访问性能是很高的。  
&emsp; 但是对于冷数据而言，是在别的 Index 里的，跟热数据 Index 不在相同的机器上，大家互相之间都没什么联系了。  
&emsp; 如果有人访问冷数据，可能大量数据是在磁盘上的，此时性能差点，就 10% 的人去访问冷数据，90% 的人在访问热数据，也无所谓了。  

### 1.1.3.4. 重建索引  
&emsp; 在重建索引之前，首先要考虑一下重建索引的必要性，因为重建索引是非常耗时的。ES 的 reindex api 不会去尝试设置目标索引，不会复制源索引的设置，所以应该在运行_reindex 操作之前设置目标索引，包括设置映射(mapping)，分片，副本等。  

&emsp; 第一步，和创建普通索引一样创建新索引。当数据量很大的时候，需要设置刷新时间间隔，把 refresh_intervals 设置为-1，即不刷新,number_of_replicas 副本数设置为 0(因为副本数可以动态调整，这样有助于提升速度)。  

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
    "conflicts": "proceed",  //意思是冲突以旧索引为准，直接跳过冲突，否则会抛出异常，停止task
    "source": {
        "index": "old_index"   //旧索引
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
