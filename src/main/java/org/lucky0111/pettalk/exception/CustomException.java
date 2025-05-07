package org.lucky0111.pettalk.exception;

import lombok.Getter;
import org.lucky0111.pettalk.domain.common.ErrorCode;
import org.springframework.http.HttpStatus;

import java.io.IOException;

@Getter
public class CustomException extends RuntimeException {
//    private final String message;
    private final HttpStatus httpStatus;

    public CustomException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.httpStatus = errorCode.getHttpStatus();
    }
}
