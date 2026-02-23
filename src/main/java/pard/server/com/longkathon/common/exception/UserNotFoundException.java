package pard.server.com.longkathon.common.exception;

/**
 * 사용자를 조회할 수 없을 때 발생하는 예외
 */
public class UserNotFoundException extends TokenException {
    public UserNotFoundException(String message) {
        super(message, "USER_NOT_FOUND", true);
    }
}
