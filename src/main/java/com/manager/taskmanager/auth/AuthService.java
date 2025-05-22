package com.manager.taskmanager.auth;

import com.manager.taskmanager.auth.dto.LoginRequestDto;
import com.manager.taskmanager.auth.dto.TokenDto;
import com.manager.taskmanager.common.CustomException;
import com.manager.taskmanager.common.ErrorCode;
import com.manager.taskmanager.config.security.JwtPayloadDto;
import com.manager.taskmanager.config.security.JwtTokenUtil;
import com.manager.taskmanager.member.MemberRepository;
import com.manager.taskmanager.member.entity.Member;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final RedisTemplate<String, String> redisTemplate;

    // 로그인
    public TokenDto login(LoginRequestDto dto) {
        Member member = memberRepository.findByEmployeeNumberAndDeletedAtIsNull(dto.getEmployeeNumber())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        JwtPayloadDto jwtPayloadDto = new JwtPayloadDto(
                member.getId(), member.getDepartment().getDepartmentName(),
                member.getEmployeeNumber(), member.getRole().getValue(), member.getPosition()
        );

        String accessToken = jwtTokenUtil.generateAccessToken(jwtPayloadDto);
        String refreshToken = jwtTokenUtil.generateRefreshToken(jwtPayloadDto);

        String redisKey = "refresh:" + member.getId();

        redisTemplate.opsForValue().set(redisKey, refreshToken, jwtTokenUtil.getRefreshTokenExpiration(), TimeUnit.MILLISECONDS);

        return new TokenDto(accessToken, refreshToken);
    }

    // 로그아웃
    public void logout(String accessToken) {
        Date tokenExpiration = jwtTokenUtil.getTokenExpiration(accessToken);

        long expiration = tokenExpiration.getTime() - System.currentTimeMillis();
        String hashToken = jwtTokenUtil.tokenToHash(accessToken);

        redisTemplate.opsForValue().set(hashToken, "logout", expiration, TimeUnit.MILLISECONDS);
    }

    // 토큰 재발급
    public TokenDto reIssueToken(String refreshToken) {
        if (refreshToken == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        String employeeNumber = "";

        try {
            employeeNumber = jwtTokenUtil.parseRefreshToken(refreshToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        Member member = memberRepository.findByEmployeeNumberAndDeletedAtIsNull(employeeNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        String storedRefreshToken = redisTemplate.opsForValue().get("refresh:" + member.getId());

        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        JwtPayloadDto jwtPayloadDto = new JwtPayloadDto(
                member.getId(), member.getDepartment().getDepartmentName(),
                member.getEmployeeNumber(), member.getRole().getValue(), member.getPosition()
        );

        String accessToken = jwtTokenUtil.generateAccessToken(jwtPayloadDto);

        return new TokenDto(accessToken, null);
    }
}
