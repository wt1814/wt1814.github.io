package com.wuw.ucenter.server.controller;

import com.alibaba.fastjson.JSON;
import com.wuw.common.api.apiResult.ApiResult;
import com.wuw.ucenter.api.model.VO.ESIndexCreateVo;
import com.wuw.ucenter.server.service.ESTestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/es")
@Slf4j
public class ESController {

    @Resource
    private ESTestService esTestService;


    @GetMapping(value = "/indexCreate")
    public ApiResult<Boolean> indexCreate(@RequestBody ESIndexCreateVo esIndexCreateVo) throws Exception{

        Boolean aBoolean = esTestService.indexCreate(esIndexCreateVo.getIndex(), esIndexCreateVo.getMapping());
        return ApiResult.success(aBoolean);
    }

    public static void main(String[] args) {
        ESIndexCreateVo esIndexCreateVo = new ESIndexCreateVo();
        esIndexCreateVo.setIndex("good");
        esIndexCreateVo.setMapping("{\n" +
                "\t\"properties\": {\n" +
                "\t  \"brandName\": {\n" +
                "\t\t\"type\": \"keyword\"\n" +
                "\t  },\n" +
                "\t  \"categoryName\": {\n" +
                "\t\t\"type\": \"keyword\"\n" +
                "\t  },\n" +
                "\t  \"createTime\": {\n" +
                "\t\t\"type\": \"date\",\n" +
                "\t\t\"format\": \"yyyy-MM-dd HH:mm:ss\"\n" +
                "\t  },\n" +
                "\t  \"id\": {\n" +
                "\t\t\"type\": \"keyword\"\n" +
                "\t  },\n" +
                "\t  \"price\": {\n" +
                "\t\t\"type\": \"double\"\n" +
                "\t  },\n" +
                "\t  \"saleNum\": {\n" +
                "\t\t\"type\": \"integer\"\n" +
                "\t  },\n" +
                "\t  \"status\": {\n" +
                "\t\t\"type\": \"integer\"\n" +
                "\t  },\n" +
                "\t  \"stock\": {\n" +
                "\t\t\"type\": \"integer\"\n" +
                "\t  },\n" +
                "\t  \"title\": {\n" +
                "\t\t\"type\": \"text\",\n" +
                "\t\t\"analyzer\": \"ik_max_word\",\n" +
                "\t\t\"search_analyzer\": \"ik_smart\"\n" +
                "\t  }\n" +
                "\t}\n" +
                "}\n");
        Object o = JSON.toJSON(esIndexCreateVo);
        System.out.println(o);

    }

}
