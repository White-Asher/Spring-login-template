package com.springboot.template.config.sercurity;

import com.springboot.template.config.properties.TokenProperties;
import com.springboot.template.config.properties.CorsProperties;
import com.springboot.template.auth.exception.RestAuthenticationEntryPoint;
import com.springboot.template.auth.filter.TokenAuthenticationFilter;
import com.springboot.template.auth.handler.OAuth2AuthenticationFailureHandler;
import com.springboot.template.auth.handler.OAuth2AuthenticationSuccessHandler;
import com.springboot.template.auth.handler.TokenAccessDeniedHandler;
import com.springboot.template.auth.repository.OAuth2AuthorizationRequestBasedOnCookieRepository;
import com.springboot.template.auth.service.CustomOAuth2UserService;
import com.springboot.template.auth.token.AuthTokenProvider;
import com.springboot.template.auth.handler.CustomLogoutSuccessHandler;
import com.springboot.template.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Slf4j
@Configuration
@EnableWebSecurity(debug = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsProperties corsProperties;
    private final TokenProperties tokenProperties;
    private final AuthTokenProvider tokenProvider;
    private final CustomOAuth2UserService oAuth2UserService;
    private final TokenAccessDeniedHandler tokenAccessDeniedHandler;
    private final RedisUtil redisUtil;
    private final CustomLogoutSuccessHandler logoutSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                // 스프링 시큐리티 세션 정책
                .sessionManagement()
                // 스프링 시큐리티가 생성하지 않고 기존것을 사용하지 않음 (JWT 토큰 방식 사용시 설정)
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // CSRF, Form Login, Http Basic 비활성화 (JWT 를 사용하므로 생략함)
                .csrf().disable()
                .formLogin().disable()
                .httpBasic().disable()
                .authorizeRequests()
                .antMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**", "/proxy/**", "/api/auth/login", "**/swagger-ui/**").permitAll()
                .anyRequest().authenticated()
                .and()
                // 지정된 필터 앞에 커스텀 필터를 추가 (UsernamePasswordAuthenticationFilter 보다 먼저 실행된다)
                .addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling()
                // 예외 터지면 RestAuthenticationEntryPoint 에서 받아서 처리함. 
                .authenticationEntryPoint(new RestAuthenticationEntryPoint())
                // 접근 권한이 거부되면 실행되는 핸들러
                .accessDeniedHandler(tokenAccessDeniedHandler)
//                .and()
                // 요청에 의한 보안검사 시작
//                .authorizeRequests()
////                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
////                .antMatchers("/api/**").hasAnyAuthority(RoleType.USER.getCode())
//                .antMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**", "/proxy/**", "/api/auth/login", "**/swagger-ui/**").permitAll()
////                .antMatchers("/**").permitAll()
//                .anyRequest().authenticated()
//                .antMatchers("/api/**/admin/**").hasAnyAuthority(RoleType.ADMIN.getCode())
                .and()
                .oauth2Login()
                .authorizationEndpoint()
                .baseUri("/oauth2/authorization")
                .authorizationRequestRepository(oAuth2AuthorizationRequestBasedOnCookieRepository())
                .and()
                .redirectionEndpoint()
                .baseUri("/*/oauth2/code/*")
                .and()
                .userInfoEndpoint()
                .userService(oAuth2UserService)
                .and()
                .successHandler(oAuth2AuthenticationSuccessHandler())
                .failureHandler(oAuth2AuthenticationFailureHandler())
                .and()
                .logout()
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler(logoutSuccessHandler)
                .logoutSuccessUrl("/");

        return http.build();
    }

//    @Bean
//    public WebSecurityCustomizer webSecurityCustomizer(){
//        return web -> {
//            web.ignoring()
//                    .antMatchers(
//                            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**", "/swagger-ui/index.html"
//                            );
//        };
//    }

    //    @Bean
//    public WebMvcConfigurer corsConfigurer() {
//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry registry) {
//                registry.addMapping("/**")
//                        .allowedMethods("*");
//            }
//        };
//    }
    /*
     * auth 매니저 설정
     * */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration auth) throws Exception {
        log.info("authenticationManager 메서드 호출됨");
        return auth.getAuthenticationManager();
    }

    /*
     * security 설정 시, 사용할 인코더 설정
     * */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
     * 토큰 필터 설정
     * */
    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider, redisUtil, tokenProperties);
    }

    /*
     * 쿠키 기반 인가 Repository
     * 인가 응답을 연계 하고 검증할 때 사용.
     * */
    @Bean
    public OAuth2AuthorizationRequestBasedOnCookieRepository oAuth2AuthorizationRequestBasedOnCookieRepository() {
        return new OAuth2AuthorizationRequestBasedOnCookieRepository();
    }

    /*
     * Oauth 인증 성공 핸들러
     * */
    @Bean
    public OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler() {
        return new OAuth2AuthenticationSuccessHandler(
                tokenProvider,
                tokenProperties,
                oAuth2AuthorizationRequestBasedOnCookieRepository(),
                redisUtil
        );
    }

    /*
     * Oauth 인증 실패 핸들러
     * */
    @Bean
    public OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler() {
        return new OAuth2AuthenticationFailureHandler(oAuth2AuthorizationRequestBasedOnCookieRepository());
    }

    /*
     * Cors 설정
     * */
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource corsConfigSource = new UrlBasedCorsConfigurationSource();

        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedHeaders(Arrays.asList(corsProperties.getAllowedHeaders().split(",")));
        corsConfig.setAllowedMethods(Arrays.asList(corsProperties.getAllowedMethods().split(",")));
        corsConfig.setAllowedOrigins(Arrays.asList(corsProperties.getAllowedOrigins().split(",")));
        corsConfig.setAllowCredentials(true);
        corsConfig.setMaxAge(corsConfig.getMaxAge());
        corsConfig.addExposedHeader("Authorization");
        corsConfigSource.registerCorsConfiguration("/**", corsConfig);
        return corsConfigSource;
    }

}
