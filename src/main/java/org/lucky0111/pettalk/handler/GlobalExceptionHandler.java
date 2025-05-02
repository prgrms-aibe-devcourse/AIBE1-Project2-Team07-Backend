package org.lucky0111.pettalk.handler;

import org.lucky0111.pettalk.domain.dto.ErrorResponseDTO;
import org.lucky0111.pettalk.exception.CustomException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class) // CustomException은 사용자 정의 예외 클래스라고 가정
    public ResponseEntity<ErrorResponseDTO> handleCustomException(CustomException e, WebRequest request) {

        // ErrorResponseDTO.builder()를 사용하여 Record 객체 생성 (Lombok 빌더 기능 활용)
        ErrorResponseDTO errorResponseDTO = ErrorResponseDTO.builder()
                .status(e.getHttpStatus().value()) // CustomException에서 HttpStatus 값을 가져옴
                .error(e.getHttpStatus().getReasonPhrase()) // HttpStatus의 이유 문구 가져옴
                .message(e.getMessage()) // 예외 메시지 가져옴
                .path(request.getDescription(false).substring(4)) // 요청 경로 가져와서 불필요한 부분 제거
                .build(); // 빌더를 통해 ErrorResponseDTO Record 객체 완성

        // 여기서 Record의 status 컴포넌트 값을 가져올 때 .status() 메소드 사용
        return ResponseEntity.status(errorResponseDTO.status()).body(errorResponseDTO); // <--- getStatus() 대신 .status() 사용
    }

    // 필요에 따라 다른 예외 핸들러 메소드 추가
    // @ExceptionHandler(AnotherException.class)
    // public ResponseEntity<ErrorResponseDTO> handleAnotherException(AnotherException e, WebRequest request) { ... }
}