package com.wuw.pay.api.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WXAmount {

    private Integer total;
    private Integer payer_total;
    private String currency;
    private String payer_currency;

}