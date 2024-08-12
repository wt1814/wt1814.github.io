package com.wuw.ucenter.server.service;

import com.wuw.ucenter.api.model.VO.UserAccountResponseVO;

public interface UserAccountService {

    UserAccountResponseVO getUserAccountByCode(String userCode);

    UserAccountResponseVO getUserAccountOfCache(String userCode);

}
