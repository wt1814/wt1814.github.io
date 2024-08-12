package com.wuw.pay.server.dao;

import com.wuw.pay.api.DO.PayInOperation;

public interface PayInOperationMapper {
    int deleteByPrimaryKey(String payInThirdId);

    int insert(PayInOperation record);

    int insertSelective(PayInOperation record);

    PayInOperation selectByPrimaryKey(String payInThirdId);

    int updateByPrimaryKeySelective(PayInOperation record);

    int updateByPrimaryKey(PayInOperation record);
}