package com.wuw.common.server.model.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DictResponseVo {

    private String id;

    private String dictCode;

    private String dictName;

    private Integer orderNum;

    private String parentCode;

    private List<DictResponseVo> dictVos;

}
