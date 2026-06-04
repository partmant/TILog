package com.tilog.global.config;

import com.tilog.domain.member.entity.MemberRole;
import com.tilog.global.exception.CustomException;
import com.tilog.global.exception.ErrorCode;
import com.tilog.global.security.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null) {
            if (!jwtTokenProvider.validate(token)) {
                throw new CustomException(ErrorCode.UNAUTHORIZED);
            }

            Long memberId = jwtTokenProvider.getMemberId(token);
            MemberRole role = jwtTokenProvider.getRole(token);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            String.valueOf(memberId),
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");

        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }

        return null;
    }
}
