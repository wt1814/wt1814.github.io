package com.wt.monitor.utils;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @Author: wt1814
 * @Date: 2021/5/31 5:56 下午
 * @Description:
 */
@Slf4j
public class SystemUtil {
    /**
     * 获得ip地址
     *
     * @return
     */
    public static String getIpAddress() {
        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            log.error("error", e);
        }

        return address.getHostAddress();
    }
}
