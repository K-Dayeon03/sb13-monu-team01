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
   * 관심사 관리 - 관심사 등록
   * 기존 관심사와 80% 이상 유사한 이름으로 등록을 시도한 경우 사용합니다.
   */
  INTEREST_ALREADY_EXISTS(
          HttpStatus.CONFLICT,
          "INTEREST_DUPLICATION",
          "이미 유사한 관심사가 존재합니다."
  ),

  INTEREST_NOT_FOUND(
          HttpStatus.NOT_FOUND,
      "INTEREST_NOT_FOUND",
              "관심사를 찾을 수 없습니다."
  ),

  SUBSCRIPTION_ALREADY_EXISTS(
          HttpStatus.CONFLICT,
      "SUBSCRIPTION_DUPLICATION",
              "이미 구독 중인 관심사입니다."
  ),

  SUBSCRIPTION_NOT_FOUND(
          HttpStatus.NOT_FOUND,
      "SUBSCRIPTION_NOT_FOUND",
              "구독 내역을 찾을 수 없습니다."
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