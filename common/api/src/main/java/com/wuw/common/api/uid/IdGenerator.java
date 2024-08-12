package com.wuw.common.api.uid;

import com.baidu.fsg.uid.impl.CachedUidGenerator;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * id自增的一种方式
 *
 * @author Administrator
 */
@Component
public class IdGenerator {

    @Resource
    private CachedUidGenerator cachedUidGenerator;

    /**
     * 获取uid
     *
     * @return
     */
    public long nextId() {
        return cachedUidGenerator.getUID();
    }

    /**
     * 格式化传入的uid，方便查看其实际含义
     *
     * @param uid
     * @return
     */
    public String parse(long uid) {
        return cachedUidGenerator.parseUID(uid);
    }
}


