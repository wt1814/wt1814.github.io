package com.wuw.pay.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.packagescan.resource.ClassPathResource;
import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.AutoUpdateCertificatesVerifier;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import com.wechat.pay.contrib.apache.httpclient.util.AesUtil;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import com.wuw.pay.api.BO.PayInBO;
import com.wuw.pay.api.BO.PayInNotifyBO;
import com.wuw.pay.api.VO.PayInQueryResponse;
import com.wuw.pay.api.VO.PayInQueryResponseOfWX;
import com.wuw.pay.api.DO.PayInThirdNotify;
import com.wuw.pay.api.VO.WXCallVO;
import com.wuw.pay.api.VO.WXJSAPIRequestVo;
import com.wuw.pay.server.dao.PayInThirdNotifyMapper;
import com.wuw.pay.server.service.WXPayService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Signature;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.wechat.pay.contrib.apache.httpclient.constant.WechatPayHttpHeaders.*;

@Service
@Slf4j
public class WXPayServiceImpl implements WXPayService{

    // 商户号
    @Value("${wx.mchid}")
    private String mchId;
    // 商户API证书的证书序列号
    @Value("${wx.mchSerialNo}")
    private String mchSerialNo;
    // apiV3密钥
    @Value("${wx.apiV3Key}")
    private String apiV3Key;
    // 支付回调通知地址
    @Value("${wx.payIn.notifyUrl}")
    private String payInNotifyUrl;

    @Resource
    private PayInThirdNotifyMapper payInThirdNotifyMapper;



    @Override
    public PayInBO payOfJSAPI(WXJSAPIRequestVo wxjsapiRequestVo) throws Exception{

        ////////////////////////////////////////////////////todo 1.构建客户端（官方客户端已经封装好了验签信息，即请求头已经添加Authorization参数） /////////////////////////////////////////////////
        // https://pay.weixin.qq.com/wiki/doc/apiv3/open/pay/chapter2_3.shtml
        // 加载商户私钥（privateKey：私钥字符串）
        String certPath = "/static/wx/apiclient_key.pem";
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        //获得文件流，因为在jar文件中，不能直接通过文件资源路径拿到文件，但是可以在jar包中拿到文件流
        InputStream stream = resolver.getResource(certPath).getInputStream();

        PrivateKey merchantPrivateKey = PemUtil.loadPrivateKey(stream);
        // 加载平台证书（mchId：商户号,mchSerialNo：商户证书序列号,apiV3Key：V3密钥）
        AutoUpdateCertificatesVerifier verifier = new AutoUpdateCertificatesVerifier(
                new WechatPay2Credentials(mchId, new PrivateKeySigner(mchSerialNo, merchantPrivateKey)),apiV3Key.getBytes("utf-8"));
        // 初始化httpClient
        CloseableHttpClient httpClient = WechatPayHttpClientBuilder.create()
                .withMerchant(mchId, mchSerialNo, merchantPrivateKey)
                .withValidator(new WechatPay2Validator(verifier)).build();

        ////////////////////////////////////////////////////todo 2.发送请求 /////////////////////////////////////////////////
        // https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_1_1.shtml
        // todo 注意两种异常（网络异常和业务异常）的处理
        HttpPost httpPost = new HttpPost("https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi");
        httpPost.addHeader("Accept", "application/json");
        httpPost.addHeader("Content-type","application/json; charset=utf-8");
        wxjsapiRequestVo.setMchid(mchId);
        wxjsapiRequestVo.setNotify_url(payInNotifyUrl);
        wxjsapiRequestVo.getAmount().setPayer_total(null);
        String s = JSON.toJSONString(wxjsapiRequestVo);
        log.info("，下单：{}",s);
        httpPost.setEntity(new StringEntity(s, "UTF-8"));

        String bodyAsString = null;
        try{
            CloseableHttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            bodyAsString = EntityUtils.toString(response.getEntity());
            if (statusCode == 200) { //处理成功
                log.info("成功，返回结果 = " + bodyAsString);
            } else if (statusCode == 204) { //处理成功，无返回Body
                log.info("成功");
            } else {
                throw new IOException("request failed");
            }
        }catch (Exception e){
            return null;
        }

        Map maps = (Map)JSON.parse(bodyAsString);
        String prepayId = (String) maps.get("prepay_id");

        ////////////////////////////////////////////////////todo 3.前端返回参数，注意字段paySign（签名，要使用验签算法） /////////////////////////////////////////////////
        // https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_1_4.shtml#menu1
        // todo 返回前端 appId、timeStamp、nonceStr、package、signType、paySign（签名，要使用验签算法）、outTradeNo
        HashMap<String, String> payMap = new HashMap<>();
        payMap.put("appid",wxjsapiRequestVo.getAppid());//appid
        long currentTimestamp = System.currentTimeMillis();//时间戳，别管那么多，他就是需要
        payMap.put("timeStamp",currentTimestamp+"");
        String nonceStr = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 32);
        payMap.put("nonceStr",nonceStr);
        payMap.put("package","prepay_id="+prepayId);
        payMap.put("signType","RSA");
        String paySign = getSign(wxjsapiRequestVo.getAppid(), currentTimestamp, nonceStr, payMap.get("package"),certPath);
        payMap.put("paySign",paySign);

        PayInBO payInBO = PayInBO.builder().payMap(payMap).result(true).desc("SUCCESS").outTradeNo(prepayId).status(1).build();
        return payInBO;
    }

    @Override
    public PayInNotifyBO payNotify(HttpServletRequest request) throws Exception{

        PayInNotifyBO payInNotifyBO = new PayInNotifyBO();

        // 1. 验签
        String s = this.verifySign(request);
        // 2. 解密
        Map<String, String> map = this.dataDecryption(s);
        log.info("微信支付处理后的数据data={}", map.get("notifyContent"));
        /*JSONObject jsonObject = JSONObject.parseObject(data);*/
        WXCallVO wxCallVO = JSON.parseObject( map.get("notifyContent"), WXCallVO.class);

        // 3. 通知数据入库
        PayInThirdNotify payInThirdNotify = PayInThirdNotify.builder().inThirdNotiryId(wxCallVO.getTransaction_id()).payInNo(wxCallVO.getOut_trade_no()).thirdTradeNo(wxCallVO.getTransaction_id())
                .payInType("wx").payInTypeDetail("jsapi").payInTime(new Date()).payInAmount(wxCallVO.getAmount().getPayer_total()).payInResult(wxCallVO.getTrade_state()).notifyContent(map.get("notifyContent")).notifyContentOriginal(map.get("notifyContentOriginal")).createTime(new Date()).build();
        payInThirdNotifyMapper.insert(payInThirdNotify);

        // 4. 封装返回
        payInNotifyBO.setOutTradeNo(wxCallVO.getOut_trade_no());
        payInNotifyBO.setPayInStatus("SUCCESS".equals(wxCallVO.getTrade_state())?2:1);
        payInNotifyBO.setPayInTotalAmount(null != wxCallVO.getAmount()?wxCallVO.getAmount().getPayer_total():0);

        return payInNotifyBO;
    }

    /**
     * 解密
     * @param data
     * @return
     */
    public Map<String,String> dataDecryption(String data){

        Map<String,String> map = new HashMap<>();

        JSONObject jsonObject = JSONObject.parseObject(data);
        System.out.println(jsonObject);

        String eventType = jsonObject.getString("event_type");
        String resourceType = jsonObject.getString("resource_type");
        if (!Objects.equals(eventType,"TRANSACTION.SUCCESS") || !Objects.equals(resourceType,"encrypt-resource")){
            log.info("不是支付通知不处理:{}",data);
            return null;
        }
        //参数解密
        JSONObject resource = jsonObject.getJSONObject("resource");
        String ciphertext = resource.getString("ciphertext");
        String nonce = resource.getString("nonce");
        String associatedData = resource.getString("associated_data");
        AesUtil aesUtil = new AesUtil(apiV3Key.getBytes(StandardCharsets.UTF_8));
        String result = null;
        try {
            result = aesUtil.decryptToString(associatedData.getBytes(StandardCharsets.UTF_8),nonce.getBytes(StandardCharsets.UTF_8),ciphertext);
        } catch (GeneralSecurityException e) {
            log.error("微信v3解密异常",e);
        }
        log.info("解密之后的数据是："+result);

        map.put("notifyContentOriginal",data);
        map.put("notifyContent",result);
        return map;
    }


    /**
     * 签名校验
     * url https://pay.weixin.qq.com/wiki/doc/apiv3_partner/apis/chapter5_1_13.shtml
     */
    public String verifySign(HttpServletRequest request) throws Exception {
        //检查header
        String[] headers = {WECHAT_PAY_SERIAL, WECHAT_PAY_SIGNATURE, WECHAT_PAY_NONCE, WECHAT_PAY_TIMESTAMP};
        for (String headerName : headers) {
            if (request.getHeader(headerName) == null) {
                log.info("{} is null", headerName);
                return null;
            }
        }
        //检查时间
        String timestamp = request.getHeader(WECHAT_PAY_TIMESTAMP);
        Instant responseTime = Instant.ofEpochSecond(Long.parseLong(timestamp));
        if (Duration.between(responseTime, Instant.now()).abs().toMinutes() >= 5) {
            log.info("超过应答时间");
            return null;
        }
        //获取微信返回的参数
        String data;
        try {
            data = request.getReader().lines().collect(Collectors.joining());
        } catch (IOException e) {
            log.error("获取微信V3回调参数失败",e);
            return null;
        }
        //校验签名
        String nonce = request.getHeader(WECHAT_PAY_NONCE);
        String message =  timestamp + "\n" + nonce + "\n" + data + "\n";
        String serial = request.getHeader(WECHAT_PAY_SERIAL);
        String signature = request.getHeader(WECHAT_PAY_SIGNATURE);

/*
        String nonce = "jxSVnv2AfgZ3I9I2GzI31vMzcZnJ8sLj";
        String timestamp = "1663904197";
        String serialNo = "7C7B5AF2584FE12CBFD9FC8DD254DA54EB853176";
        String signature = "YoGckXg4Wa89F5n5bs0VeaIrT/PVYavBBarEEWNgPX0+ns557LxCLMV79Gn7TMoat5I80dKPgJAhriWG2F9A/OBehEdotphcynUl/B6avPBGAIY+fpoRgcLkO34s2KXAlqKVzkyE1mmvqsoOk6wYXSLmUJhySEAAkXn9BKuU19FUIMM2c6FpHw3JgThqpzLSDqIL63z0wA8aU8B5cvN+OJ40JiZC48NHyPeuHaRkyZ25yVzV/i6zbMMLFo9iN37vWqK6hlfdKXw+1VXO5XQeRXURuU/R03MuSg+R1LwLvqcQMzBXNpUMoZxM8KX6Wy8inB9FyLR6HE0uW2vb0aAYcA==";

*/

        String certPath = "/static/wx/apiclient_key.pem";
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        //获得文件流，因为在jar文件中，不能直接通过文件资源路径拿到文件，但是可以在jar包中拿到文件流
        InputStream stream = resolver.getResource(certPath).getInputStream();

        PrivateKey merchantPrivateKey = PemUtil.loadPrivateKey(stream);
        // 加载平台证书（mchId：商户号,mchSerialNo：商户证书序列号,apiV3Key：V3密钥）
        AutoUpdateCertificatesVerifier verifier = new AutoUpdateCertificatesVerifier(
                new WechatPay2Credentials(mchId, new PrivateKeySigner(mchSerialNo, merchantPrivateKey)),apiV3Key.getBytes("utf-8"));

        if (!verifier.verify(serial, message.getBytes(StandardCharsets.UTF_8), signature)) {
            log.info("签名校验失败");
            return null;
        }
        return data;

    }



    @Override
    public PayInQueryResponse query(String outTradeNo) throws Exception {

        PayInQueryResponse payInQueryResponse = new PayInQueryResponse();
        ////////////////////////////////////////////////////todo 1.构建客户端（官方客户端已经封装好了验签信息，即请求头已经添加Authorization参数） /////////////////////////////////////////////////
        // https://pay.weixin.qq.com/wiki/doc/apiv3/open/pay/chapter2_3.shtml
        // 加载商户私钥（privateKey：私钥字符串）
        String certPath = "/static/wx/apiclient_key.pem";
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        //获得文件流，因为在jar文件中，不能直接通过文件资源路径拿到文件，但是可以在jar包中拿到文件流
        InputStream stream = resolver.getResource(certPath).getInputStream();
        PrivateKey merchantPrivateKey = PemUtil.loadPrivateKey(stream);
        // 加载平台证书（mchId：商户号,mchSerialNo：商户证书序列号,apiV3Key：V3密钥）
        AutoUpdateCertificatesVerifier verifier = new AutoUpdateCertificatesVerifier(
                new WechatPay2Credentials(mchId, new PrivateKeySigner(mchSerialNo, merchantPrivateKey)),apiV3Key.getBytes("utf-8"));
        // 初始化httpClient
        CloseableHttpClient httpClient = WechatPayHttpClientBuilder.create()
                .withMerchant(mchId, mchSerialNo, merchantPrivateKey)
                .withValidator(new WechatPay2Validator(verifier)).build();

        ////////////////////////////////////////////////////todo 2.发送请求 /////////////////////////////////////////////////
        //2.执行get请求并返回结果
        // 商户订单号查询 https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_1_2.shtml
        String url = "https://api.mch.weixin.qq.com/v3/pay/transactions/out-trade-no/"+outTradeNo+"?mchid="+mchId;
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Accept", "application/json");
        httpGet.addHeader("Content-type","application/json; charset=utf-8");
        CloseableHttpResponse response = null;
        String bodyAsString = "";
        try {
            response = httpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            bodyAsString = EntityUtils.toString(response.getEntity());
            log.info("");
        } catch (IOException e1) {
            log.error("");

        }

        // 3. 对结果处理
        PayInQueryResponseOfWX payInQueryResponseOfWX = (PayInQueryResponseOfWX)JSON.parse(bodyAsString);
        // 3.1. 校验支付单

        // 3.2.
        if (payInQueryResponseOfWX.getTrade_state().equals("SUCCESS")){
            payInQueryResponse.setResult(true);
        }else {
            payInQueryResponse.setResult(false);
            payInQueryResponse.setDesc(payInQueryResponseOfWX.getTrade_state_desc());
        }

        return payInQueryResponse;
    }


    /**
     * 作用：使用字段appId、timeStamp、nonceStr、package计算得出的签名值
     * 场景：根据微信统一下单接口返回的 prepay_id 生成调启支付所需的签名值
     * @param appId
     * @param timestamp
     * @param nonceStr
     * @param pack package
     * @return
     * @throws Exception
     */
    private String getSign(String appId, long timestamp, String nonceStr, String pack,String certPath) throws Exception{
        String message = buildMessage(appId, timestamp, nonceStr, pack);
        String paySign= sign(message.getBytes("utf-8"),certPath);
        return paySign;
    }

    private String buildMessage(String appId, long timestamp, String nonceStr, String pack) {
        String result = "";

        return appId + "\n"
                + timestamp + "\n"
                + nonceStr + "\n"
                + pack + "\n";
    }
    private String sign(byte[] message,String certPath) throws Exception{
        Signature sign = Signature.getInstance("SHA256withRSA");
        //这里需要一个PrivateKey类型的参数，就是商户的私钥。
        ClassPathResource classPathResource = new ClassPathResource(certPath);
        sign.initSign(PemUtil.loadPrivateKey(new FileInputStream(classPathResource.getFile())));
        sign.update(message);
        return Base64.getEncoder().encodeToString(sign.sign());
    }


}
