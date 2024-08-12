package VO;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class OrderInfoVO {

    // 总金额，分
    @NotNull(message = "总金额不能为空")
    private Integer totalAmount;
    // 实付金额
    @NotNull(message = "实付金额不能为空")
    private Integer amount;
    @NotBlank(message = "商品简要信息")
    private String body;


}
