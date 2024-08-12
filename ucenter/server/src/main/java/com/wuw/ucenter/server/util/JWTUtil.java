package com.wuw.ucenter.server.util;

import com.alibaba.fastjson.JSON;
import com.wuw.ucenter.api.model.VO.ESIndexCreateVo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class JWTUtil {

    private static String secretKey= "dXYkZDdydCM5b0BzenBuM2hvbWV3b3JrYWRtaW4" ;


    public static void main(String[] args) {

        ESIndexCreateVo esIndexCreateVo = new ESIndexCreateVo();
        esIndexCreateVo.setIndex("111");
        esIndexCreateVo.setMapping("111");
        String es = creatJwt("es", esIndexCreateVo);
        System.out.println(es);
        Object es1 = parseJWT("es", es);
        System.out.println(JSON.toJSONString(es1));

    }


    /**
     * 解析JWT
     * @param jwtStr
     * @return
     */
    public static Object parseJWT(String key,String jwtStr){
        Claims body = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(jwtStr)
                .getBody();
        Object o = body.get(key);
        return  o;
    }


    /**
     * 生成 jwt token
     * @param key  key值
     * @param object value值
     * @return
     */
    public static String creatJwt(String key,Object object) {
        return creatJwt(key,object,null);
    }

    /**
     * 生成 jwt token
     * @param key key值
     * @param object value值
     * @param tokenExpireTime 过期时间
     * @return
     */
    public static String creatJwt(String key,Object object, Long tokenExpireTime) {

        Date now = new Date();
        JwtBuilder jwtBuilder = Jwts.builder().claim(key, object)//jwtClaim为自定义对象，存放自定义参数（数据存放在Payload）
                .setIssuedAt(now)//生成时间
                .signWith(SignatureAlgorithm.HS256, secretKey);
        if (null != tokenExpireTime){
            Date date = new Date(now.getTime() + tokenExpireTime);//根据当前时间 + tokenExpireTime（token有效期，以毫秒为单位 例：7200000为2小时）
            jwtBuilder.setExpiration(date);//token到期时间
        }
        String compact = jwtBuilder.compact();
        return compact;

    }


}
