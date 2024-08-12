package com.wt.monitor.utils.dingding;

import lombok.Data;

import java.util.List;

/**
 * @author wt1814
 * @date 2020/10/27
 */
@Data
public class DingtalkAtBO {

    private List<String> atMobiles;

    private Boolean isAtAll;
}
