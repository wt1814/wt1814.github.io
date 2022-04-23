



cd /Users/wangtao/software/elk/elasticsearch-7.13.3
bin/elasticsearch

http://localhost:5601/app/dev_tools#/console
GET canal_product/_search



GET canal_product/_search



POST wt/_doc
{
    "mappings":{
        "_doc":{
            "properties":{
                "testid":{
                    "type":"long"
                },
                "name":{
                    "type":"text"
                }
            }
        }
    }
}
