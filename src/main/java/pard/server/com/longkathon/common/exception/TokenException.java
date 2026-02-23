package pard.server.com.longkathon.common.exception;

import lombok.Getter;

/**
 * 토큰 관련 예외의 추상 부모 클래스
 */
@Getter
public abstract class TokenException extends RuntimeException {
    private final String errorCode;
    private final boolean requiresLogout;

    protected TokenException(String message, String errorCode, boolean requiresLogout) {
        super(message);
        this.errorCode = errorCode;
        this.requiresLogout = requiresLogout;
    }
}
