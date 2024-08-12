package com.wt.monitor.check.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author: wt1814
 * @Date: 2021/7/1 10:14 上午
 * @Description:
 */
@Data
public class DruidDataSourceStatValueVO {

    /**
     *
     */
    private String name;

    /**
     * 数据类型
     */
    private String dbType;

    /**
     * jdbc驱动的类名
     */
    private String driverClassName;

    /**
     * 链接地址
     */
    private String url;

    /**
     * 用户名
     */
    private String userName;

    /**
     * filter类名
     */
    private List<String> filterClassNames;

    private boolean removeAbandoned;

    /**
     * 初始化连接大小
     */
    private int initialSize;

    /**
     * 连接池中最小的活跃连接数
     */
    private int minIdle;

    /**
     * 连接池中最大的活跃连接数
     */
    private int maxActive;

    /**
     * 查询超时时间
     */
    private int queryTimeout;

    /**
     * 事务查询超时时间
     */
    private int transactionQueryTimeout;

    /**
     * 登录超时时间
     */
    private int loginTimeout;

    /**
     * 连接有效性检查类名
     */
    private String validConnectionCheckerClassName;

    /**
     * ExceptionSorter类名
     */
    private String exceptionSorterClassName;

    /**
     * 获取连接时检测
     */
    private boolean testOnBorrow;

    /**
     * 连接放回连接池时检测
     */
    private boolean testOnReturn;

    /**
     * 空闲时检测
     */
    private boolean testWhileIdle;

    /**
     * 默认autocommit设置
     */
    private boolean defaultAutoCommit;

    /**
     * 默认只读设置
     */
    private boolean defaultReadOnly;

    /**
     * 默认事务隔离
     */
    private Integer defaultTransactionIsolation;

    /**
     * 活跃连接数
     */
    private int activeCount;

    /**
     * 活跃连接数峰值
     */
    private int activePeak;

    /**
     * 活跃连接数峰值时间
     */
    private long activePeakTime;

    /**
     * 池中连接数
     */
    private int poolingCount;

    /**
     * 池中连接数峰值
     */
    private int poolingPeak;

    /**
     * 池中连接数峰值时间
     */
    private long poolingPeakTime;

    /**
     * 等待线程数量
     */
    private long waitThreadCount;

    /**
     * 等待次数
     */
    private long notEmptyWaitCount;

    /**
     * 逻辑连接错误次数
     */
    private long logicConnectErrorCount;

    /**
     * 物理连接打开次数
     */
    private long physicalConnectCount;

    /**
     * 物理关闭数量
     */
    private long physicalCloseCount;

    /**
     * 物理连接错误次数
     */
    private long physicalConnectErrorCount;

    /**
     * 执行数(总共)
     */
    private long executeCount;

    /**
     * 错误数
     */
    private long errorCount;

    /**
     * 提交数
     */
    private long commitCount;

    /**
     * 回滚数
     */
    private long rollbackCount;

    /**
     * 事务启动数
     */
    private long startTransactionCount;

    /**
     * KeepAlive检测次数
     */
    private long keepAliveCheckCount;

    /**
     * 连接持有时间分布
     */
    private long[] connectionHoldTimeHistogram;
}
