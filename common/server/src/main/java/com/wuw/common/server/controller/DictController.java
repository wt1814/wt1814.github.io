package com.wuw.common.server.controller;


import com.wuw.common.api.apiResult.ApiResult;
import com.wuw.common.server.model.VO.DictRequestVo;
import com.wuw.common.server.model.VO.DictResponseVo;
import com.wuw.common.server.service.DictService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/dict")
@Slf4j
public class DictController {


    @Resource
    private DictService dictService;

    @GetMapping("/findDictsByCode")
    public ApiResult<List<DictResponseVo>> findDictsByCode(@RequestBody DictRequestVo dictRequestVo){
        List<DictResponseVo> dictsByCode = dictService.findDictsByCode(dictRequestVo);
        return ApiResult.success(dictsByCode);

    }

}
