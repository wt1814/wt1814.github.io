package com.wuw.ucenter.server.dao;

import com.wuw.ucenter.api.model.DO.UserAccountDO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
public interface UserAccountMapper {

    int deleteByPrimaryKey(String id);

    int insert(UserAccountDO record);

    int insertSelective(UserAccountDO record);

    UserAccountDO selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(UserAccountDO record);

    int updateByPrimaryKey(UserAccountDO record);

}