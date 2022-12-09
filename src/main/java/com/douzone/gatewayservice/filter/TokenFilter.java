package com.douzone.gatewayservice.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
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


@Slf4j
@Component
public class TokenFilter extends AbstractGatewayFilterFactory<TokenFilter.Config> {

    @Autowired
    private final Environment getToken;
    static class Config{ }

    public TokenFilter(Environment getToken) {
        super(Config.class);
        this.getToken = getToken;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {

            ServerHttpRequest request = exchange.getRequest();
            if(!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "no authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authorizationHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            String jwt = authorizationHeader.replace("Bearer ", "").trim();

            if(!isJwtValid(jwt)){
                return onError(exchange, "JWT token is not Valid", HttpStatus.UNAUTHORIZED);
            }
            return chain.filter(exchange);
        });

    }

    private boolean isJwtValid(String jwt) {
//        Key secretKey = Keys.hmacShaKeyFor(getToken.getProperty("jwt.secret").getBytes(StandardCharsets.UTF_8));
        boolean isValue = true;
        String subject = null;
        try {
//            subject = Jwts.parserBuilder()
//                    .setSigningKey(secretKey)
//                    .build()
//                    .parseClaimsJws(jwt)
//                    .getBody()
//                    .getSubject();
            Algorithm algorithm = Algorithm.HMAC256(getToken.getProperty("jwt.secret").getBytes(StandardCharsets.UTF_8)); // 토큰 생성할 때와 같은 알고리즘으로 풀어야함.
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT decodedJWT = verifier.verify(jwt);
            subject = decodedJWT.getSubject();
        } catch (Exception ex){
            isValue = false;
        }
        if(subject == null || subject.isEmpty()){
            isValue = false;
        }
        return isValue;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String error, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);

        log.error(error);
        return response.setComplete();
    }
}
