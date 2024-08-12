package com.wuw.pay.api.VO;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class PayInRequestVO {

    /**
     * 幂等密钥
     */
    @NotBlank(message = "幂等值不能为空")
    private String key;

    @NotBlank(message = "支付方式不能为空")
    private String payInType;
    @NotBlank(message = "支付类型不能为空")
    private String payInTypeDetail;

    @NotBlank(message = "产品代码不能为空")
    private String productCode;

    @NotBlank(message = "用户标识")
    private String openid;
    @NotBlank(message = "token不能为空")
    private String token;

    // 订单
    @NotBlank(message = "订单不能为空")
    private String orderNo;
    // 总金额，分
    @NotNull(message = "总金额不能为空")
    private Integer totalAmount;
    // 实付金额
    @NotNull(message = "实付金额不能为空")
    private Integer amount;

    private String deviceId;
    private String deviceType;
    @NotBlank(message = "clientCode不能为空")
    private String clientCode;

    @NotBlank(message = "商品简要信息")
    private String body ;

    private String subject = "App支付测试Java" ;

}
