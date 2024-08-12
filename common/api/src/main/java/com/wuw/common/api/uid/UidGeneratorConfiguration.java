package com.wuw.common.api.uid;

import com.baidu.fsg.uid.buffer.RejectedPutBufferHandler;
import com.baidu.fsg.uid.buffer.RejectedTakeBufferHandler;
import com.baidu.fsg.uid.buffer.RingBuffer;
import com.baidu.fsg.uid.impl.CachedUidGenerator;
import com.baidu.fsg.uid.impl.DefaultUidGenerator;
import com.baidu.fsg.uid.worker.DisposableWorkerIdAssigner;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 两种生成策略注入Bean
 *
 * @author Administrator
 */
@Configuration
@MapperScan(value = "com.baidu.fsg.uid.worker.dao")
public class UidGeneratorConfiguration {

    /**
     * RingBuffer size扩容参数, 可提高UID生成的吞吐量. -->
     * 默认:3， 原bufferSize=8192, 扩容后bufferSize= 8192 << 3 = 65536
     * CachedUidGenerator 参数{@link UidGeneratorConfiguration#cachedUidGenerator()}
     */
    private int boostPower = 3;
    /**
     * 指定何时向RingBuffer中填充UID, 取值为百分比(0, 100), 默认为50 -->
     * 举例: bufferSize=1024, paddingFactor=50 -> threshold=1024 * 50 / 100 = 512.
     * 当环上可用UID数量 < 512时, 将自动对RingBuffer进行填充补全
     */
    private int paddingFactor = 50;
    /**
     * 另外一种RingBuffer填充时机, 在Schedule线程中, 周期性检查填充
     * 默认:不配置此项, 即不实用Schedule线程. 如需使用, 请指定Schedule线程时间间隔, 单位:秒
     */
    private Long scheduleInterval;
    /**
     * 拒绝策略: 当环已满, 无法继续填充时 -->
     * 默认无需指定, 将丢弃Put操作, 仅日志记录. 如有特殊需求, 请实现RejectedPutBufferHandler接口(支持Lambda表达式)
     */
    private RejectedPutBufferHandler rejectedPutBufferHandler;
    /**
     * 拒绝策略: 当环已空, 无法继续获取时 -->
     * 默认无需指定, 将记录日志, 并抛出UidGenerateException异常. 如有特殊需求, 请实现RejectedTakeBufferHandler接口(支持Lambda表达式)
     */
    private RejectedTakeBufferHandler rejectedTakeBufferHandler;
    /**
     * 暂时不知道
     */
    private RingBuffer ringBuffer;

    @Bean(name = "cachedUidGenerator")
    public CachedUidGenerator cachedUidGenerator(){
        CachedUidGenerator cachedUidGenerator = new CachedUidGenerator();
        cachedUidGenerator.setWorkerIdAssigner(disposableWorkerIdAssigner());
        return cachedUidGenerator;
    }

    // todo 使用数据库生成workerId
    @Bean(name = "disposableWorkerIdAssigner")
    public DisposableWorkerIdAssigner disposableWorkerIdAssigner(){

        return new DisposableWorkerIdAssigner();
    }

    // todo 自定义生成workerId
/*    @Bean(name = "workerIdAssigner")
    public WorkerIdAssignerImpl workerIdAssigner(){

        return new WorkerIdAssignerImpl();
    }*/


    /**
     * # 时间位, 默认:28
     */
    private int timeBits;
    /**
     * # 机器位, 默认:22
     */
    private int workerBits;
    /**
     * # 序列号, 默认:13
     */
    private int seqBits;
    /**
     * # 初始时间, 默认:"2016-05-20"
     */
    //todo
    @Value("${uid.epochStr}")
    private String epochStr;

    @Bean(name = "defaultUidGenerator")
    public DefaultUidGenerator defaultUidGenerator(){
        DefaultUidGenerator defaultUidGenerator = new DefaultUidGenerator();
        defaultUidGenerator.setWorkerIdAssigner(disposableWorkerIdAssigner());
        return defaultUidGenerator;
    }
}

