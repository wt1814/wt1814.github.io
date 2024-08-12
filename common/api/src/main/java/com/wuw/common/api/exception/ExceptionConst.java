package com.wuw.common.api.exception;

/**
 * 异常常量
 * @author fck
 */
public interface ExceptionConst {

    /**
     * 当前系统的异常码10001 - 100000
     */
    interface CurrentSystem{
        BusinessException SYSTEM_ERROR = BusinessException.build(10001,"系统异常,请联系管理员");
        BusinessException VALIDATE_ERROR = BusinessException.build(10002,"入参校验异常");
    }
    /**
     * 客户信息异常码 100001 - 100090
     * @author wangxw
     */
    interface EnpterPriseException{
        BusinessException ENTP_EMPTY_EXCEPTION = BusinessException.build(100001,"未获取到用户信息");
        BusinessException ENTP_ALLOW_FILE_MAX_EXCEPTION = BusinessException.build(100002,"用户信息最大不得超过20M");
        BusinessException ENTP_INIT_USER_INFO_EMPTY_EXCEPTION = BusinessException.build(100003,"初始化用户信息失败");
        BusinessException ENTP_REG_USER_EXIST_EXCEPTION = BusinessException.build(100004,"注册用户已存在用户表中");

    }

}

