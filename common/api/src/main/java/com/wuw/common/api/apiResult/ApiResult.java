package com.wuw.common.api.apiResult;

public class ApiResult<T> {
    private boolean success = false;
    private T data = null;
    private String msg = "";
    private String code = "500";
    private String traceId;

    public ApiResult() {
        this.setupTraceId();
    }

    public static <T> ApiResult<T> success(T data) {
        ApiResult<T> r = new ApiResult();
        r.setData(data);
        r.setSuccess(true);
        r.setCode("200");
        r.setMsg("success");
        return r;
    }

    public static <T> ApiResult<T> fail(String code, String msg) {
        ApiResult<T> r = new ApiResult();
        r.setSuccess(false);
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }

    //todo
    private void setupTraceId() {
        try {
            try {
                this.traceId = String.valueOf(Thread.currentThread().getId());
            } catch (Throwable var2) {
                this.traceId = String.valueOf(Thread.currentThread().getId());
            }
        } catch (Throwable var3) {
            ;
        }

    }

    public boolean isSuccess() {
        return this.success;
    }

    public ApiResult<T> setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public T getData() {
        return this.data;
    }

    public ApiResult<T> setData(T data) {
        this.data = data;
        return this;
    }

    public String getMsg() {
        return this.msg;
    }

    public String getTraceId() {
        return this.traceId;
    }

    public ApiResult<T> setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public String getCode() {
        return this.code;
    }

    public ApiResult<T> setCode(String code) {
        this.code = code;
        return this;
    }

}

