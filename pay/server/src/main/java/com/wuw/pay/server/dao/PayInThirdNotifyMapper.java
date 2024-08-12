package com.wuw.pay.server.dao;


import com.wuw.pay.api.DO.PayInThirdNotify;

public interface PayInThirdNotifyMapper {
    int deleteByPrimaryKey(String inThirdNotiryId);

    int insert(PayInThirdNotify record);

    int insertSelective(PayInThirdNotify record);

    PayInThirdNotify selectByPrimaryKey(String inThirdNotiryId);

    int updateByPrimaryKeySelective(PayInThirdNotify record);

    int updateByPrimaryKeyWithBLOBs(PayInThirdNotify record);

    int updateByPrimaryKey(PayInThirdNotify record);
}