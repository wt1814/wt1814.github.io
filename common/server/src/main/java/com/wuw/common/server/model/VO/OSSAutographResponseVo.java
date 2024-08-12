package com.wuw.common.server.model.VO;

import lombok.Data;

/**
 * 阿里云签名
 */
@Data
public class OSSAutographResponseVo {

    private String accessid;
    private String host;
    private String policy;
    private String dir;
    private String signature;
    private String expire;
    private String STSToken;
    private String bucketName;
    private String accessKey;

}
