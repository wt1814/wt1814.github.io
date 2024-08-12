package com.wuw.common.api.exception;

import com.wuw.common.api.apiResult.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 统一异常处理
 */
@RestControllerAdvice
@Slf4j
public class CommonExceptionHandler {


    /**
     * 请求方式不支持
     */
/*    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    public ApiResult handleException(HttpRequestMethodNotSupportedException e) {
        log.error(e.getMessage(), e);

        return AjaxResult.error("不支持' " + e.getMethod() + "'请求");
    }*/

    /**
     * 拦截未知的运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ApiResult notFount(RuntimeException e) {

        log.error("运行时异常:", e);
        return ApiResult.fail("500","运行时异常:" + e.getMessage());
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public ApiResult handleException(Exception e) {

        log.error(e.getMessage(), e);
        return ApiResult.fail("500","服务器错误" + e.getMessage());

    }

    /**
     * 校验异常
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ApiResult exceptionHandler(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        String errorMesssage = "";
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errorMesssage += fieldError.getDefaultMessage() + "!";
        }
        return ApiResult.fail("500",errorMesssage);
    }

    /**
     * 校验异常
     */
    @ExceptionHandler(value = BindException.class)
    public ApiResult validationExceptionHandler(BindException e) {
        BindingResult bindingResult = e.getBindingResult();
        String errorMesssage = "";
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errorMesssage += fieldError.getDefaultMessage() + "!";
        }
        return ApiResult.fail("500",errorMesssage);
    }

    /**
     * 校验异常
     */
    @ExceptionHandler(value = ConstraintViolationException.class)
    public ApiResult ConstraintViolationExceptionHandler(ConstraintViolationException ex) {
        Set<ConstraintViolation<?>> constraintViolations = ex.getConstraintViolations();
        Iterator<ConstraintViolation<?>> iterator = constraintViolations.iterator();
        List<String> msgList = new ArrayList<>();
        while (iterator.hasNext()) {
            ConstraintViolation<?> cvl = iterator.next();
            msgList.add(cvl.getMessageTemplate());
        }
        return ApiResult.fail("500",String.join(",",msgList));
    }

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ApiResult businessException(BusinessException e) {
        return ApiResult.fail(e.getCode().toString(),e.getMessage());
    }

}