package com.wuw.common.api.apiResult;

import com.alibaba.fastjson.JSON;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * todo 统一的响应处理
 */
@RestControllerAdvice("com.wuw")
public class GlobalResponse implements ResponseBodyAdvice<Object> {

    /**
     * 拦截之前业务处理，请求先到supports再到beforeBodyWrite
     * <p>
     * 用法1：自定义是否拦截。若方法名称（或者其他维度的信息）在指定的常量范围之内，则不拦截。
     *
     * @param returnType
     * @param converterType
     * @return 返回true会执行拦截；返回false不执行拦截
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {

        //TODO 过滤
        return true;
    }

    /**
     * 向客户端返回响应信息之前的业务逻辑处理
     * <p>
     * 用法1：无论controller返回什么类型的数据，在写入客户端响应之前统一包装，客户端永远接收到的是约定的格式
     * <p>
     * 用法2：在写入客户端响应之前统一加密
     *
     * @param body     响应内容
     * @param returnType
     * @param selectedContentType
     * @param selectedConverterType
     * @param request
     * @param response
     * @return
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        //body是否为null
        if (null == body) {
            return ApiResult.fail("500","selectedContentType");
        }
        //body是否是文件
        if (body instanceof Resource) {
            return body;
        }
        //该方法返回值类型是否是void
        //if ("void".equals(methodParameter.getParameterType().getName())) {
        //  return new GlobalResponseEntity<>("55555", "response is empty.");
        //}
        if (returnType.getMethod().getReturnType().isAssignableFrom(Void.TYPE)) {
            return ApiResult.fail("500","selectedContentType");
        }
        //该方法返回值类型是否是GlobalResponseEntity。若是直接返回，无需再包装一层
        if (body instanceof ApiResult) {
            return body;
        }
        //处理string类型的返回值
        //当返回类型是String时，用的是StringHttpMessageConverter转换器，无法转换为Json格式
        //必须在方法体上标注RequestMapping(produces = "application/json; charset=UTF-8")
        if (body instanceof String) {
            String responseString = JSON.toJSONString(ApiResult.success(body));
            return responseString;
        }
        //该方法返回的媒体类型是否是application/json。若不是，直接返回响应内容
        if (!selectedContentType.includes(MediaType.APPLICATION_JSON)) {
            return body;
        }
        return ApiResult.success(body);

    }

}
