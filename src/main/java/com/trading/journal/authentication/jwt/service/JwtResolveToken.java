package com.trading.journal.authentication.jwt.service;

import org.springframework.http.server.reactive.ServerHttpRequest;

public interface JwtResolveToken {

    String resolve(ServerHttpRequest request);

}
