package com.example.chapter6.jwt;

import com.example.chapter6.exception.TokenRefreshException;
import com.example.chapter6.mapper.RefreshTokenMapper;
import com.example.chapter6.model.RefreshTokenVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class RefreshTokenService {

    private RefreshTokenMapper refreshTokenMapper;

    @Value("${app.jwt.token.refresh.duration}")
    private Long refreshTokenDurationMs;

    public RefreshTokenService(RefreshTokenMapper refreshTokenMapper) {
        this.refreshTokenMapper = refreshTokenMapper;
    }

    public Optional<RefreshTokenVO> selectByToken(String token) {
        return refreshTokenMapper.selectByToken(token);
    }

    public RefreshTokenVO insertToken(int id) {
        RefreshTokenVO refreshTokenVO = new RefreshTokenVO();
        refreshTokenVO.setToken(UUID.randomUUID().toString());
        refreshTokenVO.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshTokenVO.setRefreshCount(0);
        refreshTokenVO.setUserId(id);
        refreshTokenMapper.insertRefreshToken(refreshTokenVO);
        return refreshTokenVO;
    }

    public void verifyExpiration(RefreshTokenVO refreshTokenVO) throws TokenRefreshException {
        if (refreshTokenVO.getExpiryDate().compareTo(Instant.now()) < 0) {
            throw new TokenRefreshException(refreshTokenVO.getToken(), "토큰이 만료되었습니다.");
        }
    }

    public void deleteTokenById(int id) {
        refreshTokenMapper.deleteTokenById(id);
    }

    // refresh_token 갱신
    public RefreshTokenVO updateTokenCount(int id) {
        RefreshTokenVO refreshTokenVO = new RefreshTokenVO();
        refreshTokenVO.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshTokenVO.setToken(UUID.randomUUID().toString());
        refreshTokenVO.setUserId(id);
        refreshTokenMapper.updateTokenCount(refreshTokenVO);
        return refreshTokenVO;
    }

    // userid존재 유무
    public boolean existMemberId(int id) {
        return refreshTokenMapper.existMemberId(id);
    }

}
