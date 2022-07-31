package com.trading.journal.authentication.jwt.service.impl;

import com.trading.journal.authentication.jwt.data.AccessTokenInfo;
import com.trading.journal.authentication.jwt.service.JwtResolveToken;
import com.trading.journal.authentication.jwt.service.JwtTokenReader;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

@RequiredArgsConstructor
@Component
public class AccessTokenResolver implements HandlerMethodArgumentResolver {

    private final JwtTokenReader tokenReader;
    private final JwtResolveToken resolveToken;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(AccessTokenInfo.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        String token = resolveToken.resolve(request);
        return tokenReader.getAccessTokenInfo(token);
    }
}
