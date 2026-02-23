package pard.server.com.longkathon.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 일관된 에러 응답 형식을 제공하는 DTO
 */
@Getter
@AllArgsConstructor
public class ApiErrorResponse {
    private boolean success;           // 항상 false
    private String errorCode;          // "REFRESH_TOKEN_EXPIRED" 등
    private String message;            // 사용자 친화적 메시지
    private LocalDateTime timestamp;   // 에러 발생 시간
    private boolean requiresLogout;    // 프론트 로그아웃 트리거 플래그
}
