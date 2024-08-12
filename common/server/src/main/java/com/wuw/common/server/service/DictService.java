package com.wuw.common.server.service;

import com.wuw.common.server.model.DO.Dict;
import com.wuw.common.server.model.VO.DictRequestVo;
import com.wuw.common.server.model.VO.DictResponseVo;

import java.util.List;

public interface DictService {


    List<DictResponseVo> findDictsByCode(DictRequestVo dictRequestVo);

}
