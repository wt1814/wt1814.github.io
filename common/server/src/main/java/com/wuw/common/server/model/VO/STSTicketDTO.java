package com.wuw.common.server.model.VO;

import lombok.Data;

import java.io.Serializable;

@Data
public class STSTicketDTO implements Serializable {


    private String securityId;

    private String securityKey;

    private String securityToken;

    private String requestId;


}
