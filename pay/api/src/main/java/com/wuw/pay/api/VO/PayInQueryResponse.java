package com.wuw.pay.api.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayInQueryResponse {

    private Boolean result;
    private String desc;

}
