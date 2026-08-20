package com.project.monu.global.exception;

import java.time.Instant;
import java.util.Collections;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalHandlerException {

  // User Email 중복 확인
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusinessException(
      BusinessException exception
  ) {
    ErrorCode errorCode = exception.getErrorCode();

    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now(),
        errorCode.getCode(),
        errorCode.getMessage(),
        Collections.emptyMap(),
        exception.getClass().getSimpleName(),
        errorCode.getStatus().value()
    );

    return ResponseEntity
        .status(errorCode.getStatus())
        .body(errorResponse);
  }
}