package com.wuw.ucenter.server.service.impl;

import com.wuw.ucenter.server.service.ESTestService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.client.RequestOptions;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class ESTestServiceImpl implements ESTestService{

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Override
    public Boolean indexCreate(String index, String mapping) throws Exception{
        IndicesClient indicesClient = restHighLevelClient.indices();
        // 创建索引
        CreateIndexRequest indexRequest = new CreateIndexRequest(index);
        // 创建表 结构

        // 把映射信息添加到request请求里面
        // 第一个参数：表示数据源
        // 第二个参数：表示请求的数据类型
        indexRequest.mapping("_doc",mapping, XContentType.JSON);
        // 请求服务器
        CreateIndexResponse response = indicesClient.create(indexRequest, RequestOptions.DEFAULT);
        return response.isAcknowledged();

    }


}
