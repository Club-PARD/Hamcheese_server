package pard.server.com.longkathon.common.exception;

/**
 * JWT 토큰 검증에 실패했을 때 발생하는 예외
 */
public class InvalidJwtException extends TokenException {
    public InvalidJwtException(String message) {
        super(message, "INVALID_JWT_TOKEN", true);
    }
}
