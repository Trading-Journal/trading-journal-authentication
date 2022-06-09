package com.trading.journal.authentication.jwt.service;

import javax.servlet.http.HttpServletRequest;

public interface JwtResolveToken {

    String resolve(HttpServletRequest request);

}
