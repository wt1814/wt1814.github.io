package com.wuw.gateway.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 在成功拦截器设置了token头信息 那么在前端访问的都会带有token,所以需要配置过滤器
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class TokenGlobalFilter implements GlobalFilter {

    @Autowired(required = false)
    private RedisTemplate redisTemplate;
    // todo
/*    @Autowired
    private UserDetailServiceImpl userDetailService;*/
    private static final String AUTHORIZE_TOKEN = "token";

    @Override
    //执行过滤器逻辑
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        System.out.println("执行过滤器逻辑");
        String token = exchange.getRequest().getHeaders().getFirst(AUTHORIZE_TOKEN);
        System.out.println(token);
        if (!StringUtils.isEmpty(token)) {//判断token是否为空
            String username = (String) redisTemplate.boundValueOps(token).get();
            System.out.println(username);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {//判断Security的用户认证信息
                // todo
                //Mono<UserDetails> byUsername = userDetailService.findByUsername(username);
                Mono<Object> byUsername = null;
                // 将用户信息存入 authentication，方便后续校验 todo
                //UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(byUsername.block(), null, byUsername.block().getAuthorities());
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(byUsername.block(), null, null);
                authentication.setDetails(byUsername.block());
                // 将 authentication 存入 ThreadLocal，方便后续获取用户信息
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        //放行
        return chain.filter(exchange);
    }

}
