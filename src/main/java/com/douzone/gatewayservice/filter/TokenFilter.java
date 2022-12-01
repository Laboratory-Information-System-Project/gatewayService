package com.douzone.gatewayservice.filter;

import com.douzone.gatewayservice.security.JwtTokenProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;


@Slf4j
@Component
public class TokenFilter extends AbstractGatewayFilterFactory<TokenFilter.Config> {

    @Autowired
//    private final Environment getToken;
    private  final JwtTokenProvider jwtTokenProvider;
    static class Config{ }

//    public TokenFilter(Environment getToken) {
    public TokenFilter(JwtTokenProvider jwtTokenProvider) {
        super(TokenFilter.Config.class);
//        this.getToken = getToken;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {

            ServerHttpRequest request = exchange.getRequest();

            if(!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "no authorization header", HttpStatus.UNAUTHORIZED);
            }
            String authorizationHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            String jwt = authorizationHeader.replace("Bearer ", "");

            jwtTokenProvider.validateJwtToken(jwt);
            String subject = jwtTokenProvider.getUserId(jwt);

//            if (subject.equals("feign")) return chain.filter(exchange);
//            if (false == jwtTokenProvider.getAuth(jwt).contains("doctor")) {
//                return onError(exchange, "권한 없음", HttpStatus.UNAUTHORIZED);
//            }
            ServerHttpRequest newRequest = request.mutate()
                    .header("user-id", subject)
                    .build();


            if(!isJwtValid(jwt)){
                return onError(exchange, "JWT token is not Valid", HttpStatus.UNAUTHORIZED);
            }
            return chain.filter(exchange.mutate().request(newRequest).build());
        });

    }

    private Mono<Void> onError(ServerWebExchange exchange, String error, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);

        log.error(error);
        return response.setComplete();
    }
        private boolean isJwtValid(String jwt) {
            Key secretKey = Keys.hmacShaKeyFor(jwtTokenProvider.getAuth("access-token"));
//            Key secretKey = Keys.hmacShaKeyFor(getToken.getProperty("token.secret").getBytes(StandardCharsets.UTF_8));
            boolean returnValue = true;
            String subject = null;
        try {
            subject = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody()
                    .getSubject();
        } catch (Exception ex){
            returnValue = false;
        }
        if(subject == null || subject.isEmpty()){
            returnValue = false;
        }
        return returnValue;
    }

}
