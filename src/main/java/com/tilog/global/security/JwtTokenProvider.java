package com.tilog.global.security;

import com.tilog.domain.member.entity.MemberRole;
import com.tilog.global.exception.AuthException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT 토큰 발급 및 검증을 담당
 * - access token: 짧은 만료 시간
 * - refresh token: 긴 만료 시간 (재발급용)
 */
@Component
public class JwtTokenProvider {

    private static final String CLAIM_ROLE = "role";

    private final SecretKey secretKey;
    private final long accessTokenValidityMillis;
    private final long refreshTokenValidityMillis;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-seconds}") long accessTokenValiditySeconds,
            @Value("${jwt.refresh-token-validity-seconds}") long refreshTokenValiditySeconds
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidityMillis = accessTokenValiditySeconds * 1000;
        this.refreshTokenValidityMillis = refreshTokenValiditySeconds * 1000;
    }

    public String createAccessToken(Long memberId, MemberRole role) {
        return createToken(memberId, role, accessTokenValidityMillis);
    }

    public String createRefreshToken(Long memberId, MemberRole role) {
        return createToken(memberId, role, refreshTokenValidityMillis);
    }

    private String createToken(Long memberId, MemberRole role, long validityMillis) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityMillis);

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim(CLAIM_ROLE, role.name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public Long getMemberId(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    public MemberRole getRole(String token) {
        String role = parseClaims(token).get(CLAIM_ROLE, String.class);
        return MemberRole.valueOf(role);
    }

    public long getAccessTokenValiditySeconds() {
        return accessTokenValidityMillis / 1000;
    }

    /**
     * 토큰 유효성 검증
     * - 만료된 경우 AuthException
     * - 그 외 위변조/형식 오류는 false 반환 (호출 측에서 처리)
     */
    public boolean validate(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new AuthException("만료된 토큰입니다.");
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
