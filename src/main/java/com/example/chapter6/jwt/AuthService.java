package com.example.chapter6.jwt;

import com.example.chapter6.exception.UserNotFoundException;
import com.example.chapter6.model.RefreshTokenVO;
import com.example.chapter6.payload.response.JwtAuthenticationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private RefreshTokenService refreshTokenService;

    public AuthService(JwtTokenProvider jwtTokenProvider){
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // 토큰 생성
    public String generateToken(String userId){
        return jwtTokenProvider.generateToken(userId);
    }

    //토큰으로 아이디 반환환
   public String getUserIdFromJWT(String token){
        return jwtTokenProvider.getUserIdFromJWT(token);
    }

    // 토큰으로 만료일 반환
    public Date getTokenExpiryFromJWT(String token){
        return jwtTokenProvider.getTokenExpiryFromJWT(token);
    }

    // 리프레시 토큰 갱신
    public JwtAuthenticationResponse regenToken(String refreshTokenRequest) {
        /*
        * 1. refresh_token이 db의 row존재 여부
        * 2. row가 있을 경우, 토큰의 만료시간 검증
        * 3. 만료되었으면 오류 발생 후 재로그인 유도
        * 4. 만료가 안되었으면 refresh_token재발급
        * */
        Optional<RefreshTokenVO> refreshTokenExistCheck = refreshTokenService.selectByToken(refreshTokenRequest);

        if(refreshTokenExistCheck.isPresent()){
            refreshTokenService.verifyExpiration(refreshTokenExistCheck.get());

            RefreshTokenVO generateTokens = refreshTokenService.updateTokenCount(refreshTokenExistCheck.get().getMemberId());

            String accessToken = generateToken(refreshTokenExistCheck.get().getUserNm());
            JwtAuthenticationResponse response = new JwtAuthenticationResponse();
            response.setExpiryDuration(generateTokens.getExpiryDate().toEpochMilli());
            response.setAccessToken(accessToken);
            response.setRefreshToken(generateTokens.getRefreshToken());
            return response;
        }else {
            throw new UserNotFoundException("refresh_token 정보가 없습니다.");
        }
    }
}
