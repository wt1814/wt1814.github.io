package com.wuw.ucenter.server.service;

public interface ESTestService {


    /**
     * 创建索引
     * @param index
     * @param mapping
     * @return
     * @throws Exception
     */
    Boolean indexCreate(String index,String mapping) throws Exception;


}
