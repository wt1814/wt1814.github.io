package com.wuw.doubleCache.aspect;

import com.github.benmanes.caffeine.cache.Cache;
import com.wuw.doubleCache.annotation.CacheType;
import com.wuw.doubleCache.annotation.DoubleCache;
import com.wuw.doubleCache.config.MessageConfig;
import com.wuw.doubleCache.msg.CacheMassage;
import com.wuw.doubleCache.msg.CacheMsgType;
import com.wuw.doubleCache.msg.MessageSourceUtil;
import com.wuw.doubleCache.util.ElParser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * @program: double-cache
 * @author: Hydra
 * @create: 2022-03-13 10:59
 **/
// todo 增加 【更新、删除缓存】时，发送更新一级缓存操作
@Slf4j
@Component
@Aspect
@AllArgsConstructor
public class CacheAspect {
    private final Cache cache;
    private final RedisTemplate redisTemplate;

    @Pointcut("@annotation(com.wuw.doubleCache.annotation.DoubleCache)")
    public void cacheAspect() {
    }

    @Around("cacheAspect()")
    public Object doAround(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

//        if (!method.isAnnotationPresent(DoubleCache.class))
//            return null;

        //拼接解析springEl表达式的map
        String[] paramNames = signature.getParameterNames();
        Object[] args = point.getArgs();
        TreeMap<String, Object> treeMap = new TreeMap<>();
        for (int i = 0; i < paramNames.length; i++) {
            treeMap.put(paramNames[i],args[i]);
        }

        DoubleCache annotation = method.getAnnotation(DoubleCache.class);
        String elResult = ElParser.parse(annotation.key(), treeMap);
        String realKey = annotation.cacheName() + CacheConstant.COLON + elResult;

        //强制更新
        if (annotation.type()== CacheType.PUT){
            Object object = point.proceed();
            redisTemplate.opsForValue().set(realKey, object,annotation.l2TimeOut(), TimeUnit.SECONDS);
            cache.put(realKey, object);

            //todo 发送信息通知其他节点更新一级缓存
            CacheMassage cacheMassage
                    = new CacheMassage(annotation.cacheName(), CacheMsgType.UPDATE,
                    realKey,object, MessageSourceUtil.getMsgSource());
            // redis发送订阅
            redisTemplate.convertAndSend(MessageConfig.TOPIC,cacheMassage);


            return object;
        }
        //删除
        else if (annotation.type()== CacheType.DELETE){
            redisTemplate.delete(realKey);
            cache.invalidate(realKey);

            //todo 发送信息通知其他节点更新一级缓存
            CacheMassage cacheMassage
                    = new CacheMassage(annotation.cacheName(), CacheMsgType.UPDATE,
                    realKey,null, MessageSourceUtil.getMsgSource());
            // redis发送订阅
            redisTemplate.convertAndSend(MessageConfig.TOPIC,cacheMassage);

            return point.proceed();
        }

        //读写，查询Caffeine
        Object caffeineCache = cache.getIfPresent(realKey);
        if (Objects.nonNull(caffeineCache)) {
            log.info("get data from caffeine");
            return caffeineCache;
        }

        //查询Redis
        Object redisCache = redisTemplate.opsForValue().get(realKey);
        if (Objects.nonNull(redisCache)) {
            log.info("get data from redis");
            cache.put(realKey, redisCache);
            return redisCache;
        }

        log.info("get data from database");
        Object object = point.proceed();
        if (Objects.nonNull(object)){
            //写回Redis
            redisTemplate.opsForValue().set(realKey, object,annotation.l2TimeOut(), TimeUnit.SECONDS);
            //写入Caffeine
            cache.put(realKey, object);
        }
        return object;
    }
}