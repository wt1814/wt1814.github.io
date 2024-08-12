package com.wuw.common.server.model.VO;

import lombok.Data;

import java.io.Serializable;

@Data
public class OSSUplodVO extends STSTicketDTO implements Serializable {


    private String ossPath;

    private String fileName;


}