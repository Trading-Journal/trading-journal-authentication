package com.trading.journal.authentication.jwt.service.impl;

import com.trading.journal.authentication.jwt.data.AccessToken;
import com.trading.journal.authentication.jwt.service.JwtResolveToken;
import com.trading.journal.authentication.jwt.service.JwtTokenReader;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

public class AccessTokenResolver implements HandlerMethodArgumentResolver {

    private final JwtTokenReader tokenReader;
    private final JwtResolveToken resolveToken;

    public AccessTokenResolver(JwtTokenReader tokenReader) {
        this.tokenReader = tokenReader;
        this.resolveToken = new JwtResolveTokenHttpHeader();
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(AccessToken.class) != null;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        String token = resolveToken.resolve((HttpServletRequest) webRequest.getNativeResponse());
        return tokenReader.getAccessTokenInfo(token);
    }
}
