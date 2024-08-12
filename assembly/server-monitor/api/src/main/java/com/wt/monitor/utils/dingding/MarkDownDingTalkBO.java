package com.wt.monitor.utils.dingding;

import lombok.Data;


@Data
public class MarkDownDingTalkBO extends DingTalkBO {

    private String msgtype ;

    private MarkDownBO markdown ;

    private DingtalkAtBO at;
}
