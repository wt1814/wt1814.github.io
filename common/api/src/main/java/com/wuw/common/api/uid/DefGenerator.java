package com.wuw.common.api.uid;

import com.baidu.fsg.uid.impl.DefaultUidGenerator;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 默认的生成器策略
 *
 * @author Administrator
 */
@Component
public class DefGenerator {

    @Resource
    private DefaultUidGenerator defaultUidGenerator;

    /**
     * 获取uid
     *
     * @return
     */
    public long nextId() {
        return defaultUidGenerator.getUID();
    }

    /**
     * 格式化传入的uid，方便查看其实际含义
     *
     * @param uid
     * @return
     */
    public String parse(long uid) {
        return defaultUidGenerator.parseUID(uid);
    }
}
