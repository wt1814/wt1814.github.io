package com.wuw.common.server.service.impl;


import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.*;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.aliyuncs.sts.model.v20150401.AssumeRoleRequest;
import com.wuw.common.api.apiResult.ApiResult;
import com.wuw.common.server.model.VO.OSSAutographResponseVo;
import com.wuw.common.server.model.VO.OSSUplodVO;
import com.wuw.common.server.model.VO.STSTicketDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.Date;

@Service
public class ALiOSS {

    @Value("${aliyun.oss.file.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.file.keyid}")
    private String accessKeyId;

    @Value("${aliyun.oss.file.keysecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.file.bucketname}")
    private String bucketName;

    //////////////////////私有桶
    @Value("${aliyun.oss.sts.endpoint}")
    private String STSEndpoint;

    @Value("${aliyun.oss.sts.keyid}")
    private String STSKeyId;

    @Value("${aliyun.oss.sts.keysecret}")
    private String STSKeySecret;

    @Value("${aliyun.oss.sts.roleArn}")
    private String STSRoleArn;
    @Value("${aliyun.oss.sts.bucketname}")
    private String STSBucketName;


    /**
     * 上传公有桶及获取链接地址
     * @param inputStream
     * @param objectName
     * @return
     */
    public String uploadFileAvatar(InputStream inputStream, String objectName) {

        try{
            // 创建OSSClient实例。
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            //调用OSS方法实现上传
            //第一个参数 Bucket名称
            //第二个参数  上传到OSS文件路径和文件名称
            //第三个参数  上传文件输入流
            PutObjectResult putObjectResult = ossClient.putObject(bucketName, objectName, inputStream);

            // 关闭OSSClient。
            ossClient.shutdown();

            //把上传之后文件路径返回
            //需要把上传到阿里云oss路径手动拼接出来
            String url = "";
            url = "https://"+bucketName+"."+endpoint+"/"+objectName;

            return url;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    ///////////////////////////////////////////////////////////////////////私有桶//////////////////////////////////////
    /**
     * STS获取令牌
     * @param roleSessionName
     * @return
     */
    public ApiResult<STSTicketDTO> getAcs(String roleSessionName) {


        // 1. 先从redis获取
        STSTicketDTO stsTicketDTO = null;

        // STSTicketDTO stsTicketDTO = null;
        if (null != stsTicketDTO){
            return ApiResult.success(stsTicketDTO);
        }

        // 2.
        try {

            IClientProfile profile = DefaultProfile.getProfile("", STSKeyId, STSKeySecret);
            String policy = "{\n" +
                    "    \"Version\": \"1\", \n" +
                    "    \"Statement\": [\n" +
                    "        {\n" +
                    "            \"Action\": [\n" +
                    "                \"oss:*\"\n" +
                    "            ], \n" +
                    "            \"Resource\": [\n" +
                    "                \"acs:oss:*:*:internal01/*\" \n" +
                    "            ], \n" +
                    "            \"Effect\": \"Allow\"\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}";
            // 构造client。
            DefaultAcsClient client = new DefaultAcsClient(profile);
            final AssumeRoleRequest request = new AssumeRoleRequest();
            // 适用于Java SDK 3.12.0及以上版本。
            //request.setSysMethod(MethodType.POST);
            // 适用于Java SDK 3.12.0以下版本。
            request.setMethod(MethodType.POST);
            request.setRoleArn(STSRoleArn);
            request.setRoleSessionName(roleSessionName);
            request.setPolicy(policy); // 如果policy为空，则用户将获得该角色下所有权限。
            request.setDurationSeconds(3600L); // 设置临时访问凭证的有效时间为3600秒。
            final com.aliyuncs.sts.model.v20150401.AssumeRoleResponse response = client.getAcsResponse(request);
            stsTicketDTO = new STSTicketDTO();
            stsTicketDTO.setSecurityId(response.getCredentials().getAccessKeyId());
            stsTicketDTO.setSecurityKey(response.getCredentials().getAccessKeySecret());
            stsTicketDTO.setSecurityToken(response.getCredentials().getSecurityToken());
            stsTicketDTO.setRequestId(response.getRequestId());

            // 保存redis

        }catch (Exception e){

        }

        return ApiResult.success(stsTicketDTO);

    }


    /**
     * 下载
     * todo 从acs获取的SecurityId、SecurityToken
     * @param inputStream
     * @param ossUplodVO
     * @return
     */
    public ApiResult<Boolean> uploadOfSTS(InputStream inputStream, OSSUplodVO ossUplodVO) {
        boolean result = false;
        try{
            OSS ossClient = new OSSClientBuilder().build(endpoint, ossUplodVO.getSecurityId(), ossUplodVO.getSecurityKey(), ossUplodVO.getSecurityToken());
            PutObjectRequest putObjectRequest = new PutObjectRequest(STSBucketName, ossUplodVO.getOssPath()+"/"+ossUplodVO.getFileName(), inputStream);
            PutObjectResult putObjectResult = ossClient.putObject(putObjectRequest);
            ossClient.shutdown();
            result = true;
        }catch (Exception e){
            e.printStackTrace();
        }

        return ApiResult.success(result);
    }


    /**
     * 私有桶下载地址
     * todo 从acs获取的SecurityId、SecurityToken
     * @param id
     * @return
     */
    public ApiResult<String> downOfSTS(String id) {

        STSTicketDTO result = null;
        OSS ossClient = new OSSClientBuilder().build(endpoint, result.getSecurityId(), result.getSecurityKey(), result.getSecurityToken());

        Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(STSBucketName, "bojectName-即路径"+"/"+"文件名称", HttpMethod.GET);
        request.setExpiration(expiration);
        // 通过HTTP GET请求生成签名URL。
        URL signedUrl = ossClient .generatePresignedUrl(request);
        String s = signedUrl.toString();
        String cdnUrl = "https://.com"+ s.substring(s.lastIndexOf("com")+3);
        return ApiResult.success(cdnUrl);

    }


    //////////////////////////////////////////////////


    /**
     *
     * @param isPublic
     * @param dir 设置上传到OSS文件的前缀，可置空此项。置空后，文件将上传至Bucket的根目录下。
     * @return
     * @throws Exception
     */
    public ApiResult<OSSAutographResponseVo> getOSSAutograph(Boolean isPublic, String dir) throws Exception{

        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessId = accessKeyId;
        String accessKey = accessKeySecret;
        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint1 = endpoint;
        String bucketName1 = bucketName;
        // 填写Host地址，格式为https://bucketname.endpoint。
        String host = "https://"+bucketName+"."+endpoint1;
        String securityToken = null;
        ApiResult<STSTicketDTO> acs = null;
        if (!isPublic){ // todo 私有桶
            acs = getAcs( "xxxxxxx" );
            if (acs.isSuccess()){
                STSTicketDTO result = acs.getData();
                //accessId = result.getSecurityId();
                //accessKey = result.getSecurityKey();
                accessId = STSKeyId;
                accessKey = STSKeySecret;
                endpoint1 = STSEndpoint;
                bucketName1 = STSBucketName;
                host = "https://"+STSBucketName+"."+endpoint;
                securityToken = result.getSecurityToken();
            }else {
                return ApiResult.fail("500","获取认证失败");
            }
        }

        OSSClient client = new OSSClient(endpoint1, accessId, accessKey,securityToken);
        long expireTime = 30;
        long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
        Date expiration = new Date(expireEndTime);
        PolicyConditions policyConds = new PolicyConditions();
        policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
        policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);

        String postPolicy = client.generatePostPolicy(expiration, policyConds);
        byte[] binaryData = postPolicy.getBytes("utf-8");
        String encodedPolicy = BinaryUtil.toBase64String(binaryData);
        String postSignature = client.calculatePostSignature(postPolicy);

        OSSAutographResponseVo ossAutographResponseVo = new OSSAutographResponseVo();
        ossAutographResponseVo.setAccessid(accessId);
        ossAutographResponseVo.setHost(host);
        ossAutographResponseVo.setPolicy(encodedPolicy);
        ossAutographResponseVo.setDir(dir);
        ossAutographResponseVo.setSignature(postSignature);
        ossAutographResponseVo.setExpire(String.valueOf(expireEndTime / 1000));
        ossAutographResponseVo.setSTSToken(securityToken);
        ossAutographResponseVo.setBucketName(bucketName1);

        if (!isPublic){
            // todo 临时桶返回私有id和key
            ossAutographResponseVo.setAccessid(Base64.getEncoder().encodeToString(acs.getData().getSecurityId().getBytes()));
            ossAutographResponseVo.setAccessKey(Base64.getEncoder().encodeToString(acs.getData().getSecurityKey().getBytes()));
        }

        return ApiResult.success(ossAutographResponseVo);

    }


}
