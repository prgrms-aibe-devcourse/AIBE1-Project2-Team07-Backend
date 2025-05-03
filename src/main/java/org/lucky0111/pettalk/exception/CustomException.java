package org.lucky0111.pettalk.exception;

import lombok.Getter;
import org.lucky0111.pettalk.domain.common.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {
    private final String message;
    private final HttpStatus httpStatus;

    public CustomException(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public CustomException(ErrorCode errorCode) {
        this.message = errorCode.getMessage();
        this.httpStatus = errorCode.getHttpStatus();
    }

    public CustomException(ErrorCode errorCode, String additionalInfo) {
        this.message = errorCode.getMessage() + ": " + additionalInfo;
        this.httpStatus = errorCode.getHttpStatus();
    }
}
