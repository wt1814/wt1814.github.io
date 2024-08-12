package com.wuw.ucenter.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.wuw.common.api.uid.IdGenerator;
import com.wuw.doubleCache.annotation.CacheType;
import com.wuw.doubleCache.annotation.DoubleCache;
import com.wuw.ucenter.api.model.DO.UserAccountDO;
import com.wuw.ucenter.api.model.VO.UserAccountResponseVO;
import com.wuw.ucenter.api.model.mapper.UserAccountMapstruct;
import com.wuw.ucenter.server.dao.UserAccountMapper;
import com.wuw.ucenter.server.service.UserAccountService;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

@Service
@Slf4j
public class UserAccountServiceImpl implements UserAccountService{

    @Resource
    private UserAccountMapper userAccountMapper;

    @Resource
    private IdGenerator idGenerator;
    @Value("${aap.name}")
    private String name;


    @Override
    public UserAccountResponseVO getUserAccountByCode(String userCode) {

        System.out.println(name);

        long l = idGenerator.nextId();
        String parse = idGenerator.parse(l);
        System.out.println(parse);

        UserAccountResponseVO userAccountResponseVO = new UserAccountResponseVO();
        UserAccountDO userAccountDO = userAccountMapper.selectByPrimaryKey(userCode);
        if (null != userAccountDO){
            //BeanUtils.copyProperties(userAccountDO, userAccountResponseVO);
            UserAccountMapstruct mapper = Mappers.getMapper(UserAccountMapstruct.class);
            userAccountResponseVO = mapper.doToResponseVO(userAccountDO);
        }
        log.info("userCode：{}，userAccountResponseVO：",userCode, JSON.toJSONString(userAccountResponseVO));
        return userAccountResponseVO;
    }

    @Override
    @DoubleCache(cacheName = "order", key = "#userCode",
            type = CacheType.FULL)
    public UserAccountResponseVO getUserAccountOfCache(String userCode) {

        UserAccountResponseVO userAccountResponseVO = new UserAccountResponseVO();
        UserAccountDO userAccountDO = userAccountMapper.selectByPrimaryKey(userCode);
        if (null != userAccountDO){
            //BeanUtils.copyProperties(userAccountDO, userAccountResponseVO);
            UserAccountMapstruct mapper = Mappers.getMapper(UserAccountMapstruct.class);
            userAccountResponseVO = mapper.doToResponseVO(userAccountDO);
        }
        log.info("userCode：{}，userAccountResponseVO：",userCode, JSON.toJSONString(userAccountResponseVO));
        return userAccountResponseVO;

    }

}
