package com.project.monu.domain.users.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserCreateRequestTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  @Test
  void 잘못된_이메일_형식은_검증에_실패한다() {
    UserCreateRequest request = new UserCreateRequest(
        "잘못된이메일",
        "테스트",
        "password123!"
    );

    var violations = validator.validate(request);

    assertThat(violations).isNotEmpty();
  }

  @Test
  void 닉네임이_없으면_검증에_실패한다() {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    UserCreateRequest request = new UserCreateRequest(
        "test@test.com",
        "",
        "password123!"
    );

    Set<ConstraintViolation<UserCreateRequest>> violations =
        validator.validate(request);

    assertThat(violations).isNotEmpty();
  }
}