package pard.server.com.longkathon.common.exception;

/**
 * DB에 RefreshToken이 존재하지 않을 때 발생하는 예외
 */
public class InvalidRefreshTokenException extends TokenException {
    public InvalidRefreshTokenException(String message) {
        super(message, "INVALID_REFRESH_TOKEN", true);
    }
}
