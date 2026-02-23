package pard.server.com.longkathon.config.jwt.token;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pard.server.com.longkathon.MyPage.user.User;
import pard.server.com.longkathon.MyPage.user.UserRepo;
import pard.server.com.longkathon.MyPage.user.UserService;
import pard.server.com.longkathon.config.jwt.TokenProvider;
import pard.server.com.longkathon.config.jwt.refreshToken.RefreshToken;
import pard.server.com.longkathon.config.jwt.refreshToken.RefreshTokenRepository;
import pard.server.com.longkathon.config.jwt.refreshToken.RefreshTokenService;


import java.time.Duration;

@RequiredArgsConstructor
@Service
//리프레시 토큰을 전달받아 토큰 유효성 검사를 진행하고, 유효한 토큰일 때 새로운 AccessToken을 생성
public class TokenService {
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepo userRepository;

    //리프레시 토큰을 전달받음
    public CreateAccessTokenResponse createNewAccessToken(String refreshToken) {
        // 1. RefreshToken 검증 (DB에 존재하는지, 만료되지 않았는지)
        RefreshToken storedToken = refreshTokenRepository.findByRefreshToken(refreshToken)
                 .orElseThrow(() -> new IllegalArgumentException("Invalid RefreshToken: DB에 존재하지 않음"));
        // 2. RefreshToken이 만료되었는지 확인
        if (storedToken.isExpired()) {
            throw new IllegalArgumentException("RefreshToken expired: 기간만료");
        }

        // 3. userId로 User 조회
        User user = userRepository.findById(storedToken.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 4. JWT RefreshToken 검증 (유효한 토큰인지)
        if (!tokenProvider.validToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid JWT RefreshToken");
        }
        // 5. AccessToken 생성
        String accessToken = tokenProvider.generateToken(user, Duration.ofMinutes(15));

        // 6. isProfileCompleted 정보와 함께 반환
        return new CreateAccessTokenResponse(accessToken, user.isProfileCompleted());
    }
}