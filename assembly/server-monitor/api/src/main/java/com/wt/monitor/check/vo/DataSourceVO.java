package com.wt.monitor.check.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author: wt1814
 * @Date: 2021/7/1 10:36 上午
 * @Description:
 */
@Data
public class DataSourceVO {
    private Integer resultCode;
    private List<DruidDataSourceStatValueVO> content;
}
