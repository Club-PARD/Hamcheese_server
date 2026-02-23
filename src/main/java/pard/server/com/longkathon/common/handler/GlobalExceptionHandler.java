package pard.server.com.longkathon.common.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pard.server.com.longkathon.common.dto.ApiErrorResponse;
import pard.server.com.longkathon.common.exception.TokenException;

import java.time.LocalDateTime;

/**
 * 전역 예외 처리기
 * 모든 컨트롤러에서 발생하는 예외를 일관되게 처리합니다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * TokenException 계열 예외 처리 (401 Unauthorized 반환)
     * - InvalidRefreshTokenException: RefreshToken이 DB에 없음
     * - ExpiredRefreshTokenException: RefreshToken 만료됨
     * - InvalidJwtException: JWT 검증 실패
     * - UserNotFoundException: 사용자 조회 실패
     */
    @ExceptionHandler(TokenException.class)
    public ResponseEntity<ApiErrorResponse> handleTokenException(TokenException ex) {
        log.warn("TokenException: errorCode={}, message={}", ex.getErrorCode(), ex.getMessage());

        ApiErrorResponse errorResponse = new ApiErrorResponse(
            false,
            ex.getErrorCode(),
            ex.getMessage(),
            LocalDateTime.now(),
            ex.isRequiresLogout()
        );

        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(errorResponse);
    }

    /**
     * IllegalArgumentException 처리 (500 Internal Server Error)
     * 기존 코드와의 하위 호환성을 위해 유지합니다.
     * 다른 비즈니스 로직에서 발생하는 예외는 계속 500으로 처리됩니다.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("IllegalArgumentException: {}", ex.getMessage());

        ApiErrorResponse errorResponse = new ApiErrorResponse(
            false,
            "BAD_REQUEST",
            ex.getMessage(),
            LocalDateTime.now(),
            false
        );

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorResponse);
    }

    /**
     * 예상하지 못한 모든 예외 처리 (500 Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneral(Exception ex) {
        log.error("Unexpected exception", ex);

        ApiErrorResponse errorResponse = new ApiErrorResponse(
            false,
            "INTERNAL_SERVER_ERROR",
            "서버 오류가 발생했습니다",
            LocalDateTime.now(),
            false
        );

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorResponse);
    }
}
