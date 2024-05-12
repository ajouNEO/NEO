package com.neo.back.springjwt.config;


import com.neo.back.springjwt.jwt.CustomLogoutFilter;
import com.neo.back.springjwt.jwt.JWTFilter;
import com.neo.back.springjwt.jwt.JWTUtil;
import com.neo.back.springjwt.jwt.LoginFilter;
import com.neo.back.springjwt.oauth2.CustomSuccessHandler;
import com.neo.back.springjwt.repository.RefreshRepository;
import com.neo.back.springjwt.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final JWTUtil jwtUtil;

    private final RefreshRepository refreshRepository;
    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration, CustomOAuth2UserService customOAuth2UserService, CustomSuccessHandler customSuccessHandler, JWTUtil jwtUtil, RefreshRepository refreshRepository) {
        this.authenticationConfiguration=authenticationConfiguration;
        this.customOAuth2UserService = customOAuth2UserService;
        this.customSuccessHandler = customSuccessHandler;
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;

    }

    //AuthenticationManager Bean 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception{
        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){

        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{

        //CORS(교차 검증 해결)
        http.
                cors((cors) -> cors
                        .configurationSource(new CorsConfigurationSource() {
                            @Override
                            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                                CorsConfiguration configuration = new CorsConfiguration();

                                //프론트단 port 허용
                                configuration.setAllowedOrigins(Collections.singletonList("*"));
                                //허용할 메소드 GET,POST,DELETE 등 모두 허용
                                configuration.setAllowedMethods(Collections.singletonList("*"));
                                //프론트에서 credential 허용했으면 똑같이 true
                                //configuration.setAllowCredentials(true);
                                //헤더 허용
                                configuration.setAllowedHeaders(Collections.singletonList("*"));
                                configuration.setMaxAge(3600L);
                                //Authorization 헤더에 jwt 토큰 보낼거기 때문.
                                configuration.setExposedHeaders(Collections.singletonList("Authorization"));

                                return configuration;

                            }
                        }));

        //csrf disable
        http
                .csrf((auth)->auth.disable());

        //form 로그인 방식 disable
        http
                .formLogin((auth)->auth.disable());

        //http basic 인증 방식 disable
        http.
                httpBasic((auth) -> auth.disable());

        //http 로그아웃 방식 disable

        http
                .logout((auth)->auth.disable());

        //JWTFilter 등록
        http
                .addFilterAfter(new JWTFilter(jwtUtil), OAuth2LoginAuthenticationFilter.class);

        //oauth2
       http
                .oauth2Login((oauth2) -> oauth2
                        .userInfoEndpoint((userInfoEndpointConfig) -> userInfoEndpointConfig
                                .userService(customOAuth2UserService))
                                .successHandler(customSuccessHandler));




        //경로별 인가 작업
         http.
              authorizeHttpRequests((auth)-> auth
               .requestMatchers("/static/**", "/public/**", "/resources/**", "/META-INF/resources/**").permitAll()
                      .requestMatchers("/login","/","/api/join").permitAll()
                //       .requestMatchers("/api/**").authenticated()
               .requestMatchers("/api/admin").hasRole("ADMIN")
               .requestMatchers("/reissue").permitAll()
        //        .anyRequest().authenticated());
               .anyRequest().permitAll());


        //필터 추가 LoginFilter()는 인자를 받음 (AuthenticationManager() 메소드에 authenticationConfiguration 객체를 넣어야 함) 따라서 등록 필요
        //addfilterAt 원하는 자리에 등록, usernameauthentication 대체하는거이기에 at
        http
                .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, refreshRepository), UsernamePasswordAuthenticationFilter.class);

        http
                .addFilterBefore(new CustomLogoutFilter(jwtUtil, refreshRepository), LogoutFilter.class);

        //세션 설정 stateless로 설정
        http
                .sessionManagement((session)->session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        //세션 설정 중복 로그인 방지.
        http
                .sessionManagement((auth)-> auth
                        .maximumSessions(1) //하나의 아이디에 대한 다중 로그인 허용 개수
                        .maxSessionsPreventsLogin(false)); // 다중 로그인 개수를 초과하였을 경우 처리 방법 초과시 새로운 로그인 차단 true,기존 세션 하나 삭제 false

        // 세션 고정 보호
        http.sessionManagement((auth)-> auth
                .sessionFixation().changeSessionId());



        return http.build();
    }

}
