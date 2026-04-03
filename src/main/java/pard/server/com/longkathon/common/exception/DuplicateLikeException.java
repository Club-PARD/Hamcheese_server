package pard.server.com.longkathon.common.exception;

/**
 * 이미 좋아요를 누른 포트폴리오에 중복으로 좋아요를 시도할 때 발생하는 예외
 */
public class DuplicateLikeException extends RuntimeException {
    public DuplicateLikeException(String message) {
        super(message);
    }
}
