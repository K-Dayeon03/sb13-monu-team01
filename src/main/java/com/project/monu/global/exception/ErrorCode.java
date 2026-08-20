package com.project.monu.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

  /*
   * 사용자 관리 - 회원가입
   * 이미 사용 중인 이메일로 회원가입을 시도한 경우 사용합니다.
   */
  EMAIL_ALREADY_EXISTS(
      HttpStatus.CONFLICT,
      "EMAIL_DUPLICATION",
      "이미 존재하는 이메일입니다."
  ),

  /*
   * 사용자 관리 - 로그인
   * 존재하지 않는 이메일이거나 비밀번호가 일치하지 않는 경우 사용합니다.
   */
  LOGIN_FAILED(
      HttpStatus.UNAUTHORIZED,
      "LOGIN_FAILED",
      "이메일 또는 비밀번호가 올바르지 않습니다."
  ),

  NOTIFICATION_NOT_FOUND(
          HttpStatus.NOT_FOUND,
          "NOTIFICATION_NOT_FOUND",
          "알림을 찾을 수 없습니다."
  ),

  NOTIFICATION_ACCESS_DENIED(
          HttpStatus.FORBIDDEN,
          "NOTIFICATION_ACCESS_DENIED",
          "해당 알림에 접근할 수 없습니다."
  ),

  USER_NOT_FOUND(
          HttpStatus.NOT_FOUND,
          "USER_NOT_FOUND",
          "사용자를 찾을 수 없습니다."
  );

  private final HttpStatus status;
  private final String code;
  private final String message;

  ErrorCode(
      HttpStatus status,
      String code,
      String message
  ) {
    this.status = status;
    this.code = code;
    this.message = message;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}