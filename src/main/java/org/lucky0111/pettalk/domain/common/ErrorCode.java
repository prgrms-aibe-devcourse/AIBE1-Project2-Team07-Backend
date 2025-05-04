package org.lucky0111.pettalk.domain.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.lucky0111.pettalk.exception.CustomException;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 사용자 관련 에러
    USER_NOT_FOUND("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    UNAUTHORIZED("인증되지 않은 사용자입니다.", HttpStatus.UNAUTHORIZED),

    // 트레이너 관련 에러
    TRAINER_NOT_FOUND("트레이너를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // 신청서 관련 에러
    APPLY_ALREADY_EXISTS("현재 신청 중 입니다.", HttpStatus.CONFLICT),
    APPLY_NOT_FOUND("신청 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // 권한 관련 에러
    PERMISSION_DENIED("해당 작업에 대한 권한이 없습니다.", HttpStatus.FORBIDDEN),

    // 토큰 관련 에러
    TOKEN_NOT_FOUND("인증 토큰을 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED);

    private final String message;
    private final HttpStatus httpStatus;
}
