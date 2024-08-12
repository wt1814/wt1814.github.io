package com.wuw.common.api.exception;

/**
 * 自定义业务异常
 */
public class BusinessException extends RuntimeException {

    private Integer code;
    private String message;

    public BusinessException() {
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public static BusinessException build(Integer code, String message) {
        return new BusinessException(code, message);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}