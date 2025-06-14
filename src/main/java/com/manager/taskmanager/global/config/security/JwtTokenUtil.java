package com.manager.taskmanager.global.config.security;

import com.manager.taskmanager.global.error.CustomException;
import com.manager.taskmanager.global.error.ErrorCode;
import com.manager.taskmanager.member.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenUtil {

     private final SecretKey secretKey;
     private final Long accessTokenExpiration;
     private final Long refreshTokenExpiration;

     public JwtTokenUtil(@Value("${jwt.secretKey}") String secretKey,
                         @Value("${jwt.access.expiration}") Long accessTokenExpiration,
                         @Value("${jwt.refresh.expiration}") Long refreshTokenExpiration) {
         this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secretKey));
         this.accessTokenExpiration = accessTokenExpiration;
         this.refreshTokenExpiration = refreshTokenExpiration;
     }

    // AccessToken 생성
    public String generateAccessToken(JwtPayloadDto jwtPayloadDto) {
        if (jwtPayloadDto.getId() == null) {
            log.error(">>> ID는 필수 입력 값 입니다. <<<");

            throw new CustomException(ErrorCode.MISSING_REQUIRED_FIELD);
        }

        if (!StringUtils.hasText(jwtPayloadDto.getEmployeeNumber())) {
            log.error(">>> 사번은 필수 입력 값 입니다. <<<");

            throw new CustomException(ErrorCode.MISSING_REQUIRED_FIELD);
        }

        if (!StringUtils.hasText(jwtPayloadDto.getDepartment())) {
            log.error(">>> 부서는 필수 입력 값 입니다. <<<");

            throw new CustomException(ErrorCode.MISSING_REQUIRED_FIELD);
        }

        Role role = Role.from(jwtPayloadDto.getRole());

        if (role == null) {
            log.error(">>> 권한은 필수 입력 값 입니다. <<<");

            throw new CustomException(ErrorCode.MISSING_REQUIRED_FIELD);
        }

        if (jwtPayloadDto.getPosition() == null) {
            log.error(">>> 직급은 필수 입력 값 입니다. <<<");

            throw new CustomException(ErrorCode.MISSING_REQUIRED_FIELD);
        }

        return Jwts.builder()
                .setSubject(jwtPayloadDto.getEmployeeNumber())
                .claim("department", jwtPayloadDto.getDepartment())
                .claim("memberId", jwtPayloadDto.getId())
                .claim("role", role.getValue())
                .claim("position", jwtPayloadDto.getPosition().name())
                .claim("positionKorean", jwtPayloadDto.getPosition().getKorean())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .setIssuedAt(new Date())
                .signWith(secretKey)
                .compact();
    }

    // RefreshToken 생성
    public String generateRefreshToken(JwtPayloadDto jwtPayloadDto) {
        if (!StringUtils.hasText(jwtPayloadDto.getEmployeeNumber())) {
            log.error(">>> 사번은 필수 입력 값 입니다. <<<");

            throw new CustomException(ErrorCode.MISSING_REQUIRED_FIELD);
        }

        return Jwts.builder()
                .setSubject(jwtPayloadDto.getEmployeeNumber())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .setIssuedAt(new Date())
                .signWith(secretKey)
                .compact();
    }

    // Token 해쉬 처리
    public String tokenToHash(String accessToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = digest.digest(accessToken.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error(">>> AccessToken 해시 처리 중 오류가 발생하였습니다. <<<");

            return null;
        }
    }

    // 토큰 만료일 조회
    public Date getTokenExpiration(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
        } catch (ExpiredJwtException e) {
            log.error(">>> 토큰이 만료되었습니다. <<<");

            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        } catch (Exception e) {
            log.error(">>> 토큰이 유효하지 않습니다. <<<");

            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    // 토큰 만료여부 체크 / 만료 : true
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();

            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    // Refresh Token 만료 시간
    public Long getRefreshTokenExpiration() {
         return refreshTokenExpiration;
    }

    // Access Token 토큰 정보
    public JwtPayloadDto parseAccessToken(String accessToken) {
        Claims body = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(accessToken)
                .getBody();

        String employeeNumber = body.getSubject();
        Long memberId = ((Integer) body.get("memberId")).longValue();
        String role = (String) body.get("role");
        String department = (String) body.get("department");

        JwtPayloadDto jwtPayloadDto = new JwtPayloadDto();
        jwtPayloadDto.setId(memberId);
        jwtPayloadDto.setRole(role);
        jwtPayloadDto.setEmployeeNumber(employeeNumber);
        jwtPayloadDto.setDepartment(department);

        return jwtPayloadDto;
    }

    // Refresh Token 정보
    public String parseRefreshToken(String refreshToken) {
        Claims body = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();

        return body.getSubject();
    }
}
