package org.lucky0111.pettalk.util.error;

import org.lucky0111.pettalk.domain.common.ErrorCode;
import org.lucky0111.pettalk.exception.CustomException;
import org.springframework.http.HttpStatus;

public class ExceptionUtils {
    public static CustomException of(ErrorCode errorCode) {
        return new CustomException(errorCode);
    }

    public static CustomException of(ErrorCode errorCode, String additionalInfo) {
        return new CustomException(errorCode, additionalInfo);
    }

    // 자주 사용되는 예외를 위한 정적 메서드
    public static CustomException entityNotFound(String entityName, String id) {
        return new CustomException(
                ErrorCode.APPLY_NOT_FOUND.getMessage() + " [" + entityName + ": " + id + "]",
                HttpStatus.NOT_FOUND
        );
    }

    public static CustomException permissionDenied(String resource) {
        return new CustomException(
                "해당 " + resource + "에 대한 권한이 없습니다.",
                HttpStatus.FORBIDDEN
        );
    }
}
