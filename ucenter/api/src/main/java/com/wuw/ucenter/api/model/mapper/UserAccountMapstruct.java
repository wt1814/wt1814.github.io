package com.wuw.ucenter.api.model.mapper;

import com.wuw.ucenter.api.model.DO.UserAccountDO;
import com.wuw.ucenter.api.model.VO.UserAccountResponseVO;
import org.mapstruct.Mapper;

@Mapper
public interface UserAccountMapstruct {

    UserAccountResponseVO doToResponseVO(UserAccountDO userAccountDO);

}
