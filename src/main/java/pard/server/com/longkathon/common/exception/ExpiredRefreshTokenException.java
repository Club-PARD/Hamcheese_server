package pard.server.com.longkathon.common.exception;

/**
 * RefreshToken이 만료되었을 때 발생하는 예외
 */
public class ExpiredRefreshTokenException extends TokenException {
    public ExpiredRefreshTokenException(String message) {
        super(message, "REFRESH_TOKEN_EXPIRED", true);
    }
}
