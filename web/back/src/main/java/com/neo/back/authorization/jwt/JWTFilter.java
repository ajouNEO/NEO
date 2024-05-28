package com.neo.back.authorization.jwt;

import com.neo.back.authorization.dto.CustomUserDetails;
import com.neo.back.authorization.entity.User;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil){
        this.jwtUtil = jwtUtil;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //request에서 Authorization 헤더를 찾음
        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isEmpty()) {
            authorization = request.getParameter("token");
        }

        //Authorization 헤더 검증
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            System.out.println("token null");
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println("Authorization now");
        //Beare 부분 제거 후 순수 토큰만 획득
        String access = authorization.split(" ")[1];

        //토큰 소멸 시간 검증
        try {jwtUtil.isExpired(access);
        }  catch (ExpiredJwtException e){

            //response body
            PrintWriter writer = response.getWriter();
            writer.print("access token expired");

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

          //  filterChain.doFilter(request,response);

            return;
        }

        String category = jwtUtil.getCategory(access);

        if (!category.equals("access")) {

            //response body
            PrintWriter writer = response.getWriter();
            writer.print("invalid access token");

            //response status code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String username = jwtUtil.getUsername(access);
        String role = jwtUtil.getRole(access);

        //UserEntity를 생성하여 값 set
        User user = new User();
        user.setUsername(username);
        //임시 비밀번호, 검증할때마다 db에 요청 x
        user.setPassword("temppassword");
        user.setRole(role);

        //UserDetails에 회원 정보 객체 담기
        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        //스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails,null,customUserDetails.getAuthorities());
        //세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request,response);


    }


}
