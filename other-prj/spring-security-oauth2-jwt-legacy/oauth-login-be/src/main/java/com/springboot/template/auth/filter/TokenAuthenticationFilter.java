package com.springboot.template.auth.filter;

import com.springboot.template.auth.token.AuthToken;
import com.springboot.template.auth.token.AuthTokenProvider;
import com.springboot.template.utils.HeaderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 이 코드는 Spring Security에서 JWT 토큰을 검증하고 인증을 수행하는 필터인 `TokenAuthenticationFilter` 클래스입니다. <br>
 * `OncePerRequestFilter`를 상속받아 Spring Security의 `FilterChain`을 구현하고 있습니다. `TokenAuthenticationFilter`는 클라이언트의 모든 요청마다 실행됩니다. <br>
 * `AuthTokenProvider`를 생성자 주입(Dependency Injection) 받아, 요청에서 전달된 JWT 토큰을 검증하고 인증정보를 SecurityContext에 저장합니다. <br>
 * `HeaderUtil.getAccessToken(request)`를 사용하여 HTTP 요청 헤더에서 JWT 토큰 값을 가져옵니다. <br>
 * `tokenProvider.convertAuthToken(tokenStr)`을 호출하여 JWT 토큰 문자열을 `AuthToken` 객체로 변환합니다. <br>
 * `token.validate()`를 호출하여 JWT 토큰의 유효성을 확인합니다. <br>
 * `tokenProvider.getAuthentication(token)`을 호출하여 JWT 토큰에서 가져온 정보를 사용하여 `Authentication` 객체를 생성합니다. <br>
 * `SecurityContextHolder.getContext().setAuthentication(authentication)`을 호출하여 인증된 *Authentication` 객체를 Spring Security의 SecurityContext에 저장합니다. <br>
 * 마지막으로, `filterChain.doFilter(request, response)`를 호출하여 요청을 다음 필터로 전달합니다. <br>
 */

@Slf4j
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private final AuthTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)  throws ServletException, IOException {

        String tokenStr = HeaderUtil.getAccessToken(request);
        AuthToken token = tokenProvider.convertAuthToken(tokenStr);

        if (token.validate()) {
            Authentication authentication = tokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
