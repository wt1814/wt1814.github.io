package com.wuw.ucenter.api.model.DO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAccountDO {

    private String id;

    private String userPass;

    private Date createTime;

    private String createBy;

    private Date updateTime;

    private String updateBy;


}