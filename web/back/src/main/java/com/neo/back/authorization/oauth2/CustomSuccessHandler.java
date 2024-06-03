package com.neo.back.authorization.oauth2;

import com.neo.back.config.EnvConfig;
import com.neo.back.authorization.dto.CustomOAuth2User;
import com.neo.back.authorization.entity.RefreshEntity;
import com.neo.back.authorization.jwt.JWTUtil;
import com.neo.back.authorization.repository.RefreshRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private EnvConfig envConfig;

    private final JWTUtil jwtUtil;

    private final RefreshRepository refreshRepository;

    public CustomSuccessHandler(JWTUtil jwtUtil, RefreshRepository refreshRepository) {
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException{

        //OAuth2User
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        String username = customUserDetails.getUserName();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        String access = jwtUtil.createJwt("access",username, role, 600000L);
        String refresh = jwtUtil.createJwt("refresh",username, role, 8400000L);

        RefreshEntity refreshEntity = new RefreshEntity();
        refreshEntity.setRefresh(refresh);
        refreshEntity.setUsername(username);
        refreshEntity.setExpiration(8400000L);


        refreshRepository.save(refreshEntity);


        response.addCookie(createCookie("access", access));
        response.addCookie(createCookie("refresh", refresh));


        System.out.println(envConfig.getMainServerIp());
        response.sendRedirect("https://neo.framer.media");
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60*60*60);
        //cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(false);

        return cookie;
    }


}
