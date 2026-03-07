package pard.server.com.longkathon.config.jwt.token;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pard.server.com.longkathon.MyPage.user.User;
import pard.server.com.longkathon.MyPage.user.UserRepo;
import pard.server.com.longkathon.MyPage.user.UserService;
import pard.server.com.longkathon.MyPage.userFile.UserFileService;
import pard.server.com.longkathon.common.exception.ExpiredRefreshTokenException;
import pard.server.com.longkathon.common.exception.InvalidJwtException;
import pard.server.com.longkathon.common.exception.InvalidRefreshTokenException;
import pard.server.com.longkathon.common.exception.UserNotFoundException;
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
    private final UserFileService userFileService;

    // RefreshToken을 전달받아 새로운 AccessToken 생성
    public CreateAccessTokenResponse createNewAccessToken(String refreshToken) {
        // 1. RefreshToken 검증 (DB에 존재하는지, 만료되지 않았는지)
        RefreshToken storedToken = refreshTokenRepository.findByRefreshToken(refreshToken)
                 .orElseThrow(() -> new InvalidRefreshTokenException("RefreshToken이 DB에 존재하지 않습니다"));

        // 2. RefreshToken이 만료되었는지 확인
        if (storedToken.isExpired()) {
            throw new ExpiredRefreshTokenException("RefreshToken이 만료되었습니다");
        }

        // 3. userId로 User 조회
        User user = userRepository.findById(storedToken.getUserId())
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다"));

        // 4. JWT RefreshToken 검증 (유효한 토큰인지)
        if (!tokenProvider.validToken(refreshToken)) {
            throw new InvalidJwtException("JWT 토큰이 유효하지 않습니다");
        }

        // 5. AccessToken 생성
        String accessToken = tokenProvider.generateToken(user, Duration.ofMinutes(15));

        // 6. 프로필 사진 URL 조회 (캐시 적용됨)
        String imageUrl = userFileService.getURL(user.getUserId());

        // 7. 확장된 응답 반환
        return new CreateAccessTokenResponse(
                accessToken,
                user.isProfileCompleted(),
                user.getName(),    // User 엔티티에서 직접 (추가 쿼리 없음)
                imageUrl           // UserFile 조회 (캐시 히트 시 쿼리 없음)
        );
    }
}