package com.wuw.common.server.dao;

import com.wuw.common.server.model.DO.SmsRecord;

public interface SmsRecordMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SmsRecord record);

    int insertSelective(SmsRecord record);

    SmsRecord selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SmsRecord record);

    int updateByPrimaryKeyWithBLOBs(SmsRecord record);

    int updateByPrimaryKey(SmsRecord record);
}