package com.wuw.pay.server.dao;

import com.wuw.pay.api.DO.PayIn;

public interface PayInMapper {
    int deleteByPrimaryKey(String payInId);

    int insert(PayIn record);

    int insertSelective(PayIn record);

    PayIn selectByPrimaryKey(String payInId);

    int updateByPrimaryKeySelective(PayIn record);

    int updateByPrimaryKey(PayIn record);

    PayIn selectByOrderNo(String orderNo);


}