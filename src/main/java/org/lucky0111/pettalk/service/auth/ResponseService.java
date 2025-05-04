package org.lucky0111.pettalk.service.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * API 응답 표준화를 위한 서비스
 */
@Slf4j
@Service
public class ResponseService {

    /**
     * 성공 응답을 생성합니다.
     *
     * @param data    응답 데이터
     * @param message 성공 메시지
     * @return 표준화된 성공 응답
     */
    public ResponseEntity<?> createSuccessResponse(Object data, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("message", message);
        response.put("timestamp", Instant.now().toString());

        return ResponseEntity.ok(response);
    }

    /**
     * 성공 응답을 생성합니다 (상태 코드 지정).
     *
     * @param data    응답 데이터
     * @param message 성공 메시지
     * @param status  HTTP 상태 코드
     * @return 표준화된 성공 응답
     */
    public ResponseEntity<?> createSuccessResponse(Object data, String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("message", message);
        response.put("timestamp", Instant.now().toString());

        return ResponseEntity.status(status).body(response);
    }

    /**
     * 오류 응답을 생성합니다.
     *
     * @param code    오류 코드
     * @param message 오류 메시지
     * @return 표준화된 오류 응답
     */
    public ResponseEntity<?> createErrorResponse(String code, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);

        Map<String, String> error = new HashMap<>();
        error.put("code", code);
        error.put("message", message);

        response.put("error", error);
        response.put("timestamp", Instant.now().toString());

        // 오류 코드에 따른 HTTP 상태 코드 결정
        HttpStatus httpStatus = determineHttpStatus(code);
        log.debug("오류 응답 생성: code={}, message={}, httpStatus={}", code, message, httpStatus);

        return ResponseEntity.status(httpStatus).body(response);
    }

    /**
     * 오류 응답을 생성합니다 (상태 코드 지정).
     *
     * @param code       오류 코드
     * @param message    오류 메시지
     * @param httpStatus HTTP 상태 코드
     * @return 표준화된 오류 응답
     */
    public ResponseEntity<?> createErrorResponse(String code, String message, HttpStatus httpStatus) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);

        Map<String, String> error = new HashMap<>();
        error.put("code", code);
        error.put("message", message);

        response.put("error", error);
        response.put("timestamp", Instant.now().toString());

        log.debug("오류 응답 생성: code={}, message={}, httpStatus={}", code, message, httpStatus);

        return ResponseEntity.status(httpStatus).body(response);
    }

    /**
     * 오류 코드에 따른 HTTP 상태 코드를 결정합니다.
     *
     * @param code 오류 코드
     * @return HTTP 상태 코드
     */
    private HttpStatus determineHttpStatus(String code) {
        if (code == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return switch (code.toUpperCase()) {
            case "BAD_REQUEST", "INVALID_REQUEST", "VALIDATION_ERROR", "INVALID_DATA" -> HttpStatus.BAD_REQUEST;
            case "UNAUTHORIZED", "INVALID_TOKEN", "TOKEN_EXPIRED" -> HttpStatus.UNAUTHORIZED;
            case "FORBIDDEN" -> HttpStatus.FORBIDDEN;
            case "NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "CONFLICT", "DUPLICATE_DATA", "DUPLICATE_NICKNAME" -> HttpStatus.CONFLICT;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}