package com.manager.taskmanager.global.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
public class JwtTokenFilter extends OncePerRequestFilter {
    private final JwtTokenUtil jwtTokenUtil;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            log.error(">>> Access Token NOT Valid <<<");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);

            return;
        }

        String accessToken = authorizationHeader.substring(7);

        // 토큰이 없거나 만료되었으면 403
        if (jwtTokenUtil.isTokenExpired(accessToken)) {
            log.error(">>> Access Token 만료 <<<");

            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);

            return;
        }

        // 로그아웃되었으면 403
        if (isLogout(accessToken)) {
            log.error(">>> 로그아웃된 Access Token <<<");

            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);

            return;
        }

        JwtPayloadDto jwtPayloadDto = jwtTokenUtil.parseAccessToken(accessToken);

        Long id = jwtPayloadDto.getId();
        String employeeNumber = jwtPayloadDto.getEmployeeNumber();
        String role = jwtPayloadDto.getRole();

        if (!StringUtils.hasText(role)) {
            log.error(">>> 권한(role)이 없습니다. <<<");

            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);

            return;
        }

        CustomUserDetails customUserDetails = new CustomUserDetails(id, employeeNumber, role);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                customUserDetails,
                null,
                List.of(new SimpleGrantedAuthority(role))
        );

        SecurityContextHolder.clearContext();
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        log.info(">>> 사번 : {}, 권한 : {} <<<", employeeNumber, role);

        filterChain.doFilter(request, response);
    }

    private boolean isLogout(String accessToken) {
        String hashToken = jwtTokenUtil.tokenToHash(accessToken);

        return redisTemplate.opsForValue().get(hashToken) != null;
    }
}
