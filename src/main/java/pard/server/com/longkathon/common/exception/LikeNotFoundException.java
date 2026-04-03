package pard.server.com.longkathon.common.exception;

/**
 * 좋아요 기록을 찾을 수 없을 때 발생하는 예외
 */
public class LikeNotFoundException extends RuntimeException {
    public LikeNotFoundException(String message) {
        super(message);
    }
}
