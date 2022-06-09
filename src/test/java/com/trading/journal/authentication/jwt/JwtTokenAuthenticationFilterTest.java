package com.trading.journal.authentication.jwt;

import com.trading.journal.authentication.jwt.helper.JwtConstants;
import com.trading.journal.authentication.jwt.service.JwtTokenReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.request.NativeWebRequest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class JwtTokenAuthenticationFilterTest {

    @Mock
    JwtTokenReader tokenReader;

    JwtTokenAuthenticationFilter jwtTokenAuthenticationFilter;

    @BeforeEach
    public void setUp() {
        jwtTokenAuthenticationFilter = new JwtTokenAuthenticationFilter(tokenReader);
    }

    @Test
    @DisplayName("Given server request with token process request successfully")
    void serverRequestSuccess() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(JwtConstants.TOKEN_PREFIX.concat("123456789"));
        MockFilterChain chain = new MockFilterChain();

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer 123456789");
        when(tokenReader.isTokenValid("123456789")).thenReturn(true);
        when(tokenReader.getAuthentication("123456789")).thenReturn(new UsernamePasswordAuthenticationToken("user", null, emptyList()));

        jwtTokenAuthenticationFilter.doFilterInternal(request, response, chain);
    }

    @Test
    @DisplayName("Given server request without token process request successfully")
    void serverRequestSuccessWithoutToken() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        MockFilterChain chain = new MockFilterChain();

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        jwtTokenAuthenticationFilter.doFilterInternal(request, response, chain);

        verify(tokenReader, never()).isTokenValid(anyString());
        verify(tokenReader, never()).getAuthentication(anyString());
    }
}
