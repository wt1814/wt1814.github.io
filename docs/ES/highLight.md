

# 高亮显示
<!-- 
搜索模板、映射模板、高亮搜索和地理位置的简单玩法
https://mp.weixin.qq.com/s/BY0f47p6YETCVpQQDzG-dA
-->

```text
GET /product/_search
{
    "query" : {
        "match_phrase" : {
            "name" : "nfc phone"
        }
    },
    "highlight":{
      "fields":{
         "name":{}
      }
    }
}
```
