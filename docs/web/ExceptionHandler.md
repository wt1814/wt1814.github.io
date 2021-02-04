
<!-- TOC -->

- [1. 异常处理](#1-异常处理)
    - [1.1. 自定义异常](#11-自定义异常)
    - [1.2. 统一异常处理](#12-统一异常处理)

<!-- /TOC -->

# 1. 异常处理  

<!-- 
用 Assert(断言) 替换 throw exception
-->

## 1.1. 自定义异常
1. 新建错误码枚举类。作用：可以使用错误码枚举构造自定义异常。好处：规范编码。  

```java
/**
 * @ProjectName：
 * @ClassName: BusinessRunTimeException
 * @Description: 错误码枚举  
 * @Author:
 * @CreateDate: 
 * @UpdateUser: 
 * @UpdateDate:   
 * @UpdateRemark:
 * @Version: V1.0
 **/
public enum ErrorCodeEnum {
    SUCESS(200,"SUCCESS"),
    Internal(500,"服务器开小差啦！");


    private Integer errorCode;

    private String errorMsg;


    ErrorCodeEnum(Integer errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
```

2. 自定义异常。 

```java
/**
 * @ProjectName：
 * @ClassName: BusinessRunTimeException
 * @Description: 自定义异常 
 * @Author:  
 * @CreateDate: 
 * @UpdateUser: 
 * @UpdateDate:   
 * @UpdateRemark:
 * @Version: V1.0
 **/
public class BusinessRunTimeException extends RuntimeException{

    private Integer errorCode;

    /**
     * 构造函数
     * @param errorCode
     */
    public BusinessRunTimeException(Integer errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     * @param errorCode
     */
    public BusinessRunTimeException(String message, Integer errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 构造函数，使用枚举
     * @param errorCodeEnums
     */
    //todo 使用错误码枚举构造自定义异常，规范编码。
    public BusinessRunTimeException(ErrorCodeEnum errorCodeEnums) {
        super(errorCodeEnums.getErrorMsg());
        this.errorCode = errorCodeEnums.getErrorCode();
    }

    /**
     * get、set方法
     * @return
     */
    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }
}
```
3. 自定义异常使用

```java
try{
    // 抛出自定义异常
    throw new BusinessRunTimeException(ErrorCodeEnum.Internal);
}catch (BusinessRunTimeException e){
    // 捕获自定义异常
    log.info("errorCode:【{}】，errorMsg:【{}】",e.getErrorCode(),e.getMessage());
}
```

## 1.2. 统一异常处理
&emsp; 使用异常处理器注解@ExceptionHandler  
1. 可以在每一个Controller类都定义一套这样的异常处理方法
2. 结合@ControllerAdvice实现统一异常处理




-------------

<!-- 



    /**
     *
     * @param ex
     * @return
     */
    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Result handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        StringBuilder sb = new StringBuilder("校验失败:");
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            sb.append(fieldError.getField()).append("：").append(fieldError.getDefaultMessage()).append(", ");
        }
        String msg = sb.toString();

        return Result.fail("500", msg);
    }
    /**
     *
     * @param ex
     * @return
     */
    @ExceptionHandler({ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Result handleConstraintViolationException(ConstraintViolationException ex) {
        return Result.fail("500", ex.getMessage());
    }

    /**
     *
     * @param ex
     * @return
     */
    @ExceptionHandler({Exception.class})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Result handleException(Exception ex) {
        log.error("异常信息：{}",ex);
        return Result.fail("500", ex.getMessage());
    }

-->
