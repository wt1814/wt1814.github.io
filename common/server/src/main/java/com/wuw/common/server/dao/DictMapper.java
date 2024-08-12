package com.wuw.common.server.dao;

import com.wuw.common.server.model.DO.Dict;
import com.wuw.common.server.model.VO.DictResponseVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DictMapper {
    int deleteByPrimaryKey(String id);

    int insert(Dict record);

    int insertSelective(Dict record);

    Dict selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Dict record);

    int updateByPrimaryKey(Dict record);

    List<DictResponseVo> selectByParentCode(@Param("parentCode")String parentCode);

}