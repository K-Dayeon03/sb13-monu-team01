package com.project.monu.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

  /*
   * 사용자 관리 - 회원가입
   * 이미 사용 중인 이메일로 회원가입을 시도한 경우 사용합니다.
   */
  EMAIL_ALREADY_EXISTS(
      HttpStatus.CONFLICT,
      "이미 존재하는 이메일입니다."
  );

  private final HttpStatus status;
  private final String message;

  ErrorCode(HttpStatus status, String message) {
    this.status = status;
    this.message = message;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }




}